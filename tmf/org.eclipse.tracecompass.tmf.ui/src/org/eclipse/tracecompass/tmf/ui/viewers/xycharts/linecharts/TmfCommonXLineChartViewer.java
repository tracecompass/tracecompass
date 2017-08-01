/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal and others.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils.FlowScopeLogBuilder;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.filters.TimeQueryFilter;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfCommonXAxisResponse;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.ITmfXYDataProvider;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.xy.IYModel;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.viewmodel.ICommonXAxisModel;
import org.eclipse.tracecompass.tmf.core.viewmodel.IYSeries;
import org.eclipse.tracecompass.tmf.core.viewmodel.YSeries;
import org.eclipse.tracecompass.tmf.ui.colors.X11Color;
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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;

/**
 * Abstract line chart viewer class implementation. All series in this viewer
 * use the same X axis values. They are automatically created as values are
 * provided for a key. Series by default will be displayed as a line. Each
 * series appearance can be overridden when creating it.
 *
 * @author - Geneviève Bastien
 */
public abstract class TmfCommonXLineChartViewer extends TmfXYChartViewer {

    private static final double DEFAULT_MAXY = Double.MIN_VALUE;
    private static final double DEFAULT_MINY = Double.MAX_VALUE;
    private static final int DEFAULT_WIDTH = 1;

    // Timeout between updates in the updateData thread
    private static final long BUILD_UPDATE_TIMEOUT = 500;

    /* The desired number of points per pixel */
    private static final double RESOLUTION = 1.0;
    private static final @NonNull Logger LOGGER = TraceCompassLog.getLogger(TmfCommonXLineChartViewer.class);
    private final Map<String, Color> fColors = new HashMap<>();

    private static final Map<@NonNull String, Color> SYSTEM_COLORS = new LinkedHashMap<>();
    static {
        SYSTEM_COLORS.put("BLUE", Display.getDefault().getSystemColor(SWT.COLOR_BLUE)); //$NON-NLS-1$
        SYSTEM_COLORS.put("RED", Display.getDefault().getSystemColor(SWT.COLOR_RED)); //$NON-NLS-1$
        SYSTEM_COLORS.put("GREEN", Display.getDefault().getSystemColor(SWT.COLOR_GREEN)); //$NON-NLS-1$
        SYSTEM_COLORS.put("MAGENTA", Display.getDefault().getSystemColor(SWT.COLOR_MAGENTA)); //$NON-NLS-1$
        SYSTEM_COLORS.put("CYAN", Display.getDefault().getSystemColor(SWT.COLOR_CYAN)); //$NON-NLS-1$
        SYSTEM_COLORS.put("DARK_BLUE", Display.getDefault().getSystemColor(SWT.COLOR_DARK_BLUE)); //$NON-NLS-1$
        SYSTEM_COLORS.put("DARK_RED", Display.getDefault().getSystemColor(SWT.COLOR_DARK_RED)); //$NON-NLS-1$
        SYSTEM_COLORS.put("DARK_GREEN", Display.getDefault().getSystemColor(SWT.COLOR_DARK_GREEN)); //$NON-NLS-1$
        SYSTEM_COLORS.put("DARK_MAGENTA", Display.getDefault().getSystemColor(SWT.COLOR_DARK_MAGENTA)); //$NON-NLS-1$
        SYSTEM_COLORS.put("DARK_CYAN", Display.getDefault().getSystemColor(SWT.COLOR_DARK_CYAN)); //$NON-NLS-1$
        SYSTEM_COLORS.put("DARK_YELLOW", Display.getDefault().getSystemColor(SWT.COLOR_DARK_YELLOW)); //$NON-NLS-1$
        SYSTEM_COLORS.put("BLACK", Display.getDefault().getSystemColor(SWT.COLOR_BLACK)); //$NON-NLS-1$
        SYSTEM_COLORS.put("GRAY", Display.getDefault().getSystemColor(SWT.COLOR_GRAY)); //$NON-NLS-1$
        SYSTEM_COLORS.put("YELLOW", Display.getDefault().getSystemColor(SWT.COLOR_YELLOW)); //$NON-NLS-1$
    }

