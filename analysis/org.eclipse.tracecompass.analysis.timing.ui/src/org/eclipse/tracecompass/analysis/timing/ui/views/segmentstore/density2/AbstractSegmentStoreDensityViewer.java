/******************************************************************************
 * Copyright (c) 2015, 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density2;

import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import java.text.Format;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;
import java.util.stream.StreamSupport;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.Chart;
import org.eclipse.swtchart.IAxis;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ILegend;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ILineSeries.PlotSymbolType;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.Range;
import org.eclipse.swtchart.model.DoubleArraySeriesModel;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density2.MouseDragZoomProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density2.MouseSelectionProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density2.SimpleTooltipProvider;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.table.SegmentStoreContentProvider.SegmentStoreWithRange;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.RotatingPaletteProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
import org.eclipse.tracecompass.tmf.ui.viewers.IImageSave;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.AxisRange;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.widgets.TimeGraphColorScheme;

import com.google.common.annotations.VisibleForTesting;

/**
 * Displays the segment store provider data in a density chart.
 *
 * @author Matthew Khouzam
 * @author Marc-Andre Laperle
 *
 * @since 4.1
 */
public abstract class AbstractSegmentStoreDensityViewer extends TmfViewer implements IImageSave {

    private static final Format DENSITY_TIME_FORMATTER = SubSecondTimeWithUnitFormat.getInstance();
    private static final RGB BAR_COLOR = new RGB(0x42, 0x85, 0xf4);
    private static final ColorRegistry COLOR_REGISTRY = new ColorRegistry();
    private static final RotatingPaletteProvider PALETTE = new RotatingPaletteProvider.Builder().setSaturation(0.73f).setBrightness(0.957f).setNbColors(60).build();

    /** The color scheme for the chart */
    private TimeGraphColorScheme fColorScheme = new TimeGraphColorScheme();
    private final Chart fChart;
    private final MouseDragZoomProvider fDragZoomProvider;
    private final MouseSelectionProvider fDragProvider;
    private final SimpleTooltipProvider fTooltipProvider;

    private @Nullable ITmfTrace fTrace;
    private Map<@NonNull String, @NonNull IAnalysisProgressListener> fProgressListeners = new HashMap<>();
    private final Map<@NonNull String, @NonNull ISegmentStoreProvider> fSegmentStoreProviders = new HashMap<>();
    private AxisRange fCurrentDurationRange = new AxisRange(AbstractSegmentStoreDensityView.DEFAULT_RANGE.getFirst(), AbstractSegmentStoreDensityView.DEFAULT_RANGE.getSecond());
    private TmfTimeRange fCurrentTimeRange = TmfTimeRange.NULL_RANGE;
    private final List<ISegmentStoreDensityViewerDataListener> fListeners;
    private int fOverrideNbPoints;
    private String fSeriesType;
    private final Set<@NonNull ITmfTrace> fTraces = new HashSet<>();

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
        backgroundColor = fColorScheme.getColor(TimeGraphColorScheme.BACKGROUND);
        fChart.getPlotArea().setBackground(backgroundColor);
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
        fSeriesType = StyleProperties.SeriesType.BAR;

        fDragZoomProvider = new MouseDragZoomProvider(this);
        fDragProvider = new MouseSelectionProvider(this);
        fTooltipProvider = new SimpleTooltipProvider(this);

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
     * @since 4.1
     */
    protected void setType(String type) {
        switch (type) {
        case StyleProperties.SeriesType.BAR:
            fSeriesType = StyleProperties.SeriesType.BAR;
            break;
        case StyleProperties.SeriesType.AREA:
            fSeriesType = StyleProperties.SeriesType.AREA;
            break;
        default:
            break;
        }
    }

