/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.tid;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.List;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.kernel.KernelAnalysisBenchmark;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.Test;

/**
 * Benchmarks some typical usages of the thread id analysis
 *
 * @author Matthew Khouzam
 */
public class TidAnalysisUsageBenchmark {

    private static final String TEST_GET_RUNNING_THREAD = "TID: Threads On CPU";
    private static final int LOOP_COUNT = 25;
    private static final long SEED = 65423897234L;
    private static final int NUM_CPU_QUERIES = 20000;

    /**
     * Run the benchmark with "trace2"
     */
    @Test
    public void testTrace2() {
        runTest(CtfTestTrace.TRACE2, "Trace2");
    }

    /**
     * Run the benchmark with "many threads"
     */
    @Test
    public void testManyThreads() {
        runTest(CtfTestTrace.MANY_THREADS, "ManyThreads");
    }

    /**
     * Run the benchmark with "django httpd"
     */
    @Test
    public void testDjangoHttpd() {
        runTest(CtfTestTrace.DJANGO_HTTPD, "Django httpd");
    }

    private static TidAnalysisModule getModule(@NonNull CtfTestTrace testTrace, @NonNull LttngKernelTrace trace) {
        TidAnalysisModule module = null;
        String path = CtfTmfTestTraceUtils.getTrace(testTrace).getPath();

        try {
            /* Initialize the analysis module */
            module = new TidAnalysisModule();
            module.setId("test");
            trace.initTrace(null, path, CtfTmfEvent.class);
            module.setTrace(trace);
            TmfTestHelper.executeAnalysis(module);
        } catch (TmfAnalysisException | TmfTraceException e) {
            fail(e.getMessage());
        }
        return module;
    }

    private static void deleteSupplementaryFiles(ITmfTrace trace) {
        /*
         * Delete the supplementary files at the end of the benchmarks
         */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    private static void runTest(@NonNull CtfTestTrace testTrace, String testName) {

        /* First, complete the analysis */
        LttngKernelTrace trace = new LttngKernelTrace();
        deleteSupplementaryFiles(trace);
        TidAnalysisModule module = getModule(testTrace, trace);

        /* Benchmark some query use cases */
        benchmarkGetThreadOnCpu(testName, module);

        deleteSupplementaryFiles(trace);
        module.dispose();
        trace.dispose();

        CtfTmfTestTraceUtils.dispose(testTrace);
    }

    /**
     * Benchmarks getting a thread running on a random CPU from the kernel
     * analysis at fixed intervals. This use case mimics an analysis that reads
     * events and needs to get the currently running thread for those events.
     */
    private static void benchmarkGetThreadOnCpu(String testName, TidAnalysisModule module) {

        Performance perf = Performance.getDefault();
        PerformanceMeter pmRunningThread = perf.createPerformanceMeter(KernelAnalysisBenchmark.TEST_ID + testName + ": " + TEST_GET_RUNNING_THREAD);
        perf.tagAsSummary(pmRunningThread, TEST_GET_RUNNING_THREAD + '(' + testName + ')', Dimension.CPU_TIME);

        @Nullable
        ITmfStateSystem ss = module.getStateSystem();
        if (ss == null) {
            fail("The state system is null");
            return;
        }

        /* Get the number of CPUs */
        int cpuCount = -1;
        @NonNull List<@NonNull Integer> cpus = ss.getSubAttributes(ITmfStateSystem.ROOT_ATTRIBUTE, false);
        cpuCount = cpus.size();
        if (cpuCount < 1) {
            fail("Impossible to get the number of CPUs");
        }

        /* Get the step and start time of the queries */
        long startTime = ss.getStartTime();
        long endTime = ss.getCurrentEndTime();
        long step = Math.floorDiv(endTime - startTime, NUM_CPU_QUERIES);

        if (step < 1) {
            fail("Trace is too short to run the get thread on CPU benchmark");
        }

        /* Verify the query work by fetching a value at the end of the trace */
        Integer threadOnCpu = module.getThreadOnCpuAtTime(0, endTime);
        if (threadOnCpu == null) {
            fail("null thread on CPU at the end of the trace. Something is not right with the state system");
        }

        for (int i = 0; i < LOOP_COUNT; i++) {
            /* Get the thread running on a random CPU at fixed intervals */
            Random randomGenerator = new Random(SEED);
            pmRunningThread.start();
            for (long nextTime = startTime; nextTime < endTime; nextTime += step) {
                int cpu = Math.abs(randomGenerator.nextInt()) % cpuCount;
                module.getThreadOnCpuAtTime(cpu, nextTime);
            }
            pmRunningThread.stop();
        }
        pmRunningThread.commit();
    }
}
