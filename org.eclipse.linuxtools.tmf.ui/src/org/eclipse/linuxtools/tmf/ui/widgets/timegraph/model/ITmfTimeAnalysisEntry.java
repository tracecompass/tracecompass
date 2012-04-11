/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Alvaro Sanchez-Leon - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.widgets.timegraph.model;

import java.util.Iterator;
import java.util.Vector;

public interface ITmfTimeAnalysisEntry {
	
    public String getGroupName();

	public int getId();

	public String getName();

	public long getStartTime();

	public long getStopTime();

    /**
     * Get a vector containing all events
     * @deprecated replaced by {@link #getTraceEventsIterator()}
     */
    @Deprecated public <T extends ITimeEvent> Vector<T> getTraceEvents();
    
    /**
     * Get an iterator which returns all events
     */
    public <T extends ITimeEvent> Iterator<T> getTraceEventsIterator();
    
    /**
     * Get an iterator which only returns events that fall within the start time and the stop time.
     * The visible duration is the event duration below which further detail is not discernible.
     * If no such iterator is implemented, provide a basic iterator which returns all events.
     * 
     * @param startTime start time in nanoseconds
     * @param stopTime stop time in nanoseconds
     * @param visibleDuration duration of one pixel in nanoseconds
     */
    public <T extends ITimeEvent> Iterator<T> getTraceEventsIterator(long startTime, long stopTime, long visibleDuration);
    
    public <T extends ITimeEvent> void addTraceEvent(T event);
}
