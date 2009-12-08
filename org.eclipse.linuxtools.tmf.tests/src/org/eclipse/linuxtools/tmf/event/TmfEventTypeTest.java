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

package org.eclipse.linuxtools.tmf.event;

import junit.framework.TestCase;

/**
 * <b><u>TmfEventTypeTest</u></b>
 * <p>
 * JUnit test suite for the TmfEventType class.
 */
public class TmfEventTypeTest extends TestCase {

	private final String   fTypeId = "Some type";
	private final String   fLabel0 = "label1";
	private final String   fLabel1 = "label2";
	private final String[] fLabels = new String[] { fLabel0, fLabel1 };

	// ========================================================================
	// Housekeeping
	// ========================================================================

	public TmfEventTypeTest(String name) {
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

	public void testTmfEventTypeDefault() {
		TmfEventType type = new TmfEventType();
		try {
			assertEquals("getTypeId",     TmfEventType.DEFAULT_TYPE_ID, type.getTypeId());
			assertEquals("getNbFields",   1, type.getNbFields());
			assertEquals("getFieldIndex", 0, type.getFieldIndex(TmfEventType.DEFAULT_LABELS[0]));
			assertEquals("getLabels",     TmfEventType.DEFAULT_LABELS, type.getLabels());
			assertEquals("getLabel",      TmfEventType.DEFAULT_LABELS[0], type.getLabel(0));
		} catch (TmfNoSuchFieldException e) {
			fail("getFieldIndex: no such field");
		}
	}

	public void testTmfEventType() {
		TmfEventType type = new TmfEventType(fTypeId, fLabels);
		try {
			assertEquals("getTypeId",     fTypeId, type.getTypeId());
			assertEquals("getNbFields",   fLabels.length, type.getNbFields());
			assertEquals("getFieldIndex", 0, type.getFieldIndex(fLabel0));
			assertEquals("getFieldIndex", 1, type.getFieldIndex(fLabel1));
			assertEquals("getLabels",     fLabels, type.getLabels());
			assertEquals("getLabel",      fLabel0, type.getLabel(0));
			assertEquals("getLabel",      fLabel1, type.getLabel(1));
		} catch (TmfNoSuchFieldException e) {
			fail("getFieldIndex: no such field");
		}

		try {
			assertEquals("getFieldIndex", 0, type.getFieldIndex("Dummy"));
			fail("getFieldIndex: inexistant field");
		} catch (TmfNoSuchFieldException e) {
			// Success
		}
	}

	public void testTmfEventTypeCopy() {
		TmfEventType original = new TmfEventType(fTypeId, fLabels);
		TmfEventType type = new TmfEventType(original);
		assertEquals("getTypeId",     fTypeId, type.getTypeId());
		assertEquals("getNbFields",   fLabels.length, type.getNbFields());
		assertEquals("getLabels",     fLabels, type.getLabels());
	}

	public void testCloneShallowCopy() {
		TmfEventType original = new TmfEventType(fTypeId, fLabels);
		TmfEventType type = original.clone();
		assertEquals("getTypeId",     fTypeId, type.getTypeId());
		assertEquals("getNbFields",   fLabels.length, type.getNbFields());
		assertEquals("getLabels",     fLabels, type.getLabels());
	}

//	public void testCloneDeepCopy() {
//		fail("Not yet implemented");
//	}

	// ========================================================================
	// Operators
	// ========================================================================

	public void testToString() {
		String expected1 = "[TmfEventType:" + TmfEventType.DEFAULT_TYPE_ID + "]";
		TmfEventType type1 = new TmfEventType();
		assertEquals("toString", expected1, type1.toString());

		String expected2 = "[TmfEventType:" + fTypeId + "]";
		TmfEventType type2 = new TmfEventType(fTypeId, fLabels);
		assertEquals("toString", expected2, type2.toString());
	}

}
