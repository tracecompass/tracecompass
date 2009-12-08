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

    // ========================================================================
    // Attributes
    // ========================================================================

	protected Object fReference;

    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * 
	 */
	public TmfEventReference() {
		this(null);
	}

	/**
	 * @param reference
	 */
	public TmfEventReference(Object reference) {
		fReference = reference;
	}

	/**
	 * @param other
	 */
	public TmfEventReference(TmfEventReference other) {
		this((other != null) ? other.fReference : null);
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return
	 */
	public Object getReference() {
		return fReference;
	}

    // ========================================================================
    // Operators
    // ========================================================================

    @Override
    public TmfEventReference clone() {
    	return new TmfEventReference(this);
    }

    @Override
    public String toString() {
        return "[TmfEventReference(" + ((fReference != null) ? fReference.toString() : "null") + ")]";
    }

}
