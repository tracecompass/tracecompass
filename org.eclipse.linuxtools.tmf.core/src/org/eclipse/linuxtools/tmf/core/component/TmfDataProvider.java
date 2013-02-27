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

import java.util.Vector;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfEventThread;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfCoalescedDataRequest;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * An abstract base class that implements ITmfDataProvider.
 * <p>
 * This abstract class implements the housekeeping methods to register/
 * de-register the event provider and to handle generically the event requests.
 * <p>
 * The concrete class can either re-implement processRequest() entirely or just
 * implement the hooks (initializeContext() and getNext()).
 * <p>
 *
 * @author Francois Chouinard
 * @version 1.1
 */
public abstract class TmfDataProvider extends TmfComponent implements ITmfDataProvider {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** Default amount of events per request "chunk" */
    public static final int DEFAULT_BLOCK_SIZE = 50000;

    /** Default size of the queue */
    public static final int DEFAULT_QUEUE_SIZE = 1000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The type of event handled by this provider */
    protected Class<? extends ITmfEvent> fType;

    /** Is there some data being logged? */
    protected boolean fLogData;

    /** Are errors being logged? */
    protected boolean fLogError;

    /** Queue of events */
    protected BlockingQueue<ITmfEvent> fDataQueue;

    /** Size of the fDataQueue */
    protected int fQueueSize = DEFAULT_QUEUE_SIZE;

    private TmfRequestExecutor fExecutor;

    private int fSignalDepth = 0;
    private final Object fLock = new Object();

    private int fRequestPendingCounter = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfDataProvider() {
        super();
        fQueueSize = DEFAULT_QUEUE_SIZE;
        fDataQueue = new LinkedBlockingQueue<ITmfEvent>(fQueueSize);
        fExecutor = new TmfRequestExecutor();
    }

    /**
     * Initialize this data provider
     *
     * @param name
     *            Name of the provider
     * @param type
     *            The type of events that will be handled
     */
    public void init(String name, Class<? extends ITmfEvent> type) {
        super.init(name);
        fType = type;
        fDataQueue = (fQueueSize > 1) ? new LinkedBlockingQueue<ITmfEvent>(fQueueSize) : new SynchronousQueue<ITmfEvent>();

        fExecutor = new TmfRequestExecutor();
        fSignalDepth = 0;

        fLogData = TmfCoreTracer.isEventTraced();
//        fLogError = TmfCoreTracer.isErrorTraced();

        TmfProviderManager.register(fType, this);
    }

    /**
     * Constructor specifying the event type and the queue size.
     *
     * @param name
     *            Name of the provider
     * @param type
     *            Type of event that will be handled
     * @param queueSize
     *            Size of the event queue
     */
    protected TmfDataProvider(String name, Class<? extends ITmfEvent> type, int queueSize) {
        this();
        fQueueSize = queueSize;
        init(name, type);
    }

    /**
     * Copy constructor
     *
     * @param other
     *            The other object to copy
     */
    public TmfDataProvider(TmfDataProvider other) {
        this();
        init(other.getName(), other.fType);
    }

