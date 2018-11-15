/*******************************************************************************
 * Copyright (c) 2009, 2017 Ericsson
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

package org.eclipse.tracecompass.tmf.core.component;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.TmfCoreTracer;
import org.eclipse.tracecompass.internal.tmf.core.component.TmfEventThread;
import org.eclipse.tracecompass.internal.tmf.core.component.TmfProviderManager;
import org.eclipse.tracecompass.internal.tmf.core.request.TmfCoalescedEventRequest;
import org.eclipse.tracecompass.internal.tmf.core.request.TmfRequestExecutor;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.filter.ITmfFilter;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest.ExecutionType;
import org.eclipse.tracecompass.tmf.core.signal.TmfEndSynchSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfStartSynchSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;

/**
 * An abstract base class that implements ITmfEventProvider.
 * <p>
 * This abstract class implements the housekeeping methods to register/
 * de-register the event provider and to handle generically the event requests.
 * </p>
 *
 * @author Francois Chouinard
 */
public abstract class TmfEventProvider extends TmfComponent implements ITmfEventProvider, ITmfFilter {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /** Default amount of events per request "chunk" */
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

    /** Current timer task */
    @NonNull private TimerTask fCurrentTask = new TimerTask() { @Override public void run() {} };

    private boolean fIsTimerEnabled;

