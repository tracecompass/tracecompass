/*******************************************************************************
 * Copyright (c) 2016 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.symbols;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;

/**
 * An ISymbolProvider is used to map symbol addresses that might be found inside
 * an {@link TmfTrace} into human readable strings. This interface should be
 * used to augment
 * {@link org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider} to support
 * preference pages.
 *
 * @author Robert Kiss
 * @since 2.0
 * @see ISymbolProviderFactory
 */
public interface ISymbolProvider extends org.eclipse.tracecompass.tmf.core.symbols.ISymbolProvider {

    /**
     * Return additional information regarding the symbol from the given address
     * or null if the symbol cannot be found
     *
     * @param address
     *            the address of the symbol
     * @return the symbol {@link ITmfCallsite} information or null if the symbol
     *         cannot be found
     * @deprecated This interface should only provide function/symbol names, not
     *             full source locations.
     */
    @Deprecated
    @Nullable ITmfCallsite getSymbolInfo(long address);

    /**
     * Return additional information regarding the symbol from the given
     * pid/timestamp/address tuple, or null if the symbol cannot be found.
     *
     * @param pid
     *            The process Id for which to query
     * @param timestamp
     *            The timestamp of the query
     * @param address
     *            the address of the symbol
     * @return the symbol {@link ITmfCallsite} information or null if the symbol
     *         cannot be found
     * @deprecated This interface should only provide function/symbol names, not
     *             full source locations.
     */
    @Deprecated
    default @Nullable ITmfCallsite getSymbolInfo(int pid, long timestamp, long address) {
        return getSymbolInfo(address);
    }

    /**
     * Create the {@link ISymbolProviderPreferencePage} that can be used to
     * configure this {@link ISymbolProvider}
     *
     * @return the {@link ISymbolProviderPreferencePage} or null if this symbol
     *         provider does not offer a configuration UI
     */
    @Nullable ISymbolProviderPreferencePage createPreferencePage();

}
