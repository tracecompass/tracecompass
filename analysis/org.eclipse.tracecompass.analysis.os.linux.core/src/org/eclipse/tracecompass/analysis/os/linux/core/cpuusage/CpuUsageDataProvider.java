/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.cpuusage;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.cpuusage.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractStateSystemAnalysisDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedCpuQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedThreadQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
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
public class CpuUsageDataProvider extends AbstractStateSystemAnalysisDataProvider implements ITmfXYDataProvider {

    private static final String NOT_SELECTED = "-1"; //$NON-NLS-1$
    private static final String TOTAL_SERIES_NAME = Objects.requireNonNull(Messages.CpuUsageDataProvider_total);

    /* Empty set gets all the cores */
    private static final Set<Integer> ALL_CPUS = Collections.emptySet();

    private final KernelCpuUsageAnalysis fModule;

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
    public ITmfCommonXAxisResponse fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        ITmfCommonXAxisResponse res = verifyParameters(fModule, filter, monitor);
        if (res != null) {
            return res;
        }

        ITmfStateSystem ss = Objects.requireNonNull(fModule.getStateSystem(), "Statesystem should have been verified by verifyParameters"); //$NON-NLS-1$

        String selectedThread = NOT_SELECTED;
        Set<Integer> cpus = ALL_CPUS;

        if (filter instanceof SelectedThreadQueryFilter) {
            selectedThread = ((SelectedThreadQueryFilter) filter).getSelectedThread();

            if (filter instanceof SelectedCpuQueryFilter) {
                cpus = ((SelectedCpuQueryFilter) filter).getCpu();
            }
        }

        long[] xValues = filter.getTimesRequested();

        /* CPU usage values for total and selected thread */
        double[] totalValues = new double[xValues.length];
        double[] selectedThreadValues = new double[xValues.length];

        long prevTime = Math.max(filter.getStart(), ss.getStartTime());
        for (int i = 1; i < xValues.length; i++) {
            long time = xValues[i];
            if (time >= ss.getStartTime() && time <= ss.getCurrentEndTime() && prevTime < time) {
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
                        if (selectedThread.equals(threadName)) {
                            selectedThreadValues[i] = normalize(prevTime, time, cpuTime);
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
        ySeries.put(TOTAL_SERIES_NAME, new YModel(TOTAL_SERIES_NAME, totalValues));
        if (selectedThread != NOT_SELECTED) {
            ySeries.put(selectedThread, new YModel(selectedThread, selectedThreadValues));
        }

        long currentEnd = ss.getCurrentEndTime();
        boolean complete = ss.waitUntilBuilt(0);
        return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.CpuUsageDataProvider_title), xValues, ySeries.build(), currentEnd, complete);
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
}