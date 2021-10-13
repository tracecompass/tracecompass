/*******************************************************************************
 * Copyright (c) 2021 Ericsson
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.swt.finder.SWTBot;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.Test;

/**
 * Test Help messages
 *
 * @author Matthew Khouzam
 */
public class HelpMessageTest extends KernelTestBase {

    private static final String TRACES = "Traces";
    private static final String VIEWS = "Views";
    private static final String TRACE_NAME = "synthetic-trace";
    private static final String PROJECT_NAME = "test";
    private static final Map<String, String> ANALYSES_TO_TEST = new HashMap<>();

    static {
        ANALYSES_TO_TEST.put("Active Thread", "This analysis runs to accelerate all other analyses. It is used by the events table to fill the TID column.");
        ANALYSES_TO_TEST.put("Context switch", "Store the amount of times a thread was scheduled in or out. This helps show thread thrashing.");
        ANALYSES_TO_TEST.put("Counters", "Analysis module: Counters for trace synthetic-trace\n"
                + "\n"
                + "Cannot perform analysis \"Counters\" on this trace because the trace does not have the required characteristics");
        ANALYSES_TO_TEST.put("CPU usage", "Analysis module: CPU usage for trace synthetic-trace");
        ANALYSES_TO_TEST.put("Futex Contention Analysis", "Analysis module: Futex Contention Analysis for trace synthetic-trace");
        ANALYSES_TO_TEST.put("Input/Output", "Analysis module: Input/Output for trace synthetic-trace");
        ANALYSES_TO_TEST.put("IRQ Analysis", "Analysis module: IRQ Analysis for trace synthetic-trace");
        ANALYSES_TO_TEST.put("Kernel memory usage", "Analysis module: Kernel memory usage for trace synthetic-trace");
        ANALYSES_TO_TEST.put("OS Execution Graph", "Analysis module: OS Execution Graph for trace synthetic-trace");
        ANALYSES_TO_TEST.put("Source Code Assistant", "Analysis module: Source Code Assistant for trace synthetic-trace");
        ANALYSES_TO_TEST.put("Statistics", "Analysis module: Statistics for trace synthetic-trace");
        ANALYSES_TO_TEST.put("System Call Latency", "Analysis module: System Call Latency for trace synthetic-trace");
    }

    /**
     * Test the help menus
     */
    @Test
    public void testHelpMenus() {
        SWTWorkbenchBot bot = fBot;
        bot.viewByTitle("Project Explorer").show();

        SWTBotTreeItem projectNode = SWTBotUtils.selectProject(fBot, PROJECT_NAME);
        projectNode.expand();
        SWTBotTreeItem traceNode = SWTBotUtils.getTraceProjectItem(fBot, projectNode, TRACES, TRACE_NAME);
        traceNode.expand();
        SWTBotTreeItem node = traceNode.getNode(VIEWS);
        node.select();
        node.expand();

        for (Entry<String, String> entry : ANALYSES_TO_TEST.entrySet()) {
            SWTBotTreeItem analysisNode = node.getNode(entry.getKey());
            analysisNode.select();
            analysisNode.contextMenu("Help").click();
            SWTBotShell shell = fBot.shell("Help");
            shell.activate();
            SWTBot helpShellBot = shell.bot();
            assertEquals("Help", helpShellBot.canvas().getText());
            assertEquals(entry.getValue(), helpShellBot.label(1).getText());
            helpShellBot.button().click();
            SWTBotUtils.waitUntil(shellBot -> !shellBot.isOpen(), shell, "Help Dialog did not close.");
        }
    }

}
