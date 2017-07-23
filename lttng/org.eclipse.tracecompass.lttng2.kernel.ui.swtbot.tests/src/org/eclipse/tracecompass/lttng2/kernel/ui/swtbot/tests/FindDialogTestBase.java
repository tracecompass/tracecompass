/*******************************************************************************
 * Copyright (c) 2016, 2017 Ericsson and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keyboard;
import org.eclipse.swtbot.swt.finder.keyboard.KeyboardFactory;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.matchers.WidgetOfType;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.utils.SWTUtils;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCheckBox;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotLabel;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
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
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Abstract class to build SWTBot test for time graph find dialog. Test the time
 * graph view find dialog and its options.
 *
 * @author Jean-Christian Kouame
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class FindDialogTestBase {

    /** LTTng kernel trace type */
    protected static final String KERNEL_TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    /** LTTng kernel perspective */
    protected static final String KERNEL_PERSPECTIVE_ID = "org.eclipse.linuxtools.lttng2.kernel.ui.perspective";
    /** Default project name */
    protected static final String TRACE_PROJECT_NAME = "test";

    /** The workbench bot */
    protected static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static final Keyboard KEYBOARD = KeyboardFactory.getSWTKeyboard();
    private static final @NonNull ITmfTimestamp START_TIME = TmfTimestamp.create(1412670961211260539L, ITmfTimestamp.NANOSECOND_SCALE);
    private static final @NonNull String SPACE = " ";
    private static final String REGEX_PREFIX = "\\A";
    private static final String DIALOG_TITLE = "Find";
    private String fFindText;
    private SWTBotView fViewBot;

    private int fSelectionIndex;

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
     * Before Class
     *
     * @throws IOException
     *             When the trace could not be opened
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
        SWTBotUtils.initialize();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", fBot);
        /* Switch perspectives */
        SWTBotUtils.switchToPerspective(KERNEL_PERSPECTIVE_ID);
        /* Create the trace project */
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        /* Open the trace */
        String tracePath = FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL()).getPath();
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, tracePath, KERNEL_TRACE_TYPE);
        /* Finish waiting for eclipse to load */
        SWTBotUtils.activateEditor(fBot, CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL().getPath().replaceAll("/", ""));
    }

    /**
     * After Class
     */
    @AfterClass
    public static void afterClass() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        fLogger.removeAllAppenders();
    }

    /**
     * Initialize the test and open the timegraph view and the find dialog
     */
    @Before
    public void before() {
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
    public void after() {
        closeDialog(getDialogBot());
        SWTBotUtils.closeSecondaryShells(fBot);
    }

    private static void openDialog(SWTBotView view) {
        view.setFocus();
        SWTBotUtils.pressShortcut(KEYBOARD, Keystrokes.HOME);
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
        String upper = fFindText.toUpperCase();
        String lower = fFindText.toLowerCase();

        SearchOptions options = getOptions(true, true, false, false, false);
        search(fFindText, options, findButton, bot);
        verifyStatusLabel(bot, true);
        search(fFindText.equals(upper) ? lower : upper, options, findButton, bot);
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

    private void verifySelection(String name, SearchOptions options, SWTBotView view, boolean isWrapped) {
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
