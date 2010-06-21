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

import org.eclipse.linuxtools.tmf.Tracer;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfCoalescedEventRequest;

/**
 * <b><u>TmfEventProvider</u></b>
 * <p>
 * Implement me. Please.
 */
public abstract class TmfEventProvider<T extends TmfEvent> extends TmfDataProvider<T> {

	public TmfEventProvider(String name, Class<T> type) {
		super(name, type);
	}

	public TmfEventProvider(String name, Class<T> type, int queueSize) {
		super(name, type, queueSize);
	}
	
	public TmfEventProvider(TmfEventProvider<T> oldProvider) {
		super(oldProvider);
	}
	
	@Override
	public boolean isCompleted(ITmfDataRequest<T> request, T data, int nbRead) {
		boolean dataRequestCompleted = super.isCompleted(request, data, nbRead);
		if (!dataRequestCompleted && request instanceof ITmfEventRequest<?> && !data.isNullRef()) {
			TmfTimestamp endTime = ((ITmfEventRequest<?>) request).getRange().getEndTime();
			return data.getTimestamp().compareTo(endTime, false) > 0;
		}
		return dataRequestCompleted;
	}

	@Override
	protected synchronized void newCoalescedDataRequest(ITmfDataRequest<T> request) {
		if (request instanceof ITmfEventRequest<?>) {
			ITmfEventRequest<T> eventRequest = (ITmfEventRequest<T>) request;
			TmfCoalescedEventRequest<T> coalescedRequest = 
				new TmfCoalescedEventRequest<T>(fType, eventRequest.getRange(), eventRequest.getNbRequested(), eventRequest.getBlockize(), eventRequest.getExecType());
			coalescedRequest.addRequest(eventRequest);
	        if (Tracer.isRequestTraced()) {
		        Tracer.traceRequest(request, "coalesced with " + coalescedRequest.getRequestId());
		        Tracer.traceRequest(coalescedRequest, "added " + request.getRequestId());
	        }
			fPendingCoalescedRequests.add(coalescedRequest);
		}
		else {
			super.newCoalescedDataRequest(request);
		}
	}

}
