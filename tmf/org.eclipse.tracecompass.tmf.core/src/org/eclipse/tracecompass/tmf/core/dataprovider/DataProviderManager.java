/*******************************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.dataprovider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.component.DataProviderConstants;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;

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
    private static final String ATTR_ID = "id"; //$NON-NLS-1$

    private Map<String, IDataProviderFactory> fDataProviderFactories = new HashMap<>();

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
     * Dispose the singleton instance if it exists
     *
     * @since 3.3
     */
    public static synchronized void dispose() {
        DataProviderManager manager = INSTANCE;
        if (manager != null) {
            TmfSignalManager.deregister(manager);
            for (IDataProviderFactory factory : manager.fDataProviderFactories.values()) {
                TmfSignalManager.deregister(factory);
            }
            manager.fDataProviderFactories.clear();
            manager.fInstances.clear();
        }
        INSTANCE = null;
    }

    /**
     * Private constructor.
     */
    private DataProviderManager() {
        loadDataProviders();
        TmfSignalManager.register(this);
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
                    fDataProviderFactories.put(cElement.getAttribute(ATTR_ID), (IDataProviderFactory) extension);
                } catch (CoreException e) {
                    Activator.logError("Unable to load extensions", e); //$NON-NLS-1$
                }
            }
        }
    }

    /**
     * Get the data provider for the given trace.
     * <p>
     * This method should never be called from within a {@link TmfSignalHandler}.
     *
     * @param trace
     *            The trace
     * @param id
     *            Id of the data provider. This ID can be the concatenation of a
     *            provider ID + ':' + a secondary ID used to differentiate multiple
     *            instances of a same provider.
     * @param dataProviderClass
     *            Returned data provider must extend this class
     * @return Data provider
     * @since 4.0
     */
    public synchronized @Nullable <T extends ITmfTreeDataProvider<? extends ITmfTreeDataModel>> T getDataProvider(@NonNull ITmfTrace trace, String id, Class<T> dataProviderClass) {
        for (ITmfTreeDataProvider<? extends ITmfTreeDataModel> dataProvider : fInstances.get(trace)) {
            if (id.equals(dataProvider.getId()) && dataProviderClass.isAssignableFrom(dataProvider.getClass())) {
                return dataProviderClass.cast(dataProvider);
            }
        }
        String[] ids = id.split(DataProviderConstants.ID_SEPARATOR);
        for (ITmfTrace opened : TmfTraceManager.getInstance().getOpenedTraces()) {
            if (TmfTraceManager.getTraceSetWithExperiment(opened).contains(trace)) {
                /* if this trace or an experiment containing this trace is opened */
                IDataProviderFactory providerFactory = fDataProviderFactories.get(ids[0]);
                if (providerFactory != null) {
                    ITmfTreeDataProvider<? extends ITmfTreeDataModel> dataProvider = ids.length > 1 ? providerFactory.createProvider(trace, String.valueOf(ids[1])) : providerFactory.createProvider(trace);
                    if (dataProvider != null && id.equals(dataProvider.getId()) && dataProviderClass.isAssignableFrom(dataProvider.getClass())) {
                        fInstances.put(trace, dataProvider);
                        return dataProviderClass.cast(dataProvider);
                    }
                }
                return null;
            }
        }
        return null;
    }

    /**
     * Signal handler for the traceClosed signal.
     *
     * @param signal
     *            The incoming signal
     * @since 3.3
     */
    @TmfSignalHandler
    public void traceClosed(final TmfTraceClosedSignal signal) {
        new Thread(() -> {
            synchronized (DataProviderManager.this) {
                for (ITmfTrace trace : TmfTraceManager.getTraceSetWithExperiment(signal.getTrace())) {
                    fInstances.removeAll(trace).forEach(ITmfTreeDataProvider::dispose);
                }
            }
        }).start();
    }

    /**
     * Get the list of available providers for this trace / experiment without
     * triggering the analysis or creating the provider
     *
     * @param trace
     *            queried trace
     * @return list of the available providers for this trace / experiment
     * @since 5.0
     */
    public List<IDataProviderDescriptor> getAvailableProviders(@Nullable ITmfTrace trace) {
        if (trace == null) {
            return Collections.emptyList();
        }
        List<IDataProviderDescriptor> list = new ArrayList<>();
        for (IDataProviderFactory factory : fDataProviderFactories.values()) {
            Collection<IDataProviderDescriptor> descriptors = factory.getDescriptors(trace);
            if (!descriptors.isEmpty()) {
                list.addAll(descriptors);
            }
        }
        return list;
    }

    /**
     * Remove a data provider from the instances. This method will not dispose
     * of the data provider. It is the responsibility of the caller to dispose
     * of it if needed.
     *
     * @param <T>
     *            The type of data provider
     * @param trace
     *            The trace for which to remove the data provider
     * @param provider
     *            The data provider to remove
     * @return Whether the data provider was removed. The result would be
     *         <code>false</code> if the data provider was not present in the
     *         list.
     * @since 5.1
     */
    public <T extends ITmfTreeDataProvider<? extends ITmfTreeDataModel>> boolean removeDataProvider(ITmfTrace trace, T provider) {
        return fInstances.remove(trace, provider);
    }
}
