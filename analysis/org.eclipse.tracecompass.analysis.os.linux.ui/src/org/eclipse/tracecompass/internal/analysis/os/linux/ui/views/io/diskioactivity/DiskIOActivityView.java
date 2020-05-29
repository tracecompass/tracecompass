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

import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.inputoutput.DisksIODataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfFilteredXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.xychart.TmfChartView;

/**
 * Main view to show the Disk IO Activity
 *
 * @author Houssem Daoud
 */
public class DiskIOActivityView extends TmfChartView {

    /** ID string */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.diskioactivity"; //$NON-NLS-1$
    private static final double RESOLUTION = 0.2;

    /**
     * Constructor
     */
    public DiskIOActivityView() {
        super(Messages.DiskIOActivityView_Title);
    }

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        TmfXYChartSettings settings = new TmfXYChartSettings(Messages.DiskIOActivityViewer_Title, Messages.DiskIOActivityViewer_XAxis, null, RESOLUTION);
        return new TmfFilteredXYChartViewer(parent, settings, DisksIODataProvider.ID);
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
        return new DiskIOActivityTreeViewer(Objects.requireNonNull(parent));
    }
}
