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
import java.util.Map.Entry;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;

/**
 * The {@link MappingFile} represents a mapping file selected by a user through
 * the basic symbol provider preference page
 *
 * @author Mikael Ferland
 */
public final class MappingFile {

    private final @NonNull String DEFAULT_END_SUFFIX = "END__"; //$NON-NLS-1$

    private final String fFullPath;
    private final boolean fIsBinaryFile;
    private final @NonNull NavigableMap<Long, TmfResolvedSymbol> fSymbolMapping;

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
    public MappingFile(String path, boolean isBinaryFile, Map<Long, TmfResolvedSymbol> results) {
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
     * Get the entry that may correspond to the symbol
     *
     * @param address
     *            The address of the symbol to look for
     * @return The entry with its address/symbol if it's within this mapping's space
     */
    public TmfResolvedSymbol getSymbolEntry(long address) {
        Entry<Long, TmfResolvedSymbol> floorEntry = fSymbolMapping.floorEntry(address);
        if (floorEntry == null) {
            return null;
        }
        // See if the symbol returned is the end of a block, in this case, don't
        // use the floor unless it hits the exact address
        TmfResolvedSymbol symbol = floorEntry.getValue();
        long floorValue = symbol.getBaseAddress();
        return (symbol.getSymbolName().endsWith(getEndSuffix()) && floorValue != address) ? null : symbol;
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
