/*******************************************************************************
 * Copyright (c) 2017, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.counters.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
 * @deprecated replaced by {@link NewCounterViewTest}
 */
@Deprecated
public class CounterViewTest extends XYDataProviderBaseTest {

    private static final int NUMBER_OF_POINTS = 50;
    private static final RGB BLUE = new RGB(0, 0, 255);
    private static final @NonNull String TRACE_NAME = "kernel_vm";
    private static final @NonNull String COUNTER_NAME = "minor_faults";
    private static final @NonNull String COUNTERS_VIEW_TITLE = "Counters (Legacy)";
    private static final @NonNull String MAIN_SERIES_NAME = "kernel_vm/Ungrouped/minor_faults";
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
        assertNotNull(root);
        SWTBotTreeItem counter = retrieveTreeItem(root, COUNTER_NAME);
        assertNotNull(counter);

        // Check all elements of the tree
        root.check();
        WaitUtils.waitUntil(SWTBotTreeItem::isChecked, root, "Root entry was not checked");
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
     */
    @Test
    public void testDisplayingDataSeries() {
        // Setup the chart viewer
        IViewPart viewPart = getSWTBotView().getViewReference().getView(true);
        assertTrue(viewPart instanceof CounterView);
        final TmfCommonXAxisChartViewer chartViewer = (TmfCommonXAxisChartViewer) getChartViewer(viewPart);
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
        assertNotNull(counter);
        counter.check();
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));
        WaitUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 1, chart, "The data series did not load.");

        // Ensure the data series has the correct styling
        verifySeriesStyle(MAIN_SERIES_NAME, ISeries.SeriesType.LINE, BLUE, LineStyle.SOLID, false);

        // Ensure the data model is valid
        WaitUtils.waitUntil(json -> isChartDataValid(chart, json, MAIN_SERIES_NAME),
                "resources/minor_faults-res50.json", "The chart data is not valid.");
    }

    private @Nullable SWTBotTreeItem retrieveTreeItem(SWTBotTreeItem rootItem, @NonNull String id) {
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

    @Override
    protected void disposeTestTrace() {
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.KERNEL_VM);
    }
}
