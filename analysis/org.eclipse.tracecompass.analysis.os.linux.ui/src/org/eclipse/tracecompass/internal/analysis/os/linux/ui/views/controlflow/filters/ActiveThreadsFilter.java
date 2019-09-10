/*******************************************************************************
 * Copyright (c) 2016, 2019 EfficiOS Inc., Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.filters;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadStatusDataProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowEntry;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.BaseDataProviderTimeGraphView;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;

/**
 * Provide active threads filtering for the Control Flow view.
 *
 * The Active Thread filter can be used in two modes: Show threads running on a
 * range of CPUs or show all threads considered active
 *
 * @author Jonathan Rajotte Julien
 *
 */
public class ActiveThreadsFilter extends ViewerFilter {

    /** The filtering CPU ranges */
    private final @NonNull List<Range<Long>> fCpuRanges;
    /** The local cache for On CPU filtering */
    private @NonNull Map<ITmfTrace, Set<Long>> fCachedOnCpusThreadForTimeRange = new HashMap<>();
    /** The local cache for Active Threads filtering */
    private @NonNull Map<ITmfTrace, Set<Long>> fCachedActiveThreadForTimeRange = new HashMap<>();
    /** The cached time range */
    private TmfTimeRange fCachedTimeRange;
    /** Whether the filter is enabled */
    private boolean fEnabled = false;
    /** Whether the filter is an On CPU filter */
    private boolean fCpuRangesBasedFiltering = false;
    /** The Trace */
    private final @Nullable ITmfTrace fTrace;

    /**
     * Create an Active Threads filter with CPU ranges criteria.
     *
     * @param cpuRanges
     *            The CPU ranges for the filter if any.
     * @param cpuRangesBasedFiltering
     *            Whether or not to filter based on CPU ranges.
     * @param trace
     *            The trace the filter is for
     */
    public ActiveThreadsFilter(List<Range<Long>> cpuRanges, boolean cpuRangesBasedFiltering, @Nullable ITmfTrace trace) {
        super();
        if (cpuRanges != null) {
            fCpuRanges = ImmutableList.copyOf(cpuRanges);
        } else {
            fCpuRanges = Collections.emptyList();
        }
        fCpuRangesBasedFiltering = cpuRangesBasedFiltering;

        fTrace = trace;
    }

    /**
     * @return If the filter is enabled
     */
    public boolean isEnabled() {
        return fEnabled;
    }

    /**
     * @return If the filter is based on CPU ranges filtering
     */
    public boolean isCpuRangesBased() {
        return fCpuRangesBasedFiltering;
    }

    /**
     * Set the enabled state of the filter
     *
     * @param enabled
     *            The state of the filter
     */
    public void setEnabled(boolean enabled) {
        fEnabled = enabled;
    }

    /**
     * Get the CPU ranges of the filter
     *
     * @return The CPU ranges of the filter
     */
    public @NonNull List<Range<Long>> getCpuRanges() {
        return fCpuRanges;
    }

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {

        if (!fEnabled || !(element instanceof ControlFlowEntry)) {
            return true;
        }
        ControlFlowEntry cfe = (ControlFlowEntry) element;

        ITmfTrace trace = BaseDataProviderTimeGraphView.getTrace(cfe);

        Set<Long> onCpusThreadForTimeRange = fCachedOnCpusThreadForTimeRange.get(trace);
        Set<Long> activeThreadForTimeRange = fCachedActiveThreadForTimeRange.get(trace);

        /* Check if on CPU */
        if (fCpuRangesBasedFiltering && (onCpusThreadForTimeRange != null) && onCpusThreadForTimeRange.contains(cfe.getEntryModel().getId())) {
            return true;
        } else if ((activeThreadForTimeRange != null) && activeThreadForTimeRange.contains(cfe.getEntryModel().getId())) {
            return true;
        }

        /* Not active per see. Check children if any is active */
        for (TimeGraphEntry child : cfe.getChildren()) {
            if (select(viewer, cfe, child)) {
                return true;
            }
        }

        /* No children are active */
        return false;
    }

