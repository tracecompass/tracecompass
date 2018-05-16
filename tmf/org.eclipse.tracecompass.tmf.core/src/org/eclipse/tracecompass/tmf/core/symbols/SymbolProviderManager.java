/*******************************************************************************
 * Copyright (c) 2016, 2017 Movidius Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors
 *    Robert Kiss - Initial API and implementation
 *    Mikael Ferland - Support multiple symbol providers for a trace
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.symbols;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * This class offer services around the
 * <code>org.eclipse.tracecompass.tmf.core.symbolProvider</code> extension
 * point.
 *
 * @author Robert Kiss
 * @since 3.0
 * @deprecated Use the class with same name in the
 *             org.eclipse.tracecompass.analysis.profiling.core plugin
 */
@Deprecated
public final class SymbolProviderManager {

    /**
     * The singleton instance of this manager
     */
    private static @Nullable SymbolProviderManager INSTANCE;

    private static final String OLD_EXTENSION_POINT_ID = "org.eclipse.tracecompass.tmf.ui.symbolProvider"; //$NON-NLS-1$
    private static final String EXTENSION_POINT_ID = "org.eclipse.tracecompass.tmf.core.symbolProvider"; //$NON-NLS-1$
    private static final String ELEM_NAME_PROVIDER = "providerFactory"; //$NON-NLS-1$
    private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
    private static final String ATTR_PRIORITY = "priority"; //$NON-NLS-1$

    private final List<SymbolProviderFactoryWrapper> fProviders;

    private final Multimap<ITmfTrace, WeakReference<ISymbolProvider>> fInstances = LinkedHashMultimap.create();

    /**
     * Internal class used to store extension point information
     *
     */
    private static class SymbolProviderFactoryWrapper {

        public final ISymbolProviderFactory factory;
        public final int priority;

        private SymbolProviderFactoryWrapper(ISymbolProviderFactory factory, int priority) {
            this.factory = factory;
            this.priority = priority;
        }
    }

    /**
     *
     * @return the singleton instance of this class
     */
    public static synchronized SymbolProviderManager getInstance() {
        SymbolProviderManager manager = INSTANCE;
        if (manager == null) {
            manager = new SymbolProviderManager();
            INSTANCE = manager;
        }
        return manager;
    }

    /**
     * Dispose the singleton instance if it exists
     *
     * @since 3.3
     */
    public static synchronized void dispose() {
        SymbolProviderManager manager = INSTANCE;
        if (manager != null) {
            TmfSignalManager.deregister(manager);
            manager.fProviders.clear();
            manager.fInstances.clear();
        }
        INSTANCE = null;
    }

    /**
     * The private constructor of this manager
     */
    private SymbolProviderManager() {
        fProviders = new ArrayList<>();
        load(OLD_EXTENSION_POINT_ID);
        load(EXTENSION_POINT_ID);
        // Those with a higher priority need to be on top
        fProviders.sort(Comparator.comparingLong(o -> -o.priority));
        TmfSignalManager.register(this);
    }

    private void load(String configElemPath) {
        IConfigurationElement[] configElements = Platform.getExtensionRegistry().getConfigurationElementsFor(configElemPath);
        for (IConfigurationElement element : configElements) {
            if (element != null && ELEM_NAME_PROVIDER.equals(element.getName())) {
                try {
                    Object extension = checkNotNull(element.createExecutableExtension(ATTR_CLASS));
                    int priority = 0;
                    try {
                        priority = Integer.parseInt(element.getAttribute(ATTR_PRIORITY));
                    } catch (NumberFormatException e) {
                        // safe to ignore
                    }
                    fProviders.add(new SymbolProviderFactoryWrapper((ISymbolProviderFactory) extension, priority));
                } catch (CoreException | ClassCastException e) {
                    Activator.logError("Exception while loading extensions", e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Locate the {@link ISymbolProvider}s capable of resolving symbols from the
     * given trace. If no such provider(s) are defined, a collection containing
     * an instance of {@link DefaultSymbolProvider} will be returned
     *
     * @param trace
     *            The trace to create a provider for
     * @return The collection of symbol providers for this trace. It will
     *         contain at least one valid {@link ISymbolProvider}.
     */
    public Collection<ISymbolProvider> getSymbolProviders(ITmfTrace trace) {
        synchronized (fInstances) {
            Collection<ISymbolProvider> symbolProviders = new ArrayList<>();

            // Verify if there are already provider(s) for this trace
            for (WeakReference<ISymbolProvider> reference : fInstances.get(trace)) {
                ISymbolProvider provider = reference.get();
                if (provider != null) {
                    symbolProviders.add(provider);
                }
            }

            // Build the appropriate provider(s)
            if (symbolProviders.isEmpty()) {
                for (ITmfTrace subTrace : TmfTraceManager.getTraceSet(trace)) {
                    // Not the same trace, so get the sub trace's symbol providers
                    if (subTrace != trace) {
                        Collection<ISymbolProvider> traceSymbolProviders = getSymbolProviders(subTrace);
                        traceSymbolProviders.forEach(sp -> {
                            symbolProviders.add(sp);
                            fInstances.put(trace, new WeakReference<>(sp));
                        });
                    } else {
                        // Create the symbol providers for this trace
                        for (SymbolProviderFactoryWrapper wrapper : fProviders) {
                            ISymbolProviderFactory factory = wrapper.factory;
                            ISymbolProvider provider = factory.createProvider(trace);
                            if (provider != null) {

                                symbolProviders.add(provider);
                                fInstances.put(trace, new WeakReference<>(provider));
                            }
                        }
                    }
                }
            }

            // Build the default provider if required
            if (symbolProviders.isEmpty()) {
                DefaultSymbolProvider defaultSymbolProvider = new DefaultSymbolProvider(trace);
                fInstances.put(trace, new WeakReference<>(defaultSymbolProvider));
                symbolProviders.add(defaultSymbolProvider);
            }
            return symbolProviders;
        }
    }

    /**
     * Signal handler for the traceClosed signal.
     *
     * @param signal
     *            The incoming signal
     * @since 3.3
     */
    @TmfSignalHandler
    public synchronized void traceClosed(final TmfTraceClosedSignal signal) {
        for (ITmfTrace trace : TmfTraceManager.getTraceSet(signal.getTrace())) {
            fInstances.removeAll(trace);
        }
    }
}
