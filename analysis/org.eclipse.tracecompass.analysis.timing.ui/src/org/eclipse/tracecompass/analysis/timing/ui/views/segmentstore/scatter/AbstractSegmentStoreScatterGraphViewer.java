/******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *   Bernd Hufmann - Extracted abstract class from LatencyScatterGraphViewer
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.scatter;

import java.text.Format;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.SegmentStoreScatterDataProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.scatter.SegmentStoreScatterGraphTooltipProvider;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderManager;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.viewmodel.IYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;

/**
 * Displays the segment store provider data in a scatter graph
 *
 * @author France Lapointe Nguyen
 * @author Matthew Khouzam - reduced memory usage
 * @since 2.0
 * @deprecated Use {@link AbstractSegmentStoreScatterChartViewer} if possible
 */
@Deprecated
public abstract class AbstractSegmentStoreScatterGraphViewer extends TmfCommonXLineChartViewer {

    private static final Format FORMAT = new SubSecondTimeWithUnitFormat();

    /**
     * Constructor
     *
     * @param parent
     *            parent composite
     * @param title
     *            name of the graph
     * @param xLabel
     *            name of the x axis
     * @param yLabel
     *            name of the y axis
     */
    public AbstractSegmentStoreScatterGraphViewer(Composite parent, String title, String xLabel, String yLabel) {
        super(parent, title, xLabel, yLabel);
        setTooltipProvider(new SegmentStoreScatterGraphTooltipProvider(this));
        getSwtChart().getLegend().setVisible(false);
        getSwtChart().getAxisSet().getYAxis(0).getTick().setFormat(FORMAT);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    protected void initializeDataSource() {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            final ISegmentStoreProvider segmentStoreProvider = getSegmentStoreProvider(trace);
            String providerId = (segmentStoreProvider instanceof IAnalysisModule) ? SegmentStoreScatterDataProvider.ID + ':' + ((IAnalysisModule) segmentStoreProvider).getId() : SegmentStoreScatterDataProvider.ID;
            setDataProvider(DataProviderManager.getInstance().getDataProvider(trace, providerId, ITmfTreeXYDataProvider.class));
        }
    }

    /**
     * Returns the segment store provider
     *
     * @param trace
     *            The trace to consider
     * @return the segment store provider
     */
    protected abstract @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace);

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
    public String getSeriesType(String seriesName) {
        return IYSeries.SCATTER;
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
