/**********************************************************************
 * Copyright (c) 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.dataprovider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathModule;
import org.eclipse.tracecompass.internal.analysis.graph.core.base.CriticalPathPalette;
import org.eclipse.tracecompass.internal.analysis.graph.core.base.TmfGraphStatistics;
import org.eclipse.tracecompass.internal.analysis.graph.core.base.TmfGraphVisitor;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.IOutputStyleProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.OutputStyleModel;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphStateFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphArrow;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphRowModel;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphState;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

/**
 * Data Provider for the Critical Path.
 *
 * @author Loic Prieur-Drevon
 */
public class CriticalPathDataProvider extends AbstractTmfTraceDataProvider implements ITimeGraphDataProvider<@NonNull CriticalPathEntry>, IOutputStyleProvider {

    /**
     * Extension point ID for the provider
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.analysis.graph.core.dataprovider.CriticalPathDataProvider"; //$NON-NLS-1$
    private static final @NonNull String ARROW_SUFFIX = "arrow"; //$NON-NLS-1$
    /**
     * Atomic long to assign each entry the same unique ID every time the data
     * provider is queried
     */
    private static final AtomicLong ATOMIC_LONG = new AtomicLong();

    private static final @NonNull Map<@NonNull String, @NonNull OutputElementStyle> STATE_MAP;

    private static final @NonNull Map<@NonNull String, @NonNull OutputElementStyle> STYLE_MAP = Collections.synchronizedMap(new HashMap<>());

    static {
        ImmutableMap.Builder<@NonNull String, @NonNull OutputElementStyle> builder = new ImmutableMap.Builder<>();
        builder.putAll(CriticalPathPalette.getStyles());

        // Add the arrow types
        builder.put(EdgeType.DEFAULT.name() + ARROW_SUFFIX, new OutputElementStyle(EdgeType.UNKNOWN.name(), ImmutableMap.of(StyleProperties.STYLE_NAME, String.valueOf(Messages.CriticalPathDataProvider_UnknownArrow), StyleProperties.STYLE_GROUP, String.valueOf(Messages.CriticalPathDataProvider_GroupArrows))));
        builder.put(EdgeType.NETWORK.name() + ARROW_SUFFIX, new OutputElementStyle(EdgeType.NETWORK.name(), ImmutableMap.of(StyleProperties.STYLE_NAME, String.valueOf(Messages.CriticalPathDataProvider_NetworkArrow), StyleProperties.STYLE_GROUP, String.valueOf(Messages.CriticalPathDataProvider_GroupArrows))));
        STATE_MAP = builder.build();
    }

    private @NonNull CriticalPathModule fCriticalPathModule;

    /**
     * Remember the unique mappings from hosts to their entry IDs
     */
    private final Map<String, Long> fHostIdToEntryId = new HashMap<>();
    /**
     * Remember the unique mappings from workers to their entry IDs
     */
    private final BiMap<IGraphWorker, Long> fWorkerToEntryId = HashBiMap.create();

    private final LoadingCache<IGraphWorker, CriticalPathVisitor> fHorizontalVisitorCache = CacheBuilder.newBuilder()
            .maximumSize(10).build(new CacheLoader<IGraphWorker, CriticalPathVisitor>() {

                @Override
                public CriticalPathVisitor load(IGraphWorker key) throws Exception {
                    TmfGraph criticalPath = fCriticalPathModule.getCriticalPath();
                    return new CriticalPathVisitor(criticalPath, key);
                }
            });

    /**
     * FIXME when switching between traces, the current worker is set to null, do
     * this to remember the last arrows used.
     */
    private List<@NonNull ITimeGraphArrow> fLinks;

    /** Cache for entry metadata */
    private final Map<Long, @NonNull Multimap<@NonNull String, @NonNull Object>> fEntryMetadata = new HashMap<>();

    /**
     * Constructor
     *
     * @param trace
     *            The trace for which we will be providing the time graph models
     * @param criticalPathProvider
     *            the critical path module for this trace
     */
    public CriticalPathDataProvider(@NonNull ITmfTrace trace, @NonNull CriticalPathModule criticalPathProvider) {
        super(trace);
        fCriticalPathModule = criticalPathProvider;
    }

