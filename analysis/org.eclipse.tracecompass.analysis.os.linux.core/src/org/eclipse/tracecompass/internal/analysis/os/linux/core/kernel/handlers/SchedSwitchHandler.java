/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Matthew Khouzam - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.LinuxValues;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;

/**
 * Scheduler switch event handler
 */
public class SchedSwitchHandler extends KernelEventHandler {

    /**
     * Constructor
     *
     * @param layout
     *            event layout
     */
    public SchedSwitchHandler(IKernelAnalysisEventLayout layout) {
        super(layout);
    }

    @Override
    public void handleEvent(ITmfStateSystemBuilder ss, ITmfEvent event) throws AttributeNotFoundException {
        Integer cpu = KernelEventHandlerUtils.getCpu(event);
        if (cpu == null) {
            return;
        }

        ITmfEventField content = event.getContent();
        Integer prevTid = ((Long) content.getField(getLayout().fieldPrevTid()).getValue()).intValue();
        Long prevState = checkNotNull((Long) content.getField(getLayout().fieldPrevState()).getValue());
        String nextProcessName = checkNotNull((String) content.getField(getLayout().fieldNextComm()).getValue());
        Integer nextTid = ((Long) content.getField(getLayout().fieldNextTid()).getValue()).intValue();
        Integer nextPrio = ((Long) content.getField(getLayout().fieldNextPrio()).getValue()).intValue();

        int nodeThreads = KernelEventHandlerUtils.getNodeThreads(ss);
        int formerThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, prevTid.toString());
        int newCurrentThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, nextTid.toString());

        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        /* Set the status of the process that got scheduled out. */
        setOldProcessStatus(ss, prevState, formerThreadNode, timestamp);

        /* Set the status of the new scheduled process */
        KernelEventHandlerUtils.setProcessToRunning(timestamp, newCurrentThreadNode, ss);

        /* Set the exec name of the new process */
        setNewProcessExecName(ss, nextProcessName, newCurrentThreadNode, timestamp);

        /* Set the current prio for the new process */
        setNewProcessPio(ss, nextPrio, newCurrentThreadNode, timestamp);

        /* Make sure the PPID and system_call sub-attributes exist */
        ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
        ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.PPID);

        /* Set the current scheduled process on the relevant CPU */
        int currentCPUNode = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        setCpuProcess(ss, nextTid, timestamp, currentCPUNode);

        /* Set the status of the CPU itself */
        setCpuStatus(ss, nextTid, newCurrentThreadNode, timestamp, currentCPUNode);
    }

    private static void setOldProcessStatus(ITmfStateSystemBuilder ss, Long prevState, Integer formerThreadNode, long timestamp) throws AttributeNotFoundException {
        ITmfStateValue value;
        /*
         * Empirical observations and look into the linux code have shown that
         * the TASK_STATE_MAX flag is used internally and |'ed with other
         * states, most often the running state, so it is ignored from the
         * prevState value.
         */
        int state = (int) (prevState & ~(LinuxValues.TASK_STATE_MAX));

        if (isRunning(state)) {
            value = StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE;
        } else if (isWaiting(state)) {
            value = StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE;
        } else if (isDead(state)) {
            value = TmfStateValue.nullValue();
        } else {
            value = StateValues.PROCESS_STATUS_WAIT_UNKNOWN_VALUE;
        }
        int quark = ss.getQuarkRelativeAndAdd(formerThreadNode, Attributes.STATUS);
        ss.modifyAttribute(timestamp, value, quark);

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

    private static void setCpuStatus(ITmfStateSystemBuilder ss, Integer nextTid, Integer newCurrentThreadNode, long timestamp, int currentCPUNode) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        if (nextTid > 0) {
            /* Check if the entering process is in kernel or user mode */
            quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
            ITmfStateValue queryOngoingState = ss.queryOngoingState(quark);
            if (queryOngoingState.isNull()) {
                value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
            } else {
                value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
            }
        } else {
            value = StateValues.CPU_STATUS_IDLE_VALUE;
        }
        quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
        ss.modifyAttribute(timestamp, value, quark);
    }

    private static void setCpuProcess(ITmfStateSystemBuilder ss, Integer nextTid, long timestamp, int currentCPUNode) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
        value = TmfStateValue.newValueInt(nextTid);
        ss.modifyAttribute(timestamp, value, quark);
    }

    private static void setNewProcessPio(ITmfStateSystemBuilder ss, Integer nextPrio, Integer newCurrentThreadNode, long timestamp) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.PRIO);
        value = TmfStateValue.newValueInt(nextPrio);
        ss.modifyAttribute(timestamp, value, quark);
    }

    private static void setNewProcessExecName(ITmfStateSystemBuilder ss, String nextProcessName, Integer newCurrentThreadNode, long timestamp) throws AttributeNotFoundException {
        int quark;
        ITmfStateValue value;
        quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.EXEC_NAME);
        value = TmfStateValue.newValueString(nextProcessName);
        ss.modifyAttribute(timestamp, value, quark);
    }

}
