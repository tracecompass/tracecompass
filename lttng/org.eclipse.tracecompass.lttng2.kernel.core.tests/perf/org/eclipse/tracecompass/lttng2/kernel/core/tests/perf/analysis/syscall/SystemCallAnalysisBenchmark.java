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

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.syscall;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.tid.TidAnalysisModule;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.Test;

/**
 * Benchmarks the system call latency analysis
 *
 * @author Geneviève Bastien
 */
public class SystemCallAnalysisBenchmark {

    /**
     * Test test ID for the system call analysis benchmarks
     */
    public static final String TEST_ID = "org.eclipse.tracecompass#System Call Analysis#";
    private static final String TEST_BUILD = "Building Analysis (%s)";
    private static final String TEST_MEMORY = "Memory Usage (%s)";

    private static final int LOOP_COUNT = 25;

    private interface RunMethod {
        void execute(PerformanceMeter pm, IAnalysisModule module);
    }

    private RunMethod cpu = (pm, module) -> {
        pm.start();
        TmfTestHelper.executeAnalysis(module);
        pm.stop();
    };

    private RunMethod memory = (pm, module) -> {
        System.gc();
        pm.start();
        TmfTestHelper.executeAnalysis(module);
        System.gc();
        pm.stop();
    };

    /**
     * Run the benchmark with "trace2"
     */
    @Test
    public void testTrace2() {
        runTest(CtfTestTrace.TRACE2, String.format(TEST_BUILD, "Trace2"), cpu, Dimension.CPU_TIME);
        runTest(CtfTestTrace.TRACE2, String.format(TEST_MEMORY, "Trace2"), memory, Dimension.USED_JAVA_HEAP);
    }

    /**
     * Run the benchmark with "many thread"
     */
    @Test
    public void testManyThreads() {
        runTest(CtfTestTrace.MANY_THREADS, String.format(TEST_BUILD, "Many threads"), cpu, Dimension.CPU_TIME);
        runTest(CtfTestTrace.MANY_THREADS, String.format(TEST_MEMORY, "Many threads"), memory, Dimension.USED_JAVA_HEAP);
    }

    /**
     * Run the benchmark with "django httpd"
     */
    @Test
    public void testDjangoHttpd() {
        runTest(CtfTestTrace.DJANGO_HTTPD, String.format(TEST_BUILD, "Django HTTPD"), cpu, Dimension.CPU_TIME);
        runTest(CtfTestTrace.DJANGO_HTTPD, String.format(TEST_MEMORY, "Django HTTPD"), memory, Dimension.USED_JAVA_HEAP);
    }

    private static void runTest(@NonNull CtfTestTrace testTrace, String testName, RunMethod method, Dimension dimension) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName);
        perf.tagAsSummary(pm, "Syscall " + testName, dimension);

        for (int i = 0; i < LOOP_COUNT; i++) {
            LttngKernelTrace trace = null;
            IAnalysisModule module = null;

            String path = CtfTmfTestTraceUtils.getTrace(testTrace).getPath();

            try {
                // Make sure the TID analysis has run on this trace
                trace = new LttngKernelTrace();
                trace.initTrace(null, path, CtfTmfEvent.class);
                trace.traceOpened(new TmfTraceOpenedSignal(trace, trace, null));
                module = trace.getAnalysisModule(TidAnalysisModule.ID);
                assertNotNull(module);
                module.schedule();
                module.waitForCompletion();

                module = new SystemCallLatencyAnalysis();
                module.setId("test");
                module.setTrace(trace);

                method.execute(pm, module);

                /*
                 * Delete the supplementary files, so that the next iteration
                 * rebuilds the state system.
                 */
                File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
                for (File file : suppDir.listFiles()) {
                    file.delete();
                }

            } catch (TmfAnalysisException | TmfTraceException e) {
                fail(e.getMessage());
            } finally {
                if (module != null) {
                    module.dispose();
                }
                if (trace != null) {
                    trace.dispose();
                }
            }
        }
        pm.commit();
        CtfTmfTestTraceUtils.dispose(testTrace);
    }

}
