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

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfLightweightContext;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the CtfTmfLightweightContext class
 *
 * @author Matthew Khouzam
 * @version 1.1
 */
public class CtfTmfLightweightContextTest {

    private static final String PATH = TestParams.getPath();
    private static final long begin = 1332170682440133097L; /* Trace start time */
    private static final long end = 1332170692664579801L; /* Trace end time */

    private CtfTmfTrace fixture;

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
        fixture = new CtfTmfTrace();
        fixture.initTrace((IResource) null, PATH, CtfTmfEvent.class);
    }

    /**
     * Index all the events in the test trace.
     */
    @Test
    public void testIndexing() {
        CtfTmfLightweightContext context = new CtfTmfLightweightContext(fixture);
        context.seek(0);

        int count = 0;
        while (fixture.getNext(context) != null) {
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
        final ArrayList<CtfTmfLightweightContext> tooManyContexts = new ArrayList<CtfTmfLightweightContext>();

        for (double i = begin; i < end; i += increment) {
            SeekerThread thread = new SeekerThread() {
                @Override
                public void run() {
                    CtfTmfLightweightContext lwc = new CtfTmfLightweightContext(fixture);
                    lwc.seek(val);
                    fixture.getNext(lwc);
                    synchronized(fixture){
                        if (lwc.getCurrentEvent() != null) {
                            vals.add(lwc.getCurrentEvent().getTimestampValue());
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
}
