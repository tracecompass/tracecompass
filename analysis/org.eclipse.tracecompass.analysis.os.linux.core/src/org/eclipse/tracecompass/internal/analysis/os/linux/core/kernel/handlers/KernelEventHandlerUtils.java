/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.handlers;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.StateValues;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
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
     */
    public static int getCurrentThreadNode(Integer cpuNumber, ITmfStateSystemBuilder ss) {
        /*
         * Shortcut for the "current thread" attribute node. It requires
         * querying the current CPU's current thread.
         */
        int quark = ss.getQuarkRelativeAndAdd(getCurrentCPUNode(cpuNumber, ss), Attributes.CURRENT_THREAD);
        ITmfStateValue value = ss.queryOngoingState(quark);
        int thread = value.isNull() ? -1 : value.unboxInt();
        return ss.getQuarkRelativeAndAdd(getNodeThreads(ss), Attributes.buildThreadAttributeName(thread, cpuNumber));
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
     * @throws TimeRangeException
     *             the time is out of range
     * @throws StateValueTypeException
     *             the attribute was not set with int values
     */
    public static void setProcessToRunning(long timestamp, int currentThreadNode, ITmfStateSystemBuilder ssb)
            throws TimeRangeException,
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
        ssb.modifyAttribute(timestamp, value, currentThreadNode);
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
     * @throws TimeRangeException
     *             the time is out of range
     */
    public static void cpuExitInterrupt(long timestamp, Integer cpuNumber, ITmfStateSystemBuilder ssb)
            throws StateValueTypeException, TimeRangeException {
        int quark;
        int currentCPUNode = getCurrentCPUNode(cpuNumber, ssb);

        quark = ssb.getQuarkRelativeAndAdd(currentCPUNode, Attributes.STATUS);
        ITmfStateValue value = getCpuStatus(ssb, currentCPUNode);
        ssb.modifyAttribute(timestamp, value, quark);
    }

    /**
     * Get the ongoing Status state of a CPU.
     *
     * This will look through the states of the
     *
     * <ul>
     * <li>IRQ</li>
     * <li>Soft IRQ</li>
     * <li>Process</li>
     * </ul>
     *
     * under the CPU, giving priority to states higher in the list. If the state
     * is a null value, we continue looking down the list.
     *
     * @param ssb
     *            The state system
     * @param cpuQuark
     *            The *quark* of the CPU we are looking for. Careful, this is
     *            NOT the CPU number (or attribute name)!
     * @return The state value that represents the status of the given CPU
     */
    private static ITmfStateValue getCpuStatus(ITmfStateSystemBuilder ssb, int cpuQuark) {

        /* Check if there is a IRQ running */
        int irqQuarks = ssb.getQuarkRelativeAndAdd(cpuQuark, Attributes.IRQS);
        List<Integer> irqs = ssb.getSubAttributes(irqQuarks, false);
        for (Integer quark : irqs) {
            final ITmfStateValue irqState = ssb.queryOngoingState(quark.intValue());
            if (!irqState.isNull()) {
                return irqState;
            }
        }

        /* Check if there is a soft IRQ running */
        int softIrqQuarks = ssb.getQuarkRelativeAndAdd(cpuQuark, Attributes.SOFT_IRQS);
        List<Integer> softIrqs = ssb.getSubAttributes(softIrqQuarks, false);
        for (Integer quark : softIrqs) {
            final ITmfStateValue softIrqState = ssb.queryOngoingState(quark.intValue());
            if (!softIrqState.isNull()) {
                return softIrqState;
            }
        }

        /*
         * Check if there is a thread running. If not, report IDLE. If there is,
         * report the running state of the thread (usermode or system call).
         */
        int currentThreadQuark = ssb.getQuarkRelativeAndAdd(cpuQuark, Attributes.CURRENT_THREAD);
        ITmfStateValue currentThreadState = ssb.queryOngoingState(currentThreadQuark);
        if (currentThreadState.isNull()) {
            return TmfStateValue.nullValue();
        }
        int tid = currentThreadState.unboxInt();
        if (tid == 0) {
            return StateValues.CPU_STATUS_IDLE_VALUE;
        }
        int threadSystemCallQuark = ssb.getQuarkAbsoluteAndAdd(Attributes.THREADS, Integer.toString(tid), Attributes.SYSTEM_CALL);
        return (ssb.queryOngoingState(threadSystemCallQuark).isNull() ? StateValues.CPU_STATUS_RUN_USERMODE_VALUE : StateValues.CPU_STATUS_RUN_SYSCALL_VALUE);
    }
}
