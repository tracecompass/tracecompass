/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;

/**
 * A generic request implementation.
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 */
public abstract class TmfRequest implements ITmfRequest, ITmfFilter {

    // ------------------------------------------------------------------------
    // Static attributes
    // ------------------------------------------------------------------------

    /** The plug-in ID */
    private static final String PLUGIN_ID = Activator.PLUGIN_ID;

    /** The unique request number */
    private static int fRequestCounter = 0;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The request unique ID */
    private final int fRequestId;

    /** The request type */
    private final TmfRequestPriority fRequestPriority;

    /** The event filters */
    private Map<Class<? extends ITmfFilter>, ITmfFilter> fEventFilters = new HashMap<Class<? extends ITmfFilter>, ITmfFilter>();

    /** The event block filter - an optimization */
    private TmfBlockFilter fBlockFilter;

    /** The event range filter - an optimization */
    private TmfRangeFilter fRangeFilter;

    /** The number of events reads so far */
    private long fNbEventsRead;

    /** The request execution state */
    private TmfRequestState fRequestState;

    /** The request completion status */
    protected IStatus fRequestStatus;

    /** The parent request */
    private ITmfRequest fParentRequest;

    /** Latch used for request processing start */
    private final CountDownLatch startedLatch = new CountDownLatch(1);

    /** Latch used for request completion */
    private final CountDownLatch completedLatch = new CountDownLatch(1);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Constructor for all the events at normal priority
     */
    public TmfRequest() {
        this(TmfRequestPriority.NORMAL);
    }

    /**
     * Constructor for all the events at the specified  priority
     *
     * @param priority the request priority
     */
    public TmfRequest(TmfRequestPriority priority) {
        this(TmfTimeRange.ETERNITY, 0, ALL_EVENTS, priority);
    }

    /**
     * Constructor for all the events in a time range
     *
     * @param timeRange The time range
     */
    public TmfRequest(TmfTimeRange timeRange) {
        this(timeRange, 0, ALL_EVENTS, TmfRequestPriority.NORMAL);
    }

    /**
     * Constructor for all the events in a block
     *
     * @param startIndex  The start index
     * @param nbRequested The number of events requested
     */
    public TmfRequest(long startIndex, long nbRequested) {
        this(TmfTimeRange.ETERNITY, startIndex, nbRequested, TmfRequestPriority.NORMAL);
    }

    /**
     * Standard constructor
     *
     * @param timeRange   The time range
     * @param startIndex  The start index
     * @param nbRequested The number of events requested
     */
    public TmfRequest(TmfTimeRange timeRange, long startIndex, long nbRequested) {
        this(timeRange, startIndex, nbRequested, TmfRequestPriority.NORMAL);
    }

    /**
     * Full constructor
     *
     * @param timeRange   Time range of interest
     * @param nbRequested Number of events requested
     * @param startIndex  Index of the first event requested
     * @param priority    Request priority
     */
    public TmfRequest(TmfTimeRange timeRange, long startIndex, long nbRequested, TmfRequestPriority priority) {
        fRequestId = fRequestCounter++;
        fRequestPriority = priority;
        fBlockFilter = new TmfBlockFilter(startIndex, nbRequested);
        fRangeFilter = new TmfRangeFilter(timeRange);
        fEventFilters.put(TmfBlockFilter.class, fBlockFilter);
        fEventFilters.put(TmfRangeFilter.class, fRangeFilter);

        fRequestState = TmfRequestState.PENDING;
        fRequestStatus = null;
    }

    /**
     * Copy constructor
     *
     * @param other the other request
     */
    public TmfRequest(TmfRequest other) {
        this(null, 0, 0, other.fRequestPriority);
        setEventFilters(other.getEventFilters());
    }

