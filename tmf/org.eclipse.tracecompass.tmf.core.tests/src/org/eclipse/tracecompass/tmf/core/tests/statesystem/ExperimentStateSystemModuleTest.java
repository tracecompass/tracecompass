/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
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

package org.eclipse.tracecompass.tmf.core.tests.statesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.concurrent.TimeUnit;

import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestExperimentAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfExperimentStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Test the {@link TmfStateSystemAnalysisModule} class for an experiment
 *
 * @author Geneviève Bastien
 */
public class ExperimentStateSystemModuleTest {

    /** Time-out tests after some time */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    /** ID of the test state system analysis module */
    public static final String MODULE_SS = "org.eclipse.linuxtools.tmf.core.tests.experiment";

    private TmfStateSystemAnalysisModule fModule;
    private TmfExperiment fExperiment;

    /**
     * Setup test trace
     */
    @Before
    public void setupTraces() {
        ITmfTrace trace = TmfTestTrace.A_TEST_10K.getTrace();
        TmfSignalManager.deregister(trace);
        ITmfTrace trace2 = TmfTestTrace.A_TEST_10K2.getTrace();
        TmfSignalManager.deregister(trace2);
        ITmfTrace[] traces = { trace, trace2 };
        fExperiment = new TmfExperimentStub("Test", traces, 1000);
        fExperiment.traceOpened(new TmfTraceOpenedSignal(this, fExperiment, null));

        fModule = (TmfStateSystemAnalysisModule) fExperiment.getAnalysisModule(MODULE_SS);
        assertNotNull(fModule);
    }

    /**
     * Some tests use traces, let's clean them here
     */
    @After
    public void cleanupTraces() {
        fExperiment.dispose();
    }

    /**
     * Test the state system module execution and result
     */
    @Test
    public void testSsModule() {
        ITmfStateSystem ss = fModule.getStateSystem();
        assertNull(ss);
        fModule.schedule();
        if (fModule.waitForCompletion()) {
            ss = fModule.getStateSystem();
            assertNotNull(ss);
            try {
                int quark = ss.getQuarkAbsolute(TestExperimentAnalysis.TRACE_QUARK_NAME);
                ITmfStateInterval interval = ss.querySingleState(ss.getCurrentEndTime(), quark);
                assertEquals(2, interval.getStateValue().unboxInt());
            } catch (AttributeNotFoundException e) {
                fail("The quark for number of traces does not exist");
            } catch (StateSystemDisposedException e) {
                fail("Error: state system disposed");
            }
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
        assertNull(fModule.getStateSystem());
        fModule.schedule();

        assertTrue("Initialization succeeded", fModule.waitForInitialization());
        assertNotNull(fModule.getStateSystem());
    }

}
