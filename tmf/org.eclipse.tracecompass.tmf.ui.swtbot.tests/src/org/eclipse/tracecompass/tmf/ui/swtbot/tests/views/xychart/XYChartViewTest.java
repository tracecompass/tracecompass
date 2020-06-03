/**********************************************************************
 * Copyright (c) 2019, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.views.xychart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.TmfContext;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotEclipseSwtChart;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.xychart.TmfChartView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for XY Chart views in Trace Compass.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class XYChartViewTest {

    private static final Logger fLogger = Logger.getRootLogger();

    private SWTBotView fViewBot;

    private TmfTraceStub fTrace;

    private SWTWorkbenchBot fBot;

    private TmfXYChartViewer fXyViewer;

    private static final TmfTimeRange INITIAL_WINDOW_RANGE = new TmfTimeRange(TmfTimestamp.fromNanos(20), TmfTimestamp.fromNanos(100));

    /**
     * Set up for test
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
    }

    /**
     * Before the test is run, make the view see the items.
     *
     * Reset the perspective and close all the views.
     *
     * @throws TmfTraceException
     *             could not load a trace
     */
    @Before
    public void before() throws TmfTraceException {
        fBot = new SWTWorkbenchBot();
        fBot.closeAllEditors();
        for (SWTBotView viewBot : fBot.views()) {
            viewBot.close();
        }
        fTrace = new TmfTraceStub() {

            @Override
            public @NonNull String getName() {
                return "Stub";
            }

            @Override
            public TmfContext seekEvent(ITmfLocation location) {
                return new TmfContext();
            }
        };
        fTrace.setStartTime(TmfTimestamp.fromNanos(0));

        fTrace.setEndTime(TmfTimestamp.fromNanos(180));

        TmfTraceStub trace = fTrace;
        trace.initialize(null, "", ITmfEvent.class);
        assertNotNull(trace);

        // Register trace to trace manager
        UIThreadRunnable.syncExec(() -> TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, trace, null)));

        // Open view
        SWTBotUtils.openView(XYChartViewStub.ID);
        fViewBot = fBot.viewById(XYChartViewStub.ID);
        fViewBot.show();

        TmfChartView viewPart = (TmfChartView) fViewBot.getViewReference().getView(true);
        fXyViewer = viewPart.getChartViewer();

        // Wait till SWT chart is constructed
        fViewBot.bot().waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return fXyViewer.getSwtChart() != null;
            }
            @Override
            public String getFailureMessage() {
                return "SWT Chart is null";
            }
        });

        // Wait for trace to be loaded
        resetTimeRange();
    }


    /**
     * Clean up after a test, reset the views.
     */
    @After
    public void after() {
        TmfTraceStub trace = fTrace;
        assertNotNull(trace);
        UIThreadRunnable.syncExec(() -> TmfSignalManager.dispatchSignal(new TmfTraceClosedSignal(this, trace)));
        fViewBot.close();
        fBot.waitUntil(ConditionHelpers.viewIsClosed(fViewBot));
        fTrace.dispose();
    }

    /**
     * Put things back the way they were
     */
    @AfterClass
    public static void afterClass() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.closeAllEditors();
        fLogger.removeAllAppenders();
    }

    /**
     * Test horizontal zoom, we can see a rounding error
     */
    @Ignore
    @Test
    public void testHorizontalZoom() {
        fViewBot.setFocus();

        assertEquals(80, fXyViewer.getWindowDuration());

        SWTBotEclipseSwtChart xyChart = new SWTBotEclipseSwtChart(fViewBot.bot());

        fireKeyInGraph(xyChart, '=');
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, 52));
        fireKeyInGraph(xyChart, '+');
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, 34));
        fireKeyInGraph(xyChart, '-');
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, 51));
        fireKeyInGraph(xyChart, '-');
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, 77));

        resetTimeRange();

        /* Zoom using zoom-in and zoom-out buttons */
        SWTBotToolbarButton button = fViewBot.toolbarButton("Zoom In");
        button.click();
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, 52));
        button = fViewBot.toolbarButton("Zoom Out");
        button.click();
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, 78));

        /*
         *  Note that 'w' and 's' zooming is based on mouse position. Just check if
         *  window range was increased or decreased to avoid inaccuracy due to
         *  the mouse position in test environment.
         */
        long previousRange = fXyViewer.getWindowDuration();
        fireKeyInGraph(xyChart, 'w');
        fViewBot.bot().waitUntil(new SeriesUpdatedCondition(fXyViewer, previousRange, false));
        previousRange = fXyViewer.getWindowDuration();
        fireKeyInGraph(xyChart, 's');
        fViewBot.bot().waitUntil(new SeriesUpdatedCondition(fXyViewer, previousRange, true));
    }

    /**
     * Test zoom to selection
     */
    @Test
    public void testZoomToSelection() {
        SWTBotEclipseSwtChart xyChart = new SWTBotEclipseSwtChart(fViewBot.bot());

        xyChart.setFocus();

        assertEquals(80, fXyViewer.getWindowDuration());

        /* set selection to trace start time */
        ITmfTimestamp selStartTime = TmfTimestamp.fromNanos(30L);
        ITmfTimestamp selEndTime = TmfTimestamp.fromNanos(80L);
        TmfTimeRange range = new TmfTimeRange(selStartTime, selEndTime);
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, selStartTime, selEndTime));
        // Wait till selection is finished
        fViewBot.bot().waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                return (fXyViewer.getSelectionEndTime() - fXyViewer.getSelectionBeginTime()) == getDuration(range);
            }
            @Override
            public String getFailureMessage() {
                return "SWT Chart is null";
            }
        });
        fireKeyInGraph(xyChart, 'z');
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, getDuration(range)));
    }

    /**
     * Test 'a' and 'd' navigation
     */
    @Test
    public void testKeyboardNavigation() {
        SWTBotEclipseSwtChart xyChart = new SWTBotEclipseSwtChart(fViewBot.bot());
        xyChart.setFocus();
        assertEquals(80, fXyViewer.getWindowDuration());

        TmfTimeRange updatedWindowRange = new TmfTimeRange(TmfTimestamp.fromNanos(40), TmfTimestamp.fromNanos(120));

        // move to the right
        fireKeyInGraph(xyChart, 'd');
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, getDuration(updatedWindowRange)));

        // move to the left
        fireKeyInGraph(xyChart, 'a');
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, getDuration(INITIAL_WINDOW_RANGE)));
    }

    // ------------------------------------------------------------------------
    // Helper methods
    // ------------------------------------------------------------------------

    private static void fireKeyInGraph(SWTBotEclipseSwtChart chart, char c, int... modifiers) {
        chart.setFocus();
        // Move mouse to middle of the chart
        chart.moveMouseToWidget();
        int mask = 0;
        for (int modifier : modifiers) {
            mask |= modifier;
        }
        chart.pressShortcut(mask, c);
    }

    private static long getDuration(TmfTimeRange range) {
        return range.getEndTime().getValue() - range.getStartTime().getValue();
    }

    private void resetTimeRange() {
        TmfWindowRangeUpdatedSignal signal = new TmfWindowRangeUpdatedSignal(this, INITIAL_WINDOW_RANGE);
        TmfSignalManager.dispatchSignal(signal);
        fViewBot.bot().waitUntil(new SeriesCondition(fXyViewer, getDuration(INITIAL_WINDOW_RANGE)));
    }

    private static class SeriesCondition extends DefaultCondition {
        private TmfXYChartViewer fViewer;
        private long fExpectedRange;
        private Exception fExecption = null;

        public SeriesCondition(TmfXYChartViewer view, long expectedRange) {
            fViewer = view;
            fExpectedRange = expectedRange;
        }

        @Override
        public boolean test() throws Exception {
            try {
                Chart chart = fViewer.getSwtChart();
                ISeriesSet set = chart.getSeriesSet();
                if (set == null) {
                    return false;
                }
                ISeries<?>[] series = set.getSeries();
                return series != null && series.length > 0 && fExpectedRange == fViewer.getWindowDuration();
            } catch (Exception e) {
                fExecption = e;
                throw e;
            }
        }

        @Override
        public String getFailureMessage() {
            return "Expected window range (" + fExpectedRange + ") not achieved. Actual=" + fViewer.getWindowDuration() + (fExecption == null ? "" : (". Exception: " + fExecption));
        }
    }

    private class SeriesUpdatedCondition extends DefaultCondition {
        TmfXYChartViewer fView;
        long fPreviousRange;
        boolean fIsIncreased;
        private Exception fExecption = null;

        public SeriesUpdatedCondition(TmfXYChartViewer view, long previousRange, boolean increased) {
            fView = view;
            fPreviousRange = previousRange;
            fIsIncreased = increased;
        }

        @Override
        public boolean test() throws Exception {
            try {
                Chart chart = fView.getSwtChart();
                ISeriesSet set = chart.getSeriesSet();
                if (set == null) {
                    return false;
                }
                ISeries<?>[] series = set.getSeries();
                if (series == null) {
                    return false;
                }
                long newRange = fView.getWindowDuration();
                if (fIsIncreased) {
                    return newRange > fPreviousRange;
                }
                return newRange < fPreviousRange;
            } catch (Exception e) {
                fExecption = e;
                throw e;
            }
        }

        @Override
        public String getFailureMessage() {
            return "Window range didn't " + (fIsIncreased ? "increase" : "decrease") +
                    " (previous: " + fPreviousRange + ", actual: " + fView.getWindowDuration() + ")" + (fExecption == null ? "" : (". Exception: " + fExecption));
        }
    }
}
