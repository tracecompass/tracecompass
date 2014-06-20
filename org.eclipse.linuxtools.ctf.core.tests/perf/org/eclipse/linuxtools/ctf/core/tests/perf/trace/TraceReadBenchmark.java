/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Convert to a org.eclipse.test.performance test
 *******************************************************************************/

package org.eclipse.linuxtools.ctf.core.tests.perf.trace;

import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.ctf.core.event.EventDefinition;
import org.eclipse.linuxtools.ctf.core.tests.shared.CtfTestTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFReaderException;
import org.eclipse.linuxtools.ctf.core.trace.CTFTrace;
import org.eclipse.linuxtools.ctf.core.trace.CTFTraceReader;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.junit.Test;

/**
 * Benchmark of the CTF parser for reading a trace
 *
 * @author Matthew Khouzam
 * @author Alexandre Montplaisir
 */
public class TraceReadBenchmark {

    private static final String TEST_SUITE_NAME = "CTF Read Benchmark";
    private static final String TEST_ID = "org.eclipse.linuxtools#" + TEST_SUITE_NAME;
    private static final int LOOP_COUNT = 25;

    /**
     * Benchmark reading the trace "kernel"
     */
    @Test
    public void testKernelTrace() {
        readTrace(CtfTestTrace.KERNEL, "trace-kernel", true);
    }

    /**
     * Benchmark reading the bigger trace "kernel_vm"
     */
    @Test
    public void testKernelVmTrace() {
        readTrace(CtfTestTrace.KERNEL_VM, "trace-kernel-vm", false);
    }

    private static void readTrace(CtfTestTrace testTrace, String testName, boolean inGlobalSummary) {
        assumeTrue(testTrace.exists());

        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + '#' + testName);
        perf.tagAsSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);

        if (inGlobalSummary) {
            perf.tagAsGlobalSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);
        }

        for (int loop = 0; loop < LOOP_COUNT; loop++) {
            pm.start();
            try (CTFTrace trace = testTrace.getTrace();
                    CTFTraceReader traceReader = new CTFTraceReader(trace);) {

                while (traceReader.hasMoreEvents()) {
                    EventDefinition ed = traceReader.getCurrentEventDef();
                    /* Do something with the event */
                    ed.getCPU();
                    traceReader.advance();
                }

            } catch (CTFReaderException e) {
                /* Should not happen if assumeTrue() passed above */
                fail("Test failed at iteration " + loop + ':' + e.getMessage());
            }
            pm.stop();
        }
        pm.commit();
    }
}
