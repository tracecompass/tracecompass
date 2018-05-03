/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.ui.swtbot.tests.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
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
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.Activator;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for the XML analyses preference page
 *
 * @author Christophe Bourque Bedard
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class XMLAnalysesManagerPreferencePageTest {

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    /** LTTng kernel trace type */
    protected static final String KERNEL_TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    /** Default project name */
    protected static final String TRACE_PROJECT_NAME = "test";
    private static final String TRACE_PATH = "testfiles/syslog_collapse";
    /** XML files */
    private static final String EXTENSION = "." + XmlUtils.XML_EXTENSION;
    private static final String TEST_FILES_FOLDER = "test_xml_files/";
    private static final String VALID_FILES_FOLDER = "test_valid/";
    private static final String INVALID_FILES_FOLDER = "test_invalid/";
    private static final String FILE_DELETE = "test_valid_extended";
    private static final String FILE_IMPORT_VALID = "test_valid";
    private static final String FILE_IMPORT_INVALID = "test_invalid";
    private static final String FILE_EDIT = "kvm_exits";
    private static final String FILE_EXPORT = "state_provider_placement";
    private static final String[] FILES_BUTTONS = new String[] { "test_state_values",
                                                                 "test_state_values_pattern",
                                                                 "test_pattern_segment",
                                                                 "test_doubles",
                                                                 "test_attributes" };
    /** Button labels */
    private static final @NonNull String CHECK_SELECTED = "Check selected";
    private static final @NonNull String CHECK_ALL = "Check all";
    private static final @NonNull String UNCHECK_SELECTED = "Uncheck selected";
    private static final @NonNull String UNCHECK_ALL = "Uncheck all";
    /** Workbench bot */
    private static SWTWorkbenchBot fBot;

    /**
     * Before Class for launch and setup
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", fBot);
        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();
    }

    /**
     * Before each test
     */
    @Before
    public void before() {
        /* Create the trace project */
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
        /* set up test trace */
        setUpTrace();
    }

    private static void setUpTrace() {
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(TRACE_PATH), null);
        URI uri;
        try {
            uri = FileLocator.toFileURL(location).toURI();
            File testFile = new File(uri);
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, testFile.getAbsolutePath(), KERNEL_TRACE_TYPE);
            assertNotNull(testFile);
            assumeTrue(testFile.exists());
        } catch (URISyntaxException | IOException e) {
            fail("Failed to open the trace");
        }
    }

    /**
     * Test opening up the preference page
     */
    @Test
    public void testPreferencePage() {
        SWTBot bot = openXMLAnalysesPreferences().bot();
        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * Test the deletion of a file
     */
    @Test
    public void testDelete() {
        // Import valid analysis file
        SWTBot bot = openXMLAnalysesPreferences().bot();
        importAnalysis(bot, TEST_FILES_FOLDER + VALID_FILES_FOLDER + FILE_DELETE + EXTENSION);

        // Delete file
        SWTBotTable tablebot = bot.table(0);
        tablebot.getTableItem(FILE_DELETE).select();
        bot.button("Delete").click();

        // Check that the confirmation pop-up is displayed
        SWTBotShell deleteShell = bot.shell("Delete XML file").activate();
        deleteShell.bot().button("Yes").click();

        // Check that the file does not exist anymore
        assertFalse(tablebot.containsItem(FILE_DELETE));

        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * Test the import of a valid file
     */
    @Test
    public void testImportValid() {
        // Import valid analysis file
        SWTBot bot = openXMLAnalysesPreferences().bot();
        importAnalysis(bot, TEST_FILES_FOLDER + VALID_FILES_FOLDER + FILE_IMPORT_VALID + EXTENSION);

        // Check that the "enabled" label is displayed
        SWTBotTable tablebot = bot.table(0);
        tablebot.getTableItem(FILE_IMPORT_VALID).select();
        assertTrue(bot.label("File enabled").isVisible());

        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * Test the import of an invalid file
     */
    @Test
    public void testImportInvalid() {
        // Import invalid analysis file
        SWTBot bot = openXMLAnalysesPreferences().bot();
        importAnalysis(bot, TEST_FILES_FOLDER + INVALID_FILES_FOLDER + FILE_IMPORT_INVALID + EXTENSION);

        // Check that the parsing error pop-up is displayed
        SWTBotShell popupShell = bot.shell("Import XML analysis file failed.").activate();
        popupShell.bot().button("OK").click();

        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * Test the edit function
     */
    @Test
    public void testEdit() {
        // Import valid analysis file
        SWTBot bot = openXMLAnalysesPreferences().bot();
        importAnalysis(bot, TEST_FILES_FOLDER + VALID_FILES_FOLDER + FILE_EDIT + EXTENSION);

        // Open the editor
        SWTBotTable tablebot = bot.table(0);
        tablebot.getTableItem(FILE_EDIT).select();
        bot.button("Edit...").click();

        SWTBotUtils.pressOKishButtonInPreferences(bot);

        // Check that the editor was opened
        // No need to actually check that it is active
        fBot.editorByTitle(FILE_EDIT + EXTENSION).isActive();
    }

    /**
     * Test the invalid file label
     */
    @Test
    public void testInvalidLabel() {
        // Import invalid analysis file manually (otherwise it is rejected)
        XmlUtils.addXmlFile(Activator.getAbsolutePath(new Path(TEST_FILES_FOLDER
                                                               + INVALID_FILES_FOLDER
                                                               + FILE_IMPORT_INVALID
                                                               + EXTENSION)).toFile());
        XmlAnalysisModuleSource.notifyModuleChange();

        // Check that the "invalid" label displayed
        // and that the checkbox was unchecked as a result
        SWTBot bot = openXMLAnalysesPreferences().bot();
        SWTBotTable tablebot = bot.table(0);
        SWTBotTableItem tableItem = tablebot.getTableItem(FILE_IMPORT_INVALID);
        tableItem.select();
        assertTrue(bot.label("Invalid file").isVisible());
        assertFalse(tableItem.isChecked());

        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * Test the check/uncheck buttons
     */
    @Test
    public void testCheckButtons() {
        SWTBot bot = openXMLAnalysesPreferences().bot();

        SWTBotTable tableBot = bot.table(0);

        // Delete existing analysis files, if any
        int rowCount = tableBot.rowCount();
        for (int i = 0; i < rowCount; ++i) {
            tableBot.getTableItem(0).select();
            bot.button("Delete").click();
            SWTBotShell deleteShell = bot.shell("Delete XML file").activate();
            deleteShell.bot().button("Yes").click();
        }

        // Import files
        int preRowCount = tableBot.rowCount();
        for (String file : FILES_BUTTONS) {
            importAnalysis(bot, TEST_FILES_FOLDER + VALID_FILES_FOLDER + file + EXTENSION);
        }
        int postRowCount = tableBot.rowCount();
        assertEquals(preRowCount + FILES_BUTTONS.length, postRowCount);

        // Uncheck selected
        int preCheckCount = SWTBotUtils.getTableCheckedItemCount(tableBot);
        int uncheckIndex = 2;
        tableBot.getTableItem(FILES_BUTTONS[uncheckIndex]).select();
        bot.button(UNCHECK_SELECTED).click();
        int postCheckCount = SWTBotUtils.getTableCheckedItemCount(tableBot);
        assertEquals(UNCHECK_SELECTED, preCheckCount - 1, postCheckCount);

        // Check selected
        tableBot.getTableItem(FILES_BUTTONS[uncheckIndex]).select();
        bot.button(CHECK_SELECTED).click();
        postCheckCount = SWTBotUtils.getTableCheckedItemCount(tableBot);
        assertEquals(CHECK_SELECTED, preCheckCount, postCheckCount);

        // Uncheck all
        bot.button(UNCHECK_ALL).click();
        postCheckCount = SWTBotUtils.getTableCheckedItemCount(tableBot);
        assertEquals(UNCHECK_ALL, 0, postCheckCount);

        // Check all
        bot.button(CHECK_ALL).click();
        postCheckCount = SWTBotUtils.getTableCheckedItemCount(tableBot);
        assertEquals(CHECK_ALL, postRowCount, postCheckCount);

        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * Test the export function
     */
    @Test
    public void testExport() {
        // Import valid analysis file
        SWTBot bot = openXMLAnalysesPreferences().bot();
        importAnalysis(bot, TEST_FILES_FOLDER + VALID_FILES_FOLDER + FILE_EXPORT + EXTENSION);

        // Setup target file
        try {
            File targetFile = File.createTempFile(FILE_EXPORT, EXTENSION);
            TmfFileDialogFactory.setOverrideFiles(targetFile.getAbsolutePath());
        } catch (IOException e) {
            fail("Failed to export XML file");
        }

        // Export
        SWTBotTable tableBot = bot.table(0);
        tableBot.getTableItem(FILE_EXPORT).select();
        bot.button("Export").click();

        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * After each test, delete project
     */
    @After
    public void after() {
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        fBot.closeAllEditors();
    }

    /**
     * After Class for cleanup
     */
    @AfterClass
    public static void afterClass() {
        fLogger.removeAllAppenders();
    }

    private static void importAnalysis(SWTBot bot, String relativePath) {
        TmfFileDialogFactory.setOverrideFiles(Activator.getAbsolutePath(new Path(relativePath)).toString());
        bot.button("Import").click();
        SWTBotUtils.waitUntil(tree -> tree.rowCount() > 0, bot.tree(0), "Failed to import analysis");
    }

    private static SWTBotShell openXMLAnalysesPreferences() {
        SWTBotShell preferencesShell = SWTBotUtils.openPreferences(fBot, "Manage XML analyses files");
        SWTBot bot = preferencesShell.bot();
        SWTBotTree tree = bot.tree(0);
        SWTBotTreeItem treeNode = tree.getTreeItem("Tracing");
        treeNode.select();
        treeNode.expand();
        bot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable("XML Analyses", treeNode));
        treeNode = treeNode.getNode("XML Analyses");
        treeNode.select();
        return preferencesShell;
    }

}
