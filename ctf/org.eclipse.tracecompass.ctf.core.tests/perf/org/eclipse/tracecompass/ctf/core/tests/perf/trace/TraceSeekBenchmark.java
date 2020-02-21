/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Matthew Khouzam - Initial API and implementation
 *   Alexandre Montplaisir - Convert to a org.eclipse.test.performance test
 *******************************************************************************/

package org.eclipse.tracecompass.ctf.core.tests.perf.trace;

import static org.junit.Assert.fail;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.ctf.core.CTFException;
import org.eclipse.tracecompass.ctf.core.event.IEventDefinition;
import org.eclipse.tracecompass.ctf.core.tests.shared.CtfTestTraceUtils;
import org.eclipse.tracecompass.ctf.core.trace.CTFTrace;
import org.eclipse.tracecompass.ctf.core.trace.CTFTraceReader;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.Test;

/**
 * Tests for performance regressions of the ctf reader. It only tests the ctf
 * reader, not tmf. <br>
 * This test runs in 3 passes.
 * <ul>
 * <li>first it opens a trace</li>
 * <li>then it reads the trace completely</li>
 * <li>then it randomly (seeded) seeks NB_SEEKS locations in the trace and reads
 * one event at each position.</li>
 * </ul>
 *
 * @author Matthew Khouzam
 * @author Alexandre Montplaisir
 */
public class TraceSeekBenchmark {

    private static final Random RND = new Random(1000);

    private static final int LOOP_COUNT = 25;
    private static final int NB_SEEKS = 500;
    private static final String TEST_SUITE_NAME = "CTF Read & Seek Benchmark (" + NB_SEEKS + " seeks)";
    private static final String TEST_ID = "org.eclipse.linuxtools#" + TEST_SUITE_NAME;

    /**
     * Run the benchmark scenario for the trace "kernel"
     *
     * @throws CTFException
     *             Should not happen
     */
    @Test
    public void testKernelTrace() throws CTFException {
        readAndSeekTrace(CtfTestTrace.KERNEL, "trace-kernel", true);
    }

    private static void readAndSeekTrace(CtfTestTrace testTrace, String testName, boolean inGlobalSummary) throws CTFException {
        Performance perf = Performance.getDefault();
        PerformanceMeter pm = perf.createPerformanceMeter(TEST_ID + '#' + testName);
        perf.tagAsSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);

        if (inGlobalSummary) {
            perf.tagAsGlobalSummary(pm, TEST_SUITE_NAME + ':' + testName, Dimension.CPU_TIME);
        }

        for (int loop = 0; loop < LOOP_COUNT; loop++) {
            CTFTrace trace = CtfTestTraceUtils.getTrace(testTrace);
            try (CTFTraceReader traceReader = new CTFTraceReader(trace);) {

                /* Read the whole trace to find out the start and end times */
                IEventDefinition firstEvent = traceReader.getCurrentEventDef();
                final long startTime = firstEvent.getTimestamp();
                long endTime = startTime;
                while (traceReader.hasMoreEvents()) {
                    IEventDefinition ev = traceReader.getCurrentEventDef();
                    endTime = ev.getTimestamp();
                    traceReader.advance();
                }

                /* Generate the timestamps we will seek to */
                List<Long> seekTimestamps = new LinkedList<>();
                final long range = endTime - startTime;
                for (int i = 0; i < NB_SEEKS; i++) {
                    long delta = (RND.nextLong() % range);
                    if (delta < 0) {
                        delta += range;
                    }
                    seekTimestamps.add(startTime + delta);
                }

                /* Benchmark seeking to the generated timestamps */
                pm.start();
                for (Long ts : seekTimestamps) {
                    traceReader.seek(ts);
                    traceReader.advance();
                }
                pm.stop();

            } catch (CTFException e) {
                /* Should not happen if assumeTrue() passed above */
                fail("Test failed at iteration " + loop + ':' + e.getMessage());
            }
        }
        pm.commit();
    }
}
