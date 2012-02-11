/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.event;

/**
 * <b><u>TmfTimeRange</u></b>
 * <p>
 * A utility class to define time ranges.
 */
public class TmfTimeRange {

	// ------------------------------------------------------------------------
    // Constants
	// ------------------------------------------------------------------------

	public static final TmfTimeRange Eternity = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigCrunch);
	public static final TmfTimeRange Null = new TmfTimeRange(TmfTimestamp.BigBang, TmfTimestamp.BigBang);
	
	// ------------------------------------------------------------------------
    // Attributes
	// ------------------------------------------------------------------------

	private final ITmfTimestamp fStartTime;
	private final ITmfTimestamp fEndTime;

	// ------------------------------------------------------------------------
    // Constructors
	// ------------------------------------------------------------------------

	@SuppressWarnings("unused")
	private TmfTimeRange() {
		throw new AssertionError();
	}

	/**
	 * @param startTime
	 * @param endTime
	 */
	public TmfTimeRange(ITmfTimestamp startTime, ITmfTimestamp endTime) {
		if (startTime == null || endTime == null) {
    		throw new IllegalArgumentException();
		}
		fStartTime = startTime.clone();
		fEndTime   = endTime.clone();
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public TmfTimeRange(TmfTimeRange other) {
    	if (other == null) {
    		throw new IllegalArgumentException();
    	}
        fStartTime = other.getStartTime().clone();
        fEndTime   = other.getEndTime().clone();
	}

	// ------------------------------------------------------------------------
    // Accessors
	// ------------------------------------------------------------------------

	/**
	 * @return The time range start time
	 */
	public ITmfTimestamp getStartTime() {
		return fStartTime;
	}

	/**
	 * @return The time range end time
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
	 * @return
	 */
	public boolean contains(ITmfTimestamp ts) {
		// Zero acts as a "universal donor" timestamp
		if (ts.equals(TmfTimestamp.Zero)) return true;
		return (fStartTime.compareTo(ts, true) <= 0) && (fEndTime.compareTo(ts, true) >= 0);
	}

	/**
	 * Get intersection of two time ranges
	 * 
	 * @param other
	 *            the other time range
	 * @return the intersection time range, or null if no intersection exists
	 */
	public TmfTimeRange getIntersection(TmfTimeRange other)
	{
		if (fStartTime.compareTo(other.fEndTime, true) > 0 || fEndTime.compareTo(other.fStartTime, true) < 0)
			return null; // no intersection

		return new TmfTimeRange(
			fStartTime.compareTo(other.fStartTime, true) < 0 ? other.fStartTime : fStartTime,
			fEndTime.compareTo(other.fEndTime, true) > 0 ? other.fEndTime : fEndTime);
	}
	
	/**
	 * Check if the time range is within the time range
	 * 
	 * @param range
	 * @return
	 */
	public boolean contains(TmfTimeRange range) {
		ITmfTimestamp startTime = range.getStartTime();
		ITmfTimestamp endTime   = range.getEndTime();
		return (fStartTime.compareTo(startTime, true) <= 0) && (fEndTime.compareTo(endTime, true) >= 0);
	}

	// ------------------------------------------------------------------------
    // Object
	// ------------------------------------------------------------------------

	@Override
    public int hashCode() {
		int result = 17;
		result = 37 * result + fStartTime.hashCode();
		result = 37 * result + fEndTime.hashCode();
        return result;
    }

	@Override
    public boolean equals(Object other) {
    	if (!(other instanceof TmfTimeRange))
    		return false;
   		TmfTimeRange range = (TmfTimeRange) other;
   		return range.fStartTime.equals(fStartTime) && range.fEndTime.equals(fEndTime);
    }

	@Override
    @SuppressWarnings("nls")
	public String toString() {
		return "[TmfTimeRange(" + fStartTime + ":" + fEndTime + ")]";
	}

}
