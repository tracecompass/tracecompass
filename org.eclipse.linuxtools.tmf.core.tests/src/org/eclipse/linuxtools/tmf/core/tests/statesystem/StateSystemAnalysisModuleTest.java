/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statesystem;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.linuxtools.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test the {@link TmfStateSystemAnalysisModule} class
 *
 * @author Geneviève Bastien
 */
public class StateSystemAnalysisModuleTest {

    /** Time-out tests after 20 seconds */
    @Rule
    public TestRule globalTimeout= new Timeout(20000);

    /** ID of the test state system analysis module */
    public static final String MODULE_SS = "org.eclipse.linuxtools.tmf.core.tests.analysis.sstest";

    private TmfTraceStub fTrace;

    /**
     * Setup test trace
     */
    @Before
    public void setupTraces() {
        fTrace = (TmfTraceStub) TmfTestTrace.A_TEST_10K.getTrace();
    }

    /**
     * Some tests use traces, let's clean them here
     */
    @After
    public void cleanupTraces() {
        TmfTestTrace.A_TEST_10K.dispose();
    }

    /**
     * Test the state system module execution and result
     */
    @Test
    public void testSsModule() {
        TmfSignalManager.deregister(fTrace);
        fTrace.traceOpened(new TmfTraceOpenedSignal(this, fTrace, null));

        TmfStateSystemAnalysisModule module = (TmfStateSystemAnalysisModule) fTrace.getAnalysisModule(MODULE_SS);
        ITmfStateSystem ss = null;
        ss = module.getStateSystem();
        assertNull(ss);
        module.schedule();
        if (module.waitForCompletion(new NullProgressMonitor())) {
            ss = module.getStateSystem();
            assertNotNull(ss);
        } else {
            fail("Module did not complete properly");
        }
    }

}
