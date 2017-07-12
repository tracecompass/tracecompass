/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.syscall;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.kernel.KernelAnalysisBenchmark;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.Test;

import com.google.common.collect.Iterables;

/**
 * Benchmarks the system call latency analysis
 *
 * @author Geneviève Bastien
 */
public class SystemCallAnalysisUsageBenchmark {

    /**
     * Test test ID for the system call analysis benchmarks
     */
    public static final String TEST_ID = "org.eclipse.tracecompass#System Call Analysis#";
    private static final String TEST_ITERATE_CPU = "Iterate cpu (%s)";
    private static final String TEST_ITERATE_MEMORY = "Iterate memory (%s)";
    private static final String TEST_INTERSECTION_CPU = "Intersection cpu (%s)";
    private static final String TEST_INTERSECTION_MEMORY = "Intersection memory (%s)";

    private static final int LOOP_COUNT = 25;

    private static void deleteSupplementaryFiles(ITmfTrace trace) {
        /*
         * Delete the supplementary files at the beginning and end of the benchmarks
         */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    private static SystemCallLatencyAnalysis getModule(@NonNull CtfTestTrace testTrace, @NonNull LttngKernelTrace trace) {
        SystemCallLatencyAnalysis module = null;
        String path = CtfTmfTestTraceUtils.getTrace(testTrace).getPath();

        try {

            // Make sure the TID analysis has run on this trace
            trace.initTrace(null, path, CtfTmfEvent.class);
            deleteSupplementaryFiles(trace);

            trace.traceOpened(new TmfTraceOpenedSignal(trace, trace, null));
            IAnalysisModule tidModule = trace.getAnalysisModule(TidAnalysisModule.ID);
            assertNotNull(tidModule);
            tidModule.schedule();
            tidModule.waitForCompletion();

            /* Initialize the analysis module */
            module = new SystemCallLatencyAnalysis();
            module.setId("test");
            module.setTrace(trace);
            TmfTestHelper.executeAnalysis(module);
        } catch (TmfAnalysisException | TmfTraceException e) {
            fail(e.getMessage());
        }
        return module;
    }

    /**
     * Run the benchmark with "trace2"
     */
    @Test
    public void testTrace2() {
        runTest(CtfTestTrace.TRACE2, "Trace2");
    }

    /**
     * Run the benchmark with "many thread"
     */
    @Test
    public void testManyThreads() {
        runTest(CtfTestTrace.MANY_THREADS, "Many threads");
    }

    /**
     * Run the benchmark with "django httpd"
     */
    @Test
    public void testDjangoHttpd() {
        runTest(CtfTestTrace.DJANGO_HTTPD, "Django HTTPD");
    }

    private static void runTest(@NonNull CtfTestTrace testTrace, String traceName) {
        /* First, complete the analysis */
        LttngKernelTrace trace = new LttngKernelTrace();
        SystemCallLatencyAnalysis module = getModule(testTrace, trace);

        long segmentEnd = benchmarkIteration(traceName, module);
        benchmarkIntersection(traceName, module, trace.getStartTime().getValue(), segmentEnd);

        /*
         * Delete the supplementary files at the end of the benchmarks
         */
        deleteSupplementaryFiles(trace);

        module.dispose();
        trace.dispose();

        CtfTmfTestTraceUtils.dispose(testTrace);
    }

    /**
     * Benchmarks iterating through all the segments of the analysis
     */
    private static long benchmarkIteration(String testName, SystemCallLatencyAnalysis module) {

        Performance perf = Performance.getDefault();
        PerformanceMeter pmCpu = perf.createPerformanceMeter(KernelAnalysisBenchmark.TEST_ID + String.format(TEST_ITERATE_CPU, testName));
        perf.tagAsSummary(pmCpu, "Syscall " + String.format(TEST_ITERATE_CPU, testName), Dimension.CPU_TIME);

        PerformanceMeter pmMemory = perf.createPerformanceMeter(KernelAnalysisBenchmark.TEST_ID + String.format(TEST_ITERATE_MEMORY, testName));
        perf.tagAsSummary(pmMemory, "Syscall " + String.format(TEST_ITERATE_MEMORY, testName), Dimension.USED_JAVA_HEAP);

        ISegmentStore<@NonNull ISegment> ss = module.getSegmentStore();
        if (ss == null) {
            fail("The segment store is null");
            return -1;
        }

        long endTime = -1;

        /** Benchmark for CPU time */
        for (int i = 0; i < LOOP_COUNT; i++) {
            /* Make a full query at fixed intervals */
            pmCpu.start();
            for (ISegment segment : ss) {
                endTime = Math.max(endTime, segment.getEnd());
            }
            pmCpu.stop();
        }

        /** Benchmark for memory usage */
        for (int i = 0; i < LOOP_COUNT; i++) {
            /* Make a full query at fixed intervals */
            System.gc();
            pmMemory.start();
            for (ISegment segment : ss) {
                endTime = Math.max(endTime, segment.getEnd());
            }
            System.gc();
            pmMemory.stop();
        }

        pmCpu.commit();
        pmMemory.commit();
        return endTime;
    }

    /**
     * Benchmarks iterating through all the segments of the analysis
     */
    private static void benchmarkIntersection(String testName, SystemCallLatencyAnalysis module, long start, long end) {

        Performance perf = Performance.getDefault();
        PerformanceMeter pmCpu = perf.createPerformanceMeter(KernelAnalysisBenchmark.TEST_ID + String.format(TEST_INTERSECTION_CPU, testName));
        perf.tagAsSummary(pmCpu, "Syscall " + String.format(TEST_INTERSECTION_CPU, testName), Dimension.CPU_TIME);

        PerformanceMeter pmMemory = perf.createPerformanceMeter(KernelAnalysisBenchmark.TEST_ID + String.format(TEST_INTERSECTION_MEMORY, testName));
        perf.tagAsSummary(pmMemory, "Syscall " + String.format(TEST_INTERSECTION_MEMORY, testName), Dimension.USED_JAVA_HEAP);

        ISegmentStore<@NonNull ISegment> ss = module.getSegmentStore();
        if (ss == null) {
            fail("The segment store is null");
            return;
        }

        /** Benchmark for CPU time */
        for (int i = 0; i < LOOP_COUNT; i++) {
            /* Make a full query at fixed intervals */
            long intersectStart = start;
            long intersectEnd = end;
            pmCpu.start();
            for (int j = 0; j < 10; j++) {
                /**
                 * Iterate through the intersection -- this forces lazily evaluated iterables to
                 * do queries
                 */
                Iterables.getLast(ss.getIntersectingElements(intersectStart, intersectEnd), new BasicSegment(0l, 1l));
                long step = (long) ((intersectEnd - intersectStart) * 0.1);
                intersectStart += step;
                intersectEnd -= step;
            }
            pmCpu.stop();
        }

        /** Benchmark for memory usage */
        for (int i = 0; i < LOOP_COUNT; i++) {
            /* Make a full query at fixed intervals */
            List<Iterable<ISegment>> lists = new ArrayList<>();
            long intersectStart = start;
            long intersectEnd = end;
            System.gc();
            pmMemory.start();
            for (int j = 0; j < 5; j++) {
                Iterable<@NonNull ISegment> elements = ss.getIntersectingElements(intersectStart, intersectEnd);
                /**
                 * Iterate through the intersection -- this forces lazily evaluated iterables to
                 * do queries, and identify lingering data structures.
                 */
                Iterables.getLast(elements, new BasicSegment(0l, 1l));
                lists.add(elements);
                long step = (long) ((intersectEnd - intersectStart) * 0.2);
                intersectStart += step;
                intersectEnd -= step;
            }
            System.gc();
            pmMemory.stop();
        }

        pmCpu.commit();
        pmMemory.commit();
    }

}
