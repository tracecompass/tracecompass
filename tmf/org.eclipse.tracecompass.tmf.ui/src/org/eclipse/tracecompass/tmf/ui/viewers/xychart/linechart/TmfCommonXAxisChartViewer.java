/**********************************************************************
 * Copyright (c) 2017, 2020 Ericsson, Draeger, Auriga and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart;

import java.text.Format;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swtchart.IAxisSet;
import org.eclipse.swtchart.IAxisTick;
import org.eclipse.swtchart.IBarSeries;
import org.eclipse.swtchart.ILineSeries;
import org.eclipse.swtchart.ISeries;
import org.eclipse.swtchart.ISeries.SeriesType;
import org.eclipse.swtchart.ISeriesSet;
import org.eclipse.swtchart.ITitle;
import org.eclipse.swtchart.LineStyle;
import org.eclipse.swtchart.Range;
import org.eclipse.swtchart.model.DoubleArraySeriesModel;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TmfFilterAppliedSignal;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TraceCompassFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.ui.viewers.xychart.BaseXYPresentationProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.filters.FetchParametersUtils;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.dataprovider.DataProviderParameterUtils;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.tmf.core.model.timegraph.IFilterProperty;
import org.eclipse.tracecompass.tmf.core.model.xy.ISeriesModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfXyModel;
import org.eclipse.tracecompass.tmf.core.model.xy.TmfXYAxisDescription;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.response.ITmfResponse;
import org.eclipse.tracecompass.tmf.core.response.TmfModelResponse;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.colors.RGBAUtil;
import org.eclipse.tracecompass.tmf.ui.model.DataTypeUtils;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentInfo;
import org.eclipse.tracecompass.tmf.ui.signal.TmfTimeViewAlignmentSignal;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.AxisRange;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfChartTimeStampFormat;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import com.google.common.collect.Multimap;

/**
 * XY Chart viewer class implementation. All series in this viewer use the same
 * X axis values. They are automatically created as values are provided for a
 * key. Series by default will be displayed as a line. Each series appearance
 * can be overridden when creating it.
 *
 * @author Yonni Chen
 * @since 6.0
 */
public abstract class TmfCommonXAxisChartViewer extends TmfXYChartViewer {

    private static final String DIRTY_UNDERFLOW_ERROR = "Dirty underflow error"; //$NON-NLS-1$
    private static final @NonNull RGBAColor DEFAULT_COLOR = new RGBAColor(255, 255, 255);
    private static final int DEFAULT_SYMBOL_SIZE = 4;

    private static final Map<String, ILineSeries.PlotSymbolType> SYMBOL_MAP;
    private static final ColorRegistry COLOR_REGISTRY = new ColorRegistry();

    static {
        ImmutableMap.Builder<String, ILineSeries.PlotSymbolType> builder = new Builder<>();
        builder.put(StyleProperties.SymbolType.NONE, ILineSeries.PlotSymbolType.NONE);
        builder.put(StyleProperties.SymbolType.CIRCLE, ILineSeries.PlotSymbolType.CIRCLE);
        builder.put(StyleProperties.SymbolType.CROSS, ILineSeries.PlotSymbolType.CROSS);
        builder.put(StyleProperties.SymbolType.DIAMOND, ILineSeries.PlotSymbolType.DIAMOND);
        builder.put(StyleProperties.SymbolType.INVERTED_TRIANGLE, ILineSeries.PlotSymbolType.INVERTED_TRIANGLE);
        builder.put(StyleProperties.SymbolType.TRIANGLE, ILineSeries.PlotSymbolType.TRIANGLE);
        builder.put(StyleProperties.SymbolType.PLUS, ILineSeries.PlotSymbolType.PLUS);
        builder.put(StyleProperties.SymbolType.SQUARE, ILineSeries.PlotSymbolType.SQUARE);
        SYMBOL_MAP = builder.build();
    }
    private static final double DEFAULT_MAXY = Double.MIN_VALUE;
    private static final double DEFAULT_MINY = Double.MAX_VALUE;