    private synchronized void updateDisplay(String name, Iterable<ISegment> data) {
        ISeries<Integer> series = fSeriesType.equals(StyleProperties.SeriesType.BAR) ? createSeries() : createAreaSeries(name);
        int barWidth = 4;
        int preWidth = fOverrideNbPoints == 0 ? fChart.getPlotArea().getSize().x / barWidth : fOverrideNbPoints;
        if (!fSeriesType.equals(StyleProperties.SeriesType.BAR)) {
            preWidth += 2;
        }
        final int width = preWidth;
        double[] xOrigSeries = new double[width];
        double[] yOrigSeries = new double[width];
        // Set a positive value that is greater than 0 and less than 1.0
        Arrays.fill(yOrigSeries, Double.MIN_VALUE);
        Optional<ISegment> maxSegment = StreamSupport.stream(data.spliterator(), false).max(SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
        long maxLength = Long.MIN_VALUE;
        if (maxSegment.isPresent()) {
            maxLength = maxSegment.get().getLength();
        } else {
            for (ISegment segment : data) {
                maxLength = Math.max(maxLength, segment.getLength());
            }
            if (maxLength == Long.MIN_VALUE) {
                maxLength = 1;
            }
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
            if (!fSeriesType.equals(StyleProperties.SeriesType.BAR)) {
                xOrigSeries[i] += timeWidth / 2;
            }
        }
        double maxY = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < width; i++) {
            maxY = Math.max(maxY, yOrigSeries[i]);
        }
        if (minX == maxLength) {
            maxLength++;
            minX--;
        }
        series.setDataModel(new DoubleArraySeriesModel(xOrigSeries, yOrigSeries));
        final IAxis xAxis = fChart.getAxisSet().getXAxis(0);
        /*
         * adjustrange appears to bring origin back since we pad the series with
         * 0s, not interesting.
         */
        AxisRange currentDurationRange = fCurrentDurationRange;
        if (Double.isFinite(currentDurationRange.getLower()) && Double.isFinite(currentDurationRange.getUpper())) {
            xAxis.setRange(new Range(currentDurationRange.getLower(), currentDurationRange.getUpper()));
        } else {
            xAxis.adjustRange();
        }

        xAxis.getTick().setFormat(DENSITY_TIME_FORMATTER);
        ILegend legend = fChart.getLegend();
        legend.setVisible(fSegmentStoreProviders.size() > 1);
        legend.setPosition(SWT.BOTTOM);
        /*
         * Clamp range lower to 0.9 to make it log, 0.1 would be scientifically
         * accurate, but we cannot have partial counts.
         */
        for (ISeries<?> internalSeries : fChart.getSeriesSet().getSeries()) {
            maxY = Math.max(maxY, internalSeries.getDataModel().getMaxY().doubleValue());
        }
        fChart.getAxisSet().getYAxis(0).setRange(new Range(0.9, Math.max(1.0, maxY)));
        fChart.getAxisSet().getYAxis(0).enableLogScale(true);
        new Thread(() -> {
            for (ISegmentStoreDensityViewerDataListener l : fListeners) {
                l.chartUpdated();
            }
        }).start();
    }

    private ISeries<Integer> createSeries() {
        IBarSeries<Integer> series = (IBarSeries<Integer>) fChart.getSeriesSet().createSeries(SeriesType.BAR, Messages.AbstractSegmentStoreDensityViewer_SeriesLabel);
        series.setVisible(true);
        series.setBarPadding(0);
        series.setBarColor(getColorForRGB(BAR_COLOR));
        return series;
    }

    private ISeries<Integer> createAreaSeries(String name) {
        ILineSeries<Integer> series = (ILineSeries<Integer>) fChart.getSeriesSet().createSeries(SeriesType.LINE, name);
        series.setVisible(true);
        series.enableStep(true);
        series.enableArea(true);
        series.setSymbolType(PlotSymbolType.NONE);
        RGB rgb = getColorForItem(name);
        Color color = getColorForRGB(rgb);
        series.setLineColor(color);
        return series;
    }

