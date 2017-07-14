/*******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ********************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter.AbstractSegmentStoreScatterChartViewer;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

import com.google.common.annotations.VisibleForTesting;

/**
 * Some stuff
 *
 * @author Matthew Khouzam
 */
public class SystemCallLatencyScatterView extends TmfChartView {
    // Attributes
    // ------------------------------------------------------------------------

    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.latency.scatter"; //$NON-NLS-1$

    private @Nullable AbstractSegmentStoreScatterChartViewer fScatterViewer;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public SystemCallLatencyScatterView() {
        super(ID);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        fScatterViewer = new SystemCallLatencyScatterGraphViewer(NonNullUtils.checkNotNull(parent), nullToEmptyString(Messages.SystemCallLatencyScatterView_title), nullToEmptyString(Messages.SystemCallLatencyScatterView_xAxis),
                nullToEmptyString(Messages.SystemCallLatencyScatterView_yAxis));
        return fScatterViewer;
    }

    @VisibleForTesting
    @Override
    public TmfXYChartViewer getChartViewer() {
        return super.getChartViewer();
    }

}
