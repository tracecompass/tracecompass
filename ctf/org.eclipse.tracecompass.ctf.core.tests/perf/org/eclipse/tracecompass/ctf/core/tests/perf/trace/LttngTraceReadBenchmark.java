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

package org.eclipse.tracecompass.ctf.core.tests.perf.trace;

import static org.junit.Assert.fail;

import java.io.File;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.junit.Test;

/**
 * Benchmark of the CTF parser for reading an Lttng Kernel trace. Before running
 * this benchmark, the user should update the "path" variable with the path to
 * the main directory containing the trace(s) he wishes to use for the test. He
 * may generate such a trace using a tracer like Lttng.
 *
 *
 * @author Abdellah Rahmani
 */
public class LttngTraceReadBenchmark {

    private static final String TEST_SUITE_NAME = "Lttng Read Benchmark";
    private static final String TEST_ID = "org.eclipse.linuxtools#" + TEST_SUITE_NAME;
    private static final int LOOP_COUNT = 10;

    /**
     * Benchmark reading the Lttng traces
     */
    @Test
    public void testKernelTrace() {

        String directoryPath = "null";
        File parentDirectory = new File(directoryPath);
        if (!parentDirectory.isDirectory() || parentDirectory.list() == null) {
            System.err.println(String.format("Trace directory not found !\nYou need to setup the directory path before "
                    + "running thid benchmark. See the javadoc of this class."));
            return;
        }
        File filesList[] = parentDirectory.listFiles();
        for (File file : filesList) {
            String path = file.getAbsolutePath() + "/kernel";

            CTFTrace trace = null;
            try {
                trace = new CTFTrace(path);
            } catch (CTFException e) {
                fail(e.getMessage());
            }

            readTrace(trace, "Kernel trace: " + path, false);
        }
    }

    private static void readTrace(CTFTrace trace, String testName, boolean inGlobalSummary) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + '#' + testName);
        perf.tagAsSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);

        if (inGlobalSummary) {
            perf.tagAsGlobalSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);
        }

        for (int loop = 0; loop < LOOP_COUNT; loop++) {
            pm.start();
            try {
                try (CTFTraceReader traceReader = new CTFTraceReader(trace);) {

                    while (traceReader.hasMoreEvents()) {
                        IEventDefinition ed = traceReader.getCurrentEventDef();
                        /* Do something with the event */
                        ed.getCPU();
                        traceReader.advance();
                    }
                }
            } catch (CTFException e) {
                fail("Test failed at iteration " + loop + ':' + e.getMessage());
            }
            pm.stop();
        }
        pm.commit();
    }
}
