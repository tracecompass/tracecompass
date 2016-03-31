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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swt.SWT;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * SWTBot tests for Resources view
 *
 * @author Patrick Tasse
 */
public class ResourcesViewTest extends KernelTestBase {

    private static final String NEXT_MARKER = "Next Marker";
    private static final String PREVIOUS_MARKER = "Previous Marker";
    private static final String SELECT_NEXT_EVENT = "Select Next Event";
    private static final String SELECT_PREVIOUS_EVENT = "Select Previous Event";
    private static final String ADD_BOOKMARK = "Add Bookmark...";
    private static final String REMOVE_BOOKMARK = "Remove Bookmark";
    private static final String ADD_BOOKMARK_DIALOG = "Add Bookmark";
    private static final String LOST_EVENTS = "Lost Events";
    private static final String OK = "OK";
    private static final Keyboard KEYBOARD = KeyboardFactory.getSWTKeyboard();
    private static final @NonNull ITmfTimestamp START_TIME = new TmfNanoTimestamp(1368000272650993664L);
    private static final @NonNull ITmfTimestamp LOST_EVENT_TIME1 = new TmfNanoTimestamp(1368000272681793477L);
    private static final @NonNull ITmfTimestamp LOST_EVENT_END1 = new TmfNanoTimestamp(1368000272681793477L + 7425331L);
    private static final @NonNull ITmfTimestamp LOST_EVENT_TIME2 = new TmfNanoTimestamp(1368000272820875850L);
    private static final @NonNull ITmfTimestamp LOST_EVENT_END2 = new TmfNanoTimestamp(1368000272820875850L + 6640670L);
    private static final @NonNull ITmfTimestamp LOST_EVENT_TIME3 = new TmfNanoTimestamp(1368000272882715015L);
    private static final @NonNull ITmfTimestamp LOST_EVENT_END3 = new TmfNanoTimestamp(1368000272882715015L + 11373385L);
    private static final @NonNull ITmfTimestamp CPU0_TIME1 = new TmfNanoTimestamp(1368000272651208412L);
    private static final @NonNull ITmfTimestamp CPU0_TIME2 = new TmfNanoTimestamp(1368000272651852656L);
    private static final @NonNull ITmfTimestamp CPU0_TIME3 = new TmfNanoTimestamp(1368000272652067404L);
    private static final @NonNull ITmfTimestamp CPU0_TIME4 = new TmfNanoTimestamp(1368000272652282152L);
    private static final @NonNull ITmfTimestamp CPU0_TIME5 = new TmfNanoTimestamp(1368000272653141144L);

    private SWTBotView fViewBot;

    /**
     * Before Test
     */
    @Override
    @Before
    public void before() {
        fViewBot = fBot.viewByTitle("Resources");
        fViewBot.show();
        super.before();
        fViewBot.setFocus();
    }

    /**
     * Test keyboard marker navigation using '.' and ','
     */
    @Test
    public void testKeyboardSelectNextPreviousMarker() {
        testNextPreviousMarker(
                () -> KEYBOARD.pressShortcut(KeyStroke.getInstance('.')),
                () -> KEYBOARD.pressShortcut(Keystrokes.SHIFT, KeyStroke.getInstance('.')),
                () -> KEYBOARD.pressShortcut(KeyStroke.getInstance(',')),
                () -> KEYBOARD.pressShortcut(Keystrokes.SHIFT, KeyStroke.getInstance(',')));
    }

    /**
     * Test tool bar buttons "Next Marker" and "Previous Marker"
     */
    @Test
    public void testToolBarSelectNextPreviousMarker() {
        testNextPreviousMarker(
                () -> fViewBot.toolbarButton(NEXT_MARKER).click(),
                () -> fViewBot.toolbarButton(NEXT_MARKER).click(SWT.SHIFT),
                () -> fViewBot.toolbarButton(PREVIOUS_MARKER).click(),
                () -> fViewBot.toolbarButton(PREVIOUS_MARKER).click(SWT.SHIFT));
    }

