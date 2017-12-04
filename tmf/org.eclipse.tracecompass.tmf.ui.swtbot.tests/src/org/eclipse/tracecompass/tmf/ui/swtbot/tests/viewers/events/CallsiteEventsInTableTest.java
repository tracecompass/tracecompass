/*******************************************************************************
 * Copyright (c) 2014, 2017 Ericsson
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
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotEditor;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTable;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTableItem;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimePreferencesConstants;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.ui.IEditorReference;
import org.hamcrest.Matcher;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for testing callsite feature.
 *
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class CallsiteEventsInTableTest {

    private static final String TRACE_PROJECT_NAME = "test";
    private static final String CALLSITE_TRACE_NAME = "syslog_collapse";
    private static final String CALLSITE_TRACE_PATH = "testfiles/" + CALLSITE_TRACE_NAME;
    private static final String CALLSITE_TRACE_TYPE = "org.eclipse.linuxtools.tmf.tests.stubs.trace.text.testsyslog";
    private static final String SOURCE_FILE_NAME = "SourceFile";
    private static final String SOURCE_FILE_PATH = "testfiles/" + SOURCE_FILE_NAME;

    private static File fTestFile = null;
    private static File fSourceFile = null;

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
        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(CALLSITE_TRACE_PATH), null);
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

        /* Create source file link */
        location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(SOURCE_FILE_PATH), null);
        try {
            uri = FileLocator.toFileURL(location).toURI();
            fSourceFile = new File(uri);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            fail();
        }

        assumeTrue(fSourceFile.exists());



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

        // Open source file as a unknown trace
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fSourceFile.getAbsolutePath(), null);

        // Open the actual trace
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fTestFile.getAbsolutePath(), CALLSITE_TRACE_TYPE);
        SWTBotEditor editorBot = SWTBotUtils.activateEditor(fBot, fTestFile.getName());

        SWTBotTable tableBot = editorBot.bot().table();

        // Maximize editor area
        SWTBotUtils.maximize(editorBot.getReference(), tableBot);
        SWTBotTableItem tableItem = tableBot.getTableItem(1);

        // Open source code location
        SWTBotMenu menuBot = tableItem.contextMenu("Open Source Code");
        menuBot.click();

        // Verify that source code was actually opened
        Matcher<IEditorReference> matcher = WidgetMatcherFactory.withPartName(fSourceFile.getName());
        final SWTBotEditor sourceEditorBot = fBot.editor(matcher);
        assertTrue(sourceEditorBot.isActive());

        editorBot.show();
        SWTBotUtils.maximize(editorBot.getReference(), tableBot);

        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(TRACE_PROJECT_NAME, fBot);
    }
}
