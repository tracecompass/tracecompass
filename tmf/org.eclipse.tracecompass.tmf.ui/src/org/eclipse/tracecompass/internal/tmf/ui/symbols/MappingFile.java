/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mikael Ferland - Initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.symbols;

import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MappingFile} represents a mapping file selected by a user through
 * the basic symbol provider preference page
 *
 * @author Mikael Ferland
 * @since 3.0
 */
public final class MappingFile {

    private final @NonNull String DEFAULT_END_SUFFIX = "END__"; //$NON-NLS-1$

    private final String fFullPath;
    private final boolean fIsBinaryFile;
    private final @NonNull NavigableMap<Long, String> fSymbolMapping;

    /**
     * Create a new {@link MappingFile}
     *
     * @param path
     *            Path leading to the mapping file
     * @param isBinaryFile
     *            Type of the mapping file
     * @param results
     *            Resolved symbols for the given mapping file
     */
    public MappingFile(String path, boolean isBinaryFile, Map<Long, String> results) {
        fFullPath = path;
        fIsBinaryFile = isBinaryFile;
        fSymbolMapping = new TreeMap<>(results);
    }

    /**
     * @return path leading to mapping file
     */
    public String getFullPath() {
        return fFullPath;
    }

    /**
     * @return type of the mapping file
     */
    public boolean isBinaryFile() {
        return fIsBinaryFile;
    }

    /**
     * @return resolved symbols for the given mapping file
     */
    public NavigableMap<Long, String> getSymbolMapping() {
        return fSymbolMapping;
    }

    /**
     * Get the suffix for symbols that mark the end of address blocks in the
     * file
     *
     * @return The suffix for symbols that end blocks of mapping addresses
     */
    public String getEndSuffix() {
        return DEFAULT_END_SUFFIX;
    }

    @Override
    public int hashCode() {
        return Objects.hash(fFullPath, fIsBinaryFile, fSymbolMapping);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        MappingFile other = (MappingFile) obj;
        return (fFullPath.equals(other.fFullPath)) && (fIsBinaryFile == other.fIsBinaryFile) && (fSymbolMapping.equals(other.fSymbolMapping));
    }
}
