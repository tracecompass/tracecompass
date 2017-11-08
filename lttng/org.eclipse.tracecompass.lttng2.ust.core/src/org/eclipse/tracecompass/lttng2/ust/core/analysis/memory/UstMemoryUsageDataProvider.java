/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.memory;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.memory.MemoryUsageTreeModel;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryStrings;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractStateSystemAnalysisDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.FilterTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
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
public class UstMemoryUsageDataProvider extends AbstractStateSystemAnalysisDataProvider
    implements ITmfTreeXYDataProvider<MemoryUsageTreeModel> {

    /**
     * Entry point ID.
     * @since 3.2
     */
    public static final String ID = "org.eclipse.tracecompass.lttng2.ust.core.analysis.memory.UstMemoryUsageDataProvider"; //$NON-NLS-1$
    private static final AtomicLong ENTRY_IDS = new AtomicLong();

    /**
     * Two way association between quarks and entry IDs, ensures that a single ID is
     * reused per every quark, and finds the quarks to query for the XY models.
     */
    private final BiMap<Long, Integer> fIdToQuark = HashBiMap.create();
    private final long fTraceId = ENTRY_IDS.getAndIncrement();

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
        long[] xValues = filter.getTimesRequested();
        Map<Integer, double[]> tempModel = initSeries(filter);
        long currentEnd = ss.getCurrentEndTime();
        boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= currentEnd;
        if (tempModel.isEmpty()) {
            return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.MemoryUsageDataProvider_Title), xValues, Collections.emptyMap(), complete);
        }

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
                    for (Entry<Integer, double[]> entry : tempModel.entrySet()) {
                        int quark = entry.getKey();
                        int memoryAttribute = ss.optQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE);

                        if (memoryAttribute != ITmfStateSystem.INVALID_ATTRIBUTE) {
                            double[] values = entry.getValue();
                            Object val = fullState.get(memoryAttribute).getValue();
                            values[i] = extractValue(val);
                        }
                    }
                }
            }
        } catch (StateSystemDisposedException e) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(e.getMessage());
        }

        ImmutableMap.Builder<String, IYModel> ySeries = ImmutableMap.builder();
        for (Entry<Integer, double[]> tempEntry : tempModel.entrySet()) {
            String name = getTrace().getName() + ':' + ss.getAttributeName(tempEntry.getKey());
            ySeries.put(name, new YModel(name, tempEntry.getValue()));
        }

        return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.MemoryUsageDataProvider_Title), xValues, ySeries.build(), complete);
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
    private Map<Integer, double[]> initSeries(TimeQueryFilter filter) {
        if (filter instanceof SelectionTimeQueryFilter) {
            Collection<Long> selectedEntries = ((SelectionTimeQueryFilter) filter).getSelectedItems();
            Map<Integer, double[]> selectedSeries = new HashMap<>();
            int length = filter.getTimesRequested().length;
            for (Long id : selectedEntries) {
                Integer quark = fIdToQuark.get(id);
                if (quark != null) {
                    selectedSeries.put(quark, new double[length]);
                }
            }
            return selectedSeries;
        }
        return Collections.emptyMap();
    }

    private static long extractValue(@Nullable Object val) {
        if (val instanceof Number) {
            return ((Number) val).longValue();
        }
        return 0;
    }

    /**
     * @since 3.2
     */
    @Override
    public TmfModelResponse<List<MemoryUsageTreeModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {

        // Waiting for initialization should ensure that the state system is not null.
        fModule.waitForInitialization();
        ITmfStateSystem ss = fModule.getStateSystem();
        if (ss == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        // Get the quarks before the full states to ensure that the attributes will be present in the full state
        boolean isComplete = ss.waitUntilBuilt(0);
        // to filter the active threads or not
        boolean filtered = false;
        if (filter instanceof FilterTimeQueryFilter) {
            filtered = ((FilterTimeQueryFilter) filter).isFiltered();
        }
        List<Integer> tidQuarks = ss.getSubAttributes(-1, false);
        List<ITmfStateInterval> nameFullState;
        List<ITmfStateInterval> activeFullState;
        try {
            nameFullState = ss.queryFullState(ss.getCurrentEndTime());
            activeFullState = ss.queryFullState(Long.max(filter.getStart(), ss.getStartTime()));
        } catch (StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }

        ImmutableList.Builder<MemoryUsageTreeModel> builder = ImmutableList.builder();
        builder.add(new MemoryUsageTreeModel(fTraceId, -1L, -1, getTrace().getName()));
        for (int quark : tidQuarks) {
            int memoryAttribute = ss.optQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE);
            int procNameQuark = ss.optQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_PROCNAME_ATTRIBUTE);

            if (memoryAttribute != ITmfStateSystem.INVALID_ATTRIBUTE && procNameQuark != ITmfStateSystem.INVALID_ATTRIBUTE) {
                ITmfStateInterval threadMemoryInterval = activeFullState.get(memoryAttribute);
                if (!filtered || threadMemoryInterval.getEndTime() < filter.getEnd()) {
                    String name = String.valueOf(nameFullState.get(procNameQuark).getValue());
                    int tid = Integer.parseInt(ss.getAttributeName(quark));

                    // Check if an ID has already been created for this quark.
                    Long id = fIdToQuark.inverse().get(quark);
                    if (id == null) {
                        id = ENTRY_IDS.getAndIncrement();
                        fIdToQuark.put(id, quark);
                    }
                    builder.add(new MemoryUsageTreeModel(id, fTraceId, tid, name));
                }
            }
        }

        ImmutableList<MemoryUsageTreeModel> list = builder.build();
        if (isComplete) {
            TmfModelResponse<List<MemoryUsageTreeModel>> tmfModelResponse = new TmfModelResponse<>(list, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
            return tmfModelResponse;
        }
        return new TmfModelResponse<>(list, ITmfResponse.Status.RUNNING, CommonStatusMessage.RUNNING);
    }

    /**
     * @since 3.2
     */
    @Override
    public String getId() {
        return ID;
    }
}
