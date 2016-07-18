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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.event.lookup.ITmfCallsite;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;

/**
 * An ISymbolProvider is used to map symbol addresses that might be found inside
 * an {@link TmfTrace} into human readable strings.
 *
 * @author Robert Kiss
 * @since 2.0
 * @see ISymbolProviderFactory
 */
public interface ISymbolProvider {

    /**
     * @return the trace that this class resolves symbols for
     */
    @NonNull ITmfTrace getTrace();

    /**
     * Some providers might have configurations that take some time to load. All
     * the CPU intensive load operations shall be done in this method. The
     * adopters shall call this method at an opportune moment when cancellation
     * and UI feedback is possible. However, the implementors of this interface
     * shall not assume that this method has been called.
     *
     * @param monitor
     *            The progress monitor to use, can be null
     */
    void loadConfiguration(IProgressMonitor monitor);

    /**
     * Return the symbol text corresponding to the given address or null if
     * there is no such symbol
     *
     * @param address
     *            the address of the symbol
     * @return the symbol text or null if the symbol cannot be found
     */
    @Nullable String getSymbolText(long address);

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
     * Return the symbol text corresponding to the given pid/timestamp/address
     * tuple, or null if there is no such symbol.
     *
     * @param pid
     *            The process Id for which to query
     * @param timestamp
     *            The timestamp of the query
     * @param address
     *            the address of the symbol
     * @return the symbol text or null if the symbol cannot be found
     */
    default @Nullable String getSymbolText(int pid, long timestamp, long address) {
        return getSymbolText(address);
    }

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
