/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *   Bernd Hufmann - Updated to enum definition
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.core.control.model.impl;

/**
 * Constants for buffer type.
 *
 * @author Simon Delisle
 * @author Bernd Hufmann
 */

public enum BufferType {
    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    /**
     * Buffer type : per UID
     */
    BUFFER_PER_UID("per UID"), //$NON-NLS-1$
    /**
     * Buffer type : per PID
     */
    BUFFER_PER_PID("per PID"), //$NON-NLS-1$
    /**
     * Buffer type : shared
     */
    BUFFER_SHARED("shared"), //$NON-NLS-1$
    /**
     * If the LTTng version doesn't show the buffer type
     */
    BUFFER_TYPE_UNKNOWN("information not unavailable"); //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Name of enum
     */
    private final String fInName;

    // ------------------------------------------------------------------------
    // Constuctors
    // ------------------------------------------------------------------------

    /**
     * Private constructor
     * @param name the name of state
     */
    private BufferType(String name) {
        fInName = name;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return state name
     */
    public String getInName() {
        return fInName;
    }
}
