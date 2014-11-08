/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
    private static final String SCHED_SWITCH = "sched_switch";

    @SuppressWarnings("null")
    private static final @NonNull Collection<String> SCHED_WAKEUP_EVENTS = ImmutableList.of("sched_wakeup", "sched_wakeup_new");

    private static final String SCHED_PROCESS_FORK = "sched_process_fork";
    private static final String SCHED_PROCESS_EXIT = "sched_process_exit";
    private static final String SCHED_PROCESS_FREE = "sched_process_free";
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

}
