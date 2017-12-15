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

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter.AbstractSegmentStoreScatterChartViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

import com.google.common.annotations.VisibleForTesting;

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

    private @Nullable AbstractSegmentStoreScatterChartViewer fScatterViewer;

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
        fScatterViewer = new EventMatchingScatterGraphViewer(NonNullUtils.checkNotNull(parent), nullToEmptyString(Messages.EventMatchingScatterView_title), nullToEmptyString(Messages.EventMatchingScatterView_xAxis),
                nullToEmptyString(Messages.EventMatchingScatterView_yAxis));
        return fScatterViewer;
    }

    @VisibleForTesting
    @Override
    public TmfXYChartViewer getChartViewer() {
        return super.getChartViewer();
    }

}
