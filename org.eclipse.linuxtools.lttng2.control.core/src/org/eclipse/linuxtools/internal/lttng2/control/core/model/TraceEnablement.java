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
 *   Jonathan Rajotte - Machine interface support and utility function
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.core.model;

import java.security.InvalidParameterException;

/**
 * Enumeration for enabled/disabled states.
 *
 * @author Bernd Hufmann
 */
public enum TraceEnablement {

    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    /** Tracing is disabled */
    DISABLED("disabled", "false"), //$NON-NLS-1$ //$NON-NLS-2$
    /** Tracing is enabled */
    ENABLED("enabled", "true"); //$NON-NLS-1$ //$NON-NLS-2$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Name of enum
     */
    private final String fInName;
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
    private TraceEnablement(String name, String miName) {
        fInName = name;
        fInMiName = miName;
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
     * @return state name
     */
    public String getInMiName() {
        return fInMiName;
    }

    /**
     * @param name
     *            name of the desired enum
     * @return the corresponding {@link TraceEnablement} matching name
     */
    public static TraceEnablement valueOfString(String name) {
        if (name == null) {
            throw new InvalidParameterException();
        }
        for (TraceEnablement enablementType : TraceEnablement.values()) {
            boolean exist = enablementType.fInName.equalsIgnoreCase(name) || enablementType.fInMiName.equalsIgnoreCase(name);
            if (exist) {
                return enablementType;
            }
        }
        return DISABLED;
    }

}
