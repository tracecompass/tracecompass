/*******************************************************************************
 * Copyright (c) 2016, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.keyboard.Keystrokes;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableList;

/**
 * SWTBot test for testing export to tsv.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
@NonNullByDefault
public class ExportToTsvTest {

    private static final class FileLargerThanZeroCondition implements ICondition {
        private File fFile;

        public FileLargerThanZeroCondition(File file) {
            fFile = file;
        }

        @Override
        public boolean test() throws Exception {
            return fFile.length() >= 1;
        }

        @Override
        public void init(@Nullable SWTBot bot) {
            // nothing
        }

        @Override
        public String getFailureMessage() {
            return "File is still of length 0 : " + fFile.getAbsolutePath();
        }
    }

    private static final String HEADER_TEXT = "Timestamp\tHost\tLogger\tFile\tLine\tMessage";
    private static final String EVENT1_TEXT = "01:01:01.000 000 000\tHostA\tLoggerA\tSourceFile\t4\tMessage A";
    private static final String EVENT2_TEXT = "02:02:02.000 000 000\tHostB\tLoggerB\tSourceFile\t5\tMessage B";
    private static final String EVENT3_TEXT = "03:03:03.000 000 000\tHostC\tLoggerC\tSourceFile\t6\tMessage C";
    private static final String TRACE_PROJECT_NAME = "test";
    private static final String TRACE_NAME = "syslog_collapse";
    private static final String TRACE_PATH = "testfiles/" + TRACE_NAME;
    private static final String TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";
    private static final String EXPORT_TO_TSV = "Export To Text...";
    private static final int TIMEOUT = 20000; /* 20 second timeout */

    private @Nullable static File fTestFile = null;

    private static SWTWorkbenchBot fBot = new SWTWorkbenchBot();
    private @Nullable SWTBotEditor fEditorBot;
    private @Nullable String fAbsolutePath;

    /** The Log4j logger instance. */
    @SuppressWarnings("null")
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

        File testFile = fTestFile;
        assertNotNull(testFile);
        assumeTrue(testFile.exists());

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = TIMEOUT;
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void afterClass() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        fLogger.removeAllAppenders();
    }

    /**
     * Before Test
     */
    @Before
    public void before() {
        File testFile = fTestFile;
        assertNotNull(testFile);
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, testFile.getAbsolutePath(), TRACE_TYPE);
        fEditorBot = SWTBotUtils.activateEditor(fBot, testFile.getName());
        fAbsolutePath = TmfTraceManager.getTemporaryDirPath() + File.separator + "exportToTsvTest.tsv";
        TmfFileDialogFactory.setOverrideFiles(fAbsolutePath);
    }

    /**
     * After Test
     */
    @After
    public void after() {
        fBot.closeAllEditors();
    }

    /**
     * Test full export
     *
     * @throws IOException
     *             File not found or such
     */
    @Test
    public void testExport() throws IOException {
        SWTBotEditor editorBot = fEditorBot;
        assertNotNull(editorBot);
        final SWTBotTable tableBot = editorBot.bot().table();

        tableBot.getTableItem(1).contextMenu(EXPORT_TO_TSV).click();
        File file = new File(fAbsolutePath);
        fBot.waitUntil(new FileLargerThanZeroCondition(file));
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            long lines = br.lines().count();
            assertEquals("Line count", 23, lines);
        } finally {
            new File(fAbsolutePath).delete();
        }
    }

    /**
     * Test export when a filter is applied to the table
     *
     * @throws IOException
     *             File not found or such
     */
    @Test
    public void testExportWithFilter() throws IOException {
        SWTBotEditor editorBot = fEditorBot;
        assertNotNull(editorBot);
        final SWTBotTable tableBot = editorBot.bot().table();
        tableBot.getTableItem(0).click(3);
        SWTBotText textBot = editorBot.bot().text();
        textBot.typeText("LoggerA|LoggerB|LoggerC");
        textBot.pressShortcut(Keystrokes.CTRL, Keystrokes.CR);
        fBot.waitUntil(Conditions.tableHasRows(tableBot, 6), 5000);

        tableBot.getTableItem(1).contextMenu(EXPORT_TO_TSV).click();
        assertTsvContentsEquals(ImmutableList.of(HEADER_TEXT, EVENT1_TEXT, EVENT2_TEXT, EVENT3_TEXT));
    }

    private void assertTsvContentsEquals(final List<String> expected) throws FileNotFoundException, IOException {
        File file = new File(fAbsolutePath);
        fBot.waitUntil(new FileLargerThanZeroCondition(file));
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            List<String> lines = br.lines().collect(Collectors.toList());
            assertEquals("File content", expected, lines);
        } finally {
            file.delete();
        }
    }
}
