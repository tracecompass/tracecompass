/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.trace;

import java.util.Vector;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.ITmfRequestHandler;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.stream.ITmfEventStream;
import org.eclipse.linuxtools.tmf.stream.ITmfEventStream.StreamContext;

/**
 * <b><u>TmfTrace</u></b>
 * <p>
 * TmfTrace represents a time-ordered set of events tied to a single event
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
public class TmfTrace implements ITmfRequestHandler<TmfEvent> {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final String fId;
    private final ITmfEventStream fStream;
    private final TmfTimestamp fEpoch;
    
    // ========================================================================
    // Constructors
    // ========================================================================

    public TmfTrace(String id, ITmfEventStream stream) {
        this(id, stream, TmfTimestamp.BigBang);
    }

    public TmfTrace(String id, ITmfEventStream stream, TmfTimestamp epoch) {
        assert stream != null;
        fId = id;
        fStream = stream;
        fEpoch = epoch;
        TmfSignalManager.addListener(this);
    }

    public void dispose() {
        TmfSignalManager.removeListener(this);
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    public String getId() {
    	return fId;
    }

    public ITmfEventStream getStream() {
    	return fStream;
    }

    public TmfTimestamp getEpoch() {
    	return fEpoch;
    }

    public TmfTimeRange getTimeRange() {
    	return fStream.getTimeRange();
    }

    public int getNbEvents() {
    	return fStream.getNbEvents();
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfRequestHandler#processRequest(org.eclipse.linuxtools.tmf.eventlog.TmfDataRequest, boolean)
     */
    public void processRequest(TmfDataRequest<TmfEvent> request, boolean waitForCompletion) {
    	if (request.getRange() != null) {
    		serviceEventRequestByTimestamp(request);
    	} else {
    		serviceEventRequestByIndex(request);
    	}
        if (waitForCompletion) {
            request.waitForCompletion();
        }
    }

//    @TmfSignalHandler
//	public void handleSignal(TmfStreamUpdateSignal event) {
//		for (ITmfTraceEventListener listener : fListeners) {
//			listener.handleEvent(new TmfTraceUpdateEvent(this));
//		}
//	}

    // ========================================================================
    // Helper functions
    // ========================================================================

    /* (non-Javadoc)
     * 
     * @param request
     */
    private void serviceEventRequestByTimestamp(final TmfDataRequest<TmfEvent> request) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                TmfTimestamp startTime = request.getRange().getStartTime();
                TmfTimestamp endTime   = request.getRange().getEndTime();
                int blockSize = request.getBlockize();

                int nbRequestedEvents = request.getNbRequestedItems();
                if (nbRequestedEvents == -1) {
                    nbRequestedEvents = Integer.MAX_VALUE;
                }

                Vector<TmfEvent> events = new Vector<TmfEvent>();
                int nbEvents = 0;

                StreamContext context = new StreamContext(null);
                TmfEvent event = fStream.getEvent(context, startTime);

                while (!request.isCancelled() && nbEvents < nbRequestedEvents && event != null &&
                        event.getTimestamp().compareTo(endTime, false) <= 0 )
                {
                    events.add(event);
                    if (++nbEvents % blockSize == 0) {
                    	TmfEvent[] result = new TmfEvent[events.size()];
                    	events.toArray(result);
                    	request.setData(result);
                        request.handleData();
                        events.removeAllElements();
                    }
                    // To avoid an unnecessary read passed the last event requested 
                    if (nbEvents < nbRequestedEvents)
                        event = fStream.getNextEvent(context);
                }
            	TmfEvent[] result = new TmfEvent[events.size()];
            	events.toArray(result);
            	request.setData(result);

            	request.handleData();
                request.done();
            }
        };
        thread.start();
    }

    /* (non-Javadoc)
     * 
     * @param request
     */
    private void serviceEventRequestByIndex(final TmfDataRequest<TmfEvent> request) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                int blockSize = request.getBlockize();

                int nbRequestedEvents = request.getNbRequestedItems();
                if (nbRequestedEvents == -1) {
                    nbRequestedEvents = Integer.MAX_VALUE;
                }

                Vector<TmfEvent> events = new Vector<TmfEvent>();
                int nbEvents = 0;

                StreamContext context = new StreamContext(null);
                TmfEvent event = fStream.getEvent(context, request.getIndex());

                while (!request.isCancelled() && nbEvents < nbRequestedEvents && event != null)
                {
                    events.add(event);
                    if (++nbEvents % blockSize == 0) {
                    	TmfEvent[] result = new TmfEvent[events.size()];
                    	events.toArray(result);
                    	request.setData(result);
                        request.handleData();
                        events.removeAllElements();
                    }
                    // To avoid an unnecessary read passed the last event requested 
                    if (nbEvents < nbRequestedEvents)
                        event = fStream.getNextEvent(context);
                }
            	TmfEvent[] result = new TmfEvent[events.size()];
            	events.toArray(result);

            	request.setData(result);
                request.handleData();
                request.done();
            }
        };
        thread.start();
    }

}
