/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for the Control view in Trace Compass. This will test the enabling
 * Kernel and Syscalls at the same time.
 *
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ControlViewAllEventsTest extends ControlViewTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateSessionTestLTTng2_8.cfg";
    private static final String CREATE_SESSION_KERNEL_SCENARIO_NAME = "CreateSessionAllKernelEvents";

    private static final String SESSION_NAME = "mysession";

    @Override
    protected String getTestStream() {
        return TEST_STREAM;
    }

    @Override
    protected String getSessionName() {
        return SESSION_NAME;
    }

    @Override
    @Test
    public void testTraceSessionTree() {

        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(INIT_SCENARIO_NAME);

        testConnectToNode();
        // Prepare for saving of profile
        fProxy.setScenario(CREATE_SESSION_KERNEL_SCENARIO_NAME);
        testCreateSession();
        testEnableKernelEvent();
        testStartStopTracing(TraceSessionState.ACTIVE);
        testStartStopTracing(TraceSessionState.INACTIVE);
        testDestroySession();
        testDisconnectFromNode();
    }

    @Override
    protected void testEnableKernelEvent() {
        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName());

        sessionItem.select();
        SWTBotMenu menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();
        // all tracepoint events and syscalls
        shell.bot().radioInGroup(ControlViewSwtBotUtil.GROUP_SELECT_NAME, ControlViewSwtBotUtil.ALL_EVENT_GROUP_NAME).click();
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        SWTBotUtils.waitForJobs();

        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME, sessionItem));

        SWTBotTreeItem channelItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME,
                ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME);
        assertEquals(ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME, channelItem.getText());

        SWTBotTreeItem eventItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME,
                ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME,
                ControlViewSwtBotUtil.ALL_EVENTS_NAME);
        assertEquals(ControlViewSwtBotUtil.ALL_EVENTS_NAME, eventItem.getText());
    }
}