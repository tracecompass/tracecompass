/*******************************************************************************
 * Copyright (c) 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.dataprovider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.internal.provisional.tmf.core.model.tree.ITmfTreeDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
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
    public synchronized @Nullable <T extends ITmfTreeDataProvider<? extends ITmfTreeDataModel>> T getDataProvider(@NonNull ITmfTrace trace, String id, Class<T> dataProviderClass) {
        for (ITmfTreeDataProvider<? extends ITmfTreeDataModel> dataProvider : fInstances.get(trace)) {
            if (id.equals(dataProvider.getId()) && dataProviderClass.isAssignableFrom(dataProvider.getClass())) {
                return dataProviderClass.cast(dataProvider);
            }
        }
        for (ITmfTrace opened : TmfTraceManager.getInstance().getOpenedTraces()) {
            if (TmfTraceManager.getTraceSetWithExperiment(opened).contains(trace)) {
                /* if this trace or an experiment containing this trace is opened */
                IDataProviderFactory providerFactory = fDataProviderFactories.get(id);
                if (providerFactory != null) {
                    ITmfTreeDataProvider<? extends ITmfTreeDataModel> dataProvider = providerFactory.createProvider(trace);
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
     * @since 3.2
     */
    @TmfSignalHandler
    public synchronized void traceClosed(final TmfTraceClosedSignal signal) {
        for (ITmfTrace trace : TmfTraceManager.getTraceSetWithExperiment(signal.getTrace())) {
            fInstances.removeAll(trace);
        }
    }
}
