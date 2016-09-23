/*******************************************************************************
 * Copyright (c) 2013, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Marc-Andre Laperle
 *   Patrick Tasse - Extract base class from ImportAndReadKernelSmokeTest
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.Result;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.tracecompass.ctf.core.tests.shared.LttngTraceGenerator;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Base SWTBot test for LTTng Kernel UI.
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class KernelTestBase {

    /** LTTng kernel trace type */
    protected static final String KERNEL_TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    /** LTTng kernel perspective */
    protected static final String KERNEL_PERSPECTIVE_ID = "org.eclipse.linuxtools.lttng2.kernel.ui.perspective";
    /** Default project name */
    protected static final String TRACE_PROJECT_NAME = "test";

    /** The workbench bot */
    protected static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

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
        SWTBotUtils.switchToPerspective(KERNEL_PERSPECTIVE_ID);
        /* Create the trace project */
        SWTBotUtils.createProject(TRACE_PROJECT_NAME);
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * After Class
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
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, LttngTraceGenerator.getPath(), KERNEL_TRACE_TYPE);
        SWTBotUtils.activateEditor(fBot, LttngTraceGenerator.getName());
    }

    /**
     * After Test
     */
    @After
    public void after() {
        fBot.closeAllEditors();
        SWTBotUtils.closeSecondaryShells(fBot);
    }

    /**
     * Class to check number of checked items
     */
    static final class TreeCheckedCounter implements Result<Integer> {
        private final SWTBotTree fTreeBot;

        TreeCheckedCounter(SWTBotTree treeBot) {
            fTreeBot = treeBot;
        }

        @Override
        public Integer run() {
            int checked = 0;
            for (TreeItem item : fTreeBot.widget.getItems()) {
                checked += getChecked(item);
            }
            return checked;
        }

        private int getChecked(TreeItem item) {
            int total = 0;
            if (item.getChecked()) {
                total++;
            }
            for (TreeItem child : item.getItems()) {
                total += getChecked(child);
            }
            return total;
        }
    }
}
