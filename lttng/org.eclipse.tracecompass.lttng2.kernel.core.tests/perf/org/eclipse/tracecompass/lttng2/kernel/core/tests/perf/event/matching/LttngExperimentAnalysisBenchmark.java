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
package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.event.matching;

import java.io.File;
import java.util.function.Supplier;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;

/**
 * Benchmark Experiments (many traces) with different Trace Compass Analyzes
 *
 * Before running this benchmark, the user should do the following changes:
 *
 * 1 -Create a local folder containing the Lttng traces he wants to use for the
 * test as an experiment. The traces can be obtained with the Lttng tracer. If
 * the test requires a large number of traces, the user can make copies of the
 * same trace (or group of traces) but, he should change the "UUID" of the clock
 * in the "matadata" file of the traces.
 *
 * 2- Change the "moduleSupplier" variable in the runTest() function according
 * to the Analysis Module he wishes to test.
 *
 * @author Abdellah Rahmani
 */
public class LttngExperimentAnalysisBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools# Experiment Analysis benchmarking#";
    private static final String CPU = " (cpu usage)";
    private static final String TEST_SUMMARY = "Trace Compass Scalability- Experiment benchmark";

    /**
     * Tests an experiment (group of traces) with an analysis module
     * (OsExecutionGraph() in this example)
     *
     * @param directoryPath
     *            Path to the directory containing the group of traces
     * @param loopCount
     *            Number of iterations
     * @param moduleSupplier
     *            Parameter specifying which analysis module we want to test
     * @param isExperiment
     *            Boolean parameter to specify whether the analysis module is
     *            for an experiment (like the OsExecutionGraph module) or for a
     *            trace (like KernelAnalysisModule)
     * @throws TmfTraceException
     * @throws TmfAnalysisException
     */
    private static void testExperiment(String directoryPath, int loopCount, Supplier<IAnalysisModule> moduleSupplier, boolean isExperiment) throws TmfTraceException, TmfAnalysisException {
        File parentDirectory = new File(directoryPath);
        if (!parentDirectory.isDirectory() || parentDirectory.list() == null) {
            System.err.println(String.format("Trace directory not found !\nYou need to setup the directory path for the LttngExperimentAnalysisBenchmar class."
                    + " See the javadoc of this class."));
            return;
        }

        // List of all files and directories
        File filesList[] = parentDirectory.listFiles();

        int size = filesList.length;
        String testName = "Experiment of" + Integer.toString(size);
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName + CPU);
        perf.tagAsSummary(pm, TEST_SUMMARY + ':' + testName + CPU, Dimension.CPU_TIME);

        for (int j = 0; j < loopCount; j++) {

            CtfTmfTrace[] traces = new CtfTmfTrace[size];
            IAnalysisModule[] modules = new IAnalysisModule[size];
            int i = 0;

            for (File file : filesList) {
                String path = file.getAbsolutePath() + "/kernel";
                CtfTmfTrace trace = new CtfTmfTrace();
                try {
                    trace.initTrace(null, path, CtfTmfEvent.class);
                } finally {
                }
                traces[i] = trace;
                if (!isExperiment) {
                    try {
                        IAnalysisModule module = null;
                        module = moduleSupplier.get();
                        module.setId("test");

                        try {
                            module.setTrace(trace);

                        } finally {
                        }
                        modules[i] = module;

                    } finally {
                    }
                }
                i++;
            }

            TmfExperiment experiment = new TmfExperiment(CtfTmfEvent.class, "Test experiment", traces, TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
            IAnalysisModule module = null;
            if (isExperiment) {
                try {

                    module = moduleSupplier.get();
                    module.setId("test");

                    try {
                        module.setTrace(experiment);
                    } finally {
                    }

                } finally {
                }

                pm.start();
                module.schedule();
                module.waitForCompletion();
                pm.stop();

                module.dispose();
            } else {
                pm.start();
                for (IAnalysisModule mod : modules) {
                    mod.schedule();
                }

                for (IAnalysisModule mod : modules) {
                    mod.waitForCompletion();

                }
                pm.stop();

                for (IAnalysisModule mod : modules) {
                    mod.dispose();
                }
            }
            experiment.dispose();
        }

        pm.commit();
    }

    /**
     * Runs the Experiment test
     *
     * @throws TmfAnalysisException
     *             We do not pursue the execution of the benchmark in case of
     *             analysis module initialization problem
     * @throws TmfTraceException
     *             We do not pursue the execution of the benchmark in case of
     *             trace initialization problem
     */
    @Test
    public void runTest() throws TmfTraceException, TmfAnalysisException {

        Supplier<IAnalysisModule> moduleSupplier = () -> new KernelAnalysisModule();
        String directoryPath = "null";

        int loopCount = 5;
        boolean isExperiment = false;
        testExperiment(directoryPath, loopCount, moduleSupplier, isExperiment);

    }
}