/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Bernd Hufmann - Use state system analysis module instead of factory
 ******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel.statesystem;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * State system tests using the in-memory back-end.
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemInMemoryTest extends StateSystemTest {

    private static CtfTmfTrace trace;
    private static TestLttngKernelAnalysisModule module;

    /**
     * Test class setup
     */
    @BeforeClass
    public static void initialize() {
        CtfTmfTrace thetrace = CtfTmfTestTraceUtils.getTrace(testTrace);
        trace = thetrace;

        module = new TestLttngKernelAnalysisModule();
        try {
            assertTrue(module.setTrace(thetrace));
        } catch (TmfAnalysisException e) {
            fail();
        }
        module.schedule();
        assertTrue(module.waitForCompletion());

        fixture = module.getStateSystem();
    }

    /**
     * Class cleanup
     */
    @AfterClass
    public static void cleanup() {
        if (module != null) {
            module.dispose();
        }
        if (fixture != null) {
            fixture.dispose();
        }
        if (trace != null) {
            trace.dispose();
        }
        module = null;
        fixture = null;
        trace = null;
    }

    private static class TestLttngKernelAnalysisModule extends KernelAnalysisModule {

        /**
         * Constructor adding the views to the analysis
         */
        public TestLttngKernelAnalysisModule() {
            super();
        }

        @Override
        public boolean setTrace(ITmfTrace trace) throws TmfAnalysisException {
            if (!(trace instanceof CtfTmfTrace)) {
                return false;
            }
            return super.setTrace(trace);
        }

        @Override
        protected StateSystemBackendType getBackendType() {
            return StateSystemBackendType.INMEM;
        }
    }
}
