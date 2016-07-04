/*******************************************************************************
 * Copyright (c) 2009, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Alexandre Montplaisir - Merge with TmfCoalescedDataRequest
 *   Bernd Hufmann - Updated dispatching of events and added requests cache
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.request;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.tracecompass.internal.tmf.core.TmfCoreTracer;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.request.ITmfEventRequest;
import org.eclipse.tracecompass.tmf.core.request.TmfEventRequest;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;

/**
 * The TMF coalesced event request
 *
 * @author Francois Chouinard
 */
public class TmfCoalescedEventRequest extends TmfEventRequest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The list of coalesced requests */
    private final List<ITmfEventRequest> fRequests = new ArrayList<>();

    /**
     * We do not use super.fRange, because in the case of coalesced requests,
     * the global range can be modified as sub-request are added.
     */
    private TmfTimeRange fRange;

    /**
     * The requests cache to avoid iterating over all requests for each event.
     */
    private Map<String, Set<ITmfEventRequest>> fRequestsCache = new HashMap<>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Request 'n' events of a given type for the given time range (given
     * priority). Events are returned in blocks of the given size.
     *
     * @param dataType
     *            The requested data type
     * @param range
     *            The range of the request. You can use
     *            {@link TmfTimeRange#ETERNITY} to request all events.
     * @param index
     *            The index of the first event to retrieve. Use '0' to start at
     *            the beginning.
     * @param nbRequested
     *            The number of events requested. You can use
     *            {@link TmfEventRequest#ALL_DATA} to request all events.
     * @param priority
     *            The requested execution priority
     * @param dependencyLevel
     *            The dependency level. Use 0 if no dependency with other
     *            requests.
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType,
            TmfTimeRange range,
            long index,
            int nbRequested,
            ExecutionType priority,
            int dependencyLevel) {
        super(ITmfEvent.class, null, index, nbRequested, priority, dependencyLevel);
        fRange = range;

        if (TmfCoreTracer.isRequestTraced()) {
            String type = getClass().getName();
            type = type.substring(type.lastIndexOf('.') + 1);
            @SuppressWarnings("nls")
            String message = "CREATED "
                    + (getExecType() == ITmfEventRequest.ExecutionType.BACKGROUND ? "(BG)" : "(FG)")
                    + " Type=" + type + " Index=" + getIndex() + " NbReq=" + getNbRequested()
                    + " Range=" + getRange()
                    + " DataType=" + getDataType().getSimpleName();
            TmfCoreTracer.traceRequest(getRequestId(), message);
        }
    }

    @Override
    public TmfTimeRange getRange() {
        return fRange;
    }

    // ------------------------------------------------------------------------
    // Management
    // ------------------------------------------------------------------------

    /**
     * Add a request to this one.
     *
     * @param request
     *            The request to add
     */
    public void addRequest(ITmfEventRequest request) {
        // If it is a coalesced request only add the sub-requests
        if (request instanceof TmfCoalescedEventRequest) {
            TmfCoalescedEventRequest otherRequest = (TmfCoalescedEventRequest)request;
            for (ITmfEventRequest subRequest : otherRequest.fRequests) {
                fRequests.add(subRequest);
                merge(subRequest);
            }
        } else {
            fRequests.add(request);
            merge(request);
        }
    }

    /**
     * Check if a request is compatible with the current coalesced one
     *
     * @param request
     *            The request to verify
     * @return If the request is compatible, true or false
     */
    public boolean isCompatible(ITmfEventRequest request) {
        if (request.getExecType() == getExecType() &&
                request.getDependencyLevel() == getDependencyLevel() &&
                ranksOverlap(request) &&
                timeRangesOverlap(request)) {
            return true;
        }
        return false;
    }

    private boolean ranksOverlap(ITmfEventRequest request) {
        long start = request.getIndex();
        long end = start + request.getNbRequested();

        // Return true if either the start or end index falls within
        // the coalesced request boundaries
        return (start <= (fIndex + fNbRequested + 1) && (end >= fIndex - 1));
    }

    private boolean timeRangesOverlap(ITmfEventRequest request) {
        ITmfTimestamp startTime = request.getRange().getStartTime();
        ITmfTimestamp endTime = request.getRange().getEndTime();
        return (startTime.compareTo(endTime) <= 0) &&
                (fRange.getStartTime().compareTo(fRange.getEndTime()) <= 0);
    }

    private void merge(ITmfEventRequest request) {
        long start = request.getIndex();
        long end = Math.min(start + request.getNbRequested(), ITmfEventRequest.ALL_DATA);

        if (start < fIndex) {
            if (fNbRequested != ITmfEventRequest.ALL_DATA) {
                fNbRequested += (fIndex - start);
            }
            fIndex = start;
        }
        if ((request.getNbRequested() == ITmfEventRequest.ALL_DATA) ||
                (fNbRequested == ITmfEventRequest.ALL_DATA)) {
            fNbRequested = ITmfEventRequest.ALL_DATA;
        } else {
            fNbRequested = (int) Math.max(end - fIndex, fNbRequested);
        }

        ITmfTimestamp startTime = request.getRange().getStartTime();
        ITmfTimestamp endTime = request.getRange().getEndTime();
        if (!fRange.contains(startTime) && fRange.getStartTime().compareTo(startTime) > 0) {
            fRange = new TmfTimeRange(startTime, fRange.getEndTime());
        }
        if (!fRange.contains(endTime) && fRange.getEndTime().compareTo(endTime) < 0) {
            fRange = new TmfTimeRange(fRange.getStartTime(), endTime);
        }
    }

    /**
     * @return The list of IDs of the sub-requests
     */
    @SuppressWarnings("nls")
    public String getSubRequestIds() {
        StringBuffer result = new StringBuffer("[");
        for (int i = 0; i < fRequests.size(); i++) {
            if (i != 0) {
                result.append(", ");
            }
            result.append(fRequests.get(i).getRequestId());
        }
        result.append("]");
        return result.toString();
    }

    // ------------------------------------------------------------------------
    // ITmfEventRequest
    // ------------------------------------------------------------------------

    @Override
    public void handleData(ITmfEvent data) {
        super.handleData(data);

        long index = getIndex() + getNbRead() - 1;

        String traceName = data.getTrace().getName();
        Set<ITmfEventRequest> requests = fRequestsCache.get(traceName);

        if (requests == null) {
            // Populate requests cache
            requests = new HashSet<>();
            for (ITmfEventRequest myRequest : fRequests) {
                if (myRequest.getProviderFilter().matches(data)) {
                    requests.add(myRequest);
                }
            }
            fRequestsCache.put(traceName, requests);
        }

        // dispatch event to relevant requests
        for (ITmfEventRequest request : requests) {
            long start = request.getIndex();
            if (!request.isCompleted() && index >= start && request.getNbRead() < request.getNbRequested()) {
                ITmfTimestamp ts = data.getTimestamp();
                if (request.getRange().contains(ts)) {
                    if (request.getDataType().isInstance(data)) {
                        request.handleData(data);
                    }
                }
            }
        }
    }

    @Override
    public synchronized void start() {
        for (ITmfEventRequest request : fRequests) {
            if (!request.isCompleted()) {
                request.start();
            }
        }
        super.start();
    }

    @Override
    public synchronized void done() {
        for (ITmfEventRequest request : fRequests) {
            if (!request.isCompleted()) {
                request.done();
            }
        }
        super.done();
    }

    @Override
    public void fail(Exception e) {
        for (ITmfEventRequest request : fRequests) {
            request.fail(e);
        }
        super.fail(e);
    }

    @Override
    public void cancel() {
        for (ITmfEventRequest request : fRequests) {
            if (!request.isCompleted()) {
                request.cancel();
            }
        }
        super.cancel();
    }

    @Override
    public synchronized boolean isCompleted() {
        // Firstly, check if coalescing request is completed
        if (super.isCompleted()) {
            return true;
        }

        // Secondly, check if all sub-requests are finished
        if (fRequests.size() > 0) {
            // If all sub requests are completed the coalesced request is
            // treated as completed, too.
            for (ITmfEventRequest request : fRequests) {
                if (!request.isCompleted()) {
                    return false;
                }
            }
            return true;
        }

        // Coalescing request is not finished if there are no sub-requests
        return false;
    }

    @Override
    public synchronized boolean isCancelled() {
        // Firstly, check if coalescing request is canceled
        if (super.isCancelled()) {
            return true;
        }

        // Secondly, check if all sub-requests are canceled
        if (fRequests.size() > 0) {
            // If all sub requests are canceled the coalesced request is
            // treated as completed, too.
            for (ITmfEventRequest request : fRequests) {
                if (!request.isCancelled()) {
                    return false;
                }
            }
            return true;
        }

        // Coalescing request is not canceled if there are no sub-requests
        return false;

    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfCoalescedEventRequest(" + getRequestId() + "," + getDataType().getSimpleName()
                + "," + getExecType() + "," + getRange() + "," + getIndex() + "," + getNbRequested()
                + ", " + fRequests.toString() + ")]";
    }

}
