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

package org.eclipse.linuxtools.tmf.experiment;

import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.component.ITmfContext;
import org.eclipse.linuxtools.tmf.component.TmfProvider;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfTraceContext;
import org.eclipse.linuxtools.tmf.trace.TmfTraceUpdatedSignal;

/**
 * <b><u>TmfExperiment</u></b>
 * <p>
 * TmfExperiment presents a time-ordered, unified view of a set of TmfTraces
 * that are part of a tracing experiment. 
 * <p>
 */
public class TmfExperiment<T extends TmfEvent> extends TmfProvider<T> {

// TODO: Complete multi-trace experiment
// TODO: Add support for dynamic addition/removal of traces
// TODO: Add support for live streaming (notifications, incremental indexing, ...)
// TODO: Implement indexing-on-demand

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	// The currently selected experiment
    private static TmfExperiment<?> fCurrentExperiment;

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

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param type
     * @param id
     * @param traces
     * @param epoch
     * @param indexPageSize
     */
    public TmfExperiment(Class<T> type, String id, ITmfTrace[] traces, TmfTimestamp epoch, int indexPageSize) {
    	super(type);

    	fExperimentId = id;
    	fTraces = new Vector<ITmfTrace>();
    	for (ITmfTrace trace : traces) {
    		fTraces.add(trace);
    	}
    	fEpoch = epoch;
    	fIndexPageSize = indexPageSize;

		updateNbEvents();
		updateTimeRange();
    }

    /**
     * @param type
     * @param id
     * @param traces
     */
    public TmfExperiment(Class<T> type, String id, ITmfTrace[] traces) {
        this(type, id, traces, TmfTimestamp.Zero, DEFAULT_INDEX_PAGE_SIZE);
    }

    /**
     * @param type
     * @param id
     * @param traces
     * @param indexPageSize
     */
    public TmfExperiment(Class<T> type, String id, ITmfTrace[] traces, int indexPageSize) {
        this(type, id, traces, TmfTimestamp.Zero, indexPageSize);
    }

