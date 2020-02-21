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
import org.eclipse.tracecompass.tmf.ui.viewers.TmfViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

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
        TmfXYChartSettings settings = new TmfXYChartSettings(Messages.DiskIOActivityViewer_Title, Messages.DiskIOActivityViewer_XAxis, Messages.DiskIOActivityViewer_YAxis, RESOLUTION);
        return new DisksIOActivityViewer(parent, settings);
    }

    @Override
    protected @NonNull TmfViewer createLeftChildViewer(@Nullable Composite parent) {
        return new DiskIOActivityTreeViewer(Objects.requireNonNull(parent));
    }
}
