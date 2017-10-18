/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.analysis.os.linux.core.signals.TmfCpuSelectedSignal;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.cpuusage.CpuUsageView;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.ResourcesView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.XYDataProviderBaseTest;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.ui.IViewPart;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.swtchart.Chart;
import org.swtchart.ISeries;
import org.swtchart.LineStyle;

/**
 * SWTBot tests for {@link ResourcesView} and {@link CpuUsageView}
 *
 * @author Matthew Khouzam
 */
public class ResourcesAndCpuViewTest extends XYDataProviderBaseTest {

    private static final RGB RED = new RGB(255, 0, 0);
    private static final RGB BLUE = new RGB(0, 0, 255);
    private static final RGB GREEN = new RGB(0, 255, 0);

    private static final @NonNull String TOTAL_SERIES_NAME = "total:bug446190";
    private static final @NonNull String TRACE_NAME = "bug446190";
    private static final @NonNull String TITLE = "CPU Usage";
    private static final @NonNull String SELECTED_THREAD_TID = "482";
    private static final @NonNull String SELECTED_THREAD_SERIES = "bug446190:482";
    private static final String OTHERTHREAD_SERIES = "bug446190:496";

    private static final @NonNull ITmfTimestamp TRACE_START = TmfTimestamp.fromNanos(1412670961211260539L);
    private static final @NonNull ITmfTimestamp TRACE_END = TmfTimestamp.fromNanos(1412670967217750839L);

    private SWTBotView fViewBotRv;

    /**
     * Before Test
     */
    @Override
    @Before
    public void setup() {
        super.setup();

        SWTBotUtils.openView(ResourcesView.ID);
        fViewBotRv = fBot.viewById(ResourcesView.ID);
        getSWTBotView().setFocus();
    }

    /**
     * Simple test to check the CPU Usage view after getting signals.
     */
    @Test
    public void testSignals() {
        Widget widget = fViewBotRv.getWidget();
        assertNotNull(widget);
        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(activeTrace);

        // clear everything
        TmfCpuSelectedSignal signal = new TmfCpuSelectedSignal(widget, -1, activeTrace);
        broadcast(signal);
        assertEquals("Before signal - CPU Usage Title", "CPU Usage", getChartTitle());
        assertEquals("Before signal - Thread Table", 12, getTableCount());
        fViewBotRv.setFocus();

        // select cpu 1
        signal = new TmfCpuSelectedSignal(widget, 1, activeTrace);
        broadcast(signal);
        assertEquals("After signal - CPU Usage Title", "CPU Usage 1", getChartTitle());
        assertEquals("After signal - Thread Table", 4, getTableCount());

        // select cpu 3 and 1
        signal = new TmfCpuSelectedSignal(widget, 3, activeTrace);
        broadcast(signal);
        assertEquals("After signal 2 - CPU Usage Title", "CPU Usage 1, 3", getChartTitle());
        assertEquals("After signal 2 - Thread Table", 8, getTableCount());

        // reset
        signal = new TmfCpuSelectedSignal(widget, -1, activeTrace);
        broadcast(signal);
        assertEquals("After signal clear - CPU Usage Title", "CPU Usage", getChartTitle());
        assertEquals("After signal clear - Thread Table", 12, getTableCount());
    }

