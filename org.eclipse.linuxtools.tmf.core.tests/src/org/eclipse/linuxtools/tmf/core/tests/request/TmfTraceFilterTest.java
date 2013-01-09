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
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.TmfTraceFilter;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.Test;

/**
 * <b><u>TmfTraceFilterTest</u></b>
 * <p>
 * Test suite for the TmfTraceFilter class.
 */
@SuppressWarnings({"nls","javadoc"})
public class TmfTraceFilterTest extends TestCase {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    private static final TmfTraceFilter ALL_TRACES = TmfTraceFilter.ALL_TRACES;

    // ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

    /**
     * @param name the test name
     */
	public TmfTraceFilterTest(String name) {
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
	public void testTmfTraceFilter() {
        ITmfTrace trace1 = new TmfTraceStub();
        ITmfTrace trace2 = new TmfTraceStub();
        ITmfTrace[] traces;

	    TmfTraceFilter filter = ALL_TRACES;
	    traces = filter.getTraces();
        assertEquals("getTraces", 0, traces.length);
        assertEquals("getTraces", ALL_TRACES.getTraces(), filter.getTraces());

        filter = new TmfTraceFilter();
        traces = filter.getTraces();
        assertEquals("getTraces", 0, traces.length);
        assertEquals("getTraces", ALL_TRACES.getTraces(), filter.getTraces());

        filter = new TmfTraceFilter(new ITmfTrace[] { trace1 });
        traces = filter.getTraces();
        assertEquals("getTraces", 1, traces.length);
        assertEquals("getTraces", trace1, traces[0]);

        filter = new TmfTraceFilter(new ITmfTrace[] { trace2 });
        traces = filter.getTraces();
        assertEquals("getTraces", 1, traces.length);
        assertEquals("getTraces", trace2, traces[0]);

        filter = new TmfTraceFilter(new ITmfTrace[] { trace1, trace2 });
        traces = filter.getTraces();
        assertEquals("getTraces", 2, traces.length);
        assertEquals("getTraces", trace1, traces[0]);
        assertEquals("getTraces", trace2, traces[1]);
	}

    @Test
    public void testTmfTraceFilterCopy() {
        ITmfTrace trace1 = new TmfTraceStub();
        ITmfTrace trace2 = new TmfTraceStub();

        TmfTraceFilter filter1 = new TmfTraceFilter(new ITmfTrace[] { trace1, trace2 });
        TmfTraceFilter filter2 = new TmfTraceFilter(filter1);
        ITmfTrace[] traces = filter2.getTraces();
        assertEquals("getTraces", 2, traces.length);
        assertEquals("getTraces", trace1, traces[0]);
        assertEquals("getTraces", trace2, traces[1]);
    }

    // ------------------------------------------------------------------------
    // equals
    // ------------------------------------------------------------------------

    @Test
    public void testEqualsReflexivity() {
        TmfTraceFilter filter1 = new TmfTraceFilter();
        TmfTraceFilter filter2 = new TmfTraceFilter(new ITmfTrace[] { new TmfTraceStub() });

        assertEquals("equals", filter1, filter1);
        assertEquals("equals", filter2, filter2);

        assertFalse("equals", filter1.equals(filter2));
        assertFalse("equals", filter2.equals(filter1));
    }

    @Test
    public void testEqualsSymmetry() {
        TmfTraceFilter filter1 = new TmfTraceFilter();
        TmfTraceFilter filter2 = new TmfTraceFilter();
        TmfTraceFilter filter3 = new TmfTraceFilter(new ITmfTrace[] { new TmfTraceStub() });

        assertEquals("equals", filter1, filter2);
        assertEquals("equals", filter2, filter1);

        assertFalse("equals", filter1.equals(filter3));
        assertFalse("equals", filter3.equals(filter1));
    }

    @Test
    public void testEqualsTransivity() {
        TmfTraceFilter filter1 = new TmfTraceFilter();
        TmfTraceFilter filter2 = new TmfTraceFilter();
        TmfTraceFilter filter3 = new TmfTraceFilter();

        assertEquals("equals", filter1, filter2);
        assertEquals("equals", filter2, filter3);
        assertEquals("equals", filter3, filter1);

        ITmfTrace trace = new TmfTraceStub();
        filter1 = new TmfTraceFilter(new ITmfTrace[] { trace });
        filter2 = new TmfTraceFilter(new ITmfTrace[] { trace });
        filter3 = new TmfTraceFilter(new ITmfTrace[] { trace });

        assertEquals("equals", filter1, filter2);
        assertEquals("equals", filter2, filter3);
        assertEquals("equals", filter3, filter1);
    }

    @Test
    public void testEqualsNull() {
        TmfTraceFilter filter1 = new TmfTraceFilter();
        TmfTraceFilter filter2 = new TmfTraceFilter(new ITmfTrace[] { new TmfTraceStub() });

        assertFalse("equals", filter1.equals(null));
        assertFalse("equals", filter2.equals(null));
    }

    // ------------------------------------------------------------------------
    // hashCode
    // ------------------------------------------------------------------------

    @Test
    public void testHashCode() {
        TmfTraceFilter filter1 = new TmfTraceFilter();
        TmfTraceFilter filter2 = new TmfTraceFilter(new ITmfTrace[] { new TmfTraceStub() });

        assertFalse("hashCode",  filter1.hashCode() == filter2.hashCode());
    }

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

    @Test
    public void testToString() {
        ITmfTrace trace = new TmfTraceStub();
        TmfTraceFilter filter1 = new TmfTraceFilter();
        TmfTraceFilter filter2 = new TmfTraceFilter(new ITmfTrace[] { trace });

        String expected1 = "TmfTraceFilter [fTraces=[]]";
        String expected2 = "TmfTraceFilter [fTraces=[" + trace + "]]";

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
        ITmfTrace trace0 = new TmfTraceStub();
        ITmfTrace trace1 = new TmfTraceStub();
        ITmfTrace trace2 = new TmfTraceStub();

        TmfTraceFilter filter0 = new TmfTraceFilter();
        TmfTraceFilter filter1 = new TmfTraceFilter(new ITmfTrace[] { trace1 });
        TmfTraceFilter filter2 = new TmfTraceFilter(new ITmfTrace[] { trace2 });
        TmfTraceFilter filter3 = new TmfTraceFilter(new ITmfTrace[] { trace1, trace2 });

        ITmfEvent event = new TmfEvent(trace0, 0, new TmfTimestamp(0), null, null, null, null);
        assertTrue ("matches", TmfTraceFilter.ALL_TRACES.matches(event));
        assertTrue ("matches", filter0.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertFalse("matches", filter2.matches(event));
        assertFalse("matches", filter3.matches(event));

        event = new TmfEvent(trace1, 0, new TmfTimestamp(0), null, null, null, null);
        assertTrue ("matches", TmfTraceFilter.ALL_TRACES.matches(event));
        assertTrue ("matches", filter0.matches(event));
        assertTrue ("matches", filter1.matches(event));
        assertFalse("matches", filter2.matches(event));
        assertTrue ("matches", filter3.matches(event));

        event = new TmfEvent(trace2, 0, new TmfTimestamp(0), null, null, null, null);
        assertTrue ("matches", TmfTraceFilter.ALL_TRACES.matches(event));
        assertTrue ("matches", filter0.matches(event));
        assertFalse("matches", filter1.matches(event));
        assertTrue ("matches", filter2.matches(event));
        assertTrue ("matches", filter3.matches(event));
    }

}
