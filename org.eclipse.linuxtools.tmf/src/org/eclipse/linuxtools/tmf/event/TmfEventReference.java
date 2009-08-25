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
public class TmfEventReference {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final Object fReference;

    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * @param reference
	 */
	public TmfEventReference(Object reference) {
		fReference = reference;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return
	 */
	public Object getValue() {
		return fReference;
	}

    // ========================================================================
    // Operators
    // ========================================================================

    @Override
    public String toString() {
        return fReference.toString();
    }

}
