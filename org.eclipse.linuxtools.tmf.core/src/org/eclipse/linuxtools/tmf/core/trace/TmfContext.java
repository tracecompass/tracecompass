/*******************************************************************************
 * Copyright (c) 2009, 2010, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
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

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The trace location
    private ITmfLocation<? extends Comparable<?>> fLocation;

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
    public TmfContext(ITmfLocation<? extends Comparable<?>> location) {
        this(location, UNKNOWN_RANK);
    }

    /**
     * Full constructor
     * 
     * @param location the event location
     * @param rank the event rank
     */
    public TmfContext(ITmfLocation<? extends Comparable<?>> location, long rank) {
        fLocation = location;
        fRank = rank;
    }

    /**
     * Copy constructor
     * 
     * @param context the other context
     */
    public TmfContext(TmfContext context) {
        this(context.fLocation, context.fRank);
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
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

    // ------------------------------------------------------------------------
    // ITmfContext
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#getLocation()
     */
    @Override
    public ITmfLocation<? extends Comparable<?>> getLocation() {
        return fLocation;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#setLocation(org.eclipse.linuxtools.tmf.core.trace.ITmfLocation)
     */
    @Override
    public void setLocation(ITmfLocation<? extends Comparable<?>> location) {
        fLocation = location;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#getRank()
     */
    @Override
    public long getRank() {
        return fRank;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#setRank(long)
     */
    @Override
    public void setRank(long rank) {
        fRank = rank;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#increaseRank()
     */
    @Override
    public void increaseRank() {
        if (hasValidRank())
            fRank++;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#hasValidRank()
     */
    @Override
    public boolean hasValidRank() {
        return fRank != UNKNOWN_RANK;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfContext#dispose()
     */
    @Override
    public void dispose() {
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
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TmfContext other = (TmfContext) obj;
        if (fLocation == null) {
            if (other.fLocation != null)
                return false;
        } else if (!fLocation.equals(other.fLocation))
            return false;
        if (fRank != other.fRank)
            return false;
        return true;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfContext [fLocation=" + fLocation + ", fRank=" + fRank + "]";
    }

}
