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
import org.eclipse.linuxtools.tmf.core.filter.ITmfFilter;
import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;

/**
 * An event filter based on the requested event block (start event index +
 * number of requested events).
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 */
public final class TmfBlockFilter implements ITmfFilter {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * Filter for all events by index
     */
    public static final TmfBlockFilter ALL_EVENTS = new TmfBlockFilter(0, Long.MAX_VALUE);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Request start index
     */
    private final long fStartIndex;

    /**
     * Request end index (non-inclusive)
     */
    private final long fEndIndex;

    /**
     * Number of events requested
     */
    private final long fNbRequested;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Standard constructor
     *
     * @param startIndex start index
     * @param nbRequested nb requested events
     */
    public TmfBlockFilter(long startIndex, long nbRequested) {
        fStartIndex  = startIndex  >= 0 ? startIndex  : 0;
        fNbRequested = nbRequested >= 0 ? nbRequested : Long.MAX_VALUE;
        fEndIndex = (Long.MAX_VALUE - fNbRequested) > fStartIndex ? fStartIndex + nbRequested : Long.MAX_VALUE;
    }

    /**
     * Copy constructor
     *
     * @param other the other filter
     */
    public TmfBlockFilter(TmfBlockFilter other) {
        fStartIndex  = other.fStartIndex;
        fEndIndex    = other.fEndIndex;
        fNbRequested = other.fNbRequested;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the filter start index
     */
    public long getStartIndex() {
        return fStartIndex;
    }

    /**
     * @return the filter end index (non-inclusive)
     */
    public long getEndIndex() {
        return fEndIndex;
    }

    /**
     * @return the filter number of events requested
     */
    public long getNbRequested() {
        return fNbRequested;
    }

    // ------------------------------------------------------------------------
    // ITmfFilter
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.filter.ITmfFilter#matches(org.eclipse.linuxtools.tmf.core.event.ITmfEvent)
     */
    @Override
    public boolean matches(ITmfEvent event) {
        long rank = event.getRank();
        return ((rank == ITmfContext.UNKNOWN_RANK) || (rank >= fStartIndex && rank < (fStartIndex + fNbRequested)));
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (fEndIndex ^ (fEndIndex >>> 32));
        result = prime * result + (int) (fNbRequested ^ (fNbRequested >>> 32));
        result = prime * result + (int) (fStartIndex ^ (fStartIndex >>> 32));
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfBlockFilter)) {
            return false;
        }
        TmfBlockFilter other = (TmfBlockFilter) obj;
        if (fEndIndex != other.fEndIndex) {
            return false;
        }
        if (fNbRequested != other.fNbRequested) {
            return false;
        }
        if (fStartIndex != other.fStartIndex) {
            return false;
        }
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfBlockFilter [fStartIndex=" + fStartIndex + ", fEndIndex=" + fEndIndex + ", fNbRequested=" + fNbRequested + "]";
    }

}
