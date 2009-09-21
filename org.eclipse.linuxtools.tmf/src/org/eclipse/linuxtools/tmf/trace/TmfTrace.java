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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Vector;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.ITmfRequestHandler;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;

/**
 * <b><u>TmfTrace</u></b>
 * <p>
 * Abstract implementation of ITmfTrace. It should be sufficient to extend this
 * class and provide implementation for <code>getCurrentLocation()</code> and
 * <code>seekLocation()</code>, as well as a proper parser, to have a working
 * concrete implementation.
 * 
 * TODO: Add support for live streaming (notifications, incremental indexing, ...)
 */
public abstract class TmfTrace implements ITmfTrace, ITmfRequestHandler<TmfEvent> {

    // ========================================================================
    // Constants
    // ========================================================================

    // The default number of events to cache
    public static final int DEFAULT_PAGE_SIZE = 1000;

    // ========================================================================
    // Attributes
    // ========================================================================

    // The trace path
    private final String fPath;

    // The trace name
    private final String fName;

    // The checkpoints page size
    private final int fPageSize;

    // Indicate if the stream should be pre-indexed
    private final boolean fWaitForIndexCompletion;

    // The set of event stream checkpoints (for random access)
    protected Vector<TmfTraceCheckpoint> fCheckpoints = new Vector<TmfTraceCheckpoint>();

    // The number of events collected
    private long fNbEvents = 0;

