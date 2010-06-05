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

package org.eclipse.linuxtools.tmf.request;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfData;

/**
 * <b><u>TmfCoalescedDataRequest</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfCoalescedDataRequest<T extends TmfData> extends TmfDataRequest<T> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	protected Vector<ITmfDataRequest<T>> fRequests = new Vector<ITmfDataRequest<T>>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfCoalescedDataRequest(Class<T> dataType) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.SHORT);
    }

    public TmfCoalescedDataRequest(Class<T> dataType, ExecutionType execType) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param nbRequested
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, ExecutionType.SHORT);
    }

    public TmfCoalescedDataRequest(Class<T> dataType, int index, ExecutionType execType) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param index
     * @param nbRequested
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, ExecutionType.SHORT);
    }

    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested, ExecutionType execType) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE, execType);
    }

    /**
     * @param index
     * @param nbRequested
     * @param blockSize
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested, int blockSize) {
        super(dataType, index, nbRequested, blockSize, ExecutionType.SHORT);
    }

    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested, int blockSize, ExecutionType execType) {
        super(dataType, index, nbRequested, blockSize, execType);
    }

    // ------------------------------------------------------------------------
    // Management
    // ------------------------------------------------------------------------

	public void addRequest(ITmfDataRequest<T> request) {
		fRequests.add(request);
	}

	public boolean isCompatible(ITmfDataRequest<T> request) {

		boolean ok = request.getIndex() == getIndex();
		ok &= request.getNbRequested()  == getNbRequested();
		ok &= request.getBlockize()     == getBlockize();
		ok &= request.getExecType()     == getExecType();
		
		return ok;
	}

    // ------------------------------------------------------------------------
    // ITmfDataRequest
    // ------------------------------------------------------------------------

    @Override
	public void handleData() {
    	for (ITmfDataRequest<T> request : fRequests) {
    		request.setData(getData());
    		request.handleData();
    	}
    }

    @Override
    public void done() {
    	for (ITmfDataRequest<T> request : fRequests) {
    		request.done();
    	}
    	super.done();
    }

    @Override
    public void fail() {
    	for (ITmfDataRequest<T> request : fRequests) {
    		request.fail();
    	}
    	super.fail();
    }

    @Override
    public void cancel() {
    	for (ITmfDataRequest<T> request : fRequests) {
    		request.cancel();
    	}
    	super.cancel();
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
    	if (other instanceof TmfCoalescedDataRequest<?>) {
    		TmfCoalescedDataRequest<?> request = (TmfCoalescedDataRequest<?>) other;
       		return 	(request.getDataType()    == getDataType())   &&
       				(request.getIndex()       == getIndex())      &&
       				(request.getNbRequested() == getNbRequested() &&
      				(request.getExecType()    == getExecType()));
       	}
       	return false;
    }

    @Override
    public String toString() {
		return "[TmfCoalescedDataRequest(" + getRequestId() + "," + getDataType().getSimpleName() 
			+ "," + getIndex() + "," + getNbRequested() + "," + getBlockize() + ")]";
    }

}
