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

package org.eclipse.tracecompass.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.analysis.AnalysisManagerTest;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperimentUtils;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis;
import org.eclipse.tracecompass.tmf.tests.stubs.analysis.TestAnalysis2;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfExperimentStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test the {@link TmfExperimentUtils} class
 *
 * @author Geneviève Bastien
 */
public class TmfExperimentUtilsTest {

    private static final String EXPERIMENT = "MyExperiment";
    private static int BLOCK_SIZE = 1000;

    private TmfExperimentStub fExperiment;
    private ITmfTrace[] fTraces;

    /**
     * Setup the experiment
     */
    @Before
    public void setupExperiment() {
        fTraces = new ITmfTrace[2];
        fTraces[0] = TmfTestTrace.A_TEST_10K.getTrace();
        fTraces[1] = TmfTestTrace.A_TEST_10K2.getTraceAsStub2();
        /* Re-register the trace to the signal manager */
        TmfSignalManager.register(fTraces[1]);
        fExperiment = new TmfExperimentStub(EXPERIMENT, fTraces, BLOCK_SIZE);
        fExperiment.getIndexer().buildIndex(0, TmfTimeRange.ETERNITY, true);
        fExperiment.broadcast(new TmfTraceOpenedSignal(this, fExperiment, null));
    }

    /**
     * Cleanup after the test
     */
    @After
    public void cleanUp() {
        fExperiment.dispose();
    }

    /**
     * Test the
     * {@link TmfExperimentUtils#getAnalysisModuleForHost(TmfExperiment, String, String)}
     * method
     */
    @Test
    public void testGetModuleById() {
        String commonModule = AnalysisManagerTest.MODULE_PARAM;
        String notCommonModule = AnalysisManagerTest.MODULE_SECOND;
        String host1 = TmfTestTrace.A_TEST_10K.getPath();
        String host2 = TmfTestTrace.A_TEST_10K2.getPath();
        TmfExperiment experiment = fExperiment;
        assertNotNull(experiment);

        /* First module for trace 1 */
        IAnalysisModule module = TmfExperimentUtils.getAnalysisModuleForHost(experiment, host1, commonModule);
        assertNotNull(module);
        IAnalysisModule traceModule = fTraces[0].getAnalysisModule(commonModule);
        assertNotNull(traceModule);
        assertEquals(module, traceModule);

        /* Second inexistent module for trace 1 */
        assertNull(TmfExperimentUtils.getAnalysisModuleForHost(experiment, host1, notCommonModule));
        traceModule = fTraces[0].getAnalysisModule(notCommonModule);
        assertNull(traceModule);

        /* First module for trace 2 */
        module = TmfExperimentUtils.getAnalysisModuleForHost(experiment, host2, commonModule);
        assertNotNull(module);
        traceModule = fTraces[1].getAnalysisModule(commonModule);
        assertNotNull(traceModule);
        assertEquals(module, traceModule);

        /* Second module for trace 2 */
        module = TmfExperimentUtils.getAnalysisModuleForHost(experiment, host2, notCommonModule);
        assertNotNull(module);
        traceModule = fTraces[1].getAnalysisModule(notCommonModule);
        assertNotNull(traceModule);
        assertEquals(module, traceModule);
    }

    /**
     * Test the
     * {@link TmfExperimentUtils#getAnalysisModuleOfClassForHost(TmfExperiment, String, Class)}
     * method
     */
    @Test
    public void testGetModuleByClass() {
        Class<@NonNull TestAnalysis> commonClass = TestAnalysis.class;
        Class<@NonNull TestAnalysis2> notCommonClass = TestAnalysis2.class;
        String host1 = TmfTestTrace.A_TEST_10K.getPath();
        String host2 = TmfTestTrace.A_TEST_10K2.getPath();
        TmfExperiment experiment = fExperiment;
        assertNotNull(experiment);

        /* Common module for trace 1 */
        TestAnalysis module1 = TmfExperimentUtils.getAnalysisModuleOfClassForHost(experiment, host1, commonClass);
        assertNotNull(module1);
        /* Make sure this module belongs to the trace */
        IAnalysisModule sameModule = null;
        for (IAnalysisModule mod : fTraces[0].getAnalysisModules()) {
            if (mod == module1) {
                sameModule = mod;
            }
        }
        assertNotNull(sameModule);

        /* Uncommon module from trace 1 */
        TestAnalysis2 module2 = TmfExperimentUtils.getAnalysisModuleOfClassForHost(experiment, host1, notCommonClass);
        assertNull(module2);

        /* Common module for trace 1 */
        module1 = TmfExperimentUtils.getAnalysisModuleOfClassForHost(experiment, host2, commonClass);
        assertNotNull(module1);
        /* Make sure this module belongs to the trace */
        sameModule = null;
        for (IAnalysisModule mod : fTraces[1].getAnalysisModules()) {
            if (mod == module1) {
                sameModule = mod;
            }
        }
        assertNotNull(sameModule);

        /* Uncommon module from trace 1 */
        module2 = TmfExperimentUtils.getAnalysisModuleOfClassForHost(experiment, host2, notCommonClass);
        assertNotNull(module2);
        /* Make sure this module belongs to the trace */
        sameModule = null;
        for (IAnalysisModule mod : fTraces[1].getAnalysisModules()) {
            if (mod == module1) {
                sameModule = mod;
            }
        }
        assertNotNull(sameModule);
    }

}
