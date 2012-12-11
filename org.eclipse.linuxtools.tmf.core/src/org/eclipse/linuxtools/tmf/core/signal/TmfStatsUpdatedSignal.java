/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.signal;

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Signal sent when a call to {@link ITmfStatistics#updateStats} has completed.
 *
 * @author Alexandre Montplaisir
 * @version 1.0
 * @since 2.0
 */
public class TmfStatsUpdatedSignal extends TmfSignal {

    private final ITmfTrace trace;
    private final boolean isGlobal;
    private final long eventTotal;
    private final Map<String, Long> eventsPerType;

    /**
     * Constructor
     *
     * @param source
     *            Object sending this signal
     * @param trace
     *            The trace for which we track the statistics
     * @param isGlobal
     *            Is this a global query (whole range of the trace), or not
     * @param eventTotal
     *            The total number of events. This should be equal to the sum of
     *            the values in eventsPerType.
     * @param eventsPerType
     *            The map representing the number of events of each type
     *
     * @since 2.0
     */
    public TmfStatsUpdatedSignal(Object source, ITmfTrace trace, boolean isGlobal,
            long eventTotal, Map<String, Long> eventsPerType) {
        super(source);
        this.trace = trace;
        this.isGlobal = isGlobal;
        this.eventTotal = eventTotal;
        this.eventsPerType = eventsPerType;
    }

    /**
     * @return The trace referred to by this signal
     */
    public ITmfTrace getTrace() {
        return trace;
    }

    /**
     * @return True if it's a global query, false if it's for the current time
     *         range
     */
    public boolean isGlobal() {
        return isGlobal;
    }

    /**
     * @return The total number of events for this query
     */
    public long getEventTotal() {
        return eventTotal;
    }

    /**
     * @return The map representing the number of events per type for this query
     */
    public Map<String, Long> getEventsPerType() {
        return eventsPerType;
    }

    @Override
    public String toString() {
        return "[TmfStatsUpdatedSignal (trace = " + trace.toString() + //$NON-NLS-1$
                ", total = " + eventTotal + ")]"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
