/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis - Initial API and implementation
 *   Alexis Cabana-Loriaux - Extract the class in a compilation unit
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.piecharts.model.TmfPieChartStatisticsModel;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfStatisticsTree;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfStatisticsTreeManager;
import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfStatisticsTreeNode;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.statistics.ITmfStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Class used to update the Statistics view. Normally, it should only be used by
 * this class
 *
 * @author Mathieu Denis
 */
class StatisticsUpdateJob extends Job {

    private final ITmfTrace fJobTrace;
    private final boolean fIsGlobal;
    private final TmfStatisticsModule fStatsMod;
    private final TmfStatisticsViewer fViewer;

    /**
     * The delay (in ms) between each update in live-reading mode
     */
    private static final long LIVE_UPDATE_DELAY = 1000;

    private TmfTimeRange fTimerange;

    /**
     * @param name
     *            The name of the working job
     * @param trace
     *            The trace to query
     * @param isGlobal
     *            If the query is for the global time-range or a selection
     *            time-range
     * @param timerange
     *            The timerange of
     * @param statsMod
     *            The statistics module of the trace
     * @param viewer
     *            The viewer to update
     */
    public StatisticsUpdateJob(String name, ITmfTrace trace, boolean isGlobal, TmfTimeRange timerange, TmfStatisticsModule statsMod, TmfStatisticsViewer viewer) {
        super(name);
        fJobTrace = trace;
        fIsGlobal = isGlobal;
        fTimerange = timerange;
        fStatsMod = statsMod;
        fViewer = viewer;
    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        IStatus st = fStatsMod.schedule();
        if (!st.isOK()) {
            return st;
        }

        /* Wait until the analysis is ready to be queried */
        if (!fStatsMod.waitForInitialization()) {
            return Status.CANCEL_STATUS;
        }
        ITmfStatistics stats = fStatsMod.getStatistics();
        if (stats == null) {
            /* It should have worked, but didn't */
            throw new IllegalStateException();
        }

        /*
         * TODO Eventually this could be exposed through the
         * TmfStateSystemAnalysisModule directly.
         */
        ITmfStateSystem ss = fStatsMod.getStateSystem(TmfStatisticsEventTypesModule.ID);
        if (ss == null) {
            /*
             * It should be instantiated after the
             * statsMod.waitForInitialization() above.
             */
            throw new IllegalStateException();
        }

        /*
         * Periodically update the statistics while they are being built (or, if
         * the back-end is already completely built, it will skip over the
         * while() immediately.
         */
        long start = 0;
        long end = 0;
        boolean finished = false;
        do {
            /* This model update is done every second */
            if (monitor.isCanceled()) {
                fViewer.removeFromJobs(fIsGlobal, fJobTrace);
                return Status.CANCEL_STATUS;
            }
            finished = ss.waitUntilBuilt(LIVE_UPDATE_DELAY);
            TmfTimeRange localtimeRange = fTimerange;
            /*
             * The generic statistics are stored in nanoseconds, so we must make
             * sure the time range is scaled correctly.
             */
            start = localtimeRange.getStartTime().toNanos();
            end = localtimeRange.getEndTime().toNanos();

            Map<String, Long> map = stats.getEventTypesInRange(start, end);
            updateStats(map);
        } while (!finished);


        /* Query one last time for the final values */
        Map<String, Long> map = stats.getEventTypesInRange(start, end);
        updateStats(map);
        fViewer.refreshPieCharts(fIsGlobal, !fIsGlobal);
        /*
         * Remove job from map so that new range selection updates can be
         * processed.
         */
        fViewer.removeFromJobs(fIsGlobal, fJobTrace);
        return Status.OK_STATUS;
    }

    /*
     * Update the tree for a given trace
     */
    private void updateStats(Map<String, Long> eventsPerType) {

        final TmfStatisticsTree statsData = TmfStatisticsTreeManager.getStatTree(fViewer.getTreeID());
        if (statsData == null) {
            /* The stat tree has been disposed, abort mission. */
            return;
        }

        Map<String, Long> map = eventsPerType;
        String name = fJobTrace.getName();

        /**
         * <pre>
         * "Global", "partial", "total", etc., it's all very confusing...
         *
         * The base view shows the total count for the trace and for
         * each even types, organized in columns like this:
         *
         *                   |  Global  |  Time range |
         * trace name        |    A     |      B      |
         *    Event Type     |          |             |
         *       <event 1>   |    C     |      D      |
         *       <event 2>   |   ...    |     ...     |
         *         ...       |          |             |
         *
         * Here, we called the cells like this:
         *  A : GlobalTotal
         *  B : TimeRangeTotal
         *  C : GlobalTypeCount(s)
         *  D : TimeRangeTypeCount(s)
         * </pre>
         */

        if (map.isEmpty() && !fIsGlobal) {
            /* Reset all time range event counts (cells D) */
            TmfStatisticsTreeNode eventTypeNode = statsData.getNode(name, TmfStatisticsTree.HEADER_EVENT_TYPES);
            if (eventTypeNode != null) {
                eventTypeNode.resetTimeRangeValue();
            }
        } else {
            /* Fill in the event counts (either cells C or D) */
            for (Map.Entry<String, Long> entry : map.entrySet()) {
                statsData.setTypeCount(name, entry.getKey(), fIsGlobal, entry.getValue());
            }
        }

        /*
         * Calculate the totals (cell A or B, depending if isGlobal). We will
         * use the results of the previous request instead of sending another
         * one.
         */
        long globalTotal = 0;
        for (long val : map.values()) {
            globalTotal += val;
        }
        /* Update both the tree model and the piechart model */
        statsData.setTotal(name, fIsGlobal, globalTotal);
        TmfPieChartStatisticsModel model = fViewer.getPieChartModel();
        if (model != null) {
            model.setPieChartTypeCount(fIsGlobal, fJobTrace, eventsPerType);
        }
        /* notify that the viewer needs to be refreshed */
        fViewer.modelComplete(fIsGlobal);
    }
}
