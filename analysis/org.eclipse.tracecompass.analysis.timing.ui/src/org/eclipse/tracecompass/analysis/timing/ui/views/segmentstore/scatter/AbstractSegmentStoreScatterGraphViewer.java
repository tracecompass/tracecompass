/******************************************************************************
 * Copyright (c) 2015, 2016 Ericsson
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.timing.ui.Activator;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.scatter.Messages;
import org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.scatter.SegmentStoreScatterGraphTooltipProvider;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.IYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;

import com.google.common.primitives.Doubles;

/**
 * Displays the segment store provider data in a scatter graph
 *
 * @author France Lapointe Nguyen
 * @author Matthew Khouzam - reduced memory usage
 * @since 2.0
 */
public abstract class AbstractSegmentStoreScatterGraphViewer extends TmfCommonXLineChartViewer {

    private static final Format FORMAT = new SubSecondTimeWithUnitFormat();

    private final AtomicInteger fDirty = new AtomicInteger();

    private static final int UNKNOWN_SIZE = -1;

    private final class CompactingSegmentStoreQuery extends Job {
        private static final long MAX_POINTS = 1000;
        private final long fStart;
        private final long fEnd;

        private CompactingSegmentStoreQuery(long start, long end) {
            super(Messages.SegmentStoreScatterGraphViewer_compactTitle);
            fStart = start;
            fEnd = end;
        }

        @Override
        protected IStatus run(@Nullable IProgressMonitor monitor) {
            final IProgressMonitor statusMonitor = monitor;
            try {
                if (statusMonitor == null) {
                    return new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Monitor is null"); //$NON-NLS-1$
                }

                ISegmentStoreProvider segmentProvider = getSegmentProvider();
                final long startTime = fStart;
                final long endTime = fEnd;
                if (segmentProvider == null) {
                    redraw(statusMonitor, startTime, startTime, Collections.emptyList());
                    return new Status(IStatus.WARNING, Activator.PLUGIN_ID, "segment provider not available"); //$NON-NLS-1$
                }

                final ISegmentStore<ISegment> segStore = segmentProvider.getSegmentStore();
                if (segStore == null) {
                    redraw(statusMonitor, startTime, startTime, Collections.emptyList());
                    return new Status(IStatus.INFO, Activator.PLUGIN_ID, "Segment provider does not have segments"); //$NON-NLS-1$
                }

                fPixelStart = startTime;
                fPixelSize = Math.max(1, (endTime - startTime) / MAX_POINTS);
                final Iterable<ISegment> intersectingElements = segStore.getIntersectingElements(startTime, endTime, SegmentComparators.INTERVAL_START_COMPARATOR);
                final Iterable<ISegment> displayData = compactList(startTime, intersectingElements);

                redraw(statusMonitor, startTime, endTime, displayData);

                if (statusMonitor.isCanceled()) {
                    return Status.CANCEL_STATUS;
                }
                return Status.OK_STATUS;
            } finally {
                /*
                 * fDirty should have been incremented before creating a job, so
                 * we decrement it once the job is done
                 */
                fDirty.decrementAndGet();
            }

        }

        private void redraw(final IProgressMonitor statusMonitor, final long startTime, final long endTime, final Iterable<@NonNull ISegment> displayData) {
            fDisplayData = displayData;
            /*
             * Increment at every redraw, since the content of the view is not
             * current
             */
            fDirty.incrementAndGet();
            Display.getDefault().asyncExec(new Runnable() {

                @Override
                public void run() {
                    try {
                        updateData(startTime, endTime, UNKNOWN_SIZE, statusMonitor);
                    } finally {
                        /* Decrement once the redraw is done */
                        fDirty.decrementAndGet();
                    }
                }
            });
        }

