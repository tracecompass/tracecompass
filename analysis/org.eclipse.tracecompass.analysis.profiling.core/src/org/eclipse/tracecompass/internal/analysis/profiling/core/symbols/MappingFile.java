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

package org.eclipse.tracecompass.internal.analysis.profiling.core.symbols;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.TreeMap;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.symbols.IMappingFile;
import org.eclipse.tracecompass.analysis.profiling.core.symbols.TmfResolvedSymbol;

/**
 * This class maps addresses to their symbol text. The mappings may contain
 * several areas of addresses. Each area should end with a symbol with suffix
 * 'END__', so that if the requested symbol is not an exact match to one of the
 * mapped symbol, it will return the closest symbol that is lesser than the
 * requested address, unless that symbol has the end suffix, it is out of the
 * area of this mapping.
 *
 * @author Mikael Ferland
 */
public final class MappingFile implements IMappingFile {

    private static final String DEFAULT_END_SUFFIX = "END__"; //$NON-NLS-1$
    private static Comparator<@NonNull Long> MAP_COMPARATOR = new Comparator<@NonNull Long>() {

        @Override
        public int compare(Long o1, Long o2) {
            return Long.compareUnsigned(o1, o2);
        }

    };

    private final String fFullPath;
    private final boolean fIsBinaryFile;
    private final NavigableMap<Long, TmfResolvedSymbol> fSymbolMapping;
    private final int fPid;

    /**
     * Create a new {@link MappingFile}
     *
     * @param path
     *            Path leading to the mapping file
     * @param isBinaryFile
     *            Type of the mapping file
     * @param results
     *            Resolved symbols for the given mapping file
     * @param pid
     *            The ID of the process this mapping applies to. A negative value
     *            means it applies to all processes
     */
    public MappingFile(String path, boolean isBinaryFile, Map<Long, TmfResolvedSymbol> results, int pid) {
        fFullPath = path;
        fIsBinaryFile = isBinaryFile;
        fSymbolMapping = new TreeMap<>(MAP_COMPARATOR);
        fSymbolMapping.putAll(results);
        fPid = pid;
    }

    @Override
    public String getFullPath() {
        return fFullPath;
    }

    @Override
    public boolean isBinaryFile() {
        return fIsBinaryFile;
    }

    private long getLastAddress() {
        return fSymbolMapping.lastKey();
    }

    @Override
    public @Nullable TmfResolvedSymbol getSymbolEntry(long address) {
        Entry<Long, TmfResolvedSymbol> floorEntry = fSymbolMapping.floorEntry(address);
        if (floorEntry == null) {
            return null;
        }
        // See if the symbol returned is the end of a block or the last symbol. In this
        // case, don't use the floor unless it hits the exact address
        TmfResolvedSymbol symbol = Objects.requireNonNull(floorEntry.getValue());
        long floorValue = symbol.getBaseAddress();
        return ((symbol.getSymbolName().endsWith(getEndSuffix()) || floorValue == getLastAddress())
                && floorValue != address) ? null : symbol;
    }

    /**
     * Get the suffix for symbols that mark the end of address blocks in the file
     *
     * @return The suffix for symbols that end blocks of mapping addresses
     */
    private static String getEndSuffix() {
        return DEFAULT_END_SUFFIX;
    }

    @Override
    public int getPid() {
        return fPid;
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
