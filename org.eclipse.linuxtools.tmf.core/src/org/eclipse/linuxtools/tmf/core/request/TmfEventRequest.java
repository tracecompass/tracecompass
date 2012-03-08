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
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * <b><u>TmfEventRequest</u></b>
 * <p>
 * Implement me. Please.
 */
public abstract class TmfEventRequest<T extends ITmfEvent> extends TmfDataRequest<T> implements ITmfEventRequest<T> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final TmfTimeRange fRange;	// The requested events time range

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param range
     */
    public TmfEventRequest(Class<T> dataType) {
        this(dataType, TmfTimeRange.ETERNITY, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    public TmfEventRequest(Class<T> dataType, ExecutionType execType) {
        this(dataType, TmfTimeRange.ETERNITY, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param range
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range) {
        this(dataType, range, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }

    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, ExecutionType execType) {
        this(dataType, range, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param range
     * @param nbRequested
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested) {
        this(dataType, range, 0, nbRequested, DEFAULT_BLOCK_SIZE, ExecutionType.FOREGROUND);
    }
    
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested, ExecutionType execType) {
        this(dataType, range, 0, nbRequested, DEFAULT_BLOCK_SIZE, execType);
    }
    
    /**
     * @param range
     * @param nbRequested
     * @param blockSize Size of the largest blocks expected
     */
    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested, int blockSize) {
    	this(dataType, range, 0, nbRequested, blockSize, ExecutionType.FOREGROUND);
    }

    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int nbRequested, int blockSize, ExecutionType execType) {
    	this(dataType, range, 0, nbRequested, blockSize, execType);
    }

    public TmfEventRequest(Class<T> dataType, TmfTimeRange range, int index, int nbRequested, int blockSize, ExecutionType execType) {
    	super(dataType, index, nbRequested, blockSize, execType);
    	fRange = range;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the requested time range
     */
    @Override
	public TmfTimeRange getRange() {
        return fRange;
    }

    // ------------------------------------------------------------------------
    // Setters
    // ------------------------------------------------------------------------

    /**
     * this method is called by the event provider to set the index corresponding
     * to the time range start time once it is known
     * @param index the start time index
     */
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
    	return getRequestId();
    }

    @Override
    public boolean equals(Object other) {
    	if (other instanceof TmfEventRequest<?>) {
    		TmfEventRequest<?> request = (TmfEventRequest<?>) other;
    		return super.equals(other) && request.fRange.equals(fRange);
    	}
    	return false;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
		return "[TmfEventRequest(" + getRequestId() + "," + getDataType().getSimpleName() 
			+ "," + getRange() + "," + getIndex() + "," + getNbRequested() + "," + getBlockSize() + ")]";
    }

}
