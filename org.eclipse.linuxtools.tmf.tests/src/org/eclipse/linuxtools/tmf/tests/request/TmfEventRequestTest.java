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

	public void testTmfDataRequest() {
        TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());

        assertEquals("getIndex",             0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfDataRequestTimeRange() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range);

        assertEquals("StartTime", range.getStartTime(), request.getRange().getStartTime());
        assertEquals("EndTime",   range.getEndTime(),   request.getRange().getEndTime());

        assertEquals("getIndex",             0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfDataRequestTimeRangeNbRequested() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, 100);

        assertEquals("StartTime", range.getStartTime(), request.getRange().getStartTime());
        assertEquals("EndTime",   range.getEndTime(),   request.getRange().getEndTime());

        assertEquals("getIndex",               0, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfDataRequestTimeRangeNbRequestedBlocksize() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, 100, 200);

        assertEquals("StartTime", range.getStartTime(), request.getRange().getStartTime());
        assertEquals("EndTime",   range.getEndTime(),   request.getRange().getEndTime());

        assertEquals("getIndex",               0, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize",          200, request.getBlockize());
	}

}