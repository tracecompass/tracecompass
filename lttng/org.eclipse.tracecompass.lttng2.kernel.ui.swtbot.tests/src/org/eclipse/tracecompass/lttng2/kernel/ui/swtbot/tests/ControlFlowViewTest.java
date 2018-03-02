/*******************************************************************************
 * Copyright (c) 2015, 2017 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.ctf.core.tests.shared.LttngTraceGenerator;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowView;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot tests for Control Flow view
 *
 * @author Patrick Tasse
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ControlFlowViewTest extends KernelTimeGraphViewTestBase {

    private static final String DIALOG_OK = "OK";
    private static final String DIALOG_CANCEL = "Cancel";

    private static final String THREAD_PRESENTATION_HIERARCHICAL = "Hierarchical";
    private static final String THREAD_PRESENTATION_FLAT = "Flat";

    private static final String DYNAMIC_FILTER_ACTIVE_THREADS_ONLY_TOGGLE = "Active Threads only";
    private static final String DYNAMIC_FILTER_ON_CPU_FIELD_MESSAGE = "e.g. 0-3,5,7-8";
    private static final String DYNAMIC_FILTERS_ALL_ACTIVE_RADIO = "All Active Threads";
    private static final String DYNAMIC_FILTERS_ON_CPU_RADIO = "Active Threads on CPUs:";
    private static final String DYNAMIC_FILTERS_SHOW_ACTIVE_THREADS_ONLY_CHECKBOX = "Show Active Threads Only";
    private static final String DYNAMIC_FILTERS_SHELL_TEXT = "Dynamic Filters Configuration";
    private static final String DYNAMIC_FILTER_CONFIGURE_LABEL = "Configure...";

    private static final String CHECK_SELECTED = "Check selected";
    private static final String CHECK_ALL = "Check all";
    private static final String CHECK_SUBTREE = "Check subtree";
    private static final String CHECK_ACTIVE = "Check Active";
    private static final String UNCHECK_SELECTED = "Uncheck selected";
    private static final String UNCHECK_ALL = "Uncheck all";
    private static final String UNCHECK_SUBTREE = "Uncheck subtree";
    private static final String UNCHECK_INACTIVE = "Uncheck Inactive";

    private static final String FOLLOW_CPU_BACKWARD = "Follow CPU Backward";
    private static final String FOLLOW_CPU_FORWARD = "Follow CPU Forward";
    private static final String SELECT_PREVIOUS_STATE_CHANGE = "Select Previous State Change";
    private static final String SELECT_NEXT_STATE_CHANGE = "Select Next State Change";
    private static final Keyboard KEYBOARD = KeyboardFactory.getSWTKeyboard();
    private static final @NonNull ITmfTimestamp START_TIME = TmfTimestamp.fromNanos(1368000272650993664L);
    private static final @NonNull ITmfTimestamp TID1_TIME1 = TmfTimestamp.fromNanos(1368000272651208412L);
    private static final @NonNull ITmfTimestamp TID1_TIME2 = TmfTimestamp.fromNanos(1368000272656147616L);
    private static final @NonNull ITmfTimestamp TID1_TIME3 = TmfTimestamp.fromNanos(1368000272656362364L);
    private static final @NonNull ITmfTimestamp TID1_TIME4 = TmfTimestamp.fromNanos(1368000272663234300L);
    private static final @NonNull ITmfTimestamp TID1_TIME5 = TmfTimestamp.fromNanos(1368000272663449048L);
    private static final @NonNull ITmfTimestamp TID1_TIME6 = TmfTimestamp.fromNanos(1368000272665596528L);
    private static final @NonNull ITmfTimestamp TID2_TIME1 = TmfTimestamp.fromNanos(1368000272651852656L);
    private static final @NonNull ITmfTimestamp TID2_TIME2 = TmfTimestamp.fromNanos(1368000272652067404L);
    private static final @NonNull ITmfTimestamp TID2_TIME3 = TmfTimestamp.fromNanos(1368000272652282152L);
    private static final @NonNull ITmfTimestamp TID2_TIME4 = TmfTimestamp.fromNanos(1368000272652496900L);
    private static final @NonNull ITmfTimestamp TID5_TIME1 = TmfTimestamp.fromNanos(1368000272652496900L);

    @Override
    protected SWTBotView getViewBot() {
        return fBot.viewByTitle("Control Flow");
    }

    @Override
    protected SWTBotView openView() {
        SWTBotUtils.openView(ControlFlowView.ID);
        return getViewBot();
    }

    @Override
    protected List<String> getLegendValues() {
        return Arrays.asList("Unknown", "Usermode", "System call", "Interrupt", "Wait blocked", "Wait for CPU", "Wait");
    }

    @Override
    protected List<String> getToolbarTooltips() {
        return Arrays.asList("Optimize", SEPARATOR,
                "Show View Filters", "Show Legend", SEPARATOR,
                "Reset the Time Scale to Default", "Select Previous State Change", "Select Next State Change", SEPARATOR,
                "Add Bookmark...", "Previous Marker", "Next Marker", SEPARATOR,
                "Select Previous Process", "Select Next Process", "Zoom In", "Zoom Out", SEPARATOR,
                "Hide Arrows", "Follow CPU Backward", "Follow CPU Forward",
                "Go to previous event of the selected thread", "Go to next event of the selected thread", SEPARATOR,
                "Pin View");
    }

    /**
     * Before Test
     */
    @Override
    @Before
    public void before() {
        super.before();
        SWTBotView viewBot = getViewBot();
        viewBot.show();
        viewBot.setFocus();
    }

    /**
     * Test keyboard navigation using ARROW_RIGHT and ARROW_LEFT
     */
    @Test
    public void testKeyboardLeftRight() {
        testNextPreviousEvent(() -> KEYBOARD.pressShortcut(Keystrokes.RIGHT),
                () -> KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.RIGHT),
                () -> KEYBOARD.pressShortcut(Keystrokes.LEFT),
                () -> KEYBOARD.pressShortcut(Keystrokes.SHIFT, Keystrokes.LEFT));
    }

    /**
     * Test tool bar buttons "Select Next State Change" and "Select Previous
     * State Change"
     */
    @Test
    public void testToolBarSelectNextPreviousStateChange() {
        SWTBotView viewBot = getViewBot();
        testNextPreviousEvent(() -> viewBot.toolbarButton(SELECT_NEXT_STATE_CHANGE).click(),
                () -> viewBot.toolbarButton(SELECT_NEXT_STATE_CHANGE).click(SWT.SHIFT),
                () -> viewBot.toolbarButton(SELECT_PREVIOUS_STATE_CHANGE).click(),
                () -> viewBot.toolbarButton(SELECT_PREVIOUS_STATE_CHANGE).click(SWT.SHIFT));
    }

    private void testNextPreviousEvent(Runnable selectNext, Runnable shiftSelectNext, Runnable selectPrevious, Runnable shiftSelectPrevious) {
        /* change window range to 10 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(10000000L, ITmfTimestamp.NANOSECOND_SCALE));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, range));
        fBot.waitUntil(ConditionHelpers.windowRange(range));

        /* set selection to trace start time */
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, START_TIME));
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME));

        /* set focus on time graph */
        SWTBotTimeGraph timeGraph = new SWTBotTimeGraph(getViewBot().bot());
        timeGraph.setFocus();

        /* select first item */
        SWTBotUtils.pressShortcut(KEYBOARD, Keystrokes.HOME);
        SWTBotUtils.pressShortcut(KEYBOARD, Keystrokes.DOWN);

        /* click "Select Next State Change" 3 times */
        selectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME1, TID1_TIME1));
        selectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME2, TID1_TIME2));
        selectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME3));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME3, TID1_TIME3)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME3));

        /* shift-click "Select Next State Change" 3 times */
        shiftSelectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME4));
        shiftSelectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME5));
        shiftSelectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME6));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME3, TID1_TIME6)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME6));

        /* shift-click "Select Previous State Change" 4 times */
        shiftSelectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME5));
        shiftSelectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME4));
        shiftSelectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME3));
        shiftSelectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME2));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME3, TID1_TIME2)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME2));

        /* click "Select Next State Change" 2 times */
        selectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME3));
        selectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME4, TID1_TIME4));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME4, TID1_TIME4)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME4));

        /* shift-click "Select Previous State Change" 3 times */
        shiftSelectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME4, TID1_TIME3));
        shiftSelectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME4, TID1_TIME2));
        shiftSelectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME4, TID1_TIME1));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME4, TID1_TIME1)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME1));

        /* shift-click "Select Next State Change" 4 times */
        shiftSelectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME4, TID1_TIME2));
        shiftSelectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME4, TID1_TIME3));
        shiftSelectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME4, TID1_TIME4));
        shiftSelectNext.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME4, TID1_TIME5));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID1_TIME4, TID1_TIME5)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME5));

        /* click "Select Previous State Change" 5 times */
        selectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME4, TID1_TIME4));
        selectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME3, TID1_TIME3));
        selectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME2, TID1_TIME2));
        selectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME1, TID1_TIME1));
        selectPrevious.run();
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(START_TIME, START_TIME)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(START_TIME));
    }

    /**
     * Test the filter
     */
    @Test
    public void testFilter() {
        /* change window range to 1 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(1000000L, ITmfTimestamp.NANOSECOND_SCALE));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, range));
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME));

        SWTBotView viewBot = getViewBot();
        SWTBotToolbarButton filterButton = viewBot.toolbarButton("Show View Filters");
        filterButton.click();
        fBot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive("Filter"));
        SWTBot bot = fBot.activeShell().bot();
        SWTBotTree treeBot = bot.tree();
        // get how many items there are
        int checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals("default", 226, checked);
        // test "uncheck all button"
        bot.button(UNCHECK_ALL).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(0, checked);
        // test check active
        bot.button(CHECK_ACTIVE).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(CHECK_ACTIVE, 69, checked);
        // test check all
        bot.button(CHECK_ALL).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(CHECK_ALL, 226, checked);
        // test uncheck inactive
        bot.button(UNCHECK_INACTIVE).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(UNCHECK_INACTIVE, 69, checked);
        // test check selected
        treeBot.getTreeItem(LttngTraceGenerator.getName()).select("gnuplot");
        bot.button(UNCHECK_ALL).click();
        bot.button(CHECK_SELECTED).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(CHECK_SELECTED, 2, checked);
        // test check subtree
        bot.button(UNCHECK_ALL).click();
        bot.button(CHECK_SUBTREE).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(CHECK_SUBTREE, 2, checked);
        // test uncheck selected
        bot.button(CHECK_ALL).click();
        bot.button(UNCHECK_SELECTED).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(UNCHECK_SELECTED, 225, checked);
        // test uncheck subtree
        bot.button(CHECK_ALL).click();
        bot.button(UNCHECK_SUBTREE).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(UNCHECK_SELECTED, 225, checked);
        // test filter
        bot.button(UNCHECK_ALL).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(0, checked);
        bot.text().setText("half-life 3");
        SWTBotTreeItem treeItem = treeBot.getTreeItem(LttngTraceGenerator.getName());
        treeItem.rowCount();
        fBot.waitUntil(ConditionHelpers.treeItemCount(treeItem, 25));
        bot.button(CHECK_ALL).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals("Filtered", 26, checked);
        bot.button(DIALOG_OK).click();
        SWTBotTimeGraph timeGraph = new SWTBotTimeGraph(getViewBot().bot());
        SWTBotTimeGraphEntry traceEntry = timeGraph.getEntry(LttngTraceGenerator.getName());
        for (SWTBotTimeGraphEntry entry : traceEntry.getEntries()) {
            assertEquals("Filtered Control flow view", "Half-life 3", entry.getText());
        }
    }

    /**
     * Test dynamic filters dialog
     */
    @Test
    public void testDynamicFiltersDialog() {

        String valid_cpu_ranges = "0,1,2-100";
        String invalid_cpu_ranges = "-1,1";

        /* Change window range to 10 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(10000000L, ITmfTimestamp.NANOSECOND_SCALE));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, range));
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, range.getStartTime(), range.getEndTime()));
        timeGraphIsReadyCondition(range);

        getViewBot().viewMenu(DYNAMIC_FILTER_CONFIGURE_LABEL).click();
        fBot.waitUntil(Conditions.shellIsActive(DYNAMIC_FILTERS_SHELL_TEXT));
        SWTBotShell shell = fBot.shell(DYNAMIC_FILTERS_SHELL_TEXT);
        shell.activate();

        /* Make sure nothing is checked and radio buttons are disabled */
        SWTBotCheckBox activeThreadsCheckbox = shell.bot().checkBox(DYNAMIC_FILTERS_SHOW_ACTIVE_THREADS_ONLY_CHECKBOX);
        SWTBotRadio onCpuRadio = shell.bot().radio(DYNAMIC_FILTERS_ON_CPU_RADIO);
        SWTBotRadio allActiveRadio = shell.bot().radio(DYNAMIC_FILTERS_ALL_ACTIVE_RADIO);
        SWTBotText onCpuField = shell.bot().textWithMessage(DYNAMIC_FILTER_ON_CPU_FIELD_MESSAGE);

        assertFalse(activeThreadsCheckbox.isChecked());
        assertFalse(onCpuRadio.isEnabled());
        assertFalse(allActiveRadio.isEnabled());
        assertFalse(onCpuField.isEnabled());

        /*
         * Test Active Filter buttons toggle
         */
        activeThreadsCheckbox.click();

        /* All objects should be enabled except for the CPU ranges field */
        assertTrue(activeThreadsCheckbox.isChecked());
        assertTrue(allActiveRadio.isEnabled());
        assertTrue(onCpuRadio.isEnabled());
        assertFalse(onCpuField.isEnabled());

        /*
         * The All Active Threads option should be the default for a new filter
         */
        assertTrue(allActiveRadio.isSelected());

        /*
         * Select All Threads on CPUs option
         */
        onCpuRadio.click();

        /* All objects should be enabled */
        assertTrue(activeThreadsCheckbox.isChecked());
        assertTrue(allActiveRadio.isEnabled());
        assertTrue(onCpuRadio.isEnabled());
        assertTrue(onCpuField.isEnabled());

        assertFalse(allActiveRadio.isSelected());
        assertTrue(onCpuRadio.isSelected());

        /*
         * Select All Active Threads then Active Threads on CPUs to validate
         * toggle of options
         */
        allActiveRadio.click();

        /* All objects should be enabled except for the CPU ranges field */
        assertTrue(activeThreadsCheckbox.isChecked());
        assertTrue(allActiveRadio.isEnabled());
        assertTrue(onCpuRadio.isEnabled());
        assertFalse(onCpuField.isEnabled());

        assertTrue(allActiveRadio.isSelected());
        assertFalse(onCpuRadio.isSelected());

        /* Select Active Threads on CPUs */
        onCpuRadio.click();

        /* All objects should be enabled */
        assertTrue(activeThreadsCheckbox.isChecked());
        assertTrue(allActiveRadio.isEnabled());
        assertTrue(onCpuRadio.isEnabled());
        assertTrue(onCpuField.isEnabled());

        assertFalse(allActiveRadio.isSelected());
        assertTrue(onCpuRadio.isSelected());

        /* Put an invalid value in the CPU ranges field */
        onCpuField.setText(invalid_cpu_ranges);

        /* Make sure the OK button is not enabled when in an invalid state */
        assertFalse(shell.bot().button(DIALOG_OK).isEnabled());

        /* Put a valid value in the CPU ranges field */
        onCpuField.setText(valid_cpu_ranges);

        /* Make sure the OK button is enabled when in a valid state */
        assertTrue(shell.bot().button(DIALOG_OK).isEnabled());

        shell.bot().button(DIALOG_OK).click();
        timeGraphIsReadyCondition(range);

        /* Make sure that the quick Active Thread Filter toggle is checked */
        assertTrue(getViewBot().viewMenu(DYNAMIC_FILTER_ACTIVE_THREADS_ONLY_TOGGLE).isChecked());

        /* Make sure that the Flat presentation is checked */
        assertTrue(getViewBot().viewMenu(THREAD_PRESENTATION_FLAT).isChecked());
        assertFalse(getViewBot().viewMenu(THREAD_PRESENTATION_HIERARCHICAL).isChecked());

        /* Reopen the dialog */
        getViewBot().viewMenu(DYNAMIC_FILTER_CONFIGURE_LABEL).click();
        fBot.waitUntil(Conditions.shellIsActive(DYNAMIC_FILTERS_SHELL_TEXT));
        shell = fBot.shell(DYNAMIC_FILTERS_SHELL_TEXT);
        shell.activate();

        /* Make sure nothing is checked and radio buttons are disabled */
        activeThreadsCheckbox = shell.bot().checkBox(DYNAMIC_FILTERS_SHOW_ACTIVE_THREADS_ONLY_CHECKBOX);
        onCpuRadio = shell.bot().radio(DYNAMIC_FILTERS_ON_CPU_RADIO);
        allActiveRadio = shell.bot().radio(DYNAMIC_FILTERS_ALL_ACTIVE_RADIO);
        onCpuField = shell.bot().textWithMessage(DYNAMIC_FILTER_ON_CPU_FIELD_MESSAGE);

        /* Make sure the previous settings are set correctly */
        assertTrue(activeThreadsCheckbox.isChecked());
        assertTrue(allActiveRadio.isEnabled());
        assertTrue(onCpuRadio.isEnabled());
        assertTrue(onCpuField.isEnabled());
        assertFalse(allActiveRadio.isSelected());
        assertTrue(onCpuRadio.isSelected());
        assertTrue(onCpuField.isEnabled());
        assertEquals("CPU ranges not equal", onCpuField.getText(), valid_cpu_ranges);

        /*
         * Change to All Active Threads option click OK then reopen. The
         * previous CPU range should still be there.
         */

        allActiveRadio.click();
        /* Make sure that the ranges are still visible but disabled */
        assertFalse(onCpuField.isEnabled());
        assertEquals("Cpu ranges not equal", onCpuField.getText(), valid_cpu_ranges);

        /* Close the dialog */
        shell.bot().button(DIALOG_OK).click();
        timeGraphIsReadyCondition(range);

        /* Open the dialog */
        getViewBot().viewMenu(DYNAMIC_FILTER_CONFIGURE_LABEL).click();
        fBot.waitUntil(Conditions.shellIsActive(DYNAMIC_FILTERS_SHELL_TEXT));
        shell = fBot.shell(DYNAMIC_FILTERS_SHELL_TEXT);
        shell.activate();

        activeThreadsCheckbox = shell.bot().checkBox(DYNAMIC_FILTERS_SHOW_ACTIVE_THREADS_ONLY_CHECKBOX);
        onCpuRadio = shell.bot().radio(DYNAMIC_FILTERS_ON_CPU_RADIO);
        allActiveRadio = shell.bot().radio(DYNAMIC_FILTERS_ALL_ACTIVE_RADIO);
        onCpuField = shell.bot().textWithMessage(DYNAMIC_FILTER_ON_CPU_FIELD_MESSAGE);

        /* Range field should have a value in it */
        assertTrue(activeThreadsCheckbox.isChecked());
        assertTrue(allActiveRadio.isEnabled());
        assertTrue(onCpuRadio.isEnabled());
        assertFalse(onCpuField.isEnabled());
        assertTrue(allActiveRadio.isSelected());
        assertFalse(onCpuRadio.isSelected());
        assertFalse(onCpuField.isEnabled());
        assertEquals("CPU ranges not equal", onCpuField.getText(), valid_cpu_ranges);

        shell.bot().button(DIALOG_CANCEL).click();
    }

    /**
     * Test dynamic filters dialog
     */
    @Test
    public void testDynamicFiltering() {
        /* Change window range to 10 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(10000000L, ITmfTimestamp.NANOSECOND_SCALE));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, range));
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, range.getStartTime(), range.getEndTime()));
        timeGraphIsReadyCondition(range);

        SWTBotTimeGraph timeGraph = new SWTBotTimeGraph(getViewBot().bot());
        SWTBotTimeGraphEntry traceEntry = timeGraph.getEntry(LttngTraceGenerator.getName());
        int originLength = traceEntry.getEntries().length;

        getViewBot().viewMenu(DYNAMIC_FILTER_CONFIGURE_LABEL).click();
        fBot.waitUntil(Conditions.shellIsActive(DYNAMIC_FILTERS_SHELL_TEXT));
        SWTBotShell shell = fBot.shell(DYNAMIC_FILTERS_SHELL_TEXT);
        shell.activate();

        /* Make sure nothing is checked and radio buttons are disabled */
        SWTBotCheckBox activeThreadsCheckbox = shell.bot().checkBox(DYNAMIC_FILTERS_SHOW_ACTIVE_THREADS_ONLY_CHECKBOX);
        assertFalse(activeThreadsCheckbox.isChecked());

        /*
         * Test Active Filter buttons toggle
         */
        activeThreadsCheckbox.click();
        /* All objects should be enabled except for the CPU ranges field */
        assertTrue(activeThreadsCheckbox.isChecked());

        shell.bot().button(DIALOG_OK).click();

        /* Change window range to 50 us */
        range = new TmfTimeRange(START_TIME, START_TIME.normalize(50000L, ITmfTimestamp.NANOSECOND_SCALE));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, range));
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, range.getStartTime(), range.getEndTime()));
        timeGraphIsReadyCondition(range);

        timeGraph = new SWTBotTimeGraph(getViewBot().bot());
        traceEntry = timeGraph.getEntry(LttngTraceGenerator.getName());

        /* Verify that number active entries changed */
        assertTrue(traceEntry.getEntries().length > 0);
        assertTrue(originLength > traceEntry.getEntries().length);

        getViewBot().viewMenu(DYNAMIC_FILTER_CONFIGURE_LABEL).click();
        fBot.waitUntil(Conditions.shellIsActive(DYNAMIC_FILTERS_SHELL_TEXT));
        shell = fBot.shell(DYNAMIC_FILTERS_SHELL_TEXT);
        shell.activate();

        activeThreadsCheckbox = shell.bot().checkBox(DYNAMIC_FILTERS_SHOW_ACTIVE_THREADS_ONLY_CHECKBOX);
        assertTrue(activeThreadsCheckbox.isChecked());

        /*
         * Test Active Filter buttons toggle
         */
        activeThreadsCheckbox.click();
        /* All objects should be enabled except for the CPU ranges field */
        assertFalse(activeThreadsCheckbox.isChecked());
    }

    /**
     * Test tool bar buttons "Follow CPU Forward" and "Follow CPU Backward"
     */
    @Test
    public void testToolBarFollowCPUForwardBackward() {
        /* change window range to 10 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(10000000L, ITmfTimestamp.NANOSECOND_SCALE));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, range));
        fBot.waitUntil(ConditionHelpers.windowRange(range));

        /* set selection to trace start time */
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, START_TIME));
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME));

        SWTBotView viewBot = getViewBot();

        /* set focus on time graph */
        SWTBotTimeGraph timeGraph = new SWTBotTimeGraph(viewBot.bot());
        timeGraph.setFocus();

        /* select first item */
        SWTBotUtils.pressShortcut(KEYBOARD, Keystrokes.HOME);
        SWTBotUtils.pressShortcut(KEYBOARD, Keystrokes.DOWN);

        /* click "Follow CPU Forward" 3 times */
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME));
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME1, TID1_TIME1));
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME1, TID2_TIME1));
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME2));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME2, TID2_TIME2)));
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraph, 1, "2"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME2));

        /* shift-click "Follow CPU Forward" 3 times */
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME3));
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME4));
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME4));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME2, TID5_TIME1)));
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraph, 1, "5"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID5_TIME1));

        /* shift-click "Follow CPU Backward" 4 times */
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME4));
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME3));
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME2));
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME1));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME2, TID2_TIME1)));
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraph, 1, "2"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME1));

        /* click "Follow CPU Forward" 2 times */
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME2));
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME3));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME3, TID2_TIME3)));
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraph, 1, "2"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME3));

        /* shift-click "Follow CPU Backward" 3 times */
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME2));
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME1));
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID1_TIME1));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME3, TID1_TIME1)));
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraph, 1, "1"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME1));

        /* shift-click "Follow CPU Forward" 4 times */
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME1));
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME2));
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME3));
        viewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME4));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME3, TID2_TIME4)));
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraph, 1, "2"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME4));

        /* click "Follow CPU Backward" 5 times */
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME3));
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME2));
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME1, TID2_TIME1));
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME1, TID1_TIME1));
        viewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(START_TIME, START_TIME)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(START_TIME));
    }

    private void timeGraphIsReadyCondition(@NonNull TmfTimeRange selectionRange) {
        IWorkbenchPart part = getViewBot().getViewReference().getPart(false);
        fBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition((AbstractTimeGraphView) part, selectionRange, selectionRange.getEndTime()));
    }
}
