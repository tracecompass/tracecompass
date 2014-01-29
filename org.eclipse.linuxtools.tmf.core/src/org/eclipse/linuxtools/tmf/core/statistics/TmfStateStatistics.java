/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
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

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

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
 * @since 2.0
 */
public class TmfStateStatistics implements ITmfStatistics {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    /** The event totals state system */
    private final ITmfStateSystem totalsStats;

    /** The state system for event types */
    private final ITmfStateSystem typesStats;

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
     * @since 3.0
     */
    public TmfStateStatistics(@NonNull ITmfStateSystem totals, @NonNull ITmfStateSystem eventTypes) {
        this.totalsStats = totals;
        this.typesStats = eventTypes;
    }

    /**
     * Manual constructor. This should be used if the trace's Resource is null
     * (ie, for unit tests). It requires specifying the location of the history
     * files manually.
     *
     * @param trace
     *            The trace for which we build these statistics
     * @param totalsHistoryFile
     *            The location of the totals state history file
     * @param typesHistoryFile
     *            The location of the types state history file
     * @throws TmfTraceException
     *             If the file could not be written to
     * @since 2.2
     */
    @Deprecated
    public TmfStateStatistics(ITmfTrace trace, File totalsHistoryFile,
            File typesHistoryFile) throws TmfTraceException {
        final ITmfStateProvider totalsInput = new TmfStatisticsTotalsModule().new StatsProviderTotals(trace);
        final ITmfStateProvider typesInput = new TmfStatisticsEventTypesModule().new StatsProviderEventTypes(trace);
        this.totalsStats = TmfStateSystemFactory.newFullHistory(totalsHistoryFile, totalsInput, true);
        this.typesStats = TmfStateSystemFactory.newFullHistory(typesHistoryFile, typesInput, true);
    }

    /**
     * Return the state system containing the "totals" values
     *
     * @return The "totals" state system
     * @since 3.0
     */
    public ITmfStateSystem getTotalsSS() {
        return totalsStats;
    }

    /**
     * Return the state system containing the "event types" values
     *
     * @return The "event types" state system
     * @since 3.0
     */
    public ITmfStateSystem getEventTypesSS() {
        return typesStats;
    }

