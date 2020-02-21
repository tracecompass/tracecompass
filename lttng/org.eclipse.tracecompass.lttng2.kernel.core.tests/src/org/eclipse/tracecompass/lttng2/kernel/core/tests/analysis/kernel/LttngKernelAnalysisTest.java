/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.lttng2.lttng.kernel.core.tests.shared.LttngKernelTestTraceUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link KernelAnalysisModule} class
 *
 * @author Geneviève Bastien
 */
public class LttngKernelAnalysisTest {

    private LttngKernelTrace fTrace;
    private KernelAnalysisModule fKernelAnalysisModule;

    /**
     * Set-up the test
     */
    @Before
    public void setUp() {
        fKernelAnalysisModule = new KernelAnalysisModule();
        fTrace = LttngKernelTestTraceUtils.getTrace(CtfTestTrace.KERNEL);
    }

    /**
     * Dispose test objects
     */
    @After
    public void tearDown() {
        LttngKernelTestTraceUtils.dispose(CtfTestTrace.KERNEL);
        fKernelAnalysisModule.dispose();
        fTrace = null;
        fKernelAnalysisModule = null;
    }

    /**
     * Test the LTTng kernel analysis execution
     */
    @Test
    public void testAnalysisExecution() {
        fKernelAnalysisModule.setId("test");
        ITmfTrace trace = fTrace;
        assertNotNull(trace);
        try {
            assertTrue(fKernelAnalysisModule.setTrace(trace));
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }
        // Assert the state system has not been initialized yet
        ITmfStateSystem ss = fKernelAnalysisModule.getStateSystem();
        assertNull(ss);

        assertTrue(TmfTestHelper.executeAnalysis(fKernelAnalysisModule));

        ss = fKernelAnalysisModule.getStateSystem();
        assertNotNull(ss);

        List<Integer> quarks = ss.getQuarks("*");
        assertFalse(quarks.isEmpty());
    }

    /**
     * Test the canExecute method on valid and invalid traces
     */
    @Test
    public void testCanExecute() {
        /* Test with a valid kernel trace */
        assertNotNull(fTrace);
        assertTrue(fKernelAnalysisModule.canExecute(fTrace));

        /* Test with a CTF trace that does not have required events */
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.CYG_PROFILE);
        /*
         * TODO: This should be false, but for now there is no mandatory events
         * in the kernel analysis so it will return true.
         */
        assertTrue(fKernelAnalysisModule.canExecute(trace));
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.CYG_PROFILE);
    }

}
