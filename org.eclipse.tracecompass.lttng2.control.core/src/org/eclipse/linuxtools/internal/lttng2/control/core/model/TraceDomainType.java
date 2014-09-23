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
public enum TraceDomainType {
    /** Domain type : ust */
    UST("ust"), //$NON-NLS-1$
    /** Domain type : kernel */
    KERNEL("kernel"), //$NON-NLS-1$
    /** Domain type : jul */
    JUL("jul"), //$NON-NLS-1$
    /** Unknown domain type */
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