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

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfStatsUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemFactory;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;

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
    // Constants
    // ------------------------------------------------------------------------

    /**
     * @deprecated Do not use, it's been replaced by {@link #TOTALS_STATE_ID}
     *              and {@link #TYPES_STATE_ID}
     */
    @Deprecated
    public static final String STATE_ID = "org.eclipse.linuxtools.tmf.statistics"; //$NON-NLS-1$

    /** ID for the "event totals" statistics state system
     * @since 2.2 */
    public static final String TOTALS_STATE_ID = "org.eclipse.linuxtools.tmf.statistics.totals"; //$NON-NLS-1$

    /** ID for the "event types" statistics state system
     * @since 2.2 */
    public static final String TYPES_STATE_ID = "org.eclipse.linuxtools.tmf.statistics.types"; //$NON-NLS-1$

    /** Filename for the "event totals" state history file */
    private static final String TOTALS_STATE_FILENAME = "statistics-totals.ht"; //$NON-NLS-1$

    /** Filename for the "event types" state history file */
    private static final String TYPES_STATE_FILENAME = "statistics-types.ht"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    private final ITmfTrace trace;

    /** The event totals state system */
    private final ITmfStateSystem totalsStats;

    /** The state system for event types */
    private final ITmfStateSystem typesStats;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Empty constructor. The resulting TmfStatistics object will not be usable,
     * but it might be needed for sub-classes.
     */
    public TmfStateStatistics() {
        totalsStats = null;
        typesStats = null;
        trace = null;
    }

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we build these statistics
     * @throws TmfTraceException
     *             If something went wrong trying to initialize the statistics
     */
    public TmfStateStatistics(ITmfTrace trace) throws TmfTraceException {
        this.trace = trace;
        String directory = TmfTraceManager.getSupplementaryFileDir(trace);

        final File totalsFile = new File(directory + TOTALS_STATE_FILENAME);
        final ITmfStateProvider totalsInput = new StatsProviderTotals(trace);
        this.totalsStats = TmfStateSystemFactory.newFullHistory(totalsFile, totalsInput, false);

        final File typesFile = new File(directory + TYPES_STATE_FILENAME);
        final ITmfStateProvider typesInput = new StatsProviderEventTypes(trace);
        this.typesStats = TmfStateSystemFactory.newFullHistory(typesFile, typesInput, false);

        registerStateSystems();
    }

    /**
     * Old manual constructor.
     *
     * @param trace Trace
     * @param historyFile Full history file
     * @deprecated Need to use {@link #TmfStateStatistics(ITmfTrace trace,
     * File fullHistoryFile, File partialHistoryFile)} now.
     */
    @Deprecated
    public TmfStateStatistics(ITmfTrace trace, File historyFile) {
        this();
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
    public TmfStateStatistics(ITmfTrace trace, File totalsHistoryFile,
            File typesHistoryFile) throws TmfTraceException {
        this.trace = trace;
        final ITmfStateProvider totalsInput = new StatsProviderTotals(trace);
        final ITmfStateProvider typesInput = new StatsProviderEventTypes(trace);
        this.totalsStats = TmfStateSystemFactory.newFullHistory(totalsHistoryFile, totalsInput, true);
        this.typesStats = TmfStateSystemFactory.newFullHistory(typesHistoryFile, typesInput, true);
        registerStateSystems();
    }

    /**
     * Register the state systems used here into the trace's state system map.
     */
    private void registerStateSystems() {
        trace.registerStateSystem(TOTALS_STATE_ID, totalsStats);
        trace.registerStateSystem(TYPES_STATE_ID, typesStats);
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
    public void updateStats(final boolean isGlobal, final long start,
            final long end) {
        /*
         * Since we are currently in a signal handler (ie, in the UI thread),
         * and since state system queries can be arbitrarily long (O(log n) wrt
         * the size of the trace), we will run those queries in a separate
         * thread and update the statistics view out-of-band.
         */
        Thread statsThread = new Thread("Statistics update") { //$NON-NLS-1$
            @Override
            public void run() {
                /* Wait until the history building is completed */
                if (!waitUntilBuilt()) {
                    return;
                }

                /* Range should be valid for both global and time range queries */
                long total = getEventsInRange(start, end);
                Map<String, Long> map = getEventTypesInRange(start, end);

                /* Send the signal to notify the stats viewer to update its display */
                TmfSignal sig = new TmfStatsUpdatedSignal(this, trace, isGlobal, total, map);
                TmfSignalManager.dispatchSignal(sig);
            }
        };
        statsThread.start();
        return;
    }

    @Override
    public List<Long> histogramQuery(final long start, final long end, final int nb) {
        final List<Long> list = new LinkedList<>();
        final long increment = (end - start) / nb;

        if (!totalsStats.waitUntilBuilt()) {
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
        /* We need the complete state history to be built to answer this. */
        totalsStats.waitUntilBuilt();

        long endTime = totalsStats.getCurrentEndTime();
        int count = 0;

        try {
            final int quark = totalsStats.getQuarkAbsolute(Attributes.TOTAL);
            count= totalsStats.querySingleState(endTime, quark).getStateValue().unboxInt();

        } catch (TimeRangeException e) {
            /* Assume there is no events for that range */
            return 0;
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            e.printStackTrace();
        }

        return count;
    }

    @Override
    public Map<String, Long> getEventTypesTotal() {
        /* We need the complete state history to be built to answer this. */
        typesStats.waitUntilBuilt();

        Map<String, Long> map = new HashMap<>();
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
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public long getEventsInRange(long start, long end) {
        // FIXME Instead of waiting until the end, we could check the current
        // end time, and answer as soon as possible...
        totalsStats.waitUntilBuilt();

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
        // FIXME Instead of waiting until the end, we could check the current
        // end time, and answer as soon as possible...
        typesStats.waitUntilBuilt();

        Map<String, Long> map = new HashMap<>();

        /* Make sure the start/end times are within the state history, so we
         * don't get TimeRange exceptions.
         */
        long startTime = checkStartTime(start);
        long endTime = checkEndTime(end);

        try {
            /* Get the list of quarks, one for each even type in the database */
            int quark = typesStats.getQuarkAbsolute(Attributes.EVENT_TYPES);
            List<Integer> quarks = typesStats.getSubAttributes(quark, false);

            List<ITmfStateInterval> endState = typesStats.queryFullState(endTime);

            String curEventName;
            long countAtStart, countAtEnd, eventCount;

            if (startTime == typesStats.getStartTime()) {
                /* Only use the values picked up at the end time */
                for (int typeQuark : quarks) {
                    curEventName = typesStats.getAttributeName(typeQuark);
                    eventCount = endState.get(typeQuark).getStateValue().unboxInt();
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
                    curEventName = typesStats.getAttributeName(typeQuark);
                    countAtStart = startState.get(typeQuark).getStateValue().unboxInt();
                    countAtEnd = endState.get(typeQuark).getStateValue().unboxInt();

                    if (countAtStart == -1) {
                        countAtStart = 0;
                    }
                    if (countAtEnd == -1) {
                        countAtEnd = 0;
                    }
                    eventCount = countAtEnd - countAtStart;
                    map.put(curEventName, eventCount);
                }
            }

        } catch (TimeRangeException e) {
            /* Assume there is no events, nothing will be put in the map. */
        } catch (AttributeNotFoundException e) {
            /*
             * These other exception types would show a logic problem however,
             * so they should not happen.
             */
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
            e.printStackTrace();
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
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        } catch (StateSystemDisposedException e) {
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
     * Wait until both backing state systems are finished building.
     *
     * @return If both state systems were built successfully
     */
    private boolean waitUntilBuilt() {
        boolean check1 = totalsStats.waitUntilBuilt();
        boolean check2 = typesStats.waitUntilBuilt();
        return (check1 && check2);
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
