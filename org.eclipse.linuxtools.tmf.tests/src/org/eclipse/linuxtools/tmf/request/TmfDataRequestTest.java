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

package org.eclipse.linuxtools.tmf.request;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.ITmfRequestHandler;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * <b><u>TmfDataRequestTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfDataRequestTest {

    private static ITmfRequestHandler<TmfEvent> fProcessor = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        fProcessor = new TmfRequestHandlerStub();
    }

    // ========================================================================
    // Constructor
    // ========================================================================

    @Test
    public void testConstructorForRange() throws Exception {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 0, -1, 1);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("Offset",             0, request.getOffset());
        assertEquals("NbRequestedEvents", -1, request.getNbRequestedItems());
    }

    @Test
    public void testConstructorForNbEvents() throws Exception {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 0, 10, 1);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("Offset",             0, request.getOffset());
        assertEquals("NbRequestedEvents", 10, request.getNbRequestedItems());
    }

    @Test
    public void testConstructorWithOffset() throws Exception {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 5, 10, 1);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("Offset",             5, request.getOffset());
        assertEquals("NbRequestedEvents", 10, request.getNbRequestedItems());
    }

    @Test
    public void testConstructorWithNegativeOffset() throws Exception {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, -5, 10, 1);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());
        assertEquals("Offset",            -5, request.getOffset());
        assertEquals("NbRequestedEvents", 10, request.getNbRequestedItems());
    }

    // ========================================================================
    // process
    // ========================================================================

    @Test
    public void testProcessRequestForNbEvents() throws Exception {

        final int NB_EVENTS  = 10 * 1000;
        final int BLOCK_SIZE = 100;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handlePartialResult() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fProcessor.processRequest(request, true);

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
        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handlePartialResult() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fProcessor.processRequest(request, true);

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // @Test
    public void testProcessRequestWithOffset() throws Exception {

        final int NB_EVENTS  = -1;
        final int BLOCK_SIZE =  1;
        final int OFFSET = 5;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, OFFSET, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handlePartialResult() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fProcessor.processRequest(request, true);

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

        final int NB_EVENTS  = -1;
        final int BLOCK_SIZE =  1;
        final int OFFSET = -5;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        int nbExpectedEvents = TmfRequestHandlerStub.MAX_GENERATED_EVENTS;

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, OFFSET, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handlePartialResult() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fProcessor.processRequest(request, true);

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

        final int NB_EVENTS  = 10 * 1000;
        final int BLOCK_SIZE = 100;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, 0, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handlePartialResult() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
                // Cancel request after the first chunk is received
                cancel();
            }
        };
        fProcessor.processRequest(request, true);

        assertEquals("nbEvents",  BLOCK_SIZE, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}
