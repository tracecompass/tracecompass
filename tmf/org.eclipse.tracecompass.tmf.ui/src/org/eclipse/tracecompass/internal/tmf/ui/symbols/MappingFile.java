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

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MappingFile} represents a mapping file selected by a user through
 * the basic symbol provider preference page
 *
 * @author Mikael Ferland
 * @since 3.0
 */
public final class MappingFile {
    private final String fFullPath;
    private final boolean fIsBinaryFile;
    private final Map<String, String> fSymbolMapping = new HashMap<>();

    /**
     * Create a new {@link MappingFile}
     *
     * @param path
     *            Path leading to the mapping file
     * @param isBinaryFile
     *            Type of the mapping file
     * @param symbolMapping
     *            Resolved symbols for the given mapping file
     */
    public MappingFile(String path, boolean isBinaryFile, Map<String, String> symbolMapping) {
        fFullPath = path;
        fIsBinaryFile = isBinaryFile;
        fSymbolMapping.putAll(symbolMapping);
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
    public Map<String, String> getSymbolMapping() {
        return fSymbolMapping;
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
