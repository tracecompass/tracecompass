/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.analysis.TmfAnalysisModuleSources;
import org.eclipse.tracecompass.internal.tmf.core.analysis.TmfAnalysisParameterProviders;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;

/**
 * Manages the available analysis helpers from different sources and their
 * parameter providers.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class TmfAnalysisManager {

    private static final Multimap<String, IAnalysisModuleHelper> fAnalysisModules = NonNullUtils.checkNotNull(HashMultimap.create());
    private static final Map<String, List<Class<? extends IAnalysisParameterProvider>>> fParameterProviders = new HashMap<>();
    private static final Map<Class<? extends IAnalysisParameterProvider>, IAnalysisParameterProvider> fParamProviderInstances = new HashMap<>();
    private static final List<IAnalysisModuleSource> fSources = new ArrayList<>();
    private static final List<ITmfNewAnalysisModuleListener> fListeners = new ArrayList<>();

    /**
     * Constructor, not to be used
     */
    private TmfAnalysisManager() {

    }

    /**
     * Registers a new source of modules
     *
     * @param source
     *            A {@link IAnalysisModuleSource} instance
     */
    public static synchronized void registerModuleSource(IAnalysisModuleSource source) {
        fSources.add(source);
        refreshModules();
    }

    /**
     * Initializes sources and new module listeners from the extension point
     */
    public static synchronized void initialize() {
        fSources.clear();
        fListeners.clear();
        initializeModuleSources();
        initializeNewModuleListeners();
    }

    /**
     * Cleans the module sources list and initialize it from the extension point
     */
    private static synchronized void initializeModuleSources() {
        for (IAnalysisModuleSource source : TmfAnalysisModuleSources.getSources()) {
            fSources.add(source);
        }
    }

    /**
     * Cleans the new module listeners list and initialize it from the extension
     * point
     */
    private static synchronized void initializeNewModuleListeners() {
        for (ITmfNewAnalysisModuleListener output : TmfAnalysisModuleOutputs.getOutputListeners()) {
            fListeners.add(output);
        }
    }

    /**
     * Add a new module listener to the list of listeners
     *
     * @param listener
     *            The new module listener
     */
    public static synchronized void addNewModuleListener(ITmfNewAnalysisModuleListener listener) {
        fListeners.add(listener);
    }

    /**
     * Gets all available analysis module helpers
     *
     * This map is read-only
     *
     * @return The map of available {@link IAnalysisModuleHelper}
     * @since 1.0
     */
    public static synchronized Multimap<String, IAnalysisModuleHelper> getAnalysisModules() {
        if (fAnalysisModules.isEmpty()) {
            for (IAnalysisModuleSource source : fSources) {
                for (IAnalysisModuleHelper helper : source.getAnalysisModules()) {
                    fAnalysisModules.put(helper.getId(), helper);
                }
            }
        }
        return checkNotNull(ImmutableMultimap.copyOf(fAnalysisModules));
    }

    /**
     * Gets all analysis module helpers that apply to a given trace type. For
     * each analysis ID, only one helper will be returned if more than one
     * applies.
     *
     * This map is read-only
     *
     * TODO: This method is only used to populate the project view in the UI. It
     * should be deprecated eventually, after some UI rework, so that the trace
     * type does not drive whether the analysis module applies or not to a
     * trace, but rather the content of the trace or experiment (once it is
     * opened)
     *
     * @param traceclass
     *            The trace class to get modules for
     * @return The map of available {@link IAnalysisModuleHelper}
     */
    public static Map<String, IAnalysisModuleHelper> getAnalysisModules(Class<? extends ITmfTrace> traceclass) {
        Multimap<String, IAnalysisModuleHelper> allModules = getAnalysisModules();
        Map<String, IAnalysisModuleHelper> map = new HashMap<>();
        for (IAnalysisModuleHelper module : allModules.values()) {
            if (module.appliesToTraceType(traceclass)) {
                map.put(module.getId(), module);
            }
        }
        return ImmutableMap.copyOf(map);
    }

    /**
     * Register a new parameter provider for an analysis
     *
     * @param analysisId
     *            The id of the analysis
     * @param paramProvider
     *            The class of the parameter provider
     */
    public static void registerParameterProvider(String analysisId, Class<? extends IAnalysisParameterProvider> paramProvider) {
        synchronized (fParameterProviders) {
            if (!fParameterProviders.containsKey(analysisId)) {
                fParameterProviders.put(analysisId, new ArrayList<Class<? extends IAnalysisParameterProvider>>());
            }
            /* We checked via containsKey() above, get() should not return null */
            checkNotNull(fParameterProviders.get(analysisId)).add(paramProvider);
        }
    }

    /**
     * Get the parameter providers that apply to the requested trace
     *
     * @param module
     *            Analysis module
     * @param trace
     *            The trace
     * @return The set of parameter providers that apply to a trace for this module
     * @deprecated Use the
     *             {@link #getParameterProvidersForModule(IAnalysisModule, ITmfTrace)}
     *             method that returns a set instead.
     */
    @Deprecated
    public static List<IAnalysisParameterProvider> getParameterProviders(IAnalysisModule module, ITmfTrace trace) {
        /* Call the method that returns a set */
        Set<IAnalysisParameterProvider> providerList = getParameterProvidersForModule(module, trace);
        return new ArrayList<>(providerList);
    }

    /**
     * Get the parameter providers that apply to the requested trace
     *
     * @param module
     *            Analysis module
     * @param trace
     *            The trace
     * @return The set of parameter providers that apply to a trace for this module
     * @since 2.0
     */
    public static Set<IAnalysisParameterProvider> getParameterProvidersForModule(IAnalysisModule module, ITmfTrace trace) {
        /* First, get the parameter providers from the extension point */
        Set<IAnalysisParameterProvider> providerSet = TmfAnalysisParameterProviders.getParameterProvidersFor(module.getId());
        /* Then add any new parameter provider coming from other sources */
        synchronized (fParameterProviders) {
            if (!fParameterProviders.containsKey(module.getId())) {
                return providerSet;
            }
            /* We checked  via containsKey, get() should not return null */
            List<Class<? extends IAnalysisParameterProvider>> parameterProviders = checkNotNull(fParameterProviders.get(module.getId()));
            for (Class<? extends IAnalysisParameterProvider> providerClass : parameterProviders) {
                try {
                    IAnalysisParameterProvider provider = fParamProviderInstances.get(providerClass);
                    if (provider == null) {
                        provider = providerClass.newInstance();
                        fParamProviderInstances.put(providerClass, provider);
                    }
                    if (provider.appliesToTrace(trace)) {
                        providerSet.add(provider);
                    }
                } catch (IllegalArgumentException | SecurityException | InstantiationException | IllegalAccessException e) {
                    Activator.logError(Messages.TmfAnalysisManager_ErrorParameterProvider, e);
                }
            }
        }
        return Collections.unmodifiableSet(providerSet);
    }

    /**
     * Clear the list of modules so that next time, it is computed again from
     * sources
     */
    public static synchronized void refreshModules() {
        fAnalysisModules.clear();
    }

    /**
     * This method should be called when new analysis modules have been created
     * by module helpers to that the {@link ITmfNewAnalysisModuleListener} can
     * be executed on the module instance.
     *
     * @param module
     *            The newly created analysis module
     */
    public static synchronized void analysisModuleCreated(IAnalysisModule module) {
        for (ITmfNewAnalysisModuleListener listener : fListeners) {
            listener.moduleCreated(module);
        }
    }

}
