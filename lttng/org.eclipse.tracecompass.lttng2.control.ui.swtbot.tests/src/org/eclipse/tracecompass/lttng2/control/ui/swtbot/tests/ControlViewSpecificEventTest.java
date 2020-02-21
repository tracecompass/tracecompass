/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.List;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for the Control view in Trace Compass. This will test the enabling
 * of specific kernel event(s) by name.
 *
 * @author Bruno Roy
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ControlViewSpecificEventTest extends ControlViewTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateSessionTestLTTng2_8.cfg";

    private static final String CREATE_SESSION_SPECIFIC_KERNEL_EVENT_SCENARIO_NAME = "CreateSessionSpecificKernelEvent";

    private static final String SESSION_NAME = "mysession";

    /**
     * Get the test stream file name to use for the test suite
     *
     * @return the name of the test stream file
     */
    @Override
    protected String getTestStream() {
        return TEST_STREAM;
    }

    /**
     * Get the session name
     *
     * @return the session name for this test
     */
    @Override
    protected String getSessionName() {
        return SESSION_NAME;
    }

    @Override
    @Test
    public void testTraceSessionTree() {

        // Initialize scenario
        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(INIT_SCENARIO_NAME);

        testConnectToNode();

        // Creating a session by specifying an event scenario
        fProxy.setScenario(CREATE_SESSION_SPECIFIC_KERNEL_EVENT_SCENARIO_NAME);
        // Create a session
        testCreateSession();
        // Enable an event by specifying the event type
        testEnableSpecificKernelEvent(Arrays.asList(ControlViewSwtBotUtil.SCHED_SWITCH_EVENT_NAME), false, true);
        // Enable multiple events by specifying the event type
        testEnableSpecificKernelEvent(Arrays.asList(ControlViewSwtBotUtil.SCHED_WAKEUP_EVENT_NAME, ControlViewSwtBotUtil.SCHED_PROCESSWAIT_EVENT_NAME), false, true);
        // Enable an event by specifying the event type and selection in tree (duplication of name)
        testEnableSpecificKernelEvent(Arrays.asList(ControlViewSwtBotUtil.SCHED_PROCESSFORK_EVENT_NAME), true, true);
        // Enable an event using the tree only
        testEnableSpecificKernelEvent(Arrays.asList(ControlViewSwtBotUtil.SCHED_PROCESSEXEC_EVENT_NAME), true, false);
        // Enable all events using tree. It will ignore what is written in the specific event text box.
        testEnableSpecificKernelEvent(Arrays.asList(ControlViewSwtBotUtil.ALL_TREE_NODE), true, true);

        // Start, stop tracing then destroy the session and diconnect
        testStartStopTracing(TraceSessionState.ACTIVE);
        testStartStopTracing(TraceSessionState.INACTIVE);
        testDestroySession();
        testDisconnectFromNode();
    }

    /**
     * Test for enabling an event type by specifying its name
     *
     * @param events
     *          event names to enable
     * @param selectInTree
     *          select event names also in tree
     *          Note: using All as name then all tree node is selected
     * @param useNameField
     *          use event name field
     *
     */
    private void testEnableSpecificKernelEvent(List<String> events, boolean selectInTree, boolean useNameField) {
        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName());

        sessionItem.select();
        SWTBotMenu menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();

        // Select specific event radio button in the Specific event group
        shell.bot().radioInGroup(ControlViewSwtBotUtil.GROUP_SELECT_NAME, ControlViewSwtBotUtil.TRACEPOINTS_GROUP_NAME).click();

        if (selectInTree) {
            SWTBotTree tracepointsTree = shell.bot().tree();
            for (String event : events) {
                if (event.equals(ControlViewSwtBotUtil.ALL_TREE_NODE)) {
                    SWTBotTreeItem treeItem = tracepointsTree.getTreeItem(ControlViewSwtBotUtil.ALL_TREE_NODE);
                    treeItem.check();
                    break;
                }
                tracepointsTree.expandNode(ControlViewSwtBotUtil.ALL_TREE_NODE);
                // select specific
                SWTBotTreeItem treeItem = SWTBotUtils.getTreeItem(fBot, tracepointsTree, ControlViewSwtBotUtil.ALL_TREE_NODE, event);
                treeItem.check();
            }
        }

        // Enters the event type in the text field Event type in the Specific event group
        if (useNameField) {
            SWTBotText specificEventText = shell.bot().textInGroup(ControlViewSwtBotUtil.SPECIFIC_EVENT_GROUP_NAME);
            String suffix = "";
            StringBuffer buffer = new StringBuffer();
            for (String event : events) {
                buffer.append(suffix).append(event);
                // append comma and space to test removal of space
                suffix = ", ";
            }
            specificEventText.setText(buffer.toString());
        }

        // Click Ok to quit the dialog window
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();

        // Wait until the child of Sessions is activated
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable(ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME, sessionItem));

        // Assert that the new channel name is channel0 (which is the default name)
        SWTBotTreeItem channelItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME,
                ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME);
        assertEquals(ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME, channelItem.getText());

        channelItem.expand();

        for (String event : events) {
            // Wait until the child of Sessions is activated
            String eventName = event;
            if (event.equals(ControlViewSwtBotUtil.ALL_TREE_NODE)) {
                eventName = ControlViewSwtBotUtil.ALL_EVENTS_NAME;
            }
            fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable(eventName, channelItem));
            // Assert that the event type in the channel node are correct
            SWTBotTreeItem eventItem = SWTBotUtils.getTreeItem(fBot, fTree,
                    getNodeName(),
                    ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                    getSessionName(),
                    ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME,
                    ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME,
                    eventName);
            assertEquals(eventName, eventItem.getText());
        }

    }
}
