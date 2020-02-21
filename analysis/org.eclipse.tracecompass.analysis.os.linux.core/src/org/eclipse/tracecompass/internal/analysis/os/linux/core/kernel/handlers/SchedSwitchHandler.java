/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
        Integer prevTid = content.getFieldValue(Integer.class, getLayout().fieldPrevTid());
        Long prevState = content.getFieldValue(Long.class, getLayout().fieldPrevState());
        Integer prevPrio = content.getFieldValue(Integer.class, getLayout().fieldPrevPrio());
        String nextProcessName = content.getFieldValue(String.class, getLayout().fieldNextComm());
        Integer nextTid = content.getFieldValue(Integer.class, getLayout().fieldNextTid());
        Integer nextPrio = content.getFieldValue(Integer.class, getLayout().fieldNextPrio());

        /* Will never return null since "cpu" is null checked */
        if (prevTid == null || prevState == null || nextTid == null) {
            return;
        }
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
        ss.modifyAttribute(timestamp, cpu, quark);

        /* Set the exec name of the former process */
        setProcessExecName(ss, prevProcessName, formerThreadNode, timestamp);

        /* Set the exec name of the new process */
        if (nextProcessName != null) {
            setProcessExecName(ss, nextProcessName, newCurrentThreadNode, timestamp);
        }

        /* Set the current prio for the former process */
        if (prevPrio != null) {
            setProcessPrio(ss, prevPrio, formerThreadNode, timestamp);
        }

        /* Set the current prio for the new process */
        if (nextPrio != null) {
            setProcessPrio(ss, nextPrio, newCurrentThreadNode, timestamp);
        }

        /* Set the current scheduled process on the relevant CPU */
        int currentCPUNode = KernelEventHandlerUtils.getCurrentCPUNode(cpu, ss);
        setCpuProcess(ss, nextTid, timestamp, currentCPUNode);

        /* Set the status of the CPU itself */
        setCpuStatus(ss, nextTid, newCurrentThreadNode, timestamp, currentCPUNode, cpu);
    }

    private static void setOldProcessStatus(ITmfStateSystemBuilder ss, Long prevState, Integer formerThreadNode, int cpu, long timestamp) {
        ITmfStateValue value = ProcessStatus.getStatusFromKernelState(prevState).getStateValue();

        ss.modifyAttribute(timestamp, value.unboxValue(), formerThreadNode);

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
        ss.modifyAttribute(timestamp, value.unboxValue(), quark);
    }

    private static void setCpuStatus(ITmfStateSystemBuilder ss, Integer nextTid, Integer newCurrentThreadNode, long timestamp, int currentCPUNode, int cpu) {
        if (nextTid > 0) {
            /* Check if the entering process is in kernel or user mode */
            int quark = ss.getQuarkRelativeAndAdd(newCurrentThreadNode, Attributes.SYSTEM_CALL);
            ITmfStateValue value;
            ITmfStateValue queryOngoingState = ss.queryOngoingState(quark);
            if (queryOngoingState.isNull()) {
                value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
            } else {
                value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
            }
            ss.modifyAttribute(timestamp, value.unboxValue(), currentCPUNode);
        } else {
            KernelEventHandlerUtils.updateCpuStatus(timestamp, cpu, ss);
        }
    }

    private static void setCpuProcess(ITmfStateSystemBuilder ss, Integer nextTid, long timestamp, int currentCPUNode) {
        int quark;
        quark = ss.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
        ss.modifyAttribute(timestamp, nextTid, quark);
    }

    private static void setProcessPrio(ITmfStateSystemBuilder ss, Integer prio, Integer threadNode, long timestamp) {
        int quark;
        quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.PRIO);
        ss.modifyAttribute(timestamp, prio, quark);
    }

    private static void setProcessExecName(ITmfStateSystemBuilder ss, String processName, Integer threadNode, long timestamp) {
        int quark;
        quark = ss.getQuarkRelativeAndAdd(threadNode, Attributes.EXEC_NAME);
        ss.modifyAttribute(timestamp, processName, quark);
    }

}
