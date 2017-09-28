/**********************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IXYPresentationProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.RGBColor;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.XYPresentationProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfChartTimeStampFormat;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.LineStyle;
import org.swtchart.Range;

/**
 * XY Chart viewer class implementation. All series in this viewer use the same
 * X axis values. They are automatically created as values are provided for a
 * key. Series by default will be displayed as a line. Each series appearance
 * can be overridden when creating it.
 *
 * @author Yonni Chen
 * @since 3.1
 */
public abstract class TmfCommonXAxisChartViewer extends TmfXYChartViewer {

    private static final String DIRTY_UNDERFLOW_ERROR = "Dirty underflow error"; //$NON-NLS-1$

    private static final double DEFAULT_MAXY = Double.MIN_VALUE;
    private static final double DEFAULT_MINY = Double.MAX_VALUE;

    /** Timeout between updates in the updateData thread **/
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(TmfCommonXAxisChartViewer.class);
    private static final int DEFAULT_SERIES_WIDTH = 1;

    private final double fResolution;
    private final AtomicInteger fDirty = new AtomicInteger();
    private final Map<ITmfTrace, IXYPresentationProvider> fXYPresentationProvider;
    private ITmfXYDataProvider fXYDataProvider;
    private UpdateThread fUpdateThread;

    /** Used for testing **/
    private int fOverrideNbPoints = 0;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public TmfCommonXAxisChartViewer(Composite parent, TmfXYChartSettings settings) {
        super(parent, settings.getTitle(), settings.getXLabel(), settings.getYLabel());
        getSwtChart().getTitle().setVisible(false);
        getSwtChart().getLegend().setPosition(SWT.BOTTOM);
        getSwtChart().getAxisSet().getXAxes()[0].getTitle().setVisible(false);
        fResolution = settings.getResolution();
        setTooltipProvider(new TmfCommonXLineChartTooltipProvider(this));
        fXYPresentationProvider = new HashMap<>();
    }

    @Override
    public void loadTrace(ITmfTrace trace) {
        super.loadTrace(trace);
        fXYPresentationProvider.putIfAbsent(trace, new XYPresentationProvider());
        initializeDataProvider();
    }

    @Override
    public boolean isDirty() {
        /* Check the parent's or this view's own dirtiness */
        return super.isDirty() || (fDirty.get() != 0);
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
        updateContent();
    }

    /**
     * Initialize the data provider of this viewer
     */
    protected abstract void initializeDataProvider();

    /**
     * Gets the presentation provider
     *
     * @return The presentation provider
     */
    protected IXYPresentationProvider getPresentationProvider() {
        return Objects.requireNonNull(fXYPresentationProvider.get(getTrace()));
    }

    /**
     * Set the data provider
     *
     * @param dataProvider
     *            A data provider used for fetching a XY Model
     */
    protected void setDataProvider(ITmfXYDataProvider dataProvider) {
        fXYDataProvider = dataProvider;
    }

    /**
     * Create an instance of {@link TimeQueryFilter} that will be used by updateData
     * method. If a viewer need a more specialized instance of
     * {@link TimeQueryFilter}, it's its responsibility to override this method and
     * provide the desired instance.
     *
     * @param start
     *            The starting value
     * @param end
     *            The ending value
     * @param nb
     *            The number of entries
     * @return An {@link TimeQueryFilter} instance that data provider will use to
     *         extract a model
     */
    protected @NonNull TimeQueryFilter createQueryFilter(long start, long end, int nb) {
        return new TimeQueryFilter(start, end, nb);
    }

