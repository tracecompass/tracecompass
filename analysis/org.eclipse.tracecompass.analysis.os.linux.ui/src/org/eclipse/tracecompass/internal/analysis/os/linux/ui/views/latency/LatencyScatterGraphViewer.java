/******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   France Lapointe Nguyen - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.LatencyAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.LatencyAnalysisListener;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.Activator;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfChartTimeStampFormat;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.LineStyle;
import org.swtchart.Range;

/**
 * Displays the latency analysis data in a scatter graph
 *
 * @author France Lapointe Nguyen
 * @author Matthew Khouzam - reduced memory usage
 * @since 1.0
 */
public class LatencyScatterGraphViewer extends TmfCommonXLineChartViewer {
    private static final List<ISegment> EMPTY_LIST = NonNullUtils.checkNotNull(Collections.<ISegment> emptyList());

    private final class CompactingLatencyQuery extends Job {
        private static final String PLUGIN_ID = "org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency"; //$NON-NLS-1$
        private static final long MAX_POINTS = 1000;
        private final TmfTimeRange fCurrentRange;

        private CompactingLatencyQuery(TmfTimeRange currentRange) {
            super(Messages.LatencyScatterGraphViewer_compactTitle);
            fCurrentRange = currentRange;
        }

        @Override
        protected IStatus run(@Nullable IProgressMonitor monitor) {
            final IProgressMonitor statusMonitor = monitor;
            if (statusMonitor == null) {
                return new Status(IStatus.ERROR, PLUGIN_ID, "Monitor is null"); //$NON-NLS-1$
            }

            LatencyAnalysis module = getAnalysisModule();
            final long startTimeInNanos = getTimeInNanos(fCurrentRange.getStartTime());
            final long endTimeInNanos = getTimeInNanos(fCurrentRange.getEndTime());
            if (module == null) {
                setWindowRange(startTimeInNanos, endTimeInNanos);
                redraw(statusMonitor, startTimeInNanos, startTimeInNanos, EMPTY_LIST);
                return new Status(IStatus.WARNING, PLUGIN_ID, "Analysis module not available"); //$NON-NLS-1$
            }

            final ISegmentStore<ISegment> results = module.getResults();
            if (results == null) {
                setWindowRange(startTimeInNanos, endTimeInNanos);
                redraw(statusMonitor, startTimeInNanos, startTimeInNanos, EMPTY_LIST);
                return new Status(IStatus.INFO, PLUGIN_ID, "Analysis module does not have results"); //$NON-NLS-1$
            }

            final long startTime = fCurrentRange.getStartTime().getValue();
            final long endTime = fCurrentRange.getEndTime().getValue();
            fPixelStart = startTime;
            fPixelSize = (endTime - startTime) / MAX_POINTS;
            final Iterable<ISegment> intersectingElements = results.getIntersectingElements(startTime, endTime);

            final List<ISegment> list = convertIterableToList(intersectingElements, statusMonitor);
            final List<ISegment> displayData = (!list.isEmpty()) ? compactList(startTime, list, statusMonitor) : list;

            setWindowRange(startTimeInNanos, endTimeInNanos);
            redraw(statusMonitor, startTime, endTime, displayData);

            if (statusMonitor.isCanceled()) {
                return NonNullUtils.checkNotNull(Status.CANCEL_STATUS);
            }
            return NonNullUtils.checkNotNull(Status.OK_STATUS);

        }

