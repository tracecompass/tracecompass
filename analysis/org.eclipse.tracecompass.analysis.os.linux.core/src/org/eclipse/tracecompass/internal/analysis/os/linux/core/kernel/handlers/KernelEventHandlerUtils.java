/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Kernel Event Handler Utils is a collection of static methods to be used in
 * subclasses of IKernelEventHandler.
 *
 * @author Matthew Khouzam
 * @author Francis Giraldeau
 */
public final class KernelEventHandlerUtils {

    private KernelEventHandlerUtils() {
    }

    /**
     * Get CPU
     *
     * @param event
     *            The event containing the cpu
     *
     * @return the CPU number (null for not set)
     */
    public static @Nullable Integer getCpu(ITmfEvent event) {
        Integer cpuObj = TmfTraceUtils.resolveIntEventAspectOfClassForEvent(event.getTrace(), TmfCpuAspect.class, event);
        if (cpuObj == null) {
            /* We couldn't find any CPU information, ignore this event */
            return null;
        }
        return cpuObj;
    }

    /**
     * Gets the current CPU quark
     *
     * @param cpuNumber
     *            The cpu number
     * @param ss
     *            the state system
     *
     * @return the current CPU quark -1 for not set
     */
    public static int getCurrentCPUNode(Integer cpuNumber, ITmfStateSystemBuilder ss) {
        return ss.getQuarkRelativeAndAdd(getNodeCPUs(ss), cpuNumber.toString());
    }

    /**
     * Get the timestamp of the event
     *
     * @param event
     *            the event containing the timestamp
     *
     * @return the timestamp in long format
     */
    public static long getTimestamp(ITmfEvent event) {
        return event.getTimestamp().toNanos();
    }

    /**
     * Get the current thread node
     *
     * @param cpuNumber
     *            The cpu number
     * @param ss
     *            the state system
     *
     * @return the current thread node quark
     * @throws AttributeNotFoundException
     *             current cpu node not found
     */
    public static int getCurrentThreadNode(Integer cpuNumber, ITmfStateSystemBuilder ss) throws AttributeNotFoundException {
        /*
         * Shortcut for the "current thread" attribute node. It requires
         * querying the current CPU's current thread.
         */
        int quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(cpuNumber, ss), Attributes.CURRENT_THREAD);
        ITmfStateValue value = ss.queryOngoingState(quark);
        int thread = value.isNull() ? -1 : value.unboxInt();
        return ss.getQuarkRelativeAndAdd(getNodeThreads(ss), String.valueOf(thread));
    }

    /**
     * When we want to set a process back to a "running" state, first check its
     * current System_call attribute. If there is a system call active, we put
     * the process back in the syscall state. If not, we put it back in user
     * mode state.
     *
     * @param timestamp
     *            the time in the state system of the change
     * @param currentThreadNode
     *            The current thread node
     * @param ssb
     *            the state system
     * @throws AttributeNotFoundException
     *             an attribute does not exists yet
     * @throws TimeRangeException
     *             the time is out of range
     * @throws StateValueTypeException
     *             the attribute was not set with int values
     */
    public static void setProcessToRunning(long timestamp, int currentThreadNode, ITmfStateSystemBuilder ssb)
            throws AttributeNotFoundException, TimeRangeException,
            StateValueTypeException {
        int quark;
        ITmfStateValue value;

        quark = ssb.getQuarkRelativeAndAdd(currentThreadNode, Attributes.SYSTEM_CALL);
        if (ssb.queryOngoingState(quark).isNull()) {
            /* We were in user mode before the interruption */
            value = StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE;
        } else {
            /* We were previously in kernel mode */
            value = StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE;
        }
        quark = ssb.getQuarkRelativeAndAdd(currentThreadNode, Attributes.STATUS);
        ssb.modifyAttribute(timestamp, value, quark);
    }

    /**
     * Get the IRQs node
     *
     * @param cpuNumber
     *            the cpu core
     * @param ss
     *            the state system
     * @return the IRQ node quark
     */
    public static int getNodeIRQs(int cpuNumber, ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS, Integer.toString(cpuNumber), Attributes.IRQS);
    }

    /**
     * Get the CPUs node
     *
     * @param ss
     *            the state system
     * @return the CPU node quark
     */
    public static int getNodeCPUs(ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS);
    }

    /**
     * Get the Soft IRQs node
     *
     * @param cpuNumber
     *            the cpu core
     * @param ss
     *            the state system
     * @return the Soft IRQ node quark
     */
    public static int getNodeSoftIRQs(int cpuNumber, ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.CPUS, Integer.toString(cpuNumber), Attributes.SOFT_IRQS);
    }

    /**
     * Get the threads node
     *
     * @param ss
     *            the state system
     * @return the threads quark
     */
    public static int getNodeThreads(ITmfStateSystemBuilder ss) {
        return ss.getQuarkAbsoluteAndAdd(Attributes.THREADS);
    }

    /**
     * Reset the CPU's status when it's coming out of an interruption.
     *
     * @param timestamp
     *            the time when the status of the cpu is "leaving irq"
     * @param cpuNumber
     *            the cpu returning to its previous state
     *
     * @param ssb
     *            State system
     * @throws StateValueTypeException
     *             the attribute is not set as an int
     * @throws AttributeNotFoundException
     *             the attribute was not created yet
     * @throws TimeRangeException
     *             the time is out of range
     */
    public static void cpuExitInterrupt(long timestamp, Integer cpuNumber, ITmfStateSystemBuilder ssb)
            throws StateValueTypeException, AttributeNotFoundException,
            TimeRangeException {
        int quark;
        ITmfStateValue value;
        int currentCPUNode = getCurrentCPUNode(cpuNumber, ssb);

        quark = ssb.getQuarkRelativeAndAdd(currentCPUNode, Attributes.CURRENT_THREAD);
        if (ssb.queryOngoingState(quark).unboxInt() > 0) {
            /* There was a process on the CPU */
            quark = ssb.getQuarkRelativeAndAdd(currentCPUNode, Attributes.SYSTEM_CALL);
            if (ssb.queryOngoingState(quark).isNull()) {
                /* That process was in user mode */
                value = StateValues.CPU_STATUS_RUN_USERMODE_VALUE;
            } else {
                /* That process was in a system call */
                value = StateValues.CPU_STATUS_RUN_SYSCALL_VALUE;
            }
        } else {
            /* There was no real process scheduled, CPU was idle */
            value = StateValues.CPU_STATUS_IDLE_VALUE;
        }
        quark = ssb.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
        ssb.modifyAttribute(timestamp, value, quark);
    }

}
