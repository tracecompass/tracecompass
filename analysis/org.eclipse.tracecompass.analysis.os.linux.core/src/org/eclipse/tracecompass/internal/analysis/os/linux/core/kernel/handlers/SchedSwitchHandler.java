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

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
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
        String prevProcessName = checkNotNull((String) content.getField(getLayout().fieldPrevComm()).getValue());
        Integer prevTid = ((Long) content.getField(getLayout().fieldPrevTid()).getValue()).intValue();
        Long prevState = checkNotNull((Long) content.getField(getLayout().fieldPrevState()).getValue());
        Integer prevPrio = ((Long) content.getField(getLayout().fieldPrevPrio()).getValue()).intValue();
        String nextProcessName = checkNotNull((String) content.getField(getLayout().fieldNextComm()).getValue());
        Integer nextTid = ((Long) content.getField(getLayout().fieldNextTid()).getValue()).intValue();
        Integer nextPrio = ((Long) content.getField(getLayout().fieldNextPrio()).getValue()).intValue();

        /* Will never return null since "cpu" is null checked */
        String formerThreadAttributeName = Attributes.buildThreadAttributeName(prevTid, cpu);
        String currenThreadAttributeName = Attributes.buildThreadAttributeName(nextTid, cpu);

        int nodeThreads = KernelEventHandlerUtils.getNodeThreads(ss);
        int formerThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, formerThreadAttributeName);
        int newCurrentThreadNode = ss.getQuarkRelativeAndAdd(nodeThreads, currenThreadAttributeName);

        long timestamp = KernelEventHandlerUtils.getTimestamp(event);
        /*
         * Set the status of the process that got scheduled out. This will also
         * set it's current CPU run queue accordingly.
         */
        setOldProcessStatus(ss, prevState, formerThreadNode, cpu, timestamp);

        /* Set the status of the new scheduled process */
        KernelEventHandlerUtils.setProcessToRunning(timestamp, newCurrentThreadNode, ss);

        /*
         * Set the current CPU run queue of the new process. Should be already
         * set if we've seen the previous sched_wakeup, but doesn't hurt to set
         * it here too.
         */
        int quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.CURRENT_CPU_RQ);
        ITmfStateValue value = TmfStateValue.newValueInt(cpu);
        ss.modifyAttribute(timestamp, value, quark);

        /* Set the exec name of the former process */
        setProcessExecName(ss, prevProcessName, formerThreadNode, timestamp);

        /* Set the exec name of the new process */
        setProcessExecName(ss, nextProcessName, newCurrentThreadNode, timestamp);

        /* Set the current prio for the former process */
        setProcessPrio(ss, prevPrio, formerThreadNode, timestamp);

        /* Set the current prio for the new process */
        setProcessPrio(ss, nextPrio, newCurrentThreadNode, timestamp);

        /* Set the current scheduled process on the relevant CPU */
        int currentCPUNode = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        setCpuProcess(ss, nextTid, timestamp, currentCPUNode);

        /* Set the status of the CPU itself */
        setCpuStatus(ss, nextTid, newCurrentThreadNode, timestamp, currentCPUNode);
    }

    private static void setOldProcessStatus(ITmfStateSystemBuilder ss, Long prevState, Integer formerThreadNode, int cpu, long timestamp) {
        ITmfStateValue value = ProcessStatus.getStatusFromKernelState(prevState).getStateValue();

        ss.modifyAttribute(timestamp, value, formerThreadNode);

        boolean staysOnRunQueue = ProcessStatus.WAIT_CPU.getStateValue().equals(value);
        int quark = ss.getQuarkRelativeAndAdd(formerThreadNode, Attributes.CURRENT_CPU_RQ);
        if (staysOnRunQueue) {
            /*
             * Set the thread's run queue. This will often be redundant with
             * previous events, but it may be the first time we see the
             * information too.
             */
            value = TmfStateValue.newValueInt(cpu);
        } else {
            value = TmfStateValue.nullValue();
        }
        ss.modifyAttribute(timestamp, value, quark);
    }

    private static void setCpuStatus(ITmfStateSystemBuilder ss, Integer nextTid, Integer newCurrentThreadNode, long timestamp, int currentCPUNode) {
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
        ss.modifyAttribute(timestamp, value, currentCPUNode);
    }

    private static void setCpuProcess(ITmfStateSystemBuilder ss, Integer nextTid, long timestamp, int currentCPUNode) {
        int quark;
        ITmfStateValue value;
        quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
        value = TmfStateValue.newValueInt(nextTid);
        ss.modifyAttribute(timestamp, value, quark);
    }

    private static void setProcessPrio(ITmfStateSystemBuilder ss, Integer prio, Integer threadNode, long timestamp) {
        int quark;
        ITmfStateValue value;
        quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.PRIO);
        value = TmfStateValue.newValueInt(prio);
        ss.modifyAttribute(timestamp, value, quark);
    }

    private static void setProcessExecName(ITmfStateSystemBuilder ss, String processName, Integer threadNode, long timestamp) {
        int quark;
        ITmfStateValue value;
        quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.EXEC_NAME);
        value = TmfStateValue.newValueString(processName);
        ss.modifyAttribute(timestamp, value, quark);
    }

}
