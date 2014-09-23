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
 *********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.core.model;


/**
 * Type of log Level enumeration.
 *
 * @author Bernd Hufmann
 */
public enum LogLevelType {

    // ------------------------------------------------------------------------
    // Enum definition
    // ------------------------------------------------------------------------
    /** range of log levels [0,logLevel] */
    LOGLEVEL("<=", "RANGE"), //$NON-NLS-1$ //$NON-NLS-2$

    /** all log level */
    LOGLEVEL_ALL("", "ALL"), //$NON-NLS-1$//$NON-NLS-2$

    /** single log level */
    LOGLEVEL_ONLY("==", "SINGLE"), //$NON-NLS-1$ //$NON-NLS-2$

    /** no log level */
    LOGLEVEL_NONE("", "UNKNOWN"); //$NON-NLS-1$ //$NON-NLS-2$

    // ------------------------------------------------------------------------
    // Constuctors
    // ------------------------------------------------------------------------

    /**
     * Private constructor
     *
     * @param name
     *            the name of state
     */
    private LogLevelType(String shortName, String miName) {
        fShortName = shortName;
        fMiName = miName;
    }

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------
    /**
     * Name of enum.
     */
    private final String fShortName;
    private final String fMiName;

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------
    /**
     * @return short string
     */
    public String getShortName() {
        return fShortName;
    }

    /**
     * @return machine interface name string
     */
    public String getMiName() {
        return fMiName;
    }

    // ------------------------------------------------------------------------
    // Utility
    // ------------------------------------------------------------------------
    /**
     * Return the corresponding {@link LogLevelType} to String "name"
     *
     * @param name
     *            String to compare to retrieve the good LogLevelType
     * @return the corresponding {@link LogLevelType}
     */
    public static LogLevelType valueOfString(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        for (LogLevelType lltype : LogLevelType.values()) {
            if (!lltype.equals(LOGLEVEL_NONE)) {
                boolean isEqual = lltype.fShortName.equalsIgnoreCase(name) || lltype.fMiName.equalsIgnoreCase(name);
                if (isEqual) {
                    return lltype;
                }
            }
        }

        // No match
        return LogLevelType.LOGLEVEL_NONE;
    }
}
