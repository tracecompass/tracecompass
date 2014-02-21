/*******************************************************************************
 * Copyright (c) 2011, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Patrick Tasse - Initial API and implementation
 * Francois Chouinard - Put in shape for 1.0
 * Patrick Tasse - Updated for ranks in experiment location
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.trace;

import java.util.Arrays;

import org.eclipse.linuxtools.tmf.core.trace.location.ITmfLocation;


/**
 * A convenience class to store trace location arrays. The main purpose is to
 * provide an immutable and Comparable implementation for TmfExperimentLocation.
 *
 * @version 1.0
 * @author Patrick Tasse
 */
public final class TmfLocationArray implements Comparable<TmfLocationArray> {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ITmfLocation[] fLocations;
    private final long [] fRanks;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The standard constructor.
     *
     * @param locations the locations
     * @param ranks the ranks
     */
    public TmfLocationArray(ITmfLocation[] locations, long[] ranks) {
        fLocations = Arrays.copyOf(locations, locations.length);
        fRanks = Arrays.copyOf(ranks, ranks.length);
    }

    /**
     * The update constructor. Copies the arrays and updates a single entry.
     *
     * @param locationArray the location array
     * @param index the updated index
     * @param location the updated location
     * @param rank the updated rank
     */
    public TmfLocationArray(TmfLocationArray locationArray, int index, ITmfLocation location, long rank) {
        fLocations = Arrays.copyOf(locationArray.fLocations, locationArray.fLocations.length);
        fLocations[index] = location;
        fRanks = Arrays.copyOf(locationArray.fRanks, locationArray.fRanks.length);
        fRanks[index] = rank;
    }

    /**
     * The empty constructor.
     *
     * @param size the number of elements in the array
     */
    public TmfLocationArray(int size) {
        fLocations = new ITmfLocation[size];
        fRanks = new long[size];
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Returns the number of elements in this array.
     *
     * @return the number of elements in this array
     */
    public int size() {
        return fLocations.length;
    }

    /**
     * Get the locations inside this array.
     *
     * @return a copy of the locations array
     */
    public ITmfLocation[] getLocations() {
        return Arrays.copyOf(fLocations, fLocations.length);
    }

    /**
     * Get a specific location
     *
     * @param index the location element
     *
     * @return the specific location (possibly null)
     */
    public ITmfLocation getLocation(int index) {
        if (index >= 0 && index < fLocations.length) {
            return fLocations[index];
        }
        return null;
    }

    /**
     * Get the ranks inside this array.
     *
     * @return a copy of the ranks array
     */
    public long[] getRanks() {
        return Arrays.copyOf(fRanks, fRanks.length);
    }

    /**
     * Get a specific rank
     *
     * @param index the rank element
     *
     * @return the specific rank
     */
    public long getRank(int index) {
        if (index >= 0 && index < fRanks.length) {
            return fRanks[index];
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
    public int compareTo(TmfLocationArray o) {
        for (int i = 0; i < fRanks.length; i++) {
            long rank1 = fRanks[i];
            long rank2 = o.fRanks[i];
            if (rank1 < rank2) {
                return -1;
            } else if (rank1 > rank2) {
                return 1;
            }
        }
        return 0;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(fLocations);
        result = prime * result + Arrays.hashCode(fRanks);
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
        if (getClass() != obj.getClass()) {
            return false;
        }
        TmfLocationArray other = (TmfLocationArray) obj;
        if (!Arrays.equals(fLocations, other.fLocations)) {
            return false;
        }
        if (!Arrays.equals(fRanks, other.fRanks)) {
            return false;
        }
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName() + " [");
        for (int i = 0; i < fLocations.length; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append("[location=" + fLocations[i] + ",rank=" + fRanks[i] + "]");
        }
        sb.append("]");
        return sb.toString();
    }

}
