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

package org.eclipse.linuxtools.tmf.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

/**
 * <b><u>TmfTimeRangeTest</u></b>
 * <p>
 * JUnit test suite for the TmfTimeRange class.
 */
public class TmfTimeRangeTest {

    // ========================================================================
    // Constructors
    // ========================================================================

    @Test
    public void testConstructor() throws Exception {
        TmfTimestamp ts1   = new TmfTimestamp(12345);
        TmfTimestamp ts2   = new TmfTimestamp(12350);
        TmfTimeRange range = new TmfTimeRange(ts1, ts2);
        assertEquals("startTime", ts1, range.getStartTime());
        assertEquals("endTime",   ts2, range.getEndTime());
    }

    @Test
    public void testOpenRange1() throws Exception {
        TmfTimestamp ts2    = new TmfTimestamp(12350);
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, ts2);
        assertEquals("startTime", TmfTimestamp.BigBang, range.getStartTime());
        assertEquals("endTime",   ts2, range.getEndTime());
    }

    @Test
    public void testOpenRange2() throws Exception {
        TmfTimestamp ts1   = new TmfTimestamp(12345);
        TmfTimeRange range = new TmfTimeRange(ts1, TmfTimestamp.BigCrunch);
        assertEquals("startTime", ts1, range.getStartTime());
        assertEquals("endTime",   TmfTimestamp.BigCrunch, range.getEndTime());
    }

    @Test
    public void testOpenRange3() throws Exception {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        assertEquals("startTime", TmfTimestamp.BigBang,   range.getStartTime());
        assertEquals("endTime",   TmfTimestamp.BigCrunch, range.getEndTime());
    }

    // ========================================================================
    // Constructors
    // ========================================================================

    @Test
    public void testContains() throws Exception {
        TmfTimestamp ts1   = new TmfTimestamp(12345);
        TmfTimestamp ts2   = new TmfTimestamp(12350);
        TmfTimeRange range = new TmfTimeRange(ts1, ts2);

        assertTrue("contains (lower bound)",   range.contains(new TmfTimestamp(12345)));
        assertTrue("contains (higher bound)",  range.contains(new TmfTimestamp(12350)));
        assertTrue("contains (within bounds)", range.contains(new TmfTimestamp(12346)));

        assertFalse("contains (low value)",   range.contains(new TmfTimestamp(12340)));
        assertFalse("contains (high value)",  range.contains(new TmfTimestamp(12351)));
    }

}
