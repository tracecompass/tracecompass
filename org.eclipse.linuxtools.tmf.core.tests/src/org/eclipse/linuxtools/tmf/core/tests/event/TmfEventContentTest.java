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
import org.eclipse.linuxtools.tmf.core.event.TmfEventField;
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
	private final String            fReference;
	private final TmfEvent          fEvent;
	private final TmfEvent          fEventStub;

	private final String fRawContent0 = "Some content";
	private final String fRawContent1 = "Some other content";

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
		fEventType     = new TmfEventTypeStub();
		fEventTypeStub = new TmfEventTypeStub();
		fReference     = "";

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
		assertEquals("getContent", fRawContent0, fBasicContent0.getRawContent());
	}

	public void testTmfEventContentCopy() {
		TmfEventContent content  = new TmfEventContent(fBasicContent0);
		assertSame("getLabels",    fEvent,       content.getEvent());
		assertEquals("getType",    fEventType,   content.getType());
		assertEquals("getContent", fRawContent0, content.getRawContent());
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

//	// ------------------------------------------------------------------------
//	// setEvent
//	// ------------------------------------------------------------------------
//
//	public void testSetEvent() {
//		TmfEvent event = new TmfEvent(fTimestamp, fEventSource, fEventType, fReference);
//		TmfEventContent content1 = new TmfEventContent(event, fRawContent0);
//		event.setContent(content1);
//		TmfEventContent content2 = new TmfEventContent(null, fRawContent1);
//
//		content2.setEvent(event);
//
//		assertEquals("setEvent", event, content2.getEvent());
////		assertEquals("setEvent", content2, event.getContent());
////		assertEquals("setEvent", null, content1.getEvent());
//	}

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
	    String expected = "TmfEventContent [fRawContent=" + fRawContent0 + ", fFields=null]";
		TmfEventContent content = new TmfEventContent(fEvent, fRawContent0);
		assertEquals("toString", expected, content.toString());
	}

	public void testToString2() {
        String expected = "TmfEventContent [fRawContent=" + fRawContent0 + ", fFields=null]";
		TmfEventContentStub content = new TmfEventContentStub(fEvent, fRawContent0);
		assertEquals("toString", expected, content.toString());
	}

	// ------------------------------------------------------------------------
	// Basic content parsing
	// ------------------------------------------------------------------------

	public void testGetFields() {
	    TmfEventField expected = new TmfEventField(fBasicContent0, TmfEventContent.FIELD_ID_CONTENT, fRawContent0);
		Object[] fields = fBasicContent0.getFields(); 
		assertEquals("getFields", 1, fields.length);
		assertEquals("getFields", expected, fields[0]);
	}

//	public void testGetFieldFromId() {
//		Object field;
//		try {
//			field = fStubContent.getField("Content");
//			assertEquals("getField", fRawContent0, field.toString());
//		} catch (TmfNoSuchFieldException e) {
//			fail("Field not found");
//		} 
//	}

	public void testGetFieldFromIdFailed() {
		try {
			fBasicContent0.getField("Dummy");
			fail("Found an inexisting field...");
		} catch (TmfNoSuchFieldException e) {
			// Success
		} 
	}

	public void testGetFieldFromPos() {
        String expected = "TmfEventField [fFieldId=" + 
	           TmfEventContent.FIELD_ID_CONTENT + ", fValue=" + fRawContent0 + "]"; 
		Object field = fBasicContent0.getField(0);
		assertEquals("getField", expected, field.toString());
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
		TmfEventField expected;
		try {
			field = fStubContent.getField("Field1");
			expected = new TmfEventField(fStubContent, "field1", new Integer(1));
			assertEquals("getField", expected, field);

			field = fStubContent.getField("Field2");
            expected = new TmfEventField(fStubContent, "field2", new Integer(-10));
			assertEquals("getField", expected, field);

			field = fStubContent.getField("Field3");
            expected = new TmfEventField(fStubContent, "field3", new Boolean(true));
			assertEquals("getField", expected, field);

			field = fStubContent.getField("Field4");
            expected = new TmfEventField(fStubContent, "field4", "some string");
			assertEquals("getField", expected, field);

			field = fStubContent.getField("Field5");
            expected = new TmfEventField(fStubContent, "field5", new TmfTimestamp(1, 2, 3));
			assertEquals("getField", expected, field);

		} catch (TmfNoSuchFieldException e) {
			fail("Field not found");
		} 
	}

	public void testGetFieldFromPos2() {
		TmfEventContentStub content = new TmfEventContentStub(fEvent, fRawContent0);

        Object field;
        TmfEventField expected;

        field = content.getField(0);
		expected = new TmfEventField(content, "field1", new Integer(1));
		assertEquals("getField", expected, field);

		field = content.getField(1);
        expected = new TmfEventField(content, "field2", new Integer(-10));
		assertEquals("getField", expected, field);

		field = content.getField(2);
        expected = new TmfEventField(content, "field3", new Boolean(true));
		assertEquals("getField", expected, field);

		field = content.getField(3);
        expected = new TmfEventField(content, "field4", "some string");
		assertEquals("getField",expected, field);

		field = content.getField(4);
        expected = new TmfEventField(content, "field5", new TmfTimestamp(1, 2, 3));
		assertEquals("getField", expected, field);
	}

}
