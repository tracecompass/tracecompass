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

import java.util.Collections;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.trace.TmfContext;
import org.eclipse.linuxtools.tmf.trace.TmfTraceUpdatedSignal;

/**
 * <b><u>TmfExperiment</u></b>
 * <p>
 * TmfExperiment presents a time-ordered, unified view of a set of TmfTraces
 * that are part of a tracing experiment. 
 * <p>
 */
public class TmfExperiment<T extends TmfEvent> extends TmfEventProvider<T> implements ITmfTrace {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	// The currently selected experiment
    private static TmfExperiment<?> fCurrentExperiment;

	// The experiment ID
    private String fExperimentId;

    // The set of traces that constitute the experiment
    private ITmfTrace[] fTraces;

    // The total number of events
    private long fNbEvents;

    // The experiment time range
    private TmfTimeRange fTimeRange;

    // The experiment reference timestamp (default: BigBang)
    private TmfTimestamp fEpoch;

	// The experiment index
	private Vector<TmfCheckpoint> fCheckpoints = new Vector<TmfCheckpoint>();

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
    	fTraces = traces;
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
    	fTraces = null;
    	fCheckpoints.clear();
    	fCurrentExperiment= null;
        super.deregister();
    }

    // ------------------------------------------------------------------------
    // ITmfTrace accessors
    // ------------------------------------------------------------------------

	public String getPath() {
		return null;
	}

	@Override
	public String getName() {
		return fExperimentId;
	}

	public long getNbEvents() {
		return fNbEvents;
	}

	public TmfTimeRange getTimeRange() {
		return fTimeRange;
	}

	public TmfTimestamp getStartTime() {
		return fTimeRange.getStartTime();
	}

	public TmfTimestamp getEndTime() {
		return fTimeRange.getEndTime();
	}

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public static TmfExperiment<?> getCurrentExperiment() {
    	return fCurrentExperiment;
    }

    public TmfTimestamp getEpoch() {
    	return fEpoch;
    }

    public ITmfTrace[] getTraces() {
    	return fTraces;
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
    	ITmfTrace trace = fTraces[0];
    	TmfContext context = trace.seekEvent(ts);
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
    	ITmfTrace trace = fTraces[0];
    	TmfContext context = trace.seekEvent(index);
    	TmfEvent event = trace.getNextEvent(context);
    	TmfTimestamp timestamp = (event != null) ? event.getTimestamp() : null;
    	return timestamp;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

//    /**
//     * Add a trace to the experiment trace set
//     * 
//     * @param trace
//     */
//    public void addTrace(ITmfTrace trace) {
//		fTraces.add(trace);
//		synchronized(this) {
//			updateNbEvents();
//			updateTimeRange();
//		}
//    }

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
	public ITmfContext armRequest(TmfDataRequest<T> request) {
		TmfTimestamp timestamp = (request instanceof TmfEventRequest<?>) ?
			((TmfEventRequest<T>) request).getRange().getStartTime() : null;

		TmfExperimentContext context = (timestamp != null) ? 
			seekEvent(timestamp) : seekEvent(request.getIndex());

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

    // ------------------------------------------------------------------------
    // ITmfTrace trace positioning
    // ------------------------------------------------------------------------

	// Returns a brand new context based on the location provided
	// Arms the event queues
	// NOTE: This is a fine example of pathological coupling...
	public TmfExperimentContext seekLocation(ITmfLocation<?> location) {
		if (location instanceof TmfExperimentLocation || location == null) {
			ITmfLocation<?>[] oldloc = (location != null) ? ((TmfExperimentLocation) location).getLocation() : new TmfExperimentLocation[fTraces.length];
			ITmfLocation<?>[] newloc = new ITmfLocation[fTraces.length];
			TmfContext[] contexts = new TmfContext[fTraces.length];

			TmfExperimentContext context = new TmfExperimentContext(fTraces, contexts);
			TmfEvent[] events = context.getEvents();

			long rank = 0;
			for (int i = 0; i < fTraces.length; i++) {
				contexts[i] = fTraces[i].seekLocation(oldloc[i]);
				newloc[i]   = contexts[i].getLocation();	// No clone here
				events[i]   = fTraces[i].parseEvent(contexts[i]);
				rank += contexts[i].getRank();
			}
			context.setLocation(new TmfExperimentLocation(newloc));
			context.setRank(rank);
			return context;
		}
		return null;
	}

	public TmfExperimentContext seekEvent(TmfTimestamp timestamp) {

		if (timestamp == null) {
    		timestamp = TmfTimestamp.BigBang;
    	}

    	// First, find the right checkpoint
    	int index = Collections.binarySearch(fCheckpoints, new TmfCheckpoint(timestamp, null));

        // In the very likely case that the checkpoint was not found, bsearch
        // returns its negated would-be location (not an offset...). From that
        // index, we can then position the stream and get the event.
        if (index < 0) {
            index = Math.max(0, -(index + 2));
        }

        // Position the experiment at the checkpoint
        ITmfLocation<?> location;
        synchronized (fCheckpoints) {
        	if (fCheckpoints.size() > 0) {
        		if (index >= fCheckpoints.size()) {
        			index = fCheckpoints.size() - 1;
        		}
        		location = fCheckpoints.elementAt(index).getLocation();
        	}
        	else {
        		location = null;
        	}
        }

        TmfExperimentContext nextEventContext = seekLocation(location);
        nextEventContext.setRank(index * fIndexPageSize);
        TmfExperimentContext currentEventContext = new TmfExperimentContext(nextEventContext);

        // And get the event
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
            currentEventContext = new TmfExperimentContext(nextEventContext);
//        	currentEventContext.setLocation(nextEventContext.getLocation());
//        	currentEventContext.updateRank(1);
        	event = getNextEvent(nextEventContext);
        }

        return currentEventContext;
	}

	public TmfExperimentContext seekEvent(long rank) {

		TmfExperimentContext context;
		int page = 0;		// The checkpoint page
		int current = 0;	// The current event index (rank)
		
		// If there is no checkpoint created yet, start from the beginning
		if (fCheckpoints.size() == 0) {
			context = seekLocation(null);
		}
		else {
			page = (int) rank / fIndexPageSize;
			if (page >= fCheckpoints.size()) {
				page = fCheckpoints.size() - 1;
			}
			context = seekLocation(fCheckpoints.elementAt(page).getLocation());
			current = page * fIndexPageSize;
		}

		// Position the traces at the requested index
		while (current++ < rank) {
			getNextEvent(context);
		}

		return context;
	}

//	/**
//	 * Given an experiment event index, position the set of traces so a call
//	 * to getNextEvent() will retrieve the corresponding event.
//	 * 
//	 * @param index
//	 * @param context
//	 */
//	private synchronized void positionTraces(long index, TmfExperimentContext context) {
//
//		// Extract the relevant information
//		ITmfTrace[] traces = context.getTraces();
//		TmfEvent[]  events = context.getEvents();
//		TmfContext[] contexts = context.getContexts();
//
//		int page = 0;		// The checkpoint page
//		int current = 0;	// The current event index (rank)
//
//		// If there is no checkpoint created yet, start from the beginning
//		if (fCheckpoints.size() == 0) {
//			for (int i = 0; i < contexts.length; i++) {
//				contexts[i] = traces[i].seekLocation(null).clone();
//				events[i] = traces[i].parseEvent(contexts[i]);
//			}
//		}
//		else {
//			page = (int) index / fIndexPageSize;
//			if (page >= fCheckpoints.size()) {
//				page = fCheckpoints.size() - 1;
//			}
//
////			TmfContext[] checkpoint = fCheckpoints.elementAt(page).getContexts();
//			for (int i = 0; i < contexts.length; i++) {
//				contexts[i] = checkpoint[i].clone();
//				events[i] = traces[i].parseEvent(contexts[i]);
//			}
//			current = page * fIndexPageSize;
//		}
//
//		// Position the traces at the requested index
//		while (current++ < index) {
//			getNextEvent(context);
//		}
//	}

	/**
	 * Scan the next events from all traces and return the next one
	 * in chronological order.
	 * 
	 * @param context
	 * @return
	 */
	public synchronized TmfEvent getNextEvent(TmfContext context) {
		if (context instanceof TmfExperimentContext) {
			TmfExperimentContext expContext = (TmfExperimentContext) context;
			int trace = 0;
			TmfTimestamp timestamp = TmfTimestamp.BigCrunch;
			if (expContext.getEvents()[trace] != null) {
				timestamp = expContext.getEvents()[trace].getTimestamp();
			}
			for (int i = 1; i < expContext.getTraces().length; i++) {
				if (expContext.getEvents()[i].getTimestamp() != null) {
					TmfTimestamp otherTS = expContext.getEvents()[i].getTimestamp();
					if (otherTS.compareTo(timestamp, true) < 0) {
						trace = i;
						timestamp = otherTS;
					}
				}
			}
			TmfContext trcloc = expContext.getContexts()[trace];
			TmfEvent event = expContext.getTraces()[trace].parseEvent(trcloc);
			TmfExperimentLocation exploc = (TmfExperimentLocation) expContext.getLocation();
			exploc.getLocation()[trace] = trcloc.getLocation().clone();
			expContext.updateRank(1);
			expContext.getEvents()[trace] = expContext.getTraces()[trace].getNextEvent(trcloc);
			return event;
		}
    	return null;
	}

	//	public TmfEvent getNextEvent(TmfExperimentContext context) {
//		// TODO: Consider the time adjustment
//		int trace = 0;
//		TmfTimestamp timestamp = TmfTimestamp.BigCrunch;
//		if (context.getEvents()[trace] != null) {
//			timestamp = context.getEvents()[trace].getTimestamp();
//		}
//		for (int i = 1; i < context.getTraces().length; i++) {
//			if (context.getEvents()[i].getTimestamp() != null) {
//				TmfTimestamp otherTS = context.getEvents()[i].getTimestamp();
//				if (otherTS.compareTo(timestamp, true) < 0) {
//					trace = i;
//					timestamp = otherTS;
//				}
//			}
//		}
//		TmfEvent event = context.getTraces()[trace].getNextEvent(context.getContexts()[trace]);
//		context.getEvents()[trace] = context.getTraces()[trace].parseEvent(context.getContexts()[trace]);
//		return event;
//	}

	public TmfEvent parseEvent(TmfContext context) {
		// TODO Auto-generated method stub
		return null;
	}

//	/**
//	 * Scan the next events from all traces and return the next one
//	 * in chronological order.
//	 * 
//	 * @param context
//	 * @return
//	 */
//	private TmfTimestamp getNextEventTimestamp(TmfExperimentContext context) {
//		// TODO: Consider the time adjustment
//		int trace = 0;
//		TmfTimestamp timestamp = TmfTimestamp.BigCrunch;
//		if (context.getEvents()[trace] != null) {
//			timestamp = context.getEvents()[trace].getTimestamp();
//		}
//		for (int i = 1; i < context.getTraces().length; i++) {
//			if (context.getEvents()[i].getTimestamp() != null) {
//				TmfTimestamp otherTS = context.getEvents()[i].getTimestamp();
//				if (otherTS.compareTo(timestamp, true) < 0) {
//					trace = i;
//					timestamp = otherTS;
//				}
//			}
//		}
//		return timestamp;
//	}

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
	 * it must be possible to request the Nth event of the experiment.
	 * 
	 * The purpose of the index is to keep the information needed to rapidly
	 * restore the traces contexts at regular intervals (every INDEX_PAGE_SIZE
	 * event).
	 */

	// The index page size
	private static final int DEFAULT_INDEX_PAGE_SIZE = 1000;
	private final int fIndexPageSize;

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
			if (fTraces.length == 0) {
	            fIndexing = false;
				return Status.OK_STATUS;
			}

			monitor.beginTask("Indexing " + fExperimentId, IProgressMonitor.UNKNOWN);

            int nbEvents = 0;
            TmfTimestamp startTime = null;
            TmfTimestamp lastTime  = null;

            // Reset the index
            fCheckpoints = new Vector<TmfCheckpoint>();
            
            try {
            	// Position the trace at the beginning
            	TmfExperimentContext context = seekLocation(null);
            	TmfExperimentLocation location = (TmfExperimentLocation) context.getLocation();

                // Get the first event
               	TmfEvent event = getNextEvent(context);
               	if (event != null) {
                    startTime = new TmfTimestamp(event.getTimestamp());
               	}

               	// Index the experiment
               	while (event != null) {
                	lastTime = event.getTimestamp();
           			if ((nbEvents++ % fIndexPageSize) == 0) {
           				fCheckpoints.add(new TmfCheckpoint(lastTime, location.clone()));
                   		fNbEvents = nbEvents;
                   		fTimeRange = new TmfTimeRange(startTime, lastTime);
                   		notifyListeners(new TmfTimeRange(startTime, lastTime));

                        monitor.worked(1);

                        // Check monitor *after* fCheckpoints has been updated
                        if (monitor.isCanceled()) {
                            monitor.done();
                        	return Status.CANCEL_STATUS;
                        }
                    }

                    // We will need the contexts at the next iteration
                    if ((nbEvents % fIndexPageSize) == 0) {
                        location = (TmfExperimentLocation) context.getLocation();
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

//            dumpCheckpoints();

            return Status.OK_STATUS;
		}
    }

    protected void notifyListeners(TmfTimeRange range) {
    	broadcast(new TmfRangeSynchSignal(this, range, null));
	}
   
	// ========================================================================
	// Toubleshooting code
	// ========================================================================

//	private void dumpCheckpoints() {
//		System.out.println("-----");
//		System.out.println("Checkpoints of " + fExperimentId);
//		for (int i = 0; i < fCheckpoints.size(); i++) {
//        	System.out.println("Entry:" + i);
//        	TmfCheckpoint checkpoint = fCheckpoints.get(i);
//        	long rank = 0; 
//        	for (int j = 0; j < fTraces.length; j++) {
//        		ITmfTrace trace = fTraces[j];
//            	TmfExperimentContext context = seekLocation(checkpoint.getLocation());
//            	TmfContext[] traces = context.getContexts();
//        		rank += context.getRank(); 
//            	TmfEvent event = fTraces[j].getNextEvent(new TmfContext(traces[j]));
//            	System.out.println("  ["  + trace.getName() + "] rank: " + context.getRank() + ", timestamp: " + event.getTimestamp());
//            	assert (checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0);
//        	}
//        	System.out.println("Sum of ranks: " + rank + " (expected: " + i * fIndexPageSize + ")");
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