    // ------------------------------------------------------------------------
    // ITmfStatistics
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        totalsStats.dispose();
        typesStats.dispose();
    }

    @Override
    public List<Long> histogramQuery(final long start, final long end, final int nb) {
        final List<Long> list = new LinkedList<>();
        final long increment = (end - start) / nb;

        if (totalsStats.isCancelled()) {
            return list;
        }

        /*
         * We will do one state system query per "border", and save the
         * differences between each border.
         */
        long prevTotal = (start == totalsStats.getStartTime()) ? 0 : getEventCountAt(start);
        long curTime = start + increment;

        long curTotal, count;
        for (int i = 0; i < nb - 1; i++) {
            curTotal = getEventCountAt(curTime);
            count = curTotal - prevTotal;
            list.add(count);

            curTime += increment;
            prevTotal = curTotal;
        }

        /*
         * For the last bucket, we'll stretch its end time to the end time of
         * the requested range, in case it got truncated down.
         */
        curTotal = getEventCountAt(end);
        count = curTotal - prevTotal;
        list.add(count);

        return list;
    }

    @Override
    public long getEventsTotal() {
        long endTime = totalsStats.getCurrentEndTime();
        int count = 0;

        try {
            final int quark = totalsStats.getQuarkAbsolute(Attributes.TOTAL);
            count= totalsStats.querySingleState(endTime, quark).getStateValue().unboxInt();

        } catch (TimeRangeException e) {
            /* Assume there is no events for that range */
            return 0;
        } catch (AttributeNotFoundException | StateValueTypeException | StateSystemDisposedException e) {
            e.printStackTrace();
        }

        return count;
    }

    @Override
    public Map<String, Long> getEventTypesTotal() {
        final Map<String, Long> map = new HashMap<>();
        long endTime = typesStats.getCurrentEndTime();

        try {
            /* Get the list of quarks, one for each even type in the database */
            int quark = typesStats.getQuarkAbsolute(Attributes.EVENT_TYPES);
            List<Integer> quarks = typesStats.getSubAttributes(quark, false);

            /* Since we want the total we can look only at the end */
            List<ITmfStateInterval> endState = typesStats.queryFullState(endTime);

            String curEventName;
            long eventCount;
            for (int typeQuark : quarks) {
                curEventName = typesStats.getAttributeName(typeQuark);
                eventCount = endState.get(typeQuark).getStateValue().unboxInt();
                map.put(curEventName, eventCount);
            }

        } catch (TimeRangeException e) {
            /* Assume there is no events, nothing will be put in the map. */
        } catch (AttributeNotFoundException | StateValueTypeException | StateSystemDisposedException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public long getEventsInRange(long start, long end) {
        long startCount;
        if (start == totalsStats.getStartTime()) {
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
        final Map<String, Long> map = new HashMap<>();
        List<Integer> quarks;

        /* Make sure the start/end times are within the state history, so we
         * don't get TimeRange exceptions.
         */
        long startTime = checkStartTime(start);
        long endTime = checkEndTime(end);

        try {
            /* Get the list of quarks, one for each even type in the database */
            int quark = typesStats.getQuarkAbsolute(Attributes.EVENT_TYPES);
            quarks = typesStats.getSubAttributes(quark, false);
        } catch (AttributeNotFoundException e) {
            /*
             * The state system does not (yet?) have the needed attributes, it
             * probably means there are no events counted yet. Return the empty
             * map.
             */
            return map;
        }

        try {
            List<ITmfStateInterval> endState = typesStats.queryFullState(endTime);

            if (startTime == typesStats.getStartTime()) {
                /* Only use the values picked up at the end time */
                for (int typeQuark : quarks) {
                    String curEventName = typesStats.getAttributeName(typeQuark);
                    long eventCount = endState.get(typeQuark).getStateValue().unboxInt();
                    if (eventCount == -1) {
                        eventCount = 0;
                    }
                    map.put(curEventName, eventCount);
                }
            } else {
                /*
                 * Query the start time at -1, so the beginning of the interval
                 * is inclusive.
                 */
                List<ITmfStateInterval> startState = typesStats.queryFullState(startTime - 1);
                for (int typeQuark : quarks) {
                    String curEventName = typesStats.getAttributeName(typeQuark);
                    long countAtStart = startState.get(typeQuark).getStateValue().unboxInt();
                    long countAtEnd = endState.get(typeQuark).getStateValue().unboxInt();

                    if (countAtStart == -1) {
                        countAtStart = 0;
                    }
                    if (countAtEnd == -1) {
                        countAtEnd = 0;
                    }
                    long eventCount = countAtEnd - countAtStart;
                    map.put(curEventName, eventCount);
                }
            }

        } catch (TimeRangeException | StateSystemDisposedException e) {
            /* Assume there is no (more) events, nothing will be put in the map. */
        } catch (StateValueTypeException e) {
            /*
             * This exception type would show a logic problem however,
             * so they should not happen.
             */
            throw new IllegalStateException();
        }
        return map;
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private long getEventCountAt(long timestamp) {
        /* Make sure the target time is within the range of the history */
        long ts = checkStartTime(timestamp);
        ts = checkEndTime(ts);

        try {
            final int quark = totalsStats.getQuarkAbsolute(Attributes.TOTAL);
            long count = totalsStats.querySingleState(ts, quark).getStateValue().unboxInt();
            return count;

        } catch (TimeRangeException e) {
            /* Assume there is no events for that range */
        } catch (AttributeNotFoundException | StateValueTypeException | StateSystemDisposedException e) {
            e.printStackTrace();
        }

        return 0;
    }

    private long checkStartTime(long initialStart) {
        long start = initialStart;
        if (start < totalsStats.getStartTime()) {
            return totalsStats.getStartTime();
        }
        return start;
    }

    private long checkEndTime(long initialEnd) {
        long end = initialEnd;
        if (end > totalsStats.getCurrentEndTime()) {
            return totalsStats.getCurrentEndTime();
        }
        return end;
    }

    /**
     * The attribute names that are used in the state provider
     */
    public static class Attributes {

        /** Total nb of events */
        public static final String TOTAL = "total"; //$NON-NLS-1$

        /** event_types */
        public static final String EVENT_TYPES = "event_types"; //$NON-NLS-1$<
    }
}
