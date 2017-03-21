/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.tmf.core.analysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisParameterProvider;

/**
 * Utility class for accessing TMF analysis parameter providers extensions from
 * the platform's extensions registry and returning the module parameter
 * providers.
 *
 * @author Geneviève Bastien
 */
public final class TmfAnalysisParameterProviders {

    /** Extension point ID */
    public static final String TMF_ANALYSIS_TYPE_ID = "org.eclipse.linuxtools.tmf.core.analysis"; //$NON-NLS-1$

    /** Extension point element 'module' */
    public static final String PARAMETER_PROVIDER_ELEM = "parameterProvider"; //$NON-NLS-1$

    /** Extension point attribute 'class' */
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    /** Extension point attribute 'id' */
    public static final String ID_ATTR = "id"; //$NON-NLS-1$

    /**
     * Extension point element 'analysisId' to associate the output to a single
     * analysis
     */
    public static final String ANALYSIS_ID_ELEM = "analysisId"; //$NON-NLS-1$

    /* Maps a class name to an instance of a parameter provider */
    private static final Map<String, IAnalysisParameterProvider> fParamProviderInstances = new HashMap<>();

    private TmfAnalysisParameterProviders() {

    }

    /**
     * Disposes the analysis parameter providers
     *
     * @since 2.2
     */
    public static void dispose() {
        fParamProviderInstances.values().forEach(provider -> provider.dispose());
    }

    /**
     * Return the analysis parameter providers advertised in the extension
     * point, and associated with an analysis ID.
     *
     * @param analysisId
     *            Get the parameter providers for an analysis identified by its
     *            ID
     * @return Map of analysis ID mapped to parameter provider classes
     */
    public static Set<IAnalysisParameterProvider> getParameterProvidersFor(String analysisId) {
        Set<IAnalysisParameterProvider> providers = new HashSet<>();
        // Get the parameter provider elements from the extension point
        IExtensionRegistry extensionRegistry = Platform.getExtensionRegistry();
        if (extensionRegistry == null) {
            return Collections.emptySet();
        }
        IConfigurationElement[] config = extensionRegistry.getConfigurationElementsFor(TMF_ANALYSIS_TYPE_ID);
        for (IConfigurationElement ce : config) {
            String elementName = ce.getName();
            if (elementName.equals(PARAMETER_PROVIDER_ELEM)) {
                try {
                    IConfigurationElement[] children = ce.getChildren(ANALYSIS_ID_ELEM);
                    if (children.length == 0) {
                        throw new IllegalStateException();
                    }
                    String id = children[0].getAttribute(ID_ATTR);
                    String className = ce.getAttribute(CLASS_ATTR);
                    if (id == null || className == null) {
                        continue;
                    }
                    if (analysisId.equals(id)) {
                        IAnalysisParameterProvider provider = fParamProviderInstances.get(className);
                        if (provider == null) {
                            provider = checkNotNull((IAnalysisParameterProvider) ce.createExecutableExtension(CLASS_ATTR));
                            fParamProviderInstances.put(className, provider);
                        }
                        providers.add(provider);
                    }
                } catch (InvalidRegistryObjectException | CoreException e) {
                    Activator.logError("Error creating module parameter provider", e); //$NON-NLS-1$
                }
            }
        }
        return providers;
    }

}
