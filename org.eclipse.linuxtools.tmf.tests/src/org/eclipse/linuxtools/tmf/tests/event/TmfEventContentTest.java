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
import org.eclipse.linuxtools.tmf.event.TmfEventContentStub;
import org.eclipse.linuxtools.tmf.event.TmfEventReference;
import org.eclipse.linuxtools.tmf.event.TmfEventSource;
import org.eclipse.linuxtools.tmf.event.TmfEventType;
import org.eclipse.linuxtools.tmf.event.TmfEventTypeStub;
import org.eclipse.linuxtools.tmf.event.TmfNoSuchFieldException;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

import junit.framework.TestCase;

/**
 * <b><u>TmfEventContentTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventContentTest extends TestCase {

	private final TmfTimestamp      fTimestamp;
	private final TmfEventSource    fEventSource;
	private final TmfEventType      fEventType;
	private final TmfEventTypeStub  fEventTypeStub;
	private final TmfEventReference fReference;
	private final TmfEvent          fEvent;
	private final TmfEvent          fEventStub;

	private final Object fRawContent = new String("Some content");

	private final TmfEventContent     fBasicContent;
	private final TmfEventContentStub fStubContent;

	/**
	 * @param name
	 */
	public TmfEventContentTest(String name) {
		super(name);
		fTimestamp     = new TmfTimestamp();
		fEventSource   = new TmfEventSource();
		fEventType     = new TmfEventType();
		fEventTypeStub = new TmfEventTypeStub();
		fReference     = new TmfEventReference();

		fEvent        = new TmfEvent(fTimestamp, fEventSource, fEventType, fReference);
		fBasicContent = new TmfEventContent(fEvent, fRawContent);

		fEventStub    = new TmfEvent(fTimestamp, fEventSource, fEventTypeStub, fReference);
		fStubContent  = new TmfEventContentStub(fEventStub, fRawContent);
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

	public void testTmfEventContent() {
		assertSame("getLabels",  fEvent,      fBasicContent.getEvent());
		assertSame("getType",    fEventType,  fBasicContent.getType());
		assertSame("getContent", fRawContent, fBasicContent.getContent());
	}

	public void testTmfEventContentCopy() {
		TmfEventContent content  = new TmfEventContent(fBasicContent);
		assertSame("getLabels",  fEvent,      content.getEvent());
		assertSame("getType",    fEventType,  content.getType());
		assertSame("getContent", fRawContent, content.getContent());
	}

	public void testCloneShallowCopy() {
		TmfEventContent content  = fBasicContent.clone();
		assertSame("getLabels",  fEvent,      content.getEvent());
		assertSame("getType",    fEventType,  content.getType());
		assertSame("getContent", fRawContent, content.getContent());
	}

	public void testCloneDeepCopy() {
		TmfEventContent content  = fStubContent.clone();
		assertSame   ("getEvent",   fEventStub,     content.getEvent());
		assertSame   ("getType",    fEventTypeStub, content.getType());
		assertNotSame("getContent", fRawContent,    content.getContent());
		assertEquals ("getContent", fRawContent,    content.getContent());
	}

	// ========================================================================
	// Basic content parsing
	// ========================================================================

	public void testGetFields() {
		Object[] fields = fBasicContent.getFields(); 
		assertEquals("getFields", 1, fields.length);
		assertEquals("getFields", fRawContent, fields[0].toString());
	}

	public void testGetFieldFromId() {
		Object field;
		try {
			field = fBasicContent.getField("Content");
			assertEquals("getField", fRawContent, field.toString());
		} catch (TmfNoSuchFieldException e) {
			fail("Field not found");
		} 
	}

	public void testGetFieldFromIdFailed() {
		try {
			fBasicContent.getField("Dummy");
			fail("Found an inexisting field...");
		} catch (TmfNoSuchFieldException e) {
			// Success
		} 
	}

	public void testGetFieldFromPos() {
		Object field = fBasicContent.getField(0);
		assertEquals("getField", fRawContent, field.toString());
	}

	// ========================================================================
	// Standard content parsing
	// ========================================================================

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
		TmfEventContentStub content = new TmfEventContentStub(fEvent, fRawContent);

		Object field = content.getField(0);
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

	// ========================================================================
	// Operators
	// ========================================================================

	public void testToString() {
		String expected = "[TmfEventContent(" + fRawContent + ",)]";
		TmfEventContent content = new TmfEventContent(fEvent, fRawContent);
		assertEquals("toString", expected, content.toString());
	}

}
