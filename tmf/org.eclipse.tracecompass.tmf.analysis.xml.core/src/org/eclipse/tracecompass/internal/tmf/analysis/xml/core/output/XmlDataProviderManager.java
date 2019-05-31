/**********************************************************************
 * Copyright (c) 2017, 2018 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 **********************************************************************/

package org.eclipse.tracecompass.internal.tmf.analysis.xml.core.output;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.AnalysisCompilationData;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlTimeGraphViewCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.fsm.compile.TmfXmlXYViewCu;
import org.eclipse.tracecompass.internal.tmf.analysis.xml.core.module.XmlXYDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.timegraph.TmfTimeGraphCompositeDataProvider;
import org.eclipse.tracecompass.internal.tmf.core.model.xy.TmfTreeXYCompositeDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.ITimeGraphDataProvider;
import org.eclipse.tracecompass.tmf.core.model.timegraph.TimeGraphEntryModel;
import org.eclipse.tracecompass.tmf.core.model.tree.ITmfTreeDataModel;
import org.eclipse.tracecompass.tmf.core.model.xy.ITmfTreeXYDataProvider;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalHandler;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.w3c.dom.Element;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Iterables;
import com.google.common.collect.Table;

/**
 * Class to manage instances of XML data providers which cannot be handled by
 * extension points as there are possibly several instances of XML providers per
 * trace.
 *
 * @author Loic Prieur-Drevon
 */
public class XmlDataProviderManager {

    private static @Nullable XmlDataProviderManager INSTANCE;

    private static final String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
    private final Table<ITmfTrace, String, ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel>> fXyProviders = HashBasedTable.create();
    private final Table<ITmfTrace, String, ITimeGraphDataProvider<@NonNull TimeGraphEntryModel>> fTimeGraphProviders = HashBasedTable.create();
    private final Map<String, @Nullable DataDrivenTimeGraphProviderFactory> fTimeGraphFactories = new HashMap<>();
    private final Map<String, @Nullable DataDrivenXYProviderFactory> fXYFactories = new HashMap<>();

    /**
     * Get the instance of the manager
     *
     * @return the singleton instance
     */
    public static synchronized XmlDataProviderManager getInstance() {
        XmlDataProviderManager instance = INSTANCE;
        if (instance == null) {
            instance = new XmlDataProviderManager();
            INSTANCE = instance;
        }
        return instance;
    }

    /**
     * Dispose the singleton instance if it exists
     */
    public static synchronized void dispose() {
        XmlDataProviderManager manager = INSTANCE;
        if (manager != null) {
            TmfSignalManager.deregister(manager);
            manager.fXyProviders.clear();
            manager.fTimeGraphProviders.clear();
        }
        INSTANCE = null;
    }

    /**
     * Private constructor.
     */
    private XmlDataProviderManager() {
        TmfSignalManager.register(this);
    }

    /**
     * Create (if necessary) and get the {@link DataDrivenXYProviderFactory} for
     * the specified trace and viewElement.
     *
     * @param viewElement
     *            the XML XY view element for which we are querying a provider
     * @return the unique instance of an XY provider for the queried parameters
     */
    public synchronized @Nullable DataDrivenXYProviderFactory getXyProviderFactory(Element viewElement) {
        if (!viewElement.hasAttribute(ID_ATTRIBUTE)) {
            return null;
        }
        String viewId = viewElement.getAttribute(ID_ATTRIBUTE);
        // Factory is nullable, so make sure the key exist and return the
        // factory that can be null
        if (fXYFactories.containsKey(viewId)) {
            return fXYFactories.get(viewId);
        }
        // Create with the trace or experiment first
        DataDrivenXYProviderFactory factory = null;
        TmfXmlXYViewCu tgViewCu = TmfXmlXYViewCu.compile(new AnalysisCompilationData(), viewElement);
        if (tgViewCu != null) {
            factory = tgViewCu.generate();
        }
        fXYFactories.put(viewId, factory);
        return factory;
    }

    /**
     * Create (if necessary) and get the {@link ITmfTreeXYDataProvider} for the
     * specified trace and viewElement.
     *
     * @param trace
     *            trace for which we are querying a provider
     * @param viewElement
     *            the XML XY view element for which we are querying a provider
     * @return the unique instance of an XY provider for the queried parameters
     */
    public synchronized @Nullable ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> getXyProvider(ITmfTrace trace, Element viewElement) {
        if (!viewElement.hasAttribute(ID_ATTRIBUTE)) {
            return null;
        }
        String viewId = viewElement.getAttribute(ID_ATTRIBUTE);
        ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> provider = fXyProviders.get(trace, viewId);
        if (provider != null) {
            return provider;
        }
        if (Iterables.any(TmfTraceManager.getInstance().getOpenedTraces(),
                opened -> TmfTraceManager.getTraceSetWithExperiment(opened).contains(trace))) {

            DataDrivenXYProviderFactory xyFactory = getXyProviderFactory(viewElement);
            // Create with the trace or experiment first
            if (xyFactory != null) {
                return createXYProvider(trace, viewId, xyFactory);
            }

        }
        return null;
    }

    /**
     * Get the XY provider with a certain ID for a trace
     *
     * @param trace
     *            The trace to get the provider for
     * @param providerId
     *            The ID of the provider
     * @return The XY data provider
     */
    public synchronized @Nullable ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> getXyProvider(ITmfTrace trace, String providerId) {
        ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> provider = fXyProviders.get(trace, providerId);
        if (provider != null) {
            return provider;
        }
        DataDrivenXYProviderFactory factory = fXYFactories.get(providerId);
        if (factory != null) {
            return createXYProvider(trace, providerId, factory);
        }
        return null;
    }

