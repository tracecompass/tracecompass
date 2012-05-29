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

package org.eclipse.linuxtools.tmf.core.tests.request;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfCoalescedDataRequest;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.tests.stubs.request.TmfEventRequestStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * <b><u>TmfCoalescedEventRequestTest</u></b>
 * <p>
 * Test suite for the TmfCoalescedEventRequest class.
 */
@SuppressWarnings({ "nls" })
public class TmfCoalescedEventRequestTest extends TestCase {

	// ------------------------------------------------------------------------
	// Variables
	// ------------------------------------------------------------------------

	private TmfTimeRange range1 = new TmfTimeRange(TmfTimeRange.ETERNITY);
	private TmfTimeRange range2 = new TmfTimeRange(new TmfTimestamp(), TmfTimestamp.BIG_CRUNCH);
	
	private TmfCoalescedEventRequest<TmfEvent> fRequest1;
	private TmfCoalescedEventRequest<TmfEvent> fRequest2;
	private TmfCoalescedEventRequest<TmfEvent> fRequest3;
	private TmfCoalescedEventRequest<TmfEvent> fRequest4;

	private TmfCoalescedEventRequest<TmfEvent> fRequest1b;
	private TmfCoalescedEventRequest<TmfEvent> fRequest1c;

	private int fRequestCount;
	
	// ------------------------------------------------------------------------
	// Housekeeping
	// ------------------------------------------------------------------------

