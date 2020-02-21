/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.event.ITmfLostEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.synchronization.TimestampTransformFactory;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.After;
import org.junit.Before;
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

    private static final @NonNull CtfTestTrace testTrace = CtfTestTrace.HELLO_LOST;

    private CtfTmfTrace fixture = null;

    /**
     * Class setup
     */
    @Before
    public void setUp() {
        fixture = CtfTmfTestTraceUtils.getTrace(testTrace);
        fixture.indexTrace(true);
    }

    /**
     * Clean-up
     */
    @After
    public void tearDown() {
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
     * Test that the number of events is reported correctly (a range of lost
     * events is counted as one event). Events could be wrongly counted as lost
     * events in certain situations.
     */
    @Test
    public void testNbEventsBug475007() {
        final CtfTestTrace tmfTestTrace = CtfTestTrace.DYNSCOPE;
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(tmfTestTrace);
        trace.indexTrace(true);

        final long expectedReal = 100003;
        final long expectedLost = 1;

        EventCountRequest req = new EventCountRequest();
        trace.sendRequest(req);
        try {
            req.waitForCompletion();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        assertEquals(expectedReal, req.getReal());
        assertEquals(expectedLost, req.getLost());

        trace.dispose();
    }

    /**
     * Test getting the first lost event from the trace.
     */
    @Test
    public void testFirstLostEvent() {
        final long rank = 190;
        final long startTime = 1376592664828900165L;
        final ITmfTimestamp start = TmfTimestamp.fromNanos(startTime);
        final ITmfTimestamp end = TmfTimestamp.fromNanos(startTime + 502911L);
        final long nbLost = 859;

        validateLostEvent(rank, start, end, nbLost);
    }

    /**
     * Test getting the second lost event from the trace.
     */
    @Test
    public void testSecondLostEvent() {
        final long rank = 229;
        final long startTime = 1376592664829477058L;
        final ITmfTimestamp start = TmfTimestamp.fromNanos(startTime);
        final ITmfTimestamp end = TmfTimestamp.fromNanos(startTime + 347456L);
        final long nbLost = 488;

        validateLostEvent(rank, start, end, nbLost);
    }

    private void validateLostEvent(final long rank, final @NonNull ITmfTimestamp start, final ITmfTimestamp end, final long nbLost) {
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
        final ITmfTimestamp ts = TmfTimestamp.fromNanos(1376592664829425780L);

        final CtfTmfEvent event = getOneEventTime(ts);
        /* Make sure seeking by rank yields the same event */
        final CtfTmfEvent event2 = getOneEventRank(rank);
        assertEquals(event, event2);

        assertFalse(event instanceof ITmfLostEvent);
        assertEquals(ts, event.getTimestamp());
    }

    /**
     * Test getting a lost event from a trace that has a timestamp transform.
     */
    @Test
    public void testLostEventWithTransform() {
        CtfTmfTrace trace = CtfTmfTestTraceUtils.getTrace(testTrace);
        long offset = 1234567890L;
        trace.setTimestampTransform(TimestampTransformFactory.createWithOffset(offset));
        trace.indexTrace(true);

        final long rank = 190;
        final ITmfTimestamp start = TmfTimestamp.fromNanos(1376592664828900165L + offset);
        final ITmfTimestamp end = TmfTimestamp.fromNanos(1376592664828900165L + 502911L + offset);
        final long nbLost = 859;

        ITmfContext context = trace.seekEvent(rank);
        final CtfTmfEvent ev = trace.getNext(context);
        context.dispose();

        assertTrue(ev instanceof ITmfLostEvent);
        ITmfLostEvent event = (ITmfLostEvent) ev;

        assertEquals(start, event.getTimestamp());
        assertEquals(start, event.getTimeRange().getStartTime());
        assertEquals(end, event.getTimeRange().getEndTime());
        assertEquals(nbLost, event.getNbLostEvents());

        trace.setTimestampTransform(null);
        trace.dispose();
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

    private CtfTmfEvent getOneEventTime(@NonNull ITmfTimestamp ts) {
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

        public OneEventRequestPerTs(@NonNull ITmfTimestamp ts) {
            super(CtfTmfEvent.class,
                    new TmfTimeRange(ts, ts),
                    0, ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
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
                    ITmfEventRequest.ALL_DATA, ExecutionType.FOREGROUND);
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
