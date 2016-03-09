/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.cpuusage;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.Attributes;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * This analysis module computes the CPU usage of a system from a kernel trace.
 * It requires the LTTng Kernel analysis module to have accurate CPU usage data.
 *
 * @author Geneviève Bastien
 */
public class KernelCpuUsageAnalysis extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.cpuusage"; //$NON-NLS-1$

    /** Text used to identify 'total' entries in the returned maps */
    public static final String TOTAL = "total"; //$NON-NLS-1$
    /** String used to separate elements in the returned maps */
    public static final String SPLIT_STRING = "/"; //$NON-NLS-1$
    /** Idle process thread ID */
    public static final String TID_ZERO = "0"; //$NON-NLS-1$

    @Override
    protected ITmfStateProvider createStateProvider() {
        ITmfTrace trace = checkNotNull(getTrace());
        IKernelAnalysisEventLayout layout;

        if (trace instanceof IKernelTrace) {
            layout = ((IKernelTrace) trace).getKernelEventLayout();
        } else {
            /* Fall-back to the base LttngEventLayout */
            layout = IKernelAnalysisEventLayout.DEFAULT_LAYOUT;
        }

        return new KernelCpuUsageStateProvider(trace, layout);
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        Set<IAnalysisModule> modules = new HashSet<>();

        ITmfTrace trace = getTrace();
        if (trace == null) {
            throw new IllegalStateException();
        }
        /*
         * This analysis depends on the LTTng kernel analysis, so it's added to
         * dependent modules.
         */
        Iterable<KernelAnalysisModule> kernelModules = TmfTraceUtils.getAnalysisModulesOfClass(trace, KernelAnalysisModule.class);
        for (KernelAnalysisModule kernelModule : kernelModules) {
            /* Only add the first one we find, if there is one */
            modules.add(kernelModule);
            break;
        }
        return modules;
    }

    /**
     * Gets the maximum number of cores detected
     *
     * @return the number of cores
     * @since 2.0
     */
    public int getNumberOfCores() {

        ITmfStateSystem cpuSs = getStateSystem();
        if (cpuSs != null) {
            try {
                int cpusNode = cpuSs.getQuarkAbsolute(Attributes.CPUS);
                final @NonNull List<@NonNull Integer> subAttributes = cpuSs.getSubAttributes(cpusNode, false);
                int cpus = Integer.MIN_VALUE;
                for (Integer quark : subAttributes) {
                    cpus = Math.max(Integer.parseInt(cpuSs.getAttributeName(quark)), cpus);
                }
                return Math.max(subAttributes.size(), cpus);
            } catch (AttributeNotFoundException e) {
                Activator.getDefault().logError(e.getMessage(), e);
            }
        }
        return -1;

    }

    /**
     * Get a map of time spent on CPU by various threads during a time range.
     *
     * @param cpus
     *            A set of the desired CPUs to get. An empty set gets all the
     *            cores
     * @param start
     *            Start time of requested range
     * @param end
     *            End time of requested range
     * @return A map of TID -> time spent on CPU in the [start, end] interval
     * @since 2.0
     */
    public Map<String, Long> getCpuUsageInRange(Set<@NonNull Integer> cpus, long start, long end) {
        Map<String, Long> map = new HashMap<>();
        Map<String, Long> totalMap = new HashMap<>();

        ITmfTrace trace = getTrace();
        ITmfStateSystem cpuSs = getStateSystem();
        if (trace == null || cpuSs == null) {
            return map;
        }
        ITmfStateSystem kernelSs = TmfStateSystemAnalysisModule.getStateSystem(trace, KernelAnalysisModule.ID);
        if (kernelSs == null) {
            return map;
        }

        /*
         * Make sure the start/end times are within the state history, so we
         * don't get TimeRange exceptions.
         */
        long startTime = Math.max(start, cpuSs.getStartTime());
        startTime = Math.max(startTime, kernelSs.getStartTime());
        long endTime = Math.min(end, cpuSs.getCurrentEndTime());
        endTime = Math.min(endTime, kernelSs.getCurrentEndTime());
        long totalTime = 0;
        if (endTime < startTime) {
            return map;
        }

        try {
            /* Get the list of quarks for each CPU and CPU's TIDs */
            int cpusNode = cpuSs.getQuarkAbsolute(Attributes.CPUS);
            Map<Integer, List<Integer>> tidsPerCpu = new HashMap<>();
            for (int cpuNode : cpuSs.getSubAttributes(cpusNode, false)) {
                final @NonNull List<@NonNull Integer> cpuSubAttributes = cpuSs.getSubAttributes(cpuNode, false);
                if (cpus.isEmpty() || cpus.contains(Integer.parseInt(cpuSs.getAttributeName(cpuNode)))) {
                    tidsPerCpu.put(cpuNode, cpuSubAttributes);
                }
            }

            /* Query full states at start and end times */
            List<ITmfStateInterval> kernelEndState = kernelSs.queryFullState(endTime);
            List<ITmfStateInterval> endState = cpuSs.queryFullState(endTime);
            List<ITmfStateInterval> kernelStartState = kernelSs.queryFullState(startTime);
            List<ITmfStateInterval> startState = cpuSs.queryFullState(startTime);

            long countAtStart, countAtEnd;

            for (Entry<Integer, List<Integer>> entry : tidsPerCpu.entrySet()) {
                int cpuNode = entry.getKey();
                List<Integer> tidNodes = entry.getValue();

                String curCpuName = cpuSs.getAttributeName(cpuNode);
                long cpuTotal = 0;

                /* Get the quark of the thread running on this CPU */
                int currentThreadQuark = kernelSs.getQuarkAbsolute(Attributes.CPUS, curCpuName, Attributes.CURRENT_THREAD);
                /* Get the currently running thread on this CPU */
                int startThread = kernelStartState.get(currentThreadQuark).getStateValue().unboxInt();
                int endThread = kernelEndState.get(currentThreadQuark).getStateValue().unboxInt();

                for (int tidNode : tidNodes) {
                    String curTidName = cpuSs.getAttributeName(tidNode);
                    int tid = Integer.parseInt(curTidName);

                    countAtEnd = endState.get(tidNode).getStateValue().unboxLong();
                    countAtStart = startState.get(tidNode).getStateValue().unboxLong();
                    if (countAtStart == -1) {
                        countAtStart = 0;
                    }
                    if (countAtEnd == -1) {
                        countAtEnd = 0;
                    }

                    /*
                     * Interpolate start and end time of threads running at
                     * those times
                     */
                    if (tid == startThread || startThread == -1) {
                        long runningTime = kernelStartState.get(currentThreadQuark).getEndTime() - kernelStartState.get(currentThreadQuark).getStartTime();
                        long runningEnd = kernelStartState.get(currentThreadQuark).getEndTime();

                        countAtStart = interpolateCount(countAtStart, startTime, runningEnd, runningTime);
                    }
                    if (tid == endThread) {
                        long runningTime = kernelEndState.get(currentThreadQuark).getEndTime() - kernelEndState.get(currentThreadQuark).getStartTime();
                        long runningEnd = kernelEndState.get(currentThreadQuark).getEndTime();

                        countAtEnd = interpolateCount(countAtEnd, endTime, runningEnd, runningTime);
                    }
                    /*
                     * If startThread is -1, we made the hypothesis that the
                     * process running at start was the current one. If the
                     * count is negative, we were wrong in this hypothesis. Also
                     * if the time at end is 0, it either means the process
                     * hasn't been on the CPU or that we still don't know who is
                     * running. In both cases, that invalidates the hypothesis.
                     */
                    if ((startThread == -1) && ((countAtEnd - countAtStart < 0) || (countAtEnd == 0))) {
                        countAtStart = 0;
                    }

                    long currentCount = countAtEnd - countAtStart;
                    if (currentCount < 0) {
                        Activator.getDefault().logWarning(String.format("Negative count: start %d, end %d", countAtStart, countAtEnd)); //$NON-NLS-1$
                        currentCount = 0;
                    } else if (currentCount > endTime - startTime) {
                        Activator.getDefault().logWarning(String.format("CPU Usage: Spent more time on CPU than allowed: %s spent %d when max should be %d", curTidName, currentCount, endTime - startTime)); //$NON-NLS-1$
                        currentCount = 0;
                    }
                    cpuTotal += currentCount;
                    map.put(curCpuName + SPLIT_STRING + curTidName, currentCount);
                    addToMap(totalMap, curTidName, currentCount);
                    totalTime += (currentCount);
                }
                map.put(curCpuName, cpuTotal);
            }

            /* Add the totals to the map */
            for (Entry<String, Long> entry : totalMap.entrySet()) {
                map.put(TOTAL + SPLIT_STRING + entry.getKey(), entry.getValue());
            }
            map.put(TOTAL, totalTime);

        } catch (TimeRangeException | AttributeNotFoundException e) {
            /*
             * Assume there is no events or the attribute does not exist yet,
             * nothing will be put in the map.
             */
        } catch (StateValueTypeException | StateSystemDisposedException e) {
            /*
             * These other exception types would show a logic problem, so they
             * should not happen.
             */
            Activator.getDefault().logError("Error getting CPU usage in a time range", e); //$NON-NLS-1$
        }

        return map;
    }

    private static long interpolateCount(long count, long ts, long runningEnd, long runningTime) {
        long newCount = count;

        /* sanity check */
        if (runningTime > 0) {

            long runningStart = runningEnd - runningTime;

            if (ts < runningStart) {
                /*
                 * This interval was not started, this can happen if the current
                 * running thread is unknown and we execute this method. It just
                 * means that this process was not the one running
                 */
                return newCount;
            }
            newCount += (ts - runningStart);
        }
        return newCount;
    }

    /*
     * Add the value to the previous value in the map. If the key was not set,
     * assume 0
     */
    private static void addToMap(Map<String, Long> map, String key, Long value) {
        Long addTo = map.get(key);
        if (addTo == null) {
            map.put(key, value);
        } else {
            map.put(key, addTo + value);
        }
    }

}
