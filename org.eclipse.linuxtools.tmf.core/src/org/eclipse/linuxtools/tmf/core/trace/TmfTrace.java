/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson, École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *   Patrick Tasse - Updated for removal of context clone
 *   Geneviève Bastien  - Added timestamp transforms, its saving to file and
 *                        timestamp creation functions
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModule;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisManager;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.synchronization.ITmfTimestampTransform;
import org.eclipse.linuxtools.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfTraceIndexer;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpointIndexer;
import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;

/**
 * Abstract implementation of ITmfTrace.
 * <p>
 * Since the concept of 'location' is trace specific, the concrete classes have
 * to provide the related methods, namely:
 * <ul>
 * <li> public ITmfLocation<?> getCurrentLocation()
 * <li> public double getLocationRatio(ITmfLocation<?> location)
 * <li> public ITmfContext seekEvent(ITmfLocation<?> location)
 * <li> public ITmfContext seekEvent(double ratio)
 * <li> public IStatus validate(IProject project, String path)
 * </ul>
 * A concrete trace must provide its corresponding parser. A common way to
 * accomplish this is by making the concrete class extend TmfTrace and
 * implement ITmfEventParser.
 * <p>
 * The concrete class can either specify its own indexer or use the provided
 * TmfCheckpointIndexer (default). In this case, the trace cache size will be
 * used as checkpoint interval.
 *
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfEvent
 * @see ITmfTraceIndexer
 * @see ITmfEventParser
 */
public abstract class TmfTrace extends TmfEventProvider implements ITmfTrace, ITmfTraceCompleteness {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The resource used for persistent properties for this trace
    private IResource fResource;

    // The trace path
    private String fPath;

    // The trace cache page size
    private int fCacheSize = ITmfTrace.DEFAULT_TRACE_CACHE_SIZE;

    // The number of events collected (so far)
    private volatile long fNbEvents = 0;

    // The time span of the event stream
    private ITmfTimestamp fStartTime = TmfTimestamp.BIG_BANG;
    private ITmfTimestamp fEndTime = TmfTimestamp.BIG_BANG;

    // The trace streaming interval (0 = no streaming)
    private long fStreamingInterval = 0;

    // The trace indexer
    private ITmfTraceIndexer fIndexer;

    // The trace parser
    private ITmfEventParser fParser;

    private ITmfTimestampTransform fTsTransform;

    private final Map<String, IAnalysisModule> fAnalysisModules =
            Collections.synchronizedMap(new LinkedHashMap<String, IAnalysisModule>());

    private static final String SYNCHRONIZATION_FORMULA_FILE = "sync_formula"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * The default, parameterless, constructor
     */
    public TmfTrace() {
        super();
        fIndexer = createIndexer(DEFAULT_BLOCK_SIZE);
    }

    /**
     * Full constructor.
     *
     * @param resource
     *            The resource associated to the trace
     * @param type
     *            The type of events that will be read from this trace
     * @param path
     *            The path to the trace on the filesystem
     * @param cacheSize
     *            The trace cache size. Pass '-1' to use the default specified
     *            in {@link ITmfTrace#DEFAULT_TRACE_CACHE_SIZE}
     * @param interval
     *            The trace streaming interval. You can use '0' for post-mortem
     *            traces.
     * @param parser
     *            The trace event parser. Use 'null' if (and only if) the trace
     *            object itself is also the ITmfEventParser to be used.
     * @throws TmfTraceException
     *             If something failed during the opening
     */
    protected TmfTrace(final IResource resource,
            final Class<? extends ITmfEvent> type,
            final String path,
            final int cacheSize,
            final long interval,
            final ITmfEventParser parser)
                    throws TmfTraceException {
        super();
        fCacheSize = (cacheSize > 0) ? cacheSize : ITmfTrace.DEFAULT_TRACE_CACHE_SIZE;
        fStreamingInterval = interval;
        fParser = parser;
        initialize(resource, path, type);
    }

    /**
     * Copy constructor
     *
     * @param trace the original trace
     * @throws TmfTraceException Should not happen usually
     */
    public TmfTrace(final TmfTrace trace) throws TmfTraceException {
        super();
        if (trace == null) {
            throw new IllegalArgumentException();
        }
        fCacheSize = trace.getCacheSize();
        fStreamingInterval = trace.getStreamingInterval();
        fParser = trace.fParser;
        initialize(trace.getResource(), trace.getPath(), trace.getEventType());
    }

