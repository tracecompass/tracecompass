/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.symbols;

import java.util.Collection;
import java.util.function.Function;

import org.eclipse.jdt.annotation.Nullable;

/**
 * Utility class to resolve symbols from providers
 *
 * @author Geneviève Bastien
 * @since 3.1
 */
public final class SymbolProviderUtils {

    private SymbolProviderUtils() {
        // Nothing to do
    }

    /**
     * Utility method to get the symbol text from multiple symbol providers. It
     * looks at all symbol providers for a symbol at the requested address, and
     * return the one whose base address is closest to the requested one.
     *
     * @param providers
     *            The collection of symbol providers to search for the symbol
     * @param address
     *            The address of the symbol
     * @return The string this symbol resolves to, or its hexadecimal representation
     *         if not found
     */
    public static String getSymbolText(Collection<ISymbolProvider> providers, long address) {
        return getSymbolText(providers, provider -> provider.getSymbol(address), address);
    }

    /**
     * Utility method to get the symbol text from multiple symbol providers. It
     * looks at all symbol providers for a symbol at the requested address, and
     * return the one whose base address is closest to the requested one.
     *
     * @param providers
     *            The collection of symbol providers to search for the symbol
     * @param pid
     *            The process Id for which to query
     * @param timestamp
     *            The timestamp of the query
     * @param address
     *            the address of the symbol
     * @return The string this symbol resolves to, or its hexadecimal representation
     *         if not found
     */
    public static String getSymbolText(Collection<ISymbolProvider> providers, int pid, long timestamp, long address) {
        return getSymbolText(providers, provider -> provider.getSymbol(pid, timestamp, address), address);
    }

    private static String getSymbolText(Collection<ISymbolProvider> providers, Function<ISymbolProvider, @Nullable TmfResolvedSymbol> func, long address) {
        TmfResolvedSymbol resolvedSymbol = null;
        for (ISymbolProvider provider : providers) {
            TmfResolvedSymbol currentSymbol = func.apply(provider);
            if (currentSymbol != null) {
                if (resolvedSymbol == null) {
                    resolvedSymbol = currentSymbol;
                } else {
                    resolvedSymbol = (currentSymbol.getBaseAddress() > resolvedSymbol.getBaseAddress() ? currentSymbol : resolvedSymbol);
                }
            }
        }
        return resolvedSymbol != null ? resolvedSymbol.getSymbolName() : "0x" + Long.toHexString(address); //$NON-NLS-1$
    }

}
