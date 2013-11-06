/*******************************************************************************
 * Copyright (c) 2009, 2013 Ericsson
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

package org.eclipse.linuxtools.tmf.core.timestamp;

/**
 * A utility class to define and manage time ranges.
 *
 * @author Francois Chouinard
 * @version 1.0
 * @since 2.0
 *
 * @see ITmfTimestamp
 */
public class TmfTimeRange {

    // ------------------------------------------------------------------------
    // Constants
    // ------------------------------------------------------------------------

    /**
     * The full possible time range
     */
    public static final TmfTimeRange ETERNITY = new EternityTimeRange();

    /**
     * The null time range
     */
    public static final TmfTimeRange NULL_RANGE = new TmfTimeRange();

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ITmfTimestamp fStartTime;
    private final ITmfTimestamp fEndTime;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Default constructor
     */
    private TmfTimeRange() {
        fStartTime = TmfTimestamp.BIG_BANG;
        fEndTime = TmfTimestamp.BIG_BANG;
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
     * @param ts
     *            The timestamp to check
     * @return True if [startTime] <= [ts] <= [endTime]
     */
    public boolean contains(final ITmfTimestamp ts) {
        return (fStartTime.compareTo(ts, true) <= 0) && (fEndTime.compareTo(ts, true) >= 0);
    }

    /**
     * Check if the time range is within the time range
     *
     * @param range
     *            The other time range
     * @return True if [range] is fully contained
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
    // Object
    // ------------------------------------------------------------------------

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fEndTime.hashCode();
        result = prime * result + fStartTime.hashCode();
        return result;
    }

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

    @Override
    @SuppressWarnings("nls")
    public String toString() {
        return "TmfTimeRange [fStartTime=" + fStartTime + ", fEndTime=" + fEndTime + "]";
    }

    // ------------------------------------------------------------------------
    // Inner classes
    // ------------------------------------------------------------------------

    /**
     * "Eternity" time range, representing the largest time range possible,
     * which includes any other time range or timestamp.
     */
    private static final class EternityTimeRange extends TmfTimeRange {

        public EternityTimeRange() {
            super(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_CRUNCH);
        }

        @Override
        public boolean contains(ITmfTimestamp ts) {
            return true;
        }

        @Override
        public boolean contains(TmfTimeRange range) {
            return true;
        }

        @Override
        public TmfTimeRange getIntersection(TmfTimeRange range) {
            return range;
        }
    }
}