        private void redraw(final IProgressMonitor statusMonitor, final long startTime, final long endTime, final List<ISegment> displayData) {
            fDisplayData = displayData;
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    updateData(startTime, endTime, displayData.size(), statusMonitor);
                }
            });
        }

        private List<ISegment> compactList(final long startTime, final List<ISegment> listToCompact, final IProgressMonitor statusMonitor) {
            List<ISegment> displayData = new ArrayList<>();
            ISegment last = listToCompact.get(0);
            if (last.getStart() >= startTime) {
                displayData.add(last);
            }
            for (ISegment next : listToCompact) {
                if (next.getStart() < startTime) {
                    continue;
                }
                if (statusMonitor.isCanceled()) {
                    return NonNullUtils.checkNotNull(Collections.<ISegment> emptyList());
                }
                if (!overlaps(last, next)) {
                    displayData.add(next);
                    last = next;
                }
            }
            return displayData;
        }

        private List<ISegment> convertIterableToList(final Iterable<ISegment> iterable, final IProgressMonitor statusMonitor) {
            final List<ISegment> list = new ArrayList<>();
            for (ISegment seg : iterable) {
                if (statusMonitor.isCanceled()) {
                    return NonNullUtils.checkNotNull(Collections.<ISegment> emptyList());
                }
                list.add(seg);
            }
            Collections.sort(list, SegmentComparators.INTERVAL_START_COMPARATOR);
            return list;
        }

        private boolean overlaps(ISegment last, ISegment next) {
            long timePerPix = fPixelSize;
            final long start = last.getStart();
            final long pixelStart = fPixelStart;
            final long pixelDuration = start - pixelStart;
            long startPixBoundL = pixelDuration / timePerPix * timePerPix + pixelStart;
            long startPixBoundR = startPixBoundL + timePerPix;
            final long currentStart = next.getStart();
            if (currentStart >= startPixBoundL && currentStart <= startPixBoundR) {
                long length = last.getLength();
                long lengthNext = next.getLength();
                long lengthLow = length / timePerPix * timePerPix;
                long lengthHigh = lengthLow + timePerPix;
                return (lengthNext >= lengthLow && lengthNext <= lengthHigh);
            }
            return false;
        }
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Listener to update the model with the latency analysis results once the
     * latency analysis is fully completed
     */
    private final class LatencyListener implements LatencyAnalysisListener {

        @Override
        public void onComplete(LatencyAnalysis activeAnalysis, ISegmentStore<ISegment> results) {
            // Only update the model if trace that was analyzed is active trace
            if (activeAnalysis.equals(getAnalysisModule())) {
                updateModel(results);
                updateRange(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange());
            }
        }
    }

    private long fPixelSize = -1;

    private long fPixelStart = 0;
    /**
     * Data to display
     */
    private Collection<ISegment> fDisplayData = NonNullUtils.checkNotNull(Collections.<ISegment> emptyList());

    /**
     * Latency analysis completion listener
     */
    private LatencyListener fListener;

    /**
     * Current analysis module
     */
    private @Nullable LatencyAnalysis fAnalysisModule;

    private @Nullable Job fCompactingJob;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

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
    public LatencyScatterGraphViewer(Composite parent, String title, String xLabel, String yLabel) {
        super(parent, title, xLabel, yLabel);
        fListener = new LatencyListener();
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        initializeModule(trace);
        getSwtChart().getLegend().setVisible(false);
    }

    private final void initializeModule(@Nullable ITmfTrace trace) {
        if (trace != null) {
            final LatencyAnalysis analysisModuleOfClass = TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID);
            if (analysisModuleOfClass != null) {
                analysisModuleOfClass.addListener(fListener);
                setData(analysisModuleOfClass);
                updateRange(TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange());
            }
        }
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Update the data in the graph
     *
     * @param dataInput
     *            new model
     */
    public void updateModel(@Nullable ISegmentStore<ISegment> dataInput) {
        // Update new window range
        TmfTimeRange currentRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
        long currentStart = getTimeInNanos(currentRange.getStartTime());
        long currentEnd = getTimeInNanos(currentRange.getEndTime());
        if (dataInput == null) {
            Display.getDefault().syncExec(new Runnable() {
                @Override
                public void run() {
                    clearContent();
                }
            });
            fDisplayData = NonNullUtils.checkNotNull(Collections.EMPTY_LIST);
        } else {
            Collection<ISegment> elements = (Collection<ISegment>) dataInput.getIntersectingElements(currentStart, currentEnd);
            // getIntersectingElements can return an unsorted iterable, make
            // sure our collection is sorted
            ArrayList<ISegment> list = new ArrayList<>(elements);
            Collections.sort(list, SegmentComparators.INTERVAL_START_COMPARATOR);
            fDisplayData = list;
        }
        setWindowRange(currentStart, currentEnd);
        updateRange(currentRange);
    }

    @Override
    protected void initializeDataSource() {
        ITmfTrace trace = getTrace();
        initializeModule(trace);
        if (trace != null) {
            setData(TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID));
        }
    }

    @Override
    protected void updateData(final long start, final long end, int nb, @Nullable IProgressMonitor monitor) {
        // Third parameter is not used by implementation
        // Determine data that needs to be visible
        Collection<ISegment> data = fDisplayData;

        final int dataSize = (nb == 0) ? data.size() : nb;
        if (dataSize == 0 || end == start) {
            return;
        }

        final double[] xSeries = new double[dataSize];
        final double[] ySeries = new double[dataSize];
        // For each visible latency, add start time to x value and duration
        // for y value
        Iterator<ISegment> modelIter = data.iterator();
        long maxTempY = 1;
        for (int i = 0; i < dataSize; i++) {
            if (modelIter.hasNext()) {
                ISegment segment = modelIter.next();
                xSeries[i] = segment.getStart() - start;
                ySeries[i] = segment.getLength();
                maxTempY = Math.max(maxTempY, segment.getLength());
            }
        }
        final long maxY = maxTempY;
        setXAxis(xSeries);
        final Chart swtChart = getSwtChart();
        if (swtChart.isDisposed() || xSeries.length < 1) {
            return;
        }
        swtChart.updateLayout();
        setSeries(Messages.LatencyScatterGraphViewer_legend, ySeries); // $NON-NLS-1$
        final TmfChartTimeStampFormat tmfChartTimeStampFormat = new TmfChartTimeStampFormat(getTimeOffset());
        ILineSeries series = (ILineSeries) swtChart.getSeriesSet().getSeries(Messages.LatencyScatterGraphViewer_legend);
        if (series == null) {
            series = addSeries(Messages.LatencyScatterGraphViewer_legend);
        }
        series.setXSeries(xSeries);
        /* Find the minimal and maximum values in this series */
        series.setYSeries(ySeries);

        final IAxis xAxis = swtChart.getAxisSet().getXAxis(0);
        IAxisTick xTick = xAxis.getTick();
        xTick.setFormat(tmfChartTimeStampFormat);
        xAxis.setRange(new Range(0.0, end - start));
        xAxis.adjustRange();
        if (maxY > 0.0) {
            swtChart.getAxisSet().getYAxis(0).setRange(new Range(0.0, maxY));
        }
        swtChart.redraw();

        if (isSendTimeAlignSignals()) {
            // The width of the chart might have changed and its
            // time axis might be misaligned with the other views
            Point viewPos = LatencyScatterGraphViewer.this.getParent().getParent().toDisplay(0, 0);
            int axisPos = swtChart.toDisplay(0, 0).x + getPointAreaOffset();
            int timeAxisOffset = axisPos - viewPos.x;
            TmfTimeViewAlignmentInfo timeAlignmentInfo = new TmfTimeViewAlignmentInfo(getControl().getShell(), viewPos, timeAxisOffset);
            TmfSignalManager.dispatchSignal(new TmfTimeViewAlignmentSignal(LatencyScatterGraphViewer.this, timeAlignmentInfo, true));
        }
    }

    @Override
    protected void setWindowRange(final long windowStartTime, final long windowEndTime) {
        super.setWindowRange(windowStartTime, windowEndTime);
    }

    @Override
    protected ILineSeries addSeries(@Nullable String seriesName) {
        ISeriesSet seriesSet = getSwtChart().getSeriesSet();
        ILineSeries series = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, seriesName);
        series.setVisible(true);
        series.enableArea(false);
        series.setLineStyle(LineStyle.NONE);
        series.setSymbolType(PlotSymbolType.DIAMOND);
        return series;
    }

    /**
     * Set the data into the viewer. Will update model is analysis is completed
     * or run analysis if not completed
     *
     * @param analysis
     *            Latency analysis module
     */
    public void setData(@Nullable LatencyAnalysis analysis) {
        if (analysis == null) {
            updateModel(null);
            return;
        }
        ISegmentStore<ISegment> results = analysis.getResults();
        // If results are not null, then analysis is completed and model can be
        // updated
        if (results != null) {
            updateModel(results);
            setAnalysisModule(analysis);
            return;
        }
        updateModel(null);
        analysis.addListener(fListener);
        analysis.schedule();
        setAnalysisModule(analysis);
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
        if (signal == null) {
            return;
        }
        ITmfTrace trace = signal.getTrace();
        setTrace(trace);
        if (trace != null) {
            final TmfTimeRange timeRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
            setWindowRange(
                    timeRange.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue(),
                    timeRange.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue());
            setData(TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID));
            updateRange(timeRange);
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
        if (signal == null) {
            return;
        }
        ITmfTrace trace = signal.getTrace();
        setTrace(trace);
        if (trace != null) {
            final LatencyAnalysis analysisModuleOfClass = TmfTraceUtils.getAnalysisModuleOfClass(trace, LatencyAnalysis.class, LatencyAnalysis.ID);
            final TmfTimeRange timeRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
            setWindowRange(
                    getTimeInNanos(timeRange.getStartTime()),
                    getTimeInNanos(timeRange.getEndTime()));
            setData(analysisModuleOfClass);
        }

    }

    private void updateRange(final @Nullable TmfTimeRange timeRange) {
        Job compactingJob = fCompactingJob;
        if (compactingJob != null && compactingJob.getState() == Job.RUNNING) {
            compactingJob.cancel();
        }
        compactingJob = new CompactingLatencyQuery(NonNullUtils.checkNotNull(timeRange));
        fCompactingJob = compactingJob;
        compactingJob.schedule();
    }

    /**
     * @param signal
     *            Signal received when last opened trace is closed
     */
    @Override
    @TmfSignalHandler
    public void traceClosed(@Nullable TmfTraceClosedSignal signal) {
        super.traceClosed(signal);
        if (signal != null) {
            // Check if there is no more opened trace
            if (TmfTraceManager.getInstance().getActiveTrace() == null) {
                LatencyAnalysis analysis = getAnalysisModule();
                if (analysis != null) {
                    // TODO remove listener analysis.
                }
                clearContent();
            }
        }
        refresh();
    }

    /**
     * @param signal
     *            Signal received when window range is updated
     */
    @Override
    @TmfSignalHandler
    public void windowRangeUpdated(@Nullable TmfWindowRangeUpdatedSignal signal) {
        super.windowRangeUpdated(signal);
        if (signal == null) {
            return;
        }
        if (getTrace() != null) {
            final TmfTimeRange currentRange = signal.getCurrentRange();
            updateRange(currentRange);
        } else {
            Activator.getDefault().logInfo("No Trace to update"); //$NON-NLS-1$
        }
    }

    private @Nullable LatencyAnalysis getAnalysisModule() {
        return fAnalysisModule;
    }

    private void setAnalysisModule(LatencyAnalysis analysisModule) {
        fAnalysisModule = analysisModule;
    }

    private static long getTimeInNanos(final ITmfTimestamp currentTime) {
        return currentTime.normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
    }
}