/*******************************************************************************
 * Copyright (c) 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng.core.trace;

import org.eclipse.linuxtools.lttng.core.event.LttngEvent;
import org.eclipse.linuxtools.lttng.core.event.LttngTimestamp;
import org.eclipse.linuxtools.lttng.core.tracecontrol.utility.LiveTraceManager;
import org.eclipse.linuxtools.lttng.jni.JniTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperimentContext;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperimentLocation;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfExperimentRangeUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;

/**
 * <b><u>LTTngExperiment</u></b>
 * <p>
 * Temporary class to resolve a basic incompatibility between TMF and LTTng.
 * <p>
 */
public class LTTngExperiment<T extends TmfEvent> extends TmfExperiment<T> {

    private static final int DEFAULT_INDEX_PAGE_SIZE = 50000;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param type
     * @param id
     * @param traces
     * @param epoch
     * @param indexPageSize
     */
    public LTTngExperiment(Class<T> type, String id, ITmfTrace<T>[] traces, ITmfTimestamp epoch, int indexPageSize) {
        this(type, id, traces, TmfTimestamp.Zero, indexPageSize, false);
    }

    public LTTngExperiment(Class<T> type, String id, ITmfTrace<T>[] traces, ITmfTimestamp epoch, int indexPageSize, boolean preIndexExperiment) {
        super(type, id, traces, epoch, indexPageSize, preIndexExperiment);
    }

    /**
     * @param type
     * @param id
     * @param traces
     */
    public LTTngExperiment(Class<T> type, String id, ITmfTrace<T>[] traces) {
        this(type, id, traces, TmfTimestamp.Zero, DEFAULT_INDEX_PAGE_SIZE);
    }

    /**
     * @param type
     * @param id
     * @param traces
     * @param indexPageSize
     */
    public LTTngExperiment(Class<T> type, String id, ITmfTrace<T>[] traces, int indexPageSize) {
        this(type, id, traces, TmfTimestamp.Zero, indexPageSize);
    }

    @SuppressWarnings("unchecked")
    public LTTngExperiment(LTTngExperiment<T> other) {
        super(other.getName() + "(clone)", other.fType); //$NON-NLS-1$

        fEpoch = other.fEpoch;
        fIndexPageSize = other.fIndexPageSize;

        fTraces = new ITmfTrace[other.fTraces.length];
        for (int trace = 0; trace < other.fTraces.length; trace++) {
    		fTraces[trace] = other.fTraces[trace].copy();
        }

        fNbEvents = other.fNbEvents;
        fTimeRange = other.fTimeRange;
    }

