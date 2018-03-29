/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Fix TimeRangeException
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.core.statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;

/**
 * Implementation of ITmfStatistics which uses a state history for storing its
 * information. In reality, it uses two state histories, one for "event totals"
 * information (which should ideally use a fast backend), and another one for
 * the rest (per event type, per CPU, etc.).
 *
 * Compared to the event-request-based statistics calculations, it adds the
 * building the history first, but gives much faster response times once built :
 * Queries are O(log n) wrt the size of the trace, and O(1) wrt to the size of
 * the time interval selected.
 *
 * @author Alexandre Montplaisir
 */
public class TmfStateStatistics implements ITmfStatistics {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** The event totals state system */
    private final ITmfStateSystem fTotalsStats;

    /** The state system for event types */
    private final ITmfStateSystem fTypesStats;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param totals
     *            The state system containing the "totals" information
     * @param eventTypes
     *            The state system containing the "event types" information
     */
    public TmfStateStatistics(@NonNull ITmfStateSystem totals, @NonNull ITmfStateSystem eventTypes) {
        fTotalsStats = totals;
        fTypesStats = eventTypes;
    }

    /**
     * Return the state system containing the "totals" values
     *
     * @return The "totals" state system
     */
    public ITmfStateSystem getTotalsSS() {
        return fTotalsStats;
    }

    /**
     * Return the state system containing the "event types" values
     *
     * @return The "event types" state system
     */
    public ITmfStateSystem getEventTypesSS() {
        return fTypesStats;
    }

