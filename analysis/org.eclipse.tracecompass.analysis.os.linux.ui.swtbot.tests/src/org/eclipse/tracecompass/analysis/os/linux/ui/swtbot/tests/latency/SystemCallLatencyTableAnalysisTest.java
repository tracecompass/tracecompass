/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
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

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.BoolResult;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableView;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.table.AbstractSegmentStoreTableViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.SystemCallLatencyView;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
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
public class SystemCallLatencyTableAnalysisTest {

    private static final String TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String PROJECT_NAME = "test";
    private static final String VIEW_ID = SystemCallLatencyView.ID;
    private static final String TRACING_PERSPECTIVE_ID = "org.eclipse.linuxtools.tmf.ui.perspective";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private SystemCallLatencyView fLatencyView;
    private AbstractSegmentStoreTableViewer fTable;

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
        final List<SWTBotView> openViews = bot.views();
        for (SWTBotView view : openViews) {
            if (view.getTitle().equals("Welcome")) {
                view.close();
                bot.waitUntil(ConditionHelpers.ViewIsClosed(view));
            }
        }
        /* Switch perspectives */
        switchTracingPerspective();
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

    }

    /**
     * Opens a latency table
     */
    @Before
    public void createTable() {
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
        if (!(viewPart instanceof SystemCallLatencyView)) {
            fail("Could not instanciate view");
        }
        fLatencyView = (SystemCallLatencyView) viewPart;
        fTable = fLatencyView.getSegmentStoreViewer();
        assertNotNull(fTable);
    }

    /**
     * Closes the view
     */
    @After
    public void closeTable() {
        final SWTWorkbenchBot swtWorkbenchBot = new SWTWorkbenchBot();
        SWTBotView viewBot = swtWorkbenchBot.viewById(VIEW_ID);
        viewBot.close();
    }

    private static void switchTracingPerspective() {
        final Exception retE[] = new Exception[1];
        if (!UIThreadRunnable.syncExec(new BoolResult() {
            @Override
            public Boolean run() {
                try {
                    PlatformUI.getWorkbench().showPerspective(TRACING_PERSPECTIVE_ID,
                            PlatformUI.getWorkbench().getActiveWorkbenchWindow());
                } catch (WorkbenchException e) {
                    retE[0] = e;
                    return false;
                }
                return true;
            }
        })) {
            fail(retE[0].getMessage());
        }

    }

    /**
     * Test incrementing
     */
    @Test
    public void climbTest() {
        List<@NonNull BasicSegment> fixture = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            fixture.add(new BasicSegment(i, 2 * i));
        }

        assertNotNull(fTable);
        fTable.updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(fTable.getTableViewer().getTable());
        SWTBot bot = new SWTBot();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "99", 0, 2));
    }

    /**
     * Test decrementing
     */
    @Test
    public void decrementingTest() {
        List<@NonNull BasicSegment> fixture = new ArrayList<>();
        for (int i = 100; i >= 0; i--) {
            fixture.add(new BasicSegment(i, 2 * i));
        }
        assertNotNull(fTable);
        fTable.updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(fTable.getTableViewer().getTable());
        SWTBot bot = new SWTBot();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "100", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "100", 0, 2));
    }

    /**
     * Test small table
     */
    @Test
    public void smallTest() {
        List<@NonNull BasicSegment> fixture = new ArrayList<>();
        for (int i = 1; i >= 0; i--) {
            fixture.add(new BasicSegment(i, 2 * i));
        }
        assertNotNull(fTable);
        fTable.updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(fTable.getTableViewer().getTable());
        SWTBot bot = new SWTBot();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1", 0, 2));
    }

    /**
     * Test large
     */
    @Test
    public void largeTest() {
        final int size = 1000000;
        BasicSegment[] fixture = new BasicSegment[size];
        for (int i = 0; i < size; i++) {
            fixture[i] = (new BasicSegment(i, 2 * i));
        }
        assertNotNull(fTable);
        fTable.updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(fTable.getTableViewer().getTable());
        SWTBot bot = new SWTBot();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "999,999", 0, 2));
    }

    /**
     * Test noise
     */
    @Test
    public void noiseTest() {
        Random rnd = new Random();
        rnd.setSeed(1234);
        final int size = 1000000;
        BasicSegment[] fixture = new BasicSegment[size];
        for (int i = 0; i < size; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            int end = start + Math.abs(rnd.nextInt(1000000));
            fixture[i] = (new BasicSegment(start, end));
        }
        assertNotNull(fTable);
        fTable.updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(fTable.getTableViewer().getTable());
        SWTBot bot = new SWTBot();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "894,633", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "999,999", 0, 2));
    }

    /**
     * Test gaussian noise
     */
    @Test
    public void gaussianNoiseTest() {
        Random rnd = new Random();
        rnd.setSeed(1234);
        List<@NonNull BasicSegment> fixture = new ArrayList<>();
        for (int i = 1; i <= 1000000; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            final int delta = Math.abs(rnd.nextInt(1000));
            int end = start + delta * delta;
            fixture.add(new BasicSegment(start, end));
        }
        assertNotNull(fTable);
        fTable.updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(fTable.getTableViewer().getTable());
        SWTBot bot = new SWTBot();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "400,689", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "0", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "998,001", 0, 2));
    }

    /**
     * Test creating a tsv
     *
     * @throws NoSuchMethodException
     *             Error creating the tsv
     * @throws IOException
     *             no such file or the file is locked.
     */
    @Test
    public void testWriteToTsv() throws NoSuchMethodException, IOException {

        List<@NonNull BasicSegment> fixture = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            int start = i;
            final int delta = i;
            int end = start + delta * delta;
            fixture.add(new BasicSegment(start, end));
        }
        assertNotNull(fTable);
        fTable.updateModel(fixture);
        SWTBotTable tableBot = new SWTBotTable(fTable.getTableViewer().getTable());
        SWTBot bot = new SWTBot();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1", 0, 2));
        SWTWorkbenchBot swtWorkbenchBot = new SWTWorkbenchBot();
        SWTBotView viewBot = swtWorkbenchBot.viewById(VIEW_ID);
        List<String> actionResult = Arrays.asList(testToTsv(viewBot));
        String absolutePath = TmfTraceManager.getTemporaryDirPath() + File.separator + "syscallLatencyTest.testWriteToTsv.tsv";
        TmfFileDialogFactory.setOverrideFiles(absolutePath);
        SWTBotMenu menuBot = viewBot.viewMenu().menu("Export to TSV");
        try {
            assertTrue(menuBot.isEnabled());
            assertTrue(menuBot.isVisible());
            menuBot.click();

            try (BufferedReader br = new BufferedReader(new FileReader(absolutePath))) {
                List<String> lines = br.lines().collect(Collectors.toList());
                assertEquals("Both reads", actionResult, lines);
            }
        } finally {
            new File(absolutePath).delete();
        }

    }

    private String[] testToTsv(SWTBotView view) throws NoSuchMethodException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        assertNotNull(os);
        Class<@NonNull AbstractSegmentStoreTableView> clazz = AbstractSegmentStoreTableView.class;
        Method method = clazz.getDeclaredMethod("exportToTsv", java.io.OutputStream.class);
        method.setAccessible(true);
        final Exception[] except = new Exception[1];
        UIThreadRunnable.syncExec(() -> {
            try {
                method.invoke(fLatencyView, os);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                except[0] = e;
            }
        });
        assertNull(except[0]);
        @SuppressWarnings("null")
        String[] lines = String.valueOf(os).split(System.getProperty("line.separator"));
        assertNotNull(lines);
        assertEquals("number of lines", 21, lines.length);
        assertEquals("header", "Start Time\tEnd Time\tDuration", lines[0]);
        // not a straight up string compare due to time zones. Kathmandu and
        // Eucla have 15 minute time zones.
        assertTrue("line 1", lines[1].matches("\\d\\d:\\d\\d:00\\.000\\s000\\s001\\t\\d\\d:\\d\\d:00.000 000 002\\t1"));
        return lines;
    }

    /**
     * Test with an actual trace, this is more of an integration test than a
     * unit test. This test is a slow one too. If some analyses are not well
     * configured, this test will also generates null pointer exceptions. These
     * are will be logged.
     *
     * @throws IOException
     *             trace not found?
     */
    @Test
    public void testWithTrace() throws IOException {
        String tracePath;
        tracePath = FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL()).getPath();
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotView view = bot.viewById(VIEW_ID);
        view.close();
        bot.waitUntil(ConditionHelpers.ViewIsClosed(view));
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, tracePath, TRACE_TYPE);
        WaitUtils.waitForJobs();
        createTable();
        WaitUtils.waitForJobs();
        SWTBotTable tableBot = new SWTBotTable(fTable.getTableViewer().getTable());
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "24,100", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "1,000", 0, 2));
        tableBot.header("Duration").click();
        bot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "5,904,091,700", 0, 2));
        bot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, bot);
    }
}
