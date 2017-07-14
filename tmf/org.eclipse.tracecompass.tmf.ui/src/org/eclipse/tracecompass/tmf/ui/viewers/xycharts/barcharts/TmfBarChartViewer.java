/**********************************************************************
 * Copyright (c) 2013, 2017 Ericsson
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
package org.eclipse.tracecompass.tmf.ui.viewers.xycharts.barcharts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.viewmodel.IYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfChartTimeStampFormat;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXLineChartViewer;
import org.swtchart.Chart;
import org.swtchart.IAxisTick;
import org.swtchart.ISeries;

/**
 * Abstract bar chart viewer class implementation. Used for displaying
 * histograms.
 *
 * @author Alexandre Montplaisir
 * @author Bernd Hufmann
 * @deprecated Use {@link TmfCommonXAxisChartViewer}
 */
@Deprecated
public abstract class TmfBarChartViewer extends TmfCommonXLineChartViewer {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    /** Width of each histogram bar, in pixels */
    public static final int MINIMUM_BAR_WIDTH = 1;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** the bar width */
    private int fBarWidth = MINIMUM_BAR_WIDTH;
    /** List of series */
    private final List<String> fSeriesNames = new ArrayList<>();
    /** List of colors */
    private final List<RGB> fColors = new ArrayList<>();

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

    /**
     * @since 3.0
     */
    @Override
    protected @NonNull String getSeriesType(@NonNull String seriesName) {
        return IYSeries.BAR;
    }

    /**
     * @since 3.0
     */
    @Override
    protected int getWidth(@NonNull String seriesName) {
        return fBarWidth;
    }

    /**
     * Load the data for the given series. This method should call
     * {@link TmfBarChartViewer#drawChart} to return the results when done.
     *
     * Careful, this method is called by a signal handler which also happens to be
     * in the main UI thread. This means any processing will block the UI! In most
     * cases it's probably better to start a separate Thread/Job to do the
     * processing, and that one can call drawChart() when done to update the view.
     *
     * @param series
     *            Which series of the chart should the viewer update
     * @param start
     *            The start time (in nanoseconds) of the range to display
     * @param end
     *            The end time of the range to display.
     * @param nb
     *            The number of 'steps' in the bar chart (fewer steps means each bar
     *            is wider).
     * @deprecated use
     *             {@link #updateData(long, long, int, org.eclipse.core.runtime.IProgressMonitor)}
     */
    @Deprecated
    protected void readData(ISeries series, long start, long end, int nb) {
        /*
         * Remove the abstract and write an empty method. This will avoid new classes
         * having to implement a deprecated method. So, this method is deliberately
         * empty
         */
    }

    /**
     * Draw the given series on the chart
     *
     * @param series
     *            The series to display
     * @param x
     *            The X values. It can be computed with
     *            {@link TmfBarChartViewer#getXAxis} The values are stored in the
     *            internal time representation. To get the trace time one has to add
     *            the time offset {@link #getTimeOffset()}.
     * @param y
     *            The Y values that were computed by the extended class
     * @deprecated use {@link #updateDisplay()}
     */
    @Deprecated
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
     * Method to add a series to the chart.
     *
     * @param name
     *            Name of series
     * @param color
     *            color to use for series
     * @deprecated use {@link #addSeries2(String)}
     */
    @Deprecated
    protected void addSeries(String name, RGB color) {
        fSeriesNames.add(name);
        fColors.add(color);
    }

    /**
     * Clears all series
     *
     * @deprecated Use {@link TmfCommonXLineChartViewer#clearContent()}
     */
    @Deprecated
    protected void clearSeries() {
        fSeriesNames.clear();
        fColors.clear();
    }
}
