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

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.event.matching;

import static org.junit.Assume.assumeTrue;

import java.util.Set;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpEventMatching;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.event.matching.TcpLttngEventMatching;
import org.eclipse.tracecompass.tmf.core.event.matching.TmfEventMatching;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableSet;

/**
 * Benchmark simple event matching, without trace synchronization
 *
 * @author Geneviève Bastien
 */
public class EventMatchingBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#Event matching#";
    private static final String TIME = " (time)";
    private static final String MEMORY = " (memory usage)";
    private static final String TEST_SUMMARY = "Event matching";

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

        CtfTmfTrace trace1 = CtfTmfTestTrace.SYNC_SRC.getTrace();
        CtfTmfTrace trace2 = CtfTmfTestTrace.SYNC_DEST.getTrace();

        Set<ITmfTrace> traces = ImmutableSet.of((ITmfTrace) trace1, trace2);
        runCpuTest(traces, "Match TCP events", 100);

        trace1.dispose();
        trace2.dispose();

    }

    /**
     * Run the benchmark with 3 bigger traces
     */
    @Test
    public void testDjangoTraces() {
        assumeTrue(CtfTmfTestTrace.DJANGO_CLIENT.exists());
        assumeTrue(CtfTmfTestTrace.DJANGO_DB.exists());
        assumeTrue(CtfTmfTestTrace.DJANGO_HTTPD.exists());

        CtfTmfTrace trace1 = CtfTmfTestTrace.DJANGO_CLIENT.getTrace();
        CtfTmfTrace trace2 = CtfTmfTestTrace.DJANGO_DB.getTrace();
        CtfTmfTrace trace3 = CtfTmfTestTrace.DJANGO_HTTPD.getTrace();

        Set<ITmfTrace> traces = ImmutableSet.of((ITmfTrace) trace1, trace2, trace3);
        runCpuTest(traces, "Django traces", 10);
        runMemoryTest(traces, "Django traces", 10);

        trace1.dispose();
        trace2.dispose();
        trace3.dispose();
    }

    private static void runCpuTest(Set<ITmfTrace> testTraces, String testName, int loop_count) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName + TIME);
        perf.tagAsSummary(pm, TEST_SUMMARY + ':' + testName + TIME, Dimension.CPU_TIME);

        for (int i = 0; i < loop_count; i++) {
            TmfEventMatching traceMatch = new TmfEventMatching(testTraces);

            pm.start();
            traceMatch.matchEvents();
            pm.stop();
        }
        pm.commit();

    }

    /* Benchmark memory used by the algorithm */
    private static void runMemoryTest(Set<ITmfTrace> testTraces, String testName, int loop_count) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName + MEMORY);
        perf.tagAsSummary(pm, TEST_SUMMARY + ':' + testName + MEMORY, Dimension.USED_JAVA_HEAP);

        for (int i = 0; i < loop_count; i++) {
            TmfEventMatching traceMatch = new TmfEventMatching(testTraces);

            System.gc();
            pm.start();
            traceMatch.matchEvents();
            System.gc();
            pm.stop();
        }
        pm.commit();

    }
}
