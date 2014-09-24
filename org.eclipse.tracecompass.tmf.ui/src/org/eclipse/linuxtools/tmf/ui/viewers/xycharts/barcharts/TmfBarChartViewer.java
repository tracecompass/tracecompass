/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Bernd Hufmann - Updated for TMF base chart viewer
 **********************************************************************/
package org.eclipse.linuxtools.tmf.ui.viewers.xycharts.barcharts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfChartTimeStampFormat;
import org.eclipse.linuxtools.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.swtchart.Chart;
import org.swtchart.IAxisTick;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;
import org.swtchart.ISeries.SeriesType;

/**
 * Abstract bar chart viewer class implementation. Used for displaying
 * histograms.
 *
 * @author Alexandre Montplaisir
 * @author Bernd Hufmann
 * @since 3.0
 */
public abstract class TmfBarChartViewer extends TmfXYChartViewer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /** Width of each histogram bar, in pixels */
    public static final int MINIMUM_BAR_WIDTH = 1;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** List of series */
    private final List<String> seriesNames = new ArrayList<>();
    /** List of colors */
    private final List<RGB> colors = new ArrayList<>();
    /** the bar width */
    private int fBarWidth = MINIMUM_BAR_WIDTH;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructs a TmfXYChartViewer.
     *
     * @param parent
     *            The parent composite
     * @param title
     *            The title of the viewer
     * @param xLabel
     *            The label of the xAxis
     * @param yLabel
     *            The label of the yAXIS
     * @param barWidth
     *            The bar width
     */
    public TmfBarChartViewer(Composite parent, String title, String xLabel, String yLabel, int barWidth) {
        super(parent, title, xLabel, yLabel);
        fBarWidth = barWidth;

        setTooltipProvider(new TmfHistogramTooltipProvider(this));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------
    @Override
    protected void updateContent() {

        getDisplay().asyncExec(new Runnable() {

            @Override
            public void run() {
                Chart swtChart = getSwtChart();
                int numRequests = swtChart.getPlotArea().getBounds().width / fBarWidth;

                for (int i = 0; i < seriesNames.size(); i++) {
                    ISeries series = swtChart.getSeriesSet().getSeries(seriesNames.get(i));
                    if (series == null) {
                        series = initSeries(seriesNames.get(i), colors.get(i));
                    }
                    readData(series, getWindowStartTime(), getWindowEndTime(), numRequests);
                }
            }
        });
    }

    /**
     * Method to add a series to the chart.
     *
     * @param name
     *            Name of series
     * @param color
     *            color to use for series
     */
    protected void addSeries(String name, RGB color) {
        seriesNames.add(name);
        colors.add(color);
    }

    /**
     * Clears all series
     */
    protected void clearSeries() {
        seriesNames.clear();
        colors.clear();
    }

    /**
     * Draw the given series on the chart
     *
     * @param series
     *            The series to display
     * @param x
     *            The X values. It can be computed with
     *            {@link TmfBarChartViewer#getXAxis}
     *            The values are stored in the internal time representation.
     *            To get the trace time one has to add the time offset
     *            {@link #getTimeOffset()}.
     * @param y
     *            The Y values that were computed by the extended class
     */
    protected void drawChart(final ISeries series, final double[] x, final double[] y) {
        // Run in GUI thread to make sure that chart is ready after restart
        final Display display = getDisplay();
        if (display.isDisposed()) {
            return;
        }

        display.syncExec(new Runnable() {
            @Override
            public void run() {
                if (display.isDisposed()) {
                    return;
                }
                Chart swtChart = getSwtChart();
                IAxisTick xTick = swtChart.getAxisSet().getXAxis(0).getTick();
                xTick.setFormat(new TmfChartTimeStampFormat(getTimeOffset()));
                series.setXSeries(x);
                series.setYSeries(y);
                xTick.setTickMarkStepHint(256);

                swtChart.getAxisSet().adjustRange();
                swtChart.redraw();
            }
        });
    }

    /**
     * Convenience method to compute the X axis values for a given time range.
     *
     * @param start
     *            Start of the time range
     * @param end
     *            End of the range
     * @param nb
     *            Number of steps. This will be the size of the returned array.
     * @return The time values (converted to double) that match every step
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
     * Load the data for the given series. This method should call
     * {@link TmfBarChartViewer#drawChart} to return the results when done.
     *
     * Careful, this method is called by a signal handler which also happens to
     * be in the main UI thread. This means any processing will block the UI! In
     * most cases it's probably better to start a separate Thread/Job to do the
     * processing, and that one can call drawChart() when done to update the
     * view.
     *
     * @param series
     *            Which series of the chart should the viewer update
     * @param start
     *            The start time (in nanoseconds) of the range to display
     * @param end
     *            The end time of the range to display.
     * @param nb
     *            The number of 'steps' in the bar chart (fewer steps means each
     *            bar is wider).
     */
    protected abstract void readData(ISeries series, long start, long end, int nb);

    // initializes a series
    private IBarSeries initSeries(String name, RGB color) {
        IBarSeries bs = (IBarSeries) getSwtChart().getSeriesSet().createSeries(SeriesType.BAR, name);
        bs.enableStack(true);
        bs.setBarColor(new Color(Display.getDefault(), color));
        bs.setBarPadding(0);
        return bs;
    }
}