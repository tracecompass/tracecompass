/**********************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 **********************************************************************/
package org.eclipse.linuxtools.internal.lttng2.control.ui.views.service;

import org.osgi.framework.Version;

/**
 * A version implementation with a special compareTo implementation
 * to bypass problems of older implementation of org.osgi.framework.Version.
 *
 * @author Bernd Hufmann
 */
public class LttngVersion extends Version {

    /**
     * Constructor
     *
     * @param version
     *      The version string
     */
    public LttngVersion(String version) {
        super(version);
    }

    /**
     * Special compareTo method to fix problem of older implementations of org.osgi.framework.Version
     * where {@code Version.compareTo} takes an {@code Object} instead a {@code Version} as argument.
     *
     * @param other
     *      - Other version to compare
     * @return a negative integer, zero, or a positive integer if this version
     *         is less than, equal to, or greater than the specified
     *         {@code LttngVersion} object.
     */
    public int compareTo(LttngVersion other) {
        if (other == this) { // quicktest
            return 0;
        }

        int result = getMajor() - other.getMajor();
        if (result != 0) {
            return result;
        }

        result = getMinor() - other.getMinor();
        if (result != 0) {
            return result;
        }

        result = getMicro() - other.getMicro();
        if (result != 0) {
            return result;
        }
        return getQualifier().compareTo(other.getQualifier());
    }
}
