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
import org.eclipse.linuxtools.tmf.component.TmfComponent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.ITmfRequestHandler;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;

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
public abstract class TmfTrace extends TmfComponent implements ITmfTrace, ITmfRequestHandler<TmfEvent> {

    // ========================================================================
    // Constants
    // ========================================================================

    // The default number of events to cache
    public static final int DEFAULT_CACHE_SIZE = 1000;

    // ========================================================================
    // Attributes
    // ========================================================================

    // The trace path
    private final String fPath;

    // The trace name
    private final String fName;

    // The cache page size AND checkpoints interval
    protected int fCacheSize;

    // Indicate if the stream should be pre-indexed
    private final boolean fWaitForIndexCompletion;

    // The set of event stream checkpoints (for random access)
    protected Vector<TmfTraceCheckpoint> fCheckpoints = new Vector<TmfTraceCheckpoint>();

    // The number of events collected
    protected long fNbEvents = 0;

    // The time span of the event stream
    private TmfTimeRange fTimeRange = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigBang);

    // ========================================================================
    // Constructors
    // ========================================================================

    /**
     * @param name
     * @param pageSize
     * @param data.index
     * @throws FileNotFoundException
     */
    protected TmfTrace(String path, int pageSize, boolean waitForIndexCompletion) throws FileNotFoundException {
    	super();
    	int sep = path.lastIndexOf(File.separator);
    	fName = (sep >= 0) ? path.substring(sep + 1) : path;
    	fPath = path;
        fCacheSize = (pageSize > 0) ? pageSize : DEFAULT_CACHE_SIZE;
        fWaitForIndexCompletion = waitForIndexCompletion;
    }

    /**
     * @param name
     * @param cacheSize
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, boolean waitForIndexCompletion) throws FileNotFoundException {
    	this(name, DEFAULT_CACHE_SIZE, waitForIndexCompletion);
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
    	this(name, DEFAULT_CACHE_SIZE, false);
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
    public int getCacheSize() {
        return fCacheSize;
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
    	return context.getIndex();
    }

    protected TmfTimestamp getTimestamp(int index) {
    	TmfTraceContext context = seekEvent(index);
    	return context.getTimestamp();
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

        // In the very likely case that the checkpoint was not found, bsearch
        // returns its negated would-be location (not an offset...). From that
        // index, we can then position the stream and get the event.
        if (index < 0) {
            index = Math.max(0, -(index + 2));
        }

        // Position the stream at the checkpoint
        Object location;
        synchronized (fCheckpoints) { //Just in case we are re-indexing
            location = (index < fCheckpoints.size()) ? fCheckpoints.elementAt(index).getLocation() : null;
        }
        TmfTraceContext nextEventContext = seekLocation(location);
        nextEventContext.setIndex(index * fCacheSize);
        TmfTraceContext currentEventContext = new TmfTraceContext(nextEventContext);

        // And get the event
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
        	currentEventContext.setLocation(nextEventContext.getLocation());
        	currentEventContext.incrIndex();
        	event = getNextEvent(nextEventContext);
        }

    	currentEventContext.setTimestamp((event != null) ? event.getTimestamp() : null);
        return currentEventContext;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(int)
     */
    public TmfTraceContext seekEvent(long position) {

        // Position the stream at the previous checkpoint
        int index = (int) position / fCacheSize;
        Object location;
        synchronized (fCheckpoints) { //Just in case we are re-indexing
            location = (index < fCheckpoints.size()) ? fCheckpoints.elementAt(index).getLocation() : null;
        }
        TmfTraceContext nextEventContext = seekLocation(location);
        nextEventContext.setIndex(index * fCacheSize);
        TmfTraceContext currentEventContext = new TmfTraceContext(nextEventContext);

        // And locate the event (if it exists)
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && currentEventContext.getIndex() < position) {
        	currentEventContext.setLocation(nextEventContext.getLocation());
        	currentEventContext.setTimestamp(event.getTimestamp());
        	currentEventContext.incrIndex();
        	event = getNextEvent(nextEventContext);
        }

        return currentEventContext;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getNextEvent(org.eclipse.linuxtools.tmf.trace.ITmfTrace.TraceContext)
	 */
	public TmfEvent getNextEvent(TmfTraceContext context) {
		// parseEvent updates the context
		TmfEvent event = parseEvent(context);
		if (event != null) {
			processEvent(event);
		}
    	return event;
	}

    /**
     * To be implemented by the subclass.
     */
	public abstract Object getCurrentLocation();
    public abstract TmfEvent parseEvent(TmfTraceContext context);

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
				int nbRequestedEvents = request.getNbRequestedEvents();
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
						pushData(request, events);
					}
					// To avoid an unnecessary read passed the last event requested
					if (nbEvents < nbRequestedEvents)
						event = getNextEvent(context);
				}
				pushData(request, events);
				request.done();
			}
		};
		thread.start();
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
		return "[TmfTrace (" + fName + "]";
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

            monitor.beginTask("Indexing " + fName, IProgressMonitor.UNKNOWN);

            try {
            	// Position the trace at the beginning
                TmfTraceContext context = seekLocation(null);
                if (context.getTimestamp() == null) {
                	return Status.OK_STATUS;
                }
                // FIXME: LTTng hack - start
//               	fCheckpoints.add(new TmfTraceCheckpoint(context.getTimestamp(), context.getLocation()));	// TMF
                // FIXME: LTTng hack - end

               	TmfEvent event;
               	startTime = context.getTimestamp();
               	lastTime  = context.getTimestamp();
                while ((event = getNextEvent(context)) != null) {
                	TmfTimestamp timestamp = context.getTimestamp();
            		if (timestamp != null) {
            			lastTime = timestamp;
            		}
                    // FIXME: LTTng hack - start
//                    if (((++nbEvents % fCacheSize) == 0) && (timestamp != null)) {	// TMF
                    if (((nbEvents++ % fCacheSize) == 0) && (timestamp != null)) {	// LTTng
                        // FIXME: LTTng hack - end
                   		fCheckpoints.add(new TmfTraceCheckpoint(timestamp, context.getLocation()));
                   		fNbEvents = nbEvents - 1;
                   		lastTime = context.getTimestamp();
                   		fTimeRange = new TmfTimeRange(startTime, lastTime);
                   		notifyListeners(new TmfTimeRange(startTime, lastTime));

                        monitor.worked(1);

                        // Check monitor *after* fCheckpoints has been updated
                        if (monitor.isCanceled()) {
                            monitor.done();
                        	return Status.CANCEL_STATUS;
                        }
                    }

                    // Do whatever
                    processEvent(event);
                }
            }
            finally {
                synchronized(this) {
                	fNbEvents = nbEvents;
                	fTimeRange = new TmfTimeRange(startTime, lastTime);
            		fIndexing = false;
                }
                notifyListeners(new TmfTimeRange(startTime, lastTime));
                monitor.done();

            }

