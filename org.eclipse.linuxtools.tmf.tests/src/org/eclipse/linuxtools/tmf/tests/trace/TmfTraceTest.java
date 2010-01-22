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
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;

/**
 * <b><u>TmfTraceTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceTest extends TestCase {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "M-Test-10K";
    private static final int NB_EVENTS = 10000;
    private static TmfTraceStub fTrace =  null;

    private static byte SCALE = (byte) -3;

    // ========================================================================
    // Housekeeping
    // ========================================================================

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

    public TmfTraceTest(String name) throws Exception {
		super(name);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		setupTrace(DIRECTORY + File.separator + TEST_STREAM);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

    // ========================================================================
    // Constructors
    // ========================================================================

    public void testTmfTraceDefault() throws Exception {
		TmfTraceStub trace = null;
		try {
	        URL location = FileLocator.find(TmfCoreTestPlugin.getPlugin().getBundle(), new Path(DIRECTORY + File.separator + TEST_STREAM), null);
			File test = new File(FileLocator.toFileURL(location).toURI());
			trace = new TmfTraceStub(test.getPath(), true);
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		assertTrue("Oen Trace", trace != null);

		assertEquals("getCacheSize", TmfTraceStub.DEFAULT_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getTraceSize",   0, trace.getNbEvents());
        assertEquals("getRange-start", 0, trace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   0, trace.getTimeRange().getEndTime().getValue());
    }

    public void testTmfTrace() throws Exception {
        assertEquals("getCacheSize", 500, fTrace.getCacheSize());
        assertEquals("getTraceSize",   0, fTrace.getNbEvents());
        assertEquals("getRange-start", 0, fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   0, fTrace.getTimeRange().getEndTime().getValue());
    }

    // ========================================================================
    // seek
    // ========================================================================

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

    public void testProcessRequestForNbEvents() throws Exception {
        final int BLOCK_SIZE = 100;
        final int NB_EVENTS  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, NB_EVENTS, BLOCK_SIZE) {
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
    
    public void testProcessRequestForAllEvents() throws Exception {
        final int BLOCK_SIZE =  1;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();
//        long nbExpectedEvents = NB_EVENTS;

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, NB_EVENTS, BLOCK_SIZE) {
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
    
    // ========================================================================
    // cancel
    // ========================================================================

    public void testCancel() throws Exception {
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfDataRequest<TmfEvent> request = new TmfDataRequest<TmfEvent>(range, NB_EVENTS, NB_EVENTS) {
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
