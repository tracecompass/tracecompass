/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.tests.stubs;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
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

    private final @Nullable ITmfStateSystem fSs;

    /**
     * Default constructor
     */
    public CallStackAnalysisStub() {
        fSs = null;
    }

    /**
     * Constructor, with a pre-filled state system
     *
     * @param stateSystem The pre-filled state system
     */
    public CallStackAnalysisStub(ITmfStateSystem stateSystem) {
        fSs = stateSystem;
    }

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

    @Override
    public @Nullable ITmfStateSystem getStateSystem() {
        ITmfStateSystem ss = fSs;
        return ss == null ? super.getStateSystem() : ss;
    }

}
