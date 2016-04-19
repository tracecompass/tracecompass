/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.ui.swtbot.tests;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.callstack.CallStackView;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    /**
     * Timestamps of consecutive events in the trace
     */
    private static final long TIMESTAMPS[] =  new long[] {
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
            1378850463804904970l,
    };

    /**
     * Stack frames of consecutive events in the trace
     */
    private static final String[] STACK_FRAMES[] = new String[][] {
        {"40472b", "4045c8", "404412", "", ""},
        {"40472b", "4045c8", "404412", "40392b", ""},
        {"40472b", "4045c8", "404412", "", ""},
        {"40472b", "4045c8", "", "", ""},
        {"40472b", "4045c8", "404412", "", ""},
        {"40472b", "4045c8", "404412", "40392b", ""},
        {"40472b", "4045c8", "404412", "", ""},
        {"40472b", "4045c8", "", "", ""},
        {"40472b", "4045c8", "404412", "", ""},
        {"40472b", "4045c8", "404412", "40392b", ""},
        {"40472b", "4045c8", "404412", "", ""},
    };

    /** Tooltips of the toolbar buttons */

    private static final @NonNull String ALIGN_VIEWS = "Align Views";
    private static final @NonNull String CONFIGURE_SYMBOL_PROVIDERS = "Configure how the addresses are mapped to function names";
    // Separator
    private static final @NonNull String SORT_BY_NAME = "Sort threads by thread name";
    private static final @NonNull String SORT_BY_ID = "Sort threads by thread id";
    private static final @NonNull String SORT_BY_START = "Sort threads by start time";
    // Separator
    private static final @NonNull String SHOW_VIEW_FILTERS = "Show View Filters";
    // Separator
    private static final @NonNull String RESET_TIME_SCALE = "Reset the Time Scale to Default";
    private static final @NonNull String SELECT_PREVIOUS_EVENT = "Select Previous Event";
    private static final @NonNull String SELECT_NEXT_EVENT = "Select Next Event";
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
    private static final String PIN_VIEW = "Pin View";
    private static final List<String> TOOLBAR_BUTTONS_TOOLTIPS = ImmutableList.of(
            ALIGN_VIEWS, CONFIGURE_SYMBOL_PROVIDERS,
            "",
            SORT_BY_NAME, SORT_BY_ID, SORT_BY_START,
            "",
            SHOW_VIEW_FILTERS,
            "",
            RESET_TIME_SCALE, SELECT_PREVIOUS_EVENT, SELECT_NEXT_EVENT,
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
        /* finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();
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
        SWTBotUtils.openTrace(PROJECT_NAME, file.getAbsolutePath(), UST_ID);
        SWTBotUtils.openView(CallStackView.ID);
        SWTBotUtils.waitForJobs();
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
        String node = "glxgears-cyg-profile";
        String pid = "-1";
        String childName = "glxgears-16073";
        List<String> expected = ImmutableList.of("40472b", "", "", "", "");

        SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        final SWTBotView viewBot1 = viewBot;
        SWTBotTree tree = viewBot1.bot().tree();
        SWTBotTreeItem treeItem = tree.getTreeItem(node).getNode(pid);
        assertEquals(childName, treeItem.getNodes().get(0));
        List<String> names = treeItem.getNode(childName).getNodes();
        assertEquals(expected, names);
    }

    /**
     * Test check callstack at a time
     */
    @Test
    public void testGoToTimeAndCheckStack() {
        goToTime(TIMESTAMPS[0]);

        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        SWTBotUtils.waitForJobs();
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
            viewBot.toolbarPushButton(SELECT_NEXT_EVENT).click();
            currentEventOffset++;
            fBot.waitUntil(ConditionHelpers.selectionInEventsTable(fBot, TIMESTAMPS[currentEventOffset]));
            SWTBotUtils.waitForJobs();
            assertArrayEquals(STACK_FRAMES[currentEventOffset], getVisibleStackFrames(viewBot).toArray());

        }
        // back twice
        for (int i = 0; i < 2; i++) {
            viewBot.toolbarPushButton(SELECT_PREVIOUS_EVENT).click();
            currentEventOffset--;
            fBot.waitUntil(ConditionHelpers.selectionInEventsTable(fBot, TIMESTAMPS[currentEventOffset]));
            SWTBotUtils.waitForJobs();
            assertArrayEquals(STACK_FRAMES[currentEventOffset], getVisibleStackFrames(viewBot).toArray());
        }
        // move up and down once to make sure it doesn't explode
        viewBot.toolbarPushButton(SELECT_PREVIOUS_ITEM).click();
        SWTBotUtils.waitForJobs();
        viewBot.toolbarPushButton(SELECT_NEXT_ITEM).click();
        SWTBotUtils.waitForJobs();

        // Zoom in and out too
        viewBot.toolbarPushButton(ZOOM_IN).click();
        SWTBotUtils.waitForJobs();
        viewBot.toolbarPushButton(ZOOM_OUT).click();
        SWTBotUtils.waitForJobs();
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
        SWTBotUtils.waitForJobs();
        List<String> names = getVisibleStackFrames(viewBot);
        assertArrayEquals(STACK_FRAMES[0], names.toArray());
    }

    private static List<String> getVisibleStackFrames(final SWTBotView viewBot) {
        SWTBotTree tree = viewBot.bot().tree();
        return Arrays.stream(tree.getAllItems())
                // Process entries
                .flatMap(item -> Arrays.stream(item.getItems()))
                // Thread entries
                .flatMap(item -> Arrays.stream(item.getItems()))
                // Callstack entries
                .flatMap(item -> Arrays.stream(item.getItems()))
                .map(item -> item.cell(0))
                .collect(Collectors.toList());
    }

    private static void goToTime(long timestamp) {
        SWTBotTable table = fBot.activeEditor().bot().table();
        table.setFocus();
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(table.widget, TmfTimestamp.fromNanos(timestamp)));
        fBot.waitUntil(ConditionHelpers.selectionInEventsTable(fBot, timestamp));
    }

    /**
     * Test check callstack at a time with function map
     */
    @Ignore
    @Test
    public void testGoToTimeAndCheckStackWithNames() {
        goToTime(TIMESTAMPS[0]);
        final SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        // no way to load mappings yet! :(
        SWTBotTree tree = viewBot.bot().tree();
        SWTBotUtils.waitForJobs();
        List<String> names = new ArrayList<>();
        for (SWTBotTreeItem swtBotTreeItem : tree.getAllItems()) {
            for (SWTBotTreeItem items : swtBotTreeItem.getItems()) {
                for (SWTBotTreeItem item : items.getItems()) {
                    names.add(item.cell(0));
                }
            }
        }
    }

    /**
     * Test check callstack toolbar buttons
     */
    @Test
    public void testCallstackNavigation() {
        SWTBotView viewBot = fBot.viewById(CallStackView.ID);
        viewBot.setFocus();
        List<String> buttons = new ArrayList<>();
        for (SWTBotToolbarButton swtBotToolbarButton : viewBot.getToolbarButtons()) {
            buttons.add(swtBotToolbarButton.getToolTipText());
        }
        assertEquals(TOOLBAR_BUTTONS_TOOLTIPS, buttons);
    }
}