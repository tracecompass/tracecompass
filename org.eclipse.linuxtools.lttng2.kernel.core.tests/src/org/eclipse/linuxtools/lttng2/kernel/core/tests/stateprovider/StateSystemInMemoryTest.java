/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Bernd Hufmann - Use state system analysis module instead of factory
 ******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.LttngKernelStateProvider;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.junit.BeforeClass;

/**
 * State system tests using the in-memory back-end.
 *
 * @author Alexandre Montplaisir
 */
public class StateSystemInMemoryTest extends StateSystemTest {

    /**
     * Initialization
     */
    @BeforeClass
    public static void initialize() {
        assumeTrue(testTrace.exists());

        TestLttngKernelAnalysisModule module = new TestLttngKernelAnalysisModule();
        try {
            module.setTrace(testTrace.getTrace());
        } catch (TmfAnalysisException e) {
            fail();
        }
        module.schedule();
        assertTrue(module.waitForCompletion());
        ssq = module.getStateSystem();
        assertNotNull(ssq);
    }

    private static class TestLttngKernelAnalysisModule extends TmfStateSystemAnalysisModule {

        /**
         * Constructor adding the views to the analysis
         */
        public TestLttngKernelAnalysisModule() {
            super();
        }

        @Override
        public void setTrace(ITmfTrace trace) throws TmfAnalysisException {
            if (!(trace instanceof CtfTmfTrace)) {
                throw new IllegalStateException("TestLttngKernelAnalysisModule: trace should be of type CtfTmfTrace"); //$NON-NLS-1$
            }
            super.setTrace(trace);
        }

        @Override
        protected ITmfStateProvider createStateProvider() {
            return new LttngKernelStateProvider((CtfTmfTrace) getTrace());
        }

        @Override
        protected StateSystemBackendType getBackendType() {
            return StateSystemBackendType.INMEM;
        }
    }
}
