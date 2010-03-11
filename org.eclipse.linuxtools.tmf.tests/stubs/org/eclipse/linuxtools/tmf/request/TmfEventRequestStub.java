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

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;

/**
 * <b><u>TmfEventRequestStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventRequestStub<T extends TmfEvent> extends TmfEventRequest<T> {

    /**
     * @param range
     */
    public TmfEventRequestStub(Class<T> dataType) {
        this(dataType, TmfTimeRange.Eternity, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param range
     */
    public TmfEventRequestStub(Class<T> dataType, TmfTimeRange range) {
        this(dataType, range, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param range
     * @param nbRequested
     */
    public TmfEventRequestStub(Class<T> dataType, TmfTimeRange range, int nbRequested) {
        this(dataType, range, nbRequested, DEFAULT_BLOCK_SIZE);
    }
    
    /**
     * @param range
     * @param nbRequested
     * @param blockSize Size of the largest blocks expected
     */
    public TmfEventRequestStub(Class<T> dataType, TmfTimeRange range, int nbRequested, int blockSize) {
    	super(dataType, range, nbRequested, blockSize);
    }

	@Override
	public void handleData() {
		// TODO Auto-generated method stub
	}
}
