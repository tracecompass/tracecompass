/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.statesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
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
    public TestRule globalTimeout= new Timeout(20, TimeUnit.SECONDS);

    /** ID of the test state system analysis module */
    public static final String MODULE_SS = "org.eclipse.linuxtools.tmf.core.tests.analysis.sstest";

    private TmfStateSystemAnalysisModule module;

    /**
     * Setup test trace
     */
    @Before
    public void setupTraces() {
        TmfTraceStub trace = (TmfTraceStub) TmfTestTrace.A_TEST_10K.getTrace();
        TmfSignalManager.deregister(trace);
        trace.traceOpened(new TmfTraceOpenedSignal(this, trace, null));

        module = (TmfStateSystemAnalysisModule) trace.getAnalysisModule(MODULE_SS);
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
        ITmfStateSystem ss = module.getStateSystem();
        assertNull(ss);
        module.schedule();
        if (module.waitForCompletion()) {
            ss = module.getStateSystem();
            assertNotNull(ss);
        } else {
            fail("Module did not complete properly");
        }
    }

    /**
     * Make sure that the state system is initialized after calling
     * {@link TmfStateSystemAnalysisModule#waitForInitialization()}.
     */
    @Test
    public void testInitialization() {
        assertNull(module.getStateSystem());
        module.schedule();

        assertTrue("Initialization succeeded", module.waitForInitialization());
        assertNotNull(module.getStateSystem());
    }

    /**
     * Test that helper returns the right properties
     */
    @Test
    public void testProperties() {

        TmfStateSystemAnalysisModule mod = module;
        assertNotNull(mod);

        /* The stub state system has in mem backend so only 1 property */
        Map<String, String> properties = mod.getProperties();
        assertEquals(1, properties.size());

    }

}
