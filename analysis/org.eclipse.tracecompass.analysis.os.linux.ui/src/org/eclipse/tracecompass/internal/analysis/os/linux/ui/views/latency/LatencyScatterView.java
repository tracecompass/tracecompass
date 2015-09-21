/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ********************************************************************************/

package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency;
import static org.eclipse.tracecompass.common.core.NonNullUtils.nullToEmptyString;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.tmf.ui.views.TmfView;

/**
 * Some stuff
 *
 * @author Matthew Khouzam
 */
public class LatencyScatterView extends TmfView {
    // Attributes
    // ------------------------------------------------------------------------

    /** The view's ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.views.latency.scatter"; //$NON-NLS-1$

    private @Nullable LatencyScatterGraphViewer fScatterViewer;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     */
    public LatencyScatterView() {
        super(ID);
    }

    // ------------------------------------------------------------------------
    // ViewPart
    // ------------------------------------------------------------------------

    @Override
    public void createPartControl(@Nullable Composite parent) {
        SashForm sf = new SashForm(parent, SWT.NONE);
        fScatterViewer = new LatencyScatterGraphViewer(sf, nullToEmptyString(Messages.LatencyScatterView_title), nullToEmptyString(Messages.LatencyScatterView_xAxis), nullToEmptyString(Messages.LatencyScatterView_yAxis));
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void setFocus() {
        if (fScatterViewer != null) {
            fScatterViewer.getControl().setFocus();
        }
    }

    @Override
    public void dispose() {
        super.dispose();
        if (fScatterViewer != null) {
            fScatterViewer.dispose();
        }
    }

}
