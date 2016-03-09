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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;

import com.google.common.collect.ImmutableList;

/**
 * Event and field definitions for perf traces in CTF format.
 *
 * @author Alexandre Montplaisir
 */
public class PerfEventLayout implements IKernelAnalysisEventLayout {

    private PerfEventLayout() {}

    private static final PerfEventLayout INSTANCE = new PerfEventLayout();

    /**
     * Get the singleton instance of this event layout object.
     *
     * @return The instance
     */
    public static PerfEventLayout getInstance() {
        return INSTANCE;
    }

    // ------------------------------------------------------------------------
    // Event names
    // ------------------------------------------------------------------------

    @Override
    public String eventIrqHandlerEntry() {
        return "irq:irq_handler_exit"; //$NON-NLS-1$
    }

    @Override
    public String eventIrqHandlerExit() {
        return "irq:irq_handler_entry"; //$NON-NLS-1$
    }

    @Override
    public String eventSoftIrqEntry() {
        return "irq:softirq_entry"; //$NON-NLS-1$
    }

    @Override
    public String eventSoftIrqExit() {
        return "irq:softirq_exit"; //$NON-NLS-1$
    }

    @Override
    public String eventSoftIrqRaise() {
        return "irq:softirq_raise"; //$NON-NLS-1$
    }

    @Override
    public String eventSchedSwitch() {
        return "sched:sched_switch"; //$NON-NLS-1$
    }

    @Override
    public String eventSchedPiSetprio() {
        return "sched:sched_pi_setprio"; //$NON-NLS-1$
    }

    private static final Collection<String> WAKEUP_EVENTS =
            ImmutableList.of("sched:sched_wakeup", "sched:sched_wakeup_new"); //$NON-NLS-1$ //$NON-NLS-2$

    @Override
    public Collection<String> eventsSchedWakeup() {
        return WAKEUP_EVENTS;
    }

    @Override
    public String eventSchedProcessFork() {
        return "sched:sched_process_fork"; //$NON-NLS-1$
    }

    @Override
    public String eventSchedProcessExit() {
        return "sched:sched_process_exit"; //$NON-NLS-1$
    }

    @Override
    public String eventSchedProcessFree() {
        return "sched:sched_process_free"; //$NON-NLS-1$
    }

    @Override
    public @Nullable String eventStatedumpProcessState() {
        /* Not present in perf traces */
        return null;
    }

    @Override
    public String eventSyscallEntryPrefix() {
        return "raw_syscalls:sys_enter"; //$NON-NLS-1$
    }

    @Override
    public String eventCompatSyscallEntryPrefix() {
        return eventSyscallEntryPrefix();
    }

    @Override
    public String eventSyscallExitPrefix() {
        return "raw_syscalls:sys_exit"; //$NON-NLS-1$
    }

    @Override
    public String eventCompatSyscallExitPrefix() {
        return eventSyscallExitPrefix();
    }

    @Override
    public String eventSchedProcessExec() {
        return "sched:sched_process_exec"; //$NON-NLS-1$
    }

    @Override
    public String eventSchedProcessWakeup() {
        return "sched:sched_process_wakeup"; //$NON-NLS-1$
    }

    @Override
    public String eventSchedProcessWakeupNew() {
        return "sched:process_wakeup_new";  //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerStart() {
        return "timer:hrtimer_start";  //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerCancel() {
        return "timer:hrtimer_cancel"; //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerExpireEntry() {
        return "timer:hrtimer_expire_entry"; //$NON-NLS-1$
    }

    @Override
    public String eventHRTimerExpireExit() {
        return "timer:hrtimer_expire_exit"; //$NON-NLS-1$
    }

    // ------------------------------------------------------------------------
    // Field names
    // ------------------------------------------------------------------------

    @Override
    public String fieldIrq() {
        return "irq"; //$NON-NLS-1$
    }

    @Override
    public String fieldVec() {
        return "vec"; //$NON-NLS-1$
    }

    @Override
    public String fieldTid() {
        return "pid"; //$NON-NLS-1$
    }

    @Override
    public String fieldPrevTid() {
        return "prev_pid"; //$NON-NLS-1$
    }

    @Override
    public String fieldPrevState() {
        return "prev_state"; //$NON-NLS-1$
    }

    @Override
    public String fieldNextComm() {
        return "next_comm"; //$NON-NLS-1$
    }

    @Override
    public String fieldNextTid() {
        return "next_pid"; //$NON-NLS-1$
    }

    @Override
    public String fieldChildComm() {
        return "child_comm"; //$NON-NLS-1$
    }

    @Override
    public String fieldParentTid() {
        return "parent_pid"; //$NON-NLS-1$
    }

    @Override
    public String fieldChildTid() {
        return "child_pid"; //$NON-NLS-1$
    }

    @Override
    public String fieldPrio() {
        return "prio"; //$NON-NLS-1$
    }

    @Override
    public String fieldNewPrio() {
        return "newprio"; //$NON-NLS-1$
    }

    @Override
    public String fieldNextPrio() {
        return "next_prio"; //$NON-NLS-1$
    }

    @Override
    public String fieldComm() {
        return "comm"; //$NON-NLS-1$
    }

    @Override
    public String fieldName() {
        return "name"; //$NON-NLS-1$
    }

    @Override
    public String fieldStatus() {
        return "status"; //$NON-NLS-1$
    }

    @Override
    public String fieldPrevComm() {
        return "prev_comm"; //$NON-NLS-1$
    }

    @Override
    public String fieldFilename() {
        return "filename"; //$NON-NLS-1$
    }

    @Override
    public String fieldHRtimer() {
        return "hrtimer"; //$NON-NLS-1$
    }

    @Override
    public String fieldHRtimerFunction() {
        return "function"; //$NON-NLS-1$
    }

    @Override
    public String fieldHRtimerExpires() {
        return "expires"; //$NON-NLS-1$
    }

    @Override
    public String fieldHRtimerSoftexpires() {
        return "softexpires"; //$NON-NLS-1$
    }

    @Override
    public String fieldHRtimerNow() {
        return "now"; //$NON-NLS-1$
    }

}
