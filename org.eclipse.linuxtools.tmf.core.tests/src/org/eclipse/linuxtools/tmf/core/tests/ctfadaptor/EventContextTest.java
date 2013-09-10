/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.ctfadaptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for reading event contexts from a CtfTmfTrace.
 *
 * @author Alexandre Montplaisir
 */
public class EventContextTest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /* We use test trace #2, kernel_vm, which has event contexts */
    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.KERNEL_VM;

    private static CtfTmfTrace fixture;
    private static long startTime;
    private static long endTime;

    // ------------------------------------------------------------------------
    // Class  methods
    // ------------------------------------------------------------------------

    /**
     * Perform pre-class initialization.
     *
     * @throws TmfTraceException
     *             If the test trace is not found
     */
    @BeforeClass
    public static void setUp() throws TmfTraceException {
        assumeTrue(testTrace.exists());
        fixture = new CtfTmfTrace();
        fixture.initTrace((IResource) null, testTrace.getPath(), CtfTmfEvent.class);
        fixture.indexTrace(true);

        startTime = fixture.getStartTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
        endTime = fixture.getEndTime().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
    }

    /**
     * Perform post-class clean-up.
     */
    @AfterClass
    public static void tearDown() {
        if (fixture != null) {
            fixture.dispose();
        }
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Make sure the trace is the correct one, and its timestamps are read
     * correctly.
     */
    @Test
    public void testTrace() {
        assertEquals(1363700740555978750L, startTime);
        assertEquals(1363700770550261288L, endTime);
    }

    /**
     * Test the context of the very first event of the trace.
     */
    @Test
    public void testContextStart() {
        CtfTmfEvent firstEvent = getEventAt(startTime);
        long perfPageFault = (Long) firstEvent.getContent().getField("context._perf_page_fault").getValue();
        String procname = (String) firstEvent.getContent().getField("context._procname").getValue();
        long tid = (Long) firstEvent.getContent().getField("context._tid").getValue();

        assertEquals(613, perfPageFault);
        assertEquals("lttng-sessiond", procname);
        assertEquals(1230, tid);
    }

    /**
     * Test the context of the event at 1363700745.559739078.
     */
    @Test
    public void testContext1() {
        long time = startTime + 5000000000L; // 1363700745.559739078
        CtfTmfEvent event = getEventAt(time);
        long perfPageFault = (Long) event.getContent().getField("context._perf_page_fault").getValue();
        String procname = (String) event.getContent().getField("context._procname").getValue();
        long tid = (Long) event.getContent().getField("context._tid").getValue();

        assertEquals(6048, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the event at 1363700750.559707062.
     */
    @Test
    public void testContext2() {
        long time = startTime + 2 * 5000000000L; // 1363700750.559707062
        CtfTmfEvent event = getEventAt(time);
        long perfPageFault = (Long) event.getContent().getField("context._perf_page_fault").getValue();
        String procname = (String) event.getContent().getField("context._procname").getValue();
        long tid = (Long) event.getContent().getField("context._tid").getValue();

        assertEquals(13258, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the event at 1363700755.555723128, which is roughly
     * mid-way through the trace.
     */
    @Test
    public void testContextMiddle() {
        long midTime = startTime + (endTime - startTime) / 2L; // 1363700755.555723128
        CtfTmfEvent midEvent = getEventAt(midTime);
        long perfPageFault = (Long) midEvent.getContent().getField("context._perf_page_fault").getValue();
        String procname = (String) midEvent.getContent().getField("context._procname").getValue();
        long tid = (Long) midEvent.getContent().getField("context._tid").getValue();

        assertEquals(19438, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the event at 1363700760.559719724.
     */
    @Test
    public void testContext3() {
        long time = startTime + 4 * 5000000000L; // 1363700760.559719724
        CtfTmfEvent event = getEventAt(time);
        long perfPageFault = (Long) event.getContent().getField("context._perf_page_fault").getValue();
        String procname = (String) event.getContent().getField("context._procname").getValue();
        long tid = (Long) event.getContent().getField("context._tid").getValue();

        assertEquals(21507, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the event at 1363700765.559714634.
     */
    @Test
    public void testContext4() {
        long time = startTime + 5 * 5000000000L; // 1363700765.559714634
        CtfTmfEvent event = getEventAt(time);
        long perfPageFault = (Long) event.getContent().getField("context._perf_page_fault").getValue();
        String procname = (String) event.getContent().getField("context._procname").getValue();
        long tid = (Long) event.getContent().getField("context._tid").getValue();

        assertEquals(21507, perfPageFault);
        assertEquals("swapper/0", procname);
        assertEquals(0, tid);
    }

    /**
     * Test the context of the last event of the trace.
     */
    @Test
    public void testContextEnd() {
        CtfTmfEvent lastEvent = getEventAt(endTime);
        long perfPageFault = (Long) lastEvent.getContent().getField("context._perf_page_fault").getValue();
        String procname = (String) lastEvent.getContent().getField("context._procname").getValue();
        long tid = (Long) lastEvent.getContent().getField("context._tid").getValue();

        assertEquals(22117, perfPageFault);
        assertEquals("lttng-sessiond", procname);
        assertEquals(1230, tid);
    }

    // ------------------------------------------------------------------------
    // Private stuff
    // ------------------------------------------------------------------------

    private synchronized CtfTmfEvent getEventAt(long timestamp) {
        EventContextTestRequest req = new EventContextTestRequest(timestamp);
        fixture.sendRequest(req);
        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
            return null;
        }
        return req.getEvent();
    }

    private class EventContextTestRequest extends TmfEventRequest {

        private CtfTmfEvent retEvent = null;

        public EventContextTestRequest(long timestamp) {
            super(CtfTmfEvent.class,
                    new TmfTimeRange(new CtfTmfTimestamp(timestamp), TmfTimestamp.BIG_CRUNCH),
                    0, 1, ExecutionType.FOREGROUND);
        }

        @Override
        public void handleData(ITmfEvent event) {
            retEvent = (CtfTmfEvent) event;
        }

        public CtfTmfEvent getEvent() {
            return retEvent;
        }
    }
}
