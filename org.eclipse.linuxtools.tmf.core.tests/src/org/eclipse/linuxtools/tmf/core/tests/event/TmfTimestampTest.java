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
 *   Francois Chouinard - Adjusted for new Event Model
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestampFormat;

/**
 * Test suite for the TmfTimestamp class.
 */
@SuppressWarnings("nls")
public class TmfTimestampTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final ITmfTimestamp ts0 = new TmfTimestamp();
    private final ITmfTimestamp ts1 = new TmfTimestamp(12345,  0);
    private final ITmfTimestamp ts2 = new TmfTimestamp(12345, -1);
    private final ITmfTimestamp ts3 = new TmfTimestamp(12345,  2, 5);

    // ------------------------------------------------------------------------
    // Housekeping
    // ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
    public TmfTimestampTest(final String name) {
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

    /**
     *
     */
    public void testDefaultConstructor() {
        assertEquals("getValue", 0, ts0.getValue());
        assertEquals("getscale", 0, ts0.getScale());
        assertEquals("getPrecision", 0, ts0.getPrecision());
    }

    /**
     *
     */
    public void testValueConstructor() {
        assertEquals("getValue", 12345, ts1.getValue());
        assertEquals("getscale", 0, ts1.getScale());
        assertEquals("getPrecision", 0, ts1.getPrecision());
    }

    /**
     *
     */
    public void testValueScaleConstructor() {
        assertEquals("getValue", 12345, ts2.getValue());
        assertEquals("getscale", -1, ts2.getScale());
        assertEquals("getPrecision", 0, ts2.getPrecision());
    }

    /**
     *
     */
    public void testFullConstructor() {
        assertEquals("getValue", 12345, ts3.getValue());
        assertEquals("getscale", 2, ts3.getScale());
        assertEquals("getPrecision", 5, ts3.getPrecision());
    }

    /**
     *
     */
    public void testCopyConstructor() {
        final ITmfTimestamp ts = new TmfTimestamp(12345, 2, 5);
        final ITmfTimestamp copy = new TmfTimestamp(ts);

        assertEquals("getValue", ts.getValue(), copy.getValue());
        assertEquals("getscale", ts.getScale(), copy.getScale());
        assertEquals("getPrecision", ts.getPrecision(), copy.getPrecision());

        assertEquals("getValue", 12345, copy.getValue());
        assertEquals("getscale", 2, copy.getScale());
        assertEquals("getPrecision", 5, copy.getPrecision());
    }

    /**
     *
     */
    public void testCopyNullConstructor() {
        try {
            new TmfTimestamp(null);
            fail("TmfTimestamp: null argument");
        } catch (final IllegalArgumentException e) {
        }
    }

    /**
     *
     */
    public void testCopyConstructorBigBang() {
        final ITmfTimestamp ts = new TmfTimestamp(TmfTimestamp.BIG_BANG);
        assertEquals("getValue", TmfTimestamp.BIG_BANG.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.BIG_BANG.getScale(), ts.getScale());
        assertEquals("getPrecision", TmfTimestamp.BIG_BANG.getPrecision(), ts.getPrecision());
    }

    /**
     *
     */
    public void testCopyConstructorBigCrunch() {
        final ITmfTimestamp ts = new TmfTimestamp(TmfTimestamp.BIG_CRUNCH);
        assertEquals("getValue", TmfTimestamp.BIG_CRUNCH.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.BIG_CRUNCH.getScale(), ts.getScale());
        assertEquals("getPrecision", TmfTimestamp.BIG_CRUNCH.getPrecision(), ts.getPrecision());
    }

    /**
     *
     */
    public void testCopyConstructorZero() {
        final ITmfTimestamp ts = new TmfTimestamp(TmfTimestamp.ZERO);
        assertEquals("getValue", TmfTimestamp.ZERO.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.ZERO.getScale(), ts.getScale());
        assertEquals("getPrecision", TmfTimestamp.ZERO.getPrecision(), ts.getPrecision());
    }

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

    private static class MyTimestamp extends TmfTimestamp {

        @Override
        public boolean equals(final Object other) {
            return super.equals(other);
        }

        @Override
        public MyTimestamp clone() {
            return (MyTimestamp) super.clone();
        }
    }

    /**
     *
     */
    public void testClone() {
        final ITmfTimestamp clone = ts0.clone();

        assertTrue("clone", ts0.clone().equals(ts0));
        assertTrue("clone", clone.clone().equals(clone));

        assertEquals("clone", clone, ts0);
        assertEquals("clone", ts0, clone);
    }

    /**
     *
     */
    public void testClone2() {
        final MyTimestamp timestamp = new MyTimestamp();
        final MyTimestamp clone = timestamp.clone();

        assertTrue("clone", timestamp.clone().equals(timestamp));
        assertTrue("clone", clone.clone().equals(clone));

        assertEquals("clone", clone, timestamp);
        assertEquals("clone", timestamp, clone);
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testHashCode() {
        final ITmfTimestamp ts0copy = new TmfTimestamp(ts0);
        final ITmfTimestamp ts1copy = new TmfTimestamp(ts1);
        final ITmfTimestamp ts2copy = new TmfTimestamp(ts2);

        assertTrue("hashCode", ts0.hashCode() == ts0copy.hashCode());
        assertTrue("hashCode", ts1.hashCode() == ts1copy.hashCode());
        assertTrue("hashCode", ts2.hashCode() == ts2copy.hashCode());

        assertTrue("hashCode", ts0.hashCode() != ts1.hashCode());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testEqualsReflexivity() {
        assertTrue("equals", ts0.equals(ts0));
        assertTrue("equals", ts1.equals(ts1));

        assertTrue("equals", !ts0.equals(ts1));
        assertTrue("equals", !ts1.equals(ts0));
    }

    /**
     *
     */
    public void testEqualsSymmetry() {
        final ITmfTimestamp ts0copy = new TmfTimestamp(ts0);
        assertTrue("equals", ts0.equals(ts0copy));
        assertTrue("equals", ts0copy.equals(ts0));

        final ITmfTimestamp ts1copy = new TmfTimestamp(ts1);
        assertTrue("equals", ts1.equals(ts1copy));
        assertTrue("equals", ts1copy.equals(ts1));

        final ITmfTimestamp ts2copy = new TmfTimestamp(ts2);
        assertTrue("equals", ts2.equals(ts2copy));
        assertTrue("equals", ts2copy.equals(ts2));
    }

    /**
     *
     */
    public void testEqualsTransivity() {
        final ITmfTimestamp ts0copy1 = new TmfTimestamp(ts0);
        final ITmfTimestamp ts0copy2 = new TmfTimestamp(ts0copy1);
        assertTrue("equals", ts0.equals(ts0copy1));
        assertTrue("equals", ts0copy1.equals(ts0copy2));
        assertTrue("equals", ts0.equals(ts0copy2));

        final ITmfTimestamp ts1copy1 = new TmfTimestamp(ts1);
        final ITmfTimestamp ts1copy2 = new TmfTimestamp(ts1copy1);
        assertTrue("equals", ts1.equals(ts1copy1));
        assertTrue("equals", ts1copy1.equals(ts1copy2));
        assertTrue("equals", ts1.equals(ts1copy2));

        final ITmfTimestamp ts2copy1 = new TmfTimestamp(ts2);
        final ITmfTimestamp ts2copy2 = new TmfTimestamp(ts2copy1);
        assertTrue("equals", ts2.equals(ts2copy1));
        assertTrue("equals", ts2copy1.equals(ts2copy2));
        assertTrue("equals", ts2.equals(ts2copy2));
    }

    /**
     *
     */
    public void testEqualsNull() {
        assertTrue("equals", !ts0.equals(null));
        assertTrue("equals", !ts1.equals(null));
    }

    /**
     *
     */
    public void testEqualsNonTimestamp() {
        assertFalse("equals", ts0.equals(ts0.toString()));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testToStringDefault() {
        assertEquals("toString", "00:00:00.000 000 000", ts0.toString());
        assertEquals("toString", "03:25:45.000 000 000", ts1.toString());
        assertEquals("toString", "00:20:34.500 000 000", ts2.toString());
        assertEquals("toString", "06:55:00.000 000 000", ts3.toString());
    }

    /**
    *
    */
   public void testToStringInterval() {
       assertEquals("toString", "000.000 000 000", ts0.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
       assertEquals("toString", "12345.000 000 000", ts1.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
       assertEquals("toString", "1234.500 000 000", ts2.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
       assertEquals("toString", "1234500.000 000 000", ts3.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
   }

    // ------------------------------------------------------------------------
    // normalize
    // ------------------------------------------------------------------------

    /**
     *
     */
    public void testNormalizeOffset() {
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

    /**
     *
     */
    public void testNormalizeOffsetLowerLimits() {
        final ITmfTimestamp ref = new TmfTimestamp(Long.MIN_VALUE + 5, 0);

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

    /**
     *
     */
    public void testNormalizeOffsetUpperLimits() {
        final ITmfTimestamp ref = new TmfTimestamp(Long.MAX_VALUE - 5, 0);

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

    /**
     *
     */
    public void testNormalizeScale() {
        ITmfTimestamp ts = ts0.normalize(0, 10);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 10, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(0, -10);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", -10, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    /**
     *
     */
    public void testNormalizedScaleLimits() {
        final int MAX_SCALE_DIFF = 19;

        // Test below limit
        try {
            ts1.normalize(0, +MAX_SCALE_DIFF - 1);
            ts1.normalize(0, -MAX_SCALE_DIFF + 1);
        } catch (final ArithmeticException e) {
            fail("normalize: scale error");
        }

        // Test at limit
        try {
            ts1.normalize(0, +MAX_SCALE_DIFF);
            fail("normalize: scale error");
            ts1.normalize(0, -MAX_SCALE_DIFF);
            fail("normalize: scale error");
        } catch (final ArithmeticException e) {
        }

        // Test over limit
        try {
            ts1.normalize(0, +MAX_SCALE_DIFF + 1);
            fail("normalize: scale error");
            ts1.normalize(0, -MAX_SCALE_DIFF - 1);
            fail("normalize: scale error");
        } catch (final ArithmeticException e) {
        }
    }

    /**
     *
     */
    public void testNormalizeOffsetAndScaleTrivial() {
        final ITmfTimestamp ts = ts0.normalize(0, 0);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    /**
     *
     */
    public void testNormalizeOffsetAndScale() {
        final int SCALE = 12;

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

    /**
     *
     */
    public void testNormalizeOffsetAndScale2() {
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

    /**
     *
     */
    @SuppressWarnings("hiding")
    public void testBasicCompareTo() {
        final ITmfTimestamp ts1 = new TmfTimestamp(900, 0, 50);
        final ITmfTimestamp ts2 = new TmfTimestamp(1000, 0, 50);
        final ITmfTimestamp ts3 = new TmfTimestamp(1100, 0, 50);
        final ITmfTimestamp ts4 = new TmfTimestamp(1000, 0, 75);

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

    /**
     *
     */
    public void testCompareToCornerCases1() {
        final ITmfTimestamp ts0a = new TmfTimestamp(ts0);
        final ITmfTimestamp ts0b = new TmfTimestamp(ts0.getValue(), ts0.getScale() + 1);
        final ITmfTimestamp ts0c = new TmfTimestamp(ts0.getValue() + 1, ts0.getScale());
        final ITmfTimestamp ts0d = new TmfTimestamp(ts0.getValue() + 1, ts0.getScale() + 1);

        assertTrue("compareTo", ts0.compareTo(ts0, false) == 0);
        assertTrue("compareTo", ts0.compareTo(ts0a, false) == 0);
        assertTrue("compareTo", ts0.compareTo(ts0b, false) == 0);
        assertTrue("compareTo", ts0.compareTo(ts0c, false) == -1);
        assertTrue("compareTo", ts0.compareTo(ts0d, false) == -1);
    }

    /**
     *
     */
    public void testCompareToCornerCases2() {
        final ITmfTimestamp ts0a = new TmfTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE - 1);
        final ITmfTimestamp ts0b = new TmfTimestamp(0, Integer.MAX_VALUE);
        final ITmfTimestamp ts0c = new TmfTimestamp(Long.MAX_VALUE, Integer.MAX_VALUE);

        assertTrue("compareTo", ts0a.compareTo(ts0b, false) == 1);
        assertTrue("compareTo", ts0a.compareTo(ts0c, false) == -1);

        assertTrue("compareTo", ts0b.compareTo(ts0a, false) == -1);
        assertTrue("compareTo", ts0b.compareTo(ts0c, false) == -1);

        assertTrue("compareTo", ts0c.compareTo(ts0a, false) == 1);
        assertTrue("compareTo", ts0c.compareTo(ts0b, false) == 1);
    }

    /**
     *
     */
    public void testCompareToCornerCases3() {
        final ITmfTimestamp ts0a = new TmfTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE - 1);
        final ITmfTimestamp ts0b = new TmfTimestamp(0, Integer.MAX_VALUE);
        final ITmfTimestamp ts0c = new TmfTimestamp(Long.MIN_VALUE, Integer.MAX_VALUE);

        assertTrue("compareTo", ts0a.compareTo(ts0b, false) == -1);
        assertTrue("compareTo", ts0a.compareTo(ts0c, false) == 1);

        assertTrue("compareTo", ts0b.compareTo(ts0a, false) == 1);
        assertTrue("compareTo", ts0b.compareTo(ts0c, false) == 1);

        assertTrue("compareTo", ts0c.compareTo(ts0a, false) == -1);
        assertTrue("compareTo", ts0c.compareTo(ts0b, false) == -1);
    }

    /**
     *
     */
    public void testCompareToCornerCases4() {
        assertTrue("compareTo", ts0.compareTo(null, false) == 1);
        assertTrue("compareTo", ts0.compareTo(null, true) == 1);
    }

    /**
     *
     */
    @SuppressWarnings("hiding")
    public void testCompareToSameScale() {
        final ITmfTimestamp ts1 = new TmfTimestamp(900, 0, 50);
        final ITmfTimestamp ts2 = new TmfTimestamp(1000, 0, 50);
        final ITmfTimestamp ts3 = new TmfTimestamp(1100, 0, 50);
        final ITmfTimestamp ts4 = new TmfTimestamp(1000, 0, 75);

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

    /**
     *
     */
    @SuppressWarnings("hiding")
    public void testCompareToDifferentScale() {
        final ITmfTimestamp ts1 = new TmfTimestamp(9000, -1, 50);
        final ITmfTimestamp ts2 = new TmfTimestamp(1000, 0, 50);
        final ITmfTimestamp ts3 = new TmfTimestamp(110, 1, 50);
        final ITmfTimestamp ts4 = new TmfTimestamp(1, 3, 75);

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

    /**
     *
     */
    @SuppressWarnings("hiding")
    public void testCompareToWithinPrecision() {
        final ITmfTimestamp ts1 = new TmfTimestamp(900, 0, 50);
        final ITmfTimestamp ts2 = new TmfTimestamp(1000, 0, 50);
        final ITmfTimestamp ts3 = new TmfTimestamp(1100, 0, 50);
        final ITmfTimestamp ts4 = new TmfTimestamp(1000, 0, 75);

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

    /**
     *
     */
    @SuppressWarnings("hiding")
    public void testCompareToLargeScale1() {
        final ITmfTimestamp ts1 = new TmfTimestamp(-1, 100);
        final ITmfTimestamp ts2 = new TmfTimestamp(-1000, -100);
        final ITmfTimestamp ts3 = new TmfTimestamp(1, 100);
        final ITmfTimestamp ts4 = new TmfTimestamp(1000, -100);

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

    /**
     *
     */
    public void testCompareToLargeScale2() {
        final ITmfTimestamp ts0a = new TmfTimestamp(0, Integer.MAX_VALUE);
        final ITmfTimestamp ts0b = new TmfTimestamp(1, Integer.MAX_VALUE);

        assertTrue("CompareTo", ts0a.compareTo(ts0, false) == 0);
        assertTrue("CompareTo", ts0.compareTo(ts0a, false) == 0);

        assertTrue("CompareTo", ts0b.compareTo(ts0, false) == 1);
        assertTrue("CompareTo", ts0.compareTo(ts0b, false) == -1);
    }

    // ------------------------------------------------------------------------
    // getDelta
    // ------------------------------------------------------------------------

    /**
     *
     */
    @SuppressWarnings("hiding")
    public void testDelta() {
        // Delta for same scale and precision (delta > 0)
        ITmfTimestamp ts0 = new TmfTimestamp(10, 9);
        ITmfTimestamp ts1 = new TmfTimestamp(5, 9);
        ITmfTimestamp exp = new TmfTimestamp(5, 9);

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
