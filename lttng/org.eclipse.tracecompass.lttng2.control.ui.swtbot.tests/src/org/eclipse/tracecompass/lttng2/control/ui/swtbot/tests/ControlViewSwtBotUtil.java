/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.swtbot.tests;

import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TargetNodeComponent;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.impl.TraceSessionComponent;
import org.eclipse.tracecompass.tmf.ui.swtbot.tests.shared.ConditionHelpers.SWTBotTestCondition;

/**
 * SWTBot utilities for ControlView test
 *
 * @author Bernd Hufmann
 */
class ControlViewSwtBotUtil {

    public static final String USER_HOME = System.getProperty("user.home");
    public static final String DEFAULT_CHANNEL_NAME = "channel0";
    public static final String KERNEL_DOMAIN_NAME = "Kernel";
    public static final String UST_DOMAIN_NAME = "UST global";
    public static final String SESSION_GROUP_NAME = "Sessions";
    public static final String ALL_EVENTS_NAME = "*";
    public static final String PROFILE_SUFFIX = ".lttng";

    // Menu strings
    public static final String CONNECT_MENU_ITEM = "Connect";
    public static final String CREATE_SESSION_MENU_ITEM = "Create Session...";
    public static final String ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM = "Enable Event (default channel)...";
    public static final String ENABLE_CHANNEL_MENU_ITEM = "Enable Channel...";
    public static final String ENABLE_EVENT_MENU_ITEM = "Enable Event...";
    public static final String START_MENU_ITEM = "Start";
    public static final String STOP_MENU_ITEM = "Stop";
    public static final String DESTROY_MENU_ITEM = "Destroy Session...";
    public static final String DISCONNECT_MENU_ITEM = "Disconnect";
    public static final String SAVE_MENU_ITEM = "Save...";
    public static final String LOAD_MENU_ITEM = "Load...";

    // Dialog strings
    public static final String CREATE_SESSION_DIALOG_TITLE = "Create Session";
    public static final String SESSION_NAME_LABEL = "Session Name";
    public static final String DIALOG_OK_BUTTON = "Ok";
    public static final String CONFIRM_DIALOG_OK_BUTTON = "OK";
    public static final String ENABLE_EVENT_DIALOG_TITLE = "Enable Events";
    public static final String ALL_TREE_NODE = "All";
    public static final String SYSCALL_GROUP_NAME = "All Syscalls";
    public static final String GROUP_SELECT_NAME = "Select";
    public static final String ENABLE_CHANNEL_DIALOG_TITLE = "Enable Channel";
    public static final String DOMAIN_GROUP_NAME = "Domain";
    public static final String UST_GROUP_NAME = "UST";
    public static final String BUFFERTYPE_GROUP_NAME = "Buffer Type";
    public static final String BUFFERTYPE_PER_UID = "Per UID buffers";

    public static final String DESTROY_CONFIRM_DIALOG_TITLE = "Destroy Confirmation";
    public static final String CHANNEL_NAME_LABEL = "Channel Name";

    public static final String SAVE_DIALOG_TITLE = "Save Sessions";
    public static final String LOAD_DIALOG_TITLE = "Load Sessions";
    public static final String REMOTE_RADIO_BUTTON_LABEL = "Remote";

    private ControlViewSwtBotUtil() { }

    /**
     * Tests for Target node state
     *
     * @param node
     *            target node component
     * @param state
     *            the state to wait
     * @return the condition instance
     */
    public static ICondition isStateChanged(final TargetNodeComponent node, final TargetNodeState state) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                if (node.getTargetNodeState() != state) {
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * Tests for session state
     *
     * @param session
     *            the session component
     * @param state
     *            the state to wait
     * @return the condition instance
     */
    public static ICondition isSessionStateChanged(final TraceSessionComponent session, final TraceSessionState state) {
        return new SWTBotTestCondition() {
            @Override
            public boolean test() throws Exception {
                if (session.getSessionState() != state) {
                    return false;
                }
                return true;
            }
        };
    }

    /**
     * Finds a session for given node
     *
     * @param target
     *            target node component
     * @param sessionName
     *            session name to find
     * @return the session component or null
     */
    public static TraceSessionComponent getSessionComponent(TargetNodeComponent target, String sessionName) {
        final TraceSessionComponent[] sessions = target.getSessions();
        if (sessions != null) {
            for (int i = 0; i < sessions.length; i++) {
                if (sessionName.equals(sessions[i].getName())) {
                    return sessions[i];
                }
            }
        }
        return null;
    }

}
