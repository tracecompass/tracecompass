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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import org.eclipse.swt.widgets.Composite;

/**
 * View to display latency statistics.
 *
 * @author Bernd Hufmann
 *
 */
public class SystemCallLatencyStatisticsView extends AbstractSegmentStoreStatisticsView {

    /** The view ID*/
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.ui.views.latency.statsview"; //$NON-NLS-1$

    @Override
    protected AbstractSegmentStoreStatisticsViewer createSegmentStoreStatisticsViewer(Composite parent) {
        return checkNotNull((AbstractSegmentStoreStatisticsViewer) new SystemCallLatencyStatisticsViewer(checkNotNull(parent)));
    }

}
