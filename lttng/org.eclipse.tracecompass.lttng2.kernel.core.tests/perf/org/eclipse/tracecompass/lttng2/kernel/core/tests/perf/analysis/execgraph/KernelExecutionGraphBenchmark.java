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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathModule;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsExecutionGraph;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsWorker;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfBenchmarkTrace;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Benchmarks the kernel execution graph
 *
 * @author Geneviève Bastien
 */
@RunWith(Parameterized.class)
public class KernelExecutionGraphBenchmark {

    /**
     * Test test ID for kernel analysis benchmarks
     */
    public static final String TEST_ID = "org.eclipse.tracecompass#Kernel Execution Graph#";
    private static final String CRIT_PATH_TEST_ID = "org.eclipse.tracecompass#Critical Path#";
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

    /**
     * @return The arrays of parameters
     * @throws IOException
     *             Exception thrown by reading files
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() throws IOException {
        return Arrays.asList(new Object[][] {
                { "Trace2", LOOP_COUNT, FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.TRACE2.getTraceURL())).getAbsolutePath(), 2203, 169, 27 },
                { "ManyThreads", LOOP_COUNT, FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.MANY_THREADS.getTraceURL())).getAbsolutePath(), 2673, 8060, 5473 },
                { "Django", LOOP_COUNT, FileUtils.toFile(FileLocator.toFileURL(CtfTestTrace.DJANGO_HTTPD.getTraceURL())).getAbsolutePath(), 1089, 138, 4 },
                { "OS Events", 10, CtfBenchmarkTrace.ALL_OS_ANALYSES.getTracePath().toString(), 21264, 394, 98 },
        });
    }

    private final int fLoopCount;
    private final String fFileTracePath;
    private final String fTestName;
    private final int fThreadId;
    private final @Nullable Integer fGraphWorkerCount;
    private final @Nullable Integer fCritPathWorkerCount;

    /**
     * Constructor
     *
     * @param name
     *            Name of the test
     * @param loopCount
     *            The number of iterations to do
     * @param testTrace
     *            The CTF test trace to use
     * @param threadId
     *            The ID of the thread for which to compute the critical path
     * @param graphWorkerCount
     *            The number of expected worker on the execution graph
     * @param critPathWorkerCount
     *            The number of expected worker on the critical path
     */
    public KernelExecutionGraphBenchmark(String name, int loopCount, String testTrace, int threadId, @Nullable Integer graphWorkerCount, @Nullable Integer critPathWorkerCount) {
        fLoopCount = loopCount;
        fFileTracePath = testTrace;
        fTestName = name;
        fThreadId = threadId;
        fGraphWorkerCount = graphWorkerCount;
        fCritPathWorkerCount = critPathWorkerCount;
    }

    /**
     * Run the memory benchmarks
     */
    @Test
    public void runMemoryBenchmarks() {
        // For memory benchmarks, one iteration is enough as there is no real
        // variation
        runOneBenchmark(String.format(TEST_MEMORY, fTestName),
                memory,
                Dimension.USED_JAVA_HEAP,
                1);

    }

    /**
     * Run the CPU benchmarks
     */
    @Test
    public void runCpuBenchmarks() {
        runOneBenchmark(String.format(TEST_BUILD, fTestName),
                cpu,
                Dimension.CPU_TIME,
                fLoopCount);

    }

    private void runOneBenchmark(String testName, RunMethod method, Dimension dimension, int loopCount) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName);
        PerformanceMeter pmCritPath = perf.createPerformanceMeter(CRIT_PATH_TEST_ID + testName);
        perf.tagAsSummary(pm, "Execution graph " + testName, dimension);

        for (int i = 0; i < loopCount; i++) {
            LttngKernelTrace trace = null;
            OsExecutionGraph module = null;

            try {
                trace = new LttngKernelTrace();
                module = new OsExecutionGraph();
                module.setId("test");
                trace.initTrace(null, fFileTracePath, CtfTmfEvent.class);
                module.setTrace(trace);

                method.execute(pm, module);

                // If a thread is specified, benchmark the critical path
                if (fThreadId > 0) {
                    benchmarkCriticalPath(testName, method, pmCritPath, trace, module);
                }

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
        pmCritPath.commit();
    }

    private void benchmarkCriticalPath(String testName, RunMethod method, PerformanceMeter pm, @NonNull LttngKernelTrace trace, OsExecutionGraph module) {

        CriticalPathModule critPathModule = null;
        try {
            // Find the worker in the execution graph
            TmfGraph graph = module.getGraph();
            assertNotNull("Execution graph is null!", graph);
            IGraphWorker worker = null;
            if (fGraphWorkerCount != null) {
                assertEquals("Number of execution graph workers", (int) fGraphWorkerCount, graph.getWorkers().size());
            } else {
                System.out.println("Number of execution graph workers: " + graph.getWorkers().size());
            }
            for (IGraphWorker graphWorker : graph.getWorkers()) {
                if (((OsWorker) graphWorker).getHostThread().getTid() == fThreadId) {
                    worker = graphWorker;
                }
            }
            assertNotNull("Requested worker for critical path not found: " + fThreadId, worker);

            // Create the critical path module and benchmark its execution
            critPathModule = new CriticalPathModule(module, worker);
            critPathModule.setTrace(trace);

            method.execute(pm, critPathModule);

            // Make sure the critical path computed something and has a few
            // workers.
            TmfGraph criticalPath = critPathModule.getCriticalPath();
            assertNotNull("Critical path is null!", criticalPath);
            if (fCritPathWorkerCount != null) {
                assertEquals("Number of critical path workers", (int) fCritPathWorkerCount, criticalPath.getWorkers().size());
            } else {
                System.out.println("Number of critical path workers: " + criticalPath.getWorkers().size());
            }
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
        } finally {
            if (critPathModule != null) {
                critPathModule.dispose();
            }
        }

    }

}
