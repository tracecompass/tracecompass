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
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace.TmfTraceContext;

/**
 * <b><u>TmfExperiment</u></b>
 * <p>
 * TmfExperiment presents a time-ordered, unified view of a set of 
 * TmfTraces that are part of a tracing experiment. 
 * <p>
 * TODO: Complete multi-trace experiment
 */
public class TmfExperiment implements ITmfRequestHandler<TmfEvent> {

	// ========================================================================
    // Attributes
    // ========================================================================

//	private final int CACHE_SIZE = 10000;

    private String fExperimentId;
    private Vector<ITmfTrace> fTraces;
    private int fNbEvents;
    private TmfTimeRange fTimeRange;
	private TmfTimestamp fEpoch;

    // ========================================================================
    // Constructors/Destructor
    // ========================================================================

    public TmfExperiment(String id, ITmfTrace[] traces) {
        this(id, traces, TmfTimestamp.BigBang);
    }

    public TmfExperiment(String id, ITmfTrace[] traces, TmfTimestamp epoch) {
    	fExperimentId = id;
    	fTraces = new Vector<ITmfTrace>();
    	for (ITmfTrace trace : traces) {
    		addTrace(trace);
    	}
    	fEpoch = epoch;
        TmfSignalManager.addListener(this);
    }

