/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.request;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>TmfDataRequestTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfDataRequestTest extends TestCase {

	// ========================================================================
	// Housekeeping
	// ========================================================================

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

	// ========================================================================
	// Constructors
	// ========================================================================

	public void testTmfDataRequestIndexNbEvents() {
        TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(10, 100);

        assertEquals("getRange",            null, request.getRange());
        assertEquals("getIndex",              10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequestedEvents());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfDataRequestIndexNbEventsBlocksize() {
        TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(10, 100, 200);

        assertEquals("getRange",            null, request.getRange());
        assertEquals("getIndex",              10, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequestedEvents());
        assertEquals("getBlockize",          200, request.getBlockize());
	}

	public void testTmfDataRequestTimeRange() {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("getIndex",              0, request.getIndex());
        assertEquals("getNbRequestedEvents", -1, request.getNbRequestedEvents());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfDataRequestTimeRangeNbEvents() {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 10);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("getIndex",              0, request.getIndex());
        assertEquals("getNbRequestedEvents", 10, request.getNbRequestedEvents());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfDataRequestTimeRangeNbEventsBlockSize() {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 10, 100);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("getIndex",              0, request.getIndex());
        assertEquals("getNbRequestedEvents", 10, request.getNbRequestedEvents());
        assertEquals("getBlockize",         100, request.getBlockize());
	}

}
