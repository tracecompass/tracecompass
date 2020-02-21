/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexis Cabana-Loriaux - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.perf.analysis;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.lttng2.kernel.core.trace.LttngKernelTrace;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.statistics.ITmfStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsModule;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This is a test of the time to build a statistics module
 *
 * @author Alexis Cabana-Loriaux
 */
@NonNullByDefault
public class StatisticsAnalysisBenchmark {

    private static final String TEST_ID = "org.eclipse.linuxtools#Statistics analysis";
    private static final int LOOP_COUNT = 25;

    /* Event Tests */
    private static final Map<String, Long> fDjangoClientEntriesToTest = new HashMap<>();
    private static final Map<String, Long> fDjangoHttpdEntriesToTest = new HashMap<>();

    /**
     * Build the tests
     */
    @BeforeClass
    public static void buildTest() {
        /*
         * test cases map for the Django-client trace
         */
        fDjangoClientEntriesToTest.put("exit_syscall", 234013L);
        fDjangoClientEntriesToTest.put("sys_recvfrom", 133517L);
        fDjangoClientEntriesToTest.put("irq_handler_exit", 62743L);
        fDjangoClientEntriesToTest.put("irq_handler_entry", 62743L);
        fDjangoClientEntriesToTest.put("softirq_entry", 39627L);

        /*
         * test cases map for the Django-Httpd trace
         */
        fDjangoHttpdEntriesToTest.put("exit_syscall", 174802L);
        fDjangoHttpdEntriesToTest.put("inet_sock_local_out", 36195L);
        fDjangoHttpdEntriesToTest.put("irq_handler_exit", 93040L);
        fDjangoHttpdEntriesToTest.put("irq_handler_entry", 93040L);
        fDjangoHttpdEntriesToTest.put("softirq_entry", 63062L);
    }

    /**
     * Run the benchmark with "django-client"
     */
    @Test
    public void testDjangoClient() {
        runTest(CtfTestTrace.DJANGO_CLIENT, "Django-client trace", fDjangoClientEntriesToTest);
    }

    /**
     * Run the benchmark with "django-Httpd"
     */
    @Test
    public void testDjangoHttpd() {
        runTest(CtfTestTrace.DJANGO_HTTPD, "Django-Httpd trace", fDjangoHttpdEntriesToTest);
    }

    private static void runTest(CtfTestTrace testTrace, String testName, Map<String, Long> testCases) {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + '#' + testName);
        perf.tagAsSummary(pm, "Statistics Analysis: " + testName, Dimension.CPU_TIME);

        if (testTrace == CtfTestTrace.DJANGO_CLIENT || testTrace == CtfTestTrace.DJANGO_HTTPD) {
            /* Do not show all traces in the global summary */
            perf.tagAsGlobalSummary(pm, "Statistics Analysis: " + testName, Dimension.CPU_TIME);
        }

        for (int i = 0; i < LOOP_COUNT; i++) {
            LttngKernelTrace trace = null;
            TmfStatisticsModule module = null;
            try {
                trace = new LttngKernelTrace();
                module = new TmfStatisticsModule();
                module.setId("test");

                // TODO Allow the utility method to return a LttngKernelTrace directly
                CtfTmfTrace ctfTmfTrace = CtfTmfTestTraceUtils.getTrace(testTrace);
                trace.initTrace(null, ctfTmfTrace.getPath(), CtfTmfEvent.class);
                module.setTrace(trace);

                pm.start();
                TmfTestHelper.executeAnalysis(module);
                pm.stop();
                ITmfStatistics stats = module.getStatistics();
                if (stats == null) {
                    throw new IllegalStateException();
                }
                Map<String, Long> map = stats.getEventTypesTotal();
                /*
                 * Test each of the entries
                 */
                try {
                    for (Entry<String, Long> entry : testCases.entrySet()) {
                        Long value = map.get(entry.getKey());
                        assertNotNull(value);
                        assertTrue(value.equals(entry.getValue()));
                    }
                } catch (NullPointerException e) {
                    fail(e.getMessage());
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
        CtfTmfTestTraceUtils.dispose(testTrace);
    }
}
