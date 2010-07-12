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

import org.eclipse.linuxtools.tmf.component.TmfEventProvider;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest;
import org.eclipse.linuxtools.tmf.request.ITmfDataRequest.ExecutionType;
import org.eclipse.linuxtools.tmf.request.ITmfEventRequest;
import org.eclipse.linuxtools.tmf.request.TmfDataRequest;
import org.eclipse.linuxtools.tmf.request.TmfEventRequest;
import org.eclipse.linuxtools.tmf.signal.TmfSignalHandler;
import org.eclipse.linuxtools.tmf.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.signal.TmfTraceUpdatedSignal;

/**
 * <b><u>TmfTrace</u></b>
 * <p>
 * Abstract implementation of ITmfTrace. It should be sufficient to extend this
 * class and provide implementation for <code>getCurrentLocation()</code> and
 * <code>seekLocation()</code>, as well as a proper parser, to have a working
 * concrete implementation.
 * <p>
 * Note: The notion of event rank is still under heavy discussion. Although
 * used by the Events View and probably useful in the general case, there
 * is no easy way to implement it for LTTng (actually  a strong case is being
 * made that this is useless).
 * <p>
 * That it is not supported by LTTng does by no mean indicate that it is not
 * useful for (just about) every other tracing tool. Therefore, this class
 * provides a minimal (and partial) implementation of rank. However, the current
 * implementation should not be relied on in the general case.
 * 
 * TODO: Add support for live streaming (notifications, incremental indexing, ...)
 */
