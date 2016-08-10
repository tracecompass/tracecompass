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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.ui.Activator;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.ImmutableList;

/**
 * This class offer services around the
 * <code>org.eclipse.tracecompass.tmf.ui.symbolProvider</code> extension point.
 *
 * @author Robert Kiss
 * @since 2.0
 *
 */
public final class SymbolProviderManager {

    /**
     * The singleton instance of this manager
     */
    private static SymbolProviderManager INSTANCE;

    private static final String EXTENSION_POINT_ID = "org.eclipse.tracecompass.tmf.ui.symbolProvider"; //$NON-NLS-1$
    private static final String ELEM_NAME_PROVIDER = "providerFactory"; //$NON-NLS-1$
    private static final String ATTR_CLASS = "class"; //$NON-NLS-1$
    private static final String ATTR_PRIORITY = "priority"; //$NON-NLS-1$

    private final List<SymbolProviderFactoryWrapper> fProviders;

    private final Map<ITmfTrace, WeakReference<ISymbolProvider>> fInstances = new WeakHashMap<>();

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
     * Get the instance of the {@link SymbolProviderManager}
     * @return the singleton instance of this class
     */
    @SuppressWarnings("null")
    public static synchronized @NonNull SymbolProviderManager getInstance() {
        if (INSTANCE == null) {
            List<@NonNull SymbolProviderFactoryWrapper> providers = new ArrayList<>();
            IConfigurationElement[] configElements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
            for (IConfigurationElement element : configElements) {
                if (ELEM_NAME_PROVIDER.equals(element.getName())) {
                    try {
                        Object extension = element.createExecutableExtension(ATTR_CLASS);
                        int priority = 0;
                        try {
                            priority = Integer.parseInt(element.getAttribute(ATTR_PRIORITY));
                        } catch (NumberFormatException e) {
                            // safe to ignore
                        }
                        providers.add(new SymbolProviderFactoryWrapper((ISymbolProviderFactory) extension, priority));
                    } catch (CoreException | ClassCastException e) {
                        Activator.getDefault().logError("Exception while loading extensions", e); //$NON-NLS-1$
                    }
                }
            }
            /*
             * Those with a higher priority need to be on top
             *
             * Note: we cannot simply sort by negative priority because
             * (-Integer.MIN_VAL) == Integer.MIN_VAL
             */
            providers.sort(Comparator.<SymbolProviderFactoryWrapper> comparingInt(o -> o.priority).reversed());
            INSTANCE = new SymbolProviderManager(providers);
        }
        return INSTANCE;
    }

    /**
     * The private constructor of this manager
     */
    private SymbolProviderManager(@NonNull List<@NonNull SymbolProviderFactoryWrapper> providers) {
        fProviders = ImmutableList.copyOf(providers);
    }

    /**
     * Locate an {@link ISymbolProvider} capable to resolve symbols from the
     * given trace. If no such provider is defined an instance of
     * {@link DefaultSymbolProvider} will be returned
     *
     * @param trace
     *            The trace to create a provider for
     * @return a valid {@link ISymbolProvider}, never null
     */
    public @NonNull ISymbolProvider getSymbolProvider(@NonNull ITmfTrace trace) {
        // Check to see if we already have a provider for this trace
        synchronized (fInstances) {
            WeakReference<ISymbolProvider> reference = fInstances.get(trace);
            if (reference != null) {
                ISymbolProvider provider = reference.get();
                if (provider != null) {
                    return provider;
                }
            }
            // we don't have yet an instance, build one
            for (SymbolProviderFactoryWrapper wrapper : fProviders) {
                ISymbolProviderFactory factory = wrapper.factory;
                ISymbolProvider provider = factory.createProvider(trace);
                if (provider != null) {
                    fInstances.put(trace, new WeakReference<>(provider));
                    return provider;
                }
            }
        }
        // No provider found, return the default one
        return new DefaultSymbolProvider(trace);
    }

}
