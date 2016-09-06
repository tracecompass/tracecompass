/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore.statistics;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/**
 * Test stuf for statistics analysis
 *
 * @author Matthew Khouzam
 *
 */
class StubSegmentStatisticsAnalysis extends AbstractSegmentStatisticsAnalysis {

    private final class StubProvider extends TestAnalysis implements ISegmentStoreProvider {
        private final ISegmentStore<@NonNull ISegment> ifFixture;

        public StubProvider(ISegmentStore<@NonNull ISegment> fixture) {
            ifFixture = fixture;
        }

        @Override
        public void removeListener(@NonNull IAnalysisProgressListener listener) {
            // nothing
        }

        @Override
        public @Nullable ISegmentStore<@NonNull ISegment> getSegmentStore() {
            return ifFixture;
        }

        @Override
        public @NonNull Iterable<@NonNull ISegmentAspect> getSegmentAspects() {
            return Collections.emptyList();
        }

        @Override
        public void addListener(@NonNull IAnalysisProgressListener listener) {
            // nothing
        }
    }

    public static final int SIZE = 65535;

    private final List<@NonNull ISegment> fPreFixture;
    private final ISegmentStore<@NonNull ISegment> fFixture = SegmentStoreFactory.createSegmentStore();
    private StubProvider fSegmentStoreProvider;

    public StubSegmentStatisticsAnalysis() {
        ImmutableList.Builder<@NonNull ISegment> builder = new Builder<>();
        for (int i = 0; i < SIZE; i++) {
            ISegment seg = new BasicSegment(i, i + i);
            builder.add(seg);
        }
        fPreFixture = builder.build();
        fFixture.addAll(fPreFixture);
        fSegmentStoreProvider = new StubProvider(fFixture);
    }

    @Override
    public boolean setTrace(@NonNull ITmfTrace trace) throws TmfAnalysisException {
        if (trace instanceof TmfXmlTraceStub) {
            TmfXmlTraceStub tmfXmlTraceStub = (TmfXmlTraceStub) trace;
            tmfXmlTraceStub.addAnalysisModule(this);
            tmfXmlTraceStub.addAnalysisModule(fSegmentStoreProvider);

        }
        return super.setTrace(trace);
    }
    @Override
    protected @Nullable String getSegmentType(@NonNull ISegment segment) {
        return segment.getLength() % 2 == 0 ? "even" : "odd";
    }

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentProviderAnalysis(@NonNull ITmfTrace trace) {
        return fSegmentStoreProvider;
    }

    // visibility change
    @Override
    public boolean executeAnalysis(@NonNull IProgressMonitor monitor) throws TmfAnalysisException {
        return super.executeAnalysis(monitor);
    }

    // visibility change
    @Override
    public @NonNull Iterable<@NonNull IAnalysisModule> getDependentAnalyses() {
        return super.getDependentAnalyses();
    }
}
