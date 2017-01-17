/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.latency.statistics;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentsStatisticsView;
import org.eclipse.tracecompass.tmf.ui.viewers.tree.AbstractTmfTreeViewer;

/**
 * View to display latency statistics.
 *
 * @author Bernd Hufmann
 *
 */
public class SystemCallLatencyStatisticsView extends AbstractSegmentsStatisticsView {

    /** The view ID*/
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.ui.views.latency.statsview"; //$NON-NLS-1$

    @Override
    protected AbstractTmfTreeViewer createSegmentStoreStatisticsViewer(Composite parent) {
        return new SystemCallLatencyStatisticsViewer(parent);
    }

}
