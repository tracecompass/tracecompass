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

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * <b><u>TmfTimestampTest</u></b>
 * <p>
 * Test suite for the TmfTimestamp class.
 */
@SuppressWarnings("nls")
public class TmfTimestampTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private final TmfTimestamp ts0 = new TmfTimestamp();
	private final TmfTimestamp ts1 = new TmfTimestamp(12345);
	private final TmfTimestamp ts2 = new TmfTimestamp(12345, -1);
	private final TmfTimestamp ts3 = new TmfTimestamp(12345,  2, 5);

	private final TmfTimestamp ts0copy = new TmfTimestamp();
	private final TmfTimestamp ts1copy = new TmfTimestamp(12345);

	private final TmfTimestamp ts0copy2 = new TmfTimestamp();
	private final TmfTimestamp ts1copy2 = new TmfTimestamp(12345);

	private final TmfTimestamp bigBang   = new TmfTimestamp(TmfTimestamp.BigBang);
	private final TmfTimestamp bigCrunch = new TmfTimestamp(TmfTimestamp.BigCrunch);

	// ------------------------------------------------------------------------
	// Housekeping
	// ------------------------------------------------------------------------

	/**
	 * @param name the test name
	 */
	public TmfTimestampTest(String name) {
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

	public void testDefaultConstructor() throws Exception {
		assertEquals("getValue",     0, ts0.getValue());
		assertEquals("getscale",     0, ts0.getScale());
		assertEquals("getPrecision", 0, ts0.getPrecision());
	}

	public void testSimpleConstructor() throws Exception {
		assertEquals("getValue", 12345, ts1.getValue());
		assertEquals("getscale", 0,     ts1.getScale());
		assertEquals("getPrecision", 0, ts1.getPrecision());
	}

	public void testSimpleConstructor2() throws Exception {
		assertEquals("getValue", 12345, ts2.getValue());
		assertEquals("getscale", -1,    ts2.getScale());
		assertEquals("getPrecision", 0, ts2.getPrecision());
	}

	public void testFullConstructor() throws Exception {
		assertEquals("getValue", 12345, ts3.getValue());
		assertEquals("getscale", 2,     ts3.getScale());
		assertEquals("getPrecision", 5, ts3.getPrecision());
	}

	public void testCopyConstructor() throws Exception {
		TmfTimestamp ts0 = new TmfTimestamp(12345, 2, 5);
		TmfTimestamp ts = new TmfTimestamp(ts0);
		assertEquals("getValue", 12345, ts.getValue());
		assertEquals("getscale", 2, ts.getScale());
		assertEquals("getPrecision", 5, ts.getPrecision());
	}

	public void testCopyConstructor2() throws Exception {
		try {
			@SuppressWarnings("unused")
			TmfTimestamp timestamp = new TmfTimestamp(null);
			fail("null copy");
		}
		catch (IllegalArgumentException e) {
			// Success
		}
	}

	public void testCopyConstructorBigBang() throws Exception {
		assertEquals("getValue", TmfTimestamp.BigBang.getValue(), bigBang.getValue());
		assertEquals("getscale", TmfTimestamp.BigBang.getScale(), bigBang.getScale());
		assertEquals("getPrecision", TmfTimestamp.BigBang.getPrecision(), bigBang.getPrecision());
	}

	public void testCopyConstructorBigCrunch() throws Exception {
		assertEquals("getValue", TmfTimestamp.BigCrunch.getValue(), bigCrunch.getValue());
		assertEquals("getscale", TmfTimestamp.BigCrunch.getScale(), bigCrunch.getScale());
		assertEquals("getPrecision", TmfTimestamp.BigCrunch.getPrecision(), bigCrunch.getPrecision());
	}

	// ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
		assertTrue("equals", ts0.equals(ts0));
		assertTrue("equals", ts1.equals(ts1));

		assertTrue("equals", !ts0.equals(ts1));
		assertTrue("equals", !ts1.equals(ts0));
	}
	
	public void testEqualsSymmetry() throws Exception {
		assertTrue("equals", ts0.equals(ts0copy));
		assertTrue("equals", ts0copy.equals(ts0));

		assertTrue("equals", ts1.equals(ts1copy));
		assertTrue("equals", ts1copy.equals(ts1));
	}
	
	public void testEqualsTransivity() throws Exception {
		assertTrue("equals", ts0.equals(ts0copy));
		assertTrue("equals", ts0copy.equals(ts0copy2));
		assertTrue("equals", ts0.equals(ts0copy2));
		
		assertTrue("equals", ts1.equals(ts1copy));
		assertTrue("equals", ts1copy.equals(ts1copy2));
		assertTrue("equals", ts1.equals(ts1copy2));
	}
	
	public void testEqualsNull() throws Exception {
		assertTrue("equals", !ts0.equals(null));
		assertTrue("equals", !ts1.equals(null));
	}
	
	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
		assertTrue("hashCode", ts0.hashCode() == ts0copy.hashCode());
		assertTrue("hashCode", ts1.hashCode() == ts1copy.hashCode());

		assertTrue("hashCode", ts0.hashCode() != ts1.hashCode());
	}
	
	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	public void testToString() throws Exception {
		assertEquals("toString", "TmfTimestamp [fValue=0, fScale=0, fPrecision=0]",      ts0.toString());
		assertEquals("toString", "TmfTimestamp [fValue=12345, fScale=0, fPrecision=0]",  ts1.toString());
		assertEquals("toString", "TmfTimestamp [fValue=12345, fScale=-1, fPrecision=0]", ts2.toString());
		assertEquals("toString", "TmfTimestamp [fValue=12345, fScale=2, fPrecision=5]",  ts3.toString());
	}

	// ------------------------------------------------------------------------
	// clone
	// ------------------------------------------------------------------------

	public class MyTimestamp extends TmfTimestamp {
		@Override
		public boolean equals(Object other) {
			return super.equals(other);
		}
		@Override
		public MyTimestamp clone() {
			return (MyTimestamp) super.clone();
		}
	}

	public void testClone() throws Exception {
		TmfTimestamp timestamp = ts0.clone();
		assertEquals("clone", timestamp, ts0);
	}

	public void testClone2() throws Exception {
		MyTimestamp timestamp = new MyTimestamp();
		MyTimestamp clone = timestamp.clone();
		assertEquals("clone", clone, timestamp);
	}

	// ------------------------------------------------------------------------
	// normalize
	// ------------------------------------------------------------------------

	public void testNormalizeOffset() throws Exception {

		ITmfTimestamp ts = ts0.normalize(0, 0);
		assertEquals("getValue",     0, ts.getValue());
		assertEquals("getscale",     0, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.normalize(12345, 0);
		assertEquals("getValue", 12345, ts.getValue());
		assertEquals("getscale",     0, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.normalize(10, 0);
		assertEquals("getValue",    10, ts.getValue());
		assertEquals("getscale",     0, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.normalize(-10, 0);
		assertEquals("getValue",   -10, ts.getValue());
		assertEquals("getscale",     0, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());
	}

	public void testNormalizeScale() throws Exception {

		ITmfTimestamp ts = ts0.normalize(0, 10);
		assertEquals("getValue",     0, ts.getValue());
		assertEquals("getscale",    10, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.normalize(0, -10);
		assertEquals("getValue",     0, ts.getValue());
		assertEquals("getscale",   -10, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());
	}

	public void testNormalizeOffsetAndScale() throws Exception {
		int SCALE = 12;

		ITmfTimestamp ts = ts0.normalize(0, SCALE);
		assertEquals("getValue",     0, ts.getValue());
		assertEquals("getscale", SCALE, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.normalize(12345, SCALE);
		assertEquals("getValue", 12345, ts.getValue());
		assertEquals("getscale", SCALE, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.normalize(10, SCALE);
		assertEquals("getValue",    10, ts.getValue());
		assertEquals("getscale", SCALE, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());

		ts = ts0.normalize(-10, SCALE);
		assertEquals("getValue",   -10, ts.getValue());
		assertEquals("getscale", SCALE, ts.getScale());
		assertEquals("getPrecision", 0, ts.getPrecision());
	}	
	
	// ------------------------------------------------------------------------
	// compareTo
	// ------------------------------------------------------------------------

	public void testCompareToSameScale() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(900,  0, 50);
		TmfTimestamp ts2 = new TmfTimestamp(1000, 0, 50);
		TmfTimestamp ts3 = new TmfTimestamp(1100, 0, 50);
		TmfTimestamp ts4 = new TmfTimestamp(1000, 0, 75);

		assertTrue(ts1.compareTo(ts1, false) == 0);

		assertTrue("CompareTo", ts1.compareTo(ts2, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts4, false) < 0);

		assertTrue("CompareTo", ts2.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts2.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts2.compareTo(ts4, false) == 0);

		assertTrue("CompareTo", ts3.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts2, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts4, false) > 0);
	}

	public void testCompareToDifferentScale() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(9000, -1, 50);
		TmfTimestamp ts2 = new TmfTimestamp(1000,  0, 50);
		TmfTimestamp ts3 = new TmfTimestamp(110,   1, 50);
		TmfTimestamp ts4 = new TmfTimestamp(1,     3, 75);

		assertTrue("CompareTo", ts1.compareTo(ts1, false) == 0);

		assertTrue("CompareTo", ts1.compareTo(ts2, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts4, false) < 0);

		assertTrue("CompareTo", ts2.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts2.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts2.compareTo(ts4, false) == 0);

		assertTrue("CompareTo", ts3.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts2, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts4, false) > 0);
	}

	public void testCompareToWithinPrecision() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(900,  0, 50);
		TmfTimestamp ts2 = new TmfTimestamp(1000, 0, 50);
		TmfTimestamp ts3 = new TmfTimestamp(1100, 0, 50);
		TmfTimestamp ts4 = new TmfTimestamp(1000, 0, 75);

		assertTrue("CompareTo", ts1.compareTo(ts1, true) == 0);

		assertTrue("CompareTo", ts1.compareTo(ts2, true) == 0);
		assertTrue("CompareTo", ts1.compareTo(ts3, true) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts4, true) == 0);

		assertTrue("CompareTo", ts2.compareTo(ts1, true) == 0);
		assertTrue("CompareTo", ts2.compareTo(ts3, true) == 0);
		assertTrue("CompareTo", ts2.compareTo(ts4, true) == 0);

		assertTrue("CompareTo", ts3.compareTo(ts1, true) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts2, true) == 0);
		assertTrue("CompareTo", ts3.compareTo(ts4, true) == 0);
	}

	public void testCompareToLargeScale() throws Exception {
		TmfTimestamp ts1 = new TmfTimestamp(-1,     100);
		TmfTimestamp ts2 = new TmfTimestamp(-1000, -100);
		TmfTimestamp ts3 = new TmfTimestamp(1,      100);
		TmfTimestamp ts4 = new TmfTimestamp(1000,  -100);

		assertTrue("CompareTo", ts1.compareTo(ts2, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts1.compareTo(ts4, false) < 0);

		assertTrue("CompareTo", ts2.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts2.compareTo(ts3, false) < 0);
		assertTrue("CompareTo", ts2.compareTo(ts4, false) < 0);

		assertTrue("CompareTo", ts3.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts2, false) > 0);
		assertTrue("CompareTo", ts3.compareTo(ts4, false) > 0);

		assertTrue("CompareTo", ts4.compareTo(ts1, false) > 0);
		assertTrue("CompareTo", ts4.compareTo(ts2, false) > 0);
		assertTrue("CompareTo", ts4.compareTo(ts3, false) < 0);
	}

	public void testCompareToBigRanges() throws Exception {
		TmfTimestamp ts0a = new TmfTimestamp( 0, Integer.MAX_VALUE);
		TmfTimestamp ts0b = new TmfTimestamp( 0, Integer.MIN_VALUE);
		TmfTimestamp ts1  = new TmfTimestamp(-1, Integer.MAX_VALUE);
		TmfTimestamp ts2  = new TmfTimestamp(-1, Integer.MIN_VALUE);
		TmfTimestamp ts3  = new TmfTimestamp( 1, Integer.MAX_VALUE);
		TmfTimestamp ts4  = new TmfTimestamp( 1, Integer.MIN_VALUE);

		assertEquals("CompareTo",  1, ts0a.compareTo(TmfTimestamp.BigBang,   false));
		assertEquals("CompareTo", -1, ts0a.compareTo(TmfTimestamp.BigCrunch, false));

		assertEquals("CompareTo",  1, ts0b.compareTo(TmfTimestamp.BigBang,   false));
		assertEquals("CompareTo", -1, ts0b.compareTo(TmfTimestamp.BigCrunch, false));

		assertEquals("CompareTo",  0, ts0a.compareTo(ts0b, false));
		assertEquals("CompareTo",  0, ts0b.compareTo(ts0a, false));

		assertEquals("CompareTo",  1, ts0a.compareTo(TmfTimestamp.BigBang,   false));
		assertEquals("CompareTo", -1, ts0a.compareTo(TmfTimestamp.BigCrunch, false));

		assertEquals("CompareTo",  1, ts1.compareTo(TmfTimestamp.BigBang,   false));
		assertEquals("CompareTo", -1, ts1.compareTo(TmfTimestamp.BigCrunch, false));

		assertEquals("CompareTo",  1, ts2.compareTo(TmfTimestamp.BigBang,   false));
		assertEquals("CompareTo", -1, ts2.compareTo(TmfTimestamp.BigCrunch, false));

		assertEquals("CompareTo",  1, ts3.compareTo(TmfTimestamp.BigBang,   false));
		assertEquals("CompareTo", -1, ts3.compareTo(TmfTimestamp.BigCrunch, false));

		assertEquals("CompareTo",  1, ts4.compareTo(TmfTimestamp.BigBang,   false));
		assertEquals("CompareTo", -1, ts4.compareTo(TmfTimestamp.BigCrunch, false));
	}

    // ------------------------------------------------------------------------
    // getDelta
    // ------------------------------------------------------------------------
	
	public void testDelta() throws Exception {
	    // Delta for same scale and precision (delta > 0)
	    TmfTimestamp ts0 = new TmfTimestamp(10, 9);
	    TmfTimestamp ts1 = new TmfTimestamp(5,  9);
	    TmfTimestamp exp = new TmfTimestamp(5,  9);
	    
	    ITmfTimestamp delta = ts0.getDelta(ts1);
	    assertEquals("getDelta", 0, delta.compareTo(exp, false));
	    
	    // Delta for same scale and precision (delta < 0)
	    ts0 = new TmfTimestamp( 5, 9);
	    ts1 = new TmfTimestamp(10, 9);
	    exp = new TmfTimestamp(-5, 9);

	    delta = ts0.getDelta(ts1);
	    assertEquals("getDelta", 0, delta.compareTo(exp, false));
	    
	    // Delta for different scale and same precision (delta > 0)
        ts0 = new TmfTimestamp( 5, 9);
        ts1 = new TmfTimestamp(10, 8);
        exp = new TmfTimestamp( 4, 9);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for different scale and same precision (delta > 0)
        ts0 = new TmfTimestamp( 5, 9);
        ts1 = new TmfTimestamp(10, 7);
        exp = new TmfTimestamp( 5, 9);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for different scale and same precision
        ts0 = new TmfTimestamp(10, 9);
        ts1 = new TmfTimestamp( 5, 8);
        exp = new TmfTimestamp(10, 9);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for same scale and different precision
        ts0 = new TmfTimestamp(10, 9, 1);
        ts1 = new TmfTimestamp( 5, 9, 2);
        exp = new TmfTimestamp( 5, 9, 3);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, true));
        assertEquals("precision", 3, delta.getPrecision());
        
        // Delta for same scale and different precision
        ts0 = new TmfTimestamp( 5, 9, 2);
        ts1 = new TmfTimestamp(10, 9, 1);
        exp = new TmfTimestamp(-5, 9, 3);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta",  0, delta.compareTo(exp, true));
        assertEquals("precision", 3, delta.getPrecision());

        // Delta for different scale and different precision
        ts0 = new TmfTimestamp( 5, 9, 2);
        ts1 = new TmfTimestamp(10, 8, 1);
        exp = new TmfTimestamp( 4, 9, 3);
        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, true));
        assertEquals("precision", 2, delta.getPrecision());
	}
}