    /**
     * Creates the indexer instance. Classes extending this class can override
     * this to provide a different indexer implementation.
     *
     * @param interval the checkpoints interval
     *
     * @return the indexer
     * @since 3.0
     */
    protected ITmfTraceIndexer createIndexer(int interval) {
        return new TmfCheckpointIndexer(this, interval);
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Initializers
    // ------------------------------------------------------------------------

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> type, String name) throws TmfTraceException {
        setName(name);
        initTrace(resource, path, type);
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> type) throws TmfTraceException {
        initialize(resource, path, type);
    }

    /**
     * Initialize the trace common attributes and the base component.
     *
     * @param resource the Eclipse resource (trace)
     * @param path the trace path
     * @param type the trace event type
     *
     * @throws TmfTraceException If something failed during the initialization
     */
    protected void initialize(final IResource resource,
            final String path,
            final Class<? extends ITmfEvent> type)
                    throws TmfTraceException {
        if (path == null) {
            throw new TmfTraceException("Invalid trace path"); //$NON-NLS-1$
        }
        fPath = path;
        fResource = resource;
        String traceName = getName();
        if (traceName == null || traceName.isEmpty()) {
            traceName = (resource != null) ? resource.getName() : new Path(path).lastSegment();
        }
        if (fParser == null) {
            if (this instanceof ITmfEventParser) {
                fParser = (ITmfEventParser) this;
            } else {
                throw new TmfTraceException("Invalid trace parser"); //$NON-NLS-1$
            }
        }
        super.init(traceName, type);
        // register as VIP after super.init() because TmfComponent registers to signal manager there
        TmfSignalManager.registerVIP(this);
        if (fIndexer != null) {
            fIndexer.dispose();
        }
        fIndexer = createIndexer(fCacheSize);
    }

    /**
     * Indicates if the path points to an existing file/directory
     *
     * @param path the path to test
     * @return true if the file/directory exists
     */
    protected boolean fileExists(final String path) {
        final File file = new File(path);
        return file.exists();
    }

