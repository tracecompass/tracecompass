/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.callstack;

import java.util.Iterator;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;
import org.eclipse.tracecompass.tmf.core.symbols.IMappingFile;
import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;

/**
 * This class maps addresses to their corresponding symbol, but the symbols have
 * sizes and some may overlap. This is the case for instance for symbols of
 * functions inside the JVM, where an address may be within 2 symbols. When a
 * symbol is requested for an address, it will return the closest symbol that is
 * lesser than the requested address, but within the length of the symbol.
 *
 * @author Geneviève Bastien
 */
public class SizedMappingFile implements IMappingFile {

    private final String fFullPath;
    private final boolean fIsBinaryFile;
    private final ISegmentStore<TmfResolvedSizedSymbol> fSymbolStore;
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
    public SizedMappingFile(String path, boolean isBinaryFile, Map<Long, TmfResolvedSymbol> results, int pid) {
        fFullPath = path;
        fIsBinaryFile = isBinaryFile;
        fSymbolStore = SegmentStoreFactory.createSegmentStore();
        for (TmfResolvedSymbol symbol : results.values()) {
            if (symbol instanceof TmfResolvedSizedSymbol) {
                fSymbolStore.add((TmfResolvedSizedSymbol) symbol);
            }
        }
        fPid = pid;
    }

    @Override
    public @NonNull String getFullPath() {
        return fFullPath;
    }

    @Override
    public boolean isBinaryFile() {
        return fIsBinaryFile;
    }

    @Override
    public @Nullable TmfResolvedSymbol getSymbolEntry(long address) {
        Iterable<TmfResolvedSizedSymbol> symbols = fSymbolStore.getIntersectingElements(address);
        Iterator<TmfResolvedSizedSymbol> iterator = symbols.iterator();
        TmfResolvedSizedSymbol symbol = null;
        while (iterator.hasNext()) {
            TmfResolvedSizedSymbol current = iterator.next();
            if (symbol == null || symbol.getBaseAddress() < current.getBaseAddress()) {
                symbol = current;
            }
        }
        return symbol;
    }

    @Override
    public int getPid() {
        return fPid;
    }

}
