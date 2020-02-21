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

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.execgraph;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.EnumSet;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraph;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.Test;

/**
 * Benchmarks the kernel execution graph
 *
 * @author Geneviève Bastien
 */
public class KernelExecutionGraphBenchmark {

    /**
     * Test test ID for kernel analysis benchmarks
     */
    public static final String TEST_ID = "org.eclipse.tracecompass#Kernel Execution Graph#";
    private static final String TEST_BUILD = "Building Graph (%s)";
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

    private static final EnumSet<CtfTestTrace> fTraceSet = EnumSet.of(
            CtfTestTrace.TRACE2,
            CtfTestTrace.MANY_THREADS,
            CtfTestTrace.DJANGO_HTTPD);

    /**
     * Run all benchmarks
     */
    @Test
    public void runAllBenchmarks() {
        for (CtfTestTrace trace : fTraceSet) {

            runOneBenchmark(trace,
                    String.format(TEST_BUILD, trace.toString()),
                    cpu,
                    Dimension.CPU_TIME);

            runOneBenchmark(trace,
                    String.format(TEST_MEMORY, trace.toString()),
                    memory,
                    Dimension.USED_JAVA_HEAP);
        }
    }

    private static void runOneBenchmark(@NonNull CtfTestTrace testTrace, String testName, RunMethod method, Dimension dimension) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName);
        perf.tagAsSummary(pm, "Execution graph " + testName, dimension);

        for (int i = 0; i < LOOP_COUNT; i++) {
            LttngKernelTrace trace = null;
            IAnalysisModule module = null;

            String path = CtfTmfTestTraceUtils.getTrace(testTrace).getPath();

            try {
                trace = new LttngKernelTrace();
                module = new OsExecutionGraph();
                module.setId("test");
                trace.initTrace(null, path, CtfTmfEvent.class);
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
