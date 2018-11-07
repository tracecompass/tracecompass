/**********************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density;

import java.text.Format;
import java.text.MessageFormat;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackListener;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.swtchart.IAxis;
import org.swtchart.IBarSeries;
import org.swtchart.ISeries;

/**
 * Tool tip provider for density viewer. It displays the x and y value of the
 * current mouse position.
 *
 * @author Bernd Hufmann
 * @author Marc-Andre Laperle
 */
public class SimpleTooltipProvider extends BaseMouseProvider implements MouseTrackListener {

    private static final Format FORMAT = new SubSecondTimeWithUnitFormat();

    /**
     * Constructor for a tool tip provider.
     *
     * @param densityViewer
     *            The parent density viewer
     */
    public SimpleTooltipProvider(AbstractSegmentStoreDensityViewer densityViewer) {
        super(densityViewer);
        register();
    }

    @Override
    public final void register() {
        getChart().getPlotArea().addMouseTrackListener(this);
    }

    @Override
    public final void deregister() {
        if (!getChart().isDisposed()) {
            getChart().getPlotArea().removeMouseTrackListener(this);
        }
    }

    @Override
    public void mouseEnter(@Nullable MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseExit(@Nullable MouseEvent e) {
        // Do nothing
    }

    @Override
    public void mouseHover(@Nullable MouseEvent e) {
        if (e == null || getChart().getAxisSet().getXAxes().length == 0 || getChart().getAxisSet().getYAxes().length == 0 || getDensityViewer().getControl().getSeriesSet().getSeries().length == 0) {
            return;
        }
        ISeries series = getDensityViewer().getControl().getSeriesSet().getSeries()[0];
        getChart().getPlotArea().setToolTipText(null);
        if (series instanceof IBarSeries) {
            IBarSeries barSeries = (IBarSeries) series;
            // Note: getBounds is broken in SWTChart 0.9.0
            Rectangle[] bounds = barSeries.getBounds();

            if (barSeries.getXSeries().length < 2) {
                return;
            }
            double delta = barSeries.getXSeries()[1] - barSeries.getXSeries()[0];
            for (int i = 0; i < bounds.length; i++) {
                Rectangle rec = bounds[i];
                if (rec == null) {
                    continue;
                }
                int start = rec.x;
                int end = start + rec.width;
                if (e.x >= start && e.x <= end) {
                    long x1 = (long) barSeries.getXSeries()[i];
                    long x2 = (long) (x1 + delta);
                    IAxis yAxis = getChart().getAxisSet().getYAxes()[0];
                    long y = Math.round(yAxis.getDataCoordinate(rec.y)) - 1;
                    if (y > 0) {
                        String toolTipText = MessageFormat.format(Messages.SimpleTooltipProvider_toolTipText, FORMAT.format(x1), FORMAT.format(x2), y);
                        getChart().getPlotArea().setToolTipText(toolTipText);
                    }
                    break;
                }
            }
        }
    }
}