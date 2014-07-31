/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Jonathan Rajotte - machine interface support and utility function
 *********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.core.model;

/**
 * Session state enumeration.
 *
 * @author Bernd Hufmann
 */
public enum TraceSessionState {

    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    /** Trace session inactive */
    INACTIVE("inactive", "false"), //$NON-NLS-1$ //$NON-NLS-2$
    /** Trace session active */
    ACTIVE("active", "true"); //$NON-NLS-1$ //$NON-NLS-2$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Name of enum.
     */
    private final String fInName;
    private final String fMiName;

    // ------------------------------------------------------------------------
    // Constuctors
    // ------------------------------------------------------------------------

    /**
     * Private constructor
     *
     * @param name
     *            the name of state
     */
    private TraceSessionState(String name, String miName) {
        fInName = name;
        fMiName = miName;
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
     * @return the machine interface name
     */
    public String getfMiName() {
        return fMiName;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * Return the corresponding {@link TraceSessionState} to String "name"
     *
     * @param name
     *            String to compare to retrieve the good
     *            {@link TraceSessionState}
     * @return the corresponding {@link TraceSessionState}
     */
    public static TraceSessionState valueOfString(String name) {
        if (name == null) {
            return INACTIVE;
        }
        for (TraceSessionState tst : TraceSessionState.values()) {
            boolean isEqual = tst.fInName.equalsIgnoreCase(name) || tst.fMiName.equalsIgnoreCase(name);
            if (isEqual) {
                return tst;
            }
        }
        // No match
        return INACTIVE;
    }
}
