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
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;

/**
 * <b><u>TmfTrace</u></b>
 * <p>
 * Abstract implementation of ITmfTrace.
 * <p>
 * Document me...
 */
public abstract class TmfTrace<T extends ITmfEvent> extends TmfEventProvider<T> implements ITmfTrace<T> {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The default trace cache size
     */
    public static final int DEFAULT_TRACE_CACHE_SIZE = 1000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The resource used for persistent properties for this trace
    private IResource fResource;

    // The trace path
    private String fPath;

    /**
     * The cache page size AND trace checkpoints interval
     */
    protected int fCacheSize = DEFAULT_TRACE_CACHE_SIZE;

    // The set of event stream checkpoints
    protected Vector<TmfCheckpoint> fCheckpoints = new Vector<TmfCheckpoint>();

    // The number of events collected
    protected long fNbEvents = 0;

    // The time span of the event stream
    private ITmfTimestamp fStartTime = TmfTimestamp.BIG_CRUNCH;
    private ITmfTimestamp fEndTime = TmfTimestamp.BIG_BANG;

    /**
     * The trace streaming interval (0 = no streaming)
     */
    protected long fStreamingInterval = 0;

    /**
     * The trace indexer
     */
    protected ITmfTraceIndexer<ITmfTrace<ITmfEvent>> fIndexer;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * The default, parameterless, constructor
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TmfTrace() {
        super();
        fIndexer = new TmfTraceIndexer(this);
    }

    /**
     * The standard constructor (non-streaming trace)
     * 
     * @param resource the resource associated to the trace
     * @param type the trace event type
     * @param path the trace path
     * @param cacheSize the trace cache size
     * @throws FileNotFoundException
     */
    protected TmfTrace(final IResource resource, final Class<T> type, final String path, final int cacheSize) throws FileNotFoundException {
        this(resource, type, path, cacheSize, 0, null);
    }

    /**
     * The standard constructor (streaming trace)
     * 
     * @param resource the resource associated to the trace
     * @param type the trace event type
     * @param path the trace path
     * @param cacheSize the trace cache size
     * @param interval the trace streaming interval
     * @throws FileNotFoundException
     */
    protected TmfTrace(final IResource resource, final Class<T> type, final String path, final int cacheSize, final long interval) throws FileNotFoundException {
        this(resource, type, path, cacheSize, interval, null);
    }

