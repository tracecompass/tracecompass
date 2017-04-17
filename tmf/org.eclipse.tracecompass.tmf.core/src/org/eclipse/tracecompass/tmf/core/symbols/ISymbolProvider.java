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
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;

/**
 * An ISymbolProvider is used to map symbol addresses that might be found inside
 * an {@link TmfTrace} into human readable strings.
 *
 * @author Matthew Khouzam
 * @author Robert Kiss
 * @since 2.4
 */
public interface ISymbolProvider {

    /**
     * @return the trace that this class resolves symbols for
     */
    ITmfTrace getTrace();

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
    void loadConfiguration(@Nullable IProgressMonitor monitor);

    /**
     * Return the symbol text corresponding to the given address or null if
     * there is no such symbol
     *
     * @param address
     *            the address of the symbol
     * @return the symbol text or <code>null</code> if the symbol cannot be found
     */
    @Nullable String getSymbolText(long address);

    /**
     * Return the symbol text corresponding to the given pid/timestamp/address
     * tuple, or null if there is no such symbol. An implementation that does
     * not support pid and timestamp should return the symbol based on address
     * only.
     *
     * A caller that has pid and timestamp information should call this method.
     * {@link #getSymbolText(long)} should only be invoked by callers that do
     * not have access to the pid and timestamp.
     *
     * @param pid
     *            The process Id for which to query
     * @param timestamp
     *            The timestamp of the query
     * @param address
     *            the address of the symbol
     * @return the symbol text or <code>null</code> if the symbol cannot be
     *         found
     */
    default @Nullable String getSymbolText(int pid, long timestamp, long address) {
        return getSymbolText(address);
    }
}
