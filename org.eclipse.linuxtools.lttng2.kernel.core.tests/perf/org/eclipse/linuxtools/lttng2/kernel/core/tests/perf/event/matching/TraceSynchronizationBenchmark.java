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

package org.eclipse.linuxtools.lttng2.kernel.core.tests.perf.event.matching;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.linuxtools.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.linuxtools.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.ctf.core.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Benchmark trace synchronization
 *
 * @author Geneviève Bastien
 */
public class TraceSynchronizationBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#Trace synchronization";
    private static final String TIME = " (time)";
    private static final String MEMORY = " (memory usage)";
    private static final String TEST_SUMMARY = "Trace synchronization";
    private static int BLOCK_SIZE = 1000;

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
        assumeTrue(CtfTmfTestTrace.SYNC_SRC.exists());
        assumeTrue(CtfTmfTestTrace.SYNC_DEST.exists());
        try (CtfTmfTrace trace1 = CtfTmfTestTrace.SYNC_SRC.getTrace();
                CtfTmfTrace trace2 = CtfTmfTestTrace.SYNC_DEST.getTrace();) {
            ITmfTrace[] traces = { trace1, trace2 };
            runCpuTest(traces, "Match TCP events", 40);
        }
    }

    /**
     * Run the benchmark with 3 bigger traces
     *
     * TODO: For now, this test takes a lot of RAM. To run, remove the @Ignore
     * and set at least 1024Mb RAM, or else there is OutOfMemoryError exception
     */
    @Ignore
    @Test
    public void testDjangoTraces() {
        assumeTrue(CtfTmfTestTrace.DJANGO_CLIENT.exists());
        assumeTrue(CtfTmfTestTrace.DJANGO_DB.exists());
        assumeTrue(CtfTmfTestTrace.DJANGO_HTTPD.exists());
        try (CtfTmfTrace trace1 = CtfTmfTestTrace.DJANGO_CLIENT.getTrace();
                CtfTmfTrace trace2 = CtfTmfTestTrace.DJANGO_DB.getTrace();
                CtfTmfTrace trace3 = CtfTmfTestTrace.DJANGO_HTTPD.getTrace();) {
            ITmfTrace[] traces = { trace1, trace2, trace3 };
            runCpuTest(traces, "Django traces", 10);
            runMemoryTest(traces, "Django traces", 10);
        }
    }

    private static void runCpuTest(ITmfTrace[] testTraces, String testName, int loop_count) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + TIME + '#' + testName);
        perf.tagAsSummary(pm, TEST_SUMMARY + TIME + ':' + testName, Dimension.CPU_TIME);

        for (int i = 0; i < loop_count; i++) {
            TmfExperiment experiment = new TmfExperiment(CtfTmfEvent.class, "Test experiment", testTraces, BLOCK_SIZE);

            pm.start();
            try {
                experiment.synchronizeTraces(true);
            } catch (TmfTraceException e) {
                fail("Failed at iteration " + i + " with message: " + e.getMessage());
            }
            pm.stop();

        }
        pm.commit();

    }

    /* Benchmark memory used by the algorithm */
    private static void runMemoryTest(ITmfTrace[] testTraces, String testName, int loop_count) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + MEMORY + '#' + testName);
        perf.tagAsSummary(pm, TEST_SUMMARY + MEMORY + ':' + testName, Dimension.USED_JAVA_HEAP);

        for (int i = 0; i < loop_count; i++) {
            TmfExperiment experiment = new TmfExperiment(CtfTmfEvent.class, "Test experiment", testTraces, BLOCK_SIZE);

            System.gc();
            pm.start();
            try {
                experiment.synchronizeTraces(true);
            } catch (TmfTraceException e) {
                fail("Failed at iteration " + i + " with message: " + e.getMessage());
            }
            System.gc();
            pm.stop();

        }
        pm.commit();
    }
}
