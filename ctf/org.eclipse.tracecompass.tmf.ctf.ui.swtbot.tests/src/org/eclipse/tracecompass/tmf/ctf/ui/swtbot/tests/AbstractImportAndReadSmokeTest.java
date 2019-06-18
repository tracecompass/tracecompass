/*******************************************************************************
 * Copyright (c) 2014, 2018 Ericsson
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

package org.eclipse.tracecompass.tmf.ctf.ui.swtbot.tests;

import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.apache.log4j.Logger;
import org.apache.log4j.varia.NullAppender;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.Wizard;
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
import org.eclipse.tracecompass.internal.tmf.ui.project.wizards.importtrace.ImportConfirmation;
import org.eclipse.tracecompass.internal.tmf.ui.views.statistics.TmfStatisticsViewImpl;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.histogram.HistogramView;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.properties.PropertySheet;
import org.junit.AfterClass;
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
    /** A Generic CTF Trace */
    protected static final @NonNull CtfTmfTrace fTrace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.SYNC_DEST);
    /** SWT BOT workbench reference */
    protected static SWTWorkbenchBot fBot;
    /** Wizard to use */
    protected static Wizard fWizard;

    /** The Log4j logger instance. */
    protected static final Logger fLogger = Logger.getRootLogger();

    /** Timeout to wait for import operation */
    protected static final long IMPORT_TIME_OUT = 180000L;

    /** Test Class setup */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();

        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 50000; /* 50 second timeout */
        fLogger.removeAllAppenders();
        fLogger.addAppender(new NullAppender());
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.switchToTracingPerspective();
        /* finish waiting for eclipse to load */
        WaitUtils.waitForJobs();
    }

    /**
     * Test Class teardown
     */
    @AfterClass
    public static void terminate() {
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.SYNC_DEST);
        fLogger.removeAllAppenders();
    }

    /**
     * Creates a tracing projects
     *
     * @param traceProjectName
     *            the name of the test project
     */
    protected static void createProject(String traceProjectName) {
        SWTBotUtils.focusMainWindow(fBot.shells());
        fBot.menu("File").menu("New").menu("Project...").click();

        fBot.shell("New Project").activate();
        SWTBotTree tree = fBot.tree();
        assertNotNull(tree);
        final String tracingKey = "Tracing";
        fBot.waitUntil(ConditionHelpers.isTreeNodeAvailable(tracingKey, tree));
        final SWTBotTreeItem tracingNode = tree.expandNode(tracingKey);

        tracingNode.select();
        final String projectKey = "Tracing Project";
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable(projectKey, tracingNode));
        final SWTBotTreeItem tracingProject = tracingNode.getNode(projectKey);
        assertNotNull(tracingProject);

        tracingProject.select();
        tracingProject.click();

        SWTBotButton nextButton = fBot.button("Next >");
        fBot.waitUntil(Conditions.widgetIsEnabled(nextButton));
        nextButton.click();
        fBot.shell("Tracing Project").activate();

        final SWTBotText text = fBot.text();
        text.setText(traceProjectName);

        fBot.button("Finish").click();
        WaitUtils.waitForJobs();
    }

    /**
     * Finishes the wizard
     */
    protected void importFinish() {
        importFinish(ImportConfirmation.CONTINUE);
    }

    /**
     * Finishes the wizard
     *
     * @param confirmationMode
     *            a confirmation value
     *            Note: Only {@link ImportConfirmation#RENAME_ALL},
     *            {@link ImportConfirmation#OVERWRITE_ALL},
     *            {@link ImportConfirmation#CONTINUE} are supported
     */
    protected void importFinish(ImportConfirmation confirmationMode) {
        SWTBotShell shell = fBot.activeShell();
        final SWTBotButton finishButton = fBot.button("Finish");
        finishButton.click();
        if (confirmationMode == ImportConfirmation.RENAME_ALL) {
            SWTBotShell shell2 = fBot.shell("Confirmation").activate();
            SWTBotButton button = shell2.bot().button("Rename All");
            button.click();
        } else if (confirmationMode == ImportConfirmation.OVERWRITE_ALL) {
            SWTBotShell shell2 = fBot.shell("Confirmation").activate();
            SWTBotButton button = shell2.bot().button("Overwrite All");
            button.click();
        }
        fBot.waitUntil(Conditions.shellCloses(shell), IMPORT_TIME_OUT);
        WaitUtils.waitForJobs();
    }

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
     *
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

        WaitUtils.waitForJobs();
        SWTBotUtils.delay(1000);

        final CtfTmfEvent desiredEvent2 = getEvent(10000);
        SWTBotView hvBot = fBot.viewById(HistogramView.ID);
        List<SWTBotToolbarButton> hvTools = hvBot.getToolbarButtons();
        for (SWTBotToolbarButton hvTool : hvTools) {
            if (hvTool.getToolTipText().toLowerCase().contains("lost")) {
                hvTool.click();
            }
        }
        HistogramView hv = (HistogramView) vp;
        final TmfSelectionRangeUpdatedSignal signal = new TmfSelectionRangeUpdatedSignal(hv, desiredEvent1.getTimestamp());
        final TmfSelectionRangeUpdatedSignal signal2 = new TmfSelectionRangeUpdatedSignal(hv, desiredEvent2.getTimestamp());
        hv.updateTimeRange(100000);
        WaitUtils.waitForJobs();
        hv.selectionRangeUpdated(signal);
        hv.broadcast(signal);
        WaitUtils.waitForJobs();
        SWTBotUtils.delay(1000);

        hv.updateTimeRange(1000000000);
        WaitUtils.waitForJobs();
        hv.selectionRangeUpdated(signal2);
        hv.broadcast(signal2);
        WaitUtils.waitForJobs();
        SWTBotUtils.delay(1000);
        assertNotNull(hv);
    }

    /**
     * Verifies the statistics view
     *
     * @param vp
     *            the view part
     */
    protected void testStatisticsView(IViewPart vp) {
        TmfStatisticsViewImpl sv = (TmfStatisticsViewImpl) vp;
        assertNotNull(sv);
    }

    // ---------------------------------------------
    // Trace helpers
    // ---------------------------------------------

    /**
     * Gets an event at a given rank
     *
     * @param rank
     *            a rank
     * @return the event at given rank
     */
    protected CtfTmfEvent getEvent(int rank) {
        ITmfContext ctx = fTrace.seekEvent(rank);
        CtfTmfEvent ret = fTrace.getNext(ctx);
        ctx.dispose();
        return ret;
    }

    /**
     * Gets a view part based on view title
     *
     * @param viewTile
     *            a view title
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
