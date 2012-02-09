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

package org.eclipse.linuxtools.tmf.core.tests.event;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventContent;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * <b><u>TmfEventTest</u></b>
 * <p>
 * Test suite for the TmfEvent class.
 */
@SuppressWarnings("nls")
public class TmfEventTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private final String   fTypeId = "Some type";
	private final String   fLabel0 = "label1";
	private final String   fLabel1 = "label2";
	private final String[] fLabels = new String[] { fLabel0, fLabel1 };

	private final TmfTimestamp fTimestamp1 = new TmfTimestamp(12345, (byte) 2, 5);
	private final TmfTimestamp fTimestamp2 = new TmfTimestamp(12350, (byte) 2, 5);
	private final String       fSource     = "Source";
	private final TmfEventType fType       = new TmfEventType(fTypeId, fLabels);
	private final String       fReference  = "Some reference";

	private final TmfEvent fEvent1;
	private final TmfEvent fEvent2;

	private final TmfEventContent fContent1;
	private final TmfEventContent fContent2;
	
	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfEventTest(String name) {
		super(name);

		fEvent1 = new TmfEvent(fTimestamp1, fSource, fType, fReference);
		fContent1 = new TmfEventContent(fEvent1, "Some content");
		fEvent1.setContent(fContent1);

		fEvent2 = new TmfEvent(fTimestamp2, fSource, fType, fReference);
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

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public void testTmfEvent() {
		assertEquals("getTimestamp",         fTimestamp1, fEvent1.getTimestamp());
		assertEquals("getSource",            fSource,     fEvent1.getSource());
		assertEquals("getType",              fType,       fEvent1.getType());
		assertEquals("getContent",           fContent1,   fEvent1.getContent());
		assertEquals("getReference",         fReference,  fEvent1.getReference());
	}

	public void testTmfEvent2() {
		assertEquals("getTimestamp",         fTimestamp2, fEvent2.getTimestamp());
		assertEquals("getSource",            fSource,     fEvent2.getSource());
		assertEquals("getType",              fType,       fEvent2.getType());
		assertEquals("getContent",           fContent2,   fEvent2.getContent());
		assertEquals("getReference",         fReference,  fEvent2.getReference());
	}

	public void testTmfEventCopy() {
		TmfEvent event = new TmfEvent(fEvent1);
		assertEquals("getTimestamp",         fTimestamp1, event.getTimestamp());
		assertEquals("getSource",            fSource,     event.getSource());
		assertEquals("getType",              fType,       event.getType());
		assertEquals("getContent",           fContent1,   event.getContent());
		assertEquals("getReference",         fReference,  event.getReference());
	}

	public void testEventCopy2() throws Exception {
		try {
			new TmfEvent(null);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}
	}

	// ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
		assertTrue("equals", fEvent1.equals(fEvent1));
		assertTrue("equals", fEvent2.equals(fEvent2));

		assertTrue("equals", !fEvent1.equals(fEvent2));
		assertTrue("equals", !fEvent2.equals(fEvent1));
	}
	
	public void testEqualsSymmetry() throws Exception {
		TmfEvent event1 = new TmfEvent(fEvent1);
		TmfEvent event2 = new TmfEvent(fEvent2);

		assertTrue("equals", event1.equals(fEvent1));
		assertTrue("equals", fEvent1.equals(event1));

		assertTrue("equals", event2.equals(fEvent2));
		assertTrue("equals", fEvent2.equals(event2));
	}
	
	public void testEqualsTransivity() throws Exception {
		TmfEvent event1 = new TmfEvent(fEvent1);
		TmfEvent event2 = new TmfEvent(fEvent1);
		TmfEvent event3 = new TmfEvent(fEvent1);

		assertTrue("equals", event1.equals(event2));
		assertTrue("equals", event2.equals(event3));
		assertTrue("equals", event1.equals(event3));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !fEvent1.equals(null));
		assertTrue("equals", !fEvent2.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		TmfEvent event1 = new TmfEvent(fEvent1);
		TmfEvent event2 = new TmfEvent(fEvent2);

		assertTrue("hashCode", fEvent1.hashCode() == event1.hashCode());
		assertTrue("hashCode", fEvent2.hashCode() == event2.hashCode());

		assertTrue("hashCode", fEvent1.hashCode() != event2.hashCode());
		assertTrue("hashCode", fEvent2.hashCode() != event1.hashCode());
	}
	
//	// ------------------------------------------------------------------------
//	// toString
//	// ------------------------------------------------------------------------
//
//	public void testToString() {
//		String expected1 = "[TmfEvent (" + fTimestamp1 + "," + fSource + "," + fType + "," + fContent1 + ")]";
//		assertEquals("toString", expected1, fEvent1.toString());
//
//		String expected2 = "[TmfEvent(" + fTimestamp2 + "," + fSource + "," + fType + "," + fContent2 + ")]";
//		assertEquals("toString", expected2, fEvent2.toString());
//	}

	// ------------------------------------------------------------------------
	// setContent
	// ------------------------------------------------------------------------

	public void testSetContent() {
		TmfEvent event = new TmfEvent(fEvent1);
		assertEquals("setContent", fContent1, event.getContent());

		event.setContent(fContent2);
		assertEquals("setContent", fContent2, event.getContent());

		event.setContent(fContent1);
		assertEquals("setContent", fContent1, event.getContent());
	}

}
