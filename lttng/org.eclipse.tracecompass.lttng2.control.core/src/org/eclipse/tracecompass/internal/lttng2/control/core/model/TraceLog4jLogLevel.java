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
 * Log Level for Log4j enumeration.
 *
 * @author Bruno Roy
 */
@SuppressWarnings("nls")
public enum TraceLog4jLogLevel implements ITraceLogLevel{

    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    /** Log level off */
    LOG4J_OFF("Off"),
    /** Log level fatal */
    LOG4J_FATAL("Fatal"),
    /** Log level error */
    LOG4J_ERROR("Error"),
    /** Log level warn */
    LOG4J_WARN("Warn"),
    /** Log level info */
    LOG4J_INFO("Info"),
    /** Log level debug */
    LOG4J_DEBUG("Debug"),
    /** Log level trace */
    LOG4J_TRACE("Trace"),
    /** Log level all */
    LOG4J_ALL("All"),
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
    private TraceLog4jLogLevel(String name) {
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
     * Return the corresponding {@link TraceLog4jLogLevel} to String "name"
     *
     * @param name
     *            String to compare to retrieve the good {@link TraceLog4jLogLevel}
     * @return the corresponding {@link TraceLog4jLogLevel}
     */
    public static TraceLog4jLogLevel valueOfString(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        for (TraceLog4jLogLevel tllevel : TraceLog4jLogLevel.values()) {
            if (tllevel.name().equals(name) || tllevel.getInName().equals(name)) {
                return tllevel;
            }
        }
        // No match
        return LEVEL_UNKNOWN;
    }
}
