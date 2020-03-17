/*******************************************************************************
 * Copyright (c) 2013, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Bernd Hufmann - Updated to new TMF chart framework
 *******************************************************************************/
package org.eclipse.tracecompass.examples.ui.viewers.histogram;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.tmf.core.histogram.HistogramDataProvider;
import org.eclipse.tracecompass.tmf.core.model.OutputElementStyle;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;
import org.eclipse.tracecompass.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.barcharts.TmfHistogramTooltipProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.swtchart.IAxisSet;
import org.swtchart.LineStyle;

/**
 * Histogram Viewer implementation based on TmfBarChartViewer.
 *
 * @author Alexandre Montplaisir
 * @author Bernd Hufmann
 */
public class NewHistogramViewer extends TmfFilteredXYChartViewer {

    private static final int DEFAULT_SERIES_WIDTH = 1;

    /**
     * Creates a Histogram Viewer instance.
     *
     * @param parent
     *            The parent composite to draw in.
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public NewHistogramViewer(Composite parent, TmfXYChartSettings settings) {
        super(parent, settings, HistogramDataProvider.ID);

        /* Hide the grid */
        IAxisSet axisSet = getSwtChart().getAxisSet();
        axisSet.getXAxis(0).getGrid().setStyle(LineStyle.NONE);
        axisSet.getYAxis(0).getGrid().setStyle(LineStyle.NONE);

        setTooltipProvider(new TmfHistogramTooltipProvider(this));
    }

    @Deprecated
    @Override
    public IYAppearance getSeriesAppearance(@NonNull String seriesName) {
        return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.BAR, DEFAULT_SERIES_WIDTH);
    }

    @Override
    public @NonNull OutputElementStyle getSeriesStyle(@NonNull Long seriesId) {
        return getPresentationProvider().getSeriesStyle(seriesId, StyleProperties.SeriesType.BAR, DEFAULT_SERIES_WIDTH);
    }
}
