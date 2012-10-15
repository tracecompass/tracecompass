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

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.linuxtools.tmf.core.TmfCommonConstants;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.StateSystemManager;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Default implementation of an ITmfStatisticsProvider. It uses a state system
 * underneath to store its information.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */

public class TmfStatistics  implements ITmfStatistics {

    /** ID for the statistics state system */
    public static final String STATE_ID = "org.eclipse.linuxtools.tmf.statistics"; //$NON-NLS-1$

    /* Filename the "statistics state history" file will have */
    private static final String STATS_STATE_FILENAME = "statistics.ht"; //$NON-NLS-1$

    /*
     * The state system that's used to stored the statistics. It's hidden from
     * the trace, so that it doesn't conflict with ITmfTrace.getStateSystem()
     * (which is something else!)
     */
    private final ITmfStateSystem stats;

    /**
     * Empty constructor. The resulting TmfStatistics object will not be usable,
     * but it might be needed for sub-classes.
     */
    public TmfStatistics() {
        stats = null;
    }

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we build these statistics
     * @throws TmfTraceException
     *             If something went wrong trying to initialize the statistics
     */
    public TmfStatistics(ITmfTrace trace) throws TmfTraceException {
        /* Set up the path to the history tree file we'll use */
        IResource resource = trace.getResource();
        String supplDirectory = null;

        try {
            // get the directory where the history file will be stored.
            supplDirectory = resource.getPersistentProperty(TmfCommonConstants.TRACE_SUPPLEMENTARY_FOLDER);
        } catch (CoreException e) {
            throw new TmfTraceException(e.toString(), e);
        }

        final File htFile = new File(supplDirectory + File.separator + STATS_STATE_FILENAME);
        final IStateChangeInput htInput = new StatsStateProvider(trace);

        this.stats = StateSystemManager.loadStateHistory(htFile, htInput, STATE_ID, false);
    }

    // ------------------------------------------------------------------------
    // ITmfStatisticsProvider
    // ------------------------------------------------------------------------

    @Override
    public long getEventsTotal() {
        /*
         * The total itself is not stored in the state, so we will do a
         * "event types" query then add the contents manually.
         */
        Map<String, Long> map = getEventTypesTotal();
        long total = 0;
        for (long count : map.values()) {
            total += count;
        }
        return total;
    }

    @Override
    public Map<String, Long> getEventTypesTotal() {
        Map<String, Long> map = new HashMap<String, Long>();
        long endTime = stats.getCurrentEndTime(); //shouldn't need to check it...

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
            /* Ignore silently */
        } catch (AttributeNotFoundException e) {
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public long getEventsInRange(ITmfTimestamp start, ITmfTimestamp end) {
        /*
         * The total itself is not stored in the state, so we will do a
         * "event types" query then add the contents manually.
         */
        Map<String, Long> map = getEventTypesInRange(start, end);
        long total = 0;
        for (long count : map.values()) {
            total += count;
        }
        return total;
    }

    @Override
    public Map<String, Long> getEventTypesInRange(ITmfTimestamp start, ITmfTimestamp end) {
        Map<String, Long> map = new HashMap<String, Long>();

        /* Make sure the start/end times are within the state history, so we
         * don't get TimeRange exceptions.
         */
        long startTimestamp = checkStartTime(start.getValue());
        long endTimestamp = checkEndTime(end.getValue());

        try {
            /* Get the list of quarks, one for each even type in the database */
            int quark = stats.getQuarkAbsolute(Attributes.EVENT_TYPES);
            List<Integer> quarks = stats.getSubAttributes(quark, false);

            /*
             * Get the complete states (in our case, event counts) at the start
             * time and end time of the requested time range.
             */
            List<ITmfStateInterval> startState = stats.queryFullState(startTimestamp);
            List<ITmfStateInterval> endState = stats.queryFullState(endTimestamp);

            /* Save the relevant information in the map we will be returning */
            String curEventName;
            long countAtStart, countAtEnd, eventCount;
            for (int typeQuark : quarks) {
                curEventName = stats.getAttributeName(typeQuark);
                countAtStart = startState.get(typeQuark).getStateValue().unboxInt();
                countAtEnd = endState.get(typeQuark).getStateValue().unboxInt();

                /*
                 * The default value for the statistics is 0, rather than the
                 * value -1 used by the state system for non-initialized state.
                 */
                if (startTimestamp == stats.getStartTime() || countAtStart == -1) {
                    countAtStart = 0;
                }

                /*
                 * Workaround a bug in the state system where requests for the
                 * very last state change will give -1. Send the request 1ns
                 * before the end of the trace and add the last event to the
                 * count.
                 */
                if (countAtEnd < 0) {
                    ITmfStateInterval realInterval = stats.querySingleState(endTimestamp - 1, typeQuark);
                    countAtEnd = realInterval.getStateValue().unboxInt() + 1;
                }

                /*
                 * If after this it is still at -1, it's because no event of
                 * this type happened during the requested time range.
                 */
                if (countAtEnd < 0) {
                    countAtEnd = 0;
                }

                eventCount = countAtEnd - countAtStart;
                map.put(curEventName, eventCount);
            }
        } catch (TimeRangeException e) {
            /*
             * If a request is made for an invalid time range, we will ignore it
             * silently and not add any information to the map.
             */
        } catch (AttributeNotFoundException e) {
            /*
             * These other exceptions would show a logic problem however, so
             * they should not happen.
             */
            e.printStackTrace();
        } catch (StateValueTypeException e) {
            e.printStackTrace();
        }
        return map;
    }

    protected long checkStartTime(long start) {
        if (start < stats.getStartTime()) {
            return stats.getStartTime();
        }
        return start;
    }

    protected long checkEndTime(long end) {
        if (end > stats.getCurrentEndTime()) {
            return stats.getCurrentEndTime();
        }
        return end;
    }


    /**
     * The attribute names that are used in the state provider
     */
    public static class Attributes {

        /** event_types */
        public static final String EVENT_TYPES = "event_types"; //$NON-NLS-1$<
    }
}
