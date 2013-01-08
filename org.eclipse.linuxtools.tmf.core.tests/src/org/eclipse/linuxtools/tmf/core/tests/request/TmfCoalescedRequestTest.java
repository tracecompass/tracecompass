/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.request;

import junit.framework.TestCase;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfCoalescedRequest;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.ITmfRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfRequest.TmfRequestPriority;
import org.eclipse.linuxtools.tmf.core.request.ITmfRequest.TmfRequestState;
import org.eclipse.linuxtools.tmf.tests.stubs.request.TmfRequestStub;
import org.junit.Test;

/**
 * <b><u>TmfCoalescedRequestTest</u></b>
 * <p>
 * Test suite for the TmfCoalescedRequest class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfCoalescedRequestTest extends TestCase {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final TmfRequestPriority NORMAL = TmfRequestPriority.NORMAL;
    private static final TmfRequestPriority HIGH = TmfRequestPriority.HIGH;

    private static final long ALL_EVENTS = ITmfRequest.ALL_EVENTS;

    private static final TmfTimeRange ETERNITY = TmfTimeRange.ETERNITY;
    private static final TmfTimeRange EPOCH = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_CRUNCH);

    private static final TmfRequestState PENDING = TmfRequestState.PENDING;
    private static final TmfRequestState RUNNING = TmfRequestState.RUNNING;
    private static final TmfRequestState COMPLETED = TmfRequestState.COMPLETED;

    // ------------------------------------------------------------------------
    // Sample requests
    // ------------------------------------------------------------------------

    private static ITmfRequest fRequest1  = new TmfRequestStub(EPOCH,  10, 100, NORMAL);
    private static ITmfRequest fRequest2  = new TmfRequestStub(EPOCH,  20, 200, NORMAL);
    private static final ITmfRequest fRequest3  = new TmfRequestStub(EPOCH, 200, 100, NORMAL);

    private static final ITmfRequest fRequest4  = new TmfRequestStub(ETERNITY,  10, 100, NORMAL);
    private static final ITmfRequest fRequest5  = new TmfRequestStub(ETERNITY,  20, 200, NORMAL);
    private static final ITmfRequest fRequest6  = new TmfRequestStub(ETERNITY, 200, 100, NORMAL);

    private static final ITmfRequest fRequest7  = new TmfRequestStub(EPOCH,  10, 100, HIGH);
    private static final ITmfRequest fRequest8  = new TmfRequestStub(EPOCH,  20, 200, HIGH);
    private static final ITmfRequest fRequest9  = new TmfRequestStub(EPOCH, 200, 100, HIGH);

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private TmfCoalescedRequest fCoalescedRequest1;
    private TmfCoalescedRequest fCoalescedRequest2;
    private int fLastRequestId;

    // ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
	public TmfCoalescedRequestTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
        fRequest1  = new TmfRequestStub(EPOCH,  10, 100, NORMAL);
        fRequest2  = new TmfRequestStub(EPOCH,  20, 200, NORMAL);
	    fCoalescedRequest1 = new TmfCoalescedRequest(fRequest1);
	    fCoalescedRequest2 = new TmfCoalescedRequest(fRequest2);
	    fLastRequestId = fCoalescedRequest2.getRequestId();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    private ITmfRequest setupDummyRequest(final boolean[] flags) {

        TmfCoalescedRequest request = new TmfCoalescedRequest(fRequest1) {
            @Override
            public synchronized void handleCompleted() {
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
        request.addRequest(fRequest2);
        fLastRequestId = request.getRequestId();
        return request;
    }

    // ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	@Test
	public void testTmfRequestByPriority() {
	    TmfCoalescedRequest request = new TmfCoalescedRequest(NORMAL);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", NORMAL, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        request = new TmfCoalescedRequest(HIGH);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", HIGH, request.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, request.getTimeRange());
        assertEquals("getNbRequested", ALL_EVENTS, request.getNbRequested());
        assertEquals("getStartIndex", 0, request.getStartIndex());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
	}

    @Test
    public void testTmfRequestByRequest() {
        TmfCoalescedRequest request = new TmfCoalescedRequest(fRequest1);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", fRequest1.getRequestPriority(), request.getRequestPriority());
        assertEquals("getTimeRange", fRequest1.getTimeRange(), request.getTimeRange());
        assertEquals("getNbRequested", fRequest1.getNbRequested(), request.getNbRequested());
        assertEquals("getStartIndex", fRequest1.getStartIndex(), request.getStartIndex());

        assertEquals("getParent", request, fRequest1.getParent());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        request = new TmfCoalescedRequest(fRequest5);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", fRequest5.getRequestPriority(), request.getRequestPriority());
        assertEquals("getTimeRange", fRequest5.getTimeRange(), request.getTimeRange());
        assertEquals("getNbRequested", fRequest5.getNbRequested(), request.getNbRequested());
        assertEquals("getStartIndex", fRequest5.getStartIndex(), request.getStartIndex());

        assertEquals("getParent", request, fRequest5.getParent());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        request = new TmfCoalescedRequest(fRequest9);
        fLastRequestId++;

        assertEquals("getRequestId", fLastRequestId, request.getRequestId());
        assertEquals("getRequestPriority", fRequest9.getRequestPriority(), request.getRequestPriority());
        assertEquals("getTimeRange", fRequest9.getTimeRange(), request.getTimeRange());
        assertEquals("getNbRequested", fRequest9.getNbRequested(), request.getNbRequested());
        assertEquals("getStartIndex", fRequest9.getStartIndex(), request.getStartIndex());

        assertEquals("getParent", request, fRequest9.getParent());

        assertEquals("getNbEventsRead", 0, request.getNbEventsRead());
        assertEquals("getState", PENDING, request.getState());
        assertFalse("isRunning", request.isRunning());
        assertFalse("isCompleted", request.isCompleted());

        assertNull("getStatus", request.getStatus());
        assertFalse("isOK", request.isOK());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertEquals("equals", fCoalescedRequest1, fCoalescedRequest1);
        assertEquals("equals", fCoalescedRequest2, fCoalescedRequest2);

        assertFalse("equals", fCoalescedRequest1.equals(fCoalescedRequest2));
        assertFalse("equals", fCoalescedRequest2.equals(fCoalescedRequest1));
    }

    @Test
    public void testEqualsSymmetry() {
        TmfCoalescedRequest request1b = new TmfCoalescedRequest(fRequest1);
        fLastRequestId++;

        assertEquals("equals", fCoalescedRequest1, request1b);
        assertEquals("equals", request1b, fCoalescedRequest1);
    }

    @Test
    public void testEqualsTransivity() {
        TmfCoalescedRequest request1b = new TmfCoalescedRequest(fRequest1);
        fLastRequestId++;
        TmfCoalescedRequest request1c = new TmfCoalescedRequest(fRequest1);
        fLastRequestId++;

        assertEquals("equals", fCoalescedRequest1, request1b);
        assertEquals("equals", request1b, request1c);
        assertEquals("equals", request1c, fCoalescedRequest1);
    }

    @Test
    public void testEqualsNull() {
        assertFalse("equals", fCoalescedRequest1.equals(null));
        assertFalse("equals", fCoalescedRequest2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        assertTrue("hashCode", fCoalescedRequest1.hashCode() == fCoalescedRequest1.hashCode());
        assertTrue("hashCode", fCoalescedRequest2.hashCode() == fCoalescedRequest2.hashCode());
        assertTrue("hashCode", fCoalescedRequest1.hashCode() != fCoalescedRequest2.hashCode());
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        String expected1 = "TmfCoalescedRequest [fSubRequests=[" + fRequest1 + "]]";
        String expected2 = "TmfCoalescedRequest [fSubRequests=[" + fRequest2 + "]]";

        assertEquals("toString", expected1, fCoalescedRequest1.toString());
        assertEquals("toString", expected2, fCoalescedRequest2.toString());
    }


    // ------------------------------------------------------------------------
    // isCompatible
    // ------------------------------------------------------------------------

    @Test
    public void testIsCompatible() {
        assertTrue ("isCompatible", fCoalescedRequest1.isCompatible(fRequest1));
        assertTrue ("isCompatible", fCoalescedRequest1.isCompatible(fRequest2));
        assertFalse("isCompatible", fCoalescedRequest1.isCompatible(fRequest3));
        assertTrue ("isCompatible", fCoalescedRequest1.isCompatible(fRequest4));
        assertTrue ("isCompatible", fCoalescedRequest1.isCompatible(fRequest5));
        assertFalse("isCompatible", fCoalescedRequest1.isCompatible(fRequest6));

        assertFalse("isCompatible", fCoalescedRequest1.isCompatible(fRequest7));
        assertFalse("isCompatible", fCoalescedRequest1.isCompatible(fRequest8));
        assertFalse("isCompatible", fCoalescedRequest1.isCompatible(fRequest9));
    }

    // ------------------------------------------------------------------------
    // addRequest
    // ------------------------------------------------------------------------

    @Test
    public void testAddRequest() {
        fCoalescedRequest1.addRequest(fRequest2);
        assertEquals("getRequestPriority", NORMAL, fCoalescedRequest1.getRequestPriority());
        assertEquals("getTimeRange", EPOCH, fCoalescedRequest1.getTimeRange());
        assertEquals("getNbRequested", 210, fCoalescedRequest1.getNbRequested());
        assertEquals("getStartIndex",   10, fCoalescedRequest1.getStartIndex());

        fCoalescedRequest1.addRequest(fRequest3);
        assertEquals("getRequestPriority", NORMAL, fCoalescedRequest1.getRequestPriority());
        assertEquals("getTimeRange", EPOCH, fCoalescedRequest1.getTimeRange());
        assertEquals("getNbRequested", 290, fCoalescedRequest1.getNbRequested());
        assertEquals("getStartIndex",   10, fCoalescedRequest1.getStartIndex());

        fCoalescedRequest1.addRequest(fRequest4);
        assertEquals("getRequestPriority", NORMAL, fCoalescedRequest1.getRequestPriority());
        assertEquals("getTimeRange", ETERNITY, fCoalescedRequest1.getTimeRange());
        assertEquals("getNbRequested", 290, fCoalescedRequest1.getNbRequested());
        assertEquals("getStartIndex",   10, fCoalescedRequest1.getStartIndex());
    }

    // ------------------------------------------------------------------------
    // getSubRequestIds
    // ------------------------------------------------------------------------

    @Test
    public void testGetSubRequestIds() {
        String expected1 = "[" + fRequest1.getRequestId() + "]";
        String expected2 = "[" + fRequest1.getRequestId() + "," + fRequest2.getRequestId() + "]";
        String expected3 = "[" + fRequest1.getRequestId() + "," + fRequest2.getRequestId() + "," + fRequest3.getRequestId() + "]";

        assertEquals("getRequestIds", expected1, fCoalescedRequest1.getSubRequestIds());

        fCoalescedRequest1.addRequest(fRequest2);
        assertEquals("getRequestIds", expected2, fCoalescedRequest1.getSubRequestIds());

        fCoalescedRequest1.addRequest(fRequest3);
        assertEquals("getRequestIds", expected3, fCoalescedRequest1.getSubRequestIds());
    }

    // ------------------------------------------------------------------------
    // notifyParent
    // ------------------------------------------------------------------------

    @Test
    public void testNotifyParent() {
        final Boolean[] notifications = new Boolean[2];
        notifications[0] = notifications[1] = false;

        final TmfRequestStub request1 = new TmfRequestStub();
        final TmfRequestStub request2 = new TmfRequestStub();
        TmfCoalescedRequest request = new TmfCoalescedRequest(request1) {
            @Override
            public void notifyParent(ITmfRequest child) {
                notifications[child == request1 ? 0 : 1] = true;
                super.notifyParent(this);
            }
        };
        request.addRequest(request2);
        assertFalse("notifyParent", notifications[0]);
        assertFalse("notifyParent", notifications[1]);

        request1.notifyParent(null);
        assertTrue("notifyParent", notifications[0]);
        assertFalse("notifyParent", notifications[1]);

        request2.notifyParent(null);
        assertTrue("notifyParent", notifications[0]);
        assertTrue("notifyParent", notifications[1]);
    }

    // ------------------------------------------------------------------------
    // start
    // ------------------------------------------------------------------------

    @Test
    public void testStart() {
        final boolean[] flags = new boolean[4];
        ITmfRequest request = setupDummyRequest(flags);
        request.start();

        assertTrue("isRunning",    request.isRunning());
        assertFalse("isCompleted", request.isCompleted());
        assertEquals("getState",   RUNNING, request.getState());
        assertNull("getStatus",    request.getStatus());

        assertTrue("isRunning",    fRequest1.isRunning());
        assertFalse("isCompleted", fRequest1.isCompleted());
        assertEquals("getState",   RUNNING, fRequest1.getState());
        assertNull("getStatus",    fRequest1.getStatus());

        assertTrue("isRunning",    fRequest2.isRunning());
        assertFalse("isCompleted", fRequest2.isCompleted());
        assertEquals("getState",   RUNNING, fRequest2.getState());
        assertNull("getStatus",    fRequest2.getStatus());

        assertFalse("handleCompleted", flags[0]);
        assertFalse("handleSuccess",   flags[1]);
        assertFalse("handleFailure",   flags[2]);
        assertFalse("handleCancel",    flags[3]);
    }

    // ------------------------------------------------------------------------
    // done
    // ------------------------------------------------------------------------

    @Test
    public void testDone() {
        final boolean[] flags = new boolean[4];
        ITmfRequest request = setupDummyRequest(flags);
        request.done();

        assertFalse("isRunning",   request.isRunning());
        assertTrue("isCompleted",  request.isCompleted());
        assertEquals("getState",   COMPLETED, request.getState());
        assertEquals("getStatus",  IStatus.OK, request.getStatus().getSeverity());
        assertTrue("isOK",         request.isOK());
        assertFalse("isFailed",    request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertFalse("isRunning",   fRequest1.isRunning());
        assertTrue("isCompleted",  fRequest1.isCompleted());
        assertEquals("getState",   COMPLETED, fRequest1.getState());
        assertEquals("getStatus",  IStatus.OK, fRequest1.getStatus().getSeverity());
        assertTrue("isOK",         fRequest1.isOK());
        assertFalse("isFailed",    fRequest1.isFailed());
        assertFalse("isCancelled", fRequest1.isCancelled());

        assertFalse("isRunning",   fRequest2.isRunning());
        assertTrue("isCompleted",  fRequest2.isCompleted());
        assertEquals("getState",   COMPLETED, fRequest2.getState());
        assertEquals("getStatus",  IStatus.OK, fRequest2.getStatus().getSeverity());
        assertTrue("isOK",         fRequest2.isOK());
        assertFalse("isFailed",    fRequest2.isFailed());
        assertFalse("isCancelled", fRequest2.isCancelled());

        assertTrue("handleCompleted", flags[0]);
        assertTrue("handleSuccess",   flags[1]);
        assertFalse("handleFailure",  flags[2]);
        assertFalse("handleCancel",   flags[3]);
    }

    // ------------------------------------------------------------------------
    // fail
    // ------------------------------------------------------------------------

    @Test
    public void testFail() {
        final boolean[] flags = new boolean[4];
        ITmfRequest request = setupDummyRequest(flags);
        request.fail();

        assertFalse("isRunning",   request.isRunning());
        assertTrue("isCompleted",  request.isCompleted());
        assertEquals("getState",   COMPLETED, request.getState());
        assertEquals("getStatus",  IStatus.ERROR, request.getStatus().getSeverity());
        assertFalse("isOK",        request.isOK());
        assertTrue("isFailed",     request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertFalse("isRunning",   fRequest1.isRunning());
        assertTrue("isCompleted",  fRequest1.isCompleted());
        assertEquals("getState",   COMPLETED, fRequest1.getState());
        assertEquals("getStatus",  IStatus.ERROR, fRequest1.getStatus().getSeverity());
        assertFalse("isOK",        fRequest1.isOK());
        assertTrue("isFailed",     fRequest1.isFailed());
        assertFalse("isCancelled", fRequest1.isCancelled());

        assertFalse("isRunning",   fRequest2.isRunning());
        assertTrue("isCompleted",  fRequest2.isCompleted());
        assertEquals("getState",   COMPLETED, fRequest2.getState());
        assertEquals("getStatus",  IStatus.ERROR, fRequest2.getStatus().getSeverity());
        assertFalse("isOK",        fRequest2.isOK());
        assertTrue("isFailed",     fRequest2.isFailed());
        assertFalse("isCancelled", fRequest2.isCancelled());

        assertTrue("handleCompleted", flags[0]);
        assertFalse("handleSuccess",  flags[1]);
        assertTrue("handleFailure",   flags[2]);
        assertFalse("handleCancel",   flags[3]);
    }

	// ------------------------------------------------------------------------
	// cancel
	// ------------------------------------------------------------------------

    @Test
    public void testCancel() {
        final boolean[] flags = new boolean[4];
        ITmfRequest request = setupDummyRequest(flags);
        request.cancel();

        assertFalse("isRunning",  request.isRunning());
        assertTrue("isCompleted", request.isCompleted());
        assertEquals("getState",  COMPLETED, request.getState());
        assertEquals("getStatus", IStatus.CANCEL, request.getStatus().getSeverity());
        assertFalse("isOK",       request.isOK());
        assertFalse("isFailed",   request.isFailed());
        assertTrue("isCancelled", request.isCancelled());

        assertFalse("isRunning",  fRequest1.isRunning());
        assertTrue("isCompleted", fRequest1.isCompleted());
        assertEquals("getState",  COMPLETED, fRequest1.getState());
        assertEquals("getStatus", IStatus.CANCEL, fRequest1.getStatus().getSeverity());
        assertFalse("isOK",       fRequest1.isOK());
        assertFalse("isFailed",   fRequest1.isFailed());
        assertTrue("isCancelled", fRequest1.isCancelled());

        assertFalse("isRunning",  fRequest2.isRunning());
        assertTrue("isCompleted", fRequest2.isCompleted());
        assertEquals("getState",  COMPLETED, fRequest2.getState());
        assertEquals("getStatus", IStatus.CANCEL, fRequest2.getStatus().getSeverity());
        assertFalse("isOK",       fRequest2.isOK());
        assertFalse("isFailed",   fRequest2.isFailed());
        assertTrue("isCancelled", fRequest2.isCancelled());

        assertTrue("handleCompleted", flags[0]);
        assertFalse("handleSuccess",  flags[1]);
        assertFalse("handleFailure",  flags[2]);
        assertTrue("handleCancel",    flags[3]);
    }

}
