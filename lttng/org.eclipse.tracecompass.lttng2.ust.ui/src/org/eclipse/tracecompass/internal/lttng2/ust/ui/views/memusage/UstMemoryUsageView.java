/**********************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage;

import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.memory.MemoryUsageView;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.memory.UstMemoryUsageDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xycharts.linecharts.TmfXYChartSettings;

/**
 * UST Memory Usage View
 *
 * @author Matthew Khouzam
 */
public class UstMemoryUsageView extends MemoryUsageView {

    /** ID string */
    public static final String ID = "org.eclipse.linuxtools.lttng2.ust.memoryusage"; //$NON-NLS-1$

    /**
     * Constructor
     */
    public UstMemoryUsageView() {
        super(Messages.MemoryUsageViewer_Title, UstMemoryUsageDataProvider.ID,
                new TmfXYChartSettings(Messages.MemoryUsageViewer_Title, Messages.MemoryUsageViewer_XAxis, Messages.MemoryUsageViewer_YAxis, 1));
    }
}
