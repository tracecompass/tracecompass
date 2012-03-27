/*******************************************************************************
 * Copyright (c) 2009, 2011, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import java.io.FileNotFoundException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.linuxtools.tmf.core.component.ITmfComponent;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * <b><u>ITmfTrace</u></b>
 * <p>
 * The basic event trace structure in the TMF.
 */
public interface ITmfTrace<T extends ITmfEvent> extends ITmfComponent {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Initializers
    // ------------------------------------------------------------------------

    // initTrace variants
    public void initTrace(String name, String path, Class<T> eventType, int pageSize) throws FileNotFoundException;

    public void indexTrace(boolean waitForCompletion);

    // Trace type validation
    public boolean validate(IProject project, String path);

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the trace path
     */
    public String getPath();

    /**
     * @return the trace name
     */
    @Override
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
     * Trace time range accesses
     */
    public TmfTimeRange getTimeRange();

    public ITmfTimestamp getStartTime();

    public ITmfTimestamp getEndTime();

    /**
     * @return the streaming interval in ms (0 if not streaming)
     */
    public long getStreamingInterval();

    // ------------------------------------------------------------------------
    // Seek operations
    // ------------------------------------------------------------------------

    /**
     * Positions the trace at the first event with the specified timestamp or index (i.e. the nth event in the trace).
     * 
     * Returns a context which can later be used to read the event.
     * 
     * @param location
     * @return a context object for subsequent reads
     */
    public ITmfContext seekLocation(ITmfLocation<?> location);

    public ITmfContext seekEvent(ITmfTimestamp timestamp);

    public ITmfContext seekEvent(long rank);

    /**
     * Positions the trace at the event located at the specified ratio.
     * 
     * Returns a context which can later be used to read the event.
     * 
     * @param ratio
     *            a floating-point number between 0.0 (beginning) and 1.0 (end)
     * @return a context object for subsequent reads
     */
    public ITmfContext seekLocation(double ratio);

    // ------------------------------------------------------------------------
    // Read operations
    // ------------------------------------------------------------------------

    /**
     * Return the event pointed by the supplied context (or null if no event left) and updates the context to the next
     * event.
     * 
     * @return the next event in the stream
     */
    public ITmfEvent getNextEvent(ITmfContext context);

    /**
     * Return the event pointed by the supplied context (or null if no event left) and *does not* update the context.
     * 
     * @return the next event in the stream
     */
    public ITmfEvent parseEvent(ITmfContext context);


    // ------------------------------------------------------------------------
    // misc
    // ------------------------------------------------------------------------

    /**
     * Returns the ratio corresponding to the specified location.
     * 
     * @param location
     *            a trace location
     * @return a floating-point number between 0.0 (beginning) and 1.0 (end)
     */
    public double getLocationRatio(ITmfLocation<?> location);

    public ITmfLocation<?> getCurrentLocation();

    /**
     * Returns the rank of the first event with the requested timestamp. If none, returns the index of the next event
     * (if any).
     * 
     * @param timestamp the requested event timestamp
     * @return the corresponding event rank
     */
    public long getRank(ITmfTimestamp timestamp);

    /**
     * Set the resource used for persistent properties on this trace
     * @param resource the properties resource
     */
    public void setResource(IResource resource);

    /**
     * Get the resource used for persistent properties on this trace
     * @return the properties resource or null if none is set
     */
    public IResource getResource();

}