    // ------------------------------------------------------------------------
    // ITmfStatistics
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        fTotalsStats.dispose();
        fTypesStats.dispose();
    }

    @Override
    public List<@NonNull Long> histogramQuery(final long start, final long end, final int nb) {
        final List<@NonNull Long> list = new ArrayList<>();
        final long increment = (end - start) / nb;

        if (fTotalsStats.isCancelled()) {
            return list;
        }

        /*
         * We will do one state system query per "border", and save the
         * differences between each border.
         */
        long prevTotal = (start == fTotalsStats.getStartTime()) ? 0 : getEventCountAt(start);
        long curTime = start + increment;

        for (int i = 0; i < nb - 1; i++) {
            long curTotal = getEventCountAt(curTime);
            long count = curTotal - prevTotal;
            list.add(count);

            curTime += increment;
            prevTotal = curTotal;
        }

        /*
         * For the last bucket, we'll stretch its end time to the end time of
         * the requested range, in case it got truncated down.
         */
        long curTotal = getEventCountAt(end);
        list.add(curTotal - prevTotal);

        return list;
    }

    @Override
    public long getEventsTotal() {
        long endTime = fTotalsStats.getCurrentEndTime();
        final int quark = fTotalsStats.optQuarkAbsolute(Attributes.TOTAL);
        if (quark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return 0;
        }
        try {
            return extractCount(fTotalsStats.querySingleState(endTime, quark).getValue());
        } catch (StateSystemDisposedException e) {
            /* Assume there is no events for that range */
            return 0;
        }
    }

    @Override
    public Map<@NonNull String, @NonNull Long> getEventTypesTotal() {

        int quark = fTypesStats.optQuarkAbsolute(Attributes.EVENT_TYPES);
        if (quark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return Collections.emptyMap();
        }

        /* Get the list of quarks, one for each even type in the database */
        List<Integer> quarks = fTypesStats.getSubAttributes(quark, false);
        long endTime = fTypesStats.getCurrentEndTime();
        final Map<@NonNull String, @NonNull Long> map = new HashMap<>();
        try {

            /* Since we want the total we can look only at the end */
            List<ITmfStateInterval> endState = fTypesStats.queryFullState(endTime);

            for (int typeQuark : quarks) {
                String curEventName = fTypesStats.getAttributeName(typeQuark);
                long eventCount = extractCount(endState.get(typeQuark).getValue());
                map.put(curEventName, eventCount);
            }

            return map;
        } catch (StateSystemDisposedException e) {
            /* Assume there is no events, nothing will be put in the map. */
            return Collections.emptyMap();
        }
    }

    @Override
    public long getEventsInRange(long start, long end) {
        long startCount;
        if (start == fTotalsStats.getStartTime()) {
            startCount = 0;
        } else {
            /*
             * We want the events happening at "start" to be included, so we'll
             * need to query one unit before that point.
             */
            startCount = getEventCountAt(start - 1);
        }
        long endCount = getEventCountAt(end);

        return endCount - startCount;
    }

    @Override
    public Map<String, Long> getEventTypesInRange(long start, long end) {

        /*
         * Make sure the start/end times are within the state history, so we don't get
         * TimeRange exceptions.
         */
        long startTime = Long.max(start, fTypesStats.getStartTime());
        long endTime = Long.min(end, fTypesStats.getCurrentEndTime());
        if (endTime < startTime) {
            /*
             * The start/end times do not intersect this state system range. Return the
             * empty map.
             */
            return Collections.emptyMap();
        }

        /* Get the list of quarks, one for each even type in the database */
        int quark = fTypesStats.optQuarkAbsolute(Attributes.EVENT_TYPES);
        if (quark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            /*
             * The state system does not (yet?) have the needed attributes, it probably
             * means there are no events counted yet. Return the empty map.
             */
            return Collections.emptyMap();
        }

        List<Integer> quarks = fTypesStats.getSubAttributes(quark, false);
        final Map<String, Long> map = new HashMap<>();

        try {
            List<ITmfStateInterval> endState = fTypesStats.queryFullState(endTime);

            if (startTime == fTypesStats.getStartTime()) {
                /* Only use the values picked up at the end time */
                for (int typeQuark : quarks) {
                    String curEventName = fTypesStats.getAttributeName(typeQuark);
                    Object eventCount = endState.get(typeQuark).getValue();
                    map.put(curEventName, extractCount(eventCount));
                }
            } else {
                /*
                 * Query the start time at -1, so the beginning of the interval
                 * is inclusive.
                 */
                List<ITmfStateInterval> startState = fTypesStats.queryFullState(startTime - 1);
                for (int typeQuark : quarks) {
                    String curEventName = fTypesStats.getAttributeName(typeQuark);
                    Object countAtStart = startState.get(typeQuark).getValue();
                    Object countAtEnd = endState.get(typeQuark).getValue();
                    long eventCount = extractCount(countAtEnd) - extractCount(countAtStart);
                    map.put(curEventName, eventCount);
                }
            }

        } catch (StateSystemDisposedException e) {
            /* Assume there is no (more) events, nothing will be put in the map. */
        }
        return map;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private long getEventCountAt(long timestamp) {
        /* Make sure the target time is within the range of the history */
        long ts = Long.max(fTotalsStats.getStartTime(), timestamp);
        ts = Long.min(ts, fTotalsStats.getCurrentEndTime());

        final int quark = fTotalsStats.optQuarkAbsolute(Attributes.TOTAL);
        if (quark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            return 0l;
        }

        try {
            return extractCount(fTotalsStats.querySingleState(ts, quark).getValue());
        } catch (StateSystemDisposedException e) {
            /* Assume there is no (more) events, nothing will be put in the map. */
            return 0;
        }
    }

    private static long extractCount(@Nullable Object state) {
        if (state instanceof Number) {
            return ((Number) state).longValue();
        }
        return 0l;
    }

    /**
     * The attribute names that are used in the state provider
     */
    public static final class Attributes {
        private Attributes() {

        }

        /** Total nb of events */
        public static final String TOTAL = "total"; //$NON-NLS-1$

        /** event_types */
        public static final String EVENT_TYPES = "event_types"; //$NON-NLS-1$

        /** lost_events
         * @since 2.0*/
        public static final String LOST_EVENTS = "lost_events"; //$NON-NLS-1$
    }
}
