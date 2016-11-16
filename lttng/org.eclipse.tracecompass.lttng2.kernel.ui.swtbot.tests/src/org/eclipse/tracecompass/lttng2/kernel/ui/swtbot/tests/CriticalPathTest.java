/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.waits.DefaultCondition;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowView;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraph;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotTimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
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
    private static final @NonNull String CP_ID = "org.eclipse.linuxtools.tmf.analysis.graph.ui.criticalpath.view.criticalpathview";
    private SWTBotView fViewBotCfv;
    private SWTBotView fViewBotCp;

    /**
     * Before Test
     */
    @Override
    @Before
    public void before() {
        try {
            String traceName = Paths.get(FileLocator.toFileURL(CtfTestTrace.ARM_64_BIT_HEADER.getTraceURL()).toURI()).toString();
            SWTBotUtils.openTrace(TRACE_PROJECT_NAME, traceName, KERNEL_TRACE_TYPE);
        } catch (IOException | URISyntaxException e) {
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
        SWTBotTree treeCfv = fViewBotCfv.bot().tree();
        SWTBotTree treeCp = fViewBotCp.bot().tree();
        SWTBotTimeGraph timeGraphCp = new SWTBotTimeGraph(fViewBotCp.bot());
        assertNotNull(treeCfv.widget);
        assertNotNull(treeCp.widget);
        SWTBotTreeItem[] allItems = treeCp.getAllItems();
        for (int i = 0; i < allItems.length; i++) {
            assertEquals(0, allItems[i].getNodes().size());
        }

        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        assertNotNull(trace);
        SWTBotTreeItem entry = treeCfv.expandNode(trace.getName(), "systemd", "we", PROCESS);
        assertNotNull(entry);
        entry.select();

        SWTBotMenu menu = entry.contextMenu("Follow " + PROCESS + "/" + TID);
        assertEquals("Follow " + PROCESS + "/" + TID, menu.getText());
        menu.click();
        fBot.waitUntil(new DefaultCondition() {

            private final String EXPECTED_TREE_TEXT = "[" + PROCESS + "," + TID + "]";

            @Override
            public boolean test() throws Exception {
                SWTBotTimeGraphEntry[] entries = timeGraphCp.getEntries();
                return EXPECTED_TREE_TEXT.equals(entries[0].getEntries()[0].getText());
            }

            @Override
            public String getFailureMessage() {
                return "Could not find " + EXPECTED_TREE_TEXT + " in Critical Path view";
            }
        });
    }
}
