/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.memory.MemoryUsageTreeModel;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.AbstractTreeCommonXDataProvider;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.model.YModel;
import org.eclipse.tracecompass.tmf.core.model.filters.FilterTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

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
public class UstMemoryUsageDataProvider extends AbstractTreeCommonXDataProvider<UstMemoryAnalysisModule, MemoryUsageTreeModel> {

    /**
     * Entry point ID.
     * @since 3.2
     */
    public static final String ID = "org.eclipse.tracecompass.lttng2.ust.core.analysis.memory.UstMemoryUsageDataProvider"; //$NON-NLS-1$

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
        super(trace, module);
    }

    /**
     * @since 3.3
     */
    @Override
    protected @Nullable Map<String, IYModel> getYModels(ITmfStateSystem ss, SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        long[] xValues = filter.getTimesRequested();
        long currentEnd = ss.getCurrentEndTime();

        Map<Integer, IYModel> models = initYModels(ss, filter);

        for (ITmfStateInterval interval : ss.query2D(models.keySet(), getTimes(filter, ss.getStartTime(), currentEnd))) {
            if (monitor != null && monitor.isCanceled()) {
                return null;
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

        return Maps.uniqueIndex(models.values(), IYModel::getName);
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
    private Map<Integer, IYModel> initYModels(ITmfStateSystem ss, SelectionTimeQueryFilter filter) {
        Map<Integer, IYModel> selectedSeries = new HashMap<>();
        int length = filter.getTimesRequested().length;
        for (Entry<Long, Integer> entry : getSelectedEntries(filter).entrySet()) {
            int tidQuark = entry.getValue();
            int memoryAttribute = ss.optQuarkRelative(tidQuark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE);
            if (memoryAttribute != ITmfStateSystem.INVALID_ATTRIBUTE) {
                String name = getTrace().getName() + ':' + ss.getAttributeName(tidQuark);
                selectedSeries.put(memoryAttribute, new YModel(entry.getKey(), name, new double[length]));
            }
        }
        return selectedSeries;
    }

    /**
     * @since 3.3
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

        List<ITmfStateInterval> nameFullState = ss.queryFullState(ss.getCurrentEndTime());
        List<Integer> tidQuarks = ss.getSubAttributes(-1, false);

        ImmutableList.Builder<MemoryUsageTreeModel> builder = ImmutableList.builder();
        long rootId = getId(ITmfStateSystem.ROOT_ATTRIBUTE);
        builder.add(new MemoryUsageTreeModel(rootId, -1L, -1, getTrace().getName()));
        for (int quark : tidQuarks) {
            int memoryAttribute = ss.optQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_MEMORY_ATTRIBUTE);
            int procNameQuark = ss.optQuarkRelative(quark, UstMemoryStrings.UST_MEMORY_PROCNAME_ATTRIBUTE);

            String name = ss.getAttributeName(quark);
            if (procNameQuark != ITmfStateSystem.INVALID_ATTRIBUTE && procNameQuark < nameFullState.size()) {
                name = String.valueOf(nameFullState.get(procNameQuark).getValue());
            }

            if (memoryAttribute != ITmfStateSystem.INVALID_ATTRIBUTE
                    && (active == null || (memoryAttribute < active.size() && active.get(memoryAttribute).getEndTime() < end))) {

                int tid = Integer.parseInt(ss.getAttributeName(quark));
                builder.add(new MemoryUsageTreeModel(getId(quark), rootId, tid, name));
            }
        }

        return builder.build();
    }

    /**
     * @since 3.2
     */
    @Override
    public String getId() {
        return ID;
    }

    /**
     * @since 3.3
     */
    @Override
    protected boolean isCacheable() {
        return false;
    }

    /**
     * @since 3.3
     */
    @Override
    protected String getTitle() {
        return Objects.requireNonNull(Messages.MemoryUsageDataProvider_Title);
    }
}
