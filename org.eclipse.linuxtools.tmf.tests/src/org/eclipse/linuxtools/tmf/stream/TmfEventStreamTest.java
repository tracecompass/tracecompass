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

package org.eclipse.linuxtools.tmf.stream;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.TmfEventParserStub;
import org.eclipse.linuxtools.tmf.trace.TmfEventStreamStub;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace.StreamContext;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b><u>TmfEventStreamTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventStreamTest {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "M-Test-100K";
    private static String testfile;
    private static final int NB_EVENTS = 100000;
    private static TmfEventStreamStub fStream;

    private static byte SCALE = (byte) -3;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	String directory = new File(".").getCanonicalPath() + File.separator + DIRECTORY;
    	testfile = directory + File.separator + TEST_STREAM;

        TmfEventParserStub parser = new TmfEventParserStub();
        fStream = new TmfEventStreamStub(testfile, parser, 500);
        fStream.indexStream(true);

    }

    // ========================================================================
    // Constructor
    // ========================================================================

    @Test
    public void testDefaultConstructor() throws Exception {
        TmfEventParserStub parser = new TmfEventParserStub();
        TmfEventStreamStub stream = new TmfEventStreamStub(testfile, parser);
        stream.indexStream(true);

        assertEquals("getCacheSize", TmfEventStreamStub.DEFAULT_CACHE_SIZE, stream.getCacheSize());
        assertEquals("getTraceSize", NB_EVENTS, stream.getNbEvents());
        assertEquals("getRange-start", 1, stream.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS, stream.getTimeRange().getEndTime().getValue());
    }

    @Test
    public void testNormalConstructor() throws Exception {
        TmfEventParserStub parser = new TmfEventParserStub();
        TmfEventStreamStub stream = new TmfEventStreamStub(testfile, parser, 500);
        stream.indexStream(true);

        assertEquals("getCacheSize",    500, stream.getCacheSize());
        assertEquals("getTraceSize", NB_EVENTS, stream.getNbEvents());
        assertEquals("getRange-start",    1, stream.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS, stream.getTimeRange().getEndTime().getValue());
    }

    // ========================================================================
    // seek
    // ========================================================================

    @Test
    public void testSeekOnCacheBoundary() throws Exception {
    	StreamContext context = new StreamContext(null);

    	TmfEvent event = fStream.getEvent(context, new TmfTimestamp(0, SCALE, 0));
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        event = fStream.getEvent(context, new TmfTimestamp(1000, SCALE, 0));
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());

        event = fStream.getEvent(context, new TmfTimestamp(4000, SCALE, 0));
        assertEquals("Event timestamp", 4000, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekNotOnCacheBoundary() throws Exception {
    	StreamContext context = new StreamContext(null);

    	TmfEvent event = fStream.getEvent(context, new TmfTimestamp(1, SCALE, 0));
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        event = fStream.getEvent(context, new TmfTimestamp(999, SCALE, 0));
        assertEquals("Event timestamp", 999, event.getTimestamp().getValue());

        event = fStream.getEvent(context, new TmfTimestamp(4499, SCALE, 0));
        assertEquals("Event timestamp", 4499, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekForEventOutOfBounds() throws Exception {
    	StreamContext context = new StreamContext(null);

    	// On lower bound, returns the first event (ts = 1)
        TmfEvent event = fStream.getEvent(context, new TmfTimestamp(-1, SCALE, 0));
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        // On higher bound, returns null (no event)
        event = fStream.getEvent(context, new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        assertEquals("Event timestamp", null, event);
    }

    // ========================================================================
    // getNextEvent
    // ========================================================================

    @Test
    public void testGetNextEvent() throws Exception {
    	StreamContext context = new StreamContext(null);

    	// On lower bound, returns the first event (ts = 0)
        TmfEvent event = fStream.getEvent(context, new TmfTimestamp(0, SCALE, 0));
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        for (int i = 2; i < 20; i++) {
            event = fStream.getNextEvent(context);
            assertEquals("Event timestamp", i, event.getTimestamp().getValue());
        }
    }

}
