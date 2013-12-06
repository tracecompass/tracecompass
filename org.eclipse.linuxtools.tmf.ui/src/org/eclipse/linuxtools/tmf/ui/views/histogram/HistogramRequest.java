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
 *   Simon Delisle - Added a new parameter to the constructor
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.views.histogram;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfLostEvent;
import org.eclipse.linuxtools.tmf.core.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.core.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;

/**
 * Class to request events for given time range from a trace to fill a
 * HistogramDataModel and HistogramView.
 *
 * @version 1.0
 * @author Francois Chouinard
 *         <p>
 */
public class HistogramRequest extends TmfEventRequest {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * The histogram data model to fill.
     */
    protected final HistogramDataModel fHistogram;

    private final boolean fFullRange;

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
     * @since 3.0
     *
     */
    @Deprecated
    public HistogramRequest(HistogramDataModel histogram, TmfTimeRange range,
            int rank, int nbEvents, int blockSize,
            ITmfEventRequest.ExecutionType execType) {
        super(ITmfEvent.class, range, rank, nbEvents, execType);
        fHistogram = histogram;
        if (execType == ExecutionType.FOREGROUND) {
            fFullRange = false;
        } else {
            fFullRange = true;
        }
    }

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
     * @param fullRange
     *            Full range or time range for histogram request
     * @since 3.0
     *
     */
    public HistogramRequest(HistogramDataModel histogram, TmfTimeRange range,
            int rank, int nbEvents, int blockSize,
            ITmfEventRequest.ExecutionType execType, boolean fullRange) {
        super(ITmfEvent.class, range, rank, nbEvents, execType);
        fHistogram = histogram;
        fFullRange = fullRange;
    }

    // ------------------------------------------------------------------------
    // TmfEventRequest
    // ------------------------------------------------------------------------

    /**
     * Handle the event from the trace by updating the histogram data model.
     *
     * @param event
     *            a event from the trace
     * @see org.eclipse.linuxtools.tmf.core.request.TmfEventRequest#handleData(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public void handleData(ITmfEvent event) {
        super.handleData(event);
        if (event != null) {
            if (event instanceof ITmfLostEvent) {
                ITmfLostEvent lostEvents = (ITmfLostEvent) event;
                /* clear the old data when it is a new request */
                fHistogram.countLostEvent(lostEvents.getTimeRange(), lostEvents.getNbLostEvents(), fFullRange);

            } else { /* handle lost event */
                long timestamp = event.getTimestamp().normalize(0, ITmfTimestamp.NANOSECOND_SCALE).getValue();
                fHistogram.countEvent(getNbRead(), timestamp);
            }
        }
    }

    /**
     * Complete the request. It also notifies the histogram model about the
     * completion.
     *
     * @see org.eclipse.linuxtools.tmf.core.request.TmfEventRequest#handleCompleted()
     */
    @Override
    public void handleCompleted() {
        fHistogram.complete();
        super.handleCompleted();
    }
}
