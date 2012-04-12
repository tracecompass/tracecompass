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
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.core.trace.TmfLocation;

/**
 * <b><u>TmfContextTest</u></b>
 * <p>
 * Test suite for the TmfContext class.
 */
@SuppressWarnings("nls")
public class TmfContextTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	final String       aString    = "some location";
	final Long         aLong      = 12345L;
	final TmfTimestamp aTimestamp = new TmfTimestamp();

	final TmfLocation<String> fLocation1 = new TmfLocation<String>(aString);
	final TmfLocation<Long>   fLocation2 = new TmfLocation<Long>(aLong);
	final TmfLocation<ITmfTimestamp> fLocation3 = new TmfLocation<ITmfTimestamp>(aTimestamp);

	final long fRank1 = 1;
	final long fRank2 = 2;
	final long fRank3 = 3;
	
	final TmfContext fContext1 = new TmfContext(fLocation1, fRank1);
	final TmfContext fContext2 = new TmfContext(fLocation2, fRank2);
	final TmfContext fContext3 = new TmfContext(fLocation3, fRank3);
	
    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfContextTest(String name) {
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

	public void testTmfContextDefault() {
		TmfContext context = new TmfContext();
		assertEquals("getLocation", null, context.getLocation());
		assertEquals("getRank", TmfContext.UNKNOWN_RANK, context.getRank());
	}

	public void testTmfContextNoRank() {
		TmfContext context1 = new TmfContext(fLocation1);
		TmfContext context2 = new TmfContext(fLocation2);
		TmfContext context3 = new TmfContext(fLocation3);

		assertEquals("getLocation", fLocation1, context1.getLocation());
		assertEquals("getLocation", fLocation2, context2.getLocation());
		assertEquals("getLocation", fLocation3, context3.getLocation());

		assertEquals("getRank", TmfContext.UNKNOWN_RANK, context1.getRank());
		assertEquals("getRank", TmfContext.UNKNOWN_RANK, context2.getRank());
		assertEquals("getRank", TmfContext.UNKNOWN_RANK, context3.getRank());
	}

	public void testTmfContext() {
		assertEquals("getLocation", fLocation1, fContext1.getLocation());
		assertEquals("getLocation", fLocation2, fContext2.getLocation());
		assertEquals("getLocation", fLocation3, fContext3.getLocation());

		assertEquals("getRank", fRank1, fContext1.getRank());
		assertEquals("getRank", fRank2, fContext2.getRank());
		assertEquals("getRank", fRank3, fContext3.getRank());
	}

	public void testTmfContextCopy() {
		TmfContext context1 = new TmfContext(fContext1);
		TmfContext context2 = new TmfContext(fContext2);
		TmfContext context3 = new TmfContext(fContext3);

		assertEquals("getLocation", fLocation1, context1.getLocation());
		assertEquals("getLocation", fLocation2, context2.getLocation());
		assertEquals("getLocation", fLocation3, context3.getLocation());

		assertEquals("getRank", fRank1, context1.getRank());
		assertEquals("getRank", fRank2, context2.getRank());
		assertEquals("getRank", fRank3, context3.getRank());
	}

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
		assertTrue("equals", fContext1.equals(fContext1));
		assertTrue("equals", fContext2.equals(fContext2));

		assertTrue("equals", !fContext1.equals(fContext2));
		assertTrue("equals", !fContext2.equals(fContext1));
	}
	
	public void testEqualsSymmetry() throws Exception {
		TmfContext context1 = new TmfContext(fContext1);
		TmfContext context2 = new TmfContext(fContext2);

		assertTrue("equals", context1.equals(fContext1));
		assertTrue("equals", fContext1.equals(context1));

		assertTrue("equals", context2.equals(fContext2));
		assertTrue("equals", fContext2.equals(context2));
	}
	
	public void testEqualsTransivity() throws Exception {
		TmfContext context1 = new TmfContext(fContext1);
		TmfContext context2 = new TmfContext(context1);
		TmfContext context3 = new TmfContext(context2);

		assertTrue("equals", context1.equals(context2));
		assertTrue("equals", context2.equals(context3));
		assertTrue("equals", context1.equals(context3));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !fContext1.equals(null));
		assertTrue("equals", !fContext2.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		TmfContext context1 = new TmfContext(fContext1);
		TmfContext context2 = new TmfContext(fContext2);

		assertTrue("hashCode", fContext1.hashCode() == context1.hashCode());
		assertTrue("hashCode", fContext2.hashCode() == context2.hashCode());

		assertTrue("hashCode", fContext1.hashCode() != context2.hashCode());
		assertTrue("hashCode", fContext2.hashCode() != context1.hashCode());
	}
	
    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

	public void testToString() {
		String expected1 = "[TmfContext(" + fLocation1 + "," + 1 + ")]";
		String expected2 = "[TmfContext(" + fLocation2 + "," + 2 + ")]";
		String expected3 = "[TmfContext(" + fLocation3 + "," + 3 + ")]";

		assertEquals("toString", expected1, fContext1.toString());
		assertEquals("toString", expected2, fContext2.toString());
		assertEquals("toString", expected3, fContext3.toString());
	}

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

	public void testClone() {
		try {
			TmfContext context1 = fContext1.clone();
			TmfContext context2 = fContext2.clone();
			TmfContext context3 = fContext3.clone();

			assertEquals("clone", context1, fContext1);
			assertEquals("clone", context2, fContext2);
			assertEquals("clone", context3, fContext3);
		}
		catch (InternalError e) {
			fail("clone()");
		}
	}

    // ------------------------------------------------------------------------
    // setLocation, setRank, updateRank
    // ------------------------------------------------------------------------

	public void testSetLocation() {
		TmfContext context1 = new TmfContext(fContext1);
		context1.setLocation(fContext2.getLocation());

		assertEquals("getLocation", fLocation2, context1.getLocation());
		assertEquals("getRank", 1, context1.getRank());
	}

	public void testSetRank() {
		TmfContext context1 = new TmfContext(fContext1);
		context1.setRank(fContext2.getRank());

		assertEquals("getLocation", fLocation1, context1.getLocation());
		assertEquals("getRank", fRank2, context1.getRank());
	}

	public void testUpdatetRank() {
		TmfContext context1 = new TmfContext(fContext1);

		context1.updateRank(0);
		assertEquals("getRank", fRank1, context1.getRank());

		context1.updateRank(-1);
		assertEquals("getRank", fRank1 - 1, context1.getRank());

		context1.updateRank(2);
		assertEquals("getRank", fRank1 + 1, context1.getRank());
	}

}
