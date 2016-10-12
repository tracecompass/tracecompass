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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.editors.TmfTraceColumnManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for testing movable column feature.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class MovableColumnEventsEditorTest {

    private static final String TRACE_PROJECT_NAME = "test";
    private static final String COLUMN_TRACE = "syslog_collapse";
    private static final String COLUMN_TRACE_PATH = "testfiles/" + COLUMN_TRACE;
    private static final String COLUMN_TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";
    private static final String[] BEFORE_COLS = new String[] { "", "Timestamp", "Host", "Logger", "File", "Line", "Message" };
    private static final String[] AFTER_COLS = new String[] { "", "Timestamp", "Logger", "Host", "File", "Line", "Message" };

    private static File fTestFile = null;

    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Test Class setup
     */
    @BeforeClass
    public static void init() {
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
        WaitUtils.waitForJobs();
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
        fLogger.removeAllAppenders();
    }

    /**
     * Main test case, open a trace, reorder columns, check that they are
     * reordered, reopen it, check that its still reordered, close it, reset,
     * reopen check that its in the original order.
     */
    @Test
    public void testReorder() {
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);

        // Open the actual trace
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), COLUMN_TRACE_TYPE);
        SWTBotEditor editorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());

        SWTBotTable tableBot = editorBot.bot().table();

        // Maximize editor area
        SWTBotUtils.maximizeTable(tableBot);

        // Verify that source code was actually opened
        assertArrayEquals("Before reorder", BEFORE_COLS, tableBot.columns().toArray());
        final Table table = tableBot.widget;
        // simulate column drag
        final int[] newColOrder = { 0, 1, 3, 2, 4, 5, 6 };
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                table.setColumnOrder(newColOrder);
            }
        });

        // close and re-open
        editorBot.close();
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), COLUMN_TRACE_TYPE);
        editorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());
        tableBot = editorBot.bot().table();
        // Maximize editor area
        SWTBotUtils.maximizeTable(tableBot);
        assertArrayEquals("After reorder", AFTER_COLS, tableBot.columns().toArray());
        // close and re-open
        editorBot.close();
        TmfTraceColumnManager.clearColumnOrder(COLUMN_TRACE_TYPE);
        assertNull("After clear", TmfTraceColumnManager.loadColumnOrder(COLUMN_TRACE_TYPE));
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), COLUMN_TRACE_TYPE);
        editorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());
        tableBot = editorBot.bot().table();
        // Maximize editor area
        SWTBotUtils.maximizeTable(tableBot);
        assertNull("After reset", TmfTraceColumnManager.loadColumnOrder(COLUMN_TRACE_TYPE));
        assertArrayEquals("After reset", BEFORE_COLS, tableBot.columns().toArray());
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }
}
