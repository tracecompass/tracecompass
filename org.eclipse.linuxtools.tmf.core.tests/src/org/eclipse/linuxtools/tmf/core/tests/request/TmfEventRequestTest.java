/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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

package org.eclipse.linuxtools.tmf.core.tests.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.tests.stubs.request.TmfEventRequestStub;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfEventRequest class.
 */
@SuppressWarnings("javadoc")
public class TmfEventRequestTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static TmfTimeRange range1 = new TmfTimeRange(TmfTimeRange.ETERNITY);
    private static TmfTimeRange range2 = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BIG_CRUNCH);

    private static TmfEventRequest fRequest1;
    private static TmfEventRequest fRequest1b;
    private static TmfEventRequest fRequest1c;
    private static TmfEventRequest fRequest2;
    private static TmfEventRequest fRequest3;
    private static TmfEventRequest fRequest4;

    private static int fRequestCount;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        TmfDataRequest.reset();
        fRequest1 = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        fRequest2 = new TmfEventRequestStub(ITmfEvent.class, range2, 100, 200);
        fRequest3 = new TmfEventRequestStub(ITmfEvent.class, range2, 200, 200);
        fRequest4 = new TmfEventRequestStub(ITmfEvent.class, range2, 200, 300);
        fRequest1b = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        fRequest1c = new TmfEventRequestStub(ITmfEvent.class, range1, 100, 200);
        fRequestCount = fRequest1c.getRequestId() + 1;
    }

    private static TmfEventRequest setupTestRequest(final boolean[] flags) {

        TmfEventRequest request = new TmfEventRequestStub(ITmfEvent.class, new TmfTimeRange(TmfTimeRange.ETERNITY), 100, 200) {
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
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testTmfEventRequest() {
        TmfEventRequest request = new TmfEventRequestStub(ITmfEvent.class);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType", ITmfEvent.class, request.getDataType());

        assertEquals("StartTime", TmfTimestamp.BIG_BANG, request.getRange().getStartTime());
        assertEquals("EndTime", TmfTimestamp.BIG_CRUNCH, request.getRange().getEndTime());

        assertEquals("getIndex", 0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
    }

    @Test
    public void testTmfEventRequestTimeRange() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BIG_CRUNCH);
        TmfEventRequest request = new TmfEventRequestStub(ITmfEvent.class, range);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  ITmfEvent.class, request.getDataType());

        assertEquals("StartTime", new TmfTimestamp(), request.getRange().getStartTime());
        assertEquals("EndTime", TmfTimestamp.BIG_CRUNCH, request.getRange().getEndTime());

        assertEquals("getIndex", 0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
    }

    @Test
    public void testTmfEventRequestTimeRangeNbRequested() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BIG_CRUNCH);
        TmfEventRequest request = new TmfEventRequestStub(ITmfEvent.class, range, 100);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  ITmfEvent.class, request.getDataType());

        assertEquals("StartTime", new TmfTimestamp(), request.getRange().getStartTime());
        assertEquals("EndTime", TmfTimestamp.BIG_CRUNCH, request.getRange().getEndTime());

        assertEquals("getIndex", 0, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
    }

    @Test
    public void testTmfEventRequestTimeRangeNbRequestedBlocksize() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BIG_CRUNCH);
        TmfEventRequest request = new TmfEventRequestStub(ITmfEvent.class, range, 100, 200);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  ITmfEvent.class, request.getDataType());

        assertEquals("StartTime", new TmfTimestamp(), request.getRange().getStartTime());
        assertEquals("EndTime", TmfTimestamp.BIG_CRUNCH, request.getRange().getEndTime());

        assertEquals("getIndex", 0, request.getIndex());
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
    public void testEqualsReflexivity() {
        assertTrue("equals", fRequest1.equals(fRequest1));
        assertTrue("equals", fRequest2.equals(fRequest2));

        assertFalse("equals", fRequest1.equals(fRequest2));
        assertFalse("equals", fRequest2.equals(fRequest1));
    }

    @Test
    public void testEqualsSymmetry() {
        assertTrue("equals", fRequest1.equals(fRequest1b));
        assertTrue("equals", fRequest1b.equals(fRequest1));

        assertFalse("equals", fRequest1.equals(fRequest3));
        assertFalse("equals", fRequest2.equals(fRequest3));
        assertFalse("equals", fRequest3.equals(fRequest1));
        assertFalse("equals", fRequest3.equals(fRequest2));
    }

    @Test
    public void testEqualsTransivity() {
        assertTrue("equals", fRequest1.equals(fRequest1b));
        assertTrue("equals", fRequest1b.equals(fRequest1c));
        assertTrue("equals", fRequest1.equals(fRequest1c));
    }

    @Test
    public void testEqualsNull() {
        assertFalse("equals", fRequest1.equals(null));
        assertFalse("equals", fRequest2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        assertTrue("hashCode", fRequest1.hashCode() == fRequest1.hashCode());
        assertTrue("hashCode", fRequest2.hashCode() == fRequest2.hashCode());
        assertTrue("hashCode", fRequest1.hashCode() != fRequest2.hashCode());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        String expected1 = "[TmfEventRequest(0,ITmfEvent," + range1 + ",0,100,200)]";
        String expected2 = "[TmfEventRequest(1,ITmfEvent," + range2 + ",0,100,200)]";
        String expected3 = "[TmfEventRequest(2,ITmfEvent," + range2 + ",0,200,200)]";
        String expected4 = "[TmfEventRequest(3,ITmfEvent," + range2 + ",0,200,300)]";

        assertEquals("toString", expected1, fRequest1.toString());
        assertEquals("toString", expected2, fRequest2.toString());
        assertEquals("toString", expected3, fRequest3.toString());
        assertEquals("toString", expected4, fRequest4.toString());
    }

    // ------------------------------------------------------------------------
    // done
    // ------------------------------------------------------------------------

    @Test
    public void testDone() {
        final boolean[] flags = new boolean[4];
        TmfEventRequest request = setupTestRequest(flags);
        request.done();

        assertTrue("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertTrue("handleCompleted", flags[0]);
        assertTrue("handleSuccess", flags[1]);
        assertFalse("handleFailure", flags[2]);
        assertFalse("handleCancel", flags[3]);
    }

    // ------------------------------------------------------------------------
    // fail
    // ------------------------------------------------------------------------

    @Test
    public void testFail() {
        final boolean[] flags = new boolean[4];
        TmfEventRequest request = setupTestRequest(flags);
        request.fail();

        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertTrue("handleCompleted", flags[0]);
        assertFalse("handleSuccess", flags[1]);
        assertTrue("handleFailure", flags[2]);
        assertFalse("handleCancel", flags[3]);
    }

    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    @Test
    public void testCancel() {
        final boolean[] flags = new boolean[4];
        TmfEventRequest request = setupTestRequest(flags);
        request.cancel();

        assertTrue("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertTrue("isCancelled", request.isCancelled());

        assertTrue("handleCompleted", flags[0]);
        assertFalse("handleSuccess", flags[1]);
        assertFalse("handleFailure", flags[2]);
        assertTrue("handleCancel", flags[3]);
    }

}
