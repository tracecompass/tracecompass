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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotCCombo;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for the Control view in Trace Compass. This will test the save and load feature.
 *
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ControlViewKernelFilterTest extends ControlViewTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateSessionTestLTTng2_8.cfg";
    private static final String CREATE_SESSION_KERNEL_FILTER_SCENARIO_NAME = "KernelFilter";
    private static final String CREATE_SESSION_KERNEL_FILTER_PROVIDER_SCENARIO_NAME = "KernelFilterProvider";

    private static final String SESSION_NAME = "mysession";
    private static final String FILTER_EXPRESSION = "next_tid==1234";
    private static final String FILTER_EXPRESSION_DISPLAY = "foo > 10";

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
        fProxy.setScenario(CREATE_SESSION_KERNEL_FILTER_SCENARIO_NAME);
        testCreateSession();
        testEnableKernelEvent();
        testStartStopTracing(TraceSessionState.ACTIVE);
        testStartStopTracing(TraceSessionState.INACTIVE);
        testDestroySession();
        testDisconnectFromNode();
    }

    /**
     * Test enable events from Kernel provider
     */
    @Test
    public void testTraceSessionTreeKernelProvider() {

        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(INIT_SCENARIO_NAME);

        testConnectToNode();
        // Prepare for saving of profile
        fProxy.setScenario(CREATE_SESSION_KERNEL_FILTER_PROVIDER_SCENARIO_NAME);
        testCreateSession();
        testEnableKernelFromProviderEvent();
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

        shell.bot().radioInGroup(ControlViewSwtBotUtil.GROUP_SELECT_NAME, ControlViewSwtBotUtil.TRACEPOINTS_GROUP_NAME).click();

        SWTBotTree tracepointsTree = shell.bot().tree();
        SWTBotTreeItem allItem = SWTBotUtils.getTreeItem(fBot, tracepointsTree, ControlViewSwtBotUtil.ALL_TREE_NODE);
        allItem.check();

        SWTBotText filterText = shell.bot().textInGroup(ControlViewSwtBotUtil.FILTER_EXPRESSION_LABEL);
        filterText.setText(FILTER_EXPRESSION);
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();

        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME, sessionItem));

        SWTBotTreeItem kernelDomainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME);
        assertEquals(ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME, kernelDomainItem.getText());

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

        ITraceControlComponent comp = ControlViewSwtBotUtil.getComponent(fNode,
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME,
                ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME,
                ControlViewSwtBotUtil.ALL_EVENTS_NAME);
        assertNotNull(comp);
        assertTrue(comp instanceof TraceEventComponent);
        TraceEventComponent event = (TraceEventComponent) comp;
        assertEquals(FILTER_EXPRESSION_DISPLAY, event.getFilterExpression());
    }

    private void testEnableKernelFromProviderEvent() {
        SWTBotTreeItem kernelProviderEventItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.PROVIDER_GROUP_NAME,
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME,
                ControlViewSwtBotUtil.SCHED_SWITCH_EVENT_NAME);

        kernelProviderEventItem.select();
        SWTBotMenu menuBot = kernelProviderEventItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();
        SWTBotCCombo sessionCombo = shell.bot().ccomboBoxInGroup(ControlViewSwtBotUtil.SESSION_LIST_GROUP_NAME);
        sessionCombo.setSelection(getSessionName());
        SWTBotText filterText = shell.bot().textInGroup(ControlViewSwtBotUtil.FILTER_EXPRESSION_LABEL);
        filterText.setText(FILTER_EXPRESSION);
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();

        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName());

        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME, sessionItem));

        SWTBotTreeItem kernelDomainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME);
        assertEquals(ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME, kernelDomainItem.getText());

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
                ControlViewSwtBotUtil.SCHED_SWITCH_EVENT_NAME);
        assertEquals(ControlViewSwtBotUtil.SCHED_SWITCH_EVENT_NAME, eventItem.getText());

        ITraceControlComponent comp = ControlViewSwtBotUtil.getComponent(fNode,
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME,
                ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME,
                ControlViewSwtBotUtil.SCHED_SWITCH_EVENT_NAME);
        assertNotNull(comp);
        assertTrue(comp instanceof TraceEventComponent);
        TraceEventComponent event = (TraceEventComponent) comp;
        assertEquals(FILTER_EXPRESSION_DISPLAY, event.getFilterExpression());
    }

}