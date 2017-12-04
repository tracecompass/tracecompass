/*******************************************************************************
 * Copyright (c) 2013, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.viewers.events;

import static org.junit.Assert.assertEquals;
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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for testing collapsing feature.
 *
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CollapseEventsInTableTest {

    private static final String TRACE_PROJECT_NAME = "test";
    private static final String COLLAPSE_TRACE_NAME = "syslog_collapse";
    private static final String COLLAPSE_TRACE_PATH = "testfiles/" + COLLAPSE_TRACE_NAME;
    private static final String COLLAPSE_TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";

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

        /* set up test trace*/
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(COLLAPSE_TRACE_PATH), null);
        URI uri;
        try {
            uri = FileLocator.toFileURL(location).toURI();
            fTestFile = new File(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            fail();
        }

        assumeTrue(fTestFile.exists());

        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.DATIME, "MMM d HH:mm:ss");
        defaultPreferences.put(ITmfTimePreferencesConstants.SUBSEC, ITmfTimePreferencesConstants.SUBSEC_NO_FMT);
        TmfTimestampFormat.updateDefaultFormats();

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
        /* Set timestamp defaults */
        IEclipsePreferences defaultPreferences = InstanceScope.INSTANCE.getNode(Activator.PLUGIN_ID);
        defaultPreferences.put(ITmfTimePreferencesConstants.DATIME, ITmfTimePreferencesConstants.TIME_HOUR_FMT);
        defaultPreferences.put(ITmfTimePreferencesConstants.SUBSEC, ITmfTimePreferencesConstants.SUBSEC_NANO_FMT);
        TmfTimestampFormat.updateDefaultFormats();
    }

    /**
     * Main test case
     */
    @Test
    public void test() {
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), COLLAPSE_TRACE_TYPE);
        SWTBotEditor editorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());

        SWTBotTable tableBot = editorBot.bot().table();

        /* Maximize editor area */
        SWTBotUtils.maximize(editorBot.getReference(), tableBot);
        tableBot.click(1, 0);

        /* Collapse Events */
        SWTBotMenu menuBot = tableBot.contextMenu("Collapse Events");
        menuBot.click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "7/22", 1, 1));

        String cell = tableBot.cell(1, 1);
        assertEquals("filterString", "7/22", cell);

        /* Verify first collapsed event */
        cell = tableBot.cell(7, 0);
        assertEquals("1st repeatCount", "+14", cell);
        cell = tableBot.cell(7, 1);
        assertEquals("1st Timestamp", "Jan 1 06:06:06", cell);
        cell = tableBot.cell(7, 2);
        assertEquals("1st Host", "HostF", cell);
        cell = tableBot.cell(7, 3);
        assertEquals("1st Logger", "LoggerF", cell);
        cell = tableBot.cell(7, 4);
        assertEquals("1st File", "SourceFile", cell);
        cell = tableBot.cell(7, 5);
        assertEquals("1st Line", "9", cell);
        cell = tableBot.cell(7, 6);
        assertEquals("1st Message", "Message F", cell);

        /* Verify second collapsed event */
        cell = tableBot.cell(8, 0);
        assertEquals("2nd repeatCount", "+1", cell);
        cell = tableBot.cell(8, 1);
        assertEquals("2nd Timestamp", "Jan 1 06:06:21", cell);
        cell = tableBot.cell(8, 2);
        assertEquals("2nd Host", "HostF", cell);
        cell = tableBot.cell(8, 3);
        assertEquals("2nd Logger", "LoggerF", cell);
        cell = tableBot.cell(8, 4);
        assertEquals("2nd File", "SourceFile", cell);
        cell = tableBot.cell(8, 5);
        assertEquals("2nd Line", "10", cell);
        cell = tableBot.cell(8, 6);
        assertEquals("2nd Message", "Message D", cell);

        /* Clear Filter */
        menuBot = tableBot.contextMenu("Clear Filters");
        menuBot.click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "Jan 1 01:01:01", 1, 1));
        assertEquals("Timestamp", "Jan 1 01:01:01", tableBot.cell(1, 1));

        SWTBotUtils.maximize(editorBot.getReference(), tableBot);

        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }
}
