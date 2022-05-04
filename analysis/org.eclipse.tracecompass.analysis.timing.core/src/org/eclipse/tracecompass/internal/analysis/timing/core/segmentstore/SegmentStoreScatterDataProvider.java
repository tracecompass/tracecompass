/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IGroupingSegmentAspect;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.analysis.timing.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfXyResponseFactory;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.CoreFilterProperty;
import org.eclipse.tracecompass.tmf.core.model.SeriesModel;
import org.eclipse.tracecompass.tmf.core.model.SeriesModel.SeriesModelBuilder;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel.DisplayType;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse.Status;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.primitives.Doubles;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;

/**
 * This data provider will return a XY model (model wrapped in a response) based
 * on a query filter. The model can be used afterwards by any viewer to draw
 * charts. Model returned is for analysis using SegmentStore
 *
 * @author Yonni Chen
 * @since 3.1
 */
public class SegmentStoreScatterDataProvider extends AbstractTmfTraceDataProvider implements ITmfTreeXYDataProvider<TmfTreeDataModel> {

    /**
     * Extension point ID.
     *
     * @since 4.0
     */
    public static final String ID = "org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.scatter.dataprovider"; //$NON-NLS-1$

    private static final String DEFAULT_CATEGORY = "default"; //$NON-NLS-1$
    private static final String GROUP_PREFIX = "group"; //$NON-NLS-1$
    private static final AtomicLong ENTRY_ID = new AtomicLong();

    private final ISegmentStoreProvider fProvider;
    private final String fId;

    private final BiMap<Long, String> fIdToType = HashBiMap.create();
    private final long fTraceId = ENTRY_ID.getAndIncrement();

    private Iterable<IGroupingSegmentAspect> fGroupingAspects;

    private static class CheckSegmentType implements Predicate<ISegment> {

        private final Set<String> fSelectedTypes;
        private final String fPrefix;

        public CheckSegmentType(String prefix, Set<String> selectedTypes) {
            fSelectedTypes = selectedTypes;
            fPrefix = prefix;
        }

        @Override
        public boolean test(ISegment segment) {
            if (!(segment instanceof INamedSegment)) {
                return fSelectedTypes.contains(fPrefix + DEFAULT_CATEGORY);
            }
            return fSelectedTypes.contains(fPrefix + ((INamedSegment) segment).getName());
        }
    }

    /**
     * An iterator over a segment store that returns segments from a segment
     * store only if they do not overlap
     */
    private static class SegmentStoreIterator implements Iterator<@NonNull ISegment> {

        /* Last segment per category */
        private final Map<String, ISegment> fLasts = new HashMap<>();
        private @Nullable ISegment fNext = null;
        private final Iterator<@NonNull ISegment> fIterator;
        private final long fStartTime;
        private final long fPixelSize;

        public SegmentStoreIterator(long startTime, Iterable<@NonNull ISegment> iterableToCompact, long pixelSize) {
            fStartTime = startTime;
            fIterator = Objects.requireNonNull(iterableToCompact.iterator());
            fPixelSize = Math.max(1, pixelSize);
        }

        @Override
        public @NonNull ISegment next() {
            /* hasNext implies next != null */
            if (hasNext()) {
                ISegment segment = Objects.requireNonNull(fNext);
                fLasts.put(getSegmentName(segment), segment);
                fNext = null;
                return segment;
            }
            throw new NoSuchElementException();
        }

        @Override
        public boolean hasNext() {
            if (fLasts.isEmpty() && fNext == null) {
                // iteration hasn't started yet.
                if (fIterator.hasNext()) {
                    ISegment segment = fIterator.next();
                    if (segment.getStart() >= fStartTime) {
                        fNext = segment;
                    }
                } else {
                    return false;
                }
            }

            // clear warning in calling overlaps below.
            while (fNext == null && fIterator.hasNext()) {
                ISegment tmp = fIterator.next();
                ISegment last = fLasts.get(getSegmentName(tmp));
                if (tmp.getStart() >= fStartTime && !overlaps(last, tmp)) {
                    fNext = tmp;
                }
            }
            return fNext != null;
        }

        /*
         * Returns whether two segments overlaps or not by comparing start/end
         * of last and start/end of next.
         */
        private boolean overlaps(@Nullable ISegment last, ISegment next) {
            if (last == null) {
                return false;
            }
            long timePerPix = fPixelSize;
            final long start = last.getStart();
            final long pixelStart = fStartTime;
            final long pixelDuration = start - pixelStart;
            long startPixBoundL = pixelDuration / timePerPix * timePerPix + pixelStart;
            long startPixBoundR = startPixBoundL + timePerPix;
            final long currentStart = next.getStart();
            if (currentStart >= startPixBoundL && currentStart <= startPixBoundR) {
                long length = last.getLength();
                long lengthNext = next.getLength();
                long lengthLow = length / timePerPix * timePerPix;
                long lengthHigh = lengthLow + timePerPix;
                return (lengthNext >= lengthLow && lengthNext <= lengthHigh);
            }
            return false;
        }
    }

