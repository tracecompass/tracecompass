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

import java.io.File;
import java.net.URL;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.swtbot.eclipse.finder.SWTWorkbenchBot;
import org.eclipse.swtbot.eclipse.finder.widgets.SWTBotView;
import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.utils.SWTBotPreferences;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotText;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.stubs.service.TestRemoteSystemProxy;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.ControlView;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.tmf.remote.core.proxy.TmfRemoteConnectionFactory;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.SWTBotUtils;
import org.eclipse.ui.IViewPart;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.osgi.framework.FrameworkUtil;

/**
 * Test for the Call Stack view in trace compass
 *
 * @author Bernd Hufmann
 */
@RunWith(SWTBotJunit4ClassRunner.class)
public class ControlViewTest {


    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final String TEST_STREAM = "CreateSessionTestLTTng2_7.cfg";
    /** The initialize scenario name */
    protected static final String INIT_SCENARIO_NAME = "Initialize";
    private static final String CREATE_SESSION_WITH_LTTNG_2_7_SCENARIO_NAME = "CreateSession_2.7";

    private static final String SESSION_NAME = "mysession";
    private static final String UST_CHANNEL_NAME = ControlViewSwtBotUtil.DEFAULT_CHANNEL_NAME;
    private static final String NODE_NAME = "myNode";

    // ------------------------------------------------------------------------
    // Test data
    // ------------------------------------------------------------------------

    /** The Log4j logger instance. */
    protected static final Logger fLogger = Logger.getRootLogger();
    /** The workbench bot */
    protected static SWTWorkbenchBot fBot;
    private IRemoteConnection fHost = TmfRemoteConnectionFactory.getLocalConnection();
    /** The test remote system proxy */
    protected @NonNull TestRemoteSystemProxy fProxy = new TestRemoteSystemProxy(fHost);
    /** The trace control tree */
    protected SWTBotTree fTree;
    /** The trace control root component */
    protected ITraceControlComponent fRoot;
    /** The target node component */
    protected TargetNodeComponent fNode;


    /** The test file */
    protected String fTestFile;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    /**
     * Initialization
     */
    @BeforeClass
    public static void init() {
        SWTBotUtils.initialize();

        Thread.currentThread().setName("SWTBot Thread"); // for the debugger
        /* set up for swtbot */
        SWTBotPreferences.TIMEOUT = 20000; /* 20 second timeout */
        fLogger.addAppender(new ConsoleAppender(new SimpleLayout()));
        fBot = new SWTWorkbenchBot();

        SWTBotUtils.closeView("welcome", fBot);

        /* finish waiting for eclipse to load */
        SWTBotUtils.waitForJobs();
    }

    /**
     * Open a trace in an editor
     *
     * @throws Exception
     *             if problem occurs
     */
    @Before
    public void beforeTest() throws Exception {
        SWTBotUtils.openView(ControlView.ID);
        SWTBotUtils.waitForJobs();
        URL location = FileLocator.find(FrameworkUtil.getBundle(this.getClass()), new Path("testfiles" + File.separator + getTestStream()), null);
        File testfile = new File(FileLocator.toFileURL(location).toURI());
        fTestFile = testfile.getAbsolutePath();

        // Create root component
        SWTBotView viewBot = fBot.viewById(ControlView.ID);
        viewBot.setFocus();
        IViewPart part = viewBot.getViewReference().getView(true);
        ControlView view = (ControlView) part;
        fRoot = view.getTraceControlRoot();

        // Create node component
        fNode = new TargetNodeComponent(getNodeName(), fRoot, fProxy);
        fRoot.addChild(fNode);
        fTree = viewBot.bot().tree();
    }

    /**
     * Close the editor
     */
    @After
    public void tearDown() {
        fBot.closeAllEditors();
        if (fRoot != null) {
           fRoot.removeAllChildren();
        }
    }

    /**
     * Get the test stream file name to use for the test suite
     *
     * @return the name of the test stream file
     */
    protected String getTestStream() {
        return TEST_STREAM;
    }

    /**
     * Get the session name
     *
     * @return the session name for this test
     */
    protected String getSessionName() {
        return SESSION_NAME;
    }

