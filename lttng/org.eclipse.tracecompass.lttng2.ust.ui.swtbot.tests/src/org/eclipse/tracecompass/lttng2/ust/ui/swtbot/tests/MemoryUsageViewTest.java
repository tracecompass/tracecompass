/*******************************************************************************
 * Copyright (c) 2015, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage.UstMemoryUsageView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.views.xychart.XYDataProviderBaseTest;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfCommonXAxisChartViewer;
import org.eclipse.ui.IViewPart;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.swtchart.LineStyle;

/**
 * Test for the {@link UstMemoryUsageView} in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class MemoryUsageViewTest extends XYDataProviderBaseTest {

    private static final int EXPECTED_NUM_SERIES = 4;

    private static final RGB RED = new RGB(255, 0, 0);
    private static final RGB GREEN_BLUE = new RGB(0, 255, 255);
    private static final RGB GREEN = new RGB(0, 255, 0);
    private static final RGB MAGENTA = new RGB(255, 0, 255);

    private static final @NonNull String TITLE = "Relative Kernel Memory Usage";

    private static final @NonNull String FIRST_SERIES_NAME = "memory:10611";
    private static final @NonNull String SECOND_SERIES_NAME = "memory:10618";
    private static final @NonNull String THIRD_SERIES_NAME = "memory:10604";
    private static final @NonNull String FOURTH_SERIES_NAME = "memory:10613";

    /**
     * Test if Memory Usage is populated
     */
    @Test
    public void testOpenMemoryUsage() {
        SWTBotView viewBot = fBot.viewById(UstMemoryUsageView.ID);
        viewBot.setFocus();

        // Do some basic validation
        Matcher<Chart> matcher = WidgetOfType.widgetOfType(Chart.class);
        Chart chart = viewBot.bot().widget(matcher);

        checkAllEntries();

        // Verify that the chart has 4 series
        fBot.waitUntil(ConditionHelpers.numberOfSeries(chart, EXPECTED_NUM_SERIES));

        ISeriesSet seriesSet = chart.getSeriesSet();
        ISeries<?>[] series = seriesSet.getSeries();
        // Verify that each series is a ILineSeries
        for (ISeries<?> serie : series) {
            assertTrue(serie instanceof ILineSeries);
        }
    }

    /**
     * Test Memory Usage data model
     */
    @Test
    public void testMemoryUsage() {
        IViewPart viewPart = getSWTBotView().getViewReference().getView(true);
        assertTrue(viewPart instanceof UstMemoryUsageView);
        final TmfCommonXAxisChartViewer chartViewer = (TmfCommonXAxisChartViewer) getChartViewer(viewPart);
        assertNotNull(chartViewer);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        final Chart chart = getChart();
        assertNotNull(chart);

        checkAllEntries();

        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length > 3, chart, "No data available");
        chartViewer.setNbPoints(50);

        /* Test data model*/
        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json, FIRST_SERIES_NAME, SECOND_SERIES_NAME, THIRD_SERIES_NAME, FOURTH_SERIES_NAME), "resources/memory-res50.json", "Chart data is not valid");

        /* Test type, style and color of series */
        verifyChartStyle();
    }

    private void verifyChartStyle() {
        verifySeriesStyle(FIRST_SERIES_NAME, ISeries.SeriesType.LINE, RED, LineStyle.SOLID, false);
        verifySeriesStyle(SECOND_SERIES_NAME, ISeries.SeriesType.LINE, MAGENTA, LineStyle.SOLID, false);
        verifySeriesStyle(THIRD_SERIES_NAME, ISeries.SeriesType.LINE, GREEN, LineStyle.SOLID, false);
        verifySeriesStyle(FOURTH_SERIES_NAME, ISeries.SeriesType.LINE, GREEN_BLUE, LineStyle.SOLID, false);
    }

    /**
     * Ensure that the tree is loaded and then check all entries
     */
    private void checkAllEntries() {
        SWTBot bot = getSWTBotView().bot();

        SWTBotUtils.waitUntil(b -> b.tree().visibleRowCount() == 5, bot,
                "Incorrect number of tree entries, expected 5, was " + bot.tree().visibleRowCount());

        for (SWTBotTreeItem entry : bot.tree().getAllItems()) {
            entry.check();
        }
    }

    @Override
    protected @NonNull String getMainSeriesName() {
        return FIRST_SERIES_NAME;
    }

    @Override
    protected @NonNull String getTitle() {
        return TITLE;
    }

    @Override
    protected String getViewID() {
        return UstMemoryUsageView.ID;
    }

    @Override
    protected ITmfTrace getTestTrace() {
        return CtfTmfTestTraceUtils.getTrace(CtfTestTrace.MEMORY_ANALYSIS);
    }

    @Override
    protected void disposeTestTrace() {
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.MEMORY_ANALYSIS);
    }
}