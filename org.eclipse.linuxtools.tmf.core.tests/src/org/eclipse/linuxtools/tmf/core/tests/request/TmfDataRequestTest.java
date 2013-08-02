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
import org.eclipse.linuxtools.tmf.tests.stubs.request.TmfDataRequestStub;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the TmfDataRequest class.
 */
@SuppressWarnings("javadoc")
public class TmfDataRequestTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private static TmfDataRequest fRequest1;
    private static TmfDataRequest fRequest1b;
    private static TmfDataRequest fRequest1c;
    private static TmfDataRequest fRequest2;
    private static TmfDataRequest fRequest3;
    private static TmfDataRequest fRequest4;

    private static int fRequestCount;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    @Before
    public void setUp() {
        TmfDataRequest.reset();
        fRequest1 = new TmfDataRequestStub(ITmfEvent.class, 10, 100);
        fRequest2 = new TmfDataRequestStub(ITmfEvent.class, 20, 100);
        fRequest3 = new TmfDataRequestStub(ITmfEvent.class, 20, 200);
        fRequest4 = new TmfDataRequestStub(ITmfEvent.class, 20, 200);
        fRequest1b = new TmfDataRequestStub(ITmfEvent.class, 10, 100);
        fRequest1c = new TmfDataRequestStub(ITmfEvent.class, 10, 100);
        fRequestCount = fRequest1c.getRequestId() + 1;
    }

    private static TmfDataRequest setupTestRequest(final boolean[] flags) {

        TmfDataRequest request = new TmfDataRequestStub(ITmfEvent.class, 10, 100) {
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
    public void testTmfDataRequest() {
        TmfDataRequest request = new TmfDataRequestStub(ITmfEvent.class);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType", ITmfEvent.class, request.getDataType());

        assertEquals("getIndex", 0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
    }

    @Test
    public void testTmfDataRequestIndex() {
        TmfDataRequest request = new TmfDataRequestStub(ITmfEvent.class, 10);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType", ITmfEvent.class, request.getDataType());

        assertEquals("getIndex", 10, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
    }

    @Test
    public void testTmfDataRequestIndexNbRequested() {
        TmfDataRequest request = new TmfDataRequestStub(ITmfEvent.class, 10, 100);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType", ITmfEvent.class, request.getDataType());

        assertEquals("getIndex", 10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
    }

    @Test
    public void testTmfDataRequestIndexNbEventsBlocksize() {
        TmfDataRequest request = new TmfDataRequestStub(ITmfEvent.class, 10, 100);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType", ITmfEvent.class, request.getDataType());

        assertEquals("getIndex", 10, request.getIndex());
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
        String expected1 = "[TmfDataRequestStub(0,ITmfEvent,FOREGROUND,10,100)]";
        String expected2 = "[TmfDataRequestStub(1,ITmfEvent,FOREGROUND,20,100)]";
        String expected3 = "[TmfDataRequestStub(2,ITmfEvent,FOREGROUND,20,200)]";
        String expected4 = "[TmfDataRequestStub(3,ITmfEvent,FOREGROUND,20,200)]";

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
        TmfDataRequest request = setupTestRequest(flags);
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
        TmfDataRequest request = setupTestRequest(flags);
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
        TmfDataRequest request = setupTestRequest(flags);
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
