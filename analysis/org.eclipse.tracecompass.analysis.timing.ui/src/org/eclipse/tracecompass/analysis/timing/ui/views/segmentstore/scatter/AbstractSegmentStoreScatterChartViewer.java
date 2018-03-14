/******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter;

import java.text.Format;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.SegmentStoreScatterDataProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.scatter.SegmentStoreScatterGraphTooltipProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;

/**
 * Displays the segment store provider data in a SWT scatter chart
 *
 * @author Yonni Chen
 * @since 2.1
 */
@SuppressWarnings("restriction")
public class AbstractSegmentStoreScatterChartViewer extends TmfFilteredXYChartViewer {

    private static final Format FORMAT = new SubSecondTimeWithUnitFormat();
    private static final int DEFAULT_SERIES_WIDTH = 1;
    private String fAnalysisId;

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public AbstractSegmentStoreScatterChartViewer(Composite parent, TmfXYChartSettings settings) {
        this(parent, settings, ""); //$NON-NLS-1$
    }

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     * @param analysisId
     *            The ID of the analysis to show in this viewer
     * @since 2.3
     */
    public AbstractSegmentStoreScatterChartViewer(Composite parent, TmfXYChartSettings settings, String analysisId) {
        super(parent, settings, SegmentStoreScatterDataProvider.ID);
        fAnalysisId = analysisId;
        setTooltipProvider(new SegmentStoreScatterGraphTooltipProvider(this));
        getSwtChart().getLegend().setVisible(false);
        getSwtChart().getAxisSet().getYAxis(0).getTick().setFormat(FORMAT);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Get the analysis ID to show in the viewer
     *
     * @return The analysis ID
     * @since 2.3
     */
    protected String getAnalysisId() {
        return fAnalysisId;
    }

    @Override
    protected @Nullable ITmfXYDataProvider initializeDataProvider(ITmfTrace trace) {
        String analysisId = getAnalysisId();
        /* Support legacy code, get the analysis ID of the segment store */
        if (analysisId.isEmpty()) {
            ISegmentStoreProvider segmentStoreProvider = getSegmentStoreProvider(trace);
            if (!(segmentStoreProvider instanceof IAnalysisModule)) {
                return null;
            }
            analysisId = ((IAnalysisModule) segmentStoreProvider).getId();
        }
        /* End support of legacy */
        return DataProviderManager.getInstance().getDataProvider(trace, SegmentStoreScatterDataProvider.ID + ':' + analysisId, ITmfTreeXYDataProvider.class);
    }

    /**
     * Returns the segment store provider
     *
     * @param trace
     *            The trace to consider
     * @return the segment store provider
     * @deprecated This method is not used anymore as the analysisId is passed in the constructor
     */
    @Deprecated
    protected @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace) {
        return null;
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * @param signal
     *            Signal received when a different trace is selected
     */
    @Override
    @TmfSignalHandler
    public void traceSelected(@Nullable TmfTraceSelectedSignal signal) {
        super.traceSelected(signal);
        if (getTrace() != null) {
            final TmfTimeRange timeRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
            setWindowRange(timeRange.getStartTime().toNanos(), timeRange.getEndTime().toNanos());
        }
    }

    /**
     * @param signal
     *            Signal received when trace is opened
     */
    @Override
    @TmfSignalHandler
    public void traceOpened(@Nullable TmfTraceOpenedSignal signal) {
        super.traceOpened(signal);
        if (getTrace() != null) {
            final TmfTimeRange timeRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
            setWindowRange(timeRange.getStartTime().toNanos(), timeRange.getEndTime().toNanos());
        }
    }

    @Override
    public IYAppearance getSeriesAppearance(@NonNull String seriesName) {
        return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.SCATTER, DEFAULT_SERIES_WIDTH);
    }

    /**
     * @param signal
     *            Signal received when last opened trace is closed
     */
    @Override
    @TmfSignalHandler
    public void traceClosed(@Nullable TmfTraceClosedSignal signal) {
        super.traceClosed(signal);

        // Check if there is no more opened trace
        if (signal != null && TmfTraceManager.getInstance().getActiveTrace() == null) {
            clearContent();
        }
        refresh();
    }
}
