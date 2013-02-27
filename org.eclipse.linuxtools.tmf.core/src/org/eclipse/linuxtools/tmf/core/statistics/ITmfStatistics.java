/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.statistics;

import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.signal.TmfStatsUpdatedSignal;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;

/**
 * Provider for statistics, which is assigned to a trace. This can be used to
 * populate views like the Statistics View or the Histogram.
 *
 * As a guideline, since any trace type can use this interface, all timestamps
 * should be normalized to nanoseconds when using these methods
 * ({@link ITmfTimestamp#NANOSECOND_SCALE}).
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public interface ITmfStatistics {

    /**
     * This method provides a centralized and asynchronous way of querying
     * statistics information. It is an alternative to the other get* methods,
     * and should not block the caller for too long.
     *
     * Implementors can usually call their own getEventTotal(),
     * getEventsInRange(), etc. but should do so in a separate thread, and
     * should send a {@link TmfStatsUpdatedSignal} whenever they are done (that
     * signal will carry the results).
     *
     * @param isGlobal
     *            Is this for a global query (whole time range of a trace), or
     *            just for a specific time range.
     * @param start
     *            The start time of the query range. Has no effect if isGlobal
     *            is true.
     * @param end
     *            The end time of the query range. Has no effect if isGlobal is
     *            true.
     */
    public void updateStats(boolean isGlobal, long start, long end);

    /**
     * Run a histogram query on the statistics back-end. This means, return the
     * total number of events in a series of 'nb' equal-sized ranges between
     * 'start' and 'end'. As its name implies, this is typically used to fill
     * the histogram data (where each range represents one pixel on the
     * histogram).
     *
     * Unlike {@link #updateStats}, this method will block the caller until the
     * results are returned, so it should not be called from a signal handler or
     * from the UI thread.
     *
     * @param start
     *            Start time of the query
     * @param end
     *            End time of the query
     * @param nb
     *            The number of ranges to separate the complete time range into.
     *            It will be the size() of the returned array.
     * @return The array representing the number of events found in each
     *         sub-range.
     */
    public List<Long> histogramQuery(long start, long end, int nb);

    /**
     * Return the total number of events in the trace.
     *
     * @return The total number of events
     */
    public long getEventsTotal();

    /**
     * Return a Map of the total events in the trace, per event type. The event
     * type should come from ITmfEvent.getType().getName().
     *
     * @return The map of <event_type, count>, for the whole trace
     */
    public Map<String, Long> getEventTypesTotal();

    /**
     * Retrieve the number of events in the trace in a given time interval.
     *
     * @param start
     *            Start time of the time range
     * @param end
     *            End time of the time range
     * @return The number of events found
     */
    public long getEventsInRange(long start, long end);

    /**
     * Retrieve the number of events in the trace, per event type, in a given
     * time interval.
     *
     * @param start
     *            Start time of the time range
     * @param end
     *            End time of the time range
     * @return The map of <event_type, count>, for the given time range
     */
    public Map<String, Long> getEventTypesInRange(long start, long end);

    /**
     * Notify the statistics back-end that the trace is being closed, so it
     * should dispose itself as appropriate (release file descriptors, etc.)
     */
    public void dispose();
}
