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

package org.eclipse.linuxtools.tmf.trace;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
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
public abstract class TmfTrace<T extends TmfEvent> extends TmfProvider<T> implements ITmfTrace {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // The default number of events to cache
	// TODO: Make the DEFAULT_CACHE_SIZE a preference
    public static final int DEFAULT_CACHE_SIZE = 1000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The trace path
    private final String fPath;

    // The trace name
    private final String fName;

    // The cache page size AND checkpoints interval
    protected int fCacheSize;

    // The set of event stream checkpoints (for random access)
    protected Vector<TmfTraceCheckpoint> fCheckpoints = new Vector<TmfTraceCheckpoint>();

    // The number of events collected
    protected long fNbEvents = 0;

    // The time span of the event stream
    private TmfTimeRange fTimeRange = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigBang);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    protected TmfTrace(Class<T> type, String path, int cacheSize) throws FileNotFoundException {
    	super(type);
    	int sep = path.lastIndexOf(File.separator);
    	fName = (sep >= 0) ? path.substring(sep + 1) : path;
    	fPath = path;
        fCacheSize = (cacheSize > 0) ? cacheSize : DEFAULT_CACHE_SIZE;
    }

    /**
     * @param path
     * @throws FileNotFoundException
     */
    protected TmfTrace(Class<T> type, String path) throws FileNotFoundException {
    	this(type, path, DEFAULT_CACHE_SIZE);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

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

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getStartTime()
     */
    public TmfTimestamp getStartTime() {
    	return fTimeRange.getStartTime();
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getEndTime()
     */
    public TmfTimestamp getEndTime() {
    	return fTimeRange.getEndTime();
    }

//    /**
//     * Return the event rank based on its timestamp
//     * 
//     * @param timestamp
//     * @return
//     */
//    protected long getIndex(TmfTimestamp timestamp) {
//    	TmfTraceContext context = seekEvent(timestamp);
//    	return context.getIndex();
//    }

//    /**
//     * Return the event timestamp based on its rank
//     * @param index
//     * @return
//     */
//    protected TmfTimestamp getTimestamp(int index) {
//    	TmfTraceContext context = seekEvent(index);
//    	return context.getTimestamp();
//    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    protected void setTimeRange(TmfTimeRange range) {
    	fTimeRange = range;
    }

    protected void setStartTime(TmfTimestamp startTime) {
    	fTimeRange = new TmfTimeRange(startTime, fTimeRange.getEndTime());
    }

    protected void setEndTime(TmfTimestamp endTime) {
    	fTimeRange = new TmfTimeRange(fTimeRange.getStartTime(), endTime);
    }

	// ------------------------------------------------------------------------
	// TmfProvider
	// ------------------------------------------------------------------------

	@Override
	public ITmfContext setContext(TmfDataRequest<T> request) {
		if (request instanceof TmfEventRequest<?>) {
			return seekEvent(((TmfEventRequest<T>) request).getRange().getStartTime());
		}
		return null;
	}

	/**
	 * Return the next piece of data based on the context supplied. The context
	 * would typically be updated for the subsequent read.
	 * 
	 * @param context
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public T getNext(ITmfContext context) {
		if (context instanceof TmfTraceContext) {
			return (T) getNextEvent((TmfTraceContext) context);
		}
		return null;
	}

	@Override
	public boolean isCompleted(TmfDataRequest<T> request, T data) {
		if (request instanceof TmfEventRequest<?> && data != null) {
			return data.getTimestamp().compareTo(((TmfEventRequest<T>) request).getRange().getEndTime(), false) > 0;
		}
		return true;
	}

    
	// ------------------------------------------------------------------------
	// ITmfTrace
	// ------------------------------------------------------------------------

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
        TmfTraceContext nextEventContext = seekLocation(location);
        nextEventContext.setRank(index * fCacheSize);
        TmfTraceContext currentEventContext = new TmfTraceContext(nextEventContext);

        // And get the event
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
        	currentEventContext.setLocation(nextEventContext.getLocation());
        	currentEventContext.incrRank();
        	event = getNextEvent(nextEventContext);
        }

        return currentEventContext;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(int)
     */
    public TmfTraceContext seekEvent(long position) {

        // Position the stream at the previous checkpoint
        int index = (int) position / fCacheSize;
        Object location;
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
        TmfTraceContext context = seekLocation(location);
        long rank = index * fCacheSize;
        context.setRank(rank);

        if (rank < position) {
            TmfEvent event = getNextEvent(context);
            while (event != null && ++rank < position) {
            	event = getNextEvent(context);
            }
        }

        return new TmfTraceContext(context.getLocation(), context.getRank());
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getNextEvent(org.eclipse.linuxtools.tmf.trace.ITmfTrace.TraceContext)
	 */
	public synchronized TmfEvent getNextEvent(TmfTraceContext context) {
		// parseEvent() does not update the context
		TmfEvent event = parseEvent(context);
		context.setLocation(getCurrentLocation());
		if (event != null) {
			context.incrRank();
			processEvent(event);
		}
    	return event;
	}

    /**
	 * Hook for "special" processing by the concrete class
	 * (called by getNextEvent())
	 * 
	 * @param event
	 */
	public void processEvent(TmfEvent event) {
		// Do nothing by default
	}

    /**
     * To be implemented by the concrete class
     */
	public abstract Object getCurrentLocation();
    public abstract TmfEvent parseEvent(TmfTraceContext context);

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[TmfTrace (" + fName + "]";
	}

    // ------------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------------

	/*
	 * The purpose of the index is to keep the information needed to rapidly
	 * access a trace event based on its timestamp or rank.
	 * 
	 * NOTE: As it is, doesn't work for streaming traces.
	 */

	private IndexingJob job;
	private Boolean fIndexing = false;

	public void indexTrace(boolean waitForCompletion) {
    	synchronized (fIndexing) {
    		if (fIndexing) {
    			return;
    		}
    		fIndexing = true;
    	}

    	job = new IndexingJob("Indexing " + fName);
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

            monitor.beginTask("Indexing " + fName, IProgressMonitor.UNKNOWN);

            int nbEvents = 0;
            TmfTimestamp startTime = null;
            TmfTimestamp lastTime  = null;

            fCheckpoints = new Vector<TmfTraceCheckpoint>();
            
            try {
            	// Position the trace at the beginning
                TmfTraceContext context = seekLocation(null);
                Object location = context.getLocation();

               	TmfEvent event = getNextEvent(context);
                startTime = new TmfTimestamp(event.getTimestamp());
                lastTime  = new TmfTimestamp(startTime);
                while (event != null) {
                	lastTime = event.getTimestamp();
           			if ((nbEvents++ % fCacheSize) == 0) {
           				lastTime = new TmfTimestamp(event.getTimestamp());
                   		fCheckpoints.add(new TmfTraceCheckpoint(lastTime, location));
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

                    // We will need this location at the next iteration
                    if ((nbEvents % fCacheSize) == 0) {
                        location = context.getLocation();
           			}

                    event = getNextEvent(context);
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
//            dumpTraceCheckpoints();
            
            return Status.OK_STATUS;
		}
    }

    protected void notifyListeners(TmfTimeRange range) {
    	broadcast(new TmfTraceUpdatedSignal(this, this, range));
	}
   
//	// ------------------------------------------------------------------------
//	// Toubleshooting code
//	// ------------------------------------------------------------------------
//
//	private void dumpTraceCheckpoints() {
//		System.out.println("-----");
//		System.out.println("Checkpoints of " + fName);
//		for (int i = 0; i < fCheckpoints.size(); i++) {
//			TmfTraceCheckpoint checkpoint = fCheckpoints.get(i);
//			TmfTraceContext context = new TmfTraceContext(checkpoint.getLocation());
//			TmfEvent event = getNext(context);
//			System.out.println("  Entry: " + i + " timestamp: " + checkpoint.getTimestamp() + ", event: " + event.getTimestamp());
//			assert((checkpoint.getTimestamp().compareTo(event.getTimestamp(), false) == 0));
//		}
//		System.out.println();
//	}

//	private void createOffsetsFile() {
//
//	    try {
//			// The trace context validation file is read by TmfTraceContext
//	    	ObjectOutputStream  out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream("TmfTraceContext.dat")));
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
//
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
