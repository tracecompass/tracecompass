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
 * <b><u>TmfEventType</u></b>
 * <p>
 * The event type.
 */
public class TmfEventType {

    // ========================================================================
    // Attributes
    // ========================================================================

	private final String fTypeId;
    private final TmfEventFormat fFormat;

    // ========================================================================
    // Constructors
    // ========================================================================

	/**
	 * @param type
	 * @param format
	 */
	public TmfEventType(String typeID, TmfEventFormat format) {
		fTypeId = typeID;
		fFormat = format;
	}

    // ========================================================================
    // Accessors
    // ========================================================================

	/**
	 * @return
	 */
	public String getTypeId() {
		return fTypeId;
	}

    /**
     * @return
     */
    public TmfEventFormat getFormat() {
        return fFormat;
    }

    // ========================================================================
    // Operators
    // ========================================================================

    @Override
    public String toString() {
        return fTypeId.toString();
    }

}
