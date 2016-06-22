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
import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.latency.PatternDensityView;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;

/**
 * Test of the pattern density view
 *
 * @author Jean-Christian Kouame
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class PatternDensityViewTest extends PatternLatencyViewTestBase {

    private static final String COLUMN_HEADER = "Name";
    private static final String SYSTEM_CALL_PREFIX = "sys_";
    private static final String VIEW_ID = PatternDensityView.ID;
    private static final String VIEW_TITLE = "Latency vs Count";

    private AbstractSegmentStoreDensityView fDensityView;
    private AbstractSegmentStoreTableViewer fDensityViewer;
    private Chart fDensityChart;

    /**
     * Set the density viewer
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     *
     * @throws NoSuchFieldException
     *             Field not available
     * @throws IllegalAccessException
     *             Field is inaccessible
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     */
    public void setDensityViewer() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        SWTBotView viewBot = fBot.viewById(VIEW_ID);
        final IViewReference viewReference = viewBot.getViewReference();
        IViewPart viewPart = UIThreadRunnable.syncExec(new Result<IViewPart>() {
            @Override
            public IViewPart run() {
                return viewReference.getView(true);
            }
        });
        assertNotNull(viewPart);
        if (!(viewPart instanceof PatternDensityView)) {
            fail("Could not instanciate view");
        }
        fDensityView = (PatternDensityView) viewPart;

        /*
         * Use reflection to access the table viewer
         */
        final Field field = AbstractSegmentStoreDensityView.class.getDeclaredField("fTableViewer");
        field.setAccessible(true);
        fDensityViewer = (AbstractSegmentStoreTableViewer) field.get(fDensityView);
        fDensityChart = viewBot.bot().widget(WidgetOfType.widgetOfType(Chart.class));
        assertNotNull(fDensityViewer);
    }

    /**
     * Test the pattern density view and its viewers data
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     *
     * @throws NoSuchFieldException
     *             Field not available
     * @throws IllegalAccessException
     *             Field is inaccessible
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     *
     */
    @Test
    public void testWithTrace() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        setDensityViewer();
        SWTBotUtils.waitForJobs();

        //Test the table content
        SWTBotTable tableBot = new SWTBotTable(fDensityViewer.getTableViewer().getTable());
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, SYSTEM_CALL_PREFIX, 0, 3));
        tableBot.header(COLUMN_HEADER).click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, SYSTEM_CALL_PREFIX, 0, 3));
        tableBot.header(COLUMN_HEADER).click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, SYSTEM_CALL_PREFIX, 0, 3));

        //Test the chart content
        final Chart densityChart = fDensityChart;
        assertNotNull(densityChart);
        fBot.waitUntil(ConditionHelpers.numberOfSeries(densityChart, 1));

        SWTBotChart chartBot = new SWTBotChart(densityChart);
        assertVisible(chartBot);

        ISeriesSet seriesSet = fDensityChart.getSeriesSet();
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
        return VIEW_ID;
    }

    @Override
    protected String getViewTitle() {
        return VIEW_TITLE;
    }
}