    private void testNextPreviousMarker(Runnable nextMarker, Runnable shiftNextMarker, Runnable previousMarker, Runnable shiftPreviousMarker) {
        /* set selection to trace start time */
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, START_TIME));
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME), START_TIME);

        /* select first item */
        KEYBOARD.pressShortcut(Keystrokes.HOME);

        /* click "Next Marker" 3 times */
        nextMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME1, LOST_EVENT_END1), LOST_EVENT_TIME1);
        nextMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME2, LOST_EVENT_END2), LOST_EVENT_TIME2);
        nextMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME3, LOST_EVENT_END3), LOST_EVENT_TIME3);

        /* shift-click "Previous Marker" 3 times */
        shiftPreviousMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME3, LOST_EVENT_TIME3), LOST_EVENT_TIME3);
        shiftPreviousMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME3, LOST_EVENT_TIME2), LOST_EVENT_TIME2);
        shiftPreviousMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME3, LOST_EVENT_TIME1), LOST_EVENT_TIME1);

        /* shift-click "Next Marker" 3 times */
        shiftNextMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME3, LOST_EVENT_END1), LOST_EVENT_END1);
        shiftNextMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME3, LOST_EVENT_END2), LOST_EVENT_END2);
        shiftNextMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME3, LOST_EVENT_END3), LOST_EVENT_END3);

        /* click "Previous Marker" 3 times */
        previousMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME2, LOST_EVENT_END2), LOST_EVENT_TIME2);
        previousMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME1, LOST_EVENT_END1), LOST_EVENT_TIME1);
        previousMarker.run();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME1, LOST_EVENT_END1), LOST_EVENT_TIME1);
    }

    /**
     * Test "Show Markers" view menu
     */
    /* SWTBot doesn't support dynamic view menus yet */
    @Ignore
    @Test
    public void testShowMarkers() {
        /* set selection to trace start time */
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, START_TIME));
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME), START_TIME);

        /* select first item */
        KEYBOARD.pressShortcut(Keystrokes.HOME);

        /* check that "Next Marker" and "Previous Marker" are enabled */
        assertTrue(fViewBot.toolbarButton(NEXT_MARKER).isEnabled());
        assertTrue(fViewBot.toolbarButton(PREVIOUS_MARKER).isEnabled());

        /* disable Lost Events markers */
        fViewBot.viewMenu(LOST_EVENTS).click();

        /* check that "Next Marker" and "Previous Marker" are disabled */
        assertFalse(fViewBot.toolbarButton(NEXT_MARKER).isEnabled());
        assertFalse(fViewBot.toolbarButton(PREVIOUS_MARKER).isEnabled());

        /* enable Lost Events markers */
        fViewBot.viewMenu(LOST_EVENTS).click();

        /* check that "Next Marker" and "Previous Marker" are enabled */
        assertTrue(fViewBot.toolbarButton(NEXT_MARKER).isEnabled());
        assertTrue(fViewBot.toolbarButton(PREVIOUS_MARKER).isEnabled());
    }

    /**
     * Test "Next Event" tool bar button sub-menu
     */
    /* SWTBot doesn't support clicking the same tool bar sub-menu twice */
    @Ignore
    @Test
    public void testMarkerNavigationSubMenu() {
        /* set selection to trace start time */
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, START_TIME));
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME), START_TIME);

        /* select first item */
        KEYBOARD.pressShortcut(Keystrokes.HOME);

        /* disable Lost Events navigation */
        fViewBot.toolbarDropDownButton(NEXT_MARKER).menuItem(LOST_EVENTS).click();

        /* click "Next Marker" */
        fViewBot.toolbarButton(NEXT_MARKER).click();
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME), START_TIME);

        /* enable Lost Events navigation */
        fViewBot.toolbarDropDownButton(NEXT_MARKER).menuItem(LOST_EVENTS).click();

        /* click "Next Marker" */
        fViewBot.toolbarButton(NEXT_MARKER).click();
        timeGraphIsReadyCondition(new TmfTimeRange(LOST_EVENT_TIME1, LOST_EVENT_END1), LOST_EVENT_TIME1);
    }

    /**
     * Test tool bar button "Add Bookmark..." and "Remove Bookmark"
     */
    @Test
    public void testAddRemoveBookmark() {
        /* change window range to 10 ms */
        TmfTimeRange range = new TmfTimeRange(START_TIME, START_TIME.normalize(10000000L, ITmfTimestamp.NANOSECOND_SCALE));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, range));
        fBot.waitUntil(ConditionHelpers.windowRange(range));

        /* set selection to trace start time */
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, START_TIME));
        timeGraphIsReadyCondition(new TmfTimeRange(START_TIME, START_TIME), START_TIME);

        /* select first CPU resource */
        KEYBOARD.pressShortcut(Keystrokes.HOME);
        KEYBOARD.pressShortcut(Keystrokes.DOWN);

        /* click "Select Next Event" 2 times */
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        timeGraphIsReadyCondition(new TmfTimeRange(CPU0_TIME1, CPU0_TIME1), CPU0_TIME1);
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        timeGraphIsReadyCondition(new TmfTimeRange(CPU0_TIME2, CPU0_TIME2), CPU0_TIME2);

        /* click "Add Bookmark..." and fill Add Bookmark dialog */
        fViewBot.toolbarButton(ADD_BOOKMARK).click();
        SWTBot dialogBot = fBot.shell(ADD_BOOKMARK_DIALOG).bot();
        dialogBot.text().setText("B1");
        dialogBot.button(OK).click();

        /* click "Select Next Event" 2 times and shift-click "Select Next Event*/
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        timeGraphIsReadyCondition(new TmfTimeRange(CPU0_TIME3, CPU0_TIME3), CPU0_TIME3);
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click();
        timeGraphIsReadyCondition(new TmfTimeRange(CPU0_TIME4, CPU0_TIME4), CPU0_TIME4);
        fViewBot.toolbarButton(SELECT_NEXT_EVENT).click(SWT.SHIFT);
        timeGraphIsReadyCondition(new TmfTimeRange(CPU0_TIME4, CPU0_TIME5), CPU0_TIME5);

        /* click "Add Bookmark..." and fill Add Bookmark dialog */
        fViewBot.toolbarButton(ADD_BOOKMARK).click();
        dialogBot = fBot.shell(ADD_BOOKMARK_DIALOG).bot();
        dialogBot.text().setText("B2");
        dialogBot.button(OK).click();

        /* click "Previous Marker" */
        fViewBot.toolbarButton(PREVIOUS_MARKER).click();
        timeGraphIsReadyCondition(new TmfTimeRange(CPU0_TIME2, CPU0_TIME2), CPU0_TIME2);

        /* click "Remove Bookmark" */
        fViewBot.toolbarButton(REMOVE_BOOKMARK).click();

        /* click "Next Marker" */
        fViewBot.toolbarButton(NEXT_MARKER).click();
        timeGraphIsReadyCondition(new TmfTimeRange(CPU0_TIME4, CPU0_TIME5), CPU0_TIME5);

        /* click "Remove Bookmark" */
        fViewBot.toolbarButton(REMOVE_BOOKMARK).click();

        /* click "Previous Marker" */
        fViewBot.toolbarButton(PREVIOUS_MARKER).click();
        timeGraphIsReadyCondition(new TmfTimeRange(CPU0_TIME4, CPU0_TIME5), CPU0_TIME5);

        /* click "Select Previous Event" */
        fViewBot.toolbarButton(SELECT_PREVIOUS_EVENT).click();
        timeGraphIsReadyCondition(new TmfTimeRange(CPU0_TIME4, CPU0_TIME4), CPU0_TIME4);
    }

    private void timeGraphIsReadyCondition(@NonNull TmfTimeRange selectionRange, @NonNull ITmfTimestamp visibleTime) {
        IWorkbenchPart part = fViewBot.getViewReference().getPart(false);
        fBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition((AbstractTimeGraphView) part, selectionRange, visibleTime));
    }
}
