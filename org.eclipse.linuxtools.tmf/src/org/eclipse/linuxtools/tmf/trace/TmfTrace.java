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
import org.eclipse.linuxtools.tmf.request.ITmfEventRequest;

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
    public static final int DEFAULT_CACHE_SIZE = 1000;

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
    private TmfTimeRange fTimeRange = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigBang);

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * @param path
     * @throws FileNotFoundException
     */
    protected TmfTrace(String name, Class<T> type, String path) throws FileNotFoundException {
    	this(name, type, path, DEFAULT_CACHE_SIZE);
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
        fIndexPageSize = (cacheSize > 0) ? cacheSize : DEFAULT_CACHE_SIZE;

        try {
			fClone = clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @SuppressWarnings("unchecked")
	@Override
	public TmfTrace<T> clone() throws CloneNotSupportedException {
    	TmfTrace<T> clone = (TmfTrace<T>) super.clone();
    	clone.fCheckpoints = (Vector<TmfCheckpoint>) fCheckpoints.clone(); 
    	clone.fTimeRange = new TmfTimeRange(fTimeRange); 
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

    @SuppressWarnings("unchecked")
	public Vector<TmfCheckpoint> getCheckpoints() {
    	return (Vector<TmfCheckpoint>) fCheckpoints.clone();
    }

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
			context.setLocation(getCurrentLocation());
			updateIndex(context, context.getRank(), event.getTimestamp());
			context.updateRank(1);
			processEvent(event);
		}
    	return event;
	}

	public synchronized void updateIndex(ITmfContext context, long rank, TmfTimestamp timestamp) {
		// Build the index as we go along
		if (context.isValidRank() && (rank % fIndexPageSize) == 0) {
			// Determine the table position
			long position = rank / fIndexPageSize;
			// Add new entry at proper location (if empty) 
			if (fCheckpoints.size() == position) {
				ITmfLocation<?> location = getCurrentLocation().clone();
				fCheckpoints.add(new TmfCheckpoint(timestamp, location));
//				System.out.println(getName() + "[" + (fCheckpoints.size() - 1) + "] " + timestamp + ", " + location.toString());
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
   
}