//            createOffsetsFile();
//            dumpCheckpoints();
            
            return Status.OK_STATUS;
		}
    }

    protected void notifyListeners(TmfTimeRange range) {
    	broadcastSignal(new TmfTraceUpdatedSignal( (TmfTrace)this, (TmfTrace)this, range));
	}
   
//	/**
//	 * Dump the trace checkpoints
//	 */
//	private void dumpCheckpoints() {
//		System.out.println("-----");
//		System.out.println("Checkpoints of " + fName);
//		for (int i = 0; i < fCheckpoints.size(); i++) {
//			TmfTraceCheckpoint checkpoint = fCheckpoints.get(i);
//			TmfTraceContext context = new TmfTraceContext(checkpoint.getLocation());
//			TmfEvent event = getNextEvent(context);
//			System.out.println("  Entry: " + i + " timestamp: " + checkpoint.getTimestamp() + ", event: " + event.getTimestamp());
//			assert((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
//		}
//		System.out.println();
//	}

//	private void createOffsetsFile() {
//
//	    try {
//	    	ObjectOutputStream  out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("LTTngOffsets.dat")));
//
//	    	TmfTraceContext context = null;
//           	context = seekLocation(null);
//			out.writeObject(context.getLocation());
//
//			int nbEvents = 0;
//            while (getNextEvent(context) != null) {
//    			out.writeObject(context.getLocation());
//    			nbEvents++;
//            }
//            out.close();
//            System.out.println("TmfTrace wrote " + nbEvents + " events");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

//	private void createOffsetsFile() {
//
//		try {
//			DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream("LTTngOffsets.dat")));
//
//			TmfTraceContext context = null;
//			context = seekLocation(null);
//
//			TmfEvent event;
//			int nbEvents = 0;
//			while ((event = getNextEvent(context)) != null) {
//				out.writeUTF(event.getTimestamp().toString());
//				nbEvents++;
//			}
//			out.close();
//			System.out.println("TmfTrace wrote " + nbEvents + " events");
//		} catch (FileNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

}
