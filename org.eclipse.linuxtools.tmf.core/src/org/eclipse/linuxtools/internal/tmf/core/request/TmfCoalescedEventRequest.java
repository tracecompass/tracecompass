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
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.request;

import org.eclipse.linuxtools.internal.tmf.core.TmfCoreTracer;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;

/**
 * The TMF coalesced event request
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfCoalescedEventRequest extends TmfCoalescedDataRequest implements ITmfEventRequest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private TmfTimeRange fRange; // The requested events time range

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
     *            {@link TmfDataRequest#ALL_DATA} to request all events.
     * @param priority
     *            The requested execution priority
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType,
            TmfTimeRange range,
            long index,
            int nbRequested,
            ExecutionType priority) {
        super(ITmfEvent.class, index, nbRequested, priority);
        fRange = range;

        if (TmfCoreTracer.isRequestTraced()) {
            String type = getClass().getName();
            type = type.substring(type.lastIndexOf('.') + 1);
            @SuppressWarnings("nls")
            String message = "CREATED "
                    + (getExecType() == ITmfDataRequest.ExecutionType.BACKGROUND ? "(BG)" : "(FG)")
                    + " Type=" + type + " Index=" + getIndex() + " NbReq=" + getNbRequested()
                    + " Range=" + getRange()
                    + " DataType=" + getDataType().getSimpleName();
            TmfCoreTracer.traceRequest(this, message);
        }
    }

    // ------------------------------------------------------------------------
    // Management
    // ------------------------------------------------------------------------

    @Override
    public void addRequest(ITmfDataRequest request) {
        super.addRequest(request);
        if (request instanceof ITmfEventRequest) {
            merge((ITmfEventRequest) request);
        }
    }

    @Override
    public boolean isCompatible(ITmfDataRequest request) {
        if (request instanceof ITmfEventRequest) {
            if (super.isCompatible(request)) {
                return overlaps((ITmfEventRequest) request);
            }
        }
        return false;
    }

    private boolean overlaps(ITmfEventRequest request) {
        ITmfTimestamp startTime = request.getRange().getStartTime();
        ITmfTimestamp endTime = request.getRange().getEndTime();
        return (startTime.compareTo(endTime) <= 0) && (fRange.getStartTime().compareTo(fRange.getEndTime()) <= 0);
    }

    private void merge(ITmfEventRequest request) {
        ITmfTimestamp startTime = request.getRange().getStartTime();
        ITmfTimestamp endTime = request.getRange().getEndTime();
        if (!fRange.contains(startTime) && fRange.getStartTime().compareTo(startTime) > 0) {
            fRange = new TmfTimeRange(startTime, fRange.getEndTime());
        }
        if (!fRange.contains(endTime) && fRange.getEndTime().compareTo(endTime) < 0) {
            fRange = new TmfTimeRange(fRange.getStartTime(), endTime);
        }
    }

    // ------------------------------------------------------------------------
    // ITmfDataRequest
    // ------------------------------------------------------------------------

    @Override
    public void handleData(ITmfEvent data) {
        super.handleData(data);
        long index = getIndex() + getNbRead() - 1;
        for (ITmfDataRequest request : fRequests) {
            if (data == null) {
                request.handleData(null);
            } else {
                long start = request.getIndex();
                long end = start + request.getNbRequested();
                if (request instanceof ITmfEventRequest) {
                    ITmfEventRequest req = (ITmfEventRequest) request;
                    if (!req.isCompleted() && index >= start && index < end) {
                        ITmfTimestamp ts = data.getTimestamp();
                        if (req.getRange().contains(ts)) {
                            if (req.getDataType().isInstance(data)) {
                                req.handleData(data);
                            }
                        }
                    }
                }
                else {
                    TmfDataRequest req = (TmfDataRequest) request;
                    if (!req.isCompleted() && index >= start && index < end) {
                        if (req.getDataType().isInstance(data)) {
                            req.handleData(data);
                        }
                    }
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // ITmfEventRequest
    // ------------------------------------------------------------------------

    @Override
    public TmfTimeRange getRange() {
        return fRange;
    }

    @Override
    public void setStartIndex(int index) {
        setIndex(index);
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    // All requests have a unique id
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other instanceof TmfCoalescedEventRequest) {
            TmfCoalescedEventRequest request = (TmfCoalescedEventRequest) other;
            return (request.getDataType() == getDataType()) &&
                    (request.getIndex() == getIndex()) &&
                    (request.getNbRequested() == getNbRequested()) &&
                    (request.getRange().equals(getRange()));
        }
        if (other instanceof TmfCoalescedDataRequest) {
            return super.equals(other);
        }
        return false;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfCoalescedEventRequest(" + getRequestId() + "," + getDataType().getSimpleName()
                + "," + getExecType() + "," + getRange() + "," + getIndex() + "," + getNbRequested()
                + ", " + fRequests.toString() + ")]";
    }

}
