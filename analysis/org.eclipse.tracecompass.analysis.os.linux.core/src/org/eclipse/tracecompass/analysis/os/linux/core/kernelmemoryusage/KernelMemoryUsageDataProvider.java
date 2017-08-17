/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.kernelmemoryusage;

import java.util.List;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernelmemoryusage.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractStateSystemAnalysisDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectedThreadQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.util.Pair;

import com.google.common.collect.ImmutableMap;

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
public class KernelMemoryUsageDataProvider extends AbstractStateSystemAnalysisDataProvider implements ITmfXYDataProvider {

    private final KernelMemoryAnalysisModule fModule;
    private static final String NOT_SELECTED = "-1"; //$NON-NLS-1$

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
        super(trace);
        fModule = module;
    }

    @Override
    public @NonNull TmfModelResponse<ITmfCommonXAxisModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        TmfModelResponse<ITmfCommonXAxisModel> res = verifyParameters(fModule, filter, monitor);
        if (res != null) {
            return res;
        }

        ITmfStateSystem ss = Objects.requireNonNull(fModule.getStateSystem(), "Statesystem should have been verified by verifyParameters"); //$NON-NLS-1$
        String selectedThread = NOT_SELECTED;
        if (filter instanceof SelectedThreadQueryFilter) {
            selectedThread = ((SelectedThreadQueryFilter) filter).getSelectedThread();
        }

        long[] xValues = filter.getTimesRequested();

        /**
         * For a given time range, we plot two lines representing the memory allocation.
         * The first line represents the total memory allocation of every process. The
         * second line represent the memory allocation of the selected thread.
         */
        double[] totalKernelMemoryValues = new double[xValues.length];
        double[] selectedThreadValues = new double[xValues.length];

        int selectedThreadKey = ss.optQuarkAbsolute(selectedThread);
        long currentEnd = ss.getCurrentEndTime();

        try {
            for (int i = 0; i < xValues.length; i++) {
                if (monitor != null && monitor.isCanceled()) {
                    return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
                }

                long time = xValues[i];
                if (time >= ss.getStartTime() && time <= currentEnd) {
                    /* The subattributes of the root are the different threads */
                    List<Integer> threadQuarkList = ss.getSubAttributes(-1, false);

                    List<ITmfStateInterval> kernelState = ss.queryFullState(time);

                    /* We add the value of each thread to the total quantity */
                    for (Integer threadQuark : threadQuarkList) {
                        Object val = kernelState.get(threadQuark).getValue();
                        long value = extractValue(val);

                        totalKernelMemoryValues[i] += value;
                        if (selectedThreadKey  == threadQuark) {
                            selectedThreadValues[i] = value;
                        }
                    }
                }
            }

            Pair<Double, Double> values = extractValuesShift(ss, Long.min(filter.getEnd(), currentEnd), selectedThreadKey);

            /**
             * We shift the two displayed lines up.
             */
            for (int i = 0; i < xValues.length; i++) {
                /* Total Kernel Memory Values Shift is the first element in the pair */
                totalKernelMemoryValues[i] += values.getFirst();
                /* Select Thread Values Shift is the second element in the pair */
                selectedThreadValues[i] += values.getSecond();
            }
        } catch (StateSystemDisposedException e) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(e.getMessage());
        }

        ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
        String total = Objects.requireNonNull(Messages.KernelMemoryUsageDataProvider_Total);
        ySeries.put(total, new YModel(total, totalKernelMemoryValues));
        if (selectedThread != NOT_SELECTED) {
            ySeries.put(selectedThread, new YModel(selectedThread, selectedThreadValues));
        }

        boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= currentEnd;
        return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.KernelMemoryUsageDataProvider_title), xValues, ySeries.build(), complete);
    }

    private static Pair<Double, Double> extractValuesShift(ITmfStateSystem ss, long time, int selectedThreadKey) throws StateSystemDisposedException {

        /**
         * For each thread, we look for its lowest value since the beginning of the
         * trace. This way, we can avoid negative values in the plot.
         */
        double totalKernelMemoryValuesShift = 0;
        double selectThreadValuesShift = 0;

        /*
         * The lowest value we are searching is at the end of the current selected zone
         */
        List<ITmfStateInterval> kernelState = ss.queryFullState(time);
        List<Integer> threadQuarkList = ss.getQuarks("*", KernelMemoryAnalysisModule.THREAD_LOWEST_MEMORY_VALUE); //$NON-NLS-1$
        /* We add the lowest value of each thread */
        for (Integer threadQuark : threadQuarkList) {
            ITmfStateInterval lowestMemoryInterval = kernelState.get(threadQuark);
            Object val = lowestMemoryInterval.getValue();
            long lowestMemoryValue = extractValue(val);

            // We want to add up a positive quantity.
            totalKernelMemoryValuesShift -= lowestMemoryValue;

            if (threadQuark == selectedThreadKey) {
                // We want to add up a positive quantity.
                selectThreadValuesShift = -lowestMemoryValue;
            }
        }
        return new Pair<>(totalKernelMemoryValuesShift, selectThreadValuesShift);
    }

    private static long extractValue(@Nullable Object val) {
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return 0;
    }
}
