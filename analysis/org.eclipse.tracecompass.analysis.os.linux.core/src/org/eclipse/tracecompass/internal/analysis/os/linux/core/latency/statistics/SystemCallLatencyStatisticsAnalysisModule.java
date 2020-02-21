/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Bernd Hufmann - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

/**
 * Analysis module to calculate statistics of a latency analysis
 *
 * @author Bernd Hufmann
 */
public class SystemCallLatencyStatisticsAnalysisModule extends AbstractSegmentStatisticsAnalysis {

    /** The analysis module ID */
    public static final String ID = "org.eclipse.tracecompass.analysis.os.linux.core.latency.statistics.syscall"; //$NON-NLS-1$

    @Override
    protected @Nullable String getSegmentType(@NonNull ISegment segment) {
        if (segment instanceof SystemCall) {
            SystemCall syscall = (SystemCall) segment;
            return syscall.getName();
        }
        return null;
    }

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentProviderAnalysis(@NonNull ITmfTrace trace) {
        return TmfTraceUtils.getAnalysisModuleOfClass(trace, SystemCallLatencyAnalysis.class, SystemCallLatencyAnalysis.ID);
    }

}
