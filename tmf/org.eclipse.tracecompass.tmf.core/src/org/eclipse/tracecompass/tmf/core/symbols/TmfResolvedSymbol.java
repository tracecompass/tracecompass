/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.symbols;

import java.util.Comparator;

import org.eclipse.jdt.annotation.Nullable;

import com.google.common.base.Objects;

/**
 * A class that matches the base address of a symbol with the associated name.
 *
 * @author Geneviève Bastien
 * @since 3.2
 * @deprecated Use the class with same name in the
 *             org.eclipse.tracecompass.analysis.profiling.core plugin
 */
@Deprecated
public class TmfResolvedSymbol {

    /**
     * The comparator to compare two symbols by their base address
     */
    public static final Comparator<TmfResolvedSymbol> COMPARATOR = Comparator.comparing(TmfResolvedSymbol::getBaseAddress);

    private final long fAddress;
    private final String fName;

    /**
     * Constructor
     *
     * @param address
     *            The address of this symbol
     * @param name
     *            The name this symbol resolves to
     */
    public TmfResolvedSymbol(long address, String name) {
        fAddress = address;
        fName = name;
    }

    /**
     * Get the base address of this symbol, ie, the address where this symbol starts
     *
     * @return The base address of the symbol
     */
    public long getBaseAddress() {
        return fAddress;
    }

    /**
     * Get the name this symbol resolves to
     *
     * @return The name of the symbol
     */
    public String getSymbolName() {
        return fName;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(fAddress, fName);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (!(obj instanceof TmfResolvedSymbol)) {
            return false;
        }
        TmfResolvedSymbol other = (TmfResolvedSymbol) obj;
        return (fAddress == other.fAddress && fName.equals(other.fName));
    }

    @Override
    public String toString() {
        return Long.toHexString(fAddress) + ' ' + fName;
    }

}
