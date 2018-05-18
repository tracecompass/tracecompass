/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.AbstractSegmentStatisticsAnalysis;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.SymbolAspect;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.collect.Iterables;

/**
 * Call graph statistics analysis used to get statistics on each function type.
 *
 * @author Matthew Khouzam
 */
public class CallGraphStatisticsAnalysis extends AbstractSegmentStatisticsAnalysis {

    /** The analysis module ID */
    public static final String ID = CallGraphAnalysis.ID + ".statistics"; //$NON-NLS-1$

    @Override
    protected @Nullable ISegmentStoreProvider getSegmentProviderAnalysis(@NonNull ITmfTrace trace) {
        Iterable<CallStackAnalysis> csModules = TmfTraceUtils.getAnalysisModulesOfClass(trace, CallStackAnalysis.class);
        return Iterables.getFirst(csModules, null);
    }

    @Override
    protected @Nullable String getSegmentType(@NonNull ISegment segment) {
        return String.valueOf(SymbolAspect.SYMBOL_ASPECT.resolve(segment));
    }
}