    /**
     * Create an instance of {@link SegmentStoreScatterDataProvider} for a given
     * analysis ID. Returns a null instance if the ISegmentStoreProvider is
     * null. If the provider is an instance of {@link IAnalysisModule}, analysis
     * is also scheduled.
     * <p>
     * If the trace has multiple analysis modules with the same secondary ID,
     * <code>null</code> is returned so the caller can try to make a
     * {@link TmfTreeXYCompositeDataProvider} for all the traces instead
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @param secondaryId
     *            The ID of the analysis to use for this provider
     * @return An instance of SegmentStoreDataProvider. Returns a null if the
     *         ISegmentStoreProvider is null.
     * @since 4.0
     */
    public static @Nullable ITmfTreeDataProvider<? extends ITmfTreeDataModel> create(ITmfTrace trace, String secondaryId) {
        // The trace can be an experiment, so we need to know if there are
        // multiple analysis modules with the same ID
        Iterable<ISegmentStoreProvider> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, ISegmentStoreProvider.class);
        Iterable<ISegmentStoreProvider> filteredModules = Iterables.filter(modules, m -> ((IAnalysisModule) m).getId().equals(secondaryId));
        Iterator<ISegmentStoreProvider> iterator = filteredModules.iterator();
        if (iterator.hasNext()) {
            ISegmentStoreProvider module = iterator.next();
            if (iterator.hasNext()) {
                // More than one module, must be an experiment, return null so
                // the factory can try with individual traces
                return null;
            }
            ((IAnalysisModule) module).schedule();
            return new SegmentStoreScatterDataProvider(trace, module, secondaryId);
        }
        return null;
    }

    /**
     * Constructor
     */
    private SegmentStoreScatterDataProvider(ITmfTrace trace, ISegmentStoreProvider provider, String secondaryId) {
        super(trace);
        fProvider = provider;
        fGroupingAspects = Iterables.filter(provider.getSegmentAspects(), IGroupingSegmentAspect.class);
        fId = ID + ':' + secondaryId;
    }

    /**
     * @since 4.0
     */
    @Override
    public TmfModelResponse<TmfTreeModel<TmfTreeDataModel>> fetchTree(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        ISegmentStoreProvider provider = fProvider;
        if (provider instanceof IAnalysisModule) {
            IAnalysisModule module = (IAnalysisModule) provider;
            IProgressMonitor mon = monitor != null ? monitor : new NullProgressMonitor();
            module.waitForCompletion(mon);
            if (mon.isCanceled()) {
                return new TmfModelResponse<>(null, Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
        }
        ISegmentStore<ISegment> segStore = provider.getSegmentStore();

        if (segStore == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }
        TimeQueryFilter filter = FetchParametersUtils.createTimeQuery(fetchParameters);
        if (filter == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
        }
        long start = filter.getStart();
        long end = filter.getEnd();
        final Iterable<ISegment> intersectingElements = Iterables.filter(segStore.getIntersectingElements(start, end), s -> s.getStart() >= start);

        Map<String, INamedSegment> segmentTypes = new HashMap<>();
        IAnalysisModule module = (provider instanceof IAnalysisModule) ? (IAnalysisModule) provider : null;
        boolean complete = module == null ? true : module.isQueryable(filter.getEnd());

        // Create the list of segment types that will each create a series
        for (INamedSegment segment : Iterables.filter(intersectingElements, INamedSegment.class)) {
            if (monitor != null && monitor.isCanceled()) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
            segmentTypes.put(segment.getName(), segment);
        }

        Builder<TmfTreeDataModel> nodes = new ImmutableList.Builder<>();
        nodes.add(new TmfTreeDataModel(fTraceId, -1, Collections.singletonList(String.valueOf(getTrace().getName()))));
        Map<IGroupingSegmentAspect, Map<String, Long>> names = new HashMap<>();

        for (Entry<String, INamedSegment> series : segmentTypes.entrySet()) {
            long parentId = fTraceId;
            /*
             * Create a tree sorting aspects by "Grouping aspect" much like
             * counter analyses
             */
            for (IGroupingSegmentAspect aspect : fGroupingAspects) {
                names.putIfAbsent(aspect, new HashMap<>());
                Map<String, Long> map = names.get(aspect);
                if (map == null) {
                    break;
                }
                String name = String.valueOf(aspect.resolve(series.getValue()));
                String key = GROUP_PREFIX + name;
                Long uniqueId = map.get(key);
                if (uniqueId == null) {
                    uniqueId = getUniqueId(key);
                    map.put(key, uniqueId);
                    nodes.add(new TmfTreeDataModel(uniqueId, parentId, name));
                }
                parentId = uniqueId;
            }
            long seriesId = getUniqueId(series.getKey());
            nodes.add(new TmfTreeDataModel(seriesId, parentId, series.getKey()));
        }

        return new TmfModelResponse<>(new TmfTreeModel<>(Collections.emptyList(), nodes.build()), complete ? ITmfResponse.Status.COMPLETED : ITmfResponse.Status.RUNNING,
                complete ? CommonStatusMessage.COMPLETED : CommonStatusMessage.RUNNING);
    }

    @Override
    public TmfModelResponse<ITmfXyModel> fetchXY(Map<String, Object> fetchParameters, @Nullable IProgressMonitor monitor) {
        ISegmentStoreProvider provider = fProvider;

        // FIXME: There is no way to get the running status of a segment store
        // analysis, so we need to wait for completion before going forward, to
        // be sure the segment store is available.
        if ((provider instanceof IAnalysisModule) && !((IAnalysisModule) provider).waitForCompletion()) {
            return TmfXyResponseFactory.createFailedResponse(CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        final ISegmentStore<ISegment> segStore = provider.getSegmentStore();
        if (segStore == null) {
            return TmfXyResponseFactory.createFailedResponse(Objects.requireNonNull(Messages.SegmentStoreDataProvider_SegmentNotAvailable));
        }

        // TODO server: Parameters validation should be handle separately. It
        // can be either in the data provider itself or before calling it. It
        // will avoid the creation of filters and the content of the map can be
        // use directly.
        TimeQueryFilter filter = FetchParametersUtils.createSelectionTimeQuery(fetchParameters);
        if (filter == null) {
            filter = FetchParametersUtils.createTimeQuery(fetchParameters);
            if (filter == null) {
                return TmfXyResponseFactory.createFailedResponse(CommonStatusMessage.INCORRECT_QUERY_PARAMETERS);
            }
        }

        Map<@NonNull Integer, @NonNull Predicate<@NonNull Multimap<@NonNull String, @NonNull Object>>> predicates = new HashMap<>();
        Multimap<@NonNull Integer, @NonNull String> regexesMap = DataProviderParameterUtils.extractRegexFilter(fetchParameters);
        if (regexesMap != null) {
            predicates.putAll(computeRegexPredicate(regexesMap));
        }

        long start = filter.getStart();
        long end = filter.getEnd();
        // The types in the tree do not contain the trace name for sake of
        // readability, but the name of the series in XY model should be unique
        // per trace
        String prefix = getTrace().getName() + '/';
        Map<String, Series> types = initTypes(prefix, filter);
        if (types.isEmpty()) {
            // this would return an empty map even if we did the queries.
            return TmfXyResponseFactory.create(Objects.requireNonNull(Messages.SegmentStoreScatterGraphViewer_title), Collections.emptyList(), true);
        }
        long pixelSize = Math.max(1, (end - start) / filter.getTimesRequested().length);
        final Iterable<ISegment> intersectingElements = Iterables.filter(segStore.getIntersectingElements(start, end, SegmentComparators.INTERVAL_START_COMPARATOR), segment -> {
            CheckSegmentType cs = new CheckSegmentType(prefix, types.keySet());
            return cs.test(segment);
        });
        final Iterable<ISegment> displayData = compactList(start, intersectingElements, pixelSize);

        IAnalysisModule module = (fProvider instanceof IAnalysisModule) ? (IAnalysisModule) fProvider : null;
        boolean complete = module == null ? true : module.isQueryable(filter.getEnd());

        // For each visible segments, add start time to x value and duration for
        // y value
        for (ISegment segment : displayData) {
            if (monitor != null && monitor.isCanceled()) {
                return TmfXyResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
            }

            String name = prefix + getSegmentName(segment);
            Series thisSeries = types.get(name);
            if (thisSeries == null) {
                // This shouldn't be, log an error and continue
                Activator.getInstance().logError("Series " + thisSeries + " should exist"); //$NON-NLS-1$//$NON-NLS-2$
                continue;
            }

            addPoint(thisSeries, segment, predicates, monitor);
        }

        List<ISeriesModel> seriesModelMap = new ArrayList<>();
        for (Entry<String, Series> entry : types.entrySet()) {
            SeriesModel seriesModel = entry.getValue().build();
            seriesModelMap.add(seriesModel);
        }
        return TmfXyResponseFactory.create(Objects.requireNonNull(Messages.SegmentStoreScatterGraphViewer_title),
                seriesModelMap, complete);
    }

    private static String getSegmentName(ISegment segment) {
        return (segment instanceof INamedSegment) ? ((INamedSegment) segment).getName() : DEFAULT_CATEGORY;
    }

    /**
     * Filter the time graph state and add it to the state list
     *
     * @param stateList
     *            The timegraph state list
     * @param timeGraphState
     *            The current timegraph state
     * @param key
     *            The timegraph entry model id
     * @param predicates
     *            The predicates used to filter the timegraph state. It is a map
     *            of predicate by property. The value of the property is an
     *            integer representing a bitmask associated to that property.
     *            The status of each property will be set for the timegraph
     *            state according to the associated predicate test result.
     * @param monitor
     *            The progress monitor
     */
    private void addPoint(Series series, ISegment segment, Map<Integer, Predicate<Multimap<String, Object>>> predicates, @Nullable IProgressMonitor monitor) {

        if (!predicates.isEmpty()) {

            // Get the filter external input data
            Multimap<@NonNull String, @NonNull Object> input = ISegmentStoreProvider.getFilterInput(fProvider, segment);

            // Test each predicates and set the status of the property
            // associated to the predicate
            int mask = 0;
            for (Map.Entry<Integer, Predicate<Multimap<String, Object>>> mapEntry : predicates.entrySet()) {
                Predicate<Multimap<String, Object>> value = Objects.requireNonNull(mapEntry.getValue());
                boolean status = value.test(input);
                Integer property = Objects.requireNonNull(mapEntry.getKey());
                if (status && property != CoreFilterProperty.DIMMED) {
                    mask |= property;
                } else if (!status && property == CoreFilterProperty.DIMMED) {
                    mask |= CoreFilterProperty.DIMMED;
                } else if (!status && property == CoreFilterProperty.EXCLUDE) {
                    mask |= CoreFilterProperty.EXCLUDE;
                }
            }
            series.addPoint(segment.getStart(), segment.getLength(), mask);
        } else {
            series.addPoint(segment.getStart(), segment.getLength(), 0);
        }
    }

    private static class Series {
        private final long fId;
        private final List<Long> fXValues = new ArrayList<>();
        private final List<Double> fYValues = new ArrayList<>();
        private final List<Integer> fProperties = new ArrayList<>();

        public Series(long id) {
            fId = id;
        }

        public void addPoint(long x, double y, int properties) {
            fXValues.add(x);
            fYValues.add(y);
            fProperties.add(properties);
        }

        public SeriesModel build() {
            SeriesModelBuilder builder = new SeriesModel.SeriesModelBuilder(getId(), String.valueOf(getId()), Longs.toArray(fXValues), Doubles.toArray(fYValues));
            builder.seriesDisplayType(DisplayType.SCATTER);
            builder.setProperties(Ints.toArray(fProperties)).build();
            return builder.setProperties(Ints.toArray(fProperties)).build();
        }

        private long getId() {
            return fId;
        }

    }

    private Map<String, Series> initTypes(String prefix, TimeQueryFilter filter) {
        if (!(filter instanceof SelectionTimeQueryFilter)) {
            return Collections.emptyMap();
        }

        Map<String, Series> segmentTypes = new HashMap<>();
        synchronized (fIdToType) {
            for (Long id : ((SelectionTimeQueryFilter) filter).getSelectedItems()) {
                String string = fIdToType.get(id);
                if (string == null) {
                    continue;
                }

                String name = prefix + string;
                segmentTypes.put(name, new Series(id));
            }
        }
        return segmentTypes;
    }

    private long getUniqueId(String name) {
        synchronized (fIdToType) {
            return fIdToType.inverse().computeIfAbsent(name, n -> ENTRY_ID.getAndIncrement());
        }
    }

    private static Iterable<ISegment> compactList(final long startTime, final Iterable<@NonNull ISegment> iterableToCompact, long pixelSize) {
        return () -> new SegmentStoreIterator(startTime, iterableToCompact, pixelSize);
    }

    /**
     * @since 4.0
     */
    @Override
    public String getId() {
        return fId;
    }

    @Override
    public void dispose() {
        synchronized (fIdToType) {
            fIdToType.clear();
        }
    }

}
