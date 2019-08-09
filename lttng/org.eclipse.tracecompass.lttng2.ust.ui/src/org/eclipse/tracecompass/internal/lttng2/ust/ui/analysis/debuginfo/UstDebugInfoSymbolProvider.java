/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.lttng2.ust.ui.analysis.debuginfo;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.BinaryCallsite;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.FunctionLocation;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoAnalysisModule;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoBinaryAspect;
import org.eclipse.tracecompass.lttng2.ust.core.analysis.debuginfo.UstDebugInfoFunctionAspect;
import org.eclipse.tracecompass.lttng2.ust.core.trace.LttngUstTrace;
import org.eclipse.tracecompass.tmf.core.symbols.DefaultSymbolProvider;
import org.eclipse.tracecompass.tmf.core.symbols.SymbolProviderManager;
import org.eclipse.tracecompass.tmf.core.symbols.TmfResolvedSymbol;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProvider;
import org.eclipse.tracecompass.tmf.ui.symbols.ISymbolProviderPreferencePage;

/**
 * Symbol provider for UST traces with debug information.
 *
 * @author Alexandre Montplaisir
 * @see UstDebugInfoAnalysisModule
 */
public class UstDebugInfoSymbolProvider extends DefaultSymbolProvider implements ISymbolProvider {

    private final List<org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider> fOtherProviders = new ArrayList<>();
    private boolean fIsLoaded = false;
    /**
     * Create a new {@link UstDebugInfoSymbolProvider} for the given trace
     *
     * @param trace
     *            A non-null trace
     */
    public UstDebugInfoSymbolProvider(LttngUstTrace trace) {
        super(trace);
    }

    /**
     * Sets the configured path prefix. Usually called from the preferences
     * page.
     *
     * @param newPathPrefix
     *            The new path prefix to use
     */
    void setConfiguredPathPrefix(LttngUstTrace.SymbolProviderConfig newConfig) {
        getTrace().setSymbolProviderConfig(newConfig);
    }

    @Override
    public void loadConfiguration(@Nullable IProgressMonitor monitor) {
        synchronized (fOtherProviders) {
            if (!fIsLoaded) {
                super.loadConfiguration(monitor);
                // Get all the symbol providers that are not of default class
                for (org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider provider : SymbolProviderManager.getInstance().getSymbolProviders(getTrace())) {
                    if (!(provider instanceof DefaultSymbolProvider)) {
                        fOtherProviders.add(provider);
                    }
                }
                fIsLoaded = true;
            }
        }
    }

    @Override
    public @NonNull LttngUstTrace getTrace() {
        /* Type enforced at constructor */
        return (LttngUstTrace) super.getTrace();
    }

    @Override
    public @Nullable TmfResolvedSymbol getSymbol(int pid, long timestamp, long address) {
        BinaryCallsite bc = UstDebugInfoBinaryAspect.getBinaryCallsite(getTrace(), pid, timestamp, address);
        if (bc == null) {
            return null;
        }

        FunctionLocation loc = UstDebugInfoFunctionAspect.getFunctionFromBinaryLocation(bc);
        if (loc != null) {
            return new TmfResolvedSymbol(bc.getOffset(), loc.getFunctionName());
        }
        if (!fIsLoaded) {
            return null;
        }
        // Try to see if some other symbol provider has a symbol for this
        // relative binary callsite
        // FIXME: Ideally, it would be good to be able to specify the filename
        for (org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider provider : fOtherProviders) {
            TmfResolvedSymbol symbol = provider.getSymbol(pid, timestamp, bc.getOffset());
            if (symbol != null) {
                return symbol;
            }
        }
        return null;
    }

    @Override
    public @NonNull ISymbolProviderPreferencePage createPreferencePage() {
        return new UstDebugInfoSymbolProviderPreferencePage(this);
    }

}
