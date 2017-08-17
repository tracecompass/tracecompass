/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryStrings;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractStateSystemAnalysisDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
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

import com.google.common.collect.ImmutableMap;

/**
 * This data provider will return a XY model based on a query filter. The model
 * is used afterwards by any viewer to draw charts. Model returned is for Memory
 * Usage views
 *
 * @author Yonni Chen
 * @since 3.1
 */
@NonNullByDefault
@SuppressWarnings("restriction")
public class UstMemoryUsageDataProvider extends AbstractStateSystemAnalysisDataProvider implements ITmfXYDataProvider {

    private final UstMemoryAnalysisModule fModule;

    /**
     * Create an instance of {@link UstMemoryUsageDataProvider}. Returns a null
     * instance if the analysis module is not found.
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @return A MemoryUsageDataProvider. If analysis module is not found, it
     *         returns null
     */
    public static @Nullable UstMemoryUsageDataProvider create(ITmfTrace trace) {
        UstMemoryAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, UstMemoryAnalysisModule.class, UstMemoryAnalysisModule.ID);
        if (module != null) {
            module.schedule();
            return new UstMemoryUsageDataProvider(trace, module);
        }
        return null;
    }

    /**
     * Constructor
     */
    private UstMemoryUsageDataProvider(ITmfTrace trace, UstMemoryAnalysisModule module) {
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
        Map<Integer, double[]> tempModel = new HashMap<>();
        List<Integer> tidQuarks = ss.getSubAttributes(-1, false);
        Map<Integer, String> processName = new HashMap<>();
        long[] xValues = filter.getTimesRequested();

        long currentEnd = ss.getCurrentEndTime();

        /*
         * TODO: It should only show active threads in the time range. If a tid does not
         * have any memory value (only 1 interval in the time range with value null or
         * 0), then its series should not be displayed. TODO: Support TID reuse
         */
        try {
            for (int i = 0; i < xValues.length; i++) {
                if (monitor != null && monitor.isCanceled()) {
                    return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
                }
                long time = xValues[i];
                if (time >= ss.getStartTime() && time <= currentEnd) {
                    List<ITmfStateInterval> fullState = ss.queryFullState(time);
                    for (int quark : tidQuarks) {
                        int memoryAttribute = ss.optQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE);
                        int procNameQuark = ss.optQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_PROCNAME_ATTRIBUTE);

                        if (memoryAttribute != ITmfStateSystem.INVALID_ATTRIBUTE && procNameQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                            String procnameValue = (String) fullState.get(procNameQuark).getValue();

                            if (procnameValue != null && !procnameValue.isEmpty()) {
                                processName.put(quark, procnameValue);
                            }

                            tempModel.putIfAbsent(quark, new double[xValues.length]);

                            double[] values = Objects.requireNonNull(tempModel.get(quark));
                            Object val = fullState.get(memoryAttribute).getValue();
                            double yvalue = extractValue(val);
                            values[i] = yvalue;
                        }
                    }
                }
            }
        } catch (StateSystemDisposedException e) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(e.getMessage());
        }

        ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
        for (Entry<Integer, double[]> tempEntry : tempModel.entrySet()) {
            @Nullable String name = processName.get(tempEntry.getKey());
            name = beautifyName(name, ss, tempEntry.getKey());
            ySeries.put(name, new YModel(name, tempEntry.getValue()));
        }

        boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= currentEnd;
        return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.MemoryUsageDataProvider_Title), xValues, ySeries.build(), complete);
    }

    private static String beautifyName(@Nullable String name, ITmfStateSystem ss, int quark) {
        if (name != null && !name.isEmpty()) {
            return name + " (" + ss.getAttributeName(quark) + ')'; //$NON-NLS-1$
        }
        return '(' + ss.getAttributeName(quark) + ')';
    }

    private static long extractValue(@Nullable Object val) {
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return 0;
    }
}
