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
import org.eclipse.linuxtools.tmf.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.tests.TmfCoreTestPlugin;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;

/**
 * <b><u>TmfTraceTest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfTraceTest extends TestCase {

    private static final String DIRECTORY   = "testfiles";
    private static final String TEST_STREAM = "A-Test-10K";
    private static final int    NB_EVENTS   = 10000;
    private static TmfTraceStub fTrace      = null;

    private static byte SCALE = (byte) -3;

    // ------------------------------------------------------------------------
    // Housekeeping
    // ------------------------------------------------------------------------

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

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

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
		assertTrue("Open trace",     trace != null);
		assertEquals("getCacheSize", TmfTraceStub.DEFAULT_CACHE_SIZE, trace.getCacheSize());
        assertEquals("getTraceSize", NB_EVENTS, trace.getNbEvents());
    }

    public void testTmfTrace() throws Exception {
        assertEquals("getCacheSize",   500, fTrace.getCacheSize());
        assertEquals("getTraceSize",   NB_EVENTS, fTrace.getNbEvents());
        assertEquals("getRange-start", 1, fTrace.getTimeRange().getStartTime().getValue());
        assertEquals("getRange-end",   NB_EVENTS, fTrace.getTimeRange().getEndTime().getValue());
    }

    // ------------------------------------------------------------------------
    // seek
    // ------------------------------------------------------------------------

    public void testSeekOnCacheBoundary() throws Exception {
    	TmfContext context = fTrace.seekLocation(null);

    	context = fTrace.seekEvent(new TmfTimestamp(1, SCALE, 0));
    	TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());
        assertEquals("Event rank", 1, context.getRank());

    	context = fTrace.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

    	context = fTrace.seekEvent(new TmfTimestamp(4001, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4001, event.getTimestamp().getValue());
        assertEquals("Event rank", 4001, context.getRank());
    }

    public void testSeekNotOnCacheBoundary() throws Exception {
    	TmfContext context = fTrace.seekLocation(null);

    	context = fTrace.seekEvent(new TmfTimestamp(10, SCALE, 0));
    	TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 10, event.getTimestamp().getValue());
        assertEquals("Event rank", 10, context.getRank());

    	context = fTrace.seekEvent(new TmfTimestamp(999, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 999, event.getTimestamp().getValue());
        assertEquals("Event rank", 999, context.getRank());

    	context = fTrace.seekEvent(new TmfTimestamp(1001, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1001, event.getTimestamp().getValue());
        assertEquals("Event rank", 1001, context.getRank());

    	context = fTrace.seekEvent(new TmfTimestamp(4499, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 4499, event.getTimestamp().getValue());
        assertEquals("Event rank", 4499, context.getRank());
    }

    public void testSeekForEventOutOfBounds() throws Exception {
    	TmfContext context = fTrace.seekLocation(null);

    	// On lower bound, returns the first event (ts = 1)
    	context = fTrace.seekEvent(new TmfTimestamp(-1, SCALE, 0));
        TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        // On higher bound, returns null (no event)
    	context = fTrace.seekEvent(new TmfTimestamp(NB_EVENTS + 1, SCALE, 0));
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", null, event);
    }

    public void testSeekOnIndex() throws Exception {
    	TmfContext context = fTrace.seekLocation(null);

    	// On lower bound, returns the first event (ts = 1)
    	context = fTrace.seekEvent(0);
        TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        // On higher bound
    	context = fTrace.seekEvent(NB_EVENTS - 1);
        event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", NB_EVENTS, event.getTimestamp().getValue());

        // Above high bound
    	context = fTrace.seekEvent(NB_EVENTS);
        event = fTrace.getNextEvent(context);
        assertEquals("Event", null, event);
    }

    // ------------------------------------------------------------------------
    // getNextEvent
    // ------------------------------------------------------------------------

    public void testGetNextEvent() throws Exception {
    	TmfContext context = fTrace.seekLocation(null);

    	// On lower bound, returns the first event (ts = 0)
    	context = fTrace.seekEvent(new TmfTimestamp(0, SCALE, 0));
        TmfEvent event = fTrace.getNextEvent(context);
        assertEquals("Event timestamp", 1, event.getTimestamp().getValue());

        for (int i = 2; i < 20; i++) {
            event = fTrace.getNextEvent(context);
            assertEquals("Event timestamp", i, event.getTimestamp().getValue());
        }
    }

    // ------------------------------------------------------------------------
    // processRequest
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
	public void testProcessRequestForNbEvents() throws Exception {
        final int BLOCK_SIZE = 100;
        final int NB_EVENTS  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    @SuppressWarnings("unchecked")
	public void testProcessRequestForAllEvents() throws Exception {
        final int BLOCK_SIZE =  1;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData() {
            	TmfEvent[] events = getData();
                for (TmfEvent e : events) {
                    requestedEvents.add(e);
                }
            }
        };
        ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Ensure that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
    }
    
    // ------------------------------------------------------------------------
    // cancel
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
	public void testCancel() throws Exception {
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, NB_EVENTS) {
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
        ITmfDataProvider<TmfEvent>[] providers = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        providers[0].sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents",  NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertTrue("isCancelled", request.isCancelled());
    }

}