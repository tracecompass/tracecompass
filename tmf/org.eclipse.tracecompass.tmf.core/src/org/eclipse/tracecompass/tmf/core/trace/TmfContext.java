/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 *   Patrick Tasse - Updated for removal of context clone
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.location.ITmfLocation;

/**
 * A basic implementation of ITmfContext.
 * <p>
 * It ties a trace location to an event rank. The context should be enough to
 * restore the trace state so the corresponding event can be read.
 *
 * @author Francois Chouinard
 *
 * @see ITmfLocation
 */
public class TmfContext implements ITmfContext {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The trace location
    private @Nullable ITmfLocation fLocation;

    // The event rank
    private long fRank;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    public TmfContext() {
        this(null, UNKNOWN_RANK);
    }

    /**
     * Simple constructor (unknown rank)
     *
     * @param location the event location
     */
    public TmfContext(final ITmfLocation location) {
        this(location, UNKNOWN_RANK);
    }

    /**
     * Full constructor
     *
     * @param location the event location
     * @param rank the event rank
     */
    public TmfContext(final ITmfLocation location, final long rank) {
        fLocation = location;
        fRank = rank;
    }

    /**
     * Copy constructor
     *
     * @param context the other context
     */
    public TmfContext(final TmfContext context) {
        if (context == null) {
            throw new IllegalArgumentException();
        }
        fLocation = context.fLocation;
        fRank = context.fRank;
    }

    // ------------------------------------------------------------------------
    // ITmfContext
    // ------------------------------------------------------------------------

    @Override
    public @Nullable ITmfLocation getLocation() {
        return fLocation;
    }

    @Override
    public void setLocation(final ITmfLocation location) {
        fLocation = location;
    }

    @Override
    public long getRank() {
        return fRank;
    }

    @Override
    public void setRank(final long rank) {
        fRank = rank;
    }

    @Override
    public void increaseRank() {
        if (hasValidRank()) {
            fRank++;
        }
    }

    @Override
    public boolean hasValidRank() {
        return fRank != UNKNOWN_RANK;
    }

    @Override
    public void dispose() {
        // Do nothing
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        return Objects.hash(fRank, fLocation);
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TmfContext other = (TmfContext) obj;
        if (fRank != other.fRank) {
            return false;
        }
        return (Objects.equals(fLocation, other.fLocation));
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfContext [fLocation=" + fLocation + ", fRank=" + fRank + "]";
    }

}
