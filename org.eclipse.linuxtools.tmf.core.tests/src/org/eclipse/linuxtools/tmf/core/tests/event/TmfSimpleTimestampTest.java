/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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
import org.eclipse.linuxtools.tmf.core.event.TmfSimpleTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * <b><u>TmfSimpleTimestampTest</u></b>
 * <p>
 * Test suite for the TmfSimpleTimestampTest class.
 */
@SuppressWarnings("nls")
public class TmfSimpleTimestampTest extends TestCase {

    // ------------------------------------------------------------------------
    // Variables
    // ------------------------------------------------------------------------

    private final ITmfTimestamp ts0 = new TmfSimpleTimestamp();
    private final ITmfTimestamp ts1 = new TmfSimpleTimestamp(12345);
    private final ITmfTimestamp ts2 = new TmfSimpleTimestamp(-1234);

    // ------------------------------------------------------------------------
    // Housekeping
    // ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
    public TmfSimpleTimestampTest(final String name) {
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

    public void testFullConstructor() throws Exception {
        assertEquals("getValue", 12345, ts1.getValue());
        assertEquals("getscale", 0, ts1.getScale());
        assertEquals("getPrecision", 0, ts1.getPrecision());
    }

    public void testCopyConstructor() throws Exception {
        final ITmfTimestamp copy = new TmfSimpleTimestamp(ts1);

        assertEquals("getValue", ts1.getValue(), copy.getValue());
        assertEquals("getscale", ts1.getScale(), copy.getScale());
        assertEquals("getPrecision", ts1.getPrecision(), copy.getPrecision());

        assertEquals("getValue", 12345, copy.getValue());
        assertEquals("getscale", 0, copy.getScale());
        assertEquals("getPrecision", 0, copy.getPrecision());
    }

    public void testCopyBadTimestamp() throws Exception {
        final ITmfTimestamp ts0a = new TmfTimestamp(0, 1, 0);
        final ITmfTimestamp ts0b = new TmfTimestamp(0, 0, 1);

        try {
            new TmfSimpleTimestamp(null);
            fail("TmfSimpleTimestamp: null argument");
        } catch (final IllegalArgumentException e) {
        }

        try {
            new TmfSimpleTimestamp(ts0a);
            fail("TmfSimpleTimestamp: bad scale");
        } catch (final IllegalArgumentException e) {
        }

        try {
            new TmfSimpleTimestamp(ts0b);
            fail("TmfSimpleTimestamp: bad precision");
        } catch (final IllegalArgumentException e) {
        }
    }

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

    public static class MyTimestamp extends TmfSimpleTimestamp {

        @Override
        public boolean equals(final Object other) {
            return super.equals(other);
        }

        @Override
        public MyTimestamp clone() {
            return (MyTimestamp) super.clone();
        }
    }

    public void testClone() throws Exception {
        final ITmfTimestamp clone = ts0.clone();

        assertTrue("clone", ts0.clone().equals(ts0));
        assertTrue("clone", clone.clone().equals(clone));

        assertEquals("clone", clone, ts0);
        assertEquals("clone", ts0, clone);
    }

    public void testClone2() throws Exception {
        final MyTimestamp timestamp = new MyTimestamp();
        final MyTimestamp clone = timestamp.clone();

        assertTrue("clone", timestamp.clone().equals(timestamp));
        assertTrue("clone", clone.clone().equals(clone));

        assertEquals("clone", clone, timestamp);
        assertEquals("clone", timestamp, clone);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    public void testEqualsReflexivity() throws Exception {
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

    public void testEqualsSymmetry() throws Exception {
        final ITmfTimestamp ts0copy = new TmfSimpleTimestamp(ts0);
        assertTrue("equals", ts0.equals(ts0copy));
        assertTrue("equals", ts0copy.equals(ts0));

        final ITmfTimestamp ts1copy = new TmfSimpleTimestamp(ts1);
        assertTrue("equals", ts1.equals(ts1copy));
        assertTrue("equals", ts1copy.equals(ts1));
    }

    public void testEqualsTransivity() throws Exception {
        final ITmfTimestamp ts0copy1 = new TmfSimpleTimestamp(ts0);
        final ITmfTimestamp ts0copy2 = new TmfSimpleTimestamp(ts0copy1);
        assertTrue("equals", ts0.equals(ts0copy1));
        assertTrue("equals", ts0copy1.equals(ts0copy2));
        assertTrue("equals", ts0.equals(ts0copy2));

        final ITmfTimestamp ts1copy1 = new TmfSimpleTimestamp(ts1);
        final ITmfTimestamp ts1copy2 = new TmfSimpleTimestamp(ts1copy1);
        assertTrue("equals", ts1.equals(ts1copy1));
        assertTrue("equals", ts1copy1.equals(ts1copy2));
        assertTrue("equals", ts1.equals(ts1copy2));
    }

    public void testEqualsNull() throws Exception {
        assertTrue("equals", !ts0.equals(null));
        assertTrue("equals", !ts1.equals(null));
        assertTrue("equals", !ts2.equals(null));
    }

    public void testEqualsNonTimestamp() throws Exception {
        assertFalse("equals", ts0.equals(ts0.toString()));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    public void testToString() throws Exception {
        assertEquals("toString", "TmfSimpleTimestamp [fValue=0]", ts0.toString());
        assertEquals("toString", "TmfSimpleTimestamp [fValue=12345]", ts1.toString());
        assertEquals("toString", "TmfSimpleTimestamp [fValue=-1234]", ts2.toString());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() throws Exception {
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

    public void testNormalizeScale0() throws Exception {
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

    public void testNormalizeScaleNot0() throws Exception {
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

    public void testBasicCompareTo() throws Exception {
        final ITmfTimestamp ts1 = new TmfSimpleTimestamp(900);
        final ITmfTimestamp ts2 = new TmfSimpleTimestamp(1000);
        final ITmfTimestamp ts3 = new TmfSimpleTimestamp(1100);

        assertTrue(ts1.compareTo(ts1) == 0);

        assertTrue("CompareTo", ts1.compareTo(ts2) < 0);
        assertTrue("CompareTo", ts1.compareTo(ts3) < 0);

        assertTrue("CompareTo", ts2.compareTo(ts1) > 0);
        assertTrue("CompareTo", ts2.compareTo(ts3) < 0);

        assertTrue("CompareTo", ts3.compareTo(ts1) > 0);
        assertTrue("CompareTo", ts3.compareTo(ts2) > 0);
    }

    public void testCompareTo() throws Exception {
        final ITmfTimestamp ts0a = new TmfTimestamp(0, 2, 0);
        final ITmfTimestamp ts1a = new TmfTimestamp(123450, -1);
        final ITmfTimestamp ts2a = new TmfTimestamp(-12340, -1);

        assertTrue(ts1.compareTo(ts1) == 0);

        assertTrue("CompareTo", ts0.compareTo(ts0a) == 0);
        assertTrue("CompareTo", ts1.compareTo(ts1a) == 0);
        assertTrue("CompareTo", ts2.compareTo(ts2a) == 0);
    }

    // ------------------------------------------------------------------------
    // getDelta
    // ------------------------------------------------------------------------

    public void testDelta() throws Exception {
        // Delta for same scale and precision (delta > 0)
        TmfTimestamp ts0 = new TmfSimpleTimestamp(10);
        TmfTimestamp ts1 = new TmfSimpleTimestamp(5);
        TmfTimestamp exp = new TmfSimpleTimestamp(5);

        ITmfTimestamp delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));

        // Delta for same scale and precision (delta < 0)
        ts0 = new TmfTimestamp(5);
        ts1 = new TmfTimestamp(10);
        exp = new TmfTimestamp(-5);

        delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));
    }

    public void testDelta2() throws Exception {
        // Delta for different scale and same precision (delta > 0)
        final TmfTimestamp ts0 = new TmfSimpleTimestamp(10);
        final TmfTimestamp ts1 = new TmfTimestamp(1, 1);
        final TmfTimestamp exp = new TmfTimestamp(0, 0);

        final ITmfTimestamp delta = ts0.getDelta(ts1);
        assertEquals("getDelta", 0, delta.compareTo(exp, false));
    }

}
