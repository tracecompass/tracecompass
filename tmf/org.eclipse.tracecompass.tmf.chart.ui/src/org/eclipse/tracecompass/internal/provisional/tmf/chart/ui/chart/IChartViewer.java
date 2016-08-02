/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.tmf.chart.ui.chart;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartData;
import org.eclipse.tracecompass.internal.provisional.tmf.chart.core.chart.ChartModel;
import org.eclipse.tracecompass.internal.tmf.chart.ui.swtchart.SwtBarChart;
import org.eclipse.tracecompass.internal.tmf.chart.ui.swtchart.SwtScatterChart;

import com.google.common.collect.ImmutableList;

/**
 * Interface and factory constructor for charts.
 *
 * @author Gabriel-Andrew Pollo-Guilbert
 */
public interface IChartViewer {

    /**
     * FIXME: Use static int[] rather than Color since SWT objects need to be
     * freed manually.
     */

    /**
     * List of standard colors
     */
    public static final List<Color> COLORS = checkNotNull(ImmutableList.of(
            new Color(Display.getDefault(), 72, 120, 207),
            new Color(Display.getDefault(), 106, 204, 101),
            new Color(Display.getDefault(), 214, 95, 95),
            new Color(Display.getDefault(), 180, 124, 199),
            new Color(Display.getDefault(), 196, 173, 102),
            new Color(Display.getDefault(), 119, 190, 219)));

    /**
     * List of light colors
     */
    public static final List<@NonNull Color> COLORS_LIGHT = checkNotNull(ImmutableList.of(
            new Color(Display.getDefault(), 173, 195, 233),
            new Color(Display.getDefault(), 199, 236, 197),
            new Color(Display.getDefault(), 240, 196, 196),
            new Color(Display.getDefault(), 231, 213, 237),
            new Color(Display.getDefault(), 231, 222, 194),
            new Color(Display.getDefault(), 220, 238, 246)));

    /**
     * Dispose the viewer widget.
     */
    void dispose();

    /**
     * Factory method to create a chart.
     *
     * @param parent
     *            Parent composite
     * @param data
     *            Configured data series for the chart
     * @param model
     *            Chart model to use
     * @param title
     *            Title of the chart
     * @return The chart object
     */
    static @Nullable IChartViewer createChart(Composite parent, ChartData data, ChartModel model) {
        switch (model.getChartType()) {
        case BAR_CHART:
            return new SwtBarChart(parent, data, model);
        case SCATTER_CHART:
            return new SwtScatterChart(parent, data, model);
        case PIE_CHART:
            /**
             * TODO
             */
        default:
            return null;
        }
    }

    /**
     * Get the dark color matching the light color
     *
     * @param color
     *            A light color for which to get the dark color
     * @return The corresponding dark color or the first color if the light
     *         color was not found
     */
    static Color getCorrespondingColor(Color color) {
        int index = COLORS_LIGHT.indexOf(color);
        if (index < 0) {
            return COLORS.get(0);
        }
        return COLORS.get(index);
    }

}
