/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ********************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.ui.views.event.matching;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter.AbstractSegmentStoreScatterChartTreeViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.timing.core.event.matching.EventMatchingLatencyAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

/**
 * Scatter graph showing the event matching latencies in time
 *
 * @author Geneviève Bastien
 */
public class EventMatchingScatterView extends TmfChartView {
    // Attributes
    // ------------------------------------------------------------------------

    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.internal.analysis.timing.ui.views.event.matching.scatter"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public EventMatchingScatterView() {
        super(ID);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        return new EventMatchingScatterGraphViewer(NonNullUtils.checkNotNull(parent), nullToEmptyString(Messages.EventMatchingScatterView_title), nullToEmptyString(Messages.EventMatchingScatterView_xAxis),
                nullToEmptyString(Messages.EventMatchingScatterView_yAxis));
    }



    @Override
    protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
        return new AbstractSegmentStoreScatterChartTreeViewer(Objects.requireNonNull(parent)) {

            @Override
            protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace) {
                return TmfTraceUtils.getAnalysisModuleOfClass(trace, EventMatchingLatencyAnalysis.class, EventMatchingLatencyAnalysis.ID);
            }

        };
    }

}
