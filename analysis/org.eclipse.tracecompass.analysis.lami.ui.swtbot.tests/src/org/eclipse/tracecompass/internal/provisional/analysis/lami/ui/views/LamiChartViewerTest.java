/*******************************************************************************
 * Copyright (c) 2017, 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.ui.views;

import static org.eclipse.swtbot.swt.finder.SWTBotAssert.assertVisible;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.Collections;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.AbstractSWTBotControl;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRootMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.analysis.lami.core.tests.shared.analysis.LamiAnalyses;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiResultTable;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiTableEntry;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types.LamiLongNumber;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartType;
import org.eclipse.tracecompass.tmf.chart.ui.swtbot.tests.shared.SWTBotCustomChartUtils;
import org.eclipse.tracecompass.tmf.chart.ui.swtbot.tests.shared.SWTBotCustomChartUtils.AxisType;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.OnDemandAnalysisManager;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers.SWTBotTestCondition;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.ActionFactory;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swtchart.Chart;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Test the LAMI custom charts.
 *
 * Note: This tests only the LAMI chart specific behavior with a few stub
 * datasets. The LAMI custom chart model should be tested in the
 * o.e.t.a.lami.core.tests package and the chart maker specificities should be
 * in the custom chart's SWTbot package.
 *
 * @author Geneviève Bastien
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class LamiChartViewerTest {

    private static final String TRACE_TYPE = "org.eclipse.linuxtools.tmf.core.tests.secondtt";
    private static final String PROJECT_NAME = "test";
    private static final String VIEW_ID = LamiReportView.VIEW_ID;
    private static final TmfTestTrace TRACE = TmfTestTrace.A_TEST_10K;

    /** The Log4j logger instance. */
    private static final Logger fLogger = NonNullUtils.checkNotNull(Logger.getRootLogger());

    private final SWTWorkbenchBot fBot = new SWTWorkbenchBot();
    private @Nullable SWTBotView fViewBot = null;
    private @Nullable LamiReportViewTabPage fCurrentTab;

    /**
     * Start the SWT bot test, register the LAMI analyses and open a trace (any
     * trace) on a new project
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

        OnDemandAnalysisManager.getInstance().registerAnalysis(LamiAnalyses.MULTIPLE_ROW.getAnalysis());
        OnDemandAnalysisManager.getInstance().registerAnalysis(LamiAnalyses.MULTIPLE_SIMILAR_ROW.getAnalysis());

        // Create the project and open the trace
        String tracePath = TRACE.getFullPath();

        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, tracePath, TRACE_TYPE);
        WaitUtils.waitForJobs();
    }

    /**
     * Delete the project and de-register the LAMI analyses
     */
    @AfterClass
    public static void afterClass() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        bot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, bot);
        fLogger.removeAllAppenders();
        OnDemandAnalysisManager.getInstance().unregisterAnalysis(LamiAnalyses.MULTIPLE_ROW.getAnalysis());
        OnDemandAnalysisManager.getInstance().unregisterAnalysis(LamiAnalyses.MULTIPLE_SIMILAR_ROW.getAnalysis());
    }

    /**
     * Reset the current perspective
     */
    @After
    public void resetPerspective() {
        SWTBotView viewBot = fViewBot;
        if (viewBot != null) {
            viewBot.close();
        }
        /*
         * UI Thread executes the reset perspective action, which opens a shell
         * to confirm the action. The current thread will click on the 'Yes'
         * button
         */
        Runnable runnable = () -> fBot.button("Yes").click();
        UIThreadRunnable.asyncExec(() -> {
            IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getWorkbenchWindows()[0];
            ActionFactory.RESET_PERSPECTIVE.create(activeWorkbenchWindow).run();
        });
        runnable.run();
    }

    /**
     * Execute the LAMI analyses and return the viewBot corresponding to the
     * newly opened view. To avoid tests interracting with each other, it is
     * preferable to use the returned view bot in a try finally statement and
     * make sure the view is closed at the end of the test.
     *
     * @param analysis
     *            The analysis to execute
     * @return The view bot
     */
    private SWTBotView executeAnalysis(LamiAnalyses analysis) {
        // Open the LAMI analysis
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        SWTBotTreeItem externalAnalyses = SWTBotUtils.getTraceProjectItem(fBot, tracesFolder, TRACE.getPath(), "External Analyses", analysis.getAnalysis().getName());
        assertNotNull(externalAnalyses);
        fBot.waitUntil(isLamiAnalysisEnabled(externalAnalyses));
        externalAnalyses.doubleClick();

        fBot.shell("External Analysis Parameters").activate();
        fBot.button("OK").click();
        WaitUtils.waitForJobs();

        SWTBotView viewBot = fBot.viewById(VIEW_ID);
        viewBot.setFocus();
        final @Nullable LamiReportView lamiView = UIThreadRunnable.syncExec((Result<@Nullable LamiReportView>) () -> {
            IViewPart viewRef = viewBot.getViewReference().getView(true);
            return (viewRef instanceof LamiReportView) ? (LamiReportView) viewRef : null;
        });
        assertNotNull(lamiView);
        SWTBotUtils.maximize(lamiView);
        fViewBot = viewBot;

        fCurrentTab = lamiView.getCurrentSelectedPage();

        return viewBot;
    }

    /**
     * Test a few scatter charts with the multiple row dataset.
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     *
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     */
    @Test
    public void testScatterMultipleRow() throws SecurityException, IllegalArgumentException {
        SWTBotView viewBot = executeAnalysis(LamiAnalyses.MULTIPLE_ROW);

        // Get the expected maximum and minimum values of each axis
        LamiResultTable resultTable = LamiAnalyses.MULTIPLE_ROW.getAnalysis().getResultTable(0);
        Long minX = Long.MAX_VALUE;
        Long maxX = Long.MIN_VALUE;
        Long minY = Long.MAX_VALUE;
        Long maxY = Long.MIN_VALUE;
        for (LamiTableEntry entry : resultTable.getEntries()) {
            Long wakeupTs = ((LamiLongNumber) entry.getValue(0)).getValue();
            Long switchTs = ((LamiLongNumber) entry.getValue(1)).getValue();
            Long latency = ((LamiLongNumber) entry.getValue(2)).getValue();
            if (wakeupTs != null) {
                minX = Math.min(minX, wakeupTs);
                maxX = Math.max(maxX, wakeupTs);
            }
            if (switchTs != null) {
                minX = Math.min(minX, switchTs);
                maxX = Math.max(maxX, switchTs);
            }
            if (latency != null) {
                minY = Math.min(minY, latency);
                maxY = Math.max(maxY, latency);
            }
        }

        // Create a new chart
        SWTBotRootMenu viewMenu = viewBot.viewMenu();
        SWTBotMenu menu = viewMenu.menu("New custom chart");
        menu.click();

        // Create a scatter chart of Wakeup timestamp vs scheduling latency
        // and Switch timestamp vs scheduling latency
        SWTBotCustomChartUtils.selectChartType(fBot, ChartType.SCATTER_CHART);
        SWTBotCustomChartUtils.addSeries(fBot, "Wakeup timestamp", Collections.singleton("Scheduling latency (ns)"));
        SWTBotCustomChartUtils.addSeries(fBot, "Switch timestamp", Collections.singleton("Scheduling latency (ns)"));
        SWTBotCustomChartUtils.confirmDialog(fBot);

        WaitUtils.waitForJobs();

        // Wait for the viewer and verify its parameters
        @Nullable Chart customChart = viewBot.bot().widget(WidgetOfType.widgetOfType(Chart.class), 0);
        Event mouseMove = new Event();
        mouseMove.type = SWT.MouseEnter;
        customChart.getDisplay().post(mouseMove);
        assertNotNull(customChart);

        fBot.waitUntil(ConditionHelpers.numberOfSeries(customChart, 2));
        SWTBotChart chartBot = new SWTBotChart(customChart);
        assertVisible(chartBot);

        // Verify the titles
        SWTBotCustomChartUtils.assertTitles(customChart, "Scheduling log", "Value (ss.SSS)", "Scheduling latency (ns)");

        // Make sure the axis formatters have the right range
        SWTBotCustomChartUtils.assertAxisRange(customChart, AxisType.X, minX, maxX);
        SWTBotCustomChartUtils.assertAxisRange(customChart, AxisType.Y, minY, maxY);
        SWTBotCustomChartUtils.assertAxisLogscale(customChart, AxisType.Y, false);

        // Verify the series titles
        SWTBotCustomChartUtils.assertSeriesTitle(customChart, ImmutableList.of("Scheduling latency by Wakeup timestamp", "Scheduling latency by Switch timestamp"));
        closeCharts();

        // Create the same chart, but with log scale enabled in Y. Make sure
        // the results are the same
        menu.click();

        SWTBotCustomChartUtils.selectChartType(fBot, ChartType.SCATTER_CHART);
        SWTBotCustomChartUtils.addSeries(fBot, "Wakeup timestamp", Collections.singleton("Scheduling latency (ns)"));
        SWTBotCustomChartUtils.addSeries(fBot, "Switch timestamp", Collections.singleton("Scheduling latency (ns)"));
        SWTBotCustomChartUtils.setLogScale(fBot, AxisType.Y);
        SWTBotCustomChartUtils.confirmDialog(fBot);

        WaitUtils.waitForJobs();

        // Wait for the viewer and verify its parameters
        customChart = viewBot.bot().widget(WidgetOfType.widgetOfType(Chart.class), 0);
        assertNotNull(customChart);
        customChart.getDisplay().post(mouseMove);

        fBot.waitUntil(ConditionHelpers.numberOfSeries(customChart, 2));
        chartBot = new SWTBotChart(customChart);
        assertVisible(chartBot);

        // Verify the titles
        SWTBotCustomChartUtils.assertTitles(customChart, "Scheduling log", "Value (ss.SSS)", "Scheduling latency (ns)");

        // Make sure the axis formatter have the right range
        SWTBotCustomChartUtils.assertAxisRange(customChart, AxisType.X, minX, maxX);
        // Logscale charts are clamped to 0
        SWTBotCustomChartUtils.assertAxisRange(customChart, AxisType.Y, 0, maxY);
        SWTBotCustomChartUtils.assertAxisLogscale(customChart, AxisType.Y, true);

        // Verify the series titles
        SWTBotCustomChartUtils.assertSeriesTitle(customChart, ImmutableList.of("Scheduling latency by Wakeup timestamp", "Scheduling latency by Switch timestamp"));
        closeCharts();

    }

    private void closeCharts() {
        LamiReportViewTabPage currentTab = fCurrentTab;
        assertNotNull(currentTab);
        UIThreadRunnable.syncExec(() -> currentTab.clearAllCustomViewers());
    }

    /**
     * Test a few charts with the multiple similar row dataset.
     *
     * @throws SecurityException
     *             If a security manager is present and any the wrong class is
     *             loaded or the class loader is not the same as its ancestor's
     *             loader.
     *
     * @throws IllegalArgumentException
     *             the object is not the correct class type
     */
    @Test
    public void testSimilarRows() throws SecurityException, IllegalArgumentException {
        SWTBotView viewBot = executeAnalysis(LamiAnalyses.MULTIPLE_SIMILAR_ROW);

        // Create a new chart
        SWTBotRootMenu viewMenu = viewBot.viewMenu();
        SWTBotMenu menu = viewMenu.menu("New custom chart");
        menu.click();

        // Create a bar chart of Wakee process (name) vs scheduling latency,
        // Priority and Target CPU
        SWTBotCustomChartUtils.selectChartType(fBot, ChartType.BAR_CHART);
        SWTBotCustomChartUtils.addSeries(fBot, "Wakee process (name)", ImmutableSet.of("Scheduling latency (ns)", "Priority", "Target CPU"));
        SWTBotCustomChartUtils.confirmDialog(fBot);

        WaitUtils.waitForJobs();

        // Wait for the viewer and verify its parameters
        @Nullable
        Chart customChart = viewBot.bot().widget(WidgetOfType.widgetOfType(Chart.class), 0);
        assertNotNull(customChart);
        Event mouseMove = new Event();
        mouseMove.type = SWT.MouseEnter;
        customChart.getDisplay().post(mouseMove);

        fBot.waitUntil(ConditionHelpers.numberOfSeries(customChart, 3));
        SWTBotChart chartBot = new SWTBotChart(customChart);
        assertVisible(chartBot);

        // Verify the titles
        SWTBotCustomChartUtils.assertTitles(customChart, "Scheduling log", "Wakee process (name)", "Value");

        // Make sure the axis formatter have the right categories and range
        String[] xValues = new String[6];
        Arrays.fill(xValues, "swapper/5");
        SWTBotCustomChartUtils.assertCategoriesAxis(customChart, AxisType.X, xValues);
        SWTBotCustomChartUtils.assertAxisRange(customChart, AxisType.Y, 0, 2);
        SWTBotCustomChartUtils.assertAxisLogscale(customChart, AxisType.Y, false);

        // Verify the series titles
        SWTBotCustomChartUtils.assertSeriesTitle(customChart, ImmutableList.of("Scheduling latency (ns)", "Priority", "Target CPU"));
        closeCharts();

        // Create a bar chart of Waker process (name) vs scheduling latency,
        // Priority and Target CPU
        menu.click();
        fBot.shell("Custom chart creation").activate();

        SWTBotCustomChartUtils.selectChartType(fBot, ChartType.BAR_CHART);
        SWTBotCustomChartUtils.addSeries(fBot, "Waker process (name)", ImmutableSet.of("Scheduling latency (ns)", "Priority", "Target CPU"));
        SWTBotCustomChartUtils.confirmDialog(fBot);

        WaitUtils.waitForJobs();

        // Wait for the viewer and verify its parameters
        customChart = viewBot.bot().widget(WidgetOfType.widgetOfType(Chart.class), 0);
        assertNotNull(customChart);
        customChart.getDisplay().post(mouseMove);

        fBot.waitUntil(ConditionHelpers.numberOfSeries(customChart, 3));
        chartBot = new SWTBotChart(customChart);
        assertVisible(chartBot);

        // Verify the titles
        SWTBotCustomChartUtils.assertTitles(customChart, "Scheduling log", "Waker process (name)", "Value");

        // Make sure the axis formatter have the right categories and range
        Arrays.fill(xValues, "?");
        SWTBotCustomChartUtils.assertCategoriesAxis(customChart, AxisType.X, xValues);
        SWTBotCustomChartUtils.assertAxisRange(customChart, AxisType.Y, 0, 2);
        SWTBotCustomChartUtils.assertAxisLogscale(customChart, AxisType.Y, false);

        // Verify the series titles
        SWTBotCustomChartUtils.assertSeriesTitle(customChart, ImmutableList.of("Scheduling latency (ns)", "Priority", "Target CPU"));
        closeCharts();
    }

    /**
     * Check if a LAMI analysis is enabled, ie the item is enabled, and not
     * striked-out
     *
     * @param item
     *            the item representing the LAMI analysis element
     * @return true or false, it should swallow all exceptions
     */
    private static ICondition isLamiAnalysisEnabled(final SWTBotTreeItem item) {
        return new SWTBotTestCondition() {

            private boolean fIsOk = false;

            @Override
            public boolean test() throws Exception {
                try {
                    if (!item.isEnabled()) {
                        return false;
                    }
                    Display.getDefault().syncExec(() -> {
                        // Make sure the object is not striked-out, this
                        // property will return a non empty StyleRange[] if if
                        // is
                        Object data = item.widget.getData("org.eclipse.jfacestyled_label_key_0");
                        if (data == null) {
                            fIsOk = false;
                            return;
                        }
                        if (data instanceof StyleRange[]) {
                            StyleRange[] sr = (StyleRange[]) data;
                            fIsOk = sr.length == 0;
                        } else {
                            fIsOk = false;
                        }
                    });

                } catch (Exception e) {
                }
                return fIsOk;
            }

            @Override
            public String getFailureMessage() {
                return "The lami analysis is not enabled";
            }
        };
    }

    private static class SWTBotChart extends AbstractSWTBotControl<Chart> {
        public SWTBotChart(Chart w) throws WidgetNotFoundException {
            super(w);
        }
    }

}
