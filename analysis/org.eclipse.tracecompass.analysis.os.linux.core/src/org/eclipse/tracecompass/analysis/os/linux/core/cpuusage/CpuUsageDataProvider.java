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
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.Messages;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractStateSystemAnalysisDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
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
public class CpuUsageDataProvider extends AbstractStateSystemAnalysisDataProvider
    implements ITmfTreeXYDataProvider<CpuUsageEntryModel> {

    /**
     * Prefix for the total series.
     * @since 2.4
     */
    public static final String TOTAL = "total:"; //$NON-NLS-1$

    private static final TmfModelResponse<List<CpuUsageEntryModel>> FAILED_TREE_RESPONSE = new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);

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
    private static final AtomicLong CPU_USAGE_ID = new AtomicLong();

    /* A map that caches the mapping of a thread ID to its executable name */
    private final Map<String, String> fProcessNameMap = new HashMap<>();
    private final BiMap<Long, String> fTidToName = HashBiMap.create();
    private final long fTotalId = CPU_USAGE_ID.getAndIncrement();

    private final KernelCpuUsageAnalysis fModule;

    /**
     * {@link KernelAnalysisModule}'s {@link ITmfStateSystem} used to retrieve
     * Process names from their PIDs.
     */
    private @Nullable ITmfStateSystem fKernelSs;

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
        if (module != null) {
            module.schedule();
            return new CpuUsageDataProvider(trace, module);
        }
        return null;
    }

    /**
     * Constructor
     */
    private CpuUsageDataProvider(ITmfTrace trace, KernelCpuUsageAnalysis module) {
        super(trace);
        fModule = module;
    }

    @Override
    public TmfModelResponse<ITmfCommonXAxisModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        TmfModelResponse<ITmfCommonXAxisModel> res = verifyParameters(fModule, filter, monitor);
        if (res != null) {
            return res;
        }

        ITmfStateSystem ss = Objects.requireNonNull(fModule.getStateSystem(), "Statesystem should have been verified by verifyParameters"); //$NON-NLS-1$

        Collection<Long> selectedThreads = Collections.emptySet();
        Set<Integer> cpus = Collections.emptySet();

        if (filter instanceof SelectedCpuQueryFilter) {
            selectedThreads = ((SelectionTimeQueryFilter) filter).getSelectedItems();
            cpus = ((SelectedCpuQueryFilter) filter).getSelectedCpus();
        }

        long[] xValues = filter.getTimesRequested();

        /* CPU usage values for total and selected thread */
        double[] totalValues = new double[xValues.length];
        Map<String, double[]> selectedThreadValues = new HashMap<>();
        for (Long selectThread : selectedThreads) {
            String tid = fTidToName.get(selectThread);
            if (tid != null) {
                selectedThreadValues.put(tid, new double[xValues.length]);
            }
        }

        long prevTime = Math.max(filter.getStart(), ss.getStartTime());
        long currentEnd = ss.getCurrentEndTime();

        for (int i = 1; i < xValues.length; i++) {
            long time = xValues[i];
            if (time >= ss.getStartTime() && time <= currentEnd && prevTime < time) {
                Map<String, Long> cpuUsageMap = Maps.filterKeys(fModule.getCpuUsageInRange(cpus, prevTime, time),
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
                        double[] values = selectedThreadValues.get(entry.getKey());
                        if (values != null) {
                            values[i] = normalize(prevTime, time, cpuTime);
                        }
                    }
                }
                totalValues[i] = normalize(prevTime, time, totalCpu);
                prevTime = time;
            }
            if (monitor != null && monitor.isCanceled()) {
                return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
            }
        }

        ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
        String key = TOTAL + getTrace().getName();
        ySeries.put(key, new YModel(key, totalValues));
        for (Entry<String, double[]> entry : selectedThreadValues.entrySet()) {
            String selectedThread = getTrace().getName() + ':' + extractThreadName(entry.getKey());
            ySeries.put(selectedThread, new YModel(selectedThread, entry.getValue()));
        }

        boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= currentEnd;
        return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.CpuUsageDataProvider_title), xValues, ySeries.build(), complete);
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
     * @since 2.4
     */
    @Override
    public TmfModelResponse<List<CpuUsageEntryModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {

        if (!(filter instanceof SelectedCpuQueryFilter)) {
            return new TmfModelResponse<>(Collections.emptyList(), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        SelectedCpuQueryFilter cpuQueryFilter = (SelectedCpuQueryFilter) filter;
        long end = filter.getEnd();

        /* Initialize the data */
        fModule.waitForInitialization();
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return FAILED_TREE_RESPONSE;
        }

        ITmfStateSystem kernelSs = TmfStateSystemAnalysisModule.getStateSystem(getTrace(), KernelAnalysisModule.ID);
        if (kernelSs == null) {
            return FAILED_TREE_RESPONSE;
        }
        boolean complete = ss.waitUntilBuilt(0) && kernelSs.waitUntilBuilt(0);

        Map<String, Long> cpuUsageMap = fModule.getCpuUsageInRange(cpuQueryFilter.getSelectedCpus(), filter.getStart(), end);

        List<CpuUsageEntryModel> entryList = new ArrayList<>();
        Map<String, Long> totalMap = Maps.filterKeys(cpuUsageMap, key -> key.startsWith(KernelCpuUsageAnalysis.TOTAL));
        long totalTime = totalMap.values().stream().mapToLong(Long::longValue).sum();
        entryList.add(new CpuUsageEntryModel(fTotalId, -1, getTrace().getName(), TOTAL_SERIES_TID, totalTime));
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
                String tid = strings[1];
                if (!tid.equals(KernelCpuUsageAnalysis.TID_ZERO)) {
                    Long id = fTidToName.inverse().get(key);
                    if (id == null) {
                        id = CPU_USAGE_ID.getAndIncrement();
                        fTidToName.put(id, key);
                    }
                    entryList.add(new CpuUsageEntryModel(id, fTotalId, getProcessName(tid, filter.getStart()), Integer.parseInt(tid), entry.getValue()));
                }
            }
        }

        if (complete) {
            return new TmfModelResponse<>(entryList, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        return new TmfModelResponse<>(entryList, ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    /*
     * Get the process name from its TID by using the LTTng kernel analysis
     * module
     */
    private String getProcessName(String tid, long start) {
        String execName = fProcessNameMap.get(tid);
        if (execName != null) {
            return execName;
        }

        ITmfStateSystem kernelSs = fKernelSs;
        if (kernelSs == null) {
            kernelSs = TmfStateSystemAnalysisModule.getStateSystem(getTrace(), KernelAnalysisModule.ID);
        }
        if (kernelSs == null) {
            return tid;
        }
        fKernelSs = kernelSs;

        /* Retrieve the quark for process tid's execName */
        int execNameQuark = kernelSs.optQuarkAbsolute(Attributes.THREADS, tid, Attributes.EXEC_NAME);
        if (execNameQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
            /*
             * No information on this thread (yet?), skip it for now
             */
            return tid;
        }

        /* Find a name in this attribute's intervals */
        Iterator<ITmfStateInterval> iterator = new StateSystemUtils.QuarkIterator(kernelSs, execNameQuark, start);
        while (iterator.hasNext()) {
            Object execNameObject = iterator.next().getValue();
            if (execNameObject instanceof String) {
                execName = (String) execNameObject;
                fProcessNameMap.put(tid, execName);
                return execName;
            }
        }
        return tid;
    }

    /**
     * @since 2.4
     */
    @Override
    public String getId() {
        return ID;
    }
}