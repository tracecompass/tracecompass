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

package org.eclipse.linuxtools.tmf.tests.request;


import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.request.TmfCoalescedDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequestStub;

/**
 * <b><u>TmfCoalescedDataRequestTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfCoalescedDataRequestTest extends TestCase {

	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	public TmfCoalescedDataRequestTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
	}

	@Override
	public void tearDown() throws Exception {
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public void testTmfCoalescedDataRequest() {
		TmfCoalescedDataRequest<TmfEvent> request = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class);

        assertEquals("getIndex",             0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfCoalescedDataRequestIndex() {
		TmfCoalescedDataRequest<TmfEvent> request = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10);

        assertEquals("getIndex",             10, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfCoalescedDataRequestIndexNbRequested() {
		TmfCoalescedDataRequest<TmfEvent> request = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100);

        assertEquals("getIndex",              10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfCoalescedDataRequestIndexNbEventsBlocksize() {
		TmfCoalescedDataRequest<TmfEvent> request = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100, 200);

        assertEquals("getIndex",              10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize",          200, request.getBlockize());
	}

	// ------------------------------------------------------------------------
	// isCompatible
	// ------------------------------------------------------------------------

	public void testIsCompatible() {
		TmfCoalescedDataRequest<TmfEvent> coalescedRequest = new TmfCoalescedDataRequest<TmfEvent>(TmfEvent.class, 10, 100, 200);
		TmfDataRequest<TmfEvent> request1 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
		TmfDataRequest<TmfEvent> request2 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 11, 100, 200);
		TmfDataRequest<TmfEvent> request3 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 101, 200);
		TmfDataRequest<TmfEvent> request4 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 201);

        assertTrue ("isCompatible", coalescedRequest.isCompatible(request1));
        assertFalse("isCompatible", coalescedRequest.isCompatible(request2));
        assertFalse("isCompatible", coalescedRequest.isCompatible(request3));
        assertFalse("isCompatible", coalescedRequest.isCompatible(request4));
	}

}
