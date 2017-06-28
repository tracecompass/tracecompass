/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.segmentstore;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.Messages;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.AbstractTmfTraceDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.CommonStatusMessage;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.TmfCommonXAxisResponseFactory;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.tmf.core.model.YModel;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableMap;
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
@SuppressWarnings("restriction")
public class SegmentStoreScatterDataProvider extends AbstractTmfTraceDataProvider implements ITmfXYDataProvider {

    private final ISegmentStoreProvider fProvider;

    /**
     * We check that this field is different to 0 in {@link #fetchXY} method to
     * avoid dividing by 0. TODO : should this be a double?
     */
    private long fPixelSize = -1;

    private class SegmentStoreIterator implements Iterator<@NonNull ISegment> {

        private @Nullable ISegment fLast = null;
        private @Nullable ISegment fNext = null;
        private final Iterator<@NonNull ISegment> fIterator;
        private final long fStartTime;

        public SegmentStoreIterator(long startTime, Iterable<@NonNull ISegment> iterableToCompact) {
            fStartTime = startTime;
            fIterator = Objects.requireNonNull(iterableToCompact.iterator());
        }

        @Override
        public @NonNull ISegment next() {
            /* hasNext implies next != null */
            if (hasNext()) {
                fLast = fNext;
                fNext = null;
                return Objects.requireNonNull(fLast);
            }
            throw new NoSuchElementException();
        }

        @Override
        public boolean hasNext() {
            if (fLast == null) {
                // iteration hasn't started yet.
                if (fIterator.hasNext()) {
                    fLast = fIterator.next();
                    if (fLast.getStart() >= fStartTime) {
                        fNext = fLast;
                    }
                } else {
                    return false;
                }
            }

            // clear warning in calling overlaps below.
            ISegment prev = fLast;
            while (fNext == null && fIterator.hasNext()) {
                ISegment tmp = fIterator.next();
                if (tmp.getStart() >= fStartTime && !overlaps(prev, tmp)) {
                    fNext = tmp;
                }
            }
            return fNext != null;
        }

        /*
         * Returns whether two segments overlaps or not by comparing start/end of last
         * and start/end of next.
         */
        private boolean overlaps(ISegment last, ISegment next) {
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
     */
    public static @Nullable SegmentStoreScatterDataProvider create(ITmfTrace trace, @Nullable ISegmentStoreProvider provider) {
        if (provider == null) {
            return null;
        }

        if (provider instanceof IAnalysisModule) {
            ((IAnalysisModule) provider).schedule();
            return new SegmentStoreScatterDataProvider(trace, provider);
        }
        return null;
    }

    /**
     * Constructor
     */
    private SegmentStoreScatterDataProvider(ITmfTrace trace, ISegmentStoreProvider provider) {
        super(trace);
        fProvider = provider;
    }

    @Override
    public ITmfCommonXAxisResponse fetchXY(TimeQueryFilter filter, @Nullable IProgressMonitor monitor) {

        if (!(fProvider instanceof IAnalysisModule)) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(Messages.SegmentStoreDataProvider_SegmentMustBeAnIAnalysisModule);
        }

        if (!(((IAnalysisModule) fProvider).waitForCompletion())) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(CommonStatusMessage.ANALYSIS_INITIALIZATION_FAILED);
        }

        final ISegmentStore<ISegment> segStore = fProvider.getSegmentStore();
        if (segStore == null) {
            return TmfCommonXAxisResponseFactory.createFailedResponse(Messages.SegmentStoreDataProvider_SegmentNotAvailable);
        }

        long start = filter.getStart();
        long end = filter.getEnd();
        fPixelSize = Math.max(1, (end - start) / filter.getTimesRequested().length);
        final Iterable<ISegment> intersectingElements = segStore.getIntersectingElements(start, end, SegmentComparators.INTERVAL_START_COMPARATOR);
        final Iterable<ISegment> displayData = compactList(start, intersectingElements);

        List<Long> xSeries = new ArrayList<>();
        List<Double> yValues = new ArrayList<>();

        // For each visible segments, add start time to x value and duration for y value
        for (ISegment segment : displayData) {
            if (monitor != null && monitor.isCanceled()) {
                return TmfCommonXAxisResponseFactory.createCancelledResponse(CommonStatusMessage.TASK_CANCELLED);
            }
            xSeries.add(segment.getStart());
            yValues.add((double) segment.getLength());
        }

        IYModel yModel = new YModel(Objects.requireNonNull(Messages.SegmentStoreDataProvider_Duration), Objects.requireNonNull(Doubles.toArray(yValues)));
        Map<String, IYModel> ySeries = ImmutableMap.of(Objects.requireNonNull(Messages.SegmentStoreDataProvider_Duration), yModel);
        return TmfCommonXAxisResponseFactory.create(Objects.requireNonNull(Messages.SegmentStoreScatterGraphViewer_title), Objects.requireNonNull(Longs.toArray(xSeries)), ySeries, filter.getEnd(), true);
    }

    private Iterable<ISegment> compactList(final long startTime, final Iterable<@NonNull ISegment> iterableToCompact) {
        return () -> new SegmentStoreIterator(startTime, iterableToCompact);
    }
}
