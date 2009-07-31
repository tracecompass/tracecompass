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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeWindow;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b><u>TmfEventLogTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventLogTest {

    private static String filename = "Test-10K";
    private static int fTotalNbEvents = 10000; 
    private static ITmfEventParser fParser;
    private static TmfEventStream  fStream;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		fParser = new TmfEventParserStub();
		fStream = new TmfEventStreamStub(filename, fParser);
	}

    // ========================================================================
    // Constructor
    // ========================================================================

	@Test
	public void testBasicTmfEventLog() {
		TmfTrace eventLog = new TmfTrace("MyEventLog", fStream);

		assertEquals("GetId", "MyEventLog", eventLog.getId());
        assertEquals("GetEpoch", TmfTimestamp.BigBang, eventLog.getEpoch());
        assertEquals("GetNbEvents", fTotalNbEvents, eventLog.getNbEvents());

        TmfTimeWindow timeRange = eventLog.getTimeRange();
        assertEquals("GetTimeRange", 0, timeRange.getStartTime().getValue());
        assertEquals("GetTimeRange", fTotalNbEvents - 1, timeRange.getEndTime().getValue());
	}

	@Test
	public void testTmfEventLogWithEpoch() {
		TmfTimestamp epoch = new TmfTimestamp(100, (byte) 0, 0);
		TmfTrace eventLog = new TmfTrace("MyEventLog", fStream, epoch);

		assertEquals("GetId", "MyEventLog", eventLog.getId());
        assertEquals("GetEpoch", epoch, eventLog.getEpoch());
        assertEquals("GetNbEvents", fTotalNbEvents, eventLog.getNbEvents());

        TmfTimeWindow timeRange = eventLog.getTimeRange();
        assertEquals("GetTimeRange", 0, timeRange.getStartTime().getValue());
        assertEquals("GetTimeRange", fTotalNbEvents - 1, timeRange.getEndTime().getValue());
	}

    // ========================================================================
    // Operators
    // ========================================================================

    @Test
    public void testProcessRequestForNbEvents() throws Exception {

        TmfTrace eventLog = new TmfTrace("MyEventLog", fStream);

        final int NB_EVENTS  = 10 * 1000;
        final int BLOCK_SIZE = 100;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        eventLog.process(request, true);

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    @Test
    public void testProcessRequestForAllEvents() throws Exception {

        TmfTrace eventLog = new TmfTrace("MyEventLog", fStream);

        final int NB_EVENTS  = -1;
        final int BLOCK_SIZE =  1;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        int nbExpectedEvents = eventLog.getNbEvents();

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        eventLog.process(request, true);

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // @Test
    public void testProcessRequestWithOffset() throws Exception {

        TmfTrace eventLog = new TmfTrace("MyEventLog", fStream);

        final int NB_EVENTS  = -1;
        final int BLOCK_SIZE =  1;
        final int OFFSET = 5;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, OFFSET, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        eventLog.process(request, true);

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i + OFFSET, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // l@Test
    public void testProcessRequestWithNegativeOffset() throws Exception {

        TmfTrace eventLog = new TmfTrace("MyEventLog", fStream);

        final int NB_EVENTS  = -1;
        final int BLOCK_SIZE =  1;
        final int OFFSET = -5;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, OFFSET, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        eventLog.process(request, true);

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i + OFFSET, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // ========================================================================
    // cancel
    // ========================================================================

    @Test
    public void testCancel() throws Exception {

        TmfTrace eventLog = new TmfTrace("MyEventLog", fStream);

        final int NB_EVENTS  = 10 * 1000;
        final int BLOCK_SIZE = 100;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest request = new TmfEventRequest(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void newEvents(Vector<TmfEvent> events) {
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
                // Cancel request after the first chunk is received
                cancel();
            }
        };
        eventLog.process(request, true);

        assertEquals("nbEvents",  BLOCK_SIZE, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}
