/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.request;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.event.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;

/**
 * An event filter based on the requested time range.
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 */
public final class TmfRangeFilter implements ITmfFilter {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Filter for all events by time range
     */
    public static final TmfRangeFilter ALL_EVENTS = new TmfRangeFilter(TmfTimeRange.ETERNITY);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The time range requested */
    private final TmfTimeRange fTimeRange;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param timeRange the time range of interest
     */
    public TmfRangeFilter(TmfTimeRange timeRange) {
        fTimeRange = timeRange != null ? timeRange : TmfTimeRange.ETERNITY;
    }

    /**
     * Copy constructor
     *
     * @param other the other filter
     */
    public TmfRangeFilter(TmfRangeFilter other) {
        fTimeRange = other.fTimeRange;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the filter time range
     */
    public TmfTimeRange getTimeRange() {
        return fTimeRange;
    }

    // ------------------------------------------------------------------------
    // ITmfFilter
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.filter.ITmfFilter#matches(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public boolean matches(ITmfEvent event) {
        return fTimeRange.contains(event.getTimestamp());
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fTimeRange == null) ? 0 : fTimeRange.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfRangeFilter)) {
            return false;
        }
        TmfRangeFilter other = (TmfRangeFilter) obj;
        if (fTimeRange == null) {
            if (other.fTimeRange != null) {
                return false;
            }
        } else if (!fTimeRange.equals(other.fTimeRange)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfRangeFilter [fTimeRange=" + fTimeRange + "]";
    }

}
