/**********************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
//import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.AbstractSegmentStoreAnalysisModule;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

/*
 * Test stub for segment store table
 *
 * @author Kyrollos Bekhet
 */
public class StubSegmentStoreProvider extends AbstractSegmentStoreAnalysisModule {

    public static final int SIZE = 65535;
    private final List<@NonNull ISegment> fPreFixture;

    public StubSegmentStoreProvider() {
        ImmutableList.Builder<@NonNull ISegment> builder = new Builder<>();
        int previousStartTime = 0;
        for (int i = 0; i < SIZE; i++) {
            if (i % 7 == 0) {
                previousStartTime = i;
            }
            ISegment segment = new BasicSegment(previousStartTime, i);
            builder.add(segment);
        }
        fPreFixture = builder.build();
    }

    @Override
    protected boolean buildAnalysisSegments(@NonNull ISegmentStore<@NonNull ISegment> segmentStore, @NonNull IProgressMonitor monitor) throws TmfAnalysisException {
        return segmentStore.addAll(fPreFixture);
    }

    @Override
    protected void canceling() {
    }

    @Override
    public boolean setTrace(@NonNull ITmfTrace trace) throws TmfAnalysisException {
        if (trace instanceof TmfXmlTraceStub) {
            TmfXmlTraceStub tmfXmlTraceStub = (TmfXmlTraceStub) trace;
            tmfXmlTraceStub.addAnalysisModule(this);
        }
        return super.setTrace(trace);
    }

    @Override
    public boolean executeAnalysis(@NonNull IProgressMonitor monitor) throws TmfAnalysisException {
        return super.executeAnalysis(monitor);
    }

}
