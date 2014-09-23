/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.swtbot.tests.table;

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
import org.eclipse.jface.bindings.keys.IKeyLookup;
import org.eclipse.jface.bindings.keys.KeyStroke;
import org.eclipse.jface.bindings.keys.ParseException;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.SWTBotUtil;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions.ConditionHelpers;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for testing collapsing feature.
 *
 * @author Bernd Hufmann
 */
@SuppressWarnings("restriction")
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
        SWTBotUtil.failIfUIThread();

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
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Close welcome view */
        SWTBotUtil.closeView("Welcome", fBot);

        /* Switch perspectives */
        SWTBotUtil.switchToTracingPerspective();

        /* Finish waiting for eclipse to load */
        SWTBotUtil.waitForJobs();
    }

    /**
     * Test class tear down method.
     */
    @AfterClass
    public static void tearDown() {
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
        SWTBotUtil.createProject(TRACE_PROJECT_NAME);
        SWTBotUtil.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), COLLAPSE_TRACE_TYPE);
        SWTBotEditor editorBot = SWTBotUtil.openEditor(fBot, fTestFile.getName());

        SWTBotTable tableBot = editorBot.bot().table();

        /* Maximize editor area */
        maximizeTable(tableBot);
        tableBot.click(1, 0);

        /* Collapse Events */
        SWTBotMenu menuBot = tableBot.contextMenu("Collapse Events");
        menuBot.click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "7/22", 1, 1));

        String filterString = tableBot.cell(1, 1);
        assertEquals("filterString", "7/22", filterString);

        /* Verify collapsed event */
        filterString = tableBot.cell(7, 0);
        assertEquals("repeatCount", "+14", filterString);

        filterString = tableBot.cell(7, 1);
        assertEquals("first timestamp", "Jan 1 06:06:06", filterString);

        filterString = tableBot.cell(7, 2);
        assertEquals("source", "", filterString);

        filterString = tableBot.cell(7, 3);
        assertEquals("type", "Syslog", filterString);

        filterString = tableBot.cell(7, 4);
        assertEquals("file", "", filterString);

        filterString = tableBot.cell(7, 5);
        assertEquals("content", "Timestamp=Jan 1 06:06:06, Host=HostF, Logger=LoggerF, Message=Message F", filterString);

        filterString = tableBot.cell(8, 0);
        assertEquals("repeatCount", "+1", filterString);

        filterString = tableBot.cell(8, 1);
        assertEquals("first timestamp (2nd collapse)", "Jan 1 06:06:21", filterString);

        filterString = tableBot.cell(8, 5);
        assertEquals("content", "Timestamp=Jan 1 06:06:21, Host=HostF, Logger=LoggerF, Message=Message D", filterString);

        /* Clear Filter */
        menuBot = tableBot.contextMenu("Clear Filters");
        menuBot.click();
        fBot.waitUntil(ConditionHelpers.isTableCellFilled(tableBot, "Jan 1 01:01:01", 1, 1));
        assertEquals("timestamp", "Jan 1 01:01:01", tableBot.cell(1, 1));

        maximizeTable(tableBot);

        fBot.closeAllEditors();
        SWTBotUtil.deleteProject(TRACE_PROJECT_NAME, fBot);
    }

    private static void maximizeTable(SWTBotTable tableBot) {
        try {
            tableBot.pressShortcut(KeyStroke.getInstance(IKeyLookup.CTRL_NAME + "+"), KeyStroke.getInstance("M"));
        } catch (ParseException e) {
            fail();
        }
    }

}
