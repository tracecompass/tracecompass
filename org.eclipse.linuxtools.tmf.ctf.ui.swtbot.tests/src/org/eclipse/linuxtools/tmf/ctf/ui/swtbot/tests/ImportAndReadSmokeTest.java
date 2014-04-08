/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Marc-Andre Laperle
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ctf.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.linuxtools.tmf.ui.project.wizards.importtrace.BatchImportTraceWizard;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.SWTBotUtil;
import org.eclipse.linuxtools.tmf.ui.swtbot.tests.conditions.ConditionHelpers;
import org.eclipse.linuxtools.tmf.ui.views.histogram.HistogramView;
import org.eclipse.linuxtools.tmf.ui.views.statistics.TmfStatisticsView;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * SWTBot Smoke test. base for other tests
 *
 * @author Matthew Khouzam
 */
public class ImportAndReadSmokeTest {


    private static final String TRACE_PROJECT_NAME = "test";
    private static final String TRACE_NAME = "synthetic-trace";
    private static final String TRACE_TYPE_NAME = "Generic CTF Trace";
    private static final CtfTmfTestTrace fTrace = CtfTmfTestTrace.SYNTHETIC_TRACE;

    private static SWTWorkbenchBot fBot;
    private static Wizard fWizard;

    /** The Log4j logger instance. */
    private static final Logger fLogger = Logger.getRootLogger();

    /** Test Class setup */
    @BeforeClass
    public static void init() {
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
     * Main test case
     */
    @Test
    public void test() {
        createProject();

        batchImportOpenWizard();
        batchImportSelecTraceType();
        batchImportAddDirectory();
        batchImportSelectTrace();
        batchImportFinish();

        TmfEventsEditor tmfEd = openEditor();

        testHistogramView(getViewPart("Histogram"), tmfEd);
        testPropertyView(getViewPart("Properties"));
        testStatisticsView(getViewPart("Statistics"));

        deleteProject();
    }

    private static void createProject() {
        SWTBotUtil.focusMainWindow(fBot.shells());
        fBot.menu("File").menu("New").menu("Project...").click();

        fBot.waitUntil(Conditions.shellIsActive("New Project"));
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
        fBot.waitUntil(Conditions.shellIsActive("Tracing Project"));

        final SWTBotText text = fBot.text();
        text.setText(TRACE_PROJECT_NAME);

        fBot.button("Finish").click();
        SWTBotUtil.waitForJobs();
    }

    private static void batchImportOpenWizard() {
        fWizard = new BatchImportTraceWizard();

        UIThreadRunnable.asyncExec(new VoidResult() {
            @Override
            public void run() {
                final IWorkbench workbench = PlatformUI.getWorkbench();
                // Fire the Import Trace Wizard
                if (workbench != null) {
                    final IWorkbenchWindow activeWorkbenchWindow = workbench.getActiveWorkbenchWindow();
                    Shell shell = activeWorkbenchWindow.getShell();
                    assertNotNull(shell);
                    ((BatchImportTraceWizard) fWizard).init(PlatformUI.getWorkbench(), StructuredSelection.EMPTY);
                    WizardDialog dialog = new WizardDialog(shell, fWizard);
                    dialog.open();
                }
            }
        });

        fBot.waitUntil(ConditionHelpers.isWizardReady(fWizard));
    }

    private static void batchImportSelecTraceType() {
        final SWTBotTree tree = fBot.tree();
        final String ctfId = "Common Trace Format";
        fBot.waitUntil(ConditionHelpers.IsTreeNodeAvailable(ctfId, tree));
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(TRACE_TYPE_NAME, tree.getTreeItem(ctfId)));
        tree.getTreeItem(ctfId).getNode(TRACE_TYPE_NAME).check();
        batchImportClickNext();
    }

