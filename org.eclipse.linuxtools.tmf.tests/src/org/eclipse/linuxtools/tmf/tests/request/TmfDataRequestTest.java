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
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequestStub;

/**
 * <b><u>TmfDataRequestTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfDataRequestTest extends TestCase {

	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	public TmfDataRequestTest(String name) {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public void testTmfDataRequest() {
        TmfDataRequest<TmfEvent> request = new TmfDataRequestStub<TmfEvent>(TmfEvent.class);

        assertEquals("getIndex",             0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfDataRequestIndex() {
        TmfDataRequest<TmfEvent> request = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10);

        assertEquals("getIndex",             10, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfDataRequestIndexNbRequested() {
        TmfDataRequest<TmfEvent> request = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100);

        assertEquals("getIndex",              10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfDataRequestIndexNbEventsBlocksize() {
        TmfDataRequest<TmfEvent> request = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);

        assertEquals("getIndex",              10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize",          200, request.getBlockize());
	}

	// ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

	public void testEquals_Reflexivity() {
        TmfDataRequest<TmfEvent> request = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);

        assertTrue("request.equals(request)", request.equals(request));
	}

	public void testEquals_Symmetry() {
        TmfDataRequest<TmfEvent> request1 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
        TmfDataRequest<TmfEvent> request2 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
        TmfDataRequest<TmfEvent> request3 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 20, 100, 200);

        assertTrue("request1.equals(request2)", request1.equals(request2));
        assertTrue("request2.equals(request1)", request2.equals(request1));

        assertFalse("request1.equals(request3)", request1.equals(request3));
        assertFalse("request3.equals(request1)", request3.equals(request1));
        assertFalse("request2.equals(request3)", request2.equals(request3));
        assertFalse("request3.equals(request2)", request3.equals(request2));
	}

	public void testEquals_Transivity() {
        TmfDataRequest<TmfEvent> request1 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
        TmfDataRequest<TmfEvent> request2 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);
        TmfDataRequest<TmfEvent> request3 = new TmfDataRequestStub<TmfEvent>(TmfEvent.class, 10, 100, 200);

        assertTrue("request1.equals(request2)", request1.equals(request2));
        assertTrue("request2.equals(request3)", request2.equals(request3));
        assertTrue("request1.equals(request3)", request1.equals(request3));
	}

}