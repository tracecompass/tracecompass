/*******************************************************************************
 * Copyright (c) 2014, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.common.core.format.DataSizeWithUnitFormat;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Export and Import wizard tests
 *
 * @author Matthew Khouzam
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TestImportExportPackageWizard {

    // private static final int PACKAGE_SIZE = 213732;
    private static final String EXPORT_LOCATION = TmfTraceManager.getTemporaryDirPath() + File.separator + "test.zip";
    private static final String TRACE_LOCATION = TmfTraceManager.getTemporaryDirPath() + File.separator + "test.xml";
    private static final String IMPORT_TRACE_PACKAGE = "Import Trace Package...";
    private static final String IMPORT_TRACE_PACKAGE_TITLE = "Import trace package";
    private static final String EXPORT_TRACE_PACKAGE = "Export Trace Package...";
    private static final String EXPORT_TRACE_PACKAGE_TITLE = "Export trace package";
    private static final String PROJECT_EXPLORER = "Project Explorer";
    private static final String FINISH = "Finish";
    private static final String COMPRESS_THE_CONTENTS_OF_THE_FILE = "Compress the contents of the file";
    private static final String SAVE_IN_ZIP_FORMAT = "Save in zip format";
    private static final String SAVE_IN_TAR_FORMAT = "Save in tar format";
    private static final String SELECT_ALL = "Select All";
    private static final String DESELECT_ALL = "Deselect All";
    private static final String WELCOME_NAME = "welcome";
    private static final String SWT_BOT_THREAD_NAME = "SWTBot Thread";
    private static final String PROJECT_NAME = "Test";
    private static final String XMLSTUB_ID = "org.eclipse.linuxtools.tmf.core.tests.xmlstub";

    private static final Pattern PATTERN = Pattern.compile("Approximate uncompressed size: (.*)B");

    private static final String TRACE_CONTENT = "<trace>" +
            "<event timestamp=\"100\" name=\"event\"><field name=\"field\" value=\"1\" type=\"int\" /></event>" +
            "<event timestamp=\"200\" name=\"event1\"><field name=\"field\" value=\"2\" type=\"int\" /></event>" +
            "<event timestamp=\"201\" name=\"event\"><field name=\"field\" value=\"3\" type=\"int\" /></event>" +
            "<event timestamp=\"202\" name=\"event1\"><field name=\"field\" value=\"3\" type=\"int\" /></event>" +
            "<event timestamp=\"203\" name=\"event1\"><field name=\"field\" value=\"3\" type=\"int\" /></event>" +
            "<event timestamp=\"300\" name=\"event1\"><field name=\"field\" value=\"2\" type=\"int\" /></event>" +
            "<event timestamp=\"301\" name=\"event\"><field name=\"field\" value=\"3\" type=\"int\" /></event>" +
            "<event timestamp=\"302\" name=\"event1\"><field name=\"field\" value=\"3\" type=\"int\" /></event>" +
            "<event timestamp=\"333\" name=\"event1\"><field name=\"field\" value=\"3\" type=\"int\" /></event>" +
            "<event timestamp=\"500\" name=\"event1\"><field name=\"field\" value=\"2\" type=\"int\" /></event>" +
            "<event timestamp=\"501\" name=\"event\"><field name=\"field\" value=\"3\" type=\"int\" /></event>" +
            "<event timestamp=\"502\" name=\"event1\"><field name=\"field\" value=\"3\" type=\"int\" /></event>" +
            "<event timestamp=\"533\" name=\"event1\"><field name=\"field\" value=\"3\" type=\"int\" /></event>" +
            "</trace>";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static SWTWorkbenchBot fBot;

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        SWTBotUtils.initialize();
        Thread.currentThread().setName(SWT_BOT_THREAD_NAME); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView(WELCOME_NAME, fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

    }

    /**
     * Clean up
     */
    @AfterClass
    public static void afterClass() {
        fLogger.removeAllAppenders();
    }

    /**
     * test opening a trace, importing
     *
     * @throws IOException
     *             won't happen
     * @throws ParseException
     *             won't happen
     */
    @Test
    public void test() throws IOException, ParseException {
        File traceFile = new File(TRACE_LOCATION);
        if (traceFile.exists()) {
            traceFile.delete();
        }
        traceFile.deleteOnExit();
        try (FileWriter fw = new FileWriter(traceFile)) {
            fw.write(TRACE_CONTENT);
        }
        File exportPackage = new File(EXPORT_LOCATION);
        if (exportPackage.exists()) {
            exportPackage.delete();
        }
        assertFalse("File: " + EXPORT_LOCATION + " already present, aborting test", exportPackage.exists());
        assertTrue("Trace :" + traceFile.getAbsolutePath() + " does not exist, aborting test", traceFile.exists());
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, TRACE_LOCATION, XMLSTUB_ID);
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(trace);
        assertEquals("Incorrect opened trace!", traceFile.getAbsolutePath(), (new File(trace.getPath())).getAbsolutePath());
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        assertNotNull("Cannot find " + PROJECT_EXPLORER, projectExplorerBot);
        projectExplorerBot.show();
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);

        treeItem.contextMenu(EXPORT_TRACE_PACKAGE).click();
        SWTBotShell shell = fBot.shell(EXPORT_TRACE_PACKAGE_TITLE).activate();
        SWTBot shellBot = shell.bot();
        shellBot.button(DESELECT_ALL).click();
        SWTBotTreeItem[] items = fBot.tree().getAllItems();
        for (SWTBotTreeItem item : items) {
            assertEquals(item.isChecked(), false);
        }
        String labelText = fBot.label(1).getText();
        Matcher matcher = PATTERN.matcher(labelText);

        assertTrue(labelText + " matches", matcher.matches());
        String sizeText = matcher.group(1);
        assertEquals(labelText + " value", "0", sizeText.trim());

        shellBot.button(SELECT_ALL).click();

        for (SWTBotTreeItem item : items) {
            assertEquals(item.isChecked(), true);
        }

        labelText = fBot.label(1).getText();
        matcher = PATTERN.matcher(labelText);

        assertTrue(labelText + " matches", matcher.matches());
        // should be 138 k
        sizeText = matcher.group(1);
        int size = ((Number) DataSizeWithUnitFormat.getInstance().parseObject(sizeText)).intValue();
        assertTrue(labelText + " value", 0 < size);

        shellBot.radio(SAVE_IN_TAR_FORMAT).click();
        shellBot.radio(SAVE_IN_ZIP_FORMAT).click();

        shellBot.checkBox(COMPRESS_THE_CONTENTS_OF_THE_FILE).click();
        shellBot.checkBox(COMPRESS_THE_CONTENTS_OF_THE_FILE).click();
        shellBot.comboBox().setText(EXPORT_LOCATION);
        shellBot.button(FINISH).click();
        // finished exporting
        WaitUtils.waitForJobs();
        fBot.waitUntil(Conditions.shellCloses(shell));
        fBot = new SWTWorkbenchBot();
        exportPackage = new File(EXPORT_LOCATION);
        assertTrue("Exported package", exportPackage.exists());
        /*
         * Fixme: determine why exportPackageSize is different on different
         * machines
         */
        /*
         * assertEquals("Exported package size check", PACKAGE_SIZE,
         * exportPackage.length());
         */

        // import
        treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        treeItem.contextMenu(IMPORT_TRACE_PACKAGE).click();
        shell = fBot.shell(IMPORT_TRACE_PACKAGE_TITLE).activate();
        shellBot = shell.bot();
        shellBot.comboBox().setText(EXPORT_LOCATION);
        shellBot.comboBox().typeText("\n");

        shellBot.button(SELECT_ALL).click();
        shellBot.button(FINISH).click();
        fBot.button("Yes To All").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.openEditor(fBot, PROJECT_NAME, new Path(traceFile.getName()));
        trace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(trace);
        assertEquals("Test if import matches", traceFile.getName(), trace.getName());
        assertFalse("Test if import files don't match", traceFile.getAbsolutePath().equals(trace.getPath()));
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
        WaitUtils.waitForJobs();
        traceFile.delete();
        exportPackage.delete();

    }

}
