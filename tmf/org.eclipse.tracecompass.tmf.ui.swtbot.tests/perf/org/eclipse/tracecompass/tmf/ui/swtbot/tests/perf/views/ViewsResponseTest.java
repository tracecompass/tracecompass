/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.swtbot.tests.perf.views;

import java.io.File;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.waits.Conditions;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;

/**
 * Base class to test the responsiveness of any views. The main method of this
 * class, {@link #runTestWithTrace(String, String, Collection)}, will receive a
 * collection of view IDs. For each view, the trace under test will be navigated
 * when only the view is opened (closing all other listed views, all other
 * opened views will remain opened) and also when all the views are opened
 * simultaneously. In this last case, it will rename the trace so that events
 * from when all the views are opened can be listed separately.
 *
 * @author Geneviève Bastien
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public abstract class ViewsResponseTest {

    private static final String PROJECT_NAME = "test";

    private final @NonNull SWTWorkbenchBot fBot = new SWTWorkbenchBot();

    /**
     * Specific tests will prepare the workspace for the run. For example,
     * concrete classes can open perspectives, open views, prepare the layout,
     * etc.
     */
    protected abstract void prepareWorkspace();

    /**
     * Things to setup
     */
    @Before
    public void beforeClass() {

        SWTBotUtils.initialize();
        Thread.currentThread().setName("SWTBotTest");
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 60000; /* 60 second timeout */
        SWTBotPreferences.KEYBOARD_LAYOUT = "EN_US";
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        SWTBotUtils.closeView("welcome", bot);
        /* Prepare the workspace */
        prepareWorkspace();
        /* Finish waiting for eclipse to load */
        WaitUtils.waitForJobs();

        /* Create project */
        SWTBotUtils.createProject(PROJECT_NAME);
    }

    /**
     * Deletes the project from the workspace
     */
    @After
    public void cleanUp() {
        /* Close editors and delete project */
        fBot.closeAllEditors();
        SWTBotUtils.deleteProject(PROJECT_NAME, fBot);
    }

    private void closeAllViews(Collection<String> viewIDs) {
        viewIDs.stream().forEach(id -> {
            SWTBotUtils.openView(id);
            SWTBotUtils.closeViewById(id, fBot);
        });
    }

    /**
     * This method will be run when all views are still close, but the trace has
     * been opened. For instance, if any analysis module need to have completed
     * before the test, it can wait for completion here.
     *
     * @param trace
     *            The trace used for this test
     */
    protected abstract void beforeRunningTest(ITmfTrace trace);

    /**
     * Run this swtbot with the trace specified at the specified path. The trace
     * will be navigate for each view ID separately, then, after renaming the
     * trace, with all the views opened. After this test, all views will be
     * closed.
     *
     * @param tracePath
     *            The full path of the trace to open
     * @param traceType
     *            The trace type of the trace to open
     * @param viewIDs
     *            The IDs of the views to test.
     */
    protected void runTestWithTrace(String tracePath, String traceType, Collection<String> viewIDs) {
        closeAllViews(viewIDs);
        /* Open the trace */
        String traceName = tracePath.substring(tracePath.lastIndexOf(File.separator, tracePath.length() - 2) + 1, tracePath.length() - 1);
        SWTBotUtils.openTrace(PROJECT_NAME, tracePath, traceType);

        // Make sure all the analyses we'll need are done
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        beforeRunningTest(trace);
        WaitUtils.waitForJobs();

        SWTBotView view;

        for (String viewID : viewIDs) {
            SWTBotUtils.openView(viewID);
            view = fBot.viewById(viewID);
            navigateTrace(view);
            SWTBotUtils.closeViewById(viewID, fBot);
        }

        // Close the trace
        fBot.closeAllEditors();

        // If there is only 1 view to test, return
        if (viewIDs.size() <= 1) {
            // Close the views
            closeAllViews(viewIDs);
            return;
        }

        // Open all the views
        view = null;
        for (String viewID : viewIDs) {
            SWTBotUtils.openView(viewID);
            if (view == null) {
                view = fBot.viewById(viewID);
            }
        }

        // Rename the trace, so the results appear under another trace and
        // navigate it
        renameTrace(traceName, traceName + " full");
        navigateTrace(view);

        // Close the trace
        fBot.closeAllEditors();

        // Close the views
        closeAllViews(viewIDs);
    }

    private void renameTrace(String oldName, String newName) {

        SWTBotTreeItem traceItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME), oldName);

        traceItem.contextMenu().menu("Rename...").click();
        final String RENAME_TRACE_DIALOG_TITLE = "Rename Trace";
        fBot.waitUntil(Conditions.shellIsActive(RENAME_TRACE_DIALOG_TITLE));
        SWTBotShell shell = fBot.shell(RENAME_TRACE_DIALOG_TITLE);
        SWTBotText text = shell.bot().textWithLabel("New Trace name:");
        text.setText(newName);
        shell.bot().button("OK").click();
        fBot.waitUntil(Conditions.shellCloses(shell));
        fBot.waitWhile(new ConditionHelpers.ActiveEventsEditor(fBot, null));

        SWTBotTreeItem copiedItem = SWTBotUtils.getTraceProjectItem(fBot, SWTBotUtils.selectTracesFolder(fBot, PROJECT_NAME), newName);
        copiedItem.contextMenu().menu("Open").click();
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
        }
        WaitUtils.waitForJobs();
    }

    // TODO: Add some vertical scrollings. With eventual 2D queries, that will
    // be something to test as well
    private void navigateTrace(SWTBotView view) {
        TmfTimeRange originalWindowRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
        TmfTimeRange selectionRange = TmfTraceManager.getInstance().getCurrentTraceContext().getSelectionRange();
        IWorkbenchPart part = view.getViewReference().getPart(false);

        // Set the time range to the full trace range
        ITmfTrace activeTrace = TmfTraceManager.getInstance().getActiveTrace();
        TmfTimeRange fullRange = new TmfTimeRange(activeTrace.getStartTime(), activeTrace.getEndTime());
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, fullRange));
        waitViewReady(part, selectionRange, fullRange.getEndTime());
        TmfTimeRange windowRange = fullRange;

        // Zoom in 10 times 15 percent of the range and wait for the view to be
        // ready
        for (int i = 0; i < 10; i++) {
            double delta = (windowRange.getEndTime().getValue() - windowRange.getStartTime().getValue()) * 0.15;
            TmfTimeRange newWindowRange = new TmfTimeRange(TmfTimestamp.fromNanos((long) (windowRange.getStartTime().toNanos() + delta)), TmfTimestamp.fromNanos((long) (windowRange.getEndTime().toNanos() - delta)));
            TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, newWindowRange));
            windowRange = newWindowRange;
            waitViewReady(part, selectionRange, newWindowRange.getEndTime());
        }

        // At this zoom level, go to the end
        long scrollTime = (windowRange.getEndTime().toNanos() - windowRange.getStartTime().toNanos()) / 2;
        windowRange = new TmfTimeRange(TmfTimestamp.fromNanos(fullRange.getEndTime().toNanos() - (2 * scrollTime)), fullRange.getEndTime());
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, windowRange));
        waitViewReady(part, selectionRange, windowRange.getEndTime());

        // Scroll back horizontally half the range at a time
        for (int i = 0; i < 10; i++) {
            TmfTimeRange newWindowRange = new TmfTimeRange(TmfTimestamp.fromNanos(windowRange.getStartTime().toNanos() - scrollTime), TmfTimestamp.fromNanos(windowRange.getEndTime().toNanos() - scrollTime));
            TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, newWindowRange));
            windowRange = newWindowRange;
            waitViewReady(part, selectionRange, newWindowRange.getEndTime());
        }

        // then go all the way back to the beginning
        windowRange = new TmfTimeRange(fullRange.getStartTime(), TmfTimestamp.fromNanos(fullRange.getStartTime().toNanos() + scrollTime));
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, windowRange));
        waitViewReady(part, selectionRange, windowRange.getEndTime());

        // and zoom out again
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, fullRange));
        waitViewReady(part, selectionRange, fullRange.getEndTime());

        // Reset the original window range
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, originalWindowRange));
        waitViewReady(part, selectionRange, originalWindowRange.getEndTime());
    }

    private void waitViewReady(IWorkbenchPart part, @NonNull TmfTimeRange selectionRange, @NonNull ITmfTimestamp visibleTime) {
        if (part instanceof AbstractTimeGraphView) {
            fBot.waitUntil(ConditionHelpers.timeGraphIsReadyCondition((AbstractTimeGraphView) part, selectionRange, visibleTime));
        }
        // TODO Add conditions for other kind of views
    }

}