	public TmfCoalescedEventRequestTest(String name) {
		super(name);
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();
		TmfEventRequest.reset();
		fRequest1  = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1, 100, 200);
		fRequest2  = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range2, 100, 200);
		fRequest3  = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range2, 200, 200);
		fRequest4  = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range2, 200, 300);

		fRequest1b = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1, 100, 200);
		fRequest1c = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1, 100, 200);

		fRequestCount = fRequest1c.getRequestId() + 1;
	}

	@Override
	public void tearDown() throws Exception {
		super.tearDown();
	}

	private TmfCoalescedEventRequest<TmfEvent> setupTestRequest(final boolean[] flags) {
		
		TmfCoalescedEventRequest<TmfEvent> request = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1, 100, 200) {
		    @Override
			public void handleCompleted() {
		    	super.handleCompleted();
		    	flags[0] = true;
		    }
		    @Override
		    public void handleSuccess() {
		    	super.handleSuccess();
		    	flags[1] = true;
		    }
		    @Override
		    public void handleFailure() {
		    	super.handleFailure();
		    	flags[2] = true;
		    }
		    @Override
		    public void handleCancel() {
		    	super.handleCancel();
		    	flags[3] = true;
		    }
		};
		return request;
	}
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public void testTmfCoalescedEventRequest() {
		TmfCoalescedEventRequest<TmfEvent> request = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getRange", range1, request.getRange());
        assertEquals("getNbRequestedEvents", TmfEventRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfCoalescedEventRequestIndex() {
		TmfCoalescedEventRequest<TmfEvent> request = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getRange", range1, request.getRange());
        assertEquals("getNbRequestedEvents", TmfEventRequest.ALL_DATA, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfCoalescedEventRequestIndexNbRequested() {
		TmfCoalescedEventRequest<TmfEvent> request = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1, 100);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getRange", range1, request.getRange());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	public void testTmfCoalescedEventRequestIndexNbEventsBlocksize() {
		TmfCoalescedEventRequest<TmfEvent> request = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1, 100, 200);

        assertEquals("getRequestId", fRequestCount++, request.getRequestId());
        assertEquals("getDataType",  TmfEvent.class, request.getDataType());

        assertEquals("getRange", range1, request.getRange());
        assertEquals("getNbRequestedEvents", 100, request.getNbRequested());

        assertFalse("isCompleted", request.isCompleted());
        assertFalse("isFailed", request.isFailed());
        assertFalse("isCancelled", request.isCancelled());

        assertEquals("getNbRead", 0, request.getNbRead());
	}

	// ------------------------------------------------------------------------
	// equals
	// ------------------------------------------------------------------------

	public void testEqualsReflexivity() throws Exception {
        assertTrue("equals", fRequest1.equals(fRequest1));
        assertTrue("equals", fRequest2.equals(fRequest2));

        assertFalse("equals", fRequest1.equals(fRequest2));
        assertFalse("equals", fRequest2.equals(fRequest1));
	}

	public void testEqualsSymmetry() throws Exception {
        assertTrue("equals", fRequest1.equals(fRequest1b));
        assertTrue("equals", fRequest1b.equals(fRequest1));

        assertFalse("equals", fRequest1.equals(fRequest3));
        assertFalse("equals", fRequest2.equals(fRequest3));
        assertFalse("equals", fRequest3.equals(fRequest1));
        assertFalse("equals", fRequest3.equals(fRequest2));
	}
	
	public void testEqualsTransivity() throws Exception {
        assertTrue("equals", fRequest1.equals(fRequest1b));
        assertTrue("equals", fRequest1b.equals(fRequest1c));
        assertTrue("equals", fRequest1.equals(fRequest1c));
	}
	
	public void testEqualsNull() throws Exception {
        assertFalse("equals", fRequest1.equals(null));
        assertFalse("equals", fRequest2.equals(null));
	}

	public void testEqualsSuper() throws Exception {
		TmfCoalescedDataRequest<TmfEvent> dataRequest1 = new TmfCoalescedDataRequest<TmfEvent>(
				fRequest1.getDataType(), fRequest1.getIndex(), fRequest1.getNbRequested());
		TmfCoalescedDataRequest<TmfEvent> dataRequest2 = new TmfCoalescedDataRequest<TmfEvent>(
				fRequest1.getDataType(), fRequest1.getIndex(), fRequest1.getNbRequested());
		TmfCoalescedDataRequest<TmfEvent> dataRequest3 = new TmfCoalescedDataRequest<TmfEvent>(
				fRequest3.getDataType(), fRequest3.getIndex(), fRequest3.getNbRequested());

		assertTrue("equals", fRequest1.equals(dataRequest2));
        assertTrue("equals", fRequest2.equals(dataRequest1));
        assertFalse("equals", fRequest1.equals(dataRequest3));
        assertFalse("equals", fRequest3.equals(dataRequest1));
	}

	// ------------------------------------------------------------------------
	// hashCode
	// ------------------------------------------------------------------------

	public void testHashCode() throws Exception {
        assertTrue("hashCode", fRequest1.hashCode() == fRequest1.hashCode());
        assertTrue("hashCode", fRequest2.hashCode() == fRequest2.hashCode());
		assertTrue("hashCode", fRequest1.hashCode() != fRequest2.hashCode());
	}
	
	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	public void testToString() {
        String expected1 = "[TmfCoalescedEventRequest(0,TmfEvent," + range1 + ",0,100,200)]";
        String expected2 = "[TmfCoalescedEventRequest(1,TmfEvent," + range2 + ",0,100,200)]";
        String expected3 = "[TmfCoalescedEventRequest(2,TmfEvent," + range2 + ",0,200,200)]";
        String expected4 = "[TmfCoalescedEventRequest(3,TmfEvent," + range2 + ",0,200,300)]";

        assertEquals("toString", expected1, fRequest1.toString());
        assertEquals("toString", expected2, fRequest2.toString());
        assertEquals("toString", expected3, fRequest3.toString());
        assertEquals("toString", expected4, fRequest4.toString());
	}

	// ------------------------------------------------------------------------
	// isCompatible
	// ------------------------------------------------------------------------

	public void testIsCompatible() {
		TmfCoalescedEventRequest<TmfEvent> coalescedRequest = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1, 100, 200);
		TmfEventRequest<TmfEvent> request1 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
		TmfEventRequest<TmfEvent> request2 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range2, 100, 200);
		TmfEventRequest<TmfEvent> request3 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 101, 200);

        assertTrue("isCompatible", coalescedRequest.isCompatible(request1));
        assertTrue("isCompatible", coalescedRequest.isCompatible(request2));
        assertTrue("isCompatible", coalescedRequest.isCompatible(request3));
	}

    // ------------------------------------------------------------------------
    // addEvent
    // ------------------------------------------------------------------------

    public void testAddEvent1() {
        TmfCoalescedEventRequest<TmfEvent> coalescedRequest = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1, 0, 2147483647, 200, ExecutionType.FOREGROUND);
        TmfEventRequest<TmfEvent> request1 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 0, 2147483647, 200);
        TmfEventRequest<TmfEvent> request2 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 1, 2147483647, 200);

        assertTrue("isCompatible", coalescedRequest.isCompatible(request1));
        assertTrue("isCompatible", coalescedRequest.isCompatible(request2));

        coalescedRequest.addRequest(request1);
        coalescedRequest.addRequest(request2);

        assertEquals("addRequest",          0, coalescedRequest.getIndex());
        assertEquals("addRequest", 2147483647, coalescedRequest.getNbRequested());
        assertEquals("addRequest",        200, coalescedRequest.getBlockSize());

    }

    public void testAddEvent2() {
        TmfCoalescedEventRequest<TmfEvent> coalescedRequest = new TmfCoalescedEventRequest<TmfEvent>(TmfEvent.class, range1, 1, 2147483647, 200, ExecutionType.FOREGROUND);
        TmfEventRequest<TmfEvent> request1 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 1, 2147483647, 200);
        TmfEventRequest<TmfEvent> request2 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 0, 2147483647, 200);

        assertTrue("isCompatible", coalescedRequest.isCompatible(request1));
        assertTrue("isCompatible", coalescedRequest.isCompatible(request2));

        coalescedRequest.addRequest(request1);
        coalescedRequest.addRequest(request2);

        assertEquals("addRequest",          0, coalescedRequest.getIndex());
        assertEquals("addRequest", 2147483647, coalescedRequest.getNbRequested());
        assertEquals("addRequest",        200, coalescedRequest.getBlockSize());

    }

	// ------------------------------------------------------------------------
	// done
	// ------------------------------------------------------------------------

	public void testDone() {
		
		// Test request
		final boolean[] crFlags = new boolean[4];
		TmfCoalescedEventRequest<TmfEvent> request = setupTestRequest(crFlags);
		TmfEventRequest<TmfEvent> subRequest1 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
		TmfEventRequest<TmfEvent> subRequest2 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
		request.addRequest(subRequest1);
		request.addRequest(subRequest2);

		request.done();

		// Validate the coalescing request
		assertTrue ("isCompleted", request.isCompleted());
		assertFalse("isFailed",    request.isFailed());
		assertFalse("isCancelled", request.isCancelled());

		assertTrue ("handleCompleted", crFlags[0]);
		assertTrue ("handleSuccess",   crFlags[1]);
		assertFalse("handleFailure",   crFlags[2]);
		assertFalse("handleCancel",    crFlags[3]);

		// Validate the first coalesced request
		assertTrue ("isCompleted", subRequest1.isCompleted());
		assertFalse("isFailed",    subRequest1.isFailed());
		assertFalse("isCancelled", subRequest1.isCancelled());

		// Validate the second coalesced request
		assertTrue ("isCompleted", subRequest2.isCompleted());
		assertFalse("isFailed",    subRequest2.isFailed());
		assertFalse("isCancelled", subRequest2.isCancelled());
	}

	// ------------------------------------------------------------------------
	// fail
	// ------------------------------------------------------------------------

	public void testFail() {
		
		final boolean[] crFlags = new boolean[4];
		TmfCoalescedEventRequest<TmfEvent> request = setupTestRequest(crFlags);
		TmfEventRequest<TmfEvent> subRequest1 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
		TmfEventRequest<TmfEvent> subRequest2 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
		request.addRequest(subRequest1);
		request.addRequest(subRequest2);

		request.fail();

		// Validate the coalescing request
		assertTrue ("isCompleted", request.isCompleted());
		assertTrue ("isFailed",    request.isFailed());
		assertFalse("isCancelled", request.isCancelled());

		assertTrue ("handleCompleted", crFlags[0]);
		assertFalse("handleSuccess",   crFlags[1]);
		assertTrue ("handleFailure",   crFlags[2]);
		assertFalse("handleCancel",    crFlags[3]);

		// Validate the first coalesced request
		assertTrue ("isCompleted", subRequest1.isCompleted());
		assertTrue ("isFailed",    subRequest1.isFailed());
		assertFalse("isCancelled", subRequest1.isCancelled());

		// Validate the second coalesced request
		assertTrue ("isCompleted", subRequest2.isCompleted());
		assertTrue ("isFailed",    subRequest2.isFailed());
		assertFalse("isCancelled", subRequest2.isCancelled());
	}

	// ------------------------------------------------------------------------
	// cancel
	// ------------------------------------------------------------------------

	public void testCancel() {

		final boolean[] crFlags = new boolean[4];
		TmfCoalescedEventRequest<TmfEvent> request = setupTestRequest(crFlags);
		TmfEventRequest<TmfEvent> subRequest1 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
		TmfEventRequest<TmfEvent> subRequest2 = new TmfEventRequestStub<TmfEvent>(TmfEvent.class, range1, 100, 200);
		request.addRequest(subRequest1);
		request.addRequest(subRequest2);

		request.cancel();

		// Validate the coalescing request
		assertTrue ("isCompleted", request.isCompleted());
		assertFalse("isFailed",    request.isFailed());
		assertTrue ("isCancelled", request.isCancelled());

		assertTrue ("handleCompleted", crFlags[0]);
		assertFalse("handleSuccess",   crFlags[1]);
		assertFalse("handleFailure",   crFlags[2]);
		assertTrue ("handleCancel",    crFlags[3]);

		// Validate the first coalesced request
		assertTrue ("isCompleted", subRequest1.isCompleted());
		assertFalse("isFailed",    subRequest1.isFailed());
		assertTrue ("isCancelled", subRequest1.isCancelled());

		// Validate the second coalesced request
		assertTrue ("isCompleted", subRequest2.isCompleted());
		assertFalse("isFailed",    subRequest2.isFailed());
		assertTrue ("isCancelled", subRequest2.isCancelled());
	}

	// ------------------------------------------------------------------------
	// waitForCompletion
	// ------------------------------------------------------------------------

	// ------------------------------------------------------------------------
	// Coalescing
	// ------------------------------------------------------------------------

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "A-Test-10K";
    private static final int    NB_EVENTS   = 10000;
    private static final int    BLOCK_SIZE  = 100;

    // Initialize the test trace
    private TmfTraceStub fTrace = null;
    private synchronized TmfTraceStub setupTrace(String path) {
    	if (fTrace == null) {
    		try {
    	        URL location = FileLocator.find(TmfCoreTestPlugin.getDefault().getBundle(), new Path(path), null);
    	        File test = new File(FileLocator.toFileURL(location).toURI());
    	        fTrace = new TmfTraceStub(test.getPath(), 500);
    		} catch (TmfTraceException e) {
    			e.printStackTrace();
            } catch (URISyntaxException e) {
                e.printStackTrace();
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    	return fTrace;
    }

	Vector<TmfEvent> requestedEvents1;
    Vector<TmfEvent> requestedEvents2;
    Vector<TmfEvent> requestedEvents3;

    TmfEventRequest<TmfEvent> request1;
    TmfEventRequest<TmfEvent> request2;
    TmfEventRequest<TmfEvent> request3;

    ITmfDataProvider<TmfEvent>[] providers;

    private static class TmfTestTriggerSignal extends TmfSignal {
    	public final boolean forceCancel;
		public TmfTestTriggerSignal(Object source, boolean cancel) {
			super(source);
			forceCancel = cancel;

		}
    }

    @SuppressWarnings("unchecked")
	@TmfSignalHandler
    public void trigger(final TmfTestTriggerSignal signal) {

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);

        requestedEvents1 = new Vector<TmfEvent>();
        request1 = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
        	int nbRead = 0;
            @Override
            public void handleData(TmfEvent event) {
        		super.handleData(event);
            	if (!isCompleted()) {
          			requestedEvents1.add(event);
            		if (++nbRead >= BLOCK_SIZE && signal.forceCancel)
            			cancel();
            	}
            }
        };

        requestedEvents2 = new Vector<TmfEvent>();
        request2 = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(TmfEvent event) {
        		super.handleData(event);
            	if (!isCompleted()) {
            		requestedEvents2.add(event);
            	}
            }
        };

        requestedEvents3 = new Vector<TmfEvent>();
        request3 = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(TmfEvent event) {
        		super.handleData(event);
            	if (!isCompleted()) {
            		requestedEvents3.add(event);
            	}
            }
        };

        providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request1);
        providers[0].sendRequest(request2);
        providers[0].sendRequest(request3);
    }

    public void testCoalescedRequest() throws Exception {

		fTrace = setupTrace(DIRECTORY + File.separator + TEST_STREAM);

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
        fTrace.dispose();
        fTrace = null;
    }
    
	public void testCancelCoalescedRequest() throws Exception {

		fTrace = setupTrace(DIRECTORY + File.separator + TEST_STREAM);

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
        fTrace.dispose();
        fTrace = null;
    }

}
