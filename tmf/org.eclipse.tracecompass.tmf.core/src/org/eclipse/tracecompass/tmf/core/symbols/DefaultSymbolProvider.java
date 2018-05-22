/*******************************************************************************
 * Copyright (c) 2016-2017 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.symbols;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * A default implementation of the {@link ISymbolProvider} which return a hex
 * format representation of the symbol address
 *
 * @author Robert Kiss
 * @since 3.0
 */
public class DefaultSymbolProvider implements ISymbolProvider {

    private final ITmfTrace fTrace;

    /**
     * Create a new provider for the given trace
     *
     * @param trace
     *            the trace
     */
    public DefaultSymbolProvider(ITmfTrace trace) {
        fTrace = trace;
    }

    @Override
    public void loadConfiguration(@Nullable IProgressMonitor monitor) {
        // no configuration here
    }

    @Override
    public ITmfTrace getTrace() {
        return fTrace;
    }

    @Override
    public @NonNull TmfResolvedSymbol getSymbol(long address) {
        if ((address & (0xFFFFFFFF << 32)) == 0) {
            return new TmfResolvedSymbol(address, String.format("%08x", address)); //$NON-NLS-1$
        }
        return new TmfResolvedSymbol(address, String.format("%016x", address)); //$NON-NLS-1$
    }

    @Override
    public @Nullable TmfResolvedSymbol getSymbol(int pid, long timestamp, long address) {
        return getSymbol(address);
    }

}