    /**
     * The full constructor
     * 
     * @param resource the resource associated to the trace
     * @param type the trace event type
     * @param path the trace path
     * @param cacheSize the trace cache size
     * @param indexer the trace indexer
     * @throws FileNotFoundException
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    protected TmfTrace(final IResource resource, final Class<T> type, final String path, final int cacheSize,
            final long interval, final ITmfTraceIndexer<?> indexer) throws FileNotFoundException {
        super();
        fCacheSize = (cacheSize > 0) ? cacheSize : DEFAULT_TRACE_CACHE_SIZE;
        fStreamingInterval = interval;
        fIndexer = (indexer != null) ? indexer : new TmfTraceIndexer(this);
        initialize(resource, path, type);
    }

    /**
     * Copy constructor
     * 
     * @param trace the original trace
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public TmfTrace(final TmfTrace<T> trace) throws FileNotFoundException {
        super();
        if (trace == null)
            throw new IllegalArgumentException();
        fCacheSize = trace.getCacheSize();
        fStreamingInterval = trace.getStreamingInterval();
        fIndexer = new TmfTraceIndexer(this);
        initialize(trace.getResource(), trace.getPath(), trace.getType());
    }

    /**
     * @param resource
     * @param path
     * @param type
     * @throws FileNotFoundException
     */
    protected void initialize(final IResource resource, final String path, final Class<T> type) throws FileNotFoundException {
        fResource = resource;
        fPath = path;
        String traceName = (resource != null) ? resource.getName() : null;
        // If no display name was provided, extract it from the trace path
        if (traceName == null)
            if (path != null) {
                final int sep = path.lastIndexOf(Path.SEPARATOR);
                traceName = (sep >= 0) ? path.substring(sep + 1) : path;
            }
            else {
                traceName = ""; //$NON-NLS-1$
            }
        super.init(traceName, type);
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Initializers
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#initTrace(org.eclipse.core.resources.IResource, java.lang.String, java.lang.Class)
     */
    @Override
    public void initTrace(final IResource resource, final String path, final Class<T> type) throws FileNotFoundException {
        initialize(resource, path, type);
        fIndexer.buildIndex(false);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#validate(org.eclipse.core.resources.IProject, java.lang.String)
     * 
     * Default validation: make sure the trace file exists.
     */
    @Override
    public boolean validate(final IProject project, final String path) {
        final File file = new File(path);
        return file.exists();
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Basic getters
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.component.TmfDataProvider#getType()
     */
    @Override
    @SuppressWarnings("unchecked")
    public Class<T> getType() {
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

    // ------------------------------------------------------------------------
    // ITmfTrace - Trace characteristics getters
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getNbEvents()
     */
    @Override
    public long getNbEvents() {
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
    // Convenience setters
    // ------------------------------------------------------------------------

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
     * Update the trace streaming interval
     * 
     * @param interval the new trace streaming interval
     */
    protected void setStreamingInterval(final long interval) {
        fStreamingInterval = interval;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Seek operations (returning a reading context)
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp)
     */
    @Override
    public ITmfContext seekEvent(final ITmfTimestamp timestamp) {

        // A null timestamp indicates to seek the first event
        if (timestamp == null)
            return seekLocation(null);

        // Find the checkpoint at or before the requested timestamp.
        // In the very likely event that the timestamp is not at a checkpoint
        // boundary, bsearch will return index = (- (insertion point + 1)).
        // It is then trivial to compute the index of the previous checkpoint.
        int index = Collections.binarySearch(fCheckpoints, new TmfCheckpoint(timestamp, null));
        if (index < 0) {
            index = Math.max(0, -(index + 2));
        }

        // Position the trace at the checkpoint
        final ITmfContext context = seekCheckpoint(index);

        // And locate the requested event context
        final ITmfContext nextEventContext = context.clone(); // Must use clone() to get the right subtype...
        ITmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
            context.setLocation(nextEventContext.getLocation().clone());
            context.increaseRank();
            event = getNextEvent(nextEventContext);
        }
        return context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(long)
     */
    @Override
    public ITmfContext seekEvent(final long rank) {

        // A rank <= 0 indicates to seek the first event
        if (rank <= 0)
            return seekLocation(null);

        // Find the checkpoint at or before the requested rank.
        final int index = (int) rank / fCacheSize;
        final ITmfContext context = seekCheckpoint(index);

        // And locate the requested event context
        long pos = context.getRank();
        if (pos < rank) {
            ITmfEvent event = getNextEvent(context);
            while (event != null && ++pos < rank) {
                event = getNextEvent(context);
            }
        }
        return context;
    }


    /**
     * Position the trace at the given checkpoint
     * 
     * @param index the checkpoint index
     * @return the corresponding context
     */
    private ITmfContext seekCheckpoint(int index) {
        ITmfLocation<?> location;
        synchronized (fCheckpoints) {
            if (!fCheckpoints.isEmpty()) {
                if (index >= fCheckpoints.size()) {
                    index = fCheckpoints.size() - 1;
                }
                location = fCheckpoints.elementAt(index).getLocation();
            } else {
                location = null;
            }
        }
        final ITmfContext context = seekLocation(location);
        context.setRank(index * fCacheSize);
        return context;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Read operations (returning an actual event)
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getNextEvent(org.eclipse.linuxtools.tmf.core.trace.ITmfContext)
     */
    @Override
    public synchronized ITmfEvent getNextEvent(final ITmfContext context) {
        // parseEvent() does not update the context
        final ITmfEvent event = parseEvent(context);
        if (event != null) {
            updateIndex(context, context.getRank(), event.getTimestamp());
            context.setLocation(getCurrentLocation());
            context.increaseRank();
            processEvent(event);
        }
        return event;
    }

    /**
     * Hook for special processing by the concrete class (called by
     * getNextEvent())
     * 
     * @param event
     */
    protected void processEvent(final ITmfEvent event) {
        // Do nothing by default
    }

    protected synchronized void updateIndex(final ITmfContext context, final long rank, final ITmfTimestamp timestamp) {
        if (fStartTime.compareTo(timestamp, false) > 0) {
            fStartTime = timestamp;
        }
        if (fEndTime.compareTo(timestamp, false) < 0) {
            fEndTime = timestamp;
        }
        if (context.hasValidRank()) {
            if (fNbEvents <= rank) {
                fNbEvents = rank + 1;
            }
            // Build the index as we go along
            if ((rank % fCacheSize) == 0) {
                // Determine the table position
                final long position = rank / fCacheSize;
                // Add new entry at proper location (if empty)
                if (fCheckpoints.size() == position) {
                    final ITmfLocation<?> location = context.getLocation().clone();
                    fCheckpoints.add(new TmfCheckpoint(timestamp.clone(), location));
                    // System.out.println(getName() + "[" + (fCheckpoints.size() + "] " + timestamp + ", " + location.toString());
                }
            }
        }
    }

//    // ------------------------------------------------------------------------
//    // ITmfTrace - indexing
//    // ------------------------------------------------------------------------
//
//    /*
//     * The index is a list of contexts that point to events at regular interval
//     * (rank-wise) in the trace. After it is built, the index can be used to
//     * quickly access any event by rank or timestamp.
//     * 
//     * fIndexPageSize holds the event interval (default INDEX_PAGE_SIZE).
//     */
//
//    @SuppressWarnings({ "unchecked" })
//    protected void indexTrace(final boolean waitForCompletion) {
//
//        // The monitoring job
//        final Job job = new Job("Indexing " + getName() + "...") { //$NON-NLS-1$ //$NON-NLS-2$
//
//            @Override
//            protected IStatus run(final IProgressMonitor monitor) {
//                while (!monitor.isCanceled()) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (final InterruptedException e) {
//                        return Status.OK_STATUS;
//                    }
//                }
//                monitor.done();
//                return Status.OK_STATUS;
//            }
//        };
//        job.schedule();
//
//        // Clear the checkpoints
//        fCheckpoints.clear();
//
//        // Build a background request for all the trace data. The index is
//        // updated as we go by getNextEvent().
//        final ITmfEventRequest<ITmfEvent> request = new TmfEventRequest<ITmfEvent>(ITmfEvent.class,
//                TmfTimeRange.ETERNITY,
//                TmfDataRequest.ALL_DATA, fCacheSize, ITmfDataRequest.ExecutionType.BACKGROUND)
//                {
//
//            ITmfTimestamp startTime = null;
//            ITmfTimestamp lastTime = null;
//
//            @Override
//            public void handleData(final ITmfEvent event) {
//                super.handleData(event);
//                if (event != null) {
//                    final ITmfTimestamp timestamp = event.getTimestamp();
//                    if (startTime == null) {
//                        startTime = timestamp.clone();
//                    }
//                    lastTime = timestamp.clone();
//
//                    // Update the trace status at regular intervals
//                    if ((getNbRead() % fCacheSize) == 0) {
//                        updateTraceStatus();
//                    }
//                }
//            }
//
//            @Override
//            public void handleSuccess() {
//                updateTraceStatus();
//            }
//
//            @Override
//            public void handleCompleted() {
//                job.cancel();
//                super.handleCompleted();
//            }
//
//            private synchronized void updateTraceStatus() {
//                final int nbRead = getNbRead();
//                if (nbRead != 0) {
//                    fStartTime = startTime;
//                    fEndTime = lastTime;
//                    fNbEvents = nbRead;
//                    notifyListeners();
//                }
//            }
//                };
//
//                // Submit the request and wait for completion if required
//                sendRequest((ITmfDataRequest<T>) request);
//                if (waitForCompletion) {
//                    try {
//                        request.waitForCompletion();
//                    } catch (final InterruptedException e) {
//                    }
//                }
//    }
//
//    private void notifyListeners() {
//        broadcast(new TmfTraceUpdatedSignal(this, this, new TmfTimeRange(fStartTime, fEndTime)));
//    }

    // ------------------------------------------------------------------------
    // TmfProvider
    // ------------------------------------------------------------------------

    @Override
    public ITmfContext armRequest(final ITmfDataRequest<T> request) {
        if (request instanceof ITmfEventRequest<?>
        && !TmfTimestamp.BIG_BANG.equals(((ITmfEventRequest<T>) request).getRange().getStartTime())
        && request.getIndex() == 0) {
            final ITmfContext context = seekEvent(((ITmfEventRequest<T>) request).getRange().getStartTime());
            ((ITmfEventRequest<T>) request).setStartIndex((int) context.getRank());
            return context;

        }
        return seekEvent(request.getIndex());
    }

    /**
     * Return the next piece of data based on the context supplied. The context
     * would typically be updated for the subsequent read.
     * 
     * @param context
     * @return the event referred to by context
     */
    @Override
    @SuppressWarnings("unchecked")
    public T getNext(final ITmfContext context) {
        if (context instanceof TmfContext)
            return (T) getNextEvent(context);
        return null;
    }


    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfTrace [fPath=" + fPath + ", fCacheSize=" + fCacheSize
                + ", fNbEvents=" + fNbEvents + ", fStartTime=" + fStartTime
                + ", fEndTime=" + fEndTime + ", fStreamingInterval=" + fStreamingInterval + "]";
    }

}
