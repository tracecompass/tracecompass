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
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTraceUpdatedSignal;
import org.eclipse.linuxtools.tmf.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.trace.ITmfLocation;
import org.eclipse.linuxtools.tmf.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.trace.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.trace.TmfContext;

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
    private static TmfExperiment<?> fCurrentExperiment = null;

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
    	super(id, type);

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
    
    
    public TmfExperiment(TmfExperiment<T> other) {
    	super(other.getName(), other.fType);
    	
    	fEpoch         = other.fEpoch;
    	fIndexPageSize = other.fIndexPageSize;
    	
    	fTraces = new ITmfTrace[other.fTraces.length];
    	for (int trace = 0; trace < other.fTraces.length; trace++) {
    		fTraces[trace] = other.fTraces[trace].createTraceCopy();
    	}
    	
    	fNbEvents  = other.fNbEvents;
    	fTimeRange = other.fTimeRange;
    }
    
	public TmfExperiment<T> createTraceCopy() {
		return new TmfExperiment<T>(this);
	}
    
    /**
     * Clears the experiment
     */
    @Override
	public void dispose() {
    	for (ITmfTrace trace : fTraces) {
    		trace.dispose();
    	}
    	fTraces = null;
    	fCheckpoints.clear();
        super.dispose();
    }

    private static void setCurrentExperiment(TmfExperiment<?> experiment) {
    	fCurrentExperiment = experiment;
    }

    // ------------------------------------------------------------------------
    // ITmfTrace
    // ------------------------------------------------------------------------

	public String getPath() {
		return null;
	}

	public long getNbEvents() {
		return fNbEvents;
	}

    public int getCacheSize() {
        return fIndexPageSize;
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

    public Vector<TmfCheckpoint> getCheckpoints() {
    	return fCheckpoints;
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
     * @param timestamp
     * @return
     */
    public long getRank(TmfTimestamp timestamp) {
    	TmfExperimentContext context = seekEvent(timestamp);
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
    	TmfExperimentContext context = seekEvent(index);
    	TmfEvent event = getNextEvent(context);
    	return (event != null) ? event.getTimestamp() : null;
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

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
			if (trace.getNbEvents() > 0) {
	    		TmfTimestamp traceStartTime = trace.getStartTime();
	    		if (traceStartTime.compareTo(startTime, true) < 0)
	    			startTime = traceStartTime;

	    		TmfTimestamp traceEndTime = trace.getEndTime();
	    		if (traceEndTime.compareTo(endTime, true) > 0)
	    			endTime = traceEndTime;
			}
    	}
		fTimeRange = new TmfTimeRange(startTime, endTime);
    }

    // ------------------------------------------------------------------------
    // TmfProvider
    // ------------------------------------------------------------------------

	@Override
	public ITmfContext armRequest(ITmfDataRequest<T> request) {
		TmfTimestamp timestamp = (request instanceof ITmfEventRequest<?>) ?
				((ITmfEventRequest<T>) request).getRange().getStartTime() : null;
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
	// and initializes the event queues
	public TmfExperimentContext seekLocation(ITmfLocation<?> location) {

		// Validate the location
		if (location != null && !(location instanceof TmfExperimentLocation)) {
			return null;	// Throw an exception?
		}

		// Instantiate the location
		TmfExperimentLocation expLocation = (location == null)
		    ? new TmfExperimentLocation(new ITmfLocation<?>[fTraces.length], new long[fTraces.length])
            : (TmfExperimentLocation) location.clone();

		// Create and populate the context's traces contexts
		TmfExperimentContext context = new TmfExperimentContext(fTraces, new TmfContext[fTraces.length]);
		long rank = 0;
		for (int i = 0; i < fTraces.length; i++) {
			// Get the relevant trace attributes
			ITmfLocation<?> traceLocation = expLocation.getLocation()[i];
			long traceRank = expLocation.getRanks()[i];

			// Set the corresponding sub-context
			context.getContexts()[i] = fTraces[i].seekLocation(traceLocation);
			context.getContexts()[i].setRank(traceRank);
			rank += traceRank;

			// Set the trace location and read the corresponding event
			expLocation.getLocation()[i] = context.getContexts()[i].getLocation();
			context.getEvents()[i] = fTraces[i].getNextEvent(context.getContexts()[i]);
		}

		// Finalize context
		context.setLocation(expLocation);
		context.setRank(rank);
		context.setLastTrace(TmfExperimentContext.NO_TRACE);
		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
	 */
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

        TmfExperimentContext context = seekLocation(location);
        context.setRank((long) index * fIndexPageSize);

        // And locate the event
        TmfExperimentContext nextEventContext = new TmfExperimentContext(context);
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
            context = new TmfExperimentContext(nextEventContext);
        	event = getNextEvent(nextEventContext);
        }
    	context.setLastTrace(TmfExperimentContext.NO_TRACE);

        return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(long)
	 */
	public TmfExperimentContext seekEvent(long rank) {

        // Position the stream at the previous checkpoint
        int index = (int) rank / fIndexPageSize;
        ITmfLocation<?> location;
        synchronized (fCheckpoints) {
        	if (fCheckpoints.size() == 0) {
        		location = null;
        	}
        	else {
        		if (index >= fCheckpoints.size()) {
        			index  = fCheckpoints.size() - 1;
        		}
        		location = fCheckpoints.elementAt(index).getLocation();
        	}
        }

        TmfExperimentContext context = seekLocation(location);
        long pos = (long) index * fIndexPageSize;
        context.setRank(pos);

        // And locate the event
        TmfExperimentContext nextEventContext = new TmfExperimentContext(context);
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && pos++ < rank) {
        	event = getNextEvent(nextEventContext);
        	context = new TmfExperimentContext(nextEventContext);
        	if (event != null) context.updateRank(-1);
        }
    	context.setLastTrace(TmfExperimentContext.NO_TRACE);

        return context;
	}

	/**
	 * Scan the next events from all traces and return the next one
	 * in chronological order.
	 * 
	 * @param context
	 * @return
	 */
	public synchronized TmfEvent getNextEvent(TmfContext context) {

		// Validate the context
		if (!(context instanceof TmfExperimentContext)) {
			return null;	// Throw an exception?
		}

		TmfExperimentContext expContext = (TmfExperimentContext) context;

		// If an event was consumed previously, get the next one from that trace
		int lastTrace = expContext.getLastTrace();
		if (lastTrace != TmfExperimentContext.NO_TRACE) {
		    TmfContext traceContext = expContext.getContexts()[lastTrace];
			expContext.getEvents()[lastTrace] = expContext.getTraces()[lastTrace].getNextEvent(traceContext);
		}

		// Scan the candidate events and identify the "next" trace to read from 
		int trace = TmfExperimentContext.NO_TRACE;
		TmfTimestamp timestamp = TmfTimestamp.BigCrunch;
		for (int i = 0; i < expContext.getTraces().length; i++) {
			TmfEvent event = expContext.getEvents()[i];
			if (event != null && event.getTimestamp() != null) {
				TmfTimestamp otherTS = event.getTimestamp();
				if (otherTS.compareTo(timestamp, true) < 0) {
					trace = i;
					timestamp = otherTS;
				}
			}
		}

		// Update the experiment context and set the "next" event
		TmfEvent event = null;
		if (trace >= 0) {
			expContext.setLastTrace(trace);
			expContext.updateRank(1);
			TmfExperimentLocation expLocation = (TmfExperimentLocation) expContext.getLocation();
            TmfContext traceContext = expContext.getContexts()[trace];
			expLocation.getLocation()[trace] = traceContext.getLocation().clone();
			expLocation.getRanks()[trace] = traceContext.getRank();
			event = expContext.getEvents()[trace];
		}

		return event;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#parseEvent(org.eclipse.linuxtools.tmf.trace.TmfContext)
	 */
	public TmfEvent parseEvent(TmfContext context) {
		
		if (context instanceof TmfExperimentContext) {
			TmfExperimentContext expContext = (TmfExperimentContext) context;
			int lastTrace = expContext.getLastTrace();
			if (lastTrace != -1) {
				TmfContext traceContext = expContext.getContexts()[lastTrace];
				expContext.getEvents()[lastTrace] = expContext.getTraces()[lastTrace].getNextEvent(traceContext);
				expContext.updateRank(1);
				TmfExperimentLocation expLocation = (TmfExperimentLocation) expContext.getLocation();
				expLocation.getLocation()[lastTrace] = traceContext.getLocation().clone();
			}

			int trace = -1;
			TmfTimestamp timestamp = TmfTimestamp.BigCrunch;
			for (int i = 0; i < expContext.getTraces().length; i++) {
				if (expContext.getEvents()[i] != null) {
					if (expContext.getEvents()[i].getTimestamp() != null) {
						TmfTimestamp otherTS = expContext.getEvents()[i].getTimestamp();
						if (otherTS.compareTo(timestamp, true) < 0) {
							trace = i;
							timestamp = otherTS;
						}
					}
				}
			}
			if (trace >= 0) {
				expContext.setLastTrace(TmfExperimentContext.NO_TRACE);
				return expContext.getEvents()[trace];
			}
		}

		return null;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[TmfExperiment (" + getName() + ")]";
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

		synchronized(this) {
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

		job = new IndexingJob(new TmfExperiment<T>(this));
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

		private final String          experimentId;
		private final ITmfTrace[]     traces;
		private Vector<TmfCheckpoint> checkpoints;

		public IndexingJob(TmfExperiment<T> experiment) {
			super(experiment.getName());
			experimentId = experiment.getName();
			traces = experiment.getTraces();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {

			// Minimal check
			if (traces.length == 0) {
	            fIndexing = false;
				return Status.OK_STATUS;
			}

			monitor.beginTask("Indexing " + experimentId, IProgressMonitor.UNKNOWN);

            int nbEvents = 0;
            TmfTimestamp startTime = null;
            TmfTimestamp lastTime  = null;

            // Reset the index
            checkpoints = new Vector<TmfCheckpoint>();
            
            try {
            	// Position the trace at the beginning
            	TmfExperimentContext context = seekLocation(null);
            	TmfExperimentLocation location = (TmfExperimentLocation) context.getLocation().clone();

                // Get the first event
               	TmfEvent event = getNextEvent(context);
               	if (event != null) {
                    startTime = new TmfTimestamp(event.getTimestamp());
               	}

               	// Index the experiment
               	while (event != null) {
                	lastTime = event.getTimestamp();
           			if ((nbEvents++ % fIndexPageSize) == 0) {
           				checkpoints.add(new TmfCheckpoint(lastTime, location));

                        monitor.worked(1);

                        // Check monitor *after* fCheckpoints has been updated
                        if (monitor.isCanceled()) {
                            monitor.done();
                        	return Status.CANCEL_STATUS;
                        }
                    }

                    // We will need the contexts at the next iteration
                    if ((nbEvents % fIndexPageSize) == 0) {
                        location = (TmfExperimentLocation) context.getLocation().clone();
           			}

           			event = getNextEvent(context);
                }

            }
            finally {
                synchronized(this) {
                	fCheckpoints = checkpoints;
                	fNbEvents    = nbEvents;
                	fTimeRange   = new TmfTimeRange(startTime, lastTime);
                	fIndexing    = false;
                	fIndexed     = true;
                }
//       		    notifyListeners(fTimeRange);
                monitor.done();
            }

            return Status.OK_STATUS;
		}
    }

    protected void notifyListeners(TmfTimeRange range) {
    	broadcast(new TmfRangeSynchSignal(this, range, range.getStartTime()));
	}
   
    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<T> signal) {
    	TmfExperiment<?> experiment = signal.getExperiment();
    	if (experiment == this) {
    		setCurrentExperiment(experiment);
    	}
    	else {
    		dispose();
    	}
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