    private static void batchImportAddDirectory() {
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                ((BatchImportTraceWizard) fWizard).addFileToScan(fTrace.getPath());
            }
        });
        final SWTBotButton removeButton = fBot.button("Remove");
        fBot.waitUntil(Conditions.widgetIsEnabled(removeButton));
        removeButton.click();
        fBot.waitUntil(Conditions.tableHasRows(fBot.table(), 1));

        batchImportClickNext();
    }

    private static void batchImportSelectTrace() {
        SWTBotTree tree = fBot.tree();
        fBot.waitUntil(Conditions.widgetIsEnabled(tree));
        final SWTBotTreeItem genericCtfTreeItem = tree.getTreeItem(TRACE_TYPE_NAME);
        fBot.waitUntil(Conditions.widgetIsEnabled(genericCtfTreeItem));
        genericCtfTreeItem.expand();
        genericCtfTreeItem.check();
        batchImportClickNext();
    }

    private static void batchImportClickNext() {
        IWizardPage currentPage = fWizard.getContainer().getCurrentPage();
        IWizardPage desiredPage = fWizard.getNextPage(currentPage);
        SWTBotButton nextButton = fBot.button("Next >");
        nextButton.click();
        fBot.waitUntil(ConditionHelpers.isWizardOnPage(fWizard, desiredPage));
    }

    private static void batchImportFinish() {
        SWTBotShell shell = fBot.activeShell();
        final SWTBotButton finishButton = fBot.button("Finish");
        finishButton.click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        SWTBotUtil.waitForJobs();
    }

    private static TmfEventsEditor openEditor() {
        final SWTBotView projectExplorerBot = fBot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
        projectExplorerBot.setFocus();

        final SWTBotTree tree = fBot.tree();
        final SWTBotTreeItem treeItem = tree.getTreeItem(TRACE_PROJECT_NAME);
        treeItem.expand();

        List<String> nodes = treeItem.getNodes();
        String nodeName = "";
        for (String node : nodes) {
            if (node.startsWith("Traces")) {
                nodeName = node;
            }
        }
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(nodeName, treeItem));
        treeItem.getNode(nodeName).expand();
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(TRACE_NAME, treeItem.getNode(nodeName)));
        treeItem.getNode(nodeName).getNode(TRACE_NAME).select();
        treeItem.getNode(nodeName).getNode(TRACE_NAME).doubleClick();
        SWTBotUtil.delay(1000);
        SWTBotUtil.waitForJobs();

        final IEditorPart iep[] = new IEditorPart[1];
        UIThreadRunnable.syncExec(new VoidResult() {
            @Override
            public void run() {
                IEditorReference[] ieds = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
                assertNotNull(ieds);
                iep[0] = null;
                for (IEditorReference ied : ieds) {
                    if (ied.getTitle().equals(TRACE_NAME)) {
                        iep[0] = ied.getEditor(true);
                        break;
                    }
                }
            }
        });
        assertNotNull(iep[0]);
        return (TmfEventsEditor) iep[0];
    }

    private static void deleteProject() {
        try {
            ResourcesPlugin.getWorkspace().getRoot().getProject(TRACE_PROJECT_NAME).refreshLocal(IResource.DEPTH_INFINITE, null);
        } catch (CoreException e) {
        }

        SWTBotUtil.waitForJobs();

        final SWTBotView projectViewBot = fBot.viewById(IPageLayout.ID_PROJECT_EXPLORER);
        projectViewBot.setFocus();

        SWTBotTree treeBot = fBot.tree();
        SWTBotTreeItem treeItem = treeBot.getTreeItem(TRACE_PROJECT_NAME);
        SWTBotMenu contextMenu = treeItem.contextMenu("Delete");
        contextMenu.click();

        String shellText = "Delete Resources";
        fBot.waitUntil(Conditions.shellIsActive(shellText));
        final SWTBotButton okButton = fBot.button("OK");
        fBot.waitUntil(Conditions.widgetIsEnabled(okButton));
        okButton.click();

        SWTBotUtil.waitForJobs();
    }

    // ---------------------------------------------
    // Helpers for testing views
    // ---------------------------------------------

    private static void testPropertyView(IViewPart vp) {
        PropertySheet pv = (PropertySheet) vp;
        assertNotNull(pv);
    }

    private static void testHistogramView(IViewPart vp, final TmfEventsEditor tmfEd) {
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

    private static void testStatisticsView(IViewPart vp) {
        TmfStatisticsView sv = (TmfStatisticsView) vp;
        assertNotNull(sv);
    }

    // ---------------------------------------------
    // Trace helpers
    // ---------------------------------------------

    private static CtfTmfEvent getEvent(int rank) {
        CtfTmfTrace trace = fTrace.getTrace();
        if (trace == null) {
            return null;
        }
        ITmfContext ctx = trace.seekEvent(0);
        for (int i = 0; i < rank; i++) {
            trace.getNext(ctx);
        }
        final CtfTmfEvent retVal = trace.getNext(ctx);
        trace.dispose();
        return retVal;
    }

    private static IViewPart getViewPart(final String viewTile) {
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
