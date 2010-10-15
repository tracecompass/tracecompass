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
 * <b><u>TmfEventReference</u></b>
 * <p>
 * An application-defined event reference.
 */
public class TmfEventReference implements Cloneable {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

	protected Object fReference;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------

	/**
	 * The default constructor
	 */
	public TmfEventReference() {
		fReference = null;
	}

	/**
	 * @param reference the event reference
	 */
	public TmfEventReference(Object reference) {
		fReference = reference;
	}

	/**
	 * Copy constructor
	 * @param other the original event reference
	 */
	public TmfEventReference(TmfEventReference other) {
    	if (other == null)
    		throw new IllegalArgumentException();
    	TmfEventReference o = (TmfEventReference) other;
    	fReference = o.fReference;
	}

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

	/**
	 * @return the event reference
	 */
	public Object getReference() {
		return fReference;
	}

    // ------------------------------------------------------------------------
    // Object
    // ------------------------------------------------------------------------

	@Override
    public int hashCode() {
        return (fReference != null) ? fReference.hashCode() : 0;
    }

	@Override
    public String toString() {
        return "[TmfEventReference(" + ((fReference != null) ? fReference.toString() : "null") + ")]";
    }

	@Override
    public boolean equals(Object other) {
		if (!(other instanceof TmfEventReference))
			return false;
		TmfEventReference o = (TmfEventReference) other;
        return fReference.equals(o.fReference);
    }

	@Override
	public TmfEventReference clone() {
		TmfEventReference clone = null;
		try {
			clone = (TmfEventReference) super.clone();
			clone.fReference = null;
		}
		catch (CloneNotSupportedException e) {
			e.printStackTrace();
		}
		return clone;
	}
}
