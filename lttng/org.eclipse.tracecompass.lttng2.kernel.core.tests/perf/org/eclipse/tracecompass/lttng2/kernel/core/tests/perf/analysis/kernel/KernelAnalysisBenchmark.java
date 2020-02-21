/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *   Alexandre Montplaisir - Convert to org.eclipse.test.performance test
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis.kernel;

import static org.junit.Assert.fail;

import java.io.File;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
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
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * This is a test of the time to build a kernel state system
 *
 * @author Genevieve Bastien
 */
@RunWith(Parameterized.class)
public class KernelAnalysisBenchmark {

    /**
     * Test test ID for kernel analysis benchmarks
     */
    public static final String TEST_ID = "org.eclipse.linuxtools#LTTng kernel analysis#";
    private static final int LOOP_COUNT = 25;

    private final TestModule fTestModule;

    private enum TestModule {

        NORMAL_EXECUTION(""),
        NULL_BACKEND("(Data not saved to disk)");

        private final String fName;

        private TestModule(String name) {
            fName = name;
        }

        public String getTestNameString() {
            return fName;
        }

        public static IAnalysisModule getNewModule(TestModule moduleType) {
            switch (moduleType) {
            case NORMAL_EXECUTION:
                return new KernelAnalysisModule();
            case NULL_BACKEND:
                return new KernelAnalysisModuleNullBeStub();
            default:
                throw new IllegalStateException();
            }
        }
    }

    /**
     * Constructor
     *
     * @param testName
     *            A name for the test, to display in the header
     * @param module
     *            A test case parameter for this test
     */
    public KernelAnalysisBenchmark(String testName, TestModule module) {
        fTestModule = module;
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { TestModule.NORMAL_EXECUTION.name(), TestModule.NORMAL_EXECUTION },
                { TestModule.NULL_BACKEND.name(), TestModule.NULL_BACKEND }
        });
    }

    /**
     * Run the benchmark with "trace2"
     */
    @Test
    public void testTrace2() {
        runTest(CtfTestTrace.TRACE2, "Trace2", fTestModule);
    }

    /**
     * Run the benchmark with "many thread"
     */
    @Test
    public void testManyThreads() {
        runTest(CtfTestTrace.MANY_THREADS, "Many Threads", fTestModule);
    }

    /**
     * Run the benchmark with "django httpd"
     */
    @Test
    public void testDjangoHttpd() {
        runTest(CtfTestTrace.DJANGO_HTTPD, "Django httpd", fTestModule);
    }

    private static void runTest(@NonNull CtfTestTrace testTrace, String testName, TestModule testModule) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + testName + testModule.getTestNameString());
        perf.tagAsSummary(pm, "LTTng Kernel Analysis: " + testName + testModule.getTestNameString(), Dimension.CPU_TIME);

        if ((testTrace == CtfTestTrace.TRACE2) && (testModule == TestModule.NORMAL_EXECUTION)) {
            /* Do not show all traces in the global summary */
            perf.tagAsGlobalSummary(pm, "LTTng Kernel Analysis" + testModule.getTestNameString() + ": " + testName, Dimension.CPU_TIME);
        }

        for (int i = 0; i < LOOP_COUNT; i++) {
            LttngKernelTrace trace = null;
            IAnalysisModule module = null;
            // TODO Allow the utility method to instantiate trace sub-types
            // directly.
            String path = CtfTmfTestTraceUtils.getTrace(testTrace).getPath();

            try {
                trace = new LttngKernelTrace();
                module = TestModule.getNewModule(testModule);
                module.setId("test");
                trace.initTrace(null, path, CtfTmfEvent.class);
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
                if (trace != null) {
                    trace.dispose();
                }
            }
        }
        pm.commit();
        CtfTmfTestTraceUtils.dispose(testTrace);
    }
}
