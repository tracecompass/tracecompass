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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b><u>TmfEventStreamTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceTest {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "M-Test-10K";
    private static String testfile;
    private static final int NB_EVENTS = 10000;
    private static TmfTraceStub fTrace;

    private static byte SCALE = (byte) -3;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	String directory = new File(".").getCanonicalPath() + File.separator + DIRECTORY;
    	testfile = directory + File.separator + TEST_STREAM;
        fTrace = new TmfTraceStub(testfile, 500, true);
    }

    // ========================================================================
    // Constructor
    // ========================================================================

    @Test
    public void testDefaultConstructor() throws Exception {
        TmfTraceStub trace = new TmfTraceStub(testfile, true);

        assertEquals("getCacheSize", TmfTraceStub.DEFAULT_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getTraceSize", NB_EVENTS, trace.getNbEvents());
        assertEquals("getRange-start", 1, trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS, trace.getTimeRange().getEndTime().getValue());
    }

    @Test
    public void testNormalConstructor() throws Exception {
        assertEquals("getCacheSize",    500,    fTrace.getCacheSize());
        assertEquals("getTraceSize", NB_EVENTS, fTrace.getNbEvents());
        assertEquals("getRange-start",    1,    fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end", NB_EVENTS, fTrace.getTimeRange().getEndTime().getValue());
    }

    // ========================================================================
    // seek
    // ========================================================================

    @Test
    public void testSeekOnCacheBoundary() throws Exception {
    	TmfTraceContext context = fTrace.seekLocation(null);

    	context = fTrace.seekEvent(new TmfTimestamp(0, SCALE, 0));
    	TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

    	context = fTrace.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());

    	context = fTrace.seekEvent(new TmfTimestamp(4000, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4000, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekNotOnCacheBoundary() throws Exception {
    	TmfTraceContext context = fTrace.seekLocation(null);

    	context = fTrace.seekEvent(new TmfTimestamp(1, SCALE, 0));
    	TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

    	context = fTrace.seekEvent(new TmfTimestamp(999, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 999, event.getTimestamp().getValue());

    	context = fTrace.seekEvent(new TmfTimestamp(4499, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4499, event.getTimestamp().getValue());
    }

    @Test
    public void testSeekForEventOutOfBounds() throws Exception {
    	TmfTraceContext context = fTrace.seekLocation(null);

    	// On lower bound, returns the first event (ts = 1)
    	context = fTrace.seekEvent(new TmfTimestamp(-1, SCALE, 0));
        TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        // On higher bound, returns null (no event)
    	context = fTrace.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", null, event);
    }

    // ========================================================================
    // getNextEvent
    // ========================================================================

    @Test
    public void testGetNextEvent() throws Exception {
    	TmfTraceContext context = fTrace.seekLocation(null);

    	// On lower bound, returns the first event (ts = 0)
    	context = fTrace.seekEvent(new TmfTimestamp(0, SCALE, 0));
        TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        for (int i = 2; i < 20; i++) {
            event = fTrace.getNextEvent(context);
            assertEquals("Event timestamp", i, event.getTimestamp().getValue());
        }
    }

    // ========================================================================
    // processRequest
    // ========================================================================

    @Test
    public void testProcessRequestForNbEvents() throws Exception {
        final int BLOCK_SIZE = 100;
        final int NB_EVENTS  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fTrace.processRequest(request, true);

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    @Test
    public void testProcessRequestForAllEvents() throws Exception {
        final int NB_EVENTS  = -1;
        final int BLOCK_SIZE =  1;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        long nbExpectedEvents = fTrace.getNbEvents();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fTrace.processRequest(request, true);

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // ========================================================================
    // cancel
    // ========================================================================

    @Test
    public void testCancel() throws Exception {
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 0, NB_EVENTS, NB_EVENTS) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
                // Cancel request after the first chunk is received
                cancel();
            }
        };
        fTrace.processRequest(request, true);

        assertEquals("nbEvents",  NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}
