/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Replace background requests by pre-emptable requests
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.component;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * An extension of TmfDataProvider timestamped events providers.
 *
 * @author Francois Chouinard
 * @version 1.1
 */
public abstract class TmfEventProvider extends TmfDataProvider {

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEventProvider() {
        super();
    }

    @Override
    public void init(String name, Class<? extends ITmfEvent> type) {
        super.init(name, type);
    }

    /**
     * Standard constructor
     *
     * @param name
     *            The name of the provider
     * @param type
     *            The type of handled events
     */
   public TmfEventProvider(String name, Class<? extends ITmfEvent> type) {
        super(name, type);
    }

    /**
     * Standard constructor which also sets the queue size
     *
     * @param name
     *            The name of the provider
     * @param type
     *            The type of handled events
     * @param queueSize
     *            The size of the queue
     */
    public TmfEventProvider(String name, Class<? extends ITmfEvent> type, int queueSize) {
        super(name, type, queueSize);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            The other TmfEventProvider to copy
     */
    public TmfEventProvider(TmfEventProvider other) {
        super(other);
    }

    // ------------------------------------------------------------------------
    // TmfDataProvider
    // ------------------------------------------------------------------------

    @Override
    public boolean isCompleted(ITmfDataRequest request, ITmfEvent data, int nbRead) {
        boolean requestCompleted = super.isCompleted(request, data, nbRead);
        if (!requestCompleted && request instanceof ITmfEventRequest) {
            ITmfTimestamp endTime = ((ITmfEventRequest) request).getRange().getEndTime();
            return data.getTimestamp().compareTo(endTime, false) > 0;
        }
        return requestCompleted;
    }

    @Override
    protected synchronized void newCoalescedDataRequest(ITmfDataRequest request) {
        if (request instanceof ITmfEventRequest) {
            ITmfEventRequest eventRequest = (ITmfEventRequest) request;
            TmfCoalescedEventRequest coalescedRequest = new TmfCoalescedEventRequest(
                            eventRequest.getDataType(),
                            eventRequest.getRange(),
                            eventRequest.getIndex(),
                            eventRequest.getNbRequested(),
                            eventRequest.getExecType());
            coalescedRequest.addRequest(eventRequest);
            if (TmfCoreTracer.isRequestTraced()) {
                TmfCoreTracer.traceRequest(request, "COALESCED with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
                TmfCoreTracer.traceRequest(coalescedRequest, "now contains " + coalescedRequest.getSubRequestIds()); //$NON-NLS-1$
            }
            fPendingCoalescedRequests.add(coalescedRequest);
        } else {
            super.newCoalescedDataRequest(request);
        }
    }

}
