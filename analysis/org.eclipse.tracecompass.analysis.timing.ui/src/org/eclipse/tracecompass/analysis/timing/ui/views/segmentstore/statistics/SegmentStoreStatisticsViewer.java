/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Generic viewer to show segment store statistics analysis data.
 *
 * @since 1.4
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
        super(parent);
        fAnalysisId = analysisId;
    }

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

        };
        return module;
    }

}