    private static Color getColorForRGB(RGB rgb) {
        String rgbString = rgb.toString();
        Color color = COLOR_REGISTRY.get(rgbString);
        if (color == null) {
            COLOR_REGISTRY.put(rgbString, rgb);
            color = Objects.requireNonNull(COLOR_REGISTRY.get(rgbString));
        }
        return color;
    }

    /**
     * Get the color for a series
     *
     * @param name
     *            the series name
     * @return The color in RGB
     * @since 4.1
     */
    public RGB getColorForItem(String name) {
        if (fSegmentStoreProviders.size() == 1) {
            return BAR_COLOR;
        }
        Set<String> keys = fSegmentStoreProviders.keySet();
        int i = 0;
        for (String key : keys) {
            if (key.equals(name)) {
                break;
            }
            i++;
        }
        float pos = (float) i / keys.size();
        int index = Math.max((int) (PALETTE.getNbColors() * pos), 0) % PALETTE.getNbColors();
        return Objects.requireNonNull(RGBAUtil.fromRGBAColor(PALETTE.get().get(index)).rgb);
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
    public void select(final AxisRange durationRange) {
        fCurrentDurationRange = durationRange;
        final TmfTimeRange timeRange = fCurrentTimeRange;
        computeDataAsync(timeRange, durationRange).thenAccept(data -> {
            synchronized (fListeners) {
                if (fCurrentTimeRange.equals(timeRange) && fCurrentDurationRange.equals(durationRange)) {
                    for (ISegmentStoreDensityViewerDataListener listener : fListeners) {
                        for (SegmentStoreWithRange<ISegment> value : data.values()) {
                            listener.selectedDataChanged(value);
                        }
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
    public void zoom(final AxisRange durationRange) {
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

    private CompletableFuture<Map<String, SegmentStoreWithRange<ISegment>>> computeDataAsync(final TmfTimeRange timeRange, final AxisRange durationRange) {
        return CompletableFuture.supplyAsync(() -> computeData(timeRange, durationRange));
    }

    private @Nullable Map<String, SegmentStoreWithRange<ISegment>> computeData(final TmfTimeRange timeRange, final AxisRange durationRange) {
        Map<String, SegmentStoreWithRange<ISegment>> retVal = new HashMap<>();
        for (Entry<String, ISegmentStoreProvider> entry : fSegmentStoreProviders.entrySet()) {
            final ISegmentStoreProvider segmentProvider = Objects.requireNonNull(entry.getValue());
            final ISegmentStore<ISegment> segStore = segmentProvider.getSegmentStore();
            if (segStore == null) {
                continue;
            }

            // Filter on the segment duration if necessary
            if (durationRange.getLower() > Double.MIN_VALUE || durationRange.getUpper() < Double.MAX_VALUE) {
                Predicate<ISegment> predicate = segment -> segment.getLength() >= durationRange.getLower() && segment.getLength() <= durationRange.getUpper();
                retVal.put(entry.getKey(), new SegmentStoreWithRange<>(segStore, timeRange, predicate));
            } else {
                retVal.put(entry.getKey(), new SegmentStoreWithRange<>(segStore, timeRange));
            }
        }
        return retVal;
    }

    private void applyData(final Map<String, SegmentStoreWithRange<ISegment>> map) {
        Set<Entry<String, SegmentStoreWithRange<ISegment>>> entrySet = map.entrySet();
        if (entrySet.isEmpty()) {
            return;
        }
        entrySet.parallelStream().forEach(entry -> {
            SegmentStoreWithRange<ISegment> data = Objects.requireNonNull(entry.getValue());
            data.setComparator(SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
            Display.getDefault().asyncExec(() -> updateDisplay(entry.getKey(), data));
            if (fSegmentStoreProviders.size() > 1) {
                setType(StyleProperties.SeriesType.AREA);
            } else {
                setType(StyleProperties.SeriesType.BAR);
            }
            for (ISegmentStoreDensityViewerDataListener l : fListeners) {
                l.viewDataChanged(data);
            }
        });
        fChart.redraw();
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
        fSegmentStoreProviders.clear();
        if (ssp != null) {
            fSegmentStoreProviders.put("", ssp); //$NON-NLS-1$
        }
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
        ITmfTrace parent = getTrace();
        if (parent == null) {
            return;
        }
        fCurrentTimeRange = NonNullUtils.checkNotNull(signal.getCurrentRange());
        updateWindowRange(fCurrentTimeRange, false);
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
        final AxisRange durationRange = getDefaultRange();
        fCurrentDurationRange = durationRange;
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
        fSegmentStoreProviders.entrySet().forEach(entry -> {
            IAnalysisProgressListener listener = fProgressListeners.get(entry.getKey());
            if (listener != null) {
                Objects.requireNonNull(entry.getValue()).removeListener(listener);
            }
        });
        fTooltipProvider.dispose();
        fProgressListeners.clear();
        fDragZoomProvider.dispose();
        fDragProvider.dispose();
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
        if (fTrace != signal.getTrace()) {
            loadTrace(getTrace());
            fTrace = signal.getTrace();
        }
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
            loadTrace(getTrace());
            fTrace = signal.getTrace();
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
        TmfTraceContext ctx = TmfTraceManager.getInstance().getCurrentTraceContext();
        TmfTimeRange windowRange = ctx.getWindowRange();
        innerLoadTrace(trace);
        updateWindowRange(windowRange, true);
        fTrace = trace;
        fCurrentTimeRange = windowRange;
        zoom(getDefaultRange());
    }

    private void innerLoadTrace(@Nullable ITmfTrace trace) {
        Set<ISegmentStoreProvider> sps = new HashSet<>();

        if (trace != null) {
            boolean newTrace = !Objects.equals(trace, fTrace) || fSegmentStoreProviders.isEmpty();
            if (newTrace) {
                fTraces.clear();
                fSegmentStoreProviders.clear();
                for (ITmfTrace child : TmfTraceManager.getTraceSet(trace)) {
                    fTraces.add(child);
                    ISegmentStoreProvider segmentStoreProvider = getSegmentStoreProvider(child);
                    String name = child.getName();
                    if (segmentStoreProvider != null && name != null) {
                        fSegmentStoreProviders.put(name, segmentStoreProvider);
                        sps.add(segmentStoreProvider);
                    }
                }
                ISegmentStoreProvider segmentStoreProvider = getSegmentStoreProvider(trace);
                String name = trace.getName();
                if (segmentStoreProvider != null && name != null && !sps.contains(segmentStoreProvider)) {
                    fSegmentStoreProviders.put(name, segmentStoreProvider);
                }
            }
        }
    }

    private void updateWindowRange(TmfTimeRange windowRange, boolean updateListeners) {

        for (Entry<@NonNull String, @NonNull ISegmentStoreProvider> entry : fSegmentStoreProviders.entrySet()) {
            ISegmentStoreProvider provider = Objects.requireNonNull(entry.getValue());
            if (updateListeners) {
                IAnalysisProgressListener listener = (segmentProvider, data) -> updateWithRange(windowRange);
                provider.addListener(listener);
                fProgressListeners.put(entry.getKey(), listener);
            }
            if (provider instanceof IAnalysisModule) {
                ((IAnalysisModule) provider).schedule();
            }
        }
    }

    private static AxisRange getDefaultRange() {
        return new AxisRange(AbstractSegmentStoreDensityView.DEFAULT_RANGE.getFirst(), AbstractSegmentStoreDensityView.DEFAULT_RANGE.getSecond());
    }

    /**
     * Clears the view content.
     */
    private void clearContent() {
        final Chart chart = fChart;
        if (!chart.isDisposed()) {
            ISeriesSet set = chart.getSeriesSet();
            ISeries<?>[] series = set.getSeries();
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