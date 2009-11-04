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
import org.eclipse.linuxtools.tmf.component.TmfComponent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.ITmfRequestHandler;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;

/**
 * <b><u>TmfExperiment</u></b>
 * <p>
 * TmfExperiment presents a time-ordered, unified view of a set of TmfTraces
 * that are part of a tracing experiment. 
 * <p>
 */
public class TmfExperiment extends TmfComponent implements ITmfRequestHandler<TmfEvent> {

// TODO: Complete multi-trace experiment
// TODO: Add support for dynamic addition/removal of traces
// TODO: Add support for live streaming (notifications, incremental indexing, ...)

	// ========================================================================
    // Attributes
    // ========================================================================

	// The currently selected experiment
    private static TmfExperiment fCurrentExperiment;

	// The experiment ID
    private String fExperimentId;

    // The set of trace sthat constitute the experiment
    private Vector<ITmfTrace> fTraces;

    // The total number of events
    private int fNbEvents;

    // The experiment time range
    private TmfTimeRange fTimeRange;

    // The experiment reference timestamp (default: BigBang)
    private TmfTimestamp fEpoch;

    // Indicates if the stream should be indexed synchronously (default: false)
    private final boolean fWaitForIndexCompletion;

    // ========================================================================
    // Constructors/Destructor
    // ========================================================================

    /**
     * @param id
     * @param traces
     */
    public TmfExperiment(String id, ITmfTrace[] traces) {
        this(id, traces, TmfTimestamp.BigBang, DEFAULT_INDEX_PAGE_SIZE, false);
    }

    /**
     * @param id
     * @param traces
     * @param waitForIndexCompletion
     */
    public TmfExperiment(String id, ITmfTrace[] traces, boolean waitForIndexCompletion) {
        this(id, traces, TmfTimestamp.BigBang, DEFAULT_INDEX_PAGE_SIZE, waitForIndexCompletion);
    }

    /**
     * @param id
     * @param traces
     * @param epoch
     * @param waitForIndexCompletion
     */
    public TmfExperiment(String id, ITmfTrace[] traces, TmfTimestamp epoch, int indexPageSize, boolean waitForIndexCompletion) {
    	super();

    	fExperimentId = id;
    	fTraces = new Vector<ITmfTrace>();
    	for (ITmfTrace trace : traces) {
    		fTraces.add(trace);
    	}
    	fEpoch = epoch;
    	fIndexPageSize = indexPageSize;
        fWaitForIndexCompletion = waitForIndexCompletion;

		updateNbEvents();
		updateTimeRange();
//		indexExperiment();
    }