    /** Timeout between updates in the updateData thread **/
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(TmfCommonXAxisChartViewer.class);
    private static final int DEFAULT_SERIES_WIDTH = 1;
    private static final String DIMMED_SERIES_SUFFIX = ".dimmed"; //$NON-NLS-1$

    private final double fResolution;
    private final AtomicInteger fDirty = new AtomicInteger();
    private final Map<ITmfTrace, BaseXYPresentationProvider> fXYPresentationProvider;
    private UpdateThread fUpdateThread;

    /** Used for testing **/
    private int fOverrideNbPoints = 0;

    /**
     * Constructor
     *
     * @param parent
     *                     The parent composite
     * @param settings
     *                     See {@link TmfXYChartSettings} to know what it contains
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
        fXYPresentationProvider.computeIfAbsent(trace, t -> createPresentationProvider(trace));
    }

    /**
     * Create a new presentation provider for this XY viewer. Sub-classes can
     * overwrite this method to provide specific XY presentation provider
     * instances.
     *
     * @param trace
     *            The trace to get the provider for
     *
     * @return A new presentation provider
     */
    protected BaseXYPresentationProvider createPresentationProvider(ITmfTrace trace) {
        return new BaseXYPresentationProvider();
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
     *                     The number of points to display, cannot be negative. 0
     *                     means use native resolution. any positive integer means
     *                     that number of points
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
     *
     * @param trace
     *                  The trace
     * @return the data provider
     */
    protected abstract ITmfXYDataProvider initializeDataProvider(@NonNull ITmfTrace trace);

    /**
     * Gets the presentation provider
     *
     * @return The presentation provider
     */
    public BaseXYPresentationProvider getPresentationProvider() {
        return Objects.requireNonNull(fXYPresentationProvider.get(getTrace()));
    }

    /**
     * Create map of parameters that will be used by updateData method. If a
     * viewer need a more specialized map than just the time requested it's its
     * responsibility to override this method and provide the desired instance.
     *
     * @param start
     *            The starting value
     * @param end
     *            The ending value
     * @param nb
     *            The number of entries
     * @return Map of parameters
     */
    protected @NonNull Map<String, Object> createQueryParameters(long start, long end, int nb) {
        Map<@NonNull String, @NonNull Object> parameters = FetchParametersUtils.timeQueryToMap(new TimeQueryFilter(start, end, nb));
        Multimap<@NonNull Integer, @NonNull String> regexesMap = getRegexes();
        if (!regexesMap.isEmpty()) {
            parameters.put(DataProviderParameterUtils.REGEX_MAP_FILTERS_KEY, regexesMap.asMap());
        }
        return parameters;
    }

    /**
     * Gets the style of a given series. If style doesn't exist, a new one will
     * be created by the presentation provider
     *
     * @param seriesId
     *            The unique ID of the series
     * @return An {@link OutputElementStyle} instance for the series
     * @since 6.0
     */
    public @NonNull OutputElementStyle getSeriesStyle(@NonNull Long seriesId) {
        return getPresentationProvider().getSeriesStyle(seriesId, StyleProperties.SeriesType.LINE, DEFAULT_SERIES_WIDTH);
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
        ITmfTrace trace = getTrace();
        if (trace == null) {
            return;
        }
        cancelUpdate();
        try (FlowScopeLog parentScope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:ContentUpdateRequested").setCategory(getViewerId()).build()) { //$NON-NLS-1$
            /*
             * Content is not up to date, so we increment fDirty. It will be decremented at
             * the end of the update thread
             */
            fDirty.incrementAndGet();
            getDisplay().asyncExec(() -> {
                if (!trace.equals(getTrace())) {
                    return;
                }
                try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:CreatingUpdateThread").setParentScope(parentScope).build()) { //$NON-NLS-1$
                    newUpdateThread(trace, scope);
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

        private final ITmfTrace fTrace;
        private final IProgressMonitor fMonitor;
        private final int fNumRequests;
        private final @NonNull FlowScopeLog fScope;

        public UpdateThread(ITmfTrace trace, int numRequests, @NonNull FlowScopeLog log) {
            super("Line chart update"); //$NON-NLS-1$
            fTrace = trace;
            fNumRequests = numRequests;
            fMonitor = new NullProgressMonitor();
            fScope = log;
        }

        @Override
        public void run() {
            try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:UpdateThread", "numRequests=", fNumRequests).setParentScope(fScope).build()) { //$NON-NLS-1$ //$NON-NLS-2$
                ITmfXYDataProvider dataProvider = null;
                try (FlowScopeLog scopeDp = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:InitializeDataProvider").setParentScope(fScope).build()) { //$NON-NLS-1$
                    dataProvider = initializeDataProvider(fTrace);
                }
                if (dataProvider == null) {
                    TraceCompassLogUtils.traceInstant(LOGGER, Level.WARNING, "Data provider for this viewer is not available"); //$NON-NLS-1$
                    return;
                }
                try {
                    int numRequests = fNumRequests;
                    if (numRequests == 0) {
                        return;
                    }
                    Map<String, Object> parameters = createQueryParameters(getWindowStartTime(), getWindowEndTime(), numRequests);
                    updateData(dataProvider, parameters, fMonitor);
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
         * @param dataProvider
         *                         A data provider
         * @param parameters
         *                         A query filter
         * @param monitor
         *                         A monitor for canceling task
         */
        private void updateData(@NonNull ITmfXYDataProvider dataProvider, @NonNull Map<String, Object> parameters, IProgressMonitor monitor) {
            boolean isComplete = false;
            do {
                TmfModelResponse<ITmfXyModel> response = dataProvider.fetchXY(parameters, monitor);
                ITmfXyModel model = response.getModel();
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
                        Thread.currentThread().interrupt();
                    }
                }
            } while (!isComplete);
        }

        /**
         * Update the chart's values before refreshing the viewer
         */
        private void updateDisplay(ITmfXyModel model, IProgressMonitor monitor) {
            try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "TmfCommonXAxisChart:UpdateDisplayRequested").setCategory(getViewerId()).build()) { //$NON-NLS-1$
                /* Content is not up to date, increment dirtiness */
                final ITmfXyModel seriesValues = model;
                fDirty.incrementAndGet();

                Display.getDefault().asyncExec(() -> {
                    final TmfChartTimeStampFormat tmfChartTimeStampFormat = new TmfChartTimeStampFormat(getTimeOffset());
                    TmfXYAxisDescription xAxisDescription = null;
                    TmfXYAxisDescription yAxisDescription = null;
                    try (FlowScopeLog log = new FlowScopeLogBuilder(LOGGER, Level.FINE, "TmfCommonXAxisChart:UpdateDisplay").setParentScope(scope).build()) { //$NON-NLS-1$
                        if (!fTrace.equals(getTrace())) {
                            return;
                        }
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
                            for (ISeriesModel entry : seriesValues.getSeriesData()) {
                                double[] extractXValuesToDisplay = extractXValuesToDisplay(entry.getXAxis());
                                List<Double> dimmedX = new ArrayList<>(extractXValuesToDisplay.length);
                                List<Double> dimmedY = new ArrayList<>(extractXValuesToDisplay.length);
                                List<Double> brightX = new ArrayList<>(extractXValuesToDisplay.length);
                                List<Double> brightY = new ArrayList<>(extractXValuesToDisplay.length);

                                int[] propertiesArray = entry.getProperties();
                                double[] data = entry.getData();
                                for (int i = 0; i < extractXValuesToDisplay.length; i++) {
                                    double value = data[i];
                                    /*
                                     * Find the minimal and maximum values in this series
                                     */
                                    maxy = Math.max(maxy, value);
                                    miny = Math.min(miny, value);
                                    int properties = (i < propertiesArray.length) ? propertiesArray[i] : 0;
                                    if ((properties & IFilterProperty.EXCLUDE) == 0) {
                                        if ((properties & IFilterProperty.DIMMED) == 0) {
                                            brightX.add(extractXValuesToDisplay[i]);
                                            brightY.add(value);
                                        } else {
                                            dimmedX.add(extractXValuesToDisplay[i]);
                                            dimmedY.add(value);
                                        }
                                    }
                                }
                                double[] brightXArray = new double[brightX.size()];
                                double[] brightYArray = new double[brightY.size()];
                                for (int i = 0; i < brightX.size(); i++) {
                                    brightXArray[i] = brightX.get(i);
                                    brightYArray[i] = brightY.get(i);
                                }
                                double[] dimmedXArray = new double[dimmedX.size()];
                                double[] dimmedYArray = new double[dimmedY.size()];
                                for (int i = 0; i < dimmedX.size(); i++) {
                                    dimmedXArray[i] = dimmedX.get(i);
                                    dimmedYArray[i] = dimmedY.get(i);
                                }

                                // Get the x and y data types
                                if (xAxisDescription == null) {
                                    xAxisDescription = entry.getXAxisDescription();
                                }
                                if (yAxisDescription == null) {
                                    yAxisDescription = entry.getYAxisDescription();
                                }

                                // Create and fill the series
                                ISeriesSet seriesSet = getSwtChart().getSeriesSet();
                                ISeries<Integer> series = seriesSet.getSeries(entry.getName());
                                ISeries<Integer> dimmedSeries = seriesSet.getSeries(entry.getName() + DIMMED_SERIES_SUFFIX);
                                if (brightX.isEmpty()) {
                                    // Remove the base series since there is
                                    // nothing to show
                                    if (series != null) {
                                        seriesSet.deleteSeries(entry.getName());
                                    }
                                } else {
                                    if (series == null) {
                                        series = createSWTSeriesFromModel(entry);
                                    }
                                    series.setDataModel(new DoubleArraySeriesModel(brightXArray, brightYArray));
                                    enableStack(series, true);
                                }
                                if (dimmedX.isEmpty()) {
                                    // Remove the base series since there is
                                    // nothing to show
                                    if (dimmedSeries != null) {
                                        seriesSet.deleteSeries(entry.getName() + DIMMED_SERIES_SUFFIX);
                                    }
                                } else {
                                    if (dimmedSeries == null) {
                                        dimmedSeries = createDimmedSeriesFromModel(entry);
                                    }
                                    dimmedSeries.setDataModel(new DoubleArraySeriesModel(dimmedXArray, dimmedYArray));
                                    enableStack(dimmedSeries, true);
                                }
                            }
                            maxy = maxy == DEFAULT_MAXY ? 1.0 : maxy;
                        } else {
                            clearContent();
                            delta = 1;
                        }

                        // Set the formatters for the axis
                        IAxisSet axisSet = getSwtChart().getAxisSet();
                        IAxisTick xTick = axisSet.getXAxis(0).getTick();
                        Format xFormatter = xAxisDescription != null ? DataTypeUtils.getFormat(xAxisDescription.getDataType(), xAxisDescription.getUnit()) : null;
                        xTick.setFormat(xFormatter == null ? tmfChartTimeStampFormat : xFormatter);
                        if (yAxisDescription != null) {
                            Format format = axisSet.getYAxis(0).getTick().getFormat();
                            if (format == null) {
                                axisSet.getYAxis(0).getTick().setFormat(DataTypeUtils.getFormat(yAxisDescription.getDataType(), yAxisDescription.getUnit()));
                            }
                            ITitle title = axisSet.getYAxis(0).getTitle();
                            // Set the Y title if it was not previously set (ie it is invisible)
                            if (!title.isVisible()) {
                                title.setText(yAxisDescription.getLabel());
                                title.setVisible(true);
                            }
                        }

                        final double start = 1.0;
                        axisSet.getXAxis(0).setRange(new Range(start, start + delta));
                        AxisRange fixedYRange = getFixedYRange();
                        if (fixedYRange == null) {
                            axisSet.getYAxis(0).adjustRange();
                        } else {
                            axisSet.getYAxis(0).setRange(
                                    new Range(fixedYRange.getLower(), fixedYRange.getUpper()));
                        }
                        getSwtChart().redraw();

                        if (isSendTimeAlignSignals()) {
                            /*
                             * The width of the chart might have changed and its time axis might be
                             * misaligned with the other views
                             */
                            Composite parent = TmfCommonXAxisChartViewer.this.getParent();
                            if (parent == null || parent.getParent() == null) {
                                return;
                            }
                            Point viewPos = parent.getParent().toDisplay(0, 0);
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
        private double[] extractXValuesToDisplay(long[] xValuesRequested) {
            double[] xValuesToDisplay = new double[xValuesRequested.length];
            long offset = getTimeOffset();

            for (int i = 0; i < xValuesRequested.length; ++i) {
                xValuesToDisplay[i] = (xValuesRequested[i] - offset);
            }
            return xValuesToDisplay;
        }

        private @NonNull ISeries<Integer> createSWTSeriesFromModel(ISeriesModel yModel) {
            ISeriesSet seriesSet = getSwtChart().getSeriesSet();

            String seriesName = yModel.getName();
            OutputElementStyle appearance = getSeriesStyle(yModel.getId());
            BaseXYPresentationProvider presProvider = getPresentationProvider();

            String type = (String) presProvider.getStyleOrDefault(appearance, StyleProperties.SERIES_TYPE, StyleProperties.SeriesType.LINE);
            RGBAColor rgb = presProvider.getColorStyleOrDefault(appearance, StyleProperties.COLOR, DEFAULT_COLOR);
            COLOR_REGISTRY.put(rgb.toString(), RGBAUtil.fromRGBAColor(rgb).rgb);
            Color color = COLOR_REGISTRY.get(rgb.toString());
            String symbolType = (String) presProvider.getStyle(appearance, StyleProperties.SYMBOL_TYPE);

            if (type.equals(StyleProperties.SeriesType.BAR)) {
                IBarSeries<Integer> barSeries = (IBarSeries<Integer>) seriesSet.createSeries(SeriesType.BAR, seriesName);
                barSeries.setBarColor(color);
                barSeries.setBarPadding(0);
                barSeries.setVisible(true);
                return barSeries;
            }

            /**
             * Default is line chart
             */
            ILineSeries<Integer> lineSeries = (ILineSeries<Integer>) seriesSet.createSeries(SeriesType.LINE, seriesName);
            lineSeries.enableArea(StyleProperties.SeriesType.AREA.equals(type));
            lineSeries.setLineStyle(LineStyle.valueOf((String) presProvider.getStyle(appearance, StyleProperties.SERIES_STYLE)));
            lineSeries.setSymbolType(SYMBOL_MAP.getOrDefault(symbolType, ILineSeries.PlotSymbolType.NONE));
            lineSeries.setSymbolSize(Math.round(presProvider.getFloatStyleOrDefault(appearance, StyleProperties.HEIGHT, 1.0f).floatValue() * DEFAULT_SYMBOL_SIZE));
            lineSeries.setLineColor(color);
            lineSeries.setSymbolColor(color);
            lineSeries.setVisible(true);
            lineSeries.setLineWidth(((Number) presProvider.getFloatStyleOrDefault(appearance, StyleProperties.WIDTH, 1.0f)).intValue());
            return lineSeries;
        }

        private @NonNull ISeries<Integer> createDimmedSeriesFromModel(ISeriesModel yModel) {
            ISeriesSet seriesSet = getSwtChart().getSeriesSet();

            String seriesName = yModel.getName() + DIMMED_SERIES_SUFFIX;
            OutputElementStyle appearance = getSeriesStyle(yModel.getId());
            BaseXYPresentationProvider presProvider = getPresentationProvider();

            String type = (String) presProvider.getStyleOrDefault(appearance, StyleProperties.SERIES_TYPE, StyleProperties.SeriesType.LINE);
            RGBAColor rgbaColor = presProvider.getColorStyleOrDefault(appearance, StyleProperties.COLOR, DEFAULT_COLOR);
            float[] rgb = rgbaColor.getHSBA();
            COLOR_REGISTRY.put(rgb.toString(), new RGBA(rgb[0], rgb[1] * 0.5f, rgb[2] * 0.5f, rgb[3]).rgb);
            Color color = COLOR_REGISTRY.get(rgb.toString());
            String symbolType = (String) presProvider.getStyle(appearance, StyleProperties.SYMBOL_TYPE);

            if (type.equals(IYAppearance.Type.BAR)) {
                IBarSeries<Integer> barSeries = (IBarSeries<Integer>) seriesSet.createSeries(SeriesType.BAR, seriesName);
                barSeries.setBarColor(color);
                barSeries.setBarPadding(0);
                barSeries.setVisible(true);
                return barSeries;
            }

            /**
             * Default is line chart
             */
            ILineSeries<Integer> lineSeries = (ILineSeries<Integer>) seriesSet.createSeries(SeriesType.LINE, seriesName);
            lineSeries.enableArea(StyleProperties.SeriesType.AREA.equals(type));
            lineSeries.setLineStyle(LineStyle.valueOf((String) presProvider.getStyle(appearance, StyleProperties.SERIES_STYLE)));
            lineSeries.setSymbolType(SYMBOL_MAP.getOrDefault(symbolType, ILineSeries.PlotSymbolType.NONE));
            lineSeries.setSymbolSize(Math.round(presProvider.getFloatStyleOrDefault(appearance, StyleProperties.HEIGHT, 1.0f).floatValue() * DEFAULT_SYMBOL_SIZE));
            lineSeries.setLineColor(color);
            lineSeries.setSymbolColor(color);
            lineSeries.setVisible(true);
            lineSeries.setLineWidth(((Number) presProvider.getFloatStyleOrDefault(appearance, StyleProperties.WIDTH, 1.0f)).intValue());
            return lineSeries;
        }

        /**
         * Set x and y data in series before calling this method
         */
        private void enableStack(ISeries<?> series, boolean stacked) {
            if (series.getType().equals(SeriesType.BAR)) {
                ((IBarSeries<?>) series).enableStack(stacked);
            }
        }

        private synchronized void updateThreadFinished(UpdateThread thread) {
            if (thread == fUpdateThread) {
                fUpdateThread = null;
            }
        }
    }

    private synchronized void newUpdateThread(@NonNull ITmfTrace trace, @NonNull FlowScopeLog fScope) {
        if (getSwtChart().isDisposed()) {
            return;
        }
        int numRequests = fOverrideNbPoints != 0 ? fOverrideNbPoints : (int) Math.min(getWindowEndTime() - getWindowStartTime() + 1, (long) (getSwtChart().getPlotArea().getSize().x * fResolution));
        fUpdateThread = new UpdateThread(trace, numRequests, fScope);
        fUpdateThread.start();
    }

    @TmfSignalHandler
    @Override
    public void traceClosed(@Nullable TmfTraceClosedSignal signal) {
        cancelUpdate();
        super.traceClosed(signal);
        if (signal != null) {
            fXYPresentationProvider.remove(signal.getTrace());
        }
    }

    /**
     * Set or remove the global regex filter value
     *
     * @param signal
     *                   the signal carrying the regex value
     */
    @TmfSignalHandler
    public void regexFilterApplied(TmfFilterAppliedSignal signal) {
        updateContent();
    }

    /**
     * This method build the multimap of regexes by property that will be used to
     * filter the timegraph states
     *
     * Override this method to add other regexes with their properties. The data
     * provider should handle everything after.
     *
     * @return The multimap of regexes by property
     */
    protected @NonNull Multimap<@NonNull Integer, @NonNull String> getRegexes() {
        Multimap<@NonNull Integer, @NonNull String> regexes = HashMultimap.create();

        ITmfTrace trace = getTrace();
        if (trace == null) {
            return regexes;
        }
        TraceCompassFilter globalFilter = TraceCompassFilter.getFilterForTrace(trace);
        if (globalFilter == null) {
            return regexes;
        }
        regexes.putAll(IFilterProperty.DIMMED, globalFilter.getRegexes());

        return regexes;
    }
}
