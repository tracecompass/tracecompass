/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.analysis.counters.ui.CounterView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.XYDataProviderBaseTest;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.ui.IViewPart;
import org.junit.Test;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.LineStyle;

/**
 * SWTBot tests for Counters view.
 *
 * @author Mikael Ferland
 */
public class CounterViewTest extends XYDataProviderBaseTest {

    private static final int NUMBER_OF_POINTS = 50;
    private static final RGB BLUE = new RGB(0, 0, 255);
    private static final @NonNull String TRACE_NAME = "kernel_vm";
    private static final @NonNull String COUNTER_NAME = "minor_faults";
    private static final @NonNull String COUNTERS_VIEW_TITLE = "Counters View";
    private static final @NonNull String MAIN_SERIES_NAME = TRACE_NAME + "/Ungrouped/" + COUNTER_NAME;
    private static final @NonNull String COUNTERS_VIEW_ID = "org.eclipse.tracecompass.analysis.counters.ui.view.counters";

    /**
     * Ensure the data displayed in the chart viewer reflects the tree viewer's
     * selected entries.
     */
    @Test
    public void testManipulatingTreeViewer() {
        final Chart chart = getChart();
        assertNotNull(chart);
        assertEquals(0, chart.getSeriesSet().getSeries().length);

        SWTBotTree treeBot = getSWTBotView().bot().tree();
        WaitUtils.waitUntil(tree -> tree.rowCount() >= 1, treeBot, "The tree viewer did not finish loading.");
        SWTBotTreeItem root = treeBot.getTreeItem(TRACE_NAME);
        SWTBotTreeItem counter = retrieveTreeItem(root, COUNTER_NAME);

        // Check all elements of the tree
        root.check();
        assertTrue(root.isChecked());
        assertTrue(counter.isChecked());
        assertFalse(root.isGrayed());
        assertFalse(counter.isGrayed());
        WaitUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 3, chart, "The data series did not load.");

        // Uncheck a leaf of the tree
        counter.uncheck();
        assertTrue(root.isChecked());
        assertTrue(root.isGrayed());
        assertFalse(counter.isChecked());
        assertFalse(counter.isGrayed());
        WaitUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 2, chart,
                "A data series has not been removed.");
    }

    /**
     * Validate the Counters view data model.
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
    public void testDisplayingDataSeries() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        // Setup the chart viewer
        IViewPart viewSite = getSWTBotView().getViewReference().getView(true);
        assertTrue(viewSite instanceof CounterView);
        final TmfCommonXAxisChartViewer chartViewer = (TmfCommonXAxisChartViewer) getChartViewer(viewSite);
        assertNotNull(chartViewer);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));
        chartViewer.setNbPoints(NUMBER_OF_POINTS);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        final Chart chart = getChart();
        assertNotNull(chart);
        assertEquals(0, chart.getSeriesSet().getSeries().length);

        // Check the counter entry
        SWTBotTree treeBot = getSWTBotView().bot().tree();
        WaitUtils.waitUntil(tree -> tree.rowCount() >= 1, treeBot, "The tree viewer did not finish loading.");
        SWTBotTreeItem root = treeBot.getTreeItem(TRACE_NAME);
        SWTBotTreeItem counter = retrieveTreeItem(root, COUNTER_NAME);
        counter.check();
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));
        WaitUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 1, chart, "The data series did not load.");

        // Ensure the data series has the correct styling
        verifySeriesStyle(MAIN_SERIES_NAME, ISeries.SeriesType.LINE, BLUE, LineStyle.SOLID, false);

        // Ensure the data model is valid
        WaitUtils.waitUntil(json -> isChartDataValid(chart, json, MAIN_SERIES_NAME),
                "resources/minor_faults-res50.json", "The chart data is not valid.");
    }

    private @Nullable SWTBotTreeItem retrieveTreeItem(@NonNull SWTBotTreeItem rootItem, @NonNull String id) {
        if (rootItem.getNodes().contains(id)) {
            return rootItem.getNode(id);
        }

        for (SWTBotTreeItem child : rootItem.getItems()) {
            SWTBotTreeItem grandChild = retrieveTreeItem(child, id);
            if (grandChild != null) {
                return grandChild;
            }
        }

        return null;
    }

    @Override
    protected @NonNull String getMainSeriesName() {
        return MAIN_SERIES_NAME;
    }

    @Override
    protected @NonNull String getTitle() {
        return COUNTERS_VIEW_TITLE;
    }

    @Override
    protected String getViewID() {
        return COUNTERS_VIEW_ID;
    }

    @Override
    protected ITmfTrace getTestTrace() {
        return CtfTmfTestTraceUtils.getTrace(CtfTestTrace.KERNEL_VM);
    }

}
