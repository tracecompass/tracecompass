/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.experiment;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpointIndexer;
import org.eclipse.linuxtools.tmf.core.trace.TmfTrace;

/**
 * TmfExperiment presents a time-ordered, unified view of a set of TmfTraces
 * that are part of a tracing experiment.
 */
public class TmfExperiment<T extends ITmfEvent> extends TmfTrace<T> implements ITmfEventParser<T> {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // The index page size
    private static final int DEFAULT_INDEX_PAGE_SIZE = 5000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The currently selected experiment
    protected static TmfExperiment<?> fCurrentExperiment = null;

    // The set of traces that constitute the experiment
    protected ITmfTrace<T>[] fTraces;

    // Flag to initialize only once
    protected boolean fInitialized = false;

    // The experiment bookmarks file
    protected IFile fBookmarksFile;

//    // The properties resource
//    private IResource fResource;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Override
    public boolean validate(final IProject project, final String path) {
        return true;
    }

    @Override
    public void initTrace(final IResource resource, final String path, final Class<T> type) {
//        fResource = resource;
        try {
            super.initTrace(resource, path, type);
        } catch (TmfTraceException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * @param type
     * @param id
     * @param traces
     * @param epoch
     * @param indexPageSize
     * @throws TmfTraceException 
     */
    public TmfExperiment(final Class<T> type, final String id, final ITmfTrace<T>[] traces, final ITmfTimestamp epoch,
            final int indexPageSize)
    {
        this(type, id, traces, TmfTimestamp.ZERO, indexPageSize, false);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TmfExperiment(final Class<T> type, final String path, final ITmfTrace<T>[] traces, final ITmfTimestamp epoch,
            final int indexPageSize, final boolean preIndexExperiment)
    {
        setCacheSize(indexPageSize);
        setStreamingInterval(0);
        setIndexer(new TmfCheckpointIndexer(this, indexPageSize));
        setParser(this);
        try {
            super.initialize(null, path, type);
        } catch (TmfTraceException e) {
            e.printStackTrace();
        }

        fTraces = traces;
        setTimeRange(TmfTimeRange.NULL_RANGE);

        if (preIndexExperiment) {
            getIndexer().buildIndex(true);
        }
    }

    /**
     * @param type
     * @param id
     * @param traces
     * @throws TmfTraceException 
     */
    public TmfExperiment(final Class<T> type, final String id, final ITmfTrace<T>[] traces) {
        this(type, id, traces, TmfTimestamp.ZERO, DEFAULT_INDEX_PAGE_SIZE);
    }

    /**
     * @param type
     * @param id
     * @param traces
     * @param indexPageSize
     * @throws TmfTraceException 
     */
    public TmfExperiment(final Class<T> type, final String id, final ITmfTrace<T>[] traces, final int indexPageSize) {
        this(type, id, traces, TmfTimestamp.ZERO, indexPageSize);
    }

    /**
     * Clears the experiment
     */
    @Override
    @SuppressWarnings("rawtypes")
    public synchronized void dispose() {

        final TmfExperimentDisposedSignal<T> signal = new TmfExperimentDisposedSignal<T>(this, this);
        broadcast(signal);
        if (fCurrentExperiment == this)
            fCurrentExperiment = null;

        if (fTraces != null) {
            for (final ITmfTrace trace : fTraces)
                trace.dispose();
            fTraces = null;
        }
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public static void setCurrentExperiment(final TmfExperiment<?> experiment) {
        if (fCurrentExperiment != null && fCurrentExperiment != experiment)
            fCurrentExperiment.dispose();
        fCurrentExperiment = experiment;
    }

    public static TmfExperiment<?> getCurrentExperiment() {
        return fCurrentExperiment;
    }

    public ITmfTrace<T>[] getTraces() {
        return fTraces;
    }

    /**
     * Returns the timestamp of the event at the requested index. If none,
     * returns null.
     * 
     * @param index the event index (rank)
     * @return the corresponding event timestamp
     */
    public ITmfTimestamp getTimestamp(final int index) {
        final ITmfContext context = seekEvent(index);
        final ITmfEvent event = getNext(context);
        return (event != null) ? event.getTimestamp() : null;
    }

    // ------------------------------------------------------------------------
    // TmfProvider
    // ------------------------------------------------------------------------

    @Override
    public ITmfContext armRequest(final ITmfDataRequest<T> request) {
        ITmfTimestamp timestamp = (request instanceof ITmfEventRequest<?>) ? ((ITmfEventRequest<T>) request).getRange().getStartTime() : null;
        if (TmfTimestamp.BIG_BANG.equals(timestamp) || request.getIndex() > 0) {
            timestamp = null;
        }

        ITmfContext context = null;
        if (timestamp != null) { // Seek by timestamp
            context = seekEvent(timestamp);
            ((ITmfEventRequest<T>) request).setStartIndex((int) context.getRank());
        } else { // Seek by rank
            context = seekEvent(request.getIndex());
        }

        return context;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace trace positioning
    // ------------------------------------------------------------------------

    // Returns a brand new context based on the location provided
    // and initializes the event queues
    @Override
    public synchronized TmfExperimentContext seekEvent(final ITmfLocation<?> location) {
        // Validate the location
        if (location != null && !(location instanceof TmfExperimentLocation))
            return null; // Throw an exception?

        if (fTraces == null)
            return null;

        // Instantiate the location
        final TmfExperimentLocation expLocation = (location == null) ? new TmfExperimentLocation(new TmfLocationArray(
                new ITmfLocation<?>[fTraces.length])) : (TmfExperimentLocation) location.clone();

        // Create and populate the context's traces contexts
        final TmfExperimentContext context = new TmfExperimentContext(new ITmfContext[fTraces.length]);

        for (int i = 0; i < fTraces.length; i++) {
            // Get the relevant trace attributes
            final ITmfLocation<?> traceLocation = expLocation.getLocation().getLocations()[i];
            context.getContexts()[i] = fTraces[i].seekEvent(traceLocation);
            expLocation.getLocation().getLocations()[i] = context.getContexts()[i].getLocation().clone();
            context.getEvents()[i] = fTraces[i].getNext(context.getContexts()[i]);
        }

        // Finalize context
        context.setLocation(expLocation);
        context.setLastTrace(TmfExperimentContext.NO_TRACE);
        context.setRank(ITmfContext.UNKNOWN_RANK);
        return context;
    }

    @Override
    public ITmfContext seekEvent(final double ratio) {
        final ITmfContext context = seekEvent((long) (ratio * getNbEvents()));
        return context;
    }

    @Override
    public double getLocationRatio(final ITmfLocation<?> location) {
        if (location instanceof TmfExperimentLocation)
            return (double) seekEvent(location).getRank() / getNbEvents();
        return 0;
    }

    @Override
    public ITmfLocation<?> getCurrentLocation() {
        ITmfLocation<?>[] locations = new ITmfLocation<?>[fTraces.length];
        for (int i = 0; i < fTraces.length; i++) {
            locations[i] = fTraces[i].getCurrentLocation();
        }
        return new TmfExperimentLocation(new TmfLocationArray(locations));
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#readNextEvent(org.eclipse.linuxtools.tmf.core.trace.ITmfContext)
     */
    @Override
    public synchronized T getNext(final ITmfContext context) {
        final ITmfContext previousContext = (TmfExperimentContext) context.clone();
        final T event = parseEvent(context);
        if (event != null) {
            updateAttributes(previousContext, event.getTimestamp());

            TmfExperimentContext expContext = (TmfExperimentContext) context;
            int trace = expContext.getLastTrace();
            if (trace != TmfExperimentContext.NO_TRACE) {
                TmfExperimentLocation location = (TmfExperimentLocation) expContext.getLocation();
                location.getLocation().getLocations()[trace] = expContext.getContexts()[trace].getLocation();
            }
            
            context.increaseRank();
            processEvent(event);
        }
        return event;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#parseEvent(org.eclipse.linuxtools .tmf.trace.TmfContext)
     */
    @SuppressWarnings("unchecked")
    @Override
    public T parseEvent(ITmfContext context) {

        // Validate the context
        if (!(context instanceof TmfExperimentContext))
            return null; // Throw an exception?

        TmfExperimentContext expContext = (TmfExperimentContext) context;

        // If an event was consumed previously, first get the next one from that trace
        final int lastTrace = expContext.getLastTrace();
        if (lastTrace != TmfExperimentContext.NO_TRACE) {
            final ITmfContext traceContext = expContext.getContexts()[lastTrace];
            expContext.getEvents()[lastTrace] = fTraces[lastTrace].getNext(traceContext);
            expContext.setLastTrace(TmfExperimentContext.NO_TRACE);
        }

        // Scan the candidate events and identify the "next" trace to read from
        int trace = TmfExperimentContext.NO_TRACE;
        ITmfTimestamp timestamp = TmfTimestamp.BIG_CRUNCH;
        for (int i = 0; i < fTraces.length; i++) {
            final ITmfEvent event = expContext.getEvents()[i];
            if (event != null && event.getTimestamp() != null) {
                final ITmfTimestamp otherTS = event.getTimestamp();
                if (otherTS.compareTo(timestamp, true) < 0) {
                    trace = i;
                    timestamp = otherTS;
                }
            }
        }

        T event = null;
        if (trace != TmfExperimentContext.NO_TRACE) {
            event = (T) expContext.getEvents()[trace];
        }

        expContext.setLastTrace(trace);
        return event;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfExperiment (" + getName() + ")]";
    }

    // ------------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------------

    private synchronized void initializeStreamingMonitor() {
        if (fInitialized)
            return;
        fInitialized = true;

        if (getStreamingInterval() == 0) {
            final ITmfContext context = seekEvent(0);
            final ITmfEvent event = getNext(context);
            if (event == null)
                return;
            final TmfTimeRange timeRange = new TmfTimeRange(event.getTimestamp().clone(), TmfTimestamp.BIG_CRUNCH);
            final TmfExperimentRangeUpdatedSignal signal = new TmfExperimentRangeUpdatedSignal(this, this, timeRange);

            // Broadcast in separate thread to prevent deadlock
            new Thread() {
                @Override
                public void run() {
                    broadcast(signal);
                }
            }.start();
            return;
        }

        final Thread thread = new Thread("Streaming Monitor for experiment " + getName()) { ////$NON-NLS-1$
            private ITmfTimestamp safeTimestamp = null;
            private TmfTimeRange timeRange = null;

            @Override
            public void run() {
                while (!fExecutor.isShutdown()) {
                    if (!isIndexingBusy()) {
                        ITmfTimestamp startTimestamp = TmfTimestamp.BIG_CRUNCH;
                        ITmfTimestamp endTimestamp = TmfTimestamp.BIG_BANG;
                        for (final ITmfTrace<T> trace : fTraces) {
                            if (trace.getStartTime().compareTo(startTimestamp) < 0)
                                startTimestamp = trace.getStartTime();
                            if (trace.getStreamingInterval() != 0 && trace.getEndTime().compareTo(endTimestamp) > 0)
                                endTimestamp = trace.getEndTime();
                        }
                        if (safeTimestamp != null && safeTimestamp.compareTo(getTimeRange().getEndTime(), false) > 0)
                            timeRange = new TmfTimeRange(startTimestamp, safeTimestamp);
                        else
                            timeRange = null;
                        safeTimestamp = endTimestamp;
                        if (timeRange != null) {
                            final TmfExperimentRangeUpdatedSignal signal =
                                    new TmfExperimentRangeUpdatedSignal(TmfExperiment.this, TmfExperiment.this, timeRange);
                            broadcast(signal);
                        }
                    }
                    try {
                        Thread.sleep(getStreamingInterval());
                    } catch (final InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getStreamingInterval()
     */
    @Override
    public long getStreamingInterval() {
        long interval = 0;
        for (final ITmfTrace<T> trace : fTraces)
            interval = Math.max(interval, trace.getStreamingInterval());
        return interval;
    }

    /*
     * The experiment holds the globally ordered events of its set of traces. It
     * is expected to provide access to each individual event by index i.e. it
     * must be possible to request the Nth event of the experiment.
     * 
     * The purpose of the index is to keep the information needed to rapidly
     * restore the traces contexts at regular intervals (every INDEX_PAGE_SIZE
     * event).
     */

//    protected int fIndexPageSize;
    protected boolean fIndexing = false;
//    protected TmfTimeRange fIndexingPendingRange = TmfTimeRange.NULL_RANGE;

    private Integer fEndSynchReference;

    protected boolean isIndexingBusy() {
        return fIndexing;
    }


//    protected void notifyListeners() {
//        broadcast(new TmfExperimentUpdatedSignal(this, this));
//    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void traceUpdated(final TmfTraceUpdatedSignal signal) {
        if (signal.getTrace() == this) {
            broadcast(new TmfExperimentUpdatedSignal(this, this));
        }
    }

    @TmfSignalHandler
    public void experimentSelected(final TmfExperimentSelectedSignal<T> signal) {
        final TmfExperiment<?> experiment = signal.getExperiment();
        if (experiment == this) {
            setCurrentExperiment(experiment);
            fEndSynchReference = Integer.valueOf(signal.getReference());
        }
    }

    @TmfSignalHandler
    public void endSync(final TmfEndSynchSignal signal) {
        if (fEndSynchReference != null && fEndSynchReference.intValue() == signal.getReference()) {
            fEndSynchReference = null;
            initializeStreamingMonitor();
        }
    }

    @TmfSignalHandler
    public void experimentUpdated(final TmfExperimentUpdatedSignal signal) {
    }

    @TmfSignalHandler
    public void experimentRangeUpdated(final TmfExperimentRangeUpdatedSignal signal) {
        if (signal.getExperiment() == this) {
            getIndexer().buildIndex(false);
        }
    }

    @Override
    public String getPath() {
        // TODO Auto-generated method stub
        return null;
    }

    /**
     * Set the file to be used for bookmarks on this experiment
     * 
     * @param file the bookmarks file
     */
    public void setBookmarksFile(final IFile file) {
        fBookmarksFile = file;
    }

    /**
     * Get the file used for bookmarks on this experiment
     * 
     * @return the bookmarks file or null if none is set
     */
    public IFile getBookmarksFile() {
        return fBookmarksFile;
    }

//    /*
//     * (non-Javadoc)
//     * 
//     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getResource()
//     */
//    @Override
//    public IResource getResource() {
//        return fResource;
//    }
}
