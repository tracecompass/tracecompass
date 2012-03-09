/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * <b><u>TmfCheckpointTest</u></b>
 * <p>
 * Test suite for the TmfCheckpoint class.
 */
@SuppressWarnings("nls")
public class TmfCheckpointTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	ITmfTimestamp fTimestamp1 = new TmfTimestamp();
	ITmfTimestamp fTimestamp2 = TmfTimestamp.BIG_BANG;
	ITmfTimestamp fTimestamp3 = TmfTimestamp.BIG_CRUNCH;

	Long aLong1 = 12345L;
	Long aLong2 = 23456L;
	Long aLong3 = 34567L;
	TmfLocation<Long> fLocation1 = new TmfLocation<Long>(aLong1);
	TmfLocation<Long> fLocation2 = new TmfLocation<Long>(aLong2);
	TmfLocation<Long> fLocation3 = new TmfLocation<Long>(aLong3);

	TmfCheckpoint fCheckpoint1 = new TmfCheckpoint(fTimestamp1, fLocation1);
	TmfCheckpoint fCheckpoint2 = new TmfCheckpoint(fTimestamp2, fLocation2);
	TmfCheckpoint fCheckpoint3 = new TmfCheckpoint(fTimestamp3, fLocation3);
	
    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfCheckpointTest(String name) {
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

	public void testTmfCheckpoint() {
		assertEquals("TmfCheckpoint", fTimestamp1, fCheckpoint1.getTimestamp());
		assertEquals("TmfCheckpoint", fLocation1,  fCheckpoint1.getLocation());
	}

	public void testTmfLocationCopy() {
		TmfCheckpoint checkpoint = new TmfCheckpoint(fCheckpoint1);

		assertEquals("TmfCheckpoint", fTimestamp1, checkpoint.getTimestamp());
		assertEquals("TmfCheckpoint", fLocation1,  checkpoint.getLocation());
	}

	public void testTmfLocationCopy2() throws Exception {
		try {
			new TmfCheckpoint(null);
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
		assertTrue("equals", fCheckpoint1.equals(fCheckpoint1));
		assertTrue("equals", fCheckpoint2.equals(fCheckpoint2));

		assertTrue("equals", !fCheckpoint1.equals(fCheckpoint2));
		assertTrue("equals", !fCheckpoint2.equals(fCheckpoint1));
	}
	
	public void testEqualsSymmetry() throws Exception {
		TmfCheckpoint checkpoint1 = new TmfCheckpoint(fCheckpoint1);
		TmfCheckpoint checkpoint2 = new TmfCheckpoint(fCheckpoint2);

		assertTrue("equals", checkpoint1.equals(fCheckpoint1));
		assertTrue("equals", fCheckpoint1.equals(checkpoint1));

		assertTrue("equals", checkpoint2.equals(fCheckpoint2));
		assertTrue("equals", fCheckpoint2.equals(checkpoint2));
	}
	
	public void testEqualsTransivity() throws Exception {
		TmfCheckpoint checkpoint1 = new TmfCheckpoint(fCheckpoint1);
		TmfCheckpoint checkpoint2 = new TmfCheckpoint(checkpoint1);
		TmfCheckpoint checkpoint3 = new TmfCheckpoint(checkpoint2);

		assertTrue("equals", checkpoint1.equals(checkpoint2));
		assertTrue("equals", checkpoint2.equals(checkpoint3));
		assertTrue("equals", checkpoint1.equals(checkpoint3));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !fCheckpoint1.equals(null));
		assertTrue("equals", !fCheckpoint2.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		TmfCheckpoint checkpoint1 = new TmfCheckpoint(fCheckpoint1);
		TmfCheckpoint checkpoint2 = new TmfCheckpoint(fCheckpoint2);

		assertTrue("hashCode", fCheckpoint1.hashCode() == checkpoint1.hashCode());
		assertTrue("hashCode", fCheckpoint2.hashCode() == checkpoint2.hashCode());

		assertTrue("hashCode", fCheckpoint1.hashCode() != checkpoint2.hashCode());
		assertTrue("hashCode", fCheckpoint2.hashCode() != checkpoint1.hashCode());
	}
	
    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

	public void testToString() {
		String expected1 = "[TmfCheckpoint(" + fCheckpoint1.getTimestamp() +  "," + 
			fCheckpoint1.getLocation() + ")]";
		String expected2 = "[TmfCheckpoint(" + fCheckpoint2.getTimestamp() +  "," + 
			fCheckpoint2.getLocation() + ")]";
		String expected3 = "[TmfCheckpoint(" + fCheckpoint3.getTimestamp() +  "," + 
			fCheckpoint3.getLocation() + ")]";

		assertEquals("toString", expected1, fCheckpoint1.toString());
		assertEquals("toString", expected2, fCheckpoint2.toString());
		assertEquals("toString", expected3, fCheckpoint3.toString());
	}

    // ------------------------------------------------------------------------
    // compareTo
    // ------------------------------------------------------------------------

	public void testCompareTo() {
		assertEquals("compareTo",  0, fCheckpoint1.compareTo(fCheckpoint1));
		assertEquals("compareTo",  1, fCheckpoint1.compareTo(fCheckpoint2));
		assertEquals("compareTo", -1, fCheckpoint1.compareTo(fCheckpoint3));

		assertEquals("compareTo", -1, fCheckpoint2.compareTo(fCheckpoint1));
		assertEquals("compareTo",  0, fCheckpoint2.compareTo(fCheckpoint2));
		assertEquals("compareTo", -1, fCheckpoint2.compareTo(fCheckpoint3));

		assertEquals("compareTo",  1, fCheckpoint3.compareTo(fCheckpoint1));
		assertEquals("compareTo",  1, fCheckpoint3.compareTo(fCheckpoint2));
		assertEquals("compareTo",  0, fCheckpoint3.compareTo(fCheckpoint3));
	}

}