    private static final Pattern PATTERN = Pattern.compile("\\s*(\\d{1,3})\\s*(\\d{1,3})\\s*(\\d{1,3})"); //$NON-NLS-1$
    private static final Map<String, LineStyle> LINE_STYLES = ImmutableMap.of(
            IYSeries.SOLID, LineStyle.SOLID,
            IYSeries.DASH, LineStyle.DASH,
            IYSeries.DOT, LineStyle.DOT,
            IYSeries.DASHDOT, LineStyle.DASHDOT,
            IYSeries.DASHDOTDOT, LineStyle.DASHDOTDOT);

    private final CommonXAxisModelBuilder fModelBuilder = new CommonXAxisModelBuilder();

    private double fResolution;

    private UpdateThread fUpdateThread;

    private final AtomicInteger fDirty = new AtomicInteger();

    /**
     * Used for testing
     */
    private int fOverrideNbPoints = 0;

    /**
     * Data provider for XY viewers
     *
     * @since 3.1
     */
    private ITmfXYDataProvider fXYDataProvider;

    /**
     * Constructor
     *
     * @param parent
     *            The parent composite
     * @param title
     *            The title of the viewer
     * @param xLabel
     *            The label of the xAxis
     * @param yLabel
     *            The label of the yAXIS
     */
    public TmfCommonXLineChartViewer(Composite parent, String title, String xLabel, String yLabel) {
        super(parent, title, xLabel, yLabel);
        fModelBuilder.setTitle(title);
        getSwtChart().getTitle().setVisible(false);
        getSwtChart().getLegend().setPosition(SWT.BOTTOM);
        getSwtChart().getAxisSet().getXAxes()[0].getTitle().setVisible(false);
        setResolution(RESOLUTION);
        setTooltipProvider(new TmfCommonXLineChartTooltipProvider(this));
    }

    /**
     * Set the number of requests per pixel that should be done on this chart
     *
     * @param resolution
     *            The number of points per pixels
     */
    protected void setResolution(double resolution) {
        fResolution = resolution;
    }

    /**
     * Set the data provider
     *
     * @param dataProvider
     *            A data provider used for fetching a XY Model
     * @since 3.1
     */
    protected void setDataProvider(ITmfXYDataProvider dataProvider) {
        fXYDataProvider = dataProvider;
    }

    @Override
    public void loadTrace(ITmfTrace trace) {
        super.loadTrace(trace);
        reinitialize();
    }

    private @NonNull String getViewerId() {
        return getClass().getName();
    }

    /**
     * Get the model
     *
     * @return the model
     * @since 3.1
     */
    public ICommonXAxisModel getModel() {
        return fModelBuilder.build();
    }

    private Runnable newUiInitializeRunnable(@NonNull FlowScopeLog enclosingScope) {
        return () -> {
            try (FlowScopeLog uiScope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:UiInitialization").setParentScope(enclosingScope).build()) { //$NON-NLS-1$
                if (getSwtChart().isDisposed()) {
                    return;
                }

                /* Delete the old series */
                try {
                    clearContent();
                    createSeries();
                } finally {
                    /*
                     * View is cleared, decrement fDirty
                     */
                    fDirty.decrementAndGet();
                }
            }
        };
    }

    private Runnable newIntializeRunnable(@NonNull FlowScopeLog scope) {
        return () -> {
            try (FlowScopeLog tracer = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:InitializeThread").setParentScope(scope).build()) { //$NON-NLS-1$
                initializeDataSource();
                if (getSwtChart().isDisposed()) {
                    return;
                }
                getDisplay().asyncExec(newUiInitializeRunnable(tracer));
            }
        };
    }

