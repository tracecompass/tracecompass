/**********************************************************************
 * Copyright (c) 2018, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.core.model.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.VirtualTableQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.EventTableLine;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfFilterModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.ITmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.TmfVirtualTableModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.table.VirtualTableCell;
import org.eclipse.tracecompass.internal.tmf.core.filter.TmfCollapseFilter;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.filter.FilterManager;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;

/**
 * This data provider will return a virtual table model (wrapped in a response)
 * based on a virtual table query filter. Model returned is for event table.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class TmfEventTableDataProvider extends AbstractTmfTraceDataProvider implements ITmfVirtualTableDataProvider<TmfEventTableColumnDataModel, EventTableLine> {

    /**
     * Key for table search
     */
    public static final String TABLE_SEARCH_KEY = "table_search"; //$NON-NLS-1$

    /**
     * Key for table filters
     */
    public static final String TABLE_FILTERS_KEY = "table_filters"; //$NON-NLS-1$

    /**
     * Extension point ID.
     */
    public static final String ID = "org.eclipse.tracecompass.internal.provisional.tmf.core.model.events.TmfEventTableDataProvider"; //$NON-NLS-1$

    private @Nullable ITmfFilter fFilter;

    /**
     * Maps used for the optimization of filtered query. TODO: Since these map can
     * take a lot of memory, one solution would be to replace these maps by two
     * lists and use binary search instead of TreeMap floor
     */
    private TreeMap<Long, Long> fIndexToRankMap = new TreeMap<>();
    private TreeMap<Long, Long> fRankToIndexMap = new TreeMap<>();

    /**
     * Keep the filtered count for future request with the same filter. TODO: Remove
     * this cache since it is not thread safe and replace it with a better cache
     * with the two list of cached index/rank
     */
    private long fFilteredCount = -1L;

    /**
     * Atomic Long so that every column has a unique ID.
     */
    private static final AtomicLong fAtomicLong = new AtomicLong();

    private static final BiMap<ITmfEventAspect<?>, Long> fAspectToIdMap = HashBiMap.create();

    /**
     * To optimize filtered request a map of index to rank is used to find the
     * starting point of a request in the trace. This value is used to avoid storing
     * too many entries in the map. The corresponding rank associated to an index is
     * only store each time the index is a factor of this value. TODO: Can be
     * improve in the future to have a constant number of point per trace to avoid
     * getting a huge map
     */
    private static final int INDEX_STORING_INTERVAL = 1000;

    /**
     * Constructor
     *
     * @param trace
     *            A trace on which we are interested to fetch an event table model
     */
    public TmfEventTableDataProvider(ITmfTrace trace) {
        super(trace);
    }

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public TmfModelResponse<TmfTreeModel<TmfEventTableColumnDataModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        List<TmfEventTableColumnDataModel> model = new ArrayList<>();

        for (ITmfEventAspect<?> aspect : getTraceAspects(getTrace())) {
            synchronized (fAspectToIdMap) {
                long id = fAspectToIdMap.computeIfAbsent(aspect, a -> fAtomicLong.getAndIncrement());
                model.add(new TmfEventTableColumnDataModel(id, -1, Collections.singletonList(aspect.getName()), aspect.getHelpText(), aspect.isHiddenByDefault()));
            }
        }

        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), model), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> fetchLines(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        VirtualTableQueryFilter queryFilter = FetchParametersUtils.createVirtualTableQueryFilter(fetchParameters);
        if (queryFilter == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        @Nullable ITmfFilter filter = extractFilter(fetchParameters);
        @Nullable ITmfFilter searchFilter = extractSearchFilter(fetchParameters);
        @Nullable TmfCollapseFilter collapseFilter = extractCollapseFilter(fetchParameters);
        Map<Long, ITmfEventAspect<?>> aspects = getAspectsFromColumnsId(queryFilter.getColumnsId());

        if (aspects.isEmpty()) {
            return new TmfModelResponse<>(new TmfVirtualTableModel<EventTableLine>(Collections.emptyList(), Collections.emptyList(), queryFilter.getIndex(), 0), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        boolean forwardSearch = queryFilter.getCount() >= 0;

        TableEventRequest request;
        if (filter != null) {
            request = filteredTableRequest(Math.abs(queryFilter.getCount()), queryFilter.getIndex(), aspects, filter, searchFilter, forwardSearch, collapseFilter, monitor);
        } else {
            request = tableRequest(Math.abs(queryFilter.getCount()), queryFilter.getIndex(), aspects, searchFilter, forwardSearch, collapseFilter, monitor);
            request.setEventCount(getTrace().getNbEvents());
        }

        getTrace().sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, e.getMessage());
        }

        if (request.isCancelled()) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
        }

        List<Long> columnsIds = new ArrayList<>(aspects.keySet());
        TmfVirtualTableModel<EventTableLine> model = new TmfVirtualTableModel<>(columnsIds, request.getEventLines(), queryFilter.getIndex(), request.getCurrentCount());
        return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    /**
     * Find the index in the table of an event using the rank in the trace or
     * the timestamp value. It will take any filter into consideration.
     *
     * @param fetchParameters
     *            Map of parameters that contain the filter applied to the
     *            table, if any. Everything else is ignored.
     * @param traceRank
     *            Rank of the event in the trace
     * @param timeBegin
     *            Timestamp of the event
     * @param monitor
     *            Progress monitor
     * @return Index in the table
     */
    public TmfModelResponse<List<Long>> fetchIndex(Map<String, Object> fetchParameters, long traceRank, long timeBegin, @Nullable IProgressMonitor monitor) {
        @Nullable ITmfFilter filter = extractFilter(fetchParameters);
        long rank;
        if (traceRank == -1) {
            ITmfContext context = getTrace().seekEvent(TmfTimestamp.fromNanos(timeBegin));
            rank = context.getRank();
        } else {
            rank = traceRank;
        }

        if (filter == null) {
            return new TmfModelResponse<>(Collections.singletonList(rank), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        applyFilter(filter);

        Entry<Long, Long> nearestEntry = fRankToIndexMap.floorEntry(rank);
        long startingIndex = nearestEntry != null ? nearestEntry.getValue() : 0L;
        long startingRank = nearestEntry != null ? nearestEntry.getKey() : 0L;

        List<Long> foundIndex = new ArrayList<>();
        TmfEventRequest request = new TmfEventRequest(ITmfEvent.class, TmfTimeRange.ETERNITY, startingRank, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND) {
            private long currentIndex = startingIndex;
            private long fRank = startingRank;

            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (monitor != null && monitor.isCanceled()) {
                    cancel();
                    return;
                }

                if (fRank >= rank) {
                    foundIndex.add(currentIndex);
                    done();
                    return;
                }

                if (filter.matches(event)) {
                    currentIndex++;
                }
                fRank++;
            }
        };

        getTrace().sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, e.getMessage());
        }

        return new TmfModelResponse<>(foundIndex, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private abstract class TableEventRequest extends TmfEventRequest {

        private long fEventCount = 0L;
        private List<EventTableLine> fEventLines = new ArrayList<>();

        public TableEventRequest(long startingRank) {
            super(ITmfEvent.class, TmfTimeRange.ETERNITY, startingRank, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
        }

        public void incrementCount() {
            fEventCount++;
        }

        public void setEventCount(long count) {
            fEventCount = count;
        }

        public long getCurrentCount() {
            return fEventCount;
        }

        public List<EventTableLine> getEventLines() {
            return fEventLines;
        }
    }

    private TableEventRequest filteredTableRequest(int queryCount,
            long queryIndex,
            Map<Long, ITmfEventAspect<?>> aspects,
            ITmfFilter filter,
            @Nullable ITmfFilter searchFilter,
            boolean forwardSearch,
            @Nullable ITmfFilter collapseFilter,
            @Nullable IProgressMonitor monitor) {

        applyFilter(filter);

        Entry<Long, Long> nearestEntry = fIndexToRankMap.floorEntry(queryIndex);
        long startingRank = nearestEntry != null ? nearestEntry.getValue() : 0L;
        Long startingIndex = nearestEntry != null ? nearestEntry.getKey() : 0L;

        return new TableEventRequest(startingRank) {
            private long currentIndex = startingIndex;
            private long rank = startingRank;

            @Override
            public void handleData(@NonNull ITmfEvent event) {
                super.handleData(event);
                if (monitor != null && monitor.isCanceled()) {
                    cancel();
                    return;
                }

                List<EventTableLine> events = getEventLines();
                if (filter.matches(event) && (collapseFilter == null || collapseFilter.matches(event))) {
                    if (searchFilter == null || searchFilter.matches(event)) {
                        if (events.size() < queryCount && queryIndex <= currentIndex) {
                            events.add(buildEventTableLine(aspects, event, currentIndex, rank));
                        }
                        if (currentIndex % INDEX_STORING_INTERVAL == 0) {
                            fIndexToRankMap.put(currentIndex, rank);
                            fRankToIndexMap.put(rank, currentIndex);
                        }
                    }
                    currentIndex++;
                    incrementCount();
                } else if (collapseFilter != null && !events.isEmpty()) {
                    // If a collapse filter is present, we need to update the last event we have in
                    // our list to increment the repeat count value.
                    int lastIndex = events.size() - 1;
                    EventTableLine prevLine = events.get(lastIndex);
                    long prevRepeatCount = prevLine.getRepeatCount();
                    events.set(lastIndex, new EventTableLine(prevLine.getCells(), prevLine.getIndex(), prevLine.getTimestamp(), prevLine.getRank(), prevRepeatCount++));
                }

                if (searchFilter != null && ((!forwardSearch && getCurrentCount() == queryCount) || events.size() == queryCount)) {
                    done();
                    return;
                }
                rank++;
            }

            @Override
            public long getCurrentCount() {
                long currentCount = super.getCurrentCount();
                if (fFilteredCount == -1L) {
                    fFilteredCount = currentCount;
                }
                return fFilteredCount;
            }
        };
    }

    private TableEventRequest tableRequest(int queryCount,
            long queryIndex,
            Map<Long, ITmfEventAspect<?>> aspects,
            @Nullable ITmfFilter searchFilter,
            boolean forwardSearch,
            @Nullable ITmfFilter collapseFilter,
            @Nullable IProgressMonitor monitor) {

        return new TableEventRequest(queryIndex) {
            private long rank = queryIndex;

            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (monitor != null && monitor.isCanceled()) {
                    cancel();
                    return;
                }

                List<EventTableLine> events = getEventLines();
                if ((searchFilter == null || searchFilter.matches(event)) && (collapseFilter == null || collapseFilter.matches(event))) {
                    if (events.size() < queryCount) {
                        events.add(buildEventTableLine(aspects, event, rank, rank));
                    }
                } else if (collapseFilter != null && !events.isEmpty()) {
                    // If a collapse filter is present, we need to update the last event we have in
                    // our list to increment the repeat count value.
                    int lastIndex = events.size() - 1;
                    EventTableLine prevLine = events.get(lastIndex);
                    long prevRepeatCount = prevLine.getRepeatCount();
                    events.set(lastIndex, new EventTableLine(prevLine.getCells(), prevLine.getIndex(), prevLine.getTimestamp(), prevLine.getRank(), prevRepeatCount++));
                }

                if ((searchFilter != null && !forwardSearch && getNbRead() == queryCount) || events.size() == queryCount) {
                    done();
                    return;
                }
                rank++;
            }
        };
    }

    /**
     * Build an event line using the given aspects.
     *
     * @param aspects
     *            Aspects to resolve
     * @param event
     *            The event used to resolve the aspect
     * @param lineIndex
     *            Line index
     * @param lineRank
     *            rank in the trace
     * @return A new event table line
     */
    private static EventTableLine buildEventTableLine(Map<Long, ITmfEventAspect<?>> aspects, ITmfEvent event, long lineIndex, long lineRank) {
        List<VirtualTableCell> entry = new ArrayList<>(aspects.size());
        for (Entry<Long, ITmfEventAspect<?>> aspectEntry : aspects.entrySet()) {
            Object aspectResolved = aspectEntry.getValue().resolve(event);
            String cellContent = aspectResolved == null ? StringUtils.EMPTY : String.valueOf(aspectResolved);
            entry.add(new VirtualTableCell(cellContent));
        }
        return new EventTableLine(entry, lineIndex, event.getTimestamp(), lineRank, 0);
    }

    /**
     * Apply a filter to this provider that will be used for future request. If the
     * current filter is null or not equal to the provided filter, the IndexToRank
     * and RankToIndex maps are cleared
     *
     * @param filter
     *            Filter to apply
     */
    private void applyFilter(ITmfFilter filter) {
        if (!filter.equals(fFilter)) {
            fFilter = filter;
            fIndexToRankMap.clear();
            fRankToIndexMap.clear();
            fFilteredCount = -1L;
        }
    }

    /**
     * We want to resolve only the columns that will be shown. This limits resolve
     * operation and model size. If list of desired columns is empty, we retrieve
     * all columns by default
     *
     * @param desiredColumns
     *            The list of desired columns name that we want to
     * @return The list of {@link ITmfEventAspect} that match the desired columns
     *         name
     */
    private static Map<Long, ITmfEventAspect<?>> getAspectsFromColumnsId(List<Long> desiredColumns) {
        Map<Long, ITmfEventAspect<?>> aspects = new LinkedHashMap<>();
        if (!desiredColumns.isEmpty()) {
            for (Long columnsId : desiredColumns) {
                ITmfEventAspect<?> aspect = fAspectToIdMap.inverse().get(columnsId);
                if (aspect != null) {
                    aspects.put(columnsId, aspect);
                }
            }
            return aspects;
        }

        return Objects.requireNonNull(fAspectToIdMap.inverse());
    }

    private static @Nullable ITmfFilter extractFilter(Map<String, Object> fetchParameters) {
        Object filtersObject = fetchParameters.get(TABLE_FILTERS_KEY);
        if (filtersObject instanceof ITmfFilterModel) {
            ITmfFilterModel filters = (ITmfFilterModel) filtersObject;
            Map<Long, String> filterMap = filters.getTableFilter();
            List<String> presetFilter = filters.getPresetFilter();

            if ((filterMap == null || filterMap.isEmpty()) && (presetFilter == null || presetFilter.isEmpty())) {
                return null;
            }

            TmfFilterRootNode rootFilter = new TmfFilterRootNode();
            if (filterMap != null && !filterMap.isEmpty()) {
                for (Entry<Long, String> filterEntry : filterMap.entrySet()) {
                    TmfFilterMatchesNode filterNode = new TmfFilterMatchesNode(null);
                    ITmfEventAspect<?> aspect = fAspectToIdMap.inverse().get(filterEntry.getKey());
                    if (aspect == null) {
                        return null;
                    }
                    filterNode.setEventAspect(aspect);
                    filterNode.setRegex(filterEntry.getValue());
                    rootFilter.addChild(filterNode);
                }
            }

            if (presetFilter != null && !presetFilter.isEmpty()) {
                ITmfFilterTreeNode[] savedFilters = FilterManager.getSavedFilters();
                for (ITmfFilterTreeNode filterNode : savedFilters) {
                    if (filterNode instanceof TmfFilterNode) {
                        for (String presetFilterName : presetFilter) {
                            if (((TmfFilterNode) filterNode).getFilterName().equals(presetFilterName)) {
                                rootFilter.addChild(filterNode);
                            }
                        }
                    }
                }
            }

            return rootFilter;
        }
        return null;
    }

    private static @Nullable ITmfFilter extractSearchFilter(Map<String, Object> fetchParameters) {
        Object searchFilterObject = fetchParameters.get(TABLE_SEARCH_KEY);
        if (searchFilterObject instanceof Map<?, ?>) {
            Map<Long, String> searchMap = (Map<Long, String>) searchFilterObject;
            if (searchMap.isEmpty()) {
                return null;
            }

            TmfFilterRootNode rootFilter = new TmfFilterRootNode();
            for (Entry<Long, String> searchEntry : searchMap.entrySet()) {
                TmfFilterMatchesNode searchNode = new TmfFilterMatchesNode(rootFilter);
                ITmfEventAspect<?> aspect = fAspectToIdMap.inverse().get(searchEntry.getKey());
                if (aspect == null) {
                    return null;
                }
                searchNode.setEventAspect(aspect);
                searchNode.setRegex(searchEntry.getValue());
            }
            return rootFilter;
        }
        return null;
    }

    private static @Nullable TmfCollapseFilter extractCollapseFilter(Map<String, Object> fetchParameters) {
        Object filtersObject = fetchParameters.get(TABLE_FILTERS_KEY);
        if (filtersObject instanceof ITmfFilterModel) {
            ITmfFilterModel filters = (ITmfFilterModel) filtersObject;
            if (filters.isCollapseFilter()) {
                return new TmfCollapseFilter();
            }
            return null;
        }
        return null;
    }

    private static Iterable<ITmfEventAspect<?>> getTraceAspects(ITmfTrace trace) {
        if (trace instanceof TmfExperiment) {
            return getExperimentAspects((TmfExperiment) trace);
        }
        return trace.getEventAspects();
    }

    /**
     * Get the events table for an experiment. If all traces in the experiment are
     * of the same type, use the same behavior as if it was one trace of that type.
     *
     * @param experiment
     *            the experiment
     * @param parent
     *            the parent Composite
     * @param cacheSize
     *            the event table cache size
     * @return An event table of the appropriate type
     */
    private static Iterable<ITmfEventAspect<?>> getExperimentAspects(TmfExperiment experiment) {
        List<ITmfTrace> traces = experiment.getTraces();
        ImmutableSet.Builder<ITmfEventAspect<?>> builder = new ImmutableSet.Builder<>();

        /* For experiments, we'll add a "trace name" aspect/column */
        builder.add(TmfBaseAspects.getTraceNameAspect());

        if (hasCommonTraceType(experiment)) {
            /*
             * All the traces in this experiment are of the same type, let's just use the
             * normal table for that type.
             */
            builder.addAll(traces.get(0).getEventAspects());

        } else {
            /*
             * There are different trace types in the experiment, so we are definitely using
             * a TmfEventsTable. Aggregate the columns from all trace types.
             */
            for (ITmfTrace trace : traces) {
                builder.addAll(trace.getEventAspects());
            }
        }
        return builder.build();
    }

    /**
     * Check if an experiment contains traces of all the same type. If so, returns
     * this type as a String. If not, returns null.
     *
     * @param experiment
     *            The experiment
     * @return The common trace type if there is one, or 'null' if there are
     *         different types.
     */
    private static boolean hasCommonTraceType(TmfExperiment experiment) {
        @Nullable String commonTraceType = null;
        for (ITmfTrace trace : experiment.getTraces()) {
            String traceType = trace.getTraceTypeId();
            if (commonTraceType != null && !commonTraceType.equals(traceType)) {
                return false;
            }
            commonTraceType = traceType;
        }
        return commonTraceType != null;
    }
}