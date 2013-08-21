/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Event Model
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Updated for negative value formatting
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestampFormat;
import org.junit.Test;

/**
 * Test suite for the TmfTimestamp class.
 */
@SuppressWarnings("javadoc")
public class TmfTimestampTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final ITmfTimestamp ts0 = new TmfTimestamp();
    private final ITmfTimestamp ts1 = new TmfTimestamp(12345,  0);
    private final ITmfTimestamp ts2 = new TmfTimestamp(12345, -1);
    private final ITmfTimestamp ts3 = new TmfTimestamp(12345,  2, 5);
    private final ITmfTimestamp ts4 = new TmfTimestamp(12345, -3, 0);
    private final ITmfTimestamp ts5 = new TmfTimestamp(12345, -6, 0);
    private final ITmfTimestamp ts6 = new TmfTimestamp(12345, -9, 0);
    private final ITmfTimestamp ts7 = new TmfTimestamp(-12345, -3, 0);
    private final ITmfTimestamp ts8 = new TmfTimestamp(-12345, -6, 0);
    private final ITmfTimestamp ts9 = new TmfTimestamp(-12345, -9, 0);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        assertEquals("getValue", 0, ts0.getValue());
        assertEquals("getscale", 0, ts0.getScale());
        assertEquals("getPrecision", 0, ts0.getPrecision());
    }

    @Test
    public void testValueConstructor() {
        assertEquals("getValue", 12345, ts1.getValue());
        assertEquals("getscale", 0, ts1.getScale());
        assertEquals("getPrecision", 0, ts1.getPrecision());
    }

    @Test
    public void testValueScaleConstructor() {
        assertEquals("getValue", 12345, ts2.getValue());
        assertEquals("getscale", -1, ts2.getScale());
        assertEquals("getPrecision", 0, ts2.getPrecision());
    }

    @Test
    public void testFullConstructor() {
        assertEquals("getValue", 12345, ts3.getValue());
        assertEquals("getscale", 2, ts3.getScale());
        assertEquals("getPrecision", 5, ts3.getPrecision());
    }

    @Test
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

    @Test(expected=IllegalArgumentException.class)
    public void testCopyNullConstructor() {
        new TmfTimestamp((TmfTimestamp) null);
    }

    @Test
    public void testCopyConstructorBigBang() {
        final ITmfTimestamp ts = new TmfTimestamp(TmfTimestamp.BIG_BANG);
        assertEquals("getValue", TmfTimestamp.BIG_BANG.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.BIG_BANG.getScale(), ts.getScale());
        assertEquals("getPrecision", TmfTimestamp.BIG_BANG.getPrecision(), ts.getPrecision());
    }

    @Test
    public void testCopyConstructorBigCrunch() {
        final ITmfTimestamp ts = new TmfTimestamp(TmfTimestamp.BIG_CRUNCH);
        assertEquals("getValue", TmfTimestamp.BIG_CRUNCH.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.BIG_CRUNCH.getScale(), ts.getScale());
        assertEquals("getPrecision", TmfTimestamp.BIG_CRUNCH.getPrecision(), ts.getPrecision());
    }

    @Test
    public void testCopyConstructorZero() {
        final ITmfTimestamp ts = new TmfTimestamp(TmfTimestamp.ZERO);
        assertEquals("getValue", TmfTimestamp.ZERO.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.ZERO.getScale(), ts.getScale());
        assertEquals("getPrecision", TmfTimestamp.ZERO.getPrecision(), ts.getPrecision());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
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

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", ts0.equals(ts0));
        assertTrue("equals", ts1.equals(ts1));

        assertTrue("equals", !ts0.equals(ts1));
        assertTrue("equals", !ts1.equals(ts0));
    }

    @Test
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

    @Test
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

    @Test
    public void testEqualsNull() {
        assertTrue("equals", !ts0.equals(null));
        assertTrue("equals", !ts1.equals(null));
    }

    @Test
    public void testEqualsNonTimestamp() {
        assertFalse("equals", ts0.equals(ts0.toString()));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToStringDefault() {
        DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
        Date d0 = new Date((long) (ts0.getValue() * Math.pow(10, ts0.getScale() + 3)));
        Date d1 = new Date((long) (ts1.getValue() * Math.pow(10, ts1.getScale() + 3)));
        Date d2 = new Date((long) (ts2.getValue() * Math.pow(10, ts2.getScale() + 3)));
        Date d3 = new Date((long) (ts3.getValue() * Math.pow(10, ts3.getScale() + 3)));
        Date d4 = new Date((long) (ts4.getValue() * Math.pow(10, ts4.getScale() + 3)));
        Date d5 = new Date((long) (ts5.getValue() * Math.pow(10, ts5.getScale() + 3)));
        Date d6 = new Date((long) (ts6.getValue() * Math.pow(10, ts6.getScale() + 3)));
        Date d7 = new Date((long) (ts7.getValue() * Math.pow(10, ts7.getScale() + 3)));
        Date d8 = new Date((long) (ts8.getValue() * Math.pow(10, ts8.getScale() + 3)) - 1);
        Date d9 = new Date((long) (ts9.getValue() * Math.pow(10, ts9.getScale() + 3)) - 1);
        assertEquals("toString", df.format(d0) + " 000 000", ts0.toString());
        assertEquals("toString", df.format(d1) + " 000 000", ts1.toString());
        assertEquals("toString", df.format(d2) + " 000 000", ts2.toString());
        assertEquals("toString", df.format(d3) + " 000 000", ts3.toString());
        assertEquals("toString", df.format(d4) + " 000 000", ts4.toString());
        assertEquals("toString", df.format(d5) + " 345 000", ts5.toString());
        assertEquals("toString", df.format(d6) + " 012 345", ts6.toString());
        assertEquals("toString", df.format(d7) + " 000 000", ts7.toString());
        assertEquals("toString", df.format(d8) + " 655 000", ts8.toString());
        assertEquals("toString", df.format(d9) + " 987 655", ts9.toString());
    }

    @Test
    public void testToStringInterval() {
        assertEquals("toString", "000.000 000 000", ts0.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        assertEquals("toString", "12345.000 000 000", ts1.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        assertEquals("toString", "1234.500 000 000", ts2.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        assertEquals("toString", "1234500.000 000 000", ts3.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        assertEquals("toString", "012.345 000 000", ts4.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        assertEquals("toString", "000.012 345 000", ts5.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        assertEquals("toString", "000.000 012 345", ts6.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        assertEquals("toString", "-012.345 000 000", ts7.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        assertEquals("toString", "-000.012 345 000", ts8.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
        assertEquals("toString", "-000.000 012 345", ts9.toString(TmfTimestampFormat.getDefaulIntervalFormat()));
    }

    // ------------------------------------------------------------------------
    // normalize
    // ------------------------------------------------------------------------

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testNormalizeOffsetAndScaleTrivial() {
        final ITmfTimestamp ts = ts0.normalize(0, 0);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    @Test
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

    @Test
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

    @Test
    public void testBasicCompareTo() {
        final ITmfTimestamp t1 = new TmfTimestamp(900, 0, 50);
        final ITmfTimestamp t2 = new TmfTimestamp(1000, 0, 50);
        final ITmfTimestamp t3 = new TmfTimestamp(1100, 0, 50);
        final ITmfTimestamp t4 = new TmfTimestamp(1000, 0, 75);

        assertTrue(t1.compareTo(t1) == 0);

        assertTrue("CompareTo", t1.compareTo(t2) < 0);
        assertTrue("CompareTo", t1.compareTo(t3) < 0);
        assertTrue("CompareTo", t1.compareTo(t4) < 0);

        assertTrue("CompareTo", t2.compareTo(t1) > 0);
        assertTrue("CompareTo", t2.compareTo(t3) < 0);
        assertTrue("CompareTo", t2.compareTo(t4) == 0);

        assertTrue("CompareTo", t3.compareTo(t1) > 0);
        assertTrue("CompareTo", t3.compareTo(t2) > 0);
        assertTrue("CompareTo", t3.compareTo(t4) > 0);
    }

    @Test
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

    @Test
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

    @Test
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

    @Test
    public void testCompareToCornerCases4() {
        assertTrue("compareTo", ts0.compareTo(null, false) == 1);
        assertTrue("compareTo", ts0.compareTo(null, true) == 1);
    }

    @Test
    public void testCompareToSameScale() {
        final ITmfTimestamp t1 = new TmfTimestamp(900, 0, 50);
        final ITmfTimestamp t2 = new TmfTimestamp(1000, 0, 50);
        final ITmfTimestamp t3 = new TmfTimestamp(1100, 0, 50);
        final ITmfTimestamp t4 = new TmfTimestamp(1000, 0, 75);

        assertTrue(t1.compareTo(t1, false) == 0);

        assertTrue("CompareTo", t1.compareTo(t2, false) < 0);
        assertTrue("CompareTo", t1.compareTo(t3, false) < 0);
        assertTrue("CompareTo", t1.compareTo(t4, false) < 0);

        assertTrue("CompareTo", t2.compareTo(t1, false) > 0);
        assertTrue("CompareTo", t2.compareTo(t3, false) < 0);
        assertTrue("CompareTo", t2.compareTo(t4, false) == 0);

        assertTrue("CompareTo", t3.compareTo(t1, false) > 0);
        assertTrue("CompareTo", t3.compareTo(t2, false) > 0);
        assertTrue("CompareTo", t3.compareTo(t4, false) > 0);
    }

    @Test
    public void testCompareToDifferentScale() {
        final ITmfTimestamp t1 = new TmfTimestamp(9000, -1, 50);
        final ITmfTimestamp t2 = new TmfTimestamp(1000, 0, 50);
        final ITmfTimestamp t3 = new TmfTimestamp(110, 1, 50);
        final ITmfTimestamp t4 = new TmfTimestamp(1, 3, 75);

        assertTrue("CompareTo", t1.compareTo(t1, false) == 0);

        assertTrue("CompareTo", t1.compareTo(t2, false) < 0);
        assertTrue("CompareTo", t1.compareTo(t3, false) < 0);
        assertTrue("CompareTo", t1.compareTo(t4, false) < 0);

        assertTrue("CompareTo", t2.compareTo(t1, false) > 0);
        assertTrue("CompareTo", t2.compareTo(t3, false) < 0);
        assertTrue("CompareTo", t2.compareTo(t4, false) == 0);

        assertTrue("CompareTo", t3.compareTo(t1, false) > 0);
        assertTrue("CompareTo", t3.compareTo(t2, false) > 0);
        assertTrue("CompareTo", t3.compareTo(t4, false) > 0);
    }

    @Test
    public void testCompareToWithinPrecision() {
        final ITmfTimestamp t1 = new TmfTimestamp(900, 0, 50);
        final ITmfTimestamp t2 = new TmfTimestamp(1000, 0, 50);
        final ITmfTimestamp t3 = new TmfTimestamp(1100, 0, 50);
        final ITmfTimestamp t4 = new TmfTimestamp(1000, 0, 75);

        assertTrue("CompareTo", t1.compareTo(t1, true) == 0);

        assertTrue("CompareTo", t1.compareTo(t2, true) == 0);
        assertTrue("CompareTo", t1.compareTo(t3, true) < 0);
        assertTrue("CompareTo", t1.compareTo(t4, true) == 0);

        assertTrue("CompareTo", t2.compareTo(t1, true) == 0);
        assertTrue("CompareTo", t2.compareTo(t3, true) == 0);
        assertTrue("CompareTo", t2.compareTo(t4, true) == 0);

        assertTrue("CompareTo", t3.compareTo(t1, true) > 0);
        assertTrue("CompareTo", t3.compareTo(t2, true) == 0);
        assertTrue("CompareTo", t3.compareTo(t4, true) == 0);
    }

    @Test
    public void testCompareToLargeScale1() {
        final ITmfTimestamp t1 = new TmfTimestamp(-1, 100);
        final ITmfTimestamp t2 = new TmfTimestamp(-1000, -100);
        final ITmfTimestamp t3 = new TmfTimestamp(1, 100);
        final ITmfTimestamp t4 = new TmfTimestamp(1000, -100);

        assertTrue("CompareTo", t1.compareTo(t2, false) < 0);
        assertTrue("CompareTo", t1.compareTo(t3, false) < 0);
        assertTrue("CompareTo", t1.compareTo(t4, false) < 0);

        assertTrue("CompareTo", t2.compareTo(t1, false) > 0);
        assertTrue("CompareTo", t2.compareTo(t3, false) < 0);
        assertTrue("CompareTo", t2.compareTo(t4, false) < 0);

        assertTrue("CompareTo", t3.compareTo(t1, false) > 0);
        assertTrue("CompareTo", t3.compareTo(t2, false) > 0);
        assertTrue("CompareTo", t3.compareTo(t4, false) > 0);

        assertTrue("CompareTo", t4.compareTo(t1, false) > 0);
        assertTrue("CompareTo", t4.compareTo(t2, false) > 0);
        assertTrue("CompareTo", t4.compareTo(t3, false) < 0);
    }

    @Test
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

    @Test
    public void testDelta() {
        // Delta for same scale and precision (delta > 0)
        ITmfTimestamp t0 = new TmfTimestamp(10, 9);
        ITmfTimestamp t1 = new TmfTimestamp(5, 9);
        ITmfTimestamp exp = new TmfTimestamp(5, 9);

        ITmfTimestamp delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for same scale and precision (delta < 0)
        t0 = new TmfTimestamp(5, 9);
        t1 = new TmfTimestamp(10, 9);
        exp = new TmfTimestamp(-5, 9);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for different scale and same precision (delta > 0)
        t0 = new TmfTimestamp(5, 9);
        t1 = new TmfTimestamp(10, 8);
        exp = new TmfTimestamp(4, 9);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for different scale and same precision (delta > 0)
        t0 = new TmfTimestamp(5, 9);
        t1 = new TmfTimestamp(10, 7);
        exp = new TmfTimestamp(5, 9);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for different scale and same precision
        t0 = new TmfTimestamp(10, 9);
        t1 = new TmfTimestamp(5, 8);
        exp = new TmfTimestamp(10, 9);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for same scale and different precision
        t0 = new TmfTimestamp(10, 9, 1);
        t1 = new TmfTimestamp(5, 9, 2);
        exp = new TmfTimestamp(5, 9, 3);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp, true));
        assertEquals("precision", 3, delta.getPrecision());

        // Delta for same scale and different precision
        t0 = new TmfTimestamp(5, 9, 2);
        t1 = new TmfTimestamp(10, 9, 1);
        exp = new TmfTimestamp(-5, 9, 3);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp, true));
        assertEquals("precision", 3, delta.getPrecision());

        // Delta for different scale and different precision
        t0 = new TmfTimestamp(5, 9, 2);
        t1 = new TmfTimestamp(10, 8, 1);
        exp = new TmfTimestamp(4, 9, 3);
        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp, true));
        assertEquals("precision", 2, delta.getPrecision());
    }

}
