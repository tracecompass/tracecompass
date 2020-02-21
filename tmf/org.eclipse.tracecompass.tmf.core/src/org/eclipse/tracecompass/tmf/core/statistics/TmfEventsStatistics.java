/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfLostEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Implementation of ITmfStatistics which uses event requests to the trace to
 * retrieve its information.
 *
 * There is almost no setup time, but queries themselves are longer than with a
 * TmfStateStatistics. Queries are O(n * m), where n is the size of the trace,
 * and m is the portion of the trace covered by the selected interval.
 *
 * @author Alexandre Montplaisir
 * @deprecated use {@link TmfStateStatistics} instead
 */
@Deprecated
public class TmfEventsStatistics implements ITmfStatistics {

    /* All timestamps should be stored in nanoseconds in the statistics backend */
    private static final int SCALE = ITmfTimestamp.NANOSECOND_SCALE;

    private final ITmfTrace fTrace;

    /* Event request objects for the time-range request. */
    private StatsTotalRequest fTotalRequest = null;
    private StatsPerTypeRequest fPerTypeRequest = null;

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we are building the statistics
     */
    public TmfEventsStatistics(ITmfTrace trace) {
        fTrace = trace;
    }

    @Override
    public void dispose() {
        cancelOngoingRequests();
    }

    @Override
    public List<@NonNull Long> histogramQuery(long[] timeRequested) {
        HistogramQueryRequest req = new HistogramQueryRequest(timeRequested, timeRequested[timeRequested.length - 1]);
        sendAndWait(req);

        return new ArrayList<>(req.getResults());
    }

    private void cancelOngoingRequests() {
        killTotalRequestAndReplace(null);
        killPerTypeRequestAndReplace(null);
    }

    private synchronized void killPerTypeRequestAndReplace(StatsPerTypeRequest request) {
        if (fPerTypeRequest != null && fPerTypeRequest.isRunning()) {
            fPerTypeRequest.cancel();
        }
        fPerTypeRequest = request;
    }

    private synchronized void killTotalRequestAndReplace(StatsTotalRequest request ) {
        if (fTotalRequest != null && fTotalRequest.isRunning()) {
            fTotalRequest.cancel();
        }
        fTotalRequest = request;
    }

    @Override
    public long getEventsTotal() {
        StatsTotalRequest request = new StatsTotalRequest(fTrace, TmfTimeRange.ETERNITY);
        killTotalRequestAndReplace(request);
        sendAndWait(request);

        return request.getResult();
    }

    @Override
    public Map<@NonNull String, @NonNull Long> getEventTypesTotal() {
        StatsPerTypeRequest request = new StatsPerTypeRequest(fTrace, TmfTimeRange.ETERNITY);
        killPerTypeRequestAndReplace(request);
        sendAndWait(request);

        return request.getResults();
    }

    @Override
    public long getEventsInRange(long start, long end) {
        ITmfTimestamp startTS = TmfTimestamp.create(start, SCALE);
        ITmfTimestamp endTS = TmfTimestamp.create(end, SCALE);
        TmfTimeRange range = new TmfTimeRange(startTS, endTS);

        StatsTotalRequest request = new StatsTotalRequest(fTrace, range);
        killTotalRequestAndReplace(request);
        sendAndWait(request);

        return request.getResult();
    }

    @Override
    public Map<String, Long> getEventTypesInRange(long start, long end) {
        ITmfTimestamp startTS = TmfTimestamp.create(start, SCALE);
        ITmfTimestamp endTS = TmfTimestamp.create(end, SCALE);
        TmfTimeRange range = new TmfTimeRange(startTS, endTS);
        StatsPerTypeRequest request = new StatsPerTypeRequest(fTrace, range);
        killPerTypeRequestAndReplace(request);
        sendAndWait(request);

        return request.getResults();
    }

    private void sendAndWait(TmfEventRequest request) {
        fTrace.sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }


    /**
     * Event request to get the total number of events
     */
    private class StatsTotalRequest extends TmfEventRequest {

        /* Total number of events the request has found */
        private long total;

        public StatsTotalRequest(ITmfTrace trace, TmfTimeRange range) {
            super(trace.getEventType(), range, 0, ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            total = 0;
        }

        public long getResult() {
            return total;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (!(event instanceof ITmfLostEvent) && Objects.equals(event.getTrace(), fTrace)) {
                total += 1;
            }
        }
    }


    /**
     * Event request to get the counts per event type
     */
    private class StatsPerTypeRequest extends TmfEventRequest {

        /* Map in which the results are saved */
        private final Map<@NonNull String, @NonNull Long> stats;

        public StatsPerTypeRequest(ITmfTrace trace, TmfTimeRange range) {
            super(trace.getEventType(), range, 0, ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);
            this.stats = new HashMap<>();
        }

        public Map<@NonNull String, @NonNull Long> getResults() {
            return stats;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            if (event.getTrace() == fTrace) {
                String eventType = event.getName();
                /*
                 * Special handling for lost events: instead of counting just
                 * one, we will count how many actual events it represents.
                 */
                if (event instanceof ITmfLostEvent) {
                    ITmfLostEvent le = (ITmfLostEvent) event;
                    incrementStats(eventType, le.getNbLostEvents());
                    return;
                }

                /* For standard event types, just increment by one */
                incrementStats(eventType, 1L);
            }
        }

        private void incrementStats(@NonNull String key, long count) {
            stats.merge(key, count, Long::sum);
        }
    }

    /**
     * Event request for histogram queries. It is much faster to do one event
     * request then set the results accordingly than doing thousands of them one
     * by one.
     */
    private class HistogramQueryRequest extends TmfEventRequest {

        /** Map of <borders, number of events> */
        private final TreeMap<Long, @NonNull Long> results;

        /**
         * New histogram request
         *
         * @param borders
         *            The array of borders (not including the end time). The
         *            first element should be the start time of the queries.
         * @param endTime
         *            The end time of the query. Not used in the results map,
         *            but we need to know when to stop the event request.
         */
        public HistogramQueryRequest(long[] borders, long endTime) {
            super(fTrace.getEventType(),
                    new TmfTimeRange(
                            TmfTimestamp.create(borders[0], SCALE),
                            TmfTimestamp.create(endTime, SCALE)),
                    0,
                    ITmfEventRequest.ALL_DATA,
                    ITmfEventRequest.ExecutionType.BACKGROUND);

            /* Prepare the results map, with all counts at 0 */
            results = new TreeMap<>();
            for (long border : borders) {
                results.put(border, 0L);
            }
        }

        public Collection<@NonNull Long> getResults() {
            return results.values();
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            if (Objects.equals(event.getTrace(), fTrace)) {
                long ts = event.getTimestamp().toNanos();
                Long key = results.ceilingKey(ts);
                if (key != null) {
                    results.merge(key, 1L, Long::sum);
                }
            }
        }
    }

}
