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

package org.eclipse.linuxtools.tmf.core.component;

import org.eclipse.linuxtools.internal.tmf.core.Tracer;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;

/**
 * An extension of TmfDataProvider timestamped events providers.
 * 
 * @version 1.0
 * @author Francois Chouinard
 */
public abstract class TmfEventProvider<T extends ITmfEvent> extends TmfDataProvider<T> {

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
    public void init(String name, Class<T> type) {
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
   public TmfEventProvider(String name, Class<T> type) {
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
    public TmfEventProvider(String name, Class<T> type, int queueSize) {
        super(name, type, queueSize);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            The other TmfEventProvider to copy
     */
    public TmfEventProvider(TmfEventProvider<T> other) {
        super(other);
    }

    // ------------------------------------------------------------------------
    // TmfDataProvider
    // ------------------------------------------------------------------------

    @Override
    public boolean isCompleted(ITmfDataRequest<T> request, T data, int nbRead) {
        boolean requestCompleted = super.isCompleted(request, data, nbRead);
        if (!requestCompleted && request instanceof ITmfEventRequest<?>) {
            ITmfTimestamp endTime = ((ITmfEventRequest<?>) request).getRange().getEndTime();
            return data.getTimestamp().compareTo(endTime, false) > 0;
        }
        return requestCompleted;
    }

    @Override
    protected synchronized void newCoalescedDataRequest(ITmfDataRequest<T> request) {
        if (request instanceof ITmfEventRequest<?>) {
            ITmfEventRequest<T> eventRequest = (ITmfEventRequest<T>) request;
            TmfCoalescedEventRequest<T> coalescedRequest = new TmfCoalescedEventRequest<T>(eventRequest.getDataType(), eventRequest.getRange(),
                    eventRequest.getIndex(), eventRequest.getNbRequested(), eventRequest.getBlockSize(), eventRequest.getExecType());
            coalescedRequest.addRequest(eventRequest);
            if (Tracer.isRequestTraced()) {
                Tracer.traceRequest(request, "COALESCED with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
                Tracer.traceRequest(coalescedRequest, "now contains " + coalescedRequest.getSubRequestIds()); //$NON-NLS-1$
            }
            fPendingCoalescedRequests.add(coalescedRequest);
        } else {
            super.newCoalescedDataRequest(request);
        }
    }

    @Override
    protected void queueBackgroundRequest(final ITmfDataRequest<T> request, final int blockSize, final boolean indexing) {

        if (! (request instanceof ITmfEventRequest)) {
            super.queueBackgroundRequest(request, blockSize, indexing);
            return;
        }

        final TmfDataProvider<T> provider = this;

        Thread thread = new Thread() {
            @Override
            public void run() {

                if (Tracer.isRequestTraced()) {
                    Tracer.traceRequest(request, "is being serviced by " + provider.getName()); //$NON-NLS-1$
                }

                request.start();

                final Integer[] CHUNK_SIZE = new Integer[1];
                CHUNK_SIZE[0] = Math.min(request.getNbRequested(), blockSize + ((indexing) ? 1 : 0));

                final Integer[] nbRead = new Integer[1];
                nbRead[0] = 0;

                final Boolean[] isFinished = new Boolean[1];
                isFinished[0] = Boolean.FALSE;

                long startIndex = request.getIndex();

                while (!isFinished[0]) {

                    TmfEventRequest<T> subRequest= new TmfEventRequest<T>(request.getDataType(), ((ITmfEventRequest<?>) request).getRange(), startIndex + nbRead[0], CHUNK_SIZE[0], blockSize, ExecutionType.BACKGROUND) {

                        @Override
                        public synchronized boolean isCompleted() {
                            return super.isCompleted() || request.isCompleted();
                        }

                        @Override
                        public void handleData(T data) {
                            super.handleData(data);
                            if (request.getDataType().isInstance(data)) {
                                request.handleData(data);
                            }
                            if (this.getNbRead() > CHUNK_SIZE[0]) {
                                System.out.println("ERROR - Read too many events"); //$NON-NLS-1$
                            }
                        }

                        @Override
                        public void handleCompleted() {
                            nbRead[0] += this.getNbRead();
                            if (nbRead[0] >= request.getNbRequested() || (this.getNbRead() < CHUNK_SIZE[0])) {
                                if (this.isCancelled()) { 
                                    request.cancel();
                                } else if (this.isFailed()) {
                                    request.fail();  
                                } else {
                                    request.done();
                                }
                                isFinished[0] = Boolean.TRUE;
                            }
                            super.handleCompleted();
                        }
                    };

                    if (!isFinished[0]) {
                        queueRequest(subRequest);

                        try {
                            subRequest.waitForCompletion();
                            if (request.isCompleted()) {
                                isFinished[0] = Boolean.TRUE;
                            }
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (startIndex == 0 && nbRead[0].equals(CHUNK_SIZE[0])) { // do this only once if the event request index is unknown
                            startIndex = subRequest.getIndex(); // update the start index with the index of the first subrequest's
                        }                                       // start time event which was set during the arm request
                        CHUNK_SIZE[0] = Math.min(request.getNbRequested() - nbRead[0], blockSize);
                    }
                }
            }
        };

        thread.start();
    }
}
