/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.scatter.Messages;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.views.IViewDescriptor;

/**
 * Scatter graph showing the segments' data in the form a scatter view.
 *
 * @author Geneviève Bastien
 * @since 3.0
 * {@link org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter.SegmentStoreScatterView2}
 */
@Deprecated
public class SegmentStoreScatterView extends TmfChartView {
    /**
     * ID of this view
     */
    public static final String ID = "org.eclipse.tracecompass.analysis.timing.ui.segstore.scatter"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public SegmentStoreScatterView() {
        super(ID);
    }

    @Override
    public void createPartControl(@Nullable Composite parent) {
        super.createPartControl(parent);
        // Set the title of the view from the actual view ID
        IViewDescriptor desc = PlatformUI.getWorkbench().getViewRegistry().find(getViewId());
        if (desc != null) {
            setPartName(desc.getLabel());
        }
    }

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        String analysisId = String.valueOf(getViewSite().getSecondaryId());
        return new AbstractSegmentStoreScatterChartViewer(Objects.requireNonNull(parent),
                new TmfXYChartSettings(nullToEmptyString(Messages.SegmentStoreScatterGraphViewer_title), nullToEmptyString(Messages.SegmentStoreScatterGraphViewer_xAxis),
                        nullToEmptyString(Messages.SegmentStoreScatterGraphViewer_yAxis), 1), analysisId);
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
        String analysisId = String.valueOf(getViewSite().getSecondaryId());
        return new AbstractSegmentStoreScatterChartTreeViewer(Objects.requireNonNull(parent), analysisId);
    }

}
