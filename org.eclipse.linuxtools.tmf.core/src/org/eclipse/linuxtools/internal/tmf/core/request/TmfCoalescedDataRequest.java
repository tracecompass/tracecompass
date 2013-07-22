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

import java.util.Vector;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfDataRequest;

/**
 * The TMF coalesced data request
 *
 * @version 1.0
 * @author Francois Chouinard
 */
public class TmfCoalescedDataRequest extends TmfDataRequest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	/**
	 * The list of coalesced requests
	 */
	protected Vector<ITmfDataRequest> fRequests = new Vector<ITmfDataRequest>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Request all the events of a given type (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     */
    public TmfCoalescedDataRequest(Class<? extends ITmfEvent> dataType) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param priority the requested execution priority
     */
    public TmfCoalescedDataRequest(Class<? extends ITmfEvent> dataType, ExecutionType priority) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request all the events of a given type from the given index (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     */
    public TmfCoalescedDataRequest(Class<? extends ITmfEvent> dataType, long index) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request all the events of a given type from the given index (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param priority the requested execution priority
     */
    public TmfCoalescedDataRequest(Class<? extends ITmfEvent> dataType, long index, ExecutionType priority) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request 'n' events of a given type from the given index (high priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     */
    public TmfCoalescedDataRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    /**
     * Request 'n' events of a given type from the given index (given priority)
     * Events are returned in blocks of the default size (DEFAULT_BLOCK_SIZE).
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param priority the requested execution priority
     */
    public TmfCoalescedDataRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, ExecutionType priority) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, priority);
    }

    /**
     * Request 'n' events of a given type from the given index (high priority).
     * Events are returned in blocks of the given size.
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     */
    public TmfCoalescedDataRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, int blockSize) {
        super(ITmfEvent.class, index, nbRequested, blockSize, ExecutionType.FOREGROUND);
    }

    /**
     * Request 'n' events of a given type from the given index (given priority).
     * Events are returned in blocks of the given size.
     *
     * @param dataType the requested data type
     * @param index the index of the first event to retrieve
     * @param nbRequested the number of events requested
     * @param blockSize the number of events per block
     * @param priority the requested execution priority
     */
    public TmfCoalescedDataRequest(Class<? extends ITmfEvent> dataType, long index, int nbRequested, int blockSize, ExecutionType priority) {
        super(ITmfEvent.class, index, nbRequested, blockSize, priority);
    }

    // ------------------------------------------------------------------------
    // Management
    // ------------------------------------------------------------------------

    /**
     * Add a request to this one.
     *
     * @param request The request to add
     */
    public void addRequest(ITmfDataRequest request) {
        fRequests.add(request);
        merge(request);
    }

    /**
     * Check if a request is compatible with the current coalesced one
     *
     * @param request
     *            The request to verify
     * @return If the request is compatible, true or false
     */
    public boolean isCompatible(ITmfDataRequest request) {
        if (request.getExecType() == getExecType()) {
            return overlaps(request);
        }
        return false;
    }

    private boolean overlaps(ITmfDataRequest request) {
        long start = request.getIndex();
        long end = start + request.getNbRequested();

        // Return true if either the start or end index falls within
        // the coalesced request boundaries
        return (start <= (fIndex + fNbRequested + 1) && (end >= fIndex - 1));
    }

    private void merge(ITmfDataRequest request) {
        long start = request.getIndex();
        long end = Math.min(start + request.getNbRequested(), TmfDataRequest.ALL_DATA);

        if (start < fIndex) {
            if (fNbRequested != TmfDataRequest.ALL_DATA) {
                fNbRequested += (fIndex - start);
            }
            fIndex = start;
        }
        if ((request.getNbRequested() == TmfDataRequest.ALL_DATA) ||
             (fNbRequested == TmfDataRequest.ALL_DATA))
        {
            fNbRequested = TmfDataRequest.ALL_DATA;
        } else {
            fNbRequested = (int) Math.max(end - fIndex, fNbRequested);
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
    // ITmfDataRequest
    // ------------------------------------------------------------------------

    @Override
	public void handleData(ITmfEvent data) {
		super.handleData(data);
    	// Don't call sub-requests handleData() unless this is a
		// TmfCoalescedDataRequest; extended classes should call
		// the sub-requests handleData().
		if (getClass() == TmfCoalescedDataRequest.class) {
		    long index = getIndex() + getNbRead() - 1;
	    	for (ITmfDataRequest request : fRequests) {
	    	    if (!request.isCompleted()) {
                    if (request.getDataType().isInstance(data)) {
                        long start = request.getIndex();
                        long end = start + request.getNbRequested();
                        if (index >= start && index < end) {
                            request.handleData(data);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void start() {
        for (ITmfDataRequest request : fRequests) {
            if (!request.isCompleted()) {
                request.start();
            }
        }
        super.start();
    }

	@Override
    public void done() {
    	for (ITmfDataRequest request : fRequests) {
    	    if (!request.isCompleted()) {
    	        request.done();
    	    }
    	}
    	super.done();
    }

    @Override
    public void fail() {
    	for (ITmfDataRequest request : fRequests) {
    		request.fail();
    	}
    	super.fail();
    }

    @Override
    public void cancel() {
    	for (ITmfDataRequest request : fRequests) {
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
            for (ITmfDataRequest request : fRequests) {
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
            for (ITmfDataRequest request : fRequests) {
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
    // All requests have a unique id
    public int hashCode() {
    	return super.hashCode();
    }

    @Override
    public boolean equals(Object other) {
    	if (other instanceof TmfCoalescedDataRequest) {
    		TmfCoalescedDataRequest request = (TmfCoalescedDataRequest) other;
       		return 	(request.getDataType()    == getDataType())   &&
       				(request.getIndex()       == getIndex())      &&
       				(request.getNbRequested() == getNbRequested() &&
      				(request.getExecType()    == getExecType()));
       	}
       	return false;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfCoalescedDataRequest(" + getRequestId() + "," + getDataType().getSimpleName()
                + "," + getExecType() + "," + getIndex() + "," + getNbRequested() + ","
                + getBlockSize() + ", " + fRequests.toString() + ")]";
    }
}
