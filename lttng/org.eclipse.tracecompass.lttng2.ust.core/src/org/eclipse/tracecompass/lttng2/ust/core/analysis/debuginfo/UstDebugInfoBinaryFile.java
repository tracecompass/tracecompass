/*******************************************************************************
 * Copyright (c) 2015 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo;

import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Wrapper class to reference to a particular binary, which can be an
 * executable or library. It contains both the complete file path (at the
 * time the trace was taken) and the build ID of the binary.
 *
 * @author Alexandre Montplaisir
 * @noreference Meant to be used internally by the analysis only
 */
public class UstDebugInfoBinaryFile implements Comparable<UstDebugInfoBinaryFile> {

    private final String fFilePath;
    private final @Nullable String fBuildId;
    private final @Nullable String fDebugLink;
    private final boolean fIsPic;

    /**
     * Constructor
     *
     * @param filePath
     *            The binary's path on the filesystem.
     * @param buildId
     *            The binary's unique buildID (in base16 form).
     * @param debugLink
     *            Path to the binary's separate debug info.
     * @param isPic
     *            Whether the code in the binary is position-independent.
     */
    public UstDebugInfoBinaryFile(String filePath, @Nullable String buildId,
            @Nullable String debugLink, boolean isPic) {
        fFilePath = filePath;
        fBuildId = buildId;
        fDebugLink = debugLink;
        fIsPic = isPic;
    }

    /**
     * Get the file's path, as was referenced to in the trace.
     *
     * @return The file path
     */
    public String getFilePath() {
        return fFilePath;
    }

    /**
     * Get the build ID of the binary. It should be a unique identifier.
     *
     * On Unix systems, you can use <pre>eu-readelf -n [binary]</pre> to get
     * this ID.
     *
     * @return The file's build ID, or null if the file has no build ID..
     */
    public @Nullable String getBuildId() {
        return fBuildId;
    }

    /**
     * Get the path to the separate debug info of this binary.
     *
     * @return The path to the separate debug info, or null if the file has no
     *         separate debug info.
     */
    public @Nullable String getDebugLink() {
        return fDebugLink;
    }

    /**
     * Return whether the given file (binary or library) is Position-Independent
     * Code or not.
     *
     * This indicates whether the symbols in the ELF are absolute or relative to
     * the runtime base address, and determines which address we need to pass to
     * 'addr2line'.
     *
     * @return Whether this file is position-independent or not
     */
    public boolean isPic() {
        return fIsPic;
    }

    @Override
    public String toString() {
        return com.google.common.base.Objects.toStringHelper(this)
            .add("path", fFilePath)
            .add("build_id", fBuildId)
            .add("debug_link", fDebugLink)
            .add("is_pic", fIsPic)
            .toString();
    }

    @Override
    public int hashCode() {
        return Objects.hash(fFilePath, fBuildId, fDebugLink, fIsPic);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(getClass().equals(obj.getClass()))) {
            return false;
        }

        UstDebugInfoBinaryFile other = (UstDebugInfoBinaryFile) obj;

        return Objects.equals(fFilePath, other.fFilePath) &&
                Objects.equals(fBuildId, other.fBuildId) &&
                Objects.equals(fDebugLink, other.fDebugLink) &&
                fIsPic == other.fIsPic;
    }

    /**
     * Used for sorting. Sorts by using alphabetical order of the file
     * paths.
     */
    @Override
    public int compareTo(@Nullable UstDebugInfoBinaryFile o) {
        if (o == null) {
            return 1;
        }
        return fFilePath.compareTo(o.fFilePath);
    }
}
