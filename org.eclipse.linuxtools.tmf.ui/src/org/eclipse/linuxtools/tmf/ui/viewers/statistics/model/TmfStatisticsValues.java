/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mathieu Denis <mathieu.denis@polymtl.ca> - Intial API and Implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.ui.viewers.statistics.model;

/**
 * Primitive container for Statistics values.
 *
 * Contains information about statistics that can be retrieved with any type of
 * traces.
 *
 * There are two counters : one for the total number of events in the trace, and
 * another for the number of events in the selected time range.
 *
 * @author Mathieu Denis
 * @version 2.0
 * @since 2.0
 */
public class TmfStatisticsValues {

    /**
     * Total number of events.
     */
    protected long fNbEvents = 0;

    /**
     * Number of events within a time range (Partial event count).
     */
    protected long fNbEventsInTimeRange = 0;

    /**
     * @return the total events count
     */
    public long getTotal() {
        return fNbEvents;
    }

    /**
     * @return the partial events count within a time range
     */
    public long getPartial() {
        return fNbEventsInTimeRange;
    }

    /**
     * Set either the "global" or the "time range" value.
     *
     * @param global
     *            True to set the global value, false for the timerange one.
     * @param nb
     *            The new value to set
     */
    public void setValue(boolean global, long nb) {
        if (nb > 0) {
            if (global) {
                fNbEvents = nb;
            } else {
                fNbEventsInTimeRange = nb;
            }
        }
    }

    /**
     * Resets the total number of events.
     */
    public void resetTotalCount() {
        fNbEvents = 0;
    }

    /**
     * Resets the number of events within a time range (partial events count).
     */
    public void resetPartialCount() {
        fNbEventsInTimeRange = 0;
    }

    @Override
    public String toString() {
        return fNbEvents + ", " + fNbEventsInTimeRange; //$NON-NLS-1$
    }
}
