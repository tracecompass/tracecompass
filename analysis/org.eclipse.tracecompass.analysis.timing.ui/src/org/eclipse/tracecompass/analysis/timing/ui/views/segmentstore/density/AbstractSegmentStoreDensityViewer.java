/******************************************************************************
 * Copyright (c) 2015, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density.MouseDragZoomProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density.MouseSelectionProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density.SimpleTooltipProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.table.SegmentStoreContentProvider.SegmentStoreWithRange;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance.Type;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.IImageSave;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.LineStyle;
import org.swtchart.Range;

import com.google.common.annotations.VisibleForTesting;

/**
 * Displays the segment store provider data in a density chart.
 *
 * @author Matthew Khouzam
 * @author Marc-Andre Laperle
 *
 * @since 2.0
 */
public abstract class AbstractSegmentStoreDensityViewer extends TmfViewer implements IImageSave {

    private static final Format DENSITY_TIME_FORMATTER = SubSecondTimeWithUnitFormat.getInstance();
    private static final RGB BAR_COLOR = new RGB(0x42, 0x85, 0xf4);
    /** The color scheme for the chart */
    private TimeGraphColorScheme fColorScheme = new TimeGraphColorScheme();
    private final Chart fChart;
    private final MouseDragZoomProvider fDragZoomProvider;
    private final MouseSelectionProvider fDragProvider;
    private final SimpleTooltipProvider fTooltipProvider;

    private @Nullable ITmfTrace fTrace;
    private @Nullable IAnalysisProgressListener fListener;
    private @Nullable ISegmentStoreProvider fSegmentStoreProvider;
    private Range fCurrentDurationRange = new Range(Double.MIN_VALUE, Double.MAX_VALUE);
    private TmfTimeRange fCurrentTimeRange = TmfTimeRange.NULL_RANGE;
    private final List<ISegmentStoreDensityViewerDataListener> fListeners;
    private int fOverrideNbPoints;
    private String fSeriesType;

    /**
     * Constructs a new density viewer.
     *
     * @param parent
     *            the parent of the viewer
     */
    public AbstractSegmentStoreDensityViewer(Composite parent) {
        super(parent);
        fListeners = new ArrayList<>();
        fChart = new Chart(parent, SWT.NONE);
        Color backgroundColor = fColorScheme.getColor(TimeGraphColorScheme.TOOL_BACKGROUND);
        fChart.setBackground(backgroundColor);
        fChart.setBackgroundInPlotArea(backgroundColor);
        parent.setBackground(backgroundColor);
        Color foregroundColor = fColorScheme.getColor(TimeGraphColorScheme.TOOL_FOREGROUND);
        fChart.setForeground(foregroundColor);
        fChart.getLegend().setVisible(false);
        fChart.getTitle().setVisible(false);
        IAxis xAxis = fChart.getAxisSet().getXAxis(0);
        IAxis yAxis = fChart.getAxisSet().getYAxis(0);
        xAxis.getTitle().setText(nullToEmptyString(Messages.AbstractSegmentStoreDensityViewer_TimeAxisLabel));
        yAxis.getTitle().setText(nullToEmptyString(Messages.AbstractSegmentStoreDensityViewer_CountAxisLabel));
        xAxis.getTitle().setForeground(foregroundColor);
        yAxis.getTitle().setForeground(foregroundColor);
        xAxis.getTick().setForeground(foregroundColor);
        yAxis.getTick().setForeground(foregroundColor);
        xAxis.getGrid().setStyle(LineStyle.DOT);
        yAxis.getGrid().setStyle(LineStyle.DOT);
        fSeriesType = IYAppearance.Type.BAR;

        fDragZoomProvider = new MouseDragZoomProvider(this);
        fDragZoomProvider.register();
        fDragProvider = new MouseSelectionProvider(this);
        fDragProvider.register();
        fTooltipProvider = new SimpleTooltipProvider(this);
        fTooltipProvider.register();

        fChart.addDisposeListener(e -> internalDispose());
    }

