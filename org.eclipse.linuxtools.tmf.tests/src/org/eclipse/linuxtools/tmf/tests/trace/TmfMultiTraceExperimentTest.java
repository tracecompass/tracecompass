/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.tests.trace;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.experiment.TmfExperimentContext;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;

/**
 * <b><u>TmfExperimentTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfMultiTraceExperimentTest extends TestCase {

    private static final String DIRECTORY    = "testfiles";
    private static final String TEST_STREAM1 = "O-Test-10K";
    private static final String TEST_STREAM2 = "E-Test-10K";
    private static final String EXPERIMENT   = "MyExperiment";
    private static int          NB_EVENTS    = 20000;
    private static int    fDefaultBlockSize  = 1000;

    private static ITmfTrace[] fTrace;
    private static TmfExperiment<TmfEvent> fExperiment;

    private static byte SCALE = (byte) -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    private ITmfTrace[] setupTrace(String path1, String path2) {
    	if (fTrace == null) {
    		fTrace = new ITmfTrace[2];
    		try {
    	        URL location = FileLocator.find(TmfCoreTestPlugin.getPlugin().getBundle(), new Path(path1), null);
    			File test = new File(FileLocator.toFileURL(location).toURI());
    			TmfTraceStub trace1 = new TmfTraceStub(test.getPath(), true);
    			fTrace[0] = trace1;
    	        location = FileLocator.find(TmfCoreTestPlugin.getPlugin().getBundle(), new Path(path2), null);
    			test = new File(FileLocator.toFileURL(location).toURI());
    			TmfTraceStub trace2 = new TmfTraceStub(test.getPath(), true);
    			fTrace[1] = trace2;
    		} catch (URISyntaxException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	return fTrace;
    }

    private void setupExperiment() {
    	if (fExperiment == null) {
            fExperiment = new TmfExperiment<TmfEvent>(TmfEvent.class, EXPERIMENT, fTrace);
            fExperiment.indexExperiment(true);
    	}
    }

	public TmfMultiTraceExperimentTest(String name) throws Exception {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setupTrace(DIRECTORY + File.separator + TEST_STREAM1, DIRECTORY + File.separator + TEST_STREAM2);
		setupExperiment();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

	public void testBasicTmfExperiment() {
		assertEquals("GetId", EXPERIMENT, fExperiment.getName());
        assertEquals("GetEpoch", TmfTimestamp.Zero, fExperiment.getEpoch());
        assertEquals("GetNbEvents", NB_EVENTS, fExperiment.getNbEvents());

        TmfTimeRange timeRange = fExperiment.getTimeRange();
        assertEquals("getStartTime", 1, timeRange.getStartTime().getValue());
        assertEquals("getEndTime", NB_EVENTS, timeRange.getEndTime().getValue());
	}

    // ------------------------------------------------------------------------
    // seek
    // ------------------------------------------------------------------------

    public void testSeekOnCacheBoundary() throws Exception {
    	TmfExperimentContext context = fExperiment.seekLocation(null);

    	context = fExperiment.seekEvent(new TmfTimestamp(1, SCALE, 0));
    	TmfEvent event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());

    	context = fExperiment.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

    	context = fExperiment.seekEvent(new TmfTimestamp(4001, SCALE, 0));
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4000, context.getRank());
    }

    public void testSeekNotOnCacheBoundary() throws Exception {
    	TmfExperimentContext context = fExperiment.seekLocation(null);

    	context = fExperiment.seekEvent(new TmfTimestamp(10, SCALE, 0));
    	TmfEvent event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 9, context.getRank());

    	context = fExperiment.seekEvent(new TmfTimestamp(999, SCALE, 0));
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 999, event.getTimestamp().getValue());
        assertEquals("Event rank", 998, context.getRank());

    	context = fExperiment.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());

    	context = fExperiment.seekEvent(new TmfTimestamp(4500, SCALE, 0));
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 4500, event.getTimestamp().getValue());
        assertEquals("Event rank", 4499, context.getRank());
    }

    public void testSeekForEventOutOfBounds() throws Exception {
    	TmfExperimentContext context = fExperiment.seekLocation(null);

    	// On lower bound, returns the first event (ts = 1)
    	context = fExperiment.seekEvent(new TmfTimestamp(-1, SCALE, 0));
        TmfEvent event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        // On higher bound, returns null (no event)
    	context = fExperiment.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", null, event);
    }

    public void testSeekOnIndex() throws Exception {
    	TmfExperimentContext context = fExperiment.seekLocation(null);

    	// On lower bound, returns the first event (ts = 1)
    	context = fExperiment.seekEvent(0);
        TmfEvent event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        // On higher bound
    	context = fExperiment.seekEvent(NB_EVENTS - 1);
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", NB_EVENTS, event.getTimestamp().getValue());

        // Above high bound
    	context = fExperiment.seekEvent(NB_EVENTS);
        event = fExperiment.getNextEvent(context);
        assertEquals("Event", null, event);
    }

    // ------------------------------------------------------------------------
    // processRequest
    // ------------------------------------------------------------------------

    public void testProcessRequestForNbEvents() throws Exception {
        final int blockSize = 100;
        final int nbEvents  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbEvents; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    public void testProcessRequestForNbEvents2() throws Exception {
        final int blockSize = 2 * NB_EVENTS;
        final int nbEvents = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbEvents; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    public void testProcessRequestForAllEvents() throws Exception {
        final int nbEvents  = TmfEventRequest.ALL_DATA;
        final int blockSize =  1;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
        long nbExpectedEvents = fExperiment.getNbEvents();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", nbExpectedEvents, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < nbExpectedEvents; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    public void testCancel() throws Exception {
        final int nbEvents  = NB_EVENTS;
        final int blockSize =  fDefaultBlockSize;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
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
        fExperiment.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents",  blockSize, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }
    
    // ------------------------------------------------------------------------
    // getRank
    // ------------------------------------------------------------------------

    public void testGetRank() throws Exception {
        assertEquals("getRank",    0, fExperiment.getRank(new TmfTimestamp()));
        assertEquals("getRank",    0, fExperiment.getRank(new TmfTimestamp(   1, (byte) -3)));
        assertEquals("getRank",   10, fExperiment.getRank(new TmfTimestamp(  11, (byte) -3)));
        assertEquals("getRank",  100, fExperiment.getRank(new TmfTimestamp( 101, (byte) -3)));
        assertEquals("getRank", 1000, fExperiment.getRank(new TmfTimestamp(1001, (byte) -3)));
        assertEquals("getRank", 2000, fExperiment.getRank(new TmfTimestamp(2001, (byte) -3)));
        assertEquals("getRank", 2500, fExperiment.getRank(new TmfTimestamp(2501, (byte) -3)));
    }

    // ------------------------------------------------------------------------
    // getTimestamp
    // ------------------------------------------------------------------------

    public void testGetTimestamp() throws Exception {
        assertTrue("getTimestamp", fExperiment.getTimestamp(   0).equals(new TmfTimestamp(   1, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp(  10).equals(new TmfTimestamp(  11, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp( 100).equals(new TmfTimestamp( 101, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp(1000).equals(new TmfTimestamp(1001, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp(2000).equals(new TmfTimestamp(2001, (byte) -3)));
        assertTrue("getTimestamp", fExperiment.getTimestamp(2500).equals(new TmfTimestamp(2501, (byte) -3)));
    }

}