/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *                   (Extracted from ImportAndReadSmokeTest.java)
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.SWTBotUtil;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions.ConditionHelpers;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramView;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;

/**
 * Abstract SWTBot Smoke test class.
 *
 * @author Matthew Khouzam
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class AbstractImportAndReadSmokeTest {

    /** Trace name */
    protected static final String TRACE_NAME = "scp_dest";
    /** Trace folder */
    protected static final String TRACE_FOLDER = "synctraces";
    /** Trace type name for generic CTF traces */
    protected static final String TRACE_TYPE_NAME = "Generic CTF Trace";
    /** A Generic CTF Trace*/
    protected static final CtfTmfTestTrace fTrace = CtfTmfTestTrace.SYNC_DEST;
    /** SWT BOT workbench reference */
    protected static SWTWorkbenchBot fBot;
    /** Wizard to use */
    protected static Wizard fWizard;

    /** The Log4j logger instance. */
    protected static final Logger fLogger = Logger.getRootLogger();

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        assumeTrue(fTrace.exists());
        SWTBotUtil.failIfUIThread();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 50000; /* 50 second timeout */
        fLogger.addAppender(new NullAppender());
        fBot = new SWTWorkbenchBot();

        SWTBotUtil.closeView("welcome", fBot);

        SWTBotUtil.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        SWTBotUtil.waitForJobs();
    }

    /**
     * Creates a tracing projects
     */
    protected void createProject() {
        SWTBotUtil.focusMainWindow(fBot.shells());
        fBot.menu("File").menu("New").menu("Project...").click();

        fBot.shell("New Project").setFocus();
        SWTBotTree tree = fBot.tree();
        assertNotNull(tree);
        final String tracingKey = "Tracing";
        fBot.waitUntil(ConditionHelpers.IsTreeNodeAvailable(tracingKey, tree));
        final SWTBotTreeItem tracingNode = tree.expandNode(tracingKey);

        tracingNode.select();
        final String projectKey = "Tracing Project";
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(projectKey, tracingNode));
        final SWTBotTreeItem tracingProject = tracingNode.getNode(projectKey);
        assertNotNull(tracingProject);

        tracingProject.select();
        tracingProject.click();

        SWTBotButton nextButton = fBot.button("Next >");
        fBot.waitUntil(Conditions.widgetIsEnabled(nextButton));
        nextButton.click();
        fBot.shell("Tracing Project").setFocus();

        final SWTBotText text = fBot.text();
        text.setText(getProjectName());

        fBot.button("Finish").click();
        SWTBotUtil.waitForJobs();
    }

    /**
     * Opens and get the TmfEventsEditor
     * @return TmfEventsEditor
     */
    protected TmfEventsEditor openEditor() {
        final SWTBotView projectExplorerBot = fBot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
        projectExplorerBot.setFocus();

        final SWTBotTree tree = fBot.tree();
        final SWTBotTreeItem treeItem = tree.getTreeItem(getProjectName());
        treeItem.expand();

        String nodeName = getFullNodeName(treeItem, "Traces");
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(nodeName, treeItem));
        SWTBotTreeItem tracesNode = treeItem.getNode(nodeName);
        tracesNode.expand();


        SWTBotTreeItem traceParentNode = tracesNode;

        if (supportsFolderStructure()) {
            String nodeFolderName = getFullNodeName(tracesNode, TRACE_FOLDER);
            fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(nodeFolderName, tracesNode));
            SWTBotTreeItem traceFolder = tracesNode.getNode(nodeFolderName);
            traceFolder.select();
            traceFolder.doubleClick();
            traceParentNode = traceFolder;
        }

        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(TRACE_NAME, traceParentNode));
        traceParentNode.getNode(TRACE_NAME).select();
        traceParentNode.getNode(TRACE_NAME).doubleClick();
        SWTBotUtil.delay(1000);
        SWTBotUtil.waitForJobs();
        final String expectedTitle = supportsFolderStructure() ? TRACE_FOLDER + IPath.SEPARATOR + TRACE_NAME : TRACE_NAME;

        final IEditorPart iep[] = new IEditorPart[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IEditorReference[] ieds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
                assertNotNull(ieds);
                iep[0] = null;
                for (IEditorReference ied : ieds) {
                    if (ied.getTitle().equals(expectedTitle)) {
                        iep[0] = ied.getEditor(true);
                        break;
                    }
                }
            }
        });
        assertNotNull(iep[0]);
        return (TmfEventsEditor) iep[0];
    }

    private static String getFullNodeName(final SWTBotTreeItem treeItem, String prefix) {
        List<String> nodes = treeItem.getNodes();
        String nodeName = "";
        for (String node : nodes) {
            if (node.startsWith(prefix)) {
                nodeName = node;
            }
        }
        return nodeName;
    }

    /**
     * Finishes the wizard
     */
    protected void importFinish() {
        SWTBotShell shell = fBot.activeShell();
        final SWTBotButton finishButton = fBot.button("Finish");
        finishButton.click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        SWTBotUtil.waitForJobs();
    }

    /**
     * Gets the project Name
     * @return the project name
     */
    protected abstract String getProjectName();

    /**
     * Returns whether or not that test support folder structure
     *
     * @return true if the test supports folder structure, false otherwise
     */
    protected abstract boolean supportsFolderStructure();

    // ---------------------------------------------
    // Helpers for testing views
    // ---------------------------------------------

    /**
     * Verifies the properties view for a given view part
     *
     * @param vp
     *            a view part
     */
    protected void testPropertyView(IViewPart vp) {
        PropertySheet pv = (PropertySheet) vp;
        assertNotNull(pv);
    }

    /**
     * Verifies the Histogram View
     * @param vp
     *            the view part
     * @param tmfEd
     *            the events editor
     */
    protected void testHistogramView(IViewPart vp, final TmfEventsEditor tmfEd) {
        final CtfTmfEvent desiredEvent1 = getEvent(100);
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                tmfEd.setFocus();
                tmfEd.selectionChanged(new SelectionChangedEvent(tmfEd, new StructuredSelection(desiredEvent1)));
            }
        });

        SWTBotUtil.waitForJobs();
        SWTBotUtil.delay(1000);

        final CtfTmfEvent desiredEvent2 = getEvent(10000);
        SWTBotView hvBot = fBot.viewById(HistogramView.ID);
        List<SWTBotToolbarButton> hvTools = hvBot.getToolbarButtons();
        for (SWTBotToolbarButton hvTool : hvTools) {
            if (hvTool.getToolTipText().toLowerCase().contains("lost")) {
                hvTool.click();
            }
        }
        HistogramView hv = (HistogramView) vp;
        final TmfTimeSynchSignal signal = new TmfTimeSynchSignal(hv, desiredEvent1.getTimestamp());
        final TmfTimeSynchSignal signal2 = new TmfTimeSynchSignal(hv, desiredEvent2.getTimestamp());
        hv.updateTimeRange(100000);
        SWTBotUtil.waitForJobs();
        hv.currentTimeUpdated(signal);
        hv.broadcast(signal);
        SWTBotUtil.waitForJobs();
        SWTBotUtil.delay(1000);

        hv.updateTimeRange(1000000000);
        SWTBotUtil.waitForJobs();
        hv.currentTimeUpdated(signal2);
        hv.broadcast(signal2);
        SWTBotUtil.waitForJobs();
        SWTBotUtil.delay(1000);
        assertNotNull(hv);
    }

    /**
     * Verifies the statistics view
     * @param vp
     *            the view part
     */
    protected void testStatisticsView(IViewPart vp) {
        TmfStatisticsView sv = (TmfStatisticsView) vp;
        assertNotNull(sv);
    }

    // ---------------------------------------------
    // Trace helpers
    // ---------------------------------------------

    /**
     * Gets an event at a given rank
     * @param rank
     *            a rank
     * @return the event at given rank
     */
    protected CtfTmfEvent getEvent(int rank) {
        try (CtfTmfTrace trace = fTrace.getTrace()) {
            ITmfContext ctx = trace.seekEvent(0);
            for (int i = 0; i < rank; i++) {
                trace.getNext(ctx);
            }
            return trace.getNext(ctx);
        }
    }

    /**
     * Gets a view part based on view title
     * @param viewTile
     *              a view title
     * @return the view part
     */
    protected IViewPart getViewPart(final String viewTile) {
        final IViewPart[] vps = new IViewPart[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IViewReference[] viewRefs = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getViewReferences();
                for (IViewReference viewRef : viewRefs) {
                    IViewPart vp = viewRef.getView(true);
                    if (vp.getTitle().equals(viewTile)) {
                        vps[0] = vp;
                        return;
                    }
                }
            }
        });

        return vps[0];
    }
}
