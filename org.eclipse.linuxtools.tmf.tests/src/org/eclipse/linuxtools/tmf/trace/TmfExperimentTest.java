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
 * <b><u>TmfEventLogTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfExperimentTest {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "M-Test-10K";
    private static final String EXPERIMENT  = "MyExperiment";
    private static String testfile;
    private static int NB_EVENTS = 10000;
    private static int fDefaultBlockSize = 1000;

//    private static ITmfEventParser fParser;
    private static ITmfTrace fStream;
    private static TmfExperiment fExperiment;

//    private static byte SCALE = (byte) -3;

    @BeforeClass
	public static void setUpBeforeClass() throws Exception {
    	String directory = new File(".").getCanonicalPath() + File.separator + DIRECTORY;
    	testfile = directory + File.separator + TEST_STREAM;

		fStream = new TmfTraceStub(testfile);
        fExperiment = new TmfExperiment(EXPERIMENT, new ITmfTrace[] { fStream });
	}

    // ========================================================================
    // Constructor
    // ========================================================================

	@Test
	public void testBasicTmfTrace() {
		assertEquals("GetId", EXPERIMENT, fExperiment.getExperimentId());
        assertEquals("GetEpoch", TmfTimestamp.BigBang, fExperiment.getEpoch());
        assertEquals("GetNbEvents", NB_EVENTS, fExperiment.getNbEvents());

        TmfTimeRange timeRange = fExperiment.getTimeRange();
        assertEquals("GetTimeRange-start", 1, timeRange.getStartTime().getValue());
        assertEquals("GetTimeRange-end", NB_EVENTS, timeRange.getEndTime().getValue());
	}

    // ========================================================================
    // Operators
    // ========================================================================

    @Test
    public void testProcessRequestForNbEvents() throws Exception {
        final int BLOCK_SIZE = 100;
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
        fExperiment.processRequest(request, true);

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
        int nbExpectedEvents = fExperiment.getNbEvents();

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
        fExperiment.processRequest(request, true);

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
        fExperiment.processRequest(request, true);

        assertEquals("nbEvents",  BLOCK_SIZE, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}
