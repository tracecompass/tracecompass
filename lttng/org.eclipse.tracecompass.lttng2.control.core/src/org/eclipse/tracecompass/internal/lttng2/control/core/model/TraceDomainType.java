/**********************************************************************
 * Copyright (c) 2014, 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Jonathan Rajotte - Initial API and implementation
 **********************************************************************/
package org.eclipse.tracecompass.internal.lttng2.control.core.model;

/**
 * Trace domain type enumeration.
 *
 * @author Jonathan Rajotte
 */
public enum TraceDomainType {
    /** Linux kernel domain */
    KERNEL("kernel"), //$NON-NLS-1$
    /** user space domain */
    UST("ust"), //$NON-NLS-1$
    /** java.util.logging (JUL) domain */
    JUL("jul"), //$NON-NLS-1$s
    /** log4j domain */
    LOG4J("log4j"), //$NON-NLS-1$
    /** python domain */
    PYTHON("python"), //$NON-NLS-1$
    /** Unknown domain */
    UNKNOWN("Unknown domain type"); //$NON-NLS-1$

    private final String fInName;

    private TraceDomainType(String name) {
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
     * Return the corresponding {@link TraceDomainType} of string miName
     *
     * @param miName
     *            name of the Trace domain type to look for
     * @return the corresponding {@link TraceDomainType}
     */
    public static TraceDomainType valueOfString(String miName) {
        if (miName == null) {
            throw new IllegalArgumentException();
        }
        for (TraceDomainType tdType : TraceDomainType.values()) {
            if (tdType.getInName().equalsIgnoreCase(miName)) {
                return tdType;
            }
        }
        // Unknown domain
        return UNKNOWN;
    }
}
