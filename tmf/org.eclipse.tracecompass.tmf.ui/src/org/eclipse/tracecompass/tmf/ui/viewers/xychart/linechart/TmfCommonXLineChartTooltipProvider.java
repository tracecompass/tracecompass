/*******************************************************************************
 * Copyright (c) 2014, 2020 École Polytechnique de Montréal and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart;

import java.text.Format;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGBA;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.internal.tmf.ui.Messages;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IAxis;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.ITmfChartTimeProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IXYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfBaseProvider;

/**
 * Displays a tooltip on line charts. For each series, it shows the y value at
 * the selected x value. This tooltip assumes that all series share a common set
 * of X axis values. If the X series is not common, the tooltip text may not be
 * accurate.
 *
 * @since 6.0
 */
public class TmfCommonXLineChartTooltipProvider extends TmfBaseProvider implements MouseTrackListener {

    private final class XYToolTipHandler extends TmfAbstractToolTipHandler {
        private static final String HTML_COLOR_TOOLTIP = "<span style=\"color:%s;\">%s</span>"; //$NON-NLS-1$

        private boolean isValid(int index, IXYSeries serie) {
            double[] ySeries = serie.getYSeries();
            return serie.isVisible() && ySeries != null && ySeries.length > index;
        }

        @Override
        public void fill(Control control, MouseEvent event, Point pt) {
            if (getChartViewer().getWindowDuration() != 0) {
                IAxis xAxis = getXAxis();

                double xCoordinate = xAxis.getDataCoordinate(pt.x);

                List<IXYSeries> series = getSeries();

                if ((xCoordinate < 0) || (series.isEmpty())) {
                    return;
                }

                /* Find the index of the value we want */
                double[] xS = series.get(0).getXSeries();
                if (xS == null) {
                    return;
                }
                int index = Arrays.binarySearch(xS, xCoordinate);
                index = index >= 0 ? index : -index - 1;
                int maxLen = 0;
                for (IXYSeries serie : series) {
                    /* Make sure the series values and the value at index exist */
                    if (isValid(index, serie)) {
                        maxLen = Math.max(maxLen, serie.getId().length());
                    }
                }

                TmfCommonXAxisChartViewer viewer = null;
                Format format = null;
                ITmfChartTimeProvider timeProvider = getChartViewer();
                if (timeProvider instanceof TmfCommonXAxisChartViewer) {
                    viewer = (TmfCommonXAxisChartViewer) timeProvider;
                    format = viewer.getSwtChart().getAxisSet().getYAxes()[0].getTick().getFormat();
                }
                ITmfTimestamp time = TmfTimestamp.fromNanos((long) xCoordinate + getChartViewer().getTimeOffset());
                addItem(null, ToolTipString.fromString(Messages.TmfCommonXLineChartTooltipProvider_time), ToolTipString.fromTimestamp(time.toString(), time.toNanos()));
                /* For each series, get the value at the index */
                for (IXYSeries serie : series) {
                    double[] yS = serie.getYSeries();
                    /* Make sure the series values and the value at index exist */
                    if (isValid(index, serie)) {
                        String key = serie.getId();
                        Color color = serie.getColor();
                        if (key != null && color != null && viewer != null) {
                            RGBA rgba = color.getRGBA();
                            RGBAColor rgbaColor = new RGBAColor(rgba.rgb.red, rgba.rgb.green, rgba.rgb.blue, rgba.alpha);
                            key = String.format(HTML_COLOR_TOOLTIP, rgbaColor, key);
                        }
                        if (key == null) {
                            key = ""; //$NON-NLS-1$
                        }
                        double yValue = yS[index];
                        if (format == null) {
                            addItem(null, ToolTipString.fromHtml(key), ToolTipString.fromDecimal(yValue));
                        } else {
                            addItem(null, ToolTipString.fromHtml(key), ToolTipString.fromString(format.format(yValue)));
                        }
                    }
                }
            }
        }

    }

    private XYToolTipHandler fToolTipHandler = new XYToolTipHandler();

    /**
     * Constructor for the tooltip provider
     *
     * @param tmfChartViewer
     *            The parent chart viewer
     */
    public TmfCommonXLineChartTooltipProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
        register();
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------

    @Override
    public TmfAbstractToolTipHandler getTooltipHandler() {
        return fToolTipHandler;
    }

    @Override
    public void refresh() {
        // nothing to do
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener
    // ------------------------------------------------------------------------

    /**
     * @deprecated, do not extend, use as-is
     */
    @Deprecated
    @Override
    public void mouseEnter(MouseEvent e) {
        // do nothing
    }

    /**
     * @deprecated, do not extend, use as-is
     */
    @Deprecated
    @Override
    public void mouseExit(MouseEvent e) {
        // do nothing
    }

    /**
     * @deprecated, do not extend, use as-is
     */
    @Deprecated
    @Override
    public void mouseHover(MouseEvent e) {
        // do nothing
    }
}
