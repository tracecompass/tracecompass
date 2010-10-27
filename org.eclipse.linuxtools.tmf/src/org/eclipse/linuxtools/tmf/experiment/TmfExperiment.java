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

import org.eclipse.linuxtools.tmf.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentSelectedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfExperimentUpdatedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;
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
    protected static TmfExperiment<?> fCurrentExperiment = null;

	// The set of traces that constitute the experiment
    protected ITmfTrace[] fTraces;

    // The total number of events
    protected long fNbEvents;

    // The experiment time range
    protected TmfTimeRange fTimeRange;

    // The experiment reference timestamp (default: Zero)
    protected TmfTimestamp fEpoch;

	// The experiment index
    protected Vector<TmfCheckpoint> fCheckpoints = new Vector<TmfCheckpoint>();

    // The current experiment context
    protected TmfExperimentContext fExperimentContext;
	
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
        this(type, id, traces, TmfTimestamp.Zero, indexPageSize, false);
	}

    public TmfExperiment(Class<T> type, String id, ITmfTrace[] traces, TmfTimestamp epoch, int indexPageSize, boolean preIndexExperiment) {
    	super(id, type);

    	fTraces = traces;
    	fEpoch = epoch;
    	fIndexPageSize = indexPageSize;

    	if (preIndexExperiment) indexExperiment(true);

		updateTimeRange();
	}

    protected TmfExperiment(String id, Class<T> type) {
        super(id, type);
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
     * Copy constructor
     * @param other
     */
    public TmfExperiment(TmfExperiment<T> other) {
    	super(other.getName() + "(clone)", other.fType);
    	
    	fEpoch         = other.fEpoch;
    	fIndexPageSize = other.fIndexPageSize;
    	
    	fTraces = new ITmfTrace[other.fTraces.length];
    	for (int trace = 0; trace < other.fTraces.length; trace++) {
    		fTraces[trace] = other.fTraces[trace].createTraceCopy();
    	}
    	
    	fNbEvents  = other.fNbEvents;
    	fTimeRange = other.fTimeRange;
    }
    
	@Override
	public TmfExperiment<T> createTraceCopy() {
		TmfExperiment<T> experiment = new TmfExperiment<T>(this);
		TmfSignalManager.deregister(experiment);
		return experiment;
	}
    
    /**
     * Clears the experiment
     */
    @Override
	public synchronized void dispose() {
    	if (fTraces != null) {
    		for (ITmfTrace trace : fTraces) {
    			trace.dispose();
    		}
    		fTraces = null;
    	}
    	if (fCheckpoints != null) {
    		fCheckpoints.clear();
    	}
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // ITmfTrace
    // ------------------------------------------------------------------------

	@Override
	public String getPath() {
		return null;
	}

	@Override
	public long getNbEvents() {
		return fNbEvents;
	}

    @Override
	public int getCacheSize() {
        return fIndexPageSize;
    }

	@Override
	public TmfTimeRange getTimeRange() {
		return fTimeRange;
	}

	@Override
	public TmfTimestamp getStartTime() {
		return fTimeRange.getStartTime();
	}

	@Override
	public TmfTimestamp getEndTime() {
		return fTimeRange.getEndTime();
	}

    public Vector<TmfCheckpoint> getCheckpoints() {
    	return fCheckpoints;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    private static void setCurrentExperiment(TmfExperiment<?> experiment) {
    	fCurrentExperiment = experiment;
    }

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
    @Override
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
	public ITmfContext armRequest(ITmfDataRequest<T> request) {
//		Tracer.trace("Ctx: Arming request - start");
		TmfTimestamp timestamp = (request instanceof ITmfEventRequest<?>) ?
			((ITmfEventRequest<T>) request).getRange().getStartTime() : null;
		TmfExperimentContext context = (timestamp != null) ? 
			seekEvent(timestamp) : seekEvent(request.getIndex());
//		Tracer.trace("Ctx: Arming request - done");
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
	@Override
	public synchronized TmfExperimentContext seekLocation(ITmfLocation<?> location) {

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
//		Tracer.trace("Ctx: SeekLocation - start");

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

//		Tracer.trace("Ctx: SeekLocation - done");

		// Finalize context
		context.setLocation(expLocation);
		context.setLastTrace(TmfExperimentContext.NO_TRACE);
		context.setRank(rank);

		fExperimentContext = context;

		return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
	 */
	@Override
	public synchronized TmfExperimentContext seekEvent(TmfTimestamp timestamp) {

//		Tracer.trace("Ctx: seekEvent(TS) - start");

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
        TmfEvent event = parseEvent(context);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
        	getNextEvent(context);
        	event = parseEvent(context);
        }

        if (event == null) {
        	context.setLocation(null);
        	context.setRank(ITmfContext.UNKNOWN_RANK);
        }

        return context;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(long)
	 */
	@Override
	public synchronized TmfExperimentContext seekEvent(long rank) {

//		Tracer.trace("Ctx: seekEvent(rank) - start");

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
        context.setRank((long) index * fIndexPageSize);

		// And locate the event
        TmfEvent event = parseEvent(context);
        long pos = context.getRank();
        while (event != null && pos++ < rank) {
        	getNextEvent(context);
        	event = parseEvent(context);
        }

        if (event == null) {
        	context.setLocation(null);
        	context.setRank(ITmfContext.UNKNOWN_RANK);
        }

		return context;
	}

	/**
	 * Scan the next events from all traces and return the next one
	 * in chronological order.
	 * 
	 * @param context
	 * @return
	 */

//	private void dumpContext(TmfExperimentContext context, boolean isBefore) {

//		TmfContext context0 = context.getContexts()[0];
//		TmfEvent   event0   = context.getEvents()[0];
//		TmfExperimentLocation location0 = (TmfExperimentLocation) context.getLocation();
//		long       rank0    = context.getRank();
//		int        trace    = context.getLastTrace();
//
//		StringBuffer result = new StringBuffer("Ctx: " + (isBefore ? "B " : "A "));
//		
//		result.append("[Ctx: fLoc= " + context0.getLocation().toString() + ", fRnk= " + context0.getRank() + "] ");
//		result.append("[Evt: " + event0.getTimestamp().toString() + "] ");
//		result.append("[Loc: fLoc= " + location0.getLocation()[0].toString() + ", fRnk= " + location0.getRanks()[0] + "] ");
//		result.append("[Rnk: " + rank0 + "], [Trc: " + trace + "]");
//		Tracer.trace(result.toString());
//	}
	
	@Override
	public synchronized TmfEvent getNextEvent(TmfContext context) {

		// Validate the context
		if (!(context instanceof TmfExperimentContext)) {
			return null;	// Throw an exception?
		}

		if (!context.equals(fExperimentContext)) {
//    		Tracer.trace("Ctx: Restoring context");
			seekLocation(context.getLocation());
		}
		
		TmfExperimentContext expContext = (TmfExperimentContext) context;

//		dumpContext(expContext, true);

		// If an event was consumed previously, get the next one from that trace
		int lastTrace = expContext.getLastTrace();
		if (lastTrace != TmfExperimentContext.NO_TRACE) {
		    TmfContext traceContext = expContext.getContexts()[lastTrace];
			expContext.getEvents()[lastTrace] = expContext.getTraces()[lastTrace].getNextEvent(traceContext);
			expContext.setLastTrace(TmfExperimentContext.NO_TRACE);
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
		if (trace != TmfExperimentContext.NO_TRACE) {
	        updateIndex(expContext, timestamp);

	        TmfContext traceContext = expContext.getContexts()[trace];
	        TmfExperimentLocation expLocation = (TmfExperimentLocation) expContext.getLocation();
	        expLocation.getLocation()[trace] = traceContext.getLocation().clone();

//	        updateIndex(expContext, timestamp);

	        expLocation.getRanks()[trace] = traceContext.getRank();
			expContext.setLastTrace(trace);
			expContext.updateRank(1);
			event = expContext.getEvents()[trace];
		}

//		if (event != null) {
//    		Tracer.trace("Exp: " + (expContext.getRank() - 1) + ": " + event.getTimestamp().toString());
//    		dumpContext(expContext, false);
//    		Tracer.trace("Ctx: Event returned= " + event.getTimestamp().toString());
//		}

		return event;
	}

	public synchronized void updateIndex(ITmfContext context, TmfTimestamp timestamp) {
		// Build the index as we go along
		long rank = context.getRank();
		if (context.isValidRank() && (rank % fIndexPageSize) == 0) {
			// Determine the table position
			long position = rank / fIndexPageSize;
			// Add new entry at proper location (if empty) 
			if (fCheckpoints.size() == position) {
				ITmfLocation<?> location = context.getLocation().clone();
				fCheckpoints.add(new TmfCheckpoint(timestamp.clone(), location));
//				System.out.println(this + "[" + (fCheckpoints.size() - 1) + "] " + timestamp + ", " + location.toString());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#parseEvent(org.eclipse.linuxtools.tmf.trace.TmfContext)
	 */
	@Override
	public TmfEvent parseEvent(TmfContext context) {
		
		// Validate the context
		if (!(context instanceof TmfExperimentContext)) {
			return null;	// Throw an exception?
		}

		if (!context.equals(fExperimentContext)) {
//    		Tracer.trace("Ctx: Restoring context");
			seekLocation(context.getLocation());
		}
		
		TmfExperimentContext expContext = (TmfExperimentContext) context;

		// If an event was consumed previously, get the next one from that trace
		int lastTrace = expContext.getLastTrace();
		if (lastTrace != TmfExperimentContext.NO_TRACE) {
		    TmfContext traceContext = expContext.getContexts()[lastTrace];
			expContext.getEvents()[lastTrace] = expContext.getTraces()[lastTrace].getNextEvent(traceContext);
			expContext.setLastTrace(TmfExperimentContext.NO_TRACE);
	        fExperimentContext = (TmfExperimentContext) context;
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

		TmfEvent event = null;
		if (trace != TmfExperimentContext.NO_TRACE) {
			event = expContext.getEvents()[trace];
		}

		return event;
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
	private static final int DEFAULT_INDEX_PAGE_SIZE = 5000;
	protected int fIndexPageSize;

//	private static BufferedWriter fEventLog = null;
//	private static BufferedWriter openLogFile(String filename) {
//		BufferedWriter outfile = null;
//		try {
//			outfile = new BufferedWriter(new FileWriter(filename));
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//		return outfile;
//	}

	@SuppressWarnings("unchecked")
	private void indexExperiment(boolean waitForCompletion) {

		fCheckpoints.clear();
		
//		fEventLog = openLogFile("TraceEvent.log");
//		System.out.println(System.currentTimeMillis() + ": Experiment indexing started");

		ITmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, TmfTimeRange.Eternity,
				TmfDataRequest.ALL_DATA, 1, ITmfDataRequest.ExecutionType.BACKGROUND) {

//			long indexingStart = System.nanoTime();
			
			TmfTimestamp startTime =  null;
			TmfTimestamp lastTime  =  null;

			@Override
			public void handleData(TmfEvent event) {
        		super.handleData(event);
				if (event != null) {
					TmfTimestamp ts = event.getTimestamp();
					if (startTime == null)
						startTime = new TmfTimestamp(ts);
					lastTime = new TmfTimestamp(ts);

					if ((getNbRead() % DEFAULT_INDEX_PAGE_SIZE) == 0) {
						updateExperiment();
					}
				}
			}

			@Override
			public void handleSuccess() {
//				long indexingEnd = System.nanoTime();

				updateExperiment();
//				System.out.println(System.currentTimeMillis() + ": Experiment indexing completed");

//				long average = (indexingEnd - indexingStart) / fNbEvents;
//				System.out.println(getName() + ": start=" + startTime + ", end=" + lastTime + ", elapsed=" + (indexingEnd * 1.0 - indexingStart) / 1000000000);
//				System.out.println(getName() + ": nbEvents=" + fNbEvents + " (" + (average / 1000) + "." + (average % 1000) + " us/evt)");
			}

			private void updateExperiment() {
				int nbRead = getNbRead();
				if (nbRead != 0) {
//					updateTimeRange();
//					updateNbEvents();
					fTimeRange = new TmfTimeRange(startTime, new TmfTimestamp(lastTime));
					fNbEvents  = nbRead;
					notifyListeners();
				}
			}
		};

		sendRequest((ITmfDataRequest<T>) request);
		if (waitForCompletion)
			try {
				request.waitForCompletion();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
	
	protected void notifyListeners() {
    	broadcast(new TmfExperimentUpdatedSignal(this, this)); // , null));
	}
   
    // ------------------------------------------------------------------------
    // Signal handlers
    // ------------------------------------------------------------------------

    @TmfSignalHandler
    public void experimentSelected(TmfExperimentSelectedSignal<T> signal) {
    	TmfExperiment<?> experiment = signal.getExperiment();
    	if (experiment == this) {
    		setCurrentExperiment(experiment);
        	indexExperiment(false);
    	}
    	else {
    		dispose();
    	}
    }

    @TmfSignalHandler
    public void experimentUpdated(TmfExperimentUpdatedSignal signal) {
    }

    @TmfSignalHandler
    public void traceUpdated(TmfTraceUpdatedSignal signal) {
    	// TODO: Incremental index update
    	synchronized(this) {
    		updateNbEvents();
    		updateTimeRange();
    	}
		broadcast(new TmfExperimentUpdatedSignal(this, this)); // , signal.getTrace()));
    }

    // ------------------------------------------------------------------------
    // TmfDataProvider
    // ------------------------------------------------------------------------

	@Override
	protected void queueBackgroundRequest(final ITmfDataRequest<T> request, final int blockSize, final boolean indexing) {

		// TODO: Handle the data requests also...
		if (!(request instanceof ITmfEventRequest<?>)) {
			super.queueRequest(request);
			return;
		}
		final ITmfEventRequest<T> eventRequest = (ITmfEventRequest<T>) request;

		Thread thread = new Thread() {
			@Override
			public void run() {
				
//				final long requestStart = System.nanoTime();

				final Integer[] CHUNK_SIZE = new Integer[1];
				CHUNK_SIZE[0] = blockSize + ((indexing) ? 1 : 0);
				
				final Integer[] nbRead = new Integer[1];
				nbRead[0] = 0;

//				final TmfTimestamp[] timestamp = new TmfTimestamp[1];
//				timestamp[0] = new TmfTimestamp(eventRequest.getRange().getStartTime());
//				final TmfTimestamp endTS = eventRequest.getRange().getEndTime();

				final Boolean[] isFinished = new Boolean[1];
				isFinished[0] = Boolean.FALSE;

				while (!isFinished[0]) {

//					TmfEventRequest<T> subRequest = new TmfEventRequest<T>(eventRequest.getDataType(), new TmfTimeRange(timestamp[0], endTS), CHUNK_SIZE[0], eventRequest.getBlockize(), ExecutionType.BACKGROUND)
//					TmfDataRequest<T> subRequest = new TmfDataRequest<T>(eventRequest.getDataType(), nbRead[0], CHUNK_SIZE[0], eventRequest.getBlockize(), ExecutionType.BACKGROUND)
					TmfDataRequest<T> subRequest = new TmfDataRequest<T>(eventRequest.getDataType(), nbRead[0], CHUNK_SIZE[0], ExecutionType.BACKGROUND)
					{
						@Override
						public void handleData(T data) {
							super.handleData(data);
							eventRequest.handleData(data);
							if (getNbRead() == CHUNK_SIZE[0]) {
								nbRead[0] += getNbRead();
							}
							if (getNbRead() > CHUNK_SIZE[0]) {
								System.out.println("ERROR - Read too many events");
							}
						}

						@Override
						public void handleCompleted() {
//							System.out.println("Request completed at: " + timestamp[0]);
							if (getNbRead() < CHUNK_SIZE[0]) {
								eventRequest.done();
								isFinished[0] = Boolean.TRUE;
								nbRead[0] += getNbRead();
//								System.out.println("fNbRead=" + fNbRead + ", count=" + count +", total=" + nbRead[0]);
							}
							super.handleCompleted();
						}
					};

					if (!isFinished[0]) {
						queueRequest(subRequest);

						try {
							subRequest.waitForCompletion();
//							System.out.println("Finished at " + timestamp[0]);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}

//						TmfTimestamp newTS = new TmfTimestamp(timestamp[0].getValue() + 1, timestamp[0].getScale(), timestamp[0].getPrecision());
//						timestamp[0] = newTS;
						CHUNK_SIZE[0] = blockSize;
//						System.out.println("New timestamp: " + timestamp[0]);
					}
				}
//				final long requestEnded = System.nanoTime();
//				System.out.println("Background request completed. Elapsed= " + (requestEnded * 1.0 - requestStart) / 1000000000);
			}
		};

		thread.start();
	}

}
