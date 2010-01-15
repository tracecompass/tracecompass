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

package org.eclipse.linuxtools.tmf.tests.event;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfEventType;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

import junit.framework.TestCase;

/**
 * <b><u>TmfEventTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventTest extends TestCase {

	private final String   fTypeId = "Some type";
	private final String   fLabel0 = "label1";
	private final String   fLabel1 = "label2";
	private final String[] fLabels = new String[] { fLabel0, fLabel1 };

	private final TmfTimestamp      fTimestamp1 = new TmfTimestamp(12345, (byte) 2, 5);
	private final TmfTimestamp      fTimestamp2 = new TmfTimestamp(12350, (byte) 2, 5);
	private final TmfEventSource    fSource     = new TmfEventSource("Source");
	private final TmfEventType      fType       = new TmfEventType(fTypeId, fLabels);
	private final TmfEventReference fReference  = new TmfEventReference("Some reference");

	private final TmfEvent fEvent1;
	private final TmfEvent fEvent2;

	private final TmfEventContent fContent1;
	private final TmfEventContent fContent2;
	
	// ========================================================================
	// Housekeeping
	// ========================================================================

	public TmfEventTest(String name) {
		super(name);

		fEvent1 = new TmfEvent(fTimestamp1, fSource, fType, fReference);
		fContent1 = new TmfEventContent(fEvent1, "Some content");
		fEvent1.setContent(fContent1);

		fEvent2 = new TmfEvent(fTimestamp1, fTimestamp2, fSource, fType, fReference);
		fContent2 = new TmfEventContent(fEvent2, "Some other content");
		fEvent2.setContent(fContent2);
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

	public void testTmfEvent() {
		assertEquals("getTimestamp",         fTimestamp1, fEvent1.getTimestamp());
		assertEquals("getOriginalTimestamp", fTimestamp1, fEvent1.getOriginalTimestamp());
		assertEquals("getSource",            fSource,     fEvent1.getSource());
		assertEquals("getType",              fType,       fEvent1.getType());
		assertEquals("getContent",           fContent1,   fEvent1.getContent());
		assertEquals("getReference",         fReference,  fEvent1.getReference());
	}

	public void testTmfEvent2() {
		assertEquals("getTimestamp",         fTimestamp2, fEvent2.getTimestamp());
		assertEquals("getOriginalTimestamp", fTimestamp1, fEvent2.getOriginalTimestamp());
		assertEquals("getSource",            fSource,     fEvent2.getSource());
		assertEquals("getType",              fType,       fEvent2.getType());
		assertEquals("getContent",           fContent2,   fEvent2.getContent());
		assertEquals("getReference",         fReference,  fEvent2.getReference());
	}

	public void testTmfEventCopy() {
		TmfEvent event = new TmfEvent(fEvent1);
		assertEquals("getTimestamp",         fTimestamp1, event.getTimestamp());
		assertEquals("getOriginalTimestamp", fTimestamp1, event.getOriginalTimestamp());
		assertEquals("getSource",            fSource,     event.getSource());
		assertEquals("getType",              fType,       event.getType());
		assertEquals("getContent",           fContent1,   event.getContent());
		assertEquals("getReference",         fReference,  event.getReference());
	}

//	public void testTmfEventCloneShallowCopy() {
//		TmfEvent event = fEvent1.clone();
//		assertEquals("getTimestamp",         fTimestamp1, event.getTimestamp());
//		assertEquals("getOriginalTimestamp", fTimestamp1, event.getOriginalTimestamp());
//		assertEquals("getSource",            fSource,     event.getSource());
//		assertEquals("getType",              fType,       event.getType());
//		assertEquals("getContent",           fContent1,   event.getContent());
//		assertEquals("getReference",         fReference,  event.getReference());
//	}

//	public void testTmfEventCloneDeepCopy() {
//		TmfEvent event = fEvent1.clone();
//		assertEquals("getTimestamp",         fTimestamp1, event.getTimestamp());
//		assertEquals("getOriginalTimestamp", fTimestamp1, event.getOriginalTimestamp());
//		assertEquals("getSource",            fSource,     event.getSource());
//		assertEquals("getType",              fType,       event.getType());
//		assertEquals("getContent",           fContent1,   event.getContent());
//		assertEquals("getReference",         fReference,  event.getReference());
//	}

	// ========================================================================
	// Operators
	// ========================================================================

//	public void testToString() {
//		String expected1 = "[TmfEventType:" + TmfEventType.DEFAULT_TYPE_ID + "]";
//		assertEquals("toString", expected1, fEvent1.toString());
//
//		String expected2 = "[TmfEventType:" + fTypeId + "]";
//		assertEquals("toString", expected2, fEvent2.toString());
//	}

}
