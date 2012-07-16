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

package org.eclipse.linuxtools.tmf.core.trace;

import java.io.File;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;

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
 * <li> public boolean validate(IProject project, String path)
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
public abstract class TmfTrace<T extends ITmfEvent> extends TmfEventProvider<T> implements ITmfTrace<T> {

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
    private long fNbEvents = 0;

    // The time span of the event stream
    private ITmfTimestamp fStartTime = TmfTimestamp.BIG_CRUNCH;
    private ITmfTimestamp fEndTime = TmfTimestamp.BIG_BANG;

    // The trace streaming interval (0 = no streaming)
    private long fStreamingInterval = 0;

    // The trace indexer
    private ITmfTraceIndexer<ITmfTrace<ITmfEvent>> fIndexer;

    // The trace parser
    private ITmfEventParser<T> fParser;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * The default, parameterless, constructor
     */
    public TmfTrace() {
        super();
    }

    /**
     * The standard constructor (non-live trace). Applicable when the trace
     * implements its own parser and if at checkpoint-based index is OK.
     *
     * @param resource the resource associated to the trace
     * @param type the trace event type
     * @param path the trace path
     * @param cacheSize the trace cache size
     * @throws TmfTraceException
     */
    protected TmfTrace(final IResource resource, final Class<T> type, final String path, final int cacheSize) throws TmfTraceException {
        this(resource, type, path, cacheSize, 0);
    }

    /**
     * The standard constructor (live trace). Applicable when the trace
     * implements its own parser and if at checkpoint-based index is OK.
     *
     * @param resource the resource associated to the trace
     * @param type the trace event type
     * @param path the trace path
     * @param cacheSize the trace cache size
     * @param interval the trace streaming interval
     * @throws TmfTraceException
     */
    protected TmfTrace(final IResource resource, final Class<T> type, final String path, final int cacheSize, final long interval) throws TmfTraceException {
        this(resource, type, path, cacheSize, interval, null);
    }

    /**
     * The 'non-default indexer' constructor. Allows to provide a trace
     * specific indexer.
     *
     * @param resource the resource associated to the trace
     * @param type the trace event type
     * @param path the trace path
     * @param cacheSize the trace cache size
     * @param indexer the trace indexer
     * @throws TmfTraceException
     */
    protected TmfTrace(final IResource resource, final Class<T> type, final String path, final int cacheSize,
            final long interval, final ITmfTraceIndexer<?> indexer) throws TmfTraceException {
        this(resource, type, path, cacheSize, interval, indexer, null);
    }

    /**
     * The full constructor where trace specific indexer/parser are provided.
     *
     * @param resource the resource associated to the trace
     * @param type the trace event type
     * @param path the trace path
     * @param cacheSize the trace cache size
     * @param indexer the trace indexer
     * @param parser the trace event parser
     * @throws TmfTraceException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected TmfTrace(final IResource resource, final Class<T> type, final String path, final int cacheSize,
            final long interval, final ITmfTraceIndexer<?> indexer, final ITmfEventParser<T> parser) throws TmfTraceException {
        super();
        fCacheSize = (cacheSize > 0) ? cacheSize : ITmfTrace.DEFAULT_TRACE_CACHE_SIZE;
        fStreamingInterval = interval;
        fIndexer = (indexer != null) ? indexer : new TmfCheckpointIndexer(this, fCacheSize);
        fParser = parser;
        initialize(resource, path, type);
    }

    /**
     * Copy constructor
     *
     * @param trace the original trace
     * @throws TmfTraceException Should not happen usually
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TmfTrace(final TmfTrace<T> trace) throws TmfTraceException {
        super();
        if (trace == null) {
            throw new IllegalArgumentException();
        }
        fCacheSize = trace.getCacheSize();
        fStreamingInterval = trace.getStreamingInterval();
        fIndexer = new TmfCheckpointIndexer(this);
        fParser = trace.fParser;
        initialize(trace.getResource(), trace.getPath(), trace.getEventType());
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Initializers
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#initTrace(org.eclipse.core.resources.IResource, java.lang.String, java.lang.Class)
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void initTrace(final IResource resource, final String path, final Class<T> type) throws TmfTraceException {
        fIndexer = new TmfCheckpointIndexer(this, fCacheSize);
        initialize(resource, path, type);
    }

    /**
     * Initialize the trace common attributes and the base component.
     *
     * @param resource the Eclipse resource (trace)
     * @param path the trace path
     * @param type the trace event type
     *
     * @throws TmfTraceException
     */
    @SuppressWarnings("unchecked")
    protected void initialize(final IResource resource, final String path, final Class<T> type) throws TmfTraceException {
        if (path == null) {
            throw new TmfTraceException("Invalid trace path"); //$NON-NLS-1$
        }
        fPath = path;
        fResource = resource;
        String traceName = (resource != null) ? resource.getName() : null;
        // If no resource was provided, extract the display name the trace path
        if (traceName == null) {
            final int sep = path.lastIndexOf(IPath.SEPARATOR);
            traceName = (sep >= 0) ? path.substring(sep + 1) : path;
        }
        if (fParser == null) {
            if (this instanceof ITmfEventParser) {
                fParser = (ITmfEventParser<T>) this;
            } else {
                throw new TmfTraceException("Invalid trace parser"); //$NON-NLS-1$
            }
        }
        super.init(traceName, type);
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
     * Index the trace
     *
     * @param waitForCompletion index synchronously (true) or not (false)
     */
    protected void indexTrace(boolean waitForCompletion) {
        getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, waitForCompletion);
    }

