/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.xycharts.linecharts;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ui.TmfUiRefreshHandler;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfChartTimeStampFormat;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.IAxisTick;
import org.swtchart.ILineSeries;
import org.swtchart.ILineSeries.PlotSymbolType;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;
import org.swtchart.ISeriesSet;
import org.swtchart.LineStyle;
import org.swtchart.Range;

/**
 * Abstract line chart viewer class implementation. All series in this viewer
 * use the same X axis values. They are automatically created as values are
 * provided for a key. Series by default will be displayed as a line. Each
 * series appearance can be overridden when creating it.
 *
 * @author - Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfCommonXLineChartViewer extends TmfXYChartViewer {

    private static final double DEFAULT_MAXY = Double.MIN_VALUE;
    private static final double DEFAULT_MINY = Double.MAX_VALUE;

    /* The desired number of points per pixel */
    private static final double RESOLUTION = 1.0;

    private static final int[] LINE_COLORS = { SWT.COLOR_BLUE, SWT.COLOR_RED, SWT.COLOR_GREEN,
            SWT.COLOR_MAGENTA, SWT.COLOR_CYAN,
            SWT.COLOR_DARK_BLUE, SWT.COLOR_DARK_RED, SWT.COLOR_DARK_GREEN,
            SWT.COLOR_DARK_MAGENTA, SWT.COLOR_DARK_CYAN, SWT.COLOR_DARK_YELLOW,
            SWT.COLOR_BLACK, SWT.COLOR_GRAY };
    private static final LineStyle[] LINE_STYLES = { LineStyle.SOLID, LineStyle.DASH, LineStyle.DOT, LineStyle.DASHDOT };

    private final Map<String, double[]> fSeriesValues = new LinkedHashMap<>();
    private double[] fXValues;
    private double fResolution;

    private UpdateThread fUpdateThread;

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

    @Override
    public void loadTrace(ITmfTrace trace) {
        super.loadTrace(trace);
        reinitialize();
    }

    /**
     * Forces a reinitialization of the data sources, even if it has already
     * been initialized for this trace before
     *
     * @since 3.1
     */
    protected void reinitialize() {
        fSeriesValues.clear();
        Thread thread = new Thread() {
            @Override
            public void run() {
                initializeDataSource();
                TmfUiRefreshHandler.getInstance().queueUpdate(TmfCommonXLineChartViewer.this,
                        new Runnable() {
                    @Override
                    public void run() {
                        if (!getSwtChart().isDisposed()) {
                            /* Delete the old series */
                            clearContent();
                            createSeries();
                        }
                    }
                });
            }
        };
        thread.start();
    }

    /**
     * Initialize the source of the data for this viewer. This method is run in
     * a separate thread, so this is where for example one can execute an
     * analysis module and wait for its completion to initialize the series
     */
    protected void initializeDataSource() {

    }

    private class UpdateThread extends Thread {
        private final IProgressMonitor fMonitor;
        private final int fNumRequests;

        public UpdateThread(int numRequests) {
            super("Line chart update"); //$NON-NLS-1$
            fNumRequests = numRequests;
            fMonitor = new NullProgressMonitor();
        }

        @Override
        public void run() {
            updateData(getWindowStartTime(), getWindowEndTime(), fNumRequests, fMonitor);
            updateThreadFinished(this);
        }

        public void cancel() {
            fMonitor.setCanceled(true);
        }
    }

    private synchronized void newUpdateThread() {
        cancelUpdate();
        final int numRequests = (int) (getSwtChart().getPlotArea().getBounds().width * fResolution);
        fUpdateThread = new UpdateThread(numRequests);
        fUpdateThread.start();
    }

    private synchronized void updateThreadFinished(UpdateThread thread) {
        if (thread == fUpdateThread) {
            fUpdateThread = null;
        }
    }

    /**
     * Cancels the currently running update thread. It is automatically called
     * when the content is updated, but child viewers may want to call it
     * manually to do some operations before calling
     * {@link TmfCommonXLineChartViewer#updateContent}
     */
    protected synchronized void cancelUpdate() {
        if (fUpdateThread != null) {
            fUpdateThread.cancel();
        }
    }

    @Override
    protected void updateContent() {
        getDisplay().asyncExec(new Runnable() {
            @Override
            public void run() {
                newUpdateThread();
            }
        });
    }

    /**
     * Convenience method to compute the values of the X axis for a given time
     * range. This method will return nb values depending, equally separated
     * from start to end.
     *
     * The returned time values are in internal time, ie to get trace time, the
     * time offset needs to be added to those values.
     *
     * @param start
     *            The start time of the time range
     * @param end
     *            End time of the range
     * @param nb
     *            The number of steps in the x axis.
     * @return The time values (converted to double) to match every step.
     */
    protected final double[] getXAxis(long start, long end, int nb) {
        setTimeOffset(start - 1);

        double timestamps[] = new double[nb];
        long steps = (end - start);
        double step = steps / (double) nb;

        double curTime = 1;
        for (int i = 0; i < nb; i++) {
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
     *            The values for the x axis. The values must be in internal
     *            time, ie time offset have been subtracted from trace time
     *            values.
     */
    protected final void setXAxis(double[] xaxis) {
        fXValues = xaxis;
    }

    /**
     * Update the series data because the time range has changed. The x axis
     * values for this data update can be computed using the
     * {@link TmfCommonXLineChartViewer#getXAxis(long, long, int)} method which
     * will return a list of uniformely separated time values.
     *
     * Each series values should be set by calling the
     * {@link TmfCommonXLineChartViewer#setSeries(String, double[])}.
     *
     * This method is responsible for calling the
     * {@link TmfCommonXLineChartViewer#updateDisplay()} when needed for the new
     * values to be displayed.
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
    protected abstract void updateData(long start, long end, int nb, IProgressMonitor monitor);

    /**
     * Set the data for a given series of the graph. The series does not need to
     * be created before calling this, but it needs to have at least as many
     * values as the x axis.
     *
     * If the series does not exist, it will automatically be created at display
     * time, with the default values.
     *
     * @param seriesName
     *            The name of the series for which to set the values
     * @param seriesValues
     *            The array of values for the series
     */
    protected void setSeries(String seriesName, double[] seriesValues) {
        if (fXValues.length > seriesValues.length) {
            throw new IllegalStateException();
        }
        fSeriesValues.put(seriesName, seriesValues);
    }

    /**
     * Add a new series to the XY line chart. By default, it is a simple solid
     * line.
     *
     * @param seriesName
     *            The name of the series to create
     * @return The series so that the concrete viewer can modify its properties
     *         if required
     */
    protected ILineSeries addSeries(String seriesName) {
        ISeriesSet seriesSet = getSwtChart().getSeriesSet();
        int seriesCount = seriesSet.getSeries().length;
        ILineSeries series = (ILineSeries) seriesSet.createSeries(SeriesType.LINE, seriesName);
        series.setVisible(true);
        series.enableArea(false);
        series.setLineStyle(LINE_STYLES[(seriesCount / (LINE_COLORS.length)) % LINE_STYLES.length]);
        series.setSymbolType(PlotSymbolType.NONE);
        series.setLineColor(Display.getDefault().getSystemColor(LINE_COLORS[seriesCount % LINE_COLORS.length]));
        return series;
    }

    /**
     * Delete a series from the chart and its values from the viewer.
     *
     * @param seriesName
     *            Name of the series to delete
     */
    protected void deleteSeries(String seriesName) {
        ISeries series = getSwtChart().getSeriesSet().getSeries(seriesName);
        if (series != null) {
            getSwtChart().getSeriesSet().deleteSeries(series.getId());
        }
        fSeriesValues.remove(seriesName);
    }

    /**
     * Update the chart's values before refreshing the viewer
     */
    protected void updateDisplay() {
        Display.getDefault().asyncExec(new Runnable() {
            final TmfChartTimeStampFormat tmfChartTimeStampFormat = new TmfChartTimeStampFormat(getTimeOffset());

            @Override
            public void run() {
                if (!getSwtChart().isDisposed()) {
                    double maxy = DEFAULT_MAXY;
                    double miny = DEFAULT_MINY;
                    for (Entry<String, double[]> entry : fSeriesValues.entrySet()) {
                        ILineSeries series = (ILineSeries) getSwtChart().getSeriesSet().getSeries(entry.getKey());
                        if (series == null) {
                            series = addSeries(entry.getKey());
                        }
                        series.setXSeries(fXValues);
                        /* Find the minimal and maximum values in this series */
                        for (double value : entry.getValue()) {
                            maxy = Math.max(maxy, value);
                            miny = Math.min(miny, value);
                        }
                        series.setYSeries(entry.getValue());
                    }
                    if (maxy == DEFAULT_MAXY) {
                        maxy = 1.0;
                    }

                    IAxisTick xTick = getSwtChart().getAxisSet().getXAxis(0).getTick();
                    xTick.setFormat(tmfChartTimeStampFormat);

                    final double start = fXValues[0];
                    int lastX = fXValues.length - 1;
                    double end = (start == fXValues[lastX]) ? start + 1 : fXValues[lastX];
                    getSwtChart().getAxisSet().getXAxis(0).setRange(new Range(start, end));
                    getSwtChart().getAxisSet().getXAxis(0).adjustRange();
                    if (maxy > miny) {
                        getSwtChart().getAxisSet().getYAxis(0).setRange(new Range(miny, maxy));
                    }
                    getSwtChart().redraw();
                }
            }
        });
    }

    /**
     * Create the series once the initialization of the viewer's data source is
     * done. Series do not need to be created before setting their values, but
     * if their appearance needs to be customized, this method is a good place
     * to do so. It is called only once per trace.
     */
    protected void createSeries() {

    }

}
