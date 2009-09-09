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
 * <b><u>ITmfEventStream</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public interface ITmfTrace {

	/**
	 * <b><u>StreamContext</u></b>
	 * <p>
	 * Stream context keeper to avoid conflicting, concurrent accesses to the
	 * underlying stream. 
	 */
	public class StreamContext {
		public Object location;
		public int    index;

		public StreamContext(Object loc, int ind) {
			location = loc;
			index = ind;
		}

		public StreamContext(StreamContext other) {
			if (other != null) {
				location = other.location;
				index = other.index;
			}
		}
	}
    
	/**
	 * @return 
	 */
	public String getName();
    
	/**
	 * @return the number of events in the stream
	 */
	public int getNbEvents();
    
	/**
	 * @return the stream time range
	 */
    public TmfTimeRange getTimeRange();

//	/**
//	 * @return The stream time range
//	 */
//    public Map<String, Object> getAttributes();

    /**
     * Positions the stream at the first event with timestamp.
     * 
     * @param timestamp
     * @return a context object for subsequent reads
     */
    public StreamContext seekEvent(TmfTimestamp timestamp);
    public StreamContext seekEvent(int index);

    /**
     * Reads and the next event on the stream and updates the context.
     * If there is no event left, return null.
     * 
     * @return the next event in the stream
     */
    public TmfEvent peekEvent(StreamContext context);
    public TmfEvent getEvent(StreamContext context, TmfTimestamp timestamp);
    public TmfEvent getEvent(StreamContext context, int index);
    public TmfEvent getNextEvent(StreamContext context);

    /**
     * Parse the stream and creates the checkpoint structure.
     * Normally invoked once at the creation of the event stream.
     */
    public void indexStream(boolean waitForCompletion);

    public Object getCurrentLocation();
    public StreamContext seekLocation(Object location);

    /**
     * Returns the index of the event at that timestamp
     * 
     * @param timestamp
     * @return
     */
	public int getIndex(TmfTimestamp timestamp); 

	/**
	 * Returns the timestamp of the event at position [index]
	 * 
	 * @param index the event index
	 * @return the corresponding timestamp
	 */
    public TmfTimestamp getTimestamp(int index);
}
