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

package org.eclipse.linuxtools.tmf.tests.stubs.request;

import org.eclipse.linuxtools.tmf.core.event.TmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;

/**
 * <b><u>TmfEventRequestStub</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public class TmfEventRequestStub<T extends TmfEvent> extends TmfEventRequest<T> {

    /**
     * @param dataType
     */
    public TmfEventRequestStub(final Class<T> dataType) {
        super(dataType);
    }

    /**
     * @param dataType
     * @param range
     */
    public TmfEventRequestStub(final Class<T> dataType, final TmfTimeRange range) {
        super(dataType, range);
    }

    /**
     * @param dataType
     * @param range
     * @param nbRequested
     */
    public TmfEventRequestStub(final Class<T> dataType, final TmfTimeRange range, final int nbRequested) {
        super(dataType, range, nbRequested);
    }

    /**
     * @param dataType
     * @param range
     * @param nbRequested
     * @param blockSize
     */
    public TmfEventRequestStub(final Class<T> dataType, final TmfTimeRange range, final int nbRequested, final int blockSize) {
        super(dataType, range, nbRequested, blockSize);
    }

    /**
     * @param dataType
     * @param range
     * @param nbRequested
     * @param blockSize
     */
    public TmfEventRequestStub(final Class<T> dataType, final TmfTimeRange range, final long index, final int nbRequested, final int blockSize) {
        super(dataType, range, index, nbRequested, blockSize);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfDataRequest#handleData(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public void handleData(final T data) {
        super.handleData(data);
    }
}
