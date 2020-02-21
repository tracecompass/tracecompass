/*******************************************************************************
 * Copyright (c) 2016, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.ui.swtbot.tests.latency;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertVisible;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.PatternScatterGraphView;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

/**
 * Tests of the pattern scatter chart view
 *
 * @author Jean-Christian Kouame
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class PatternScatterChartViewTest extends PatternLatencyViewTestBase {

    private static final String VIEW_ID = PatternScatterGraphView.ID;
    private static final String VIEW_TITLE = "Latency vs Time";
    private Chart fScatterChart;

    private TmfXYChartViewer getChartViewer() {
        SWTBotView viewBot = fBot.viewById(VIEW_ID);
        PatternScatterGraphView viewPart = (PatternScatterGraphView) viewBot.getViewReference().getView(true);
        TmfXYChartViewer chartViewer = viewPart.getChartViewer();
        fScatterChart = chartViewer.getSwtChart();
        return chartViewer;
    }

    /**
     * Test the pattern latency scatter graph. This method test if the chart has one
     * series and the series has data
     */
    @Test
    public void testWithTrace() {

        // Get the chart viewer and wait for the view to be ready
        WaitUtils.waitForJobs();
        TmfXYChartViewer chartViewer = getChartViewer();
        assertNotNull(chartViewer);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        // Check all the items in the tree
        final Chart chart = fScatterChart;
        assertNotNull(chart);
        SWTBotView viewBot = fBot.viewById(VIEW_ID);
        SWTBotTreeItem[] items = viewBot.bot().tree().getAllItems();
        for (SWTBotTreeItem item : items) {
            item.check();
        }

        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length > 0, chart, "No data available");

        // Look at the presence of data in the chart
        SWTBotChart chartBot = new SWTBotChart(chart);
        assertVisible(chartBot);
        final Range range = chart.getAxisSet().getXAxes()[0].getRange();
        assertEquals(100000000, range.upper - range.lower, 0);
        ISeriesSet seriesSet = fScatterChart.getSeriesSet();
        assertNotNull(seriesSet);
        ISeries[] series = seriesSet.getSeries();
        assertNotNull(series);

        // Verify that the chart has more than 1 series
        assertTrue(series.length > 1);
        // Verify that each series has data
        for (int i = 0; i < series.length; i++) {
            assertTrue("Verifying series " + i, series[i].getXSeries().length > 0);
        }
    }

    private static class SWTBotChart extends AbstractSWTBotControl<Chart> {
        public SWTBotChart(Chart w) throws WidgetNotFoundException {
            super(w);
        }
    }

    @Override
    protected String getViewId() {
        return VIEW_ID;
    }

    @Override
    protected String getViewTitle() {
        return VIEW_TITLE;
    }
}
