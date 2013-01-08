/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.request.TmfRequest;

/**
 * <b><u>TmfRequestStub</u></b>
 */
public class TmfRequestStub extends TmfRequest {

    /**
     * Constructor for all the events at normal priority
     */
    public TmfRequestStub() {
        super();
    }

    /**
     * Constructor for all the events at the specified  priority
     *
     * @param priority the request priority
     */
    public TmfRequestStub(TmfRequestPriority priority) {
        super(priority);
    }

    /**
     * Constructor for all the events in a time range
     *
     * @param timeRange The time range
     */
    public TmfRequestStub(TmfTimeRange timeRange) {
        super(timeRange);
    }

    /**
     * Constructor for all the events in a block
     *
     * @param startIndex  The start index
     * @param nbRequested The number of events requested
     */
    public TmfRequestStub(long startIndex, long nbRequested) {
        super(startIndex, nbRequested);
    }

    /**
     * Standard constructor
     *
     * @param timeRange   The time range
     * @param startIndex  The start index
     * @param nbRequested The number of events requested
     */
    public TmfRequestStub(TmfTimeRange timeRange, long startIndex, long nbRequested) {
        super(timeRange, startIndex, nbRequested);
    }

    /**
     * Full constructor
     *
     * @param timeRange   Time range of interest
     * @param nbRequested Number of events requested
     * @param startIndex  Index of the first event requested
     * @param priority    Request priority
     */
    public TmfRequestStub(TmfTimeRange timeRange, long startIndex, long nbRequested, TmfRequestPriority priority) {
        super(timeRange, startIndex, nbRequested, priority);
    }

    /**
     * Copy constructor
     *
     * @param other the other request
     */
    public TmfRequestStub(TmfRequestStub other) {
        super(other);
    }

    /**
     * @param timeRange the time range
     */
    @Override
    public void setTimeRange(TmfTimeRange timeRange) {
        super.setTimeRange(timeRange);
    }

    /**
     * @param nbRequested the number of events requested
     */
    @Override
    public void setNbRequested(long nbRequested) {
        super.setNbRequested(nbRequested);
    }

    /**
     * @param index the index of the first event requested
     */
    @Override
    public void setStartIndex(long index) {
        super.setStartIndex(index);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.request.TmfRequest#handleEvent(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public synchronized void handleEvent(final ITmfEvent data) {
        super.handleEvent(data);
    }
}
