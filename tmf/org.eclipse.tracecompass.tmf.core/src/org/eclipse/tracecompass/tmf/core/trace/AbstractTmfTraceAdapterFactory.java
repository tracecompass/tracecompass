/*******************************************************************************
 * Copyright (c) 2015, 2021 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Patrick Tasse - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.trace;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

/**
 * Base class for a trace adapter factory. The factory creates a single instance
 * of each adapter type per trace, and disposes the adapter when the trace is
 * closed, if it is an instance of {@link IDisposableAdapter}.
 *
 * @since 2.0
 */
public abstract class AbstractTmfTraceAdapterFactory implements IAdapterFactory {

    /**
     * Interface for trace adapters that manage resources which must be freed
     * when the trace is closed.
     */
    public interface IDisposableAdapter {
        /**
         * Disposes of this trace adapter. All resources must be freed.
         */
        void dispose();
    }

    private final Table<ITmfTrace, Class<?>, Object> fAdapters = HashBasedTable.create();

    /**
     * Constructor.
     */
    public AbstractTmfTraceAdapterFactory() {
        TmfSignalManager.register(this);
    }

    /**
     * Disposes the trace adapter factory's resources and all of its adapters.
     */
    public synchronized void dispose() {
        TmfSignalManager.deregister(this);
        disposeAdapters(fAdapters.values());
        fAdapters.clear();
    }

    private static void disposeAdapters(Collection<Object> adapters) {
        for (Object adapter : adapters) {
            if (adapter instanceof IDisposableAdapter) {
                ((IDisposableAdapter) adapter).dispose();
            }
        }
    }

    @Override
    public synchronized <T> T getAdapter(Object adaptableObject, Class<T> adapterType) {
        if (adaptableObject instanceof ITmfTrace) {
            ITmfTrace trace = (ITmfTrace) adaptableObject;
            Object adapter = fAdapters.get(trace, adapterType);
            if (adapter == null) {
                adapter = getTraceAdapter(trace, adapterType);
            }
            if (adapter != null) {
                fAdapters.put(trace, adapterType, adapter);
                return adapterType.cast(adapter);
            }
        }
        return null;
    }

    /**
     * Returns an object which is an instance of the given class associated with
     * the given trace. Returns null if no such object can be found.
     *
     * @param trace
     *            the trace being adapted
     * @param adapterType
     *            the type of adapter to look up
     * @return a object of the given adapter type, or null if this factory does
     *         not have an adapter of the given type for the given trace
     */
    protected abstract <T> @Nullable T getTraceAdapter(@NonNull ITmfTrace trace, Class<T> adapterType);

    /**
     * Signal handler for the trace closed signal.
     *
     * @param signal
     *            the trace closed signal
     */
    @TmfSignalHandler
    public synchronized void traceClosed(TmfTraceClosedSignal signal) {
        for (ITmfTrace trace : TmfTraceManager.getTraceSetWithExperiment(signal.getTrace())) {
            Map<Class<?>, Object> row = fAdapters.row(trace);
            disposeAdapters(row.values());
            row.clear();
        }
    }
}
