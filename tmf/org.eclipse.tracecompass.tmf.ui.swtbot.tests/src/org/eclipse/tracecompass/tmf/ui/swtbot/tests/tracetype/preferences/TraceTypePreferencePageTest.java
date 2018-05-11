/*******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.tracetype.preferences;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.project.model.TmfTraceType;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.dialog.TmfFileDialogFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.filter.FilterView;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for the trace type preference page
 *
 * @author Jean-Christian Kouame
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TraceTypePreferencePageTest {

    private static final @NonNull String FILTER =
            "<ROOT>"
                + "<FILTER name=\"test\">"
                    + "<TRACETYPE name=\"Custom XML : testxmlextension\" type=\"custom.xml.trace:Custom XML:testxmlextension\">"
                        + "<MATCHES eventaspect=\"Message\" not=\"false\" regex=\"msg\" tracetypeid=\"custom.xml.trace:Custom XML:testxmlextension\"/>"
                    + "</TRACETYPE>"
                + "</FILTER>"
            + "</ROOT>";

    private static final @NonNull String CHECK_SELECTED = "Check selected";
    private static final @NonNull String CHECK_ALL = "Check all";
    private static final @NonNull String UNCHECK_SELECTED = "Uncheck selected";
    private static final @NonNull String UNCHECK_ALL = "Uncheck all";
    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static final String TRACE_PATH = "testfiles/syslog_collapse";

    /** LTTng kernel trace type */
    protected static final String KERNEL_TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    /** LTTng kernel perspective */
    protected static final String KERNEL_PERSPECTIVE_ID = "org.eclipse.linuxtools.lttng2.kernel.ui.perspective";
    /** Default project name */
    protected static final String TRACE_PROJECT_NAME = "test";

    private static SWTWorkbenchBot fBot;

    /**
     * Before Class
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
     * Before method to reset the preference values
     */
    @Before
    public void before() {
        setTraceTypePreferences(CHECK_ALL);
    }

    /**
     * After Class
     */
    @AfterClass
    public static void afterClass() {
        setTraceTypePreferences(CHECK_ALL);
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
        fBot.closeAllEditors();
        fLogger.removeAllAppenders();
    }

    /**
     * Test the filter
     */
    @Test
    public void testPreferencePage() {
        SWTBot bot = openTraceTypePreferences().bot();
        SWTBotTree treeBot = bot.tree(1);
        //get default count
        bot.button(CHECK_ALL).click();
        int defaultCount = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        int defaultVisibleRows = treeBot.visibleRowCount();
        // test "uncheck all button"
        bot.button(UNCHECK_ALL).click();
        int checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(0, checked);
        // test check all
        bot.button(CHECK_ALL).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(CHECK_ALL, defaultCount, checked);
        // test check selected
        treeBot.getTreeItem("Custom XML").select();
        bot.button(UNCHECK_ALL).click();
        bot.button(CHECK_SELECTED).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(CHECK_SELECTED, 2, checked);
        // test uncheck selected
        bot.button(CHECK_ALL).click();
        bot.button(UNCHECK_SELECTED).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(UNCHECK_SELECTED, defaultCount - 2, checked);
        // test filter
        bot.button(UNCHECK_ALL).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals(0, checked);
        bot.text(1).setText("Custom XML");
        SWTBotUtils.waitUntil(tree -> tree.visibleRowCount() == 2, treeBot, "Visible row count: Default expected 2, but actual value is " + treeBot.visibleRowCount());
        assertEquals("Filtered no checked", 0, checked);
        bot.button(CHECK_ALL).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals("Filtered check all", 2, checked);
        bot.text(1).setText("");
        SWTBotUtils.waitUntil(tree -> tree.visibleRowCount() == defaultVisibleRows + 1, treeBot, "Visible row count: Default expected " + defaultCount +", but actual value is " + treeBot.visibleRowCount());
        bot.button(CHECK_ALL).click();
        checked = SWTBotUtils.getTreeCheckedItemCount(treeBot);
        assertEquals("Filtered removed all check", defaultCount, checked);
        bot.button("Apply").click();
        SWTBotUtils.pressOKishButtonInPreferences(fBot);
    }

    /**
     * Test the select trace type menu
     */
    @Test
    public void testSelectTraceType() {
        List<String> menuItems = getSelectTraceTypeMenuItems();
        List<String> categories = Arrays.asList(
                "Common Trace Format",
                "Custom Text",
                "Custom XML",
                "Test trace",
                "",
                "Manage Custom Parsers...");
        assertEquals("Test all categories enabled", categories, menuItems);

        //Change the preference values and test the 'Select Trace Type...' options
        setTraceTypePreferences(UNCHECK_ALL, "Custom Text");
        menuItems = getSelectTraceTypeMenuItems();
        categories = Arrays.asList(
                "Custom Text",
                "Test trace",
                "",
                "Manage Custom Parsers...");
        assertEquals("Test 1 category enabled", categories, menuItems);

        setTraceTypePreferences(CHECK_ALL);
    }

    /**
     * Test the import trace's trace type combo
     */
    @Test
    public void testImportTraceTypeOptions() {
        int defaultCount = TmfTraceType.getAvailableTraceTypes().length;

        setTraceTypePreferences(UNCHECK_ALL, "Custom Text", "testtxtextension");
        String[] traceTypeComboItems = getTraceTypeComboItems();
        String[] availableTraceTypes = new String[] {
                "<Automatic Detection>",
                "Custom Text : testtxtextension"
        };
        assertArrayEquals("Test one trace type enabled", availableTraceTypes, traceTypeComboItems);

        setTraceTypePreferences(CHECK_ALL);
        traceTypeComboItems = getTraceTypeComboItems();
        assertEquals("Test all trace type enabled", defaultCount + 1, traceTypeComboItems.length);
    }

    /**
     * Test enabled/disabled trace types behavior in the filters view
     */
    @Test
    public void testFiltersView() {
        int defaultCount = TmfTraceType.getAvailableTraceTypes().length;
        SWTBotUtils.openView(FilterView.ID);
        SWTBotView viewBot = fBot.viewById(FilterView.ID);
        importFilter(viewBot);
        SWTBot bot = viewBot.bot();

        SWTBotTree tree = bot.tree(0);
        SWTBotTreeItem item = tree.expandNode("FILTER test", "WITH TRACETYPE Custom XML : testxmlextension");
        item.select();

        SWTBotCombo comboBox = bot.comboBox(0);
        assertEquals("Combo: number of trace types", defaultCount, comboBox.itemCount());

        // Change the preference value for testxmlextension
        setTraceTypePreferences(UNCHECK_ALL, "Custom XML", "testxmlextension");

        // Change node to refresh the tree
        item = tree.expandNode("FILTER test");
        item.select();
        item = tree.expandNode("FILTER test", "WITH TRACETYPE Custom XML : testxmlextension");
        item.select();

        comboBox = bot.comboBox(0);
        assertEquals("Combo: number of trace types", 1, comboBox.itemCount());

        setTraceTypePreferences(UNCHECK_ALL, "Custom Text", "testtxtextension");

        // Change node to refresh the tree
        item = tree.expandNode("FILTER test");
        item.select();
        item = tree.expandNode("FILTER test", "WITH TRACETYPE Custom XML : testxmlextension");
        item.select();

        comboBox = bot.comboBox(0);
        assertEquals("Combo: number of trace types", 2, comboBox.itemCount());
    }

    private static void importFilter(SWTBotView viewBot) {
        try {
            File tempFile = File.createTempFile("test", ".xml");
            FileWriter fw = new FileWriter(tempFile);
            try (BufferedWriter bw = new BufferedWriter(fw)) {
                bw.write(FILTER);
            }
            TmfFileDialogFactory.setOverrideFiles(tempFile.getAbsolutePath());
        } catch (IOException e) {
            fail("Failed to add a filter");
        }
        viewBot.toolbarButton("Import filters").click();
        SWTBotUtils.waitUntil(tree -> tree.rowCount() > 0, viewBot.bot().tree(0), "Failed to import test filter");
    }

    private static void setTraceTypePreferences(@NonNull String button, @NonNull String... pathToCheck) {
        SWTBot bot = openTraceTypePreferences().bot();
        SWTBotTree treeBot = bot.tree(1);
        if (!button.isEmpty()) {
            bot.button(button).click();
        }

        if (pathToCheck.length > 0) {
            SWTBotTreeItem treeItem = treeBot.expandNode(pathToCheck);
            assertNotNull("Tree item not null", treeItem);
            treeItem.select();
            bot.button(CHECK_SELECTED).click();
        }
        bot.button("Apply").click();
        SWTBotUtils.pressOKishButtonInPreferences(fBot);
    }

    private static List<String> getSelectTraceTypeMenuItems() {
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        tracesFolder.expand();
        SWTBotTreeItem trace = tracesFolder.getNode("syslog_collapse");
        trace.select();
        List<String> menuItems = trace.contextMenu().menu("Select Trace Type...").menuItems();
        return menuItems;
    }

    private static SWTBotShell openTraceTypePreferences() {
        SWTBotShell preferencesShell = SWTBotUtils.openPreferences(fBot);
        SWTBot bot = preferencesShell.bot();
        SWTBotTree tree = bot.tree(0);
        SWTBotTreeItem treeNode = tree.getTreeItem("Tracing");
        treeNode.select();
        treeNode.expand();
        bot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable("Trace Types", treeNode));
        treeNode = treeNode.getNode("Trace Types");
        treeNode.select();
        return preferencesShell;
    }

    private static String[] getTraceTypeComboItems() {
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(fBot, TRACE_PROJECT_NAME);
        SWTBotMenu menu = tracesFolder.contextMenu().menu("Import...");
        menu.click();
        SWTBot bot = fBot.shell("Trace Import").activate().bot();
        SWTBotCombo combo = bot.comboBox(2);
        String[] items = combo.items();
        bot.button("Cancel").click();
        return items;
    }
}
