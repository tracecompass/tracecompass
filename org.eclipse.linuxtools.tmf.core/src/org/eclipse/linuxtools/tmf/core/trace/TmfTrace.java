/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;

/**
 * <b><u>TmfTrace</u></b>
 * <p>
 * Abstract implementation of ITmfTrace. It should be sufficient to extend this class and provide implementation for
 * <code>getCurrentLocation()</code> and <code>seekLocation()</code>, as well as a proper parser, to have a working
 * concrete implementation.
 * <p>
 * Note: The notion of event rank is still under heavy discussion. Although used by the Events View and probably useful
 * in the general case, there is no easy way to implement it for LTTng (actually a strong case is being made that this
 * is useless).
 * <p>
 * That it is not supported by LTTng does by no mean indicate that it is not useful for (just about) every other tracing
 * tool. Therefore, this class provides a minimal (and partial) implementation of rank. However, the current
 * implementation should not be relied on in the general case.
 * 
 * TODO: Add support for live streaming (notifications, incremental indexing, ...)
 */
public abstract class TmfTrace<T extends TmfEvent> extends TmfEventProvider<T> implements ITmfTrace<T>, Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // The default number of events to cache
    // TODO: Make the DEFAULT_CACHE_SIZE a preference
    public static final int DEFAULT_INDEX_PAGE_SIZE = 50000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The trace path
    private String fPath;

    // The trace name
    private String fTraceName;

    // The cache page size AND checkpoints interval
    protected int fIndexPageSize = DEFAULT_INDEX_PAGE_SIZE;

    // The set of event stream checkpoints (for random access)
    protected Vector<TmfCheckpoint> fCheckpoints = new Vector<TmfCheckpoint>();

    // The number of events collected
    protected long fNbEvents = 0;

    // The time span of the event stream
    private ITmfTimestamp fStartTime = TmfTimestamp.BigCrunch;
    private ITmfTimestamp fEndTime = TmfTimestamp.BigBang;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfTrace() {
        super();
    }

    @Override
    public void initTrace(String path, Class<T> eventType) throws FileNotFoundException {
        initTmfTrace(path, eventType, DEFAULT_INDEX_PAGE_SIZE, false, null);
    }

    @Override
    public void initTrace(String path, Class<T> eventType, int cacheSize) throws FileNotFoundException {
        initTmfTrace(path, eventType, cacheSize, false, null);
    }

    @Override
    public void initTrace(String path, Class<T> eventType, boolean indexTrace) throws FileNotFoundException {
        initTmfTrace(path, eventType, DEFAULT_INDEX_PAGE_SIZE, indexTrace, null);
    }

    @Override
    public void initTrace(String path, Class<T> eventType, int cacheSize, boolean indexTrace) throws FileNotFoundException {
        initTmfTrace(path, eventType, cacheSize, indexTrace, null);
    }

    @Override
    public void initTrace(String path, Class<T> eventType, int cacheSize, boolean indexTrace, String name) throws FileNotFoundException {
        initTmfTrace(path, eventType, cacheSize, indexTrace, name);
    }

    private void initTmfTrace(String path, Class<T> eventType, int cacheSize, boolean indexTrace, String name) throws FileNotFoundException {
        fPath = path;
        if (name != null) {
            fTraceName = name;
        }
        if (fTraceName == null) {
            fTraceName = ""; //$NON-NLS-1$
            if (path != null) {
                int sep = path.lastIndexOf(Path.SEPARATOR);
                fTraceName = (sep >= 0) ? path.substring(sep + 1) : path;
            }
        }
        super.init(fTraceName, eventType);
        fIndexPageSize = (cacheSize > 0) ? cacheSize : DEFAULT_INDEX_PAGE_SIZE;
        if (indexTrace)
            indexTrace(false);
    }

    @Override
    public boolean validate(IProject project, String path) {
        File file = new File(path);
        return file.exists();
    }

    /**
     * @param path
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, Class<T> type, String path) throws FileNotFoundException {
        this(name, type, path, DEFAULT_INDEX_PAGE_SIZE, true);
    }

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, Class<T> type, String path, int cacheSize) throws FileNotFoundException {
        this(name, type, path, cacheSize, true);
    }

    /**
     * @param path
     * @param indexTrace
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, Class<T> type, String path, boolean indexTrace) throws FileNotFoundException {
        this(name, type, path, DEFAULT_INDEX_PAGE_SIZE, indexTrace);
    }

    /**
     * @param path
     * @param cacheSize
     * @param indexTrace
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, Class<T> type, String path, int cacheSize, boolean indexTrace) throws FileNotFoundException {
        super();
        fTraceName = name;
        initTrace(path, type, cacheSize, indexTrace);
    }

    @SuppressWarnings("unchecked")
    @Override
    public TmfTrace<T> clone() throws CloneNotSupportedException {
        TmfTrace<T> clone = (TmfTrace<T>) super.clone();
        clone.fCheckpoints = fCheckpoints;
        clone.fStartTime = fStartTime.clone();
        clone.fEndTime = fEndTime.clone();
        return clone;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the trace path
     */
    @Override
    public String getPath() {
        return fPath;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getNbEvents()
     */
    @Override
    public long getNbEvents() {
        return fNbEvents;
    }

    /**
     * @return the size of the cache
     */
    @Override
    public int getCacheSize() {
        return fIndexPageSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getTimeRange()
     */
    @Override
    public TmfTimeRange getTimeRange() {
        return new TmfTimeRange(fStartTime, fEndTime);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getStartTime()
     */
    @Override
    public ITmfTimestamp getStartTime() {
        return fStartTime;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getEndTime()
     */
    @Override
    public ITmfTimestamp getEndTime() {
        return fEndTime;
    }

    @SuppressWarnings("unchecked")
    public Vector<TmfCheckpoint> getCheckpoints() {
        return (Vector<TmfCheckpoint>) fCheckpoints.clone();
    }

    /**
     * Returns the rank of the first event with the requested timestamp. If none, returns the index of the next event
     * (if any).
     * 
     * @param timestamp
     * @return
     */
    @Override
    public long getRank(ITmfTimestamp timestamp) {
        TmfContext context = seekEvent(timestamp);
        return context.getRank();
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    protected void setTimeRange(TmfTimeRange range) {
        fStartTime = range.getStartTime();
        fEndTime = range.getEndTime();
    }

    protected void setStartTime(TmfTimestamp startTime) {
        fStartTime = startTime;
    }

    protected void setEndTime(TmfTimestamp endTime) {
        fEndTime = endTime;
    }

    // ------------------------------------------------------------------------
    // TmfProvider
    // ------------------------------------------------------------------------

    @Override
    public ITmfContext armRequest(ITmfDataRequest<T> request) {
        if (request instanceof ITmfEventRequest<?>
                && !TmfTimestamp.BigBang.equals(((ITmfEventRequest<T>) request).getRange().getStartTime()) && request.getIndex() == 0) {
            ITmfContext context = seekEvent(((ITmfEventRequest<T>) request).getRange().getStartTime());
            ((ITmfEventRequest<T>) request).setStartIndex((int) context.getRank());
            return context;

        }
        return seekEvent(request.getIndex());
    }

    /**
     * Return the next piece of data based on the context supplied. The context would typically be updated for the
     * subsequent read.
     * 
     * @param context
     * @return
     */
    @SuppressWarnings("unchecked")
    @Override
    public T getNext(ITmfContext context) {
        if (context instanceof TmfContext) {
            return (T) getNextEvent((TmfContext) context);
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
     */
    @Override
    public TmfContext seekEvent(ITmfTimestamp timestamp) {

        if (timestamp == null) {
            timestamp = TmfTimestamp.BigBang;
        }

        // First, find the right checkpoint
        int index = Collections.binarySearch(fCheckpoints, new TmfCheckpoint(timestamp, null));

        // In the very likely case that the checkpoint was not found, bsearch
        // returns its negated would-be location (not an offset...). From that
        // index, we can then position the stream and get the event.
        if (index < 0) {
            index = Math.max(0, -(index + 2));
        }

        // Position the stream at the checkpoint
        ITmfLocation<?> location;
        synchronized (fCheckpoints) {
            if (fCheckpoints.size() > 0) {
                if (index >= fCheckpoints.size()) {
                    index = fCheckpoints.size() - 1;
                }
                location = fCheckpoints.elementAt(index).getLocation();
            } else {
                location = null;
            }
        }
        TmfContext context = seekLocation(location);
        context.setRank(index * fIndexPageSize);

        // And locate the event
        TmfContext nextEventContext = context.clone(); // Must use clone() to get the right subtype...
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
            context.setLocation(nextEventContext.getLocation().clone());
            context.updateRank(1);
            event = getNextEvent(nextEventContext);
        }

        return context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(int)
     */
    @Override
    public TmfContext seekEvent(long rank) {

        // Position the stream at the previous checkpoint
        int index = (int) rank / fIndexPageSize;
        ITmfLocation<?> location;
        synchronized (fCheckpoints) {
            if (fCheckpoints.size() == 0) {
                location = null;
            } else {
                if (index >= fCheckpoints.size()) {
                    index = fCheckpoints.size() - 1;
                }
                location = fCheckpoints.elementAt(index).getLocation();
            }
        }

        TmfContext context = seekLocation(location);
        long pos = index * fIndexPageSize;
        context.setRank(pos);

        if (pos < rank) {
            TmfEvent event = getNextEvent(context);
            while (event != null && ++pos < rank) {
                event = getNextEvent(context);
            }
        }

        return context;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getNextEvent(org.eclipse.
     * linuxtools.tmf.trace.ITmfTrace.TraceContext)
     */
    @Override
    public synchronized TmfEvent getNextEvent(TmfContext context) {
        // parseEvent() does not update the context
        TmfEvent event = parseEvent(context);
        if (event != null) {
            updateIndex(context, context.getRank(), event.getTimestamp());
            context.setLocation(getCurrentLocation());
            context.updateRank(1);
            processEvent(event);
        }
        return event;
    }

    protected synchronized void updateIndex(ITmfContext context, long rank, ITmfTimestamp timestamp) {
        if (fStartTime.compareTo(timestamp, false) > 0)
            fStartTime = timestamp;
        if (fEndTime.compareTo(timestamp, false) < 0)
            fEndTime = timestamp;
        if (context.isValidRank()) {
            if (fNbEvents <= rank)
                fNbEvents = rank + 1;
            // Build the index as we go along
            if ((rank % fIndexPageSize) == 0) {
                // Determine the table position
                long position = rank / fIndexPageSize;
                // Add new entry at proper location (if empty)
                if (fCheckpoints.size() == position) {
                    ITmfLocation<?> location = context.getLocation().clone();
                    fCheckpoints.add(new TmfCheckpoint(timestamp.clone(), location));
                    // System.out.println(getName() + "[" + (fCheckpoints.size()
                    // - 1) + "] " + timestamp + ", " + location.toString());
                }
            }
        }
    }

    /**
     * Hook for special processing by the concrete class (called by getNextEvent())
     * 
     * @param event
     */
    protected void processEvent(TmfEvent event) {
        // Do nothing by default
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfTrace (" + getName() + ")]";
    }

    // ------------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------------

    /*
     * The purpose of the index is to keep the information needed to rapidly
     * restore the traces contexts at regular intervals (every INDEX_PAGE_SIZE
     * event).
     */

    @SuppressWarnings({ "unchecked" })
    protected void indexTrace(boolean waitForCompletion) {

        final Job job = new Job("Indexing " + getName() + "...") { //$NON-NLS-1$ //$NON-NLS-2$
            @Override
            protected IStatus run(IProgressMonitor monitor) {
                while (!monitor.isCanceled()) {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        return Status.OK_STATUS;
                    }
                }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();

        fCheckpoints.clear();
        ITmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, TmfTimeRange.Eternity, TmfDataRequest.ALL_DATA,
                fIndexPageSize, ITmfDataRequest.ExecutionType.BACKGROUND) {

            ITmfTimestamp startTime = null;
            ITmfTimestamp lastTime = null;

            @Override
            public void handleData(TmfEvent event) {
                super.handleData(event);
                if (event != null) {
                    ITmfTimestamp ts = event.getTimestamp();
                    if (startTime == null)
                        startTime = ts.clone();
                    lastTime = ts.clone();

                    if ((getNbRead() % fIndexPageSize) == 0) {
                        updateTrace();
                    }
                }
            }

            @Override
            public void handleSuccess() {
                updateTrace();
            }

            @Override
            public void handleCompleted() {
                job.cancel();
                super.handleCompleted();
            }

            private void updateTrace() {
                int nbRead = getNbRead();
                if (nbRead != 0) {
                    fStartTime = startTime;
                    fEndTime = lastTime;
                    fNbEvents = nbRead;
                    notifyListeners();
                }
            }
        };

        sendRequest((ITmfDataRequest<T>) request);
        if (waitForCompletion)
            try {
                request.waitForCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
    }

    protected void notifyListeners() {
        broadcast(new TmfTraceUpdatedSignal(this, this, new TmfTimeRange(fStartTime, fEndTime)));
    }
}