    /**
     * Returns the segment store provider
     *
     * @param trace
     *            The trace to consider
     * @return the
     */
    protected abstract @Nullable ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace);

    @Nullable
    private static ITmfTrace getTrace() {
        return TmfTraceManager.getInstance().getActiveTrace();
    }

    /**
     * Set the type of series you want
     * @param type the type, bar or area
     * @since 4.0
     */
    protected void setType(String type) {
        switch (type) {
        case Type.BAR:
            fSeriesType = Type.BAR;
            break;
        case Type.AREA:
            fSeriesType = Type.AREA;
            break;
        default:
            break;
        }
    }

    private void updateDisplay(SegmentStoreWithRange<ISegment> data) {
        ISeries series = fSeriesType.equals(Type.BAR) ? createSeries() : createAreaSeries();
        int barWidth = 4;
        int preWidth = fOverrideNbPoints == 0 ? fChart.getPlotArea().getBounds().width / barWidth : fOverrideNbPoints;
        if (!fSeriesType.equals(Type.BAR)) {
            preWidth += 2;
        }
        final int width = preWidth;
        double[] xOrigSeries = new double[width];
        double[] yOrigSeries = new double[width];
        // Set a positive value that is greater than 0 and less than 1.0
        Arrays.fill(yOrigSeries, Double.MIN_VALUE);
        data.setComparator(SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
        ISegment maxSegment = data.getElement(SegmentStoreWithRange.LAST);
        long maxLength = Long.MAX_VALUE;
        if (maxSegment != null) {
            maxLength = maxSegment.getLength();
        }
        double maxFactor = 1.0 / (maxLength + 1.0);
        long minX = Long.MAX_VALUE;
        for (ISegment segment : data) {
            double xBox = segment.getLength() * maxFactor * width;
            if (yOrigSeries[(int) xBox] < 1) {
                yOrigSeries[(int) xBox] = 1;
            } else {
                yOrigSeries[(int) xBox]++;
            }
            minX = Math.min(minX, segment.getLength());
        }
        double timeWidth = (double) maxLength / (double) width;
        for (int i = 0; i < width; i++) {
            xOrigSeries[i] = i * timeWidth;
            if (!fSeriesType.equals(Type.BAR)) {
                xOrigSeries[i] += timeWidth / 2;
            }
        }
        double maxY = Double.MIN_VALUE;
        for (int i = 0; i < width; i++) {
            maxY = Math.max(maxY, yOrigSeries[i]);
        }
        if (minX == maxLength) {
            maxLength++;
            minX--;
        }
        series.setYSeries(yOrigSeries);
        series.setXSeries(xOrigSeries);
        final IAxis xAxis = fChart.getAxisSet().getXAxis(0);
        /*
         * adjustrange appears to bring origin back since we pad the series with
         * 0s, not interesting.
         */
        xAxis.adjustRange();
        Range range = xAxis.getRange();
        // fix for overly aggressive lower after an adjust range
        range.lower = minX - range.upper + maxLength;
        xAxis.setRange(range);
        xAxis.getTick().setFormat(DENSITY_TIME_FORMATTER);
        /*
         * Set the range to slightly under 1 but above 0 so that log scales
         * display properly.
         */
        fChart.getAxisSet().getYAxis(0).setRange(new Range(0.9, maxY));
        fChart.getAxisSet().getYAxis(0).enableLogScale(true);
        fChart.redraw();
        new Thread(() -> {
            for (ISegmentStoreDensityViewerDataListener l : fListeners) {
                l.chartUpdated();
            }
        }).start();

    }

    private ISeries createSeries() {
        IBarSeries series = (IBarSeries) fChart.getSeriesSet().createSeries(SeriesType.BAR, Messages.AbstractSegmentStoreDensityViewer_SeriesLabel);
        series.setVisible(true);
        series.setBarPadding(0);
        series.setBarColor(new Color(Display.getDefault(), BAR_COLOR));
        return series;
    }

    private ISeries createAreaSeries() {
        ILineSeries series = (ILineSeries) fChart.getSeriesSet().createSeries(SeriesType.LINE, Messages.AbstractSegmentStoreDensityViewer_SeriesLabel);
        series.setVisible(true);
        series.enableStep(true);
        series.enableArea(true);
        series.setSymbolType(PlotSymbolType.NONE);
        series.setLineColor(new Color(Display.getDefault(), BAR_COLOR));
        return series;
    }

    @Override
    public Chart getControl() {
        return fChart;
    }

    /**
     * Select a range of latency durations in the viewer.
     *
     * @param durationRange
     *            a range of latency durations
     */
    public void select(final Range durationRange) {
        fCurrentDurationRange = durationRange;
        final TmfTimeRange timeRange = fCurrentTimeRange;
        computeDataAsync(timeRange, durationRange).thenAccept(data -> {
            synchronized (fListeners) {
                if (fCurrentTimeRange.equals(timeRange) && fCurrentDurationRange.equals(durationRange)) {
                    for (ISegmentStoreDensityViewerDataListener listener : fListeners) {
                        listener.selectedDataChanged(data);
                    }
                }
            }
        });
    }

    /**
     * Zoom to a range of latency durations in the viewer.
     *
     * @param durationRange
     *            a range of latency durations
     */
    public void zoom(final Range durationRange) {
        fCurrentDurationRange = durationRange;
        final TmfTimeRange timeRange = fCurrentTimeRange;
        computeDataAsync(timeRange, durationRange).thenAccept(data -> {
            synchronized (fListeners) {
                if (fCurrentTimeRange.equals(timeRange) && fCurrentDurationRange.equals(durationRange)) {
                    applyData(data);
                }
            }
        });
    }

    private CompletableFuture<@Nullable SegmentStoreWithRange<ISegment>> computeDataAsync(final TmfTimeRange timeRange, final Range durationRange) {
        return CompletableFuture.supplyAsync(() -> computeData(timeRange, durationRange));
    }

    private @Nullable SegmentStoreWithRange<ISegment> computeData(final TmfTimeRange timeRange, final Range durationRange) {
        final ISegmentStoreProvider segmentProvider = fSegmentStoreProvider;
        if (segmentProvider == null) {
            return null;
        }
        final ISegmentStore<ISegment> segStore = segmentProvider.getSegmentStore();
        if (segStore == null) {
            return null;
        }

        // Filter on the segment duration if necessary
        if (durationRange.lower > Double.MIN_VALUE || durationRange.upper < Double.MAX_VALUE) {
            Predicate<ISegment> predicate = segment -> segment.getLength() >= durationRange.lower && segment.getLength() <= durationRange.upper;
            return new SegmentStoreWithRange<>(segStore, timeRange, predicate);
        }

        return new SegmentStoreWithRange<>(segStore, timeRange);

    }

    private void applyData(final @Nullable SegmentStoreWithRange<ISegment> data) {
        if (data != null) {
            data.setComparator(SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
            Display.getDefault().asyncExec(() -> updateDisplay(data));
            for (ISegmentStoreDensityViewerDataListener l : fListeners) {
                l.viewDataChanged(data);
            }
        }
    }

    /**
     * Sets the segment store provider
     *
     * @param ssp
     *            The segment store provider to give to this view
     *
     * @since 1.2
     */
    @VisibleForTesting
    public void setSegmentProvider(@Nullable ISegmentStoreProvider ssp) {
        fSegmentStoreProvider = ssp;
    }

    /**
     * Signal handler for handling of the window range signal.
     *
     * @param signal
     *            The {@link TmfWindowRangeUpdatedSignal}
     */
    @TmfSignalHandler
    public void windowRangeUpdated(@Nullable TmfWindowRangeUpdatedSignal signal) {
        if (signal == null) {
            return;
        }
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }
        fSegmentStoreProvider = getSegmentStoreProvider(trace);
        fCurrentTimeRange = NonNullUtils.checkNotNull(signal.getCurrentRange());
        updateWithRange(fCurrentTimeRange);
    }

    /**
     * Update the display range
     *
     * @param timeRange
     *            the range
     * @since 1.2
     */
    @VisibleForTesting
    public void updateWithRange(final TmfTimeRange timeRange) {
        fCurrentTimeRange = timeRange;
        fCurrentDurationRange = new Range(Double.MIN_VALUE, Double.MAX_VALUE);
        final Range durationRange = fCurrentDurationRange;
        computeDataAsync(timeRange, durationRange).thenAccept(data -> {
            synchronized (fListeners) {
                if (fCurrentTimeRange.equals(timeRange) && fCurrentDurationRange.equals(durationRange)) {
                    applyData(data);
                }
            }
        });
    }

    @Override
    public void refresh() {
        fChart.redraw();
    }

    @Override
    public void dispose() {
        if (!fChart.isDisposed()) {
            fChart.dispose();
        }
    }

    private void internalDispose() {
        if (fSegmentStoreProvider != null && fListener != null) {
            fSegmentStoreProvider.removeListener(fListener);
        }
        fDragZoomProvider.deregister();
        fTooltipProvider.deregister();
        fDragProvider.deregister();
        super.dispose();
    }

    /**
     * Signal handler for handling of the trace opened signal.
     *
     * @param signal
     *            The trace opened signal {@link TmfTraceOpenedSignal}
     */
    @TmfSignalHandler
    public void traceOpened(TmfTraceOpenedSignal signal) {
        fTrace = signal.getTrace();
        loadTrace(getTrace());
    }

    /**
     * Signal handler for handling of the trace selected signal.
     *
     * @param signal
     *            The trace selected signal {@link TmfTraceSelectedSignal}
     */
    @TmfSignalHandler
    public void traceSelected(TmfTraceSelectedSignal signal) {
        if (fTrace != signal.getTrace()) {
            fTrace = signal.getTrace();
            loadTrace(getTrace());
        }
    }

    /**
     * Signal handler for handling of the trace closed signal.
     *
     * @param signal
     *            The trace closed signal {@link TmfTraceClosedSignal}
     */
    @TmfSignalHandler
    public void traceClosed(TmfTraceClosedSignal signal) {

        if (signal.getTrace() != fTrace) {
            return;
        }

        fTrace = null;
        clearContent();
    }

    /**
     * A Method to load a trace into the viewer.
     *
     * @param trace
     *            A trace to apply in the viewer
     */
    protected void loadTrace(@Nullable ITmfTrace trace) {
        clearContent();

        fTrace = trace;
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        TmfTimeRange windowRange = ctx.getWindowRange();
        fCurrentTimeRange = windowRange;

        if (trace != null) {
            fSegmentStoreProvider = getSegmentStoreProvider(trace);
            final ISegmentStoreProvider provider = fSegmentStoreProvider;
            if (provider != null) {
                fListener = (segmentProvider, data) -> updateWithRange(windowRange);
                provider.addListener(fListener);
                if (provider instanceof IAnalysisModule) {
                    ((IAnalysisModule) provider).schedule();
                }
            }
        }
        zoom(new Range(0, Long.MAX_VALUE));
    }

    /**
     * Clears the view content.
     */
    private void clearContent() {
        final Chart chart = fChart;
        if (!chart.isDisposed()) {
            ISeriesSet set = chart.getSeriesSet();
            ISeries[] series = set.getSeries();
            for (int i = 0; i < series.length; i++) {
                set.deleteSeries(series[i].getId());
            }
            for (IAxis axis : chart.getAxisSet().getAxes()) {
                axis.setRange(new Range(0, 1));
            }
            chart.redraw();
        }
    }

    /**
     * Force the number of points to a fixed value
     *
     * @param nbPoints
     *            The number of points to display, cannot be negative. 0 means use
     *            native resolution. any positive integer means that number of
     *            points
     * @since 2.2
     */
    public synchronized void setNbPoints(int nbPoints) {
        if (nbPoints < 0) {
            throw new IllegalArgumentException("Number of points cannot be negative"); //$NON-NLS-1$
        }
        fOverrideNbPoints = nbPoints;
        updateWithRange(fCurrentTimeRange);
    }

    /**
     * Add a data listener.
     *
     * @param dataListener
     *            the data listener to add
     */
    public void addDataListener(ISegmentStoreDensityViewerDataListener dataListener) {
        fListeners.add(dataListener);
    }

    /**
     * Remove a data listener.
     *
     * @param dataListener
     *            the data listener to remove
     */
    public void removeDataListener(ISegmentStoreDensityViewerDataListener dataListener) {
        fListeners.remove(dataListener);
    }
}