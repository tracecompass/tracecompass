/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Robert Kiss - Initial API and implementation
 *   Mikael Ferland - Refactor API to support multiple symbol files
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.symbols;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
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

    private final @NonNull List<@NonNull MappingFile> fMappingFiles = new ArrayList<>();

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
     * @since 3.0
     */
    public synchronized @NonNull List<@NonNull MappingFile> getMappingFiles() {
        return ImmutableList.copyOf(fMappingFiles);
    }

    /**
     * @param mappingFiles
     *            List of mapping files for symbol resolving
     * @since 3.0
     */
    public synchronized void setMappingFiles(@NonNull List<@NonNull MappingFile> mappingFiles) {
        fMappingFiles.clear();
        fMappingFiles.addAll(mappingFiles);
    }

    @Override
    public void loadConfiguration(IProgressMonitor monitor) {
        // Do nothing because the resolved symbols are already stored in
        // fMappingFiles
    }

    @Override
    public @Nullable String getSymbolText(long address) {
        for (MappingFile mf : fMappingFiles) {
            String key = Long.toHexString(address);
            Map<String, String> symbolMapping = mf.getSymbolMapping();
            if (symbolMapping.containsKey(key)) {
                return symbolMapping.get(key);
            }
        }
        return null;
    }

    @Override
    public ISymbolProviderPreferencePage createPreferencePage() {
        return new BasicSymbolProviderPreferencePage(this);
    }

}
