/*******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.exceptions.WidgetNotFoundException;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.results.StringResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for testing copy to clipboard.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CopyToClipboardTest {

    private static final String LINE_SEPARATOR = System.getProperty("line.separator"); //$NON-NLS-1$
    private static final String HEADER_TEXT = "Timestamp\tHost\tLogger\tFile\tLine\tMessage" + LINE_SEPARATOR;
    private static final String EVENT1_TEXT = "01:01:01.000 000 000\tHostA\tLoggerA\tSourceFile\t4\tMessage A" + LINE_SEPARATOR;
    private static final String EVENT2_TEXT = "02:02:02.000 000 000\tHostB\tLoggerB\tSourceFile\t5\tMessage B" + LINE_SEPARATOR;
    private static final String EVENT3_TEXT = "03:03:03.000 000 000\tHostC\tLoggerC\tSourceFile\t6\tMessage C" + LINE_SEPARATOR;
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String TRACE_NAME = "syslog_collapse";
    private static final String TRACE_PATH = "testfiles/" + TRACE_NAME;
    private static final String TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";
    private static final String COPY_TO_CLIPBOARD = "Copy to Clipboard";
    private static final int TIMEOUT = 20000; /* 20 second timeout */

    private static File fTestFile = null;

    private static SWTWorkbenchBot fBot;
    private static SWTBotEditor fEditorBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();

        /* set up test trace */
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TRACE_PATH), null);
        URI uri;
        try {
            uri = FileLocator.toFileURL(location).toURI();
            fTestFile = new File(uri);
        } catch (URISyntaxException | IOException e) {
            fail(e.getMessage());
        }

        assumeTrue(fTestFile.exists());

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = TIMEOUT;
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), TRACE_TYPE);
        fEditorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());

    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void afterClass() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        fBot.closeAllEditors();
        SWTBotUtils.closeSecondaryShells(fBot);
        fLogger.removeAllAppenders();
    }

    /**
     * Test copy to clipboard with single selection
     */
    @Test
    public void testCopySingleSelection() {
        final SWTBotTable tableBot = fEditorBot.bot().table();
        tableBot.getTableItem(1).click();

        tableBot.contextMenu(COPY_TO_CLIPBOARD).click();
        assertClipboardContentsEquals(HEADER_TEXT + EVENT1_TEXT);
    }

    /**
     * Test copy to clipboard with multiple selection
     */
    @Test
    public void testCopyMultipleSelection() {
        final SWTBotTable tableBot = fEditorBot.bot().table();
        tableBot.getTableItem(1).click();
        tableBot.pressShortcut(Keystrokes.SHIFT, Keystrokes.DOWN);
        tableBot.pressShortcut(Keystrokes.SHIFT, Keystrokes.DOWN);

        tableBot.contextMenu(COPY_TO_CLIPBOARD).click();
        assertClipboardContentsEquals(HEADER_TEXT + EVENT1_TEXT + EVENT2_TEXT + EVENT3_TEXT);
    }

    /**
     * Test copy to clipboard not enabled when selection includes search row
     */
    @Test
    public void testNoCopySearchRow() {
        final SWTBotTable tableBot = fEditorBot.bot().table();
        tableBot.getTableItem(1).click();
        tableBot.pressShortcut(Keystrokes.SHIFT, Keystrokes.UP);

        assertContextMenuAbsent(tableBot, COPY_TO_CLIPBOARD);
    }

    private static void assertContextMenuAbsent(final SWTBotTable tableBot, final String text) {
        fBot.waitUntil(new DefaultCondition() {
            @Override
            public boolean test() throws Exception {
                try {
                    SWTBotPreferences.TIMEOUT = 0;
                    tableBot.contextMenu(text);
                } catch (WidgetNotFoundException e) {
                    return true;
                } finally {
                    SWTBotPreferences.TIMEOUT = TIMEOUT;
                }
                return false;
            }
            @Override
            public String getFailureMessage() {
                return text + " context menu present, absent expected.";
            }
        });
    }

    private static void assertClipboardContentsEquals(final String expected) {
        fBot.waitUntil(new DefaultCondition() {
            String actual;
            @Override
            public boolean test() throws Exception {
                actual = UIThreadRunnable.syncExec(new StringResult() {
                    @Override
                    public String run() {
                        Clipboard clipboard = new Clipboard(Display.getDefault());
                        TextTransfer textTransfer = TextTransfer.getInstance();
                        try {
                            return (String) clipboard.getContents(textTransfer);
                        } finally {
                            clipboard.dispose();
                        }
                    }
                });
                return expected.equals(actual);
            }
            @Override
            public String getFailureMessage() {
                return NLS.bind("Clipboard contents:\n{0}\nExpected:\n{1}",
                        actual, expected);
            }
        });
    }
}
