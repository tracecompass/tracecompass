/*******************************************************************************
 * Copyright (c) 2011, 2012 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Patrick Tasse - Initial API and implementation
 * Francois Chouinard - Put in shape for 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace;

import java.util.Arrays;

import org.eclipse.linuxtools.tmf.core.trace.ITmfLocation;


/**
 * A convenience class to store trace location arrays. The main purpose is to
 * provide a Comparable implementation for TmfExperimentLocation.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public final class TmfLocationArray implements Comparable<TmfLocationArray> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ITmfLocation[] fLocations;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The standard constructor
     *
     * @param locations the locations
     */
    public TmfLocationArray(ITmfLocation[] locations) {
        fLocations = locations;
    }

    /**
     * The "update" constructor. Copies the array of locations and updates
     * a single entry.
     *
     * @param locations the locations
     * @param index the entry to modify
     * @param location the new entry
     */
    public TmfLocationArray(TmfLocationArray locations, int index, ITmfLocation location) {
        assert(locations != null && index >= 0 && index < locations.fLocations.length);
        fLocations = Arrays.copyOf(locations.fLocations, locations.fLocations.length);
        fLocations[index] = location;
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Get a specific location
     *
     * @param index the location element
     *
     * @return the specific location (possibly null)
     */
    public ITmfLocation getLocation(int index) {
        if (fLocations != null && index >= 0 && index < fLocations.length) {
            return fLocations[index];
        }
        return null;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public int compareTo(TmfLocationArray o) {
        for (int i = 0; i < fLocations.length; i++) {
            Comparable l1 = fLocations[i].getLocationInfo();
            Comparable l2 = o.fLocations[i].getLocationInfo();
            int result = l1.compareTo(l2);
            if (result != 0) {
                return result;
            }
        }
        return 0;
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
        result = prime * result + Arrays.hashCode(fLocations);
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
        TmfLocationArray other = (TmfLocationArray) obj;
        if (!Arrays.equals(fLocations, other.fLocations)) {
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
        return "TmfLocationArray [locations=" + Arrays.toString(fLocations) + "]";
    }

}
