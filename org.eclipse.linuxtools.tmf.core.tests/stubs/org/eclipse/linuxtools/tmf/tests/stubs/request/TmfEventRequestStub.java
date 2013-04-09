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

package org.eclipse.linuxtools.tmf.tests.stubs.request;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;

/**
 * <b><u>TmfEventRequestStub</u></b>
 */
public class TmfEventRequestStub extends TmfEventRequest {

    /**
     * @param dataType the event type
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType) {
        super(dataType);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType, final TmfTimeRange range) {
        super(dataType, range);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param nbRequested the number of events requested
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType, final TmfTimeRange range, final int nbRequested) {
        super(dataType, range, nbRequested);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param nbRequested the number of events requested
     * @param blockSize the event block size
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType, final TmfTimeRange range, final int nbRequested, final int blockSize) {
        super(dataType, range, nbRequested, blockSize);
    }

    /**
     * @param dataType the event type
     * @param range the requested time range
     * @param index the initial event index
     * @param nbRequested the number of events requested
     * @param blockSize the event block size
     */
    public TmfEventRequestStub(final Class<? extends ITmfEvent> dataType, final TmfTimeRange range, final long index, final int nbRequested, final int blockSize) {
        super(dataType, range, index, nbRequested, blockSize);
    }

    @Override
    public void handleData(final ITmfEvent data) {
        super.handleData(data);
    }
}