    /**
     * Simple test to check the CPU Usage view after getting signals.
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
    public void testCpuView() throws NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        IViewPart viewSite = getSWTBotView().getViewReference().getView(true);
        assertTrue(viewSite instanceof CpuUsageView);
        final TmfCommonXAxisChartViewer chartViewer = (TmfCommonXAxisChartViewer) getChartViewer(viewSite);
        assertNotNull(chartViewer);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        final Chart chart = getChart();
        assertNotNull(chart);

        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length > 0, chart, "No data available");
        chartViewer.setNbPoints(10);
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        /* Test data model */
        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json), "resources/cpuusage/cpu-usage-res10.json", "Chart data is not valid");

        /* Test chart style */
        verifySeriesStyle(TOTAL_SERIES_NAME, ISeries.SeriesType.LINE, BLUE, LineStyle.SOLID, false);

        /* Select a thread */
        SWTBotTreeItem rootEntry = getSWTBotView().bot().tree().getTreeItem(TRACE_NAME);
        SWTBotUtils.waitUntil(tree -> getTableCount() >= 8, rootEntry, "Did not finish loading");
        SWTBotTreeItem selectedTheadNode = rootEntry.getNode(SELECTED_THREAD_TID);
        selectedTheadNode.check();
        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 2, chart, "Only total available");

        /* Test data model */
        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json, SELECTED_THREAD_SERIES), "resources/cpuusage/cpu-usage-res10Selected.json", "Chart data is not valid");

        /* Test chart style */
        verifySeriesStyle(SELECTED_THREAD_SERIES, ISeries.SeriesType.LINE, RED, LineStyle.SOLID, true);
        selectedTheadNode.uncheck();

        /* Selected an another thread and test in HD */
        String otherSelectedThread = "496";
        SWTBotTreeItem otherSelectedThreadNode = rootEntry.getNode(otherSelectedThread);
        otherSelectedThreadNode.check();
        chartViewer.setNbPoints(100);
        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 2, chart, "Only total available");
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        /* Test data model */
        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json, OTHERTHREAD_SERIES), "resources/cpuusage/cpu-usage-res100Selected.json", "Chart data is not valid");

        /* Test chart style */
        verifySeriesStyle(OTHERTHREAD_SERIES, ISeries.SeriesType.LINE, GREEN, LineStyle.SOLID, true);

        /*
         * Test new TimeRange
         */
        chartViewer.setNbPoints(10);
        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 2, chart, "Only total available");
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(activeTrace);
        fViewBotRv.getToolbarButtons().stream().filter(button -> button.getToolTipText().contains("Reset")).findAny().get().click();
        fBot.waitUntil(ConditionHelpers.windowRange(new TmfTimeRange(TRACE_START, TRACE_END)));
        SWTBotUtils.waitUntil(c -> c.getSeriesSet().getSeries().length >= 2, chart, "Only total available");
        fBot.waitUntil(ConditionHelpers.xyViewerIsReadyCondition(chartViewer));

        /* Test data model */
        SWTBotUtils.waitUntil(json -> isChartDataValid(chart, json, OTHERTHREAD_SERIES), "resources/cpuusage/cpu-usage-all-res10.json", "Chart data is not valid");

        /* Test chart style */
        verifySeriesStyle(OTHERTHREAD_SERIES, ISeries.SeriesType.LINE, GREEN, LineStyle.SOLID, true);
    }

    @Override
    protected String getMainSeriesName() {
        return TOTAL_SERIES_NAME;
    }

    @Override
    protected String getTitle() {
        return TITLE;
    }

    @Override
    protected String getViewID() {
        return CpuUsageView.ID;
    }

    @Override
    protected ITmfTrace getTestTrace() {
        return CtfTmfTestTraceUtils.getTrace(CtfTestTrace.ARM_64_BIT_HEADER);
    }

    private static void broadcast(TmfSignal signal) {
        UIThreadRunnable.syncExec(() -> TmfSignalManager.dispatchSignal(signal));
        WaitUtils.waitForJobs();
    }

    private String getChartTitle() {
        getSWTBotView().setFocus();
        // Do some basic validation
        Matcher<Chart> matcher = WidgetOfType.widgetOfType(Chart.class);
        Chart chart = getSWTBotView().bot().widget(matcher);
        return chart.getTitle().getText();
    }

    private int getTableCount() {
        getSWTBotView().setFocus();
        // Do some basic validation
        Matcher<Tree> matcher = WidgetOfType.widgetOfType(Tree.class);
        SWTBotTree treeBot = new SWTBotTree(getSWTBotView().bot().widget(matcher));
        int count = 0;
        for (SWTBotTreeItem bot : treeBot.getTreeItem(getTestTrace().getName()).getItems()) {
            final String text = bot.getText();
            if (!text.isEmpty()) {
                count++;
            }
        }
        return count;
    }
}
