/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;

/**
 * Information provider utility class that retrieves thread-related information
 * from a Linux Kernel Analysis
 *
 * @author Geneviève Bastien
 */
public final class KernelThreadInformationProvider {

    private KernelThreadInformationProvider() {
    }

    /**
     * Get the ID of the thread running on the CPU at time ts
     *
     * TODO: This method may later be replaced by an aspect, when the aspect can
     * resolve to something that is not an event
     *
     * @param module
     *            The lttng kernel analysis instance to run this method on
     * @param cpuId
     *            The CPU number the process is running on
     * @param ts
     *            The timestamp at which we want the running process
     * @return The TID of the thread running on CPU cpuId at time ts or
     *         {@code null} if either no thread is running or we do not know.
     * @since 1.0
     */
    public static @Nullable Integer getThreadOnCpu(KernelAnalysisModule module, long cpuId, long ts) {
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return null;
        }
        try {
            int cpuQuark = ss.getQuarkAbsolute(Attributes.CPUS, Long.toString(cpuId), Attributes.CURRENT_THREAD);
            ITmfStateInterval interval = ss.querySingleState(ts, cpuQuark);
            ITmfStateValue val = interval.getStateValue();
            switch (val.getType()) {
            case INTEGER:
                return val.unboxInt();
            case LONG:
            case DOUBLE:
            case NULL:
            case STRING:
            default:
                break;
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
        }
        return null;
    }

    /**
     * Get the TIDs of the threads from an analysis
     *
     * @param module
     *            The lttng kernel analysis instance to run this method on
     * @return The set of TIDs corresponding to the threads
     * @since 1.0
     */
    public static Collection<Integer> getThreadIds(KernelAnalysisModule module) {
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return Collections.EMPTY_SET;
        }
        int threadQuark;
        try {
            threadQuark = ss.getQuarkAbsolute(Attributes.THREADS);
            Set<@NonNull Integer> tids = new TreeSet<>();
            for (Integer quark : ss.getSubAttributes(threadQuark, false)) {
                tids.add(Integer.parseInt(ss.getAttributeName(quark)));
            }
            return tids;
        } catch (AttributeNotFoundException e) {
        }
        return Collections.EMPTY_SET;
    }

    /**
     * Get the parent process ID of a thread
     *
     * @param module
     *            The lttng kernel analysis instance to run this method on
     * @param threadId
     *            The thread ID of the process for which to get the parent
     * @param ts
     *            The timestamp at which to get the parent
     * @return The parent PID or {@code null} if the PPID is not found.
     * @since 1.0
     */
    public static @Nullable Integer getParentPid(KernelAnalysisModule module, Integer threadId, long ts) {
        Integer ppid = null;
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return ppid;
        }
        Integer ppidNode;
        try {
            ppidNode = ss.getQuarkAbsolute(Attributes.THREADS, threadId.toString(), Attributes.PPID);
            ITmfStateInterval ppidInterval = ss.querySingleState(ts, ppidNode);
            ITmfStateValue ppidValue = ppidInterval.getStateValue();

            switch (ppidValue.getType()) {
            case INTEGER:
                ppid = NonNullUtils.checkNotNull(Integer.valueOf(ppidValue.unboxInt()));
                break;
            case DOUBLE:
            case LONG:
            case NULL:
            case STRING:
            default:
                break;
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
        }
        return ppid;
    }

    /**
     * Get the executable name of the thread ID. If the thread ID was used
     * multiple time or the name changed in between, it will return the last
     * name the thread has taken, or {@code null} if no name is found
     *
     * @param module
     *            The lttng kernel analysis instance to run this method on
     * @param threadId
     *            The thread ID of the process for which to get the name
     * @return The last executable name of this process, or {@code null} if not
     *         found
     * @since 1.0
     */
    public static @Nullable String getExecutableName(KernelAnalysisModule module, Integer threadId) {
        String execName = null;
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return execName;
        }
        Integer execNameNode;
        try {
            execNameNode = ss.getQuarkAbsolute(Attributes.THREADS, threadId.toString(), Attributes.EXEC_NAME);
            List<ITmfStateInterval> execNameIntervals = StateSystemUtils.queryHistoryRange(ss, execNameNode, ss.getStartTime(), ss.getCurrentEndTime());

            ITmfStateValue execNameValue;
            for (ITmfStateInterval interval : execNameIntervals) {
                execNameValue = interval.getStateValue();
                switch (execNameValue.getType()) {
                case STRING:
                    execName = execNameValue.unboxStr();
                    break;
                case DOUBLE:
                case LONG:
                case NULL:
                case INTEGER:
                default:
                    break;
                }
            }
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
        }
        return execName;
    }

    /**
     * Get the priority of a thread running at a time ts.
     *
     * @param module
     *            The kernel analysis instance to run this method on
     * @param threadId
     *            The thread ID of the target thread
     * @param ts
     *            The timestamp at which to get the priority
     * @return The priority of this thread, or {@code null} if not found
     * @since 1.0
     */
    public static @Nullable Integer getThreadPrio(KernelAnalysisModule module, Integer threadId, long ts) {
        Integer execPrio = null;
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return execPrio;
        }
        try {
            int execPrioQuark = ss.getQuarkAbsolute(Attributes.THREADS, threadId.toString(), Attributes.PRIO);
            ITmfStateInterval interval = ss.querySingleState(ts, execPrioQuark);
            ITmfStateValue prioValue = interval.getStateValue();
            /* We know the prio must be an Integer */
            execPrio = prioValue.unboxInt();
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
        }
        return execPrio;
    }

    /**
     * Get the status intervals for a given thread with a resolution
     *
     * @param module
     *            The lttng kernel analysis instance to run this method on
     * @param threadId
     *            The ID of the thread to get the intervals for
     * @param start
     *            The start time of the requested range
     * @param end
     *            The end time of the requested range
     * @param resolution
     *            The resolution or the minimal time between the requested
     *            intervals. If interval times are smaller than resolution, only
     *            the first interval is returned, the others are ignored.
     * @param monitor
     *            A progress monitor for this task
     * @return The list of status intervals for this thread, an empty list is
     *         returned if either the state system is {@code null} or the quark
     *         is not found
     * @since 1.0
     */
    public static List<ITmfStateInterval> getStatusIntervalsForThread(KernelAnalysisModule module, Integer threadId, long start, long end, long resolution, IProgressMonitor monitor) {
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            return Collections.EMPTY_LIST;
        }

        try {
            int threadQuark = ss.getQuarkAbsolute(Attributes.THREADS, threadId.toString());
            int statusQuark = ss.getQuarkRelative(threadQuark, Attributes.STATUS);
            List<ITmfStateInterval> statusIntervals = StateSystemUtils.queryHistoryRange(ss, statusQuark, Math.max(start, ss.getStartTime()), Math.min(end - 1, ss.getCurrentEndTime()), resolution, monitor);
            return statusIntervals;
        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
        }
        return Collections.EMPTY_LIST;
    }

}
