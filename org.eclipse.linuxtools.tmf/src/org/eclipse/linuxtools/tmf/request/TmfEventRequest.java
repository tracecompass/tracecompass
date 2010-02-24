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
 * <b><u>TmfEventRequest</u></b>
 * <p>
 * Implement me. Please.
 */
public class TmfEventRequest<T extends TmfEvent> extends TmfDataRequest<T> {

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
    public TmfEventRequest(Class<? extends TmfEvent> dataType) {
        this(dataType, TmfTimeRange.Eternity, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param range
     */
    public TmfEventRequest(Class<? extends TmfEvent> dataType, TmfTimeRange range) {
        this(dataType, range, ALL_DATA, DEFAULT_BLOCK_SIZE);
    }

    /**
     * @param range
     * @param nbRequested
     */
    public TmfEventRequest(Class<? extends TmfEvent> dataType, TmfTimeRange range, int nbRequested) {
        this(dataType, range, nbRequested, DEFAULT_BLOCK_SIZE);
    }
    
    /**
     * @param range
     * @param nbRequested
     * @param blockSize Size of the largest blocks expected
     */
    public TmfEventRequest(Class<? extends TmfEvent> dataType, TmfTimeRange range, int nbRequested, int blockSize) {
    	super(dataType, 0, nbRequested, blockSize);
    	fRange = range;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the requested time range
     */
    public TmfTimeRange getRange() {
        return fRange;
    }

}
