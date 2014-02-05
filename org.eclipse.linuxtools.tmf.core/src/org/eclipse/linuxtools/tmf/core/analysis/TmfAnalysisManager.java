/*******************************************************************************
 * Copyright (c) 2013 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.analysis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.internal.tmf.core.Activator;
import org.eclipse.linuxtools.internal.tmf.core.analysis.TmfAnalysisModuleSources;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Manages the available analysis helpers from different sources and their
 * parameter providers.
 *
 * TODO: Add the concept of analysis source. Now only a plugin's extension point
 * is implemented
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfAnalysisManager {

    private static final Map<String, IAnalysisModuleHelper> fAnalysisModules = new HashMap<>();
    private static final Map<String, List<Class<? extends IAnalysisParameterProvider>>> fParameterProviders = new HashMap<>();
    private static final Map<Class<? extends IAnalysisParameterProvider>, IAnalysisParameterProvider> fParamProviderInstances = new HashMap<>();
    private static final List<IAnalysisModuleSource> fSources = new ArrayList<>();
    private static final List<ITmfNewAnalysisModuleListener> fListeners = new ArrayList<>();

    /**
     * Registers a new source of modules
     *
     * @param source
     *            A {@link IAnalysisModuleSource} instance
     */
    public static void registerModuleSource(IAnalysisModuleSource source) {
        synchronized (fSources) {
            fSources.add(source);
            refreshModules();
        }
    }

    /**
     * Initializes sources and new module listeners from the extension point
     */
    public static void initialize() {
        initializeModuleSources();
        initializeNewModuleListeners();
    }

    /**
     * Cleans the module sources list and initialize it from the extension point
     */
    private static void initializeModuleSources() {
        synchronized (fSources) {
            fSources.clear();
            for (IAnalysisModuleSource source : TmfAnalysisModuleSources.getSources()) {
                fSources.add(source);
            }
        }
    }

    /**
     * Cleans the new module listeners list and initialize it from the extension
     * point
     */
    private static void initializeNewModuleListeners() {
        synchronized (fListeners) {
            fListeners.clear();
            for (ITmfNewAnalysisModuleListener output : TmfAnalysisModuleOutputs.getOutputListeners()) {
                fListeners.add(output);
            }
        }
    }

    /**
     * Gets all available analysis module helpers
     *
     * This map is read-only
     *
     * @return The map of available {@link IAnalysisModuleHelper}
     */
    public static Map<String, IAnalysisModuleHelper> getAnalysisModules() {
        synchronized (fAnalysisModules) {
            if (fAnalysisModules.isEmpty()) {
                for (IAnalysisModuleSource source : fSources) {
                    for (IAnalysisModuleHelper helper : source.getAnalysisModules()) {
                        fAnalysisModules.put(helper.getId(), helper);
                    }
                }
            }
        }
        return Collections.unmodifiableMap(fAnalysisModules);
    }

    /**
     * Gets all analysis module helpers that apply to a given trace type
     *
     * This map is read-only
     *
     * @param traceclass
     *            The trace class to get modules for
     * @return The map of available {@link IAnalysisModuleHelper}
     */
    public static Map<String, IAnalysisModuleHelper> getAnalysisModules(Class<? extends ITmfTrace> traceclass) {
        Map<String, IAnalysisModuleHelper> allModules = getAnalysisModules();
        Map<String, IAnalysisModuleHelper> map = new HashMap<>();
        for (IAnalysisModuleHelper module : allModules.values()) {
            if (module.appliesToTraceType(traceclass)) {
                map.put(module.getId(), module);
            }
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Gets an analysis module helper identified by an id
     *
     * @param id
     *            Id of the analysis module to get
     * @return The {@link IAnalysisModuleHelper}
     */
    public static IAnalysisModuleHelper getAnalysisModule(String id) {
        Map<String, IAnalysisModuleHelper> map = getAnalysisModules();
        return map.get(id);
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
            fParameterProviders.get(analysisId).add(paramProvider);
        }
    }

    /**
     * Get a parameter provider that applies to the requested trace
     *
     * @param module
     *            Analysis module
     * @param trace
     *            The trace
     * @return A parameter provider if one applies to the trace, null otherwise
     */
    public static List<IAnalysisParameterProvider> getParameterProviders(IAnalysisModule module, ITmfTrace trace) {
        List<IAnalysisParameterProvider> providerList = new ArrayList<>();
        synchronized (fParameterProviders) {
            if (!fParameterProviders.containsKey(module.getId())) {
                return providerList;
            }
            for (Class<? extends IAnalysisParameterProvider> providerClass : fParameterProviders.get(module.getId())) {
                try {
                    IAnalysisParameterProvider provider = fParamProviderInstances.get(providerClass);
                    if (provider == null) {
                        provider = providerClass.newInstance();
                        fParamProviderInstances.put(providerClass, provider);
                    }
                    if (provider != null) {
                        if (provider.appliesToTrace(trace)) {
                            providerList.add(provider);
                        }
                    }
                } catch (IllegalArgumentException e) {
                    Activator.logError(Messages.TmfAnalysisManager_ErrorParameterProvider, e);
                } catch (SecurityException e) {
                    Activator.logError(Messages.TmfAnalysisManager_ErrorParameterProvider, e);
                } catch (InstantiationException e) {
                    Activator.logError(Messages.TmfAnalysisManager_ErrorParameterProvider, e);
                } catch (IllegalAccessException e) {
                    Activator.logError(Messages.TmfAnalysisManager_ErrorParameterProvider, e);
                }
            }
        }
        return providerList;
    }

    /**
     * Clear the list of modules so that next time, it is computed again from
     * sources
     */
    public static void refreshModules() {
        synchronized (fAnalysisModules) {
            fAnalysisModules.clear();
        }
    }

    /**
     * This method should be called when new analysis modules have been created
     * by module helpers to that the {@link ITmfNewAnalysisModuleListener} can
     * be executed on the module instance.
     *
     * @param module
     *            The newly created analysis module
     */
    public static void analysisModuleCreated(IAnalysisModule module) {
        synchronized (fListeners) {
            for (ITmfNewAnalysisModuleListener listener : fListeners) {
                listener.moduleCreated(module);
            }
        }
    }

}
