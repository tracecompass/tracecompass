/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntry.IdGetter;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output.DataDrivenOutputEntry.QuarkCallback;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.util.Pair;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Multimap;
import com.google.common.collect.Table;

/**
 * This data provider will return time graph models (wrapped in a response)
 * based on a query filter. The models can be used afterwards by any viewer to
 * draw time graphs. Model returned is for XML analysis.
 *
 * @author Loic Prieur-Drevon
 * @author Geneviève Bastien
 */
public class DataDrivenTimeGraphDataProvider extends AbstractTmfTraceDataProvider implements ITimeGraphDataProvider<TimeGraphEntryModel> {

    /**
     * Provider unique ID.
     */
    public static final String ID = "org.eclipse.tracecompass.tmf.analysis.xml.core.output.DataDrivenTimeGraphDataProvider"; //$NON-NLS-1$
    private static final AtomicLong sfAtomicId = new AtomicLong();

    private final List<ITmfStateSystem> fSs;
    private final List<DataDrivenOutputEntry> fEntries;
    private final List<DataDrivenPresentationState> fValues;

    /**
     * Remember the unique mappings of state system and quark to entry ID.
     */
    private final Table<ITmfStateSystem, Integer, Long> fBaseQuarkToId = HashBasedTable.create();
    private final Map<Long, Pair<ITmfStateSystem, Integer>> fIDToDisplayQuark = new HashMap<>();

    private final IdGetter fIdGenerator = (ss, quark) -> fBaseQuarkToId.row(ss).computeIfAbsent(quark, s -> sfAtomicId.getAndIncrement());
    private final QuarkCallback fQuarkCallback = (id, ss, quark, dt) -> fIDToDisplayQuark.put(id, new Pair<>(ss, quark));
    private final String fId;

    /**
     * Constructor
     *
     * @param trace
     *            The trace this data provider is for
     * @param stateSystems
     *            The list of state systems to build it for
     * @param entries
     *            The entries
     * @param values
     *            The presentation values
     * @param id
     *            The ID of the data provider
     */
    public DataDrivenTimeGraphDataProvider(ITmfTrace trace, List<ITmfStateSystem> stateSystems, List<DataDrivenOutputEntry> entries, List<DataDrivenPresentationState> values, @Nullable String id) {
        super(trace);
        fSs = stateSystems;
        fEntries = entries;
        fValues = values;
        fId = (id == null) ? ID : id;
    }

    @Override
    public final TmfModelResponse<TmfTreeModel<@NonNull TimeGraphEntryModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        List<TimeGraphEntryModel> entryList = new ArrayList<>();
        boolean isComplete = true;

