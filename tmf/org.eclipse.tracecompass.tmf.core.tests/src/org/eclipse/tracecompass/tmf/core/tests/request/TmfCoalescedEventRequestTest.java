/*******************************************************************************
 * Copyright (c) 2009, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.IllformedLocaleException;
import java.util.Vector;

import org.eclipse.tracecompass.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.tracecompass.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.tracecompass.tmf.core.component.ITmfEventProvider;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestTrace;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.tests.stubs.request.TmfEventRequestStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfCoalescedEventRequest class.
 */
@SuppressWarnings("javadoc")
public class TmfCoalescedEventRequestTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final TmfTimeRange range1 = TmfTimeRange.ETERNITY;
    private final TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.create(0, ITmfTimestamp.SECOND_SCALE), TmfTimestamp.BIG_CRUNCH);

    private TmfCoalescedEventRequest fRequest1;
    private TmfCoalescedEventRequest fRequest2;
    private TmfCoalescedEventRequest fRequest3;
    private TmfCoalescedEventRequest fRequest4;

    private TmfCoalescedEventRequest fRequest1c;

    private int fRequestCount;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        fRequest1 = new TmfCoalescedEventRequest(ITmfEvent.class, range1, 0, 100, ExecutionType.FOREGROUND, 0);
        fRequest2 = new TmfCoalescedEventRequest(ITmfEvent.class, range2, 0, 100, ExecutionType.FOREGROUND, 0);
        fRequest3 = new TmfCoalescedEventRequest(ITmfEvent.class, range2, 0, 200, ExecutionType.FOREGROUND, 0);
        fRequest4 = new TmfCoalescedEventRequest(ITmfEvent.class, range2, 0, 200, ExecutionType.FOREGROUND, 0);

        fRequest1c = new TmfCoalescedEventRequest(ITmfEvent.class, range1, 0, 100, ExecutionType.FOREGROUND, 0);

        fRequestCount = fRequest1c.getRequestId() + 1;
    }

    private TmfCoalescedEventRequest setupTestRequest(final boolean[] flags) {

        TmfCoalescedEventRequest request = new TmfCoalescedEventRequest(ITmfEvent.class, range1, 0, 100, ExecutionType.FOREGROUND, 0) {
            @Override
            public void handleCompleted() {
                super.handleCompleted();
                flags[0] = true;
            }

            @Override
            public void handleSuccess() {
                super.handleSuccess();
                flags[1] = true;
            }

            @Override
            public void handleFailure() {
                super.handleFailure();
                flags[2] = true;
            }

            @Override
            public void handleCancel() {
                super.handleCancel();
                flags[3] = true;
            }
        };
        return request;
    }

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    @Test
    public void testTmfCoalescedEventRequestIndexNbEventsBlocksize() {
        TmfCoalescedEventRequest request = new TmfCoalescedEventRequest(ITmfEvent.class, range1, 0, 100, ExecutionType.FOREGROUND, 0);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType", ITmfEvent.class, request.getDataType());

        assertEquals("getRange", range1, request.getRange());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEquals() {
        /*
         * No two different requests should be "equal", even if they used the
         * same constructor parameters.
         */
        assertTrue(fRequest1.equals(fRequest1));
        assertFalse(fRequest1.equals(fRequest2));
        assertFalse(fRequest1.equals(fRequest3));
        assertFalse(fRequest1.equals(fRequest4));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        String expected1 = "[TmfCoalescedEventRequest(" + fRequest1.getRequestId() + ",ITmfEvent,FOREGROUND," + range1 + ",0,100, [])]";
        String expected2 = "[TmfCoalescedEventRequest(" + fRequest2.getRequestId() + ",ITmfEvent,FOREGROUND," + range2 + ",0,100, [])]";
        String expected3 = "[TmfCoalescedEventRequest(" + fRequest3.getRequestId() + ",ITmfEvent,FOREGROUND," + range2 + ",0,200, [])]";
        String expected4 = "[TmfCoalescedEventRequest(" + fRequest4.getRequestId() + ",ITmfEvent,FOREGROUND," + range2 + ",0,200, [])]";

        assertEquals("toString", expected1, fRequest1.toString());
        assertEquals("toString", expected2, fRequest2.toString());
        assertEquals("toString", expected3, fRequest3.toString());
        assertEquals("toString", expected4, fRequest4.toString());
    }

    // ------------------------------------------------------------------------
    // isCompatible
    // ------------------------------------------------------------------------

    @Test
    public void testIsCompatible() {
        TmfCoalescedEventRequest coalescedRequest = new TmfCoalescedEventRequest(ITmfEvent.class, range1, 0, 100, ExecutionType.FOREGROUND, 0);
        TmfEventRequest req1 = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        TmfEventRequest req2 = new TmfEventRequestStub(ITmfEvent.class, range2, 100, 200);
        TmfEventRequest req3 = new TmfEventRequestStub(ITmfEvent.class, range1, 101, 200);

        assertTrue("isCompatible", coalescedRequest.isCompatible(req1));
        assertTrue("isCompatible", coalescedRequest.isCompatible(req2));
        assertTrue("isCompatible", coalescedRequest.isCompatible(req3));
    }

    @Test
    public void testIsCompatibleDependency() {
        TmfCoalescedEventRequest coalescedRequest = new TmfCoalescedEventRequest(ITmfEvent.class, range1, 0, 100, ExecutionType.FOREGROUND, 1);
        TmfEventRequest req1 = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200, ExecutionType.FOREGROUND, 0);
        TmfEventRequest req2 = new TmfEventRequestStub(ITmfEvent.class, range2, 100, 2000, ExecutionType.FOREGROUND, 1);
        TmfEventRequest req3 = new TmfEventRequestStub(ITmfEvent.class, range1, 101, 200, ExecutionType.FOREGROUND, 2);

        assertFalse("isCompatible", coalescedRequest.isCompatible(req1));
        assertTrue("isCompatible", coalescedRequest.isCompatible(req2));
        assertFalse("isCompatible", coalescedRequest.isCompatible(req3));

        coalescedRequest = new TmfCoalescedEventRequest(ITmfEvent.class, range1, 0, 100, ExecutionType.FOREGROUND, 0);
        assertTrue("isCompatible", coalescedRequest.isCompatible(req1));
        assertFalse("isCompatible", coalescedRequest.isCompatible(req2));
        assertFalse("isCompatible", coalescedRequest.isCompatible(req3));

    }

    // ------------------------------------------------------------------------
    // addEvent
    // ------------------------------------------------------------------------

    @Test
    public void testAddEvent1() {
        TmfCoalescedEventRequest coalescedRequest = new TmfCoalescedEventRequest(ITmfEvent.class, range1, 0, 2147483647, ExecutionType.FOREGROUND, 0);
        TmfEventRequest req1 = new TmfEventRequestStub(ITmfEvent.class, range1, 0, 2147483647, 200);
        TmfEventRequest req2 = new TmfEventRequestStub(ITmfEvent.class, range1, 1, 2147483647, 200);

        assertTrue("isCompatible", coalescedRequest.isCompatible(req1));
        assertTrue("isCompatible", coalescedRequest.isCompatible(req2));

        coalescedRequest.addRequest(req1);
        coalescedRequest.addRequest(req2);

        assertEquals("addRequest", 0, coalescedRequest.getIndex());
        assertEquals("addRequest", 2147483647, coalescedRequest.getNbRequested());
    }

    @Test
    public void testAddEvent2() {
        TmfCoalescedEventRequest coalescedRequest = new TmfCoalescedEventRequest(ITmfEvent.class, range1, 1, 2147483647, ExecutionType.FOREGROUND, 0);
        TmfEventRequest req1 = new TmfEventRequestStub(ITmfEvent.class, range1, 1, 2147483647, 200);
        TmfEventRequest req2 = new TmfEventRequestStub(ITmfEvent.class, range1, 0, 2147483647, 200);

        assertTrue("isCompatible", coalescedRequest.isCompatible(req1));
        assertTrue("isCompatible", coalescedRequest.isCompatible(req2));

        coalescedRequest.addRequest(req1);
        coalescedRequest.addRequest(req2);

        assertEquals("addRequest", 0, coalescedRequest.getIndex());
        assertEquals("addRequest", 2147483647, coalescedRequest.getNbRequested());
    }

    // ------------------------------------------------------------------------
    // done
    // ------------------------------------------------------------------------

    @Test
    public void testDone() {
        // Test request
        final boolean[] crFlags = new boolean[4];
        TmfCoalescedEventRequest request = setupTestRequest(crFlags);
        TmfEventRequest subRequest1 = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        TmfEventRequest subRequest2 = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        request.addRequest(subRequest1);
        request.addRequest(subRequest2);

        request.done();

        // Validate the coalescing request
        assertTrue("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertTrue("handleCompleted", crFlags[0]);
        assertTrue("handleSuccess", crFlags[1]);
        assertFalse("handleFailure", crFlags[2]);
        assertFalse("handleCancel", crFlags[3]);

        // Validate the first coalesced request
        assertTrue("isCompleted", subRequest1.isCompleted());
        assertFalse("isFailed", subRequest1.isFailed());
        assertFalse("isCancelled", subRequest1.isCancelled());

        // Validate the second coalesced request
        assertTrue("isCompleted", subRequest2.isCompleted());
        assertFalse("isFailed", subRequest2.isFailed());
        assertFalse("isCancelled", subRequest2.isCancelled());
    }

    // ------------------------------------------------------------------------
    // fail
    // ------------------------------------------------------------------------

    @Test
    public void testFail() {
        final boolean[] crFlags = new boolean[4];
        TmfCoalescedEventRequest request = setupTestRequest(crFlags);
        TmfEventRequest subRequest1 = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        TmfEventRequest subRequest2 = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        request.addRequest(subRequest1);
        request.addRequest(subRequest2);

        request.fail(new IllformedLocaleException("Hi"));

        // Validate the coalescing request
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isFailed", request.isFailed());
        final Throwable failCause = request.getFailureCause();
        assertNotNull("Cause of failure", failCause);
        assertEquals("Cause of failure message", "Hi", failCause.getMessage());
        assertFalse("isCancelled", request.isCancelled());

        assertTrue("handleCompleted", crFlags[0]);
        assertFalse("handleSuccess", crFlags[1]);
        assertTrue("handleFailure", crFlags[2]);
        assertFalse("handleCancel", crFlags[3]);

        // Validate the first coalesced request
        assertTrue("isCompleted", subRequest1.isCompleted());
        assertTrue("isFailed", subRequest1.isFailed());
        assertFalse("isCancelled", subRequest1.isCancelled());

        // Validate the second coalesced request
        assertTrue("isCompleted", subRequest2.isCompleted());
        assertTrue("isFailed", subRequest2.isFailed());
        assertFalse("isCancelled", subRequest2.isCancelled());
    }

    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    @Test
    public void testCancel() {
        final boolean[] crFlags = new boolean[4];
        TmfCoalescedEventRequest request = setupTestRequest(crFlags);
        TmfEventRequest subRequest1 = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        TmfEventRequest subRequest2 = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        request.addRequest(subRequest1);
        request.addRequest(subRequest2);

        request.cancel();

        // Validate the coalescing request
        assertTrue("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertTrue("isCancelled", request.isCancelled());

        assertTrue("handleCompleted", crFlags[0]);
        assertFalse("handleSuccess", crFlags[1]);
        assertFalse("handleFailure", crFlags[2]);
        assertTrue("handleCancel", crFlags[3]);

        // Validate the first coalesced request
        assertTrue("isCompleted", subRequest1.isCompleted());
        assertFalse("isFailed", subRequest1.isFailed());
        assertTrue("isCancelled", subRequest1.isCancelled());

        // Validate the second coalesced request
        assertTrue("isCompleted", subRequest2.isCompleted());
        assertFalse("isFailed", subRequest2.isFailed());
        assertTrue("isCancelled", subRequest2.isCancelled());
    }

    // ------------------------------------------------------------------------
    // Coalescing
    // ------------------------------------------------------------------------

    private static final TmfTestTrace TEST_TRACE = TmfTestTrace.A_TEST_10K;
    private static final int NB_EVENTS = 5000;

    // Initialize the test trace
    private TmfTraceStub fTrace = null;

    private synchronized TmfTraceStub setupTrace(String path) {
        if (fTrace == null) {
            try {
                fTrace = new TmfTraceStub(path, 500, false, null);
            } catch (TmfTraceException e) {
                e.printStackTrace();
            }
        }
        return fTrace;
    }

    Vector<ITmfEvent> requestedEvents1;
    Vector<ITmfEvent> requestedEvents2;
    Vector<ITmfEvent> requestedEvents3;

    TmfEventRequest request1;
    TmfEventRequest request2;
    TmfEventRequest request3;

    ITmfEventProvider[] providers;

    private static class TmfTestTriggerSignal extends TmfSignal {
        public final boolean forceCancel;
        public final long fIndex;

        public TmfTestTriggerSignal(Object source, long index, boolean cancel) {
            super(source);
            forceCancel = cancel;
            fIndex = index;
        }
    }

    private static class TmfTestTriggerSignal2 extends TmfSignal {
        public TmfTestTriggerSignal2(Object source) {
            super(source);
        }
    }

    @TmfSignalHandler
    public void trigger(final TmfTestTriggerSignal signal) {

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final long REQUEST_OFFSET = 1000;

        requestedEvents1 = new Vector<>();
        request1 = new TmfEventRequest(ITmfEvent.class, range, signal.fIndex,
                NB_EVENTS, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (!isCompleted()) {
                    requestedEvents1.add(event);
                    if (signal.forceCancel) {
                        cancel();
                    }
                }
            }
        };

        requestedEvents2 = new Vector<>();
        request2 = new TmfEventRequest(ITmfEvent.class, range,
                signal.fIndex + REQUEST_OFFSET, NB_EVENTS, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (!isCompleted()) {
                    requestedEvents2.add(event);
                }
            }
        };

        requestedEvents3 = new Vector<>();
        request3 = new TmfEventRequest(ITmfEvent.class, range,
                signal.fIndex + 2 * REQUEST_OFFSET, NB_EVENTS, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (!isCompleted()) {
                    requestedEvents3.add(event);
                }
            }
        };

        providers = TmfProviderManager.getProviders(ITmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request1);
        providers[0].sendRequest(request2);
        providers[0].sendRequest(request3);
    }

    /**
     * @param signal
     *            the trigger signal
     */
    @TmfSignalHandler
    public void trigger(final TmfTestTriggerSignal2 signal) {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.create(100, -3), TmfTimestamp.BIG_CRUNCH);
        requestedEvents1 = new Vector<>();
        request1 = new TmfEventRequest(ITmfEvent.class, range, 0, 1, ExecutionType.FOREGROUND) {
            @Override
            public void handleData(ITmfEvent event) {
                super.handleData(event);
                if (!isCompleted()) {
                    requestedEvents1.add(event);
                }
            }
        };
        providers = TmfProviderManager.getProviders(ITmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request1);
    }

    public void runCoalescedRequest(long startIndex) throws InterruptedException {

        fTrace = setupTrace(TEST_TRACE.getFullPath());

        TmfSignalManager.register(this);
        TmfTestTriggerSignal signal = new TmfTestTriggerSignal(this, startIndex, false);
        TmfSignalManager.dispatchSignal(signal);

        request1.waitForCompletion();
        request2.waitForCompletion();
        request3.waitForCompletion();

        try {
            assertEquals("Request1: nbEvents", NB_EVENTS, requestedEvents1.size());
            assertTrue("Request1: isCompleted", request1.isCompleted());
            assertFalse("Request1: isCancelled", request1.isCancelled());

            assertEquals("Request2: nbEvents", NB_EVENTS, requestedEvents2.size());
            assertTrue("Request2: isCompleted", request2.isCompleted());
            assertFalse("Request2: isCancelled", request2.isCancelled());

            assertEquals("Request3: nbEvents", NB_EVENTS, requestedEvents3.size());
            assertTrue("Request3: isCompleted", request3.isCompleted());
            assertFalse("Request3: isCancelled", request3.isCancelled());

            // Ensure that we have distinct events.
            // Don't go overboard: we are not validating the stub!
            for (int i = 0; i < NB_EVENTS; i++) {
                assertEquals("Distinct events", i + 1 + request1.getIndex(), requestedEvents1.get(i).getTimestamp().getValue());
                assertEquals("Distinct events", i + 1 + request2.getIndex(), requestedEvents2.get(i).getTimestamp().getValue());
                assertEquals("Distinct events", i + 1 + request3.getIndex(), requestedEvents3.get(i).getTimestamp().getValue());
            }
        } finally {
            TmfSignalManager.deregister(this);
            fTrace.dispose();
            fTrace = null;
        }
    }

    @Test
    public void testCoalescedRequest() throws InterruptedException {
        runCoalescedRequest(0);
        runCoalescedRequest(1);
        runCoalescedRequest(5);
    }

    @Test
    public void testCancelCoalescedRequest() throws InterruptedException {

        fTrace = setupTrace(TEST_TRACE.getFullPath());

        TmfSignalManager.register(this);
        TmfTestTriggerSignal signal = new TmfTestTriggerSignal(this, 0, true);
        TmfSignalManager.dispatchSignal(signal);

        request1.waitForCompletion();
        request2.waitForCompletion();
        request3.waitForCompletion();

        assertTrue("Request1: isCompleted", request1.isCompleted());
        assertTrue("Request1: isCancelled", request1.isCancelled());

        assertEquals("Request2: nbEvents", NB_EVENTS, requestedEvents2.size());
        assertTrue("Request2: isCompleted", request2.isCompleted());
        assertFalse("Request2: isCancelled", request2.isCancelled());

        assertEquals("Request3: nbEvents", NB_EVENTS, requestedEvents3.size());
        assertTrue("Request3: isCompleted", request3.isCompleted());
        assertFalse("Request3: isCancelled", request3.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i + 1 + request2.getIndex(), requestedEvents2.get(i).getTimestamp().getValue());
            assertEquals("Distinct events", i + 1 + request3.getIndex(), requestedEvents3.get(i).getTimestamp().getValue());
        }

        TmfSignalManager.deregister(this);
        fTrace.dispose();
        fTrace = null;
    }

    @Test
    public void testSingleTimeRequest() throws InterruptedException {

        fTrace = setupTrace(TEST_TRACE.getFullPath());

        TmfSignalManager.register(this);
        TmfTestTriggerSignal2 signal = new TmfTestTriggerSignal2(this);
        TmfSignalManager.dispatchSignal(signal);

        request1.waitForCompletion();

        assertTrue("Request1: isCompleted", request1.isCompleted());

        // We have to have one event processed
        assertEquals("Request1: nbEvents", 1, requestedEvents1.size());

        TmfSignalManager.deregister(this);
        fTrace.dispose();
        fTrace = null;
    }

}
