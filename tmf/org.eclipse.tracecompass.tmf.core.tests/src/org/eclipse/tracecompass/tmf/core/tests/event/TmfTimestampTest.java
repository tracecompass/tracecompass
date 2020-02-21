/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Adjusted for new Event Model
 *   Alexandre Montplaisir - Port to JUnit4
 *   Patrick Tasse - Updated for negative value formatting
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestampFormat;
import org.junit.Test;

/**
 * Test suite for the TmfTimestamp class.
 */
@SuppressWarnings("javadoc")
public class TmfTimestampTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final ITmfTimestamp ts0 = TmfTimestamp.create(0, ITmfTimestamp.SECOND_SCALE);
    private final ITmfTimestamp ts1 = TmfTimestamp.create(12345, 0);
    private final ITmfTimestamp ts2 = TmfTimestamp.create(12345, -1);
    private final ITmfTimestamp ts3 = TmfTimestamp.create(12345, 2);
    private final ITmfTimestamp ts4 = TmfTimestamp.create(12345, -3);
    private final ITmfTimestamp ts5 = TmfTimestamp.create(12345, -6);
    private final ITmfTimestamp ts6 = TmfTimestamp.create(12345, -9);
    private final ITmfTimestamp ts7 = TmfTimestamp.create(-12345, -3);
    private final ITmfTimestamp ts8 = TmfTimestamp.create(-12345, -6);
    private final ITmfTimestamp ts9 = TmfTimestamp.create(-12345, -9);
    private final ITmfTimestamp ts10 = TmfTimestamp.create(Long.MAX_VALUE / 100, -6);
    private final ITmfTimestamp ts11 = TmfTimestamp.create(Long.MIN_VALUE / 100, -6);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        assertEquals("getValue", 0, ts0.getValue());
        assertEquals("getscale", 0, ts0.getScale());
    }

    @Test
    public void testValueConstructor() {
        assertEquals("getValue", 12345, ts1.getValue());
        assertEquals("getscale", 0, ts1.getScale());
    }

    @Test
    public void testValueScaleConstructor() {
        assertEquals("getValue", 12345, ts2.getValue());
        assertEquals("getscale", -1, ts2.getScale());
    }

    @Test
    public void testFullConstructor() {
        assertEquals("getValue", 12345, ts3.getValue());
        assertEquals("getscale", 2, ts3.getScale());
    }

    @Test
    public void testCopyConstructor() {
        final ITmfTimestamp ts = TmfTimestamp.create(12345, 2);
        final ITmfTimestamp copy = TmfTimestamp.create(ts.getValue(), ts.getScale());

        assertEquals("getValue", ts.getValue(), copy.getValue());
        assertEquals("getscale", ts.getScale(), copy.getScale());

        assertEquals("getValue", 12345, copy.getValue());
        assertEquals("getscale", 2, copy.getScale());
    }

    @Test
    public void testCopyConstructorBigBang() {
        final ITmfTimestamp ts = TmfTimestamp.create(TmfTimestamp.BIG_BANG.getValue(), TmfTimestamp.BIG_BANG.getScale());
        assertEquals("getValue", TmfTimestamp.BIG_BANG.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.BIG_BANG.getScale(), ts.getScale());
    }

    @Test
    public void testCopyConstructorBigCrunch() {
        final ITmfTimestamp ts = TmfTimestamp.create(TmfTimestamp.BIG_CRUNCH.getValue(), TmfTimestamp.BIG_CRUNCH.getScale());
        assertEquals("getValue", TmfTimestamp.BIG_CRUNCH.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.BIG_CRUNCH.getScale(), ts.getScale());
    }

    @Test
    public void testCopyConstructorZero() {
        final ITmfTimestamp ts = TmfTimestamp.create(TmfTimestamp.ZERO.getValue(), TmfTimestamp.ZERO.getScale());
        assertEquals("getValue", TmfTimestamp.ZERO.getValue(), ts.getValue());
        assertEquals("getscale", TmfTimestamp.ZERO.getScale(), ts.getScale());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        final ITmfTimestamp ts0copy = TmfTimestamp.create(ts0.getValue(), ts0.getScale());
        final ITmfTimestamp ts1copy = TmfTimestamp.create(ts1.getValue(), ts1.getScale());
        final ITmfTimestamp ts2copy = TmfTimestamp.create(ts2.getValue(), ts2.getScale());

        assertEquals("hashCode", ts0.hashCode(), ts0copy.hashCode());
        assertEquals("hashCode", ts1.hashCode(), ts1copy.hashCode());
        assertEquals("hashCode", ts2.hashCode(), ts2copy.hashCode());

        assertTrue("hashCode", ts0.hashCode() != ts1.hashCode());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertEquals("equals", ts0, ts0);
        assertEquals("equals", ts1, ts1);

        assertFalse("Different", ts0.equals(ts1));
        assertFalse("Different", ts1.equals(ts0));
    }

    @Test
    public void testEqualsSymmetry() {
        final ITmfTimestamp ts0copy = TmfTimestamp.create(ts0.getValue(), ts0.getScale());
        assertEquals("equals", ts0, ts0copy);
        assertEquals("equals", ts0copy, ts0);

        final ITmfTimestamp ts1copy = TmfTimestamp.create(ts1.getValue(), ts1.getScale());
        assertEquals("equals", ts1, ts1copy);
        assertEquals("equals", ts1copy, ts1);

        final ITmfTimestamp ts2copy = TmfTimestamp.create(ts2.getValue(), ts2.getScale());
        assertEquals("equals", ts2, ts2copy);
        assertEquals("equals", ts2copy, ts2);
    }

    @Test
    public void testEqualsTransivity() {
        final ITmfTimestamp ts0copy1 = TmfTimestamp.create(ts0.getValue(), ts0.getScale());
        final ITmfTimestamp ts0copy2 = TmfTimestamp.create(ts0copy1.getValue(), ts0copy1.getScale());
        assertTrue("equals", ts0.equals(ts0copy1));
        assertTrue("equals", ts0copy1.equals(ts0copy2));
        assertTrue("equals", ts0.equals(ts0copy2));

        final ITmfTimestamp ts1copy1 = TmfTimestamp.create(ts1.getValue(), ts1.getScale());
        final ITmfTimestamp ts1copy2 = TmfTimestamp.create(ts1copy1.getValue(), ts1copy1.getScale());
        assertTrue("equals", ts1.equals(ts1copy1));
        assertTrue("equals", ts1copy1.equals(ts1copy2));
        assertTrue("equals", ts1.equals(ts1copy2));

        final ITmfTimestamp ts2copy1 = TmfTimestamp.create(ts2.getValue(), ts2.getScale());
        final ITmfTimestamp ts2copy2 = TmfTimestamp.create(ts2copy1.getValue(), ts2copy1.getScale());
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

        ts = ts0.normalize(12345, 0);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());

        ts = ts0.normalize(10, 0);
        assertEquals("getValue", 10, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());

        ts = ts0.normalize(-10, 0);
        assertEquals("getValue", -10, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
    }

    @Test
    public void testNormalizeOffsetLowerLimits() {
        final ITmfTimestamp ref = TmfTimestamp.create(Long.MIN_VALUE + 5, 0);

        ITmfTimestamp ts = ref.normalize(-4, 0);
        assertEquals("getValue", Long.MIN_VALUE + 1, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());

        ts = ref.normalize(-5, 0);
        assertEquals("getValue", Long.MIN_VALUE, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());

        ts = ref.normalize(-6, 0);
        assertEquals("getValue", Long.MIN_VALUE, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
    }

    @Test
    public void testNormalizeOffsetUpperLimits() {
        final ITmfTimestamp ref = TmfTimestamp.create(Long.MAX_VALUE - 5, 0);

        ITmfTimestamp ts = ref.normalize(4, 0);
        assertEquals("getValue", Long.MAX_VALUE - 1, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());

        ts = ref.normalize(5, 0);
        assertEquals("getValue", Long.MAX_VALUE, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());

        ts = ref.normalize(6, 0);
        assertEquals("getValue", Long.MAX_VALUE, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
    }

    @Test
    public void testNormalizeScale() {
        ITmfTimestamp ts = ts1.normalize(0, 3);
        assertEquals("getValue", 12, ts.getValue());
        assertEquals("getscale", 3, ts.getScale());

        ts = ts1.normalize(0, -3);
        assertEquals("getValue", 12345000L, ts.getValue());
        assertEquals("getscale", -3, ts.getScale());
    }

    @Test
    public void testNormalizeLargeScale() {
        ITmfTimestamp ts = ts1.normalize(0, 10);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());

        ts = ts1.normalize(0, -10);
        assertEquals("getValue", 123450000000000L, ts.getValue());
        assertEquals("getscale", -10, ts.getScale());
    }

    @Test
    public void testNormalizeZeroScale() {
        ITmfTimestamp ts = ts0.normalize(0, 10);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());

        ts = ts0.normalize(0, -10);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
    }

    @Test
    public void testNormalizedScaleLimits() {
        final int MAX_SCALE_DIFF = 19;

        // Test below limit
        assertEquals(Long.MAX_VALUE, ts1.normalize(0, -MAX_SCALE_DIFF + 1).getValue());
        assertEquals(0, ts1.normalize(0, +MAX_SCALE_DIFF - 1).getValue());

        // Test at limit
        assertEquals(Long.MAX_VALUE, ts1.normalize(0, -MAX_SCALE_DIFF).getValue());
        assertEquals(0, ts1.normalize(0, +MAX_SCALE_DIFF).getValue());

        // Test over limit
        assertEquals(Long.MAX_VALUE, ts1.normalize(0, -MAX_SCALE_DIFF - 1).getValue());
        assertEquals(0, ts1.normalize(0, +MAX_SCALE_DIFF - 1).getValue());
    }

    @Test
    public void testNormalizeOffsetAndScaleTrivial() {
        final ITmfTimestamp ts = ts0.normalize(0, 0);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale());
    }

    @Test
    public void testNormalizeOffsetAndScale() {
        final int SCALE = 12;

        ITmfTimestamp ts = ts1.normalize(0, SCALE);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 0, ts.getScale()); // zeroed

        ts = ts1.normalize(12345, SCALE);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());

        ts = ts1.normalize(10, SCALE);
        assertEquals("getValue", 10, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());

        ts = ts1.normalize(-10, SCALE);
        assertEquals("getValue", -10, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
    }

    @Test
    public void testNormalizeOffsetAndScale2() {
        int SCALE = 2;
        ITmfTimestamp ts = ts1.normalize(0, SCALE);
        assertEquals("getValue", 123, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());

        ts = ts1.normalize(12345, SCALE);
        assertEquals("getValue", 12468, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());

        SCALE = -2;
        ts = ts1.normalize(0, SCALE);
        assertEquals("getValue", 1234500, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());

        ts = ts1.normalize(67, SCALE);
        assertEquals("getValue", 1234567, ts.getValue());
        assertEquals("getscale", SCALE, ts.getScale());
    }

    @Test
    public void testToNanos() {
        assertEquals(12345000000000L, ts1.toNanos());
        assertEquals(-12345, ts9.toNanos());
        assertEquals(Long.MAX_VALUE, ts10.toNanos());
        assertEquals(Long.MIN_VALUE, ts11.toNanos());
    }

    // ------------------------------------------------------------------------
    // compareTo
    // ------------------------------------------------------------------------

    @Test
    public void testBasicCompareTo() {
        final ITmfTimestamp t1 = TmfTimestamp.create(900, 0);
        final ITmfTimestamp t2 = TmfTimestamp.create(1000, 0);
        final ITmfTimestamp t3 = TmfTimestamp.create(1100, 0);
        final ITmfTimestamp t4 = TmfTimestamp.create(1000, 0);

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
        final ITmfTimestamp ts0a = TmfTimestamp.create(ts0.getValue(), ts0.getScale());
        final ITmfTimestamp ts0b = TmfTimestamp.create(ts0.getValue(), ts0.getScale() + 1);
        final ITmfTimestamp ts0c = TmfTimestamp.create(ts0.getValue() + 1, ts0.getScale());
        final ITmfTimestamp ts0d = TmfTimestamp.create(ts0.getValue() + 1, ts0.getScale() + 1);

        assertTrue("compareTo", ts0.compareTo(ts0) == 0);
        assertTrue("compareTo", ts0.compareTo(ts0a) == 0);
        assertTrue("compareTo", ts0.compareTo(ts0b) == 0);
        assertTrue("compareTo", ts0.compareTo(ts0c) == -1);
        assertTrue("compareTo", ts0.compareTo(ts0d) == -1);
    }

    @Test
    public void testCompareToCornerCases2() {
        final ITmfTimestamp ts0a = TmfTimestamp.create(Long.MAX_VALUE, Integer.MAX_VALUE - 1);
        final ITmfTimestamp ts0b = TmfTimestamp.create(0, Integer.MAX_VALUE);
        final ITmfTimestamp ts0c = TmfTimestamp.create(Long.MAX_VALUE, Integer.MAX_VALUE);

        assertTrue("compareTo", ts0a.compareTo(ts0b) > 0);
        assertTrue("compareTo", ts0a.compareTo(ts0c) < 0);

        assertTrue("compareTo", ts0b.compareTo(ts0a) < 0);
        assertTrue("compareTo", ts0b.compareTo(ts0c) < 0);

        assertTrue("compareTo", ts0c.compareTo(ts0a) > 0);
        assertTrue("compareTo", ts0c.compareTo(ts0b) > 0);
    }

    @Test
    public void testCompareToCornerCases3() {
        final ITmfTimestamp ts0a = TmfTimestamp.create(Long.MIN_VALUE, Integer.MAX_VALUE - 1);
        final ITmfTimestamp ts0b = TmfTimestamp.create(0, Integer.MAX_VALUE);
        final ITmfTimestamp ts0c = TmfTimestamp.create(Long.MIN_VALUE, Integer.MAX_VALUE);

        assertTrue("compareTo", ts0a.compareTo(ts0b) < 0);
        assertTrue("compareTo", ts0a.compareTo(ts0c) > 0);

        assertTrue("compareTo", ts0b.compareTo(ts0a) > 0);
        assertTrue("compareTo", ts0b.compareTo(ts0c) > 0);

        assertTrue("compareTo", ts0c.compareTo(ts0a) < 0);
        assertTrue("compareTo", ts0c.compareTo(ts0b) < 0);
    }

    @Test(expected = NullPointerException.class)
    public void testCompareToCornerCases4() {
        assertTrue("compareTo", ts0.compareTo(null) > 0);
    }

    @Test
    public void testCompareToSameScale() {
        final ITmfTimestamp t1 = TmfTimestamp.create(900, 0);
        final ITmfTimestamp t2 = TmfTimestamp.create(1000, 0);
        final ITmfTimestamp t3 = TmfTimestamp.create(1100, 0);
        final ITmfTimestamp t4 = TmfTimestamp.create(1000, 0);

        assertEquals(0, t1.compareTo(t1));

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
    public void testCompareToDifferentScale() {
        final ITmfTimestamp t1 = TmfTimestamp.create(9000, -1);
        final ITmfTimestamp t2 = TmfTimestamp.create(1000, 0);
        final ITmfTimestamp t3 = TmfTimestamp.create(110, 1);
        final ITmfTimestamp t4 = TmfTimestamp.create(1, 3);

        assertTrue("CompareTo", t1.compareTo(t1) == 0);

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
    public void testCompareToDifferentScale2() {
        final ITmfTimestamp t5 = TmfTimestamp.fromSeconds(1);
        final ITmfTimestamp t6 = TmfTimestamp.fromMillis(1234);

        assertTrue("CompareTo", t5.compareTo(t6) < 0);
        assertTrue("CompareTo", t6.compareTo(t5) > 0);
    }

    @Test
    public void testCompareToSpecials() {
        ITmfTimestamp ersatzBigBang = TmfTimestamp.create(Long.MIN_VALUE, Integer.MAX_VALUE);
        ITmfTimestamp ersatzBigCrunch = TmfTimestamp.create(Long.MAX_VALUE, Integer.MAX_VALUE);

        ITmfTimestamp lolo = TmfTimestamp.fromMicros(Long.MIN_VALUE);
        ITmfTimestamp lo = TmfTimestamp.fromMillis(-100);
        ITmfTimestamp lohi = TmfTimestamp.fromMicros(100);
        ITmfTimestamp hilo = TmfTimestamp.fromMillis(Long.MIN_VALUE);
        ITmfTimestamp hi = TmfTimestamp.fromMillis(100);

        assertTrue("CompareTo", TmfTimestamp.BIG_BANG.compareTo(ts0) < 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_BANG.compareTo(ts1) < 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_BANG.compareTo(ts2) < 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_BANG.compareTo(ts3) < 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_BANG.compareTo(ts4) < 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_BANG.compareTo(TmfTimestamp.fromSeconds(Long.MIN_VALUE)) < 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_BANG.compareTo(ersatzBigBang) < 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_BANG.compareTo(TmfTimestamp.BIG_CRUNCH) < 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_BANG.compareTo(TmfTimestamp.BIG_BANG) == 0);
        assertTrue("CompareTo", ts0.compareTo(TmfTimestamp.BIG_BANG) > 0);
        assertTrue("CompareTo", ts1.compareTo(TmfTimestamp.BIG_BANG) > 0);
        assertTrue("CompareTo", ts2.compareTo(TmfTimestamp.BIG_BANG) > 0);
        assertTrue("CompareTo", ts3.compareTo(TmfTimestamp.BIG_BANG) > 0);
        assertTrue("CompareTo", ts4.compareTo(TmfTimestamp.BIG_BANG) > 0);
        assertTrue("CompareTo", ersatzBigBang.compareTo(TmfTimestamp.BIG_BANG) > 0);

        assertTrue("CompareTo", TmfTimestamp.BIG_CRUNCH.compareTo(ts0) > 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_CRUNCH.compareTo(ts1) > 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_CRUNCH.compareTo(ts2) > 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_CRUNCH.compareTo(ts3) > 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_CRUNCH.compareTo(ts4) > 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_CRUNCH.compareTo(TmfTimestamp.fromSeconds(Long.MIN_VALUE)) > 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_CRUNCH.compareTo(ersatzBigCrunch) > 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_CRUNCH.compareTo(TmfTimestamp.BIG_BANG) > 0);
        assertTrue("CompareTo", TmfTimestamp.BIG_CRUNCH.compareTo(TmfTimestamp.BIG_CRUNCH) == 0);
        assertTrue("CompareTo", ts0.compareTo(TmfTimestamp.BIG_CRUNCH) < 0);
        assertTrue("CompareTo", ts1.compareTo(TmfTimestamp.BIG_CRUNCH) < 0);
        assertTrue("CompareTo", ts2.compareTo(TmfTimestamp.BIG_CRUNCH) < 0);
        assertTrue("CompareTo", ts3.compareTo(TmfTimestamp.BIG_CRUNCH) < 0);
        assertTrue("CompareTo", ts4.compareTo(TmfTimestamp.BIG_CRUNCH) < 0);
        assertTrue("CompareTo", ersatzBigCrunch.compareTo(TmfTimestamp.BIG_CRUNCH) < 0);

        assertTrue("CompareTo", ersatzBigBang.compareTo(ersatzBigCrunch) < 0);
        assertTrue("CompareTo", ersatzBigCrunch.compareTo(ersatzBigBang) > 0);
        assertTrue("CompareTo", ersatzBigBang.compareTo(ersatzBigBang) == 0);

        assertTrue("CompareTo", lolo.compareTo(hi) < 0);
        assertTrue("CompareTo", hi.compareTo(lolo) > 0);
        assertTrue("CompareTo", lolo.compareTo(lolo) == 0);

        assertTrue("CompareTo", lo.compareTo(hi) < 0);
        assertTrue("CompareTo", hi.compareTo(lo) > 0);
        assertTrue("CompareTo", lo.compareTo(lo) == 0);

        assertTrue("CompareTo", hilo.compareTo(lohi) < 0);
        assertTrue("CompareTo", lohi.compareTo(hilo) > 0);
        assertTrue("CompareTo", hilo.compareTo(hilo) == 0);

    }

    @Test
    public void testCompareToLargeScale1() {
        final ITmfTimestamp t1 = TmfTimestamp.create(-1, 100);
        final ITmfTimestamp t2 = TmfTimestamp.create(-1000, -100);
        final ITmfTimestamp t3 = TmfTimestamp.create(1, 100);
        final ITmfTimestamp t4 = TmfTimestamp.create(1000, -100);

        assertTrue("CompareTo", t1.compareTo(t2) < 0);
        assertTrue("CompareTo", t1.compareTo(t3) < 0);
        assertTrue("CompareTo", t1.compareTo(t4) < 0);

        assertTrue("CompareTo", t2.compareTo(t1) > 0);
        assertTrue("CompareTo", t2.compareTo(t3) < 0);
        assertTrue("CompareTo", t2.compareTo(t4) < 0);

        assertTrue("CompareTo", t3.compareTo(t1) > 0);
        assertTrue("CompareTo", t3.compareTo(t2) > 0);
        assertTrue("CompareTo", t3.compareTo(t4) > 0);

        assertTrue("CompareTo", t4.compareTo(t1) > 0);
        assertTrue("CompareTo", t4.compareTo(t2) > 0);
        assertTrue("CompareTo", t4.compareTo(t3) < 0);
    }

    @Test
    public void testCompareToLargeScale2() {
        final ITmfTimestamp ts0a = TmfTimestamp.create(0, Integer.MAX_VALUE);
        final ITmfTimestamp ts0b = TmfTimestamp.create(1, Integer.MAX_VALUE);

        assertEquals("CompareTo", 0, ts0a.compareTo(ts0));
        assertEquals("CompareTo", 0, ts0.compareTo(ts0a));

        assertEquals("CompareTo", 1, ts0b.compareTo(ts0));
        assertEquals("CompareTo", -1, ts0.compareTo(ts0b));
    }

    @Test
    public void testCompareToLargeScale3() {
        final ITmfTimestamp ts0a = TmfTimestamp.create(1, Integer.MAX_VALUE);
        final ITmfTimestamp ts0b = TmfTimestamp.create(2, Integer.MAX_VALUE);

        assertTrue("CompareTo", ts0b.compareTo(ts0a) > 0);
        assertTrue("CompareTo", ts0a.compareTo(ts0b) < 0);
    }

    // ------------------------------------------------------------------------
    // getDelta
    // ------------------------------------------------------------------------

    @Test
    public void testDelta() {
        // Delta for same scale and precision (delta > 0)
        ITmfTimestamp t0 = TmfTimestamp.create(10, 9);
        ITmfTimestamp t1 = TmfTimestamp.create(5, 9);
        ITmfTimestamp exp = TmfTimestamp.create(5, 9);

        ITmfTimestamp delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp));

        // Delta for same scale and precision (delta < 0)
        t0 = TmfTimestamp.create(5, 9);
        t1 = TmfTimestamp.create(10, 9);
        exp = TmfTimestamp.create(-5, 9);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp));

        // Delta for different scale and same precision (delta > 0)
        t0 = TmfTimestamp.create(5, 9);
        t1 = TmfTimestamp.create(10, 8);
        exp = TmfTimestamp.create(4, 9);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp));

        // Delta for different scale and same precision (delta > 0)
        t0 = TmfTimestamp.create(5, 9);
        t1 = TmfTimestamp.create(10, 7);
        exp = TmfTimestamp.create(5, 9);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp));

        // Delta for different scale and same precision
        t0 = TmfTimestamp.create(10, 9);
        t1 = TmfTimestamp.create(5, 8);
        exp = TmfTimestamp.create(10, 9);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp));

        // Delta for same scale
        t0 = TmfTimestamp.create(10, 9);
        t1 = TmfTimestamp.create(5, 9);
        exp = TmfTimestamp.create(5, 9);

        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp));

        // Delta for different scale
        t0 = TmfTimestamp.create(5, 9);
        t1 = TmfTimestamp.create(10, 8);
        exp = TmfTimestamp.create(4, 9);
        delta = t0.getDelta(t1);
        assertEquals("getDelta", 0, delta.compareTo(exp));
    }

}