    @Override
	public LTTngExperiment<T> copy() {
        LTTngExperiment<T> experiment = new LTTngExperiment<T>(this);
        TmfSignalManager.deregister(experiment);
        return experiment;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace trace positioning
    // ------------------------------------------------------------------------

    @Override
    public synchronized TmfEvent getNextEvent(TmfContext context) {

        // Validate the context
        if (!(context instanceof TmfExperimentContext)) {
            return null; // Throw an exception?
        }

        if (!context.equals(fExperimentContext)) {
//    		Tracer.trace("Ctx: Restoring context");
            fExperimentContext = seekLocation(context.getLocation());
        }

        TmfExperimentContext expContext = (TmfExperimentContext) context;

//		dumpContext(expContext, true);

        // If an event was consumed previously, get the next one from that trace
        int lastTrace = expContext.getLastTrace();
        if (lastTrace != TmfExperimentContext.NO_TRACE) {
            TmfContext traceContext = expContext.getContexts()[lastTrace];
            expContext.getEvents()[lastTrace] = expContext.getTraces()[lastTrace].getNextEvent(traceContext);
            expContext.setLastTrace(TmfExperimentContext.NO_TRACE);
        }

        // Scan the candidate events and identify the "next" trace to read from
        TmfEvent eventArray[] = expContext.getEvents();
        if (eventArray == null) {
            return null;
        }
        int trace = TmfExperimentContext.NO_TRACE;
        ITmfTimestamp timestamp = TmfTimestamp.BigCrunch;
        if (eventArray.length == 1) {
            if (eventArray[0] != null) {
                timestamp = eventArray[0].getTimestamp();
                trace = 0;
            }
        } else {
            for (int i = 0; i < eventArray.length; i++) {
                TmfEvent event = eventArray[i];
                if (event != null && event.getTimestamp() != null) {
                    ITmfTimestamp otherTS = event.getTimestamp();
                    if (otherTS.compareTo(timestamp, true) < 0) {
                        trace = i;
                        timestamp = otherTS;
                    }
                }
            }
        }

        // Update the experiment context and set the "next" event
        TmfEvent event = null;
        if (trace != TmfExperimentContext.NO_TRACE) {
//	        updateIndex(expContext, timestamp);

            TmfContext traceContext = expContext.getContexts()[trace];
            TmfExperimentLocation expLocation = (TmfExperimentLocation) expContext.getLocation();
            expLocation.getLocation().locations[trace] = traceContext.getLocation();

            updateIndex(expContext, timestamp);

            expLocation.getRanks()[trace] = traceContext.getRank();
            expContext.setLastTrace(trace);
            expContext.updateRank(1);
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

    @SuppressWarnings("unchecked")
    @Override
    protected void indexExperiment(final boolean waitForCompletion) {
        if (waitForCompletion) {
            TmfExperimentRangeUpdatedSignal signal = new TmfExperimentRangeUpdatedSignal(LTTngExperiment.this, LTTngExperiment.this,
                    TmfTimeRange.Eternity);
            broadcast(signal);
            while (isIndexingBusy()) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            ;
            return;
        }
        for (ITmfTrace<?> trace : fTraces) {
            if (trace instanceof LTTngTrace) {
                JniTrace jniTrace = ((LTTngTrace) trace).getCurrentJniTrace();
                if (jniTrace != null && (!jniTrace.isLiveTraceSupported() || !LiveTraceManager.isLiveTrace(jniTrace.getTracepath()))) {
                    updateTimeRange();
                    TmfExperimentRangeUpdatedSignal signal = new TmfExperimentRangeUpdatedSignal(LTTngExperiment.this, LTTngExperiment.this,
                            getTimeRange());
                    broadcast(signal);
                    return;
                }
            }
        }
        final Thread thread = new Thread("Streaming Monitor for " + getName()) { //$NON-NLS-1$
            LttngTimestamp safeTimestamp = null;
            TmfTimeRange timeRange = null;

            @Override
            public void run() {
                while (!fExecutor.isShutdown()) {
                    final TmfEventRequest<LttngEvent> request = new TmfEventRequest<LttngEvent>(LttngEvent.class, TmfTimeRange.Eternity, 0,
                            ExecutionType.FOREGROUND) {
                        @Override
                        public void handleCompleted() {
                            super.handleCompleted();
                            if (isIndexingBusy()) {
                                timeRange = null;
                                return;
                            }
                            long startTime = Long.MAX_VALUE;
                            long endTime = Long.MIN_VALUE;
                            for (ITmfTrace<?> trace : getTraces()) {
                                if (trace instanceof LTTngTrace) {
                                    LTTngTrace lttngTrace = (LTTngTrace) trace;
                                    JniTrace jniTrace = lttngTrace.getCurrentJniTrace();
                                    jniTrace.updateTrace();
                                    startTime = Math.min(startTime, lttngTrace.getStartTime().getValue());
                                    endTime = Math.max(endTime, jniTrace.getEndTime().getTime());
                                }
                            }
                            LttngTimestamp startTimestamp = new LttngTimestamp(startTime);
                            LttngTimestamp endTimestamp = new LttngTimestamp(endTime);
                            if (safeTimestamp != null && safeTimestamp.compareTo(getTimeRange().getEndTime(), false) > 0) {
                                timeRange = new TmfTimeRange(startTimestamp, safeTimestamp);
                            } else {
                                timeRange = null;
                            }
                            safeTimestamp = endTimestamp;
                        }
                    };
                    try {
                        sendRequest((ITmfDataRequest<T>) request);
                        request.waitForCompletion();
                        if (timeRange != null && !timeRange.equals(TmfTimeRange.Null)) {
                            TmfExperimentRangeUpdatedSignal signal = new TmfExperimentRangeUpdatedSignal(LTTngExperiment.this, LTTngExperiment.this,
                                    timeRange);
                            broadcast(signal);
                        }
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        thread.start();
    }

    @TmfSignalHandler
    public void experimentRangeUpdated(TmfExperimentRangeUpdatedSignal signal) {
        indexExperiment(false, (int) fNbEvents, signal.getRange());
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[LTTngExperiment (" + getName() + ")]";
    }

}
