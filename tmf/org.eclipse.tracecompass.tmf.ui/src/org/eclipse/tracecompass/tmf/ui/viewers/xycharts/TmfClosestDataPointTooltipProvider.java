/**********************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.tmf.ui.viewers.xycharts;

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;
import org.swtchart.Chart;
import org.swtchart.IAxis;
import org.swtchart.ISeries;

/**
 * Abstract tooltip provider for xy chart viewers. It displays the y value and y
 * value of the data point of the mouse position. Extending classes can provide
 * a custom tooltip text.
 *
 * @author Bernd Hufmann
 * @since 2.0
 * @deprecated use {@link org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfClosestDataPointTooltipProvider}
 */
@Deprecated
public class TmfClosestDataPointTooltipProvider extends TmfBaseProvider implements MouseMoveListener, PaintListener {

    private static final @NonNull String OLD_TOOLTIP = ""; //$NON-NLS-1$

    private final class XYToolTipHandler extends TmfAbstractToolTipHandler {
        @Override
        public void fill(Control control, MouseEvent e, Point pt) {
            if ((getChartViewer().getWindowDuration() != 0) && (e != null)) {
                Chart chart = getChart();
                IAxis xAxis = chart.getAxisSet().getXAxis(0);
                IAxis yAxis = chart.getAxisSet().getYAxis(0);

                ISeries[] series = chart.getSeriesSet().getSeries();

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
                         * only consider it if the mouse is close the data
                         * point.
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
                Map<String, Map<String, Object>> tooltip = null;
                if (param != null) {
                    tooltip = createToolTipMap(param);
                    if (tooltip == null) {
                        return;
                    }
                    fIsHighlight = true;
                    chart.redraw();
                }
                if (tooltip == null) {
                    return;
                }
                /*
                 * Note that tooltip might be null which will clear the previous
                 * tooltip string. This is intentional.
                 */
                for (Entry<String, Map<String, Object>> entry : tooltip.entrySet()) {
                    ToolTipString category = entry.getKey().isEmpty() || entry.getKey().equals(OLD_TOOLTIP) ? null : ToolTipString.fromString(entry.getKey());
                    for (Entry<String, Object> secondEntry : entry.getValue().entrySet()) {

                        Object value = secondEntry.getValue();
                        String key = secondEntry.getKey();
                        if (value instanceof Number) {
                            addItem(category, ToolTipString.fromString(key), ToolTipString.fromDecimal((Number) value));
                        } else if (value instanceof ITmfTimestamp) {
                            addItem(category, ToolTipString.fromString(key), ToolTipString.fromTimestamp(String.valueOf(value), ((ITmfTimestamp) value).toNanos()));
                        } else {
                            addItem(category, ToolTipString.fromString(key), ToolTipString.fromString(String.valueOf(value)));
                        }
                    }
                }
            }
        }
    }

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
    /** Tooltip handler */
    private TmfAbstractToolTipHandler fTooltipHandler = new XYToolTipHandler();

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    /**
     * Constructor for a tool tip provider.
     *
     * @param tmfChartViewer
     *            - the parent chart viewer
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
        Chart chart = getChart();
        chart.getPlotArea().addMouseMoveListener(this);
        chart.getPlotArea().addPaintListener(this);
        fTooltipHandler.activateHoverHelp(chart.getPlotArea());
    }

    @Override
    public void deregister() {

        Chart chart = getChart();
        if ((chart != null) && !chart.isDisposed()) {
            chart.getPlotArea().removeMouseMoveListener(this);
            chart.getPlotArea().removePaintListener(this);
            fTooltipHandler.deactivateHoverHelp(chart.getPlotArea());
        }
    }

    @Override
    public void refresh() {
        // nothing to do
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
     * @return the tooltip map based on the given parameter. The Map<String,
     *         Map<String, Object>> can be seen as a table. The first element is
     *         the category, the second is the key, third is the value.
     * @since 5.0
     */
    protected @Nullable Map<@NonNull String, @NonNull Map<@NonNull String, @NonNull Object>> createToolTipMap(@NonNull Parameter param) {
        ISeries[] series = getChart().getSeriesSet().getSeries();
        int seriesIndex = param.getSeriesIndex();
        int dataIndex = param.getDataIndex();
        if ((series != null) && (seriesIndex < series.length)) {
            ISeries serie = series[seriesIndex];
            double[] xS = serie.getXSeries();
            double[] yS = serie.getYSeries();
            if ((xS != null) && (yS != null) && (dataIndex < xS.length) && (dataIndex < yS.length)) {
                StringBuilder buffer = new StringBuilder();
                buffer.append("x="); //$NON-NLS-1$
                buffer.append(TmfTimestamp.fromNanos((long) xS[dataIndex] + getChartViewer().getTimeOffset()).toString());
                buffer.append('\n');
                buffer.append("y="); //$NON-NLS-1$
                buffer.append((long) yS[dataIndex]);
                return Collections.singletonMap(OLD_TOOLTIP, Collections.singletonMap(OLD_TOOLTIP, buffer.toString()));
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