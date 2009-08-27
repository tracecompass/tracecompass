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
import org.eclipse.linuxtools.tmf.stream.ITmfEventParser;
import org.eclipse.linuxtools.tmf.stream.ITmfEventStream;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b><u>TmfEventLogTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceTest {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "M-Test-10K";
    private static String testfile;
    private static int fTotalNbEvents = 10000;
    private static int fDefaultBlockSize = 1000;

    private static ITmfEventParser fParser;
    private static ITmfEventStream fStream;
    private static TmfTrace        fTrace;

//    private static byte SCALE = (byte) -3;

    @BeforeClass
	public static void setUpBeforeClass() throws Exception {
    	String directory = new File(".").getCanonicalPath() + File.separator + DIRECTORY;
    	testfile = directory + File.separator + TEST_STREAM;

		fParser = new TmfEventParserStub();
		fStream = new TmfEventStreamStub(testfile, fParser);
        fTrace  = new TmfTrace("MyTrace", fStream);
        fStream.indexStream(true);
	}

    // ========================================================================
    // Constructor
    // ========================================================================

	@Test
	public void testBasicTmfEventLog() {
		assertEquals("GetId", "MyTrace", fTrace.getId());
        assertEquals("GetEpoch", TmfTimestamp.BigBang, fTrace.getEpoch());
        assertEquals("GetNbEvents", fTotalNbEvents, fTrace.getNbEvents());

        TmfTimeRange timeRange = fTrace.getTimeRange();
        assertEquals("GetTimeRange-start", 1, timeRange.getStartTime().getValue());
        assertEquals("GetTimeRange-end", fTotalNbEvents, timeRange.getEndTime().getValue());
	}

//  TODO: Fix the test when epoch is implemented
//	@Test
//	public void testTmfEventLogWithEpoch() {
//		TmfTimestamp epoch = new TmfTimestamp(100, SCALE, 0);
//		TmfEventLog eventLog = new TmfEventLog("MyEventLog", fStream, epoch);
//
//		assertEquals("GetId", "MyEventLog", eventLog.getId());
//        assertEquals("GetEpoch", epoch, eventLog.getEpoch());
//        assertEquals("GetNbEvents", fTotalNbEvents, eventLog.getNbEvents());
//
//        TmfTimeWindow timeRange = eventLog.getTimeRange();
//        assertEquals("GetTimeRange-start", 1, timeRange.getStartTime().getValue());
//        assertEquals("GetTimeRange-end", fTotalNbEvents, timeRange.getEndTime().getValue());
//	}

//    // ========================================================================
//    // Accessors
//    // ========================================================================
//
//    @Test
//    public void testGetNbEvents() throws Exception {
//    	TmfTrace eventLog = new TmfTrace("MyEventLog", fStream);
//        assertEquals("nbEvents", fTotalNbEvents, eventLog.getNbEvents());
//    }

    // ========================================================================
    // Operators
    // ========================================================================

    @Test
    public void testProcessRequestForNbEvents() throws Exception {
        final int NB_EVENTS  = fTotalNbEvents;
        final int BLOCK_SIZE = 100;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        fStream.indexStream(true);
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
        int nbExpectedEvents = fTrace.getNbEvents();

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
    
//    // @Test
//    // TODO: Implement offset handling first...
//    public void testProcessRequestWithOffset() throws Exception {
//        final int NB_EVENTS  = -1;
//        final int BLOCK_SIZE =  1;
//        final int OFFSET = 5;
//        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
//        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;
//
//        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
//        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, OFFSET, NB_EVENTS, BLOCK_SIZE) {
//            @Override
//            public void handlePartialResult() {
//            	TmfEvent[] events = getData();
//                for (TmfEvent e : events) {
//                    requestedEvents.add(e);
//                }
//            }
//        };
//        fEventLog.process(request, true);
//
//        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
//        assertTrue("isCompleted",  request.isCompleted());
//        assertFalse("isCancelled", request.isCancelled());
//
//        // Ensure that we have distinct events.
//        // Don't go overboard: we are not validating the stub! 
//        for (int i = 0; i < nbExpectedEvents; i++) {
//            assertEquals("Distinct events", i + OFFSET, requestedEvents.get(i).getTimestamp().getValue());
//        }
//    }
    
//    // l@Test
//    public void testProcessRequestWithNegativeOffset() throws Exception {
//        final int NB_EVENTS  = -1;
//        final int BLOCK_SIZE =  1;
//        final int OFFSET = -5;
//        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
//        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;
//
//        TmfTimeWindow range = new TmfTimeWindow(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
//        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, OFFSET, NB_EVENTS, BLOCK_SIZE) {
//            @Override
//            public void handlePartialResult() {
//            	TmfEvent[] events = getData();
//                for (TmfEvent e : events) {
//                    requestedEvents.add(e);
//                }
//            }
//        };
//        fEventLog.process(request, true);
//
//        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
//        assertTrue("isCompleted",  request.isCompleted());
//        assertFalse("isCancelled", request.isCancelled());
//
//        // Ensure that we have distinct events.
//        // Don't go overboard: we are not validating the stub! 
//        for (int i = 0; i < nbExpectedEvents; i++) {
//            assertEquals("Distinct events", i + OFFSET, requestedEvents.get(i).getTimestamp().getValue());
//        }
//    }
    
    // ========================================================================
    // cancel
    // ========================================================================

    @Test
    public void testCancel() throws Exception {
        final int NB_EVENTS  = fTotalNbEvents;
        final int BLOCK_SIZE = fDefaultBlockSize;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 0, NB_EVENTS, BLOCK_SIZE) {
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

        assertEquals("nbEvents",  BLOCK_SIZE, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}
