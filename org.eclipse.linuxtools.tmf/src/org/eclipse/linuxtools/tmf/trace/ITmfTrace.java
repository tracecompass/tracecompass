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
	 * <b><u>TmfTraceContext</u></b>
	 * <p>
	 * Stream context keeper. It ties an event index to a stream location
	 * which can later be restored. Used to prevent conflicting, concurrent
	 * accesses to the trace. 
	 */
	public class TmfTraceContext {
		public Object       location;
		public TmfTimestamp timestamp;
		public long         index;

		public TmfTraceContext(Object loc, TmfTimestamp ts, long ind) {
			location = loc;
			timestamp = (ts != null) ? ts : TmfTimestamp.BigBang;
			index = ind;
		}

		public TmfTraceContext(TmfTraceContext other) {
			this(other.location, other.timestamp, other.index);
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
	public long getNbEvents();
    
	/**
	 * Trace time range accessors
	 */
	public TmfTimeRange getTimeRange();
    public TmfTimestamp getStartTime();
    public TmfTimestamp getEndTime();

    /**
     * Positions the trace at the first event with the specified
     * timestamp or index (i.e. the nth event in the trace).
     * 
     * Returns a context which can later be used to read the event.
     * 
     * @param timestamp
     * @param index
     * @return a context object for subsequent reads
     */
    public TmfTraceContext seekLocation(Object location);
    public TmfTraceContext seekEvent(TmfTimestamp timestamp);
    public TmfTraceContext seekEvent(long index);

    /**
     * These functions return the event pointed by the supplied context
     * (or null if no event left).
     * 
     * - <code>getEvent()</code> consumes and returns the event at the
     * specified timestamp/index. Updates the stream context.
     * 
     * - <code>getNextEvent()</code> consumes and returns the next event in
     * the trace. Updates the stream context.
     * 
     * @return the next event in the stream
     */
    public TmfEvent getNextEvent(TmfTraceContext context);

}
