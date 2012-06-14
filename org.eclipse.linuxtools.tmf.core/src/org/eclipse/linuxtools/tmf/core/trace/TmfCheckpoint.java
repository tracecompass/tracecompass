/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Trace Model 1.0
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.trace;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;

/**
 * A basic implementation of ITmfCheckpoint. It simply maps an event timestamp
 * to a generic location.
 * 
 * @version 1.0
 * @author Francois Chouinard
 *
 * @see ITmfLocation
 * @see ITmfTimestamp
 */
public class TmfCheckpoint implements ITmfCheckpoint, Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    // The checkpoint context
    private ITmfContext fContext;

    // The checkpoint timestamp
    private ITmfTimestamp fTimestamp;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    @SuppressWarnings("unused")
    private TmfCheckpoint() {
    }

    /**
     * Full constructor
     * 
     * @param timestamp the checkpoint timestamp
     * @param location the corresponding trace location
     */
    public TmfCheckpoint(final ITmfTimestamp timestamp, final ITmfContext context) {
        fTimestamp = timestamp;
        fContext = context;
    }

    /**
     * Copy constructor
     * 
     * @param other the other checkpoint
     */
    public TmfCheckpoint(final TmfCheckpoint other) {
        if (other == null) {
            throw new IllegalArgumentException();
        }
        fTimestamp = other.fTimestamp;
        fContext = other.fContext;
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TmfCheckpoint clone() {
        TmfCheckpoint clone = null;
        try {
            clone = (TmfCheckpoint) super.clone();
            clone.fContext = (fContext != null) ? fContext.clone() : null;
            clone.fTimestamp = (fTimestamp != null) ? fTimestamp.clone() : null;
        } catch (final CloneNotSupportedException e) {
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // ITmfCheckpoint
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfCheckpoint#getTimestamp()
     */
    @Override
    public ITmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfCheckpoint#getLocation()
     */
    @Override
    public ITmfContext getContext() {
        return fContext;
    }

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfCheckpoint#getLocation()
     */
    @Override
    public ITmfLocation<? extends Comparable<?>> getLocation() {
        return fContext.getLocation();
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfCheckpoint#compareTo(org.eclipse.linuxtools.tmf.core.trace.ITmfCheckpoint)
     * 
     * Compares the checkpoints timestamp. If either is null, compares the
     * trace checkpoints locations.
     */
    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compareTo(final ITmfCheckpoint other) {
        if (fTimestamp == null || other.getTimestamp() == null) {
            final Comparable location1 = getLocation().getLocation();
            final Comparable location2 = other.getLocation().getLocation();
            return location1.compareTo(location2);
        }
        return fTimestamp.compareTo(other.getTimestamp(), false);
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
        result = prime * result + ((fContext == null) ? 0 : fContext.hashCode());
        result = prime * result + ((fTimestamp == null) ? 0 : fTimestamp.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof TmfCheckpoint)) {
            return false;
        }
        final TmfCheckpoint other = (TmfCheckpoint) obj;
        if (fContext == null) {
            if (other.fContext != null) {
                return false;
            }
        } else if (!fContext.equals(other.fContext)) {
            return false;
        }
        if (fTimestamp == null) {
            if (other.fTimestamp != null) {
                return false;
            }
        } else if (!fTimestamp.equals(other.fTimestamp)) {
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
        return "TmfCheckpoint [fContext=" + fContext + ", fTimestamp=" + fTimestamp + "]";
    }

}
