/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Revisit for TMF Event Model 1.0
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

	private final ITmfTimestamp ts0 = new TmfTimestamp();
	private final ITmfTimestamp ts1 = new TmfTimestamp(12345);
	private final ITmfTimestamp ts2 = new TmfTimestamp(12345, -1);
	private final ITmfTimestamp ts3 = new TmfTimestamp(12345, 2, 5);

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
        assertEquals("getValue", 0, ts0.getValue());
        assertEquals("getscale", 0, ts0.getScale());
        assertEquals("getPrecision", 0, ts0.getPrecision());
    }

    public void testValueConstructor() throws Exception {
        assertEquals("getValue", 12345, ts1.getValue());
        assertEquals("getscale", 0, ts1.getScale());
        assertEquals("getPrecision", 0, ts1.getPrecision());
    }

    public void testValueScaleConstructor() throws Exception {
        assertEquals("getValue", 12345, ts2.getValue());
        assertEquals("getscale", -1, ts2.getScale());
        assertEquals("getPrecision", 0, ts2.getPrecision());
    }

    public void testFullConstructor() throws Exception {
        assertEquals("getValue", 12345, ts3.getValue());
        assertEquals("getscale", 2, ts3.getScale());
        assertEquals("getPrecision", 5, ts3.getPrecision());
    }

    public void testCopyConstructor() throws Exception {
        ITmfTimestamp ts = new TmfTimestamp(12345, 2, 5);
        ITmfTimestamp copy = new TmfTimestamp(ts);

        assertEquals("getValue", ts.getValue(), copy.getValue());
        assertEquals("getscale", ts.getScale(), copy.getScale());
        assertEquals("getPrecision", ts.getPrecision(), copy.getPrecision());

        assertEquals("getValue", 12345, copy.getValue());
        assertEquals("getscale", 2, copy.getScale());
        assertEquals("getPrecision", 5, copy.getPrecision());
    }

    public void testCopyNullConstructor() throws Exception {
        try {
            @SuppressWarnings("unused")
            ITmfTimestamp timestamp = new TmfTimestamp(null);
            fail("null copy");
        } catch (IllegalArgumentException e) {
        }
    }

    public void testCopyConstructorBigBang() throws Exception {
        ITmfTimestamp ts = new TmfTimestamp(TmfTimestamp.BigBang);
        assertEquals("getValue", TmfTimestamp.BigBang.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.BigBang.getScale(), ts.getScale());
        assertEquals("getPrecision", TmfTimestamp.BigBang.getPrecision(), ts.getPrecision());
    }

    public void testCopyConstructorBigCrunch() throws Exception {
        ITmfTimestamp ts = new TmfTimestamp(TmfTimestamp.BigCrunch);
        assertEquals("getValue", TmfTimestamp.BigCrunch.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.BigCrunch.getScale(), ts.getScale());
        assertEquals("getPrecision", TmfTimestamp.BigCrunch.getPrecision(), ts.getPrecision());
    }

    public void testCopyConstructorZero() throws Exception {
        ITmfTimestamp ts = new TmfTimestamp(TmfTimestamp.Zero);
        assertEquals("getValue", TmfTimestamp.Zero.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.Zero.getScale(), ts.getScale());
        assertEquals("getPrecision", TmfTimestamp.Zero.getPrecision(), ts.getPrecision());
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
        ITmfTimestamp ts0copy = new TmfTimestamp(ts0);
        assertTrue("equals", ts0.equals(ts0copy));
        assertTrue("equals", ts0copy.equals(ts0));

        ITmfTimestamp ts1copy = new TmfTimestamp(ts1);
        assertTrue("equals", ts1.equals(ts1copy));
        assertTrue("equals", ts1copy.equals(ts1));

        ITmfTimestamp ts2copy = new TmfTimestamp(ts2);
        assertTrue("equals", ts2.equals(ts2copy));
        assertTrue("equals", ts2copy.equals(ts2));
    }

    public void testEqualsTransivity() throws Exception {
        ITmfTimestamp ts0copy1 = new TmfTimestamp(ts0);
        ITmfTimestamp ts0copy2 = new TmfTimestamp(ts0copy1);
        assertTrue("equals", ts0.equals(ts0copy1));
        assertTrue("equals", ts0copy1.equals(ts0copy2));
        assertTrue("equals", ts0.equals(ts0copy2));

        ITmfTimestamp ts1copy1 = new TmfTimestamp(ts1);
        ITmfTimestamp ts1copy2 = new TmfTimestamp(ts1copy1);
        assertTrue("equals", ts1.equals(ts1copy1));
        assertTrue("equals", ts1copy1.equals(ts1copy2));
        assertTrue("equals", ts1.equals(ts1copy2));

        ITmfTimestamp ts2copy1 = new TmfTimestamp(ts2);
        ITmfTimestamp ts2copy2 = new TmfTimestamp(ts2copy1);
        assertTrue("equals", ts2.equals(ts2copy1));
        assertTrue("equals", ts2copy1.equals(ts2copy2));
        assertTrue("equals", ts2.equals(ts2copy2));
    }

    public void testEqualsNull() throws Exception {
        assertTrue("equals", !ts0.equals(null));
        assertTrue("equals", !ts1.equals(null));
    }

    public void testEqualsNonTimestamp() throws Exception {
        assertFalse("equals", ts0.equals(ts0.toString()));
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
        ITmfTimestamp timestamp = ts0.clone();
        assertEquals("clone", timestamp, ts0);
    }

    public void testClone2() throws Exception {
        MyTimestamp timestamp = new MyTimestamp();
        MyTimestamp clone = timestamp.clone();
        assertEquals("clone", clone, timestamp);
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    public void testToString() throws Exception {
        assertEquals("toString", "TmfTimestamp [fValue=0, fScale=0, fPrecision=0]", ts0.toString());
        assertEquals("toString", "TmfTimestamp [fValue=12345, fScale=0, fPrecision=0]", ts1.toString());
        assertEquals("toString", "TmfTimestamp [fValue=12345, fScale=-1, fPrecision=0]", ts2.toString());
        assertEquals("toString", "TmfTimestamp [fValue=12345, fScale=2, fPrecision=5]", ts3.toString());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() throws Exception {
        ITmfTimestamp ts0copy = new TmfTimestamp(ts0);
        ITmfTimestamp ts1copy = new TmfTimestamp(ts1);
        ITmfTimestamp ts2copy = new TmfTimestamp(ts2);

        assertTrue("hashCode", ts0.hashCode() == ts0copy.hashCode());
        assertTrue("hashCode", ts1.hashCode() == ts1copy.hashCode());
        assertTrue("hashCode", ts2.hashCode() == ts2copy.hashCode());

        assertTrue("hashCode", ts0.hashCode() != ts1.hashCode());
    }
    
    // ------------------------------------------------------------------------
    // normalize
    // ------------------------------------------------------------------------

    public void testNormalizeOffset() throws Exception {
        ITmfTimestamp ts = ts0.normalize(0, 0);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(12345, 0);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(10, 0);
        assertEquals("getValue", 10, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(-10, 0);
        assertEquals("getValue", -10, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    public void testNormalizeOffsetLowerLimits() throws Exception {
        ITmfTimestamp ref = new TmfTimestamp(Long.MIN_VALUE + 5);

        ITmfTimestamp ts = ref.normalize(-4, 0);
        assertEquals("getValue", Long.MIN_VALUE + 1, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ref.normalize(-5, 0);
        assertEquals("getValue", Long.MIN_VALUE, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ref.normalize(-6, 0);
        assertEquals("getValue", Long.MIN_VALUE, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    public void testNormalizeOffsetUpperLimits() throws Exception {
        ITmfTimestamp ref = new TmfTimestamp(Long.MAX_VALUE - 5);

        ITmfTimestamp ts = ref.normalize(4, 0);
        assertEquals("getValue", Long.MAX_VALUE - 1, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ref.normalize(5, 0);
        assertEquals("getValue", Long.MAX_VALUE, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ref.normalize(6, 0);
        assertEquals("getValue", Long.MAX_VALUE, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    public void testNormalizeScale() throws Exception {
        ITmfTimestamp ts = ts0.normalize(0, 10);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 10, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(0, -10);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", -10, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    public void testNormalizedScaleLimits() throws Exception {
        int MAX_SCALE_DIFF = 19;

        // Test below limit
        try {
            ts1.normalize(0, +MAX_SCALE_DIFF - 1);
            ts1.normalize(0, -MAX_SCALE_DIFF + 1);
        } catch (ArithmeticException e) {
            fail();
        }

        // Test at limit
        try {
            ts1.normalize(0, +MAX_SCALE_DIFF);
            fail();
            ts1.normalize(0, -MAX_SCALE_DIFF);
            fail();
        } catch (ArithmeticException e) {
        }

        // Test over limit
        try {
            ts1.normalize(0, +MAX_SCALE_DIFF + 1);
            fail();
            ts1.normalize(0, -MAX_SCALE_DIFF - 1);
            fail();
        } catch (ArithmeticException e) {
        }
    }

    public void testNormalizeOffsetAndScaleTrivial() throws Exception {
        ITmfTimestamp ts = ts0.normalize(0, 0);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    public void testNormalizeOffsetAndScale() throws Exception {
        int SCALE = 12;

        ITmfTimestamp ts = ts0.normalize(0, SCALE);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(12345, SCALE);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(10, SCALE);
        assertEquals("getValue", 10, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(-10, SCALE);
        assertEquals("getValue", -10, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    public void testNormalizeOffsetAndScale2() throws Exception {
        int SCALE = 2;
        ITmfTimestamp ts = ts1.normalize(0, SCALE);
        assertEquals("getValue", 123, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts1.normalize(12345, SCALE);
        assertEquals("getValue", 12468, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        SCALE = -2;
        ts = ts1.normalize(0, SCALE);
        assertEquals("getValue", 1234500, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts1.normalize(67, SCALE);
        assertEquals("getValue", 1234567, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }
    
    // ------------------------------------------------------------------------
    // compareTo
    // ------------------------------------------------------------------------

    public void testBasicCompareTo() throws Exception {
        TmfTimestamp ts1 = new TmfTimestamp(900, 0, 50);
        TmfTimestamp ts2 = new TmfTimestamp(1000, 0, 50);
        TmfTimestamp ts3 = new TmfTimestamp(1100, 0, 50);
        TmfTimestamp ts4 = new TmfTimestamp(1000, 0, 75);

        assertTrue(ts1.compareTo(ts1) == 0);

        assertTrue("CompareTo", ts1.compareTo(ts2) < 0);
        assertTrue("CompareTo", ts1.compareTo(ts3) < 0);
        assertTrue("CompareTo", ts1.compareTo(ts4) < 0);

        assertTrue("CompareTo", ts2.compareTo(ts1) > 0);
        assertTrue("CompareTo", ts2.compareTo(ts3) < 0);
        assertTrue("CompareTo", ts2.compareTo(ts4) == 0);

        assertTrue("CompareTo", ts3.compareTo(ts1) > 0);
        assertTrue("CompareTo", ts3.compareTo(ts2) > 0);
        assertTrue("CompareTo", ts3.compareTo(ts4) > 0);
    }

    public void testCompareToCornerCases1() throws Exception {
        TmfTimestamp ts0a = new TmfTimestamp(ts0);
        TmfTimestamp ts0b = new TmfTimestamp(ts0.getValue(), ts0.getScale() + 1);
        TmfTimestamp ts0c = new TmfTimestamp(ts0.getValue() + 1, ts0.getScale());
        TmfTimestamp ts0d = new TmfTimestamp(ts0.getValue() + 1, ts0.getScale() + 1);

        assertTrue("compareTo", ts0.compareTo(ts0, false) == 0);
        assertTrue("compareTo", ts0.compareTo(ts0a, false) == 0);
        assertTrue("compareTo", ts0.compareTo(ts0b, false) == 0);
        assertTrue("compareTo", ts0.compareTo(ts0c, false)== -1);
        assertTrue("compareTo", ts0.compareTo(ts0d, false) == -1);
    }

    public void testCompareToCornerCases2() throws Exception {
        TmfTimestamp ts0a = new TmfTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE - 1);
        TmfTimestamp ts0b = new TmfTimestamp(0, Integer.MAX_VALUE);
        TmfTimestamp ts0c = new TmfTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE);

        assertTrue("compareTo", ts0a.compareTo(ts0b, false)== 1);
        assertTrue("compareTo", ts0a.compareTo(ts0c, false)== -1);

        assertTrue("compareTo", ts0b.compareTo(ts0a, false)== -1);
        assertTrue("compareTo", ts0b.compareTo(ts0c, false)== -1);

        assertTrue("compareTo", ts0c.compareTo(ts0a, false)== 1);
        assertTrue("compareTo", ts0c.compareTo(ts0b, false)== 1);
    }

    public void testCompareToCornerCases3() throws Exception {
        TmfTimestamp ts0a = new TmfTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE - 1);
        TmfTimestamp ts0b = new TmfTimestamp(0, Integer.MAX_VALUE);
        TmfTimestamp ts0c = new TmfTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE);

        assertTrue("compareTo", ts0a.compareTo(ts0b, false)== -1);
        assertTrue("compareTo", ts0a.compareTo(ts0c, false)== 1);

        assertTrue("compareTo", ts0b.compareTo(ts0a, false)== 1);
        assertTrue("compareTo", ts0b.compareTo(ts0c, false)== 1);

        assertTrue("compareTo", ts0c.compareTo(ts0a, false)== -1);
        assertTrue("compareTo", ts0c.compareTo(ts0b, false)== -1);
    }

    public void testCompareToSameScale() throws Exception {
        TmfTimestamp ts1 = new TmfTimestamp(900, 0, 50);
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
        TmfTimestamp ts2 = new TmfTimestamp(1000, 0, 50);
        TmfTimestamp ts3 = new TmfTimestamp(110, 1, 50);
        TmfTimestamp ts4 = new TmfTimestamp(1, 3, 75);

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
        TmfTimestamp ts1 = new TmfTimestamp(900, 0, 50);
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

    public void testCompareToLargeScale1() throws Exception {
        TmfTimestamp ts1 = new TmfTimestamp(-1, 100);
        TmfTimestamp ts2 = new TmfTimestamp(-1000, -100);
        TmfTimestamp ts3 = new TmfTimestamp(1, 100);
        TmfTimestamp ts4 = new TmfTimestamp(1000, -100);

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

    public void testCompareToLargeScale2() throws Exception {
        TmfTimestamp ts0a = new TmfTimestamp(0, Integer.MAX_VALUE);
        TmfTimestamp ts0b = new TmfTimestamp(1, Integer.MAX_VALUE);

        assertTrue("CompareTo", ts0a.compareTo(ts0, false) == 0);
        assertTrue("CompareTo", ts0.compareTo(ts0a, false) == 0);

        assertTrue("CompareTo", ts0b.compareTo(ts0, false) == 1);
        assertTrue("CompareTo", ts0.compareTo(ts0b, false) == -1);
    }

    // ------------------------------------------------------------------------
    // getDelta
    // ------------------------------------------------------------------------
    
    public void testDelta() throws Exception {
        // Delta for same scale and precision (delta > 0)
        TmfTimestamp ts0 = new TmfTimestamp(10, 9);
        TmfTimestamp ts1 = new TmfTimestamp(5, 9);
        TmfTimestamp exp = new TmfTimestamp(5, 9);

        ITmfTimestamp delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for same scale and precision (delta < 0)
        ts0 = new TmfTimestamp(5, 9);
        ts1 = new TmfTimestamp(10, 9);
        exp = new TmfTimestamp(-5, 9);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for different scale and same precision (delta > 0)
        ts0 = new TmfTimestamp(5, 9);
        ts1 = new TmfTimestamp(10, 8);
        exp = new TmfTimestamp(4, 9);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for different scale and same precision (delta > 0)
        ts0 = new TmfTimestamp(5, 9);
        ts1 = new TmfTimestamp(10, 7);
        exp = new TmfTimestamp(5, 9);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for different scale and same precision
        ts0 = new TmfTimestamp(10, 9);
        ts1 = new TmfTimestamp(5, 8);
        exp = new TmfTimestamp(10, 9);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for same scale and different precision
        ts0 = new TmfTimestamp(10, 9, 1);
        ts1 = new TmfTimestamp(5, 9, 2);
        exp = new TmfTimestamp(5, 9, 3);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, true));
        assertEquals("precision", 3, delta.getPrecision());

        // Delta for same scale and different precision
        ts0 = new TmfTimestamp(5, 9, 2);
        ts1 = new TmfTimestamp(10, 9, 1);
        exp = new TmfTimestamp(-5, 9, 3);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, true));
        assertEquals("precision", 3, delta.getPrecision());

        // Delta for different scale and different precision
        ts0 = new TmfTimestamp(5, 9, 2);
        ts1 = new TmfTimestamp(10, 8, 1);
        exp = new TmfTimestamp(4, 9, 3);
        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, true));
        assertEquals("precision", 2, delta.getPrecision());
    }

}