    /**
     * Get the node name
     *
     * @return the node name for the test
     */
    protected String getNodeName() {
        return NODE_NAME;
    }

    /**
     * Test basic trace session generation.
     */
    @Test
    public void testTraceSessionTree() {

        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(INIT_SCENARIO_NAME);

        testConnectToNode();
        // Set the scenario
        fProxy.setScenario(CREATE_SESSION_WITH_LTTNG_2_7_SCENARIO_NAME);
        testCreateSession();
        testEnableKernelEvent();
        testEnableSyscalls();
        testEnableUstChannel();
        testEnableUstEvents();
        testStartStopTracing(TraceSessionState.ACTIVE);
        testStartStopTracing(TraceSessionState.INACTIVE);
        testDestroySession();
        testDisconnectFromNode();
    }

    /**
     * Test connect to node
     */
    protected void testConnectToNode() {
        SWTBotTreeItem nodeItem = SWTBotUtils.getTreeItem(fBot, fTree, getNodeName());
        nodeItem.select();
        SWTBotMenu menuBot = nodeItem.contextMenu(ControlViewSwtBotUtil.CONNECT_MENU_ITEM);
        menuBot.click();
        SWTBotUtils.waitForJobs();

        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(ControlViewSwtBotUtil.SESSION_GROUP_NAME, nodeItem));

