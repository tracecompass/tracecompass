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

package org.eclipse.linuxtools.tmf.stream;

import java.util.Map;

import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.trace.TmfTrace;

/**
 * <b><u>ITmfEventStream</u></b>
 * <p>
 * TODO: Implement me. Please.
 */
public interface ITmfEventStream {

	public class StreamContext {
		Object location;
		public StreamContext(Object loc) {
			location = loc;
		}
	}
    
	public int getNbEvents();
    
    public TmfTimeRange getTimeRange();

    public Map<String, Object> getAttributes();

    /**
     * Positions the stream at the first event with timestamp.
     * 
     * @param timestamp
     * @return a context object for subsequent reads
     */
    public StreamContext seekEvent(TmfTimestamp timestamp);

    /**
     * Positions the stream on the event at the wanted position.
     * 
     * @param index
     * @return a context object for subsequent reads
     */
    public StreamContext seekEvent(int index);

    /**
     * Reads and the next event on the stream and updates the context.
     * If there is no event left, return null.
     * 
     * @return the next event in the stream
     */
    public TmfEvent getNextEvent(StreamContext context);

    public TmfEvent getEvent(StreamContext context, TmfTimestamp timestamp);
    public TmfEvent getEvent(StreamContext context, int index);

    /**
     * Parse the stream and creates the checkpoint structure.
     * Normally performed once at the creation of the event stream.
     */
    public void indexStream(boolean waitForCompletion);

    public Object getCurrentLocation();
    public StreamContext seekLocation(Object location);

	public void addListener(TmfTrace tmfEventLog);
	public void removeListener(TmfTrace tmfEventLog);
}