    /**
     * @since 2.0
     */
    @Override
    public void indexTrace(boolean waitForCompletion) {
        getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, waitForCompletion);
    }

    /**
     * Instantiate the applicable analysis modules and executes the analysis
     * modules that are meant to be automatically executed
     *
     * @return An IStatus indicating whether the analysis could be run
     *         successfully or not
     * @since 3.0
     */
    protected IStatus executeAnalysis() {
        MultiStatus status = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, null, null);
        Map<String, IAnalysisModuleHelper> modules = TmfAnalysisManager.getAnalysisModules(this.getClass());
        for (IAnalysisModuleHelper helper : modules.values()) {
            try {
                IAnalysisModule module = helper.newModule(this);
                fAnalysisModules.put(module.getId(), module);
                if (module.isAutomatic()) {
                    status.add(module.schedule());
                }
            } catch (TmfAnalysisException e) {
                status.add(new Status(IStatus.WARNING, Activator.PLUGIN_ID, e.getMessage()));
            }
        }
        return status;
    }

    /**
     * @since 3.0
     */
    @Override
    public IAnalysisModule getAnalysisModule(String analysisId) {
        return fAnalysisModules.get(analysisId);
    }


    /**
     * @since 3.0
     */
    @Override
    public Iterable<IAnalysisModule> getAnalysisModules() {
        synchronized (fAnalysisModules) {
            Set<IAnalysisModule> modules = new HashSet<>(fAnalysisModules.values());
            return modules;
        }
    }

    /**
     * @since 3.0
     */
    @Override
    public <T extends IAnalysisModule> T getAnalysisModuleOfClass(Class<T> moduleClass, String id) {
        Iterable<T> modules = getAnalysisModulesOfClass(moduleClass);
        for (T module : modules) {
            if (id.equals(module.getId())) {
                return module;
            }
        }
        return null;
    }

    /**
     * @since 3.0
     */
    @Override
    public <T> Iterable<T> getAnalysisModulesOfClass(Class<T> moduleClass) {
        Set<T> modules = new HashSet<>();
        synchronized (fAnalysisModules) {
            for (Entry<String, IAnalysisModule> entry : fAnalysisModules.entrySet()) {
                if (moduleClass.isAssignableFrom(entry.getValue().getClass())) {
                    modules.add(moduleClass.cast(entry.getValue()));
                }
            }
        }
        return modules;
    }

    /**
     * Clears the trace
     */
    @Override
    public synchronized void dispose() {
        /* Clean up the index if applicable */
        if (getIndexer() != null) {
            getIndexer().dispose();
        }

        /* Clean up the analysis modules */
        synchronized (fAnalysisModules) {
            for (IAnalysisModule module : fAnalysisModules.values()) {
                module.dispose();
            }
            fAnalysisModules.clear();
        }

        super.dispose();
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Basic getters
    // ------------------------------------------------------------------------

    @Override
    public Class<? extends ITmfEvent> getEventType() {
        return super.getType();
    }

    @Override
    public IResource getResource() {
        return fResource;
    }

    @Override
    public String getPath() {
        return fPath;
    }

    @Override
    public int getCacheSize() {
        return fCacheSize;
    }

    @Override
    public long getStreamingInterval() {
        return fStreamingInterval;
    }

    /**
     * @return the trace indexer
     * @since 3.0
     */
    protected ITmfTraceIndexer getIndexer() {
        return fIndexer;
    }

    /**
     * @return the trace parser
     */
    protected ITmfEventParser getParser() {
        return fParser;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Trace characteristics getters
    // ------------------------------------------------------------------------

    @Override
    public long getNbEvents() {
        return fNbEvents;
    }

    /**
     * @since 2.0
     */
    @Override
    public TmfTimeRange getTimeRange() {
        return new TmfTimeRange(fStartTime, fEndTime);
    }

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getStartTime() {
        return fStartTime;
    }

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return fEndTime;
    }

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getInitialRangeOffset() {
        final long DEFAULT_INITIAL_OFFSET_VALUE = (1L * 100 * 1000 * 1000); // .1sec
        return new TmfTimestamp(DEFAULT_INITIAL_OFFSET_VALUE, ITmfTimestamp.NANOSECOND_SCALE);
    }

    /**
     * @since 3.0
     */
    @Override
    public String getHostId() {
        return this.getName();
    }

    // ------------------------------------------------------------------------
    // Convenience setters
    // ------------------------------------------------------------------------

    /**
     * Set the trace cache size. Must be done at initialization time.
     *
     * @param cacheSize The trace cache size
     */
    protected void setCacheSize(final int cacheSize) {
        fCacheSize = cacheSize;
    }

    /**
     * Set the trace known number of events. This can be quite dynamic
     * during indexing or for live traces.
     *
     * @param nbEvents The number of events
     */
    protected synchronized void setNbEvents(final long nbEvents) {
        fNbEvents = (nbEvents > 0) ? nbEvents : 0;
    }

    /**
     * Update the trace events time range
     *
     * @param range the new time range
     * @since 2.0
     */
    protected void setTimeRange(final TmfTimeRange range) {
        fStartTime = range.getStartTime();
        fEndTime = range.getEndTime();
    }

    /**
     * Update the trace chronologically first event timestamp
     *
     * @param startTime the new first event timestamp
     * @since 2.0
     */
    protected void setStartTime(final ITmfTimestamp startTime) {
        fStartTime = startTime;
    }

    /**
     * Update the trace chronologically last event timestamp
     *
     * @param endTime the new last event timestamp
     * @since 2.0
     */
    protected void setEndTime(final ITmfTimestamp endTime) {
        fEndTime = endTime;
    }

    /**
     * Set the polling interval for live traces (default = 0 = no streaming).
     *
     * @param interval the new trace streaming interval
     */
    protected void setStreamingInterval(final long interval) {
        fStreamingInterval = (interval > 0) ? interval : 0;
    }

    /**
     * Set the trace parser. Must be done at initialization time.
     *
     * @param parser the new trace parser
     */
    protected void setParser(final ITmfEventParser parser) {
        fParser = parser;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - SeekEvent operations (returning a trace context)
    // ------------------------------------------------------------------------

    @Override
    public synchronized ITmfContext seekEvent(final long rank) {

        // A rank <= 0 indicates to seek the first event
        if (rank <= 0) {
            ITmfContext context = seekEvent((ITmfLocation) null);
            context.setRank(0);
            return context;
        }

        // Position the trace at the checkpoint
        final ITmfContext context = fIndexer.seekIndex(rank);

        // And locate the requested event context
        long pos = context.getRank();
        if (pos < rank) {
            ITmfEvent event = getNext(context);
            while ((event != null) && (++pos < rank)) {
                event = getNext(context);
            }
        }
        return context;
    }

    /**
     * @since 2.0
     */
    @Override
    public synchronized ITmfContext seekEvent(final ITmfTimestamp timestamp) {

        // A null timestamp indicates to seek the first event
        if (timestamp == null) {
            ITmfContext context = seekEvent((ITmfLocation) null);
            context.setRank(0);
            return context;
        }

        // Position the trace at the checkpoint
        ITmfContext context = fIndexer.seekIndex(timestamp);

        // And locate the requested event context
        ITmfLocation previousLocation = context.getLocation();
        long previousRank = context.getRank();
        ITmfEvent event = getNext(context);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
            previousLocation = context.getLocation();
            previousRank = context.getRank();
            event = getNext(context);
        }
        if (event == null) {
            context.setLocation(null);
            context.setRank(ITmfContext.UNKNOWN_RANK);
        } else {
            context.dispose();
            context = seekEvent(previousLocation);
            context.setRank(previousRank);
        }
        return context;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Read operations (returning an actual event)
    // ------------------------------------------------------------------------

    @Override
    public synchronized ITmfEvent getNext(final ITmfContext context) {
        // parseEvent() does not update the context
        final ITmfEvent event = fParser.parseEvent(context);
        if (event != null) {
            updateAttributes(context, event.getTimestamp());
            context.setLocation(getCurrentLocation());
            context.increaseRank();
            processEvent(event);
        }
        return event;
    }

    /**
     * Hook for special event processing by the concrete class
     * (called by TmfTrace.getEvent())
     *
     * @param event the event
     */
    protected void processEvent(final ITmfEvent event) {
        // Do nothing
    }

    /**
     * Update the trace attributes
     *
     * @param context the current trace context
     * @param timestamp the corresponding timestamp
     * @since 2.0
     */
    protected synchronized void updateAttributes(final ITmfContext context, final ITmfTimestamp timestamp) {
        if (fStartTime.equals(TmfTimestamp.BIG_BANG) || (fStartTime.compareTo(timestamp, false) > 0)) {
            fStartTime = timestamp;
        }
        if (fEndTime.equals(TmfTimestamp.BIG_CRUNCH) || (fEndTime.compareTo(timestamp, false) < 0)) {
            fEndTime = timestamp;
        }
        if (context.hasValidRank()) {
            long rank = context.getRank();
            if (fNbEvents <= rank) {
                fNbEvents = rank + 1;
            }
            if (fIndexer != null) {
                fIndexer.updateIndex(context, timestamp);
            }
        }
    }

    // ------------------------------------------------------------------------
    // TmfDataProvider
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public synchronized ITmfContext armRequest(final ITmfEventRequest request) {
        if (executorIsShutdown()) {
            return null;
        }
        if (!TmfTimestamp.BIG_BANG.equals(request.getRange().getStartTime())
                && (request.getIndex() == 0)) {
            final ITmfContext context = seekEvent(request.getRange().getStartTime());
            request.setStartIndex((int) context.getRank());
            return context;

        }
        return seekEvent(request.getIndex());
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the Trace Opened signal
     *
     * @param signal
     *            The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        boolean signalIsForUs = false;
        for (ITmfTrace trace : TmfTraceManager.getTraceSet(signal.getTrace())) {
            if (trace == this) {
                signalIsForUs = true;
                break;
            }
        }

        if (!signalIsForUs) {
            return;
        }

        /*
         * The signal is either for this trace, or for an experiment containing
         * this trace.
         */
        IStatus status = executeAnalysis();
        if (!status.isOK()) {
            Activator.log(status);
        }

        TmfTraceManager.refreshSupplementaryFiles(this);

        if (signal.getTrace() == this) {
            /* Additionally, the signal is directly for this trace. */
            if (getNbEvents() == 0) {
                return;
            }

            /* For a streaming trace, the range updated signal should be sent
             * by the subclass when a new safe time is determined.
             */
            if (getStreamingInterval() > 0) {
                return;
            }

            if (isComplete()) {
                final TmfTimeRange timeRange = new TmfTimeRange(getStartTime(), TmfTimestamp.BIG_CRUNCH);
                final TmfTraceRangeUpdatedSignal rangeUpdatedsignal = new TmfTraceRangeUpdatedSignal(this, this, timeRange);

                // Broadcast in separate thread to prevent deadlock
                broadcastAsync(rangeUpdatedsignal);
            }
            return;
        }
    }

    /**
     * Signal handler for the TmfTraceRangeUpdatedSignal signal
     *
     * @param signal The incoming signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void traceRangeUpdated(final TmfTraceRangeUpdatedSignal signal) {
        if (signal.getTrace() == this) {
            getIndexer().buildIndex(getNbEvents(), signal.getRange(), false);
        }
    }

    /**
     * Signal handler for the TmfTraceUpdatedSignal signal
     *
     * @param signal The incoming signal
     * @since 3.0
     */
    @TmfSignalHandler
    public void traceUpdated(final TmfTraceUpdatedSignal signal) {
        if (signal.getSource() == getIndexer()) {
            fNbEvents = signal.getNbEvents();
            fStartTime = signal.getRange().getStartTime();
            fEndTime = signal.getRange().getEndTime();
        }
    }

    /**
     * Returns the file resource used to store synchronization formula. The file
     * may not exist.
     *
     * @return the synchronization file
     */
    private File getSyncFormulaFile() {
        File file = null;
        if (fResource instanceof IFolder) {
            try {
                String supplDirectory;

                supplDirectory = fResource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);

                file = new File(supplDirectory + File.separator + SYNCHRONIZATION_FORMULA_FILE);

            } catch (CoreException e) {

            }
        }
        return file;
    }

    // ------------------------------------------------------------------------
    // Timestamp transformation functions
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public ITmfTimestampTransform getTimestampTransform() {
        if (fTsTransform == null) {
            /* Check if a formula is stored somewhere in the resources */
            File sync_file = getSyncFormulaFile();
            if (sync_file != null && sync_file.exists()) {

                try (FileInputStream fis = new FileInputStream(sync_file);
                        ObjectInputStream ois = new ObjectInputStream(fis);) {

                    fTsTransform = (ITmfTimestampTransform) ois.readObject();

                } catch (ClassNotFoundException | IOException e) {
                    fTsTransform = TimestampTransformFactory.getDefaultTransform();
                }
            } else {
                fTsTransform = TimestampTransformFactory.getDefaultTransform();
            }
        }
        return fTsTransform;
    }

    /**
     * @since 3.0
     */
    @Override
    public void setTimestampTransform(final ITmfTimestampTransform tt) {
        fTsTransform = tt;

        /* Save the timestamp transform to a file */
        File sync_file = getSyncFormulaFile();
        if (sync_file != null) {
            if (sync_file.exists()) {
                sync_file.delete();
            }

            /* Save the header of the file */
            try (FileOutputStream fos = new FileOutputStream(sync_file, false);
                    ObjectOutputStream oos = new ObjectOutputStream(fos);) {

                oos.writeObject(fTsTransform);
            } catch (IOException e1) {
                Activator.logError("Error writing timestamp transform for trace", e1); //$NON-NLS-1$
            }
        }
    }

    /**
     * @since 3.0
     */
    @Override
    public ITmfTimestamp createTimestamp(long ts) {
        return new TmfTimestamp(getTimestampTransform().transform(ts));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("nls")
    public synchronized String toString() {
        return "TmfTrace [fPath=" + fPath + ", fCacheSize=" + fCacheSize
                + ", fNbEvents=" + fNbEvents + ", fStartTime=" + fStartTime
                + ", fEndTime=" + fEndTime + ", fStreamingInterval=" + fStreamingInterval + "]";
    }

    /**
     * @since 3.1
     */
    @Override
    public boolean isComplete() {
        /*
         * Be default, all traces are "complete" which means no more data will
         * be added later
         */
        return true;
    }

    /**
     * @since 3.1
     */
    @Override
    public void setComplete(boolean isComplete) {
        /*
         * This should be overridden by trace classes that can support live
         * reading (traces in an incomplete state)
         */
    }
}
