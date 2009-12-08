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
 * <b><u>TmfEventReferenceTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventReferenceTest extends TestCase {

	private final Object fReference = new String("Some reference");

	// ========================================================================
	// Housekeeping
	// ========================================================================

	public TmfEventReferenceTest(String name) {
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

	public void testTmfEventReferenceDefault() {
		TmfEventReference reference = new TmfEventReference();
		assertEquals("getReference", null, reference.getReference());
	}

	public void testTmfEventReference() {
		TmfEventReference reference = new TmfEventReference(fReference);
		assertSame("getReference", fReference, reference.getReference());
	}

	public void testTmfEventReferenceCopy() {
		TmfEventReference original = new TmfEventReference(fReference);
		TmfEventReference reference = new TmfEventReference(original);
		assertSame("getReference", fReference, reference.getReference());
	}

	public void testCloneShallowCopy() {
		TmfEventReference original = new TmfEventReference(fReference);
		TmfEventReference reference = original.clone();
		assertSame("getReference", fReference, reference.getReference());
	}

//	public void testCloneDeepCopy() {
//		TmfEventReference original = new TmfEventReference(fReference);
//		TmfEventReference reference = original.clone();
//		assertNotSame("getReference", fReference, reference.getReference());
//		assertEquals ("getReference", fReference, reference.getReference());
//	}

	// ========================================================================
	// Operators
	// ========================================================================

	public void testToString() {
		String expected1 = "[TmfEventReference(" + "null" + ")]";
		TmfEventReference reference1 = new TmfEventReference();
		assertEquals("toString", expected1, reference1.toString());

		String expected2 = "[TmfEventReference(" + fReference.toString() + ")]";
		TmfEventReference reference2 = new TmfEventReference(fReference);
		assertEquals("toString", expected2, reference2.toString());
	}

}
