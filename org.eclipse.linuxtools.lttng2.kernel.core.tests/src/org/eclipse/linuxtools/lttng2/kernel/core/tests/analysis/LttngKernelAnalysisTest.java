/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.analysis;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.List;

import org.eclipse.linuxtools.lttng2.kernel.core.analysis.LttngKernelAnalysisModule;
import org.eclipse.linuxtools.statesystem.core.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link LttngKernelAnalysisModule} class
 *
 * @author Geneviève Bastien
 */
public class LttngKernelAnalysisTest {

    private ITmfTrace fTrace;

    /**
     * Set-up the test
     */
    @Before
    public void setUp() {
        assumeTrue(CtfTmfTestTrace.KERNEL.exists());
        fTrace = CtfTmfTestTrace.KERNEL.getTrace();
    }

    /**
     * Dispose test objects
     */
    @After
    public void tearDown() {
        fTrace.dispose();
    }

    /**
     * Test the LTTng kernel analysis execution
     */
    @Test
    public void testAnalysisExecution() {
        LttngKernelAnalysisModule module = new LttngKernelAnalysisModule();
        module.setId("test");
        try {
            module.setTrace(fTrace);
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        }
        // Assert the state system has not been initialized yet
        ITmfStateSystem ss = module.getStateSystem();
        assertNull(ss);

        assertTrue(TmfTestHelper.executeAnalysis(module));

        ss = module.getStateSystem();
        assertNotNull(ss);

        List<Integer> quarks = ss.getQuarks("*");
        assertFalse(quarks.isEmpty());
    }
}
