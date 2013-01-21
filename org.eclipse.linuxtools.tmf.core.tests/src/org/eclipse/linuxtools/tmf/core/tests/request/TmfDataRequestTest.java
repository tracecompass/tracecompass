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
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.tests.stubs.request.TmfDataRequestStub;

/**
 * <b><u>TmfDataRequestTest</u></b>
 * <p>
 * Test suite for the TmfDataRequest class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfDataRequestTest extends TestCase {

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

    /**
     * @param name the test name
     */
	public TmfDataRequestTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		TmfDataRequest.reset();
		fRequest1  = new TmfDataRequestStub(TmfEvent.class, 10, 100, 200);
		fRequest2  = new TmfDataRequestStub(TmfEvent.class, 20, 100, 200);
		fRequest3  = new TmfDataRequestStub(TmfEvent.class, 20, 200, 200);
		fRequest4  = new TmfDataRequestStub(TmfEvent.class, 20, 200, 300);
    	fRequest1b = new TmfDataRequestStub(TmfEvent.class, 10, 100, 200);
		fRequest1c = new TmfDataRequestStub(TmfEvent.class, 10, 100, 200);
		fRequestCount = fRequest1c.getRequestId() + 1;
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private static TmfDataRequest setupTestRequest(final boolean[] flags) {

		TmfDataRequest request = new TmfDataRequestStub(TmfEvent.class, 10, 100, 200) {
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

	public void testTmfDataRequest() {
        TmfDataRequest request = new TmfDataRequestStub(TmfEvent.class);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getIndex", 0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfDataRequestIndex() {
        TmfDataRequest request = new TmfDataRequestStub(TmfEvent.class, 10);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getIndex", 10, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfDataRequestIndexNbRequested() {
        TmfDataRequest request = new TmfDataRequestStub(TmfEvent.class, 10, 100);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getIndex", 10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfDataRequestIndexNbEventsBlocksize() {
        TmfDataRequest request = new TmfDataRequestStub(TmfEvent.class, 10, 100, 200);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

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
        String expected1 = "[TmfDataRequest(0,TmfEvent,10,100,200)]";
        String expected2 = "[TmfDataRequest(1,TmfEvent,20,100,200)]";
        String expected3 = "[TmfDataRequest(2,TmfEvent,20,200,200)]";
        String expected4 = "[TmfDataRequest(3,TmfEvent,20,200,300)]";

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
		TmfDataRequest request = setupTestRequest(flags);
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
		TmfDataRequest request = setupTestRequest(flags);
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
		TmfDataRequest request = setupTestRequest(flags);
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
