/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation, replace background
 *       requests by preemptable requests
 *   Alexandre Montplaisir - Merge with TmfDataProvider
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfEventThread;
import org.eclipse.linuxtools.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.linuxtools.internal.tmf.core.request.TmfRequestExecutor;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * An abstract base class that implements ITmfEventProvider.
 * <p>
 * This abstract class implements the housekeeping methods to register/
 * de-register the event provider and to handle generically the event requests.
 * </p>
 *
 * @author Francois Chouinard
 */
public abstract class TmfEventProvider extends TmfComponent implements ITmfEventProvider {

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

    /** List of coalesced requests */
    protected final List<TmfCoalescedEventRequest> fPendingCoalescedRequests =
            new ArrayList<TmfCoalescedEventRequest>();

    /** The type of event handled by this provider */
    protected Class<? extends ITmfEvent> fType;

    /** Queue of events */
    protected BlockingQueue<ITmfEvent> fDataQueue;

    /** Size of the fDataQueue */
    protected int fQueueSize = DEFAULT_QUEUE_SIZE;

    private final TmfRequestExecutor fExecutor;

    private final Object fLock = new Object();

    private int fSignalDepth = 0;

    private int fRequestPendingCounter = 0;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEventProvider() {
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

        fExecutor.init();
        fSignalDepth = 0;

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
    protected TmfEventProvider(String name, Class<? extends ITmfEvent> type, int queueSize) {
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
    public TmfEventProvider(TmfEventProvider other) {
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
    public TmfEventProvider(String name, Class<? extends ITmfEvent> type) {
        this(name, type, DEFAULT_QUEUE_SIZE);
    }

    @Override
    public void dispose() {
        TmfProviderManager.deregister(fType, this);
        fExecutor.stop();
        super.dispose();
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
    public void sendRequest(final ITmfEventRequest request) {
        synchronized (fLock) {
            if (fSignalDepth > 0) {
                coalesceEventRequest(request);
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
                for (ITmfEventRequest request : fPendingCoalescedRequests) {
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
    // Coalescing
    // ------------------------------------------------------------------------

    /**
     * Create a new request from an existing one, and add it to the coalesced
     * requests
     *
     * @param request
     *            The request to copy
     */
    protected synchronized void newCoalescedEventRequest(ITmfEventRequest request) {
            TmfCoalescedEventRequest coalescedRequest = new TmfCoalescedEventRequest(
                    request.getDataType(),
                    request.getRange(),
                    request.getIndex(),
                    request.getNbRequested(),
                    request.getExecType());
            coalescedRequest.addRequest(request);
            if (TmfCoreTracer.isRequestTraced()) {
                TmfCoreTracer.traceRequest(request, "COALESCED with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
                TmfCoreTracer.traceRequest(coalescedRequest, "now contains " + coalescedRequest.getSubRequestIds()); //$NON-NLS-1$
            }
            fPendingCoalescedRequests.add(coalescedRequest);
    }

    /**
     * Add an existing requests to the list of coalesced ones
     *
     * @param request
     *            The request to add to the list
     */
    protected void coalesceEventRequest(ITmfEventRequest request) {
        synchronized (fLock) {
            for (TmfCoalescedEventRequest coalescedRequest : fPendingCoalescedRequests) {
                if (coalescedRequest.isCompatible(request)) {
                    coalescedRequest.addRequest(request);
                    if (TmfCoreTracer.isRequestTraced()) {
                        TmfCoreTracer.traceRequest(request, "COALESCED with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
                        TmfCoreTracer.traceRequest(coalescedRequest, "now contains " + coalescedRequest.getSubRequestIds()); //$NON-NLS-1$
                    }
                    return;
                }
            }
            newCoalescedEventRequest(request);
        }
    }

    // ------------------------------------------------------------------------
    // Request processing
    // ------------------------------------------------------------------------

    private void dispatchRequest(final ITmfEventRequest request) {
        if (request.getExecType() == ExecutionType.FOREGROUND) {
            queueRequest(request);
        } else {
            queueBackgroundRequest(request, true);
        }
    }

    /**
     * Queue a request.
     *
     * @param request
     *            The data request
     */
    protected void queueRequest(final ITmfEventRequest request) {

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
     * @param indexing
     *            Should we index the chunks
     * @since 3.0
     */
    protected void queueBackgroundRequest(final ITmfEventRequest request, final boolean indexing) {
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
    public abstract ITmfContext armRequest(ITmfEventRequest request);

    /**
     * Checks if the data meets the request completion criteria.
     *
     * @param request
     *            The request
     * @param event
     *            The data to verify
     * @param nbRead
     *            The number of events read so far
     * @return true if completion criteria is met
     */
    public boolean isCompleted(ITmfEventRequest request, ITmfEvent event, int nbRead) {
        boolean requestCompleted = isCompleted2(request, nbRead);
        if (!requestCompleted) {
            ITmfTimestamp endTime = request.getRange().getEndTime();
            return event.getTimestamp().compareTo(endTime, false) > 0;
        }
        return requestCompleted;
    }

    private static boolean isCompleted2(ITmfEventRequest request,int nbRead) {
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
