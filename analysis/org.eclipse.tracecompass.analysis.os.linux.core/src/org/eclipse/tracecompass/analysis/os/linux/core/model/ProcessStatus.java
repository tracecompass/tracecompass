/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.model;

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.LinuxValues;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

/**
 * A list of status a thread can be in
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 * @since 2.4
 */
public enum ProcessStatus {

    /** Unknown process status */
    UNKNOWN(StateValues.PROCESS_STATUS_UNKNOWN_VALUE),
    /** Waiting for a fork */
    WAIT_FORK(StateValues.PROCESS_STATUS_WAIT_FORK_VALUE),
    /** Waiting for the CPU */
    WAIT_CPU(StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE),
    /**
     * The thread has exited, but is not dead yet
     *
     * @since 2.3
     */
    EXIT(StateValues.PROCESS_STATUS_UNKNOWN_VALUE),
    /** The thread is a zombie thread */
    ZOMBIE(StateValues.PROCESS_STATUS_UNKNOWN_VALUE),
    /** The thread is blocked */
    WAIT_BLOCKED(StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE),
    /** The thread is running */
    RUN(StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE),
    /** The thread is dead or hasn't started yet */
    NOT_ALIVE(TmfStateValue.nullValue()),
    /** The thread is running in system call */
    RUN_SYTEMCALL(StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE),
    /** The thread is running but interrupted */
    INTERRUPTED(StateValues.PROCESS_STATUS_INTERRUPTED_VALUE),
    /** Waiting for an unknown reason */
    WAIT_UNKNOWN(StateValues.PROCESS_STATUS_WAIT_UNKNOWN_VALUE);

    private final ITmfStateValue fValue;

    private ProcessStatus(ITmfStateValue value) {
        fValue = value;
    }

    /**
     * Get the state value that represents this status, to use to store the status
     * to the state system
     *
     * @return The state value corresponding to this process status
     */
    public ITmfStateValue getStateValue() {
        return fValue;
    }

    /**
     * Get the ProcessStatus associated with a long value
     *
     * @param status
     *            The long value corresponding to a status
     * @return The {@link ProcessStatus} enum value
     */
    public static ProcessStatus getStatusFromStatedump(long status) {
        switch (Long.valueOf(status).intValue()) {
        case LinuxValues.STATEDUMP_PROCESS_STATUS_UNNAMED:
            return UNKNOWN;
        case LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT_FORK:
            return WAIT_FORK;
        case LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT_CPU:
            return WAIT_CPU;
        case LinuxValues.STATEDUMP_PROCESS_STATUS_EXIT:
            return EXIT;
        case LinuxValues.STATEDUMP_PROCESS_STATUS_ZOMBIE:
            return ZOMBIE;
        case LinuxValues.STATEDUMP_PROCESS_STATUS_WAIT:
            /*
             * We have no information on what the process is waiting on (unlike a
             * sched_switch for example), so we will use the WAIT_UNKNOWN state instead of
             * the "normal" WAIT_BLOCKED state.
             */
            return WAIT_UNKNOWN;
        case LinuxValues.STATEDUMP_PROCESS_STATUS_RUN:
            return RUN;
        case LinuxValues.STATEDUMP_PROCESS_STATUS_DEAD:
            return NOT_ALIVE;
        default:
            return UNKNOWN;
        }
    }

    /**
     * Get the ProcessStatus associated with a state value
     *
     * @param sv
     *            state value
     * @return The {@link ProcessStatus} enum value
     */
    static public ProcessStatus getStatusFromStateValue(ITmfStateValue sv) {
        for (ProcessStatus e : ProcessStatus.values()) {
            if (e.getStateValue().equals(sv)) {
                return e;
            }
        }
        return UNKNOWN;
    }

    private static boolean isDead(int state) {
        return (state & LinuxValues.TASK_DEAD) != 0;
    }

    private static boolean isWaiting(int state) {
        return (state & (LinuxValues.TASK_INTERRUPTIBLE | LinuxValues.TASK_UNINTERRUPTIBLE)) != 0;
    }

    private static boolean isRunning(int state) {
        // special case, this means ALL STATES ARE 0
        // this is effectively an anti-state
        return state == 0;
    }

    /**
     * Get the ProcessStatus from the value of the state in the kernel
     *
     * @param prevState the prev state
     * @return The {@link ProcessStatus} enum value
     */
    public static ProcessStatus getStatusFromKernelState(Long prevState) {
        /*
         * Empirical observations and look into the linux code have
         * shown that the TASK_STATE_MAX flag is used internally and
         * |'ed with other states, most often the running state, so it
         * is ignored from the prevState value.
         *
         * Since Linux 4.1, the TASK_NOLOAD state was created and
         * TASK_STATE_MAX is now 2048. We use TASK_NOLOAD as the new max
         * because it does not modify the displayed state value.
         */
        int state = (int) (prevState & (LinuxValues.TASK_NOLOAD - 1));

        if (isRunning(state)) {
            return WAIT_CPU;
        } else if (isWaiting(state)) {
            return WAIT_BLOCKED;
        } else if (isDead(state)) {
            return NOT_ALIVE;
        } else {
            return WAIT_UNKNOWN;
        }
    }

}
