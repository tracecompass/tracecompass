/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.cpuusage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.AbstractTreeCommonXDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

/**
 * This data provider will return a XY model based on a query filter. The model
 * is used afterwards by any viewer to draw charts. Model returned is for CPU
 * Usage views
 *
 * @author Yonni Chen
 * @since 2.3
 */
@SuppressWarnings("restriction")
public class CpuUsageDataProvider extends AbstractTreeCommonXDataProvider<KernelCpuUsageAnalysis, CpuUsageEntryModel> {

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

    /**
     * @since 2.5
     */
    @Override
    protected @Nullable Map<String, IYModel> getYModels(ITmfStateSystem ss, SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Set<Integer> cpus = Collections.emptySet();

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

        ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
        String key = TOTAL + getTrace().getName();
        ySeries.put(key, new YModel(getId(ITmfStateSystem.ROOT_ATTRIBUTE), key, totalValues));
        for (IYModel entry : selectedThreadValues.values()) {
            ySeries.put(entry.getName(), entry);
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
    protected List<CpuUsageEntryModel> getTree(ITmfStateSystem ss, TimeQueryFilter filter, @Nullable IProgressMonitor monitor)
            throws StateSystemDisposedException {
        if (!(filter instanceof SelectedCpuQueryFilter)) {
            return Collections.emptyList();
        }

        SelectedCpuQueryFilter cpuQueryFilter = (SelectedCpuQueryFilter) filter;
        long end = filter.getEnd();

        List<CpuUsageEntryModel> entryList = new ArrayList<>();
        Map<String, Long> cpuUsageMap = getAnalysisModule().getCpuUsageInRange(cpuQueryFilter.getSelectedCpus(), filter.getStart(), end);

        long totalTime = cpuUsageMap.getOrDefault(KernelCpuUsageAnalysis.TOTAL, 0l);
        long totalId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        entryList.add(new CpuUsageEntryModel(totalId, -1, getTrace().getName(), TOTAL_SERIES_TID, totalTime));

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
                if (tid != 0) {
                    entryList.add(new CpuUsageEntryModel(getId(tid), totalId, getProcessName(tid, strings[1]), tid, entry.getValue()));
                }
            }
        }
        return entryList;
    }

    /*
     * Get the process name from its TID by using the LTTng kernel analysis
     * module
     */
    private String getProcessName(int tid, String defaultTidName) {
        // try and get from cache
        String execName = fProcessNameMap.get(tid);
        if (execName != null) {
            return execName;
        }

        execName = KernelThreadInformationProvider.getExecutableName(fKernelAnalysisModule, tid);
        ITmfStateSystem ss = fKernelAnalysisModule.getStateSystem();
        if (ss != null && ss.waitUntilBuilt(0) && execName != null) {
            // cache only if non null and state system analysis completed
            fProcessNameMap.put(tid, execName);
            return execName;
        }
        return defaultTidName;
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