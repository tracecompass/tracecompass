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

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceEventComponent;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test for the Control view in Trace Compass. This will test the exclude events feature.
 *
 * @author Bruno Roy
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ControlViewExcludeEventsTest extends ControlViewTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateSessionTestLTTng2_8.cfg";

    private static final String CREATE_SESSION_UST_EXCLUDE_SCENARIO_NAME = "ExcludeEvent";

    private static final String SESSION_NAME = "mysession";
    private static final String EXCLUDE_EXPRESSION = "foo";
    private static final String PROPERTIES_VIEW = "Properties";
    private static final String EXCLUDE_TREE_ITEM = "Exclude";

    @Override
    protected String getTestStream() {
        return TEST_STREAM;
    }

    @Override
    protected String getSessionName() {
        return SESSION_NAME;
    }

    /**
     * Testing the trace session tree.
     */
    @Override
    @Test
    public void testTraceSessionTree() {

        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(INIT_SCENARIO_NAME);
        testConnectToNode();

        // Enable all UST events with one excluded event
        fProxy.setScenario(CREATE_SESSION_UST_EXCLUDE_SCENARIO_NAME);
        testCreateSession();
        testEnableUstEventExclude();
        testStartStopTracing(TraceSessionState.ACTIVE);
        testStartStopTracing(TraceSessionState.INACTIVE);
        // Verify that the Properties view shows to right excluded event
        testPropertiesEventExclude();
        // Clean session
        testDestroySession();
        testDisconnectFromNode();
    }

    /**
     * Enable all UST events with one excluded event.
     */
    protected void testEnableUstEventExclude() {
        // Getting the 'Sessions' tree
        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName());
        sessionItem.select();

        // Clicking on the 'Enable Event (default channel)...'
        SWTBotMenu menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();

        // Switching to the UST domain
        shell.bot().radioInGroup(ControlViewSwtBotUtil.UST_GROUP_NAME, ControlViewSwtBotUtil.DOMAIN_GROUP_NAME).click();

        // Selecting all UST events
        SWTBotTree tracepointsTree = shell.bot().treeInGroup(ControlViewSwtBotUtil.TRACEPOINTS_GROUP_NAME);
        SWTBotTreeItem treeItem = tracepointsTree.getTreeItem(ControlViewSwtBotUtil.ALL_TREE_NODE);
        treeItem.check();

        // Click the checkbox for the Exclude event
        shell.bot().checkBoxInGroup(ControlViewSwtBotUtil.GROUP_SELECT_NAME, ControlViewSwtBotUtil.EXCLUDE_EVENT_LABEL).click();

        // Enter the event to exclude in the text field
        SWTBotText excludeText = shell.bot().textInGroup(ControlViewSwtBotUtil.EXCLUDE_EVENT_LABEL);
        excludeText.setText(EXCLUDE_EXPRESSION);

        // Click the Ok at the bottom of the dialog window
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();

        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(ControlViewSwtBotUtil.UST_DOMAIN_NAME, sessionItem));

        // Assert that the domain is UST global
        SWTBotTreeItem ustGlobalDomainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.UST_DOMAIN_NAME);
        assertEquals(ControlViewSwtBotUtil.UST_DOMAIN_NAME, ustGlobalDomainItem.getText());

        // Assert that the new channel name is channel0 (which is the default name)
        SWTBotTreeItem channelItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.UST_DOMAIN_NAME,
                ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME);
        assertEquals(ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME, channelItem.getText());

        // Assert that the event type in the channel node are correct (all events = *)
        SWTBotTreeItem eventItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.UST_DOMAIN_NAME,
                ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME,
                ControlViewSwtBotUtil.ALL_EVENTS_NAME);
        assertEquals(ControlViewSwtBotUtil.ALL_EVENTS_NAME, eventItem.getText());

        // Assert that the excluded event is the correct one
        ITraceControlComponent comp = ControlViewSwtBotUtil.getComponent(fNode,
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.UST_DOMAIN_NAME,
                ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME,
                ControlViewSwtBotUtil.ALL_EVENTS_NAME);
        assertNotNull(comp);
        assertTrue(comp instanceof TraceEventComponent);
        TraceEventComponent event = (TraceEventComponent) comp;
        assertEquals(EXCLUDE_EXPRESSION, event.getExcludedEvents());
    }

    /**
     * Test that the Properties view has been update and shows the the right
     * information.
     */
    protected void testPropertiesEventExclude() {
        // Open the properties view (by id)
        SWTBotUtils.openView("org.eclipse.ui.views.PropertySheet");

        // Select the event in the Control view
        fBot.viewById(ControlView.ID).show();
        SWTBotTreeItem eventItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.UST_DOMAIN_NAME,
                ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME,
                ControlViewSwtBotUtil.ALL_EVENTS_NAME);
        eventItem.select();

        // Get a bot and open the Properties view
        SWTBotView propertiesViewBot = fBot.viewByTitle(PROPERTIES_VIEW);
        propertiesViewBot.show();

        // Get the Exclude field in the tree
        SWTBotTree propertiesViewTree = propertiesViewBot.bot().tree();
        SWTBotTreeItem excludeTreeItem = propertiesViewTree.getTreeItem(EXCLUDE_TREE_ITEM);
        // We want the VALUE of the 'Exclude' row so the cell index is 1
        String excludeExpression = excludeTreeItem.cell(1);

        // Assert that the expression in the Properties view is the same as
        // the one we entered
        assertEquals(EXCLUDE_EXPRESSION, excludeExpression);

        // Close the Properties view
        SWTBotUtils.closeView(PROPERTIES_VIEW, fBot);
    }

}