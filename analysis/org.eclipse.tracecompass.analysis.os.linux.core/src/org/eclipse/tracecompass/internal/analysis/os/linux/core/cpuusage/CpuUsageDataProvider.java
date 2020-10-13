/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage;

import java.text.Format;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageEntryModel;
import org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.KernelCpuUsageAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.model.OsStrings;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.AbstractTreeCommonXDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

/**
 * This data provider will return a XY model based on a query filter. The model
 * is used afterwards by any viewer to draw charts. Model returned is for CPU
 * Usage views
 *
 * @author Yonni Chen
 * @since 2.3
 */
public class CpuUsageDataProvider extends AbstractTreeCommonXDataProvider<KernelCpuUsageAnalysis, CpuUsageEntryModel> {

    private static final Format TIME_FORMATTER = SubSecondTimeWithUnitFormat.getInstance();
    /**
     * Prefix for the total series.
     * @since 2.4
     */
    public static final String TOTAL = "total:"; //$NON-NLS-1$

    /**
     * This provider's extension point ID.
     * @since 2.4
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.core.cpuusage.CpuUsageDataProvider"; //$NON-NLS-1$

    /**
     * Parameter key to extract cpus from the parameters map
     */
    public static final String REQUESTED_CPUS_KEY = "requested_cpus"; //$NON-NLS-1$

    /**
     * The Fake Tid to identify the total entry.
     * @since 2.4
     */
    public static final int TOTAL_SERIES_TID = -2;

    /* A map that caches the mapping of a thread ID to its executable name */
    private final Map<Integer, String> fProcessNameMap = new HashMap<>();

    /**
     * {@link KernelAnalysisModule} used to retrieve Process names from their TIDs.
     */
    private final KernelAnalysisModule fKernelAnalysisModule;

