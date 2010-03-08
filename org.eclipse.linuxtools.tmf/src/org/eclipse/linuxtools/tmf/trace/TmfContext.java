/*******************************************************************************
 * Copyright (c) 2009, 2010 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *   Francois Chouinard - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.trace;


/**
 * <b><u>TmfContext</u></b>
 * <p>
 * Trace context structure. It ties a trace location to an event rank. The
 * context should be enough to restore the trace state so the corresponding
 * event can be read.
 * <p>
 * Used to handle conflicting, concurrent accesses to the trace. 
 */
public class TmfContext implements ITmfContext, Cloneable {

	private ITmfLocation<?> fLocation;
	private long fRank;
	
	// ------------------------------------------------------------------------
	// Constructors
	// ------------------------------------------------------------------------

	public TmfContext(ITmfLocation<?> loc, long rank) {
		fLocation = loc;
		fRank = rank;
	}

	public TmfContext(ITmfLocation<?> location) {
		this(location, 0);
	}

	public TmfContext(TmfContext other) {
		this(other.fLocation, other.fRank);
	}

	public TmfContext() {
		this(null, 0);
	}

	// ------------------------------------------------------------------------
	// Cloneable
	// ------------------------------------------------------------------------

	@Override
	public TmfContext clone() {
		try {
			return (TmfContext) super.clone();
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return null;
	}

	// ------------------------------------------------------------------------
	// ITmfContext
	// ------------------------------------------------------------------------

	public void setLocation(ITmfLocation<?> location) {
		fLocation = location;
	}

	public ITmfLocation<?> getLocation() {
		return fLocation;
	}

	public void setRank(long rank) {
		fRank = rank;
	}

	public long getRank() {
		return fRank;
	}

	public void updateRank(int delta) {
		fRank += delta;
	}

}
