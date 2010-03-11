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

package org.eclipse.linuxtools.tmf.tests.request;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.tmf.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfCoalescedEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequestStub;
import org.eclipse.linuxtools.tmf.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;

/**
 * <b><u>TmfCoalescedEventRequestTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfCoalescedEventRequestTest extends TestCase {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "M-Test-10K";
    private static final int    NB_EVENTS   = 10000;
    private static final int    BLOCK_SIZE  = 100;

    private static TmfTraceStub fTrace      = null;

    private TmfTraceStub setupTrace(String path) {
    	if (fTrace == null) {
    		try {
    	        URL location = FileLocator.find(TmfCoreTestPlugin.getPlugin().getBundle(), new Path(path), null);
    			File test = new File(FileLocator.toFileURL(location).toURI());
    			TmfTraceStub trace = new TmfTraceStub(test.getPath(), 500, true);
    	        fTrace = trace;
    		} catch (URISyntaxException e) {
    			e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	return fTrace;
    }

    // ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	public TmfCoalescedEventRequestTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public void testTmfCoalescedEventRequest() {
        TmfCoalescedEventRequest<TmfEvent> request = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class);

        assertEquals("StartTime", TmfTimestamp.BigBang,   request.getRange().getStartTime());
        assertEquals("EndTime",   TmfTimestamp.BigCrunch, request.getRange().getEndTime());

        assertEquals("getIndex",             0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfCoalescedEventRequestTimeRange() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfCoalescedEventRequest<TmfEvent> request = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range);

        assertEquals("StartTime", range.getStartTime(), request.getRange().getStartTime());
        assertEquals("EndTime",   range.getEndTime(),   request.getRange().getEndTime());

        assertEquals("getIndex",             0, request.getIndex());
        assertEquals("getNbRequestedEvents", TmfDataRequest.ALL_DATA, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfCoalescedEventRequestTimeRangeNbRequested() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfCoalescedEventRequest<TmfEvent> request = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range, 100);

        assertEquals("StartTime", range.getStartTime(), request.getRange().getStartTime());
        assertEquals("EndTime",   range.getEndTime(),   request.getRange().getEndTime());

        assertEquals("getIndex",               0, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize", TmfDataRequest.DEFAULT_BLOCK_SIZE, request.getBlockize());
	}

	public void testTmfCoalescedEventRequestTimeRangeNbRequestedBlocksize() {
        TmfTimeRange range = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BigCrunch);
        TmfCoalescedEventRequest<TmfEvent> request = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range, 100, 200);

        assertEquals("StartTime", range.getStartTime(), request.getRange().getStartTime());
        assertEquals("EndTime",   range.getEndTime(),   request.getRange().getEndTime());

        assertEquals("getIndex",               0, request.getIndex());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());
        assertEquals("getBlockize",          200, request.getBlockize());
	}

	// ------------------------------------------------------------------------
	// isCompatible
	// ------------------------------------------------------------------------

	public void testIsCompatible() {
		TmfTimestamp startTime = new TmfTimestamp(10);
		TmfTimestamp endTime   = new TmfTimestamp(20);
        TmfTimeRange range1 = new TmfTimeRange(startTime, endTime);
        TmfTimeRange range2 = new TmfTimeRange(TmfTimestamp.BigBang, endTime);
        TmfTimeRange range3 = new TmfTimeRange(startTime, TmfTimestamp.BigCrunch);

        TmfCoalescedEventRequest<TmfEvent> coalescedRequest = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1);
		TmfDataRequest<TmfEvent> request1 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1);
		TmfDataRequest<TmfEvent> request2 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range2);
		TmfDataRequest<TmfEvent> request3 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range3);

        assertTrue ("isCompatible", coalescedRequest.isCompatible(request1));
        assertFalse("isCompatible", coalescedRequest.isCompatible(request2));
        assertFalse("isCompatible", coalescedRequest.isCompatible(request3));
	}

	// ------------------------------------------------------------------------
	// Coalescing
	// ------------------------------------------------------------------------

	Vector<TmfEvent> requestedEvents1;
    Vector<TmfEvent> requestedEvents2;
    Vector<TmfEvent> requestedEvents3;

    TmfEventRequest<TmfEvent> request1;
    TmfEventRequest<TmfEvent> request2;
    TmfEventRequest<TmfEvent> request3;

    private class TmfTestTriggerSignal extends TmfSignal {
    	public final boolean forceCancel;
		public TmfTestTriggerSignal(Object source, boolean cancel) {
			super(source);
			forceCancel = cancel;
		}
    }

    @SuppressWarnings("unchecked")
	@TmfSignalHandler
    public void trigger(final TmfTestTriggerSignal signal) {

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);

        requestedEvents1 = new Vector<TmfEvent>();
        request1 = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData() {
            	if (!isCompleted()) {
            		TmfEvent[] events = getData();
            		for (TmfEvent e : events) {
            			requestedEvents1.add(e);
            		}
            		if (signal.forceCancel)
            			cancel();
            	}
            }
        };

        requestedEvents2 = new Vector<TmfEvent>();
        request2 = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData() {
            	if (!isCompleted()) {
            		TmfEvent[] events = getData();
            		for (TmfEvent e : events) {
            			requestedEvents2.add(e);
            		}
            	}
            }
        };

        requestedEvents3 = new Vector<TmfEvent>();
        request3 = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData() {
            	if (!isCompleted()) {
            		TmfEvent[] events = getData();
            		for (TmfEvent e : events) {
            			requestedEvents3.add(e);
            		}
            	}
            }
        };

        ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request1);
        providers[0].sendRequest(request2);
        providers[0].sendRequest(request3);
    }

    public void testCoalescedRequest() throws Exception {

		setupTrace(DIRECTORY + File.separator + TEST_STREAM);

    	TmfSignalManager.register(this);
		TmfTestTriggerSignal signal = new TmfTestTriggerSignal(this, false);
    	TmfSignalManager.dispatchSignal(signal);

        request1.waitForCompletion();
        request2.waitForCompletion();
        request3.waitForCompletion();

        assertEquals("Request1: nbEvents", NB_EVENTS, requestedEvents1.size());
        assertTrue  ("Request1: isCompleted", request1.isCompleted());
        assertFalse ("Request1: isCancelled", request1.isCancelled());

        assertEquals("Request2: nbEvents", NB_EVENTS, requestedEvents2.size());
        assertTrue  ("Request2: isCompleted", request2.isCompleted());
        assertFalse ("Request2: isCancelled", request2.isCancelled());

        assertEquals("Request3: nbEvents", NB_EVENTS, requestedEvents3.size());
        assertTrue  ("Request3: isCompleted", request3.isCompleted());
        assertFalse ("Request3: isCancelled", request3.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i+1, requestedEvents1.get(i).getTimestamp().getValue());
            assertEquals("Distinct events", i+1, requestedEvents2.get(i).getTimestamp().getValue());
            assertEquals("Distinct events", i+1, requestedEvents3.get(i).getTimestamp().getValue());
        }

        TmfSignalManager.deregister(this);
    }
    
	public void testCancelCoalescedRequest() throws Exception {
		setupTrace(DIRECTORY + File.separator + TEST_STREAM);

    	TmfSignalManager.register(this);
		TmfTestTriggerSignal signal = new TmfTestTriggerSignal(this, true);
    	TmfSignalManager.dispatchSignal(signal);

        request1.waitForCompletion();
        request2.waitForCompletion();
        request3.waitForCompletion();

        assertEquals("Request1: nbEvents", BLOCK_SIZE, requestedEvents1.size());
        assertTrue  ("Request1: isCompleted", request1.isCompleted());
        assertTrue  ("Request1: isCancelled", request1.isCancelled());

        assertEquals("Request2: nbEvents", NB_EVENTS, requestedEvents2.size());
        assertTrue  ("Request2: isCompleted", request2.isCompleted());
        assertFalse ("Request2: isCancelled", request2.isCancelled());

        assertEquals("Request3: nbEvents", NB_EVENTS, requestedEvents3.size());
        assertTrue  ("Request3: isCompleted", request3.isCompleted());
        assertFalse ("Request3: isCancelled", request3.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i+1, requestedEvents2.get(i).getTimestamp().getValue());
            assertEquals("Distinct events", i+1, requestedEvents3.get(i).getTimestamp().getValue());
        }

        TmfSignalManager.deregister(this);
    }


}
