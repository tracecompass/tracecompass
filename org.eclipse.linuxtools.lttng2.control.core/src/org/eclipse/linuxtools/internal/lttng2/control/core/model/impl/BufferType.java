/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Simon Delisle - Initial API and implementation
 *   Bernd Hufmann - Updated to enum definition
 *   Jonathan Rajotte - Updated enum definition for lttng machine interface
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.core.model.impl;

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
    BUFFER_PER_UID("per UID", "PER_UID"), //$NON-NLS-1$ //$NON-NLS-2$
    /**
     * Buffer type : per PID
     */
    BUFFER_PER_PID("per PID", "PER_PID"), //$NON-NLS-1$ //$NON-NLS-2$
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

    /**
     * Name of the machine interface enum
     */
    private final String fInMiName;

    // ------------------------------------------------------------------------
    // Constuctors
    // ------------------------------------------------------------------------

    /**
     * Private constructor
     *
     * @param name
     *            the name of state
     */
    private BufferType(String name, String miName) {
        fInName = name;
        fInMiName = miName;
    }

    private BufferType(String name) {
        fInName = name;
        fInMiName = ""; //$NON-NLS-1$
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

    /**
     * @return machine interface buffer name
     */
    public String getInMiName() {
        return fInMiName;
    }

    // /
    // ------------------------------------------------------------------------
    // Utility function
    // -------------------------------------------------------------------------
    /**
     * @param name
     *            the string representation of the type
     * @return enum BufferType of the corresponding type
     */
    public static BufferType valueOfString(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        for (BufferType bufferType : BufferType.values()) {
            boolean isEqual = bufferType.getInName().equalsIgnoreCase(name) || bufferType.getInMiName().equalsIgnoreCase(name);
            if (isEqual) {
                return bufferType;
            }
        }
        return BUFFER_TYPE_UNKNOWN;
    }
}
