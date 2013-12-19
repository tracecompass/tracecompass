/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.linuxtools.tracing.examples.ui.views.histogram;

import org.eclipse.linuxtools.tmf.ui.views.TmfChartView;
import org.eclipse.linuxtools.tracing.examples.ui.viewers.histogram.NewHistogramViewer;
import org.eclipse.swt.widgets.Composite;

/**
 * Histogram View based on TmfChartView.
 *
 * @author Bernd Hufmann
 */
public class NewHistogramView extends TmfChartView {
    /** The view ID. */
    public static final String ID = "org.eclipse.linuxtools.tracing.examples.ui.views.NewHistogramView"; //$NON-NLS-1$

    /**
     * Default Constructor
     */
    public NewHistogramView() {
        super(ID);
    }

    @Override
    public void createPartControl(Composite parent) {
        setChartViewer(new NewHistogramViewer(parent));
        super.createPartControl(parent);
    }

    @Override
    public void setFocus() {
    }
}
