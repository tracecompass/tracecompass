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
 * <b><u>TmfEventSource</u></b>
 * <p>
 * The event source.
 */
public class TmfEventSource implements Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	protected Object fSourceId;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

    /**
     * The default constructor
     */
    public TmfEventSource() {
    	fSourceId = null;
    }

	/**
	 * @param sourceId
	 */
	public TmfEventSource(Object sourceId) {
		fSourceId = sourceId;
	}

	/**
	 * Copy constructor
	 * @param other
	 */
	public TmfEventSource(TmfEventSource other) {
    	if (other == null)
    		throw new IllegalArgumentException();
    	TmfEventSource o = (TmfEventSource) other;
    	fSourceId = o.fSourceId;
	}

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

	/**
	 * @return
	 */
	public Object getSourceId() {
		return fSourceId;
	}

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

	@Override
    public int hashCode() {
        return (fSourceId != null) ? fSourceId.hashCode() : 0;
    }

	@Override
    public boolean equals(Object other) {
		if (!(other instanceof TmfEventSource))
			return false;
		TmfEventSource o = (TmfEventSource) other;
        return fSourceId.equals(o.fSourceId);
    }

	@Override
    public String toString() {
        return "[TmfEventSource(" + ((fSourceId != null) ? fSourceId.toString() : "null") + ")]";
    }

	@Override
	public TmfEventSource clone() {
		TmfEventSource clone = null;
		try {
			clone = (TmfEventSource) super.clone();
			clone.fSourceId = null;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}
}
