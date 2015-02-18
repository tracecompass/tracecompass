/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.trace;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;

import com.google.common.collect.ImmutableList;

/**
 * A kernel event layout to be used by default. This can be useful for
 * data-driven traces for example, where they can provide whatever event names
 * they want.
 *
 * Due to historical reasons, the definitions are the same as LTTng event names.
 *
 * @author Alexandre Montplaisir
 */
public class DefaultEventLayout implements IKernelAnalysisEventLayout{

    /* Event names */
    private static final String IRQ_HANDLER_ENTRY = "irq_handler_entry"; //$NON-NLS-1$
    private static final String IRQ_HANDLER_EXIT = "irq_handler_exit"; //$NON-NLS-1$
    private static final String SOFTIRQ_ENTRY = "softirq_entry"; //$NON-NLS-1$
    private static final String SOFTIRQ_EXIT = "softirq_exit"; //$NON-NLS-1$
    private static final String SOFTIRQ_RAISE = "softirq_raise"; //$NON-NLS-1$
    private static final String SCHED_SWITCH = "sched_switch"; //$NON-NLS-1$
    private static final String SCHED_PI_SETPRIO = "sched_pi_setprio"; //$NON-NLS-1$

    private static final Collection<String> SCHED_WAKEUP_EVENTS =
            checkNotNull(ImmutableList.of("sched_wakeup", "sched_wakeup_new")); //$NON-NLS-1$ //$NON-NLS-2$

    private static final String SCHED_PROCESS_FORK = "sched_process_fork"; //$NON-NLS-1$
    private static final String SCHED_PROCESS_EXIT = "sched_process_exit"; //$NON-NLS-1$
    private static final String SCHED_PROCESS_FREE = "sched_process_free"; //$NON-NLS-1$
    private static final String STATEDUMP_PROCESS_STATE = "lttng_statedump_process_state"; //$NON-NLS-1$

    private static final String SYSCALL_ENTRY_PREFIX = "sys_"; //$NON-NLS-1$
    private static final String COMPAT_SYSCALL_ENTRY_PREFIX = "compat_sys_"; //$NON-NLS-1$
    private static final String SYSCALL_EXIT_PREFIX = "exit_syscall"; //$NON-NLS-1$

    /* Field names */
    private static final String IRQ = "irq"; //$NON-NLS-1$
    private static final String TID = "tid"; //$NON-NLS-1$
    private static final String VEC = "vec"; //$NON-NLS-1$
    private static final String PREV_TID = "prev_tid"; //$NON-NLS-1$
    private static final String PREV_STATE = "prev_state"; //$NON-NLS-1$
    private static final String NEXT_COMM = "next_comm"; //$NON-NLS-1$
    private static final String NEXT_TID = "next_tid"; //$NON-NLS-1$
    private static final String PARENT_TID = "parent_tid"; //$NON-NLS-1$
    private static final String CHILD_COMM = "child_comm"; //$NON-NLS-1$
    private static final String CHILD_TID = "child_tid"; //$NON-NLS-1$
    private static final String PRIO = "prio"; //$NON-NLS-1$
    private static final String NEW_PRIO = "newprio"; //$NON-NLS-1$
    private static final String NEXT_PRIO = "next_prio"; //$NON-NLS-1$

    /** All instances are the same. Only provide a static instance getter */
    private DefaultEventLayout() {
    }

    /**
     * The instance of this event layout
     *
     * This object is completely immutable, so no need to create additional
     * instances via the constructor.
     */
    static final IKernelAnalysisEventLayout INSTANCE = new DefaultEventLayout();

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

}
