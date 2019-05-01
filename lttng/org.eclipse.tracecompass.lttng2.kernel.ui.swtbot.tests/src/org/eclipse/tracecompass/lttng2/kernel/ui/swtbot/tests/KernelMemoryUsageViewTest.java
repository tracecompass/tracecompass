/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.kernelmemoryusage.KernelMemoryUsageView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.XYDataProviderBaseTest;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.ui.IViewPart;
import org.junit.Test;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.LineStyle;

/**
 * SWTBot tests for {@link KernelMemoryUsageView}
 *
 * @author Yonni Chen
 */
public class KernelMemoryUsageViewTest extends XYDataProviderBaseTest {

    private static final @NonNull String TITLE = "Relative Kernel Memory Usage";

    private static final @NonNull String TOTAL_PID = "bug446190:total";
    private static final @NonNull String SESSIOND_PID = "bug446190:482";
    private static final @NonNull String CONSUMERD_PID = "bug446190:496";

    private static final RGB GREEN = new RGB(0, 255, 0);
    private static final RGB BLUE = new RGB(0, 0, 255);
    private static final RGB PURPLE = new RGB(255, 0, 255);

    private static final int NUMBER_OF_POINT = 50;
    private static final int MORE_POINTS = 100;

    private String fTraceName = null;

    /**
     * Simple test to check the Kernel Memory Usage view data model
     */
    @Test
    public void testKernelMemoryView() {
        IViewPart viewPart = getSWTBotView().getViewReference().getView(true);
        assertTrue(viewPart instanceof KernelMemoryUsageView);
        final TmfCommonXAxisChartViewer chartViewer = (TmfCommonXAxisChartViewer) getChartViewer(viewPart);
        assertNotNull(chartViewer);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        final Chart chart = getChart();
        assertNotNull(chart);
        chartViewer.setNbPoints(NUMBER_OF_POINT);

        SWTBotTree treeBot = getSWTBotView().bot().tree();
        SWTBotTreeItem totalNode = treeBot.getTreeItem(fTraceName);
        SWTBotUtils.waitUntil(root -> root.getItems().length >= 5, totalNode, "Did not finish loading");

        /*
         * Select the total entry, which should be the first entry with an empty pid
         * column.
         */
        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 1, chart, "No data available");

        /* Test type, style and color of series */
        verifySeriesStyle(TOTAL_PID, ISeries.SeriesType.LINE, BLUE, LineStyle.SOLID, false);

        /* Test data model*/
        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json), "resources/kernelmemory/kernel-memory-res50.json", "Chart data is not valid");

        /*
         * Select a thread
         */
        SWTBotTreeItem sessiondEntry = totalNode.getNode("lttng-sessiond");
        sessiondEntry.check();
        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 2, chart, "Only total available");

        /* Test type, style and color of series */
        verifySeriesStyle(SESSIOND_PID, ISeries.SeriesType.LINE, GREEN, LineStyle.SOLID, false);

        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json, SESSIOND_PID), "resources/kernelmemory/kernel-memory-res50Selected.json", "Chart data is not valid");

        /*
         * Select an another thread and change zoom
         */
        SWTBotTreeItem consumerdEntry = totalNode.getNode("lttng-consumerd");
        consumerdEntry.check();
        chartViewer.setNbPoints(MORE_POINTS);
        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 3, chart, "Only total and sessiond available");

        /* Test type, style and color of series */
        verifySeriesStyle(CONSUMERD_PID, ISeries.SeriesType.LINE, PURPLE, LineStyle.SOLID, false);

        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json, CONSUMERD_PID), "resources/kernelmemory/kernel-memory-res100Selected.json", "Chart data is not valid");
    }

    /**
     * Test that the filter button works
     */
    @Test
    public void testFilter() {
        IViewPart viewPart = getSWTBotView().getViewReference().getView(true);
        assertTrue(viewPart instanceof KernelMemoryUsageView);
        final TmfCommonXAxisChartViewer chartViewer = (TmfCommonXAxisChartViewer) getChartViewer(viewPart);
        assertNotNull(chartViewer);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        SWTBotUtils.waitUntil(bot -> bot.tree().getTreeItem(fTraceName).getItems().length == 5, getSWTBotView().bot(), "Failed to load the filtered threads");

        getSWTBotView().toolbarButton("Showing active threads").click();
        SWTBotUtils.waitUntil(bot -> bot.tree().getTreeItem(fTraceName).getItems().length == 16, getSWTBotView().bot(), "Failed to load all the threads");

        getSWTBotView().toolbarButton("Showing all threads").click();
        SWTBotUtils.waitUntil(bot -> bot.tree().getTreeItem(fTraceName).getItems().length == 5, getSWTBotView().bot(), "Failed to filter the threads");
    }

    @Override
    protected @NonNull String getMainSeriesName() {
        return TOTAL_PID;
    }

    @Override
    protected @NonNull String getTitle() {
        return TITLE;
    }

    @Override
    protected String getViewID() {
        return KernelMemoryUsageView.ID;
    }

    @Override
    protected ITmfTrace getTestTrace() {
        ITmfTrace trace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.ARM_64_BIT_HEADER);
        fTraceName = trace.getName();
        return trace;
    }

    @Override
    protected void disposeTestTrace() {
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.ARM_64_BIT_HEADER);
    }
}
