/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * Provider for statistics, which is assigned to a trace. This can be used to
 * populate views like the Statistics View or the Histogram.
 *
 * @author Alexandre Montplaisir
 * @since 2.0
 */
public interface ITmfStatistics {

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
    public long getEventsInRange(ITmfTimestamp start, ITmfTimestamp end);

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
    public Map<String, Long> getEventTypesInRange(ITmfTimestamp start,
            ITmfTimestamp end);
}
