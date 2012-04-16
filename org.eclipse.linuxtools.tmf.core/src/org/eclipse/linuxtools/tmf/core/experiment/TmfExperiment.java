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

package org.eclipse.linuxtools.tmf.core.experiment;

import java.util.Collections;
import java.util.Vector;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.core.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

/**
 * <b><u>TmfExperiment</u></b>
 * <p>
 * TmfExperiment presents a time-ordered, unified view of a set of TmfTraces
 * that are part of a tracing experiment.
 * <p>
 */
public class TmfExperiment<T extends ITmfEvent> extends TmfEventProvider<T> implements ITmfTrace<T> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The currently selected experiment
    protected static TmfExperiment<?> fCurrentExperiment = null;

    // The set of traces that constitute the experiment
    protected ITmfTrace<T>[] fTraces;

    // The total number of events
    protected long fNbEvents;

    // The experiment time range
    protected TmfTimeRange fTimeRange;

    // The experiment reference timestamp (default: ZERO)
    protected ITmfTimestamp fEpoch;

    // The experiment index
    protected Vector<TmfCheckpoint> fCheckpoints = new Vector<TmfCheckpoint>();

    // The current experiment context
    protected TmfExperimentContext fExperimentContext;

    // Flag to initialize only once
    private boolean fInitialized = false;

    // The experiment bookmarks file
    private IFile fBookmarksFile;

    // The properties resource
    private IResource fResource;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Override
    public TmfExperiment<T> clone() throws CloneNotSupportedException {
        throw new CloneNotSupportedException();
    }

    @Override
    public boolean validate(final IProject project, final String path) {
        return true;
    }

    @Override
    public void initTrace(final String name, final String path, final Class<T> eventType) {
    }

    /**
     * @param type
     * @param id
     * @param traces
     * @param epoch
     * @param indexPageSize
     */
    public TmfExperiment(final Class<T> type, final String id, final ITmfTrace<T>[] traces, final ITmfTimestamp epoch,
            final int indexPageSize) {
        this(type, id, traces, TmfTimestamp.ZERO, indexPageSize, false);
    }

    public TmfExperiment(final Class<T> type, final String id, final ITmfTrace<T>[] traces, final ITmfTimestamp epoch,
            final int indexPageSize, final boolean preIndexExperiment) {
        super(id, type);

        fTraces = traces;
        fEpoch = epoch;
        fIndexPageSize = indexPageSize;
        fTimeRange = TmfTimeRange.NULL_RANGE;

        if (preIndexExperiment) {
            indexExperiment(true, 0, TmfTimeRange.ETERNITY);
            updateTimeRange();
        }
    }

    protected TmfExperiment(final String id, final Class<T> type) {
        super(id, type);
    }

    /**
     * @param type
     * @param id
     * @param traces
     */
    public TmfExperiment(final Class<T> type, final String id, final ITmfTrace<T>[] traces) {
        this(type, id, traces, TmfTimestamp.ZERO, DEFAULT_INDEX_PAGE_SIZE);
    }

    /**
     * @param type
     * @param id
     * @param traces
     * @param indexPageSize
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
        if (fCheckpoints != null)
            fCheckpoints.clear();
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // ITmfTrace
    // ------------------------------------------------------------------------

    @Override
    public long getNbEvents() {
        return fNbEvents;
    }

    @Override
    public int getIndexPageSize() {
        return fIndexPageSize;
    }

    @Override
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    @Override
    public ITmfTimestamp getStartTime() {
        return fTimeRange.getStartTime();
    }

    @Override
    public ITmfTimestamp getEndTime() {
        return fTimeRange.getEndTime();
    }

    public Vector<TmfCheckpoint> getCheckpoints() {
        return fCheckpoints;
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

    public ITmfTimestamp getEpoch() {
        return fEpoch;
    }

    public ITmfTrace<T>[] getTraces() {
        return fTraces;
    }

    /**
     * Returns the rank of the first event with the requested timestamp. If
     * none, returns the index of the next event (if any).
     * 
     * @param timestamp the event timestamp
     * @return the corresponding event rank
     */
    @Override
    public long getRank(final ITmfTimestamp timestamp) {
        final TmfExperimentContext context = seekEvent(timestamp);
        return context.getRank();
    }

    /**
     * Returns the timestamp of the event at the requested index. If none,
     * returns null.
     * 
     * @param index the event index (rank)
     * @return the corresponding event timestamp
     */
    public ITmfTimestamp getTimestamp(final int index) {
        final TmfExperimentContext context = seekEvent(index);
        final ITmfEvent event = getNextEvent(context);
        return (event != null) ? event.getTimestamp() : null;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    /**
     * Update the global time range
     */
    protected void updateTimeRange() {
        ITmfTimestamp startTime = fTimeRange != TmfTimeRange.NULL_RANGE ? fTimeRange.getStartTime()
                : TmfTimestamp.BIG_CRUNCH;
        ITmfTimestamp endTime = fTimeRange != TmfTimeRange.NULL_RANGE ? fTimeRange.getEndTime() : TmfTimestamp.BIG_BANG;

        for (final ITmfTrace<T> trace : fTraces) {
            final ITmfTimestamp traceStartTime = trace.getStartTime();
            if (traceStartTime.compareTo(startTime, true) < 0)
                startTime = traceStartTime;
            final ITmfTimestamp traceEndTime = trace.getEndTime();
            if (traceEndTime.compareTo(endTime, true) > 0)
                endTime = traceEndTime;
        }
        fTimeRange = new TmfTimeRange(startTime, endTime);
    }

    // ------------------------------------------------------------------------
    // TmfProvider
    // ------------------------------------------------------------------------
    @Override
    public ITmfContext armRequest(final ITmfDataRequest<T> request) {
        //		Tracer.trace("Ctx: Arming request - start");
        ITmfTimestamp timestamp = (request instanceof ITmfEventRequest<?>) ? ((ITmfEventRequest<T>) request).getRange().getStartTime() : null;
        if (TmfTimestamp.BIG_BANG.equals(timestamp) || request.getIndex() > 0)
            timestamp = null; // use request index
        TmfExperimentContext context = null;
        if (timestamp != null) {
            // seek by timestamp
            context = seekEvent(timestamp);
            ((ITmfEventRequest<T>) request).setStartIndex((int) context.getRank());
        } else // Seek by rank
            if ((fExperimentContext != null) && fExperimentContext.getRank() == request.getIndex())
                // We are already at the right context -> no need to seek
                context = fExperimentContext;
            else
                context = seekEvent(request.getIndex());
        // Tracer.trace("Ctx: Arming request - done");
        return context;
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getNext(final ITmfContext context) {
        if (context instanceof TmfExperimentContext)
            return (T) getNextEvent(context);
        return null;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace trace positioning
    // ------------------------------------------------------------------------

    // Returns a brand new context based on the location provided
    // and initializes the event queues
    @Override
    public synchronized TmfExperimentContext seekLocation(final ITmfLocation<?> location) {
        // Validate the location
        if (location != null && !(location instanceof TmfExperimentLocation))
            return null; // Throw an exception?

        if (fTraces == null)
            return null;

        // Instantiate the location
        final TmfExperimentLocation expLocation = (location == null) ? new TmfExperimentLocation(new TmfLocationArray(
                new ITmfLocation<?>[fTraces.length]), new long[fTraces.length]) : (TmfExperimentLocation) location.clone();

        // Create and populate the context's traces contexts
        final TmfExperimentContext context = new TmfExperimentContext(fTraces, new ITmfContext[fTraces.length]);
        // Tracer.trace("Ctx: SeekLocation - start");

        long rank = 0;
        for (int i = 0; i < fTraces.length; i++) {
            // Get the relevant trace attributes
            final ITmfLocation<?> traceLocation = expLocation.getLocation().locations[i];
            final long traceRank = expLocation.getRanks()[i];

            // Set the corresponding sub-context
            context.getContexts()[i] = fTraces[i].seekLocation(traceLocation);
            context.getContexts()[i].setRank(traceRank);
            rank += traceRank;

            // Set the trace location and read the corresponding event
            /*
             * The (TmfContext) cast should be safe since we created 'context'
             * ourselves higher up.
             */
            expLocation.getLocation().locations[i] = context.getContexts()[i].getLocation().clone();
            context.getEvents()[i] = fTraces[i].getNextEvent(context.getContexts()[i]);
        }

        // Tracer.trace("Ctx: SeekLocation - done");

        // Finalize context
        context.setLocation(expLocation);
        context.setLastTrace(TmfExperimentContext.NO_TRACE);
        context.setRank(rank);

        fExperimentContext = context;

        return context;
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools .tmf.event.TmfTimestamp)
     */
    @Override
    public synchronized TmfExperimentContext seekEvent(ITmfTimestamp timestamp) {

        //		Tracer.trace("Ctx: seekEvent(TS) - start");

        if (timestamp == null)
            timestamp = TmfTimestamp.BIG_BANG;

        // First, find the right checkpoint
        int index = Collections.binarySearch(fCheckpoints, new TmfCheckpoint(timestamp, null));

        // In the very likely case that the checkpoint was not found, bsearch
        // returns its negated would-be location (not an offset...). From that
        // index, we can then position the stream and get the event.
        if (index < 0)
            index = Math.max(0, -(index + 2));

        // Position the experiment at the checkpoint
        ITmfLocation<?> location;
        synchronized (fCheckpoints) {
            if (fCheckpoints.size() > 0) {
                if (index >= fCheckpoints.size())
                    index = fCheckpoints.size() - 1;
                location = fCheckpoints.elementAt(index).getLocation();
            } else
                location = null;
        }

        final TmfExperimentContext context = seekLocation(location);
        context.setRank((long) index * fIndexPageSize);

        // And locate the event
        ITmfEvent event = parseEvent(context);
        while ((event != null) && (event.getTimestamp().compareTo(timestamp, false) < 0)) {
            getNextEvent(context);
            event = parseEvent(context);
        }

        if (event == null) {
            context.setLocation(null);
            context.setRank(ITmfContext.UNKNOWN_RANK);
        }

        return context;
    }

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(long)
     */
    @Override
    public synchronized TmfExperimentContext seekEvent(final long rank) {

        //		Tracer.trace("Ctx: seekEvent(rank) - start");

        // Position the stream at the previous checkpoint
        int index = (int) rank / fIndexPageSize;
        ITmfLocation<?> location;
        synchronized (fCheckpoints) {
            if (fCheckpoints.size() == 0)
                location = null;
            else {
                if (index >= fCheckpoints.size())
                    index = fCheckpoints.size() - 1;
                location = fCheckpoints.elementAt(index).getLocation();
            }
        }

        final TmfExperimentContext context = seekLocation(location);
        context.setRank((long) index * fIndexPageSize);

        // And locate the event
        ITmfEvent event = parseEvent(context);
        long pos = context.getRank();
        while ((event != null) && (pos++ < rank)) {
            getNextEvent(context);
            event = parseEvent(context);
        }

        if (event == null) {
            context.setLocation(null);
            context.setRank(ITmfContext.UNKNOWN_RANK);
        }

        return context;
    }

    @Override
    public TmfContext seekLocation(final double ratio) {
        final TmfContext context = seekEvent((long) (ratio * getNbEvents()));
        return context;
    }

    @Override
    public double getLocationRatio(final ITmfLocation<?> location) {
        if (location instanceof TmfExperimentLocation)
            return (double) seekLocation(location).getRank() / getNbEvents();
        return 0;
    }

    @Override
    public ITmfLocation<?> getCurrentLocation() {
        if (fExperimentContext != null)
            return fExperimentContext.getLocation();
        return null;
    }

    //	private void dumpContext(TmfExperimentContext context, boolean isBefore) {

    //		TmfContext context0 = context.getContexts()[0];
    //		TmfEvent   event0   = context.getEvents()[0];
    //		TmfExperimentLocation location0 = (TmfExperimentLocation) context.getLocation();
    //		long       rank0    = context.getRank();
    //		int        trace    = context.getLastTrace();
    //
    //		StringBuffer result = new StringBuffer("Ctx: " + (isBefore ? "B " : "A "));
    //
    //		result.append("[Ctx: fLoc= " + context0.getLocation().toString() + ", fRnk= " + context0.getRank() + "] ");
    //		result.append("[Evt: " + event0.getTimestamp().toString() + "] ");
    //		result.append("[Loc: fLoc= " + location0.getLocation()[0].toString() + ", fRnk= " + location0.getRanks()[0] + "] ");
    //		result.append("[Rnk: " + rank0 + "], [Trc: " + trace + "]");
    //		Tracer.trace(result.toString());
    //	}

    /**
     * Scan the next events from all traces and return the next one in
     * chronological order.
     * 
     * @param context the trace context
     * @return the next event
     */
    @Override
    public synchronized ITmfEvent getNextEvent(final ITmfContext context) {

        // Validate the context
        if (!(context instanceof TmfExperimentContext))
            return null; // Throw an exception?

        if (!context.equals(fExperimentContext))
            // Tracer.trace("Ctx: Restoring context");
            fExperimentContext = seekLocation(context.getLocation());

        final TmfExperimentContext expContext = (TmfExperimentContext) context;

        //		dumpContext(expContext, true);

        // If an event was consumed previously, get the next one from that trace
        final int lastTrace = expContext.getLastTrace();
        if (lastTrace != TmfExperimentContext.NO_TRACE) {
            final ITmfContext traceContext = expContext.getContexts()[lastTrace];
            expContext.getEvents()[lastTrace] = expContext.getTraces()[lastTrace].getNextEvent(traceContext);
            expContext.setLastTrace(TmfExperimentContext.NO_TRACE);
        }

        // Scan the candidate events and identify the "next" trace to read from
        final ITmfEvent eventArray[] = expContext.getEvents();
        if (eventArray == null)
            return null;
        int trace = TmfExperimentContext.NO_TRACE;
        ITmfTimestamp timestamp = TmfTimestamp.BIG_CRUNCH;
        if (eventArray.length == 1) {
            if (eventArray[0] != null) {
                timestamp = eventArray[0].getTimestamp();
                trace = 0;
            }
        } else
            for (int i = 0; i < eventArray.length; i++) {
                final ITmfEvent event = eventArray[i];
                if (event != null && event.getTimestamp() != null) {
                    final ITmfTimestamp otherTS = event.getTimestamp();
                    if (otherTS.compareTo(timestamp, true) < 0) {
                        trace = i;
                        timestamp = otherTS;
                    }
                }
            }
        // Update the experiment context and set the "next" event
        ITmfEvent event = null;
        if (trace != TmfExperimentContext.NO_TRACE) {
            updateIndex(expContext, timestamp);

            final ITmfContext traceContext = expContext.getContexts()[trace];
            final TmfExperimentLocation expLocation = (TmfExperimentLocation) expContext.getLocation();
            //	        expLocation.getLocation()[trace] = traceContext.getLocation().clone();
            expLocation.getLocation().locations[trace] = traceContext.getLocation().clone();

            //	        updateIndex(expContext, timestamp);

            expLocation.getRanks()[trace] = traceContext.getRank();
            expContext.setLastTrace(trace);
            expContext.increaseRank();
            event = expContext.getEvents()[trace];
            fExperimentContext = expContext;
        }

        //		if (event != null) {
        //    		Tracer.trace("Exp: " + (expContext.getRank() - 1) + ": " + event.getTimestamp().toString());
        //    		dumpContext(expContext, false);
        //    		Tracer.trace("Ctx: Event returned= " + event.getTimestamp().toString());
        //		}

        return event;
    }

    public synchronized void updateIndex(final ITmfContext context, final ITmfTimestamp timestamp) {
        // Build the index as we go along
        final long rank = context.getRank();
        if (context.hasValidRank() && (rank % fIndexPageSize) == 0) {
            // Determine the table position
            final long position = rank / fIndexPageSize;
            // Add new entry at proper location (if empty)
            if (fCheckpoints.size() == position) {
                final ITmfLocation<?> location = context.getLocation().clone();
                fCheckpoints.add(new TmfCheckpoint(timestamp.clone(), location));
                //                System.out.println(this + "[" + (fCheckpoints.size() - 1) + "] " + timestamp + ", "
                //                        + location.toString());
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#parseEvent(org.eclipse.linuxtools .tmf.trace.TmfContext)
     */
    @Override
    public ITmfEvent parseEvent(final ITmfContext context) {

        // Validate the context
        if (!(context instanceof TmfExperimentContext))
            return null; // Throw an exception?

        if (!context.equals(fExperimentContext))
            // Tracer.trace("Ctx: Restoring context");
            seekLocation(context.getLocation());

        final TmfExperimentContext expContext = (TmfExperimentContext) context;

        // If an event was consumed previously, get the next one from that trace
        final int lastTrace = expContext.getLastTrace();
        if (lastTrace != TmfExperimentContext.NO_TRACE) {
            final ITmfContext traceContext = expContext.getContexts()[lastTrace];
            expContext.getEvents()[lastTrace] = expContext.getTraces()[lastTrace].getNextEvent(traceContext);
            expContext.setLastTrace(TmfExperimentContext.NO_TRACE);
            fExperimentContext = (TmfExperimentContext) context;
        }

        // Scan the candidate events and identify the "next" trace to read from
        int trace = TmfExperimentContext.NO_TRACE;
        ITmfTimestamp timestamp = TmfTimestamp.BIG_CRUNCH;
        for (int i = 0; i < expContext.getTraces().length; i++) {
            final ITmfEvent event = expContext.getEvents()[i];
            if (event != null && event.getTimestamp() != null) {
                final ITmfTimestamp otherTS = event.getTimestamp();
                if (otherTS.compareTo(timestamp, true) < 0) {
                    trace = i;
                    timestamp = otherTS;
                }
            }
        }

        ITmfEvent event = null;
        if (trace != TmfExperimentContext.NO_TRACE)
            event = expContext.getEvents()[trace];

        return event;
    }

    /*
     * (non-Javadoc)
     *
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
            final TmfContext context = seekLocation(null);
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

        final Thread thread = new Thread("Streaming Monitor for experiment " + getName()) { //$NON-NLS-1$

            ITmfTimestamp safeTimestamp = null;
            TmfTimeRange timeRange = null;

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
                                    new TmfExperimentRangeUpdatedSignal(TmfExperiment.this, TmfExperiment.this,
                                            timeRange);
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

    // The index page size
    private static final int DEFAULT_INDEX_PAGE_SIZE = 5000;
    protected int fIndexPageSize;
    protected boolean fIndexing = false;
    protected TmfTimeRange fIndexingPendingRange = TmfTimeRange.NULL_RANGE;

    private Integer fEndSynchReference;

    //	private static BufferedWriter fEventLog = null;
    //	private static BufferedWriter openLogFile(String filename) {
    //		BufferedWriter outfile = null;
    //		try {
    //			outfile = new BufferedWriter(new FileWriter(filename));
    //		} catch (IOException e) {
    //			e.printStackTrace();
    //		}
    //		return outfile;
    //	}

    protected boolean isIndexingBusy() {
        synchronized (fCheckpoints) {
            return fIndexing;
        }
    }

    @Override
    public void indexTrace(final boolean waitForCompletion) {
        if (waitForCompletion)
            initializeStreamingMonitor();
    }

    @SuppressWarnings("unchecked")
    private void indexExperiment(final boolean waitForCompletion, final int index, final TmfTimeRange timeRange) {

        synchronized (fCheckpoints) {
            if (fIndexing)
                return;
            fIndexing = true;
        }

        final Job job = new Job("Indexing " + getName() + "...") { //$NON-NLS-1$ //$NON-NLS-2$

            @Override
            protected IStatus run(final IProgressMonitor monitor) {
                while (!monitor.isCanceled())
                    try {
                        Thread.sleep(100);
                    } catch (final InterruptedException e) {
                        return Status.OK_STATUS;
                    }
                monitor.done();
                return Status.OK_STATUS;
            }
        };
        job.schedule();

        //		fEventLog = openLogFile("TraceEvent.log");
        //        System.out.println(System.currentTimeMillis() + ": Experiment indexing started");

        final ITmfEventRequest<ITmfEvent> request = new TmfEventRequest<ITmfEvent>(ITmfEvent.class, timeRange, index,
                TmfDataRequest.ALL_DATA,
                fIndexPageSize, ITmfDataRequest.ExecutionType.BACKGROUND) { // PATA
            // FOREGROUND

            //            long indexingStart = System.nanoTime();

            ITmfTimestamp startTime = (fTimeRange == TmfTimeRange.NULL_RANGE) ? null : fTimeRange.getStartTime();
            ITmfTimestamp lastTime = (fTimeRange == TmfTimeRange.NULL_RANGE) ? null : fTimeRange.getEndTime();
            long initialNbEvents = fNbEvents;

            @Override
            public void handleStarted() {
                super.handleStarted();
            }

            @Override
            public void handleData(final ITmfEvent event) {
                super.handleData(event);
                if (event != null) {
                    final ITmfTimestamp ts = event.getTimestamp();
                    if (startTime == null)
                        startTime = ts.clone();
                    lastTime = ts.clone();
                    if ((getNbRead() % fIndexPageSize) == 1 && getNbRead() != 1)
                        updateExperiment();
                }
            }

            @Override
            public void handleSuccess() {
                //                long indexingEnd = System.nanoTime();

                // if the end time is a real value then it is the streaming safe
                // time stamp
                // set the last time to the safe time stamp to prevent
                // unnecessary indexing requests
                if (getRange().getEndTime() != TmfTimestamp.BIG_CRUNCH)
                    lastTime = getRange().getEndTime();
                updateExperiment();
                //                System.out.println(System.currentTimeMillis() + ": Experiment indexing completed");

                //                long average = (indexingEnd - indexingStart) / fNbEvents;
                //                System.out.println(getName() + ": start=" + startTime + ", end=" + lastTime + ", elapsed="
                //                        + (indexingEnd * 1.0 - indexingStart) / 1000000000);
                //                System.out.println(getName() + ": nbEvents=" + fNbEvents + " (" + (average / 1000) + "."
                //                        + (average % 1000) + " us/evt)");
                super.handleSuccess();
            }

            @Override
            public void handleCompleted() {
                job.cancel();
                super.handleCompleted();
                synchronized (fCheckpoints) {
                    fIndexing = false;
                    if (fIndexingPendingRange != TmfTimeRange.NULL_RANGE) {
                        indexExperiment(false, (int) fNbEvents, fIndexingPendingRange);
                        fIndexingPendingRange = TmfTimeRange.NULL_RANGE;
                    }
                }
            }

            private void updateExperiment() {
                final int nbRead = getNbRead();
                if (startTime != null)
                    fTimeRange = new TmfTimeRange(startTime, lastTime.clone());
                if (nbRead != 0) {
                    //					updateTimeRange();
                    //					updateNbEvents();
                    fNbEvents = initialNbEvents + nbRead;
                    notifyListeners();
                }
            }
        };

        sendRequest((ITmfDataRequest<T>) request);
        if (waitForCompletion)
            try {
                request.waitForCompletion();
            } catch (final InterruptedException e) {
                e.printStackTrace();
            }
    }

    protected void notifyListeners() {
        broadcast(new TmfExperimentUpdatedSignal(this, this)); // , null));
        // broadcast(new TmfExperimentRangeUpdatedSignal(this, this,
        // fTimeRange)); // , null));
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

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
        if (signal.getExperiment() == this)
            indexExperiment(false, (int) fNbEvents, signal.getRange());
    }

    @TmfSignalHandler
    public void traceUpdated(final TmfTraceUpdatedSignal signal) {
        for (final ITmfTrace<T> trace : fTraces)
            if (trace == signal.getTrace()) {
                synchronized (fCheckpoints) {
                    if (fIndexing) {
                        if (fIndexingPendingRange == TmfTimeRange.NULL_RANGE)
                            fIndexingPendingRange = signal.getRange();
                        else {
                            ITmfTimestamp startTime = fIndexingPendingRange.getStartTime();
                            ITmfTimestamp endTime = fIndexingPendingRange.getEndTime();
                            if (signal.getRange().getStartTime().compareTo(startTime) < 0)
                                startTime = signal.getRange().getStartTime();
                            if (signal.getRange().getEndTime().compareTo(endTime) > 0)
                                endTime = signal.getRange().getEndTime();
                            fIndexingPendingRange = new TmfTimeRange(startTime, endTime);
                        }
                        return;
                    }
                }
                indexExperiment(false, (int) fNbEvents, signal.getRange());
                return;
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

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#setResource(org.eclipse
     * .core.resources.IResource)
     */
    @Override
    public void setResource(final IResource resource) {
        fResource = resource;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getResource()
     */
    @Override
    public IResource getResource() {
        return fResource;
    }
}
