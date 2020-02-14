/**********************************************************************
 * Copyright (c) 2014, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.views.memusage;

import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.memory.MemoryUsageView2;
import org.eclipse.tracecompass.internal.lttng2.ust.core.analysis.memory.UstMemoryUsageDataProvider;
import org.eclipse.tracecompass.tmf.ui.viewers.xychart.linechart.TmfXYChartSettings;

/**
 * UST Memory Usage View
 *
 * @author Matthew Khouzam
 */
public class UstMemoryUsageView extends MemoryUsageView2 {

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