    public void dispose() {
        TmfSignalManager.removeListener(this);
    	fTraces.clear();
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    public String getExperimentId() {
    	return fExperimentId;
    }

    public ITmfTrace[] getTraces() {
    	ITmfTrace[] result = new ITmfTrace[fTraces.size()];
    	return fTraces.toArray(result);
    }

    public TmfTimestamp getEpoch() {
    	return fEpoch;
    }

    public TmfTimeRange getTimeRange() {
    	return fTimeRange;
    }

    public int getNbEvents() {
    	return fNbEvents;
    }

    // TODO: Go over all the traces
    public int getIndex(TmfTimestamp ts) {
    	return fTraces.firstElement().getIndex(ts);
    }

    // TODO: Go over all the traces
    public TmfTimestamp getTimestamp(int index) {
    	return fTraces.firstElement().getTimestamp(index);
    }

//	public TmfTimestamp getTimestamp(int index) {
//		if (fIndices.size() == 0) {
//			indexExperiment();
//		}
//
//		int offset = index / CACHE_SIZE;
//
//		ITmfEventStream[] traces = new ITmfEventStream[0];
//		StreamContext[] contexts;
//		TmfEvent[] peekEvents;
//
//		traces = fTraces.toArray(traces);
//		contexts = new StreamContext[traces.length];
//		peekEvents = new TmfEvent[traces.length];
//
//		for (int i = 0; i < traces.length; i++) {
//			contexts[i] = new StreamContext(fIndices.get(offset)[i]);
//			peekEvents[i] = traces[i].peekEvent(contexts[i]);
//		}
//
//		TmfEvent event = getNextEvent(traces, contexts, peekEvents);
//		for (int i = offset * CACHE_SIZE; i < index; i++) {
//			event = getNextEvent(traces, contexts, peekEvents);
//		}
//		
//		return event != null ? event.getTimestamp() : null;
//	}

    // ========================================================================
    // Operators
    // ========================================================================

    public void addTrace(ITmfTrace trace) {
		fTraces.add(trace);
		synchronized(this) {
			updateNbEvents();
			updateTimeRange();
		}
    }

    private void updateNbEvents() {
    	int nbEvents = 0;
    	for (ITmfTrace trace : fTraces) {
    		nbEvents += trace.getNbEvents();
    	}
    	fNbEvents = nbEvents;
    }

    private void updateTimeRange() {
		TmfTimestamp startTime = fTimeRange != null ? fTimeRange.getStartTime() : TmfTimestamp.BigCrunch;
		TmfTimestamp endTime   = fTimeRange != null ? fTimeRange.getEndTime()   : TmfTimestamp.BigBang;

		for (ITmfTrace trace : fTraces) {
    		TmfTimestamp traceStartTime = trace.getTimeRange().getStartTime();
    		if (traceStartTime.compareTo(startTime, true) < 0)
    			startTime = traceStartTime;

    		TmfTimestamp traceEndTime = trace.getTimeRange().getEndTime();
    		if (traceEndTime.compareTo(endTime, true) > 0)
    			endTime = traceEndTime;
    	}
		fTimeRange = new TmfTimeRange(startTime, endTime);
    }

    // ========================================================================
    // ITmfRequestHandler
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.eventlog.ITmfRequestHandler#processRequest(org.eclipse.linuxtools.tmf.eventlog.TmfDataRequest, boolean)
     */
	public void processRequest(TmfDataRequest<TmfEvent> request, boolean waitForCompletion) {
    	if (request.getRange() != null) {
    		processEventRequestByTimestamp(request);
    	} else {
    		processEventRequestByIndex(request);
    	}
		if (waitForCompletion) {
			request.waitForCompletion();
		}
	}

	/**
	 * Process a time range request - this has to be seriously re-worked...
	 * 
	 * @param request
	 */
	private void processEventRequestByTimestamp(final TmfDataRequest<TmfEvent> request) {

		Thread thread = new Thread() {

			private ITmfTrace[] traces = new ITmfTrace[0];
			private TmfTraceContext[] contexts;
			private TmfEvent[] peekEvents;

			@Override
			public void run() {
				// Extract the request information
				TmfTimestamp startTime = request.getRange().getStartTime();
				TmfTimestamp endTime = request.getRange().getEndTime();
				int blockSize = request.getBlockize();

				int nbRequestedEvents = request.getNbRequestedItems();
				if (nbRequestedEvents == -1) {
					nbRequestedEvents = Integer.MAX_VALUE;
				}

				// Create the result buffer
				Vector<TmfEvent> events = new Vector<TmfEvent>();
				int nbEvents = 0;

				// Initialize the traces array and position the streams
				traces = fTraces.toArray(traces);
				contexts = new TmfTraceContext[traces.length];
				peekEvents = new TmfEvent[traces.length];
				for (int i = 0; i < contexts.length; i++) {
					contexts[i] = traces[i].seekEvent(startTime);
					peekEvents[i] = traces[i].peekEvent(contexts[i]);
				}

				// Get the ordered events
				TmfEvent event = getNextEvent(traces, contexts, peekEvents);
				while (!request.isCancelled() && nbEvents < nbRequestedEvents && event != null
						&& event.getTimestamp().compareTo(endTime, false) <= 0)
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
						event = getNextEvent(traces, contexts, peekEvents);
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

	/**
	 * Process an index range request - this has to be seriously re-worked...
	 * Does not work for multiple traces - yet
	 * 
	 * @param request
	 */
	private void processEventRequestByIndex(final TmfDataRequest<TmfEvent> request) {

		Thread thread = new Thread() {

			private ITmfTrace[] traces = new ITmfTrace[0];
			private TmfTraceContext[] contexts;
			private TmfEvent[] peekEvents;

			@Override
			public void run() {
				// Extract the request information
				int blockSize = request.getBlockize();

				int nbRequestedEvents = request.getNbRequestedItems();
				if (nbRequestedEvents == -1) {
					nbRequestedEvents = Integer.MAX_VALUE;
				}

				// Create the result buffer
				Vector<TmfEvent> events = new Vector<TmfEvent>();
				int nbEvents = 0;

				// Initialize the traces array and position the streams
				traces = fTraces.toArray(traces);
				contexts = new TmfTraceContext[traces.length];
				peekEvents = new TmfEvent[traces.length];
				for (int i = 0; i < contexts.length; i++) {
					contexts[i] = traces[i].seekEvent(request.getIndex());
					peekEvents[i] = traces[i].peekEvent(contexts[i]);
				}

				// Get the ordered events
				TmfEvent event = getNextEvent(traces, contexts, peekEvents);
				while (!request.isCancelled() && nbEvents < nbRequestedEvents && event != null) {
					events.add(event);
					if (++nbEvents % blockSize == 0) {
						TmfEvent[] result = new TmfEvent[events.size()];
						events.toArray(result);
						request.setData(result);
						request.handleData();
						events.removeAllElements();
					}
					// To avoid an unnecessary read passed the last event
					// requested
					if (nbEvents < nbRequestedEvents)
						event = getNextEvent(traces, contexts, peekEvents);
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

    // Returns the next event in chronological order
	// TODO: Consider the time adjustment
	private TmfEvent getNextEvent(ITmfTrace[] traces, TmfTraceContext[] contexts, TmfEvent[] peekEvents) {
		int index = 0;
		TmfEvent evt = peekEvents[0];
		TmfTimestamp ts0 = evt != null ? evt.getTimestamp() : TmfTimestamp.BigCrunch;
		for (int i = 1; i < traces.length; i++) {
			if (peekEvents[i] != null) {
				TmfTimestamp ts1 = peekEvents[i].getTimestamp();
				if (ts1.compareTo(ts0, true) < 0) {
					index = i;
					ts0 = ts1;
				}
			}
		}
		TmfEvent event = traces[index].getNextEvent(contexts[index]);
		peekEvents[index] = traces[index].peekEvent(contexts[index]);
		return event;
	}

//	private Vector<TraceContext[]> fIndices = new Vector<TraceContext[]>();
//	public void indexExperiment() {
//		ITmfTrace[] traces = new ITmfTrace[0];
//		TraceContext[] contexts;
//		TmfEvent[] peekEvents;
//
//		traces = fTraces.toArray(traces);
//		contexts = new TraceContext[traces.length];
//		peekEvents = new TmfEvent[traces.length];
//		for (int i = 0; i < contexts.length; i++) {
//			contexts[i] = traces[i].seekEvent(TmfTimestamp.BigBang);
//			peekEvents[i] = traces[i].peekEvent(contexts[i]);
//		}
//
//		// Initialize the indices
//		TraceContext[] ctx = new TraceContext[contexts.length];
//		for (int i = 0; i < ctx.length; i++) {
//			ctx[i] = new TraceContext(contexts[i]);
//		}
//		fIndices.add(ctx);
//
//		// Get the ordered events and populate the indices
//		int nbEvents = 0;
//		while (getNextEvent(traces, contexts, peekEvents) != null)
//		{
//			if (++nbEvents % CACHE_SIZE == 0) {
//				ctx = new TraceContext[contexts.length];
//				for (int i = 0; i < ctx.length; i++) {
//					ctx[i] = new TraceContext(contexts[i]);
//				}
//				fIndices.add(ctx);
//			}
//		}
//	}

    // ========================================================================
    // Signal handlers
    // ========================================================================

    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
    	synchronized(this) {
    		updateNbEvents();
    		updateTimeRange();
    	}
		TmfSignalManager.dispatchSignal(new TmfExperimentUpdatedSignal(this, this, signal.getTrace()));
    }

}