    /**
     * Create an instance of {@link CpuUsageDataProvider}. Returns a null instance
     * if the analysis module is not found.
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @return A CpuUsageDataProvider instance. If analysis module is not found, it
     *         returns null
     */
    public static @Nullable CpuUsageDataProvider create(ITmfTrace trace) {
        KernelCpuUsageAnalysis module = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelCpuUsageAnalysis.class, KernelCpuUsageAnalysis.ID);
        KernelAnalysisModule kernelAnalysisModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);
        if (module != null && kernelAnalysisModule != null) {
            module.schedule();
            kernelAnalysisModule.schedule();
            return new CpuUsageDataProvider(trace, module, kernelAnalysisModule);
        }
        return null;
    }

    /**
     * Constructor
     */
    private CpuUsageDataProvider(ITmfTrace trace, KernelCpuUsageAnalysis module, KernelAnalysisModule kernelAnalysisModule) {
        super(trace, module);
        fKernelAnalysisModule = kernelAnalysisModule;
    }

    @Deprecated
    @Override
    protected @Nullable Map<String, IYModel> getYModels(ITmfStateSystem ss, Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        return Maps.uniqueIndex(getYSeriesModels(ss, fetchParameters, monitor), IYModel::getName);
    }

    /**
     * @since 2.5
     */
    @Override
    protected @Nullable Collection<IYModel> getYSeriesModels(ITmfStateSystem ss, Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        Set<Integer> cpus = Collections.emptySet();

        SelectionTimeQueryFilter filter = createCpuQuery(fetchParameters);
        if (filter == null) {
            filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
            if (filter == null) {
                return null;
            }
        }

        if (filter instanceof SelectedCpuQueryFilter) {
            cpus = ((SelectedCpuQueryFilter) filter).getSelectedCpus();
        }

        long[] xValues = filter.getTimesRequested();

        /* CPU usage values for total and selected thread */
        double[] totalValues = new double[xValues.length];
        Map<String, IYModel> selectedThreadValues = new HashMap<>();
        for (Entry<Long, Integer> entry : getSelectedEntries(filter).entrySet()) {
            String name = Integer.toString(entry.getValue());
            selectedThreadValues.put(name, new YModel(entry.getKey(), getTrace().getName() + ':' + name, new double[xValues.length]));
        }

        long prevTime = Math.max(filter.getStart(), ss.getStartTime());
        long currentEnd = ss.getCurrentEndTime();

        for (int i = 1; i < xValues.length; i++) {
            long time = xValues[i];
            if (time >= ss.getStartTime() && time <= currentEnd && prevTime < time) {
                Map<String, Long> cpuUsageMap = Maps.filterKeys(getAnalysisModule().getCpuUsageInRange(cpus, prevTime, time),
                    key -> key.startsWith(KernelCpuUsageAnalysis.TOTAL)
                );

                /*
                 * Calculate the sum of all total entries, and add a data point to the selected
                 * one
                 */
                long totalCpu = 0;
                for (Entry<String, Long> entry : cpuUsageMap.entrySet()) {
                    String threadName = extractThreadName(entry.getKey());
                    if (threadName != null) {
                        long cpuTime = entry.getValue();
                        totalCpu += cpuTime;
                        IYModel values = selectedThreadValues.get(threadName);
                        if (values != null) {
                            values.getData()[i] = normalize(prevTime, time, cpuTime);
                        }
                    }
                }
                totalValues[i] = normalize(prevTime, time, totalCpu);
                prevTime = time;
            }
            if (monitor != null && monitor.isCanceled()) {
                return null;
            }
        }

        ImmutableList.Builder<IYModel> ySeries = ImmutableList.builder();
        String key = TOTAL + getTrace().getName();
        ySeries.add(new YModel(getId(ITmfStateSystem.ROOT_ATTRIBUTE), key, totalValues));
        for (IYModel entry : selectedThreadValues.values()) {
            ySeries.add(entry);
        }

        return ySeries.build();
    }

    private static double normalize(long prevTime, long time, long value) {
        return (double) value / (time - prevTime) * 100;
    }

    private static @Nullable String extractThreadName(String key) {
        String[] strings = key.split(KernelCpuUsageAnalysis.SPLIT_STRING, 2);
        if ((strings.length > 1) && !(strings[1].equals(KernelCpuUsageAnalysis.TID_ZERO))) {
            return strings[1];
        }
        return null;
    }

    /**
     * @since 2.5
     */
    @Override
    protected TmfTreeModel<CpuUsageEntryModel> getTree(ITmfStateSystem ss, Map<String, Object> parameters, @Nullable IProgressMonitor monitor)
            throws StateSystemDisposedException {
        TimeQueryFilter filter = FetchParametersUtils.createTimeQuery(parameters);
        if (filter == null) {
            return new TmfTreeModel<>(Collections.emptyList(), Collections.emptyList());
        }

        long end = filter.getEnd();

        List<CpuUsageEntryModel> entryList = new ArrayList<>();
        Set<Integer> cpus = extractCpuSet(parameters);
        Map<String, Long> cpuUsageMap = getAnalysisModule().getCpuUsageInRange(cpus, filter.getStart(), end);
        double timeRange = end - filter.getStart();

        long totalTime = cpuUsageMap.getOrDefault(KernelCpuUsageAnalysis.TOTAL, 0l);
        long totalId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        entryList.add(new CpuUsageEntryModel(totalId, -1,ImmutableList.of(getTrace().getName(), String.valueOf(Messages.CpuUsageDataProvider_Total), String.format(Messages.CpuUsageDataProvider_TextPercent, timeRange > 0 ? 100 * totalTime / timeRange : (float) 0), TIME_FORMATTER.format(totalTime)), TOTAL_SERIES_TID, totalTime));

        for (Entry<String, Long> entry : cpuUsageMap.entrySet()) {
            /*
             * Process only entries representing the total of all CPUs and that
             * have time on CPU
             */
            String key = entry.getKey();
            if (entry.getValue() == 0 || !key.startsWith(KernelCpuUsageAnalysis.TOTAL)) {
                continue;
            }
            String[] strings = key.split(KernelCpuUsageAnalysis.SPLIT_STRING, 2);

            if (strings.length > 1) {
                int tid = Integer.parseInt(strings[1]);
                Long time = entry.getValue();
                if (tid != 0) {
                    entryList.add(new CpuUsageEntryModel(getId(tid), totalId, ImmutableList.of(getProcessName(tid, strings[1], end), String.valueOf(tid), String.format(Messages.CpuUsageDataProvider_TextPercent, timeRange > 0 ? 100 * time / timeRange : (float) 0), TIME_FORMATTER.format(time)), tid, time));
                }
            }
        }
        return new TmfTreeModel<>(ImmutableList.of(String.valueOf(Messages.CpuUsageDataProvider_ColumnProcess), OsStrings.tid(), String.valueOf(Messages.CpuUsageDataProvider_ColumnPercent), String.valueOf(Messages.CpuUsageDataProvider_ColumnTime)), entryList);
    }

    /*
     * Get the process name from its TID by using the LTTng kernel analysis
     * module
     */
    private String getProcessName(int tid, String defaultTidName, long endTime) {
        // try and get from cache
        String execName = fProcessNameMap.get(tid);
        if (execName != null) {
            return execName;
        }

        execName = KernelThreadInformationProvider.getExecutableName(fKernelAnalysisModule, tid, endTime);
        ITmfStateSystem ss = fKernelAnalysisModule.getStateSystem();
        if (ss != null && ss.waitUntilBuilt(0) && execName != null) {
            // cache only if non null and state system analysis completed
            fProcessNameMap.put(tid, execName);
            return execName;
        }
        return defaultTidName;
    }

    private static @Nullable SelectedCpuQueryFilter createCpuQuery(Map<String, Object> parameters) {
        List<Long> timeRequested = DataProviderParameterUtils.extractTimeRequested(parameters);
        List<Long> selectedItems = DataProviderParameterUtils.extractSelectedItems(parameters);
        Set<Integer> cpus = extractCpuSet(parameters);

        if (timeRequested == null || selectedItems == null) {
            return null;
        }

        return new SelectedCpuQueryFilter(timeRequested, selectedItems, cpus);
    }

    private static Set<Integer> extractCpuSet(Map<String, Object> parameters) {
        Object cpus = parameters.get(REQUESTED_CPUS_KEY);
        if (cpus instanceof Collection<?>) {
            return ((Collection<?>) cpus).stream().filter(cpu -> cpu instanceof Integer)
                    .map(cpu -> (Integer) cpu)
                    .collect(Collectors.toSet());
        }
        return Collections.emptySet();
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
        return Objects.requireNonNull(Messages.CpuUsageDataProvider_title);
    }
}