    /**
     * Clears the trace
     */
    @Override
    public synchronized void dispose() {
        // Clean up the index if applicable
        if (getIndexer() != null) {
            getIndexer().dispose();
        }
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Basic getters
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getEventType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getEventType() {
        return (Class<T>) super.getType();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getResource()
     */
    @Override
    public IResource getResource() {
        return fResource;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getPath()
     */
    @Override
    public String getPath() {
        return fPath;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getIndexPageSize()
     */
    @Override
    public int getCacheSize() {
        return fCacheSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getStreamingInterval()
     */
    @Override
    public long getStreamingInterval() {
        return fStreamingInterval;
    }

    /**
     * @return the trace indexer
     */
    protected ITmfTraceIndexer<ITmfTrace<ITmfEvent>> getIndexer() {
        return fIndexer;
    }

    /**
     * @return the trace parser
     */
    protected ITmfEventParser<T> getParser() {
        return fParser;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Trace characteristics getters
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getNbEvents()
     */
    @Override
    public synchronized long getNbEvents() {
        return fNbEvents;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getTimeRange()
     */
    @Override
    public TmfTimeRange getTimeRange() {
        return new TmfTimeRange(fStartTime, fEndTime);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getStartTime()
     */
    @Override
    public ITmfTimestamp getStartTime() {
        return fStartTime.clone();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getEndTime()
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return fEndTime.clone();
    }

    // ------------------------------------------------------------------------
    // Convenience setters/getters
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
     */
    protected void setTimeRange(final TmfTimeRange range) {
        fStartTime = range.getStartTime().clone();
        fEndTime = range.getEndTime().clone();
    }

    /**
     * Update the trace chronologically first event timestamp
     *
     * @param startTime the new first event timestamp
     */
    protected void setStartTime(final ITmfTimestamp startTime) {
        fStartTime = startTime.clone();
    }

    /**
     * Update the trace chronologically last event timestamp
     *
     * @param endTime the new last event timestamp
     */
    protected void setEndTime(final ITmfTimestamp endTime) {
        fEndTime = endTime.clone();
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
     * Set the trace indexer. Must be done at initialization time.
     *
     * @param indexer the trace indexer
     */
    protected void setIndexer(final ITmfTraceIndexer<ITmfTrace<ITmfEvent>> indexer) {
        fIndexer = indexer;
    }

    /**
     * Set the trace parser. Must be done at initialization time.
     *
     * @param parser the new trace parser
     */
    protected void setParser(final ITmfEventParser<T> parser) {
        fParser = parser;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - SeekEvent operations (returning a trace context)
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(long)
     */
    @Override
    public synchronized ITmfContext seekEvent(final long rank) {

        // A rank <= 0 indicates to seek the first event
        if (rank <= 0) {
            ITmfContext context = seekEvent((ITmfLocation<?>) null);
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

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp)
     */
    @Override
    public synchronized ITmfContext seekEvent(final ITmfTimestamp timestamp) {

        // A null timestamp indicates to seek the first event
        if (timestamp == null) {
            ITmfContext context = seekEvent((ITmfLocation<?>) null);
            context.setRank(0);
            return context;
        }

        // Position the trace at the checkpoint
        ITmfContext context = fIndexer.seekIndex(timestamp);

        // And locate the requested event context
        final ITmfContext nextEventContext = context.clone(); // Must use clone() to get the right subtype...
        ITmfEvent event = getNext(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
            context = nextEventContext.clone();
            event = getNext(nextEventContext);
        }
        if (event == null) {
            context.setLocation(null);
            context.setRank(ITmfContext.UNKNOWN_RANK);
        }
        return context;
    }
    
    // ------------------------------------------------------------------------
    // ITmfTrace - Read operations (returning an actual event)
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#readNextEvent(org.eclipse.linuxtools.tmf.core.trace.ITmfContext)
     */
    @Override
    public synchronized T getNext(final ITmfContext context) {
        // parseEvent() does not update the context
        final T event = fParser.parseEvent(context);
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
     */
    protected synchronized void updateAttributes(final ITmfContext context, final ITmfTimestamp timestamp) {
        if (fStartTime.equals(TmfTimestamp.BIG_BANG) || (fStartTime.compareTo(timestamp, false) > 0)) {
            fStartTime = timestamp.clone();
        }
        if (fEndTime.equals(TmfTimestamp.BIG_CRUNCH) || (fEndTime.compareTo(timestamp, false) < 0)) {
            fEndTime = timestamp.clone();
        }
        if (context.hasValidRank()) {
            long rank = context.getRank();
            if (fNbEvents <= rank) {
                fNbEvents = rank + 1;
            }
            fIndexer.updateIndex(context, timestamp);
        }
    }

    // ------------------------------------------------------------------------
    // TmfDataProvider
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.component.TmfDataProvider#armRequest(org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest)
     */
    @Override
    protected ITmfContext armRequest(final ITmfDataRequest<T> request) {
        if ((request instanceof ITmfEventRequest<?>)
            && !TmfTimestamp.BIG_BANG.equals(((ITmfEventRequest<T>) request).getRange().getStartTime())
            && (request.getIndex() == 0))
        {
            final ITmfContext context = seekEvent(((ITmfEventRequest<T>) request).getRange().getStartTime());
            ((ITmfEventRequest<T>) request).setStartIndex((int) context.getRank());
            return context;

        }
        return seekEvent(request.getIndex());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public synchronized String toString() {
        return "TmfTrace [fPath=" + fPath + ", fCacheSize=" + fCacheSize
                + ", fNbEvents=" + fNbEvents + ", fStartTime=" + fStartTime
                + ", fEndTime=" + fEndTime + ", fStreamingInterval=" + fStreamingInterval + "]";
    }

}
