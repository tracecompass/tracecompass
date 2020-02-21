/*******************************************************************************
 * Copyright (c) 2009, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *   Francois Chouinard - Updated as per TMF Event Model 1.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.timestamp;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * A utility class to define and manage time ranges.
 *
 * @author Francois Chouinard
 *
 * @see ITmfTimestamp
 */
@NonNullByDefault
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
    public static final TmfTimeRange NULL_RANGE = new TmfTimeRange(TmfTimestamp.BIG_BANG, TmfTimestamp.BIG_BANG);

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    private final ITmfTimestamp fStartTime;
    private final ITmfTimestamp fEndTime;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * Full constructor
     *
     * @param startTime start of the time range
     * @param endTime end of the time range
     */
    public TmfTimeRange(final ITmfTimestamp startTime, final ITmfTimestamp endTime) {
        fStartTime = startTime;
        fEndTime = endTime;
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
        return (fStartTime.compareTo(ts) <= 0) && (fEndTime.compareTo(ts) >= 0);
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
        return (fStartTime.compareTo(startTime) <= 0) && (fEndTime.compareTo(endTime) >= 0);
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
    public @Nullable TmfTimeRange getIntersection(final TmfTimeRange range) {
        if (fStartTime.compareTo(range.fEndTime) > 0 || fEndTime.compareTo(range.fStartTime) < 0) {
            return null; // no intersection
        }

        return new TmfTimeRange(fStartTime.compareTo(range.fStartTime) < 0
                ? range.fStartTime
                : fStartTime, fEndTime.compareTo(range.fEndTime) > 0
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
    public boolean equals(final @Nullable Object obj) {
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
        return (fStartTime.equals(other.fStartTime));
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
        public @NonNull TmfTimeRange getIntersection(TmfTimeRange range) {
            return range;
        }
    }
}
