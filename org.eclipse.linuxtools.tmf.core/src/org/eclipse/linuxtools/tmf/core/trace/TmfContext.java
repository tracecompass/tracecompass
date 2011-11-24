/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

/**
 * <b><u>TmfContext</u></b>
 * <p>
 * Trace context structure. It ties a trace location to an event rank. The
 * context should be enough to restore the trace state so the corresponding
 * event can be read.
 */
public class TmfContext implements ITmfContext, Cloneable {

    private ITmfLocation<? extends Comparable<?>> fLocation;
    private long fRank;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    public TmfContext(ITmfLocation<? extends Comparable<?>> loc, long rank) {
        fLocation = loc;
        fRank = rank;
    }

    public TmfContext(ITmfLocation<? extends Comparable<?>> location) {
        this(location, UNKNOWN_RANK);
    }

    public TmfContext(TmfContext other) {
        this(other.fLocation, other.fRank);
    }

    public TmfContext() {
        this(null, UNKNOWN_RANK);
    }

    // ------------------------------------------------------------------------
    // ITmfContext
    // ------------------------------------------------------------------------

    @Override
    public void dispose() {
        // override if necessary
    }

    @Override
    public void setLocation(ITmfLocation<? extends Comparable<?>> location) {
        fLocation = location;
    }

    @Override
    public ITmfLocation<? extends Comparable<?>> getLocation() {
        return fLocation;
    }

    @Override
    public void setRank(long rank) {
        fRank = rank;
    }

    @Override
    public long getRank() {
        return fRank;
    }

    @Override
    public void updateRank(int delta) {
        if (isValidRank())
            fRank += delta;
    }

    @Override
    public boolean isValidRank() {
        return fRank != UNKNOWN_RANK;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        int result = 17;
        result = 37 * result + fLocation.hashCode();
        result = 37 * result + (int) (fRank ^ (fRank >>> 32));
        return result;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof TmfContext)) {
            return false;
        }
        TmfContext o = (TmfContext) other;
        return fLocation.equals(o.fLocation) && (fRank == o.fRank);
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "[TmfContext(" + fLocation.toString() + "," + fRank + ")]";
    }

    @Override
    public TmfContext clone() {
        TmfContext clone = null;
        try {
            clone = (TmfContext) super.clone();
            clone.fLocation = fLocation.clone();
            clone.fRank = fRank;
        } catch (CloneNotSupportedException e) {
        }
        return clone;
    }

}