    /**
     * 
     */
    @Override
	public void dispose() {
        super.dispose();
    	fTraces.clear();
    	fCurrentExperiment= null;
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    public static TmfExperiment getCurrentExperiment() {
    	return fCurrentExperiment;
    }

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

    /**
     * Returns the index of the first event with the requested timestamp.
     * If none, returns the index of the next event (if any).
     *  
     * @param ts
     * @return
     */
    public long getIndex(TmfTimestamp ts) {
        // TODO: Go over all the traces
    	ITmfTrace trace = fTraces.firstElement();
    	TmfTraceContext context = trace.seekEvent(ts);
    	return context.getIndex();
    }

    /**
     * Returns the timestamp of the event at the requested index.
     * If none, returns null.
     *  
     * @param index
     * @return
     */
    public TmfTimestamp getTimestamp(int index) {
        // TODO: Go over all the traces
    	ITmfTrace trace = fTraces.firstElement();
    	TmfTraceContext context = trace.seekEvent(index);
    	return context.getTimestamp();
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /**
     * Add a trace to the experiment trace set
     * 
     * @param trace
     */
    public void addTrace(ITmfTrace trace) {
		fTraces.add(trace);
		synchronized(this) {
			updateNbEvents();
			updateTimeRange();
		}
    }

    /**
     * Update the total number of events
     */
    private void updateNbEvents() {
    	int nbEvents = 0;
    	for (ITmfTrace trace : fTraces) {
    		nbEvents += trace.getNbEvents();
    	}
    	fNbEvents = nbEvents;
    }

    /**
     * Update the global time range
     */
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
	 * @see org.eclipse.linuxtools.tmf.request.ITmfRequestHandler#processRequest(org.eclipse.linuxtools.tmf.request.TmfDataRequest, boolean)
	 */
	public void processRequest(TmfDataRequest<TmfEvent> request, boolean waitForCompletion) {

		// Process the request
		processDataRequest(request);
		
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
	private void processDataRequest(final TmfDataRequest<TmfEvent> request) {

		// General request parameters
		final TmfTimestamp endTime;
		final long index;

		// Initialize request params depending on request type
		if (request.getRange() != null) {
			index = getIndex(request.getRange().getStartTime());
			endTime = request.getRange().getEndTime();
    	} else {
    		index = request.getIndex();
    		endTime = TmfTimestamp.BigCrunch;
    	}

		// Process the request
		Thread thread = new Thread() {

			@Override
			public void run() {

				// Key variables
				ITmfTrace[] traces = new ITmfTrace[0];	// The set of traces
				TmfTraceContext[] contexts;				// The set of trace contexts

				// Extract the general request information
				int blockSize = request.getBlockize();
				int nbRequestedEvents = request.getNbRequestedItems();
				if (nbRequestedEvents == -1) {
					nbRequestedEvents = Integer.MAX_VALUE;
				}

				// Create the result buffer
				Vector<TmfEvent> events = new Vector<TmfEvent>();
				int nbEvents = 0;

				// Initialize the traces array and position the traces
				// at the first requested event
				traces = fTraces.toArray(traces);
				contexts = new TmfTraceContext[traces.length];
				positionTraces(index, traces, contexts);

				// Get the ordered events
				TmfEvent event = getNextEvent(traces, contexts);
				while (!request.isCancelled() && nbEvents < nbRequestedEvents && event != null
						&& event.getTimestamp().compareTo(endTime, false) < 0)
				{
					events.add(event);
					if (++nbEvents % blockSize == 0) {
						pushData(request, events);
					}
					// Avoid an unnecessary read passed the last event requested
					if (nbEvents < nbRequestedEvents)
						event = getNextEvent(traces, contexts);
				}

				if (!request.isCancelled() && !request.isFailed()) {
					pushData(request, events);
					request.done();
				}
			}
		};
		thread.start();
	}

	/**
	 * Given an experiment event index, position the set of traces so a call
	 * to getNextEvent() will retrieve the corresponding event.
	 * 
	 * @param index
	 * @param traces
	 * @param contexts
	 * @param nextEvents
	 */
	private synchronized void positionTraces(long index, ITmfTrace[] traces, TmfTraceContext[] contexts) {

		// Compute the index page and corresponding index
		int page = (int) index / fIndexPageSize;
		int current = page * fIndexPageSize;

		// Retrieve the checkpoint and set the contexts (make copies)
		TmfTraceContext[] saveContexts = new TmfTraceContext[contexts.length];
		if (page < fExperimentIndex.size()) {
			saveContexts = fExperimentIndex.elementAt(page);
			for (int i = 0; i < contexts.length; i++) {
				contexts[i] = new TmfTraceContext(saveContexts[i]);
			}
		} else {
			// If the page entry doesn't exist (e.g. indexing not completed),
			// set contexts at the the last entry (if it exists)
			page = fExperimentIndex.size() - 1;
			if (page >= 0) {
				saveContexts = fExperimentIndex.elementAt(page);
				for (int i = 0; i < contexts.length; i++) {
					contexts[i] = new TmfTraceContext(saveContexts[i]);
				}
				current = page * fIndexPageSize;
			}
			// Index is empty... position traces at their beginning
			else {
				for (int i = 0; i < contexts.length; i++) {
					contexts[i] = new TmfTraceContext(traces[i].seekLocation(null));
				}
				current = 0;
			}
		}

		// Position the traces at the requested index
		while (current++ < index) {
			getNextEvent(traces, contexts);
		}
	}

	/**
	 * Scan the next events from all traces and return the next one
	 * in chronological order.
	 * 
	 * @param traces
	 * @param contexts
	 * @param nextEvents
	 * @return
	 */
	private TmfEvent getNextEvent(ITmfTrace[] traces, TmfTraceContext[] contexts) {
		// TODO: Consider the time adjustment
		int trace = 0;
		TmfTimestamp timestamp = contexts[trace].getTimestamp();
		if (timestamp == null) {
			timestamp = TmfTimestamp.BigCrunch;
		}
		for (int i = 1; i < traces.length; i++) {
			if (contexts[i].getTimestamp() != null) {
				TmfTimestamp otherTS = contexts[i].getTimestamp();
				if (otherTS.compareTo(timestamp, true) < 0) {
					trace = i;
					timestamp = otherTS;
				}
			}
		}
		TmfEvent event = traces[trace].getNextEvent(contexts[trace]);
		return event;
	}

	/**
	 * Format the result data and notify the requester.
	 * Note: after handling, the data is *removed*.
	 * 
	 * @param request
	 * @param events
	 */
	private void pushData(TmfDataRequest<TmfEvent> request, Vector<TmfEvent> events) {
		TmfEvent[] result = new TmfEvent[events.size()];
		events.toArray(result);
		request.setData(result);
		request.handleData();
		events.removeAllElements();
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[TmfExperiment (" + fExperimentId + ")]";
	}

    // ========================================================================
    // Indexing
    // ========================================================================

	/*
	 * The experiment holds the globally ordered events of its set of traces.
	 * It is expected to provide access to each individual event by index i.e.
	 * it must be possible to request the Nth event of the experiment.
	 * 
	 * The purpose of the index is to keep the information needed to rapidly
	 * restore the traces contexts at regular intervals (every INDEX_PAGE_SIZE
	 * event).
	 */

	// The index page size
	private static final int DEFAULT_INDEX_PAGE_SIZE = 1000;
	private final int fIndexPageSize;

	// The experiment index
	private Vector<TmfTraceContext[]> fExperimentIndex = new Vector<TmfTraceContext[]>();

	// Indicates that an indexing job is already running
	private Boolean fIndexing = false;

	// The indexing job
	private IndexingJob job;

	/**
	 * indexExperiment
	 * 
	 * Creates the experiment index.
	 */
	private void indexExperiment() {

		synchronized(fIndexing) {
			if (fIndexing) {
				// An indexing job is already running but a new request came
				// in (probably due to a change in the trace set). The index
				// being currently built is therefore already invalid.
				// TODO: Cancel and restart the job
				// TODO: Add support for dynamically adding/removing traces
				return;
			}
			fIndexing = true;
		}

		job = new IndexingJob(fExperimentId);
		job.schedule();
		if (fWaitForIndexCompletion) {
	        ProgressMonitorDialog dialog = new ProgressMonitorDialog(null);
	        try {
				// TODO: Handle cancel!
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

			Vector<TmfTraceContext[]> indices = new Vector<TmfTraceContext[]>();
			
			// Minimal check
			if (fTraces.size() == 0) {
	            fIndexing = false;
				return Status.OK_STATUS;
			}

			monitor.beginTask("Indexing " + fExperimentId, IProgressMonitor.UNKNOWN);

    		ITmfTrace[] traces = new ITmfTrace[0];
    		TmfTraceContext[] contexts;
    		int nbTraces = fTraces.size();

    		// Initialize the traces and contexts arrays
    		traces = fTraces.toArray(traces);
    		contexts = new TmfTraceContext[nbTraces];
    		TmfTraceContext[] savedContexts = new TmfTraceContext[nbTraces];
			int nullEvents = 0;
    		for (int i = 0; i < nbTraces; i++) {
    			// Context of the first event of the trace
    			contexts[i] = traces[i].seekLocation(null);
    			savedContexts[i] = new TmfTraceContext(contexts[i].getLocation(), contexts[i].getTimestamp(), 0);
    			if (contexts[i].getTimestamp() == null)
    				nullEvents++;
    		}
    		// Check if there is anything to index
    		if (nullEvents >= nbTraces) {
	            fIndexing = false;
				return Status.OK_STATUS;
    		}
    		// FIXME: LTTng hack - start
//			indices.add(savedContexts);	// TMF
    		// FIXME: LTTng hack - end
    		
    		// Get the ordered events and populate the indices
    		// FIXME: LTTng hack - start
//    		int nbEvents = 0; // TMF
    		int nbEvents = -1; // LTTng
    		// FIXME: LTTng hack - end
    		while ((getNextEvent(traces, contexts)) != null)
    		{
    			if (++nbEvents % fIndexPageSize == 0) {
    				// Special case: if the total number of events is a multiple of the
    				// DEFAULT_PAGE_SIZE then all the pending events are null. In that
    				// case, we don't store an additional entry in the index array.
    				nullEvents = 0;
    				savedContexts = new TmfTraceContext[nbTraces];
    	    		for (int i = 0; i < nbTraces; i++) {
    	    			savedContexts[i] = new TmfTraceContext(contexts[i]);
    	    			if (contexts[i].getTimestamp() ==  null)
    	    				nullEvents++;
    	    		}
    	    		if (nullEvents < nbTraces) {
    	    			indices.add(savedContexts);
    	    		}
    			}

    			monitor.worked(1);
                if (monitor.isCanceled()) {
                    monitor.done();
                	return Status.CANCEL_STATUS;
                }
    		}

            monitor.done();
            fExperimentIndex = indices;

//            dumpIndex();

            fIndexing = false;
            return Status.OK_STATUS;
		}
    }

//	/**
//	 * Dump the experiment index
//	 */
//	private void dumpIndex() {
//		System.out.println("-----");
//		System.out.println("Index of " + fExperimentId);
//		for (int i = 0; i < fExperimentIndex.size(); i++) {
//        	System.out.println("Entry:" + i);
//        	TmfTraceContext[] contexts = fExperimentIndex.get(i);
//        	int nbEvents = 0;
//        	for (int j = 0; j < contexts.length; j++) {
//        		ITmfTrace trace = fTraces.get(j);
//            	TmfTraceContext context = trace.seekLocation(contexts[j].getLocation());
//            	TmfEvent event = fTraces.get(j).getNextEvent(new TmfTraceContext(context));
//            	nbEvents += contexts[j].getIndex();
//            	System.out.println("  ["  + trace.getName() + "]" 
//            			+  " index: "     + contexts[j].getIndex()
//            			+ ", timestamp: " + contexts[j].getTimestamp()
//            			+ ", event: "     + event.getTimestamp());
//            	assert (contexts[j].getTimestamp().compareTo(event.getTimestamp(), false) == 0);
//        	}
//        	assert ((i+1) * fIndexPageSize == nbEvents);
//	
//        }
//	}

    // ========================================================================
    // Signal handlers
    // ========================================================================

    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
		fCurrentExperiment = this;
    	indexExperiment();
    }

    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
//    	indexExperiment();
    }

    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
    	// TODO: Incremental index update
    	synchronized(this) {
    		updateNbEvents();
    		updateTimeRange();
    	}
		broadcastSignal(new TmfExperimentUpdatedSignal(this, this, signal.getTrace()));
    }
}