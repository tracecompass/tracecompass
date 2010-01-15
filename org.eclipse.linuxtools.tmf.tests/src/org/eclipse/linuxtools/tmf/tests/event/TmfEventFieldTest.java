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
import org.eclipse.linuxtools.tmf.event.TmfEventField;
import org.eclipse.linuxtools.tmf.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfEventType;
import org.eclipse.linuxtools.tmf.event.TmfEventTypeStub;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

import junit.framework.TestCase;

/**
 * <b><u>TmfEventFieldTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventFieldTest extends TestCase {

	private final TmfEventContent fContent;
	private final String fFieldId = "Field";
	private final Object fValue1  = new String("Value"); 
	private final Object fValue2  = new Integer(10); 

	// ========================================================================
	// Housekeeping
	// ========================================================================

	public TmfEventFieldTest(String name) {
		super(name);
		TmfTimestamp      fTimestamp   = new TmfTimestamp();
		TmfEventSource    fEventSource = new TmfEventSource();
		TmfEventType      fEventType   = new TmfEventTypeStub();
		TmfEventReference fReference   = new TmfEventReference();
		TmfEvent          fEvent       = new TmfEvent(fTimestamp, fEventSource, fEventType, fReference);

		fContent = new TmfEventContent(fEvent, "Some content");
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

	public void testTmfEventField() {
		TmfEventField field = new TmfEventField(fContent, fFieldId, fValue1);
		assertSame("getParent", fContent, field.getParent());
		assertSame("getId",     fFieldId, field.getId());
		assertSame("getValue",  fValue1,  field.getValue());
	}

	public void testTmfEventFieldCopy() {
		TmfEventField original = new TmfEventField(fContent, fFieldId, fValue1);
		TmfEventField field = new TmfEventField(original);
		assertSame("getParent", fContent, field.getParent());
		assertSame("getId",     fFieldId, field.getId());
		assertSame("getValue",  fValue1,  field.getValue());
	}

	public void testCloneShallowCopy() {
		TmfEventField original = new TmfEventField(fContent, fFieldId, fValue1);
		TmfEventField field = original.clone();
		assertSame("getParent", fContent, field.getParent());
		assertSame("getId",     fFieldId, field.getId());
		assertSame("getValue",  fValue1,  field.getValue());
	}

//	public void testCloneDeepCopy() {
//		TmfEventField original = new TmfEventField(fContent, fFieldId, fValue1);
//		TmfEventField field = original.clone();
//		assertNotSame("getParent", fContent, field.getParent());
//		assertNotSame("getId",     fFieldId, field.getId());
//		assertNotSame("getValue",  fValue1,  field.getValue());
//		assertEquals ("getParent", fContent, field.getParent());
//		assertEquals ("getId",     fFieldId, field.getId());
//		assertEquals ("getValue",  fValue1,  field.getValue());
//	}

	// ========================================================================
	// Operators
	// ========================================================================

	public void testToString() {
		String expected1 = "[TmfEventField(" + fFieldId + ":" + fValue1.toString() + ")]";
		TmfEventField field = new TmfEventField(fContent, fFieldId, fValue1);
		assertEquals("toString", expected1, field.toString());

		String expected2 = "[TmfEventField(" + fFieldId + ":" + fValue2.toString() + ")]";
		field = new TmfEventField(fContent, fFieldId, fValue2);
		assertEquals("toString", expected2, field.toString());
	}

}
