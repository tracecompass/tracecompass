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
 * Primitive container for Statistics data
 *
 * Contains information about statistics that can be retrieved with any type of
 * traces
 *
 * There are two counters : one for the total number of events in the trace and
 * another for the number of events in the selected time range
 *
 * @version 2.0
 * @version 2.0
 * @since 2.0
 * @author Mathieu Denis
 */
public class TmfStatistics {

    /**
     * Total number of events.
     *
     * @since 2.0
     */
    protected long fNbEvents = 0;

    /**
     * Number of events within a time range (Partial event count).
     *
     * @since 2.0
     */
    protected long fNbEventsInTimeRange = 0;

    /**
     * @return the total events count
     * @since 2.0
     */
    public long getTotal() {
        return fNbEvents;
    }

    /**
     * @return the partial events count within a time range
     * @since 2.0
     */
    public long getPartial() {
        return fNbEventsInTimeRange;
    }

    /**
     * Increments by one the total number of events.
     *
     * @since 2.0
     */
    public void incrementTotal() {
        ++fNbEvents;
    }

    /**
     * Increments <b>nb</b> times the total number of events.
     *
     * @param nb
     *            Amount that will be added to the total events count. Ignored
     *            if negative.
     * @since 2.0
     */
    public void incrementTotal(int nb) {
        if (nb > 0) {
            fNbEvents += nb;
        }
    }

    /**
     * Increments by one the number of events within a time range (partial events
     * count).
     *
     * @since 2.0
     */
    public void incrementPartial() {
        ++fNbEventsInTimeRange;
    }

    /**
     * Increments <b>nb</b> times the number of events within a time range
     * (partial events count).
     *
     * @param nb
     *            Amount that will be added to the partial events count. Ignored
     *            if negative.
     * @since 2.0
     */
    public void incrementPartial(int nb) {
        if (nb > 0) {
            fNbEventsInTimeRange += nb;
        }
    }

    /**
     * Resets the total number of events.
     *
     * @since 2.0
     */
    public void resetTotalCount() {
        fNbEvents = 0;
    }

    /**
     * Resets the number of events within a time range (partial events count).
     *
     * @since 2.0
     */
    public void resetPartialCount() {
        fNbEventsInTimeRange = 0;
    }
}
