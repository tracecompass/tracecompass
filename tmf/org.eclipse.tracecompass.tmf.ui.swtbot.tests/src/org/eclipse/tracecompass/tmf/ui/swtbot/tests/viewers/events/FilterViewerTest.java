/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimePreferences;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.filter.FilterView;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for filter views in trace compass
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class FilterViewerTest {


    private static final String COMPARE = "COMPARE";
    private static final String CONTAINS = "CONTAINS";
    private static final String XMLSTUB_ID = "org.eclipse.linuxtools.tmf.core.tests.xmlstub";
    private static final String TRACETYPE = "Test trace : XML Trace Stub (ns)";
    private static final String AND = "AND";
    private static final String WITH_TRACETYPE = "WITH TRACETYPE " + TRACETYPE;
    private static final String FILTER_TEST = "FILTER ";
    private static final String TIMESTAMP = "Timestamp";
    private static final String CONTENTS = "Contents";

    private static final String TRACE_START = "<trace>";
    private static final String EVENT_BEGIN = "<event timestamp=\"";
    private static final String EVENT_MIDDLE = " \" name=\"event\"><field name=\"field\" value=\"";
    private static final String EVENT_END = "\" type=\"int\" />" + "</event>";
    private static final String TRACE_END = "</trace>";

    private static final String PROJECT_NAME = "TestForFiltering";

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();
    private static final String OR = "OR";
    private static SWTWorkbenchBot fBot;

    private static String makeEvent(int ts, int val) {
        return EVENT_BEGIN + Integer.toString(ts) + EVENT_MIDDLE + Integer.toString(val) + EVENT_END + "\n";
    }

    private static File fFileLocation;

    /**
     * Initialization, creates a temp trace
     *
     * @throws IOException
     *             should not happen
     */
    @BeforeClass
    public static void init() throws IOException {
        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.TIME_ZONE, "GMT-05:00");
        TmfTimestampFormat.updateDefaultFormats();

        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
        fFileLocation = File.createTempFile("sample", ".xml");
        try (BufferedRandomAccessFile braf = new BufferedRandomAccessFile(fFileLocation, "rw")) {
            braf.writeBytes(TRACE_START);
            for (int i = 0; i < 100; i++) {
                braf.writeBytes(makeEvent(i * 100, i % 4));
            }
            braf.writeBytes(TRACE_END);
        }
    }

    /**
     * Open a trace in an editor
     */
    @Before
    public void beforeTest() {
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(bot, PROJECT_NAME);
        assertNotNull(treeItem);
        SWTBotUtils.openTrace(PROJECT_NAME, fFileLocation.getAbsolutePath(), XMLSTUB_ID);
        SWTBotUtils.openView(FilterView.ID);
    }

    /**
     * Delete the file
     */
    @AfterClass
    public static void cleanUp() {
        fFileLocation.delete();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
        fLogger.removeAllAppenders();
        SWTBotUtils.closeViewById(FilterView.ID, fBot);

        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.TIME_ZONE, TmfTimePreferences.getDefaultPreferenceMap().get(ITmfTimePreferencesConstants.TIME_ZONE));
        TmfTimestampFormat.updateDefaultFormats();
    }

    /**
     * Close the editor
     */
    @After
    public void tearDown() {
        fBot.closeAllEditors();
    }

    /**
     * Return all timestamps ending with 100... for reasons
     */
    @Test
    public void testTimestampFilter() {
        SWTBotView viewBot = fBot.viewById(FilterView.ID);
        viewBot.setFocus();
        SWTBot filterBot = viewBot.bot();
        SWTBotTree treeBot = filterBot.tree();

        viewBot.toolbarButton("Add new filter").click();
        treeBot.getTreeItem("FILTER <name>").select();
        SWTBotText textBot = filterBot.text();
        textBot.setFocus();
        String filterName = "timestamp";
        textBot.setText(filterName);
        SWTBotTreeItem filterNodeBot = treeBot.getTreeItem(FILTER_TEST + filterName);
        filterNodeBot.click();
        filterNodeBot.contextMenu("TRACETYPE").click();
        filterNodeBot.expand();
        SWTBotCCombo comboBot = filterBot.ccomboBox();
        comboBot.setSelection(TRACETYPE);
        filterNodeBot.getNode(WITH_TRACETYPE).expand();

        // --------------------------------------------------------------------
        // add AND
        // --------------------------------------------------------------------

        filterNodeBot.getNode(WITH_TRACETYPE).contextMenu(AND).click();

        // --------------------------------------------------------------------
        // add CONTAINS "100"
        // --------------------------------------------------------------------

        filterNodeBot.getNode(WITH_TRACETYPE).getNode(AND).contextMenu(CONTAINS).click();
        filterNodeBot.getNode(WITH_TRACETYPE).getNode(AND).expand();
        comboBot = filterBot.ccomboBox(1); // aspect
        comboBot.setSelection(TIMESTAMP);
        textBot = filterBot.text();
        textBot.setFocus();
        textBot.setText("100");
        filterNodeBot.getNode(WITH_TRACETYPE).getNode(AND).getNode("Timestamp CONTAINS \"100\"").select();
        filterNodeBot.getNode(WITH_TRACETYPE).getNode(AND).select();

        viewBot.toolbarButton("Save filters").click();

        String ret = applyFilter(fBot, filterName);
        assertEquals("10/100", ret);
    }

    /**
     * Return all timestamps ending with 100... for reasons
     */
    @Test
    public void testTimestampEqualsOr() {
        SWTBotView viewBot = fBot.viewById(FilterView.ID);
        viewBot.setFocus();
        SWTBot filterBot = viewBot.bot();
        SWTBotTree treeBot = filterBot.tree();

        viewBot.toolbarButton("Add new filter").click();
        treeBot.getTreeItem("FILTER <name>").select();
        SWTBotText textBot = filterBot.text();
        textBot.setFocus();
        String filterName = "matchAndEquals";
        textBot.setText(filterName);
        SWTBotTreeItem filterNodeBot = treeBot.getTreeItem(FILTER_TEST + filterName);
        filterNodeBot.click();
        filterNodeBot.contextMenu("TRACETYPE").click();
        filterNodeBot.expand();
        SWTBotCCombo comboBot = filterBot.ccomboBox();
        comboBot.setSelection(TRACETYPE);
        filterNodeBot.getNode(WITH_TRACETYPE).expand();

        // --------------------------------------------------------------------
        // add OR
        // --------------------------------------------------------------------

        filterNodeBot.getNode(WITH_TRACETYPE).contextMenu(OR).click();

        // --------------------------------------------------------------------
        // add EQUALS "19...300"
        // --------------------------------------------------------------------

        SWTBotTreeItem orNode = filterNodeBot.getNode(WITH_TRACETYPE).getNode(OR);
        orNode.contextMenu("EQUALS").click();
        orNode.expand();
        orNode.getNode(0).select();
        comboBot = filterBot.ccomboBox(1); // aspect
        comboBot.setSelection(TIMESTAMP);
        textBot = filterBot.text();
        textBot.setFocus();
        textBot.setText("19:00:00.000 000 300");

        // --------------------------------------------------------------------
        // add MATCHES "1"
        // --------------------------------------------------------------------
        orNode.contextMenu("MATCHES").click();
        orNode.expand();
        orNode.getNode(1).select();
        comboBot = filterBot.ccomboBox(1); // aspect
        comboBot.setSelection(CONTENTS);
        textBot = filterBot.text(0); // field
        textBot.setFocus();
        textBot.setText("field");
        textBot = filterBot.text(1); // value
        textBot.setFocus();
        textBot.setText("1");

        viewBot.toolbarButton("Save filters").click();

        String ret = applyFilter(fBot, filterName);
        assertEquals("26/100", ret);
//        filterNodeBot.contextMenu().menu("Delete").click();
    }

    /**
     * test compare field >= 2
     */
    @Test
    public void testField01() {
        SWTBotView viewBot = fBot.viewById(FilterView.ID);
        viewBot.setFocus();
        SWTBot filterBot = viewBot.bot();
        SWTBotTree treeBot = filterBot.tree();

        viewBot.toolbarButton("Add new filter").click();
        treeBot.getTreeItem("FILTER <name>").select();
        SWTBotText textBot = filterBot.text();
        textBot.setFocus();
        String filterName = "field";
        textBot.setText(filterName);
        SWTBotTreeItem filterNodeBot = treeBot.getTreeItem(FILTER_TEST + filterName);
        filterNodeBot.click();
        filterNodeBot.contextMenu("TRACETYPE").click();
        filterNodeBot.expand();
        SWTBotCCombo comboBot = filterBot.ccomboBox();
        comboBot.setSelection(TRACETYPE);
        filterNodeBot.getNode(WITH_TRACETYPE).expand();

        // --------------------------------------------------------------------
        // add Compare > 1.5
        // --------------------------------------------------------------------

        filterNodeBot.getNode(WITH_TRACETYPE).contextMenu(COMPARE).click();
        SWTBotTreeItem contentNode = filterNodeBot.getNode(WITH_TRACETYPE).getNode("<select aspect> " + "=" + " <value>");
        contentNode.expand();
        comboBot = filterBot.ccomboBox(1); // aspect
        comboBot.setSelection(CONTENTS);
        textBot = filterBot.text(0); // field
        textBot.setFocus();
        textBot.setText(filterName);

        textBot = filterBot.text(1); // value
        textBot.setFocus();
        textBot.setText("1.5");
        filterBot.radio(">").click();

        // --------------------------------------------------------------------
        // apply
        // --------------------------------------------------------------------
        viewBot.toolbarButton("Save filters").click();

        String ret = applyFilter(fBot, filterName);
//        filterNodeBot.contextMenu().menu("Delete").click();
        assertEquals("50/100", ret);
    }

    private static String applyFilter(SWTWorkbenchBot bot, final String filterName) {
        WaitUtils.waitForJobs();
        final SWTBotTable eventsTable = SWTBotUtils.activeEventsEditor(bot).bot().table();
        SWTBotTableItem tableItem = eventsTable.getTableItem(2);
        tableItem.contextMenu(filterName).click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(eventsTable, "/100", 1, 1));
        return eventsTable.cell(1, 1);
    }
}
