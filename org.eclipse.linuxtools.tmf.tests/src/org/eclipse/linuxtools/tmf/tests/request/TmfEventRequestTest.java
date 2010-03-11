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
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequestStub;

/**
 * <b><u>TmfEventRequestTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventRequestTest extends TestCase {

	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	public TmfEventRequestTest(String name) {
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

	public void testTmfEventRequest() {
        TmfEventRequest<TmfEvent> request = new TmfEventRequestStub<TmfEvent>(TmfEvent.class);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());

        assertEquals("getIndex",             0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfEventRequestTimeRange() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfEventRequest<TmfEvent> request = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range);

        assertEquals("StartTime", range.getStartTime(), request.getRange().getStartTime());
        assertEquals("EndTime",   range.getEndTime(),   request.getRange().getEndTime());

        assertEquals("getIndex",             0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfEventRequestTimeRangeNbRequested() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfEventRequest<TmfEvent> request = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range, 100);

        assertEquals("StartTime", range.getStartTime(), request.getRange().getStartTime());
        assertEquals("EndTime",   range.getEndTime(),   request.getRange().getEndTime());

        assertEquals("getIndex",               0, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfEventRequestTimeRangeNbRequestedBlocksize() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfEventRequest<TmfEvent> request = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range, 100, 200);

        assertEquals("StartTime", range.getStartTime(), request.getRange().getStartTime());
        assertEquals("EndTime",   range.getEndTime(),   request.getRange().getEndTime());

        assertEquals("getIndex",               0, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize",          200, request.getBlockize());
	}

	// ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

	public void testEquals_Reflexivity() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfEventRequest<TmfEvent> request = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range, 100, 200);

        assertTrue("request.equals(request)", request.equals(request));
	}

	public void testEquals_Symmetry() {
        TmfTimeRange range1 = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfTimeRange range2 = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfTimeRange range3 = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.Zero);

        TmfEventRequest<TmfEvent> request1 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
        TmfEventRequest<TmfEvent> request2 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range2, 100, 200);
        TmfEventRequest<TmfEvent> request3 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range3, 100, 200);

        assertTrue("request1.equals(request2)", request1.equals(request2));
        assertTrue("request2.equals(request1)", request2.equals(request1));

        assertFalse("request1.equals(request3)", request1.equals(request3));
        assertFalse("request3.equals(request1)", request3.equals(request1));
        assertFalse("request2.equals(request3)", request2.equals(request3));
        assertFalse("request3.equals(request2)", request3.equals(request2));
	}

	public void testEquals_Transivity() {
        TmfTimeRange range1 = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);

        TmfEventRequest<TmfEvent> request1 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
        TmfEventRequest<TmfEvent> request2 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
        TmfEventRequest<TmfEvent> request3 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);

        assertTrue("request1.equals(request2)", request1.equals(request2));
        assertTrue("request1.equals(request3)", request1.equals(request3));
        assertTrue("request2.equals(request1)", request2.equals(request1));
        assertTrue("request2.equals(request3)", request2.equals(request3));
        assertTrue("request3.equals(request1)", request3.equals(request1));
        assertTrue("request3.equals(request2)", request3.equals(request2));
	}

}