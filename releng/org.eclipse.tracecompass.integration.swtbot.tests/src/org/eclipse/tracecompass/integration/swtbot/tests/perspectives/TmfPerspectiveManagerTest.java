/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.integration.swtbot.tests.perspectives;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.ctf.core.tests.shared.LttngTraceGenerator;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * SWTBot test for the perspective manager.
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class TmfPerspectiveManagerTest {

    private static final String TRACE_PROJECT_NAME = "test";
    private static final String KERNEL_TRACE_TYPE = "org.eclipse.linuxtools.lttng2.kernel.tracetype";
    private static final String UST_TRACE_TYPE = "org.eclipse.linuxtools.lttng2.ust.tracetype";

    private static File fTmpFolder = null;
    private static String fKernelPath = null;
    private static String fUstPath = null;
    private static SWTWorkbenchBot fBot;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /**
     * Test Class setup
     *
     * @throws IOException if an exception occurs
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
        SWTBotUtils.initialize();

        fTmpFolder = File.createTempFile("testTraces", "");
        if (!fTmpFolder.isDirectory()) {
            fTmpFolder.delete();
            fTmpFolder.mkdir();
        }
        LttngTraceGenerator kernelGenerator = new LttngTraceGenerator(1000, 1000, 1);
        LttngTraceGenerator ustGenerator = new LttngTraceGenerator(1000, 1000, 1, false);
        fKernelPath = fTmpFolder.getAbsolutePath() + File.separator + "kernel";
        fUstPath = fTmpFolder.getAbsolutePath() + File.separator + "ust";
        kernelGenerator.writeTrace(new File(fKernelPath));
        ustGenerator.writeTrace(new File(fUstPath));

        /* Set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout(), ConsoleAppender.SYSTEM_OUT));
        fBot = new SWTWorkbenchBot();

        /* Close welcome view */
        SWTBotUtils.closeView("Welcome", fBot);

        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

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
        /* Reset to default (Resource) perspective */
        SWTBotUtils.switchToPerspective("org.eclipse.ui.resourcePerspective");
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
     * Test the Switch to Perspective prompt, selecting Yes
     */
    @Test
    public void testPromptYes() {
        setSwitchToPerspectivePreference("Prompt");
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fKernelPath, KERNEL_TRACE_TYPE);
        confirmPerspectiveSwitch(true, false);
        assertActivePerspective("LTTng Kernel");
        assertSwitchToPerspectivePreference("Prompt");
    }

    /**
     * Test the Switch to Perspective prompt, selecting No
     */
    @Test
    public void testPromptNo() {
        setSwitchToPerspectivePreference("Prompt");
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fKernelPath, KERNEL_TRACE_TYPE);
        confirmPerspectiveSwitch(false, false);
        assertActivePerspective("Resource");
        assertSwitchToPerspectivePreference("Prompt");
    }

    /**
     * Test the Switch to Perspective prompt, selecting Yes and remember
     */
    @Test
    public void testPromptYesRemember() {
        setSwitchToPerspectivePreference("Prompt");
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fKernelPath, KERNEL_TRACE_TYPE);
        confirmPerspectiveSwitch(true, true);
        assertActivePerspective("LTTng Kernel");
        assertSwitchToPerspectivePreference("Always");
    }

    /**
     * Test the Switch to Perspective prompt, selecting No and remember
     */
    @Test
    public void testPromptNoRemember() {
        setSwitchToPerspectivePreference("Prompt");
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fKernelPath, KERNEL_TRACE_TYPE);
        confirmPerspectiveSwitch(false, true);
        assertActivePerspective("Resource");
        assertSwitchToPerspectivePreference("Never");
    }

    /**
     * Test the Switch to Perspective "Always" preference
     */
    @Test
    public void testAlways() {
        setSwitchToPerspectivePreference("Always");
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fKernelPath, KERNEL_TRACE_TYPE);
        assertActivePerspective("LTTng Kernel");
    }

    /**
     * Test the Switch to Perspective "Never" preference
     */
    @Test
    public void testNever() {
        setSwitchToPerspectivePreference("Never");
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fKernelPath, KERNEL_TRACE_TYPE);
        assertActivePerspective("Resource");
    }

    /**
     * Test trace with no associated perspective
     */
    @Test
    public void testNoAssociatedPerspective() {
        setSwitchToPerspectivePreference("Always");
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, fUstPath, UST_TRACE_TYPE);
        assertActivePerspective("Resource");
    }

    private static void setSwitchToPerspectivePreference(String value) {
        SWTBotUtils.openPreferences(fBot);
        SWTBot shellBot = fBot.shell("Preferences").bot();
        shellBot.tree().expandNode("Tracing", "Perspectives").select();
        shellBot.radioInGroup(value, "Open the associated perspective when a trace is opened").click();
        SWTBotUtils.pressOKishButtonInPreferences(fBot);
    }

    private static void assertSwitchToPerspectivePreference(String value) {
        SWTBotUtils.openPreferences(fBot);
        SWTBot shellBot = fBot.shell("Preferences").bot();
        shellBot.tree().expandNode("Tracing", "Perspectives").select();
        assertTrue(shellBot.radioInGroup(value, "Open the associated perspective when a trace is opened").isSelected());
        shellBot.button("Cancel").click();
    }

    private static void confirmPerspectiveSwitch(boolean confirm, boolean remember) {
        SWTBot shellBot = fBot.shell("Confirm Perspective Switch").bot();
        if (remember) {
            shellBot.checkBox("Remember my decision").click();
        }
        if (confirm) {
            shellBot.button("Yes").click();
        } else {
            shellBot.button("No").click();
        }
    }

    private static void assertActivePerspective(String perspective) {
        assertEquals(perspective, new SWTWorkbenchBot().activePerspective().getLabel());
    }
}
