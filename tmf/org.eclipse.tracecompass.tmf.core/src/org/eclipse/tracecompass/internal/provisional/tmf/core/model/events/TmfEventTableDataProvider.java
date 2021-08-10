/**********************************************************************
 * Copyright (c) 2018, 2021 Ericsson
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
import java.util.Optional;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
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
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventField;
import org.eclipse.tracecompass.tmf.core.event.ITmfEventType;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfBaseAspects;
import org.eclipse.tracecompass.tmf.core.filter.FilterManager;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.filter.model.ITmfFilterTreeNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterMatchesNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterNode;
import org.eclipse.tracecompass.tmf.core.filter.model.TmfFilterRootNode;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.CoreFilterProperty;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;

/**
 * This data provider will return a virtual table model (wrapped in a response)
 * based on a virtual table query filter. Model returned is for event table.
 *
 * @author Yonni Chen
 * @since 4.0
 */
public class TmfEventTableDataProvider extends AbstractTmfTraceDataProvider implements ITmfVirtualTableDataProvider<TmfEventTableColumnDataModel, EventTableLine> {

    /**
     * Key for table search regex filter expressions (regex only)
     */
    public static final String TABLE_SEARCH_EXPRESSION_KEY = "table_search_expressions"; //$NON-NLS-1$
    /**
     * Key for table search direction (forward or backward)
     */
    public static final String TABLE_SEARCH_DIRECTION_KEY = "table_search_direction"; //$NON-NLS-1$
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
     * Maps used for the optimization of filtered query.
     *
     * TODO: Since these map can take a lot of memory, one solution would be to
     * replace these maps by two lists and use binary search instead of TreeMap
     * floor.
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
     * Direction of search, navigation etc.
     * @since 7.1
     */
    public enum Direction {
        /** Search next */
        NEXT,
        /** Search previous*/
        PREVIOUS
    }

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
        boolean hasTs = false;
        for (ITmfEventAspect<?> aspect : getTraceAspects(getTrace())) {
            synchronized (fAspectToIdMap) {
                long id = fAspectToIdMap.computeIfAbsent(aspect, a -> fAtomicLong.getAndIncrement());
                model.add(new TmfEventTableColumnDataModel(id, -1, Collections.singletonList(aspect.getName()), aspect.getHelpText(), aspect.isHiddenByDefault()));
                hasTs |= (aspect == TmfBaseAspects.getTimestampAspect());
            }
        }
        if (hasTs) {
            synchronized (fAspectToIdMap) {
                ITmfEventAspect<Long> aspect = TmfBaseAspects.getTimestampNsAspect();
                long id = fAspectToIdMap.computeIfAbsent(aspect, a -> fAtomicLong.getAndIncrement());
                model.add(new TmfEventTableColumnDataModel(id, -1, Collections.singletonList(aspect.getName()), aspect.getHelpText(), aspect.isHiddenByDefault()));
            }
        }
        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), model), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public TmfModelResponse<ITmfVirtualTableModel<EventTableLine>> fetchLines(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        if (!fetchParameters.containsKey(DataProviderParameterUtils.REQUESTED_TABLE_INDEX_KEY) &&
                fetchParameters.containsKey(DataProviderParameterUtils.REQUESTED_TIME_KEY)) {
            fetchParameters.put(DataProviderParameterUtils.REQUESTED_TABLE_INDEX_KEY, getTableIndex(fetchParameters));
        }
        if (!fetchParameters.containsKey(DataProviderParameterUtils.REQUESTED_COLUMN_IDS_KEY)) {
            fetchParameters.put(DataProviderParameterUtils.REQUESTED_COLUMN_IDS_KEY, Collections.emptyList());
        }
        VirtualTableQueryFilter queryFilter = FetchParametersUtils.createVirtualTableQueryFilter(fetchParameters);
        if (queryFilter == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }

        @Nullable ITmfFilter filter = extractFilter(fetchParameters);
        @Nullable ITmfFilter searchFilter = extractSearchFilter(fetchParameters);
        @Nullable TmfCollapseFilter collapseFilter = extractCollapseFilter(fetchParameters);
        Map<Long, ITmfEventAspect<?>> aspects = getAspectsFromColumnsId(queryFilter.getColumnsId());

        if (aspects.isEmpty()) {
            return new TmfModelResponse<>(new TmfVirtualTableModel<>(Collections.emptyList(), Collections.emptyList(), queryFilter.getIndex(), 0), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        List<Long> columnsIds = new ArrayList<>(aspects.keySet());
        if (getTrace().getNbEvents() == 0) {
            return new TmfModelResponse<>(new TmfVirtualTableModel<>(columnsIds, Collections.emptyList(), queryFilter.getIndex(), 0), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        /*
         * Search for the next or previous event starting from the given event index
         */
        Object directionValue = fetchParameters.get(TABLE_SEARCH_DIRECTION_KEY);

        /////////////////////////////////////
        // TODO remove isFiltered when Theia front-end is updated to use TABLE_SEARCH_DIRECTION_KEY instead
        Boolean isFiltered = DataProviderParameterUtils.extractIsFiltered(fetchParameters);
        boolean isIndexRequest = isFiltered != null && isFiltered;
        if (isIndexRequest && directionValue == null) {
            directionValue = Direction.NEXT.name();
        }
        /////////////////////////////////////

        if (searchFilter != null && directionValue != null) {
            Direction direction = directionValue.equals(Direction.PREVIOUS.name()) ? Direction.PREVIOUS : Direction.NEXT;
            @Nullable WrappedEvent event = null;
            Predicate<@NonNull ITmfEvent> predicate;
            if (filter == null) {
                predicate = searchFilter::matches;
            } else {
                predicate = e -> (filter.matches(e) && searchFilter.matches(e));
            }
            if (direction == Direction.NEXT) {
                event = getNextWrappedEventMatching(getTrace(), Math.abs(queryFilter.getIndex()), predicate, monitor);
            } else if (direction == Direction.PREVIOUS) {
                event = getPreviousWrappedEventMatching(getTrace(), Math.abs(queryFilter.getIndex()), predicate, monitor);
            }
            List<EventTableLine> lines = new ArrayList<>();
            long rank = queryFilter.getIndex();
            if (event != null) {
                rank = event.getRank();
                // create new queryFilter with updated start rank to get number of events starting from first matching event
                queryFilter = new VirtualTableQueryFilter(queryFilter.getColumnsId(), rank, queryFilter.getCount());
                lines.add(buildEventTableLine(aspects, event.getOriginalEvent(), rank, rank, true));
            }
            if ((queryFilter.getCount() == 1) || (event == null)) {
                /**
                 * If no event was found or the number of requested events is one
                 * reply here since all required data for the reply is available.
                 */
                TmfVirtualTableModel<EventTableLine> model = new TmfVirtualTableModel<>(columnsIds, lines, rank, getTrace().getNbEvents());
                return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
            }
        }

        /*
         * Collect queryFilter.getCount() number of events from start rank
         */

        /*
         * TODO implement upper limit of queryFilter.getCount() to avoid running out of memory.
         * TBD if the check and should be handled here or in the calling methods.
         */
        TableEventRequest request;
        if (filter != null) {
            request = filteredTableRequest(Math.abs(queryFilter.getCount()), queryFilter.getIndex(), aspects, filter, searchFilter, collapseFilter, monitor);
        } else {
            request = tableRequest(Math.abs(queryFilter.getCount()), queryFilter.getIndex(), aspects, searchFilter, collapseFilter, monitor);
            request.setEventCount(getTrace().getNbEvents());
        }

        getTrace().sendRequest(request);
        try {
            request.waitForCompletion();
        } catch (InterruptedException e) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, NonNullUtils.nullToEmptyString(e.getMessage()));
        }
        if (request.isCancelled()) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
        }
        TmfVirtualTableModel<EventTableLine> model = new TmfVirtualTableModel<>(columnsIds, request.getEventLines(), queryFilter.getIndex(), request.getCurrentCount());
        return new TmfModelResponse<>(model, ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private long getTableIndex(Map<String, Object> fetchParameters) {
        List<Long> timeRequested = DataProviderParameterUtils.extractTimeRequested(fetchParameters);
        long index = 0;
        if (timeRequested != null && !timeRequested.isEmpty()) {
            ITmfTrace trace = getTrace();
            ITmfContext context = trace.seekEvent(TmfTimestamp.fromNanos(timeRequested.get(0)));
            long rank = context.getRank();
            index = (rank == ITmfContext.UNKNOWN_RANK) ? trace.getNbEvents() - 1 : rank;
            context.dispose();
        }
        return index;
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
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, NonNullUtils.nullToEmptyString(e.getMessage()));
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

    /**
     * Create a table event request that fills a list of {@link EventTableLine}
     * based on the query parameters and based on a filter.
     *
     * @param queryCount
     *            number of requested events
     * @param queryIndex
     *            start rank to start looking for requested events
     * @param aspects
     *            Aspects to resolve
     * @param filter
     *            The filter to apply
     * @param searchFilter
     *            Search filter used to tag event lines
     * @param collapseFilter
     *            The collapse filter to apply
     * @param monitor
     *            a progress monitor
     * @return a {@link TableEventRequest} to fill a list of {@link EventTableLine}
     */
    private TableEventRequest filteredTableRequest(int queryCount,
            long queryIndex,
            Map<Long, ITmfEventAspect<?>> aspects,
            ITmfFilter filter,
            @Nullable ITmfFilter searchFilter,
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
                    boolean matches = searchFilter != null && searchFilter.matches(event);
                    if (events.size() < queryCount && queryIndex <= currentIndex) {
                        events.add(buildEventTableLine(aspects, event, currentIndex, rank, matches));
                    }
                    if (currentIndex % INDEX_STORING_INTERVAL == 0) {
                        fIndexToRankMap.put(currentIndex, rank);
                        fRankToIndexMap.put(rank, currentIndex);
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

                /*
                 *  FIXME: Right now the whole trace is read to determine the number of matched events.
                 *  In case of search filter being present, it only starts counting from the first
                 *  matched event and the total number of filtered event is incorrect.
                 *
                 *  However, the number of matched events should only be done once per filter and
                 *  after the filter changed. Implement way to determine the number of matched events.
                 */

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

    /**
     * Create a table event request that fills a list of {@link EventTableLine}
     * based on the query parameters.
     *
     * @param queryCount
     *            number of requested events
     * @param queryIndex
     *            start rank to start looking for requested events
     * @param aspects
     *            Aspects to resolve
     * @param searchFilter
     *            Search filter used to tag event lines
     * @param collapseFilter
     *            The collapse filter to apply
     * @param monitor
     *            a progress monitor
     * @return a {@link TableEventRequest} to fill a list of {@link EventTableLine}
     */
    private TableEventRequest tableRequest(int queryCount,
            long queryIndex,
            Map<Long, ITmfEventAspect<?>> aspects,
            @Nullable ITmfFilter searchFilter,
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
                boolean matches = searchFilter != null && searchFilter.matches(event);
                if (collapseFilter == null || collapseFilter.matches(event)) {
                    if (events.size() < queryCount) {
                        events.add(buildEventTableLine(aspects, event, rank, rank, matches));
                    }
                } else if (!events.isEmpty()) {
                    // If a collapse filter is present, we need to update the last event we have in
                    // our list to increment the repeat count value.
                    int lastIndex = events.size() - 1;
                    EventTableLine prevLine = events.get(lastIndex);
                    long prevRepeatCount = prevLine.getRepeatCount();
                    events.set(lastIndex, new EventTableLine(prevLine.getCells(), prevLine.getIndex(), prevLine.getTimestamp(), prevLine.getRank(), prevRepeatCount++));
                }

                if ((getNbRead() == queryCount) || events.size() == queryCount) {
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
    private static EventTableLine buildEventTableLine(Map<Long, ITmfEventAspect<?>> aspects, ITmfEvent event, long lineIndex, long lineRank, boolean matches) {
        List<VirtualTableCell> entry = new ArrayList<>(aspects.size());
        for (Entry<Long, ITmfEventAspect<?>> aspectEntry : aspects.entrySet()) {
            Object aspectResolved = aspectEntry.getValue().resolve(event);
            String cellContent = aspectResolved == null ? StringUtils.EMPTY : String.valueOf(aspectResolved);
            entry.add(new VirtualTableCell(cellContent));
        }
        EventTableLine tableLine = new EventTableLine(entry, lineIndex, event.getTimestamp(), lineRank, 0);
        tableLine.setActiveProperties(matches ? CoreFilterProperty.HIGHLIGHT : 0);
        return tableLine;
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
        // TODO verify if this works correctly
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

    @SuppressWarnings("unchecked")
    private static @Nullable ITmfFilter extractSearchFilter(Map<String, Object> fetchParameters) {
        Object searchFilterObject = fetchParameters.get(TABLE_SEARCH_EXPRESSION_KEY);
        if (searchFilterObject instanceof Map<?, ?>) {
            return extractSimpleSearchFilter((Map<?, String>) searchFilterObject);
        }
        return null;
    }

    private static @Nullable ITmfFilter extractSimpleSearchFilter (Map<?, String> searchMap) {
        if (searchMap.isEmpty()) {
            return null;
        }
        TmfFilterRootNode rootFilter = new TmfFilterRootNode();
        for (Entry<?, String> searchEntry : searchMap.entrySet()) {
            TmfFilterMatchesNode searchNode = new TmfFilterMatchesNode(rootFilter);
            Long key = extractColumnId(searchEntry.getKey());
            if (key != null) {
                ITmfEventAspect<?> aspect = fAspectToIdMap.inverse().get(key);
                if (aspect == null) {
                    return null;
                }
                searchNode.setEventAspect(aspect);
                searchNode.setRegex(searchEntry.getValue());
            }
        }
        return rootFilter;
    }

    private static @Nullable Long extractColumnId(@Nullable Object key) {
        try {
            if (key instanceof String) {
                return Long.valueOf((String) key);
            }
            if (key instanceof Long) {
                return (Long) key;
            }
            if (key instanceof Integer) {
                return Long.valueOf((Integer) key);
            }
        } catch (NumberFormatException e) {
            // fall through
        }
        return null;
    }

    private static @Nullable TmfCollapseFilter extractCollapseFilter(Map<?, Object> fetchParameters) {
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


    /**
     * Retrieve from a trace the next event, from a starting rank, matching the
     * given predicate.
     *
     * Code inspired from TmfTraceUtils implementation, however this will return
     * a {@link WrappedEvent} with the correct event rank.
     *
     * @param trace
     *            The trace
     * @param startRank
     *            The rank of the event at which to start searching. Use
     *            <code>0</code> to search from the start of the trace.
     * @param predicate
     *            The predicate to test events against
     * @param monitor
     *            Optional progress monitor that can be used to cancel the operation
     * @return The first {@link WrappedEvent} matching the predicate, or null if the end of the
     *         trace was reached and no event was found
     */
    private static @Nullable WrappedEvent getNextWrappedEventMatching(ITmfTrace trace, long startRank,
            Predicate<ITmfEvent> predicate, @Nullable IProgressMonitor monitor) {
        if (monitor != null && monitor.isCanceled()) {
            return null;
        }

        EventMatchingRequest req = new EventMatchingRequest(startRank, predicate, monitor);
        trace.sendRequest(req);
        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
            return null;
        }

        return req.getFoundEvent();
    }

    /**
     * Retrieve from a trace the previous event, from a given rank, matching the
     * given predicate.
     *
     * Code inspired from TmfTraceUtils implementation, however this will return
     * a {@link WrappedEvent} with the correct event rank.
     *
     * @param trace
     *            The trace
     * @param startRank
     *            The rank of the event at which to start searching backwards.
     * @param predicate
     *            The predicate to test events against
     * @param monitor
     *            Optional progress monitor that can be used to cancel the operation
     * @return The first {@link WrappedEvent} found matching the predicate, or null if the
     *         beginning of the trace was reached and no event was found
     */
    private static @Nullable WrappedEvent getPreviousWrappedEventMatching(ITmfTrace trace, long startRank,
            Predicate<ITmfEvent> predicate, @Nullable IProgressMonitor monitor) {
        if (monitor != null && monitor.isCanceled()) {
            return null;
        }
        /*
         * We are going to do a series of queries matching the trace's cache size in
         * length (which should minimize on-disk seeks), then iterate on the found
         * events in reverse order until we find a match.
         */
        int step = trace.getCacheSize();

        long currentRank = startRank + 1;
        try {
            while (currentRank > 0) {

                currentRank = currentRank - step;
                if (currentRank < 0) {
                    step += currentRank;
                    currentRank = 0;
                }

                List<WrappedEvent> list = new ArrayList<>(step);
                ArrayFillingRequest req = new ArrayFillingRequest(currentRank, step, list, monitor);
                trace.sendRequest(req);

                /* Check periodically if the job was cancelled */
                req.waitForCompletion();

                Optional<WrappedEvent> matchingEvent = Lists.reverse(list).stream()
                        .filter(e -> predicate.test(e.getOriginalEvent()))
                        .findFirst();

                if (matchingEvent.isPresent()) {
                    /* We found an event matching, return it! */
                    return matchingEvent.get();
                }
                /* Keep searching, next loop */

            }
        } catch (InterruptedException e) {
            return null;
        }

        /*
         * We searched up to the beginning of the trace and didn't find anything.
         */
        return null;

    }

    /**
     * Event request looking for an event matching a Predicate.
     */
    private static class EventMatchingRequest extends TmfEventRequest {

        private final Predicate<ITmfEvent> fPredicate;
        private final @Nullable IProgressMonitor fMonitor;

        private @Nullable WrappedEvent fFoundEvent = null;

        /**
         * Basic constructor, will query the trace until the end.
         *
         * @param startRank
         *            The rank at which to start, use 0 for the beginning
         * @param predicate
         *            The predicate to test against each event
         * @param monitor
         *            an optional progress monitor
         */
        public EventMatchingRequest(long startRank, Predicate<ITmfEvent> predicate, @Nullable IProgressMonitor monitor) {
            super(ITmfEvent.class, startRank, ALL_DATA, ExecutionType.FOREGROUND);
            fPredicate = predicate;
            fMonitor = monitor;
        }

        /**
         * Basic constructor, will query the trace the limit is reached.
         *
         * @param startRank
         *            The rank at which to start, use 0 for the beginning
         * @param limit
         *            The limit on the number of events
         * @param predicate
         *            The predicate to test against each event
         * @param monitor
         *            an optional progress monitor
         */
        public EventMatchingRequest(long startRank, int limit, Predicate<ITmfEvent> predicate, @Nullable IProgressMonitor monitor) {
            super(ITmfEvent.class, startRank, limit, ExecutionType.FOREGROUND);
            fPredicate = predicate;
            fMonitor = monitor;
        }

        public @Nullable WrappedEvent getFoundEvent() {
            return fFoundEvent;
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            if (fPredicate.test(event)) {
                fFoundEvent = new WrappedEvent(event, getNbRead() + getIndex() - 1);
                done();
            }
            if (fMonitor != null && fMonitor.isCanceled()) {
                cancel();
            }
        }
    }

    /**
     * Event request that simply puts all returned events into a list passed in
     * parameter.
     */
    private static class ArrayFillingRequest extends TmfEventRequest {

        private final List<WrappedEvent> fList;
        private final @Nullable IProgressMonitor fMonitor;

        public ArrayFillingRequest(long startRank, int limit, List<WrappedEvent> listToFill, @Nullable IProgressMonitor monitor) {
            super(ITmfEvent.class, startRank, limit, ExecutionType.FOREGROUND);
            fList = listToFill;
            fMonitor = monitor;
        }

        @Override
        public void handleData(ITmfEvent event) {
            super.handleData(event);
            fList.add(new WrappedEvent(event, getIndex() + getNbRead() - 1));
            if (fMonitor != null && fMonitor.isCanceled()) {
                cancel();
            }
        }
    }

    /**
     * A TMF Events wrapper class wrapping a ITmfEvent and the corresponding, valid rank.
     */
    private static class WrappedEvent implements ITmfEvent {
        /**
         * Event reference.
         */
        ITmfEvent fEvent;
        /**
         * Events rank.
         */
        long fRank;

        /**
         * Constructor for new cached events.
         *
         * @param tmfEvent
         *            The original trace event
         * @param rank
         *            The rank of this event in the trace
         */
        public WrappedEvent (ITmfEvent tmfEvent, long rank) {
            this.fEvent = tmfEvent;
            this.fRank = rank;
        }

        public ITmfEvent getOriginalEvent() {
            return fEvent;
        }

        @Override
        public @Nullable <T> T getAdapter(@Nullable Class<T> adapterType) {
            return fEvent.getAdapter(adapterType);
        }

        @Override
        public ITmfTrace getTrace() {
            return fEvent.getTrace();
        }

        @Override
        public long getRank() {
            return fRank;
        }

        @Override
        public String getName() {
            return fEvent.getName();
        }

        @Override
        public ITmfTimestamp getTimestamp() {
            return fEvent.getTimestamp();
        }

        @Override
        public @Nullable ITmfEventType getType() {
            return fEvent.getType();
        }

        @Override
        public @Nullable ITmfEventField getContent() {
            return fEvent.getContent();
        }
    }

}