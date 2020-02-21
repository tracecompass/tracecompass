/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Robert Kiss - Initial API and implementation
 *   Mikael Ferland - Refactor API to support multiple symbol files
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.ui.symbols;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.symbols.IMappingFile;
import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProviderPreferencePage;

import com.google.common.collect.ImmutableList;

/**
 * The {@link BasicSymbolProvider} can use either an executable or a simple
 * symbol mapping file to resolve symbols.
 *
 * @author Robert Kiss
 * @author Mikael Ferland
 *
 */
public class BasicSymbolProvider implements ISymbolProvider {

    private final @NonNull ITmfTrace fTrace;

    private final @NonNull List<@NonNull IMappingFile> fMappingFiles = new ArrayList<>();

    /**
     * Create a new {@link BasicSymbolProvider} for the given trace
     *
     * @param trace
     *            A non-null trace
     */
    public BasicSymbolProvider(@NonNull ITmfTrace trace) {
        fTrace = trace;
    }

    @Override
    public @NonNull ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * @return mapping files for a given trace
     */
    public synchronized @NonNull List<@NonNull IMappingFile> getMappingFiles() {
        return ImmutableList.copyOf(fMappingFiles);
    }

    /**
     * @param mappingFiles
     *            List of mapping files for symbol resolving
     * @since 3.0
     */
    public synchronized void setMappingFiles(@NonNull List<@NonNull IMappingFile> mappingFiles) {
        fMappingFiles.clear();
        fMappingFiles.addAll(mappingFiles);
    }

    @Override
    public void loadConfiguration(IProgressMonitor monitor) {
        // Do nothing because the resolved symbols are already stored in
        // fMappingFiles
    }

    @Override
    public @Nullable TmfResolvedSymbol getSymbol(long address) {
        return getSymbol(address, fMappingFiles);
    }

    @Override
    public @Nullable TmfResolvedSymbol getSymbol(int pid, long timestamp, long address) {
        // First look at the mapping files that are specific for the process
        TmfResolvedSymbol symbol = getSymbol(address, fMappingFiles.stream()
                .filter(mf -> mf.getPid() == pid)
                .collect(Collectors.toList()));
        if (symbol != null) {
            return symbol;
        }

        // The symbol was not found, look in global mapping files
        return getSymbol(address, fMappingFiles.stream()
                .filter(mf -> mf.getPid() < 0)
                .collect(Collectors.toList()));
    }

    private static @Nullable TmfResolvedSymbol getSymbol(long address, List<IMappingFile> mappingFiles) {
        TmfResolvedSymbol currentFloorEntry = null;
        for (IMappingFile mf : mappingFiles) {
            TmfResolvedSymbol floorEntry = mf.getSymbolEntry(address);
            if (floorEntry == null) {
                continue;
            }
            long floorValue = floorEntry.getBaseAddress();
            // A symbol may come from different file, prioritize the symbol
            // closest to value
            if (floorValue == address) {
                return floorEntry;
            }
            if (currentFloorEntry == null) {
                currentFloorEntry = floorEntry;
            } else {
                if (address - floorValue < address - currentFloorEntry.getBaseAddress()) {
                    currentFloorEntry = floorEntry;
                }
            }
        }
        if (currentFloorEntry == null) {
            return null;
        }
        return currentFloorEntry;
    }

    @Override
    public ISymbolProviderPreferencePage createPreferencePage() {
        return new BasicSymbolProviderPreferencePage(this);
    }
}
