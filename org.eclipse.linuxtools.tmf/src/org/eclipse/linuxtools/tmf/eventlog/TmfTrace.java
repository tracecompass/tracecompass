/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.eventlog;

import java.io.IOException;
import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeWindow;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>TmfEventLog</u></b>
 * <p>
 * TmfEventLog represents a time-ordered set of events tied to a single event
 * stream. It keeps track of the global information about the event log:
 * <ul>
 * <li> the epoch, a reference timestamp for the whole log (t0)
 * <li> the span of the log i.e. the timestamps range
 * <li> the total number of events
 * </ul>
 * As an ITmfRequestHandler, it provides an implementation of process()
 * which handles event requests.
 * <p>
 * TODO: Handle concurrent and possibly overlapping requests in a way that
 * optimizes the stream access and event parsing.
 */
public class TmfTrace implements ITmfRequestHandler {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final String fId;
    private final TmfEventStream fStream;
    private final TmfTimestamp fEpoch;
    
    // ========================================================================
    // Constructors
    // ========================================================================

    public TmfTrace(String id, TmfEventStream stream) {
        assert stream != null;
        fId = id;
        fStream = stream;
        fEpoch = TmfTimestamp.BigBang;
    }

    public TmfTrace(String id, TmfEventStream stream, TmfTimestamp epoch) {
        assert stream != null;
        fId = id;
        fStream = stream;
        fEpoch = epoch;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    public String getId() {
    	return fId;
    }

    public TmfEventStream getStream() {
    	return fStream;
    }

    public TmfTimestamp getEpoch() {
    	return fEpoch;
    }

    public TmfTimeWindow getTimeRange() {
    	return fStream.getTimeRange();
    }

    public int getNbEvents() {
    	return fStream.getNbEvents();
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfRequestExecutor#execute(org.eclipse.linuxtools.tmf.eventlog.TmfEventRequest, boolean)
     */
    public void process(final TmfEventRequest request, boolean waitForCompletion) {
        serviceEventRequest(request);
        if (waitForCompletion) {
            request.waitForCompletion();
        }
    }

    // ========================================================================
    // Helper functions
    // ========================================================================

    /* (non-Javadoc)
     * 
     * @param request
     */
    private void serviceEventRequest(final TmfEventRequest request) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                TmfTimestamp startTime = request.getRange().getStartTime();
                TmfTimestamp endTime   = request.getRange().getEndTime();
                int blockSize = request.getBlockize();

                int nbRequestedEvents = request.getNbRequestedEvents();
                if (nbRequestedEvents == -1) {
                    nbRequestedEvents = Integer.MAX_VALUE;
                }

                Vector<TmfEvent> events = new Vector<TmfEvent>();
                int nbEvents = 0;
                try {
                    TmfEvent event = fStream.seek(startTime);
                    while (!request.isCancelled() && nbEvents < nbRequestedEvents && event != null &&
                            event.getTimestamp().compareTo(endTime, false) <= 0 )
                    {
                        events.add(event);
                        if (++nbEvents % blockSize == 0) {
                            request.newEvents(events);
                            events.removeAllElements();
                        }
                        event = fStream.getNextEvent();
                    }
                    request.newEvents(events);
                    request.done();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        };
        thread.start();
    }

}
