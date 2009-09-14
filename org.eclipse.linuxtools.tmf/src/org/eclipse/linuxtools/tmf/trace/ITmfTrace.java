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

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>ITmfTrace</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public interface ITmfTrace {

	/**
	 * <b><u>StreamContext</u></b>
	 * <p>
	 * Stream context keeper. Used to prevent conflicting, concurrent accesses
	 * to the underlying trace. 
	 */
	public class TmfTraceContext {
		public Object location;
		public int    index;

		public TmfTraceContext(Object loc, int ind) {
			location = loc;
			index = ind;
		}

		public TmfTraceContext(TmfTraceContext other) {
			if (other != null) {
				location = other.location;
				index = other.index;
			}
		}
	}
    
	/**
	 * @return the trace path 
	 */
	public String getPath();
    
	/**
	 * @return the trace name 
	 */
	public String getName();

	/**
	 * @return the number of events in the trace
	 */
	public int getNbEvents();
    
	/**
	 * Trace time range handlers
	 */
	public void setTimeRange(TmfTimeRange range);
	public void setStartTime(TmfTimestamp startTime);
	public void setEndTime(TmfTimestamp endTime);

	public TmfTimeRange getTimeRange();
    public TmfTimestamp getStartTime();
    public TmfTimestamp getEndTime();

    /**
     * Positions the trace at the first event with the specified
     * timestamp or index (i.e. the nth event in the trace)
     * 
     * @param timestamp
     * @param index
     * @return a context object for subsequent reads
     */
    public TmfTraceContext seekEvent(TmfTimestamp timestamp);
    public TmfTraceContext seekEvent(int index);

    /**
     * These functions handle the mapping between an abstract trace
     * and the actual implementation.
     * 
     * <code>parseEvent()</code> parses the event at the current
     * trace location.
     * 
     * <code>processEvent()</code> is a hook for application
     * specific processing once the event has been read.
     */
    public Object getCurrentLocation();
    public TmfTraceContext seekLocation(Object location);
    public TmfEvent parseNextEvent();
	public void processEvent(TmfEvent event);

    /**
     * These functions return the event pointed by the supplied context
     * (or null if no event left).
     * 
     * The context is updated to point to the next trace event, expect
     * for tpeekEvent() which doesn't update the context.
     * 
     * @return the next event in the stream
     */
    public TmfEvent peekEvent(TmfTraceContext context);
    public TmfEvent getEvent(TmfTraceContext context, TmfTimestamp timestamp);
    public TmfEvent getEvent(TmfTraceContext context, int index);
    public TmfEvent getNextEvent(TmfTraceContext context);

    /**
     * Index the stream and creates the checkpoint structure.
     * Normally invoked once at the creation of the event stream.
     */
    public void indexStream();

    /**
     * Returns the index of the first event at the supplied timestamp.
     * If there is no such event, return the next one (null if none left).
     * 
     * @param timestamp
     * @return
     */
	public int getIndex(TmfTimestamp timestamp); 

	/**
	 * Returns the timestamp of the event at position [index]
	 * (null if none left). 
	 * 
	 * @param index the event index
	 * @return the corresponding timestamp
	 */
    public TmfTimestamp getTimestamp(int index);
}
