/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
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
 * SWTBot tests for Kernel Memory Usage view
 *
 * @author Yonni Chen
 */
public class KernelMemoryUsageViewTest extends XYDataProviderBaseTest {

    private static final @NonNull String TOTAL_SERIES_NAME = "Total";
    private static final @NonNull String TITLE = "Relative Kernel Memory Usage";

    private static final RGB RED = new RGB(255, 0, 0);
    private static final RGB BLUE = new RGB(0, 0, 255);

    private static final int NUMBER_OF_POINT = 50;
    private static final int MORE_POINTS = 100;

    /**
     * Simple test to check the Kernel Memory Usage view data model
     *
     * @throws NoSuchMethodException
     *             Reflection exception should not happen
     * @throws SecurityException
     *             Reflection exception should not happen
     * @throws IllegalAccessException
     *             Reflection exception should not happen
     * @throws IllegalArgumentException
     *             Reflection exception should not happen
     * @throws InvocationTargetException
     *             Reflection exception should not happen
     */
    @Test
    public void testKernelMemoryView() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        IViewPart viewSite = getSWTBotView().getViewReference().getView(true);
        assertTrue(viewSite instanceof KernelMemoryUsageView);
        final TmfCommonXAxisChartViewer chartViewer = (TmfCommonXAxisChartViewer) getChartViewer(viewSite);
        assertNotNull(chartViewer);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        final Chart chart = getChart();
        assertNotNull(chart);

        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length > 0, chart, "No data available");
        chartViewer.setNbPoints(NUMBER_OF_POINT);

        /* Test type, style and color of series */
        verifyChartStyle(null);

        /* Test data model*/
        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json), "resources/kernel-memory-res50.json", "Chart data is not valid");

        /*
         * Select a thread and change zoom
         */
        String selectedThread = "496";
        SWTBotTree treeBot = getSWTBotView().bot().tree();
        SWTBotUtils.waitUntil(tree -> tree.rowCount() >= 5, treeBot, "Did not finish loading");
        treeBot.getTreeItem(selectedThread).click();
        chartViewer.setNbPoints(MORE_POINTS);
        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 2, chart, "Only total available");

        /* Test type, style and color of series */
        verifyChartStyle(selectedThread);

        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json, selectedThread), "resources/kernel-memory-res100Selected.json", "Chart data is not valid");
    }

    /**
     * Verify the chart style. This method will test <i>Total</i> series style as well as
     * the given selected thread series.
     *
     * @param selectedThread
     *            The selected thread. If no selected thread, give <code>null</code>
     *            as parameter.
     */
    private void verifyChartStyle(String selectedThread) {
        verifySeriesStyle(TOTAL_SERIES_NAME, ISeries.SeriesType.LINE, BLUE, LineStyle.SOLID, false);

        if (selectedThread != null) {
            verifySeriesStyle(selectedThread, ISeries.SeriesType.LINE, RED, LineStyle.SOLID, false);
        }
    }

    @Override
    protected @NonNull String getMainSeriesName() {
        return TOTAL_SERIES_NAME;
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
        return CtfTmfTestTraceUtils.getTrace(CtfTestTrace.ARM_64_BIT_HEADER);
    }
}