        private Iterable<ISegment> compactList(final long startTime, final Iterable<@NonNull ISegment> iterableToCompact) {

            return new Iterable<@NonNull ISegment>() {

                @Override
                public Iterator<ISegment> iterator() {

                    return new Iterator<@NonNull ISegment>() {

                        private @Nullable ISegment fLast = null;
                        private @Nullable ISegment fNext = null;
                        private Iterator<@NonNull ISegment> fIterator = iterableToCompact.iterator();

                        @Override
                        public @NonNull ISegment next() {
                            /* hasNext implies next != null */
                            if (hasNext()) {
                                fLast = fNext;
                                fNext = null;
                                return Objects.requireNonNull(fLast);
                            }
                            throw new NoSuchElementException();
                        }

                        @Override
                        public boolean hasNext() {
                            if (fLast == null) {
                                // iteration hasn't started yet.
                                if (fIterator.hasNext()) {
                                    fLast = fIterator.next();
                                    if (fLast.getStart() >= startTime) {
                                        fNext = fLast;
                                    }
                                } else {
                                    return false;
                                }
                            }

                            // clear warning in calling overlaps below.
                            ISegment prev = fLast;
                            while (fNext == null && fIterator.hasNext()) {
                                ISegment tmp = fIterator.next();
                                if (tmp.getStart() >= startTime && !overlaps(prev, tmp)) {
                                    fNext = tmp;
                                }
                            }
                            return fNext != null;
                        }
                    };
                }
            };
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
     * Listener to update the model with the segment store provider results once
     * its segment store is fully completed
     */
    private final class SegmentStoreProviderProgressListener implements IAnalysisProgressListener {

        @Override
        public void onComplete(ISegmentStoreProvider segmentProvider, ISegmentStore<ISegment> segmentStore) {
            // Only update the model if trace that was analyzed is active trace
            if (segmentProvider.equals(getSegmentProvider())) {
                updateModel(segmentStore);
            }
        }
    }

    private long fPixelSize = -1;

    private long fPixelStart = 0;
    /**
     * Data to display
     */
    private Iterable<@NonNull ISegment> fDisplayData = Collections.emptyList();

    /**
     * Provider completion listener
     */
    private SegmentStoreProviderProgressListener fListener;

    /**
     * Current segment provider
     */
    private @Nullable ISegmentStoreProvider fSegmentProvider;

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
    public AbstractSegmentStoreScatterGraphViewer(Composite parent, String title, String xLabel, String yLabel) {
        super(parent, title, xLabel, yLabel);
        setTooltipProvider(new SegmentStoreScatterGraphTooltipProvider(this));
        fListener = new SegmentStoreProviderProgressListener();
        ITmfTrace trace = TmfTraceManager.getInstance().getActiveTrace();
        initializeProvider(trace);
        getSwtChart().getLegend().setVisible(false);
        getSwtChart().getAxisSet().getYAxis(0).getTick().setFormat(FORMAT);
    }

    private final void initializeProvider(@Nullable ITmfTrace trace) {
        if (trace != null) {
            final ISegmentStoreProvider segmentStoreProvider = getSegmentStoreProvider(trace);
            if (segmentStoreProvider != null) {
                segmentStoreProvider.addListener(fListener);
                setData(segmentStoreProvider);
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
        long currentStart = currentRange.getStartTime().toNanos();
        long currentEnd = currentRange.getEndTime().toNanos();
        if (dataInput == null) {
            if (!getDisplay().isDisposed()) {
                Display.getDefault().syncExec(new Runnable() {
                    @Override
                    public void run() {
                        clearContent();
                    }
                });
            }
            fDisplayData = Collections.emptyList();
        }
        setWindowRange(currentStart, currentEnd);
        updateContent();
    }

    @Override
    protected void initializeDataSource() {
        ITmfTrace trace = getTrace();
        initializeProvider(trace);
        if (trace != null) {
            setData(getSegmentStoreProvider(trace));
        }
    }

    @Override
    protected void updateData(final long start, final long end, int nb, @Nullable IProgressMonitor monitor) {
        if (end == start) {
            return;
        }
        // Determine data that needs to be visible
        List<Double> xSeries = nb != UNKNOWN_SIZE ? new ArrayList<>(nb) : new ArrayList<>();
        List<Double> ySeries = nb != UNKNOWN_SIZE ? new ArrayList<>(nb) : new ArrayList<>();
        // For each visible segments, add start time to x value and duration
        // for y value
        for (ISegment segment : fDisplayData) {
            if (monitor != null && monitor.isCanceled()) {
                return;
            }
            xSeries.add((double) (segment.getStart() - start));
            ySeries.add((double) segment.getLength());
        }
        setXAxis(Doubles.toArray(xSeries));
        setSeries(Messages.SegmentStoreScatterGraphViewer_legend, Doubles.toArray(ySeries));
        updateDisplay();
    }

    @Override
    protected void setWindowRange(final long windowStartTime, final long windowEndTime) {
        super.setWindowRange(windowStartTime, windowEndTime);
    }

    /**
     * Set the data into the viewer. If the provider is an analysis, it will
     * update the model if the analysis is completed or run the analysis if not
     * completed
     *
     * @param provider
     *            Segment store provider
     */
    public void setData(@Nullable ISegmentStoreProvider provider) {
        if (provider == null) {
            updateModel(null);
            return;
        }
        ISegmentStore<ISegment> segStore = provider.getSegmentStore();
        // If results are not null, then segment store is completed and model
        // can be updated
        if (segStore != null) {
            updateModel(segStore);
            setSegmentProvider(provider);
            return;
        }
        updateModel(null);
        provider.addListener(fListener);
        if (provider instanceof IAnalysisModule) {
            ((IAnalysisModule) provider).schedule();
        }
        setSegmentProvider(provider);
    }

    /**
     * Returns the segment store provider
     *
     * @param trace
     *            The trace to consider
     * @return the segment store provider
     */
    protected @Nullable abstract ISegmentStoreProvider getSegmentStoreProvider(ITmfTrace trace);

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
                    timeRange.getStartTime().toNanos(),
                    timeRange.getEndTime().toNanos());
            setData(getSegmentStoreProvider(trace));
            updateContent();
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

            final ISegmentStoreProvider segmentStoreProvider = getSegmentStoreProvider(trace);
            final TmfTimeRange timeRange = TmfTraceManager.getInstance().getCurrentTraceContext().getWindowRange();
            setWindowRange(
                    timeRange.getStartTime().toNanos(),
                    timeRange.getEndTime().toNanos());
            setData(segmentStoreProvider);
        }

    }

    @Override
    protected void updateContent() {
        /*
         * Update is requested, content is not up to date, fDirty will be
         * decremented in the compacting job
         */
        fDirty.incrementAndGet();
        Job compactingJob = fCompactingJob;
        if (compactingJob != null && compactingJob.getState() == Job.RUNNING) {
            compactingJob.cancel();
        }
        compactingJob = new CompactingSegmentStoreQuery(getWindowStartTime(), getWindowEndTime());
        fCompactingJob = compactingJob;
        compactingJob.schedule();
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
        if (signal != null) {
            // Check if there is no more opened trace
            if (TmfTraceManager.getInstance().getActiveTrace() == null) {
                ISegmentStoreProvider provider = getSegmentProvider();
                if (provider != null) {
                    provider.removeListener(fListener);
                }
                clearContent();
            }
        }
        refresh();
    }

    private @Nullable ISegmentStoreProvider getSegmentProvider() {
        return fSegmentProvider;
    }

    private void setSegmentProvider(ISegmentStoreProvider provider) {
        fSegmentProvider = provider;
    }

    @Override
    public boolean isDirty() {
        /* Check the parent's or this view's own dirtiness */
        return super.isDirty() || (fDirty.get() != 0);
    }
}