/**********************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/

package org.eclipse.tracecompass.internal.lttng2.control.core.model;

/**
 * Trace event type enumeration.
 *
 * @author Bernd Hufmann
 */
public enum TraceEventType {
    /** Event type: tracepoint */
    TRACEPOINT("tracepoint"), //$NON-NLS-1$
    /** Event type: syscall */
    SYSCALL("syscall"), //$NON-NLS-1$
    /** Event type: probe */
    PROBE("probe"), //$NON-NLS-1$
    /** Event type: function */
    FUNCTION("function"), //$NON-NLS-1$
    /** Event type unknown */
    UNKNOWN("unknown"); //$NON-NLS-1$

    private final String fInName;

    private TraceEventType(String name) {
        fInName = name;
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
     * Return the corresponding {@link TraceEventType} of string miName
     *
     * @param name
     *            name of the {@link TraceEventType} to look for
     * @return the corresponding {@link TraceEventType}
     */
    public static TraceEventType valueOfString(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        for (TraceEventType teType : TraceEventType.values()) {
            if (teType.getInName().equalsIgnoreCase(name)) {
                return teType;
            }
        }
        return UNKNOWN;
    }
}
