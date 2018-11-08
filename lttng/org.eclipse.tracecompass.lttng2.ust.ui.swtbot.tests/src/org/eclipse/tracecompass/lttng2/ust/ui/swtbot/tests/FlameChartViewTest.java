/*******************************************************************************
 * Copyright (c) 2015, 2017 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Mikael Ferland - Add tests for BasicSymbolProvider dialog
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.analysis.profiling.ui.views.flamechart.FlameChartView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

/**
 * Test for the Call Stack view in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class FlameChartViewTest {

    private static final String UST_ID = "org.eclipse.linuxtools.lttng2.ust.tracetype";

    private static final String PROJECT_NAME = "TestForCallstack";
    private static final String TRACE = "glxgears-cyg-profile";
    private static final String PROCESS = "UNKNOWN";
    private static final @NonNull String THREAD = "glxgears-16073";

    /** The Log4j logger instance. */
    private static final Logger sfLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot sfBot;

    /**
     * Start time of the trace
     */
    private static final @NonNull ITmfTimestamp START_TIME = TmfTimestamp.fromNanos(1378850463596911581l);

    /**
     * Timestamp for loading mapping files test
     */
    private static final long TIMESTAMP = 1378850463804917148l;

    /**
     * Timestamps of consecutive events in the trace
     */
    private static final long TIMESTAMPS[] = new long[] {
            1378850463804898643l,
            1378850463804899057l,
            1378850463804900219l,
            1378850463804900678l,
            1378850463804901308l,
            1378850463804901909l,
            1378850463804902763l,
            1378850463804903168l,
            1378850463804903766l,
            1378850463804904165l,
            1378850463804904970l
    };

    /**
     * Stack frames of consecutive events in the trace
     */
    private static final String[] STACK_FRAMES[] = new String[][] {
            { "0x40472b", "0x4045c8", "0x404412" },
            { "0x40472b", "0x4045c8", "0x404412", "0x40392b" },
            { "0x40472b", "0x4045c8", "0x404412" },
            { "0x40472b", "0x4045c8" },
            { "0x40472b", "0x4045c8", "0x404412" },
            { "0x40472b", "0x4045c8", "0x404412", "0x40392b" },
            { "0x40472b", "0x4045c8", "0x404412" },
            { "0x40472b", "0x4045c8" },
            { "0x40472b", "0x4045c8", "0x404412" },
            { "0x40472b", "0x4045c8", "0x404412", "0x40392b" },
            { "0x40472b", "0x4045c8", "0x404412" },
    };

    /** Tooltips of the toolbar buttons */

    private static final @NonNull String CONFIGURE_SYMBOL_PROVIDERS = "Configure how the addresses are mapped to function names";
    // Separator
    private static final @NonNull String SORT_BY_NAME = "Name";
    private static final @NonNull String SORT_BY_ID = "PID/TID";
    private static final @NonNull String SORT_BY_START = "Start time";
    // Separator
    private static final @NonNull String SHOW_VIEW_FILTERS = "Show View Filters";
    // Separator
    private static final @NonNull String RESET_TIME_SCALE = "Reset the Time Scale to Default";
    private static final @NonNull String SELECT_PREVIOUS_STATE_CHANGE = "Select Previous State Change";
    private static final @NonNull String SELECT_NEXT_STATE_CHANGE = "Select Next State Change";
    // Separator
    private static final @NonNull String ADD_BOOKMARK = "Add Bookmark...";
    private static final @NonNull String PREVIOUS_MARKER = "Previous Marker";
    private static final @NonNull String NEXT_MARKER = "Next Marker";
    // Separator
    private static final @NonNull String SELECT_PREVIOUS_ITEM = "Select Previous Item";
    private static final @NonNull String SELECT_NEXT_ITEM = "Select Next Item";
    private static final @NonNull String ZOOM_IN = "Zoom In";
    private static final @NonNull String ZOOM_OUT = "Zoom Out";
    // Separator
    private static final @NonNull String PIN_VIEW = "Pin View";

    private static final List<String> TOOLBAR_BUTTONS_TOOLTIPS = ImmutableList.of(
            CONFIGURE_SYMBOL_PROVIDERS,
            "",
            SHOW_VIEW_FILTERS,
            "",
            RESET_TIME_SCALE, SELECT_PREVIOUS_STATE_CHANGE, SELECT_NEXT_STATE_CHANGE,
            "",
            ADD_BOOKMARK, PREVIOUS_MARKER, NEXT_MARKER,
            "",
            SELECT_PREVIOUS_ITEM, SELECT_NEXT_ITEM, ZOOM_IN, ZOOM_OUT,
            "",
            PIN_VIEW);

    /**
     * Initialization
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();

        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        sfLogger.removeAllAppenders();
        sfLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        sfBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("Statistics", sfBot);
        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Clean up
     */
    @AfterClass
    public static void afterClass() {
        sfLogger.removeAllAppenders();
    }

    /**
     * Open a trace in an editor
     */
    @Before
    public void beforeTest() {
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(sfBot, PROJECT_NAME);
        assertNotNull(treeItem);
        final CtfTestTrace cygProfile = CtfTestTrace.CYG_PROFILE;
        final File file = new File(CtfTmfTestTraceUtils.getTrace(cygProfile).getPath());
        CtfTmfTestTraceUtils.dispose(cygProfile);
        SWTBotUtils.openTrace(PROJECT_NAME, file.getAbsolutePath(), UST_ID);
        SWTBotUtils.openView(FlameChartView.ID);
        WaitUtils.waitForJobs();
        final SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);
        IWorkbenchPart part = viewBot.getViewReference().getPart(false);
        sfBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition((AbstractTimeGraphView) part, new TmfTimeRange(START_TIME, START_TIME), START_TIME));
    }

    /**
     * Close the editor
     */
    @After
    public void tearDown() {
        sfBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, sfBot);
    }

    /**
     * Test if callstack is populated
     */
    @Test
    public void testOpenCallstack() {
        SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);
        viewBot.setFocus();
        waitForSymbolNames("0x40472b");
    }

    /**
     * Test check callstack at a time
     */
    @Test
    public void testGoToTimeAndCheckStack() {
        final SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);
        viewBot.setFocus();
        WaitUtils.waitForJobs();

        goToTime(TIMESTAMPS[0]);
        waitForSymbolNames(STACK_FRAMES[0]);
    }

    /**
     * Test check callstack at a time after navigating
     */
    @Test
    public void testGoToTimeGoBackAndForthAndCheckStack() {
        int currentEventOffset = 0;
        final SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);

        goToTime(TIMESTAMPS[currentEventOffset]);

        // forward 10 times
        for (int i = 0; i < 10; i++) {
            viewBot.toolbarPushButton(SELECT_NEXT_STATE_CHANGE).click();
            currentEventOffset++;
            sfBot.waitUntil(ConditionHelpers.selectionInEventsTable(sfBot, TIMESTAMPS[currentEventOffset]));
            WaitUtils.waitForJobs();
            waitForSymbolNames(STACK_FRAMES[currentEventOffset]);
        }

        // back twice
        for (int i = 0; i < 2; i++) {
            viewBot.toolbarPushButton(SELECT_PREVIOUS_STATE_CHANGE).click();
            currentEventOffset--;
            sfBot.waitUntil(ConditionHelpers.selectionInEventsTable(sfBot, TIMESTAMPS[currentEventOffset]));
            WaitUtils.waitForJobs();
            waitForSymbolNames(STACK_FRAMES[currentEventOffset]);
        }
        // move up and down once to make sure it doesn't explode
        viewBot.toolbarPushButton(SELECT_PREVIOUS_ITEM).click();
        WaitUtils.waitForJobs();
        viewBot.toolbarPushButton(SELECT_NEXT_ITEM).click();
        WaitUtils.waitForJobs();

        // Zoom in and out too
        viewBot.toolbarPushButton(ZOOM_IN).click();
        WaitUtils.waitForJobs();
        viewBot.toolbarPushButton(ZOOM_OUT).click();
        WaitUtils.waitForJobs();
    }

    /**
     * Test check callstack at a time with sorting, the trace is not sortable,
     * this is a smoke test
     */
    @Test
    public void testGoToTimeSortAndCheckStack() {
        goToTime(TIMESTAMPS[0]);
        final SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);
        viewBot.setFocus();
        SWTBotTree tree = viewBot.bot().tree();
        tree.header(SORT_BY_NAME).click();
        tree.header(SORT_BY_ID).click();
        tree.header(SORT_BY_START).click();
        viewBot.setFocus();
        WaitUtils.waitForJobs();
        waitForSymbolNames(STACK_FRAMES[0]);
    }

    private static void waitForSymbolNames(String... symbolNames) {
        final SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);
        List<String> symbolNameList = Lists.newArrayList(symbolNames);
        WaitUtils.waitUntil(vBot -> symbolNameList.equals(getVisibleStackFrames(vBot)),
                viewBot, "Wrong symbol names, expected:" + symbolNameList
                + ", got: " + getVisibleStackFrames(viewBot));
    }

    private static List<String> getVisibleStackFrames(final SWTBotView viewBot) {
        SWTBotTimeGraph timeGraph = new SWTBotTimeGraph(viewBot.bot());
        List<String> stackFrames = new ArrayList<>();
        for (SWTBotTimeGraphEntry entry : timeGraph.getEntry(TRACE, PROCESS, THREAD).getEntries()) {
            String name = entry.getText();
            if (!name.isEmpty()) {
                stackFrames.add(name);
            }
        }
        return stackFrames;
    }

    private static void goToTime(long timestamp) {
        ITmfTimestamp time = TmfTimestamp.fromNanos(timestamp);
        SWTBotTable table = sfBot.activeEditor().bot().table();
        table.setFocus();
        WaitUtils.waitForJobs();
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(table.widget, time));
        sfBot.waitUntil(ConditionHelpers.selectionInEventsTable(sfBot, timestamp));
        final SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);
        IWorkbenchPart part = viewBot.getViewReference().getPart(false);
        sfBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition((AbstractTimeGraphView) part, new TmfTimeRange(time, time), time));
    }

    /**
     * Test manipulating valid and invalid mapping files (add, remove and change
     * priority of files)
     *
     * @throws IOException
     *             Missing file
     */
    @Test
    public void testManipulatingMappingFiles() throws IOException {

        // 1- Open valid mapping files and invalid mapping file
        URL mapUrlA = CtfTmfTestTraceUtils.class.getResource("cyg-profile-mapping.txt");
        URL mapUrlB = CtfTmfTestTraceUtils.class.getResource("dummy-mapping.txt");
        URL mapUrlC = CtfTmfTestTraceUtils.class.getResource("invalid-cyg-profile-mapping.txt");
        URL mapUrlD = CtfTmfTestTraceUtils.class.getResource("random.out");
        URL mapUrlE = CtfTmfTestTraceUtils.class.getResource("win32Random.exe");

        String absoluteFileA = FileLocator.toFileURL(mapUrlA).getFile();
        String absoluteFileB = FileLocator.toFileURL(mapUrlB).getFile();
        String absoluteFileC = FileLocator.toFileURL(mapUrlC).getFile();
        String absoluteFileD = FileLocator.toFileURL(mapUrlD).getFile();
        String absoluteFileE = FileLocator.toFileURL(mapUrlE).getFile();
        String[] overrideFiles = { absoluteFileA, absoluteFileA, absoluteFileB, absoluteFileC, absoluteFileD, absoluteFileE };
        TmfFileDialogFactory.setOverrideFiles(overrideFiles);

        SWTBotShell shell = openSymbolProviderDialog();
        final SWTBot symbolDialog = shell.bot();
        symbolDialog.button("Add...").click();
        final SWTBot errorDialog = sfBot.shell("Import failure").bot();
        errorDialog.button("OK").click();
        final SWTBotTable table = symbolDialog.table();
        assertEquals(4, table.rowCount());
        assertEquals(absoluteFileA, table.getTableItem(0).getText());
        assertEquals(absoluteFileB, table.getTableItem(1).getText());
        assertEquals(absoluteFileD, table.getTableItem(2).getText());
        assertEquals(absoluteFileE, table.getTableItem(3).getText());

        // 2- Change priority of mapping files
        table.select(0);
        symbolDialog.button("Down").click().click().click();
        assertEquals(absoluteFileB, table.getTableItem(0).getText());
        assertEquals(absoluteFileD, table.getTableItem(1).getText());
        assertEquals(absoluteFileE, table.getTableItem(2).getText());
        assertEquals(absoluteFileA, table.getTableItem(3).getText());

        symbolDialog.button("Up").click().click().click();
        assertEquals(absoluteFileA, table.getTableItem(0).getText());
        assertEquals(absoluteFileB, table.getTableItem(1).getText());
        assertEquals(absoluteFileD, table.getTableItem(2).getText());
        assertEquals(absoluteFileE, table.getTableItem(3).getText());

        // 3- Remove multiple mapping files
        table.select(0, 1);
        symbolDialog.button("Remove").click();
        assertEquals(2, table.rowCount());

        // 4- Close symbol provider dialog
        symbolDialog.button("Cancel").click();
    }

    /**
     * Test loading conflicting mapping files.
     *
     * @throws IOException
     *             Missing file
     */
    @Test
    public void testLoadingMappingFiles() throws IOException {

        // 1- Open conflicting mapping files
        URL mapUrlA = CtfTmfTestTraceUtils.class.getResource("cyg-profile-mapping.txt");
        URL mapUrlB = CtfTmfTestTraceUtils.class.getResource("dummy-mapping.txt");
        String absoluteFileA = FileLocator.toFileURL(mapUrlA).getFile();
        String absoluteFileB = FileLocator.toFileURL(mapUrlB).getFile();
        String[] overrideFiles = { absoluteFileA, absoluteFileB };
        TmfFileDialogFactory.setOverrideFiles(overrideFiles);

        SWTBotShell shell = openSymbolProviderDialog();
        final SWTBot symbolDialog = shell.bot();
        symbolDialog.button("Add...").click();
        symbolDialog.button("OK").click();

        // 2- Ensure symbols are loaded and prioritized
        sfBot.viewById(FlameChartView.ID).setFocus();
        WaitUtils.waitForJobs();
        goToTime(TIMESTAMP);
        waitForSymbolNames("main", "event_loop", "draw_frame", "draw_gears", "drawB");
    }

    private static SWTBotShell openSymbolProviderDialog() {
        final SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);
        viewBot.setFocus();
        viewBot.toolbarButton(CONFIGURE_SYMBOL_PROVIDERS).click();
        return sfBot.shell("Symbol mapping");
    }

    /**
     * Test check callstack at a time with function map
     *
     * @throws IOException
     *             Missing file
     */
    @Test
    public void testGoToTimeAndCheckStackWithNames() throws IOException {
        goToTime(TIMESTAMPS[0]);
        final SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);
        viewBot.setFocus();

        URL mapUrl = CtfTmfTestTraceUtils.class.getResource("cyg-profile-mapping.txt");
        String absoluteFile = FileLocator.toFileURL(mapUrl).getFile();
        TmfFileDialogFactory.setOverrideFiles(absoluteFile);

        SWTBotShell shell = openSymbolProviderDialog();
        SWTBot shellBot = shell.bot();
        shellBot.button("Add...").click();
        shellBot.button("OK").click();
        shellBot.waitUntil(Conditions.shellCloses(shell));
        SWTBotTimeGraph timeGraph = new SWTBotTimeGraph(viewBot.bot());
        SWTBotTimeGraphEntry[] threads = timeGraph.getEntry(TRACE, PROCESS).getEntries();
        assertEquals(1, threads.length);
        assertEquals(THREAD, threads[0].getText(0));
        waitForSymbolNames("main", "event_loop", "handle_event");
    }

    /**
     * Test Call Stack tool bar
     */
    @Test
    public void testCallStackToolBar() {
        SWTBotView viewBot = sfBot.viewById(FlameChartView.ID);
        viewBot.setFocus();
        List<String> buttons = Lists.transform(viewBot.getToolbarButtons(), SWTBotToolbarButton::getToolTipText);
        assertEquals(TOOLBAR_BUTTONS_TOOLTIPS, buttons);
    }
}
