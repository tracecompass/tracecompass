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

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.eclipse.linuxtools.internal.tmf.core.Tracer;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfCoalescedDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfRequestExecutor;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * <b><u>TmfProvider</u></b>
 * <p>
 * The TmfProvider<T> is a provider for a data of type <T>.
 * <p>
 * This abstract class implements the housekeeking methods to register/ deregister the event provider and to handle
 * generically the event requests.
 * <p>
 * The concrete class can either re-implement processRequest() entirely or just implement the hooks (initializeContext()
 * and getNext()).
 * <p>
 * TODO: Add support for providing multiple data types.
 */
public abstract class TmfDataProvider<T extends ITmfEvent> extends TmfComponent implements ITmfDataProvider<T> {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    public static final int DEFAULT_BLOCK_SIZE = 50000;
    public static final int DEFAULT_QUEUE_SIZE = 1000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    protected Class<T> fType;
    protected boolean fLogData;
    protected boolean fLogError;

    protected int fQueueSize = DEFAULT_QUEUE_SIZE;
    protected BlockingQueue<T> fDataQueue;
    protected TmfRequestExecutor fExecutor;

    private int fSignalDepth = 0;
    private final Object fLock = new Object();

    private int fRequestPendingCounter = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfDataProvider() {
        super();
        fQueueSize = DEFAULT_QUEUE_SIZE;
        fDataQueue = new LinkedBlockingQueue<T>(fQueueSize);
        fExecutor = new TmfRequestExecutor();
    }

    public void init(String name, Class<T> type) {
        super.init(name);
        fType = type;
        fDataQueue = (fQueueSize > 1) ? new LinkedBlockingQueue<T>(fQueueSize) : new SynchronousQueue<T>();

        fExecutor = new TmfRequestExecutor();
        fSignalDepth = 0;

        fLogData = Tracer.isEventTraced();
        fLogError = Tracer.isErrorTraced();

        TmfProviderManager.register(fType, this);
    }

    protected TmfDataProvider(String name, Class<T> type, int queueSize) {
        this();
        fQueueSize = queueSize;
        init(name, type);
    }

    public TmfDataProvider(TmfDataProvider<T> other) {
        this();
        init(other.getName(), other.fType);
    }

    public TmfDataProvider(String name, Class<T> type) {
        this(name, type, DEFAULT_QUEUE_SIZE);
    }

