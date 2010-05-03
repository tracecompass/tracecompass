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

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.event.TmfEventSource;

/**
 * <b><u>TmfEventSourceTest</u></b>
 * <p>
 * Test suite for the TmfEventSource class.
 */
public class TmfEventSourceTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private final Object source1 = new String("Some source");
	private final Object source2 = new String("Some other source");

	private final TmfEventSource fSource0 = new TmfEventSource(source1);
	private final TmfEventSource fSource1 = new TmfEventSource(source1);
	private final TmfEventSource fSource2 = new TmfEventSource(source1);
	private final TmfEventSource fSource3 = new TmfEventSource(source2);
	
	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfEventSourceTest(String name) {
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

	public void testTmfEventSourceDefault() {
		TmfEventSource source = new TmfEventSource();
		assertEquals("getSourceId", null, source.getSourceId());
	}

	public void testTmfEventSource() {
		TmfEventSource source = new TmfEventSource(source1);
		assertSame("getSourceId", source1, source.getSourceId());
	}

	public void testTmfEventSourceCopy() {
		TmfEventSource original = new TmfEventSource(source1);
		TmfEventSource source = new TmfEventSource(original);
		assertSame("getSourceId", source1, source.getSourceId());
	}

	public void testTmfEventSourceCopy2() {
		try {
			@SuppressWarnings("unused")
			TmfEventSource source = new TmfEventSource(null);
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
		assertTrue("equals", fSource0.equals(fSource0));
		assertTrue("equals", fSource3.equals(fSource3));

		assertTrue("equals", !fSource0.equals(fSource3));
		assertTrue("equals", !fSource3.equals(fSource0));
	}
	
	public void testEqualsSymmetry() throws Exception {
		assertTrue("equals", fSource0.equals(fSource2));
		assertTrue("equals", fSource2.equals(fSource0));

		assertTrue("equals", !fSource0.equals(fSource3));
		assertTrue("equals", !fSource3.equals(fSource0));
	}
	
	public void testEqualsTransivity() throws Exception {
		assertTrue("equals", fSource0.equals(fSource1));
		assertTrue("equals", fSource1.equals(fSource2));
		assertTrue("equals", fSource0.equals(fSource2));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !fSource0.equals(null));
		assertTrue("equals", !fSource3.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		assertTrue("hashCode", fSource0.hashCode() == fSource1.hashCode());
		assertTrue("hashCode", fSource0.hashCode() != fSource3.hashCode());
	}
	
	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	public void testToString() {
		String expected1 = "[TmfEventSource(" + "null" + ")]";
		TmfEventSource nullSource = new TmfEventSource();
		assertEquals("toString", expected1, nullSource.toString());

		String expected2 = "[TmfEventSource(" + source1.toString() + ")]";
		TmfEventSource source = new TmfEventSource(source1);
		assertEquals("toString", expected2, source.toString());
	}

}
