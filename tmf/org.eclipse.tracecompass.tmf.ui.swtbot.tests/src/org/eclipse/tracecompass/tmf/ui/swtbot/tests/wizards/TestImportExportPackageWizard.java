/*******************************************************************************
 * Copyright (c) 2014, 2015 Ericsson
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
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
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
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView(WELCOME_NAME, fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

    }

    /**
     * test opening a trace, importing
     *
     * @throws IOException
     *             won't happen
     */
    @Test
    public void test() throws IOException {
        File f = File.createTempFile("temp", ".xml").getCanonicalFile();
        try (FileWriter fw = new FileWriter(f)) {
            fw.write(TRACE_CONTENT);
        }
        File exportPackage = new File(EXPORT_LOCATION);
        if (exportPackage.exists()) {
            exportPackage.delete();
        }
        assertFalse("File: " + EXPORT_LOCATION + " already present, aborting test", exportPackage.exists());
        assertTrue("Trace :" + f.getAbsolutePath() + " does not exist, aborting test", f.exists());
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, f.getAbsolutePath(), XMLSTUB_ID);
        WaitUtils.waitForJobs();
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(trace);
        assertEquals("Incorrect opened trace!", f.getAbsolutePath(), (new File(trace.getPath())).getAbsolutePath());
        SWTBotView projectExplorerBot = fBot.viewByTitle(PROJECT_EXPLORER);
        assertNotNull("Cannot find " + PROJECT_EXPLORER, projectExplorerBot);
        projectExplorerBot.show();
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);

        treeItem.contextMenu(EXPORT_TRACE_PACKAGE).click();
        fBot.waitUntil(Conditions.shellIsActive(EXPORT_TRACE_PACKAGE_TITLE));
        SWTBot shellBot = fBot.activeShell().bot();
        shellBot.button(DESELECT_ALL).click();
        SWTBotTreeItem[] items = fBot.tree().getAllItems();
        for (SWTBotTreeItem item : items) {
            assertEquals(item.isChecked(), false);
        }
        shellBot.button(SELECT_ALL).click();
        for (SWTBotTreeItem item : items) {
            assertEquals(item.isChecked(), true);
        }
        shellBot.radio(SAVE_IN_TAR_FORMAT).click();
        shellBot.radio(SAVE_IN_ZIP_FORMAT).click();

        shellBot.checkBox(COMPRESS_THE_CONTENTS_OF_THE_FILE).click();
        shellBot.checkBox(COMPRESS_THE_CONTENTS_OF_THE_FILE).click();
        shellBot.comboBox().setText(EXPORT_LOCATION);
        SWTBotShell shell = fBot.activeShell();
        shellBot.button(FINISH).click();
        // finished exporting
        WaitUtils.waitForJobs();
        fBot.waitUntil(Conditions.shellCloses(shell));
        fBot = new SWTWorkbenchBot();
        exportPackage = new File(EXPORT_LOCATION);
        assertTrue("Exported package", exportPackage.exists());
        // Fixme: determine why exportPackageSize is different on different machines
        // assertEquals("Exported package size check", PACKAGE_SIZE, exportPackage.length());

        // import
        treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        treeItem.contextMenu(IMPORT_TRACE_PACKAGE).click();
        fBot.waitUntil(Conditions.shellIsActive(IMPORT_TRACE_PACKAGE_TITLE));
        shellBot = fBot.activeShell().bot();
        shellBot.comboBox().setText(EXPORT_LOCATION);
        shellBot.comboBox().typeText("\n");

        shellBot.button(SELECT_ALL).click();
        shell = fBot.activeShell();
        shellBot.button(FINISH).click();
        fBot.button("Yes To All").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.openEditor(fBot, PROJECT_NAME, new Path(f.getName()));
        trace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(trace);
        assertEquals("Test if import matches", f.getName(), trace.getName());
        assertFalse("Test if import files don't match", f.getAbsolutePath().equals(trace.getPath()));
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
        WaitUtils.waitForJobs();
    }

}