    /**
     * 
     */
    @Override
	public void deregister() {
    	fTraces.clear();
    	fCurrentExperiment= null;
        super.deregister();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public static TmfExperiment<?> getCurrentExperiment() {
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
     * Returns the rank of the first event with the requested timestamp.
     * If none, returns the index of the next event (if any).
     *  
     * @param ts
     * @return
     */
    public long getRank(TmfTimestamp ts) {
        // FIXME: Go over all the traces
    	ITmfTrace trace = fTraces.firstElement();
    	TmfTraceContext context = trace.seekEvent(ts);
    	return context.getRank();
    }

    /**
     * Returns the timestamp of the event at the requested index.
     * If none, returns null.
     *  
     * @param index
     * @return
     */
    public TmfTimestamp getTimestamp(int index) {
        // FIXME: Go over all the traces
    	ITmfTrace trace = fTraces.firstElement();
    	TmfTraceContext context = trace.seekEvent(index);
    	TmfEvent event = trace.getNextEvent(context);
    	TmfTimestamp ts = (event != null) ? event.getTimestamp() : null;
    	return ts;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

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
    		TmfTimestamp traceStartTime = trace.getStartTime();
    		if (traceStartTime.compareTo(startTime, true) < 0)
    			startTime = traceStartTime;

    		TmfTimestamp traceEndTime = trace.getEndTime();
    		if (traceEndTime.compareTo(endTime, true) > 0)
    			endTime = traceEndTime;
    	}
		fTimeRange = new TmfTimeRange(startTime, endTime);
    }

    // ------------------------------------------------------------------------
    // TmfProvider
    // ------------------------------------------------------------------------

	@Override
	public ITmfContext setContext(TmfDataRequest<T> request) {
		TmfExperimentContext context = new TmfExperimentContext(fTraces);
		positionTraces(request.getIndex(), context);
		return context;
	}

	@SuppressWarnings("unchecked")
	@Override
	public T getNext(ITmfContext context) {
		if (context instanceof TmfExperimentContext) {
			return (T) getNextEvent((TmfExperimentContext) context);
		}
		return null;
	}

	@Override
	public boolean isCompleted(TmfDataRequest<T> request, T data) {
		if (request instanceof TmfEventRequest<?> && data != null) {
			return data.getTimestamp().compareTo(((TmfEventRequest<T>) request).getRange().getEndTime(), false) > 0;
		}
		return false;
	}

	/**
	 * Given an experiment event index, position the set of traces so a call
	 * to getNextEvent() will retrieve the corresponding event.
	 * 
	 * @param index
	 * @param context
	 */
	private synchronized void positionTraces(long index, TmfExperimentContext context) {

		// Extract the relevant information
		ITmfTrace[] traces = context.getTraces();
		TmfEvent[]  events = context.getEvents();
		TmfTraceContext[] contexts = context.getContexts();

		int page = 0;		// The checkpoint page
		int current = 0;	// The current event index (rank)

		// If there is no checkpoint created yet, start from the beginning
		if (fExperimentIndex.size() == 0) {
			for (int i = 0; i < contexts.length; i++) {
				contexts[i] = traces[i].seekLocation(null).clone();
				events[i] = traces[i].parseEvent(contexts[i]);
			}
		}
		else {
			page = (int) index / fIndexPageSize;
			if (page >= fExperimentIndex.size()) {
				page = fExperimentIndex.size() - 1;
			}

			TmfTraceContext[] checkpoint = fExperimentIndex.elementAt(page).getContexts();
			for (int i = 0; i < contexts.length; i++) {
				contexts[i] = checkpoint[i].clone();
				events[i] = traces[i].parseEvent(contexts[i]);
			}
			current = page * fIndexPageSize;
		}

		// Position the traces at the requested index
		while (current++ < index) {
			getNextEvent(context);
		}
	}

	/**
	 * Scan the next events from all traces and return the next one
	 * in chronological order.
	 * 
	 * @param context
	 * @return
	 */
	private TmfEvent getNextEvent(TmfExperimentContext context) {
		// TODO: Consider the time adjustment
		int trace = 0;
		TmfTimestamp timestamp = TmfTimestamp.BigCrunch;
		if (context.getEvents()[trace] != null) {
			timestamp = context.getEvents()[trace].getTimestamp();
		}
		for (int i = 1; i < context.getTraces().length; i++) {
			if (context.getEvents()[i].getTimestamp() != null) {
				TmfTimestamp otherTS = context.getEvents()[i].getTimestamp();
				if (otherTS.compareTo(timestamp, true) < 0) {
					trace = i;
					timestamp = otherTS;
				}
			}
		}
		TmfEvent event = context.getTraces()[trace].getNextEvent(context.getContexts()[trace]);
		context.getEvents()[trace] = context.getTraces()[trace].parseEvent(context.getContexts()[trace]);
		return event;
	}

	/**
	 * Scan the next events from all traces and return the next one
	 * in chronological order.
	 * 
	 * @param context
	 * @return
	 */
	private TmfTimestamp getNextEventTimestamp(TmfExperimentContext context) {
		// TODO: Consider the time adjustment
		int trace = 0;
		TmfTimestamp timestamp = TmfTimestamp.BigCrunch;
		if (context.getEvents()[trace] != null) {
			timestamp = context.getEvents()[trace].getTimestamp();
		}
		for (int i = 1; i < context.getTraces().length; i++) {
			if (context.getEvents()[i].getTimestamp() != null) {
				TmfTimestamp otherTS = context.getEvents()[i].getTimestamp();
				if (otherTS.compareTo(timestamp, true) < 0) {
					trace = i;
					timestamp = otherTS;
				}
			}
		}
		return timestamp;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[TmfExperiment (" + fExperimentId + ")]";
	}

    // ------------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------------

	/*
	 * The experiment holds the globally ordered events of its set of traces.
	 * It is expected to provide access to each individual event by index i.e.
	 * it must be possible to request the nth event of the experiment.
	 * 
	 * The purpose of the index is to keep the information needed to rapidly
	 * restore the traces contexts at regular intervals (every INDEX_PAGE_SIZE
	 * event).
	 */

	// The index page size
	private static final int DEFAULT_INDEX_PAGE_SIZE = 1000;
	private final int fIndexPageSize;

	// The experiment index
	private Vector<TmfExperimentCheckpoint> fExperimentIndex = new Vector<TmfExperimentCheckpoint>();

	// Indicates that an indexing job is already running
	private Boolean fIndexing = false;
	private Boolean fIndexed  = false;

	// The indexing job
	private IndexingJob job;

	/**
	 * indexExperiment
	 * 
	 * Creates the experiment index.
	 */
	public void indexExperiment(boolean waitForCompletion) {

		synchronized(fIndexing) {
			if (fIndexed || fIndexing) {
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

    	if (waitForCompletion) {
    		try {
    			job.join();
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

            int nbEvents = 0;
            TmfTimestamp startTime = null;
            TmfTimestamp lastTime  = null;

            fExperimentIndex = new Vector<TmfExperimentCheckpoint>();
            
            try {
            	// Reset the traces
        		TmfExperimentContext context = new TmfExperimentContext(fTraces);
        		positionTraces(0, context);
        		TmfTraceContext[] traces = context.cloneContexts();

               	TmfTimestamp timestamp = getNextEventTimestamp(context);
                startTime = new TmfTimestamp(timestamp);
                lastTime  = new TmfTimestamp(timestamp);
                TmfEvent event = getNextEvent(context);
                while (event != null) {
           			if ((nbEvents++ % fIndexPageSize) == 0) {
           				fExperimentIndex.add(new TmfExperimentCheckpoint(lastTime, traces));
                   		fNbEvents = nbEvents;
                   		fTimeRange = new TmfTimeRange(startTime, lastTime);

                        monitor.worked(1);

                        // Check monitor *after* fCheckpoints has been updated
                        if (monitor.isCanceled()) {
                            monitor.done();
                        	return Status.CANCEL_STATUS;
                        }
                    }

                    // We will need the contexts at the next iteration
                    if ((nbEvents % fIndexPageSize) == 0) {
               			traces = context.cloneContexts();
           				lastTime = new TmfTimestamp(event.getTimestamp());
           			}

           			event = getNextEvent(context);
                }

            }
            finally {
                synchronized(this) {
                	fNbEvents = nbEvents;
                	fTimeRange = new TmfTimeRange(startTime, lastTime);
                	fIndexing = false;
                	fIndexed = true;
                }
                monitor.done();
            }

//            dumpExperimentCheckpoints();

            return Status.OK_STATUS;
		}
    }

//	// ------------------------------------------------------------------------
//	// Toubleshooting code
//	// ------------------------------------------------------------------------
//
//	private void dumpExperimentCheckpoints() {
//		System.out.println("-----");
//		System.out.println("Checkpoints of " + fExperimentId);
//		for (int i = 0; i < fExperimentIndex.size(); i++) {
//        	System.out.println("Entry:" + i);
//        	TmfExperimentCheckpoint checkpoint = fExperimentIndex.get(i);
//        	TmfTraceContext[] contexts = checkpoint.getContexts();
//        	for (int j = 0; j < contexts.length; j++) {
//        		ITmfTrace trace = fTraces.get(j);
//            	TmfTraceContext context = trace.seekLocation(contexts[j].getLocation());
//            	TmfEvent event = fTraces.get(j).getNextEvent(new TmfTraceContext(context));
//            	System.out.println("  ["  + trace.getName() + "] rank: " + context.getRank() + ", timestamp: " + event.getTimestamp());
//            	assert (checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0);
//        	}
//        }
//	}

    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal signal) {
		fCurrentExperiment = signal.getExperiment();
//    	if (signal.getExperiment() == this) {
//    		indexExperiment(true);
//    	}
    }

    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
//    	indexExperiment(true);
    }

    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
    	// TODO: Incremental index update
    	synchronized(this) {
    		updateNbEvents();
    		updateTimeRange();
    	}
		broadcast(new TmfExperimentUpdatedSignal(this, this, signal.getTrace()));
    }

}
