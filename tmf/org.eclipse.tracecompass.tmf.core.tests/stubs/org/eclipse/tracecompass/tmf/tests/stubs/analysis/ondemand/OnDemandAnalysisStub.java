/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis.ondemand;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.IOnDemandAnalysis;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub2;

/**
 * Stub for on-demand analysis tests.
 *
 * It applies to trace type {@link TmfTraceStub2} only.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public class OnDemandAnalysisStub implements IOnDemandAnalysis {

    @Override
    public String getName() {
        return "Test On-Demand Analysis";
    }

    @Override
    public boolean appliesTo(ITmfTrace trace) {
        if (trace instanceof TmfTraceStub2) {
            return true;
        }
        return false;
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {
        return appliesTo(trace);
    }

    @Override
    public Object execute(ITmfTrace trace, @Nullable TmfTimeRange range, String extraParams, @Nullable IProgressMonitor monitor) {
        /* Do nothing */
        return new Object();
    }

    @Override
    public boolean isUserDefined() {
        return false;
    }
}
