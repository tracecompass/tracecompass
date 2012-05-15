/*******************************************************************************
 * Copyright (c) 2009, 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

/**
 * A utility class to define and manage time ranges.
 * 
 * @version 1.0
 * @author Francois Chouinard
 * 
 * @see ITmfTimestamp
 */
public final class TmfTimeRange implements Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The full possible time range
     */
    public static final TmfTimeRange ETERNITY =
            new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);

    /**
     * The null time range
     */
    public static final TmfTimeRange NULL_RANGE =
            new TmfTimeRange(TmfTimestamp.BIG_CRUNCH, TmfTimestamp.BIG_BANG);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private ITmfTimestamp fStartTime;
    private ITmfTimestamp fEndTime;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    @SuppressWarnings("unused")
    private TmfTimeRange() {
    }

    /**
     * Full constructor
     * 
     * @param startTime start of the time range
     * @param endTime end of the time range
     */
    public TmfTimeRange(final ITmfTimestamp startTime, final ITmfTimestamp endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException();
        }
        fStartTime = startTime;
        fEndTime = endTime;
    }

    /**
     * Copy constructor
     * 
     * @param range the other time range
     */
    public TmfTimeRange(final TmfTimeRange range) {
        if (range == null) {
            throw new IllegalArgumentException();
        }
        fStartTime = range.getStartTime();
        fEndTime = range.getEndTime();
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * @return the time range start time
     */
    public ITmfTimestamp getStartTime() {
        return fStartTime;
    }

    /**
     * @return the time range end time
     */
    public ITmfTimestamp getEndTime() {
        return fEndTime;
    }

    // ------------------------------------------------------------------------
    // Predicates
    // ------------------------------------------------------------------------

    /**
     * Check if the timestamp is within the time range
     * 
     * @param ts the timestamp to check
     * @return true if [startTime] <= [ts] <= [endTime]
     */
    public boolean contains(final ITmfTimestamp ts) {
        // Zero acts as a "universal donor" timestamp
        if (ts.equals(TmfTimestamp.ZERO)) {
            return true;
        }
        return (fStartTime.compareTo(ts, true) <= 0) && (fEndTime.compareTo(ts, true) >= 0);
    }

    /**
     * Check if the time range is within the time range
     * 
     * @param range the other time range
     * @return true if [range] is fully contained
     */
    public boolean contains(final TmfTimeRange range) {
        final ITmfTimestamp startTime = range.getStartTime();
        final ITmfTimestamp endTime = range.getEndTime();
        return (fStartTime.compareTo(startTime, true) <= 0) && (fEndTime.compareTo(endTime, true) >= 0);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Get intersection of two time ranges
     * 
     * @param range the other time range
     * @return the intersection time range, or null if no intersection exists
     */
    public TmfTimeRange getIntersection(final TmfTimeRange range) {
        if (fStartTime.compareTo(range.fEndTime, true) > 0 || fEndTime.compareTo(range.fStartTime, true) < 0) {
            return null; // no intersection
        }

        return new TmfTimeRange(fStartTime.compareTo(range.fStartTime, true) < 0 
                ? range.fStartTime 
                : fStartTime, fEndTime.compareTo(range.fEndTime, true) > 0 
                        ? range.fEndTime 
                        : fEndTime);
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public TmfTimeRange clone() throws CloneNotSupportedException {
        TmfTimeRange clone = null;
        try {
            clone = (TmfTimeRange) super.clone();
            clone.fStartTime = fStartTime.clone();
            clone.fEndTime = fEndTime.clone();
        }
        catch (final CloneNotSupportedException e) {
        }
        return clone;
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
        result = prime * result + fEndTime.hashCode();
        result = prime * result + fStartTime.hashCode();
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
        if (!(obj instanceof TmfTimeRange)) {
            return false;
        }
        final TmfTimeRange other = (TmfTimeRange) obj;
        if (!fEndTime.equals(other.fEndTime)) {
            return false;
        }
        if (!fStartTime.equals(other.fStartTime)) {
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
        return "TmfTimeRange [" + fStartTime + ", " + fEndTime + "]";
    }

}
