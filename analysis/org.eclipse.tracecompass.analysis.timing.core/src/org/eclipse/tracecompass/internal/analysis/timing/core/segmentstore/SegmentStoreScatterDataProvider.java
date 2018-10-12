/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.analysis.timing.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.TmfXyResponseFactory;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.tmf.core.model.SeriesModel;
import org.eclipse.tracecompass.tmf.core.model.filters.SelectionTimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.tree.TmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.primitives.Doubles;
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
     * @since 4.0
     */
    public static final String ID = "org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.scatter.dataprovider"; //$NON-NLS-1$

    private static final Map<ISegmentStoreProvider, SegmentStoreScatterDataProvider> PROVIDER_MAP = new WeakHashMap<>();
    private static final String DEFAULT_CATEGORY = "default"; //$NON-NLS-1$
    private static final AtomicLong ENTRY_ID = new AtomicLong();

    private final ISegmentStoreProvider fProvider;
    private final String fId;

    private final BiMap<Long, String> fIdToType = HashBiMap.create();
    private final long fTraceId = ENTRY_ID.getAndIncrement();

    private static class CheckSegmentType implements Predicate<ISegment> {

        private final Set<String> fSelectedTypes;
        private final String fPrefix;

        public CheckSegmentType(String prefix, Set<String> selectedTypes) {
            fSelectedTypes = selectedTypes;
            fPrefix = prefix;
        }

        @Override
        public boolean apply(ISegment segment) {
            if (!(segment instanceof INamedSegment)) {
                return fSelectedTypes.contains(fPrefix + DEFAULT_CATEGORY);
            }
            return fSelectedTypes.contains(fPrefix + ((INamedSegment) segment).getName());
        }
    }

    /**
     * An iterator over a segment store that returns segments from a segment store
     * only if they do not overlap
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
         * Returns whether two segments overlaps or not by comparing start/end of last
         * and start/end of next.
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
     * Create an instance of {@link SegmentStoreScatterDataProvider}. Returns a null
     * instance if the ISegmentStoreProvider is null. If the provider is an instance
     * of {@link IAnalysisModule}, analysis is also scheduled.
     *
     * @param trace
     *            A trace on which we are interested to fetch a model
     * @param provider
     *            A segment store provider.
     * @return An instance of SegmentStoreDataProvider. Returns a null if the
     *         ISegmentStoreProvider is null.
     * @deprecated Use the
     *             {@link SegmentStoreScatterDataProvider#create(ITmfTrace, String)}
     *             method instead
     */
    @Deprecated
    public static synchronized @Nullable SegmentStoreScatterDataProvider create(ITmfTrace trace, @Nullable ISegmentStoreProvider provider) {
        if (provider == null) {
            return null;
        }

        return PROVIDER_MAP.computeIfAbsent(provider, p -> {
            if (p instanceof IAnalysisModule) {
                ((IAnalysisModule) p).schedule();
            }
            return new SegmentStoreScatterDataProvider(trace, p, ""); //$NON-NLS-1$
        });

    }

    /**
     * Create an instance of {@link SegmentStoreScatterDataProvider} for a given
     * analysis ID. Returns a null instance if the ISegmentStoreProvider is null. If
     * the provider is an instance of {@link IAnalysisModule}, analysis is also
     * scheduled.
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
        // The trace can be an experiment, so we need to know if there are multiple analysis modules with the same ID
        Iterable<ISegmentStoreProvider> modules = TmfTraceUtils.getAnalysisModulesOfClass(trace, ISegmentStoreProvider.class);
        Iterable<ISegmentStoreProvider> filteredModules = Iterables.filter(modules, m -> ((IAnalysisModule) m).getId().equals(secondaryId));
        Iterator<ISegmentStoreProvider> iterator = filteredModules.iterator();
        if (iterator.hasNext()) {
            ISegmentStoreProvider module = iterator.next();
            if (iterator.hasNext()) {
                // More than one module, must be an experiment, return null so the factory can try with individual traces
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
        fId = ID + ':' + secondaryId;
    }

    /**
     * @since 4.0
     */
    @Override
    public TmfModelResponse<List<TmfTreeDataModel>> fetchTree(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        ISegmentStoreProvider provider = fProvider;
        ISegmentStore<ISegment> segStore = provider.getSegmentStore();

        if (segStore == null) {
            return new TmfModelResponse<>(null, ITmfResponse.Status.FAILED, CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }
        long start = filter.getStart();
        long end = filter.getEnd();
        // The dot is drawn at segment start, so we filter only the segment that start
        // within the range
        final Predicate<ISegment> startInRangePredication = s -> s.getStart() >= start;
        final Iterable<ISegment> intersectingElements = Iterables.filter(segStore.getIntersectingElements(start, end), startInRangePredication);

        Set<String> segmentTypes = new HashSet<>();
        IAnalysisModule module = (provider instanceof IAnalysisModule) ? (IAnalysisModule) provider : null;
        boolean complete = module == null ? true : module.isQueryable(filter.getEnd());

        // Create the list of segment types that will each create a series
        for (INamedSegment segment : Iterables.filter(intersectingElements, INamedSegment.class)) {
            if (monitor != null && monitor.isCanceled()) {
                return new TmfModelResponse<>(null, ITmfResponse.Status.CANCELLED, CommonStatusMessage.TASK_CANCELLED);
            }
            segmentTypes.add(segment.getName());
        }

        Builder<TmfTreeDataModel> nodes = new ImmutableList.Builder<>();
        nodes.add(new TmfTreeDataModel(fTraceId, -1, String.valueOf(getTrace().getName())));

        // There are segments, but no type, probably not named segments, so just add a category
        if (segmentTypes.isEmpty() && intersectingElements.iterator().hasNext()) {
            long seriesId = getUniqueId(DEFAULT_CATEGORY);
            nodes.add(new TmfTreeDataModel(seriesId, fTraceId, DEFAULT_CATEGORY));
        }
        for (String seriesName : segmentTypes) {
            long seriesId = getUniqueId(seriesName);
            nodes.add(new TmfTreeDataModel(seriesId, fTraceId, seriesName));
        }

        return new TmfModelResponse<>(nodes.build(), complete ? ITmfResponse.Status.COMPLETED : ITmfResponse.Status.RUNNING,
                complete ? CommonStatusMessage.COMPLETED: CommonStatusMessage.RUNNING);
    }

    @Override
    public TmfModelResponse<ITmfXyModel> fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {
        ISegmentStoreProvider provider = fProvider;

        // FIXME: There is no way to get the running status of a segment store analysis,
        // so we need to wait for completion before going forward, to be sure the
        // segment store is available.
        if ((provider instanceof IAnalysisModule) && !((IAnalysisModule) provider).waitForCompletion()) {
            return TmfXyResponseFactory.createFailedResponse(CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        final ISegmentStore<ISegment> segStore = provider.getSegmentStore();
        if (segStore == null) {
            return TmfXyResponseFactory.createFailedResponse(Objects.requireNonNull(Messages.SegmentStoreDataProvider_SegmentNotAvailable));
        }

        long start = filter.getStart();
        long end = filter.getEnd();
        // The types in the tree do not contain the trace name for sake of readability, but
        // the name of the series in XY model should be unique per trace
        String prefix = getTrace().getName() + '/';
        Map<String, Series> types = initTypes(prefix, filter);
        if (types.isEmpty()) {
            // this would return an empty map even if we did the queries.
            return TmfXyResponseFactory.create(Objects.requireNonNull(Messages.SegmentStoreScatterGraphViewer_title), Collections.emptyMap(), true);
        }
        Predicate<ISegment> predicate = new CheckSegmentType(prefix, types.keySet());
        long pixelSize = Math.max(1, (end - start) / filter.getTimesRequested().length);
        final Iterable<ISegment> intersectingElements = Iterables.filter(segStore.getIntersectingElements(start, end, SegmentComparators.INTERVAL_START_COMPARATOR), predicate);
        final Iterable<ISegment> displayData = compactList(start, intersectingElements, pixelSize);

        IAnalysisModule module = (fProvider instanceof IAnalysisModule) ? (IAnalysisModule) fProvider : null;
        boolean complete = module == null ? true : module.isQueryable(filter.getEnd());

        // For each visible segments, add start time to x value and duration for y value
        for (ISegment segment : displayData) {
            if (monitor != null && monitor.isCanceled()) {
                return TmfXyResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
            }

            String name = prefix + getSegmentName(segment);
            Series thisSeries = types.get(name);
            if (thisSeries == null) {
                // This shouldn't be, log an error and continue
                Activator.getInstance().logError("Series " + thisSeries + " should exist");  //$NON-NLS-1$//$NON-NLS-2$
                continue;
            }
            thisSeries.addPoint(segment.getStart(), segment.getLength());
        }

        return TmfXyResponseFactory.create(Objects.requireNonNull(Messages.SegmentStoreScatterGraphViewer_title),
                Maps.transformValues(types, Series::build), complete);
    }

    private static String getSegmentName(ISegment segment) {
        return (segment instanceof INamedSegment) ? ((INamedSegment) segment).getName() : DEFAULT_CATEGORY;
    }

    private static class Series {
        private final long fId;
        private final String fName;
        private final List<Long> fXValues = new ArrayList<>();
        private final List<Double> fYValues = new ArrayList<>();

        public Series(long id, String name) {
            fId = id;
            fName = name;
        }

        public void addPoint(long x, double y) {
            fXValues.add(x);
            fYValues.add(y);
        }

        public SeriesModel build() {
            return new SeriesModel(fId, fName, Longs.toArray(fXValues), Doubles.toArray(fYValues));
        }
    }

    private Map<String, Series> initTypes(String prefix, TimeQueryFilter filter) {
        if (!(filter instanceof SelectionTimeQueryFilter)) {
            return Collections.emptyMap();
        }

        Map<String, Series> segmentTypes = new HashMap<>();
        for (Long id : ((SelectionTimeQueryFilter) filter).getSelectedItems()) {
            String string = fIdToType.get(id);
            if (string == null) {
                continue;
            }

            String name = prefix + string;
            segmentTypes.put(name, new Series(id, name));
        }
        return segmentTypes;
    }

    private long getUniqueId(String name) {
        return fIdToType.inverse().computeIfAbsent(name, n -> ENTRY_ID.getAndIncrement());
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

}
