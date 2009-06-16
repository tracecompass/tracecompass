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

package org.eclipse.linuxtools.tmf.eventlog;

import static org.junit.Assert.*;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b><u>TmfEventStreamTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventStreamTest {

    private static final String TEST_STREAM = "Test-10K";
    private static final int NB_EVENTS = 10000;
    private static TmfEventStreamStub fStream;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        TmfEventParserStub parser = new TmfEventParserStub();
        fStream = new TmfEventStreamStub(TEST_STREAM, parser, 500);
    }

    // ========================================================================
    // Constructor
    // ========================================================================

    @Test
    public void testDefaultConstructor() throws Exception {
        TmfEventParserStub parser = new TmfEventParserStub();
        TmfEventStreamStub stream = new TmfEventStreamStub(TEST_STREAM, parser);

        assertEquals("getCacheSize", TmfEventStream.DEFAULT_CACHE_SIZE, stream.getCacheSize());
        assertEquals("getTraceSize", NB_EVENTS, stream.getNbEvents());
        assertEquals("getRange-start", 0, stream.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS - 1, stream.getTimeRange().getEndTime().getValue());
    }

    @Test
    public void testNormalConstructor() throws Exception {
        TmfEventParserStub parser = new TmfEventParserStub();
        TmfEventStreamStub stream = new TmfEventStreamStub(TEST_STREAM, parser, 500);

        assertEquals("getCacheSize",    500, stream.getCacheSize());
        assertEquals("getTraceSize", NB_EVENTS, stream.getNbEvents());
        assertEquals("getRange-start",    0, stream.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS - 1, stream.getTimeRange().getEndTime().getValue());
    }

    // ========================================================================
    // seek
    // ========================================================================

    @Test
    public void testSeekOnCacheBoundary() throws Exception {
        TmfEvent event = fStream.seek(new TmfTimestamp(0, (byte) 0, 0));
        assertEquals("Event timestamp", 0, event.getTimestamp().getValue());

        event = fStream.seek(new TmfTimestamp(1000, (byte) 0, 0));
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());

        event = fStream.seek(new TmfTimestamp(4000, (byte) 0, 0));
        assertEquals("Event timestamp", 4000, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekNotOnCacheBoundary() throws Exception {
        TmfEvent event = fStream.seek(new TmfTimestamp(1, (byte) 0, 0));
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        event = fStream.seek(new TmfTimestamp(999, (byte) 0, 0));
        assertEquals("Event timestamp", 999, event.getTimestamp().getValue());

        event = fStream.seek(new TmfTimestamp(4499, (byte) 0, 0));
        assertEquals("Event timestamp", 4499, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekForEventOutOfBounds() throws Exception {
        // On lower bound, returns the first event (ts = 0)
        TmfEvent event = fStream.seek(new TmfTimestamp(-1, (byte) 0, 0));
        assertEquals("Event timestamp", 0, event.getTimestamp().getValue());

        // On higher bound, returns null (no event)
        event = fStream.seek(new TmfTimestamp(NB_EVENTS, (byte) 0, 0));
        assertEquals("Event timestamp", null, event);
    }

    // ========================================================================
    // getNextEvent
    // ========================================================================

    @Test
    public void testGetNextEvent() throws Exception {
        // On lower bound, returns the first event (ts = 0)
        TmfEvent event = fStream.seek(new TmfTimestamp(0, (byte) 0, 0));
        assertEquals("Event timestamp", 0, event.getTimestamp().getValue());

        for (int i = 0; i < 10; i++) {
            event = fStream.getNextEvent();
            assertEquals("Event timestamp", i + 1, event.getTimestamp().getValue());
        }
    }

}
