/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.swtbot.tests.latency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentsStatisticsView;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.statistics.SystemCallLatencyStatisticsView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests of the latency table
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class SystemCallLatencyStatisticsTableAnalysisTest {

    private static final int MIN_COL = 1;
    private static final int MAX_COL = 2;
    private static final int AVERAGE_COL = 3;
    private static final int STDEV_COL = 4;
    private static final int COUNT_COL = 5;
    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String PROJECT_NAME = "test";
    private static final String VIEW_ID = SystemCallLatencyStatisticsView.ID;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private SWTBotTree fTreeBot;
    private static SWTWorkbenchBot fBot;

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
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", fBot);
        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

    }

    /**
     * Opens a latency table
     */
    @Before
    public void createTree() {
        /*
         * Open latency view
         */
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
        if (!(viewPart instanceof SystemCallLatencyStatisticsView)) {
            fail("Could not instanciate view");
        }
        fTreeBot = viewBot.bot().tree();
        assertNotNull(fTreeBot);
    }

    /**
     * Closes the view
     */
    @After
    public void closeTree() {
        SWTBotUtils.closeViewById(VIEW_ID, fBot);
    }

    /**
     * Test with an actual trace, this is more of an integration test than a
     * unit test. This test is a slow one too. If some analyses are not well
     * configured, this test will also generates null pointer exceptions. These
     * are will be logged.
     *
     * @throws IOException
     *             trace not found?
     * @throws SecurityException
     *             Reflection error
     * @throws NoSuchMethodException
     *             Reflection error
     * @throws IllegalArgumentException
     *             Reflection error
     */
    @Test
    public void testWithTrace() throws IOException, NoSuchMethodException, SecurityException, IllegalArgumentException {
        String tracePath;
        tracePath = FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL()).getPath();
        SWTBotView view = fBot.viewById(VIEW_ID);
        SWTBotUtils.closeViewById(VIEW_ID, fBot);
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, tracePath, TRACE_TYPE);
        WaitUtils.waitForJobs();
        createTree();
        WaitUtils.waitForJobs();
        assertEquals("1.000 µs", fTreeBot.cell(0, MIN_COL));
        assertEquals("5.904 s", fTreeBot.cell(0, MAX_COL));
        assertEquals("15.628 ms", fTreeBot.cell(0, AVERAGE_COL)); // double
        assertEquals("175.875 ms", fTreeBot.cell(0, STDEV_COL));
        assertEquals("1801", fTreeBot.cell(0, COUNT_COL));
        SWTBotTreeItem treeItem = fTreeBot.getTreeItem("Total");
        assertEquals(55, treeItem.getNodes().size());
        validate(treeItem.getNode(2), "select", "13.600 µs", "1.509 s", "192.251 ms", "386.369 ms", "58");
        validate(treeItem.getNode(3), "poll", "6.300 µs", "6.800 µs", "6.550 µs", "---", "2");
        validate(treeItem.getNode(5), "set_tid_address", "2.300 µs", "2.300 µs", "2.300 µs", "---", "1");
        validate(treeItem.getNode(7), "pipe", "27.900 µs", "29.700 µs", "28.800 µs", "---", "2");
        testToTsv(view);
        SWTBotMenu menuBot = view.viewMenu().menu("Export to TSV");
        assertTrue(menuBot.isEnabled());
        assertTrue(menuBot.isVisible());

        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    private static void testToTsv(SWTBotView view) throws NoSuchMethodException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertNotNull(os);
        IViewPart viewPart = view.getReference().getView(true);
        assertTrue(viewPart instanceof SystemCallLatencyStatisticsView);
        Class<@NonNull AbstractSegmentsStatisticsView> clazz = AbstractSegmentsStatisticsView.class;
        Method method = clazz.getDeclaredMethod("exportToTsv", java.io.OutputStream.class);
        method.setAccessible(true);
        final Exception[] except = new Exception[1];
        UIThreadRunnable.syncExec(() -> {
            try {
                method.invoke((SystemCallLatencyStatisticsView) viewPart, os);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                except[0] = e;
            }
        });
        assertNull(except[0]);
        @SuppressWarnings("null")
        String[] lines = String.valueOf(os).split(System.getProperty("line.separator"));
        assertNotNull(lines);
        assertEquals("header", "Level\tMinimum\tMaximum\tAverage\tStandard Deviation\tCount\tTotal", lines[0]);
        assertEquals("line 1", "Total\t1.000 µs\t5.904 s\t15.628 ms\t175.875 ms\t1801\t28.146 s", lines[1]);

    }

    private static void validate(SWTBotTreeItem treeItem, final String nodeName, final String min, final String max, final String avg, final String stdev, final String count) {
        assertEquals(nodeName, treeItem.cell(0));
        assertEquals(min, treeItem.cell(MIN_COL));
        assertEquals(max, treeItem.cell(MAX_COL));
        assertEquals(avg, treeItem.cell(AVERAGE_COL)); // double
        assertEquals(stdev, treeItem.cell(STDEV_COL));
        assertEquals(count, treeItem.cell(COUNT_COL));
    }
}
