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

package org.eclipse.linuxtools.tmf.trace;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace.TmfTraceContext;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b><u>TmfEventStreamTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceTest {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "M-Test-100K";
    private static String testfile;
    private static final int NB_EVENTS = 100000;
    private static TmfTraceStub fTrace;

    private static byte SCALE = (byte) -3;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	String directory = new File(".").getCanonicalPath() + File.separator + DIRECTORY;
    	testfile = directory + File.separator + TEST_STREAM;
        fTrace = new TmfTraceStub(testfile, 500);
    }

    // ========================================================================
    // Constructor
    // ========================================================================

    @Test
    public void testDefaultConstructor() throws Exception {
        TmfTraceStub stream = new TmfTraceStub(testfile);

        assertEquals("getCacheSize", TmfTraceStub.DEFAULT_PAGE_SIZE, stream.getPageSize());
        assertEquals("getTraceSize", NB_EVENTS, stream.getNbEvents());
        assertEquals("getRange-start", 1, stream.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS, stream.getTimeRange().getEndTime().getValue());
    }

    @Test
    public void testNormalConstructor() throws Exception {
        TmfTraceStub stream = new TmfTraceStub(testfile, 500);

        assertEquals("getCacheSize",    500, stream.getPageSize());
        assertEquals("getTraceSize", NB_EVENTS, stream.getNbEvents());
        assertEquals("getRange-start",    1, stream.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS, stream.getTimeRange().getEndTime().getValue());
    }

    // ========================================================================
    // seek
    // ========================================================================

    @Test
    public void testSeekOnCacheBoundary() throws Exception {
    	TmfTraceContext context = new TmfTraceContext(null);

    	TmfEvent event = fTrace.getEvent(context, new TmfTimestamp(0, SCALE, 0));
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        event = fTrace.getEvent(context, new TmfTimestamp(1000, SCALE, 0));
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());

        event = fTrace.getEvent(context, new TmfTimestamp(4000, SCALE, 0));
        assertEquals("Event timestamp", 4000, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekNotOnCacheBoundary() throws Exception {
    	TmfTraceContext context = new TmfTraceContext(null);

    	TmfEvent event = fTrace.getEvent(context, new TmfTimestamp(1, SCALE, 0));
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        event = fTrace.getEvent(context, new TmfTimestamp(999, SCALE, 0));
        assertEquals("Event timestamp", 999, event.getTimestamp().getValue());

        event = fTrace.getEvent(context, new TmfTimestamp(4499, SCALE, 0));
        assertEquals("Event timestamp", 4499, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekForEventOutOfBounds() throws Exception {
    	TmfTraceContext context = new TmfTraceContext(null);

    	// On lower bound, returns the first event (ts = 1)
        TmfEvent event = fTrace.getEvent(context, new TmfTimestamp(-1, SCALE, 0));
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        // On higher bound, returns null (no event)
        event = fTrace.getEvent(context, new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        assertEquals("Event timestamp", null, event);
    }

    // ========================================================================
    // getNextEvent
    // ========================================================================

    @Test
    public void testGetNextEvent() throws Exception {
    	TmfTraceContext context = new TmfTraceContext(null);

    	// On lower bound, returns the first event (ts = 0)
        TmfEvent event = fTrace.getEvent(context, new TmfTimestamp(0, SCALE, 0));
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        for (int i = 2; i < 20; i++) {
            event = fTrace.getNextEvent(context);
            assertEquals("Event timestamp", i, event.getTimestamp().getValue());
        }
    }

}
