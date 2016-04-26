/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.analysis.lami.core.types;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Lami 'version' data type
 *
 * @author Alexandre Montplaisir
 */
public class LamiVersion {

    private final int fMajor;
    private final int fMinor;
    private final int fPatchLevel;
    private final @Nullable String fExtra;

    /**
     * Construct a new version number. Normally to be show as:
     *
     * major.minor.patchlevel.extra
     *
     * @param major
     *            Major version number
     * @param minor
     *            Minor version number
     * @param patchLevel
     *            Patch version number
     * @param extra
     *            Extra version number
     */
    public LamiVersion(int major, int minor, int patchLevel, @Nullable String extra) {
        fMajor = major;
        fMinor = minor;
        fPatchLevel = patchLevel;
        fExtra = extra;
    }

    /**
     * @return The major version number
     */
    public int getMajor() {
        return fMajor;
    }

    /**
     * @return The minor version number
     */
    public int getMinor() {
        return fMinor;
    }

    /**
     * @return The patchlevel version number
     */
    public int getPatchLevel() {
        return fPatchLevel;
    }

    /**
     * @return The extra version number. May be a string, and may be null.
     */
    public @Nullable String getExtra() {
        return fExtra;
    }
}
