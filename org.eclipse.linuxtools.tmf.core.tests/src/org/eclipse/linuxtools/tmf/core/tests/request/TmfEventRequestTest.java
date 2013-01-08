/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.tests.stubs.request.TmfEventRequestStub;

/**
 * <b><u>TmfEventRequestTest</u></b>
 * <p>
 * Test suite for the TmfEventRequest class.
 */
@SuppressWarnings({"nls","javadoc", "deprecation"})
public class TmfEventRequestTest extends TestCase {

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

    /**
     * @param name the test name
     */
    public TmfEventRequestTest(final String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		fRequest1  = new TmfEventRequestStub(TmfEvent.class, range1, 100, 200);
		fRequest2  = new TmfEventRequestStub(TmfEvent.class, range2, 100, 200);
		fRequest3  = new TmfEventRequestStub(TmfEvent.class, range2, 200, 200);
		fRequest4  = new TmfEventRequestStub(TmfEvent.class, range2, 200, 300);
		fRequest1b = new TmfEventRequestStub(TmfEvent.class, range1, 100, 200);
		fRequest1c = new TmfEventRequestStub(TmfEvent.class, range1, 100, 200);
		fRequestCount = fRequest1c.getRequestId() + 1;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private static TmfEventRequest setupTestRequest(final boolean[] flags) {

		TmfEventRequest request = new TmfEventRequestStub(TmfEvent.class, new TmfTimeRange(TmfTimeRange.ETERNITY), 100, 200) {
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
		return request;
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public void testTmfEventRequest() {
        TmfEventRequest request = new TmfEventRequestStub(TmfEvent.class);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("StartTime", TmfTimestamp.BIG_BANG,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BIG_CRUNCH, request.getRange().getEndTime());

        assertEquals("getIndex", 0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfEventRequestTimeRange() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BIG_CRUNCH);
        TmfEventRequest request = new TmfEventRequestStub(TmfEvent.class, range);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("StartTime", new TmfTimestamp(), request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BIG_CRUNCH, request.getRange().getEndTime());

        assertEquals("getIndex", 0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfEventRequestTimeRangeNbRequested() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BIG_CRUNCH);
        TmfEventRequest request = new TmfEventRequestStub(TmfEvent.class, range, 100);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("StartTime", new TmfTimestamp(), request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BIG_CRUNCH, request.getRange().getEndTime());

        assertEquals("getIndex", 0, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfEventRequestTimeRangeNbRequestedBlocksize() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BIG_CRUNCH);
        TmfEventRequest request = new TmfEventRequestStub(TmfEvent.class, range, 100, 200);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("StartTime", new TmfTimestamp(), request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BIG_CRUNCH, request.getRange().getEndTime());

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

	public void testEqualsReflexivity() {
        assertTrue("equals", fRequest1.equals(fRequest1));
        assertTrue("equals", fRequest2.equals(fRequest2));

        assertFalse("equals", fRequest1.equals(fRequest2));
        assertFalse("equals", fRequest2.equals(fRequest1));
	}

	public void testEqualsSymmetry() {
        assertTrue("equals", fRequest1.equals(fRequest1b));
        assertTrue("equals", fRequest1b.equals(fRequest1));

        assertFalse("equals", fRequest1.equals(fRequest3));
        assertFalse("equals", fRequest2.equals(fRequest3));
        assertFalse("equals", fRequest3.equals(fRequest1));
        assertFalse("equals", fRequest3.equals(fRequest2));
	}

	public void testEqualsTransivity() {
        assertTrue("equals", fRequest1.equals(fRequest1b));
        assertTrue("equals", fRequest1b.equals(fRequest1c));
        assertTrue("equals", fRequest1.equals(fRequest1c));
	}

	public void testEqualsNull() {
        assertFalse("equals", fRequest1.equals(null));
        assertFalse("equals", fRequest2.equals(null));
	}

	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() {
        assertTrue("hashCode", fRequest1.hashCode() == fRequest1.hashCode());
        assertTrue("hashCode", fRequest2.hashCode() == fRequest2.hashCode());
		assertTrue("hashCode", fRequest1.hashCode() != fRequest2.hashCode());
	}

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	public void testToString() {
        String expected1 = "[TmfEventRequest(" + fRequest1.getRequestId() + ",TmfEvent," + range1 + ",0,100,200)]";
        String expected2 = "[TmfEventRequest(" + fRequest2.getRequestId() + ",TmfEvent," + range2 + ",0,100,200)]";
        String expected3 = "[TmfEventRequest(" + fRequest3.getRequestId() + ",TmfEvent," + range2 + ",0,200,200)]";
        String expected4 = "[TmfEventRequest(" + fRequest4.getRequestId() + ",TmfEvent," + range2 + ",0,200,300)]";

        assertEquals("toString", expected1, fRequest1.toString());
        assertEquals("toString", expected2, fRequest2.toString());
        assertEquals("toString", expected3, fRequest3.toString());
        assertEquals("toString", expected4, fRequest4.toString());
	}

	// ------------------------------------------------------------------------
	// done
	// ------------------------------------------------------------------------

	public void testDone() {

		final boolean[] flags = new boolean[4];
		TmfEventRequest request = setupTestRequest(flags);
		request.done();

		assertTrue ("isCompleted", request.isCompleted());
		assertFalse("isFailed",    request.isFailed());
		assertFalse("isCancelled", request.isCancelled());

		assertTrue ("handleCompleted", flags[0]);
		assertTrue ("handleSuccess",   flags[1]);
		assertFalse("handleFailure",   flags[2]);
		assertFalse("handleCancel",    flags[3]);
	}

	// ------------------------------------------------------------------------
	// fail
	// ------------------------------------------------------------------------

	public void testFail() {

		final boolean[] flags = new boolean[4];
		TmfEventRequest request = setupTestRequest(flags);
		request.fail();

		assertTrue ("isCompleted", request.isCompleted());
		assertTrue ("isFailed",    request.isFailed());
		assertFalse("isCancelled", request.isCancelled());

		assertTrue ("handleCompleted", flags[0]);
		assertFalse("handleSuccess",   flags[1]);
		assertTrue ("handleFailure",   flags[2]);
		assertFalse("handleCancel",    flags[3]);
	}

	// ------------------------------------------------------------------------
	// cancel
	// ------------------------------------------------------------------------

	public void testCancel() {

		final boolean[] flags = new boolean[4];
		TmfEventRequest request = setupTestRequest(flags);
		request.cancel();

		assertTrue ("isCompleted", request.isCompleted());
		assertFalse("isFailed",    request.isFailed());
		assertTrue ("isCancelled", request.isCancelled());

		assertTrue ("handleCompleted", flags[0]);
		assertFalse("handleSuccess",   flags[1]);
		assertFalse("handleFailure",   flags[2]);
		assertTrue ("handleCancel",    flags[3]);
	}

	// ------------------------------------------------------------------------
	// waitForCompletion
	// ------------------------------------------------------------------------

}
