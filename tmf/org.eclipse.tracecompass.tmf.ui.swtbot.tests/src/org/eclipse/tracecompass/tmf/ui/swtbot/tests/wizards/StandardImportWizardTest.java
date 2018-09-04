/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.ui.swtbot.tests.wizards;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.Calendar;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportTraceWizardPage;
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.Messages;
import org.eclipse.tracecompass.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot standard import test class
 *
 * @author Simon Delisle
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class StandardImportWizardTest extends AbstractStandardImportWizardTest {

    private static final String TEST_FOLDER_NAME = "testfiles";
    private static final int YEAR = Calendar.getInstance().get(Calendar.YEAR);
    private static final String START_TIME = YEAR + "-01-01 02:00:00";
    private static final String END_TIME = YEAR + "-01-01 05:05:00";

    /**
     * create the project
     */
    @Before
    public void setup() {
        createProject();
        SWTBotPreferences.TIMEOUT = 20000;
    }

    /**
     * Tear down the test
     */
    @After
    public void tearDown() {
        SWTBotUtils.deleteProject(PROJECT_NAME, getSWTBot());
    }

    /**
     * Test the time based import
     *
     * @throws Exception
     *             Exception
     */
    @Test
    @Ignore
    public void testImportTimerange() throws Exception {
        openImportWizard();
        selectSyslog();
        SWTBotImportWizardUtils.setOptions(getSWTBot(), ImportTraceWizardPage.OPTION_FILTER_TIMERANGE, null);
        setTimeRange(START_TIME, END_TIME);
        importFinish();

        verifyImport(4);
    }

    /**
     * Test the import wizard when the time range is not valid
     *
     * @throws Exception
     *             Exception
     */
    @Test
    @Ignore
    public void testImportTimerangeInvalidFormat() throws Exception {
        SWTWorkbenchBot bot = getSWTBot();
        openImportWizard();
        selectSyslog();
        SWTBotImportWizardUtils.setOptions(bot, ImportTraceWizardPage.OPTION_FILTER_TIMERANGE, null);
        setTimeRange("invalid timestamp", END_TIME);
        SWTBotImportWizardUtils.checkFinishButton(bot, false);
        setTimeRange("02:00:00", END_TIME);
        SWTBotImportWizardUtils.checkFinishButton(bot, false);
    }

    /**
     * Test the import by time range with end < start time
     *
     * @throws Exception
     *             Exception
     */
    @Test
    @Ignore
    public void testImportTimerangeInverseTime() throws Exception {
        openImportWizard();
        selectSyslog();
        SWTBotImportWizardUtils.setOptions(getSWTBot(), ImportTraceWizardPage.OPTION_FILTER_TIMERANGE, null);
        setTimeRange(END_TIME, START_TIME);
        importFinish();

        verifyImport(4);
    }

    private static void setTimeRange(String start, String end) {
        SWTWorkbenchBot bot = getSWTBot();
        bot.textWithLabel(Messages.ImportTraceWizard_StartTime).setText(start);
        bot.textWithLabel(Messages.ImportTraceWizard_EndTime).setText(end);
    }

    private static void verifyImport(int expectedLength) {
        SWTBotTreeItem tracesFolder = SWTBotUtils.selectTracesFolder(getSWTBot(), PROJECT_NAME);
        // Expand the tree to get children
        tracesFolder.expand();
        SWTBotTreeItem[] traceItems = tracesFolder.getItems();
        assertEquals(expectedLength, traceItems.length);
    }

    private static void selectSyslog() throws Exception {
        SWTWorkbenchBot bot = getSWTBot();
        URL resource = TmfCoreTestPlugin.getDefault().getBundle().getResource(TEST_FOLDER_NAME);
        String path = FileLocator.toFileURL(resource).toURI().getPath();
        SWTBotImportWizardUtils.selectImportFromDirectory(bot, path);
        for (int i = 1; i <= 6; i++) {
            SWTBotImportWizardUtils.selectFile(bot, "syslog" + i, TEST_FOLDER_NAME);
        }
    }
}
