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

package org.eclipse.linuxtools.tmf.trace;

import org.eclipse.linuxtools.tmf.event.TmfTimestamp;

/**
 * <b><u>TmfCheckpoint</u></b>
 * <p>
 * This class maps an event timestamp to a generic location.
 */
public class TmfCheckpoint implements Comparable<TmfCheckpoint>, Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    
    private TmfTimestamp fTimestamp;
    private ITmfLocation<?> fLocation;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    @SuppressWarnings("unused")
	private TmfCheckpoint() {
        fTimestamp = null;
        fLocation  = null;
    }

    /**
     * @param ts the checkpoint timestamp
     * @param location the corresponding trace location
     */
    public TmfCheckpoint(TmfTimestamp ts, ITmfLocation<?> location) {
        fTimestamp = ts;
        fLocation = location;
    }

    /**
     * Deep copy constructor
     * @param other the other checkpoint
     */
    public TmfCheckpoint(TmfCheckpoint other) {
    	if (other == null)
    		throw new IllegalArgumentException();
        fTimestamp = (TmfTimestamp) other.fTimestamp.clone();
        fLocation  = other.fLocation.clone();
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * @return the checkpoint timestamp
     */
    public TmfTimestamp getTimestamp() {
        return fTimestamp;
    }

    /**
     * @return the checkpoint stream location
     */
    public ITmfLocation<?> getLocation() {
        return fLocation;
    }

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

    @Override
    public TmfCheckpoint clone() {
    	TmfCheckpoint result = null;
		try {
			result = (TmfCheckpoint) super.clone();
	    	result.fTimestamp = new TmfTimestamp(fTimestamp);
	    	result.fLocation  = fLocation.clone();
	    	return result;
		} catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return result;
    }
 
    @Override
    public int hashCode() {
    	return fTimestamp.hashCode();
    }
 
    @Override
    public boolean equals(Object other) {
    	if (!(other instanceof TmfCheckpoint)) {
    		return false;
    	}
    	TmfCheckpoint o = (TmfCheckpoint) other;
    	return fTimestamp.equals(o.fTimestamp);
    }
 
    @Override
    @SuppressWarnings("nls")
    public String toString() {
    	return "[TmfCheckpoint(" + fTimestamp +  "," + fLocation + ")]";
    }
 
    // ------------------------------------------------------------------------
    // Comparable
    // ------------------------------------------------------------------------

    @Override
	public int compareTo(TmfCheckpoint other) {
        return fTimestamp.compareTo(other.fTimestamp, false);
    }

}
