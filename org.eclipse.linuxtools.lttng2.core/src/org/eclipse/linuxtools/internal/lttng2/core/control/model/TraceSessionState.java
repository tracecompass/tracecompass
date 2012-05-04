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
package org.eclipse.linuxtools.internal.lttng2.core.control.model;

/**
 * <b><u>TraceSessionState</u></b>
 * <p>
 * Session state enumeration.
 * </p>
 */
public enum TraceSessionState {
    
    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    INACTIVE("inactive"), //$NON-NLS-1$
    ACTIVE("active"); //$NON-NLS-1$

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
    private TraceSessionState(String name) {
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

