/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Patrick Tasse - Modified from TmfSimpleTimestamp to use nanosecond scale
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
import org.eclipse.linuxtools.tmf.core.timestamp.TmfNanoTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.junit.Test;

/**
 * Test suite for the TmfNanoTimestampTest class.
 */
@SuppressWarnings("javadoc")
public class TmfNanoTimestampTest {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final ITmfTimestamp ts0 = new TmfNanoTimestamp();
    private final ITmfTimestamp ts1 = new TmfNanoTimestamp(12345);
    private final ITmfTimestamp ts2 = new TmfNanoTimestamp(-1234);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @Test
    public void testDefaultConstructor() {
        assertEquals("getValue", 0, ts0.getValue());
        assertEquals("getscale", -9, ts0.getScale());
        assertEquals("getPrecision", 0, ts0.getPrecision());
    }

    @Test
    public void testFullConstructor() {
        assertEquals("getValue", 12345, ts1.getValue());
        assertEquals("getscale", -9, ts1.getScale());
        assertEquals("getPrecision", 0, ts1.getPrecision());
    }

    @Test
    public void testCopyConstructor() {
        final ITmfTimestamp copy = new TmfNanoTimestamp(ts1);

        assertEquals("getValue", ts1.getValue(), copy.getValue());
        assertEquals("getscale", ts1.getScale(), copy.getScale());
        assertEquals("getPrecision", ts1.getPrecision(), copy.getPrecision());

        assertEquals("getValue", 12345, copy.getValue());
        assertEquals("getscale", -9, copy.getScale());
        assertEquals("getPrecision", 0, copy.getPrecision());
    }

    @Test
    public void testCopyBadTimestamp() {
        try {
            new TmfNanoTimestamp(null);
            fail("TmfNanoTimestamp: null argument");
        } catch (final NullPointerException e) {
        }
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        assertTrue("equals", ts0.equals(ts0));
        assertTrue("equals", ts1.equals(ts1));
        assertTrue("equals", ts2.equals(ts2));

        assertTrue("equals", !ts0.equals(ts1));
        assertTrue("equals", !ts0.equals(ts2));

        assertTrue("equals", !ts1.equals(ts0));
        assertTrue("equals", !ts1.equals(ts2));

        assertTrue("equals", !ts2.equals(ts0));
        assertTrue("equals", !ts2.equals(ts1));
    }

    @Test
    public void testEqualsSymmetry() {
        final ITmfTimestamp ts0copy = new TmfNanoTimestamp(ts0);
        assertTrue("equals", ts0.equals(ts0copy));
        assertTrue("equals", ts0copy.equals(ts0));

        final ITmfTimestamp ts1copy = new TmfNanoTimestamp(ts1);
        assertTrue("equals", ts1.equals(ts1copy));
        assertTrue("equals", ts1copy.equals(ts1));
    }

    @Test
    public void testEqualsTransivity() {
        final ITmfTimestamp ts0copy1 = new TmfNanoTimestamp(ts0);
        final ITmfTimestamp ts0copy2 = new TmfNanoTimestamp(ts0copy1);
        assertTrue("equals", ts0.equals(ts0copy1));
        assertTrue("equals", ts0copy1.equals(ts0copy2));
        assertTrue("equals", ts0.equals(ts0copy2));

        final ITmfTimestamp ts1copy1 = new TmfNanoTimestamp(ts1);
        final ITmfTimestamp ts1copy2 = new TmfNanoTimestamp(ts1copy1);
        assertTrue("equals", ts1.equals(ts1copy1));
        assertTrue("equals", ts1copy1.equals(ts1copy2));
        assertTrue("equals", ts1.equals(ts1copy2));
    }

    @Test
    public void testEqualsNull() {
        assertTrue("equals", !ts0.equals(null));
        assertTrue("equals", !ts1.equals(null));
        assertTrue("equals", !ts2.equals(null));
    }

