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

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.Test;

/**
 * Benchmarks Experiments Indexing
 *
 * * Before running this benchmark, the user should Create a local folder
 * containing the Lttng traces he wants to use for the test as an experiment and
 * put his path in the "directoryPath" of "runTest()". The traces can be
 * obtained with the Lttng tracer. If the test requires a large number of
 * traces, the user can make copies of the same trace (or group of traces) but,
 * he should change the "UUID" of the clock in the "matadata" file of the
 * traces.
 *
 * @author Abdellah Rahmani
 */
public class LttngExperimentIndexingBenchmark {

    private static final String TEST_ID = "org.eclipse.tracecompass.lttng2.kernel#Experiment Indexing benchmark#";
    private static final String CPU = " (cpu usage)";
    private static final String TEST_SUMMARY = "Trace Compass scalabilty - Experiment indexing benchmark";

    /**
     * Tests experiment indexing from the parent directory containing the traces
     * whose path is given as an input.
     *
     * @param directoryPath
     *            Path to the directory containing the group of traces
     * @param loopCount
     *            Number of iterations
     */
    public static void testIndexing(String directoryPath, int loopCount) {

        File parentDirectory = new File(directoryPath);
        if (!parentDirectory.isDirectory() || parentDirectory.list() == null) {
            System.err.println(String.format("Trace directory not found !\nYou need to setup the directory path for the "
                    + "LttngExperimentIndexingBenchmark class. See the javadoc of this class."));
            return;
        }

        // List of all files and directories
        File filesList[] = parentDirectory.listFiles();
        int size = filesList.length;
        String testName = "Experiment of " + Integer.toString(size) + " traces";
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName + CPU);
        perf.tagAsSummary(pm, TEST_SUMMARY + ':' + testName + CPU, Dimension.CPU_TIME);

        for (int j = 0; j < loopCount; j++) {

            CtfTmfTrace[] traces = new CtfTmfTrace[size];
            int i = 0;

            for (File file : filesList) {
                String path = file.getAbsolutePath() + "/kernel";
                CtfTmfTrace trace = new CtfTmfTrace();
                try {
                    trace.initTrace(null, path, CtfTmfEvent.class);
                } catch (TmfTraceException e) {
                    e.printStackTrace();
                    break;
                }
                traces[i] = trace;
                i++;
            }

            TmfExperiment experiment = new TmfExperiment(CtfTmfEvent.class, "Test experiment", traces,
                    TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
            pm.start();
            experiment.indexTrace(true);
            pm.stop();
            experiment.dispose();
        }
        pm.commit();
    }

    /**
     * Runs the Experiment test
     */
    @Test
    public void runTest() {
        int loopCount = 10;
        String directoryPath = "null";
        testIndexing(directoryPath, loopCount);
    }
}
