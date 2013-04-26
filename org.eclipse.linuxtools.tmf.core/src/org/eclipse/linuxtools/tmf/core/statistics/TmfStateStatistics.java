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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
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

/**
 * Implementation of ITmfStatistics which uses a state history for storing its
 * information.
 *
 * It requires building the history first, but gives very fast response times
 * when built : Queries are O(log n) wrt the size of the trace, and O(1) wrt to
 * the size of the time interval selected.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public class TmfStateStatistics implements ITmfStatistics {

    /** ID for the statistics state system */
    public static final String STATE_ID = "org.eclipse.linuxtools.tmf.statistics"; //$NON-NLS-1$

    /** Filename the "statistics state history" file will have */
    private static final String STATS_STATE_FILENAME = "statistics.ht"; //$NON-NLS-1$

    private final ITmfTrace trace;

    /**
     * The state system that's used to stored the statistics. It's hidden from
     * the trace, so that it doesn't conflict with ITmfTrace.getStateSystem()
     * (which is something else!)
     */
    private final ITmfStateSystem stats;

    /**
     * Empty constructor. The resulting TmfStatistics object will not be usable,
     * but it might be needed for sub-classes.
     */
    public TmfStateStatistics() {
        stats = null;
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
        /* Set up the path to the history tree file we'll use */
        this.trace = trace;
        IResource resource = trace.getResource();
        String supplDirectory = null;

        try {
            // get the directory where the history file will be stored.
            supplDirectory = resource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);
        } catch (CoreException e) {
            throw new TmfTraceException(e.toString(), e);
        }

        final File htFile = new File(supplDirectory + File.separator + STATS_STATE_FILENAME);
        final ITmfStateProvider htInput = new StatsStateProvider(trace);

        this.stats = TmfStateSystemFactory.newFullHistory(htFile, htInput, false);
        registerStateSystems();
    }

    /**
     * Manual constructor. This should be used if the trace's Resource is null
     * (ie, for unit tests). It requires specifying the location of the history
     * file manually.
     *
     * @param trace
     *            The trace for which we build these statistics
     * @param historyFile
     *            The location of the state history file to build for the stats
     * @throws TmfTraceException
     *             If the file could not be written to
     */
    public TmfStateStatistics(ITmfTrace trace, File historyFile) throws TmfTraceException {
        this.trace = trace;
        final ITmfStateProvider htInput = new StatsStateProvider(trace);
        this.stats = TmfStateSystemFactory.newFullHistory(historyFile, htInput, true);
        registerStateSystems();
    }

    /**
     * Register the state systems used here into the trace's state system array.
     */
    private void registerStateSystems() {
        trace.registerStateSystem(STATE_ID, stats);
    }

    // ------------------------------------------------------------------------
    // ITmfStatistics
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        stats.dispose();
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
                long total;
                Map<String, Long> map;

                /* Wait until the history building completed */
                if (!stats.waitUntilBuilt()) {
                    return;
                }

                /* Range should be valid for both global and time range queries */
                total = getEventsInRange(start, end);
                map = getEventTypesInRange(start, end);

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
        final List<Long> list = new LinkedList<Long>();
        final long increment = (end - start) / nb;

        /* Wait until the history building completed */
        if (!stats.waitUntilBuilt()) {
            return null;
        }

        /*
         * We will do one state system query per "border", and save the
         * differences between each border.
         */
        long prevTotal = (start == stats.getStartTime()) ? 0 : getEventCountAt(start);
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
        stats.waitUntilBuilt();

        long endTime = stats.getCurrentEndTime();
        int count = 0;

        try {
            final int quark = stats.getQuarkAbsolute(Attributes.TOTAL);
            count= stats.querySingleState(endTime, quark).getStateValue().unboxInt();

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
        stats.waitUntilBuilt();

        Map<String, Long> map = new HashMap<String, Long>();
        long endTime = stats.getCurrentEndTime();

        try {
            /* Get the list of quarks, one for each even type in the database */
            int quark = stats.getQuarkAbsolute(Attributes.EVENT_TYPES);
            List<Integer> quarks = stats.getSubAttributes(quark, false);

            /* Since we want the total we can look only at the end */
            List<ITmfStateInterval> endState = stats.queryFullState(endTime);

            String curEventName;
            long eventCount;
            for (int typeQuark : quarks) {
                curEventName = stats.getAttributeName(typeQuark);
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
        stats.waitUntilBuilt();

        long startCount;
        if (start == stats.getStartTime()) {
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
        stats.waitUntilBuilt();

        Map<String, Long> map = new HashMap<String, Long>();

        /* Make sure the start/end times are within the state history, so we
         * don't get TimeRange exceptions.
         */
        long startTime = checkStartTime(start);
        long endTime = checkEndTime(end);

        try {
            /* Get the list of quarks, one for each even type in the database */
            int quark = stats.getQuarkAbsolute(Attributes.EVENT_TYPES);
            List<Integer> quarks = stats.getSubAttributes(quark, false);

            List<ITmfStateInterval> endState = stats.queryFullState(endTime);

            String curEventName;
            long countAtStart, countAtEnd, eventCount;

            if (startTime == stats.getStartTime()) {
                /* Only use the values picked up at the end time */
                for (int typeQuark : quarks) {
                    curEventName = stats.getAttributeName(typeQuark);
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
                List<ITmfStateInterval> startState = stats.queryFullState(startTime - 1);
                for (int typeQuark : quarks) {
                    curEventName = stats.getAttributeName(typeQuark);
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

    private long getEventCountAt(long timestamp) {
        /* Make sure the target time is within the range of the history */
        long ts = checkStartTime(timestamp);
        ts = checkEndTime(ts);

        try {
            final int quark = stats.getQuarkAbsolute(Attributes.TOTAL);
            long count = stats.querySingleState(ts, quark).getStateValue().unboxInt();
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
        if (start < stats.getStartTime()) {
            return stats.getStartTime();
        }
        return start;
    }

    private long checkEndTime(long initialEnd) {
        long end = initialEnd;
        if (end > stats.getCurrentEndTime()) {
            return stats.getCurrentEndTime();
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
