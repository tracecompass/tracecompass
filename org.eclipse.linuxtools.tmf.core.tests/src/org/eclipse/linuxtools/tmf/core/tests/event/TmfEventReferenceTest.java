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

import org.eclipse.linuxtools.tmf.core.event.TmfEventReference;

/**
 * <b><u>TmfEventReferenceTest</u></b>
 * <p>
 * Test suite for the TmfEventReference class.
 */
@SuppressWarnings("nls")
public class TmfEventReferenceTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private final Object reference0 = new String("Some reference");
	private final Object reference2 = new String("Some other reference");

	private TmfEventReference fReference0 = new TmfEventReference(reference0);
	private TmfEventReference fReference1 = new TmfEventReference(reference0);
	private TmfEventReference fReference2 = new TmfEventReference(reference0);
	private TmfEventReference fReference3 = new TmfEventReference(reference2);

	// ------------------------------------------------------------------------
	// Housekeeping
    // ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
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

    // ------------------------------------------------------------------------
	// Constructors
    // ------------------------------------------------------------------------

	public void testTmfEventReferenceDefault() {
		TmfEventReference reference = new TmfEventReference();
		assertEquals("getReference", null, reference.getReference());
	}

	public void testTmfEventReference() {
		TmfEventReference reference = new TmfEventReference(reference0);
		assertSame("getReference", reference0, reference.getReference());
	}

	public void testTmfEventReferenceCopy() {
		TmfEventReference original = new TmfEventReference(reference0);
		TmfEventReference reference = new TmfEventReference(original);
		assertSame("getReference", reference0, reference.getReference());
	}

	public void testTmfEventReferenceCopy2() {
		try {
			@SuppressWarnings("unused")
			TmfEventReference reference = new TmfEventReference(null);
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
		assertTrue("equals", fReference0.equals(fReference0));
		assertTrue("equals", fReference3.equals(fReference3));

		assertTrue("equals", !fReference0.equals(fReference3));
		assertTrue("equals", !fReference3.equals(fReference0));
	}
	
	public void testEqualsSymmetry() throws Exception {
		assertTrue("equals", fReference0.equals(fReference1));
		assertTrue("equals", fReference1.equals(fReference0));

		assertTrue("equals", !fReference0.equals(fReference3));
		assertTrue("equals", !fReference3.equals(fReference0));
	}
	
	public void testEqualsTransivity() throws Exception {
		assertTrue("equals", fReference0.equals(fReference1));
		assertTrue("equals", fReference1.equals(fReference2));
		assertTrue("equals", fReference0.equals(fReference2));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !fReference0.equals(null));
		assertTrue("equals", !fReference3.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		assertTrue("hashCode", fReference0.hashCode() == fReference1.hashCode());
		assertTrue("hashCode", fReference0.hashCode() != fReference3.hashCode());
	}
	
	public void testHashCode2() throws Exception {
		TmfEventReference reference0 = new TmfEventReference();
		assertTrue("hashCode", fReference0.hashCode() != reference0.hashCode());
		assertTrue("hashCode", fReference3.hashCode() != reference0.hashCode());
	}
	
    // ------------------------------------------------------------------------
	// toString
    // ------------------------------------------------------------------------

	public void testToString() {
		String expected1 = "[TmfEventReference(" + "null" + ")]";
		TmfEventReference reference1 = new TmfEventReference();
		assertEquals("toString", expected1, reference1.toString());

		String expected2 = "[TmfEventReference(" + reference0.toString() + ")]";
		TmfEventReference reference2 = new TmfEventReference(reference0);
		assertEquals("toString", expected2, reference2.toString());
	}

}
