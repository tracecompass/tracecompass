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
import static org.junit.Assume.assumeTrue;

import org.eclipse.swtbot.swt.finder.junit.SWTBotJunit4ClassRunner;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotMenu;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotRadio;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotShell;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTree;
import org.eclipse.swtbot.swt.finder.widgets.SWTBotTreeItem;
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
public class ControlViewProfileTest extends ControlViewTest {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final boolean IS_LINUX = System.getProperty("os.name").contains("Linux") ? true : false;
    private static final String TEST_STREAM = "Profile.cfg";
    private static final String CREATE_PROFILE_SCENARIO_NAME = "ProfileTest";

    private static final String SESSION_NAME = String.valueOf(System.nanoTime());

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

        // Save and load feature will only work on a Linux remote
        assumeTrue(IS_LINUX);

        fProxy.setSessionName(getSessionName());
        fProxy.setTestFile(fTestFile);
        fProxy.setScenario(INIT_SCENARIO_NAME);

        testConnectToNode();
        // Prepare for saving of profile
        fProxy.setScenario(CREATE_PROFILE_SCENARIO_NAME);
        testSaveSession();
        testDestroySession();
        testLoadSession();
        testDestroySession();
        // Disable saving of profiles
        fProxy.setProfileName(null);
        fProxy.deleteProfileFile();
        fProxy.setSessionName(null);
        testDisconnectFromNode();
    }

    /**
     * Test save session
     */
    private void testSaveSession() {
        fProxy.setProfileName(getSessionName());

        SWTBotTreeItem sessionItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(),
                ControlViewSwtBotUtil.SESSION_GROUP_NAME,
                SESSION_NAME);

        assertEquals(SESSION_NAME, sessionItem.getText());

        sessionItem.select();
        SWTBotMenu menuBot = sessionItem.contextMenu(ControlViewSwtBotUtil.SAVE_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.SAVE_DIALOG_TITLE).activate();
        shell.bot().button(ControlViewSwtBotUtil.CONFIRM_DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();
    }

    /**
     * Test load session
     */
    private void testLoadSession() {
        SWTBotTreeItem nodeItem = SWTBotUtils.getTreeItem(fBot, fTree, getNodeName());
        SWTBotTreeItem sessionGroupItem = nodeItem.getNode(ControlViewSwtBotUtil.SESSION_GROUP_NAME);

        sessionGroupItem.select();
        SWTBotMenu menuBot = sessionGroupItem.contextMenu(ControlViewSwtBotUtil.LOAD_MENU_ITEM);
        menuBot.click();

        SWTBotShell shell = fBot.shell(ControlViewSwtBotUtil.LOAD_DIALOG_TITLE).activate();

        SWTBotRadio button = shell.bot().radio(ControlViewSwtBotUtil.REMOTE_RADIO_BUTTON_LABEL);
        button.click();

        SWTBotTree shellTree = shell.bot().tree();

        SWTBotTreeItem profileItem = shellTree.getTreeItem(SESSION_NAME + ControlViewSwtBotUtil.PROFILE_SUFFIX);
        profileItem.select();
        profileItem.click();

        shell.bot().button(ControlViewSwtBotUtil.CONFIRM_DIALOG_OK_BUTTON).click();
        WaitUtils.waitForJobs();

        sessionGroupItem = SWTBotUtils.getTreeItem(fBot, fTree,
                getNodeName(), ControlViewSwtBotUtil.SESSION_GROUP_NAME);

        fBot.waitUntil(ConditionHelpers.isTreeChildNodeAvailable(SESSION_NAME, sessionGroupItem));
        assertEquals(1, sessionGroupItem.getNodes().size());
    }

}