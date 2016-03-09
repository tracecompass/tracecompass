/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.xycharts;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.swtchart.IAxis;
import org.swtchart.ISeries;

/**
 * Abstract tooltip provider for xy chart viewers. It displays the y value and y
 * value of the data point of the mouse position. Extending classes can provide
 * a custom tooltip text.
 *
 * @author Bernd Hufmann
 * @since 2.0
 */
public class TmfClosestDataPointTooltipProvider extends TmfBaseProvider implements MouseTrackListener, MouseMoveListener, PaintListener {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------
    private static final int ALPHA = 128;
    private static final int HIGHLIGHT_RADIUS = 5;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /** X coordinate for highlighting */
    private int fHighlightX;
    /** y coordinate for highlighting */
    private int fHighlightY;
    /** Flag to do highlighting or not */
    private boolean fIsHighlight;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for a tool tip provider.
     *
     * @param tmfChartViewer
     *                  - the parent chart viewer
     */
    public TmfClosestDataPointTooltipProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------
    @Override
    public void register() {
        getChart().getPlotArea().addMouseTrackListener(this);
        getChart().getPlotArea().addMouseMoveListener(this);
        getChart().getPlotArea().addPaintListener(this);
    }

    @Override
    public void deregister() {
        if ((getChartViewer().getControl() != null) && !getChartViewer().getControl().isDisposed()) {
            getChart().getPlotArea().removeMouseTrackListener(this);
            getChart().getPlotArea().removeMouseMoveListener(this);
            getChart().getPlotArea().removePaintListener(this);
        }
    }

    @Override
    public void refresh() {
        // nothing to do
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseEnter(MouseEvent e) {
    }

    @Override
    public void mouseExit(MouseEvent e) {
    }

    @Override
    public void mouseHover(MouseEvent e) {
        if ((getChartViewer().getWindowDuration() != 0) && (e != null)) {
            IAxis xAxis = getChart().getAxisSet().getXAxis(0);
            IAxis yAxis = getChart().getAxisSet().getYAxis(0);

            ISeries[] series = getChart().getSeriesSet().getSeries();

            double smallestDistance = Double.MAX_VALUE;
            Parameter param = null;

            // go over all series
            for (int k = 0; k < series.length; k++) {
                ISeries serie = series[k];
                double[] xS = serie.getXSeries();
                double[] yS = serie.getYSeries();

                if ((xS == null) || (yS == null)) {
                    continue;
                }
                // go over all data points
                for (int i = 0; i < xS.length; i++) {
                    int xs = xAxis.getPixelCoordinate(xS[i]) - e.x;
                    int ys = yAxis.getPixelCoordinate(yS[i]) - e.y;
                    double currentDistance = xs * xs + ys * ys;

                    /*
                     * Check for smallest distance to mouse position and
                     * only consider it if the mouse is close the data point.
                     */
                    if ((currentDistance < smallestDistance) && (currentDistance < (HIGHLIGHT_RADIUS * HIGHLIGHT_RADIUS))) {
                        smallestDistance = currentDistance;
                        fHighlightX = xs + e.x;
                        fHighlightY = ys + e.y;
                        if (param == null) {
                            param = new Parameter();
                        }
                        param.setSeriesIndex(k);
                        param.setDataIndex(i);
                    }
                }
            }
            String tooltip = null;
            if (param != null) {
                 tooltip = createToolTipText(param);
                if (tooltip != null) {
                    fIsHighlight = true;
                    getChart().redraw();
                }
            }
            /*
             *  Note that tooltip might be null which will clear the
             *  previous tooltip string. This is intentional.
             */
            getChart().getPlotArea().setToolTipText(tooltip);
        }
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseMove(@Nullable MouseEvent e) {
        if (fIsHighlight) {
            fIsHighlight = false;
            getChart().redraw();
        }
    }

    // ------------------------------------------------------------------------
    // PaintListener
    // ------------------------------------------------------------------------
    @Override
    public void paintControl(PaintEvent e) {
        if (fIsHighlight && e != null) {
            e.gc.setBackground(Display.getDefault().getSystemColor(
                    SWT.COLOR_RED));
            e.gc.setAlpha(ALPHA);

            e.gc.fillOval(fHighlightX - HIGHLIGHT_RADIUS, fHighlightY - HIGHLIGHT_RADIUS,
                    2 * HIGHLIGHT_RADIUS, 2 * HIGHLIGHT_RADIUS);
        }
    }

    /**
     * Creates the tooltip based on the given parameter.
     *
     * @param param
     *            parameter to create the tooltip string
     * @return the tooltip based on the given parameter.
     */
    protected String createToolTipText(@NonNull Parameter param) {
        ISeries[] series = getChart().getSeriesSet().getSeries();
        int seriesIndex = param.getSeriesIndex();
        int dataIndex = param.getDataIndex();
        if ((series != null) && (seriesIndex < series.length)) {
            ISeries serie = series[seriesIndex];
            double[] xS = serie.getXSeries();
            double[] yS = serie.getYSeries();
            if ((xS != null) && (yS != null) && (dataIndex < xS.length) && (dataIndex < yS.length)) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("x="); //$NON-NLS-1$
                buffer.append(new TmfTimestamp((long) xS[dataIndex] + getChartViewer().getTimeOffset(), ITmfTimestamp.NANOSECOND_SCALE).toString());
                buffer.append('\n');
                buffer.append("y="); //$NON-NLS-1$
                buffer.append((long) yS[dataIndex]);
                return buffer.toString();
            }
        }
        return null;
    }

    /**
     * Parameter class
     */
    protected static class Parameter {
        /* A series index */
        private int seriesIndex;
        /* A data point index within a series */
        private int dataIndex;

        /**
         * @return the series index
         */
        public int getSeriesIndex() {
            return seriesIndex;
        }

        /**
         * @param seriesIndex
         *            index the seriesIndex to set
         */
        public void setSeriesIndex(int seriesIndex) {
            this.seriesIndex = seriesIndex;
        }

        /**
         * @return the data index
         */
        public int getDataIndex() {
            return dataIndex;
        }

        /**
         * @param dataIndex
         *            the data index to set
         */
        public void setDataIndex(int dataIndex) {
            this.dataIndex = dataIndex;
        }

    }
}