    // The time span of the event stream
    private TmfTimeRange fTimeRange = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigBang);

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param name
     * @param pageSize
     * @param index
     * @throws FileNotFoundException
     */
    protected TmfTrace(String path, int pageSize, boolean waitForIndexCompletion) throws FileNotFoundException {
    	int sep = path.lastIndexOf(File.separator);
    	fName = (sep >= 0) ? path.substring(sep + 1) : path;
    	fPath = path;
        fPageSize = (pageSize > 0) ? pageSize : DEFAULT_PAGE_SIZE;
        fWaitForIndexCompletion = waitForIndexCompletion;
    }

    /**
     * @param name
     * @param cacheSize
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, boolean waitForIndexCompletion) throws FileNotFoundException {
    	this(name, DEFAULT_PAGE_SIZE, waitForIndexCompletion);
    }

    /**
     * @param name
     * @param cacheSize
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, int pageSize) throws FileNotFoundException {
    	this(name, pageSize, false);
    }

    /**
     * @param name
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name) throws FileNotFoundException {
    	this(name, DEFAULT_PAGE_SIZE, false);
    }

    // ========================================================================
    // Accessors
    // ========================================================================

    /**
     * @return the trace path
     */
    public String getPath() {
        return fPath;
    }

    /**
     * @return the trace name
     */
    public String getName() {
        return fName;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getNbEvents()
     */
    public long getNbEvents() {
        return fNbEvents;
    }

    /**
     * @return the size of the cache
     */
    public int getPageSize() {
        return fPageSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getTimeRange()
     */
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    public TmfTimestamp getStartTime() {
    	return fTimeRange.getStartTime();
    }

    public TmfTimestamp getEndTime() {
    	return fTimeRange.getEndTime();
    }

    protected long getIndex(TmfTimestamp timestamp) {
    	TmfTraceContext context = seekEvent(timestamp);
    	return context.index;
    }

    protected TmfTimestamp getTimestamp(int index) {
    	TmfTraceContext context = seekEvent(index);
    	return context.timestamp;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    protected void setTimeRange(TmfTimeRange range) {
    	fTimeRange = range;
    }

    protected void setStartTime(TmfTimestamp startTime) {
    	fTimeRange = new TmfTimeRange(startTime, fTimeRange.getEndTime());
    }

    protected void setEndTime(TmfTimestamp endTime) {
    	fTimeRange = new TmfTimeRange(fTimeRange.getStartTime(), endTime);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
     */
    public TmfTraceContext seekEvent(TmfTimestamp timestamp) {

    	if (timestamp == null) {
    		timestamp = TmfTimestamp.BigBang;
    	}

    	// First, find the right checkpoint
    	int index = Collections.binarySearch(fCheckpoints, new TmfTraceCheckpoint(timestamp, 0));

        // In the very likely event that the checkpoint was not found, bsearch
        // returns its negated would-be location (not an offset...). From that
        // index, we can then position the stream and get the event.
        if (index < 0) {
            index = Math.max(0, -(index + 2));
        }

        // Position the stream at the checkpoint
        Object location = (index < fCheckpoints.size()) ? fCheckpoints.elementAt(index).getLocation() : null;
        TmfTraceContext nextEventContext = seekLocation(location);
        nextEventContext.index = index * fPageSize;
        TmfTraceContext currentEventContext = new TmfTraceContext(nextEventContext);

        // And get the event
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
        	currentEventContext.location = nextEventContext.location;
        	currentEventContext.index++;
        	event = getNextEvent(nextEventContext);
        }

    	currentEventContext.timestamp = (event != null) ? event.getTimestamp() : null;
        return currentEventContext;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(int)
     */
    public TmfTraceContext seekEvent(long position) {

        // Position the stream at the previous checkpoint
        int index = (int) position / fPageSize;
        Object location = (index < fCheckpoints.size()) ? fCheckpoints.elementAt(index).getLocation() : null;
        TmfTraceContext nextEventContext = seekLocation(location);
        nextEventContext.index = index * fPageSize;
        TmfTraceContext currentEventContext = new TmfTraceContext(nextEventContext);

        // And locate the event (if it exists)
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && currentEventContext.index < position) {
        	currentEventContext.location = nextEventContext.location;
        	currentEventContext.timestamp = event.getTimestamp();
        	currentEventContext.index++;
        	event = getNextEvent(nextEventContext);
        }

        return currentEventContext;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getNextEvent(org.eclipse.linuxtools.tmf.trace.ITmfTrace.TraceContext)
	 */
	public synchronized TmfEvent getNextEvent(TmfTraceContext context) {
		seekLocation(context.location);
		TmfEvent event = parseEvent();
		if (event != null) {
			context.location = getCurrentLocation();
			context.timestamp = event.getTimestamp();
			context.index++;
			processEvent(event);
		}
    	return event;
	}

    /**
     * TODO: Document me
     * @return
     */
	public abstract Object getCurrentLocation();
    public abstract TmfEvent parseEvent();

	/**
	 * Hook for "special" processing by the extending class
	 * @param event
	 */
	public void processEvent(TmfEvent event) {
		// Do nothing by default
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

		// Initialize the trace context
		final TmfTraceContext context = (request.getRange() != null) ? 
				seekEvent(request.getRange().getStartTime()) : 
			    seekEvent(request.getIndex());

		final TmfTimestamp endTime = (request.getRange() != null) ? 
				request.getRange().getEndTime() :
				TmfTimestamp.BigCrunch;

		// Process the request
		Thread thread = new Thread() {

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

				// Get the ordered events
				TmfEvent event = getNextEvent(context);
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
						event = getNextEvent(context);
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

	// ========================================================================
    // Trace indexing. Essentially, parse the stream asynchronously and build
	// the checkpoints index. This index is used to quickly find an event based
	// on a timestamp or an index.
    // ========================================================================

	private Boolean fIndexing = false;
    public void indexStream() {
    	synchronized (fIndexing) {
    		if (fIndexing) {
    			return;
    		}
    		fIndexing = true;
    	}

    	final IndexingJob job = new IndexingJob("Indexing " + fName);
    	job.schedule();

    	if (fWaitForIndexCompletion) {
    		try {
    			job.join();
    		} catch (InterruptedException e) {
    			// TODO Auto-generated catch block
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

            int nbEvents = 0;
            TmfTimestamp startTime = new TmfTimestamp();
            TmfTimestamp lastTime  = new TmfTimestamp();
            TmfTimestamp rangeStartTime = new TmfTimestamp();

            monitor.beginTask("Indexing " + fName, IProgressMonitor.UNKNOWN);

            try {
                TmfTraceContext nextEventContext;
                synchronized(this) {
                	nextEventContext = seekLocation(null);
                }
                TmfTraceContext currentEventContext = new TmfTraceContext(nextEventContext);
                TmfEvent event = getNextEvent(nextEventContext);
                if (event == null) {
                	return Status.OK_STATUS;
                }

               	startTime = event.getTimestamp();
               	lastTime  = event.getTimestamp();
               	fCheckpoints.add(new TmfTraceCheckpoint(lastTime, currentEventContext.location));
                currentEventContext.location = nextEventContext.location;

                rangeStartTime = startTime;
                while ((event = getNextEvent(nextEventContext)) != null) {
                    lastTime = event.getTimestamp();
                    if ((++nbEvents % fPageSize) == 0) {
                        synchronized(this) {
                        	fCheckpoints.add(new TmfTraceCheckpoint(lastTime, currentEventContext.location));
                        	fNbEvents = nbEvents;
                            fTimeRange = new TmfTimeRange(startTime, lastTime);
                        }
                        notifyListeners(new TmfTimeRange(rangeStartTime, lastTime));

                        monitor.worked(1);

                        // Check monitor *after* fCheckpoints has been updated
                        if (monitor.isCanceled()) {
                            monitor.done();
                        	return Status.CANCEL_STATUS;
                        }
                    }

                    // Do whatever
                    processEvent(event);
                    currentEventContext.location = nextEventContext.location;
                }
            }
            finally {
                synchronized(this) {
                	fNbEvents = ++nbEvents;
                	fTimeRange = new TmfTimeRange(startTime, lastTime);
            		fIndexing = false;
                }
                notifyListeners(new TmfTimeRange(rangeStartTime, lastTime));
                monitor.done();
            }

            return Status.OK_STATUS;
		}
    }

    private void notifyListeners(TmfTimeRange range) {
		TmfSignalManager.dispatchSignal(new TmfTraceUpdatedSignal(this, this, range));
	}
   
}
