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
 * @since 1.0
 */
public class DefaultEventLayout implements IKernelAnalysisEventLayout {

    /* Event names */
    private static final String IRQ_HANDLER_ENTRY = "irq_handler_entry"; //$NON-NLS-1$
    private static final String IRQ_HANDLER_EXIT = "irq_handler_exit"; //$NON-NLS-1$
    private static final String SOFTIRQ_ENTRY = "softirq_entry"; //$NON-NLS-1$
    private static final String SOFTIRQ_EXIT = "softirq_exit"; //$NON-NLS-1$
    private static final String SOFTIRQ_RAISE = "softirq_raise"; //$NON-NLS-1$
    private static final String HRTIMER_START = "hrtimer_start"; //$NON-NLS-1$
    private static final String HRTIMER_CANCEL = "hrtimer_cancel"; //$NON-NLS-1$
    private static final String HRTIMER_EXPIRE_ENTRY = "hrtimer_expire_entry"; //$NON-NLS-1$
    private static final String HRTIMER_EXPIRE_EXIT = "hrtimer_expire_exit"; //$NON-NLS-1$
    private static final String SCHED_SWITCH = "sched_switch"; //$NON-NLS-1$
    private static final String SCHED_PI_SETPRIO = "sched_pi_setprio"; //$NON-NLS-1$

    private static final String SCHED_TTWU = "sched_ttwu"; //$NON-NLS-1$
    private static final String SCHED_WAKING = "sched_waking"; //$NON-NLS-1$
    private static final String SCHED_WAKEUP = "sched_wakeup"; //$NON-NLS-1$
    private static final String SCHED_WAKEUP_NEW = "sched_wakeup_new"; //$NON-NLS-1$
    private static final Collection<String> SCHED_WAKEUP_EVENTS =
            ImmutableList.of(SCHED_WAKEUP, SCHED_WAKEUP_NEW);

    private static final String SCHED_PROCESS_FORK = "sched_process_fork"; //$NON-NLS-1$
    private static final String SCHED_PROCESS_EXIT = "sched_process_exit"; //$NON-NLS-1$
    private static final String SCHED_PROCESS_FREE = "sched_process_free"; //$NON-NLS-1$
    private static final String SCHED_PROCESS_EXEC = "sched_process_exec"; //$NON-NLS-1$
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
    private static final String COMM = "comm"; //$NON-NLS-1$
    private static final String NAME = "name"; //$NON-NLS-1$
    private static final String STATUS = "status"; //$NON-NLS-1$
    private static final String PREV_COMM = "prev_comm"; //$NON-NLS-1$
    private static final String FILENAME = "filename"; //$NON-NLS-1$
    private static final String HRTIMER = "hrtimer"; //$NON-NLS-1$
    private static final String FUNCTION = "function"; //$NON-NLS-1$
    private static final String EXPIRES = "expires"; //$NON-NLS-1$
    private static final String NOW = "now"; //$NON-NLS-1$
    private static final String SOFT_EXPIRES = "softexpires"; //$NON-NLS-1$

    /**
     * Constructor, to be used by classes extending this one. To get an instance
     * of this class, INSTANCE should be used.
     *
     * @since 2.0
     */
    protected DefaultEventLayout() {
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

    /**
     * @since 1.0
     */
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

    /** @since 2.0 */
    @Override
    public String eventCompatSyscallExitPrefix() {
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

    /** @since 1.0 */
    @Override
    public String fieldPrio() {
        return PRIO;
    }

    /** @since 1.0 */
    @Override
    public String fieldNewPrio() {
        return NEW_PRIO;
    }

    /** @since 1.0 */
    @Override
    public String fieldNextPrio() {
        return NEXT_PRIO;
    }

    /** @since 2.0 */
    @Override
    public String fieldComm() {
        return COMM;
    }

    /** @since 2.0 */
    @Override
    public String fieldName() {
        return NAME;
    }

    /** @since 2.0 */
    @Override
    public String fieldStatus() {
        return STATUS;
    }

    /** @since 2.0 */
    @Override
    public String fieldPrevComm() {
        return PREV_COMM;
    }

    /** @since 2.0 */
    @Override
    public String fieldFilename() {
        return FILENAME;
    }

    /** @since 2.0 */
    @Override
    public String eventSchedProcessExec() {
        return SCHED_PROCESS_EXEC;
    }

    /** @since 2.0 */
    @Override
    public String eventSchedProcessWakeup() {
        return SCHED_WAKEUP;
    }

    /** @since 2.0 */
    @Override
    public String eventSchedProcessWakeupNew() {
        return SCHED_WAKEUP_NEW;
    }

    /** @since 2.0 */
    @Override
    public String eventHRTimerStart() {
        return HRTIMER_START;
    }

    /** @since 2.0 */
    @Override
    public String eventHRTimerCancel() {
        return HRTIMER_CANCEL;
    }

    /** @since 2.0 */
    @Override
    public String eventHRTimerExpireEntry() {
        return HRTIMER_EXPIRE_ENTRY;
    }

    /** @since 2.0 */
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

    /** @since 2.0 */
    @Override
    public String fieldHRtimer() {
        return HRTIMER;
    }

    /** @since 2.0 */
    @Override
    public String fieldHRtimerFunction() {
        return FUNCTION;
    }

    /** @since 2.0 */
    @Override
    public String fieldHRtimerExpires() {
        return EXPIRES;
    }

    /** @since 2.0 */
    @Override
    public String fieldHRtimerSoftexpires() {
        return SOFT_EXPIRES;
    }

    /** @since 2.0 */
    @Override
    public String fieldHRtimerNow() {
        return NOW;
    }

    /**
     * Event indicating the source of the wakeup signal.
     *
     * @return The name of the event
     * @since 2.0
     */
    public String eventSchedProcessWaking() {
        return SCHED_WAKING;
    }

}
