/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
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
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;

/**
 * <b><u>TmfTimeRangeTest</u></b>
 * <p>
 * Test suite for the TmfTimeRange class.
 */
@SuppressWarnings("nls")
public class TmfTimeRangeTest extends TestCase {

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    public TmfTimeRangeTest(final String name) {
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

    public void testConstructor() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range = new TmfTimeRange(ts1, ts2);

        assertEquals("startTime", ts1, range.getStartTime());
        assertEquals("endTime", ts2, range.getEndTime());
    }

    public void testBadConstructor() throws Exception {
        try {
            new TmfTimeRange(TmfTimestamp.BIG_BANG, null);
            fail("TmfTimeRange: bad end time");
        } catch (final IllegalArgumentException e) {
            // Success
        }

        try {
            new TmfTimeRange(null, TmfTimestamp.BIG_CRUNCH);
            fail("TmfTimeRange: bad start time");
        } catch (final IllegalArgumentException e) {
            // Success
        }
    }

    public void testOpenRange1() throws Exception {
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, ts2);

        assertEquals("startTime", TmfTimestamp.BIG_BANG, range.getStartTime());
        assertEquals("endTime", ts2, range.getEndTime());
    }

    public void testOpenRange2() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final TmfTimeRange range = new TmfTimeRange(ts1, TmfTimestamp.BIG_CRUNCH);

        assertEquals("startTime", ts1, range.getStartTime());
        assertEquals("endTime", TmfTimestamp.BIG_CRUNCH, range.getEndTime());
    }

    public void testOpenRange3() throws Exception {
        final TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);

        assertEquals("startTime", TmfTimestamp.BIG_BANG, range.getStartTime());
        assertEquals("endTime", TmfTimestamp.BIG_CRUNCH, range.getEndTime());
    }

    public void testCopyConstructor() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range0 = new TmfTimeRange(ts1, ts2);
        final TmfTimeRange range1 = new TmfTimeRange(range0);

        assertEquals("startTime", ts1, range1.getStartTime());
        assertEquals("endTime", ts2, range1.getEndTime());

        final TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfTimeRange range3 = new TmfTimeRange(range2);

        assertEquals("startTime", TmfTimestamp.BIG_BANG, range3.getStartTime());
        assertEquals("endTime", TmfTimestamp.BIG_CRUNCH, range3.getEndTime());
    }

    public void testCopyConstructor2() throws Exception {
        try {
            new TmfTimeRange(null);
            fail("TmfTimeRange: null argument");
        } catch (final IllegalArgumentException e) {
            // Success
        }
    }

    // ------------------------------------------------------------------------
    // clone
    // ------------------------------------------------------------------------

    public void testClone() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);

        final TmfTimeRange range = new TmfTimeRange(ts1, ts2);
        final TmfTimeRange clone = range.clone();

        assertTrue("clone", range.clone().equals(range));
        assertTrue("clone", clone.clone().equals(clone));

        assertEquals("clone", range, clone);
        assertEquals("clone", ts1, clone.getStartTime());
        assertEquals("clone", ts2, clone.getEndTime());
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    public void testHashCode() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range1 = new TmfTimeRange(ts1, ts2);
        final TmfTimeRange range1b = new TmfTimeRange(range1);
        final TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfTimeRange range2b = new TmfTimeRange(range2);

        assertTrue("hashCode", range1.hashCode() == range1b.hashCode());
        assertTrue("hashCode", range2.hashCode() == range2b.hashCode());

        assertTrue("hashCode", range1.hashCode() != range2.hashCode());
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    public void testEqualsReflexivity() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range1 = new TmfTimeRange(ts1, ts2);
        final TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);

        assertTrue("equals", range1.equals(range1));
        assertTrue("equals", range2.equals(range2));

        assertTrue("equals", !range1.equals(range2));
        assertTrue("equals", !range2.equals(range1));
    }

    public void testEqualsSymmetry() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range1a = new TmfTimeRange(ts1, ts2);
        final TmfTimeRange range1b = new TmfTimeRange(ts1, ts2);

        final TmfTimeRange range2a = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfTimeRange range2b = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);

        assertTrue("equals", range1a.equals(range1b));
        assertTrue("equals", range1b.equals(range1a));

        assertTrue("equals", range2a.equals(range2b));
        assertTrue("equals", range2b.equals(range2a));
    }

    public void testEqualsTransivity() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range1a = new TmfTimeRange(ts1, ts2);
        final TmfTimeRange range1b = new TmfTimeRange(ts1, ts2);
        final TmfTimeRange range1c = new TmfTimeRange(ts1, ts2);

        assertTrue("equals", range1a.equals(range1b));
        assertTrue("equals", range1b.equals(range1c));
        assertTrue("equals", range1a.equals(range1c));
    }

    public void testEqualsNull() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range1 = new TmfTimeRange(ts1, ts2);

        assertTrue("equals", !range1.equals(null));
    }

    public void testEqualsBadType() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range1 = new TmfTimeRange(ts1, ts2);

        assertTrue("equals", !range1.equals(ts1));
    }

    public void testEqualStartTime() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final ITmfTimestamp ts3 = new TmfTimestamp(12355);

        final TmfTimeRange range1 = new TmfTimeRange(ts1, ts3);
        final TmfTimeRange range2 = new TmfTimeRange(ts2, ts3);
        final TmfTimeRange range3 = new TmfTimeRange(ts1, ts2);

        assertTrue("equals", !range1.equals(range2));
        assertTrue("equals", !range1.equals(range3));
    }

    public void testEqualsEndTime() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final ITmfTimestamp ts3 = new TmfTimestamp(12355);

        final TmfTimeRange range1 = new TmfTimeRange(ts1, ts2);
        final TmfTimeRange range2 = new TmfTimeRange(ts1, ts3);
        final TmfTimeRange range3 = new TmfTimeRange(ts2, ts3);

        assertTrue("equals", !range1.equals(range2));
        assertTrue("equals", !range1.equals(range3));
    }

    // ------------------------------------------------------------------------
    // toString
    // ------------------------------------------------------------------------

    public void testToString() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range = new TmfTimeRange(ts1, ts2);

        final String expected = "TmfTimeRange [fStartTime=" + ts1 + ", fEndTime=" + ts2 + "]";
        assertEquals("toString", expected, range.toString());
    }

    // ------------------------------------------------------------------------
    // contains
    // ------------------------------------------------------------------------

    public void testContainsTimestamp() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(12345);
        final ITmfTimestamp ts2 = new TmfTimestamp(12350);
        final TmfTimeRange range = new TmfTimeRange(ts1, ts2);

        assertTrue("contains (lower bound)", range.contains(new TmfTimestamp(12345)));
        assertTrue("contains (higher bound)", range.contains(new TmfTimestamp(12350)));
        assertTrue("contains (within bounds)", range.contains(new TmfTimestamp(12346)));

        assertFalse("contains (low value)", range.contains(new TmfTimestamp(12340)));
        assertFalse("contains (high value)", range.contains(new TmfTimestamp(12351)));

        assertTrue("contains (zero)", range.contains(TmfTimestamp.ZERO));
    }

    public void testContainsRange() throws Exception {
        final ITmfTimestamp ts1 = new TmfTimestamp(10);
        final ITmfTimestamp ts2 = new TmfTimestamp(20);
        final ITmfTimestamp ts3 = new TmfTimestamp(30);
        final ITmfTimestamp ts4 = new TmfTimestamp(40);
        final ITmfTimestamp ts5 = new TmfTimestamp(50);
        final ITmfTimestamp ts6 = new TmfTimestamp(60);
        final ITmfTimestamp ts7 = new TmfTimestamp(70);
        final ITmfTimestamp ts8 = new TmfTimestamp(80);

        // Reference range
        final TmfTimeRange range0 = new TmfTimeRange(ts3, ts6);

        // Start time below range
        final TmfTimeRange range1 = new TmfTimeRange(ts1, ts2);
        final TmfTimeRange range2 = new TmfTimeRange(ts2, ts3);
        final TmfTimeRange range3 = new TmfTimeRange(ts2, ts4);
        final TmfTimeRange range4 = new TmfTimeRange(ts2, ts6);
        final TmfTimeRange range5 = new TmfTimeRange(ts2, ts7);

        assertFalse("contains", range0.contains(range1));
        assertFalse("contains", range0.contains(range2));
        assertFalse("contains", range0.contains(range3));
        assertFalse("contains", range0.contains(range4));
        assertFalse("contains", range0.contains(range5));

        // End time above range
        final TmfTimeRange range6 = new TmfTimeRange(ts3, ts7);
        final TmfTimeRange range7 = new TmfTimeRange(ts4, ts7);
        final TmfTimeRange range8 = new TmfTimeRange(ts6, ts7);
        final TmfTimeRange range9 = new TmfTimeRange(ts7, ts8);

        assertFalse("contains", range0.contains(range6));
        assertFalse("contains", range0.contains(range7));
        assertFalse("contains", range0.contains(range8));
        assertFalse("contains", range0.contains(range9));

        // Within range
        final TmfTimeRange range10 = new TmfTimeRange(ts3, ts4);
        final TmfTimeRange range11 = new TmfTimeRange(ts3, ts6);
        final TmfTimeRange range12 = new TmfTimeRange(ts4, ts5);
        final TmfTimeRange range13 = new TmfTimeRange(ts4, ts6);

        assertTrue("contains", range0.contains(range10));
        assertTrue("contains", range0.contains(range11));
        assertTrue("contains", range0.contains(range12));
        assertTrue("contains", range0.contains(range13));
    }

    // ------------------------------------------------------------------------
    // getIntersection
    // ------------------------------------------------------------------------

    public void testGetIntersection() throws Exception {

        final ITmfTimestamp ts1a = new TmfTimestamp(1000);
        final ITmfTimestamp ts1b = new TmfTimestamp(2000);
        final TmfTimeRange range1 = new TmfTimeRange(ts1a, ts1b);

        final ITmfTimestamp ts2a = new TmfTimestamp(2000);
        final ITmfTimestamp ts2b = new TmfTimestamp(3000);
        final TmfTimeRange range2 = new TmfTimeRange(ts2a, ts2b);

        final ITmfTimestamp ts3a = new TmfTimestamp(3000);
        final ITmfTimestamp ts3b = new TmfTimestamp(4000);
        final TmfTimeRange range3 = new TmfTimeRange(ts3a, ts3b);

        final ITmfTimestamp ts4a = new TmfTimestamp(1500);
        final ITmfTimestamp ts4b = new TmfTimestamp(2500);
        final TmfTimeRange range4 = new TmfTimeRange(ts4a, ts4b);

        final ITmfTimestamp ts5a = new TmfTimestamp(1500);
        final ITmfTimestamp ts5b = new TmfTimestamp(2000);
        final TmfTimeRange range5 = new TmfTimeRange(ts5a, ts5b);

        final ITmfTimestamp ts6a = new TmfTimestamp(2000);
        final ITmfTimestamp ts6b = new TmfTimestamp(2500);
        final TmfTimeRange range6 = new TmfTimeRange(ts6a, ts6b);

        final ITmfTimestamp ts7a = new TmfTimestamp(1500);
        final ITmfTimestamp ts7b = new TmfTimestamp(3500);
        final TmfTimeRange range7 = new TmfTimeRange(ts7a, ts7b);

        final ITmfTimestamp ts8a = new TmfTimestamp(2250);
        final ITmfTimestamp ts8b = new TmfTimestamp(2750);
        final TmfTimeRange range8 = new TmfTimeRange(ts8a, ts8b);

        assertEquals("getIntersection (below - not contiguous)", null, range1.getIntersection(range3));
        assertEquals("getIntersection (above - not contiguous)", null, range3.getIntersection(range1));

        assertEquals("getIntersection (below - contiguous)", new TmfTimeRange(ts1b, ts1b), range1.getIntersection(range2));
        assertEquals("getIntersection (above - contiguous)", new TmfTimeRange(ts3a, ts3a), range3.getIntersection(range2));

        assertEquals("getIntersection (below - overlap)", new TmfTimeRange(ts2a, ts4b), range2.getIntersection(range4));
        assertEquals("getIntersection (above - overlap)", new TmfTimeRange(ts2a, ts4b), range4.getIntersection(range2));

        assertEquals("getIntersection (within - overlap1)", range6, range2.getIntersection(range6));
        assertEquals("getIntersection (within - overlap2)", range6, range6.getIntersection(range2));

        assertEquals("getIntersection (within - overlap3)", range5, range1.getIntersection(range5));
        assertEquals("getIntersection (within - overlap4)", range5, range5.getIntersection(range1));

        assertEquals("getIntersection (within - overlap5)", range8, range2.getIntersection(range8));
        assertEquals("getIntersection (within - overlap6)", range8, range8.getIntersection(range2));

        assertEquals("getIntersection (accross1)", range2, range2.getIntersection(range7));
        assertEquals("getIntersection (accross2)", range2, range7.getIntersection(range2));
    }

}