        String traceName = String.valueOf(getTrace().getName());
        for (ITmfStateSystem ss : fSs) {
            isComplete &= ss.waitUntilBuilt(0);
            /* Don't query empty state system */
            if (ss.getNbAttributes() > 0 && ss.getStartTime() != Long.MIN_VALUE) {
                long start = ss.getStartTime();
                long end = ss.getCurrentEndTime();
                long id = fBaseQuarkToId.row(ss).computeIfAbsent(ITmfStateSystem.ROOT_ATTRIBUTE, s -> sfAtomicId.getAndIncrement());
                TimeGraphEntryModel ssEntry = new TimeGraphEntryModel(id, -1, traceName, start, end);
                entryList.add(ssEntry);

                for (DataDrivenOutputEntry entry : fEntries) {
                    entryList.addAll(entry.buildEntries(ss, ssEntry.getId(), getTrace(), -1, StringUtils.EMPTY, end, fIdGenerator, fQuarkCallback));
                }
            }
        }
        Status status = isComplete ? Status.COMPLETED : Status.RUNNING;
        String msg = isComplete ? CommonStatusMessage.COMPLETED : CommonStatusMessage.RUNNING;
        TmfTreeModel<@NonNull TimeGraphEntryModel> tree = new TmfTreeModel<>(Collections.emptyList(), entryList);
        return new TmfModelResponse<>(tree, status, msg);
    }

    @Override
    public @NonNull String getId() {
        return fId;
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull TimeGraphModel> fetchRowModel(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        // TODO server: Parameters validation should be handle separately. It
        // can be either in the data provider itself or before calling it. It
        // will avoid the creation of filters and the content of the map can be
        // use directly.
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter == null) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        Table<ITmfStateSystem, Integer, Long> table = HashBasedTable.create();
        for (Long id : filter.getSelectedItems()) {
            Pair<ITmfStateSystem, Integer> pair = fIDToDisplayQuark.get(id);
            if (pair != null) {
                table.put(pair.getFirst(), pair.getSecond(), id);
            }
        }
        List<@NonNull ITimeGraphRowModel> allRows = new ArrayList<>();
        try {
            for (Entry<ITmfStateSystem, Map<Integer, Long>> ssEntry : table.rowMap().entrySet()) {
                Collection<@NonNull ITimeGraphRowModel> rows = createRows(ssEntry.getKey(), ssEntry.getValue(), fetchParameters, monitor);
                allRows.addAll(rows);
            }
        } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.STATE_SYSTEM_FAILED);
        }
        return new TmfModelResponse<>(new TimeGraphModel(allRows), Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private Collection<ITimeGraphRowModel> createRows(ITmfStateSystem ss, Map<Integer, Long> idToDisplayQuark,
            Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) throws StateSystemDisposedException {
        Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull String>>> predicates = new HashMap<>();
        Multimap<@NonNull Integer, @NonNull String> regexesMap = DataProviderParameterUtils.extractRegexFilter(fetchParameters);
        if (regexesMap != null) {
            predicates.putAll(computeRegexPredicate(regexesMap));
        }

        long currentEndTime = ss.getCurrentEndTime();
        Map<Integer, ITimeGraphRowModel> quarkToRow = new HashMap<>(idToDisplayQuark.size());
        for (Entry<Integer, Long> entry : idToDisplayQuark.entrySet()) {
            quarkToRow.put(entry.getKey(), new TimeGraphRowModel(entry.getValue(), new ArrayList<>()));
        }
        List<Long> timesRequested = DataProviderParameterUtils.extractTimeRequested(fetchParameters);
        for (ITmfStateInterval interval : ss.query2D(idToDisplayQuark.keySet(), getTimes(ss, timesRequested))) {
            if (monitor != null && monitor.isCanceled()) {
                return Collections.emptyList();
            }
            ITimeGraphRowModel row = quarkToRow.get(interval.getAttribute());
            if (row != null) {
                List<@NonNull ITimeGraphState> states = row.getStates();
                ITimeGraphState timeGraphState = getStateFromInterval(interval, currentEndTime);
                applyFilterAndAddState(states, timeGraphState, row.getEntryID(), predicates, monitor);
            }
        }
        for (ITimeGraphRowModel model : quarkToRow.values()) {
            model.getStates().sort(Comparator.comparingLong(ITimeGraphState::getStartTime));
        }
        return quarkToRow.values();
    }

    private static TimeGraphState getStateFromInterval(ITmfStateInterval statusInterval, long currentEndTime) {
        long time = statusInterval.getStartTime();
        long duration = Math.min(currentEndTime, statusInterval.getEndTime() + 1) - time;
        Object o = statusInterval.getValue();
        if (o instanceof Integer) {
            return new TimeGraphState(time, duration, ((Integer) o).intValue(), String.valueOf(o));
        } else if (o instanceof Long) {
            long l = (long) o;
            return new TimeGraphState(time, duration, (int) l, "0x" + Long.toHexString(l)); //$NON-NLS-1$
        } else if (o instanceof String) {
            return new TimeGraphState(time, duration, Integer.MIN_VALUE, (String) o);
        } else if (o instanceof Double) {
            return new TimeGraphState(time, duration, ((Double) o).intValue());
        }
        return new TimeGraphState(time, duration, Integer.MIN_VALUE);
    }

    private static Set<Long> getTimes(ITmfStateSystem key, List<Long> timesRequested) {
        Set<@NonNull Long> times = new HashSet<>();
        for (long t : timesRequested) {
            if (key.getStartTime() <= t && t <= key.getCurrentEndTime()) {
                times.add(t);
            }
        }
        return times;
    }

    @Override
    public @NonNull TmfModelResponse<List<ITimeGraphArrow>> fetchArrows(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public @NonNull TmfModelResponse<Map<String, String>> fetchTooltip(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Deprecated
    @Override
    public TmfModelResponse<List<TimeGraphEntryModel>> fetchTree(@NonNull TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        @NonNull Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        TmfModelResponse<TmfTreeModel<@NonNull TimeGraphEntryModel>> response = fetchTree(parameters, monitor);
        TmfTreeModel<@NonNull TimeGraphEntryModel> model = response.getModel();
        List<TimeGraphEntryModel> treeModel = null;
        if (model != null) {
            treeModel = model.getEntries();
        }
        return new TmfModelResponse<>(treeModel, response.getStatus(), response.getStatusMessage());
    }

    @Deprecated
    @Override
    public @NonNull TmfModelResponse<@NonNull List<@NonNull ITimeGraphRowModel>> fetchRowModel(@NonNull SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        @NonNull Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(filter);
        TmfModelResponse<@NonNull TimeGraphModel> response = fetchRowModel(parameters, monitor);
        TimeGraphModel model = response.getModel();
        List<@NonNull ITimeGraphRowModel> rows = null;
        if (model != null) {
            rows = model.getRows();
        }
        return new TmfModelResponse<>(rows, response.getStatus(), response.getStatusMessage());
    }

    @Deprecated
    @Override
    public @NonNull TmfModelResponse<@NonNull List<@NonNull ITimeGraphArrow>> fetchArrows(@NonNull TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Map<String, Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        return fetchArrows(parameters, monitor);
    }

    @Deprecated
    @Override
    public @NonNull TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> fetchTooltip(@NonNull SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Map<String, Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(filter);
        return fetchTooltip(parameters, monitor);
    }

    /**
     * Get the values
     *
     * TODO: Remove when support of presentation from data provider is added
     *
     * @return The values
     */
    protected List<DataDrivenPresentationState> getValues() {
        return fValues;
    }

}
