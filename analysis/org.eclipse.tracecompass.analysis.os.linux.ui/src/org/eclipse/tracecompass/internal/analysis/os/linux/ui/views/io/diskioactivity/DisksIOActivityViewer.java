/**********************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.io.diskioactivity;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swtchart.Chart;
import org.eclipse.tracecompass.common.core.format.DataSpeedWithUnitFormat;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.DisksIODataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;

/**
 * Disk IO Activity viewer, shows read and write bandwidth used over time.
 *
 * @author Houssem Daoud
 */
public class DisksIOActivityViewer extends TmfFilteredXYChartViewer {

    /**
     * Constructor
     *
     * @param parent
     *            parent view
     * @param settings
     *            See {@link TmfXYChartSettings} to know what it contains
     */
    public DisksIOActivityViewer(@Nullable Composite parent, TmfXYChartSettings settings) {
        super(parent, settings, DisksIODataProvider.ID);
        Chart chart = getSwtChart();
        chart.getAxisSet().getYAxis(0).getTick().setFormat(DataSpeedWithUnitFormat.getInstance());
        chart.getLegend().setPosition(SWT.LEFT);
    }

}