public abstract class TmfTrace<T extends TmfEvent> extends TmfEventProvider<T> implements ITmfTrace, Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // The default number of events to cache
	// TODO: Make the DEFAULT_CACHE_SIZE a preference
    public static final int DEFAULT_INDEX_PAGE_SIZE = 1000;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The trace path
    private final String fPath;

    // The cache page size AND checkpoints interval
    protected int fIndexPageSize;

    // The set of event stream checkpoints (for random access)
    protected Vector<TmfCheckpoint> fCheckpoints = new Vector<TmfCheckpoint>();

    // The number of events collected
    protected long fNbEvents = 0;

    // The time span of the event stream
    private TmfTimestamp fStartTime = TmfTimestamp.BigCrunch;
    private TmfTimestamp fEndTime   = TmfTimestamp.BigBang;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param path
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, Class<T> type, String path) throws FileNotFoundException {
    	this(name, type, path, DEFAULT_INDEX_PAGE_SIZE);
    }

    /**
     * @param path
     * @param cacheSize
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, Class<T> type, String path, int cacheSize) throws FileNotFoundException {
    	super(name, type);
    	int sep = path.lastIndexOf(File.separator);
    	String simpleName = (sep >= 0) ? path.substring(sep + 1) : path;
    	setName(simpleName);
    	fPath = path;
        fIndexPageSize = (cacheSize > 0) ? cacheSize : DEFAULT_INDEX_PAGE_SIZE;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @SuppressWarnings("unchecked")
	@Override
	public TmfTrace<T> clone() throws CloneNotSupportedException {
    	TmfTrace<T> clone = (TmfTrace<T>) super.clone();
    	clone.fCheckpoints = (Vector<TmfCheckpoint>) fCheckpoints; 
    	clone.fStartTime = new TmfTimestamp(fStartTime); 
    	clone.fEndTime   = new TmfTimestamp(fEndTime); 
    	return clone;
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
        return fIndexPageSize;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.stream.ITmfEventStream#getTimeRange()
     */
    public TmfTimeRange getTimeRange() {
        return new TmfTimeRange(fStartTime, fEndTime);
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getStartTime()
     */
    public TmfTimestamp getStartTime() {
    	return fStartTime;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getEndTime()
     */
    public TmfTimestamp getEndTime() {
    	return fEndTime;
    }

    @SuppressWarnings("unchecked")
	public Vector<TmfCheckpoint> getCheckpoints() {
    	return (Vector<TmfCheckpoint>) fCheckpoints.clone();
    }

    /**
     * Returns the rank of the first event with the requested timestamp.
     * If none, returns the index of the next event (if any).
     *  
     * @param timestamp
     * @return
     */
    public long getRank(TmfTimestamp timestamp) {
        TmfContext context = seekEvent(timestamp);
        return context.getRank();
    }

    // ------------------------------------------------------------------------
    // Operators
    // ------------------------------------------------------------------------

    protected void setTimeRange(TmfTimeRange range) {
    	fStartTime = range.getStartTime();
    	fEndTime   = range.getEndTime();
    }

    protected void setStartTime(TmfTimestamp startTime) {
    	fStartTime = startTime;
    }

    protected void setEndTime(TmfTimestamp endTime) {
    	fEndTime = endTime;
    }

	// ------------------------------------------------------------------------
	// TmfProvider
	// ------------------------------------------------------------------------

	@Override
	public ITmfContext armRequest(ITmfDataRequest<T> request) {
		if (request instanceof ITmfEventRequest<?>) {
			return seekEvent(((ITmfEventRequest<T>) request).getRange().getStartTime());
		}
		return seekEvent(request.getIndex());
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
		if (context instanceof TmfContext) {
			return (T) getNextEvent((TmfContext) context);
		}
		return null;
	}

	// ------------------------------------------------------------------------
	// ITmfTrace
	// ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(org.eclipse.linuxtools.tmf.event.TmfTimestamp)
     */
    public TmfContext seekEvent(TmfTimestamp timestamp) {

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

        // Position the stream at the checkpoint
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
        TmfContext context = seekLocation(location);
        context.setRank(index * fIndexPageSize);

        // And locate the event
        TmfContext nextEventContext = context.clone(); // Must use clone() to get the right subtype...
        TmfEvent event = getNextEvent(nextEventContext);
        while (event != null && event.getTimestamp().compareTo(timestamp, false) < 0) {
        	context.setLocation(nextEventContext.getLocation().clone());
        	context.updateRank(1);
        	event = getNextEvent(nextEventContext);
        }

        return context;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#seekEvent(int)
     */
    public TmfContext seekEvent(long rank) {

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

        TmfContext context = seekLocation(location);
        long pos = index * fIndexPageSize;
        context.setRank(pos);

        if (pos < rank) {
            TmfEvent event = getNextEvent(context);
            while (event != null && ++pos < rank) {
            	event = getNextEvent(context);
            }
        }

        return context;
    }

	/* (non-Javadoc)
	 * @see org.eclipse.linuxtools.tmf.trace.ITmfTrace#getNextEvent(org.eclipse.linuxtools.tmf.trace.ITmfTrace.TraceContext)
	 */
	public synchronized TmfEvent getNextEvent(TmfContext context) {
		// parseEvent() does not update the context
		TmfEvent event = parseEvent(context);
		if (event != null) {
			updateIndex(context, context.getRank(), event.getTimestamp());
			context.setLocation(getCurrentLocation());
			context.updateRank(1);
			processEvent(event);
		}
    	return event;
	}

	protected synchronized void updateIndex(ITmfContext context, long rank, TmfTimestamp timestamp) {
		if (fStartTime.compareTo(timestamp, false) > 0) fStartTime = timestamp;
		if (fEndTime.compareTo(timestamp, false) < 0) fEndTime = timestamp;
		if (context.isValidRank()) {
			if (fNbEvents <= rank)
				fNbEvents = rank + 1;
			// Build the index as we go along
			if ((rank % fIndexPageSize) == 0) {
				// Determine the table position
				long position = rank / fIndexPageSize;
				// Add new entry at proper location (if empty) 
				if (fCheckpoints.size() == position) {
					ITmfLocation<?> location = context.getLocation().clone();
					fCheckpoints.add(new TmfCheckpoint(timestamp, location));
//					System.out.println(getName() + "[" + (fCheckpoints.size() - 1) + "] " + timestamp + ", " + location.toString());
				}
			}
		}
	}

    /**
	 * Hook for "special" processing by the concrete class
	 * (called by getNextEvent())
	 * 
	 * @param event
	 */
	protected void processEvent(TmfEvent event) {
		// Do nothing by default
	}

    /**
     * To be implemented by the concrete class
     */
    public abstract TmfContext seekLocation(ITmfLocation<?> location);
	public abstract ITmfLocation<?> getCurrentLocation();
    public abstract TmfEvent parseEvent(TmfContext context);

	// ------------------------------------------------------------------------
	// toString
	// ------------------------------------------------------------------------

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "[TmfTrace (" + getName() + ")]";
	}
   
    // ------------------------------------------------------------------------
    // Indexing
    // ------------------------------------------------------------------------

	/*
	 * The purpose of the index is to keep the information needed to rapidly
	 * restore the traces contexts at regular intervals (every INDEX_PAGE_SIZE
	 * event).
	 */

	@SuppressWarnings("unchecked")
	private void indexTrace(boolean waitForCompletion) {

		fCheckpoints.clear();
		
		ITmfEventRequest<TmfEvent> request = new TmfEventRequest<TmfEvent>(TmfEvent.class, TmfTimeRange.Eternity, TmfDataRequest.ALL_DATA, 1, ITmfDataRequest.ExecutionType.LONG) {

			TmfTimestamp startTime =  null;
			TmfTimestamp lastTime  =  null;

			@Override
			public void handleData() {
				TmfEvent[] events = getData();
				if (events.length > 0) {
					TmfTimestamp ts = events[0].getTimestamp();
					if (startTime == null) {
						startTime = new TmfTimestamp(ts);
						fStartTime = startTime;
					}
					lastTime = new TmfTimestamp(ts);

					if ((fNbRead % DEFAULT_INDEX_PAGE_SIZE) == 0) {
						updateTraceData();
					}
				}
			}

			@Override
			public void handleSuccess() {
				updateTraceData();
			}

			private void updateTraceData() {
				fEndTime  = new TmfTimestamp(lastTime);
				fNbEvents = fNbRead;
				notifyListeners();
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
    	broadcast(new TmfTraceUpdatedSignal(this, this, new TmfTimeRange(fStartTime, fEndTime)));
	}
   
	// ------------------------------------------------------------------------
	// TmfDataProvider
	// ------------------------------------------------------------------------

	@Override
    protected void queueLongRequest(final ITmfDataRequest<T> request) {

        // TODO: Handle the data requests also...
        if (!(request instanceof ITmfEventRequest<?>)) {
            super.queueRequest(request);
            return;
        }

        final TmfTrace<T> trace = this;
        
        Thread thread = new Thread() {
            @Override
            public void run() {
                
//              final long requestStart = System.nanoTime();

                final Integer[] CHUNK_SIZE = new Integer[1];
                CHUNK_SIZE[0] = fIndexPageSize + 1;
                
                final ITmfEventRequest<T> req = (ITmfEventRequest<T>) request;
                
                final Integer[] nbRead = new Integer[1];
                nbRead[0] = 0;

//              final TmfTimestamp[] timestamp = new TmfTimestamp[1];
//              timestamp[0] = new TmfTimestamp(req.getRange().getStartTime());
//              final TmfTimestamp endTS = req.getRange().getEndTime();

                final Boolean[] isFinished = new Boolean[1];
                isFinished[0] = Boolean.FALSE;

                while (!isFinished[0]) {

//                  TmfEventRequest<T> subRequest = new TmfEventRequest<T>(req.getDataType(), new TmfTimeRange(timestamp[0], endTS), 
//                          requestedSize, req.getBlockize(), ExecutionType.LONG)
                    TmfDataRequest<T> subRequest = new TmfDataRequest<T>(req.getDataType(), nbRead[0], CHUNK_SIZE[0],
                            req.getBlockize(), ExecutionType.LONG)
                    {
//                      int count = 0;
                        @Override
                        public void handleData() {
                            T[] data = getData();
//                          if (count == 0) {
//                              System.out.println("First event of the block: " + data[0].getTimestamp());
//                          }
//                          count++;
//                          Tracer.trace("Ndx: " + ((TmfEvent) data[0]).getTimestamp());
                            req.setData(data);
                            req.handleData();
                            if (fNbRead == CHUNK_SIZE[0]) {
                                nbRead[0] += fNbRead;
//                              System.out.println("fNbRead=" + fNbRead + ", count=" + count +", total=" + nbRead[0] + ", TS=" + data[0].getTimestamp());
                            }
                            if (fNbRead > CHUNK_SIZE[0]) {
                                System.out.println("ERROR - Read too many events");
                            }
                        }
                        @Override
                        public void handleCompleted() {
//                          System.out.println("Request completed at: " + timestamp[0]);
                            if (fNbRead < CHUNK_SIZE[0]) {
                                req.done();
                                isFinished[0] = Boolean.TRUE;
                                nbRead[0] += fNbRead;
//                              System.out.println("fNbRead=" + fNbRead + ", count=" + count +", total=" + nbRead[0]);
                            }
                            super.handleCompleted();
                        }
                    };

                    if (!isFinished[0]) {
                        trace.queueRequest(subRequest);

                        try {
                            subRequest.waitForCompletion();
//                          System.out.println("Finished at " + timestamp[0]);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

//                      TmfTimestamp newTS = new TmfTimestamp(timestamp[0].getValue() + 1, timestamp[0].getScale(), timestamp[0].getPrecision());
//                      timestamp[0] = newTS;
                        CHUNK_SIZE[0] = fIndexPageSize;
//                      System.out.println("New timestamp: " + timestamp[0]);
                    }
                }
//              final long requestEnded = System.nanoTime();
//              System.out.println("Background request completed. Elapsed= " + (requestEnded * 1.0 - requestStart) / 1000000000);
            }
        };
        thread.start();
    }

	// ------------------------------------------------------------------------
	// Signal handlers
	// ------------------------------------------------------------------------

	@TmfSignalHandler
	public void traceOpened(TmfTraceOpenedSignal signal) {
	    ITmfTrace trace = signal.getTrace();
	    if (trace == this) {
	        indexTrace(false);
	    }
	}
}
