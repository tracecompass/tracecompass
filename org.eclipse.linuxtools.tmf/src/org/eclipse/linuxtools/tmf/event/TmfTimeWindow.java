/*******************************************************************************
 * Copyright (c) 2009 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard (fchouinard@gmail.com) - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.event;

/**
 * <b><u>TmfTimeWindow</u></b>
 * <p>
 * A utility class to define time ranges.
 */
public class TmfTimeWindow {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final TmfTimestamp fStartTime;
	private final TmfTimestamp fEndTime;

    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * @param startTime
	 * @param endTime
	 */
	public TmfTimeWindow(TmfTimestamp startTime, TmfTimestamp endTime) {
		fStartTime = startTime;
		fEndTime   = endTime;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return The time range start time
	 */
	public TmfTimestamp getStartTime() {
		return fStartTime;
	}

	/**
	 * @return The time range end time
	 */
	public TmfTimestamp getEndTime() {
		return fEndTime;
	}
}
