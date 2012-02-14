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
 * <b><u>TmfTimeRange</u></b>
 * <p>
 * A utility class to define and manage time ranges.
 */
public class TmfTimeRange implements Cloneable {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    public static final TmfTimeRange Eternity =
        new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);

    public static final TmfTimeRange Null =
        new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigBang);

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
        throw new AssertionError();
    }

    /**
     * Full constructor
     * 
     * @param startTime start of the time range
     * @param endTime end of the time range
     */
    public TmfTimeRange(ITmfTimestamp startTime, ITmfTimestamp endTime) {
        if (startTime == null || endTime == null) {
            throw new IllegalArgumentException();
        }
        fStartTime = startTime.clone();
        fEndTime = endTime.clone();
    }

    /**
     * Copy constructor
     * 
     * @param range the other time range
     */
    public TmfTimeRange(TmfTimeRange range) {
        if (range == null) {
            throw new IllegalArgumentException();
        }
        fStartTime = range.getStartTime().clone();
        fEndTime = range.getEndTime().clone();
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
    public boolean contains(ITmfTimestamp ts) {
        // Zero acts as a "universal donor" timestamp
        if (ts.equals(TmfTimestamp.Zero))
            return true;
        return (fStartTime.compareTo(ts, true) <= 0) && (fEndTime.compareTo(ts, true) >= 0);
    }

    /**
     * Check if the time range is within the time range
     * 
     * @param range the other time range
     * @return true if [range] is fully contained
     */
    public boolean contains(TmfTimeRange range) {
        ITmfTimestamp startTime = range.getStartTime();
        ITmfTimestamp endTime = range.getEndTime();
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
    public TmfTimeRange getIntersection(TmfTimeRange range) {
        if (fStartTime.compareTo(range.fEndTime, true) > 0 || fEndTime.compareTo(range.fStartTime, true) < 0)
            return null; // no intersection

        return new TmfTimeRange(fStartTime.compareTo(range.fStartTime, true) < 0 ? range.fStartTime
                        : fStartTime, fEndTime.compareTo(range.fEndTime, true) > 0 ? range.fEndTime
                        : fEndTime);
    }

    // ------------------------------------------------------------------------
    // Cloneable
    // ------------------------------------------------------------------------

    public TmfTimeRange clone() {
        TmfTimeRange clone = null;
        try {
            clone = (TmfTimeRange) super.clone();
            clone.fStartTime = fStartTime.clone();
            clone.fEndTime = fEndTime.clone();
        }
        catch (CloneNotSupportedException e) {
        }
        return clone;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fEndTime == null) ? 0 : fEndTime.hashCode());
        result = prime * result + ((fStartTime == null) ? 0 : fStartTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        TmfTimeRange other = (TmfTimeRange) obj;
        if (fEndTime == null) {
            if (other.fEndTime != null)
                return false;
        } else if (!fEndTime.equals(other.fEndTime))
            return false;
        if (fStartTime == null) {
            if (other.fStartTime != null)
                return false;
        } else if (!fStartTime.equals(other.fStartTime))
            return false;
        return true;
    }

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfTimeRange [fStartTime=" + fStartTime + ", fEndTime=" + fEndTime + "]";
    }

}
