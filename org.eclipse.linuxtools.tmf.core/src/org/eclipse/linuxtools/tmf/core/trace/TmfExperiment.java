/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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
 *   Patrick Tasse - Updated for ranks in experiment location
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentContext;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfExperimentLocation;
import org.eclipse.linuxtools.internal.tmf.core.trace.TmfLocationArray;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;

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

    // ------------------------------------------------------------------------
    // Construction
    // ------------------------------------------------------------------------

    /**
     * @param type the event type
     * @param id the experiment id
     * @param traces the experiment set of traces
     */
    public TmfExperiment(final Class<? extends ITmfEvent> type, final String id, final ITmfTrace[] traces) {
        this(type, id, traces, DEFAULT_INDEX_PAGE_SIZE, null);
    }

    /**
     * Constructor of experiment taking type, path, traces and resource
     *
     * @param type
     *            the event type
     * @param id
     *            the experiment id
     * @param traces
     *            the experiment set of traces
     * @param resource
     *            the resource associated to the experiment
     */
    public TmfExperiment(final Class<? extends ITmfEvent> type, final String id, final ITmfTrace[] traces, IResource resource) {
        this(type, id, traces, DEFAULT_INDEX_PAGE_SIZE, resource);
    }


    /**
     * @param type the event type
     * @param path the experiment path
     * @param traces the experiment set of traces
     * @param indexPageSize the experiment index page size
     */
    public TmfExperiment(final Class<? extends ITmfEvent> type, final String path, final ITmfTrace[] traces, final int indexPageSize) {
        this(type, path, traces, indexPageSize, null);
    }

    /**
     * Full constructor of an experiment, taking the type, path, traces,
     * indexPageSize and resource
     *
     * @param type
     *            the event type
     * @param path
     *            the experiment path
     * @param traces
     *            the experiment set of traces
     * @param indexPageSize
     *            the experiment index page size
     * @param resource
     *            the resource associated to the experiment
     */
    public TmfExperiment(final Class<? extends ITmfEvent> type, final String path, final ITmfTrace[] traces, final int indexPageSize, IResource resource) {
        setCacheSize(indexPageSize);
        setStreamingInterval(0);
        setIndexer(new TmfCheckpointIndexer(this, indexPageSize));
        setParser(this);
        try {
            super.initialize(resource, path, type);
        } catch (TmfTraceException e) {
            e.printStackTrace();
        }

        fTraces = traces;
    }

    /**
     * Clears the experiment
     */
    @Override
    public synchronized void dispose() {

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

    // ------------------------------------------------------------------------
    // ITmfTrace - Initializers
    // ------------------------------------------------------------------------

    @Override
    public void initTrace(final IResource resource, final String path, final Class<? extends ITmfEvent> type) {
    }

    /**
     * @since 2.0
     */
    @Override
    public IStatus validate(final IProject project, final String path) {
        return Status.OK_STATUS;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get the traces contained in this experiment.
     *
     * @return The array of contained traces
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
     * @since 2.0
     */
    public ITmfTimestamp getTimestamp(final int index) {
        final ITmfContext context = seekEvent(index);
        final ITmfEvent event = getNext(context);
        context.dispose();
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

    /**
     * @since 2.0
     */
    @Override
    public synchronized ITmfContext armRequest(final ITmfDataRequest request) {

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

        return seekEvent(request.getIndex());
    }

    // ------------------------------------------------------------------------
    // ITmfTrace trace positioning
    // ------------------------------------------------------------------------

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

        // Initialize the location array if necessary
        TmfLocationArray locationArray = ((location == null) ?
                new TmfLocationArray(fTraces.length) :
                ((TmfExperimentLocation) location).getLocationInfo());

        ITmfLocation[] locations = locationArray.getLocations();
        long[] ranks = locationArray.getRanks();

        // Create and populate the context's traces contexts
        final TmfExperimentContext context = new TmfExperimentContext(fTraces.length);

        // Position the traces
        long rank = 0;
        for (int i = 0; i < fTraces.length; i++) {
            // Get the relevant trace attributes
            final ITmfContext traceContext = fTraces[i].seekEvent(locations[i]);
            context.getContexts()[i] = traceContext;
            traceContext.setRank(ranks[i]);
            locations[i] = traceContext.getLocation(); // update location after seek
            context.getEvents()[i] = fTraces[i].getNext(traceContext);
            rank += ranks[i];
        }

        // Finalize context
        context.setLocation(new TmfExperimentLocation(new TmfLocationArray(locations, ranks)));
        context.setLastTrace(TmfExperimentContext.NO_TRACE);
        context.setRank(rank);

        return context;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace - SeekEvent operations (returning a trace context)
    // ------------------------------------------------------------------------

    @Override
    public ITmfContext seekEvent(final double ratio) {
        final ITmfContext context = seekEvent(Math.round(ratio * getNbEvents()));
        return context;
    }

    @Override
    public double getLocationRatio(final ITmfLocation location) {
        if (location instanceof TmfExperimentLocation) {
            long rank = 0;
            TmfLocationArray locationArray = ((TmfExperimentLocation) location).getLocationInfo();
            for (int i = 0; i < locationArray.size(); i++) {
                rank += locationArray.getRank(i);
            }
            return (double) rank / getNbEvents();
        }
        return 0.0;
    }

    @Override
    public ITmfLocation getCurrentLocation() {
        // never used
        return null;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace trace positioning
    // ------------------------------------------------------------------------

    @Override
    public synchronized ITmfEvent parseEvent(final ITmfContext context) {
        final ITmfContext tmpContext = seekEvent(context.getLocation());
        final ITmfEvent event = getNext(tmpContext);
        return event;
    }

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

                // Update the experiment location
                TmfLocationArray locationArray = new TmfLocationArray(
                        ((TmfExperimentLocation) expContext.getLocation()).getLocationInfo(),
                        trace, traceContext.getLocation(), traceContext.getRank());
                expContext.setLocation(new TmfExperimentLocation(locationArray));

                processEvent(event);
            }
        }

        return event;
    }

    /**
     * @since 2.0
     */
    @Override
    public ITmfTimestamp getInitialRangeOffset() {
        if ((fTraces == null) || (fTraces.length == 0)) {
            return super.getInitialRangeOffset();
        }

        ITmfTimestamp initTs = TmfTimestamp.BIG_CRUNCH;
        for (int i = 0; i < fTraces.length; i++) {
            ITmfTimestamp ts = fTraces[i].getInitialRangeOffset();
            if (ts.compareTo(initTs) < 0) {
                initTs = ts;
            }
        }
        return initTs;
    }

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
            context.dispose();
            if (event == null) {
                return;
            }
            final TmfTimeRange timeRange = new TmfTimeRange(event.getTimestamp(), TmfTimestamp.BIG_CRUNCH);
            final TmfTraceRangeUpdatedSignal signal = new TmfTraceRangeUpdatedSignal(this, this, timeRange);

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
                            final TmfTraceRangeUpdatedSignal signal =
                                    new TmfTraceRangeUpdatedSignal(TmfExperiment.this, TmfExperiment.this, timeRange);
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

    @Override
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        if (signal.getTrace() == this) {
            initializeStreamingMonitor();
        }
    }

}
