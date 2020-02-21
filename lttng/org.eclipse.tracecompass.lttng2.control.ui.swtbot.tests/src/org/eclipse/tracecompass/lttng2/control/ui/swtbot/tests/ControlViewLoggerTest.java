/*******************************************************************************
 * Copyright (c) 2016 Ericsson
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

import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceDomainType;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.tracecompass.tmf.ui.tests.shared.WaitUtils;
import org.junit.Test;

/**
 * Test for the Control view in Trace Compass. This will test the different logger domain.
 *
 * @author Bruno Roy
 */
public class ControlViewLoggerTest extends ControlViewTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateSessionTestLTTng2_8.cfg";
    private static final String CREATE_SESSION_JUL_SCENARIO_NAME = "JulLogger";
    private static final String CREATE_SESSION_LOG4J_SCENARIO_NAME = "Log4jLogger";
    private static final String CREATE_SESSION_PYTHON_SCENARIO_NAME = "PythonLogger";

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

        // Enable JUL loggers
        fProxy.setScenario(CREATE_SESSION_JUL_SCENARIO_NAME);
        testCreateSession();
        testEnableLoggers(TraceDomainType.JUL);
        testStartStopTracing(TraceSessionState.ACTIVE);
        testStartStopTracing(TraceSessionState.INACTIVE);
        // Verify that the Properties view shows to right logger log level
        testLoggerProperties(TraceDomainType.JUL);

        // Enable LOG4J loggers
        fProxy.setScenario(CREATE_SESSION_LOG4J_SCENARIO_NAME);
        testEnableLoggers(TraceDomainType.LOG4J);
        testStartStopTracing(TraceSessionState.ACTIVE);
        testStartStopTracing(TraceSessionState.INACTIVE);
        // Verify that the Properties view shows to right logger log level
        testLoggerProperties(TraceDomainType.LOG4J);

        // Enable Python loggers
        fProxy.setScenario(CREATE_SESSION_PYTHON_SCENARIO_NAME);
        testEnableLoggers(TraceDomainType.PYTHON);
        testStartStopTracing(TraceSessionState.ACTIVE);
        testStartStopTracing(TraceSessionState.INACTIVE);
        // Verify that the Properties view shows to right logger log level
        testLoggerProperties(TraceDomainType.PYTHON);

        // Clean session
        testDestroySession();
        testDisconnectFromNode();
    }

    /**
     * Enable loggers with different log level and log level type
     *
     * @param domain
     *            the logger domain to test
     */
    protected void testEnableLoggers(TraceDomainType domain) {
        String domainName = new String();
        String logLevel = new String();
        switch (domain) {
        case JUL:
            domainName = ControlViewSwtBotUtil.JUL_DOMAIN_NAME;
            logLevel = "Warning";
            break;
        case LOG4J:
            domainName = ControlViewSwtBotUtil.LOG4J_DOMAIN_NAME;
            logLevel = "Fatal";
            break;
        case PYTHON:
            domainName = ControlViewSwtBotUtil.PYTHON_DOMAIN_NAME;
            logLevel = "Critical";
            break;
            //$CASES-OMITTED$
        default:
            break;
        }
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

        // Switching to the logger domain
        shell.bot().radioInGroup(domainName, ControlViewSwtBotUtil.DOMAIN_GROUP_NAME).click();

        // Selecting all loggers
        SWTBotTree loggersTree = shell.bot().treeInGroup(ControlViewSwtBotUtil.LOGGERS_GROUP_NAME);
        SWTBotTreeItem treeItem = loggersTree.getTreeItem(ControlViewSwtBotUtil.ALL_TREE_NODE);
        treeItem.check();

        // Click the Ok at the bottom of the dialog window
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable(domainName, sessionItem));

        // Assert that the domain is correct
        SWTBotTreeItem domainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName);
        assertEquals(domainName, domainItem.getText());

        // Assert that the logger type in the domain node are correct (all events = *)
        SWTBotTreeItem loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName,
                ControlViewSwtBotUtil.ALL_EVENTS_NAME);
        assertEquals(ControlViewSwtBotUtil.ALL_EVENTS_NAME, loggerItem.getText());

        // Case 2: Enabling a specific logger with no particular log level
        sessionItem.select();
        menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();
        shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();
        shell.bot().radioInGroup(domainName, ControlViewSwtBotUtil.DOMAIN_GROUP_NAME).click();
        loggersTree = shell.bot().treeInGroup(ControlViewSwtBotUtil.LOGGERS_GROUP_NAME);
        // Expand the "All" and "All - application name" node
        SWTBotTreeItem allItem = loggersTree.getTreeItem(ControlViewSwtBotUtil.ALL_TREE_NODE);
        allItem.expand();
        allItem = SWTBotUtils.getTreeItem(fBot, loggersTree, ControlViewSwtBotUtil.ALL_TREE_NODE, ControlViewSwtBotUtil.LOGGER_APPLICATION_NAME);
        allItem.expand();
        treeItem = SWTBotUtils.getTreeItem(fBot, loggersTree,
                ControlViewSwtBotUtil.ALL_TREE_NODE,
                ControlViewSwtBotUtil.LOGGER_APPLICATION_NAME,
                ControlViewSwtBotUtil.LOGGER_NAME);
        treeItem.check();

        // Click the Ok at the bottom of the dialog window
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable(domainName, sessionItem));

        // Assert that the domain is correct
        domainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName);
        assertEquals(domainName, domainItem.getText());

        // Assert that the logger type in the domain node are correct (all events = *)
        loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName,
                ControlViewSwtBotUtil.LOGGER_NAME);
        assertEquals(ControlViewSwtBotUtil.LOGGER_NAME, loggerItem.getText());

        // Case 3: Enabling a specific logger with a log level
        sessionItem.select();
        menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();
        shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();
        shell.bot().radioInGroup(domainName, ControlViewSwtBotUtil.DOMAIN_GROUP_NAME).click();
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
        shell.bot().ccomboBoxInGroup(LOGLEVEL_PROPERTY_NAME).setSelection(logLevel);
        // Click the Ok at the bottom of the dialog window
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable(domainName, sessionItem));

        // Assert that the domain is correct
        domainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName);
        assertEquals(domainName, domainItem.getText());

        // Assert that the logger type in the domain node are correct (all events = *)
        loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName,
                ControlViewSwtBotUtil.ANOTHER_LOGGER_NAME);
        assertEquals(ControlViewSwtBotUtil.ANOTHER_LOGGER_NAME, loggerItem.getText());

        // Case 4: Enabling a logger by specifying the name
        sessionItem.select();
        menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();
        shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();
        shell.bot().radioInGroup(domainName, ControlViewSwtBotUtil.DOMAIN_GROUP_NAME).click();
        loggersTree = shell.bot().treeInGroup(ControlViewSwtBotUtil.LOGGERS_GROUP_NAME);
        // Write a logger name in the Specific logger text field
        shell.bot().textInGroup("Specific logger").setText(ControlViewSwtBotUtil.SPECIFIC_LOGGER_NAME1 + "," + ControlViewSwtBotUtil.SPECIFIC_LOGGER_NAME2);
        // Click the Ok at the bottom of the dialog window
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();
        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable(domainName, sessionItem));

        // Assert that the domain is correct
        domainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName);
        assertEquals(domainName, domainItem.getText());

        // Assert that the logger type in the domain node are correct (all events = *)
        loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName,
                ControlViewSwtBotUtil.SPECIFIC_LOGGER_NAME1);
        assertEquals(ControlViewSwtBotUtil.SPECIFIC_LOGGER_NAME1, loggerItem.getText());

        // Assert that the logger type in the domain node are correct (all events = *)
        loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName,
                ControlViewSwtBotUtil.SPECIFIC_LOGGER_NAME2);
        assertEquals(ControlViewSwtBotUtil.SPECIFIC_LOGGER_NAME2, loggerItem.getText());
    }

    /**
     * Test that the Properties view has been update and shows the the right
     * information.
     *
     * @param domain
     *            the logger domain to test
     */
    protected void testLoggerProperties(TraceDomainType domain) {
        String domainName = new String();
        String logLevel = new String();
        switch (domain) {
        case JUL:
            domainName = ControlViewSwtBotUtil.JUL_DOMAIN_NAME;
            logLevel = "<= Warning";
            break;
        case LOG4J:
            domainName = ControlViewSwtBotUtil.LOG4J_DOMAIN_NAME;
            logLevel = "<= Fatal";
            break;
        case PYTHON:
            domainName = ControlViewSwtBotUtil.PYTHON_DOMAIN_NAME;
            logLevel = "<= Critical";
            break;
            //$CASES-OMITTED$
        default:
            break;
        }

        // Open the properties view (by id)
        SWTBotUtils.openView("org.eclipse.ui.views.PropertySheet");

        // Case 1: Select the "logger" logger in the Control view
        fBot.viewById(ControlView.ID).show();
        SWTBotTreeItem loggerItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                domainName,
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
                domainName,
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
        assertEquals(logLevel, loglevelExpression);

        // Close the Properties view
        SWTBotUtils.closeView(PROPERTIES_VIEW, fBot);
    }

}
