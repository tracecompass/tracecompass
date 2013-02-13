/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Matthew Khouzam - Initial implementation
 *   Alexandre Montplaisir
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfContext;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the CtfTmfLightweightContext class
 *
 * @author Matthew Khouzam
 * @version 1.1
 */
public class CtfTmfContextTest {

    private static final int TRACE_INDEX = 0;
    private static final long begin = 1332170682440133097L; /* Trace start time */
    private static final long end = 1332170692664579801L; /* Trace end time */

    private CtfTmfTrace trace;

    private class SeekerThread extends Thread {
        long val;

        public void setVal(long val) {
            this.val = val;
        }
    }

    /**
     * Pre-test initialization
     *
     * @throws TmfTraceException
     *             If the trace couldn't be init'ed, which shouldn't happen.
     */
    @Before
    public void setUp() throws TmfTraceException {
        assumeTrue(CtfTmfTestTraces.tracesExist());
        trace = new CtfTmfTrace();
        String path = CtfTmfTestTraces.getTestTracePath(TRACE_INDEX);
        trace.initTrace((IResource) null, path, CtfTmfEvent.class);
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
        final ArrayList<Long> vals = new ArrayList<Long>();
        final ArrayList<Thread> threads = new ArrayList<Thread>();
        final ArrayList<CtfTmfContext> tooManyContexts = new ArrayList<CtfTmfContext>();

        for (double i = begin; i < end; i += increment) {
            SeekerThread thread = new SeekerThread() {
                @Override
                public void run() {
                    CtfTmfContext lwc = new CtfTmfContext(trace);
                    lwc.seek(val);
                    trace.getNext(lwc);
                    synchronized(trace){
                        if (lwc.getCurrentEvent() != null) {
                            vals.add(lwc.getCurrentEvent().getTimestamp().getValue());
                        }
                        tooManyContexts.add(lwc);
                    }
                }
            };
            thread.setVal((long)i);
            threads.add(thread);
            thread.start();
        }

        for( Thread t: threads){
            t.join();
        }

        for( Long val : vals){
            assertTrue(val >= begin);
            assertTrue(val <= end);
        }
    }

    /**
     * Test for clone method
     */
    @Test
    public void testClone() {
        CtfTmfContext fixture1 = new CtfTmfContext(trace);
        CtfTmfContext fixture2 = fixture1.clone();
        //assertTrue(fixture1.equals(fixture2)); FIXME no .equals() override!
        assertNotSame(fixture1, fixture2);

        /* Make sure clone() did its job */
        assertSame(fixture1.getTrace(), fixture2.getTrace());
        assertSame(fixture1.getLocation(), fixture2.getLocation());
        assertSame(fixture1.getRank(), fixture2.getRank());
    }
}
