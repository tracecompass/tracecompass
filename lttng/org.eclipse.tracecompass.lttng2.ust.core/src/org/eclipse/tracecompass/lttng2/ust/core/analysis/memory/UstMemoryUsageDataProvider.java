/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.memory;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
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
import com.google.common.collect.Maps;

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
        long currentEnd = ss.getCurrentEndTime();
        boolean complete = ss.waitUntilBuilt(0) || filter.getEnd() <= currentEnd;

        Map<Integer, IYModel> models = getYModels(ss, filter);
        if (models.isEmpty()) {
            return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.MemoryUsageDataProvider_Title), xValues, Collections.emptyMap(), complete);
        }

        try {
            for (ITmfStateInterval interval : ss.query2D(models.keySet(), getTimes(xValues, ss.getStartTime(), currentEnd))) {
                if (monitor != null && monitor.isCanceled()) {
                    return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
                }

                IYModel model = models.get(interval.getAttribute());
                Object value = interval.getValue();
                if (model != null && value instanceof Number) {
                    int from = Arrays.binarySearch(xValues, interval.getStartTime());
                    from = (from >= 0) ? from : -1 - from;

                    int to = Arrays.binarySearch(xValues, interval.getEndTime());
                    to = (to >= 0) ? to + 1 : -1 - to;

                    Arrays.fill(model.getData(), from, to, ((Number) value).doubleValue());
                }
            }
        } catch (StateSystemDisposedException e) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(e.getMessage());
        }

        Map<String, IYModel> map = Maps.uniqueIndex(models.values(), IYModel::getName);
        return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.MemoryUsageDataProvider_Title), xValues, map, complete);
    }

    /**
     * Get map of UST_MEMORY_MEMORY_ATTRIBUTE to relevant model
     *
     * @param ss
     *            the queried {@link ITmfStateSystem}
     * @param filter
     *            the {@link TimeQueryFilter}
     * @return a map of the UST_MEMORY_MEMORY_ATTRIBUTE attributes to the
     *         initialized model
     */
    private Map<Integer, IYModel> getYModels(ITmfStateSystem ss, TimeQueryFilter filter) {
        if (!(filter instanceof SelectionTimeQueryFilter)) {
            return Collections.emptyMap();
        }
        Collection<Long> selectedItems = ((SelectionTimeQueryFilter) filter).getSelectedItems();
        Map<Integer, IYModel> selectedSeries = new HashMap<>();
        int length = filter.getTimesRequested().length;
        for (Long id : selectedItems) {
            Integer tidQuark = fIdToQuark.get(id);
            if (tidQuark != null) {
                int memoryAttribute = ss.optQuarkRelative(tidQuark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE);
                if (memoryAttribute != ITmfStateSystem.INVALID_ATTRIBUTE) {
                    String name = getTrace().getName() + ':' + ss.getAttributeName(tidQuark);
                    selectedSeries.put(memoryAttribute, new YModel(name, new double[length]));
                }
            }
        }
        return selectedSeries;
    }

    /**
     * Get a set of the time stamps from the query that intersect the state system
     *
     * @param xValues
     *            queried time stamps
     * @param start
     *            {@link ITmfStateSystem} start time
     * @param end
     *            {@link ITmfStateSystem} current end time
     * @return a set of the intersecting time stamps
     */
    private static Collection<Long> getTimes(long[] xValues, long start, long end) {
        Set<Long> set = new HashSet<>();
        for (long t : xValues) {
            if (start <= t && t <= end) {
                set.add(t);
            }
        }
        return set;
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
