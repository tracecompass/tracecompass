/*******************************************************************************
 * Copyright (c) 2022 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.tests.callstack.provider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.analysis.profiling.core.tests.data.CallStackTestData;
import org.eclipse.tracecompass.analysis.profiling.core.tests.data.TestDataBigCallStack;
import org.eclipse.tracecompass.analysis.profiling.core.tests.data.TestDataSmallCallStack;
import org.eclipse.tracecompass.analysis.profiling.core.tests.stubs.CallStackAnalysisStub;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.provider.CallStackDataProvider;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.provider.CallStackDataProviderFactory;
import org.eclipse.tracecompass.internal.tmf.core.model.timegraph.TmfTimeGraphCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Class to test {@link CallStackDataProviderFactory}
 */
public class CallStackDataProviderFactoryTest {

    private static CallStackTestData fTraceData1;
    private static CallStackTestData fTraceData2;
    private static TmfExperiment fExperiment;
    private static TmfExperiment fExperimentSingleTrace;
    private static CallStackDataProviderFactory fFactory;

    /**
     * Test class setup
     */
    @BeforeClass
    public static void setup() {
        fTraceData1 = new TestDataSmallCallStack();
        fTraceData2 = new TestDataBigCallStack();

        TmfXmlTraceStub trace1 = (TmfXmlTraceStub) fTraceData1.getTrace();
        TmfXmlTraceStub trace2 = (TmfXmlTraceStub) fTraceData2.getTrace();

        CallStackAnalysisStub module = TmfTraceUtils.getAnalysisModuleOfClass(trace1, CallStackAnalysisStub.class, CallStackAnalysisStub.ID);
        assertNotNull(module);

        module = TmfTraceUtils.getAnalysisModuleOfClass(trace2, CallStackAnalysisStub.class, CallStackAnalysisStub.ID);
        assertNotNull(module);

        ITmfTrace[] traces = new ITmfTrace[1];
        traces[0] = trace1;
        fExperimentSingleTrace = new TmfExperiment(ITmfEvent.class, "experiment", traces, 1000, null);

        traces = new ITmfTrace[2];
        traces[0] = trace1;
        traces[1] = trace2;
        fExperiment = new TmfExperiment(ITmfEvent.class, "experiment", traces, 1000, null);
        fFactory = new CallStackDataProviderFactory();
    }

    /**
     * Dispose of the test data and experiments
     */
    @AfterClass
    public static void tearDown() {
        CallStackTestData[] traceData =  { fTraceData1, fTraceData2 };
        for (int i = 0; i < traceData.length; i++) {
            if (traceData[i] != null) {
                traceData[i].dispose();
            }
        }

        TmfExperiment[] experiments = { fExperiment, fExperimentSingleTrace };
        for (int i = 0; i < traceData.length; i++) {
            if (experiments[i] != null) {
                experiments[i].dispose();
            }
        }
    }

    /**
     * Verify that the correct data provider for a single trace is returned
     */
    @Test
    public void testCreateProviderForTrace() {
        ITmfTrace trace = fTraceData1.getTrace();
        assertNotNull(trace);
        ITmfTreeDataProvider<?> dp = fFactory.createProvider(trace);
        assertNotNull(dp);
        assertTrue(dp instanceof CallStackDataProvider);
    }

    /**
     * Verify that the correct data provider for an experiment is returned
     */
    @Test
    public void testCreateProviderForExperiment() {
        assertNotNull(fExperiment);
        ITmfTreeDataProvider<?> dp = fFactory.createProvider(fExperiment);
        assertNotNull(dp);
        TmfTimeGraphCompositeDataProvider<?, ?> compositeDp = (TmfTimeGraphCompositeDataProvider<?,?>) dp;
        assertTrue(dp instanceof TmfTimeGraphCompositeDataProvider);
        assertEquals(CallStackDataProvider.ID, compositeDp.getId());

        assertNotNull(fExperimentSingleTrace);
        dp = fFactory.createProvider(fExperimentSingleTrace);
        assertNotNull(dp);
        assertTrue(dp instanceof CallStackDataProvider);
    }
}
