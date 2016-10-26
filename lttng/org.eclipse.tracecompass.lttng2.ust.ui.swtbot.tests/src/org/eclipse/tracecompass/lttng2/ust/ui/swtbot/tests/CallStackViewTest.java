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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.callstack.CallStackView;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;

/**
 * Test for the Call Stack view in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CallStackViewTest {

    private static final String UST_ID = "org.eclipse.linuxtools.lttng2.ust.tracetype";

    private static final String PROJECT_NAME = "TestForCallstack";
    private static final String TRACE = "glxgears-cyg-profile";
    private static final String PROCESS = "UNKNOWN";
    private static final @NonNull String THREAD = "glxgears-16073";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

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
    private static final @NonNull String SORT_BY_NAME = "Sort threads by thread name";
    private static final @NonNull String SORT_BY_ID = "Sort threads by thread id";
    private static final @NonNull String SORT_BY_START = "Sort threads by start time";
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
            SORT_BY_NAME, SORT_BY_ID, SORT_BY_START,
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
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();
        SWTBotUtils.closeView("Statistics", fBot);
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
        final CtfTestTrace cygProfile = CtfTestTrace.CYG_PROFILE;
        final File file = new File(CtfTmfTestTraceUtils.getTrace(cygProfile).getPath());
        CtfTmfTestTraceUtils.dispose(cygProfile);
        SWTBotUtils.openTrace(PROJECT_NAME, file.getAbsolutePath(), UST_ID);
        SWTBotUtils.openView(CallStackView.ID);
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
     * Test if callstack is populated
     */
    @Test
    public void testOpenCallstack() {
        SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        assertEquals(Arrays.asList("0x40472b"), getVisibleStackFrames(viewBot));
    }

    /**
     * Test check callstack at a time
     */
    @Test
    public void testGoToTimeAndCheckStack() {
        goToTime(TIMESTAMPS[0]);

        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        WaitUtils.waitForJobs();
        List<String> names = getVisibleStackFrames(viewBot);
        assertArrayEquals(STACK_FRAMES[0], names.toArray());
    }

    /**
     * Test check callstack at a time after navigating
     */
    @Test
    public void testGoToTimeGoBackAndForthAndCheckStack() {
        int currentEventOffset = 0;
        goToTime(TIMESTAMPS[currentEventOffset]);

        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        // forward 10 times
        for (int i = 0; i < 10; i++) {
            viewBot.toolbarPushButton(SELECT_NEXT_STATE_CHANGE).click();
            currentEventOffset++;
            fBot.waitUntil(ConditionHelpers.selectionInEventsTable(fBot, TIMESTAMPS[currentEventOffset]));
            WaitUtils.waitForJobs();
            assertArrayEquals(STACK_FRAMES[currentEventOffset], getVisibleStackFrames(viewBot).toArray());

        }
        // back twice
        for (int i = 0; i < 2; i++) {
            viewBot.toolbarPushButton(SELECT_PREVIOUS_STATE_CHANGE).click();
            currentEventOffset--;
            fBot.waitUntil(ConditionHelpers.selectionInEventsTable(fBot, TIMESTAMPS[currentEventOffset]));
            WaitUtils.waitForJobs();
            assertArrayEquals(STACK_FRAMES[currentEventOffset], getVisibleStackFrames(viewBot).toArray());
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
        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        viewBot.toolbarToggleButton(SORT_BY_NAME).click();
        viewBot.toolbarToggleButton(SORT_BY_ID).click();
        viewBot.toolbarToggleButton(SORT_BY_START).click();
        viewBot.setFocus();
        WaitUtils.waitForJobs();
        List<String> names = getVisibleStackFrames(viewBot);
        assertArrayEquals(STACK_FRAMES[0], names.toArray());
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
        SWTBotTable table = fBot.activeEditor().bot().table();
        table.setFocus();
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(table.widget, TmfTimestamp.fromNanos(timestamp)));
        fBot.waitUntil(ConditionHelpers.selectionInEventsTable(fBot, timestamp));
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
        openSymbolProviderDialog();

        // 1- Open valid mapping files and invalid mapping file
        Object mapObjA = CtfTmfTestTraceUtils.class.getResource("cyg-profile-mapping.txt");
        Object mapObjB = CtfTmfTestTraceUtils.class.getResource("dummy-mapping.txt");
        Object mapObjC = CtfTmfTestTraceUtils.class.getResource("invalid-cyg-profile-mapping.txt");
        Object mapObjD = CtfTmfTestTraceUtils.class.getResource("random.out");
        Object mapObjE = CtfTmfTestTraceUtils.class.getResource("win32Random.exe");
        URL mapUrlA = (URL) mapObjA;
        URL mapUrlB = (URL) mapObjB;
        URL mapUrlC = (URL) mapObjC;
        URL mapUrlD = (URL) mapObjD;
        URL mapUrlE = (URL) mapObjE;
        String absoluteFileA = FileLocator.toFileURL(mapUrlA).getFile();
        String absoluteFileB = FileLocator.toFileURL(mapUrlB).getFile();
        String absoluteFileC = FileLocator.toFileURL(mapUrlC).getFile();
        String absoluteFileD = FileLocator.toFileURL(mapUrlD).getFile();
        String absoluteFileE = FileLocator.toFileURL(mapUrlE).getFile();
        String[] overrideFiles = { absoluteFileA, absoluteFileA, absoluteFileB, absoluteFileC, absoluteFileD, absoluteFileE };
        TmfFileDialogFactory.setOverrideFiles(overrideFiles);

        final SWTBot symbolDialog = fBot.shell("Symbol mapping").bot();
        symbolDialog.button("Add...").click();
        final SWTBot errorDialog = fBot.shell("Import failure").bot();
        errorDialog.button("OK").click();
        final SWTBotTable table = symbolDialog.table();
        assertEquals(table.rowCount(), 4);
        assertEquals(table.getTableItem(0).getText(), absoluteFileA);
        assertEquals(table.getTableItem(1).getText(), absoluteFileB);
        assertEquals(table.getTableItem(2).getText(), absoluteFileD);
        assertEquals(table.getTableItem(3).getText(), absoluteFileE);

        // 2- Change priority of mapping files
        table.select(0);
        symbolDialog.button("Down").click().click().click();
        assertEquals(table.getTableItem(0).getText(), absoluteFileB);
        assertEquals(table.getTableItem(1).getText(), absoluteFileD);
        assertEquals(table.getTableItem(2).getText(), absoluteFileE);
        assertEquals(table.getTableItem(3).getText(), absoluteFileA);
        symbolDialog.button("Up").click().click().click();
        assertEquals(table.getTableItem(0).getText(), absoluteFileA);
        assertEquals(table.getTableItem(1).getText(), absoluteFileB);
        assertEquals(table.getTableItem(2).getText(), absoluteFileD);
        assertEquals(table.getTableItem(3).getText(), absoluteFileE);

        // 3- Remove multiple mapping files
        table.select(0, 1);
        symbolDialog.button("Remove").click();
        assertEquals(table.rowCount(), 2);

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
        openSymbolProviderDialog();

        // 1- Open conflicting mapping files
        Object mapObjA = CtfTmfTestTraceUtils.class.getResource("cyg-profile-mapping.txt");
        Object mapObjB = CtfTmfTestTraceUtils.class.getResource("dummy-mapping.txt");
        URL mapUrlA = (URL) mapObjA;
        URL mapUrlB = (URL) mapObjB;
        String absoluteFileA = FileLocator.toFileURL(mapUrlA).getFile();
        String absoluteFileB = FileLocator.toFileURL(mapUrlB).getFile();
        String[] overrideFiles = { absoluteFileA, absoluteFileB };
        TmfFileDialogFactory.setOverrideFiles(overrideFiles);
        final SWTBot symbolDialog = fBot.shell("Symbol mapping").bot();
        symbolDialog.button("Add...").click();
        symbolDialog.button("OK").click();

        // 2- Ensure symbols are loaded and prioritized
        goToTime(TIMESTAMP);
        assertEquals(Arrays.asList("main", "event_loop", "draw_frame", "draw_gears", "drawB"), getVisibleStackFrames(fBot.viewById(CallStackView.ID)));
    }

    private static void openSymbolProviderDialog() {
        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        viewBot.toolbarButton(CONFIGURE_SYMBOL_PROVIDERS).click();
        fBot.waitUntil(Conditions.shellIsActive("Symbol mapping"));
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
        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        Object mapObj = CtfTmfTestTraceUtils.class.getResource("cyg-profile-mapping.txt");
        assertTrue(mapObj instanceof URL);
        URL mapUrl = (URL) mapObj;

        String absoluteFile = FileLocator.toFileURL(mapUrl).getFile();
        TmfFileDialogFactory.setOverrideFiles(absoluteFile);
        viewBot.toolbarButton("Configure how the addresses are mapped to function names").click();
        String shellTitle = "Symbol mapping";
        fBot.waitUntil(Conditions.shellIsActive(shellTitle));
        SWTBot shellBot = fBot.shell(shellTitle).bot();
        SWTBotShell activeShell = shellBot.activeShell();
        shellBot.button("Add...").click();
        shellBot.button("OK").click();
        shellBot.waitUntil(Conditions.shellCloses(activeShell));
        /*
         * FIXME: Seek to time needed to update the call stack entry names.
         * Remove when applying symbol configuration correctly updates entries.
         */
        goToTime(TIMESTAMPS[0]);
        WaitUtils.waitForJobs();
        SWTBotTimeGraph timeGraph = new SWTBotTimeGraph(viewBot.bot());
        SWTBotTimeGraphEntry[] threads = timeGraph.getEntry(TRACE, PROCESS).getEntries();
        assertEquals(1, threads.length);
        assertEquals(THREAD, threads[0].getText(0));
        assertEquals(Arrays.asList("main", "event_loop", "handle_event"), getVisibleStackFrames(viewBot));
    }

    /**
     * Test Call Stack tool bar
     */
    @Test
    public void testCallStackToolBar() {
        SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        List<String> buttons = new ArrayList<>();
        for (SWTBotToolbarButton swtBotToolbarButton : viewBot.getToolbarButtons()) {
            buttons.add(swtBotToolbarButton.getToolTipText());
        }
        assertEquals(TOOLBAR_BUTTONS_TOOLTIPS, buttons);
    }
}
