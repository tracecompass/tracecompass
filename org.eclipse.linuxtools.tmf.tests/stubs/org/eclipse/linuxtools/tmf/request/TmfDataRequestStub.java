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

import org.eclipse.linuxtools.tmf.event.TmfData;

/**
 * <b><u>TmfDataRequestStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfDataRequestStub<T extends TmfData> extends TmfDataRequest<T> {

    /**
     * Default constructor
     */
    public TmfDataRequestStub(Class<T> dataType) {
        this(dataType, 0, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param nbRequested
     */
    public TmfDataRequestStub(Class<T> dataType, int index) {
        this(dataType, index, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param index
     * @param nbRequested
     */
    public TmfDataRequestStub(Class<T> dataType, int index, int nbRequested) {
        this(dataType, index, nbRequested, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param index
     * @param nbRequested
     * @param blockSize
     */
    public TmfDataRequestStub(Class<T> dataType, int index, int nbRequested, int blockSize) {
        super(dataType, index, nbRequested, blockSize);
    }

	@Override
	public void handleData() {
		// TODO Auto-generated method stub
	}

}
