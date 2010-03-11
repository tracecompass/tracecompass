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

	protected Vector<TmfDataRequest<T>> fRequests = new Vector<TmfDataRequest<T>>();

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfCoalescedDataRequest(Class<T> dataType) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param nbRequested
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param index
     * @param nbRequested
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param index
     * @param nbRequested
     * @param blockSize
     */
    public TmfCoalescedDataRequest(Class<T> dataType, int index, int nbRequested, int blockSize) {
        super(dataType, index, nbRequested, blockSize);
    }

    // ------------------------------------------------------------------------
    // Management
    // ------------------------------------------------------------------------

	public void addRequest(TmfDataRequest<T> request) {
		fRequests.add(request);
	}

	public boolean isCompatible(TmfDataRequest<T> request) {

		boolean ok = request.getIndex() == getIndex();;
		ok &= request.getNbRequested() == getNbRequested();
		ok &= request.getBlockize() == getBlockize();
		
		return ok;
	}

    // ------------------------------------------------------------------------
    // ITmfDataRequest
    // ------------------------------------------------------------------------

    @Override
	public void handleData() {
    	for (TmfDataRequest<T> request : fRequests) {
    		request.setData(getData());
    		request.handleData();
    	}
    }

    @Override
    public void handleCompleted() {
    	for (TmfDataRequest<T> request : fRequests) {
    		request.handleCompleted();
    	}
    }

    @Override
    public void handleSuccess() {
    	for (TmfDataRequest<T> request : fRequests) {
    		request.handleSuccess();
    	}
    }

    @Override
    public void handleFailure() {
    	for (TmfDataRequest<T> request : fRequests) {
    		request.handleFailure();
    	}
    }

    @Override
    public void handleCancel() {
    	for (TmfDataRequest<T> request : fRequests) {
    		request.handleCancel();
    	}
    }

    @Override
    public void done() {
    	for (TmfDataRequest<T> request : fRequests) {
    		request.done();
    	}
    }

    @Override
    public void fail() {
    	for (TmfDataRequest<T> request : fRequests) {
    		request.fail();
    	}
    }

    @Override
    public void cancel() {
    	for (TmfDataRequest<T> request : fRequests) {
    		request.cancel();
    	}
    }

}