    @Test
    public void testEqualsNonTimestamp() {
        assertFalse("equals", ts0.equals(ts0.toString()));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    @Test
    public void testToString() {
        DateFormat df = new SimpleDateFormat("HH:mm:ss.SSS");
        Date d0 = new Date(ts0.getValue() / 1000000);
        Date d1 = new Date(ts1.getValue() / 1000000);
        Date d2 = new Date(ts2.getValue() / 1000000 - 1);
        assertEquals("toString", df.format(d0) + " 000 000", ts0.toString());
        assertEquals("toString", df.format(d1) + " 012 345", ts1.toString());
        assertEquals("toString", df.format(d2) + " 998 766", ts2.toString());
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
    // normalize
    // ------------------------------------------------------------------------

    @Test
    public void testNormalizeScale0() {
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
    public void testNormalizeScaleNot0() {
        ITmfTimestamp ts = ts0.normalize(0, 1);
        assertEquals("getValue", 0, ts.getValue());
        assertEquals("getscale", 1, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(12345, 1);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale", 1, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(10, 1);
        assertEquals("getValue", 10, ts.getValue());
        assertEquals("getscale", 1, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.normalize(-10, 1);
        assertEquals("getValue", -10, ts.getValue());
        assertEquals("getscale", 1, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    // ------------------------------------------------------------------------
    // compareTo
    // ------------------------------------------------------------------------

    @Test
    public void testBasicCompareTo() {
        final ITmfTimestamp tstamp1 = new TmfNanoTimestamp(900);
        final ITmfTimestamp tstamp2 = new TmfNanoTimestamp(1000);
        final ITmfTimestamp tstamp3 = new TmfNanoTimestamp(1100);

        assertTrue(tstamp1.compareTo(tstamp1) == 0);

        assertTrue("CompareTo", tstamp1.compareTo(tstamp2) < 0);
        assertTrue("CompareTo", tstamp1.compareTo(tstamp3) < 0);

        assertTrue("CompareTo", tstamp2.compareTo(tstamp1) > 0);
        assertTrue("CompareTo", tstamp2.compareTo(tstamp3) < 0);

        assertTrue("CompareTo", tstamp3.compareTo(tstamp1) > 0);
        assertTrue("CompareTo", tstamp3.compareTo(tstamp2) > 0);
    }

    @Test
    public void testCompareTo() {
        final ITmfTimestamp ts0a = new TmfTimestamp(0, 2, 0);
        final ITmfTimestamp ts1a = new TmfTimestamp(123450, -10);
        final ITmfTimestamp ts2a = new TmfTimestamp(-12340, -10);

        assertTrue(ts1.compareTo(ts1) == 0);

        assertTrue("CompareTo", ts0.compareTo(ts0a) == 0);
        assertTrue("CompareTo", ts1.compareTo(ts1a) == 0);
        assertTrue("CompareTo", ts2.compareTo(ts2a) == 0);
    }

    // ------------------------------------------------------------------------
    // getDelta
    // ------------------------------------------------------------------------

    @Test
    public void testDelta() {
        // Delta for same scale and precision (delta > 0)
        TmfTimestamp tstamp0 = new TmfNanoTimestamp(10);
        TmfTimestamp tstamp1 = new TmfNanoTimestamp(5);
        TmfTimestamp expectd = new TmfNanoTimestamp(5);

        ITmfTimestamp delta = tstamp0.getDelta(tstamp1);
        assertEquals("getDelta", 0, delta.compareTo(expectd, false));

        // Delta for same scale and precision (delta < 0)
        tstamp0 = new TmfTimestamp(5);
        tstamp1 = new TmfTimestamp(10);
        expectd = new TmfTimestamp(-5);

        delta = tstamp0.getDelta(tstamp1);
        assertEquals("getDelta", 0, delta.compareTo(expectd, false));
    }

    @Test
    public void testDelta2() {
        // Delta for different scale and same precision (delta > 0)
        final TmfTimestamp tstamp0 = new TmfNanoTimestamp(10);
        final TmfTimestamp tstamp1 = new TmfTimestamp(1, -8);
        final TmfTimestamp expectd = new TmfTimestamp(0, 0);

        final ITmfTimestamp delta = tstamp0.getDelta(tstamp1);
        assertEquals("getDelta", 0, delta.compareTo(expectd, false));
    }

}
