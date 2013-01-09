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
import org.eclipse.linuxtools.tmf.core.request.ITmfRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfBlockFilter;
import org.junit.Test;

/**
 * <b><u>TmfBlockFilterTest</u></b>
 * <p>
 * Test suite for the TmfBlockFilter class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfBlockFilterTest extends TestCase {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final long ALL_EVENTS = ITmfRequest.ALL_EVENTS;

    // ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
	public TmfBlockFilterTest(String name) {
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
	public void testTmfBlockFilter() {
	    TmfBlockFilter filter = TmfBlockFilter.ALL_EVENTS;
        assertEquals("getStartIndex",           0, filter.getStartIndex());
        assertEquals("getEndIndex",    ALL_EVENTS, filter.getEndIndex());
        assertEquals("getNbRequested", ALL_EVENTS, filter.getNbRequested());

        filter = new TmfBlockFilter(0, 1000);
        assertEquals("getStartIndex",     0, filter.getStartIndex());
        assertEquals("getEndIndex",    1000, filter.getEndIndex());
        assertEquals("getNbRequested", 1000, filter.getNbRequested());

        filter = new TmfBlockFilter(-1, 1000);
        assertEquals("getStartIndex",     0, filter.getStartIndex());
        assertEquals("getEndIndex",    1000, filter.getEndIndex());
        assertEquals("getNbRequested", 1000, filter.getNbRequested());

        filter = new TmfBlockFilter(0, -1);
        assertEquals("getStartIndex",           0, filter.getStartIndex());
        assertEquals("getEndIndex",    ALL_EVENTS, filter.getEndIndex());
        assertEquals("getNbRequested", ALL_EVENTS, filter.getNbRequested());

        filter = new TmfBlockFilter(1000, ALL_EVENTS);
        assertEquals("getStartIndex",        1000, filter.getStartIndex());
        assertEquals("getEndIndex",    ALL_EVENTS, filter.getEndIndex());
        assertEquals("getNbRequested", ALL_EVENTS, filter.getNbRequested());
	}

    @Test
    public void testTmfBlockFilterCopy() {
        TmfBlockFilter filter1 = new TmfBlockFilter(0, 1000);
        TmfBlockFilter filter2 = new TmfBlockFilter(filter1);

        assertEquals("getStartIndex",  filter1.getStartIndex(),  filter2.getStartIndex());
        assertEquals("getEndIndex",    filter1.getEndIndex(),    filter2.getEndIndex());
        assertEquals("getNbRequested", filter1.getNbRequested(), filter2.getNbRequested());
    }

    // ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        TmfBlockFilter filter1 = new TmfBlockFilter( 0, 1000);
        TmfBlockFilter filter2 = new TmfBlockFilter(10, 1000);

        assertEquals("equals", filter1, filter1);
        assertEquals("equals", filter2, filter2);

        assertFalse("equals", filter1.equals(filter2));
        assertFalse("equals", filter2.equals(filter1));
    }

    @Test
    public void testEqualsSymmetry() {
        TmfBlockFilter filter1 = new TmfBlockFilter( 0, 1000);
        TmfBlockFilter filter2 = new TmfBlockFilter( 0, 1000);
        TmfBlockFilter filter3 = new TmfBlockFilter(10, 1000);

        assertEquals("equals", filter1, filter2);
        assertEquals("equals", filter2, filter1);

        assertFalse("equals", filter1.equals(filter3));
        assertFalse("equals", filter3.equals(filter1));
    }

    @Test
    public void testEqualsTransivity() {
        TmfBlockFilter filter1 = new TmfBlockFilter(0, 1000);
        TmfBlockFilter filter2 = new TmfBlockFilter(0, 1000);
        TmfBlockFilter filter3 = new TmfBlockFilter(0, 1000);

        assertEquals("equals", filter1, filter2);
        assertEquals("equals", filter2, filter3);
        assertEquals("equals", filter3, filter1);
    }

    @Test
    public void testEqualsNull() {
        TmfBlockFilter filter1 = new TmfBlockFilter( 0, 1000);
        TmfBlockFilter filter2 = new TmfBlockFilter(10, 1000);

        assertFalse("equals", filter1.equals(null));
        assertFalse("equals", filter2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        TmfBlockFilter filter1 = new TmfBlockFilter( 0, 1000);
        TmfBlockFilter filter2 = new TmfBlockFilter(10, 1000);

        assertFalse("hashCode",  filter1.hashCode() == filter2.hashCode());
    }

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

    @Test
    public void testToString() {
        TmfBlockFilter filter1 = new TmfBlockFilter( 0, 1000);
        TmfBlockFilter filter2 = new TmfBlockFilter(10, 2000);

        String expected0 = "TmfBlockFilter [fStartIndex=0, fEndIndex=" + Long.MAX_VALUE + ", fNbRequested=" + Long.MAX_VALUE + "]";
        String expected1 = "TmfBlockFilter [fStartIndex=0, fEndIndex=1000, fNbRequested=1000]";
        String expected2 = "TmfBlockFilter [fStartIndex=10, fEndIndex=2010, fNbRequested=2000]";

        assertEquals("toString", expected0, TmfBlockFilter.ALL_EVENTS.toString());
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

        TmfBlockFilter filter1 = new TmfBlockFilter( 0, 1000);
        TmfBlockFilter filter2 = new TmfBlockFilter(10, 2000);

        ITmfEvent event = new TmfEvent(null, 0, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertFalse("matches", filter2.matches(event));

        event = new TmfEvent(null, 5, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertFalse("matches", filter2.matches(event));

        event = new TmfEvent(null, 9, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertFalse("matches", filter2.matches(event));

        event = new TmfEvent(null, 10, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 999, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 1000, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 1999, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 2000, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 2009, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));

        event = new TmfEvent(null, 2010, null, null, null, null, null);
        assertTrue ("matches", TmfBlockFilter.ALL_EVENTS.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertFalse("matches", filter2.matches(event));
    }

}
