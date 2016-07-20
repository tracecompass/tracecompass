/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.ArrayResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableColumn;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for event table column header menu.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ColumnHeaderMenuTest {

    private static final String TRACE_PROJECT_NAME = "test";
    private static final String COLUMN_TRACE = "syslog_collapse";
    private static final String COLUMN_TRACE_PATH = "testfiles/" + COLUMN_TRACE;
    private static final String COLUMN_TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";

    private static File fTestFile = null;

    private static SWTWorkbenchBot fBot;
    private SWTBotEditor fEditorBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void beforeClass() {
        SWTBotUtils.initialize();

        /* set up test trace */
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(COLUMN_TRACE_PATH), null);
        URI uri;
        try {
            uri = FileLocator.toFileURL(location).toURI();
            fTestFile = new File(uri);
        } catch (URISyntaxException | IOException e) {
            fail(e.getMessage());
        }

        assumeTrue(fTestFile.exists());

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", fBot);

        /* Switch perspectives */
        SWTBotUtils.switchToTracingPerspective();

        /* Finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();

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
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), COLUMN_TRACE_TYPE);
        fEditorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());
    }

    /**
     * After Test
     */
    @After
    public void after() {
        fBot.closeAllEditors();
    }

    /**
     * Test the check menu items to toggle column visibility
     */
    @Test
    public void testToggleColumns() {
        final SWTBotTable tableBot = fEditorBot.bot().table();
        SWTBotTableColumn headerBot = tableBot.header("");
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Logger", "File", "Line", "Message" });

        headerBot.contextMenu("Message").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Logger", "File", "Line" });

        headerBot.contextMenu("Line").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Logger", "File" });

        headerBot.contextMenu("File").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Logger" });

        headerBot.contextMenu("Logger").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host" });

        headerBot.contextMenu("Host").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp" });

        headerBot.contextMenu("Timestamp").click();
        assertVisibleColumns(tableBot.widget, new String[] {});

        headerBot.contextMenu("Message").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Message" });

        headerBot.contextMenu("Timestamp").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Message" });

        headerBot.contextMenu("Line").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Line", "Message" });

        headerBot.contextMenu("Host").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Line", "Message" });

        headerBot.contextMenu("File").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "File", "Line", "Message" });

        headerBot.contextMenu("Logger").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Logger", "File", "Line", "Message" });
    }

    /**
     * Test the Show All menu item
     */
    @Test
    public void testPersistHiding() {
        SWTBotTable tableBot = fEditorBot.bot().table();
        SWTBotTableColumn headerBot = tableBot.header("");
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Logger", "File", "Line", "Message" });

        headerBot.contextMenu("Timestamp").click();
        headerBot.contextMenu("Host").click();
        headerBot.contextMenu("Logger").click();
        headerBot.contextMenu("File").click();
        headerBot.contextMenu("Message").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Line" });
        after();

        before();
        tableBot = fEditorBot.bot().table();
        assertVisibleColumns(tableBot.widget, new String[] { "Line" });
        headerBot = tableBot.header("");
        headerBot.contextMenu("Show All").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Logger", "File", "Line", "Message" });
    }

    /**
     * Test the Show All menu item
     */
    @Test
    public void testShowAll() {
        final SWTBotTable tableBot = fEditorBot.bot().table();
        SWTBotTableColumn headerBot = tableBot.header("");
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Logger", "File", "Line", "Message" });

        headerBot.contextMenu("Timestamp").click();
        headerBot.contextMenu("Host").click();
        headerBot.contextMenu("Logger").click();
        headerBot.contextMenu("File").click();
        headerBot.contextMenu("Line").click();
        headerBot.contextMenu("Message").click();
        assertVisibleColumns(tableBot.widget, new String[] {});

        headerBot.contextMenu("Show All").click();
        assertVisibleColumns(tableBot.widget, new String[] { "Timestamp", "Host", "Logger", "File", "Line", "Message" });
    }

    private static void assertVisibleColumns(final Table table, String[] expected) {
        String[] actual = UIThreadRunnable.syncExec(new ArrayResult<String>() {
            @Override
            public String[] run() {
                List<String> visible = new ArrayList<>();
                for (int i : table.getColumnOrder()) {
                    TableColumn column = table.getColumns()[i];
                    if (column.getResizable() && column.getWidth() > 0) {
                        visible.add(column.getText());
                    }
                }
                return visible.toArray(new String[0]);
            }
        });
        assertArrayEquals(expected, actual);
    }
}
