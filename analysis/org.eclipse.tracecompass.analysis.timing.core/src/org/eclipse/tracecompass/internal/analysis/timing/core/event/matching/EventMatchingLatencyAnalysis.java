/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.event.matching;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisEventBasedModule;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory.SegmentStoreType;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.matching.IEventMatchingKey;
import org.eclipse.tracecompass.tmf.core.event.matching.IMatchProcessingUnit;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventDependency;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;

/**
 * This class analyzes the latencies when an event matching dependency is found.
 * It can be used to see how much latency there is in the trace before or after
 * trace synchronization.
 *
 * @author Geneviève Bastien
 */
public class EventMatchingLatencyAnalysis extends AbstractSegmentStoreAnalysisEventBasedModule {

    /**
     * ID of this analysis
     */
    public static final String ID = "org.eclipse.tracecompass.internal.analysis.timing.core.event.matching"; //$NON-NLS-1$

    private static final Collection<ISegmentAspect> BASE_ASPECTS =
            ImmutableList.of(EventMatchingTypeAspect.INSTANCE);

    private static class EventMatchingLatencyProcessing implements IMatchProcessingUnit {

        private final ISegmentStore<ISegment> fSegmentStore;
        private int fCount = 0;

        public EventMatchingLatencyProcessing(ISegmentStore<ISegment> segmentStore) {
            fSegmentStore = segmentStore;
        }

        @Override
        public void init(Collection<ITmfTrace> fTraces) {
            // Nothing to do
        }

        @Override
        public void addMatch(TmfEventDependency match) {
            if (match.getSource().getTrace().getHostId().equals(match.getDestination().getTrace().getHostId())) {
                return;
            }
            EventMatchingLatency segment = new EventMatchingLatency(null, match);
            fSegmentStore.add(segment);
        }

        @Override
        public void addMatch(@NonNull IEventMatchingKey eventKey, @NonNull TmfEventDependency match) {
            if (match.getSource().getTrace().getHostId().equals(match.getDestination().getTrace().getHostId())) {
                return;
            }
            fCount++;
            EventMatchingLatency segment = new EventMatchingLatency(eventKey, match);
            fSegmentStore.add(segment);
        }

        @Override
        public void matchingEnded() {
            // Nothing to do
        }

        @Override
        public int countMatches() {
            return fCount;
        }

    }

    @Override
    protected AbstractSegmentStoreAnalysisRequest createAnalysisRequest(ISegmentStore<ISegment> segmentStore, IProgressMonitor monitor) {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            throw new NullPointerException("The trace should not be null"); //$NON-NLS-1$
        }
        TmfEventMatching matching = new TmfEventMatching(Collections.singleton(trace), new EventMatchingLatencyProcessing(segmentStore));
        matching.initMatching();
        return new LatencyMatchingEventRequest(segmentStore, matching, monitor);
    }

    @Override
    protected @NonNull IHTIntervalReader<@NonNull ISegment> getSegmentReader() {
        return EventMatchingLatency.MATCHING_LATENCY_READ_FACTORY;
    }

    @Override
    protected @NonNull SegmentStoreType getSegmentStoreType() {
        return SegmentStoreType.OnDisk;
    }

    private class LatencyMatchingEventRequest extends AbstractSegmentStoreAnalysisRequest {

        private final TmfEventMatching fMatching;
        private final IProgressMonitor fMonitor;

        public LatencyMatchingEventRequest(ISegmentStore<ISegment> segmentStore, TmfEventMatching matching, IProgressMonitor monitor) {
            super(segmentStore);
            fMatching = matching;
            fMonitor = monitor;
        }

        @Override
        public void handleData(final ITmfEvent event) {
            super.handleData(event);
            fMatching.matchEvent(event, event.getTrace(), fMonitor);
        }
    }

    @Override
    public Iterable<ISegmentAspect> getSegmentAspects() {
        return BASE_ASPECTS;
    }

    private static final class EventMatchingTypeAspect implements ISegmentAspect {
        public static final ISegmentAspect INSTANCE = new EventMatchingTypeAspect();

        private EventMatchingTypeAspect() { }

        @Override
        public String getHelpText() {
            return checkNotNull("The type of match that made this latency"); //$NON-NLS-1$
        }
        @Override
        public String getName() {
            return checkNotNull("Type"); //$NON-NLS-1$
        }
        @Override
        public @Nullable Comparator<?> getComparator() {
            return null;
        }
        @Override
        public @Nullable String resolve(ISegment segment) {
            if (segment instanceof EventMatchingLatency) {
                return ((EventMatchingLatency) segment).getName();
            }
            return EMPTY_STRING;
        }
    }

}
