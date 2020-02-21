/**********************************************************************
 * Copyright (c) 2015, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/
package org.eclipse.tracecompass.internal.analysis.timing.ui.views.segmentstore.density;

import java.text.Format;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;
import org.swtchart.Chart;
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
public class SimpleTooltipProvider extends BaseMouseProvider {

    private static final Format FORMAT = SubSecondTimeWithUnitFormat.getInstance();

    private final class DensityToolTipHandler extends TmfAbstractToolTipHandler {

        @Override
        public void fill(Control control, MouseEvent event, Point pt) {
            Chart chart = getChart();
            if (chart.getSeriesSet().getSeries().length != 0) {
                if (event == null || chart.getAxisSet().getXAxes().length == 0 || chart.getAxisSet().getYAxes().length == 0 || getDensityViewer().getControl().getSeriesSet().getSeries().length == 0) {
                    return;
                }
                ISeries series = getDensityViewer().getControl().getSeriesSet().getSeries()[0];
                chart.getPlotArea().setToolTipText(null);
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
                        if (event.x >= start && event.x <= end) {
                            long x1 = (long) barSeries.getXSeries()[i];
                            long x2 = (long) (x1 + delta);
                            IAxis yAxis = chart.getAxisSet().getYAxes()[0];
                            long y = Math.round(yAxis.getDataCoordinate(rec.y)) - 1;
                            if (y > 0) {
                                addItem(Messages.SimpleTooltipProvider_duration, FORMAT.format(x1) + '-' + FORMAT.format(x2));
                                addItem(null, ToolTipString.fromString(Messages.SimpleTooltipProvider_count), ToolTipString.fromDecimal(y));
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private DensityToolTipHandler fToolTipHandler = new DensityToolTipHandler();

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
    public void register() {
        fToolTipHandler.activateHoverHelp(getChart().getPlotArea());
    }

    @Override
    public void deregister() {
        if (!getDensityViewer().getControl().isDisposed()) {
            fToolTipHandler.deactivateHoverHelp(getChart().getPlotArea());
        }
    }
}