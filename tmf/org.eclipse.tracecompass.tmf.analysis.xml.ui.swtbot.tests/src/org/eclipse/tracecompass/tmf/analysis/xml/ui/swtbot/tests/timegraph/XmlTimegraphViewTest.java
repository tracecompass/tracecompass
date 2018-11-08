/*******************************************************************************
 * Copyright (c) 2016, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.tmf.analysis.xml.ui.swtbot.tests.timegraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.Path;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlAnalysisModuleSource;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlUtils;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.ui.views.timegraph.XmlTimeGraphView;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.Activator;
import org.eclipse.tracecompass.tmf.analysis.xml.core.tests.common.TmfXmlTestFiles;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Class for XML Timegraph View test
 *
 * @author Jean-Christian Kouame
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class XmlTimegraphViewTest {

    private static final String PROJECT_NAME = "test";
    private static final String TRACE_TYPE = "org.eclipse.linuxtools.tmf.core.tests.xmlstub";
    private static final String TRACE_NAME = "testTrace2.xml";
    private static final String ANALYSIS_NAME = "test.xml.conditions";
    private static final String VIEW_NAME = "Xml Timegraph View Test";
    private static SWTWorkbenchBot fBot;

    /**
     * Things to setup
     */
    @Before
    public void before() {

        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBotTest");
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 10000; /* 10 second timeout */
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        fBot = new SWTWorkbenchBot();

        loadXmlFile();
        openTrace();
        openView(VIEW_NAME);
    }

    /**
     * Bypassing the native import wizard and programmatically load the XML
     * analysis
     */
    private static void loadXmlFile() {
        XmlUtils.addXmlFile(TmfXmlTestFiles.CONDITION_FILE.getFile());
        XmlUtils.addXmlFile(TmfXmlTestFiles.VALID_TIMEGRAPH_VIEW_ELEMENT_FILE.getFile());

        XmlAnalysisModuleSource.notifyModuleChange();
    }

    /**
     * Create a tracing project and open the test trace
     */
    private static void openTrace() {
        SWTBotUtils.createProject(PROJECT_NAME);
        SWTBotUtils.openTrace(PROJECT_NAME, Activator.getAbsolutePath(new Path("test_traces/testTrace2.xml")).toString(), TRACE_TYPE);
        WaitUtils.waitForJobs();
    }

    /**
     * Open a the timegraph view
     *
     * @param viewTitle
     *            The view title
     */
    private static void openView(final String viewTitle) {
        SWTBotTreeItem treeItem = SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME);
        treeItem = SWTBotUtils.getTreeItem(fBot, treeItem, TRACE_NAME, "Views", ANALYSIS_NAME, viewTitle);
        treeItem.doubleClick();
        WaitUtils.waitForJobs();
    }

    /**
     * Test that the timegraph view is open and is populated
     */
    @Test
    public void testData() {
        SWTBotTimeGraph timegraph = getTimegraph();

        // Test the window range
        TimeGraphControl widget = timegraph.widget;
        SWTBotUtils.waitUntil(control -> control.getTimeDataProvider().getTime0() == 1, widget, "window start time");
        SWTBotUtils.waitUntil(control -> control.getTimeDataProvider().getTime1() == 7, widget, "window end time");

        // test entries
        SWTBotTimeGraphEntry traceEntry = timegraph.getEntry(TRACE_NAME);
        SWTBotTimeGraphEntry entry = traceEntry.getEntry("checkpoint");
        assertEquals("number of entries", 1, timegraph.getEntries().length);
        assertEquals("number of entries", 1, traceEntry.getEntries().length);
        assertEquals("name of entry", "checkpoint", entry.getText());
    }

    private static SWTBotTimeGraph getTimegraph() {
        SWTBotView viewBot = fBot.viewById(XmlTimeGraphView.ID);
        SWTBotTimeGraph timegraph = new SWTBotTimeGraph(viewBot.bot());
        assertTrue("timegraph visible", timegraph.isVisible());
        timegraph.setFocus();
        return timegraph;
    }

    /**
     * Close the editor and delete the project
     */
    @After
    public void tearDown() {
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }
}
