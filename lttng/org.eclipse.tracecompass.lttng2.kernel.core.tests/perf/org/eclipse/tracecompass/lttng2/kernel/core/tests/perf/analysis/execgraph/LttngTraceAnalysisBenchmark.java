/*******************************************************************************
 * Copyright (c) 2021 École Polytechnique de Montréal
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
import java.util.function.Supplier;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;

/**
 * Benchmark the cpu and memory usage of trace compass analyses. This benchmark
 * is a utility benchmark that allows the user to just specify a directory of
 * traces and it will run analyses benchmark for each trace independently.
 *
 * Memory usage is more applicable in the case of the OsExecutionGraph()
 *
 * Before running this benchmark, the user should do the following changes:
 *
 * 1-Create a local folder containing the Lttng trace(s) he wants to use for the
 * test and update the "directoryPath" variable with its path. The trace(s) can
 * be obtained with the Lttng tracer.
 *
 * 2- Update the "moduleSupplier" variable in the runAllBenchmarks() function
 * according to the Analysis Module he wishes to test.
 *
 * @author Abdellah Rahmani
 */
public class LttngTraceAnalysisBenchmark {

    /**
     * Test test ID for the analysis benchmarks
     */
    public static final String TEST_ID = "org.eclipse.tracecompass.lttng2.kernel#Lttng trace Analysis test#";
    private static final String TEST_CPU = "CPU Usage (%s)";
    private static final String TEST_MEMORY = "Memory Usage (%s)";
    private static final int LOOP_COUNT = 5;

    private interface runMethod {
        void execute(PerformanceMeter pm, IAnalysisModule module);
    }

    private runMethod cpu = (pm, module) -> {
        pm.start();
        TmfTestHelper.executeAnalysis(module);
        pm.stop();
    };

    private runMethod memory = (pm, module) -> {
        System.gc();
        pm.start();
        TmfTestHelper.executeAnalysis(module);
        System.gc();
        pm.stop();
    };

    /**
     * Runs all the benchmarks
     */
    @Test
    public void runAllBenchmarks() {

        Supplier<IAnalysisModule> moduleSupplier = () -> new KernelAnalysisModule();
        String directoryPath = "null";
        File parentDirectory = new File(directoryPath);
        if (!parentDirectory.isDirectory() || parentDirectory.list() == null) {
            System.err.println(String.format("Trace directory not found !\nYou need to setup the directory path before "
                    + "running this benchmark. See the javadoc of this class."));
            return;
        }

        File filesList[] = parentDirectory.listFiles();

        for (File file : filesList) {
            String path = file.getAbsolutePath() + "/kernel";
            CtfTmfTrace trace = new CtfTmfTrace();
            try {
                trace.initTrace(null, path, CtfTmfEvent.class);
            } catch (TmfTraceException e) {
                e.printStackTrace();
                break;
            }

            runOneBenchmark(trace,
                    String.format(TEST_CPU, trace.toString()),
                    cpu, Dimension.CPU_TIME, moduleSupplier);

            runOneBenchmark(trace,
                    String.format(TEST_MEMORY, trace.toString()),
                    memory, Dimension.USED_JAVA_HEAP, moduleSupplier);
            trace.dispose();

        }
    }

    private static void runOneBenchmark(@NonNull CtfTmfTrace testTrace, String testName, runMethod method, Dimension dimension, Supplier<IAnalysisModule> moduleSupplier) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName);
        perf.tagAsSummary(pm, "Trace Compass Analysis " + testName, dimension);

        for (int i = 0; i < LOOP_COUNT; i++) {
            LttngKernelTrace trace = null;
            IAnalysisModule module = null;

            String path = testTrace.getPath();
            try {
                trace = new LttngKernelTrace();
                module = moduleSupplier.get();
                module.setId("test");
                trace.initTrace(null, path, CtfTmfEvent.class);
                module.setTrace(trace);
                method.execute(pm, module);

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
    }
}
