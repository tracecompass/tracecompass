/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.dataprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Manager for org.eclipse.tracecompass.tmf.core.dataprovider extension point.
 *
 * @author Simon Delisle
 * @since 3.2
 */
public class DataProviderManager {

    /**
     * The singleton instance of this manager
     */
    private static @Nullable DataProviderManager INSTANCE;

    private static final String EXTENSION_POINT_ID = "org.eclipse.tracecompass.tmf.core.dataprovider"; //$NON-NLS-1$
    private static final String ELEMENT_NAME_PROVIDER = "dataProviderFactory"; //$NON-NLS-1$
    private static final String ATTR_CLASS = "class"; //$NON-NLS-1$

    private List<IDataProviderFactory> fDataProviderFactories = new ArrayList<>();

    private final Multimap<ITmfTrace, ITmfTreeDataProvider<? extends ITmfTreeDataModel>> fInstances = LinkedHashMultimap.create();

    /**
     * Get the instance of the manager
     *
     * @return the singleton instance
     */
    public synchronized static DataProviderManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new DataProviderManager();
        }
        return INSTANCE;
    }

    /**
     * Private constructor.
     */
    private DataProviderManager() {
        loadDataProviders();
    }

    /**
     * Load data provider factories from the registry
     */
    private void loadDataProviders() {
        IConfigurationElement[] configElements = Platform.getExtensionRegistry().getConfigurationElementsFor(EXTENSION_POINT_ID);
        for (IConfigurationElement cElement : configElements) {
            if (cElement != null && cElement.getName().equals(ELEMENT_NAME_PROVIDER)) {
                try {
                    Object extension = cElement.createExecutableExtension(ATTR_CLASS);
                    fDataProviderFactories.add((IDataProviderFactory) extension);
                } catch (CoreException e) {
                    Activator.logError("Unable to load extensions", e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Get the data provider for the given trace
     *
     * @param trace
     *            The trace
     * @param id
     *            Id of the data provider
     * @param dataProviderClass
     *            Returned data provider must extend this class
     * @return Data provider
     */
    public @Nullable <T extends ITmfTreeDataProvider<? extends ITmfTreeDataModel>> T getDataProvider(@NonNull ITmfTrace trace, String id, Class<T> dataProviderClass) {
        Collection<T> dataProvider = getDataProvider(trace, dataProviderClass);
        for (T provider : dataProvider) {
            if (provider != null && id.equals(provider.getId())) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Get a collection of data providers for the given trace
     *
     * @param trace
     *            The trace
     * @param dataProviderClass
     *            Returned data provider must extend this class
     * @return Collection of data provider
     */
    public synchronized <T> Collection<T> getDataProvider(@NonNull ITmfTrace trace, Class<T> dataProviderClass) {
        Collection<T> dataProviders = new ArrayList<>();
        for (ITmfTreeDataProvider<? extends ITmfTreeDataModel> dataProvider : fInstances.get(trace)) {
            if (dataProvider != null && dataProviderClass.isAssignableFrom(dataProvider.getClass())) {
                dataProviders.add(dataProviderClass.cast(dataProvider));
            }
        }

        if (dataProviders.isEmpty()) {
            for (IDataProviderFactory providerFactory : fDataProviderFactories) {
                ITmfTreeDataProvider<? extends ITmfTreeDataModel> provider = providerFactory.createProvider(trace);
                if (provider != null) {
                    fInstances.put(trace, provider);
                    if (dataProviderClass.isAssignableFrom(provider.getClass())) {
                        dataProviders.add(dataProviderClass.cast(provider));
                    }
                }
            }
        }

        return dataProviders;
    }
}
