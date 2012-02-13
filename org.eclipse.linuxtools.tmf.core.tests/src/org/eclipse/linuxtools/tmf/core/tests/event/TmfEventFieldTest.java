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
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.stubs.event.TmfEventTypeStub;

/**
 * <b><u>TmfEventFieldTest</u></b>
 * <p>
 * Test suite for the TmfEventField class.
 */
@SuppressWarnings("nls")
public class TmfEventFieldTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private final TmfEventContent fContent;
	private final String fFieldId = "Field";
	private final Object fValue1  = new String("Value"); 
	private final Object fValue2  = new Integer(10); 

	private TmfEventField fField0;
	private TmfEventField fField1;
	private TmfEventField fField2;
	private TmfEventField fField3;

    // ------------------------------------------------------------------------
	// Housekeeping
    // ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfEventFieldTest(String name) {
		super(name);
		TmfTimestamp fTimestamp   = new TmfTimestamp();
		String       fEventSource = "";
		TmfEventType fEventType   = new TmfEventTypeStub();
		String       fReference   = "";
		TmfEvent     fEvent       = new TmfEvent(fTimestamp, fEventSource, fEventType, fReference);

		fContent = new TmfEventContent(fEvent, "Some content");

		fField0 = new TmfEventField(fContent, fFieldId, fValue1);
		fField1 = new TmfEventField(fContent, fFieldId, fValue1);
		fField2 = new TmfEventField(fContent, fFieldId, fValue1);
		fField3 = new TmfEventField(fContent, fFieldId, fValue2);
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

	public void testTmfEventField() {
		assertSame("getParent", fContent, fField0.getContent());
		assertSame("getId",     fFieldId, fField0.getId());
		assertSame("getValue",  fValue1,  fField0.getValue());
	}

	public void testTmfEventFieldBadArg() {
		try {
			new TmfEventField(fContent, null, fValue1);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}
	}

	public void testTmfEventFieldCopy() {
		TmfEventField original = new TmfEventField(fContent, fFieldId, fValue1);
		TmfEventField field = new TmfEventField(original);
		assertSame("getContent", fContent, field.getContent());
		assertSame("getId",      fFieldId, field.getId());
		assertSame("getValue",   fValue1,  field.getValue());
	}

	public void testTmfEventFieldCopy2() {
		try {
			new TmfEventField(null);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}
	}

    // ------------------------------------------------------------------------
	// Modifiers
    // ------------------------------------------------------------------------

	private class MyField extends TmfEventField {
		public MyField(TmfEventContent parent, String id, Object value) {
			super(parent, id, value);
		}
	    public MyField(TmfEventField field) {
			super(field);
		}
		@Override
		public void setValue(Object value) {
	    	super.setValue(value);
	    }
	}

	public void testSetValue() {
		TmfEventField original = new TmfEventField(fContent, fFieldId, fValue1);
		TmfEventField field = new TmfEventField(original);

		MyField myField = new MyField(field);
		assertSame("getValue", fValue1,  myField.getValue());

		myField.setValue(fValue2);
		assertSame("getValue", fValue2,  myField.getValue());
	}

    // ------------------------------------------------------------------------
	// equals
    // ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
		assertTrue("equals", fField0.equals(fField0));
		assertTrue("equals", fField3.equals(fField3));

		assertTrue("equals", !fField0.equals(fField3));
		assertTrue("equals", !fField3.equals(fField0));
	}
	
	public void testEqualsSymmetry() throws Exception {
		assertTrue("equals", fField0.equals(fField1));
		assertTrue("equals", fField1.equals(fField0));

		assertTrue("equals", !fField0.equals(fField3));
		assertTrue("equals", !fField3.equals(fField0));
	}
	
	public void testEqualsTransivity() throws Exception {
		assertTrue("equals", fField0.equals(fField1));
		assertTrue("equals", fField1.equals(fField2));
		assertTrue("equals", fField0.equals(fField2));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !fField0.equals(null));
		assertTrue("equals", !fField3.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		assertTrue("hashCode", fField0.hashCode() == fField1.hashCode());
		assertTrue("hashCode", fField0.hashCode() != fField3.hashCode());
	}
	
    // ------------------------------------------------------------------------
	// toString
    // ------------------------------------------------------------------------

	public void testToString() {
		String expected1 = "TmfEventField [fFieldId=" + fFieldId + ", fValue=" + fValue1.toString() + "]";
		TmfEventField field = new TmfEventField(fContent, fFieldId, fValue1);
		assertEquals("toString", expected1, field.toString());

        String expected2 = "TmfEventField [fFieldId=" + fFieldId + ", fValue=" + fValue2.toString() + "]";
		field = new TmfEventField(fContent, fFieldId, fValue2);
		assertEquals("toString", expected2, field.toString());
	}

}
