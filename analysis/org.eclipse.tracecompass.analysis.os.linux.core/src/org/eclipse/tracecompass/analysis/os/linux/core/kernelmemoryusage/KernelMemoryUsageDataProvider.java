/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.memory.MemoryUsageTreeModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelmemoryusage.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.FilterTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.AbstractTreeXyDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * This data provider will return a XY model based on a query filter. The model
 * is used afterwards by any viewer to draw charts. Model returned is for Kernel
 * Memory Usage views
 *
 * @author Yonni Chen
 * @since 2.3
 */
@NonNullByDefault
@SuppressWarnings("restriction")
public class KernelMemoryUsageDataProvider extends AbstractTreeXyDataProvider<KernelMemoryAnalysisModule, MemoryUsageTreeModel> {

    /**
     * This data provider's extension point ID
     * @since 2.4
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage"; //$NON-NLS-1$
    /**
     * Fake Tid used for the total entry
     * @since 2.4
     */
    private static final int TOTAL_TID = -2;

    private @Nullable KernelAnalysisModule fKernelModule;

    /* A map that saves the mapping of a thread ID to its executable name */
    private final Map<String, String> fProcessNameMap = new HashMap<>();

    /**
     * Create an instance of {@link KernelMemoryUsageDataProvider}. Returns a null
     * instance if the analysis module is not found.
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @return A KernelMemoryUsageDataProvider. If analysis module is not found, it
     *         returns null
     */
    public static @Nullable KernelMemoryUsageDataProvider create(ITmfTrace trace) {
        KernelMemoryAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelMemoryAnalysisModule.class, KernelMemoryAnalysisModule.ID);
        if (module != null) {
            module.schedule();
            return new KernelMemoryUsageDataProvider(trace, module);
        }
        return null;
    }

    /**
     * Constructor
     */
    private KernelMemoryUsageDataProvider(ITmfTrace trace, KernelMemoryAnalysisModule module) {
        super(trace, module);
    }

    /**
     * @since 2.5
     */
    @Override
    protected @Nullable Map<String, IYModel> getYModels(ITmfStateSystem ss, SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        long[] xValues = filter.getTimesRequested();

        long currentEnd = ss.getCurrentEndTime();

        /**
         * For a given time range, we plot lines representing the memory allocation for
         * the total and the selected entries.
         */
        double[] totalKernelMemoryValues = new double[xValues.length];
        Map<Integer, double[]> selectedSeries = initSeries(filter);

        for (int i = 0; i < xValues.length; i++) {
            if (monitor != null && monitor.isCanceled()) {
                return null;
            }

            long time = xValues[i];
            if (time >= ss.getStartTime() && time <= currentEnd) {
                /* The subattributes of the root are the different threads */
                List<Integer> threadQuarkList = ss.getSubAttributes(-1, false);
                List<ITmfStateInterval> kernelState = ss.queryFullState(time);

                for (Integer threadQuark : threadQuarkList) {
                    long value = extractValue(kernelState.get(threadQuark).getValue());

                    /* We add the value of each thread to the total quantity */
                    totalKernelMemoryValues[i] += value;

                    double[] selectedThreadValues = selectedSeries.get(threadQuark);
                    if (selectedThreadValues != null) {
                        selectedThreadValues[i] = value;
                    }
                }
            }
        }

        /**
         * We shift the series up.
         */
        List<ITmfStateInterval> endState = ss.queryFullState(Long.min(filter.getEnd(), currentEnd));

        double d = extractTotalValueShift(ss, endState);
        Arrays.setAll(totalKernelMemoryValues, i -> totalKernelMemoryValues[i] + d);

        for (Entry<Integer, double[]> entry : selectedSeries.entrySet()) {
            int lowestMemoryQuark = ss.optQuarkRelative(entry.getKey(), KernelMemoryAnalysisModule.THREAD_LOWEST_MEMORY_VALUE);
            if (lowestMemoryQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                double[] threadValues = entry.getValue();
                long shift = extractValue(endState.get(lowestMemoryQuark).getValue());
                Arrays.setAll(threadValues, i -> threadValues[i] - shift);
            }
        }

        ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();

        String total = getTrace().getName() + MemoryUsageTreeModel.TOTAL_SUFFIX;
        ySeries.put(total, new YModel(total, totalKernelMemoryValues));

        for (Entry<Integer, double[]> entry : selectedSeries.entrySet()) {
            String selectedThreadName = getTrace().getName() + ':' + ss.getAttributeName(entry.getKey());
            ySeries.put(selectedThreadName, new YModel(selectedThreadName, entry.getValue()));
        }

        return ySeries.build();
    }

    /**
     * Initialize a map of quark to primitive double array
     *
     * @param filter
     *            the query object
     * @return a Map of quarks for the entries which exist for this provider to
     *         newly initialized primitive double arrays of the same length as the
     *         number of requested timestamps.
     */
    private Map<Integer, double[]> initSeries(SelectionTimeQueryFilter filter) {
        int length = filter.getTimesRequested().length;
        return Maps.toMap(getSelectedQuarks(filter), k -> new double[length]);
    }

    /**
     * For each thread, we look for its lowest value since the beginning of the
     * trace. This way, we can avoid negative values in the plot.
     *
     * @param ss
     *            the queried state system
     * @param endState
     *            the fullstate at the query end time.
     * @return the sum of the lowest memory values for all threads.
     */
    private static double extractTotalValueShift(ITmfStateSystem ss, List<ITmfStateInterval> endState) {
        double totalKernelMemoryValuesShift = 0;

        /* We add the lowest value of each thread */
        List<Integer> threadQuarkList = ss.getQuarks("*", KernelMemoryAnalysisModule.THREAD_LOWEST_MEMORY_VALUE); //$NON-NLS-1$
        for (Integer threadQuark : threadQuarkList) {
            ITmfStateInterval lowestMemoryInterval = endState.get(threadQuark);
            Object val = lowestMemoryInterval.getValue();
            // We want to add up a positive quantity.
            totalKernelMemoryValuesShift -= extractValue(val);
        }
        return totalKernelMemoryValuesShift;
    }

    private static long extractValue(@Nullable Object val) {
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return 0;
    }

    /**
     * @since 2.5
     */
    @Override
    protected List<MemoryUsageTreeModel> getTree(ITmfStateSystem ss, TimeQueryFilter filter, @Nullable IProgressMonitor monitor)
            throws StateSystemDisposedException {

        long start = filter.getStart();
        long end = filter.getEnd();

        // Let the list of active states be null if we aren't filtering
        List<ITmfStateInterval> active = null;
        if (filter instanceof FilterTimeQueryFilter && ((FilterTimeQueryFilter) filter).isFiltered()) {
            if (start == end || start > ss.getCurrentEndTime() || end < ss.getStartTime()) {
                /*
                 * return an empty list if the filter is empty or does not intersect the state
                 * system
                 */
                return Collections.emptyList();
            }
            active = ss.queryFullState(Long.max(start, ss.getStartTime()));
        }

        List<MemoryUsageTreeModel> nodes = new ArrayList<>();
        List<Integer> threadQuarkList = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false);

        long totalId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        nodes.add(new MemoryUsageTreeModel(totalId, -1, TOTAL_TID, getTrace().getName()));
        for (Integer threadQuark : threadQuarkList) {
            if (active == null || active.get(threadQuark).getEndTime() < end) {
                String tidString = ss.getAttributeName(threadQuark);
                String procname = getProcessName(tidString);

                // Ensure that we reuse the same id for a given quark.
                long id = getId(threadQuark);
                nodes.add(new MemoryUsageTreeModel(id, totalId, parseTid(tidString), procname));
            }
        }
        return nodes;
    }

    private static int parseTid(String tidString) {
        try {
            return Integer.parseInt(tidString);
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    /*
     * Get the process name from its TID by using the LTTng kernel analysis module
     */
    private String getProcessName(String tid) {
        String execName = fProcessNameMap.get(tid);
        if (execName != null) {
            return execName;
        }
        if (tid.equals(KernelMemoryStateProvider.OTHER_TID)) {
            fProcessNameMap.put(tid, tid);
            return tid;
        }
        KernelAnalysisModule kernelModule = fKernelModule;
        if (kernelModule == null) {
            kernelModule = TmfTraceUtils.getAnalysisModuleOfClass(getTrace(), KernelAnalysisModule.class, KernelAnalysisModule.ID);
        }
        if (kernelModule == null) {
            return tid;
        }
        fKernelModule = kernelModule;
        execName = KernelThreadInformationProvider.getExecutableName(kernelModule, Integer.parseInt(tid));
        if (execName == null) {
            return tid;
        }
        fProcessNameMap.put(tid, execName);
        return execName;
    }

    /**
     * @since 2.4
     */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * @since 2.5
     */
    @Override
    protected boolean isCacheable() {
        return false;
    }

    /**
     * @since 2.5
     */
    @Override
    protected String getTitle() {
        return Objects.requireNonNull(Messages.KernelMemoryUsageDataProvider_title);
    }
}
