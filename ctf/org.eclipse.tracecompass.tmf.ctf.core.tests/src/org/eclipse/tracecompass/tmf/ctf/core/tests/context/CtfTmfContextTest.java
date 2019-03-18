/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation
 *   Alexandre Montplaisir
 *   Patrick Tasse - Updated for removal of context clone
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.context;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.ctf.core.context.CtfTmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the CtfTmfLightweightContext class
 *
 * @author Matthew Khouzam
 * @version 1.1
 */
public class CtfTmfContextTest {

    private static final @NonNull CtfTestTrace testTrace = CtfTestTrace.KERNEL;
    private static final long begin = 1332170682440133097L; /*
                                                             * Trace start time
                                                             */
    private static final long end = 1332170692664579801L; /* Trace end time */

    private static CtfTmfTrace trace;

    private class SeekerThread extends Thread {
        long val;

        public void setVal(long val) {
            this.val = val;
        }
    }

    /**
     * Pre-test initialization
     */
    @BeforeClass
    public static void setUp() {
        trace = CtfTmfTestTraceUtils.getTrace(testTrace);
    }

    /**
     * Post-test clean-up.
     */
    @AfterClass
    public static void tearDown() {
        if (trace != null) {
            trace.dispose();
        }
    }

    /**
     * Index all the events in the test trace.
     */
    @Test
    public void testIndexing() {
        CtfTmfContext context = new CtfTmfContext(trace);
        context.seek(0);

        int count = 0;
        while (trace.getNext(context) != null) {
            count++;
        }
        assertTrue(count > 0);
    }

    /**
     * Context fuzzer. Use an amount of contexts greater than the size of the
     * iterator cache and have them access the trace in parallel.
     *
     * @throws InterruptedException
     *             Would fail the test
     */
    @Test
    public void testTooManyContexts() throws InterruptedException {
        final int lwcCount = 101;
        double increment = (end - begin) / lwcCount;
        final ArrayList<Long> vals = new ArrayList<>();
        final ArrayList<Thread> threads = new ArrayList<>();

        double time = begin;
        for (int i = 0; i < lwcCount; i++) {
            SeekerThread thread = new SeekerThread() {
                @Override
                public void run() {
                    CtfTmfContext lwc = new CtfTmfContext(trace);
                    lwc.seek(val);
                    trace.getNext(lwc);
                    synchronized (trace) {
                        if (lwc.getCurrentEvent() != null) {
                            vals.add(lwc.getCurrentEvent().getTimestamp().getValue());
                        }
                    }
                }
            };
            thread.setVal((long) time);
            threads.add(thread);
            thread.start();
            time += increment;
        }

        for (Thread t : threads) {
            t.join();
        }
        assertEquals("seeks done ", lwcCount, vals.size());
        for (long val : vals) {
            assertTrue("val >= begin, " + val + " " + begin, val >= begin);
            assertTrue("val >= end, " + val + " " + end, val <= end);
        }
    }
}