    /**
     * Forces a reinitialization of the data sources, even if it has already been
     * initialized for this trace before
     */
    protected void reinitialize() {
        try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:ReinitializeRequested").setCategory(getViewerId()).build()) { //$NON-NLS-1$
            fModelBuilder.setXValues(new double[0]);
            /* Initializing data: the content is not current */
            fDirty.incrementAndGet();
            // Don't use TmfUiRefreshHandler (bug 467751)
            Thread thread = new Thread(newIntializeRunnable(scope));
            thread.start();
        }
    }

    /**
     * Initialize the source of the data for this viewer. This method is run in a
     * separate thread, so this is where for example one can execute an analysis
     * module and wait for its completion to initialize the series
     */
    protected void initializeDataSource() {

    }

    private class UpdateThread extends Thread {
        private final IProgressMonitor fMonitor;
        private final int fNumRequests;
        private final @NonNull FlowScopeLog fScope;

        public UpdateThread(int numRequests, @NonNull FlowScopeLog log) {
            super("Line chart update"); //$NON-NLS-1$
            fNumRequests = numRequests;
            fMonitor = new NullProgressMonitor();
            fScope = log;
        }

        @Override
        public void run() {
            try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:UpdateThread", "numRequests=", fNumRequests).setParentScope(fScope).build()) { //$NON-NLS-1$ //$NON-NLS-2$
                try {
                    updateData(getWindowStartTime(), getWindowEndTime(), fNumRequests, fMonitor);
                } finally {
                    /*
                     * fDirty should have been incremented before creating the thread, so we
                     * decrement it once it is finished
                     */
                    fDirty.decrementAndGet();
                }
                updateThreadFinished(this);
            }
        }

        public void cancel() {
            TraceCompassLogUtils.traceInstant(LOGGER, Level.FINE, "CommonXLineChart:UpdateThreadCanceled"); //$NON-NLS-1$
            fMonitor.setCanceled(true);
        }
    }

    private synchronized void newUpdateThread(@NonNull FlowScopeLog fScope) {
        cancelUpdate();
        if (getSwtChart().isDisposed()) {
            return;
        }
        int numRequests = fOverrideNbPoints != 0 ? fOverrideNbPoints : (int) (getSwtChart().getPlotArea().getBounds().width * fResolution);
        fUpdateThread = new UpdateThread(numRequests, fScope);
        fUpdateThread.start();
    }

    /**
     * Force the number of points to a fixed value
     *
     * @param nbPoints
     *            The number of points to display, cannot be negative. 0 means use
     *            native resolution. any positive integer means that number of
     *            points
     *
     * @since 3.1
     */
    public synchronized void setNbPoints(int nbPoints) {
        if (nbPoints < 0) {
            throw new IllegalArgumentException("Number of points cannot be negative"); //$NON-NLS-1$
        }
        fOverrideNbPoints = nbPoints;
        updateContent();
    }

    private synchronized void updateThreadFinished(UpdateThread thread) {
        if (thread == fUpdateThread) {
            fUpdateThread = null;
        }
    }

    /**
     * Cancels the currently running update thread. It is automatically called when
     * the content is updated, but child viewers may want to call it manually to do
     * some operations before calling
     * {@link TmfCommonXLineChartViewer#updateContent}
     */
    protected synchronized void cancelUpdate() {
        if (fUpdateThread != null) {
            fUpdateThread.cancel();
        }
    }

    @Override
    protected void updateContent() {
        try (FlowScopeLog parentScope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:ContentUpdateRequested").setCategory(getViewerId()).build()) { //$NON-NLS-1$
            /*
             * Content is not up to date, so we increment fDirty. It will be decremented at
             * the end of the update thread
             */
            fDirty.incrementAndGet();
            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:CreatingUpdateThread").setParentScope(parentScope).build()) { //$NON-NLS-1$
                        newUpdateThread(scope);
                    }
                }
            });
        }
    }

    /**
     * Convenience method to compute the values of the X axis for a given time
     * range. This method will return at most nb values, equally separated from
     * start to end. The step between values will be at least 1.0, so the number of
     * values returned can be lower than nb.
     *
     * The returned time values are in internal time, ie to get trace time, the time
     * offset needs to be added to those values.
     *
     * @param start
     *            The start time of the time range
     * @param end
     *            End time of the range
     * @param nb
     *            The maximum number of steps in the x axis.
     * @return The time values (converted to double) to match every step.
     */
    protected static final double[] getXAxis(long start, long end, int nb) {
        long steps = (end - start);
        int nbVals = nb;
        if (steps < nb) {
            nbVals = (int) steps;
            if (nbVals <= 0) {
                nbVals = 1;
            }
        }
        double step = steps / (double) nbVals;

        double[] timestamps = new double[nbVals];
        double curTime = 1;
        for (int i = 0; i < nbVals; i++) {
            timestamps[i] = curTime;
            curTime += step;
        }
        return timestamps;
    }

    /**
     * Set the values of the x axis. There is only one array of values for the x
     * axis for all series of a line chart so it needs to be set once here.
     *
     * @param xaxis
     *            The values for the x axis. The values must be in internal time, ie
     *            time offset have been subtracted from trace time values.
     * @since 3.1
     */
    protected final void setXAxis(double @NonNull [] xaxis) {
        fModelBuilder.setXValues(xaxis);
    }

    /**
     * Since the XY Model returned by data provider contains directly the requested
     * time as long array, we need to convert it to double array for SWT Chart. This
     * method is intended also to refresh the {@link CommonXAxisSeriesModel} of the
     * viewer. <br/>
     * <br/>
     * See {@link ITmfCommonXAxisModel} <br/>
     * See {@link CommonXAxisSeriesModel}. <br/>
     * <br/>
     * FIXME: Unify the two models and find a solution to manage colors instead of
     * having it in the viewmodel. This is, hopefully, an intermediate solution with
     * two "models" for a future patch that will fix the style/color problem.
     *
     * @param model
     *            The model returned by XY Data providers
     * @since 3.1
     */
    private void extractXYModelAndUpdateViewModel(ITmfCommonXAxisModel model) {

        long[] xValuesRequested = model.getXAxis();
        double[] xValuesToDisplay = new double[xValuesRequested.length];
        long offset = getTimeOffset();

        for (int i = 0; i < xValuesRequested.length; ++i) {
            xValuesToDisplay[i] = (xValuesRequested[i] - offset);
        }

        setXAxis(xValuesToDisplay);

        Map<String, IYModel> yData = model.getYData();
        for (Entry<String, IYModel> entry : yData.entrySet()) {
            setSeries(entry.getKey(), entry.getValue().getData());
        }
    }

    /**
     * Update the series data because the time range has changed.
     *
     * @param start
     *            The start time of the range for which the get the data
     * @param end
     *            The end time of the range
     * @param nb
     *            The number of 'points' in the chart.
     * @param monitor
     *            The progress monitor object
     */
    protected void updateData(long start, long end, int nb, IProgressMonitor monitor) {
        TimeQueryFilter filters = new TimeQueryFilter(start, end, nb);
        updateData(filters, monitor);
    }

    /**
     * This method is responsible for calling the
     * {@link TmfCommonXLineChartViewer#updateDisplay()} when needed for the new
     * values to be displayed.
     *
     * @param filters
     *            A query analysis filter
     * @param monitor
     *            A monitor for cancelling task
     */
    private void updateData(@NonNull TimeQueryFilter filters, IProgressMonitor monitor) {
        if (fXYDataProvider == null) {
            LOGGER.log(Level.WARNING, "Data provider for this viewer is not available"); //$NON-NLS-1$
            return;
        }

        long currentEnd = 0;
        while (currentEnd < filters.getEnd()) {
            ITmfCommonXAxisResponse response = fXYDataProvider.fetchXY(filters, monitor);
            ITmfCommonXAxisModel model = response.getModel();
            if (model != null) {
                extractXYModelAndUpdateViewModel(model);
                updateDisplay();
            }

            ITmfCommonXAxisResponse.Status status = response.getStatus();
            currentEnd = response.getCurrentEnd();

            /* Model is complete, no need to request again the data provider */
            if (status == ITmfCommonXAxisResponse.Status.COMPLETED) {
                return;
            }
            /* Error occured, log and return */
            else if (status == ITmfCommonXAxisResponse.Status.FAILED || status == ITmfCommonXAxisResponse.Status.CANCELLED) {
                LOGGER.log(Level.WARNING, response.getStatusMessage());
                return;
            }
            /*
             * Status is RUNNING. Sleeping current thread to wait before request data
             * provider again
             */
            else {
                try {
                    Thread.sleep(BUILD_UPDATE_TIMEOUT);
                } catch (InterruptedException e) {
                    LOGGER.log(Level.INFO, e.getMessage());
                    return;
                }
            }
        }
    }

    /**
     * Set the data for a given series of the graph. The series does not
     * need to be created before calling this, but it needs to have at least as many
     * values as the x axis.
     *
     * If the series does not exist, it will automatically be created at display
     * time, with the default values.
     *
     * Warning, do not override if possible.
     *
     * @param seriesName
     *            The name of the series for which to set the values
     * @param seriesValues
     *            The array of values for the series
     */
    protected void setSeries(String seriesName, double[] seriesValues) {
        if (seriesName == null) {
            throw new IllegalArgumentException("seriesName cannot be null"); //$NON-NLS-1$
        }
        if (seriesValues == null) {
            throw new IllegalArgumentException("Series values cannot be null"); //$NON-NLS-1$
        }
        String seriesType = getSeriesType(seriesName);
        fModelBuilder.addYSeries(
                new YSeries(
                        seriesType,
                        getSeriesColor(seriesName),
                        getWidth(seriesName),
                        getSeriesStyle(seriesType),
                        seriesName,
                        seriesValues));
    }

    /**
     * Get the width
     *
     * @param seriesName
     *            The series
     * @return the width
     * @since 3.1
     */
    protected int getWidth(@NonNull String seriesName) {
        return DEFAULT_WIDTH;
    }

    /**
     * @param seriesName
     *            The series
     * @return the series type, see {@link IYSeries}'s strings
     * @since 3.1
     */
    protected @NonNull String getSeriesType(@NonNull String seriesName) {
        return IYSeries.LINE;
    }

    /**
     * Get the color
     *
     * @param seriesName
     *            The series
     * @return The color
     * @since 3.1
     */
    protected @Nullable String getSeriesColor(@NonNull String seriesName) {
        return Iterables.get(SYSTEM_COLORS.keySet(), getModel().getSeries().size() % SYSTEM_COLORS.size());
    }

    /**
     * Get the series style
     *
     * @param seriesType
     *            the series type, obtained by {@link #getSeriesType(String)}
     * @return the series style
     * @since 3.1
     */
    protected @Nullable String getSeriesStyle(@NonNull String seriesType) {
        return getLineStyle(seriesType, getModel().getSeries().size());
    }

    /**
     * Add a new series to the XY line chart. By default, it is a simple solid line.
     *
     * Warning do not override
     *
     * @param seriesName
     *            The name of the series to create
     * @return The series so that the concrete viewer can modify its properties if
     *         required
     *
     * @deprecated Use {@link TmfCommonXLineChartViewer#addSeries2(String)} instead.
     */
    @Deprecated
    protected ILineSeries addSeries(String seriesName) {
        if (seriesName == null) {
            return null;
        }
        IYSeries ySeries = getModel().findSeries(seriesName);
        if (ySeries != null && ySeries.getSeriesType() == IYSeries.BAR) {
            return null;
        }
        return (ILineSeries) addSeries2(seriesName);
    }

    /**
     * Add a new series to the XY line chart. By default, it is a simple solid line.
     *
     * Warning do not override
     *
     * @param seriesName
     *            The name of the series to create
     * @return The series so that the concrete viewer can modify its properties if
     *         required
     * @since 3.1
     */
    protected ISeries addSeries2(String seriesName) {
        if (seriesName == null) {
            return null;
        }
        ISeriesSet seriesSet = getSwtChart().getSeriesSet();
        int seriesCount = seriesSet.getSeries().length;
        ICommonXAxisModel currentModel = getModel();
        IYSeries ySeries = currentModel.findSeries(seriesName);
        if (ySeries == null) {
            // if it does not exist, create a dummy series
            setSeries(seriesName, new double[currentModel.getXAxis().length]);
            currentModel = getModel();
            ySeries = currentModel.findSeries(seriesName);
        }
        if (ySeries == null) {
            return null;
        }

        String colorTxt = ySeries.getColor();
        Color color = getColor(seriesName, colorTxt);

        if (ySeries.getSeriesType() == IYSeries.BAR) {
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
        boolean isScatter = IYSeries.SCATTER.equals(ySeries.getSeriesType());
        lineSeries.enableArea(IYSeries.AREA.equals(ySeries.getSeriesType()));
        lineSeries.setLineStyle(!isScatter ? LINE_STYLES.get(getLineStyle(ySeries.getSeriesType(), seriesCount)) : LineStyle.NONE);
        lineSeries.setSymbolType(isScatter ? PlotSymbolType.DIAMOND : PlotSymbolType.NONE);
        lineSeries.setLineColor(color);
        lineSeries.setVisible(true);
        return lineSeries;
    }

    private Color getColor(String seriesName, String colorTxt) {
        Color sysColor = SYSTEM_COLORS.get(colorTxt);
        if (sysColor != null) {
            return sysColor;
        }
        Color color;
        // Try with the names
        RGB rgb = X11Color.toRGB(colorTxt);
        // Try with 124 80 223
        if (rgb == null) {
            rgb = parseX11Rgb(colorTxt);
        }
        // Try with ff2234
        if (rgb == null) {
            rgb = parseWebRgb(colorTxt);
        }
        if (rgb != null) {
            color = new Color(Display.getDefault(), rgb);
            fColors.put(seriesName, color);
        } else {
            color = Display.getDefault().getSystemColor(SWT.COLOR_BLACK);
        }
        return color;
    }

    private static RGB parseWebRgb(String colorTxt) {
        java.awt.Color elem = java.awt.Color.decode(colorTxt);
        if (elem != null) {
            return new RGB(elem.getRed(), elem.getGreen(), elem.getBlue());
        }
        return null;
    }

    private static RGB parseX11Rgb(String colorTxt) {
        Matcher matcher = PATTERN.matcher(colorTxt);
        if (matcher.matches()) {
            int r = Integer.parseInt(matcher.group(1));
            int g = Integer.parseInt(matcher.group(2));
            int b = Integer.parseInt(matcher.group(3));
            return new RGB(r, g, b);
        }
        return null;
    }

    private static String getLineStyle(String seriesType, int seriesCount) {
        if (!IYSeries.SCATTER.equals(seriesType)) {
            String[] styleStrings = LINE_STYLES.keySet().toArray(new String[LINE_STYLES.size()]);
            return styleStrings[(seriesCount / (SYSTEM_COLORS.size())) % styleStrings.length];
        }
        return IYSeries.NONE;
    }

    /**
     * Delete a series from the chart and its values from the viewer.
     *
     * @param seriesName
     *            Name of the series to delete
     */
    protected void deleteSeries(String seriesName) {
        ISeriesSet seriesSet = getSwtChart().getSeriesSet();
        ISeries series = seriesSet.getSeries(seriesName);
        if (series != null) {
            seriesSet.deleteSeries(series.getId());
        }
        Color color = fColors.get(seriesName);
        if (color != null) {
            color.dispose();
        }
        fModelBuilder.deleteSeries(seriesName);
    }

    /**
     * Update the chart's values before refreshing the viewer
     */
    protected void updateDisplay() {
        updateDisplay(getModel());
    }

    /**
     * Update the chart's values before refreshing the viewer
     *
     * @param model
     *            The model
     * @since 3.1
     */
    protected void updateDisplay(ICommonXAxisModel model) {

        try (FlowScopeLog scope = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:UpdateDisplayRequested").setCategory(getViewerId()).build()) { //$NON-NLS-1$
            /* Content is not up to date, increment dirtiness */
            final ICommonXAxisModel seriesValues = model;
            fDirty.incrementAndGet();
            Display.getDefault().asyncExec(new Runnable() {
                final TmfChartTimeStampFormat tmfChartTimeStampFormat = new TmfChartTimeStampFormat(getTimeOffset());

                @Override
                public void run() {
                    try (FlowScopeLog log = new FlowScopeLogBuilder(LOGGER, Level.FINE, "CommonXLineChart:UpdateDisplay").setParentScope(scope).build()) { //$NON-NLS-1$
                        if (!getSwtChart().isDisposed()) {
                            double[] xValues = seriesValues.getXAxis();
                            double maxy = DEFAULT_MAXY;
                            double miny = DEFAULT_MINY;
                            double end = getWindowEndTime() - getWindowStartTime();
                            if (end > 0.0) {
                                for (IYSeries entry : seriesValues.getSeries().values()) {
                                    ISeries series = getSwtChart().getSeriesSet().getSeries(entry.getLabel());
                                    if (series == null) {
                                        series = addSeries2(entry.getLabel());
                                    }
                                    if (series == null) {
                                        return;
                                    }
                                    series.setXSeries(xValues);
                                    /*
                                     * Find the minimal and maximum values in this series
                                     */
                                    for (double value : entry.getDatapoints()) {
                                        maxy = Math.max(maxy, value);
                                        miny = Math.min(miny, value);
                                    }
                                    series.setYSeries(entry.getDatapoints());
                                }
                                if (maxy == DEFAULT_MAXY) {
                                    maxy = 1.0;
                                }
                            }else {
                                clearContent();
                                end =1;
                            }
                            IAxisTick xTick = getSwtChart().getAxisSet().getXAxis(0).getTick();
                            xTick.setFormat(tmfChartTimeStampFormat);
                            final double start = 0.0;
                            getSwtChart().getAxisSet().getXAxis(0).setRange(new Range(start, end));
                            if (maxy > miny) {
                                getSwtChart().getAxisSet().getYAxis(0).setRange(new Range(miny, maxy));
                            }
                            getSwtChart().redraw();

                            if (isSendTimeAlignSignals()) {
                                /*
                                 * The width of the chart might have changed and its time axis might be
                                 * misaligned with the other views
                                 */
                                Point viewPos = TmfCommonXLineChartViewer.this.getParent().getParent().toDisplay(0, 0);
                                int axisPos = getSwtChart().toDisplay(0, 0).x + getPointAreaOffset();
                                int timeAxisOffset = axisPos - viewPos.x;
                                TmfTimeViewAlignmentInfo timeAlignmentInfo = new TmfTimeViewAlignmentInfo(getControl().getShell(), viewPos, timeAxisOffset);
                                TmfSignalManager.dispatchSignal(new TmfTimeViewAlignmentSignal(TmfCommonXLineChartViewer.this, timeAlignmentInfo, true));
                            }
                        }
                    } finally {
                        /* Content has been updated, decrement dirtiness */
                        fDirty.decrementAndGet();
                    }
                }
            });
        }
    }

    /**
     * Create the series once the initialization of the viewer's data source is
     * done. Series do not need to be created before setting their values, but if
     * their appearance needs to be customized, this method is a good place to do
     * so. It is called only once per trace.
     */
    protected void createSeries() {

    }

    @Override
    protected void clearContent() {
        getSwtChart().getAxisSet().getXAxis(0).getTick().setFormat(null);
        super.clearContent();
    }

    @Override
    public boolean isDirty() {
        /* Check the parent's or this view's own dirtiness */
        return super.isDirty() || (fDirty.get() != 0);
    }
}
