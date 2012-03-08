/**********************************************************************
 * Copyright (c) 2012 Ericsson
 * 
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: 
 *   Bernd Hufmann - Initial API and implementation
 *********************************************************************/
package org.eclipse.linuxtools.lttng.ui.views.control.model;

/**
 * <b><u>TraceLogLevels</u></b>
 * <p>
 * Log Level enumeration.
 * </p>
 */
@SuppressWarnings("nls")
public enum TraceLogLevel {
    
    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    TRACE_EMERG("TRACE_EMERG"), // 0
    TRACE_ALERT("TRACE_ALERT"), // 1
    TRACE_CRIT("TRACE_CRIT"), // 2
    TRACE_ERR("TRACE_ERR"), // 3
    TRACE_WARNING("TRACE_WARNING"), // 4
    TRACE_NOTICE("TRACE_NOTICE"), // 5
    TRACE_INFO("TRACE_INFO"), // 6
    TRACE_DEBUG_SYSTEM("TRACE_DEBUG_SYSTEM"), // 7
    TRACE_DEBUG_PROGRAM("TRACE_DEBUG_PROGRAM"), // 8
    TRACE_DEBUG_PROCESS("TRACE_DEBUG_PROCESS"), // 9
    TRACE_DEBUG_MODULE("TRACE_DEBUG_MODULE"), // 10
    TRACE_DEBUG_UNIT("TRACE_DEBUG_UNIT"), // 11
    TRACE_DEBUG_FUNCTION("TRACE_DEBUG_FUNCTION"), //12
    TRACE_DEBUG_LINE("TRACE_DEBUG_LINE"), //13
    TRACE_DEBUG("TRACE_DEBUG"), // 14
    LEVEL_UNKNOWN("LEVEL_UNKNOWN"); // 15

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
     * @param name the name of state
     */
    private TraceLogLevel(String name) {
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
};


