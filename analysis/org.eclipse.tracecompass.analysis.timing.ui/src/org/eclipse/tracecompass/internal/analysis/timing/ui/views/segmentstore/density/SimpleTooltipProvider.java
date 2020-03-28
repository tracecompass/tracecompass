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
import java.util.Arrays;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;
import org.swtchart.Chart;
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
                double[] xValues = series.getXSeries();
                if (xValues.length < 2) {
                    return;
                }
                double delta = xValues[1] - xValues[0];
                double coords = chart.getAxisSet().getXAxis(0).getDataCoordinate(event.x);
                int index = Arrays.binarySearch(xValues, coords);
                if (index < 0) {
                    index = -index - 2;
                }
                if (index < 0) {
                    return;
                }
                long x1 = (long) xValues[index];
                long x2 = (long) (x1 + delta);
                long y = Math.round(series.getYSeries()[index]);
                if (y > 0) {
                    addItem(Messages.SimpleTooltipProvider_duration, FORMAT.format(x1) + '-' + FORMAT.format(x2));
                    addItem(null, ToolTipString.fromString(Messages.SimpleTooltipProvider_count), ToolTipString.fromDecimal(y));
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