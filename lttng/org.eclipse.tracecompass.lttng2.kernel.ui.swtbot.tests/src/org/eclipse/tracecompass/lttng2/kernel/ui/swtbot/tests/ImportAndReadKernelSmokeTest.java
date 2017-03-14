/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
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

import static org.eclipse.swtbot.swt.finder.waits.Conditions.treeHasRows;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.matchers.WidgetMatcherFactory;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.finders.UIThreadRunnable;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.results.VoidResult;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotToolbarButton;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowView;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.resources.ResourcesView;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.eclipse.tracecompass.tmf.ui.editors.TmfEventsEditor;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.eclipse.tracecompass.tmf.ui.views.histogram.HistogramView;
import org.eclipse.tracecompass.tmf.ui.views.statesystem.TmfStateSystemExplorer;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.hamcrest.Matcher;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.common.collect.ImmutableSet;

/**
 * SWTBot Smoke test for LTTng Kernel UI.
 *
 * @author Matthew Khouzam
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ImportAndReadKernelSmokeTest extends KernelTestBase {

    private static final @NonNull Map<String, Set<String>> EXPECTED_ANALYSES = new HashMap<>();

    static {
        EXPECTED_ANALYSES.put("Input/Output", Collections.singleton("org.eclipse.tracecompass.analysis.os.linux.inputoutput"));
        EXPECTED_ANALYSES.put("Tmf Statistics", ImmutableSet.of("org.eclipse.linuxtools.tmf.statistics.totals", "org.eclipse.linuxtools.tmf.statistics.types"));
        EXPECTED_ANALYSES.put("Active Thread", Collections.singleton("org.eclipse.tracecompass.analysis.os.linux.kernel.tid"));
        EXPECTED_ANALYSES.put("Linux Kernel", Collections.singleton("org.eclipse.tracecompass.analysis.os.linux.kernel"));
        EXPECTED_ANALYSES.put("Context switch", Collections.singleton("org.eclipse.tracecompass.analysis.os.linux.contextswitch"));
        EXPECTED_ANALYSES.put("Kernel memory usage", Collections.singleton("org.eclipse.tracecompass.analysis.os.linux.core.kernelmemory"));
        EXPECTED_ANALYSES.put("CPU usage", Collections.singleton("org.eclipse.tracecompass.analysis.os.linux.cpuusage"));
    }

    private ITmfEvent fDesired1;
    private ITmfEvent fDesired2;

    /**
     * Main test case
     */
    @Test
    public void test() {
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getSyntheticTrace();
        try {
            Matcher<IEditorReference> matcher = WidgetMatcherFactory.withPartName(trace.getName());
            IEditorPart iep = fBot.editor(matcher).getReference().getEditor(true);
            final TmfEventsEditor tmfEd = (TmfEventsEditor) iep;

            fDesired1 = getEvent(trace, 100);
            fDesired2 = getEvent(trace, 10000);

            UIThreadRunnable.syncExec(new VoidResult() {
                @Override
                public void run() {
                    tmfEd.setFocus();
                    tmfEd.selectionChanged(new SelectionChangedEvent(tmfEd, new StructuredSelection(fDesired1)));
                }
            });
            testHV(getViewPart("Histogram"));
            testCFV((ControlFlowView) getViewPart("Control Flow"));
            testRV((ResourcesView) getViewPart("Resources"));
            testStateSystemExplorer(trace.getPath());
        } finally {
            trace.dispose();
        }
    }

    private static void testCFV(ControlFlowView vp) {
        assertNotNull(vp);
    }

    private void testHV(IViewPart vp) {
        SWTBotView hvBot = (new SWTWorkbenchBot()).viewById(HistogramView.ID);
        List<SWTBotToolbarButton> hvTools = hvBot.getToolbarButtons();
        for (SWTBotToolbarButton hvTool : hvTools) {
            if (hvTool.getToolTipText().toLowerCase().contains("lost")) {
                hvTool.click();
            }
        }
        HistogramView hv = (HistogramView) vp;
        final TmfSelectionRangeUpdatedSignal signal = new TmfSelectionRangeUpdatedSignal(hv, fDesired1.getTimestamp());
        final TmfSelectionRangeUpdatedSignal signal2 = new TmfSelectionRangeUpdatedSignal(hv, fDesired2.getTimestamp());
        hvBot.close();
        hv = (HistogramView) UIThreadRunnable.syncExec(() -> {
            try {
                return (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView(HistogramView.ID));
            } catch (PartInitException e) {
                // Do nothing, returning null fails
            }
            return null;
        });
        assertNotNull(hv);
        hvBot = (new SWTWorkbenchBot()).viewById(HistogramView.ID);
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

    private static void testRV(ResourcesView vp) {
        assertNotNull(vp);
    }

    private static CtfTmfEvent getEvent(CtfTmfTrace trace, int rank) {
        ITmfContext ctx = trace.seekEvent(0);
        for (int i = 0; i < rank; i++) {
            trace.getNext(ctx);
        }
        return trace.getNext(ctx);
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

    private static void testStateSystemExplorer(String tracePath) {
        // Set up
        SWTWorkbenchBot bot = new SWTWorkbenchBot();
        // Open the view
        SWTBotUtils.openView(TmfStateSystemExplorer.ID);
        SWTBotView sseBot = bot.viewByTitle("State System Explorer");
        sseBot.show();
        // get the list and compare it.
        List<String> actual = getSSNames(sseBot);
        assertTrue("State systems are not what expected : " + EXPECTED_ANALYSES.keySet() + " instead " + actual, actual.containsAll(EXPECTED_ANALYSES.keySet()));
        // Re-open the view and make sure it has the same results
        sseBot.close();
        SWTBotUtils.openView(TmfStateSystemExplorer.ID);
        sseBot = bot.viewByTitle("State System Explorer");
        sseBot.show();
        List<String> secondOpen = getSSNames(sseBot);
        assertEquals("Test reopen view", actual, secondOpen);
        // Close the trace, and re-open it, let's compare one last time
        bot.closeAllEditors();
        bot.waitUntil(treeHasRows(sseBot.bot().tree(), 0));
        // re-open the trace
        SWTBotUtils.openTrace(TRACE_PROJECT_NAME, tracePath, KERNEL_TRACE_TYPE);
        secondOpen = getSSNames(sseBot);
        assertEquals("Test reopen trace", actual, secondOpen);
        sseBot.close();
    }

    private static List<String> getSSNames(SWTBotView bot) {
        SWTBotTree tree = bot.bot().tree();
        bot.bot().waitWhile(treeHasRows(tree, 0));
        SWTBotTreeItem item = tree.getTreeItem("synthetic-trace").expand();
        List<String> strings = new ArrayList<>();
        strings.addAll(item.getNodes());
        // The items may be out of order, is this a bug?
        strings.sort(null);
        return strings;
    }

}
