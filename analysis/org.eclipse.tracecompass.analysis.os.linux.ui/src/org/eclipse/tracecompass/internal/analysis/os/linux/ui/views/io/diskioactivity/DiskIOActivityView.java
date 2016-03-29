/**********************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.io.diskioactivity;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

/**
 * Main view to show the Disk IO Activity
 *
 * @author Houssem Daoud
 */
public class DiskIOActivityView extends TmfChartView {

    /** ID string */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.diskioactivity"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public DiskIOActivityView() {
        super(Messages.DiskIOActivityView_Title);
    }

    @Override
    protected TmfXYChartViewer createChartViewer(@Nullable Composite parent) {
        return new DisksIOActivityViewer(parent);
    }
}
