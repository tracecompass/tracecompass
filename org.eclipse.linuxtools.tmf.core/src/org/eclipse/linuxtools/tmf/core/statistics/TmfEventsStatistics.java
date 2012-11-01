/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statistics;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfStatsUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Implementation of ITmfStatistics which uses event requests to the trace to
 * retrieve its information.
 *
 * There is almost no setup time, but queries themselves are longer than with a
 * TmfStateStatistics. Queries are O(n * m), where n is the size of the trace,
 * and m is the portion of the trace covered by the selected interval.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfEventsStatistics implements ITmfStatistics {

    private final ITmfTrace trace;

    /* Event request objects for the time-range request. */
    private StatsTotalRequest totalRequest = null;
    private StatsPerTypeRequest perTypeRequest = null;

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we are building the statistics
     */
    public TmfEventsStatistics(ITmfTrace trace) {
        this.trace = trace;
    }

    @Override
    public void dispose() {
        cancelOngoingRequests();
    }

    @Override
    public void updateStats(final boolean isGlobal, ITmfTimestamp start,
            ITmfTimestamp end) {
        cancelOngoingRequests();

        /*
         * Prepare and send the event requests. This needs to be done in the
         * same thread, since it will be run by TmfStatisticsViewer's signal
         * handlers, to ensure they get correctly coalesced.
         */
        TmfTimeRange range = isGlobal ? TmfTimeRange.ETERNITY : new TmfTimeRange(start, end);
        final StatsTotalRequest totalReq = new StatsTotalRequest(trace, range);
        final StatsPerTypeRequest perTypeReq = new StatsPerTypeRequest(trace, range);

        /*
         * Only allow one time-range request at a time (there should be only one
         * global request at the beginning anyway, no need to track those).
         */
        if (!isGlobal) {
            this.totalRequest = totalReq;
            this.perTypeRequest = perTypeReq;
        }

        trace.sendRequest(totalReq);
        trace.sendRequest(perTypeReq);

        /*
         * This thread can now return. Start a new thread that will wait until
         * the request are done and will then send the results.
         */
        Thread statsThread = new Thread("Statistics update") { //$NON-NLS-1$
            @Override
            public void run() {
                /* Wait for both requests to complete */
                try {
                    totalReq.waitForCompletion();
                    perTypeReq.waitForCompletion();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                /*
                 * If the request was cancelled, this means a newer one was
                 * sent, discard the current one and return without sending
                 * the signal.
                 */
                if (totalReq.isCancelled() || perTypeReq.isCancelled()) {
                    return;
                }

                /* If it completed successfully, retrieve the results. */
                long total = totalReq.getResult();
                Map<String, Long> map = perTypeReq.getResults();

                /* Send the signal to notify the stats viewer to update its display. */
                TmfSignal sig = new TmfStatsUpdatedSignal(this, trace, isGlobal, total, map);
                TmfSignalManager.dispatchSignal(sig);
            }
        };
        statsThread.start();
        return;
    }

    private synchronized void cancelOngoingRequests() {
        if (totalRequest != null && totalRequest.isRunning()) {
            totalRequest.cancel();
        }
        if (perTypeRequest != null && perTypeRequest.isRunning()) {
            perTypeRequest.cancel();
        }
    }

    @Override
    public long getEventsTotal() {
        StatsTotalRequest request = new StatsTotalRequest(trace, TmfTimeRange.ETERNITY);
        sendAndWait(request);

        long total = request.getResult();
        return total;
    }

    @Override
    public Map<String, Long> getEventTypesTotal() {
        StatsPerTypeRequest request = new StatsPerTypeRequest(trace, TmfTimeRange.ETERNITY);
        sendAndWait(request);

        Map<String, Long> stats =  request.getResults();
        return stats;
    }

    @Override
    public long getEventsInRange(ITmfTimestamp start, ITmfTimestamp end) {
        TmfTimeRange range = new TmfTimeRange(start, end);
        StatsTotalRequest request = new StatsTotalRequest(trace, range);
        sendAndWait(request);

        long total =  request.getResult();
        return total;
    }

    @Override
    public Map<String, Long> getEventTypesInRange(ITmfTimestamp start,
            ITmfTimestamp end) {
        TmfTimeRange range = new TmfTimeRange(start, end);
        StatsPerTypeRequest request = new StatsPerTypeRequest(trace, range);
        sendAndWait(request);

        Map<String, Long> stats =  request.getResults();
        return stats;
    }

    private void sendAndWait(TmfEventRequest request) {
        trace.sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * Event request to get the total number of events
     */
    private class StatsTotalRequest extends TmfEventRequest {

        /* Total number of events the request has found */
        private long total;

        public StatsTotalRequest(ITmfTrace trace, TmfTimeRange range) {
            super(trace.getEventType(), range, TmfDataRequest.ALL_DATA,
                    trace.getCacheSize(), ITmfDataRequest.ExecutionType.BACKGROUND);
            total = 0;
        }

        public long getResult() {
            return total;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event != null) {
                if (event.getTrace() == trace) {
                    total += 1;
                }
            }
        }
    }


    /**
     * Event request to get the counts per event type
     */
    private class StatsPerTypeRequest extends TmfEventRequest {

        /* Map in which the results are saved */
        private final Map<String, Long> stats;

        public StatsPerTypeRequest(ITmfTrace trace, TmfTimeRange range) {
            super(trace.getEventType(), range, TmfDataRequest.ALL_DATA,
                    trace.getCacheSize(), ITmfDataRequest.ExecutionType.BACKGROUND);
            this.stats = new HashMap<String, Long>();
        }

        public Map<String, Long> getResults() {
            return stats;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event != null) {
                if (event.getTrace() == trace) {
                    processEvent(event);
                }
            }
        }

        private void processEvent(ITmfEvent event) {
            String eventType = event.getType().getName();
            if (stats.containsKey(eventType)) {
                long curValue = stats.get(eventType);
                stats.put(eventType, curValue + 1L);
            } else {
                stats.put(eventType, 1L);
            }
        }
    }

}
