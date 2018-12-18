/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.tests.stubs;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A call stack analysis stub, using a call stack state provider stub
 *
 * @author Geneviève Bastien
 */
public class CallStackAnalysisStub extends CallStackAnalysis {

    /**
     * The ID of this analysis
     */
    public static final @NonNull String ID = "org.eclipse.tracecompass.profiling.callstack.test";

    @Override
    protected @NonNull ITmfStateProvider createStateProvider() {
        ITmfTrace trace = getTrace();
        if (trace == null) {
            throw new NullPointerException();
        }
        return new CallStackProviderStub(trace);
    }

    @Override
    public List<String[]> getPatterns() {
        return super.getPatterns();
    }

}
