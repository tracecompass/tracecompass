/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.examples.ui.views.histogram;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.examples.ui.viewers.histogram.NewHistogramViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.TmfXYChartViewer;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;
import org.eclipse.tracecompass.tmf.ui.views.TmfChartView;

/**
 * Histogram View based on TmfChartView.
 *
 * @author Bernd Hufmann
 */
public class NewHistogramView extends TmfChartView {
    /** The view ID. */
    public static final String ID = "org.eclipse.tracecompass.examples.ui.views.NewHistogramView"; //$NON-NLS-1$

    /**
     * Default Constructor
     */
    public NewHistogramView() {
        super(ID);
    }

    @Override
    protected TmfXYChartViewer createChartViewer(Composite parent) {
        return new NewHistogramViewer(parent, new TmfXYChartSettings(null, null, null, 1));
    }
}