    @Override
    public synchronized @NonNull TmfModelResponse<@NonNull TmfTreeModel<@NonNull CriticalPathEntry>> fetchTree(
            Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        TmfGraph graph = fCriticalPathModule.getCriticalPath();
        if (graph == null) {
            return new TmfModelResponse<>(null, Status.RUNNING, CommonStatusMessage.RUNNING);
        }

        IGraphWorker current = getCurrent();
        if (current == null) {
            return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        CriticalPathVisitor visitor = fHorizontalVisitorCache.getUnchecked(current);
        for (CriticalPathEntry model : visitor.getEntries()) {
            fEntryMetadata.put(model.getId(), model.getMetadata());
        }
        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), visitor.getEntries()), Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    /**
     * Get the current {@link IGraphWorker} from the {@link CriticalPathModule}
     *
     * @return the current graph worker if it is set, else null.
     */
    private @Nullable IGraphWorker getCurrent() {
        Object obj = fCriticalPathModule.getParameter(CriticalPathModule.PARAM_WORKER);
        if (obj == null) {
            return null;
        }
        if (!(obj instanceof IGraphWorker)) {
            throw new IllegalStateException("Wrong type for critical path module parameter " + //$NON-NLS-1$
                    CriticalPathModule.PARAM_WORKER +
                    " expected IGraphWorker got " + //$NON-NLS-1$
                    obj.getClass().getSimpleName());
        }
        return (IGraphWorker) obj;
    }

