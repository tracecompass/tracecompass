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
import org.eclipse.linuxtools.tmf.core.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.core.event.TmfEventType;
import org.eclipse.linuxtools.tmf.core.event.TmfNoSuchFieldException;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.stubs.event.TmfEventContentStub;
import org.eclipse.linuxtools.tmf.stubs.event.TmfEventTypeStub;

/**
 * <b><u>TmfEventContentTest</u></b>
 * <p>
 * Test suite for the TmfEventContent class.
 */
@SuppressWarnings("nls")
public class TmfEventContentTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private final TmfTimestamp      fTimestamp;
	private final String            fEventSource;
	private final TmfEventType      fEventType;
	private final TmfEventTypeStub  fEventTypeStub;
	private final TmfEventReference fReference;
	private final TmfEvent          fEvent;
	private final TmfEvent          fEventStub;

	private final Object fRawContent0 = new String("Some content");
	private final Object fRawContent1 = new String("Some other content");

	private final TmfEventContent     fBasicContent0;
	private final TmfEventContent     fBasicContent1;
	private final TmfEventContent     fBasicContent2;
	private final TmfEventContentStub fStubContent;

	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfEventContentTest(String name) {
		super(name);
		fTimestamp     = new TmfTimestamp();
		fEventSource   = "";
		fEventType     = new TmfEventType();
		fEventTypeStub = new TmfEventTypeStub();
		fReference     = new TmfEventReference();

		fEvent         = new TmfEvent(fTimestamp, fEventSource, fEventType, fReference);
		fBasicContent0 = new TmfEventContent(fEvent, fRawContent0);
		fBasicContent1 = new TmfEventContent(fEvent, fRawContent0);
		fBasicContent2 = new TmfEventContent(fEvent, fRawContent0);

		fEventStub    = new TmfEvent(fTimestamp, fEventSource, fEventTypeStub, fReference);
		fStubContent  = new TmfEventContentStub(fEventStub, fRawContent1);
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

	public void testTmfEventContent() {
		assertSame("getLabels",    fEvent,       fBasicContent0.getEvent());
		assertEquals("getType",    fEventType,   fBasicContent0.getType());
		assertEquals("getContent", fRawContent0, fBasicContent0.getContent());
	}

	public void testTmfEventContentCopy() {
		TmfEventContent content  = new TmfEventContent(fBasicContent0);
		assertSame("getLabels",    fEvent,       content.getEvent());
		assertEquals("getType",    fEventType,   content.getType());
		assertEquals("getContent", fRawContent0, content.getContent());
	}

	public void testTmfEventContentCopy2() {
		try {
			new TmfEventContent(null);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}
	}

	// ------------------------------------------------------------------------
	// setEvent
	// ------------------------------------------------------------------------

	public void testSetEvent() {
		TmfEvent event = new TmfEvent(fTimestamp, fEventSource, fEventType, fReference);
		TmfEventContent content1 = new TmfEventContent(event, fRawContent0);
		event.setContent(content1);
		TmfEventContent content2 = new TmfEventContent(null, fRawContent1);

		content2.setEvent(event);

		assertEquals("setEvent", event, content2.getEvent());
//		assertEquals("setEvent", content2, event.getContent());
//		assertEquals("setEvent", null, content1.getEvent());
	}

	// ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
		@SuppressWarnings("unused")
		Object[] fields1 = fBasicContent0.getFields(); 
		@SuppressWarnings("unused")
		Object[] fields2 = fStubContent.getFields(); 

		assertTrue("equals", fBasicContent0.equals(fBasicContent0));
		assertTrue("equals", fStubContent.equals(fStubContent));

		assertTrue("equals", !fBasicContent0.equals(fStubContent));
		assertTrue("equals", !fStubContent.equals(fBasicContent0));
	}
	
	public void testEqualsSymmetry() throws Exception {
		assertTrue("equals", fBasicContent0.equals(fBasicContent2));
		assertTrue("equals", fBasicContent2.equals(fBasicContent0));

		assertTrue("equals", !fBasicContent0.equals(fStubContent));
		assertTrue("equals", !fStubContent.equals(fBasicContent0));
	}
	
	public void testEqualsTransivity() throws Exception {
		assertTrue("equals", fBasicContent0.equals(fBasicContent1));
		assertTrue("equals", fBasicContent1.equals(fBasicContent2));
		assertTrue("equals", fBasicContent0.equals(fBasicContent2));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !fBasicContent0.equals(null));
		assertTrue("equals", !fStubContent.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		assertTrue("hashCode", fBasicContent0.hashCode() == fBasicContent2.hashCode());
		assertTrue("hashCode", fBasicContent0.hashCode() != fStubContent.hashCode());
	}
	
	public void testHashCode2() throws Exception {
		TmfEventContent basicContent0 = new TmfEventContent(null, fRawContent0);
		TmfEventContent basicContent1 = new TmfEventContent(fEvent, null);
		TmfEventContent basicContent2 = new TmfEventContent(null, null);

		assertTrue("hashCode", fBasicContent0.hashCode() == basicContent0.hashCode());
		assertTrue("hashCode", fBasicContent0.hashCode() != basicContent1.hashCode());
		assertTrue("hashCode", fBasicContent0.hashCode() != basicContent2.hashCode());

		assertTrue("hashCode", basicContent0.hashCode() != basicContent1.hashCode());
		assertTrue("hashCode", basicContent0.hashCode() != basicContent2.hashCode());

		assertTrue("hashCode", basicContent1.hashCode() == basicContent2.hashCode());
	}
	
	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	public void testToString() {
		String expected = "[TmfEventContent(" + fRawContent0 + ")]";
		TmfEventContent content = new TmfEventContent(fEvent, fRawContent0);
		assertEquals("toString", expected, content.toString());
	}

	public void testToString2() {
		String expected = "[TmfEventContent(1,-10,true,some string,[TmfTimestamp(1,2,3)])]";
		TmfEventContentStub content = new TmfEventContentStub(fEvent, fRawContent0);
		assertEquals("toString", expected, content.toString());
	}

	// ------------------------------------------------------------------------
	// Basic content parsing
	// ------------------------------------------------------------------------

	public void testGetFields() {
		Object[] fields = fBasicContent0.getFields(); 
		assertEquals("getFields", 1, fields.length);
		assertEquals("getFields", fRawContent0, fields[0].toString());
	}

	public void testGetFieldFromId() {
		Object field;
		try {
			field = fBasicContent0.getField("Content");
			assertEquals("getField", fRawContent0, field.toString());
		} catch (TmfNoSuchFieldException e) {
			fail("Field not found");
		} 
	}

	public void testGetFieldFromIdFailed() {
		try {
			fBasicContent0.getField("Dummy");
			fail("Found an inexisting field...");
		} catch (TmfNoSuchFieldException e) {
			// Success
		} 
	}

	public void testGetFieldFromPos() {
		Object field = fBasicContent0.getField(0);
		assertEquals("getField", fRawContent0, field.toString());
	}

	public void testGetFieldFromPosFailed() {
		Object field = fBasicContent0.getField(10);
		assertEquals("getField", null, field);
	}

	// ------------------------------------------------------------------------
	// Standard content parsing
	// ------------------------------------------------------------------------

	public void testGetFields2() {
		Object[] fields = fStubContent.getFields(); 
		assertEquals("getFields", 5, fields.length);
	}

	public void testGetFieldFromId2() {
		Object field;
		try {
			field = fStubContent.getField("Field1");
			assertEquals("getField", new Integer(1), field);

			field = fStubContent.getField("Field2");
			assertEquals("getField", new Integer(-10), field);

			field = fStubContent.getField("Field3");
			assertEquals("getField", new Boolean(true), field);

			field = fStubContent.getField("Field4");
			assertEquals("getField", new String("some string"), field);

			field = fStubContent.getField("Field5");
			assertEquals("getField", new TmfTimestamp(1, (byte) 2, 3), field);

		} catch (TmfNoSuchFieldException e) {
			fail("Field not found");
		} 
	}

	public void testGetFieldFromPos2() {
		TmfEventContentStub content = new TmfEventContentStub(fEvent, fRawContent0);

		Object field;
		field = content.getField(0);
		assertEquals("getField", new Integer(1), field);

		field = content.getField(1);
		assertEquals("getField", new Integer(-10), field);

		field = content.getField(2);
		assertEquals("getField", new Boolean(true), field);

		field = content.getField(3);
		assertEquals("getField", new String("some string"), field);

		field = content.getField(4);
		assertEquals("getField", new TmfTimestamp(1, (byte) 2, 3), field);
	}

}
