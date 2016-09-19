/*******************************************************************************
 * Copyright (c) 2016 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Abstract class to build SWTBot test for time graph find dialog. Test the time
 * graph view find dialog and its options.
 *
 * @author Jean-Christian Kouame
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class FindDialogTestBase extends KernelTestBase {

    private static final Keyboard KEYBOARD = KeyboardFactory.getSWTKeyboard();
    private static final @NonNull ITmfTimestamp START_TIME = TmfTimestamp.create(1368000272650993664L, ITmfTimestamp.NANOSECOND_SCALE);
    private static final @NonNull String SPACE = " ";
    private static final String REGEX_PREFIX = "\\A";
    private static final String DIALOG_TITLE = "Find";
    private String fFindText;
    private SWTBotView fViewBot;

    private static int fSelectionIndex;

    /**
     * Get the title of the time graph view the find dialog will use
     *
     * @return The view title
     */
    protected abstract String getViewTitle();

    /**
     * Get the string that will be used for the search
     *
     * @return The string
     */
    protected abstract String getFindText();

    /**
     * Initialize the test and open the timegraph view and the find dialog
     */
    @Before
    public void init() {
        String title = getViewTitle();
        fViewBot = fBot.viewByTitle(title);
        fViewBot.show();
        fViewBot.setFocus();

        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, START_TIME));
        fBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition((AbstractTimeGraphView) fViewBot.getViewReference().getPart(false), new TmfTimeRange(START_TIME, START_TIME), START_TIME));
        openDialog(fViewBot);

        fFindText = getFindText();
    }

    /**
     * After method to close the dialog
     */
    @After
    public void afterTest() {
        closeDialog(getDialogBot());
    }

    private static void openDialog(SWTBotView view) {
        view.setFocus();
        SWTBotUtils.pressShortcutGoToTreeTop(KEYBOARD);
        if (SWTUtils.isMac()) {
            KEYBOARD.pressShortcut(Keystrokes.COMMAND, KeyStroke.getInstance('F'));
        } else {
            KEYBOARD.pressShortcut(Keystrokes.CTRL, KeyStroke.getInstance('F'));
        }
        fBot.waitUntil(Conditions.shellIsActive(DIALOG_TITLE));
    }

    /**
     * Test wrap search option
     */
    @Test
    public void testWrapSearch() {
        SWTBot bot = getDialogBot();
        SWTBotButton findButton = bot.button("Find");

        SearchOptions options = getOptions(false, false, true, false, false);
        search(fFindText, options, findButton, bot);
        assertTrue(isWrapped(bot));
        verifySelection(fFindText, options, fViewBot, isWrapped(bot));
        options = getOptions(true, false, true, false, false);
        search(fFindText, options, findButton, bot);
        assertTrue(isWrapped(bot));
    }

    /**
     * Test the direction search option
     */
    @Test
    public void testDirection() {
        SWTBot bot = getDialogBot();
        SWTBotButton findButton = bot.button("Find");

        // forward
        testDirectionSearch(true, fFindText, bot, findButton, fViewBot);
        testDirectionSearch(false, fFindText, bot, findButton, fViewBot);
    }

    private void testDirectionSearch(boolean forward, String findText, SWTBot bot, SWTBotButton findButton, SWTBotView view) {
        SearchOptions options = getOptions(forward, false, true, false, false);
        fSelectionIndex = getSelectionIndex(view);
        search(findText, options, findButton, bot);
        verifySelection(findText, options, view, isWrapped(bot));
    }

    /**
     * Test the case sensitive search option
     */
    @Test
    public void testCaseSensitive() {
        SWTBot bot = getDialogBot();
        SWTBotButton findButton = bot.button("Find");

        SearchOptions options = getOptions(true, true, false, false, false);
        search(fFindText, options, findButton, bot);
        verifyStatusLabel(bot, true);
        search(fFindText.toLowerCase(), options, findButton, bot);
        verifyStatusLabel(bot, false);
    }

    /**
     * Test the whole word search option
     */
    @Test
    public void testWholeWord() {
        SWTBot bot = getDialogBot();
        SWTBotButton findButton = bot.button("Find");

        @NonNull
        String text = fFindText.split(SPACE)[0];
        System.out.println("Reg ex : " + text);
        SearchOptions options = getOptions(true, false, false, true, false);
        search(text, options, findButton, bot);
        verifyStatusLabel(bot, true);
        search(text.substring(0, text.length() - 1), options, findButton, bot);
        verifyStatusLabel(bot, false);
    }

    /**
     * Test the regular expression search option
     */
    @Test
    public void testRegEx() {
        SWTBot bot = getDialogBot();
        SWTBotButton findButton = bot.button("Find");

        final String text = REGEX_PREFIX + fFindText.split(SPACE)[0];
        System.out.println("Reg ex : " + text);
        SearchOptions options = getOptions(true, false, false, false, true);
        search(text, options, findButton, bot);
        verifyStatusLabel(bot, true);
        options = getOptions(true, false, false, false, false);
        search(text, options, findButton, bot);
        verifyStatusLabel(bot, false);
    }

    /**
     * Test open/close the find dialog
     */
    @Test
    public void testOpenCloseDialog() {
        SWTBotShell shell = getDialogShell();
        closeDialog(getDialogBot());
        fBot.waitUntil(Conditions.shellCloses(shell));
        openDialog(fViewBot);
    }

    private static void verifyStatusLabel(SWTBot bot, boolean shouldBeFound) {
        // Get the second label in the dialog
        SWTBotLabel statusLabel = bot.label(1);
        assertTrue("status label", shouldBeFound == !statusLabel.getText().equals("Entry not found"));
    }

    /**
     * Get the find dialog bot
     *
     * @return The bot
     */
    private static SWTBot getDialogBot() {
        return getDialogShell().bot();
    }

    /**
     * Get the find dialog shell bot
     *
     * @return The shell bot
     */
    private static SWTBotShell getDialogShell() {
        return fBot.shell(DIALOG_TITLE);
    }

    private static void closeDialog(SWTBot bot) {
        bot.button("Close").click();
    }

    private static void search(String findText, SearchOptions options, SWTBotButton findButton, SWTBot bot) {
        // set the text to search
        SWTBotCombo findFieldCombo = bot.comboBox();
        findFieldCombo.setText(findText);
        assertTrue("Find combo", findFieldCombo.getText().equals(findText));

        // set the options
        SWTBotRadio directions = options.forwardSearch ? bot.radio("Forward").click() : bot.radio("Backward").click();
        assertTrue("direction", directions.isSelected());

        setCheckButton("Case sensitive", options.caseSensitive, bot);
        setCheckButton("Wrap search", options.wrapSearch, bot);
        setCheckButton("Whole word", options.wholeWord, bot);
        setCheckButton("Regular expression", options.regExSearch, bot);

        findButton.click();
    }

    private SearchOptions getOptions(boolean forward, boolean caseSensitive, boolean wrapSearch, boolean wholeWord, boolean regEx) {
        SearchOptions options = new SearchOptions();
        options.forwardSearch = forward;
        options.caseSensitive = caseSensitive;
        options.wrapSearch = wrapSearch;
        options.wholeWord = wholeWord;
        options.regExSearch = regEx;
        return options;
    }

    private static void setCheckButton(String mnemonic, boolean option, SWTBot bot) {
        final SWTBotCheckBox checkBox = bot.checkBox(mnemonic);
        if (checkBox.isEnabled()) {
            if (option) {
                checkBox.select();
            } else {
                checkBox.deselect();
            }
        }
    }

    private static void verifySelection(String name, SearchOptions options, SWTBotView view, boolean isWrapped) {
        final String entryName = getTimegraphSelectionName(view);
        assertTrue("entry name", entryName != null && entryName.contains(name));

        final int selectionIndex = getSelectionIndex(view);
        if (!isWrapped) {
            assertTrue("selection index", options.forwardSearch ? selectionIndex > fSelectionIndex : selectionIndex < fSelectionIndex);
        } else {
            assertTrue("selection index", options.forwardSearch ? selectionIndex <= fSelectionIndex : selectionIndex >= fSelectionIndex);
        }
        fSelectionIndex = selectionIndex;
    }

    private static boolean isWrapped(final SWTBot bot) {
        return bot.label(1).getText().equals("Wrapped search");
    }

    /**
     * Get the timegraph view selected entry
     *
     * @param viewBot
     *            The timegraph view bot
     * @return The selected entry
     */
    private static String getTimegraphSelectionName(final SWTBotView view) {
        final TimeGraphControl timegraph = view.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class));
        return UIThreadRunnable.syncExec(() -> {
            ITimeGraphEntry entry = timegraph.getSelectedTrace();
            if (entry != null) {
                return entry.getName();
            }
            return null;
        });
    }

    /**
     * Get the index of the entry selected in the timegraph view
     *
     * @param viewBot
     *            The timegraph view bot
     * @return
     */
    private static Integer getSelectionIndex(SWTBotView viewBot) {
        final TimeGraphControl timegraph = viewBot.bot().widget(WidgetOfType.widgetOfType(TimeGraphControl.class));
        return UIThreadRunnable.syncExec(() -> {
            return timegraph.getSelectedIndex();
        });
    }

    private class SearchOptions {
        boolean forwardSearch;
        boolean caseSensitive;
        boolean wrapSearch;
        boolean wholeWord;
        boolean regExSearch;
    }
}
