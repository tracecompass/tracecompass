/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
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

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * <b><u>TmfCoalescedEventRequest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfCoalescedEventRequest<T extends ITmfEvent> extends TmfCoalescedDataRequest<T> implements ITmfEventRequest<T> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	private TmfTimeRange fRange;	// The requested events time range

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * @param range
     */
    public TmfCoalescedEventRequest(Class<T> dataType) {
        this(dataType, TmfTimeRange.Eternity, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    public TmfCoalescedEventRequest(Class<T> dataType, ExecutionType execType) {
        this(dataType, TmfTimeRange.Eternity, ALL_DATA, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param range
     */
    public TmfCoalescedEventRequest(Class<T> dataType, TmfTimeRange range) {
        this(dataType, range, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    public TmfCoalescedEventRequest(Class<T> dataType, TmfTimeRange range, ExecutionType execType) {
        this(dataType, range, ALL_DATA, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param range
     * @param nbRequested
     */
    public TmfCoalescedEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested) {
        this(dataType, range, nbRequested, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }
    
    public TmfCoalescedEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested, ExecutionType execType) {
        this(dataType, range, nbRequested, DEFAULT_BLOCK_SIZE, execType);
    }
    
    /**
     * @param range
     * @param nbRequested
     * @param blockSize Size of the largest blocks expected
     */
    public TmfCoalescedEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested, int blockSize) {
    	super(dataType, 0, nbRequested, blockSize, ExecutionType.FOREGROUND);
    	fRange = range;
    }

    public TmfCoalescedEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested, int blockSize, ExecutionType execType) {
    	super(dataType, 0, nbRequested, blockSize, execType);
    	fRange = range;
    }

    public TmfCoalescedEventRequest(Class<T> dataType, TmfTimeRange range, int index, int nbRequested, int blockSize, ExecutionType execType) {
    	super(dataType, index, nbRequested, blockSize, execType);
    	fRange = range;
    }

    // ------------------------------------------------------------------------
    // Management
    // ------------------------------------------------------------------------

	@Override
	public boolean isCompatible(ITmfDataRequest<T> request) {
		if (request instanceof ITmfEventRequest<?>) {
			boolean ok = getNbRequested() == request.getNbRequested();
			ok &= getIndex() == request.getIndex();
			ok &= getExecType() == request.getExecType();
            //ok &= getDataType() == request.getDataType();
			if (ok) {
				ITmfTimestamp startTime = ((ITmfEventRequest<T>) request).getRange().getStartTime();
				ITmfTimestamp endTime   = ((ITmfEventRequest<T>) request).getRange().getEndTime();
				if (!fRange.contains(startTime))
					fRange = new TmfTimeRange(startTime, fRange.getEndTime());
				if (!fRange.contains(endTime))
					fRange = new TmfTimeRange(fRange.getStartTime(), endTime);
			}
			return ok;
		}
		return false;
	}

    // ------------------------------------------------------------------------
    // ITmfDataRequest
    // ------------------------------------------------------------------------

    @Override
    public void handleData(T data) {
        super.handleData(data);
        for (ITmfDataRequest<T> request : fRequests) {
            if (data == null) {
                request.handleData(null);
            } else {
                if (request instanceof TmfEventRequest<?>) {
                    TmfEventRequest<T> req = (TmfEventRequest<T>) request;
                    if (!req.isCompleted()) {
                        ITmfTimestamp ts = data.getTimestamp();
                        if (req.getRange().contains(ts)) {
                            if (req.getDataType().isInstance(data)) {
                                req.handleData(data);
                            }
                        }
                    }
                }
                else {
                    TmfDataRequest<T> req = (TmfDataRequest<T>) request;
                    if (!req.isCompleted()) {
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
    	if (other instanceof TmfCoalescedEventRequest<?>) {
    		TmfCoalescedEventRequest<?> request = (TmfCoalescedEventRequest<?>) other;
       		return 	(request.getDataType()    == getDataType()) &&
       				(request.getIndex()       == getIndex())    &&
       				(request.getNbRequested() == getNbRequested()) &&
       	    		(request.getRange().equals(getRange()));
       	}
    	if (other instanceof TmfCoalescedDataRequest<?>) {
       		return super.equals(other);
    	}
  		return false;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[TmfCoalescedEventRequest(" + getRequestId() + "," + getDataType().getSimpleName() 
			+ "," + getRange() + "," + getIndex() + "," + getNbRequested() + "," + getBlockSize() + ")]";
    }

}
