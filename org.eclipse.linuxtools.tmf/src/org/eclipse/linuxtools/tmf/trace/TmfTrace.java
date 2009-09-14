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
import org.eclipse.linuxtools.tmf.signal.TmfSignalManager;

/**
 * <b><u>TmfTrace</u></b>
 * <p>
 * Abstract implementation of ITmfTrace. It should be sufficient to extend this
 * class and provide implementation for <code>getCurrentLocation()</code> and
 * <code>seekLocation()</code>, as well as a proper parser, to have a working
 * concrete imlpementation.
 */
public abstract class TmfTrace implements ITmfTrace {

    // ========================================================================
    // Constants
    // ========================================================================

    // The default number of events to cache
    public static final int DEFAULT_PAGE_SIZE = 1000;

    // ========================================================================
    // Attributes
    // ========================================================================

    // The stream name
    private final String fName;

    // The checkpoints page size
    private final int fPageSize;

    // The checkpoints page size
    private final boolean fIndex;

    // The set of event stream checkpoints (for random access)
    protected Vector<TmfTraceCheckpoint> fCheckpoints = new Vector<TmfTraceCheckpoint>();

    // The number of events collected
    private int fNbEvents = 0;

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
    protected TmfTrace(String name, int pageSize, boolean index) throws FileNotFoundException {
    	fName = name;
        fPageSize = pageSize;
        fIndex = index;
    }

    /**
     * @param name
     * @param cacheSize
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, boolean index) throws FileNotFoundException {
    	this(name, DEFAULT_PAGE_SIZE, index);
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
     * @return the size of the cache
     */
    public int getPageSize() {
        return fPageSize;
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
    public int getNbEvents() {
        return fNbEvents;
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

    public void setTimeRange(TmfTimeRange range) {
    	fTimeRange = range;
    }

    public void setStartTime(TmfTimestamp startTime) {
    	fTimeRange = new TmfTimeRange(startTime, fTimeRange.getEndTime());
    }

    public void setEndTime(TmfTimestamp endTime) {
    	fTimeRange = new TmfTimeRange(fTimeRange.getStartTime(), endTime);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getIndex(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
     */
    public int getIndex(TmfTimestamp timestamp) {
    	TmfTraceContext context = seekEvent(timestamp);
    	return context.index;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getTimestamp(int)
     */
    public TmfTimestamp getTimestamp(int index) {
    	TmfTraceContext context = seekEvent(index);
    	TmfEvent event = peekEvent(context);
    	return event.getTimestamp();
    }

    // ========================================================================
    // Operators
    // ========================================================================

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
     */
    public TmfTraceContext seekEvent(TmfTimestamp timestamp) {

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
        TmfTraceContext nextEventContext;
        synchronized(this) {
        	nextEventContext = seekLocation(location);
        }
        TmfTraceContext currentEventContext = new TmfTraceContext(nextEventContext.location, index * fPageSize);

        // And get the event
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
        	currentEventContext.location = nextEventContext.location;
        	currentEventContext.index++;
        	event = getNextEvent(nextEventContext);
        }

        return currentEventContext;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(int)
     */
    public TmfTraceContext seekEvent(int position) {

        // Position the stream at the previous checkpoint
        int index = position / fPageSize;
        Object location = (index < fCheckpoints.size()) ? fCheckpoints.elementAt(index).getLocation() : null;
        TmfTraceContext nextEventContext;
        synchronized(this) {
        	nextEventContext = seekLocation(location);
        }
        TmfTraceContext currentEventContext = new TmfTraceContext(nextEventContext);

        // And locate the event (if it exists)
        int current = index * fPageSize;
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && current < position) {
        	currentEventContext.location = nextEventContext.location;
        	event = getNextEvent(nextEventContext);
            current++;
        }

        return currentEventContext;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#peekEvent(org.eclipse.linuxtools.tmf.trace.ITmfTrace.TraceContext)
     */
    public TmfEvent peekEvent(TmfTraceContext context) {
    	TmfTraceContext ctx = new TmfTraceContext(context);
        return getNextEvent(ctx);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getEvent(org.eclipse.linuxtools.tmf.trace.ITmfTrace.TraceContext, org.eclipse.linuxtools.tmf.event.TmfTimestamp)
     */
    public TmfEvent getEvent(TmfTraceContext context, TmfTimestamp timestamp) {
    	TmfTraceContext ctx = seekEvent(timestamp);
    	context.location = ctx.location; 
    	return getNextEvent(context);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getEvent(org.eclipse.linuxtools.tmf.trace.ITmfTrace.TraceContext, int)
     */
    public TmfEvent getEvent(TmfTraceContext context, int position) {
    	TmfTraceContext ctx = seekEvent(position);
    	context.location = ctx.location; 
    	return getNextEvent(context);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getNextEvent(org.eclipse.linuxtools.tmf.trace.ITmfTrace.TraceContext)
	 */
	public synchronized TmfEvent getNextEvent(TmfTraceContext context) {
		seekLocation(context.location);
		TmfEvent event = parseNextEvent();
		processEvent(event);
		context.location = getCurrentLocation();
    	return event;
	}

	/**
	 * Hook for "special" processing by the extending class
	 * @param event
	 */
	public void processEvent(TmfEvent event) {
		// Do nothing by default
	}
    
    // ========================================================================
    // Stream indexing. Essentially, parse the file asynchronously and build
	// the checkpoints index. This index is used to quickly find an event based
	// on a timestamp or an index.
    // ========================================================================

    public void indexStream() {
    	IndexingJob job = new IndexingJob(fName);
    	job.schedule();
    	if (fIndex) {
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
                if (event != null) {
                	startTime = event.getTimestamp();
                	lastTime  = event.getTimestamp();
                }

                rangeStartTime = startTime;
                while (event != null) {
                    lastTime = event.getTimestamp();
                    if ((nbEvents++ % fPageSize) == 0) {
                        TmfTraceCheckpoint bookmark = new TmfTraceCheckpoint(lastTime, currentEventContext.location);
                        synchronized(this) {
                        	fCheckpoints.add(bookmark);
                        	fNbEvents = nbEvents;
                            fTimeRange = new TmfTimeRange(startTime, lastTime);
                        }
                        notifyListeners(new TmfTimeRange(rangeStartTime, lastTime));
                        monitor.worked(1);
                        // Check monitor *after* fCheckpoints has been updated
                        if (monitor.isCanceled()) {
                        	return Status.CANCEL_STATUS;
                        }
                    }

                    // Do whatever
                    processEvent(event);

                    currentEventContext.location = nextEventContext.location;
					event = getNextEvent(nextEventContext);
                }
            }
            finally {
                synchronized(this) {
                	fNbEvents = nbEvents;
                	fTimeRange = new TmfTimeRange(startTime, lastTime);
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
