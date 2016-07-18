/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.ui.symbols;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.callstack.FunctionNameMapper;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.event.lookup.TmfCallsite;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProviderPreferencePage;

/**
 * The {@link BasicSymbolProvider} can use either an executable or a simple
 * symbol mapping file to resolve symbols.
 *
 * @author Robert Kiss
 *
 */
public class BasicSymbolProvider implements ISymbolProvider {

    private final @NonNull ITmfTrace fTrace;

    private @NonNull Map<String, String> fMapping = Collections.emptyMap();

    private String fSource;

    private @NonNull SourceKind fKind = SourceKind.BINARY;

    private boolean fConfigured;

    /**
     * The kind of source this provider is configured with
     *
     */
    public static enum SourceKind {
        /**
         * Literal for binary configuration
         */
        BINARY,

        /**
         * Literal for mapping configuration
         */
        MAPPING;
    }

    /**
     * Create a new {@link BasicSymbolProvider} for the given trace
     *
     * @param trace
     *            A non-null trace
     */
    public BasicSymbolProvider(@NonNull ITmfTrace trace) {
        fTrace = trace;
    }

    /**
     *
     * @return the configured source
     */
    public String getConfiguredSource() {
        return fSource;
    }

    /**
     * @return the configured source kind
     */
    public @NonNull SourceKind getConfiguredSourceKind() {
        return fKind;
    }

    /**
     * Set the configuration to the given source and kind.
     *
     * @param fileSource
     *            File path to either a binary file or a mapping file.
     * @param kind
     *            the type of the referenced file
     */
    public void setConfiguredSource(String fileSource, @NonNull SourceKind kind) {
        fSource = fileSource;
        fKind = kind;
        fConfigured = false;
    }

    @Override
    public @NonNull ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public void loadConfiguration(IProgressMonitor monitor) {
        if (!fConfigured) {
            synchronized (this) {
                if (!fConfigured) {
                    try {
                        fMapping = Collections.emptyMap();
                        if (fSource != null) {
                            File file = new File(fSource);
                            if (file.isFile()) {
                                Map<String, String> result;
                                if (fKind == SourceKind.BINARY) {
                                    result = FunctionNameMapper.mapFromBinaryFile(file);
                                } else {
                                    result = FunctionNameMapper.mapFromNmTextFile(file);
                                }
                                if (result != null) {
                                    fMapping = result;
                                }
                            }
                        }
                    } finally {
                        fConfigured = true;
                    }
                }
            }
        }
    }

    @Override
    public @Nullable String getSymbolText(long address) {
        loadConfiguration(null);
        return fMapping.get(Long.toHexString(address));
    }

    @Deprecated
    @Override
    public @Nullable ITmfCallsite getSymbolInfo(long address) {
        loadConfiguration(null);
        String symbolText = getSymbolText(address);
        if (symbolText != null) {
            return new TmfCallsite(null, symbolText, -1);
        }
        return null;
    }

    @Override
    public ISymbolProviderPreferencePage createPreferencePage() {
        return new BasicSymbolProviderPreferencePage(this);
    }

}
