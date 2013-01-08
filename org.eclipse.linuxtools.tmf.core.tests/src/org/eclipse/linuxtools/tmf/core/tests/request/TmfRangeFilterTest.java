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

package org.eclipse.linuxtools.tmf.core.tests.request;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.TmfRangeFilter;
import org.junit.Test;

/**
 * <b><u>TmfRangeFilterTest</u></b>
 * <p>
 * Test suite for the TmfRangeFilter class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfRangeFilterTest extends TestCase {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final TmfRangeFilter ALL_EVENTS = TmfRangeFilter.ALL_EVENTS;

    // ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
	public TmfRangeFilterTest(String name) {
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

	@Test
	public void testTmfRangeFilter() {
	    TmfRangeFilter filter = ALL_EVENTS;
        assertEquals("getTimeRange", TmfTimeRange.ETERNITY, filter.getTimeRange());

        TmfTimeRange range = TmfTimeRange.NULL_RANGE;
        filter = new TmfRangeFilter(range);
        assertEquals("getTimeRange", range, filter.getTimeRange());

        range = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_CRUNCH);
        filter = new TmfRangeFilter(range);
        assertEquals("getTimeRange", range, filter.getTimeRange());

        range = new TmfTimeRange(new TmfTimestamp(10), TmfTimestamp.BIG_CRUNCH);
        filter = new TmfRangeFilter(range);
        assertEquals("getTimeRange", range, filter.getTimeRange());
	}

    @Test
    public void testTmfRangeFilterCopy() {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_CRUNCH);
        TmfRangeFilter filter1 = new TmfRangeFilter(range);
        TmfRangeFilter filter2 = new TmfRangeFilter(filter1);

        assertEquals("getTimeRange",   filter1.getTimeRange(),   filter2.getTimeRange());
    }

    // ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        TmfTimeRange range1 = TmfTimeRange.ETERNITY;
        TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_CRUNCH);
        TmfRangeFilter filter1 = new TmfRangeFilter(range1);
        TmfRangeFilter filter2 = new TmfRangeFilter(range2);

        assertEquals("equals", filter1, filter1);
        assertEquals("equals", filter2, filter2);

        assertFalse("equals", filter1.equals(filter2));
        assertFalse("equals", filter2.equals(filter1));
    }

    @Test
    public void testEqualsSymmetry() {
        TmfTimeRange range1 = TmfTimeRange.ETERNITY;
        TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_CRUNCH);
        TmfRangeFilter filter1 = new TmfRangeFilter(range1);
        TmfRangeFilter filter2 = new TmfRangeFilter(range1);
        TmfRangeFilter filter3 = new TmfRangeFilter(range2);

        assertEquals("equals", filter1, filter2);
        assertEquals("equals", filter2, filter1);

        assertFalse("equals", filter1.equals(filter3));
        assertFalse("equals", filter3.equals(filter1));
    }

    @Test
    public void testEqualsTransivity() {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_CRUNCH);
        TmfRangeFilter filter1 = new TmfRangeFilter(range);
        TmfRangeFilter filter2 = new TmfRangeFilter(range);
        TmfRangeFilter filter3 = new TmfRangeFilter(range);

        assertEquals("equals", filter1, filter2);
        assertEquals("equals", filter2, filter3);
        assertEquals("equals", filter3, filter1);
    }

    @Test
    public void testEqualsNull() {
        TmfTimeRange range1 = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_CRUNCH);
        TmfTimeRange range2 = new TmfTimeRange(new TmfTimestamp(10), TmfTimestamp.BIG_CRUNCH);
        TmfRangeFilter filter1 = new TmfRangeFilter(range1);
        TmfRangeFilter filter2 = new TmfRangeFilter(range2);

        assertFalse("equals", filter1.equals(null));
        assertFalse("equals", filter2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        TmfTimeRange range1 = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_CRUNCH);
        TmfTimeRange range2 = new TmfTimeRange(new TmfTimestamp(10), TmfTimestamp.BIG_CRUNCH);
        TmfRangeFilter filter1 = new TmfRangeFilter(range1);
        TmfRangeFilter filter2 = new TmfRangeFilter(range2);

        assertEquals("hashCode", filter1.hashCode(), filter1.hashCode());
        assertEquals("hashCode", filter2.hashCode(), filter2.hashCode());
        assertFalse("hashCode",  filter1.hashCode() == filter2.hashCode());
    }

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

    @Test
    public void testToString() {
        TmfTimeRange range1 = new TmfTimeRange(TmfTimestamp.ZERO, TmfTimestamp.BIG_CRUNCH);
        TmfTimeRange range2 = new TmfTimeRange(new TmfTimestamp(10), new TmfTimestamp(1000));
        TmfRangeFilter filter1 = new TmfRangeFilter(range1);
        TmfRangeFilter filter2 = new TmfRangeFilter(range2);

        String expected0 = "TmfRangeFilter [fTimeRange=TmfTimeRange [fStartTime=" + TmfTimestamp.BIG_BANG + ", fEndTime=" + TmfTimestamp.BIG_CRUNCH + "]]";
        String expected1 = "TmfRangeFilter [fTimeRange=TmfTimeRange [fStartTime=" + TmfTimestamp.ZERO     + ", fEndTime=" + TmfTimestamp.BIG_CRUNCH + "]]";
        String expected2 = "TmfRangeFilter [fTimeRange=TmfTimeRange [fStartTime=" + new TmfTimestamp(10) + ", fEndTime=" +  new TmfTimestamp(1000) + "]]";

        assertEquals("toString", expected0, TmfRangeFilter.ALL_EVENTS.toString());
        assertEquals("toString", expected1, filter1.toString());
        assertEquals("toString", expected2, filter2.toString());
    }

    // ------------------------------------------------------------------------
    // matches
    // ------------------------------------------------------------------------

    /**
     * The only test that really matters...
     */
    @Test
    public void testMatches() {

        TmfTimeRange range1 = new TmfTimeRange(new TmfTimestamp(0), new TmfTimestamp(1000));
        TmfTimeRange range2 = new TmfTimeRange(new TmfTimestamp(10), new TmfTimestamp(2000));

        TmfRangeFilter filter1 = new TmfRangeFilter(range1);
        TmfRangeFilter filter2 = new TmfRangeFilter(range2);

        ITmfEvent event = new TmfEvent(null, 0, new TmfTimestamp(0), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 0, new TmfTimestamp(5), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertFalse("matches", filter2.matches(event));

        event = new TmfEvent(null, 0, new TmfTimestamp(9), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertFalse("matches", filter2.matches(event));

        event = new TmfEvent(null, 0, new TmfTimestamp(10), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 0, new TmfTimestamp(999), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 0, new TmfTimestamp(1000), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 0, new TmfTimestamp(1001), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 0, new TmfTimestamp(1999), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 0, new TmfTimestamp(2000), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 0, new TmfTimestamp(2001), null, null, null, null);
        assertTrue ("matches", TmfRangeFilter.ALL_EVENTS.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertFalse("matches", filter2.matches(event));
    }

}
