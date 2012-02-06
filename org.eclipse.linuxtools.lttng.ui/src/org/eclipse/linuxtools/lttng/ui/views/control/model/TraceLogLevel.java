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
 * Log Level state enumeration.
 * </p>
 */
@SuppressWarnings("nls")
public enum TraceLogLevel {
    
    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    TRACE_EMERG("TRACE_EMERG0"),
    TRACE_ALERT("TRACE_ALERT1"),
    TRACE_CRIT("TRACE_CRIT2"),
    TRACE_ERR("TRACE_ERR3"),
    TRACE_WARNING("TRACE_WARNING4"),
    TRACE_NOTICE("TRACE_NOTICE5"),
    TRACE_INFO("TRACE_INFO6"),
    TRACE_SYSTEM("TRACE_SYSTEM7"),
    TRACE_PROGRAM("TRACE_PROGRAM8"),
    TRACE_PROCESS("TRACE_PROCESS9"),
    TRACE_MODULE("TRACE_MODULE10"),
    TRACE_UNIT("TRACE_UNIT11"),
    TRACE_FUNCTION("TRACE_FUNCTION12"),
    TRACE_DEFAULT("TRACE_DEFAULT13"),
    TRACE_VERBOSE("TRACE_VERBOSE14"),
    TRACE_DEBUG("TRACE_DEBUG15");
    

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