    @Override
    public @NonNull String getId() {
        return ID;
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull TimeGraphModel> fetchRowModel(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        IGraphWorker graphWorker = getCurrent();
        if (graphWorker == null) {
            return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        CriticalPathVisitor visitor = fHorizontalVisitorCache.getIfPresent(graphWorker);
        if (visitor == null) {
            return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }

        // TODO server: Parameters validation should be handle separately. It
        // can be either in the data provider itself or before calling it. It
        // will avoid the creation of filters and the content of the map can be
        // use directly.
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter == null) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates = new HashMap<>();
        Multimap<@NonNull Integer, @NonNull String> regexesMap = DataProviderParameterUtils.extractRegexFilter(fetchParameters);
        if (regexesMap != null) {
            predicates.putAll(computeRegexPredicate(regexesMap));
        }

        List<@NonNull ITimeGraphRowModel> rowModels = new ArrayList<>();
        for (Long id : filter.getSelectedItems()) {
            /*
             * need to use asMap, so that we don't return a row for an ID that does not
             * belong to this provider, else fStates.get(id) might return an empty
             * collection for an id from another data provider.
             */
            Collection<ITimeGraphState> states = visitor.fStates.asMap().get(id);
            if (states != null) {
                List<ITimeGraphState> filteredStates = new ArrayList<>();
                for (ITimeGraphState state : states) {
                    if (overlaps(state.getStartTime(), state.getDuration(), filter.getTimesRequested())) {
                        // Reset the properties for this state before filtering
                        state.setActiveProperties(0);
                        applyFilterAndAddState(filteredStates, state, id, predicates, monitor);
                    }
                }
                rowModels.add(new TimeGraphRowModel(id, filteredStates));
            }
        }
        return new TmfModelResponse<>(new TimeGraphModel(rowModels), Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private static final boolean overlaps(long start, long duration, long[] times) {
        int pos = Arrays.binarySearch(times, start);
        if (pos >= 0) {
            // start is one of the times
            return true;
        }
        int ins = -pos - 1;
        if (ins >= times.length) {
            // start is larger than the last time
            return false;
        }
        /*
         * the first queried time which is larger than the state start time, is also
         * smaller than the state end time. I.e. at least one queried time is in the
         * state range
         */
        return times[ins] <= start + duration;
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull List<@NonNull ITimeGraphArrow>> fetchArrows(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        // TODO server: Parameters validation should be handle separately. It
        // can be either in the data provider itself or before calling it. It
        // will avoid the creation of filters and the content of the map can be
        // use directly.
        TimeQueryFilter filter = FetchParametersUtils.createTimeQuery(fetchParameters);
        if (filter == null) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        return new TmfModelResponse<>(getLinkList(filter.getStart(), filter.getEnd()), Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> fetchTooltip(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        SelectionTimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter == null) {
            return new TmfModelResponse<>(null, Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        IGraphWorker worker = fWorkerToEntryId.inverse().get(filter.getSelectedItems().iterator().next());
        if (worker == null) {
            return new TmfModelResponse<>(null, Status.COMPLETED, CommonStatusMessage.COMPLETED);
        }
        Map<@NonNull String, @NonNull String> info = worker.getWorkerInformation(filter.getStart());
        return new TmfModelResponse<>(info, Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

    private final class CriticalPathVisitor extends TmfGraphVisitor {
        private final TmfGraph fGraph;
        /**
         * The {@link IGraphWorker} for which the view (tree / states) are computed
         */
        private final Map<String, CriticalPathEntry> fHostEntries = new HashMap<>();
        private final Map<IGraphWorker, CriticalPathEntry> fWorkers = new LinkedHashMap<>();
        private final TmfGraphStatistics fStatistics = new TmfGraphStatistics();

        /**
         * Store the states in a {@link TreeMultimap} so that they are grouped by entry
         * and sorted by start time.
         */
        private final TreeMultimap<Long, ITimeGraphState> fStates = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparingLong(ITimeGraphState::getStartTime));
        private long fStart;
        private long fEnd;
        private List<@NonNull CriticalPathEntry> fCached;
        /**
         * Cache the links once the graph has been traversed.
         */
        private @Nullable List<@NonNull ITimeGraphArrow> fGraphLinks;

        private CriticalPathVisitor(TmfGraph graph, IGraphWorker worker) {
            fGraph = graph;
            fStart = getTrace().getStartTime().toNanos();
            fEnd = getTrace().getEndTime().toNanos();

            TmfVertex head = graph.getHead();
            if (head != null) {
                fStart = Long.min(fStart, head.getTs());
                for (IGraphWorker w : graph.getWorkers()) {
                    TmfVertex tail = graph.getTail(w);
                    if (tail != null) {
                        fEnd = Long.max(fEnd, tail.getTs());
                    }
                }
            }
            fStatistics.computeGraphStatistics(graph, worker);
        }

        @Override
        public void visitHead(TmfVertex node) {
            IGraphWorker owner = fGraph.getParentOf(node);
            if (owner == null) {
                return;
            }
            if (fWorkers.containsKey(owner)) {
                return;
            }
            TmfVertex first = fGraph.getHead(owner);
            TmfVertex last = fGraph.getTail(owner);
            if (first == null || last == null) {
                return;
            }
            fStart = Long.min(getTrace().getStartTime().toNanos(), first.getTs());
            fEnd = Long.max(getTrace().getEndTime().toNanos(), last.getTs());
            Long sum = fStatistics.getSum(owner);
            Double percent = fStatistics.getPercent(owner);

            // create host entry
            String host = owner.getHostId();
            long parentId = fHostIdToEntryId.computeIfAbsent(host, h -> ATOMIC_LONG.getAndIncrement());
            fHostEntries.computeIfAbsent(host, h -> new CriticalPathEntry(parentId, -1L, Collections.singletonList(host), fStart, fEnd, sum, percent));

            long entryId = fWorkerToEntryId.computeIfAbsent(owner, w -> ATOMIC_LONG.getAndIncrement());
            CriticalPathEntry entry = new CriticalPathEntry(entryId, parentId, owner, fStart, fEnd, sum, percent);

            fWorkers.put(owner, entry);
        }

        @Override
        public void visit(TmfEdge link, boolean horizontal) {
            if (horizontal) {
                IGraphWorker parent = fGraph.getParentOf(link.getVertexFrom());
                Long id = fWorkerToEntryId.get(parent);
                if (id != null) {
                    String linkQualifier = link.getLinkQualifier();
                    ITimeGraphState ev = new TimeGraphState(link.getVertexFrom().getTs(), link.getDuration(), linkQualifier, getMatchingState(link.getType(), false));
                    fStates.put(id, ev);
                }
            } else {
                IGraphWorker parentFrom = fGraph.getParentOf(link.getVertexFrom());
                IGraphWorker parentTo = fGraph.getParentOf(link.getVertexTo());
                CriticalPathEntry entryFrom = fWorkers.get(parentFrom);
                CriticalPathEntry entryTo = fWorkers.get(parentTo);
                List<ITimeGraphArrow> graphLinks = fGraphLinks;
                if (graphLinks != null && entryFrom != null && entryTo != null) {
                    ITimeGraphArrow lk = new TimeGraphArrow(entryFrom.getId(), entryTo.getId(), link.getVertexFrom().getTs(),
                            link.getVertexTo().getTs() - link.getVertexFrom().getTs(), getMatchingState(link.getType(), true));
                    graphLinks.add(lk);
                }
            }
        }

        public @NonNull List<@NonNull CriticalPathEntry> getEntries() {
            if (fCached != null) {
                return fCached;
            }

            fGraph.scanLineTraverse(fGraph.getHead(), this);
            List<@NonNull CriticalPathEntry> list = new ArrayList<>(fHostEntries.values());
            list.addAll(fWorkers.values());
            fCached = list;
            return list;
        }

        public synchronized List<@NonNull ITimeGraphArrow> getGraphLinks() {
            if (fGraphLinks == null) {
                // the graph has not been traversed yet
                fGraphLinks = new ArrayList<>();
                fGraph.scanLineTraverse(fGraph.getHead(), this);
            }
            return fGraphLinks;
        }
    }

    /**
     * Critical path typically has relatively few links, so we calculate and save
     * them all, but just return those in range
     */
    private @Nullable List<@NonNull ITimeGraphArrow> getLinkList(long startTime, long endTime) {
        IGraphWorker current = getCurrent();
        List<@NonNull ITimeGraphArrow> graphLinks = fLinks;
        if (current == null) {
            if (graphLinks != null) {
                return graphLinks;
            }
            return Collections.emptyList();
        }
        CriticalPathVisitor visitor = fHorizontalVisitorCache.getIfPresent(current);
        if (visitor == null) {
            return Collections.emptyList();
        }
        graphLinks = visitor.getGraphLinks();
        fLinks = graphLinks;
        return getLinksInRange(graphLinks, startTime, endTime);
    }

    private static List<@NonNull ITimeGraphArrow> getLinksInRange(List<ITimeGraphArrow> allLinks, long startTime, long endTime) {
        List<@NonNull ITimeGraphArrow> linksInRange = new ArrayList<>();
        for (ITimeGraphArrow link : allLinks) {
            if (link.getStartTime() <= endTime &&
                    link.getStartTime() + link.getDuration() >= startTime) {
                linksInRange.add(link);
            }
        }
        return linksInRange;
    }

    private static @NonNull OutputElementStyle getMatchingState(EdgeType type, boolean arrow) {
        String parentStyleName = type.name();
        parentStyleName = STATE_MAP.containsKey(parentStyleName) ? parentStyleName : EdgeType.UNKNOWN.name();
        parentStyleName = arrow ? parentStyleName + ARROW_SUFFIX : parentStyleName;
        return STYLE_MAP.computeIfAbsent(type.name(), style -> new OutputElementStyle(style));
    }

    @Deprecated
    @Override
    public TmfModelResponse<List<CriticalPathEntry>> fetchTree(@NonNull TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        Map<String, Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        TmfModelResponse<@NonNull TmfTreeModel<@NonNull CriticalPathEntry>> response = fetchTree(parameters, monitor);
        TmfTreeModel<@NonNull CriticalPathEntry> model = response.getModel();
        List<CriticalPathEntry> treeModel = null;
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
        @NonNull Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.timeQueryToMap(filter);
        return fetchArrows(parameters, monitor);
    }

    @Deprecated
    @Override
    public @NonNull TmfModelResponse<@NonNull Map<@NonNull String, @NonNull String>> fetchTooltip(@NonNull SelectionTimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        @NonNull Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.selectionTimeQueryToMap(filter);
        return fetchTooltip(parameters, monitor);
    }

    @Override
    public @NonNull Multimap<@NonNull String, @NonNull Object> getFilterData(long entryId, long time, @Nullable IProgressMonitor monitor) {
        return ITimeGraphStateFilter.mergeMultimaps(ITimeGraphDataProvider.super.getFilterData(entryId, time, monitor),
                fEntryMetadata.getOrDefault(entryId, ImmutableMultimap.of()));
    }

    @Override
    public @NonNull TmfModelResponse<@NonNull OutputStyleModel> fetchStyle(@NonNull Map<@NonNull String, @NonNull Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        return new TmfModelResponse<>(new OutputStyleModel(STATE_MAP), ITmfResponse.Status.COMPLETED, CommonStatusMessage.COMPLETED);
    }

}
