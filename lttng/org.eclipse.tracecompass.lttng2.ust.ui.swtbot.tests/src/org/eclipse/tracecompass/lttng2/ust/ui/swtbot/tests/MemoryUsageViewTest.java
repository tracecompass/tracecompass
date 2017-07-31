/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage.MemoryUsageView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.XYDataProviderBaseTest;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.eclipse.ui.IViewPart;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.LineStyle;

/**
 * Test for the Memory Usage view in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class MemoryUsageViewTest extends XYDataProviderBaseTest {

    private static final int EXPECTED_NUM_SERIES = 4;

    private static final RGB RED = new RGB(255, 0, 0);
    private static final RGB BLUE = new RGB(0, 0, 255);
    private static final RGB GREEN = new RGB(0, 255, 0);
    private static final RGB MAGENTA = new RGB(255, 0, 255);

    private static final @NonNull String TITLE = "Relative Kernel Memory Usage";

    private static final @NonNull String FIRST_SERIES_NAME = "challenger (10611)";
    private static final @NonNull String SECOND_SERIES_NAME = "master_player (10618)";
    private static final @NonNull String THIRD_SERIES_NAME = "challenger (10604)";
    private static final @NonNull String FOURTH_SERIES_NAME = "master_player (10613)";

    /**
     * Test if Memory Usage is populated
     */
    @Test
    public void testOpenMemoryUsage() {
        SWTBotView viewBot = fBot.viewById(MemoryUsageView.ID);
        viewBot.setFocus();

        // Do some basic validation
        Matcher<Chart> matcher = WidgetOfType.widgetOfType(Chart.class);
        Chart chart = viewBot.bot().widget(matcher);

        // Verify that the chart has 4 series
        fBot.waitUntil(ConditionHelpers.numberOfSeries(chart, EXPECTED_NUM_SERIES));

        ISeriesSet seriesSet = chart.getSeriesSet();
        ISeries[] series = seriesSet.getSeries();
        // Verify that each series is a ILineSeries
        for (int i = 0; i < series.length; i++) {
            assertTrue(series[i] instanceof ILineSeries);
        }
    }

    /**
     * Test Memory Usage data model
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
    public void testMemoryUsage() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        IViewPart viewSite = getSWTBotView().getViewReference().getView(true);
        assertTrue(viewSite instanceof MemoryUsageView);
        final TmfCommonXLineChartViewer chartViewer = getChartViewer(viewSite);
        assertNotNull(chartViewer);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        final Chart chart = getChart();
        assertNotNull(chart);

        WaitUtils.waitUntil(c -> c.getSeriesSet().getSeries().length > 3, chart, "No data available");
        chartViewer.setNbPoints(50);

        /* Test data model*/
        WaitUtils.waitUntil(json -> isChartDataValid(chart, json, SECOND_SERIES_NAME, THIRD_SERIES_NAME, FOURTH_SERIES_NAME), "resources/memory-res50.json", "Chart data is not valid");

        /* Test type, style and color of series */
        verifyChartStyle();
    }

    private void verifyChartStyle() {
        verifySeriesStyle(FIRST_SERIES_NAME, ISeries.SeriesType.LINE, BLUE, LineStyle.SOLID, false);
        verifySeriesStyle(SECOND_SERIES_NAME, ISeries.SeriesType.LINE, RED, LineStyle.SOLID, false);
        verifySeriesStyle(THIRD_SERIES_NAME, ISeries.SeriesType.LINE, GREEN, LineStyle.SOLID, false);
        verifySeriesStyle(FOURTH_SERIES_NAME, ISeries.SeriesType.LINE, MAGENTA, LineStyle.SOLID, false);
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
        return MemoryUsageView.ID;
    }

    @Override
    protected ITmfTrace getTestTrace() {
        return CtfTmfTestTraceUtils.getTrace(CtfTestTrace.MEMORY_ANALYSIS);
    }
}