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

package org.eclipse.linuxtools.tmf.component;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfSyntheticEventStub;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfContext;

/**
 * <b><u>TmfSyntheticEventProviderStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfSyntheticEventProviderStub extends TmfEventProvider<TmfSyntheticEventStub> {

    public static final int BLOCK_SIZE = 100;
    public static final int NB_EVENTS  = 1000;

    public TmfSyntheticEventProviderStub() {
		super("TmfSyntheticEventProviderStub", TmfSyntheticEventStub.class);
	}

	@SuppressWarnings("unchecked")
	@Override
	public ITmfContext armRequest(final ITmfDataRequest<TmfSyntheticEventStub> request) {

		// Get the TmfSyntheticEventStub provider
		ITmfDataProvider<TmfEvent>[] eventProviders = (ITmfDataProvider<TmfEvent>[]) TmfProviderManager.getProviders(TmfEvent.class, TmfEventProviderStub.class);
		ITmfDataProvider<TmfEvent> provider = eventProviders[0];

		// make sure we have the right type of request
		if (!(request instanceof ITmfEventRequest<?>)) {
			request.cancel();
			return null;
		}

		TmfEventRequest<TmfSyntheticEventStub> eventRequest = (TmfEventRequest<TmfSyntheticEventStub>) request;
        TmfTimeRange range = eventRequest.getRange();
        final TmfEventRequest<TmfEvent> subRequest =
        	new TmfEventRequest<TmfEvent>(TmfEvent.class, range, NB_EVENTS, BLOCK_SIZE) {
            	@Override
            	public void handleData() {
            		TmfEvent[] events = getData();
            		if (events.length > 0) {
            			for (TmfEvent e : events) {
            				handleIncomingData(e);
            			}
            		} else {
            			request.done();
            		}
            	}
        	};
        provider.sendRequest(subRequest);

        // Return a dummy context
        return new TmfContext();
	}

	// Queue 2 synthetic events per base event
	private void handleIncomingData(TmfEvent e) {
		try {
			queueResult(new TmfSyntheticEventStub(e));
			queueResult(new TmfSyntheticEventStub(e));
		} catch (InterruptedException e1) {
//			e1.printStackTrace();
		}
	}

	@Override
	public void sendRequest(ITmfDataRequest<TmfSyntheticEventStub> request) {
		super.sendRequest(request);
	}
	
}
