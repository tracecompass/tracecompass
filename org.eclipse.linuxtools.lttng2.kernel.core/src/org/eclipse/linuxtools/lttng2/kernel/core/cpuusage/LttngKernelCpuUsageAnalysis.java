/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.cpuusage;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Activator;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.lttng2.kernel.core.analysis.LttngKernelAnalysisModule;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * This analysis module computes the CPU usage of a system from a kernel trace.
 * It requires the LTTng Kernel analysis module to have accurate CPU usage data.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class LttngKernelCpuUsageAnalysis extends TmfStateSystemAnalysisModule {

    /** The ID of this analysis */
    public static final String ID = "org.eclipse.linuxtools.lttng2.kernel.core.cpuusage"; //$NON-NLS-1$

    /** Text used to identify 'total' entries in the returned maps */
    public static final String TOTAL = "total"; //$NON-NLS-1$
    /** String used to separate elements in the returned maps */
    public static final String SPLIT_STRING = "/"; //$NON-NLS-1$
    /** Idle process thread ID */
    public static final String TID_ZERO = "0"; //$NON-NLS-1$

    @Override
    protected ITmfStateProvider createStateProvider() {
        return new LttngKernelCpuStateProvider(getTrace());
    }

    @Override
    protected StateSystemBackendType getBackendType() {
        return StateSystemBackendType.FULL;
    }

    @Override
    protected boolean executeAnalysis(IProgressMonitor monitor) {
        /*
         * This analysis depends on the LTTng kernel analysis, so we'll start
         * that build at the same time
         */
        LttngKernelAnalysisModule module = getTrace().getAnalysisModuleOfClass(LttngKernelAnalysisModule.class, LttngKernelAnalysisModule.ID);
        if (module != null) {
            module.schedule();
        }
        return super.executeAnalysis(monitor);
    }

    /**
     * Get a map of time spent on CPU by various threads during a time range.
     *
     * @param start
     *            Start time of requested range
     * @param end
     *            End time of requested range
     * @return A map of TID -> time spent on CPU in the [start, end] interval
     */
    public Map<String, Long> getCpuUsageInRange(long start, long end) {
        Map<String, Long> map = new HashMap<>();
        Map<String, Long> totalMap = new HashMap<>();

        ITmfStateSystem cpuSs = getStateSystem();
        if (cpuSs == null) {
            return map;
        }
        TmfStateSystemAnalysisModule module = getTrace().getAnalysisModuleOfClass(TmfStateSystemAnalysisModule.class, LttngKernelAnalysisModule.ID);
        if (module == null) {
            return map;
        }
        module.schedule();
        module.waitForInitialization();
        ITmfStateSystem kernelSs = module.getStateSystem();
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
                tidsPerCpu.put(cpuNode, cpuSs.getSubAttributes(cpuNode, false));
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