    private static @NonNull Set<Long> getOnCpuThreads(@NonNull List<Range<Long>> cpuRanges, TmfTimeRange winRange, @NonNull ITmfTrace trace) {

        ThreadStatusDataProvider threadStatusProvider = DataProviderManager.getInstance().getDataProvider(trace,
                ThreadStatusDataProvider.ID, ThreadStatusDataProvider.class);

        if (threadStatusProvider == null) {
            return Collections.emptySet();
        }

        long beginTS = winRange.getStartTime().getValue();
        long endTS = winRange.getEndTime().getValue();

        @NonNull Set<@NonNull Long> cpus = new HashSet<>();
        for (Range<Long> range : cpuRanges) {
            for (long cpu = range.lowerEndpoint(); cpu <= range.upperEndpoint(); cpu ++) {
                cpus.add(cpu);
            }
        }
        SelectionTimeQueryFilter filter = new SelectionTimeQueryFilter(beginTS, endTS, 2, cpus);
        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(filter);
        parameters.put(ThreadStatusDataProvider.ACTIVE_THREAD_FILTER_KEY, true);
        TmfModelResponse<TmfTreeModel<@NonNull ThreadEntryModel>> response = threadStatusProvider.fetchTree(parameters, null);
        TmfTreeModel<@NonNull ThreadEntryModel> model = response.getModel();

        if (model == null) {
            return Collections.emptySet();
        }
        HashSet<Long> onCpuThreads = Sets.newHashSet(Iterables.transform(model.getEntries(), ThreadEntryModel::getId));
        return onCpuThreads == null ? Collections.emptySet() : onCpuThreads;
    }

    private static @NonNull Set<Long> getActiveThreads(TmfTimeRange winRange, @NonNull ITmfTrace trace) {

        ThreadStatusDataProvider threadStatusProvider = DataProviderManager.getInstance().getDataProvider(trace,
                ThreadStatusDataProvider.ID, ThreadStatusDataProvider.class);

        if (threadStatusProvider == null) {
            return Collections.emptySet();
        }

        long beginTS = winRange.getStartTime().getValue();
        long endTS = winRange.getEndTime().getValue();

        TimeQueryFilter filter = new TimeQueryFilter(beginTS, endTS, 2);
        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        parameters.put(ThreadStatusDataProvider.ACTIVE_THREAD_FILTER_KEY, true);
        TmfModelResponse<TmfTreeModel<@NonNull ThreadEntryModel>> response = threadStatusProvider.fetchTree(parameters, null);
        TmfTreeModel<@NonNull ThreadEntryModel> model = response.getModel();

        if (model == null) {
            return Collections.emptySet();
        }
        HashSet<Long> activeThreads = Sets.newHashSet(Iterables.transform(model.getEntries(), ThreadEntryModel::getId));
        return activeThreads == null ? Collections.emptySet() : activeThreads;
    }

    /**
     * Compute the filter internal data
     *
     * @param beginTS
     *            start timestamp to update the filter for
     * @param endTS
     *            end timestamp to update the filter for
     * @return map of filter data per trace, or null
     */
    public @Nullable Map<ITmfTrace, Set<Long>> computeData(long beginTS, long endTS) {
        TmfTimeRange timeRange = new TmfTimeRange(TmfTimestamp.fromNanos(beginTS), TmfTimestamp.fromNanos(endTS));

        ITmfTrace parentTrace = fTrace;
        if (parentTrace == null || !fEnabled || (fCachedTimeRange != null && fCachedTimeRange.equals(timeRange))) {
            return null;
        }
        Map<ITmfTrace, Set<Long>> data = new HashMap<>();
        for (ITmfTrace trace : TmfTraceManager.getTraceSet(parentTrace)) {
            if (fCpuRangesBasedFiltering) {
                Set<Long> onCpusThreadForTimeRange = getOnCpuThreads(fCpuRanges, timeRange, trace);
                data.put(trace, onCpusThreadForTimeRange);
            } else {
                Set<Long> activeThreadForTimeRange = getActiveThreads(timeRange, trace);
                data.put(trace, activeThreadForTimeRange);
            }
        }
        return data;
    }

    /**
     * Update the filter internal data
     *
     * @param beginTS
     *            start timestamp to update the filter for
     * @param endTS
     *            end timestamp to update the filter for
     * @param data
     *            map of filter data per trace
     */
    public void updateData(long beginTS, long endTS, @NonNull Map<ITmfTrace, Set<Long>> data) {
        /* Caching result for subsequent select() call for other entry */
        fCachedTimeRange = new TmfTimeRange(TmfTimestamp.fromNanos(beginTS), TmfTimestamp.fromNanos(endTS));
        if (fCpuRangesBasedFiltering) {
            fCachedOnCpusThreadForTimeRange = data;
        } else {
            fCachedActiveThreadForTimeRange = data;
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(fCpuRanges, fCpuRangesBasedFiltering, fEnabled);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        ActiveThreadsFilter other = (ActiveThreadsFilter) obj;
        return Objects.equals(fCpuRanges, other.fCpuRanges) &&
                fCpuRangesBasedFiltering == other.fCpuRangesBasedFiltering &&
                fEnabled == other.fEnabled;
    }
}
