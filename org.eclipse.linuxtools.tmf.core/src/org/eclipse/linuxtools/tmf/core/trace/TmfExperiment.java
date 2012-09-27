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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentContext;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentLocation;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfLocationArray;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfClearExperimentSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentDisposedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceUpdatedSignal;

/**
 * TmfExperiment presents a time-ordered, unified view of a set of ITmfTrace:s
 * that are part of a tracing experiment.
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfExperiment extends TmfTrace implements ITmfEventParser {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The default index page size
     */
    public static final int DEFAULT_INDEX_PAGE_SIZE = 5000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The currently selected experiment (null if none)
     */
    protected static TmfExperiment fCurrentExperiment = null;

    /**
     * The set of traces that constitute the experiment
     */
    protected ITmfTrace[] fTraces;

    /**
     * The set of traces that constitute the experiment
     */
    private boolean fInitialized = false;

    /**
     * The experiment bookmarks file
     */
    private IFile fBookmarksFile;


    // Saved experiment context (optimization)
    private TmfExperimentContext fExperimentContext;

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * @param type the event type
     * @param id the experiment id
     * @param traces the experiment set of traces
     */
    public TmfExperiment(final Class<? extends ITmfEvent> type, final String id, final ITmfTrace[] traces) {
        this(type, id, traces, DEFAULT_INDEX_PAGE_SIZE);
    }

    /**
     * @param type the event type
     * @param path the experiment path
     * @param traces the experiment set of traces
     * @param indexPageSize the experiment index page size
     */
    public TmfExperiment(final Class<? extends ITmfEvent> type, final String path, final ITmfTrace[] traces, final int indexPageSize) {
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
    }

    /**
     * Clears the experiment
     */
    @Override
    public synchronized void dispose() {

        final TmfExperimentDisposedSignal signal = new TmfExperimentDisposedSignal(this, this);
        broadcast(signal);

        if (fCurrentExperiment == this) {
            fCurrentExperiment = null;
        }

        // Clean up the index if applicable
        if (getIndexer() != null) {
            getIndexer().dispose();
        }

        if (fTraces != null) {
            for (final ITmfTrace trace : fTraces) {
                trace.dispose();
            }
            fTraces = null;
        }
        super.dispose();
    }

    /**
     * @param signal the clear view signal
     * @since 2.0
     */
    @TmfSignalHandler
    public void handleClearExperimentSignal(TmfClearExperimentSignal signal) {
        dispose();
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - Initializers
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfTrace#initTrace(org.eclipse.core.resources.IResource, java.lang.String, java.lang.Class)
     */
    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> type) {
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#validate(org.eclipse.core.resources.IProject, java.lang.String)
     */
    @Override
    public boolean validate(final IProject project, final String path) {
        return true;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Selects the current, framework-wide, experiment
     *
     * @param experiment das experiment
     */
    public static void setCurrentExperiment(final TmfExperiment experiment) {
        if (fCurrentExperiment != null && fCurrentExperiment != experiment) {
            fCurrentExperiment.dispose();
        }
        fCurrentExperiment = experiment;
    }

    /**
     * @return das experiment
     */
    public static TmfExperiment getCurrentExperiment() {
        return fCurrentExperiment;
    }

    /**
     * Get the list of traces. Handle with care...
     *
     * @return the experiment traces
     */
    public ITmfTrace[] getTraces() {
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

    // ------------------------------------------------------------------------
    // Request management
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfTrace#armRequest(org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest)
     */
    @Override
    protected synchronized ITmfContext armRequest(final ITmfDataRequest request) {

        // Make sure we have something to read from
        if (fTraces == null) {
            return null;
        }

        if (request instanceof ITmfEventRequest
            && !TmfTimestamp.BIG_BANG.equals(((ITmfEventRequest) request).getRange().getStartTime())
            && request.getIndex() == 0)
        {
            final ITmfContext context = seekEvent(((ITmfEventRequest) request).getRange().getStartTime());
            ((ITmfEventRequest) request).setStartIndex((int) context.getRank());
            return context;

        }

        // Check if we are already at the right index
        if ((fExperimentContext != null) && fExperimentContext.getRank() == request.getIndex()) {
            return fExperimentContext;
        }

        return seekEvent(request.getIndex());
    }

    // ------------------------------------------------------------------------
    // ITmfTrace trace positioning
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     *
     * Returns a brand new context based on the location provided and
     * initializes the event queues
     *
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.core.trace.ITmfLocation)
     */
    @Override
    public synchronized ITmfContext seekEvent(final ITmfLocation location) {
        // Validate the location
        if (location != null && !(location instanceof TmfExperimentLocation)) {
            return null; // Throw an exception?
        }
        // Make sure we have something to read from
        if (fTraces == null) {
            return null;
        }

        // Instantiate the location
        final TmfExperimentLocation expLocation = (location == null)
                ? new TmfExperimentLocation(new TmfLocationArray(new ITmfLocation[fTraces.length]))
                : (TmfExperimentLocation) location.clone();

        // Create and populate the context's traces contexts
        final TmfExperimentContext context = new TmfExperimentContext(new ITmfContext[fTraces.length]);

        for (int i = 0; i < fTraces.length; i++) {
            // Get the relevant trace attributes
            final ITmfLocation trcLocation = expLocation.getLocationInfo().getLocations()[i];
            context.getContexts()[i] = fTraces[i].seekEvent(trcLocation);
            expLocation.getLocationInfo().getLocations()[i] = context.getContexts()[i].getLocation().clone();
            context.getEvents()[i] = fTraces[i].getNext(context.getContexts()[i]);
        }

        // Finalize context
        context.setLocation(expLocation);
        context.setLastTrace(TmfExperimentContext.NO_TRACE);
        context.setRank((location == null) ? 0 : ITmfContext.UNKNOWN_RANK);

        fExperimentContext = context;
        return context;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - SeekEvent operations (returning a trace context)
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#seekEvent(double)
     */
    @Override
    public ITmfContext seekEvent(final double ratio) {
        final ITmfContext context = seekEvent((long) (ratio * getNbEvents()));
        return context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getLocationRatio(org.eclipse.linuxtools.tmf.core.trace.ITmfLocation)
     */
    @Override
    public double getLocationRatio(final ITmfLocation location) {
        if (location instanceof TmfExperimentLocation) {
            return (double) seekEvent(location).getRank() / getNbEvents();
        }
        return 0.0;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfTrace#getCurrentLocation()
     */
    @Override
    public ITmfLocation getCurrentLocation() {
        ITmfLocation[] locations = new ITmfLocation[fTraces.length];
        for (int i = 0; i < fTraces.length; i++) {
            locations[i] = fTraces[i].getCurrentLocation();
        }
        return new TmfExperimentLocation(new TmfLocationArray(locations));
    }

    // ------------------------------------------------------------------------
    // ITmfTrace trace positioning
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfEventParser#parseEvent(org.eclipse.linuxtools.tmf.core.trace.ITmfContext)
     */
    @Override
    public synchronized ITmfEvent parseEvent(final ITmfContext context) {
        final ITmfContext savedContext = context.clone();
        final ITmfEvent event = getNext(savedContext);
        return event;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.TmfTrace#getNext(org.eclipse.linuxtools.tmf.core.trace.ITmfContext)
     */
    @Override
    public synchronized ITmfEvent getNext(ITmfContext context) {

        // Validate the context
        if (!(context instanceof TmfExperimentContext)) {
            return null; // Throw an exception?
        }

        // Make sure that we have something to read from
        if (fTraces == null) {
            return null;
        }

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

        ITmfEvent event = null;
        if (trace != TmfExperimentContext.NO_TRACE) {
            event = expContext.getEvents()[trace];
            if (event != null) {
                updateAttributes(expContext, event.getTimestamp());
                expContext.increaseRank();
                expContext.setLastTrace(trace);
                final ITmfContext traceContext = expContext.getContexts()[trace];

                TmfExperimentLocation location = (TmfExperimentLocation) expContext.getLocation();
                if (location != null) {
                    location.getLocationInfo().getLocations()[trace] = traceContext.getLocation().clone();
                }

                fExperimentContext = expContext.clone();
                processEvent(event);
            }
        }

        return event;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public synchronized String toString() {
        return "[TmfExperiment (" + getName() + ")]";
    }

    // ------------------------------------------------------------------------
    // Streaming support
    // ------------------------------------------------------------------------

    private synchronized void initializeStreamingMonitor() {

        if (fInitialized) {
            return;
        }
        fInitialized = true;

        if (getStreamingInterval() == 0) {
            final ITmfContext context = seekEvent(0);
            final ITmfEvent event = getNext(context);
            if (event == null) {
                return;
            }
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
            private ITmfTimestamp safeTimestamp = null;
            private ITmfTimestamp lastSafeTimestamp = null;
            private TmfTimeRange timeRange = null;

            @Override
            public void run() {
                while (!executorIsShutdown()) {
                    if (!getIndexer().isIndexing()) {
                        ITmfTimestamp startTimestamp = TmfTimestamp.BIG_CRUNCH;
                        ITmfTimestamp endTimestamp = TmfTimestamp.BIG_BANG;
                        for (final ITmfTrace trace : fTraces) {
                            if (trace.getStartTime().compareTo(startTimestamp) < 0) {
                                startTimestamp = trace.getStartTime();
                            }
                            if (trace.getStreamingInterval() != 0 && trace.getEndTime().compareTo(endTimestamp) > 0) {
                                endTimestamp = trace.getEndTime();
                            }
                        }
                        if (safeTimestamp != null && (lastSafeTimestamp == null || safeTimestamp.compareTo(lastSafeTimestamp, false) > 0)) {
                            timeRange = new TmfTimeRange(startTimestamp, safeTimestamp);
                            lastSafeTimestamp = safeTimestamp;
                        } else {
                            timeRange = null;
                        }
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

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getStreamingInterval()
     */
    @Override
    public long getStreamingInterval() {
        long interval = 0;
        for (final ITmfTrace trace : fTraces) {
            interval = Math.max(interval, trace.getStreamingInterval());
        }
        return interval;
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    private Integer fEndSynchReference;

    /**
     * Signal handler for the TmfExperimentSelectedSignal signal
     *
     * @param signal The incoming signal
     */
    @TmfSignalHandler
    public void experimentSelected(final TmfExperimentSelectedSignal signal) {
        final TmfExperiment experiment = signal.getExperiment();
        if (experiment == this) {
            setCurrentExperiment(experiment);
            fEndSynchReference = Integer.valueOf(signal.getReference());
        }
    }

    /**
     * Signal handler for the TmfEndSynchSignal signal
     *
     * @param signal The incoming signal
     */
    @TmfSignalHandler
    public void endSync(final TmfEndSynchSignal signal) {
        if (fEndSynchReference != null && fEndSynchReference.intValue() == signal.getReference()) {
            fEndSynchReference = null;
            initializeStreamingMonitor();
        }
    }

    /**
     * Signal handler for the TmfTraceUpdatedSignal signal
     *
     * @param signal The incoming signal
     */
    @TmfSignalHandler
    public void traceUpdated(final TmfTraceUpdatedSignal signal) {
        if (signal.getTrace() == this) {
            broadcast(new TmfExperimentUpdatedSignal(this, this));
        }
    }

    /**
     * Signal handler for the TmfExperimentRangeUpdatedSignal signal
     *
     * @param signal The incoming signal
     */
    @TmfSignalHandler
    public void experimentRangeUpdated(final TmfExperimentRangeUpdatedSignal signal) {
        if (signal.getExperiment() == this) {
            getIndexer().buildIndex(getNbEvents(), signal.getRange(), false);
        }
    }

}
