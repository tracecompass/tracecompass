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

package org.eclipse.linuxtools.tmf.event;

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
	
	// ------------------------------------------------------------------------
    // Attributes
	// ------------------------------------------------------------------------

	private final TmfTimestamp fStartTime;
	private final TmfTimestamp fEndTime;

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
	public TmfTimeRange(TmfTimestamp startTime, TmfTimestamp endTime) {
		if (startTime == null || endTime == null) {
    		throw new IllegalArgumentException();
		}
		fStartTime =  new TmfTimestamp(startTime);
		fEndTime   =  new TmfTimestamp(endTime);
	}
	
	/**
	 * Copy constructor
	 * @param other
	 */
	public TmfTimeRange(TmfTimeRange other) {
    	if (other == null) {
    		throw new IllegalArgumentException();
    	}
		fStartTime = new TmfTimestamp(other.fStartTime);
		fEndTime   = new TmfTimestamp(other.fEndTime);
	}

	// ------------------------------------------------------------------------
    // Accessors
	// ------------------------------------------------------------------------

	/**
	 * @return The time range start time
	 */
	public TmfTimestamp getStartTime() {
		return new TmfTimestamp(fStartTime);
	}

	/**
	 * @return The time range end time
	 */
	public TmfTimestamp getEndTime() {
		return new TmfTimestamp(fEndTime);
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
	public boolean contains(TmfTimestamp ts) {
		return (fStartTime.compareTo(ts, true) <= 0) && (fEndTime.compareTo(ts, true) >= 0);
	}

	/**
	 * Check if the time range is within the time range
	 * 
	 * @param range
	 * @return
	 */
	public boolean contains(TmfTimeRange range) {
		TmfTimestamp startTime = range.getStartTime();
		TmfTimestamp endTime   = range.getEndTime();
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
	public String toString() {
		return "[TmfTimeRange(" + fStartTime + ":" + fEndTime + ")]";
	}

}
