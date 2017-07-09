/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the collapse state of the time graph view
 *
 * @author Jean-Christian Kouame
 *
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class TimegraphViewExpandStateTestBase {

    private static final String FLIPPING_ENDIANNESS = "flipping-endianness";
    private static final String BUG446190 = "bug446190";
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
     *
     * @throws IOException
     *             If the traces can not be found
     */
    @BeforeClass
    public static void beforeClass() throws IOException {
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
        /* Open traces */
        String tracePath1 = FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL()).getPath();
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, tracePath1, KERNEL_TRACE_TYPE);
        String tracePath2 = FileLocator.toFileURL(CtfTestTrace.FLIPPING_ENDIANNESS.getTraceURL()).getPath();
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, tracePath2, KERNEL_TRACE_TYPE);
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
     * Open the view
     */
    @Before
    public void before() {
        SWTBotUtils.openView(getViewId());
    }

    /**
     * After Test
     */
    @After
    public void after() {
        fBot.closeAllEditors();
        SWTBotUtils.closeSecondaryShells(fBot);
    }

    private SWTBotView getViewBot() {
        return fBot.viewByTitle(getViewTitle());
    }

    /**
     * Get the time graph view id
     *
     * @return The view ID
     */
    protected abstract String getViewId();

    /**
     * Get the time graph view title
     *
     * @return The view title
     */
    protected abstract String getViewTitle();

    /**
     * Get the item label to expand/collapse in the test
     *
     * @return The label
     */
    protected abstract String[] getItemLabel();

    /**
     * Test the expand state of time graph entries when switching traces This test
     * collapse some entries, switch between traces then test that the expanded
     * count did not change
     */
    @Test
    public void testExpandedState() {

        // Select editor
        SWTBotTimeGraph timegraphBot = new SWTBotTimeGraph(getViewBot().bot());
        SWTBotUtils.activateEditor(fBot, BUG446190);
        WaitUtils.waitUntil(root -> timegraphBot.getEntries()[0].getText().equals(root), BUG446190, "Failed to activate editor " + BUG446190);

        // expand all entries
        timegraphBot.expandAll();
        int count1 = timegraphBot.getExpandedElementCount();

        // Collapse specific entries
        for (String label : getItemLabel()) {
            timegraphBot.expandEntry(false, BUG446190, label);
        }
        int count2 = timegraphBot.getExpandedElementCount();
        assertTrue("Expanded entries count sould be less than " + count1 + " but actual value is " + count2, count2 < count1);

        int count3 = switchBetweenTraces(timegraphBot);
        assertEquals("Expanded entries count is " + count3 + " but it should be " + count2, count2, count3);

        // collapse all entries
        timegraphBot.collapseAll();
        int count4 = switchBetweenTraces(timegraphBot);
        assertTrue("Expanded entries count sould be less than " + count3 + " but actual value is " + count4, count4 < count3);

        int count5 = switchBetweenTraces(timegraphBot);
        assertEquals("Expanded entries count is " + count5 + " but it should be " + count4, count4, count5);
    }

    private static int switchBetweenTraces(SWTBotTimeGraph timegraphBot) {
        // Switch between traces
        SWTBotUtils.activateEditor(fBot, FLIPPING_ENDIANNESS);
        WaitUtils.waitUntil(root -> timegraphBot.getEntries()[0].getText().equals(root), FLIPPING_ENDIANNESS, "Failed to activate editor " + FLIPPING_ENDIANNESS);
        SWTBotUtils.activateEditor(fBot, BUG446190);
        WaitUtils.waitUntil(root -> timegraphBot.getEntries()[0].getText().equals(root), BUG446190, "Failed to activate editor " + BUG446190);
        return timegraphBot.getExpandedElementCount();
    }
}
