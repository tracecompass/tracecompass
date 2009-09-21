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

import java.lang.reflect.InvocationTargetException;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
 * TODO: Add support for live streaming (notifications, incremental indexing, ...)
 */
public class TmfExperiment implements ITmfRequestHandler<TmfEvent> {

	// ========================================================================
    // Attributes
    // ========================================================================

	private final int DEFAULT_PAGE_SIZE = 1000;

    private String fExperimentId;
    private Vector<ITmfTrace> fTraces;
    private int fNbEvents;
    private TmfTimeRange fTimeRange;
	private TmfTimestamp fEpoch;

    // Indicate if the stream should be pre-indexed
    private final boolean fWaitForIndexCompletion;

    // ========================================================================
    // Constructors/Destructor
    // ========================================================================

    public TmfExperiment(String id, ITmfTrace[] traces) {
        this(id, traces, TmfTimestamp.BigBang, false);
    }

    public TmfExperiment(String id, ITmfTrace[] traces, boolean waitForIndexCompletion) {
        this(id, traces, TmfTimestamp.BigBang, waitForIndexCompletion);
    }

    public TmfExperiment(String id, ITmfTrace[] traces, TmfTimestamp epoch, boolean waitForIndexCompletion) {
    	fExperimentId = id;
    	fTraces = new Vector<ITmfTrace>();
    	for (ITmfTrace trace : traces) {
    		fTraces.add(trace);
    	}
    	fEpoch = epoch;
        TmfSignalManager.addListener(this);
        fWaitForIndexCompletion = waitForIndexCompletion;

        updateNbEvents();
		updateTimeRange();
		indexExperiment();
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
    public long getIndex(TmfTimestamp ts) {
    	ITmfTrace trace = fTraces.firstElement();
    	TmfTraceContext context = trace.seekEvent(ts);
    	return context.index;
    }

    // TODO: Go over all the traces
    public TmfTimestamp getTimestamp(int index) {
    	ITmfTrace trace = fTraces.firstElement();
    	TmfTraceContext context = trace.seekEvent(index);
    	return context.timestamp;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    public void addTrace(ITmfTrace trace) {
		fTraces.add(trace);
		synchronized(this) {
			updateNbEvents();
			updateTimeRange();
		}
		indexExperiment();
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

		// Process the request
		processEventRequest(request);
		
		// Wait for completion if needed
    	if (waitForCompletion) {
			request.waitForCompletion();
		}
	}

	/**
	 * Process a data request
	 * 
	 * @param request
	 */
	private void processEventRequest(final TmfDataRequest<TmfEvent> request) {

		// General request parameters
		final TmfTimestamp endTime;
		final long index;

		// Initialize depending on request type
		if (request.getRange() != null) {
			index = getIndex(request.getRange().getStartTime());
			endTime = request.getRange().getEndTime();
    	} else {
    		index = request.getIndex();
    		endTime = TmfTimestamp.BigCrunch;
    	}

		// Process the request
		Thread thread = new Thread() {

			private ITmfTrace[] traces = new ITmfTrace[0];
			private TmfTraceContext[] contexts;
			private TmfEvent[] peekEvents;

			@Override
			public void run() {
				// Extract the general request information
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
				positionTraces(index, traces, contexts, peekEvents);

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
	 * Given an experiment event index, position the set of traces at the
	 * corresponding location.
	 * 
	 * @param index
	 * @param traces
	 * @param contexts
	 * @param peekEvents
	 */
	private void positionTraces(long index, ITmfTrace[] traces, TmfTraceContext[] contexts, TmfEvent[] peekEvents) {

		// Initialize the contexts at the page boundary
		TmfTraceContext[] ctx = new TmfTraceContext[contexts.length];
		for (int i = 0; i < contexts.length; i++) {
			contexts[i] = new TmfTraceContext(traces[i].seekLocation(null));
		}

		int entry = (int) index / DEFAULT_PAGE_SIZE;
		if (entry < fIndices.size()) {
			ctx = fIndices.elementAt((int) index / DEFAULT_PAGE_SIZE);
			for (int i = 0; i < contexts.length; i++) {
				contexts[i] = new TmfTraceContext(ctx[i]);
			}
		}

		// Set the current position (a page boundary)
		int current = ((int) index / DEFAULT_PAGE_SIZE) * DEFAULT_PAGE_SIZE;

		for (int i = 0; i < contexts.length; i++) {
			peekEvents[i] = traces[i].getNextEvent(contexts[i]);
		}

		// Position the traces
		while (current++ < index) {
			getNextEvent(traces, contexts, peekEvents);
		}
	}

	// Returns the next event in chronological order
	// TODO: Consider the time adjustment
	private TmfEvent getNextEvent(ITmfTrace[] traces, TmfTraceContext[] contexts, TmfEvent[] peekEvents) {
		int index = 0;
		TmfEvent current = peekEvents[0];
		TmfTimestamp currentTS = (current != null) ? current.getTimestamp() : TmfTimestamp.BigCrunch;
		for (int i = 1; i < traces.length; i++) {
			if (peekEvents[i] != null) {
				TmfTimestamp newTS = peekEvents[i].getTimestamp();
				if (newTS.compareTo(currentTS, true) < 0) {
					index = i;
					currentTS = newTS;
					current = peekEvents[i];
				}
			}
		}
		TmfEvent event = (current != null) ? new TmfEvent(current) : null;
		peekEvents[index] = traces[index].getNextEvent(contexts[index]);
		return event;
	}

    // ========================================================================
    // Indexing
    // ========================================================================

	/**
	 * Index the experiment 
	 */
	private Vector<TmfTraceContext[]> fIndices = new Vector<TmfTraceContext[]>();
	private Boolean fIndexing = false;
	private IndexingJob job;

	public void indexExperiment() {

		synchronized(fIndexing) {
			if (fIndexing) {
				// An indexing job is already running but a new request came
				// in (probably due to a change in the trace set). The index
				// being currently built is therefore already invalid.
				// TODO: Cancel the and restart the job
				return;
			}
			fIndexing = true;
		}

		job = new IndexingJob(fExperimentId);
		job.schedule();
		if (fWaitForIndexCompletion) {
	        ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
	        try {
	            dialog.run(true, true, new IRunnableWithProgress() {
	                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
	                    monitor.beginTask("Indexing " + fExperimentId, IProgressMonitor.UNKNOWN);
	    				job.join();
	                    monitor.done();
	                }
	            });
	        } catch (InvocationTargetException e) {
	            e.printStackTrace();
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
		}
	}

	private class IndexingJob extends Job {

		public IndexingJob(String name) {
			super(name);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {

			// Minimal check
			if (fTraces.size() == 0) {
	            fIndexing = false;
				return Status.OK_STATUS;
			}

			monitor.beginTask("Indexing " + fExperimentId, IProgressMonitor.UNKNOWN);

    		ITmfTrace[] traces = new ITmfTrace[0];
    		TmfTraceContext[] contexts;
    		TmfEvent[] events;

    		// Initialize the traces and contexts arrays
    		traces = fTraces.toArray(traces);
    		contexts = new TmfTraceContext[traces.length];
    		events = new TmfEvent[traces.length];
    		TmfTraceContext[] savedContexts = new TmfTraceContext[contexts.length];
    		for (int i = 0; i < contexts.length; i++) {
    			// Context of the first event of the trace
    			contexts[i] = traces[i].seekLocation(null);
    			savedContexts[i] = new TmfTraceContext(contexts[i]);
    			// Set the peeked event
    			events[i] = traces[i].getNextEvent(new TmfTraceContext(contexts[i]));
    		}
    
    		// Initialize the index array
    		fIndices.clear();
			
    		// Get the ordered events and populate the indices
    		int nbEvents = 0;
    		while ((getNextEvent(traces, contexts, events)) != null)
    		{
    			if (nbEvents++ % DEFAULT_PAGE_SIZE == 0) {
    	    		for (int i = 0; i < contexts.length; i++) {
    	    			savedContexts[i].timestamp = events[i].getTimestamp();
    	    		}
    				fIndices.add(savedContexts);
    			}
    			// Prepare the saved contexts for the upcoming save (next iteration)
    			if ((nbEvents + 1) % DEFAULT_PAGE_SIZE == 0) {
    				savedContexts = new TmfTraceContext[contexts.length];
    				for (int i = 0; i < contexts.length; i++) {
    					savedContexts[i] = new TmfTraceContext(contexts[i]);
    				}
    			}

    			monitor.worked(1);
                // Check monitor *after* fCheckpoints has been updated
                if (monitor.isCanceled()) {
                    monitor.done();
                	return Status.CANCEL_STATUS;
                }
    		}

            monitor.done();

            fIndexing = false;
            return Status.OK_STATUS;
		}
    }

    // ========================================================================
    // Signal handlers
    // ========================================================================

    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
    	indexExperiment();
    }

    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
    	indexExperiment();
    }

    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
    	// TODO: Update index
    	synchronized(this) {
    		updateNbEvents();
    		updateTimeRange();
    	}
		TmfSignalManager.dispatchSignal(new TmfExperimentUpdatedSignal(this, this, signal.getTrace()));
    }
}
