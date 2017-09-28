/**********************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.io.diskioactivity;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.os.linux.core.inputoutput.DisksIODataProvider;
import org.eclipse.tracecompass.common.core.format.DataSpeedWithUnitFormat;
import org.eclipse.tracecompass.internal.provisional.tmf.core.presentation.IYAppearance;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfCommonXAxisChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.swtchart.Chart;

/**
 * Disk IO Activity viewer, shows read and write bandwidth used over time.
 *
 * @author Houssem Daoud
 */
@SuppressWarnings("restriction")
public class DisksIOActivityViewer extends TmfCommonXAxisChartViewer {

    private static final int DEFAULT_SERIES_WIDTH = 1;

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public DisksIOActivityViewer(@Nullable Composite parent, TmfXYChartSettings settings) {
        super(parent, settings);
        Chart chart = getSwtChart();
        chart.getAxisSet().getYAxis(0).getTick().setFormat(DataSpeedWithUnitFormat.getInstance());
        chart.getLegend().setPosition(SWT.LEFT);
    }

    @Override
    public IYAppearance getSeriesAppearance(@NonNull String seriesName) {
        return getPresentationProvider().getAppearance(seriesName, IYAppearance.Type.AREA, DEFAULT_SERIES_WIDTH);
    }

    @Override
    protected void initializeDataProvider() {
        ITmfTrace trace = getTrace();
        setDataProvider(DisksIODataProvider.create(trace));
    }
}
