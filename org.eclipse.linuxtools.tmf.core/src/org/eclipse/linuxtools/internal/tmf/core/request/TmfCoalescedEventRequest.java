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

	private TmfTimeRange fRange;	// The requested events time range

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Request all the events of a given type (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType) {
        this(dataType, TmfTimeRange.ETERNITY, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param priority the requested execution priority
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType, ExecutionType priority) {
        this(dataType, TmfTimeRange.ETERNITY, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request all the events of a given type for the given time range (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param range the time range of the requested events
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType, TmfTimeRange range) {
        this(dataType, range, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type for the given time range (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param priority the requested execution priority
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType, TmfTimeRange range, ExecutionType priority) {
        this(dataType, range, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request 'n' events of a given type from the given time range (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param nbRequested the number of events requested
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType, TmfTimeRange range, int nbRequested) {
        this(dataType, range, nbRequested, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request 'n' events of a given type for the given time range (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param nbRequested the number of events requested
     * @param priority the requested execution priority
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType, TmfTimeRange range, int nbRequested, ExecutionType priority) {
        this(dataType, range, nbRequested, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request 'n' events of a given type for the given time range (high priority).
     * Events are returned in blocks of the given size.
     *
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType, TmfTimeRange range, int nbRequested, int blockSize) {
        this(dataType, range, 0, nbRequested, blockSize, ExecutionType.FOREGROUND);
    }

    /**
     * Request 'n' events of a given type for the given time range (given priority).
     * Events are returned in blocks of the given size.
     *
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     * @param priority the requested execution priority
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType, TmfTimeRange range, int nbRequested, int blockSize, ExecutionType priority) {
        this(dataType, range, 0, nbRequested, blockSize, priority);
    }

    /**
     * Request 'n' events of a given type for the given time range (given priority).
     * Events are returned in blocks of the given size.
     *
     * @param dataType the requested data type
     * @param range the time range of the requested events
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     * @param priority the requested execution priority
     */
    public TmfCoalescedEventRequest(Class<? extends ITmfEvent> dataType, TmfTimeRange range, long index, int nbRequested, int blockSize, ExecutionType priority) {
        super(ITmfEvent.class, index, nbRequested, blockSize, priority);
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
        ITmfTimestamp endTime   = request.getRange().getEndTime();
        return (startTime.compareTo(endTime) <= 0) && (fRange.getStartTime().compareTo(fRange.getEndTime()) <= 0);
    }

    private void merge(ITmfEventRequest request) {
        ITmfTimestamp startTime = request.getRange().getStartTime();
        ITmfTimestamp endTime   = request.getRange().getEndTime();
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
       		return 	(request.getDataType()    == getDataType()) &&
       				(request.getIndex()       == getIndex())    &&
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
                + "," + getBlockSize() + ", " + fRequests.toString() + ")]";
    }

}
