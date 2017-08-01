/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.os.linux.ui.swtbot.tests.memoryusage;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertVisible;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.DoubleSummaryStatistics;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.kernelmemoryusage.KernelMemoryUsageView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;
import org.swtchart.Range;

/**
 * Test for the kernel memory usage view
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class KernelMemoryUsageTest {

    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String PROJECT_NAME = "test";
    private static final String VIEW_ID = KernelMemoryUsageView.ID;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private Chart fChart;
    private KernelMemoryUsageView fKernelMemoryUsageView = null;

    /**
     * Things to setup
     */
    @BeforeClass
    public static void beforeClass() {

        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBotTest");
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", bot);
        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

    }

    /**
     * Opens a line chart
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     *
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     */
    @Before
    public void setupView() throws SecurityException, IllegalArgumentException {
        SWTBotUtils.openView(VIEW_ID);
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotView viewBot = bot.viewById(VIEW_ID);
        final IViewReference viewReference = viewBot.getViewReference();
        IViewPart viewPart = UIThreadRunnable.syncExec(new Result<IViewPart>() {
            @Override
            public IViewPart run() {
                return viewReference.getView(true);
            }
        });
        assertNotNull(viewPart);
        if (!(viewPart instanceof KernelMemoryUsageView)) {
            fail("Could not instanciate view");
        }
        fKernelMemoryUsageView = (KernelMemoryUsageView) viewPart;
        fChart = viewBot.bot().widget(WidgetOfType.widgetOfType(Chart.class));
        assertNotNull(fChart);
    }

    /**
     * Closes the view
     */
    @After
    public void closeView() {
        final SWTWorkbenchBot swtWorkbenchBot = new SWTWorkbenchBot();
        SWTBotView viewBot = swtWorkbenchBot.viewById(VIEW_ID);
        viewBot.close();
    }

    /**
     * Test with an actual trace, this is more of an integration test than a
     * unit test. This test is a slow one too. If some analyses are not well
     * configured, this test will also generates null pointer exceptions. These
     * will be logged.
     *
     * @throws IOException
     *             trace not found?
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     */
    @Test
    public void testWithTrace() throws IOException, SecurityException, IllegalArgumentException {

        // create a project and open the view
        String tracePath = FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL()).getPath();
        // Update the time range to a range where there is no data intersecting
        // the bounds
        long rangeStart = 1412670961274443542L;
        long rangeEnd = 1412670961298823940L;
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotView view = bot.viewById(VIEW_ID);
        view.close();
        bot.waitUntil(ConditionHelpers.ViewIsClosed(view));
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, tracePath, TRACE_TYPE);
        WaitUtils.waitForJobs();
        setupView();
        WaitUtils.waitForJobs();

        final Chart chart = fChart;
        assertNotNull(chart);
        bot.waitUntil(ConditionHelpers.numberOfSeries(chart, 1));

        // test the initial state of the newly opened view
        SWTBotChart chartBot = new SWTBotChart(chart);
        assertVisible(chartBot);
        assertEquals("", chartBot.getToolTipText());
        @NonNull
        TmfTimeRange traceWindowRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
        assertEquals("Unexpected trace window range", 100000000, traceWindowRange.getEndTime().getValue() - traceWindowRange.getStartTime().getValue());
        Range range = chart.getAxisSet().getXAxes()[0].getRange();
        assertEquals("Unexpected X-axis range", 100000000, range.upper - range.lower, 0);
        ISeriesSet seriesSet = fChart.getSeriesSet();
        assertNotNull(seriesSet);
        ISeries[] series = seriesSet.getSeries();
        assertNotNull(series);

        TmfTimeRange windowRange = new TmfTimeRange(TmfTimestamp.fromNanos(rangeStart), TmfTimestamp.fromNanos(rangeEnd));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, windowRange));

        bot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(fKernelMemoryUsageView.getChartViewer()));

        range = chart.getAxisSet().getXAxes()[0].getRange();
        assertEquals(rangeEnd - rangeStart, range.upper - range.lower, 0);

        // Test the data in the chart

        // Verify that the chart has 1 series
        assertEquals(1, series.length);
        // Verify that each series is a ILineSeries
        for (int i = 0; i < series.length; i++) {
            assertTrue(series[i] instanceof ILineSeries);
        }
        // Verify the data with statistical data
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        for (double data : series[0].getYSeries()) {
            stats.accept(data);
        }
        assertEquals(839680, stats.getMin(), 1.0);
        assertEquals(929792, stats.getMax(), 1.0);
        // average can have some variance depending on sampling
        assertEquals(870849.823129, stats.getAverage(), 1000.0);
        bot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, bot);
    }

    private static class SWTBotChart extends AbstractSWTBotControl<Chart> {
        public SWTBotChart(Chart w) throws WidgetNotFoundException {
            super(w);
        }
    }

}
