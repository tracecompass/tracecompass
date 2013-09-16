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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfEvent;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTimestamp;
import org.eclipse.linuxtools.tmf.core.ctfadaptor.CtfTmfTrace;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfLostEvent;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests to verify that lost events are handled correctly.
 *
 * Be wary if you are using Babeltrace to cross-check those values. There could
 * be a bug in Babeltrace with regards to lost events. See
 * http://bugs.lttng.org/issues/589
 *
 * It's not 100% sure at this point which implementation is correct, so for now
 * these tests assume the Java implementation is the right one.
 *
 * @author Alexandre Montplaisir
 */
public class CtfTmfLostEventsTest {

    private static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.HELLO_LOST;

    private static CtfTmfTrace fixture = null;

    /**
     * Class setup
     */
    @BeforeClass
    public static void setUpClass() {
        assumeTrue(testTrace.exists());
        fixture = testTrace.getTrace();
        fixture.indexTrace(true);
    }

    /**
     * Clean-up
     */
    @AfterClass
    public static void tearDownClass() {
        if (fixture != null) {
            fixture.dispose();
        }
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /**
     * Test that the number of events is reported correctly (a range of lost
     * events is counted as one event).
     */
    @Test
    public void testNbEvents() {
        final long expectedReal = 32300;
        final long expectedLost = 562;

        EventCountRequest req = new EventCountRequest();
        fixture.sendRequest(req);
        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(expectedReal, req.getReal());
        assertEquals(expectedLost, req.getLost());
    }

    /**
     * Test getting the first lost event from the trace.
     */
    @Test
    public void testFirstLostEvent() {
        final long rank = 153;
        final ITmfTimestamp start = new CtfTmfTimestamp(1376592664828848222L);
        final ITmfTimestamp end   = new CtfTmfTimestamp(1376592664828848540L);
        final long nbLost = 859;

        final CtfTmfEvent ev = getOneEventTime(start);
        /* Make sure seeking by rank yields the same event */
        final CtfTmfEvent ev2 = getOneEventRank(rank);
        assertEquals(ev, ev2);

        assertTrue(ev instanceof ITmfLostEvent);
        ITmfLostEvent event = (ITmfLostEvent) ev;

        assertEquals(start, event.getTimestamp());
        assertEquals(start, event.getTimeRange().getStartTime());
        assertEquals(end, event.getTimeRange().getEndTime());
        assertEquals(nbLost, event.getNbLostEvents());
    }

    /**
     * Test getting the second lost event from the trace.
     */
    @Test
    public void testSecondLostEvent() {
        final long rank = 191;
        final ITmfTimestamp start = new CtfTmfTimestamp(1376592664829402521L);
        final ITmfTimestamp end   = new CtfTmfTimestamp(1376592664829403076L);
        final long nbLost = 488;

        final CtfTmfEvent ev = getOneEventTime(start);
        /* Make sure seeking by rank yields the same event */
        final CtfTmfEvent ev2 = getOneEventRank(rank);
        assertEquals(ev, ev2);

        assertTrue(ev instanceof ITmfLostEvent);
        ITmfLostEvent event = (ITmfLostEvent) ev;

        assertEquals(start, event.getTimestamp());
        assertEquals(start, event.getTimeRange().getStartTime());
        assertEquals(end, event.getTimeRange().getEndTime());
        assertEquals(nbLost, event.getNbLostEvents());
    }

    /**
     * Test getting one normal event from the trace (lost events should not
     * interfere).
     */
    @Test
    public void testNormalEvent() {
        final long rank = 200;
        final ITmfTimestamp ts = new CtfTmfTimestamp(1376592664829423928L);

        final CtfTmfEvent event = getOneEventTime(ts);
        /* Make sure seeking by rank yields the same event */
        final CtfTmfEvent event2 = getOneEventRank(rank);
        assertEquals(event, event2);

        assertFalse(event instanceof ITmfLostEvent);
        assertEquals(ts, event.getTimestamp());
    }

    // ------------------------------------------------------------------------
    // Event requests
    // ------------------------------------------------------------------------

    private CtfTmfEvent getOneEventRank(long rank) {
        OneEventRequestPerRank req = new OneEventRequestPerRank(rank);
        fixture.sendRequest(req);
        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return req.getEvent();
    }

    private CtfTmfEvent getOneEventTime(ITmfTimestamp ts) {
        OneEventRequestPerTs req = new OneEventRequestPerTs(ts);
        fixture.sendRequest(req);
        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return req.getEvent();
    }

    private class OneEventRequestPerRank extends TmfEventRequest {

        private CtfTmfEvent event = null;

        public OneEventRequestPerRank(long rank) {
            super(CtfTmfEvent.class, TmfTimeRange.ETERNITY, rank, 1, ExecutionType.FOREGROUND);
        }

        @Override
        public void handleData(ITmfEvent ev) {
            /* Type is checked by the request, cast should be safe */
            event = (CtfTmfEvent) ev;
        }

        public CtfTmfEvent getEvent() {
            return event;
        }
    }

    private class OneEventRequestPerTs extends TmfEventRequest {

        private CtfTmfEvent event = null;

        public OneEventRequestPerTs(ITmfTimestamp ts) {
            super(CtfTmfEvent.class,
                    new TmfTimeRange(ts, TmfTimestamp.PROJECT_IS_CANNED),
                    0, 1, ExecutionType.FOREGROUND);
        }

        @Override
        public void handleData(ITmfEvent ev) {
            event = (CtfTmfEvent) ev;
        }

        public CtfTmfEvent getEvent() {
            return event;
        }
    }

    private class EventCountRequest extends TmfEventRequest {

        private long nbReal = 0;
        private long nbLost = 0;

        public EventCountRequest() {
            super(CtfTmfEvent.class, TmfTimeRange.ETERNITY, 0,
                    TmfDataRequest.ALL_DATA, ExecutionType.FOREGROUND);
        }

        @Override
        public void handleData(ITmfEvent event) {
            if (event instanceof ITmfLostEvent) {
                nbLost++;
            } else {
                nbReal++;
            }
        }

        public long getReal() {
            return nbReal;
        }

        public long getLost() {
            return nbLost;
        }
    }
}
