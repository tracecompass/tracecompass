/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
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
 *   Bernd Hufmann - Add timer based coalescing for background requests
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.component;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
 * @since 3.0
 */
public abstract class TmfEventProvider extends TmfComponent implements ITmfEventProvider {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** Default amount of events per request "chunk"
     * @since 3.0 */
    public static final int DEFAULT_BLOCK_SIZE = 50000;

    /** Delay for coalescing background requests (in milli-seconds) */
    private static final long DELAY = 1000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** List of coalesced requests */
    private final List<TmfCoalescedEventRequest> fPendingCoalescedRequests = new LinkedList<>();

    /** The type of event handled by this provider */
    private Class<? extends ITmfEvent> fType;

    private final TmfRequestExecutor fExecutor;

    private final Object fLock = new Object();

    private int fSignalDepth = 0;

    private int fRequestPendingCounter = 0;

    private Timer fTimer;

    private boolean fIsTimeout = false;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEventProvider() {
        super();
        fExecutor = new TmfRequestExecutor();
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
        this();
        init(name, type);
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
        fExecutor.init();

        fSignalDepth = 0;

        synchronized (fLock) {
             fTimer = new Timer();
        }

        TmfProviderManager.register(fType, this);
    }

    @Override
    public void dispose() {
        TmfProviderManager.deregister(fType, this);
        fExecutor.stop();
        synchronized (fLock) {
            if (fTimer != null) {
                fTimer.cancel();
            }
            fTimer = null;
        }
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get the event type this provider handles
     *
     * @return The type of ITmfEvent
     */
    public Class<? extends ITmfEvent> getType() {
        return fType;
    }

    // ------------------------------------------------------------------------
    // ITmfRequestHandler
    // ------------------------------------------------------------------------

    /**
     * @since 3.0
     */
    @Override
    public void sendRequest(final ITmfEventRequest request) {
        synchronized (fLock) {
            if (request.getExecType() == ExecutionType.FOREGROUND) {
                if ((fSignalDepth > 0) || (fRequestPendingCounter > 0)) {
                    coalesceEventRequest(request);
                } else {
                    queueRequest(request);
                }
                return;
            }

            /*
             * Dispatch request in case timer is not running.
             */
            if (fTimer == null) {
                queueRequest(request);
                return;
            }

            /*
             *  For the first background request in the request pending queue
             *  a timer will be started to allow other background requests to
             *  coalesce.
             */
            boolean startTimer = (getNbPendingBackgroundRequests() == 0);
            coalesceEventRequest(request);
            if (startTimer) {
                TimerTask task = new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (fLock) {
                            fIsTimeout = true;
                            fireRequest();
                        }
                    }
                };
                fTimer.schedule(task, DELAY);
            }
        }
    }

    private void fireRequest() {
        synchronized (fLock) {
            if (fRequestPendingCounter > 0) {
                return;
            }

            if (fPendingCoalescedRequests.size() > 0) {
                Iterator<TmfCoalescedEventRequest> iter = fPendingCoalescedRequests.iterator();
                while (iter.hasNext()) {
                    ExecutionType type = (fIsTimeout ? ExecutionType.BACKGROUND : ExecutionType.FOREGROUND);
                    ITmfEventRequest request = iter.next();
                    if (type == request.getExecType()) {
                        queueRequest(request);
                        iter.remove();
                    }
                }
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
                fRequestPendingCounter++;
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
     * @since 3.0
     */
    protected void newCoalescedEventRequest(ITmfEventRequest request) {
        synchronized (fLock) {
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
    }

    /**
     * Add an existing requests to the list of coalesced ones
     *
     * @param request
     *            The request to add to the list
     * @since 3.0
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

    /**
     * Gets the number of background requests in pending queue.
     *
     * @return the number of background requests in pending queue
     */
    private int getNbPendingBackgroundRequests() {
        int nbBackgroundRequests = 0;
        synchronized (fLock) {
            for (ITmfEventRequest request : fPendingCoalescedRequests) {
                if (request.getExecType() == ExecutionType.BACKGROUND) {
                    nbBackgroundRequests++;
                }
            }
        }
        return nbBackgroundRequests;
    }

    // ------------------------------------------------------------------------
    // Request processing
    // ------------------------------------------------------------------------

    /**
     * Queue a request.
     *
     * @param request
     *            The data request
     * @since 3.0
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
     * Initialize the provider based on the request. The context is provider
     * specific and will be updated by getNext().
     *
     * @param request
     *            The request
     * @return An application specific context; null if request can't be
     *         serviced
     * @since 3.0
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
     * @since 3.0
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
                fIsTimeout = false;
                fireRequest();
            }
        }
    }

}
