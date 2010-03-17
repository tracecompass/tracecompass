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
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfCoalescedEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;

/**
 * <b><u>TmfEventProvider</u></b>
 * <p>
 * Implement me. Please.
 */
public abstract class TmfEventProvider<T extends TmfEvent> extends TmfDataProvider<T> {

	public TmfEventProvider(Class<T> type) {
		super("TmfEventProvider", type);
	}

	public TmfEventProvider(Class<T> type, int queueSize) {
		super("TmfEventProvider", type, queueSize);
	}

	@Override
	public boolean isCompleted(TmfDataRequest<T> request, T data, int nbRead) {
		boolean dataRequestCompleted = super.isCompleted(request, data, nbRead);
		if (!dataRequestCompleted && request instanceof TmfEventRequest<?> && data != null) {
			TmfTimestamp endTime = ((TmfEventRequest<?>) request).getRange().getEndTime();
			return data.getTimestamp().compareTo(endTime, false) > 0;
		}
		return dataRequestCompleted;
	}

	@Override
	protected synchronized void newCoalescedDataRequest(TmfDataRequest<T> request) {
		if (request instanceof TmfEventRequest<?>) {
			TmfEventRequest<T> eventRequest = (TmfEventRequest<T>) request;
			TmfCoalescedEventRequest<T> coalescedRequest = 
				new TmfCoalescedEventRequest<T>(fType, eventRequest.getRange(), eventRequest.getNbRequested(), eventRequest.getBlockize());
			coalescedRequest.addRequest(eventRequest);
			fPendingCoalescedRequests.add(coalescedRequest);
		}
		else {
			super.newCoalescedDataRequest(request);
		}
	}

}
