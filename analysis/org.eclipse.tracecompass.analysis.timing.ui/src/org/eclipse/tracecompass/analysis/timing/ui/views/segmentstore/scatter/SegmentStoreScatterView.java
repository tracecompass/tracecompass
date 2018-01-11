/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.scatter.Messages;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
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
 * @since 2.2
 */
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
                        nullToEmptyString(Messages.SegmentStoreScatterGraphViewer_yAxis), 1)) {

            @Override
            protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace) {
                IAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, IAnalysisModule.class, analysisId);
                if (module instanceof ISegmentStoreProvider) {
                    return (ISegmentStoreProvider) module;
                }
                return null;
            }

        };
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
        String analysisId = String.valueOf(getViewSite().getSecondaryId());
        return new AbstractSegmentStoreScatterChartTreeViewer(Objects.requireNonNull(parent)) {

            @Override
            protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace) {
                IAnalysisModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, IAnalysisModule.class, analysisId);
                if (module instanceof ISegmentStoreProvider) {
                    return (ISegmentStoreProvider) module;
                }
                return null;
            }

        };
    }

}
