/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.ctf.core.tests.shared.LttngTraceGenerator;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
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
public class ControlFlowViewTest extends KernelTestBase {

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
    private static final String SELECT_NEXT_PROCESS = "Select Next Process";
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

    private SWTBotView fViewBot;

    /**
     * Before Test
     */
    @Override
    @Before
    public void before() {
        super.before();
        fViewBot = fBot.viewByTitle("Control Flow");
        fViewBot.show();
        fViewBot.setFocus();
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
     * Test tool bar buttons "Select Next State Change" and "Select Previous State Change"
     */
    @Test
    public void testToolBarSelectNextPreviousStateChange() {
        testNextPreviousEvent(() -> fViewBot.toolbarButton(SELECT_NEXT_STATE_CHANGE).click(),
                () -> fViewBot.toolbarButton(SELECT_NEXT_STATE_CHANGE).click(SWT.SHIFT),
                () -> fViewBot.toolbarButton(SELECT_PREVIOUS_STATE_CHANGE).click(),
                () -> fViewBot.toolbarButton(SELECT_PREVIOUS_STATE_CHANGE).click(SWT.SHIFT));
    }

    private void testNextPreviousEvent(Runnable selectNext, Runnable shiftSelectNext, Runnable selectPrevious, Runnable shiftSelectPrevious) {
        /* change window range to 10 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(10000000L, ITmfTimestamp.NANOSECOND_SCALE));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, range));
        fBot.waitUntil(ConditionHelpers.windowRange(range));

        /* set selection to trace start time */
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, START_TIME));
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME));

        /* select first item */
        SWTBotUtils.pressShortcutGoToTreeTop(KEYBOARD);
        fViewBot.toolbarButton(SELECT_NEXT_PROCESS).click();

        /* set focus on time graph */
        final TimeGraphControl timegraph = fViewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class));
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                timegraph.setFocus();
            }
        });

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
     * Test the legend content
     */
    @Test
    public void testLegend() {
        String[] labelValues = { "UNKNOWN", "WAIT_UNKNOWN", "WAIT_BLOCKED", "WAIT_FOR_CPU", "USERMODE", "SYSCALL", "INTERRUPTED" };
        SWTBotToolbarButton legendButton = fViewBot.toolbarButton("Show Legend");
        legendButton.click();
        fBot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive("States Transition Visualizer"));
        SWTBot bot = fBot.activeShell().bot();
        for (int i = 1; i < 8; i++) {
            SWTBotLabel label = bot.label(i);
            assertNotNull(label);
            assertEquals(labelValues[i - 1], label.getText());
        }
        bot.button("OK").click();
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

        SWTBotToolbarButton filterButton = fViewBot.toolbarButton("Show View Filters");
        filterButton.click();
        fBot.waitUntil(org.eclipse.swtbot.swt.finder.waits.Conditions.shellIsActive("Filter"));
        SWTBot bot = fBot.activeShell().bot();
        SWTBotTree treeBot = bot.tree();
        TreeCheckedCounter treeCheckCounter = new TreeCheckedCounter(treeBot);
        // get how many items there are
        Integer checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals("default", 226, checked.intValue());
        // test "uncheck all button"
        bot.button(UNCHECK_ALL).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals(0, checked.intValue());
        // test check active
        bot.button(CHECK_ACTIVE).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals(CHECK_ACTIVE, 69, checked.intValue());
        // test check all
        bot.button(CHECK_ALL).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals(CHECK_ALL, 226, checked.intValue());
        // test uncheck inactive
        bot.button(UNCHECK_INACTIVE).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals(UNCHECK_INACTIVE, 69, checked.intValue());
        // test check selected
        treeBot.getTreeItem(LttngTraceGenerator.getName()).select("gnuplot");
        bot.button(UNCHECK_ALL).click();
        bot.button(CHECK_SELECTED).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals(CHECK_SELECTED, 2, checked.intValue());
        // test check subtree
        bot.button(UNCHECK_ALL).click();
        bot.button(CHECK_SUBTREE).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals(CHECK_SUBTREE, 2, checked.intValue());
        // test uncheck selected
        bot.button(CHECK_ALL).click();
        bot.button(UNCHECK_SELECTED).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals(UNCHECK_SELECTED, 225, checked.intValue());
        // test uncheck subtree
        bot.button(CHECK_ALL).click();
        bot.button(UNCHECK_SUBTREE).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals(UNCHECK_SELECTED, 225, checked.intValue());
        // test filter
        bot.button(UNCHECK_ALL).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals(0, checked.intValue());
        bot.text().setText("half-life 3");
        SWTBotTreeItem treeItem = treeBot.getTreeItem(LttngTraceGenerator.getName());
        treeItem.rowCount();
        fBot.waitUntil(ConditionHelpers.treeItemCount(treeItem, 25));
        bot.button(CHECK_ALL).click();
        checked = UIThreadRunnable.syncExec(treeCheckCounter);
        assertEquals("Filtered", 26, checked.intValue());
        bot.button("OK").click();
        treeBot = fViewBot.bot().tree();
        treeItem = treeBot.getTreeItem(LttngTraceGenerator.getName());
        for (int i = 0; i < 25; i++) {
            assertEquals("Filtered Control flow view", "Half-life 3", treeItem.cell(i, 0));
        }
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
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(START_TIME, START_TIME)));

        /* select first item */
        final SWTBotTree tree = fViewBot.bot().tree();
        SWTBotUtils.pressShortcutGoToTreeTop(KEYBOARD);
        fViewBot.toolbarButton(SELECT_NEXT_PROCESS).click();

        /* set focus on time graph */
        final TimeGraphControl timegraph = fViewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class));
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                timegraph.setFocus();
            }
        });

        /* click "Follow CPU Forward" 3 times */
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME));
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME1, TID1_TIME1));
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME1, TID2_TIME1));
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME2));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME2, TID2_TIME2)));
        fBot.waitUntil(ConditionHelpers.treeSelectionContains(tree, 1, "2"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME2));

        /* shift-click "Follow CPU Forward" 3 times */
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME3));
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME4));
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME4));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME2, TID5_TIME1)));
        fBot.waitUntil(ConditionHelpers.treeSelectionContains(tree, 1, "5"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID5_TIME1));

        /* shift-click "Follow CPU Backward" 4 times */
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME4));
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME3));
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME2));
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME1));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME2, TID2_TIME1)));
        fBot.waitUntil(ConditionHelpers.treeSelectionContains(tree, 1, "2"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME1));

        /* click "Follow CPU Forward" 2 times */
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME2));
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME3));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME3, TID2_TIME3)));
        fBot.waitUntil(ConditionHelpers.treeSelectionContains(tree, 1, "2"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME3));

        /* shift-click "Follow CPU Backward" 3 times */
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME2));
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME1));
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID1_TIME1));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME3, TID1_TIME1)));
        fBot.waitUntil(ConditionHelpers.treeSelectionContains(tree, 1, "1"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID1_TIME1));

        /* shift-click "Follow CPU Forward" 4 times */
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME1));
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME2));
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME3));
        fViewBot.toolbarButton(FOLLOW_CPU_FORWARD).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME4));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(TID2_TIME3, TID2_TIME4)));
        fBot.waitUntil(ConditionHelpers.treeSelectionContains(tree, 1, "2"));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(TID2_TIME4));

        /* click "Follow CPU Backward" 5 times */
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME3, TID2_TIME3));
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME2, TID2_TIME2));
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID2_TIME1, TID2_TIME1));
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(TID1_TIME1, TID1_TIME1));
        fViewBot.toolbarButton(FOLLOW_CPU_BACKWARD).click();
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME));
        fBot.waitUntil(ConditionHelpers.selectionRange(new TmfTimeRange(START_TIME, START_TIME)));
        assertTrue(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange().contains(START_TIME));
    }

    private void timeGraphIsReadyCondition(@NonNull TmfTimeRange selectionRange) {
        IWorkbenchPart part = fViewBot.getViewReference().getPart(false);
        fBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition((AbstractTimeGraphView) part, selectionRange, selectionRange.getEndTime()));
    }
}