    /**
     * Gets the appearance of a given series. If appearance doesn't exist, a new one
     * will be created by the presentation provider
     *
     * @param seriesName
     *            The name of the series
     * @return An {@link IYAppearance} instance for the series
     */
    public @NonNull IYAppearance getSeriesAppearance(@NonNull String seriesName) {
        return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.LINE, DEFAULT_SERIES_WIDTH);
    }

    /**
     * Cancels the currently running update thread. It is automatically called when
     * the content is updated, but child viewers may want to call it manually to do
     * some operations before calling
     * {@link TmfCommonXAxisChartViewer#updateContent}
     */
    protected synchronized void cancelUpdate() {
        if (fUpdateThread != null) {
            fUpdateThread.cancel();
        }
    }

    @Override
    protected void updateContent() {
        cancelUpdate();
        try (FlowScopeLog parentScope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:ContentUpdateRequested").setCategory(getViewerId()).build()) { //$NON-NLS-1$
            /*
             * Content is not up to date, so we increment fDirty. It will be decremented at
             * the end of the update thread
             */
            fDirty.incrementAndGet();
            getDisplay().asyncExec(() -> {
                try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:CreatingUpdateThread").setParentScope(parentScope).build()) { //$NON-NLS-1$
                    newUpdateThread(scope);
                }
            });
        }
    }

    @Override
    protected void clearContent() {
        getSwtChart().getAxisSet().getXAxis(0).getTick().setFormat(null);
        super.clearContent();
    }

    private @NonNull String getViewerId() {
        return getClass().getName();
    }

    private class UpdateThread extends Thread {

        private final IProgressMonitor fMonitor;
        private final int fNumRequests;
        private final @NonNull FlowScopeLog fScope;
        private final ITmfXYDataProvider fDataProvider;

        public UpdateThread(int numRequests, @NonNull FlowScopeLog log, ITmfXYDataProvider dataProvider) {
            super("Line chart update"); //$NON-NLS-1$
            fNumRequests = numRequests;
            fMonitor = new NullProgressMonitor();
            fScope = log;
            fDataProvider = dataProvider;
        }

        @Override
        public void run() {
            try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:UpdateThread", "numRequests=", fNumRequests).setParentScope(fScope).build()) { //$NON-NLS-1$ //$NON-NLS-2$
                try {
                    TimeQueryFilter filter = createQueryFilter(getWindowStartTime(), getWindowEndTime(), fNumRequests);
                    updateData(filter, fMonitor);
                } finally {
                    /*
                     * fDirty should have been incremented before creating the thread, so we
                     * decrement it once it is finished
                     */
                    if (fDirty.decrementAndGet() < 0) {
                        Activator.getDefault().logError(DIRTY_UNDERFLOW_ERROR, new Throwable());
                    }
                }
                updateThreadFinished(this);
            }
        }

        public void cancel() {
            TraceCompassLogUtils.traceInstant(LOGGER, Level.FINE, "CommonXLineChart:UpdateThreadCanceled"); //$NON-NLS-1$
            fMonitor.setCanceled(true);
        }

        /**
         * This method is responsible for calling the
         * {@link UpdateThread#updateDisplay(ITmfCommonXAxisModel)} when needed for the
         * new values to be displayed.
         *
         * @param filters
         *            A query filter
         * @param monitor
         *            A monitor for canceling task
         */
        private void updateData(@NonNull TimeQueryFilter filters, IProgressMonitor monitor) {
            if (fDataProvider == null) {
                TraceCompassLogUtils.traceInstant(LOGGER, Level.WARNING, "Data provider for this viewer is not available"); //$NON-NLS-1$
                return;
            }

            boolean isComplete = false;
            do {
                TmfModelResponse<ITmfCommonXAxisModel> response = fDataProvider.fetchXY(filters, monitor);
                ITmfCommonXAxisModel model = response.getModel();
                if (model != null) {
                    updateDisplay(model, monitor);
                }

                ITmfResponse.Status status = response.getStatus();
                if (status == ITmfResponse.Status.COMPLETED) {
                    /* Model is complete, no need to request again the data provider */
                    isComplete = true;
                } else if (status == ITmfResponse.Status.FAILED || status == ITmfResponse.Status.CANCELLED) {
                    /* Error occurred, log and return */
                    TraceCompassLogUtils.traceInstant(LOGGER, Level.WARNING, response.getStatusMessage());
                    isComplete = true;
                } else {
                    /**
                     * Status is RUNNING. Sleeping current thread to wait before request data
                     * provider again
                     **/
                    try {
                        Thread.sleep(BUILD_UPDATE_TIMEOUT);
                    } catch (InterruptedException e) {
                        /**
                         * InterruptedException is throw by Thread.Sleep and we should retry querying
                         * the data provider
                         **/
                        TraceCompassLogUtils.traceInstant(LOGGER, Level.INFO, e.getMessage());
                    }
                }
            } while (!isComplete);
        }

        /**
         * Update the chart's values before refreshing the viewer
         */
        private void updateDisplay(ITmfCommonXAxisModel model, IProgressMonitor monitor) {
            try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "TmfCommonXAxisChart:UpdateDisplayRequested").setCategory(getViewerId()).build()) { //$NON-NLS-1$
                /* Content is not up to date, increment dirtiness */
                final ITmfCommonXAxisModel seriesValues = model;
                fDirty.incrementAndGet();

                Display.getDefault().asyncExec(() -> {
                    final TmfChartTimeStampFormat tmfChartTimeStampFormat = new TmfChartTimeStampFormat(getTimeOffset());
                    try (FlowScopeLog log = new FlowScopeLogBuilder(LOGGER, Level.FINE, "TmfCommonXAxisChart:UpdateDisplay").setParentScope(scope).build()) { //$NON-NLS-1$
                        if (getSwtChart().isDisposed()) {
                            return;
                        }
                        if (monitor != null && monitor.isCanceled()) {
                            return;
                        }
                        double maxy = DEFAULT_MAXY;
                        double miny = DEFAULT_MINY;
                        long delta = getWindowEndTime() - getWindowStartTime();
                        if (delta > 0) {
                            for (IYModel entry : seriesValues.getYData().values()) {
                                ISeries series = getSwtChart().getSeriesSet().getSeries(entry.getName());
                                if (series == null) {
                                    series = createSWTSeriesFromModel(entry);
                                }
                                series.setXSeries(extractXValuesToDisplay(seriesValues));
                                /*
                                 * Find the minimal and maximum values in this series
                                 */
                                for (double value : entry.getData()) {
                                    maxy = Math.max(maxy, value);
                                    miny = Math.min(miny, value);
                                }

                                series.setYSeries(entry.getData());
                            }
                            maxy = maxy == DEFAULT_MAXY ? 1.0 : maxy;
                        } else {
                            clearContent();
                            delta = 1;
                        }

                        IAxisTick xTick = getSwtChart().getAxisSet().getXAxis(0).getTick();
                        xTick.setFormat(tmfChartTimeStampFormat);
                        final double start = 1.0;
                        getSwtChart().getAxisSet().getXAxis(0).setRange(new Range(start, start + delta));
                        if (maxy > miny) {
                            getSwtChart().getAxisSet().getYAxis(0).setRange(new Range(miny, maxy));
                        }
                        getSwtChart().redraw();

                        if (isSendTimeAlignSignals()) {
                            /*
                             * The width of the chart might have changed and its time axis might be
                             * misaligned with the other views
                             */
                            Point viewPos = TmfCommonXAxisChartViewer.this.getParent().getParent().toDisplay(0, 0);
                            int axisPos = getSwtChart().toDisplay(0, 0).x + getPointAreaOffset();
                            int timeAxisOffset = axisPos - viewPos.x;
                            TmfTimeViewAlignmentInfo timeAlignmentInfo = new TmfTimeViewAlignmentInfo(getControl().getShell(), viewPos, timeAxisOffset);
                            TmfSignalManager.dispatchSignal(new TmfTimeViewAlignmentSignal(TmfCommonXAxisChartViewer.this, timeAlignmentInfo, true));
                        }

                    } finally {
                        /* Content has been updated, decrement dirtiness */
                        if (fDirty.decrementAndGet() < 0) {
                            Activator.getDefault().logError(DIRTY_UNDERFLOW_ERROR, new Throwable());
                        }
                    }
                });
            }
        }

        /**
         * Since the XY Model returned by data provider contains directly the requested
         * time as long array, we need to convert it to double array for the SWT Chart.
         */
        private double[] extractXValuesToDisplay(ITmfCommonXAxisModel model) {
            long[] xValuesRequested = model.getXAxis();
            double[] xValuesToDisplay = new double[xValuesRequested.length];
            long offset = getTimeOffset();

            for (int i = 0; i < xValuesRequested.length; ++i) {
                xValuesToDisplay[i] = (xValuesRequested[i] - offset);
            }
            return xValuesToDisplay;
        }

        private @NonNull ISeries createSWTSeriesFromModel(IYModel yModel) {
            ISeriesSet seriesSet = getSwtChart().getSeriesSet();

            String seriesName = yModel.getName();
            IYAppearance appearance = getSeriesAppearance(seriesName);

            String type = appearance.getType();
            RGBColor rgb = appearance.getColor();
            Color color = new Color(Display.getDefault(), rgb.getRed(), rgb.getGreen(), rgb.getBlue());

            if (type.equals(IYAppearance.Type.BAR)) {
                IBarSeries barSeries = (IBarSeries) seriesSet.createSeries(SeriesType.BAR, seriesName);
                barSeries.enableStack(true);
                barSeries.setBarColor(color);
                barSeries.setBarPadding(0);
                barSeries.setVisible(true);
                return barSeries;
            }

            /**
             * Default is line chart
             */
            ILineSeries lineSeries = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, seriesName);
            boolean isScatter = IYAppearance.Type.SCATTER.equals(type);
            lineSeries.enableArea(IYAppearance.Type.AREA.equals(type));
            lineSeries.setLineStyle(LineStyle.valueOf(appearance.getStyle()));
            lineSeries.setSymbolType(isScatter ? PlotSymbolType.DIAMOND : PlotSymbolType.NONE);
            lineSeries.setLineColor(color);
            lineSeries.setVisible(true);
            lineSeries.setLineWidth(appearance.getWidth());
            return lineSeries;
        }

        private synchronized void updateThreadFinished(UpdateThread thread) {
            if (thread == fUpdateThread) {
                fUpdateThread = null;
            }
        }
    }

    private synchronized void newUpdateThread(@NonNull FlowScopeLog fScope) {
        if (getSwtChart().isDisposed()) {
            return;
        }
        int numRequests = fOverrideNbPoints != 0 ? fOverrideNbPoints : (int) Math.min(getWindowEndTime() - getWindowStartTime() + 1, (long) (getSwtChart().getPlotArea().getBounds().width * fResolution));
        fUpdateThread = new UpdateThread(numRequests, fScope, fXYDataProvider);
        fUpdateThread.start();
    }

    @TmfSignalHandler
    @Override
    public void traceClosed(@Nullable TmfTraceClosedSignal signal) {
        cancelUpdate();
        super.traceClosed(signal);
        setDataProvider(null);
        if (signal != null) {
            fXYPresentationProvider.remove(signal.getTrace());
        }
    }
}
