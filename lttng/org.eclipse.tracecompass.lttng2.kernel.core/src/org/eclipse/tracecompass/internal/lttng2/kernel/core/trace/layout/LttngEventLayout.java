/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.kernel.core.trace.layout;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;

import com.google.common.collect.ImmutableList;

/**
 * This file defines all the known event and field names for LTTng kernel
 * traces, for versions of lttng-modules up to 2.5.
 *
 * These should not be externalized, since they need to match exactly what the
 * tracer outputs. If you want to localize them in a view, you should do a
 * mapping in the view itself.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("nls")
public class LttngEventLayout implements IKernelAnalysisEventLayout {

    /* Event names */
    private static final String IRQ_HANDLER_ENTRY = "irq_handler_entry";
    private static final String IRQ_HANDLER_EXIT = "irq_handler_exit";
    private static final String SOFTIRQ_ENTRY = "softirq_entry";
    private static final String SOFTIRQ_EXIT = "softirq_exit";
    private static final String SOFTIRQ_RAISE = "softirq_raise";
    private static final String HRTIMER_START = "hrtimer_start";
    private static final String HRTIMER_CANCEL = "hrtimer_cancel";
    private static final String HRTIMER_EXPIRE_ENTRY = "hrtimer_expire_entry";
    private static final String HRTIMER_EXPIRE_EXIT = "hrtimer_expire_exit";
    private static final String SCHED_SWITCH = "sched_switch";
    private static final String SCHED_PI_SETPRIO = "sched_pi_setprio";

    private static final String SCHED_TTWU = "sched_ttwu";
    private static final String SCHED_WAKEUP = "sched_wakeup";
    private static final String SCHED_WAKEUP_NEW = "sched_wakeup_new";
    private static final Collection<String> SCHED_WAKEUP_EVENTS =
            ImmutableList.of(SCHED_WAKEUP, SCHED_WAKEUP_NEW);

    private static final String SCHED_PROCESS_FORK = "sched_process_fork";
    private static final String SCHED_PROCESS_EXIT = "sched_process_exit";
    private static final String SCHED_PROCESS_FREE = "sched_process_free";
    private static final String SCHED_PROCESS_EXEC = "sched_process_exec";
    private static final String STATEDUMP_PROCESS_STATE = "lttng_statedump_process_state";

    private static final String SYSCALL_ENTRY_PREFIX = "sys_";
    private static final String COMPAT_SYSCALL_ENTRY_PREFIX = "compat_sys_";
    private static final String SYSCALL_EXIT_PREFIX = "exit_syscall";

    /* Field names */
    private static final String IRQ = "irq";
    private static final String TID = "tid";
    private static final String VEC = "vec";
    private static final String PREV_TID = "prev_tid";
    private static final String PREV_STATE = "prev_state";
    private static final String NEXT_COMM = "next_comm";
    private static final String NEXT_TID = "next_tid";
    private static final String PARENT_TID = "parent_tid";
    private static final String CHILD_COMM = "child_comm";
    private static final String CHILD_TID = "child_tid";
    private static final String PRIO = "prio";
    private static final String NEXT_PRIO = "next_prio";
    private static final String NEW_PRIO = "newprio";
    private static final String COMM = "comm";
    private static final String NAME = "name";
    private static final String STATUS = "status";
    private static final String PREV_COMM = "prev_comm";
    private static final String FILENAME = "filename";
    private static final String HRTIMER = "hrtimer";
    private static final String HRTIMER_FUNCTION = "function";
    private static final String HRTIMER_EXPIRES = "expires";
    private static final String HRTIMER_NOW = "now";
    private static final String HRTIMER_SOFT_EXPIRES = "softexpires";

    /** All instances are the same. Only provide a static instance getter */
    protected LttngEventLayout() {
    }

    private static final IKernelAnalysisEventLayout INSTANCE = new LttngEventLayout();

    /**
     * Get an instance of this event layout
     *
     * This object is completely immutable, so no need to create additional
     * instances via the constructor.
     *
     * @return The instance
     */
    public static IKernelAnalysisEventLayout getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------
    // Event names
    // ------------------------------------------------------------------------

    @Override
    public String eventIrqHandlerEntry() {
        return IRQ_HANDLER_ENTRY;
    }

    @Override
    public String eventIrqHandlerExit() {
        return IRQ_HANDLER_EXIT;
    }

    @Override
    public String eventSoftIrqEntry() {
        return SOFTIRQ_ENTRY;
    }

    @Override
    public String eventSoftIrqExit() {
        return SOFTIRQ_EXIT;
    }

    @Override
    public String eventSoftIrqRaise() {
        return SOFTIRQ_RAISE;
    }

    @Override
    public String eventSchedSwitch() {
        return SCHED_SWITCH;
    }

    @Override
    public String eventSchedPiSetprio() {
        return SCHED_PI_SETPRIO;
    }

    @Override
    public Collection<String> eventsSchedWakeup() {
        return SCHED_WAKEUP_EVENTS;
    }

    @Override
    public String eventSchedProcessFork() {
        return SCHED_PROCESS_FORK;
    }

    @Override
    public String eventSchedProcessExit() {
        return SCHED_PROCESS_EXIT;
    }

    @Override
    public String eventSchedProcessFree() {
        return SCHED_PROCESS_FREE;
    }

    @Override
    public @NonNull String eventStatedumpProcessState() {
        return STATEDUMP_PROCESS_STATE;
    }

    @Override
    public String eventSyscallEntryPrefix() {
        return SYSCALL_ENTRY_PREFIX;
    }

    @Override
    public String eventCompatSyscallEntryPrefix() {
        return COMPAT_SYSCALL_ENTRY_PREFIX;
    }

    @Override
    public String eventSyscallExitPrefix() {
        return SYSCALL_EXIT_PREFIX;
    }

    @Override
    public String eventCompatSyscallExitPrefix() {
        /*
         * In LTTng < 2.6, the same generic event name is used for both standard
         * and compat syscalls.
         */
        return SYSCALL_EXIT_PREFIX;
    }

    // ------------------------------------------------------------------------
    // Event field names
    // ------------------------------------------------------------------------

    @Override
    public String fieldIrq() {
        return IRQ;
    }

    @Override
    public String fieldVec() {
        return VEC;
    }

    @Override
    public String fieldTid() {
        return TID;
    }

    @Override
    public String fieldPrevTid() {
        return PREV_TID;
    }

    @Override
    public String fieldPrevState() {
        return PREV_STATE;
    }

    @Override
    public String fieldNextComm() {
        return NEXT_COMM;
    }

    @Override
    public String fieldNextTid() {
        return NEXT_TID;
    }

    @Override
    public String fieldChildComm() {
        return CHILD_COMM;
    }

    @Override
    public String fieldParentTid() {
        return PARENT_TID;
    }

    @Override
    public String fieldChildTid() {
        return CHILD_TID;
    }

    @Override
    public String fieldPrio() {
        return PRIO;
    }

    @Override
    public String fieldNewPrio() {
        return NEW_PRIO;
    }

    @Override
    public String fieldNextPrio() {
        return NEXT_PRIO;
    }

    @Override
    public String fieldComm() {
        return COMM;
    }

    @Override
    public String fieldName() {
        return NAME;
    }

    @Override
    public String fieldStatus() {
        return STATUS;
    }

    @Override
    public String fieldPrevComm() {
        return PREV_COMM;
    }

    @Override
    public String fieldFilename() {
        return FILENAME;
    }

    @Override
    public String eventSchedProcessExec() {
        return SCHED_PROCESS_EXEC;
    }

    @Override
    public String eventSchedProcessWakeup() {
        return SCHED_WAKEUP;
    }

    @Override
    public String eventSchedProcessWakeupNew() {
        return SCHED_WAKEUP_NEW;
    }

    @Override
    public String eventHRTimerStart() {
        return HRTIMER_START;
    }

    @Override
    public String eventHRTimerCancel() {
        return HRTIMER_CANCEL;
    }

    @Override
    public String eventHRTimerExpireEntry() {
        return HRTIMER_EXPIRE_ENTRY;
    }

    @Override
    public String eventHRTimerExpireExit() {
        return HRTIMER_EXPIRE_EXIT;
    }

    /**
     * Event indicating the source of the wakeup signal.
     *
     * @return The name of the event
     * @since 2.0
     */
    public String eventSchedProcessTTWU() {
        return SCHED_TTWU;
    }

    @Override
    public String fieldHRtimer() {
        return HRTIMER;
    }
    @Override
    public String fieldHRtimerFunction() {
        return HRTIMER_FUNCTION;
    }

    @Override
    public String fieldHRtimerExpires() {
        return HRTIMER_EXPIRES;
    }

    @Override
    public String fieldHRtimerSoftexpires() {
        return HRTIMER_SOFT_EXPIRES;
    }
    @Override
    public String fieldHRtimerNow() {
        return HRTIMER_NOW;
    }

}
