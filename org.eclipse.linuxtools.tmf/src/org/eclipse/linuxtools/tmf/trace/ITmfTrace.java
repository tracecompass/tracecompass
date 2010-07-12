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

import org.eclipse.linuxtools.tmf.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.event.TmfEvent;
import org.eclipse.linuxtools.tmf.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>ITmfTrace</u></b>
 * <p>
 */
public interface ITmfTrace extends ITmfComponent {
	
	public ITmfTrace createTraceCopy();
	
	/**
	 * @return the trace path 
	 */
	public String getPath();
    
	/**
	 * @return the trace name 
	 */
	public String getName();

    /**
     * @return the cache size 
     */
    public int getCacheSize();

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
     * @param data.timestamp
     * @param data.index
     * @return a context object for subsequent reads
     */
    public TmfContext seekLocation(ITmfLocation<?> location);
    public TmfContext seekEvent(TmfTimestamp timestamp);
    public TmfContext seekEvent(long rank);

    /**
     * Returns the rank of the first event with the requested timestamp.
     * If none, returns the index of the next event (if any).
     *  
     * @param timestamp
     * @return
     */
    public long getRank(TmfTimestamp timestamp);
    
    /**
     * Return the event pointed by the supplied context (or null if
     * no event left) and updates the context to the next event.
     * 
     * @return the next event in the stream
     */
    public TmfEvent getNextEvent(TmfContext context);

    /**
     * Return the event pointed by the supplied context (or null if
     * no event left) and *does not* update the context.
     * 
     * @return the next event in the stream
     */
    public TmfEvent parseEvent(TmfContext context);

}