        // Verify that node is connected
        fBot.waitUntil(ControlViewSwtBotUtil.isStateChanged(fNode, TargetNodeState.CONNECTED));
        assertEquals(TargetNodeState.CONNECTED, fNode.getTargetNodeState());
    }

    /**
     * Test create session
     */
    protected void testCreateSession() {
        SWTBotTreeItem nodeItem = SWTBotUtils.getTreeItem(fBot, fTree, getNodeName());

        SWTBotTreeItem sessionGroupItem = nodeItem.getNode(ControlViewSwtBotUtil.SESSION_GROUP_NAME);

        sessionGroupItem.select();
        SWTBotMenu menuBot = sessionGroupItem.contextMenu(ControlViewSwtBotUtil.CREATE_SESSION_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.CREATE_SESSION_DIALOG_TITLE).activate();

        SWTBotText sessionText = shell.bot().textWithLabel(ControlViewSwtBotUtil.SESSION_NAME_LABEL);
        sessionText.setText(SESSION_NAME);

        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        SWTBotUtils.waitForJobs();

        sessionGroupItem.expand();

        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(getSessionName(), sessionGroupItem));
        assertEquals(1, sessionGroupItem.getNodes().size());

        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName());
        assertEquals(getSessionName(), sessionItem.getText());
    }

    /**
     * Test enable event (all kernel tracepoints) on session level
     */
    protected void testEnableKernelEvent() {
        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName());

        sessionItem.select();
        SWTBotMenu menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();

        SWTBotTree tracepointsTree = shell.bot().tree();
        tracepointsTree.select(ControlViewSwtBotUtil.ALL_TREE_NODE);
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

        SWTBotTreeItem kernelDomainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME);
        assertEquals(ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME, kernelDomainItem.getText());
    }

    /**
     * Test enable Event (syscall) on domain level
     */
    protected void testEnableSyscalls() {
        SWTBotTreeItem kernelDomainItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.KERNEL_DOMAIN_NAME);
        kernelDomainItem.select();
        SWTBotMenu menuBot = kernelDomainItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();
        shell.bot().radioInGroup(ControlViewSwtBotUtil.GROUP_SELECT_NAME, ControlViewSwtBotUtil.SYSCALL_GROUP_NAME).click();
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        SWTBotUtils.waitForJobs();
    }

    /**
     * Test enable UST channel on session level (default values)
     */
    protected void testEnableUstChannel() {
        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName());
        sessionItem.select();
        SWTBotMenu menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.ENABLE_CHANNEL_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_CHANNEL_DIALOG_TITLE).activate();
        SWTBotText channelText = shell.bot().textWithLabel(ControlViewSwtBotUtil.CHANNEL_NAME_LABEL);
        channelText.setText(UST_CHANNEL_NAME);

        shell.bot().radioInGroup(ControlViewSwtBotUtil.UST_GROUP_NAME, ControlViewSwtBotUtil.DOMAIN_GROUP_NAME).click();
        shell.bot().radioInGroup(ControlViewSwtBotUtil.BUFFERTYPE_PER_UID, ControlViewSwtBotUtil.BUFFERTYPE_GROUP_NAME).click();
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        SWTBotUtils.waitForJobs();
        fBot.waitUntil(ConditionHelpers.IsTreeChildNodeAvailable(ControlViewSwtBotUtil.UST_DOMAIN_NAME, sessionItem));
    }

    /**
     * Test enable event (all tracepoints) on channel level
     */
    protected void testEnableUstEvents() {
        SWTBotTreeItem channelItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(), ControlViewSwtBotUtil.UST_DOMAIN_NAME,
                UST_CHANNEL_NAME);
        assertEquals(UST_CHANNEL_NAME, channelItem.getText());

        channelItem.select();
        SWTBotMenu menuBot = channelItem.contextMenu(ControlViewSwtBotUtil.ENABLE_EVENT_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.ENABLE_EVENT_DIALOG_TITLE).activate();
        SWTBotTree tracepointsTree = shell.bot().tree();
        tracepointsTree.select(ControlViewSwtBotUtil.ALL_TREE_NODE);
        shell.bot().button(ControlViewSwtBotUtil.DIALOG_OK_BUTTON).click();
        SWTBotUtils.waitForJobs();

        SWTBotTreeItem eventItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName(),
                ControlViewSwtBotUtil.UST_DOMAIN_NAME,
                UST_CHANNEL_NAME,
                ControlViewSwtBotUtil.ALL_EVENTS_NAME);
        assertEquals(ControlViewSwtBotUtil.ALL_EVENTS_NAME, eventItem.getText());
    }

    /**
     * Test start or stop tracing
     *
     * @param state
     *            the state to change to
     */
    protected void testStartStopTracing(TraceSessionState state) {
        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName());
        sessionItem.select();

        if (state == TraceSessionState.ACTIVE) {
            SWTBotMenu menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.START_MENU_ITEM);
            menuBot.click();
            SWTBotUtils.waitForJobs();
        } else {
            SWTBotMenu menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.STOP_MENU_ITEM);
            menuBot.click();
            SWTBotUtils.waitForJobs();
        }
        TraceSessionComponent sessionComp = ControlViewSwtBotUtil.getSessionComponent(fNode, getSessionName());
        assertNotNull(sessionComp);

        fBot.waitUntil(ControlViewSwtBotUtil.isSessionStateChanged(sessionComp, state));
        assertEquals(state, sessionComp.getSessionState());
    }

    /**
     * Test destroy session
     */
    protected void testDestroySession() {
        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                getSessionName());

        sessionItem.select();
        SWTBotMenu menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.DESTROY_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.DESTROY_CONFIRM_DIALOG_TITLE).activate();
        shell.bot().button(ControlViewSwtBotUtil.CONFIRM_DIALOG_OK_BUTTON).click();
        SWTBotUtils.waitForJobs();

        SWTBotTreeItem sessionGroupItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(), ControlViewSwtBotUtil.SESSION_GROUP_NAME);

        fBot.waitUntil(ConditionHelpers.isTreeChildNodeRemoved(0, sessionGroupItem));
        assertEquals(0, sessionGroupItem.getNodes().size());
    }

    /**
     * Test disconnect from node
     */
    protected void testDisconnectFromNode() {
        SWTBotTreeItem nodeItem = SWTBotUtils.getTreeItem(fBot, fTree, getNodeName());

        nodeItem.select();
        SWTBotMenu menuBot = nodeItem.contextMenu(ControlViewSwtBotUtil.DISCONNECT_MENU_ITEM);
        menuBot.click();
        SWTBotUtils.waitForJobs();

        // Verify that node is connected
        fBot.waitUntil(ControlViewSwtBotUtil.isStateChanged(fNode, TargetNodeState.DISCONNECTED));
        assertEquals(TargetNodeState.DISCONNECTED, fNode.getTargetNodeState());
        assertEquals(0, nodeItem.getNodes().size());
    }
}