    /**
     * The parent event provider.
     */
    private TmfEventProvider fParent = null;
    /**
     * The list if children event provider.
     */
    private final List<TmfEventProvider> fChildren = Collections.synchronizedList(new ArrayList<TmfEventProvider>());

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfEventProvider() {
        super();
        setTimerEnabled(true);
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

        synchronized (fChildren) {
            for (TmfEventProvider child : fChildren) {
                child.dispose();
            }
        }
        clearPendingRequests();
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @since 2.0
     */
    @Override
    public Class<? extends ITmfEvent> getEventType() {
        return fType;
    }

    // ------------------------------------------------------------------------
    // ITmfRequestHandler
    // ------------------------------------------------------------------------

    @Override
    public void sendRequest(final ITmfEventRequest request) {
        synchronized (fLock) {

            if (TmfCoreTracer.isRequestTraced()) {
                TmfCoreTracer.traceRequest(request.getRequestId(), "SENT to provider " + getName()); //$NON-NLS-1$
            }

            if (request.getProviderFilter() == null) {
                request.setProviderFilter(this);
            }

            if (sendWithParent(request)) {
                return;
            }

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

            coalesceEventRequest(request);

            if (fIsTimerEnabled) {
                fCurrentTask.cancel();
                fCurrentTask = new TimerTask() {
                    @Override
                    public void run() {
                        synchronized (fLock) {
                            fireRequest(true);
                        }
                    }
                };
                fTimer.schedule(fCurrentTask, DELAY);
            }
        }
    }

    private void fireRequest(boolean isTimeout) {
        synchronized (fLock) {
            if (fRequestPendingCounter > 0) {
                return;
            }

            if (!fPendingCoalescedRequests.isEmpty()) {
                Iterator<TmfCoalescedEventRequest> iter = fPendingCoalescedRequests.iterator();
                while (iter.hasNext()) {
                    ExecutionType type = (isTimeout ? ExecutionType.BACKGROUND : ExecutionType.FOREGROUND);
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
                    fireRequest(false);
                    fireRequest(true);
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
    protected void newCoalescedEventRequest(ITmfEventRequest request) {
        synchronized (fLock) {
            TmfCoalescedEventRequest coalescedRequest = new TmfCoalescedEventRequest(
                    request.getDataType(),
                    request.getRange(),
                    request.getIndex(),
                    request.getNbRequested(),
                    request.getExecType(),
                    request.getDependencyLevel());
            coalescedRequest.addRequest(request);
            coalescedRequest.setProviderFilter(this);
            if (TmfCoreTracer.isRequestTraced()) {
                TmfCoreTracer.traceRequest(request.getRequestId(), "COALESCED with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
                TmfCoreTracer.traceRequest(coalescedRequest.getRequestId(), "now contains " + coalescedRequest.getSubRequestIds()); //$NON-NLS-1$
            }
            coalesceChildrenRequests(coalescedRequest);
            fPendingCoalescedRequests.add(coalescedRequest);
        }
    }

    /**
     * Add an existing requests to the list of coalesced ones
     *
     * @param request
     *            The request to add to the list
     */
    protected void coalesceEventRequest(ITmfEventRequest request) {
        synchronized (fLock) {
            for (TmfCoalescedEventRequest coalescedRequest : getPendingRequests()) {
                if (coalescedRequest.isCompatible(request)) {
                    coalescedRequest.addRequest(request);
                    if (TmfCoreTracer.isRequestTraced()) {
                        TmfCoreTracer.traceRequest(request.getRequestId(), "COALESCED with " + coalescedRequest.getRequestId()); //$NON-NLS-1$
                        TmfCoreTracer.traceRequest(coalescedRequest.getRequestId(), "now contains " + coalescedRequest.getSubRequestIds()); //$NON-NLS-1$
                    }
                    coalesceChildrenRequests(coalescedRequest);
                    return;
                }
            }
            newCoalescedEventRequest(request);
        }
    }

    /*
     * Sends a request with the parent if compatible.
     */
    private boolean sendWithParent(final ITmfEventRequest request) {
        ITmfEventProvider parent = getParent();
        if (parent instanceof TmfEventProvider) {
            return ((TmfEventProvider) parent).sendIfCompatible(request);
        }
        return false;
    }

    /*
     * Sends a request if compatible with a pending coalesced request.
     */
    private boolean sendIfCompatible(ITmfEventRequest request) {
        synchronized (fLock) {
            for (TmfCoalescedEventRequest coalescedRequest : getPendingRequests()) {
                if (coalescedRequest.isCompatible(request)) {
                    // Send so it can be coalesced with the parent(s)
                    sendRequest(request);
                    return true;
                }
            }
        }
        return sendWithParent(request);
    }

    /*
     * Coalesces children requests with given request if compatible.
     */
    private void coalesceChildrenRequests(final TmfCoalescedEventRequest request) {
        synchronized (fChildren) {
            for (TmfEventProvider child : fChildren) {
                child.coalesceCompatibleRequests(request);
            }
        }
    }


    /*
     * Coalesces all pending requests that are compatible with coalesced request.
     */
    private void coalesceCompatibleRequests(TmfCoalescedEventRequest request) {
        Iterator<TmfCoalescedEventRequest> iter = getPendingRequests().iterator();
        while (iter.hasNext()) {
            TmfCoalescedEventRequest pendingRequest = iter.next();
            if (request.isCompatible(pendingRequest)) {
                request.addRequest(pendingRequest);
                if (TmfCoreTracer.isRequestTraced()) {
                    TmfCoreTracer.traceRequest(pendingRequest.getRequestId(), "COALESCED with " + request.getRequestId()); //$NON-NLS-1$
                    TmfCoreTracer.traceRequest(request.getRequestId(), "now contains " + request.getSubRequestIds()); //$NON-NLS-1$
                }
                iter.remove();
            }
        }
    }

    // ------------------------------------------------------------------------
    // Request processing
    // ------------------------------------------------------------------------

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
            TmfCoreTracer.traceRequest(request.getRequestId(), "QUEUED"); //$NON-NLS-1$
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
            return event.getTimestamp().compareTo(endTime) > 0;
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
     */
    protected boolean executorIsShutdown() {
        return fExecutor.isShutdown();
    }

    /**
     * @return the termination state
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
                fireRequest(false);
            }
        }
    }

    @Override
    public ITmfEventProvider getParent() {
        synchronized (fLock) {
            return fParent;
        }
    }

    @Override
    public void setParent(ITmfEventProvider parent) {
        if (!(parent instanceof TmfEventProvider)) {
            throw new IllegalArgumentException();
        }

        synchronized (fLock) {
            fParent = (TmfEventProvider) parent;
        }
    }

    @Override
    public List<ITmfEventProvider> getChildren() {
        synchronized (fChildren) {
            List<ITmfEventProvider> list = new ArrayList<>();
            list.addAll(fChildren);
            return list;
        }
    }

    @Override
    public <T extends ITmfEventProvider> List<T> getChildren(Class<T> clazz) {
       List<@NonNull T> list = new ArrayList<>();
       synchronized (fChildren) {
           for (TmfEventProvider child : fChildren) {
               if (clazz.isAssignableFrom(child.getClass())) {
                   list.add(checkNotNull(clazz.cast(child)));
               }
           }
       }
       return list;
    }

    @Override
    public ITmfEventProvider getChild(String name) {
        synchronized (fChildren) {
            for (TmfEventProvider child : fChildren) {
                if (child.getName().equals(name)) {
                    return child;
                }
            }
        }
        return null;
    }

    @Override
    public ITmfEventProvider getChild(int index) {
        return NonNullUtils.checkNotNull(fChildren.get(index));
    }

    @Override
    public void addChild(ITmfEventProvider child) {
        if (!(child instanceof TmfEventProvider)) {
            throw new IllegalArgumentException();
        }
        child.setParent(this);
        fChildren.add((TmfEventProvider)child);
    }

    @Override
    public int getNbChildren() {
        return fChildren.size();
    }

    /**
     * Returns true if an event was provided by this event provider or one of
     * its children event providers else false.
     *
     * @param event
     *            the event to check
     * @return <code>true</code> if event was provided by this provider or one
     *         of its children else <code>false</code>
     */
    @Override
    public boolean matches(ITmfEvent event) {
        if ((event.getTrace() == this)) {
            return true;
        }
        if (!fChildren.isEmpty()) {
            synchronized (fLock) {
                List <TmfEventProvider> children = getChildren(TmfEventProvider.class);
                for (TmfEventProvider child : children) {
                    if (child.matches(event)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Debug code (will also used in tests using reflection)
    // ------------------------------------------------------------------------

    /**
     * Gets a list of all pending requests. Debug code.
     *
     * @return a list of all pending requests
     */
    private List<TmfCoalescedEventRequest> getPendingRequests() {
        return fPendingCoalescedRequests;
    }

    /**
     * Cancels and clears all pending requests. Debug code.
     */
    private void  clearPendingRequests() {
        synchronized (fLock) {
            for (TmfCoalescedEventRequest request : fPendingCoalescedRequests) {
                request.cancel();
            }
            fPendingCoalescedRequests.clear();
        }
    }

    /**
     * Enables/disables the timer. Debug code.
     *
     * @param enabled
     *            the enable flag to set
     */
    private void setTimerEnabled(Boolean enabled) {
        fIsTimerEnabled = enabled;
    }
}
