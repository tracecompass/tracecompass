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

package org.eclipse.tracecompass.analysis.profiling.core.tests;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.profiling.core.tests.data.CallStackTestData;
import org.eclipse.tracecompass.analysis.profiling.core.tests.stubs.CallStackAnalysisStub;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.After;
import org.junit.Before;

/**
 * Base class for call stack tests. It sets up the trace and analysis module.
 *
 * @author Geneviève Bastien
 */
public class CallStackTestBase {

    private final CallStackTestData fTraceData;
    private CallStackAnalysisStub fModule;

    /**
     * Constructor, receives the callstack test data
     *
     * @param data The test data
     */
    public CallStackTestBase(CallStackTestData data) {
        fTraceData = data;
    }

    /**
     * Setup the trace for the tests
     */
    @Before
    public void setUp() {
        ITmfTrace trace = fTraceData.getTrace();

        CallStackAnalysisStub module = TmfTraceUtils.getAnalysisModuleOfClass(trace, CallStackAnalysisStub.class, CallStackAnalysisStub.ID);
        assertNotNull(module);

        module.schedule();
        assertTrue(module.waitForCompletion());
        fModule = module;
    }

    /**
     * Dispose of the test data
     */
    @After
    public void tearDown() {
        CallStackTestData traceData = fTraceData;
        if (traceData != null) {
            traceData.dispose();
        }
        CallStackAnalysis module = fModule;
        if (module != null) {
            module.dispose();
        }
    }

    /**
     * Get the analysis module. Its execution is complete.
     *
     * @return The analysis module
     */
    public CallStackAnalysisStub getModule() {
        return fModule;
    }

    /**
     * Get the trace data for this test case
     *
     * @return The trace data used for this test
     */
    public @NonNull CallStackTestData getTraceData() {
        CallStackTestData traceData = fTraceData;
        if (traceData == null) {
            throw new NullPointerException();
        }
        return traceData;
    }

}
