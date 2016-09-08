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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tracecompass.analysis.timing.ui.views.segmentstore.statistics.AbstractSegmentStoreStatisticsViewer;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics.SystemCallLatencyStatisticsAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;

/**
 * A tree viewer implementation for displaying latency statistics
 *
 * @author Bernd Hufmann
 *
 */
public class SystemCallLatencyStatisticsViewer extends AbstractSegmentStoreStatisticsViewer {

    private static final String SYSCALL_LEVEL = checkNotNull(Messages.LatencyStatistics_SyscallLevelName);

    /**
     * Constructor
     *
     * @param parent
     *            the parent composite
     */
    public SystemCallLatencyStatisticsViewer(Composite parent) {
        super(parent);
    }

    /**
     * Gets the statistics analysis module
     *
     * @return the statistics analysis module
     */
    @Override
    protected @Nullable TmfAbstractAnalysisModule createStatisticsAnalysiModule() {
        SystemCallLatencyStatisticsAnalysisModule module = new SystemCallLatencyStatisticsAnalysisModule();
        return module;
    }

    @Override
    protected @NonNull final String getTotalLabel() {
        return checkNotNull(Messages.LatencyStatistics_TotalLabel);
    }

    @Override
    protected @NonNull final String getTypeLabel() {
        return SYSCALL_LEVEL;
    }



}
