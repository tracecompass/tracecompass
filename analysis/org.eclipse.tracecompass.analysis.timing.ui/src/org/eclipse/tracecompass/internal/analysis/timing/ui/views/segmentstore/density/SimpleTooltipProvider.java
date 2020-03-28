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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.density.AbstractSegmentStoreDensityViewer;
import org.eclipse.tracecompass.common.core.format.SubSecondTimeWithUnitFormat;
import org.eclipse.tracecompass.tmf.core.presentation.RGBAColor;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfAbstractToolTipHandler;
import org.swtchart.Chart;
import org.swtchart.IAxis;
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
    private static final String HTML_COLOR_TOOLTIP = "<span style=\"color:%s;\">%s</span>"; //$NON-NLS-1$

    private final class DensityToolTipHandler extends TmfAbstractToolTipHandler {

        @Override
        public void fill(Control control, MouseEvent event, Point pt) {
            Chart chart = getChart();
            ISeries[] seriesSet = chart.getSeriesSet().getSeries();
            if (seriesSet.length != 0) {

                if (event == null || chart.getAxisSet().getXAxes().length == 0 || chart.getAxisSet().getYAxes().length == 0 || seriesSet.length == 0) {
                    return;
                }
                chart.getPlotArea().setToolTipText(null);
                long x1 = -1;
                long x2 = -1;
                List<String> names = new ArrayList<>();
                List<Long> yValues = new ArrayList<>();
                List<RGB> colors = new ArrayList<>();
                for (ISeries ySeriesProvider : seriesSet) {
                    double[] xValues = ySeriesProvider.getXSeries();
                    if (xValues.length < 2) {
                        continue;
                    }
                    double delta = xValues[1] - xValues[0];
                    IAxis xAxis = chart.getAxisSet().getXAxis(0);
                    double coords = xAxis.getDataCoordinate(event.x);
                    int index = Arrays.binarySearch(xValues, coords);
                    if (index < 0) {
                        index = -index - 2;
                    }
                    double[] ySeries = ySeriesProvider.getYSeries();
                    long y = Math.round(ySeries[index]);
                    if (y > 0) {
                        x1 = (long) xValues[index];
                        x2 = (long) (x1 + delta);
                        String id = ySeriesProvider.getId();
                        colors.add(getDensityViewer().getColorForItem(id));
                        names.add(id);
                        yValues.add(y);
                    }
                }
                if (!names.isEmpty()) {
                    addItem(Messages.SimpleTooltipProvider_duration, FORMAT.format(x1) + '-' + FORMAT.format(x2));
                    if (seriesSet.length == 1) {
                        // No Color if there's only one
                        addItem(null, ToolTipString.fromString(String.valueOf(Messages.SimpleTooltipProvider_count)), ToolTipString.fromDecimal(yValues.get(0)));
                    } else {
                        for (int i = 0; i < names.size(); i++) {
                            String id = names.get(i);
                            RGB color = getDensityViewer().getColorForItem(id);
                            addItem(null, ToolTipString.fromHtml(String.format(HTML_COLOR_TOOLTIP, new RGBAColor(color.red, color.green, color.blue).toString(), id)), ToolTipString.fromDecimal(yValues.get(i)));
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