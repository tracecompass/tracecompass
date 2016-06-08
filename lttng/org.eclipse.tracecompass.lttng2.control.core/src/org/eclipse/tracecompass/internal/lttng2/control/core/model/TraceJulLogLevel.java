/**********************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bruno Roy - Initial API and implementation
 *********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

/**
 * Log Level for JUL enumeration.
 *
 * @author Bruno Roy
 */
@SuppressWarnings("nls")
public enum TraceJulLogLevel implements ITraceLogLevel{

    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    /** Log level off */
    JUL_OFF("Off"),
    /** Log level severe */
    JUL_SEVERE("Severe"),
    /** Log level warning */
    JUL_WARNING("Warning"),
    /** Log level info */
    JUL_INFO("Info"),
    /** Log level config */
    JUL_CONFIG("Config"),
    /** Log level fine */
    JUL_FINE("Fine"),
    /** Log level finer */
    JUL_FINER("Finer"),
    /** Log level finest */
    JUL_FINEST("Finest"),
    /** Log level all */
    JUL_ALL("All"),
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
    private TraceJulLogLevel(String name) {
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
     * Return the corresponding {@link TraceJulLogLevel} to String "name"
     *
     * @param name
     *            String to compare to retrieve the good {@link TraceJulLogLevel}
     * @return the corresponding {@link TraceJulLogLevel}
     */
    public static TraceJulLogLevel valueOfString(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        for (TraceJulLogLevel tllevel : TraceJulLogLevel.values()) {
            if (tllevel.name().equals(name)) {
                return tllevel;
            }
        }
        // No match
        return LEVEL_UNKNOWN;
    }
}
