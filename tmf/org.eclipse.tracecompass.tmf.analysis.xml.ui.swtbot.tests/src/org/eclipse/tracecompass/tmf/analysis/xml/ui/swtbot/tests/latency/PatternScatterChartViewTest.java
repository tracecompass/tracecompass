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

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.PatternScatterGraphView;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
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

    private static final String VIEW_TITLE = "Latency vs Time";

    /**
     * Test the pattern latency scatter graph. This method test if the chart has
     * one series and the series has data
     */
    @Test
    public void testWithTrace() {
        SWTBotView viewBot = fBot.viewById(PatternScatterGraphView.ID);
        viewBot.setFocus();
        WaitUtils.waitForJobs();

        Chart chart = viewBot.bot().widget(WidgetOfType.widgetOfType(Chart.class));
        assertNotNull(chart);
        fBot.waitUntil(ConditionHelpers.numberOfSeries(chart, 1));

        SWTBotChart chartBot = new SWTBotChart(chart);
        assertVisible(chartBot);
        final Range range = chart.getAxisSet().getXAxes()[0].getRange();
        assertEquals(100000000, range.upper - range.lower, 0);
        ISeriesSet seriesSet = chart.getSeriesSet();
        assertNotNull(seriesSet);
        ISeries[] series = seriesSet.getSeries();
        assertNotNull(series);

        // Verify that the chart has 1 series
        assertEquals(1, series.length);
        // Verify that the series has data
        assertTrue(series[0].getXSeries().length > 0);
    }

    private static class SWTBotChart extends AbstractSWTBotControl<Chart> {
        public SWTBotChart(Chart w) throws WidgetNotFoundException {
            super(w);
        }
    }

    @Override
    protected String getViewId() {
        return PatternScatterGraphView.ID;
    }

    @Override
    protected String getViewTitle() {
        return VIEW_TITLE;
    }
}
