/**********************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Bernd Hufmann - Initial API and implementation
 *   Jonathan Rajotte - Machine interface support and new information
 **********************************************************************/

package org.eclipse.linuxtools.internal.lttng2.control.ui.views.service;

import org.osgi.framework.Version;

/**
 * A version implementation with a special compareTo implementation to bypass
 * problems of older implementation of org.osgi.framework.Version.
 *
 * @author Bernd Hufmann
 */
public class LttngVersion extends Version {

    private final String fLicense;
    private final String fCommit;
    private final String fName;
    private final String fDescription;
    private final String fUrl;
    private final String fFullVersion;

    /**
     * Constructor
     *
     * @param version
     *            The version string
     */
    public LttngVersion(String version) {
        super(version);
        fLicense = ""; //$NON-NLS-1$
        fCommit = ""; //$NON-NLS-1$
        fName = ""; //$NON-NLS-1$
        fDescription = ""; //$NON-NLS-1$
        fUrl = ""; //$NON-NLS-1$
        fFullVersion = ""; //$NON-NLS-1$
    }

    /**
     * @param major
     *            major version number
     * @param minor
     *            minor version number
     * @param micro
     *            micro version number
     * @param license
     *            licence text of LTTng
     * @param commit
     *            current git commit information about LTTng
     * @param name
     *            name of the version
     * @param description
     *            description of the version
     * @param url
     *            url to website
     * @param fullVersion
     *            complete string representation of the version
     */
    public LttngVersion(int major, int minor, int micro, String license, String commit, String name, String description, String url, String fullVersion) {
        super(major, minor, micro);
        fLicense = license;
        fCommit = commit;
        fName = name;
        fDescription = description;
        fUrl = url;
        fFullVersion = fullVersion;
    }

    /**
     * Special compareTo method to fix problem of older implementations of
     * org.osgi.framework.Version where {@code Version.compareTo} takes an
     * {@code Object} instead a {@code Version} as argument.
     *
     * @param other
     *            - Other version to compare
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

    /**
     * @return String representing the lttng license
     */
    public String getLicense() {
        return fLicense;
    }

    /**
     * @return commit id of lttng
     */
    public String getCommit() {
        return fCommit;
    }

    /**
     * @return name of lttng version
     */
    public String getName() {
        return fName;
    }

    /**
     * @return full description of lttng
     */
    public String getDescription() {
        return fDescription;
    }

    /**
     * @return url of lttng
     */
    public String getUrl() {
        return fUrl;
    }

    /**
     * @return the full_version
     */
    public String getFullVersion() {
        return fFullVersion;
    }
}
