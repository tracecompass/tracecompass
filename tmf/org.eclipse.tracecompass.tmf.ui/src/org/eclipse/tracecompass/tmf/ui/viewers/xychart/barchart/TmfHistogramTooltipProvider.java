/**********************************************************************
 * Copyright (c) 2013, 2020 Ericsson
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
package org.eclipse.tracecompass.tmf.ui.viewers.xychart.barchart;

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IAxis;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.ITmfChartTimeProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.IXYSeries;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfBaseProvider;

/**
 * Tool tip provider for TMF bar chart viewer. It displays the y value of
 * position x as well as it highlights the bar of the x position.
 * It only considers the first series of the chart.
 *
 * @author Bernd Hufmann
 * @since 6.0
 */
public class TmfHistogramTooltipProvider extends TmfBaseProvider implements MouseTrackListener, MouseMoveListener, PaintListener {

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
    public TmfHistogramTooltipProvider(ITmfChartTimeProvider tmfChartViewer) {
        super(tmfChartViewer);
    }

    // ------------------------------------------------------------------------
    // TmfBaseProvider
    // ------------------------------------------------------------------------
    @Override
    public void refresh() {
        // nothing to do
    }

    // ------------------------------------------------------------------------
    // MouseTrackListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseEnter(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseExit(MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseHover(MouseEvent e) {
        if (getChartViewer().getWindowDuration() != 0) {
            IAxis xAxis = getXAxis();
            IAxis yAxis = getYAxis();

            double xCoordinate = xAxis.getDataCoordinate(e.x);

            List<IXYSeries> series = getSeries();

            if ((xCoordinate < 0) || series.isEmpty()) {
                return;
            }

            double y = 0.0;
            double rangeStart = 0.0;
            double rangeEnd = 0.0;

            // Consider first series only
            IXYSeries firstSeries = series.get(0);
            double[] xS = firstSeries.getXSeries();
            double[] yS = firstSeries.getYSeries();

            if ((xS == null) || (yS == null)) {
                return;
            }

            for (int i = 0; i < xS.length - 1; i++) {
                int pixel = xAxis.getPixelCoordinate(xS[i]);
                if (pixel <= e.x) {
                    rangeStart = xS[i];
                    rangeEnd = (long) xS[i + 1];
                    if (xCoordinate >= rangeStart) {
                        y = yS[i + 1];
                    } else {
                        y = yS[i];
                    }
                }
            }

            ITmfChartTimeProvider viewer = getChartViewer();

            /* set tooltip of closest data point */
            StringBuilder builder = new StringBuilder();
            builder.append("Range=["); //$NON-NLS-1$
            builder.append(TmfTimestamp.fromNanos((long) rangeStart + viewer.getTimeOffset()).toString());
            builder.append(',');
            builder.append(TmfTimestamp.fromNanos((long) rangeEnd + viewer.getTimeOffset()).toString());
            builder.append("]\n"); //$NON-NLS-1$
            builder.append("y="); //$NON-NLS-1$
            builder.append((long) y);
            setToolTipText(builder.toString());

            fHighlightX = e.x;
            fHighlightY = yAxis.getPixelCoordinate(y);
            fIsHighlight = true;
            redraw();
        }
    }

    // ------------------------------------------------------------------------
    // MouseMoveListener
    // ------------------------------------------------------------------------
    @Override
    public void mouseMove(MouseEvent e) {
        fIsHighlight = false;
        redraw();
    }

    // ------------------------------------------------------------------------
    // PaintListener
    // ------------------------------------------------------------------------
    @Override
    public void paintControl(PaintEvent e) {
        if (fIsHighlight) {
            e.gc.setBackground(Display.getDefault().getSystemColor(
                    SWT.COLOR_RED));
            e.gc.setAlpha(128);

            e.gc.fillOval(fHighlightX - 5, fHighlightY - 5, 10, 10);
        }
    }

}