/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jdt.annotation.Nullable;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

/**
 * This class manages adapter factories for traces. An adapter can be specific
 * to a given trace type id, or to traces of a given trace class.
 *
 * @since 2.0
 */
public class TmfTraceAdapterManager {

    private static Multimap<String, IAdapterFactory> fFactoriesById = HashMultimap.create();
    private static Multimap<Class<? extends ITmfTrace>, IAdapterFactory> fFactoriesByClass = HashMultimap.create();

    /**
     * Registers the given adapter factory as extending traces with the given
     * trace type id.
     * </p>
     *
     * @param factory
     *            the adapter factory
     * @param traceTypeId
     *            the trace type id of traces being extended
     */
    public static void registerFactory(IAdapterFactory factory, String traceTypeId) {
        fFactoriesById.put(traceTypeId, factory);
    }

    /**
     * Registers the given adapter factory as extending traces of the given
     * class.
     * <p>
     * If the trace class being extended is a class, the given factory's
     * adapters are available on instances of that class and any of its
     * subclasses. If it is an interface, the adapters are available to all
     * classes that directly or indirectly implement that interface.
     * </p>
     *
     * @param factory
     *            the adapter factory
     * @param traceClass
     *            the class of traces being extended
     */
    public static void registerFactory(IAdapterFactory factory, Class<? extends ITmfTrace> traceClass) {
        fFactoriesByClass.put(traceClass, factory);
    }

    /**
     * Removes the given adapter factory completely from the list of registered
     * factories.
     *
     * @param factory
     *            the adapter factory to remove
     * @see #registerFactory(IAdapterFactory, Class)
     * @see #registerFactory(IAdapterFactory, String)
     */
    public static void unregisterFactory(IAdapterFactory factory) {
        fFactoriesById.values().remove(factory);
        fFactoriesByClass.values().remove(factory);
    }

    /**
     * Returns a list of object which are instances of the given class
     * associated with the given trace. Returns an empty list if no such object
     * can be found.
     * <p>
     *
     * @param trace
     *            the trace being queried
     * @param adapterType
     *            the type of adapter to look up
     * @return a list of objects of the given adapter type
     */
    public static <T> List<T> getAdapters(ITmfTrace trace, Class<T> adapterType) {
        Collection<IAdapterFactory> factoriesById = fFactoriesById.get(trace.getTraceTypeId());
        Collection<Entry<Class<? extends ITmfTrace>, IAdapterFactory>> entries = fFactoriesByClass.entries();
        List<T> adapters = new ArrayList<>(factoriesById.size() + entries.size());
        for (IAdapterFactory factory : factoriesById) {
            @Nullable T adapter = factory.getAdapter(trace, adapterType);
            if (adapter != null) {
                adapters.add(adapter);
            }
        }
        for (Entry<Class<? extends ITmfTrace>, IAdapterFactory> entry : entries) {
            if (entry.getKey().isInstance(trace)) {
                @Nullable T adapter = entry.getValue().getAdapter(trace, adapterType);
                if (adapter != null) {
                    adapters.add(adapter);
                }
            }
        }
        return adapters;
    }

}
