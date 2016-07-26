/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.swtbot.tests;

import static org.junit.Assert.assertEquals;

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.junit.Test;

/**
 * Test for the Control view in Trace Compass. This will test the JUL loggers.
 *
 * @author Bruno Roy
 */
public class ControlViewJulLoggerTest extends ControlViewTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateSessionTestLTTng2_8.cfg";
    private static final String CREATE_SESSION_JUL_SCENARIO_NAME = "JulLogger";

    private static final String SESSION_NAME = "mysession";
    private static final String PROPERTIES_VIEW = "Properties";
    private static final String LOGLEVEL_PROPERTY_NAME = "Log Level";

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

        // Enable all JUL loggers
        fProxy.setScenario(CREATE_SESSION_JUL_SCENARIO_NAME);
        testCreateSession();
        testEnableJulLoggers();
        testStartStopTracing(TraceSessionState.ACTIVE);
        testStartStopTracing(TraceSessionState.INACTIVE);

        // Verify that the Properties view shows to right logger log level
        testLoggerProperties();

        // Clean session
        testDestroySession();
        testDisconnectFromNode();
    }

    /**
     * Enable JUL loggers with different log level and log level type
     */
    protected void testEnableJulLoggers() {
        // Case 1: Enabling all loggers
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

        // Switching to the JUL domain
        shell.bot().radioInGroup(ControlViewSwtBotUtil.JUL_DOMAIN_NAME, ControlViewSwtBotUtil.DOMAIN_GROUP_NAME).click();

        // Selecting all JUL loggers
        SWTBotTree loggersTree = shell.bot().treeInGroup(ControlViewSwtBotUtil.LOGGERS_GROUP_NAME);
        SWTBotTreeItem treeItem = loggersTree.getTreeItem(ControlViewSwtBotUtil.ALL_TREE_NODE);
        treeItem.check();

        // Click the Ok at the bottom of the dialog window
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        SWTBotUtils.waitForJobs();
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(ControlViewSwtBotUtil.JUL_DOMAIN_NAME, sessionItem));

        // Assert that the domain is JUL
        SWTBotTreeItem julDomainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.JUL_DOMAIN_NAME);
        assertEquals(ControlViewSwtBotUtil.JUL_DOMAIN_NAME, julDomainItem.getText());

        // Assert that the logger type in the domain node are correct (all events = *)
        SWTBotTreeItem loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.JUL_DOMAIN_NAME,
                ControlViewSwtBotUtil.ALL_EVENTS_NAME);
        assertEquals(ControlViewSwtBotUtil.ALL_EVENTS_NAME, loggerItem.getText());

        // Case 2: Enabling a specific logger with no particular log level
        sessionItem.select();
        menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();
        shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();
        shell.bot().radioInGroup(ControlViewSwtBotUtil.JUL_DOMAIN_NAME, ControlViewSwtBotUtil.DOMAIN_GROUP_NAME).click();
        loggersTree = shell.bot().treeInGroup(ControlViewSwtBotUtil.LOGGERS_GROUP_NAME);
        // Expand the "All" and "All - application name" node
        SWTBotTreeItem allItem = loggersTree.getTreeItem(ControlViewSwtBotUtil.ALL_TREE_NODE);
        allItem.expand();
        allItem = SWTBotUtils.getTreeItem(fBot, loggersTree, ControlViewSwtBotUtil.ALL_TREE_NODE, ControlViewSwtBotUtil.JUL_APPLICATION_NAME);
        allItem.expand();
        treeItem = SWTBotUtils.getTreeItem(fBot, loggersTree,
                ControlViewSwtBotUtil.ALL_TREE_NODE,
                ControlViewSwtBotUtil.JUL_APPLICATION_NAME,
                ControlViewSwtBotUtil.LOGGER_NAME);
        treeItem.check();

        // Click the Ok at the bottom of the dialog window
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        SWTBotUtils.waitForJobs();
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(ControlViewSwtBotUtil.JUL_DOMAIN_NAME, sessionItem));

        // Assert that the domain is JUL global
        julDomainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.JUL_DOMAIN_NAME);
        assertEquals(ControlViewSwtBotUtil.JUL_DOMAIN_NAME, julDomainItem.getText());

        // Assert that the logger type in the domain node are correct (all events = *)
        loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.JUL_DOMAIN_NAME,
                ControlViewSwtBotUtil.LOGGER_NAME);
        assertEquals(ControlViewSwtBotUtil.LOGGER_NAME, loggerItem.getText());

        // Case 3: Enabling a specific logger with WARNING log level
        sessionItem.select();
        menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();
        shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();
        shell.bot().radioInGroup(ControlViewSwtBotUtil.JUL_DOMAIN_NAME, ControlViewSwtBotUtil.DOMAIN_GROUP_NAME).click();
        loggersTree = shell.bot().treeInGroup(ControlViewSwtBotUtil.LOGGERS_GROUP_NAME);
        // Expand the "All" and "All - application name" node
        allItem = loggersTree.getTreeItem(ControlViewSwtBotUtil.ALL_TREE_NODE);
        allItem.expand();
        allItem = SWTBotUtils.getTreeItem(fBot, loggersTree, ControlViewSwtBotUtil.ALL_TREE_NODE, "All - ./client_bin/challenger [PID=14237] (With logger)");
        allItem.expand();
        treeItem = SWTBotUtils.getTreeItem(fBot, loggersTree, ControlViewSwtBotUtil.ALL_TREE_NODE, "All - ./client_bin/challenger [PID=14237] (With logger)", ControlViewSwtBotUtil.ANOTHER_LOGGER_NAME);
        treeItem.check();
        // Select a log level
        shell.bot().checkBoxInGroup(LOGLEVEL_PROPERTY_NAME).select();
        shell.bot().ccomboBoxInGroup(LOGLEVEL_PROPERTY_NAME).setSelection("Warning");
        // Click the Ok at the bottom of the dialog window
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        SWTBotUtils.waitForJobs();
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(ControlViewSwtBotUtil.JUL_DOMAIN_NAME, sessionItem));

        // Assert that the domain is JUL global
        julDomainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.JUL_DOMAIN_NAME);
        assertEquals(ControlViewSwtBotUtil.JUL_DOMAIN_NAME, julDomainItem.getText());

        // Assert that the logger type in the domain node are correct (all events = *)
        loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.JUL_DOMAIN_NAME,
                ControlViewSwtBotUtil.ANOTHER_LOGGER_NAME);
        assertEquals(ControlViewSwtBotUtil.ANOTHER_LOGGER_NAME, loggerItem.getText());
    }

    /**
     * Test that the Properties view has been update and shows the the right
     * information.
     */
    protected void testLoggerProperties() {
        // Open the properties view (by id)
        SWTBotUtils.openView("org.eclipse.ui.views.PropertySheet");

        // Case 1: Select the "logger" logger in the Control view
        fBot.viewById(ControlView.ID).show();
        SWTBotTreeItem loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.JUL_DOMAIN_NAME,
                ControlViewSwtBotUtil.LOGGER_NAME);
        loggerItem.select();

        // Get a bot and open the Properties view
        SWTBotView propertiesViewBot = fBot.viewByTitle(PROPERTIES_VIEW);
        propertiesViewBot.show();

        // Get the Log Level field in the tree
        SWTBotTree propertiesViewTree = propertiesViewBot.bot().tree();
        SWTBotTreeItem loglevelTreeItem = propertiesViewTree.getTreeItem(LOGLEVEL_PROPERTY_NAME);
        // We want the VALUE of the 'Log Level' row so the cell index is 1
        String loglevelExpression = loglevelTreeItem.cell(1);

        // Assert that the expression in the Properties view is the same as
        // the one we entered
        assertEquals("All", loglevelExpression);

        // Case 2: Select the "anotherLogger" logger in the Control view
        fBot.viewById(ControlView.ID).show();
        loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.JUL_DOMAIN_NAME,
                ControlViewSwtBotUtil.ANOTHER_LOGGER_NAME);
        loggerItem.select();

        // Get a bot and open the Properties view
        propertiesViewBot = fBot.viewByTitle(PROPERTIES_VIEW);
        propertiesViewBot.show();

        // Get the Log Level field in the tree
        propertiesViewTree = propertiesViewBot.bot().tree();
        loglevelTreeItem = propertiesViewTree.getTreeItem(LOGLEVEL_PROPERTY_NAME);
        // We want the VALUE of the 'Log Level' row so the cell index is 1
        loglevelExpression = loglevelTreeItem.cell(1);

        // Assert that the expression in the Properties view is the same as
        // the one we entered
        assertEquals("<= Warning", loglevelExpression);

        // Close the Properties view
        SWTBotUtils.closeView(PROPERTIES_VIEW, fBot);
    }

}
