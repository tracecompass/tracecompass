/*******************************************************************************
 * Copyright (c) 2016, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.AbstractTimeGraphView;
import org.eclipse.ui.IWorkbenchPart;
import org.junit.Before;
import org.junit.Test;

/**
 * SWTBot tests for Critical Flow view. This test also tests the control flow
 * view's thread selection as the two views are tightly coupled.
 *
 * @author Matthew Khouzam
 */
public class CriticalPathTest extends KernelTestBase {

    private static final int TID_NO = 338;
    private static final String TID = String.valueOf(TID_NO);
    private static final String PROCESS = "weston";
    private static final int TID_NO2 = 340;
    private static final String TID2 = String.valueOf(TID_NO2);
    private static final String PROCESS2 = "weston-desktop-";
    private static final @NonNull String CP_ID = "org.eclipse.linuxtools.tmf.analysis.graph.ui.criticalpath.view.criticalpathview";
    private static final String KWORKER_PROCESS = "kworker/u16:0";
    private static final String CRIT_PATH_MAIN_ENTRY = "[" + PROCESS + "," + TID + "]";
    private static final String CRIT_PATH_OTHER_ENTRY = "[" + KWORKER_PROCESS + ",6]";
    private static final String CRIT_PATH_MAIN_ENTRY2 = "[" + PROCESS2 + "," + TID2 + "]";

    private static final String FOLLOW_FORWARD = "Follow critical path forward";
    private static final String FOLLOW_BACKWARD = "Follow critical path backward";
    private static final @NonNull ITmfTimestamp CPU_TIME0 = TmfTimestamp.fromNanos(1412670963793647239L);
    private static final @NonNull ITmfTimestamp CPU_TIME1 = TmfTimestamp.fromNanos(1412670963793673139L);
    private SWTBotView fViewBotCfv;
    private SWTBotView fViewBotCp;

    /**
     * Before Test
     */
    @Override
    @Before
    public void before() {
        try {
            String tracePath = FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL())).getAbsolutePath();
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, tracePath, KERNEL_TRACE_TYPE);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        SWTBotUtils.activateEditor(fBot, "bug446190");
        fViewBotCfv = fBot.viewById(ControlFlowView.ID);
        SWTBotUtils.openView(CP_ID);
        fViewBotCp = fBot.viewById(CP_ID);
        fViewBotCp.show();
        fViewBotCfv.show();
        fViewBotCfv.setFocus();
    }

    /**
     * test the behavior of the critical path for a thread selection signal from
     * the control flow view
     */
    @Test
    public void testFull() {
        SWTBotTimeGraph timeGraphCfv = new SWTBotTimeGraph(fViewBotCfv.bot());
        SWTBotTree treeCp = fViewBotCp.bot().tree();
        SWTBotTimeGraph timeGraphCp = new SWTBotTimeGraph(fViewBotCp.bot());
        assertNotNull(timeGraphCfv.widget);
        assertNotNull(treeCp.widget);
        SWTBotTreeItem[] allItems = treeCp.getAllItems();
        for (int i = 0; i < allItems.length; i++) {
            assertEquals(0, allItems[i].getNodes().size());
        }

        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(trace);
        SWTBotTimeGraphEntry entry = timeGraphCfv.getEntry(trace.getName(), "systemd", "we", PROCESS);
        assertNotNull(entry);
        entry.select();

        SWTBotMenu menu = entry.contextMenu("Follow " + PROCESS + "/" + TID);
        assertEquals("Follow " + PROCESS + "/" + TID, menu.getText());
        menu.click();
        fBot.waitUntil(new DefaultCondition() {

            @Override
            public boolean test() throws Exception {
                SWTBotTimeGraphEntry[] entries = timeGraphCp.getEntries();
                return CRIT_PATH_MAIN_ENTRY.equals(entries[0].getEntries()[0].getText());
            }

            @Override
            public String getFailureMessage() {
                return "Could not find " + CRIT_PATH_MAIN_ENTRY + " in Critical Path view";
            }
        });

        // Test navigating the critical path view with the follow arrows buttons
        IWorkbenchPart part = fViewBotCp.getViewReference().getPart(false);
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, CPU_TIME0));
        SWTBotTimeGraphEntry critPathEntry = timeGraphCp.getEntry(trace.getHostId(), CRIT_PATH_MAIN_ENTRY);
        critPathEntry.select();

        // Condition to wait for the time graph view to refresh
        ICondition timeGraphIsReadyCondition = ConditionHelpers.timeGraphIsReadyCondition((AbstractTimeGraphView) part, new TmfTimeRange(CPU_TIME1, CPU_TIME1), CPU_TIME1);
        // Reach the end of the current event
        fViewBotCp.toolbarButton(FOLLOW_FORWARD).click();
        fBot.waitUntil(timeGraphIsReadyCondition);
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraphCp, 0, CRIT_PATH_MAIN_ENTRY));
        // Follow the arrow down to the next item
        fViewBotCp.toolbarButton(FOLLOW_FORWARD).click();
        fBot.waitUntil(timeGraphIsReadyCondition);
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraphCp, 0, CRIT_PATH_OTHER_ENTRY));
        // Make sure changing the selection changed the selection in CFV too
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraphCfv, 0, KWORKER_PROCESS));
        // Follow it back up
        fViewBotCp.toolbarButton(FOLLOW_BACKWARD).click();
        fBot.waitUntil(timeGraphIsReadyCondition);
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraphCp, 0, CRIT_PATH_MAIN_ENTRY));
        // Make sure changing the selection changed the selection in CFV too
        fBot.waitUntil(ConditionHelpers.timeGraphSelectionContains(timeGraphCfv, 0, PROCESS));

        // Follow another process and make sure the critical path changes
        entry = timeGraphCfv.getEntry(trace.getName(), "systemd", "we", PROCESS, PROCESS2);
        assertNotNull(entry);
        entry.select();

        menu = entry.contextMenu("Follow " + PROCESS2 + "/" + TID2);
        assertEquals("Follow " + PROCESS2 + "/" + TID2, menu.getText());
        menu.click();
        fBot.waitUntil(new DefaultCondition() {

            @Override
            public boolean test() throws Exception {
                SWTBotTimeGraphEntry[] entries = timeGraphCp.getEntries();
                return CRIT_PATH_MAIN_ENTRY2.equals(entries[0].getEntries()[0].getText());
            }

            @Override
            public String getFailureMessage() {
                return "Could not find " + CRIT_PATH_MAIN_ENTRY2 + " in Critical Path view";
            }
        });
    }

}
