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

package org.eclipse.linuxtools.tmf.tests.component;

import java.io.IOException;
import java.util.Vector;

import junit.framework.TestCase;

import org.eclipse.linuxtools.tmf.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.component.TmfEventProviderStub;
import org.eclipse.linuxtools.tmf.component.TmfProviderManager;
import org.eclipse.linuxtools.tmf.component.TmfSyntheticEventProviderStub;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfSyntheticEventStub;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.trace.TmfTraceStub;

/**
 * <b><u>TmfClientTest</u></b>
 * <p>
 * Test suite for the TmfEventProvider class.
 */
public class TmfEventProviderTest extends TestCase {

	TmfEventProviderStub fEventProvider;
	TmfSyntheticEventProviderStub fSyntheticEventProvider;

	public TmfEventProviderTest(String name) throws IOException {
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

		// There should be 2 TmfEvent providers: a TmfTraceStub and a TmfEventProviderStub
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
	public void testGetPlainEvents() throws InterruptedException {

        final int BLOCK_SIZE = 100;
        final int NB_EVENTS  = 1000;
        final Vector<TmfEvent> requestedEvents = new Vector<TmfEvent>();

        // Get the TmfSyntheticEventStub provider
		ITmfDataProvider<TmfEvent>[] eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfEventProviderStub.class);
		ITmfDataProvider<TmfEvent> provider = eventProviders[0];

        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
        final TmfEventRequest<TmfEvent> request =
        	new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            	@Override
            	public void handleData() {
            		TmfEvent[] events = getData();
            		for (TmfEvent e : events) {
            			requestedEvents.add(e);
            		}
            	}
        	};
        provider.sendRequest(request);
        request.waitForCompletion();

        assertEquals("nbEvents", NB_EVENTS, requestedEvents.size());
        assertTrue("isCompleted",  request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // Make that we have distinct events.
        // Don't go overboard: we are not validating the stub! 
        for (int i = 0; i < NB_EVENTS; i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(i).getTimestamp().getValue());
        }
	}

	@SuppressWarnings("unchecked")
	private void getSyntheticData(final TmfTimeRange range, final int nbEvents, final int blockSize) throws InterruptedException {

        final Vector<TmfSyntheticEventStub> requestedEvents = new Vector<TmfSyntheticEventStub>();

        // Get the event provider
		ITmfDataProvider<TmfSyntheticEventStub>[] eventProviders = (ITmfDataProvider<TmfSyntheticEventStub>[]) TmfProviderManager.getProviders(TmfSyntheticEventStub.class);
		ITmfDataProvider<TmfSyntheticEventStub> provider = eventProviders[0];

        final TmfEventRequest<TmfSyntheticEventStub> request =
        	new TmfEventRequest<TmfSyntheticEventStub>(TmfSyntheticEventStub.class, range, nbEvents, blockSize) {
            	@Override
            	public void handleData() {
            		TmfSyntheticEventStub[] events = getData();
            		for (TmfSyntheticEventStub e : events) {
            			requestedEvents.add(e);
            		}
            	}
        	};
        provider.sendRequest(request);
        request.waitForCompletion();

        if (nbEvents != -1)
        	assertEquals("nbEvents", nbEvents, requestedEvents.size());
        assertTrue("isCompleted", request.isCompleted());
        assertFalse("isCancelled", request.isCancelled());

        // For each base event, the stub will queue 2 identical synthetic events
        // Ensure that the events are queued properly.
        // Don't go overboard: we are not validating the stub!
        for (int i = 0; i < (nbEvents / 2); i++) {
            assertEquals("Distinct events", i+1, requestedEvents.get(2 * i + 0).getTimestamp().getValue());
            assertEquals("Distinct events", i+1, requestedEvents.get(2 * i + 1).getTimestamp().getValue());
        }
	}

	// The following tests are the same but for the size of the requested blocks
	// with regards to the size of the TmfSyntheticEventProviderStub block
    public void testGetSyntheticEvents_EqualBlockSizes() throws InterruptedException {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
		getSyntheticData(range, 1000, TmfSyntheticEventProviderStub.BLOCK_SIZE);
	}

	public void testGetSyntheticEvents_SmallerBlock() throws InterruptedException {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
		getSyntheticData(range, 1000, TmfSyntheticEventProviderStub.BLOCK_SIZE / 2);
	}

	public void testGetSyntheticEvents_LargerBlock() throws InterruptedException {
        TmfTimeRange range = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
		getSyntheticData(range, 1000, TmfSyntheticEventProviderStub.BLOCK_SIZE * 2);
	}

	public void testGetSyntheticEvents_TimeRange() throws InterruptedException {
		TmfTimestamp start = new TmfTimestamp(   1, (byte) -3, 0);
		TmfTimestamp end   = new TmfTimestamp(1000, (byte) -3, 0);
        TmfTimeRange range = new TmfTimeRange(start, end);
		getSyntheticData(range, -1, TmfSyntheticEventProviderStub.BLOCK_SIZE);
	}

	public void testGetSyntheticEvents_WeirdTimeRange1() throws InterruptedException {
		TmfTimestamp start = TmfTimestamp.BigBang;
		TmfTimestamp end   = new TmfTimestamp(0, (byte) -3, 0);
        TmfTimeRange range = new TmfTimeRange(start, end);
		getSyntheticData(range, -1, TmfSyntheticEventProviderStub.BLOCK_SIZE);
	}

	public void testGetSyntheticEvents_WeirdTimeRange2() throws InterruptedException {
		TmfTimestamp start = new TmfTimestamp(0, (byte) -3, 0);
		TmfTimestamp end   = TmfTimestamp.BigCrunch;
        TmfTimeRange range = new TmfTimeRange(start, end);
		getSyntheticData(range, -1, TmfSyntheticEventProviderStub.BLOCK_SIZE);
	}

	// ------------------------------------------------------------------------
	// getProviders (more a sanity check than a test)
	// ------------------------------------------------------------------------

	@SuppressWarnings("unchecked")
	public void testGetProviders2() {

		// There should be 2 TmfEvent providers: a TmfTraceStub and a TmfEventProviderStub
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
