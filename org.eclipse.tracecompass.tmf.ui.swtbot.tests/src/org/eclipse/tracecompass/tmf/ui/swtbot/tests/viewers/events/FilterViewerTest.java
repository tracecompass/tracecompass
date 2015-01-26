/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.finders.ContextMenuFinder;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.io.BufferedRandomAccessFile;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.filter.FilterView;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test for filter views in trace compass
 */
public class FilterViewerTest {


    private static final String COMPARE = "COMPARE";
    private static final String CONTAINS = "CONTAINS";
    private static final String XMLSTUB_ID = "org.eclipse.linuxtools.tmf.core.tests.xmlstub";
    private static final String TRACETYPE = "Test trace : XML Trace Stub";
    private static final String AND = "AND";
    private static final String WITH_TRACETYPE = "WITH TRACETYPE " + TRACETYPE;
    private static final String FILTER_TEST = "FILTER ";

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
        SWTBotUtils.failIfUIThread();
        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();
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
        SWTBotCombo comboBot = filterBot.comboBox();
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
        comboBot = filterBot.comboBox();
        comboBot.setSelection(comboBot.itemCount() - 3);
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
        SWTBotCombo comboBot = filterBot.comboBox();
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
        comboBot = filterBot.comboBox();
        //timestamp
        comboBot.setSelection(comboBot.itemCount() - 3);
        textBot = filterBot.text();
        textBot.setFocus();
        textBot.setText("19:00:00.000 000 300");

        // --------------------------------------------------------------------
        // add MATCHES "1"
        // --------------------------------------------------------------------
        orNode.contextMenu("MATCHES").click();
        orNode.expand();
        orNode.getNode(1).select();
        // contents
        comboBot = filterBot.comboBox();
        comboBot.setSelection(comboBot.itemCount() - 1);
        textBot = filterBot.text(0);
        textBot.setFocus();
        textBot.setText("field");
        textBot = filterBot.text(1);
        textBot.setFocus();
        textBot.setText("1");

        viewBot.toolbarButton("Save filters").click();

        String ret = applyFilter(fBot, filterName);
        assertEquals("26/100", ret);
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
        SWTBotCombo comboBot = filterBot.comboBox();
        comboBot.setSelection(TRACETYPE);
        filterNodeBot.getNode(WITH_TRACETYPE).expand();

        // --------------------------------------------------------------------
        // add Compare > 1.5
        // --------------------------------------------------------------------

        filterNodeBot.getNode(WITH_TRACETYPE).contextMenu(COMPARE).click();
        SWTBotTreeItem contentNode = filterNodeBot.getNode(WITH_TRACETYPE).getNode("<select aspect> " + "=" + " <value>");
        contentNode.expand();
        comboBot = filterBot.comboBox();
        comboBot.setSelection(comboBot.itemCount() - 1);
        textBot = filterBot.text(0);
        textBot.setFocus();
        textBot.setText(filterName);

        textBot = filterBot.text(1);
        textBot.setFocus();
        textBot.setText("1.5");
        filterBot.radio(2).click();

        // --------------------------------------------------------------------
        // apply
        // --------------------------------------------------------------------
        viewBot.toolbarButton("Save filters").click();

        String ret = applyFilter(fBot, filterName);
        assertEquals("50/100", ret);
    }

    private static String applyFilter(SWTWorkbenchBot bot, final String filterName) {
        SWTBotUtils.waitForJobs();
        final SWTBotTable eventsEditor = bot.activeEditor().bot().table();
        eventsEditor.select(2);
        UIThreadRunnable.syncExec(new VoidResult() {

            @Override
            public void run() {
                ContextMenuFinder cmf = new ContextMenuFinder(eventsEditor.widget);
                ContextMenuListeners matcher = new ContextMenuListeners(filterName);
                cmf.findMenus(matcher);
            }
        });
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(eventsEditor, "/100", 1, 1));
        return eventsEditor.cell(1, 1);
    }

    /**
     * FIXME: This is a big hack until SWTBot supports context menus better (bug 458975)
     */
    private static final class ContextMenuListeners extends BaseMatcher<MenuItem> {
        private final String filterName;

        private boolean found = false;

        private ContextMenuListeners(String filterName) {
            this.filterName = filterName;
        }

        @Override
        public void describeTo(Description description) {
        }

        @Override
        public boolean matches(Object item) {
            if (item instanceof MenuItem) {
                MenuItem menuItem = (MenuItem) item;
                if (menuItem.getText().equals(filterName)) {
                    for (Listener listener : menuItem.getListeners(SWT.Selection)) {
                        if (!found) {
                            Event event = new Event();
                            event.type = SWT.Selection;
                            event.widget = menuItem;
                            event.button = 1;
                            listener.handleEvent(event);
                            found = true;
                        }
                        return true;
                    }
                }
            }
            return false;
        }
    }
}
