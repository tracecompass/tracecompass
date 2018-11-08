/*******************************************************************************
 * Copyright (c) 2016, 2018 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.SegmentStoreStatisticsDataProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.statistics.Messages;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Generic viewer to show segment store statistics analysis data.
 *
 * @since 2.0
 */
public class SegmentStoreStatisticsViewer extends AbstractSegmentsStatisticsViewer {

    private final String fAnalysisId;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param analysisId
     *            The ID of the segment store provider to do statistics on
     */
    public SegmentStoreStatisticsViewer(Composite parent, String analysisId) {
        super(parent, SegmentStoreStatisticsDataProvider.ID + ':' + analysisId);
        fAnalysisId = analysisId;
    }

    @Deprecated
    @Override
    protected @Nullable TmfAbstractAnalysisModule createStatisticsAnalysiModule() {
        AbstractSegmentStatisticsAnalysis module = new AbstractSegmentStatisticsAnalysis() {

            @Override
            protected @Nullable String getSegmentType(@NonNull ISegment segment) {
                if (segment instanceof INamedSegment) {
                    return ((INamedSegment) segment).getName();
                }
                return null;
            }

            @Override
            protected @Nullable ISegmentStoreProvider getSegmentProviderAnalysis(@NonNull ITmfTrace trace) {
                IAnalysisModule m = trace.getAnalysisModule(fAnalysisId);
                if (!(m instanceof ISegmentStoreProvider)) {
                    return null;
                }
                return (ISegmentStoreProvider) m;
            }

            @Override
            public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
                ISegmentStoreProvider m = getSegmentProviderAnalysis(trace);
                if (m instanceof IAnalysisModule) {
                    setName(Objects.requireNonNull(NLS.bind(Messages.SegmentStoreStatisticsViewer_AnalysisName, ((IAnalysisModule) m).getName())));
                }
                return super.setTrace(trace);
            }
        };
        return module;
    }

}
