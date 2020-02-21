/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.kernel;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * Benchmarks some typical usages of the kernel analysis
 *
 * @author Geneviève Bastien
 */
public class KernelAnalysisUsageBenchmark {

    private static final String TEST_GET_RUNNING_THREAD = "Kernel: Threads On CPU";
    private static final String TEST_CFV_ZOOM = "Kernel: Zoom control flow";
    private static final String TEST_BUILD_ENTRY_LIST = "Kernel: build control flow entries";
    private static final int LOOP_COUNT = 25;
    private static final long SEED = 65423897234L;
    private static final int NUM_CPU_QUERIES = 20000;
    private static final String WILDCARD = "*";
    private static final int STEP = 32;
    private static final int TYPICAL_MONITOR_WIDTH = 2000;
    private static final int NUM_DISJOINT_TIME_ARRAYS = 10;

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

    private static KernelAnalysisModule getModule(@NonNull CtfTestTrace testTrace, @NonNull LttngKernelTrace trace) {
        KernelAnalysisModule module = null;
        String path = CtfTmfTestTraceUtils.getTrace(testTrace).getPath();

        try {
            /* Initialize the analysis module */
            module = new KernelAnalysisModule();
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
        KernelAnalysisModule module = getModule(testTrace, trace);

        /* Benchmark some query use cases */
        benchmarkGetThreadOnCpu(testName, module);
        benchmarkFullQueries(testName, module);

        /*
         * Delete the supplementary files at the end of the benchmarks
         */
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
    private static void benchmarkGetThreadOnCpu(String testName, KernelAnalysisModule module) {

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
        try {
            int cpusQuark = ss.getQuarkAbsolute(Attributes.CPUS);
            @NonNull
            List<@NonNull Integer> cpus = ss.getSubAttributes(cpusQuark, false);
            cpuCount = cpus.size();
        } catch (AttributeNotFoundException e) {
            fail(e.getMessage());
        }
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
        Integer threadOnCpu = KernelThreadInformationProvider.getThreadOnCpu(module, 0, endTime);
        if (threadOnCpu == null) {
            fail("null thread on CPU at the end of the trace. Something is not right with the state system");
        }

        for (int i = 0; i < LOOP_COUNT; i++) {
            /* Get the thread running on a random CPU at fixed intervals */
            Random randomGenerator = new Random(SEED);
            pmRunningThread.start();
            for (long nextTime = startTime; nextTime < endTime; nextTime += step) {
                int cpu = Math.abs(randomGenerator.nextInt()) % cpuCount;
                KernelThreadInformationProvider.getThreadOnCpu(module, cpu, nextTime);
            }
            pmRunningThread.stop();
        }
        pmRunningThread.commit();
    }

    /**
     * Benchmarks getting full queries at different times. This use cases is
     * often used to populate the views.
     */
    private static void benchmarkFullQueries(String testName, KernelAnalysisModule module) {

        Performance perf = Performance.getDefault();
        PerformanceMeter pmRunningThread = perf.createPerformanceMeter(KernelAnalysisBenchmark.TEST_ID + testName + ": " + TEST_CFV_ZOOM);
        perf.tagAsSummary(pmRunningThread, TEST_CFV_ZOOM + '(' + testName + ')', Dimension.CPU_TIME);
        PerformanceMeter buildEntryList = perf.createPerformanceMeter(KernelAnalysisBenchmark.TEST_ID + testName + ": " + TEST_BUILD_ENTRY_LIST);
        perf.tagAsSummary(buildEntryList, TEST_BUILD_ENTRY_LIST + '(' + testName + ')', Dimension.CPU_TIME);

        @Nullable
        ITmfStateSystem ss = module.getStateSystem();
        assertNotNull("The state system is null", ss);

        /* Get the step and start time of the queries */
        long startTime = ss.getStartTime();
        long endTime = ss.getCurrentEndTime();
        long delta = (endTime - startTime) / NUM_DISJOINT_TIME_ARRAYS;
        assertFalse("Trace is too short to run the get full queries benchmark", delta < 1l);

        /* Create a List with the threads' PPID and EXEC_NAME quarks for the 2D query .*/
        List<Integer> entryListQuarks = new ArrayList<>(ss.getQuarks(Attributes.THREADS, WILDCARD, Attributes.EXEC_NAME));
        entryListQuarks.addAll(ss.getQuarks(Attributes.THREADS, WILDCARD, Attributes.PPID));
        List<@NonNull Integer> threadQuarks = ss.getQuarks(Attributes.THREADS, WILDCARD);

        for (int i = 0; i < LOOP_COUNT; i++) {
            buildEntryList.start();
            try {
                Iterables.size(ss.query2D(entryListQuarks, startTime, endTime));
            } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
                fail(e.getMessage());
            }
            buildEntryList.stop();

            Random randomGenerator = new Random(SEED);
            pmRunningThread.start();
            try {
                for (long t = startTime; t < endTime - delta; t += delta) {
                    /*
                     * Create a list of threads to zoom on, limit it to 32 because the timegraph is
                     * virtual
                     */
                    int startPos = Math.abs(randomGenerator.nextInt()) % threadQuarks.size();
                    List<Integer> zoomQuarks = threadQuarks.subList(startPos, Math.min(startPos + STEP, threadQuarks.size() - 1));

                    /*
                     * sample the number of points to display on each 10th of the trace range for 2k
                     * points. Do the loop inside the zoom PerformanceMeter for benchmark parity
                     * with the previous full queries.
                     */
                    List<Long> times = StateSystemUtils.getTimes(startTime, t + delta, delta / TYPICAL_MONITOR_WIDTH);
                    Iterables.size(ss.query2D(zoomQuarks, times));
                }
            } catch (IndexOutOfBoundsException | TimeRangeException | StateSystemDisposedException e) {
                fail(e.getMessage());
            }
            pmRunningThread.stop();
        }
        buildEntryList.commit();
        pmRunningThread.commit();
    }
}
