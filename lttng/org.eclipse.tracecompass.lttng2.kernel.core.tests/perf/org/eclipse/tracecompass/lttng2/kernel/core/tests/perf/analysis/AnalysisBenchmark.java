/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Alexandre Montplaisir - Convert to org.eclipse.test.performance test
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysisModule;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTrace;
import org.junit.Test;

/**
 * This is a test of the time to build a kernel state system
 *
 * @author Genevieve Bastien
 */
public class AnalysisBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#LTTng kernel analysis";
    private static final int LOOP_COUNT = 25;

    /**
     * Run the benchmark with "trace2"
     */
    @Test
    public void testTrace2() {
        runTest(CtfTmfTestTrace.TRACE2, "Trace2");
    }

    private static void runTest(CtfTmfTestTrace testTrace, String testName) {
        assumeTrue(testTrace.exists());

        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + '#' + testName);
        perf.tagAsSummary(pm, "LTTng Kernel Analysis: " + testName, Dimension.CPU_TIME);

        if (testTrace == CtfTmfTestTrace.TRACE2) {
            /* Do not show all traces in the global summary */
            perf.tagAsGlobalSummary(pm, "LTTng Kernel Analysis: " + testName, Dimension.CPU_TIME);
        }

        for (int i = 0; i < LOOP_COUNT; i++) {
            IAnalysisModule module = null;
            try (LttngKernelTrace trace = new LttngKernelTrace()) {
                module = new KernelAnalysisModule();
                module.setId("test");
                trace.initTrace(null, testTrace.getPath(), CtfTmfEvent.class);
                module.setTrace(trace);

                pm.start();
                TmfTestHelper.executeAnalysis(module);
                pm.stop();

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
            }
        }
        pm.commit();
        testTrace.dispose();
    }
}
