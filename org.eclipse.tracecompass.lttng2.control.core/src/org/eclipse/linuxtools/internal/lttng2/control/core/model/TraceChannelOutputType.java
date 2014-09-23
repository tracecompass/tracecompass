/**********************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Jonathan Rajotte - Initial API and implementation
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.core.model;

/**
 * Trace domain type enumeration.
 *
 * @author Jonathan Rajotte
 */
public enum TraceChannelOutputType {
    /** Channel output type : splice */
    SPLICE("splice()", "SPLICE" ), //$NON-NLS-1$ //$NON-NLS-2$
    /** Channel output type : mmap */
    MMAP("mmap()", "MMAP"), //$NON-NLS-1$ //$NON-NLS-2$
    /** Channel output type : unknown */
    UNKNOWN("unknown", "unknown"); //$NON-NLS-1$ //$NON-NLS-2$

    private final String fInName;
    private final String fInMiName;

    private TraceChannelOutputType(String name, String miName) {
        fInName = name;
        fInMiName = miName;
    }

    /**
     * Get the type's name
     *
     * @return The type's name
     */
    public String getInName() {
        return fInName;
    }

    /**
     * Get the type's name
     *
     * @return The type's name
     */
    public String getInMiName() {
        return fInMiName;
    }

    /**
     * Return the corresponding {@link TraceChannelOutputType} of string miName
     *
     * @param name
     *            name of the Trace domain type to look for
     * @return the corresponding {@link TraceChannelOutputType}
     */
    public static TraceChannelOutputType valueOfString(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        for (TraceChannelOutputType tdType : TraceChannelOutputType.values()) {
            boolean isEqual = tdType.getInName().equalsIgnoreCase(name) || tdType.getInMiName().equalsIgnoreCase(name);
            if (isEqual) {
                return tdType;
            }
        }
        // Unknown domain
        return UNKNOWN;
    }
}