    /**
     * Standard constructor. Instantiate and initialize at the same time.
     *
     * @param name
     *            Name of the provider
     * @param type
     *            The type of events that will be handled
     */
    public TmfDataProvider(String name, Class<? extends ITmfEvent> type) {
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

    /**
     * Get the queue size of this provider
     *
     * @return The size of the queue
     */
    public int getQueueSize() {
        return fQueueSize;
    }

    /**
     * Get the event type this provider handles
     *
     * @return The type of ITmfEvent
     */
    public Class<?> getType() {
        return fType;
    }

    // ------------------------------------------------------------------------
    // ITmfRequestHandler
    // ------------------------------------------------------------------------

    @Override
    public void sendRequest(final ITmfDataRequest request) {
        synchronized (fLock) {
            if (fSignalDepth > 0) {
                coalesceDataRequest(request);
            } else {
                dispatchRequest(request);
            }
        }
    }

    @Override
    public void fireRequest() {
        synchronized (fLock) {
            if (fRequestPendingCounter > 0) {
                return;
            }
            if (fPendingCoalescedRequests.size() > 0) {
                for (TmfDataRequest request : fPendingCoalescedRequests) {
                    dispatchRequest(request);
                }
                fPendingCoalescedRequests.clear();
            }
        }
    }

    /**
     * Increments/decrements the pending requests counters and fires the request
     * if necessary (counter == 0). Used for coalescing requests across multiple
     * TmfDataProvider's.
     *
     * @param isIncrement
     *            Should we increment (true) or decrement (false) the pending
     *            counter
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

    /** List of coalesced requests */
    protected Vector<TmfCoalescedDataRequest> fPendingCoalescedRequests = new Vector<TmfCoalescedDataRequest>();

    /**
     * Create a new request from an existing one, and add it to the coalesced
     * requests
     *
     * @param request
     *            The request to copy
     */
    protected void newCoalescedDataRequest(ITmfDataRequest request) {
        synchronized (fLock) {
            TmfCoalescedDataRequest coalescedRequest = new TmfCoalescedDataRequest(request.getDataType(), request.getIndex(),
                    request.getNbRequested(), request.getBlockSize(), request.getExecType());
            coalescedRequest.addRequest(request);
            if (TmfCoreTracer.isRequestTraced()) {
                TmfCoreTracer.traceRequest(request, "COALESCED with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
                TmfCoreTracer.traceRequest(coalescedRequest, "now contains " + coalescedRequest.getSubRequestIds()); //$NON-NLS-1$
            }
            fPendingCoalescedRequests.add(coalescedRequest);
        }
    }

    /**
     * Add an existing requests to the list of coalesced ones
     *
     * @param request
     *            The request to add to the list
     */
    protected void coalesceDataRequest(ITmfDataRequest request) {
        synchronized (fLock) {
            for (TmfCoalescedDataRequest coalescedRequest : fPendingCoalescedRequests) {
                if (coalescedRequest.isCompatible(request)) {
                    coalescedRequest.addRequest(request);
                    if (TmfCoreTracer.isRequestTraced()) {
                        TmfCoreTracer.traceRequest(request, "COALESCED with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
                        TmfCoreTracer.traceRequest(coalescedRequest, "now contains " + coalescedRequest.getSubRequestIds()); //$NON-NLS-1$
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

    private void dispatchRequest(final ITmfDataRequest request) {
        if (request.getExecType() == ExecutionType.FOREGROUND) {
            queueRequest(request);
        } else {
            queueBackgroundRequest(request, request.getBlockSize(), true);
        }
    }

    /**
     * Queue a request.
     *
     * @param request
     *            The data request
     */
    protected void queueRequest(final ITmfDataRequest request) {

        if (fExecutor.isShutdown()) {
            request.cancel();
            return;
        }

        TmfEventThread thread = new TmfEventThread(this, request);

        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(request, "QUEUED"); //$NON-NLS-1$
        }

        fExecutor.execute(thread);
    }

    /**
     * Queue a background request
     *
     * @param request
     *            The request
     * @param blockSize
     *            The request should be split in chunks of this size
     * @param indexing
     *            Should we index the chunks
     */
    protected void queueBackgroundRequest(final ITmfDataRequest request, final int blockSize, final boolean indexing) {
        queueRequest(request);
    }

    /**
     * Initialize the provider based on the request. The context is provider
     * specific and will be updated by getNext().
     *
     * @param request
     *            The request
     * @return An application specific context; null if request can't be
     *         serviced
     * @since 2.0
     */
    public abstract ITmfContext armRequest(ITmfDataRequest request);

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
    public boolean isCompleted(ITmfDataRequest request, ITmfEvent data, int nbRead) {
        return request.isCompleted() || nbRead >= request.getNbRequested();
    }

    // ------------------------------------------------------------------------
    // Pass-through's to the request executor
    // ------------------------------------------------------------------------

    /**
     * @return the shutdown state (i.e. if it is accepting new requests)
     * @since 2.0
     */
    protected boolean executorIsShutdown() {
        return fExecutor.isShutdown();
    }

    /**
     * @return the termination state
     * @since 2.0
     */
    protected boolean executorIsTerminated() {
        return fExecutor.isTerminated();
    }

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    /**
     * Handler for the start synch signal
     *
     * @param signal
     *            Incoming signal
     */
    @TmfSignalHandler
    public void startSynch(TmfStartSynchSignal signal) {
        synchronized (fLock) {
            fSignalDepth++;
        }
    }

    /**
     * Handler for the end synch signal
     *
     * @param signal
     *            Incoming signal
     */
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