    private @Nullable ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> createXYProvider(ITmfTrace trace, String providerId, DataDrivenXYProviderFactory xyFactory) {
        ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> provider = xyFactory.create(trace);

        if (provider == null) {
            // Otherwise, see if it's an experiment and create a composite if
            // that's the case
            Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
            if (traces.size() > 1) {
                // Try creating a composite only if there are many traces,
                // otherwise, the previous call to create should have returned
                // the data provider
                provider = generateExperimentProviderXy(traces, providerId);
            }
        }
        if (provider != null) {
            fXyProviders.put(trace, providerId, provider);
        }
        return provider;
    }

    private @Nullable ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> generateExperimentProviderXy(Collection<@NonNull ITmfTrace> traces, String providerId) {
        List<@NonNull ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel>> providers = new ArrayList<>();
        for (ITmfTrace child : traces) {
            ITmfTreeXYDataProvider<@NonNull ITmfTreeDataModel> childProvider = getXyProvider(child, providerId);
            if (childProvider != null) {
                providers.add(childProvider);
            }
        }
        if (providers.isEmpty()) {
            return null;
        } else if (providers.size() == 1) {
            return providers.get(0);
        }
        return new TmfTreeXYCompositeDataProvider<>(providers, XmlXYDataProvider.ID, XmlXYDataProvider.ID);
    }

    /**
     * Create (if necessary) and get the
     * {@link DataDrivenTimeGraphProviderFactory} from the viewElement.
     *
     * @param viewElement
     *            the XML time graph view element for which we are querying a
     *            provider
     * @return the unique instance of a time graph provider for the queried
     *         parameters
     */
    public synchronized @Nullable DataDrivenTimeGraphProviderFactory getTimeGraphProviderFactory(Element viewElement) {
        if (!viewElement.hasAttribute(ID_ATTRIBUTE)) {
            return null;
        }
        String viewId = viewElement.getAttribute(ID_ATTRIBUTE);
        // Factory is nullable, so make sure the key exist and return the
        // factory that can be null
        if (fTimeGraphFactories.containsKey(viewId)) {
            return fTimeGraphFactories.get(viewId);
        }
        // Create with the trace or experiment first
        DataDrivenTimeGraphProviderFactory factory = null;
        TmfXmlTimeGraphViewCu tgViewCu = TmfXmlTimeGraphViewCu.compile(new AnalysisCompilationData(), viewElement);
        if (tgViewCu != null) {
            factory = tgViewCu.generate();
        }
        fTimeGraphFactories.put(viewId, factory);
        return factory;
    }

    /**
     * Create (if necessary) and get the time graph data provider for the
     * specified trace and viewElement.
     *
     * @param trace
     *            trace for which we are querying a provider
     * @param viewElement
     *            the XML time graph view element for which we are querying a
     *            provider
     * @return the unique instance of a time graph provider for the queried
     *         parameters
     */
    public synchronized @Nullable ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> getTimeGraphProvider(ITmfTrace trace, Element viewElement) {
        if (!viewElement.hasAttribute(ID_ATTRIBUTE)) {
            return null;
        }
        String viewId = viewElement.getAttribute(ID_ATTRIBUTE);
        ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> provider = fTimeGraphProviders.get(trace, viewId);
        if (provider != null) {
            return provider;
        }

        if (Iterables.any(TmfTraceManager.getInstance().getOpenedTraces(),
                opened -> TmfTraceManager.getTraceSetWithExperiment(opened).contains(trace))) {

            // Create with the trace or experiment first
            TmfXmlTimeGraphViewCu tgViewCu = TmfXmlTimeGraphViewCu.compile(new AnalysisCompilationData(), viewElement);
            if (tgViewCu != null) {
                DataDrivenTimeGraphProviderFactory timeGraphFactory = tgViewCu.generate();
                provider = timeGraphFactory.create(trace);

                if (provider == null) {
                    // Otherwise, see if it's an experiment and create a composite if that's the
                    // case
                    Collection<ITmfTrace> traces = TmfTraceManager.getTraceSet(trace);
                    if (traces.size() > 1) {
                        // Try creating a composite only if there are many traces, otherwise, the
                        // previous call to create should have returned the data provider
                        provider = generateExperimentProviderTimeGraph(traces, viewElement);
                    }
                }
                if (provider != null) {
                    fTimeGraphProviders.put(trace, viewId, provider);
                }
                return provider;
            }

        }
        return null;
    }

    private @Nullable ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> generateExperimentProviderTimeGraph(Collection<@NonNull ITmfTrace> traces, Element viewElement) {
        List<@NonNull ITimeGraphDataProvider<@NonNull TimeGraphEntryModel>> providers = new ArrayList<>();
        for (ITmfTrace child : traces) {
            ITimeGraphDataProvider<@NonNull TimeGraphEntryModel> childProvider = getTimeGraphProvider(child, viewElement);
            if (childProvider != null) {
                providers.add(childProvider);
            }
        }
        if (providers.isEmpty()) {
            return null;
        } else if (providers.size() == 1) {
            return providers.get(0);
        }
        return new TmfTimeGraphCompositeDataProvider<>(providers, DataDrivenTimeGraphDataProvider.ID);
    }

    /**
     * Signal handler for the traceClosed signal.
     *
     * @param signal
     *            The incoming signal
     */
    @TmfSignalHandler
    public synchronized void traceClosed(final TmfTraceClosedSignal signal) {
        for (ITmfTrace trace : TmfTraceManager.getTraceSetWithExperiment(signal.getTrace())) {
            fXyProviders.row(trace).clear();
            fTimeGraphProviders.row(trace).clear();
        }
    }
}