    // ------------------------------------------------------------------------
    // Getters and Setters
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getRequestId()
     */
    @Override
    public int getRequestId() {
        return fRequestId;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getRequestPriority()
     */
    @Override
    public TmfRequestPriority getRequestPriority() {
        return fRequestPriority;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getTimeRange()
     */
    @Override
    public TmfTimeRange getTimeRange() {
        return fRangeFilter.getTimeRange();
    }

    /**
     * @param timeRange the new time range
     */
    protected void setTimeRange(final TmfTimeRange timeRange) {
        fRangeFilter = new TmfRangeFilter(timeRange);
        addEventFilter(fRangeFilter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getNbRequested()
     */
    @Override
    public long getNbRequested() {
        return fBlockFilter.getNbRequested();
    }

    /**
     * @param nbRequested the number of events requested
     */
    protected void setNbRequested(long nbRequested) {
        fBlockFilter = new TmfBlockFilter(fBlockFilter.getStartIndex(), nbRequested);
        fRangeFilter = new TmfRangeFilter(fRangeFilter.getTimeRange());
        addEventFilter(fBlockFilter);
        addEventFilter(fRangeFilter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getStartIndex()
     */
    @Override
    public long getStartIndex() {
        return fBlockFilter.getStartIndex();
    }

    /**
     * @param index the new start index
     */
    protected void setStartIndex(long index) {
        fBlockFilter = new TmfBlockFilter(index, fBlockFilter.getNbRequested());
        addEventFilter(fBlockFilter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getEventFilter(java.lang.Class)
     */
    @Override
    public ITmfFilter getEventFilter(Class<?> filterType) {
        return fEventFilters.get(filterType);
    }

    /**
     * @return the list of event filters
     */
    protected Collection<ITmfFilter> getEventFilters() {
        return fEventFilters.values();
    }

    /**
     * @param filters the new list of event filters
     */
    public void setEventFilters(Collection<ITmfFilter> filters) {
        clearFilters();
        for (ITmfFilter filter : filters) {
            addEventFilter(filter);
        }
    }

    private void clearFilters() {
        fEventFilters.clear();
        fBlockFilter = TmfBlockFilter.ALL_EVENTS;
        fRangeFilter = TmfRangeFilter.ALL_EVENTS;
        fEventFilters.put(TmfBlockFilter.class, fBlockFilter);
        fEventFilters.put(TmfRangeFilter.class, fRangeFilter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#addEventFilter(org.eclipse.linuxtools.tmf.core.filter.ITmfFilter)
     */
    @Override
    public void addEventFilter(ITmfFilter filter) {
        if (filter instanceof TmfBlockFilter) {
            fBlockFilter = (TmfBlockFilter) filter;
        } else if (filter instanceof TmfRangeFilter) {
            fRangeFilter = (TmfRangeFilter) filter;
        }
        fEventFilters.put(filter.getClass(), filter);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getNbEventRead()
     */
    @Override
    public long getNbEventsRead() {
        return fNbEventsRead;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#setParent(org.eclipse.linuxtools.tmf.core.request.ITmfRequest)
     */
    @Override
    public void setParent(ITmfRequest parent) {
        fParentRequest = parent;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getParent()
     */
    @Override
    public ITmfRequest getParent() {
        return fParentRequest;
    }

    // ------------------------------------------------------------------------
    // Request execution state
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getState()
     */
    @Override
    public TmfRequestState getState() {
        return fRequestState;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#isRunning()
     */
    @Override
    public boolean isRunning() {
        return fRequestState == TmfRequestState.RUNNING;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#isCompleted()
     */
    @Override
    public boolean isCompleted() {
        return fRequestState == TmfRequestState.COMPLETED;
    }

    // ------------------------------------------------------------------------
    // Request completion status
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#getStatus()
     */
    @Override
    public IStatus getStatus() {
        return fRequestStatus;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#isOK()
     */
    @Override
    public boolean isOK() {
        return fRequestStatus != null && fRequestStatus.getSeverity() == IStatus.OK;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#isFailed()
     */
    @Override
    public boolean isFailed() {
        return fRequestStatus != null && fRequestStatus.getSeverity() == IStatus.ERROR;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#isCancelled()
     */
    @Override
    public boolean isCancelled() {
        return fRequestStatus != null && fRequestStatus.getSeverity() == IStatus.CANCEL;
    }

    // ------------------------------------------------------------------------
    // Request operations
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#start()
     */
    @Override
    public void start() {
        synchronized (this) {
            fRequestState  = TmfRequestState.RUNNING;
        }
        handleStarted();
        startedLatch.countDown();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#done()
     */
    @Override
    public void done() {
        synchronized (this) {
            if (fRequestState != TmfRequestState.COMPLETED) {
                fRequestState = TmfRequestState.COMPLETED;
                if (fRequestStatus == null) {
                    fRequestStatus = new Status(IStatus.OK, PLUGIN_ID, "OK"); //$NON-NLS-1$
                }
            } else {
                return;
            }
        }
        try {
            handleCompleted();
        } finally {
            completedLatch.countDown();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#fail()
     */
    @Override
    public void fail() {
        synchronized (this) {
            if (fRequestStatus == null) {
                fRequestStatus = new Status(IStatus.ERROR, PLUGIN_ID, "FAILED"); //$NON-NLS-1$
            }
        }
        done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#cancel()
     */
    @Override
    public void cancel() {
        synchronized (this) {
            if (fRequestStatus == null) {
                fRequestStatus = new Status(IStatus.CANCEL, PLUGIN_ID, "CANCEL"); //$NON-NLS-1$
            }
        }
        done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#waitForStart()
     */
    @Override
    public void waitForStart() throws InterruptedException {
        while (!isRunning() && !isCompleted()) {
            startedLatch.await();
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#waitForCompletion()
     */
    @Override
    public void waitForCompletion() throws InterruptedException {
        while (!isCompleted()) {
            completedLatch.await();
        }
    }

    // ------------------------------------------------------------------------
    // Request processing hooks
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#handleStarted()
     */
    @Override
    public void handleStarted() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "STARTED"); //$NON-NLS-1$
        }
    }

    /**
     * Handle incoming event one at a time i.e. this method is invoked
     * for every data item obtained by the request.
     *
     * @param event a piece of data
     */
    @Override
    public synchronized void handleEvent(ITmfEvent event) {
        if (event != null) {
            fNbEventsRead++;
        }
    }

    /**
     * Handle the completion of the request. It is called when there is no
     * more data available either because:
     * - the request completed normally
     * - the request failed
     * - the request was canceled
     *
     * As a convenience, handleXXXX methods are provided. They are meant to be
     * overridden by the application if it needs to handle these conditions.
     */
    @Override
    public synchronized void handleCompleted() {
        if (isFailed()) {
            handleFailure();
        } else if (isCancelled()) {
            handleCancel();
        } else {
            handleSuccess();
        }
        notifyParent(this);
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "COMPLETED (" + fNbEventsRead + " events read)"); //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#notifyParent(org.eclipse.linuxtools.tmf.core.request.ITmfRequest)
     */
    @Override
    public void notifyParent(ITmfRequest child) {
        if (fParentRequest != null) {
            fParentRequest.notifyParent(this);
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#handleSuccess()
     */
    @Override
    public void handleSuccess() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "SUCCEEDED"); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#handleFailure()
     */
    @Override
    public void handleFailure() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "FAILED"); //$NON-NLS-1$
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#handleCancel()
     */
    @Override
    public void handleCancel() {
        if (TmfCoreTracer.isRequestTraced()) {
            TmfCoreTracer.traceRequest(this, "CANCELLED"); //$NON-NLS-1$
        }
    }

    // ------------------------------------------------------------------------
    // ITmfFilter
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.filter.ITmfFilter#matches(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public boolean matches(ITmfEvent event) {
        for (ITmfFilter filter : fEventFilters.values()) {
            if (!filter.matches(event)) {
                return false;
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fRequestId;
        result = prime * result + ((fRequestPriority == null) ? 0 : fRequestPriority.hashCode());
        result = prime * result + ((fEventFilters == null) ? 0 : fEventFilters.hashCode());
        result = prime * result + ((fBlockFilter == null) ? 0 : fBlockFilter.hashCode());
        result = prime * result + ((fRangeFilter == null) ? 0 : fRangeFilter.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfRequest)) {
            return false;
        }
        TmfRequest other = (TmfRequest) obj;
        if (fRequestPriority != other.fRequestPriority) {
            return false;
        }
        // Check that our filters match other's
        for (ITmfFilter filter : fEventFilters.values()) {
            ITmfFilter filter2 = other.getEventFilter(filter.getClass());
            if (filter2 == null) {
                return false;
            }
            if (!filter.equals(filter2)) {
                return false;
            }
        }
        // Check that others' filters match ours
        for (ITmfFilter filter : other.fEventFilters.values()) {
            ITmfFilter filter2 = getEventFilter(filter.getClass());
            if (filter2 == null) {
                return false;
            }
            if (!filter.equals(filter2)) {
                return false;
            }
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfRequest [fRequestId=" + fRequestId + "]";
    }

}
