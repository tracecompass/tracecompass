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
 * A abstract implementation of ITmfLocation. The concrete classes must provide
 * comparable location information.
 *
 * @version 2.0
 * @author Francois Chouinard
 */
public abstract class TmfLocation implements ITmfLocation {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final Comparable<?> fLocationInfo;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor (for the 'null' location)
     */
    @SuppressWarnings("unused")
    private TmfLocation() {
        fLocationInfo = null;
    }

    /**
     * Standard constructor.
     *
     * @param locationInfo the concrete trace location
     */
    public TmfLocation(final Comparable<?> locationInfo) {
        fLocationInfo = locationInfo;
    }

    /**
     * Copy constructor
     *
     * @param location the original trace location
     */
    public TmfLocation(final TmfLocation location) {
        fLocationInfo = location.fLocationInfo;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see org.eclipse.linuxtools.tmf.core.trace.ITmfLocation#getLocationInfo()
     */
    /**
     * @since 2.0
     */
    @Override
    public Comparable<?> getLocationInfo() {
        return fLocationInfo;
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
        result = prime * result + ((fLocationInfo != null) ? fLocationInfo.hashCode() : 0);
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        final TmfLocation other = (TmfLocation) obj;
        if (fLocationInfo == null) {
            if (other.fLocationInfo != null) {
                return false;
            }
        } else if (!fLocationInfo.equals(other.fLocationInfo)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfLocation [fLocation=" + fLocationInfo + "]";
    }

}
