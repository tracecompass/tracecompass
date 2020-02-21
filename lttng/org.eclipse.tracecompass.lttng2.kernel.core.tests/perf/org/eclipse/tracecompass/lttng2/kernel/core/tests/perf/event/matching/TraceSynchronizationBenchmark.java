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

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.event.matching;

import static org.junit.Assert.assertNotNull;

import java.util.Collections;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationAlgorithm;
import org.eclipse.tracecompass.tmf.core.synchronization.SynchronizationManager;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Benchmark trace synchronization
 *
 * @author Geneviève Bastien
 */
public class TraceSynchronizationBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#Trace synchronization#";
    private static final String TIME = " (time)";
    private static final String MEMORY = " (memory usage)";
    private static final String TEST_SUMMARY = "Trace synchronization";

    /**
     * Initialize some data
     */
    @BeforeClass
    public static void setUp() {
        TmfEventMatching.registerMatchObject(new TcpEventMatching());
        TmfEventMatching.registerMatchObject(new TcpLttngEventMatching());
    }

    /**
     * Run the benchmark with 2 small traces
     */
    @Test
    public void testSmallTraces() {
        CtfTmfTrace trace1 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.SYNC_SRC);
        CtfTmfTrace trace2 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.SYNC_DEST);

        ITmfTrace[] traces = { trace1, trace2 };
        TmfExperiment experiment = new TmfExperiment(CtfTmfEvent.class, "Test experiment", traces, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
        runCpuTest(experiment, "Match TCP events", 40);

        trace1.dispose();
        trace2.dispose();
    }

    /**
     * Run the benchmark with 3 bigger traces
     */
    @Test
    public void testDjangoTraces() {
        CtfTmfTrace trace1 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.DJANGO_CLIENT);
        CtfTmfTrace trace2 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.DJANGO_DB);
        CtfTmfTrace trace3 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.DJANGO_HTTPD);

        ITmfTrace[] traces = { trace1, trace2, trace3 };
        TmfExperiment experiment = new TmfExperiment(CtfTmfEvent.class, "Test experiment", traces, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
        runCpuTest(experiment, "Django traces", 10);
        runMemoryTest(experiment, "Django traces", 10);

        trace1.dispose();
        trace2.dispose();
        trace3.dispose();
    }

    private static void runCpuTest(@NonNull TmfExperiment experiment, String testName, int loop_count) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName + TIME);
        perf.tagAsSummary(pm, TEST_SUMMARY + ':' + testName + TIME, Dimension.CPU_TIME);

        for (int i = 0; i < loop_count; i++) {
            pm.start();
            SynchronizationManager.synchronizeTraces(null, Collections.singleton(experiment), true);
            pm.stop();
        }
        pm.commit();

    }

    /* Benchmark memory used by the algorithm */
    private static void runMemoryTest(@NonNull TmfExperiment experiment, String testName, int loop_count) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName + MEMORY);
        perf.tagAsSummary(pm, TEST_SUMMARY + ':' + testName + MEMORY, Dimension.USED_JAVA_HEAP);

        for (int i = 0; i < loop_count; i++) {

            System.gc();
            pm.start();
            SynchronizationAlgorithm algo = SynchronizationManager.synchronizeTraces(null, Collections.singleton(experiment), true);
            assertNotNull(algo);

            System.gc();
            pm.stop();
        }
        pm.commit();
    }
}
