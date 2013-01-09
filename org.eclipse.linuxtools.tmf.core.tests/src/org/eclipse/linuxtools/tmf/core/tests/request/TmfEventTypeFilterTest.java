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
import org.eclipse.linuxtools.tmf.core.request.TmfEventTypeFilter;
import org.junit.Test;

/**
 * <b><u>TmfEventTypeFilterTest</u></b>
 * <p>
 * Test suite for the TmfEventTypeFilter class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfEventTypeFilterTest extends TestCase {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final TmfEventTypeFilter ALL_EVENTS = TmfEventTypeFilter.ALL_EVENTS;

    // ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
	public TmfEventTypeFilterTest(String name) {
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
	    TmfEventTypeFilter filter = ALL_EVENTS;
        assertEquals("getEventType", ITmfEvent.class, filter.getEventType());

        filter = new TmfEventTypeFilter(ITmfEvent.class);
        assertEquals("getEventType", ITmfEvent.class, filter.getEventType());

        filter = new TmfEventTypeFilter(TmfEvent.class);
        assertEquals("getEventType", TmfEvent.class, filter.getEventType());

	}

    @Test
    public void testTmfBlockFilterCopy() {
        TmfEventTypeFilter filter1 = new TmfEventTypeFilter(TmfEvent.class);
        TmfEventTypeFilter filter2 = new TmfEventTypeFilter(filter1);

        assertEquals("getEventType", filter1.getEventType(), filter2.getEventType());
        assertEquals("getEventType", TmfEvent.class, filter2.getEventType());
    }

    // ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        TmfEventTypeFilter filter1 = new TmfEventTypeFilter(ITmfEvent.class);
        TmfEventTypeFilter filter2 = new TmfEventTypeFilter(TmfEvent.class);

        assertEquals("equals", filter1, filter1);
        assertEquals("equals", filter2, filter2);

        assertFalse("equals", filter1.equals(filter2));
        assertFalse("equals", filter2.equals(filter1));
    }

    @Test
    public void testEqualsSymmetry() {
        TmfEventTypeFilter filter1 = new TmfEventTypeFilter(ITmfEvent.class);
        TmfEventTypeFilter filter2 = new TmfEventTypeFilter(ITmfEvent.class);
        TmfEventTypeFilter filter3 = new TmfEventTypeFilter(TmfEvent.class);

        assertEquals("equals", filter1, filter2);
        assertEquals("equals", filter2, filter1);

        assertFalse("equals", filter1.equals(filter3));
        assertFalse("equals", filter3.equals(filter1));
    }

    @Test
    public void testEqualsTransivity() {
        TmfEventTypeFilter filter1 = new TmfEventTypeFilter(TmfEvent.class);
        TmfEventTypeFilter filter2 = new TmfEventTypeFilter(TmfEvent.class);
        TmfEventTypeFilter filter3 = new TmfEventTypeFilter(TmfEvent.class);

        assertEquals("equals", filter1, filter2);
        assertEquals("equals", filter2, filter3);
        assertEquals("equals", filter3, filter1);
    }

    @Test
    public void testEqualsNull() {
        TmfEventTypeFilter filter1 = new TmfEventTypeFilter(ITmfEvent.class);
        TmfEventTypeFilter filter2 = new TmfEventTypeFilter(TmfEvent.class);

        assertFalse("equals", filter1.equals(null));
        assertFalse("equals", filter2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        TmfEventTypeFilter filter1 = new TmfEventTypeFilter(ITmfEvent.class);
        TmfEventTypeFilter filter2 = new TmfEventTypeFilter(TmfEvent.class);

        assertFalse("hashCode",  filter1.hashCode() == filter2.hashCode());
    }

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

    @Test
    public void testToString() {
        TmfEventTypeFilter filter1 = new TmfEventTypeFilter(ITmfEvent.class);
        TmfEventTypeFilter filter2 = new TmfEventTypeFilter(TmfEvent.class);

        String expected0 = "TmfEventTypeFilter [fEventType=ITmfEvent]";
        String expected1 = "TmfEventTypeFilter [fEventType=ITmfEvent]";
        String expected2 = "TmfEventTypeFilter [fEventType=TmfEvent]";

        assertEquals("toString", expected0, ALL_EVENTS.toString());
        assertEquals("toString", expected1, filter1.toString());
        assertEquals("toString", expected2, filter2.toString());
    }

    // ------------------------------------------------------------------------
    // matches
    // ------------------------------------------------------------------------

    private class TmfEvent1 extends TmfEvent {
        public TmfEvent1(final ITmfEvent event) {
            super(event);
        }
    }

    private class TmfEvent2 extends TmfEvent {
        public TmfEvent2(final ITmfEvent event) {
            super(event);
        }
    }

    /**
     * The only test that really matters...
     */
    @Test
    public void testMatches() {

        TmfEventTypeFilter filter1 = new TmfEventTypeFilter(TmfEvent1.class);
        TmfEventTypeFilter filter2 = new TmfEventTypeFilter(TmfEvent2.class);

        TmfEvent  event0 = new TmfEvent(null, 0, null, null, null, null, null);
        TmfEvent1 event1 = new TmfEvent1(event0);
        TmfEvent2 event2 = new TmfEvent2(event0);

        assertFalse("matches", filter1.matches(event0));
        assertFalse("matches", filter2.matches(event0));

        assertTrue ("matches", filter1.matches(event1));
        assertFalse("matches", filter2.matches(event1));

        assertFalse("matches", filter2.matches(event1));
        assertTrue ("matches", filter2.matches(event2));

        assertTrue("matches", ALL_EVENTS.matches(event1));
        assertTrue("matches", ALL_EVENTS.matches(event2));
        assertTrue("matches", ALL_EVENTS.matches(event0));
    }

}
