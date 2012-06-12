/*******************************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.trace.ITmfContext;
import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;

/**
 * A pair of trace location and trace rank.
 * 
 * @version 1.0
 * @author Patrick Tasse
 */
public class TmfRankedLocation implements Comparable<TmfRankedLocation>, Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private ITmfLocation<? extends Comparable<?>> fLocation;
    private long fRank;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The standard constructor
     * 
     * @param context a trace context
     */
    public TmfRankedLocation(ITmfContext context) {
        fLocation = context.getLocation().clone();
        fRank = context.getRank();
    }

    /**
     * Private constructor
     * 
     * @param location the trace location
     * @param rank the trace rank
     */
    private TmfRankedLocation(ITmfLocation<? extends Comparable<?>> location, long rank) {
        fLocation = location;
        fRank = rank;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Get the trace location
     * 
     * @return the trace location
     */
    public ITmfLocation<? extends Comparable<?>> getLocation() {
        return fLocation;
    }

    /**
     * Get the trace rank
     * 
     * @return the trace rank
     */
    public long getRank() {
        return fRank;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TmfRankedLocation clone() {
        return new TmfRankedLocation(fLocation.clone(), fRank);
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    /*
     * (non-Javadoc)
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    @Override
    public int compareTo(TmfRankedLocation o) {
        return Long.valueOf(fRank).compareTo(Long.valueOf(o.fRank));
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
        result = prime * result + ((fLocation == null) ? 0 : fLocation.hashCode());
        result = prime * result + (int) (fRank ^ (fRank >>> 32));
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfRankedLocation other = (TmfRankedLocation) obj;
        if (fLocation == null) {
            if (other.fLocation != null) {
                return false;
            }
        } else if (!fLocation.equals(other.fLocation)) {
            return false;
        }
        if (fRank != other.fRank) {
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
        return fLocation + "," + fRank;
    }

}
