/**********************************************************************
 * Copyright (c) 2018, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.ui.swtbot.tests.preferences;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
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
import org.eclipse.tracecompass.tmf.ui.dialog.DirectoryDialogFactory;
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
    /** Default project name */
    protected static final String TRACE_PROJECT_NAME = "xml-test";
    /** XML files */
    private static final String EXTENSION = "." + XmlUtils.XML_EXTENSION;
    private static final String TEST_FILES_FOLDER = "test_xml_files/";
    private static final String VALID_FILES_FOLDER = "test_valid/";
    private static final String INVALID_FILES_FOLDER = "test_invalid/";
    private static final String[] FILES_IMPORT_VALID = new String[] { "test_valid",
                                                                      "test_valid_xml_timegraphView" };
    private static final String FILE_IMPORT_INVALID = "test_invalid";
    private static final String[] FILES_DELETE = new String[] { "test_valid_extended",
                                                                "test_valid_pattern" };
    private static final String[] FILES_EDIT = new String[] { "kvm_exits",
                                                              "test_consuming_fsm" };
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

    private static final String TEMP_DIRECTORY = "/tmp";
    private static final String PROJECT_EXPLORER_VIEW_NAME = "Project Explorer";
    private static final String MANAGE_XML_ANALYSES_COMMAND_NAME = "Manage XML analyses...";
    private static final String MANAGE_XML_ANALYSES_PREF_TITLE = "Manage XML analyses files";
    private static final String PREFERENCES_SHELL = "Preferences";
    private static final String MANAGE_XML_ANALYSES_PREF_TITLE_FILTERED = "Manage XML analyses files (Filtered)";

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
    }

    /**
     * Before each test
     */
    @Before
    public void before() {
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Test opening up the preference page through menu bar
     */
    @Test
    public void testPreferencePageMenuBar() {
        SWTBot bot = openXMLAnalysesPreferences().bot();
        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * Test opening up the preference page from context menu of Traces folder
     */
    @Test
    public void testPreferencePageContextMenu() {
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        /* Open preferences page from context menu */
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        fBot.viewByTitle(PROJECT_EXPLORER_VIEW_NAME).setFocus();
        tracesFolder.contextMenu().menu(MANAGE_XML_ANALYSES_COMMAND_NAME).click();

        SWTBotShell shell = SWTBotUtils.anyShellOf(fBot, PREFERENCES_SHELL, MANAGE_XML_ANALYSES_PREF_TITLE_FILTERED).activate();
        SWTBot bot = shell.bot();

        /* Close preference page*/
        SWTBotUtils.pressOKishButtonInPreferences(bot);

        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }

    /**
     * Test the deletion of files
     */
    @Test
    public void testDelete() {
        // Import valid analysis file
        SWTBot bot = openXMLAnalysesPreferences().bot();
        importAnalysis(bot, FILES_DELETE.length, getRelativePaths(VALID_FILES_FOLDER, FILES_DELETE));

        SWTBotTable tablebot = bot.table(0);

        // Open editor for first file
        tablebot.select(FILES_DELETE[0]);
        bot.button("Edit...").click();

        // Delete files
        tablebot.select(FILES_DELETE);
        bot.button("Delete").click();

        // Check that the confirmation pop-up is displayed
        SWTBotShell deleteShell = bot.shell("Delete XML file(s)").activate();
        deleteShell.bot().button("Yes").click();

        // Check that the files do not exist anymore
        for (String deleteFile : FILES_DELETE) {
            assertFalse(deleteFile, tablebot.containsItem(deleteFile));
        }

        // Check that the opened editor was closed
        fBot.editors().forEach(editor -> {
            if (editor != null) {
                if (editor.getTitle().equals(FILES_DELETE[0] + EXTENSION)) {
                    fail("Editor is still open: " + FILES_DELETE[0] + EXTENSION);
                }
            }
        });

        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * Test the import of valid files
     */
    @Test
    public void testImportValid() {
        // Import valid analysis file
        SWTBot bot = openXMLAnalysesPreferences().bot();
        importAnalysis(bot, FILES_IMPORT_VALID.length, getRelativePaths(VALID_FILES_FOLDER, FILES_IMPORT_VALID));

        // Check that the "enabled" label is displayed
        SWTBotTable tablebot = bot.table(0);
        for (String importedItem : FILES_IMPORT_VALID) {
            tablebot.getTableItem(importedItem).select();
            assertTrue(bot.label("File enabled").isVisible());
        }

        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * Test the import of an invalid file
     */
    @Test
    public void testImportInvalid() {
        // Import invalid analysis file
        SWTBot bot = openXMLAnalysesPreferences().bot();
        importAnalysis(bot, 0, TEST_FILES_FOLDER + INVALID_FILES_FOLDER + FILE_IMPORT_INVALID + EXTENSION);

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
        // Import valid analysis files
        SWTBot bot = openXMLAnalysesPreferences().bot();
        importAnalysis(bot, FILES_EDIT.length, getRelativePaths(VALID_FILES_FOLDER, FILES_EDIT));

        // Open the editor
        SWTBotTable tablebot = bot.table(0);
        tablebot.select(FILES_EDIT);
        bot.button("Edit...").click();

        SWTBotUtils.pressOKishButtonInPreferences(bot);

        // Check that the editors were opened
        // No need to actually check that they are active
        for (String editFile : FILES_EDIT) {
            fBot.editorByTitle(editFile + EXTENSION).isActive();
        }
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
        assertNotNull(bot.label("Invalid file"));
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
        int rowsCount = tableBot.rowCount();
        if (rowsCount > 0) {
            String[] itemNames = new String[rowsCount];
            for (int i = 0; i < rowsCount; ++i) {
                itemNames[i] = tableBot.getTableItem(i).getText();
            }
            tableBot.select(itemNames);
            bot.button("Delete").click();
            SWTBotShell deleteShell = bot.shell("Delete XML file(s)").activate();
            deleteShell.bot().button("Yes").click();
        }

        // Import files
        int preRowCount = tableBot.rowCount();
        importAnalysis(bot, FILES_BUTTONS.length, getRelativePaths(VALID_FILES_FOLDER, FILES_BUTTONS));
        int postRowCount = tableBot.rowCount();
        assertEquals(preRowCount + FILES_BUTTONS.length, postRowCount);

        // Uncheck selected
        int preCheckCount = SWTBotUtils.getTableCheckedItemCount(tableBot);
        int uncheckCount = 2;
        String[] toUncheck = Arrays.copyOfRange(FILES_BUTTONS, 0, uncheckCount);
        tableBot.select(toUncheck);
        bot.button(UNCHECK_SELECTED).click();
        int postCheckCount = SWTBotUtils.getTableCheckedItemCount(tableBot);
        assertEquals(UNCHECK_SELECTED, preCheckCount - uncheckCount, postCheckCount);

        // Check selected
        tableBot.select(toUncheck);
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
        final String fileNameXml = FILE_EXPORT + EXTENSION;
        importAnalysis(bot, 1, TEST_FILES_FOLDER + VALID_FILES_FOLDER + fileNameXml);

        // Setup target folder
        File targetDirectory = new File(TEMP_DIRECTORY);
        DirectoryDialogFactory.setOverridePath(targetDirectory.getAbsolutePath());

        // Export
        SWTBotTable tableBot = bot.table(0);
        tableBot.select(FILE_EXPORT);
        bot.button("Export").click();

        // Check that the file was created
        File targetFile = new File(targetDirectory, fileNameXml);
        assertTrue(targetFile.toString(), targetFile.exists());

        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * After each test, delete all analyses
     */
    @After
    public void after() {
        fBot.closeAllEditors();
        SWTBot bot = openXMLAnalysesPreferences().bot();
        SWTBotTable tableBot = bot.table(0);

        // Delete existing analysis files, if any
        int rowsCount = tableBot.rowCount();
        if (rowsCount > 0) {
            String[] itemNames = new String[rowsCount];
            for (int i = 0; i < rowsCount; ++i) {
                itemNames[i] = tableBot.getTableItem(i).getText();
            }
            tableBot.select(itemNames);
            bot.button("Delete").click();
            SWTBotShell deleteShell = bot.shell("Delete XML file(s)").activate();
            deleteShell.bot().button("Yes").click();
        }
        SWTBotUtils.pressOKishButtonInPreferences(bot);
    }

    /**
     * After Class for cleanup
     */
    @AfterClass
    public static void afterClass() {
        fLogger.removeAllAppenders();
    }

    private static String[] getRelativePaths(String folder, String[] files) {
        String[] relativePaths = new String[files.length];
        for (int i = 0; i < files.length; ++i) {
            relativePaths[i] = TEST_FILES_FOLDER + folder + files[i] + EXTENSION;
        }
        return relativePaths;
    }

    private static void importAnalysis(SWTBot bot, int expected, String... relativePaths) {
        String[] absolutePaths = new String[relativePaths.length];
        for (int i = 0; i < relativePaths.length; ++i) {
            absolutePaths[i] = Activator.getAbsolutePath(new Path(relativePaths[i])).toString();
        }
        TmfFileDialogFactory.setOverrideFiles(absolutePaths);
        bot.button("Import").click();
        SWTBotUtils.waitUntil(table -> table.rowCount() == expected, bot.table(0), "Failed to import analysis");
    }

    private static SWTBotShell openXMLAnalysesPreferences() {
        SWTBotShell preferencesShell = SWTBotUtils.openPreferences(fBot, MANAGE_XML_ANALYSES_PREF_TITLE);
        SWTBot bot = preferencesShell.bot();
        SWTBotTree tree = bot.tree(0);
        SWTBotTreeItem treeNode = tree.getTreeItem("Tracing");
        treeNode.select();
        treeNode.expand();
        bot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable("XML Analyses", treeNode));
        treeNode = treeNode.getNode("XML Analyses");
        treeNode.select();
        return preferencesShell;
    }
}
