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

import java.util.Arrays;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * An event filter based on the event's trace.
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 */
public final class TmfTraceFilter implements ITmfFilter {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Filter for all events by time range
     */
    public  static final TmfTraceFilter ALL_TRACES = new TmfTraceFilter(new ITmfTrace[0]);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /** The traces of interest */
    private final ITmfTrace[] fTraces;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfTraceFilter() {
        this(ALL_TRACES);
    }

    /**
     * Standard constructor
     *
     * @param traces the traces of interest
     */
    public TmfTraceFilter(ITmfTrace[] traces) {
        fTraces = traces;
    }

    /**
     * Copy constructor
     *
     * @param other the other filter
     */
    public TmfTraceFilter(TmfTraceFilter other) {
        fTraces = other.fTraces;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the filter traces
     */
    public ITmfTrace[] getTraces() {
        return fTraces;
    }

    // ------------------------------------------------------------------------
    // ITmfFilter
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.filter.ITmfFilter#matches(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public boolean matches(ITmfEvent event) {
        // The empty set is the universal element
        if (fTraces.length == 0) {
            return true;
        }
        for (ITmfTrace trace : fTraces) {
            if (event.getTrace() == trace) {
                return true;
            }
        }
        return false;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(fTraces);
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
        if (!(obj instanceof TmfTraceFilter)) {
            return false;
        }
        TmfTraceFilter other = (TmfTraceFilter) obj;
        if (!Arrays.equals(fTraces, other.fTraces)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfTraceFilter [fTraces=" + Arrays.toString(fTraces) + "]";
    }

}
