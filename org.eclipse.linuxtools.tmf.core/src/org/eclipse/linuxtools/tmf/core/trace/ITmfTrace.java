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
import org.eclipse.linuxtools.tmf.core.component.ITmfDataProvider;
import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;

/**
 * <b><u>ITmfTrace</u></b>
 * <p>
 * The event stream structure in TMF. In its basic form, a trace has:
 * <ul>
 * <li> the associated Eclipse resource
 * <li> the path to its location on the file system
 * <li> the type of the events it contains
 * <li> the number of events it contains
 * <li> the time range (span) of the events it contains
 * </ul>
 * Concrete ITmfTrace classes have to provide a parameter-less constructor and
 * an initialization method (initTace())if they are to be opened from the
 * Project View. Also, a validation (validate()) method has to be provided to
 * ensure that the trace is of the correct type.
 * <p>
 * A trace can be accessed simultaneously from multiple threads by various
 * application components. To avoid obvious multi-threading issues, the trace
 * uses an ITmfContext as a synchronization aid for its read operations.
 * <p>
 * A proper ITmfContext can be obtained by performing a seek operation on the
 * trace. Seek operations can be performed for a particular event (by rank or
 * timestamp) or for a plain trace location.
 * <p>
 * <b>Example 1</b>: Read a whole trace
 * <pre>
 * ITmfContext context = trace.seekLocationt(null);
 * ITmfEvent event = trace.getEvent(context);
 * while (event != null) {
 *     // Do something ...
 *     event = trace.getEvent(context);
 * }
 * </pre>
 * <b>Example 2</b>: Process 50 events starting from the 1000th event
 * <pre>
 * int nbEventsRead = 0;
 * ITmfContext context = trace.seekEvent(1000);
 * ITmfEvent event = trace.getEvent(context);
 * while (event != null && nbEventsRead < 50) {
 *     nbEventsRead++;
 *     // Do something ...
 *     event = trace.getEvent(context);
 * }
 * </pre>
 * <b>Example 3</b>: Process the events between 2 timestamps (inclusive)
 * <pre>
 * ITmfTimestamp startTime = ...;
 * ITmfTimestamp endTime = ...;
 * ITmfContext context = trace.seekEvent(startTime);
 * ITmfEvent event = trace.getEvent(context);
 * while (event != null && event.getTimestamp().compareTo(endTime) <= 0) {
 *     // Do something ...
 *     event = trace.getEvent(context);
 * }
 * </pre>
 */
public interface ITmfTrace<T extends ITmfEvent> extends ITmfDataProvider<T> {

    // ------------------------------------------------------------------------
    // Initializers
    // ------------------------------------------------------------------------

    /**
     * Initialize a newly instantiated "empty" trace object. This is used to
     * properly parameterize an ITmfTrace instantiated with its parameterless
     * constructor.
     * 
     * Typically, the parameterless constructor will provide the block size
     * and its associated parser and indexer.
     * 
     * @param resource the trace resource
     * @param path the trace path
     * @param type the trace event type
     * @throws FileNotFoundException
     */
    public void initTrace(IResource resource, String path, Class<T> type) throws FileNotFoundException;

    /**
     * Validate that the trace is of the correct type.
     * 
     * @param project the eclipse project
     * @param path the trace path
     * 
     * @return true if trace is valid
     */
    public boolean validate(IProject project, String path);

    // ------------------------------------------------------------------------
    // Basic getters
    // ------------------------------------------------------------------------

    /**
     * @return the trace event type
     */
    public Class<T> getType();

    /**
     * @return the associated trace resource
     */
    public IResource getResource();

    /**
     * @return the trace path
     */
    public String getPath();

    // ------------------------------------------------------------------------
    // Trace characteristics getters
    // ------------------------------------------------------------------------

    /**
     * @return the number of events in the trace
     */
    public long getNbEvents();

    /**
     * @return the trace time range
     */
    public TmfTimeRange getTimeRange();

    /**
     * @return the timestamp of the first trace event
     */
    public ITmfTimestamp getStartTime();

    /**
     * @return the timestamp of the last trace event
     */
    public ITmfTimestamp getEndTime();

    // ------------------------------------------------------------------------
    // Trace positioning getters
    // ------------------------------------------------------------------------

    /**
     * @return the current trace location
     */
    public ITmfLocation<?> getCurrentLocation();

    /**
     * Returns the ratio (proportion) corresponding to the specified location.
     * 
     * @param location a trace specific location
     * @return a floating-point number between 0.0 (beginning) and 1.0 (end)
     */
    public double getLocationRatio(ITmfLocation<?> location);

    // ------------------------------------------------------------------------
    // Seek operations (returning a reading context)
    // ------------------------------------------------------------------------

    /**
     * Position the trace at the specified location. The null location
     * is used to indicate that the first trace event is requested.
     * <p>
     * <ul>
     * <li> a <b>null</b> location returns the context of the first event
     * <li> an invalid location, including beyond the last event, returns a null context
     * </ul>
     * <p>
     * @param location the trace specific location (null for 1st event)
     * @return a context which can later be used to read the corresponding event
     */
    public ITmfContext seekLocation(ITmfLocation<?> location);

    /**
     * Position the trace at the event located at the specified ratio in the
     * trace file.
     * <p>
     * The notion of ratio (0.0 <= r <= 1.0) is trace specific and left
     * voluntarily vague. Typically, it would refer to the event proportional
     * rank (arguably more intuitive) or timestamp in the trace file.
     * 
     * @param ratio the proportional 'rank' in the trace
     * @return a context which can later be used to read the corresponding event
     */
    public ITmfContext seekLocation(double ratio);

    /**
     * Position the trace at the first event with the specified timestamp. If
     * there is no event with the requested timestamp, a context pointing to
     * the next chronological event is returned.
     * <p>
     * A null timestamp is interpreted as seeking for the first event of the
     * trace.
     * <p>
     * If the requested timestamp is beyond the last trace event, the context
     * returned will yield a null event if used in a subsequent read.
     * 
     * @param timestamp the timestamp of desired event
     * @return a context which can later be used to read the corresponding event
     */
    public ITmfContext seekEvent(ITmfTimestamp timestamp);

    /**
     * Position the trace at the 'rank'th event in the trace.
     * <p>
     * If the requested rank is beyond the last trace event, the context
     * returned will yield a null event if used in a subsequent read.
     * 
     * @param rank the event rank
     * @return a context which can later be used to read the corresponding event
     */
    public ITmfContext seekEvent(long rank);

    // ------------------------------------------------------------------------
    // Read operations (returning an actual event)
    // ------------------------------------------------------------------------

    /**
     * Return the event pointed by the supplied context (or null if no event
     * left) and updates the context to point the next event.
     * 
     * @param context the read context (will be updated)
     * @return the event pointed to by the context
     */
    public ITmfEvent getNextEvent(ITmfContext context);

    /**
     * Return the event pointed by the supplied context (or null if no event
     * left) and *does not* update the context.
     * 
     * @param context the read context
     * @return the next event in the stream
     */
    public ITmfEvent parseEvent(ITmfContext context);

    // ------------------------------------------------------------------------
    // ------------------------------------------------------------------------

    /**
     * @return the trace index page size
     */
    public int getIndexPageSize();

    /**
     * @return the streaming interval in ms (0 if not a streaming trace)
     */
    public long getStreamingInterval();

}
