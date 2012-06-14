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

package org.eclipse.linuxtools.tmf.core.tests.component;

import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.linuxtools.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.linuxtools.tmf.tests.stubs.component.TmfEventProviderStub;
import org.eclipse.linuxtools.tmf.tests.stubs.component.TmfSyntheticEventProviderStub;
import org.eclipse.linuxtools.tmf.tests.stubs.event.TmfSyntheticEventStub;
import org.eclipse.linuxtools.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * <b><u>TmfClientTest</u></b>
 * <p>
 * Test suite for the TmfEventProvider class.
 */
@SuppressWarnings({ "nls" })
public class TmfEventProviderTest extends TestCase {

    TmfEventProviderStub fEventProvider;
    TmfSyntheticEventProviderStub fSyntheticEventProvider;

    public TmfEventProviderTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        fEventProvider = new TmfEventProviderStub();
        fSyntheticEventProvider = new TmfSyntheticEventProviderStub();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        fEventProvider.dispose();
        fSyntheticEventProvider.dispose();
    }

    // ------------------------------------------------------------------------
    // getProviders (more a sanity check than a test)
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public void testGetProviders() {

        // There should be 2 TmfEvent providers: a TmfTraceStub and a
        // TmfEventProviderStub
        ITmfDataProvider<TmfEvent>[] eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
        assertEquals("getProviders", 2, eventProviders.length);

        eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        assertEquals("getProviders", 1, eventProviders.length);

        eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfEventProviderStub.class);
        assertEquals("getProviders", 1, eventProviders.length);

        // There should be 1 TmfSyntheticEventStub provider
        eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfSyntheticEventStub.class);
        assertEquals("getProviders", 1, eventProviders.length);
    }

    // ------------------------------------------------------------------------
    // getSyntheticEvent
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public void testGetPlainEvents() {

        final int BLOCK_SIZE = 100;
        final int NB_EVENTS = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        // Get the TmfSyntheticEventStub provider
        ITmfDataProvider<TmfEvent>[] eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class,
                TmfEventProviderStub.class);
        ITmfDataProvider<TmfEvent> provider = eventProviders[0];

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        final TmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(TmfEvent event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };

        provider.sendRequest(request);
        try {
            request.waitForCompletion();
            assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
            assertTrue("isCompleted", request.isCompleted());
            assertFalse("isCancelled", request.isCancelled());

            // Make that we have distinct events.
            // Don't go overboard: we are not validating the stub!
            for (int i = 0; i < NB_EVENTS; i++) {
                assertEquals("Distinct events", i + 1, requestedEvents.get(i).getTimestamp().getValue());
            }
        } catch (InterruptedException e) {
            fail();
        }
    }

    @SuppressWarnings("unchecked")
    public void testCancelRequests() {

        final int BLOCK_SIZE = 100;
        final int NB_EVENTS = 1000;
        final int NUMBER_EVENTS_BEFORE_CANCEL_REQ1 = 10;
        final int NUMBER_EVENTS_BEFORE_CANCEL_REQ2 = 800;

        final Vector<TmfEvent> requestedEventsReq1 = new Vector<TmfEvent>();
        final Vector<TmfEvent> requestedEventsReq2 = new Vector<TmfEvent>();

        // Get the TmfSyntheticEventStub provider
        ITmfDataProvider<TmfEvent>[] eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class,
                TmfEventProviderStub.class);
        ITmfDataProvider<TmfEvent> provider = eventProviders[0];

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);

        // Create first request
        final TmfEventRequest<TmfEvent> request1 = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(TmfEvent event) {
                super.handleData(event);
                requestedEventsReq1.add(event);

                // cancel sub request
                if (getNbRead() == NUMBER_EVENTS_BEFORE_CANCEL_REQ1) {
                    cancel();
                }
            }
        };

        // Synchronize requests
        ((TmfEventProviderStub) provider).startSynch(new TmfStartSynchSignal(0));

        // Additionally, notify provider for up-coming requests
        provider.notifyPendingRequest(true);

        // Call sendRequest, which will create a coalescing request, but it
        // doesn't send request1 yet
        provider.sendRequest(request1);

        // Check if request1 is not running yet.
        assertFalse("isRunning", request1.isRunning());

        // Create second request
        final TmfEventRequest<TmfEvent> request2 = new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            @Override
            public void handleData(TmfEvent event) {
                super.handleData(event);
                requestedEventsReq2.add(event);

                // cancel sub request which will cancel also main request
                if (getNbRead() == NUMBER_EVENTS_BEFORE_CANCEL_REQ2) {
                    cancel();
                }
            }
        };

        // Call sendRequest, which will create a coalescing request, but it
        // doesn't send request2 yet
        provider.sendRequest(request2);

        // Check if request1/2 is not running yet.
        assertFalse("isRunning", request1.isRunning());
        assertFalse("isRunning", request2.isRunning());

        // Send end synch signal, however requests won't be sent
        ((TmfEventProviderStub) provider).endSynch(new TmfEndSynchSignal(0));

        // Check if request1/2 is not running yet.
        assertFalse("isRunning", request1.isRunning());
        assertFalse("isRunning", request2.isRunning());

        // Finally, trigger sending of requests
        provider.notifyPendingRequest(false);

        try {

            // Wait until requests start
            request1.waitForStart();
            request2.waitForStart();

//	        // Verify that the requests are running
//	        assertTrue("isRunning", request1.isRunning());
//	        assertTrue("isRunning", request2.isRunning());

            request1.waitForCompletion();

//	        // Check if request2 is still running
//            assertTrue("isRunning",  request2.isRunning());

            // Verify result (request1)
            assertEquals("nbEvents", NUMBER_EVENTS_BEFORE_CANCEL_REQ1, requestedEventsReq1.size());
            assertTrue("isCompleted", request1.isCompleted());
            assertTrue("isCancelled", request1.isCancelled());

            request2.waitForCompletion();

            // Verify result (request2)
            assertEquals("nbEvents", NUMBER_EVENTS_BEFORE_CANCEL_REQ2, requestedEventsReq2.size());
            assertTrue("isCompleted", request2.isCompleted());
            assertTrue("isCancelled", request2.isCancelled());

        } catch (InterruptedException e) {
            fail();
        }
    }

    @SuppressWarnings("unchecked")
    private static void getSyntheticData(final TmfTimeRange range,
            final int nbEvents, final int blockSize) throws InterruptedException {

        final Vector<TmfSyntheticEventStub> requestedEvents = new Vector<TmfSyntheticEventStub>();

        // Get the event provider
        ITmfDataProvider<TmfSyntheticEventStub>[] eventProviders = (ITmfDataProvider<TmfSyntheticEventStub>[]) TmfProviderManager
                .getProviders(TmfSyntheticEventStub.class);
        ITmfDataProvider<TmfSyntheticEventStub> provider = eventProviders[0];

        final TmfEventRequest<TmfSyntheticEventStub> request = new TmfEventRequest<TmfSyntheticEventStub>(TmfSyntheticEventStub.class, range,
                nbEvents, blockSize) {
            @Override
            public void handleData(TmfSyntheticEventStub event) {
                super.handleData(event);
                requestedEvents.add(event);
            }
        };
        provider.sendRequest(request);

        request.waitForCompletion();
        if (nbEvents != -1) {
            assertEquals("nbEvents", nbEvents, requestedEvents.size());
        }
        assertTrue("isCompleted", request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // For each base event, the stub will queue 2 identical synthetic events
        // Ensure that the events are queued properly.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < (nbEvents / 2); i++) {
            assertEquals("Distinct events", i + 1, requestedEvents.get(2 * i + 0).getTimestamp().getValue());
            assertEquals("Distinct events", i + 1, requestedEvents.get(2 * i + 1).getTimestamp().getValue());
        }
    }

    // The following tests are the same but for the size of the requested blocks
    // with regards to the size of the TmfSyntheticEventProviderStub block
    public void testGetSyntheticEvents_EqualBlockSizes() {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        try {
            getSyntheticData(range, 1000, TmfSyntheticEventProviderStub.BLOCK_SIZE);
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testGetSyntheticEvents_SmallerBlock() {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        try {
            getSyntheticData(range, 1000, TmfSyntheticEventProviderStub.BLOCK_SIZE / 2);
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testGetSyntheticEvents_LargerBlock() {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        try {
            getSyntheticData(range, 1000, TmfSyntheticEventProviderStub.BLOCK_SIZE * 2);
        } catch (InterruptedException e) {
            fail();
        }
    }

    public void testGetSyntheticEvents_TimeRange() {
        TmfTimestamp start = new TmfTimestamp(1, (byte) -3, 0);
        TmfTimestamp end = new TmfTimestamp(1000, (byte) -3, 0);
        TmfTimeRange range = new TmfTimeRange(start, end);
        try {
            getSyntheticData(range, -1, TmfSyntheticEventProviderStub.BLOCK_SIZE);
        } catch (InterruptedException e) {
            fail();
        }
    }

//    public void testGetSyntheticEvents_WeirdTimeRange1() {
//        TmfTimestamp start = TmfTimestamp.BigBang;
//        TmfTimestamp end = TmfTimestamp.Zero; // new TmfTimestamp(0, (byte) -3,
//                                              // 0);
//        TmfTimeRange range = new TmfTimeRange(start, end);
//        try {
//            getSyntheticData(range, -1, TmfSyntheticEventProviderStub.BLOCK_SIZE);
//        } catch (InterruptedException e) {
//            fail();
//        }
//    }

//    public void testGetSyntheticEvents_WeirdTimeRange2() {
//        TmfTimestamp start = TmfTimestamp.Zero; // new TmfTimestamp(0, (byte)
//                                                // -3, 0);
//        TmfTimestamp end = TmfTimestamp.BigCrunch;
//        TmfTimeRange range = new TmfTimeRange(start, end);
//        try {
//            getSyntheticData(range, -1, TmfSyntheticEventProviderStub.BLOCK_SIZE);
//        } catch (InterruptedException e) {
//            fail();
//        }
//    }

    // ------------------------------------------------------------------------
    // getProviders (more a sanity check than a test)
    // ------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    public void testGetProviders2() {

        // There should be 2 TmfEvent providers: a TmfTraceStub and a
        // TmfEventProviderStub
        ITmfDataProvider<TmfEvent>[] eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class);
        assertEquals("getProviders", 2, eventProviders.length);

        eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfTraceStub.class);
        assertEquals("getProviders", 1, eventProviders.length);

        eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfEventProviderStub.class);
        assertEquals("getProviders", 1, eventProviders.length);

        // There should be 1 TmfSyntheticEventStub provider
        eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfSyntheticEventStub.class);
        assertEquals("getProviders", 1, eventProviders.length);
    }

}
