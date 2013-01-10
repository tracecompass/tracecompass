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

package org.eclipse.linuxtools.internal.tmf.core.request;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.ITmfRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfBlockFilter;
import org.eclipse.linuxtools.tmf.core.request.TmfRangeFilter;
import org.eclipse.linuxtools.tmf.core.request.TmfRequest;

/**
 * The TMF coalesced request
 * <p>
 * Since different TMF components can issue simultaneous requests to their event
 * provider (e.g. as the result of a user action), it is desirable to coalesce
 * these requests when possible in order to reduce costly I/O with the back-end.
 * <p>
 * The TmfCoalescedRequest acts as a request aggregator. It bundles compatible
 * requests and is the one dispatched to the event provider. As each event is
 * read in sequence, it re-distributes them to its sub-requests as appropriate.
 * <p>
 * The sub-request compatibility is evaluated based on the following criteria:
 * <ul>
 * <li>Request type (pre-emptible or not)
 * <li>Block ranges (start index + nb requested) overlap or are contiguous
 * <li>Time ranges overlap or are contiguous
 * </ul>
 *
 * @author Francois Chouinard
 * @version 1.0
 */
public class TmfCoalescedRequest extends TmfRequest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

   /** The list of coalesced requests */
   private final List<ITmfRequest> fSubRequests = new ArrayList<ITmfRequest>();

    /** The list of coalesced requests */
    private int fNbSubRequests;

   // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfCoalescedRequest() {
        this(TmfRequestPriority.NORMAL);
    }

    /**
     * Basic constructor
     * @param priority the request priority
     */
    public TmfCoalescedRequest(TmfRequestPriority priority) {
       super(priority);
    }

    /**
     * Create a coalesced request based on the provided request
     *
     * @param request the base request
     */
    public TmfCoalescedRequest(ITmfRequest request) {
        super(request != null ? request.getRequestPriority() : null);

        // Initialize sub-requests list with the request
        if (request != null) {
            fSubRequests.add(request);
            fNbSubRequests++;
            request.setParent(this);

            // Collect the filter values of interestIndex
            TmfBlockFilter blockFilter = (TmfBlockFilter) request.getEventFilter(TmfBlockFilter.class);
            long startIndex = blockFilter.getStartIndex();
            long nbRequested = blockFilter.getNbRequested();
            addEventFilter(new TmfBlockFilter(startIndex, nbRequested));

            TmfRangeFilter rangeFilter = (TmfRangeFilter) request.getEventFilter(TmfRangeFilter.class);
            TmfTimeRange timeRange = rangeFilter.getTimeRange();
            addEventFilter(new TmfRangeFilter(timeRange));
        }
    }

    // ------------------------------------------------------------------------
    // Request execution state
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#isCompleted()
     */
    @Override
    public synchronized boolean isCompleted() {
        for (ITmfRequest request : fSubRequests) {
            if (!request.isCompleted()) {
                return false;
            }
        }
        return super.isCompleted();
    }

    // ------------------------------------------------------------------------
    // Request completion status
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#isCancelled()
     */
    @Override
    public synchronized boolean isCancelled() {
        for (ITmfRequest request : fSubRequests) {
            if (!request.isCancelled()) {
                return false;
            }
        }
        return super.isCancelled();
    }

    // ------------------------------------------------------------------------
    // Request operations
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#start()
     */
    @Override
    public void start() {
        for (ITmfRequest request : fSubRequests) {
            if (!request.isCompleted()) {
                request.start();
            }
        }
        super.start();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#done()
     */
    @Override
    public void done() {
        if (fRequestStatus == null) {
            fRequestStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.OK, "OK", null); //$NON-NLS-1$
            for (ITmfRequest request : fSubRequests) {
                if (!request.isCompleted()) {
                    request.done();
                }
                ((MultiStatus) fRequestStatus).add(request.getStatus());
            }
        }
        super.done();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#fail()
     */
    @Override
    public void fail() {
        fRequestStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.ERROR, "FAIL", null); //$NON-NLS-1$
        for (ITmfRequest request : fSubRequests) {
            if (!request.isCompleted()) {
                request.fail();
                ((MultiStatus) fRequestStatus).add(request.getStatus());
            }
        }
        super.fail();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#cancel()
     */
    @Override
    public void cancel() {
        fRequestStatus = new MultiStatus(Activator.PLUGIN_ID, IStatus.CANCEL, "CANCEL", null); //$NON-NLS-1$
        for (ITmfRequest request : fSubRequests) {
            if (!request.isCompleted()) {
                request.cancel();
            }
            ((MultiStatus) fRequestStatus).add(request.getStatus());
        }
        super.cancel();
    }

    // ------------------------------------------------------------------------
    // Request processing hooks
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#handleEvent(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public synchronized void handleEvent(ITmfEvent event) {
        super.handleEvent(event);
        for (ITmfRequest request : fSubRequests) {
            if (!request.isCompleted() && request.matches(event)) {
                request.handleEvent(event);
            }
        }
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.ITmfRequest#notifyParent(org.eclipse.linuxtools.tmf.core.request.ITmfRequest)
     */
    @Override
    public synchronized void notifyParent(ITmfRequest child) {
        if (--fNbSubRequests <= 0) {
            done();
            super.notifyParent(this);
        }
    }

    // ------------------------------------------------------------------------
    // Management
    // ------------------------------------------------------------------------

    /**
     * Add a request to this one.
     *
     * @param request The request to add
     * @return true if the request was successfully coalesced, false otherwise
     */
    public synchronized boolean addRequest(ITmfRequest request) {
        if (isCompatible(request)) {
            fSubRequests.add(request);
            fNbSubRequests++;
            request.setParent(this);
            adjustFilters(request);
            return true;
        }
        return false;
    }

    /**
     * @return The list of IDs of the sub-requests
     */
    @SuppressWarnings("nls")
    public String getSubRequestIds() {
        StringBuffer result = new StringBuffer("[");
        for (int i = 0; i < fSubRequests.size(); i++) {
            if (i != 0) {
                result.append(",");
            }
            result.append(fSubRequests.get(i).getRequestId());
        }
        result.append("]");
        return result.toString();
    }

    // ------------------------------------------------------------------------
    // Compatibility checks
    // ------------------------------------------------------------------------

    /**
     * Check if a request is compatible i.e. can be coalesced with the
     * other sub-requests.
     * Compatibility is evaluated on the following criteria:
     * - Request type (pre-emptible or not)
     * - Block parameters (start index + requested)
     * - Time range
     *
     * @param request The request to evaluate
     * @return true if the request is compatible, false otherwise
     */
    public boolean isCompatible(ITmfRequest request) {
        if (request.getRequestPriority() != getRequestPriority()) {
            return false;
        }
        // Check the block range
        TmfBlockFilter blockFilter = (TmfBlockFilter) request.getEventFilter(TmfBlockFilter.class);
        if (!isCompatible(blockFilter)) {
            return false;
        }
        // Check the time range
        TmfRangeFilter rangeFilter = (TmfRangeFilter) request.getEventFilter(TmfRangeFilter.class);
        if (!isCompatible(rangeFilter)) {
            return false;
        }
        return true;
    }

    /**
     * Check if the filter time range overlaps the coalesced request time range.
     * The test boils down to a verification of the intersection of the ranges.
     *
     * @param filter the time range filter to test
     * @return true if the time range is compatible; false otherwise
     */
    private boolean isCompatible(TmfRangeFilter filter) {
        TmfRangeFilter rangeFilter = (TmfRangeFilter) getEventFilter(TmfRangeFilter.class);
        return rangeFilter.getTimeRange().getIntersection(filter.getTimeRange()) != null;
    }

    /**
     * Check if the filter block overlaps the coalesced request block.
     * The test boils down to a verification that at least one of the block
     * boundaries falls within the other block boundaries.
     *
     * @param filter the block filter to test
     * @return true if the time range is compatible; false otherwise
     */
    private boolean isCompatible(TmfBlockFilter filter) {
        TmfBlockFilter blockFilter = (TmfBlockFilter) getEventFilter(TmfBlockFilter.class);
        return filter.getStartIndex() - 1 <= (blockFilter.getEndIndex()) &&
               filter.getEndIndex() - 1   >= (blockFilter.getStartIndex());
    }

    // ------------------------------------------------------------------------
    // Filter adjustments
    // ------------------------------------------------------------------------

    /**
     * Adjust the coalesced request filters based on a given request
     *
     * @param request the request to consider
     */
    private void adjustFilters(ITmfRequest request) {
        TmfBlockFilter blockFilter = (TmfBlockFilter) request.getEventFilter(TmfBlockFilter.class);
        adjustFilter(blockFilter);
        TmfRangeFilter rangeFilter = (TmfRangeFilter) request.getEventFilter(TmfRangeFilter.class);
        adjustFilter(rangeFilter);
    }

    /**
     * @param filter the block filter to adjust
     */
    private void adjustFilter(TmfBlockFilter filter) {
        TmfBlockFilter blockFilter = (TmfBlockFilter) getEventFilter(TmfBlockFilter.class);
        long startIndex = Math.min(blockFilter.getStartIndex(), filter.getStartIndex());
        long endIndex   = Math.max(blockFilter.getEndIndex(), filter.getEndIndex());
        long nbRequested = endIndex - startIndex;
        addEventFilter(new TmfBlockFilter(startIndex, nbRequested));
    }

    /**
     * @param filter the time range filter to adjust
     */
    private void adjustFilter(TmfRangeFilter filter) {
        TmfRangeFilter rangeFilter = (TmfRangeFilter) getEventFilter(TmfRangeFilter.class);
        TmfTimeRange timeRange = rangeFilter.getTimeRange().getUnion(filter.getTimeRange());
        addEventFilter(new TmfRangeFilter(timeRange));
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((fSubRequests == null) ? 0 : fSubRequests.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!super.equals(obj)) {
            return false;
        }
        if (!(obj instanceof TmfCoalescedRequest)) {
            return false;
        }
        TmfCoalescedRequest other = (TmfCoalescedRequest) obj;
        if (fSubRequests == null) {
            if (other.fSubRequests != null) {
                return false;
            }
        } else if (!fSubRequests.equals(other.fSubRequests)) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfCoalescedRequest [fSubRequests=" + fSubRequests + "]";
    }

}
