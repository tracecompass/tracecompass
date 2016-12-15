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

import java.io.File;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage.MemoryUsageView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.swtchart.Chart;
import org.swtchart.ILineSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeriesSet;

/**
 * Test for the Memory Usage view in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class MemoryUsageViewTest {

    private static final int EXPECTED_NUM_SERIES = 4;

    private static final String UST_ID = "org.eclipse.linuxtools.lttng2.ust.tracetype";

    private static final String PROJECT_NAME = "TestForMemory";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    /**
     * Initialization
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();

        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();

        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Open a trace in an editor
     */
    @Before
    public void beforeTest() {
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        assertNotNull(treeItem);
        final CtfTestTrace cygProfile = CtfTestTrace.MEMORY_ANALYSIS;
        final File file = new File(CtfTmfTestTraceUtils.getTrace(cygProfile).getPath());
        CtfTmfTestTraceUtils.dispose(cygProfile);
        SWTBotUtils.openTrace(PROJECT_NAME, file.getAbsolutePath(), UST_ID);
        SWTBotUtils.openView(MemoryUsageView.ID);
        WaitUtils.waitForJobs();
    }

    /**
     * Close the editor
     */
    @After
    public void tearDown() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

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

}