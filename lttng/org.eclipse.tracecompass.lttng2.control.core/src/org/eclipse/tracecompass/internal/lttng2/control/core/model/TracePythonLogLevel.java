/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 *********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

/**
 * Log Level for Python enumeration.
 *
 * @author Bruno Roy
 */
@SuppressWarnings("nls")
public enum TracePythonLogLevel implements ITraceLogLevel{

    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    /** Log level critical */
    PYTHON_CRITICAL("Critical"),
    /** Log level error */
    PYTHON_ERROR("Error"),
    /** Log level warning */
    PYTHON_WARNING("Warning"),
    /** Log level info */
    PYTHON_INFO("Info"),
    /** Log level debug */
    PYTHON_DEBUG("Debug"),
    /** Log level not set */
    PYTHON_NOTSET("Notset"),
    /** Log level unknown */
    LEVEL_UNKNOWN("LEVEL_UNKNOWN");

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Name of enum.
     */
    private final String fInName;

    // ------------------------------------------------------------------------
    // Constuctors
    // ------------------------------------------------------------------------

    /**
     * Private constructor
     *
     * @param name
     *            the name of state
     */
    private TracePythonLogLevel(String name) {
        fInName = name;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public String getInName() {
        return fInName;
    }

    // ------------------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------------------
    /**
     * Return the corresponding {@link TracePythonLogLevel} to String "name"
     *
     * @param name
     *            String to compare to retrieve the good {@link TracePythonLogLevel}
     * @return the corresponding {@link TracePythonLogLevel}
     */
    public static TracePythonLogLevel valueOfString(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        for (TracePythonLogLevel tllevel : TracePythonLogLevel.values()) {
            if (tllevel.name().equals(name) || tllevel.getInName().equals(name)) {
                return tllevel;
            }
        }
        // No match
        return LEVEL_UNKNOWN;
    }
}
