/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   William Bourque - Initial API and implementation
 *   Yuriy Vashchuk - Heritage correction.
 *   Francois Chouinard - Cleanup and refactoring
 *   Francois Chouinard - Moved from LTTng to TMF
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Class to request events for given time range from a trace to fill a HistogramDataModel and HistogramView.
 *
 * @version 1.0
 * @author Francois Chouinard
 * <p>
 */
public class HistogramRequest extends TmfEventRequest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The histogram data model to fill.
     */
    protected final HistogramDataModel fHistogram;

    // ------------------------------------------------------------------------
    // Constructor
    // ------------------------------------------------------------------------

    /**
     * Constructor
     *
     * @param histogram
     *            The histogram data model
     * @param range
     *            The time range to request data
     * @param rank
     *            The index of the first event to retrieve
     * @param nbEvents
     *            The number of events requested
     * @param blockSize
     *            The number of events per block
     * @param execType
     *            The requested execution priority
     * @since 2.0
     *
     */
    public HistogramRequest(HistogramDataModel histogram, TmfTimeRange range,
            int rank, int nbEvents, int blockSize,
            ITmfDataRequest.ExecutionType execType) {
        super(ITmfEvent.class, range, rank, nbEvents,
                (blockSize > 0) ? blockSize : ITmfTrace.DEFAULT_TRACE_CACHE_SIZE,
                execType);
        fHistogram = histogram;
    }

    // ------------------------------------------------------------------------
    // TmfEventRequest
    // ------------------------------------------------------------------------

    /**
     * Handle the event from the trace by updating the histogram data model.
     *
     * @param event a event from the trace
     * @see org.eclipse.linuxtools.tmf.core.request.TmfDataRequest#handleData(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public void handleData(ITmfEvent event) {
        super.handleData(event);
        if (event != null) {
            long timestamp = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
            fHistogram.countEvent(getNbRead(), timestamp);
        }
    }

    /**
     * Complete the request. It also notifies the histogram model about the completion.
     *
     * @see org.eclipse.linuxtools.tmf.core.request.TmfDataRequest#handleCompleted()
     */
    @Override
    public void handleCompleted() {
        fHistogram.complete();
        super.handleCompleted();
    }
}