    @Override
    public void dispose() {
        TmfProviderManager.deregister(fType, this);
        fExecutor.stop();
        super.dispose();
        // if (Tracer.isComponentTraced()) Tracer.traceComponent(this, "stopped");
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public int getQueueSize() {
        return fQueueSize;
    }

    public Class<?> getType() {
        return fType;
    }

    // ------------------------------------------------------------------------
    // ITmfRequestHandler
    // ------------------------------------------------------------------------

    @Override
    public void sendRequest(final ITmfDataRequest<T> request) {
        synchronized (fLock) {
            if (fSignalDepth > 0) {
                coalesceDataRequest(request);
            } else {
                dispatchRequest(request);
            }
        }
    }

    /**
     * This method queues the coalesced requests.
     */
    @Override
    public void fireRequest() {
        synchronized (fLock) {
            if (fRequestPendingCounter > 0) {
                return;
            }
            if (fPendingCoalescedRequests.size() > 0) {
                for (TmfDataRequest<T> request : fPendingCoalescedRequests) {
                    dispatchRequest(request);
                }
                fPendingCoalescedRequests.clear();
            }
        }
    }

    /**
     * Increments/decrements the pending requests counters and fires the request if necessary (counter == 0). Used for
     * coalescing requests accross multiple TmfDataProvider.
     * 
     * @param isIncrement
     */
    @Override
    public void notifyPendingRequest(boolean isIncrement) {
        synchronized (fLock) {
            if (isIncrement) {
                if (fSignalDepth > 0) {
                    fRequestPendingCounter++;
                }
            } else {
                if (fRequestPendingCounter > 0) {
                    fRequestPendingCounter--;
                }

                // fire request if all pending requests are received
                if (fRequestPendingCounter == 0) {
                    fireRequest();
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Coalescing (primitive test...)
    // ------------------------------------------------------------------------

    protected Vector<TmfCoalescedDataRequest<T>> fPendingCoalescedRequests = new Vector<TmfCoalescedDataRequest<T>>();

    protected void newCoalescedDataRequest(ITmfDataRequest<T> request) {
        synchronized (fLock) {
            TmfCoalescedDataRequest<T> coalescedRequest = new TmfCoalescedDataRequest<T>(request.getDataType(), request.getIndex(),
                    request.getNbRequested(), request.getBlockSize(), request.getExecType());
            coalescedRequest.addRequest(request);
            if (Tracer.isRequestTraced()) {
                Tracer.traceRequest(request, "coalesced with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
            }
            fPendingCoalescedRequests.add(coalescedRequest);
        }
    }

    protected void coalesceDataRequest(ITmfDataRequest<T> request) {
        synchronized (fLock) {
            for (TmfCoalescedDataRequest<T> coalescedRequest : fPendingCoalescedRequests) {
                if (coalescedRequest.isCompatible(request)) {
                    coalescedRequest.addRequest(request);
                    if (Tracer.isRequestTraced()) {
                        Tracer.traceRequest(request, "coalesced with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
                    }
                    return;
                }
            }
            newCoalescedDataRequest(request);
        }
    }

    // ------------------------------------------------------------------------
    // Request processing
    // ------------------------------------------------------------------------

    private void dispatchRequest(final ITmfDataRequest<T> request) {
        if (request.getExecType() == ExecutionType.FOREGROUND)
            queueRequest(request);
        else
            queueBackgroundRequest(request, request.getBlockSize(), true);
    }

    protected void queueRequest(final ITmfDataRequest<T> request) {

        if (fExecutor.isShutdown()) {
            request.cancel();
            return;
        }

        final TmfDataProvider<T> provider = this;

        // Process the request
        TmfThread thread = new TmfThread(request.getExecType()) {
            
            @Override
            public void run() {

                if (Tracer.isRequestTraced())
                    Tracer.trace("Request #" + request.getRequestId() + " is being serviced by " + provider.getName()); //$NON-NLS-1$//$NON-NLS-2$

                // Extract the generic information
                request.start();
                int nbRequested = request.getNbRequested();
                int nbRead = 0;

                // Initialize the execution
                ITmfContext context = armRequest(request);
                if (context == null) {
                    request.cancel();
                    return;
                }

                try {
                    // Get the ordered events
                    T data = getNext(context);
                    if (Tracer.isRequestTraced())
                        Tracer.trace("Request #" + request.getRequestId() + " read first event"); //$NON-NLS-1$ //$NON-NLS-2$
                    while (data != null && !isCompleted(request, data, nbRead)) {
                        if (fLogData)
                            Tracer.traceEvent(provider, request, data);
                        if (request.getDataType().isInstance(data)) {
                            request.handleData(data);
                        }

                        // To avoid an unnecessary read passed the last data
                        // requested
                        if (++nbRead < nbRequested) {
                            data = getNext(context);
                        }
                    }
                    if (Tracer.isRequestTraced())
                        Tracer.trace("Request #" + request.getRequestId() + " finished"); //$NON-NLS-1$//$NON-NLS-2$

                    if (request.isCancelled()) {
                        request.cancel();
                    } else {
                        request.done();
                    }
                } catch (Exception e) {
                    request.fail();
                }

                // Cleanup
                context.dispose();
            }

            @Override
            public void cancel() {
                if (!request.isCompleted()) {
                    request.cancel();
                }
            }
        };

        if (Tracer.isRequestTraced())
            Tracer.traceRequest(request, "queued"); //$NON-NLS-1$
        fExecutor.execute(thread);

    }

    protected void queueBackgroundRequest(final ITmfDataRequest<T> request, final int blockSize, final boolean indexing) {

        Thread thread = new Thread() {
            @Override
            public void run() {
                request.start();

                final Integer[] CHUNK_SIZE = new Integer[1];
                CHUNK_SIZE[0] = Math.min(request.getNbRequested(), blockSize + ((indexing) ? 1 : 0));

                final Integer[] nbRead = new Integer[1];
                nbRead[0] = 0;

                final Boolean[] isFinished = new Boolean[1];
                isFinished[0] = Boolean.FALSE;

                while (!isFinished[0]) {

                    TmfDataRequest<T> subRequest = new TmfDataRequest<T>(request.getDataType(), request.getIndex()
                            + nbRead[0], CHUNK_SIZE[0], blockSize, ExecutionType.BACKGROUND) {
                        @Override
                        public void handleData(T data) {
                            super.handleData(data);
                            if (request.getDataType().isInstance(data)) {
                                request.handleData(data);
                            }
                            if (getNbRead() > CHUNK_SIZE[0]) {
                                System.out.println("ERROR - Read too many events"); //$NON-NLS-1$
                            }
                        }

                        @Override
                        public void handleCompleted() {
                            nbRead[0] += getNbRead();
                            if (nbRead[0] >= request.getNbRequested() || (getNbRead() < CHUNK_SIZE[0])) {
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
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        CHUNK_SIZE[0] = Math.min(request.getNbRequested() - nbRead[0], blockSize);
                    }
                }
            }
        };

        thread.start();
    }

    /**
     * Initialize the provider based on the request. The context is provider
     * specific and will be updated by getNext().
     * 
     * @param request
     * @return an application specific context; null if request can't be serviced
     */
    public abstract ITmfContext armRequest(ITmfDataRequest<T> request);

//    /**
//     * Return the next event based on the context supplied. The context
//     * will be updated for the subsequent read.
//     * 
//     * @param context the trace read context (updated)
//     * @return the event referred to by context
//     */
//    public abstract T getNext(ITmfContext context);

    /**
     * Checks if the data meets the request completion criteria.
     * 
     * @param request the request
     * @param data the data to verify
     * @param nbRead the number of events read so far
     * @return true if completion criteria is met
     */
    public boolean isCompleted(ITmfDataRequest<T> request, T data, int nbRead) {
        return request.isCompleted() || nbRead >= request.getNbRequested();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void startSynch(TmfStartSynchSignal signal) {
        synchronized (fLock) {
            fSignalDepth++;
        }
    }

    @TmfSignalHandler
    public void endSynch(TmfEndSynchSignal signal) {
        synchronized (fLock) {
            fSignalDepth--;
            if (fSignalDepth == 0) {
                fireRequest();
            }
        }
    }

}
