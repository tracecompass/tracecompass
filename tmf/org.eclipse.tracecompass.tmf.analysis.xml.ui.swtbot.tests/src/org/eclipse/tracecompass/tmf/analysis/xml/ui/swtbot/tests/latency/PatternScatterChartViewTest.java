/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.analysis.xml.ui.swtbot.tests.latency;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertVisible;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.PatternScatterGraphView;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
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

    private TmfXYChartViewer getChartViewer() throws NoSuchMethodException, SecurityException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        SWTBotView viewBot = fBot.viewById(VIEW_ID);
        final IViewReference viewReference = viewBot.getViewReference();
        IViewPart viewPart = UIThreadRunnable.syncExec(new Result<IViewPart>() {
            @Override
            public IViewPart run() {
                return viewReference.getView(true);
            }
        });
        assertNotNull(viewPart);
        assertTrue("Could not instanciate view", viewPart instanceof PatternScatterGraphView);
        TmfChartView chartView = (TmfChartView) viewPart;
        Method viewer = TmfChartView.class.getDeclaredMethod("getChartViewer");
        viewer.setAccessible(true);
        TmfXYChartViewer chartViewer = (TmfXYChartViewer) viewer.invoke(chartView);

        fScatterChart = viewBot.bot().widget(WidgetOfType.widgetOfType(Chart.class));
        assertNotNull(fScatterChart);
        return chartViewer;
    }

    /**
     * Test the pattern latency scatter graph. This method test if the chart has one
     * series and the series has data
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
    public void testWithTrace() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {

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
