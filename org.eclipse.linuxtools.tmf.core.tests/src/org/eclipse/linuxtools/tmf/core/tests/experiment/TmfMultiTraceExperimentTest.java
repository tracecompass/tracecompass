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

package org.eclipse.linuxtools.tmf.core.tests.experiment;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.experiment.TmfExperimentContext;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.TmfContext;
import org.eclipse.linuxtools.tmf.stubs.trace.TmfTraceStub;

/**
 * <b><u>TmfExperimentTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
@SuppressWarnings("nls")
public class TmfMultiTraceExperimentTest extends TestCase {

    private static final String DIRECTORY    = "testfiles";
    private static final String TEST_STREAM1 = "O-Test-10K";
    private static final String TEST_STREAM2 = "E-Test-10K";
    private static final String EXPERIMENT   = "MyExperiment";
    private static int          NB_EVENTS    = 20000;
    private static int          BLOCK_SIZE   = 1000;

    private static ITmfTrace<?>[] fTraces;
    private static TmfExperiment<TmfEvent> fExperiment;

    private static byte SCALE = (byte) -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

    private synchronized static ITmfTrace<?>[] setupTrace(String path1, String path2) {
    	if (fTraces == null) {
    		fTraces = new ITmfTrace[2];
    		try {
    	        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path1), null);
    			File test = new File(FileLocator.toFileURL(location).toURI());
    			TmfTraceStub trace1 = new TmfTraceStub(test.getPath(), true);
    			fTraces[0] = trace1;
    	        location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path2), null);
    			test = new File(FileLocator.toFileURL(location).toURI());
    			TmfTraceStub trace2 = new TmfTraceStub(test.getPath(), true);
    			fTraces[1] = trace2;
    		} catch (URISyntaxException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	return fTraces;
    }

    @SuppressWarnings("unchecked")
    private synchronized static void setupExperiment() {
    	synchronized (TmfMultiTraceExperimentTest.class) {
    		if (fExperiment == null) {
    			fExperiment = new TmfExperiment<TmfEvent>(TmfEvent.class, EXPERIMENT, (ITmfTrace<TmfEvent>[]) fTraces, TmfTimestamp.Zero, BLOCK_SIZE, true);
    		}
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

	public void testBasicTmfExperimentConstructor() {

		assertEquals("GetId", EXPERIMENT, fExperiment.getName());
        assertEquals("GetEpoch", TmfTimestamp.Zero, fExperiment.getEpoch());
        assertEquals("GetNbEvents", NB_EVENTS, fExperiment.getNbEvents());

        TmfTimeRange timeRange = fExperiment.getTimeRange();
        assertEquals("getStartTime", 1, timeRange.getStartTime().getValue());
        assertEquals("getEndTime", NB_EVENTS, timeRange.getEndTime().getValue());
	}

    // ------------------------------------------------------------------------
    // Verify checkpoints
    // ------------------------------------------------------------------------

    public void testValidateCheckpoints() throws Exception {

    	Vector<TmfCheckpoint> checkpoints = fExperiment.getCheckpoints();
    	int pageSize = fExperiment.getCacheSize();
		assertTrue("Checkpoints exist", checkpoints != null);

		// Validate that each checkpoint points to the right event
		for (int i = 0; i < checkpoints.size(); i++) {
			TmfCheckpoint checkpoint = checkpoints.get(i);
        	TmfExperimentContext context = fExperiment.seekLocation(checkpoint.getLocation());
			TmfEvent event = fExperiment.parseEvent(context);
			assertEquals("Event rank", context.getRank(), i * pageSize);
			assertTrue("Timestamp", (checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
		}        	
    }

    // ------------------------------------------------------------------------
    // seekLocation
    // ------------------------------------------------------------------------

    public void testSeekLocationOnCacheBoundary() throws Exception {

    	// Position trace at event rank 0
    	TmfContext context = fExperiment.seekLocation(null);
        assertEquals("Event rank", 0, context.getRank());

        TmfEvent event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
        assertEquals("Event rank", 2, context.getRank());

    	// Position trace at event rank 1000
        TmfContext tmpContext = fExperiment.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        context = fExperiment.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", 1000, context.getRank());
    	
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1002, context.getRank());

    	// Position trace at event rank 4000
        tmpContext = fExperiment.seekEvent(new TmfTimestamp(4001, SCALE, 0));
        context = fExperiment.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", 4000, context.getRank());
    	
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4000, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4001, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 4002, event.getTimestamp().getValue());
        assertEquals("Event rank", 4002, context.getRank());
    }

    public void testSeekLocationNotOnCacheBoundary() throws Exception {

    	// Position trace at event rank 9
    	TmfContext tmpContext = fExperiment.seekEvent(new TmfTimestamp(10, SCALE, 0));
        TmfContext context = fExperiment.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", 9, context.getRank());
    	
        TmfEvent event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 9, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 10, context.getRank());

    	// Position trace at event rank 999
        tmpContext = fExperiment.seekEvent(new TmfTimestamp(1000, SCALE, 0));
        context = fExperiment.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", 999, context.getRank());
    	
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
        assertEquals("Event rank", 1000, context.getRank());

    	// Position trace at event rank 1001
        tmpContext = fExperiment.seekEvent(new TmfTimestamp(1002, SCALE, 0));
        context = fExperiment.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", 1001, context.getRank());
    	
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
        assertEquals("Event rank", 1002, context.getRank());

    	// Position trace at event rank 4500
        tmpContext = fExperiment.seekEvent(new TmfTimestamp(4501, SCALE, 0));
        context = fExperiment.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", 4500, context.getRank());
    	
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4500, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
        assertEquals("Event rank", 4501, context.getRank());
    }

    public void testSeekLocationOutOfScope() throws Exception {

    	// Position trace at beginning
    	TmfContext tmpContext = fExperiment.seekLocation(null);
        TmfContext context = fExperiment.seekLocation(tmpContext.getLocation());
        assertEquals("Event rank", 0, context.getRank());
    	
        TmfEvent event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 0, context.getRank());
    	
        event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

    	// Position trace at event passed the end
        tmpContext = fExperiment.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        assertEquals("Event location", null, tmpContext.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, tmpContext.getRank());
    }

	// ------------------------------------------------------------------------
	// seekEvent on timestamp
	// ------------------------------------------------------------------------

	public void testSeekEventOnTimestampOnCacheBoundary() throws Exception {

		// Position trace at event rank 0
		TmfContext context = fExperiment.seekEvent(new TmfTimestamp(1, SCALE, 0));
		assertEquals("Event rank", 0, context.getRank());

		TmfEvent event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
		assertEquals("Event rank", 0, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
		assertEquals("Event rank", 1, context.getRank());

		// Position trace at event rank 1000
		context = fExperiment.seekEvent(new TmfTimestamp(1001, SCALE, 0));
		assertEquals("Event rank", 1000, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
		assertEquals("Event rank", 1000, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
		assertEquals("Event rank", 1001, context.getRank());

		// Position trace at event rank 4000
		context = fExperiment.seekEvent(new TmfTimestamp(4001, SCALE, 0));
		assertEquals("Event rank", 4000, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
		assertEquals("Event rank", 4000, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
		assertEquals("Event rank", 4001, context.getRank());
	}

	public void testSeekEventOnTimestampNotOnCacheBoundary() throws Exception {

		// Position trace at event rank 1
		TmfContext context = fExperiment.seekEvent(new TmfTimestamp(2, SCALE, 0));
		assertEquals("Event rank", 1, context.getRank());
		
		TmfEvent event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
		assertEquals("Event rank", 1, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 2, event.getTimestamp().getValue());
		assertEquals("Event rank", 2, context.getRank());

		// Position trace at event rank 9
		context = fExperiment.seekEvent(new TmfTimestamp(10, SCALE, 0));
		assertEquals("Event rank", 9, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
		assertEquals("Event rank", 9, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
		assertEquals("Event rank", 10, context.getRank());

		// Position trace at event rank 999
		context = fExperiment.seekEvent(new TmfTimestamp(1000, SCALE, 0));
		assertEquals("Event rank", 999, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
		assertEquals("Event rank", 999, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
		assertEquals("Event rank", 1000, context.getRank());

		// Position trace at event rank 1001
		context = fExperiment.seekEvent(new TmfTimestamp(1002, SCALE, 0));
		assertEquals("Event rank", 1001, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
		assertEquals("Event rank", 1001, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
		assertEquals("Event rank", 1002, context.getRank());

		// Position trace at event rank 4500
		context = fExperiment.seekEvent(new TmfTimestamp(4501, SCALE, 0));
		assertEquals("Event rank", 4500, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
		assertEquals("Event rank", 4500, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
		assertEquals("Event rank", 4501, context.getRank());
	}

	public void testSeekEventOnTimestampOutOfScope() throws Exception {

		// Position trace at beginning
		TmfContext context = fExperiment.seekEvent(new TmfTimestamp(-1, SCALE, 0));
		assertEquals("Event rank", 0, context.getRank());
		
		TmfEvent event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
		assertEquals("Event rank", 0, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
		assertEquals("Event rank", 1, context.getRank());

		// Position trace at event passed the end
		context = fExperiment.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        assertEquals("Event location", null, context.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
	}

	// ------------------------------------------------------------------------
	// seekEvent on rank
	// ------------------------------------------------------------------------

	public void testSeekOnRankOnCacheBoundary() throws Exception {

		// On lower bound, returns the first event (ts = 1)
		TmfContext context = fExperiment.seekEvent(0);
		assertEquals("Event rank", 0, context.getRank());
		
		TmfEvent event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
		assertEquals("Event rank", 0, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
		assertEquals("Event rank", 1, context.getRank());

		// Position trace at event rank 1000
		context = fExperiment.seekEvent(1000);
		assertEquals("Event rank", 1000, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
		assertEquals("Event rank", 1000, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
		assertEquals("Event rank", 1001, context.getRank());

		// Position trace at event rank 4000
		context = fExperiment.seekEvent(4000);
		assertEquals("Event rank", 4000, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
		assertEquals("Event rank", 4000, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
		assertEquals("Event rank", 4001, context.getRank());
	}

	public void testSeekOnRankNotOnCacheBoundary() throws Exception {

		// Position trace at event rank 9
		TmfContext context = fExperiment.seekEvent(9);
		assertEquals("Event rank", 9, context.getRank());
		
		TmfEvent event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
		assertEquals("Event rank", 9, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
		assertEquals("Event rank", 10, context.getRank());

		// Position trace at event rank 999
		context = fExperiment.seekEvent(999);
		assertEquals("Event rank", 999, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
		assertEquals("Event rank", 999, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1000, event.getTimestamp().getValue());
		assertEquals("Event rank", 1000, context.getRank());

		// Position trace at event rank 1001
		context = fExperiment.seekEvent(1001);
		assertEquals("Event rank", 1001, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
		assertEquals("Event rank", 1001, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1002, event.getTimestamp().getValue());
		assertEquals("Event rank", 1002, context.getRank());

		// Position trace at event rank 4500
		context = fExperiment.seekEvent(4500);
		assertEquals("Event rank", 4500, context.getRank());
		
		event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
		assertEquals("Event rank", 4500, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 4501, event.getTimestamp().getValue());
		assertEquals("Event rank", 4501, context.getRank());
	}

	public void testSeekEventOnRankOutOfScope() throws Exception {

		// Position trace at beginning
		TmfContext context = fExperiment.seekEvent(-1);
		assertEquals("Event rank", 0, context.getRank());
		
		TmfEvent event = fExperiment.parseEvent(context);
		assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
		assertEquals("Event rank", 0, context.getRank());
		
		event = fExperiment.getNextEvent(context);
		assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
		assertEquals("Event rank", 1, context.getRank());

		// Position trace at event passed the end
		context = fExperiment.seekEvent(NB_EVENTS);
        assertEquals("Event location", null, context.getLocation());
        assertEquals("Event rank", ITmfContext.UNKNOWN_RANK, context.getRank());
	}

    // ------------------------------------------------------------------------
    // parseEvent - make sure parseEvent doesn't update the context
    // Note: This test is essentially the same as the one from TmfTraceTest
    // ------------------------------------------------------------------------

    public void testParseEvent() throws Exception {

    	int NB_READS = 20;

    	// On lower bound, returns the first event (ts = 1)
    	TmfContext context = fExperiment.seekEvent(new TmfTimestamp(0, SCALE, 0));

    	// Read NB_EVENTS
    	TmfEvent event = null;;
        for (int i = 0; i < NB_READS; i++) {
            event = fExperiment.getNextEvent(context);
            assertEquals("Event timestamp", i + 1, event.getTimestamp().getValue());
            assertEquals("Event rank", i + 1, context.getRank());
        }

        // Make sure we stay positioned
        event = fExperiment.parseEvent(context);
        assertEquals("Event timestamp", NB_READS + 1, event.getTimestamp().getValue());
        assertEquals("Event rank", NB_READS, context.getRank());
    }

    // ------------------------------------------------------------------------
    // getNextEvent - updates the context
    // ------------------------------------------------------------------------

    public void testGetNextEvent() throws Exception {

    	// On lower bound, returns the first event (ts = 0)
    	TmfContext context = fExperiment.seekEvent(new TmfTimestamp(0, SCALE, 0));
        TmfEvent event = fExperiment.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        for (int i = 2; i < 20; i++) {
            event = fExperiment.getNextEvent(context);
            assertEquals("Event timestamp", i, event.getTimestamp().getValue());
        }
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
        	public void handleData(TmfEvent event) {
        		super.handleData(event);
        		requestedEvents.add(event);
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
        	public void handleData(TmfEvent event) {
        		super.handleData(event);
        		requestedEvents.add(event);
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
        long nbExpectedEvents = NB_EVENTS;

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
        	@Override
        	public void handleData(TmfEvent event) {
        		super.handleData(event);
        		requestedEvents.add(event);
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
        final int blockSize = BLOCK_SIZE;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, nbEvents, blockSize) {
        	int nbRead = 0;
        	@Override
        	public void handleData(TmfEvent event) {
        		super.handleData(event);
        		requestedEvents.add(event);
        		if (++nbRead == blockSize)
        			cancel();
        	}
            @Override
            public void handleCancel() {
            	if (requestedEvents.size() < blockSize) {
            		System.out.println("aie");
            	}
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