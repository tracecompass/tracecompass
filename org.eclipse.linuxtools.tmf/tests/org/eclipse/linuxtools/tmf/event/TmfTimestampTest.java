/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

import static org.junit.Assert.*;

import org.junit.Test;

/**
 * <b><u>TmfTimestampTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTimestampTest {

    // ========================================================================
    // Constructors
    // ========================================================================

    @Test
    public void testDefaultConstructor() throws Exception {
        TmfTimestamp ts = new TmfTimestamp();
        assertEquals("getValue",     0, ts.getValue());
        assertEquals("getscale",     0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    @Test
    public void testSimpleConstructor() throws Exception {
        TmfTimestamp ts = new TmfTimestamp(12345, (byte) -1);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale",    -1, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    @Test
    public void testFullConstructor() throws Exception {
        TmfTimestamp ts = new TmfTimestamp(12345, (byte) 2, 5);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale",     2, ts.getScale());
        assertEquals("getPrecision", 5, ts.getPrecision());
    }

    @Test
    public void testCopyConstructor() throws Exception {
        TmfTimestamp ts0 = new TmfTimestamp(12345, (byte) 2, 5);
        TmfTimestamp ts  = new TmfTimestamp(ts0);
        assertEquals("getValue", 12345, ts.getValue());
        assertEquals("getscale",     2, ts.getScale());
        assertEquals("getPrecision", 5, ts.getPrecision());
    }

    // ========================================================================
    // BigBang, BigCrunch
    // ========================================================================

    @Test
    public void testCopyConstructorBigBang() throws Exception {
        TmfTimestamp ts = new TmfTimestamp(TmfTimestamp.BigBang);
        assertEquals("getValue",     0, ts.getValue());
        assertEquals("getscale",     0, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());
    }

    @Test
    public void testCopyConstructorBigCrunch() throws Exception {
        TmfTimestamp ts = new TmfTimestamp(TmfTimestamp.BigCrunch);
        assertEquals("getValue", Long.MAX_VALUE, ts.getValue());
        assertEquals("getscale", Byte.MAX_VALUE, ts.getScale());
        assertEquals("getPrecision",          0, ts.getPrecision());
    }

    // ========================================================================
    // toString
    // ========================================================================

    @Test
    public void testToString() throws Exception {
        TmfTimestamp ts1 = new TmfTimestamp();
        TmfTimestamp ts2  = new TmfTimestamp(1234,  (byte)  2);
        TmfTimestamp ts3  = new TmfTimestamp(1234,  (byte)  2, 1);
        TmfTimestamp ts4  = new TmfTimestamp(-1234, (byte) -1, 4);

        assertEquals("toString", "[TmfTimestamp:0,0,0]",      ts1.toString());
        assertEquals("toString", "[TmfTimestamp:1234,2,0]",   ts2.toString());
        assertEquals("toString", "[TmfTimestamp:1234,2,1]",   ts3.toString());
        assertEquals("toString", "[TmfTimestamp:-1234,-1,4]", ts4.toString());
    }

    // ========================================================================
    // synchronize
    // ========================================================================

    @Test
    public void testSynchronizeOffset() throws Exception {
        TmfTimestamp ts0 = new TmfTimestamp(1234, (byte) 0, 1);
        TmfTimestamp ts  = new TmfTimestamp();

        ts = ts0.synchronize(0, (byte) 0);
        assertEquals("getValue",  1234, ts.getValue());
        assertEquals("getscale",     0, ts.getScale());
        assertEquals("getPrecision", 1, ts.getPrecision());

        ts = ts0.synchronize(10, (byte) 0);
        assertEquals("getValue",  1244, ts.getValue());
        assertEquals("getscale",     0, ts.getScale());
        assertEquals("getPrecision", 1, ts.getPrecision());

        ts = ts0.synchronize(-10, (byte) 0);
        assertEquals("getValue",  1224, ts.getValue());
        assertEquals("getscale",     0, ts.getScale());
        assertEquals("getPrecision", 1, ts.getPrecision());
    }

    @Test
    public void testSynchronizeScale() throws Exception {
        TmfTimestamp ts0 = new TmfTimestamp(1234, (byte) 0, 12);
        TmfTimestamp ts  = new TmfTimestamp();

        ts = ts0.synchronize(0, (byte) 0);
        assertEquals("getValue",   1234, ts.getValue());
        assertEquals("getscale",      0, ts.getScale());
        assertEquals("getPrecision",  12, ts.getPrecision());

        ts = ts0.synchronize(0, (byte) 1);
        assertEquals("getValue",   123, ts.getValue());
        assertEquals("getscale",     1, ts.getScale());
        assertEquals("getPrecision", 1, ts.getPrecision());

        ts = ts0.synchronize(0, (byte) 2);
        assertEquals("getValue",    12, ts.getValue());
        assertEquals("getscale",     2, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.synchronize(0, (byte) 4);
        assertEquals("getValue",     0, ts.getValue());
        assertEquals("getscale",     4, ts.getScale());
        assertEquals("getPrecision", 0, ts.getPrecision());

        ts = ts0.synchronize(0, (byte) -2);
        assertEquals("getValue",   123400, ts.getValue());
        assertEquals("getscale",       -2, ts.getScale());
        assertEquals("getPrecision", 1200, ts.getPrecision());
    }

    @Test
    public void testSynchronizeOffsetAndScale() throws Exception {
        TmfTimestamp ts0 = new TmfTimestamp(1234, (byte) 0, 12);
        TmfTimestamp ts  = new TmfTimestamp();

        ts = ts0.synchronize(10, (byte) 1);
        assertEquals("getValue",   133, ts.getValue());
        assertEquals("getscale",     1, ts.getScale());
        assertEquals("getPrecision", 1, ts.getPrecision());

        ts = ts0.synchronize(-10, (byte) -1);
        assertEquals("getValue",   12330, ts.getValue());
        assertEquals("getscale",      -1, ts.getScale());
        assertEquals("getPrecision", 120, ts.getPrecision());
    }

    @Test
    public void testSynchronizeWithArithmeticException() throws Exception {
        TmfTimestamp bigBang   = new TmfTimestamp(TmfTimestamp.BigBang);
        TmfTimestamp bigCrunch = new TmfTimestamp(TmfTimestamp.BigCrunch);

        try {
            bigCrunch.synchronize(bigBang.getValue(), bigBang.getScale());
            fail("Exception not thrown");
        } catch (ArithmeticException e) {          
        }
    }

    // ========================================================================
    // getAdjustment
    // ========================================================================

    @Test
    public void testGetAdjustmentSameScale() throws Exception {
        TmfTimestamp ts0 = new TmfTimestamp(12345, (byte) -2);
        TmfTimestamp ts  = new TmfTimestamp(10000, (byte) -2);

        long delta = ts.getAdjustment(ts0);
        assertEquals("delta", 2345, delta);
    }

    @Test
    public void testGetAdjustmentDifferentScale() throws Exception {
        TmfTimestamp ts0 = new TmfTimestamp(12345, (byte) -2);
        TmfTimestamp ts  = new TmfTimestamp(    1, (byte)  2);

        long delta = ts.getAdjustment(ts0);
        assertEquals("delta", 2345, delta);

        delta = ts0.getAdjustment(ts);
        assertEquals("delta", 0, delta);
    }

    @Test
    public void testGetAdjustmentDifferentScaleWithException() throws Exception {
        try {
            TmfTimestamp.BigBang.getAdjustment(TmfTimestamp.BigCrunch);
            fail("ArithmeticException not thrown");
        } catch (ArithmeticException e) {
        }
    }

    // ========================================================================
    // CompareTo
    // ========================================================================

    @Test
    public void testCompareToSameScale() throws Exception {
        TmfTimestamp ts1 = new TmfTimestamp( 900, (byte) 0, 50);
        TmfTimestamp ts2 = new TmfTimestamp(1000, (byte) 0, 50);
        TmfTimestamp ts3 = new TmfTimestamp(1100, (byte) 0, 50);
        TmfTimestamp ts4 = new TmfTimestamp(1000, (byte) 0, 75);

        assertTrue(ts1.compareTo(ts1, false) == 0);

        assertTrue(ts1.compareTo(ts2, false)  < 0);
        assertTrue(ts1.compareTo(ts3, false)  < 0);
        assertTrue(ts1.compareTo(ts4, false)  < 0);

        assertTrue(ts2.compareTo(ts1, false)  > 0);
        assertTrue(ts2.compareTo(ts3, false)  < 0);
        assertTrue(ts2.compareTo(ts4, false) == 0);

        assertTrue(ts3.compareTo(ts1, false)  > 0);
        assertTrue(ts3.compareTo(ts2, false)  > 0);
        assertTrue(ts3.compareTo(ts4, false)  > 0);
    }

    @Test
    public void testCompareToDifferentScale() throws Exception {
        TmfTimestamp ts1 = new TmfTimestamp(9000, (byte) -1, 50);
        TmfTimestamp ts2 = new TmfTimestamp(1000, (byte)  0, 50);
        TmfTimestamp ts3 = new TmfTimestamp( 110, (byte)  1, 50);
        TmfTimestamp ts4 = new TmfTimestamp(   1, (byte)  3, 75);

        assertTrue(ts1.compareTo(ts1, false) == 0);

        assertTrue(ts1.compareTo(ts2, false)  < 0);
        assertTrue(ts1.compareTo(ts3, false)  < 0);
        assertTrue(ts1.compareTo(ts4, false)  < 0);

        assertTrue(ts2.compareTo(ts1, false)  > 0);
        assertTrue(ts2.compareTo(ts3, false)  < 0);
        assertTrue(ts2.compareTo(ts4, false) == 0);

        assertTrue(ts3.compareTo(ts1, false)  > 0);
        assertTrue(ts3.compareTo(ts2, false)  > 0);
        assertTrue(ts3.compareTo(ts4, false)  > 0);
    }

    @Test
    public void testCompareToWithinPrecision() throws Exception {
        TmfTimestamp ts1 = new TmfTimestamp( 900, (byte) 0, 50);
        TmfTimestamp ts2 = new TmfTimestamp(1000, (byte) 0, 50);
        TmfTimestamp ts3 = new TmfTimestamp(1100, (byte) 0, 50);
        TmfTimestamp ts4 = new TmfTimestamp(1000, (byte) 0, 75);

        assertTrue(ts1.compareTo(ts1, true) == 0);

        assertTrue(ts1.compareTo(ts2, true) == 0);
        assertTrue(ts1.compareTo(ts3, true)  < 0);
        assertTrue(ts1.compareTo(ts4, true) == 0);

        assertTrue(ts2.compareTo(ts1, true) == 0);
        assertTrue(ts2.compareTo(ts3, true) == 0);
        assertTrue(ts2.compareTo(ts4, true) == 0);

        assertTrue(ts3.compareTo(ts1, true)  > 0);
        assertTrue(ts3.compareTo(ts2, true) == 0);
        assertTrue(ts3.compareTo(ts4, true) == 0);
    }

    @Test
    public void testCompareToLargeScale() throws Exception {
        TmfTimestamp ts1 = new TmfTimestamp(   -1, (byte)  100);
        TmfTimestamp ts2 = new TmfTimestamp(-1000, (byte) -100);
        TmfTimestamp ts3 = new TmfTimestamp(    1, (byte)  100);
        TmfTimestamp ts4 = new TmfTimestamp( 1000, (byte) -100);

        assertTrue(ts1.compareTo(ts2, false) < 0);
        assertTrue(ts1.compareTo(ts3, false) < 0);
        assertTrue(ts1.compareTo(ts4, false) < 0);

        assertTrue(ts2.compareTo(ts1, false) > 0);
        assertTrue(ts2.compareTo(ts3, false) < 0);
        assertTrue(ts2.compareTo(ts4, false) < 0);

        assertTrue(ts3.compareTo(ts1, false) > 0);
        assertTrue(ts3.compareTo(ts2, false) > 0);
        assertTrue(ts3.compareTo(ts4, false) > 0);

        assertTrue(ts4.compareTo(ts1, false) > 0);
        assertTrue(ts4.compareTo(ts2, false) > 0);
        assertTrue(ts4.compareTo(ts3, false) < 0);
    }

    @Test
    public void testCompareToBigGuys() throws Exception {
        TmfTimestamp ts1 = new TmfTimestamp(-1, Byte.MAX_VALUE);
        TmfTimestamp ts2 = new TmfTimestamp(-1, Byte.MIN_VALUE);
        TmfTimestamp ts3 = new TmfTimestamp( 1, Byte.MAX_VALUE);
        TmfTimestamp ts4 = new TmfTimestamp( 1, Byte.MIN_VALUE);

        assertTrue(ts1.compareTo(TmfTimestamp.BigBang,   false) < 0);
        assertTrue(ts1.compareTo(TmfTimestamp.BigCrunch, false) < 0);

        assertTrue(ts2.compareTo(TmfTimestamp.BigBang,   false) < 0);
        assertTrue(ts2.compareTo(TmfTimestamp.BigCrunch, false) < 0);

        assertTrue(ts3.compareTo(TmfTimestamp.BigBang,   false) > 0);
        assertTrue(ts3.compareTo(TmfTimestamp.BigCrunch, false) < 0);

        assertTrue(ts4.compareTo(TmfTimestamp.BigBang,   false) > 0);
        assertTrue(ts4.compareTo(TmfTimestamp.BigCrunch, false) < 0);
    }

}
