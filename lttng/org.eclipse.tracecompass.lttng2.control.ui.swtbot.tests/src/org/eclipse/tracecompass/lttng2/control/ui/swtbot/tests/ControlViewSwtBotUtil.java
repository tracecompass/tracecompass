/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.control.ui.swtbot.tests;

import java.util.Arrays;

import org.eclipse.swtbot.swt.finder.waits.ICondition;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TargetNodeState;
import org.eclipse.tracecompass.internal.lttng2.control.core.model.TraceSessionState;
import org.eclipse.tracecompass.internal.lttng2.control.ui.views.model.ITraceControlComponent;
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
    public static final String JUL_DOMAIN_NAME = "JUL";
    public static final String LOG4J_DOMAIN_NAME = "LOG4J";
    public static final String PYTHON_DOMAIN_NAME = "Python";
    public static final String SESSION_GROUP_NAME = "Sessions";
    public static final String PROVIDER_GROUP_NAME = "Provider";
    public static final String ALL_EVENTS_NAME = "*";
    public static final String SCHED_SWITCH_EVENT_NAME = "sched_switch";
    public static final String SCHED_WAKEUP_EVENT_NAME = "sched_wakeup";
    public static final String SCHED_PROCESSWAIT_EVENT_NAME = "sched_process_wait";
    public static final String SCHED_PROCESSFORK_EVENT_NAME = "sched_process_fork";
    public static final String SCHED_PROCESSEXEC_EVENT_NAME = "sched_process_exec";
    public static final String LOGGER_NAME = "logger";
    public static final String ANOTHER_LOGGER_NAME = "anotherLogger";
    public static final String SPECIFIC_LOGGER_NAME1 = "specificLogger1";
    public static final String SPECIFIC_LOGGER_NAME2 = "specificLogger2";
    public static final String PROFILE_SUFFIX = ".lttng";
    public static final String KERNEL_TRACE_NAME = "kernel";

    // Menu strings
    public static final String CONNECT_MENU_ITEM = "Connect";
    public static final String CREATE_SESSION_MENU_ITEM = "Create Session...";
    public static final String ENABLE_EVENT_DEFAULT_CHANNEL_MENU_ITEM = "Enable Event (default channel)...";
    public static final String ENABLE_CHANNEL_MENU_ITEM = "Enable Channel...";
    public static final String ENABLE_EVENT_MENU_ITEM = "Enable Event...";
    public static final String START_MENU_ITEM = "Start";
    public static final String STOP_MENU_ITEM = "Stop";
    public static final String IMPORT_MENU_ITEM = "Import...";
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
    public static final String ALL_EVENT_GROUP_NAME = "All Tracepoint Events and Syscalls";
    public static final String SPECIFIC_EVENT_GROUP_NAME = "Specific event";
    public static final String TRACEPOINTS_GROUP_NAME = "Tracepoint Events";
    public static final String SYSCALL_GROUP_NAME = "Syscall Events";
    public static final String SYSCALL_WRITE_EVENT = "write";
    public static final String SYSCALL_READ_EVENT = "read";
    public static final String SYSCALL_CLOSE_EVENT = "close";
    public static final String LOGGERS_GROUP_NAME = "Loggers";
    public static final String GROUP_SELECT_NAME = "Select";
    public static final String ENABLE_CHANNEL_DIALOG_TITLE = "Enable Channel";
    public static final String DOMAIN_GROUP_NAME = "Domain";
    public static final String UST_GROUP_NAME = "UST";
    public static final String LOGGER_APPLICATION_NAME = "All - ./client_bin/challenger [PID=14237] (With logger)";
    public static final String BUFFERTYPE_GROUP_NAME = "Buffer Type";
    public static final String BUFFERTYPE_PER_UID = "Per UID buffers";
    public static final String FILTER_EXPRESSION_LABEL = "Filter Expression";
    public static final String EXCLUDE_EVENT_LABEL = "Exclude Events";
    public static final String SESSION_LIST_GROUP_NAME = "Session List";

    public static final String DESTROY_CONFIRM_DIALOG_TITLE = "Destroy Confirmation";
    public static final String CHANNEL_NAME_LABEL = "Channel Name";

    public static final String SAVE_DIALOG_TITLE = "Save Sessions";
    public static final String LOAD_DIALOG_TITLE = "Load Sessions";
    public static final String REMOTE_RADIO_BUTTON_LABEL = "Remote";

    // Remote import strings
    public static final String IMPORT_WIZARD_TITLE = "Fetch Remote Traces";
    public static final String DEFAULT_REMOTE_PROJECT = "Remote";
    public static final String FINISH_BUTTON = "Finish";
    public static final String CANCEL_BUTTON = "Cancel";
    public static final String OPTION_GROUP_NAME = "Options";

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

    /**
     * Finds a {@link ITraceControlComponent} in a tree for given path.
     *
     * @param root
     *            root component
     * @param path
     *            path to element
     * @return the matched component or null
     */
    public static ITraceControlComponent getComponent(ITraceControlComponent root, String... path) {
        ITraceControlComponent newRoot = root;
        for (String segment : path) {
            newRoot = Arrays.asList(newRoot.getChildren()).stream()
            .filter(child -> (child.getName().equals(segment)))
            .findFirst()
            .orElse(null);
            if (newRoot == null) {
                return null;
            }
        }
        return newRoot;
    }

}
