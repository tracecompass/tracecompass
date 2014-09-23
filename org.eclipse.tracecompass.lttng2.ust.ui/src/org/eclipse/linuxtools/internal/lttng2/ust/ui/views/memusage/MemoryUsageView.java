/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.ust.ui.views.memusage;

import org.eclipse.linuxtools.tmf.ui.views.TmfChartView;
import org.eclipse.swt.widgets.Composite;

/**
 * Memory Usage View
 *
 * @author Matthew Khouzam
 */
public class MemoryUsageView extends TmfChartView {

    /** ID string */
    public static final String ID = "org.eclipse.linuxtools.lttng2.ust.memoryusage"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public MemoryUsageView() {
        super(Messages.MemoryUsageView_Title);
    }

    @Override
    public void createPartControl(Composite parent) {
        setChartViewer( new MemoryUsageViewer(parent));
        super.createPartControl(parent);
    }

    @Override
    public void setFocus() {
    }
}
