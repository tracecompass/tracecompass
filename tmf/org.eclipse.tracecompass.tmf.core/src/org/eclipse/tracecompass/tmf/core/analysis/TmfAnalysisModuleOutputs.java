/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.InvalidRegistryObjectException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.core.Activator;

/**
 * Utility class for accessing TMF analysis module extensions from the
 * platform's extensions registry and returning the modules' outputs, wrapped as
 * new module listeners.
 *
 * @author Geneviève Bastien
 */
public class TmfAnalysisModuleOutputs {

    /** Extension point ID */
    public static final String TMF_ANALYSIS_TYPE_ID = "org.eclipse.linuxtools.tmf.core.analysis"; //$NON-NLS-1$

    /** Extension point element 'output' */
    public static final String OUTPUT_ELEM = "output"; //$NON-NLS-1$

    /** Extension point attribute 'outputClass' */
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    /** Extension point attribute 'id' */
    public static final String ID_ATTR = "id"; //$NON-NLS-1$

    /**
     * Extension point element 'analysisId' to associate the output to a single
     * analysis
     */
    public static final String ANALYSIS_ID_ELEM = "analysisId"; //$NON-NLS-1$

    /**
     * Extension point element 'analysisModuleClass' to associate the output
     * with an analysis module class
     */
    public static final String MODULE_CLASS_ELEM = "analysisModuleClass"; //$NON-NLS-1$

    /** Extension point element 'listener' */
    private static final String LISTENER_ELEM = "listener"; //$NON-NLS-1$

    private TmfAnalysisModuleOutputs() {

    }

    /**
     * Return the analysis module outputs, wrapped as new module listeners,
     * advertised in the extension point, in iterable format.
     *
     * @return List of {@link ITmfNewAnalysisModuleListener}
     */
    public static Iterable<@NonNull ITmfNewAnalysisModuleListener> getOutputListeners() {
        List<@NonNull ITmfNewAnalysisModuleListener> newModuleListeners = new ArrayList<>();
        // Get the sources element from the extension point
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_ANALYSIS_TYPE_ID);
        for (IConfigurationElement ce : config) {
            String elementName = ce.getName();
            ITmfNewAnalysisModuleListener listener = null;
            if (elementName.equals(OUTPUT_ELEM)) {
                listener = getListenerFromOutputElement(ce);
            } else if (elementName.equals(LISTENER_ELEM)) {
                listener = getListenerFromListenerElement(ce);
            }
            if (listener != null) {
                newModuleListeners.add(listener);
            }
        }
        return newModuleListeners;
    }

    private static ITmfNewAnalysisModuleListener getListenerFromOutputElement(IConfigurationElement ce) {
        ITmfNewAnalysisModuleListener listener = null;
        try {
            IAnalysisOutput output = (IAnalysisOutput) ce.createExecutableExtension(CLASS_ATTR);
            if (output == null) {
                Activator.logWarning("An output could not be created"); //$NON-NLS-1$
                return listener;
            }
            for (IConfigurationElement childCe : ce.getChildren()) {
                if (childCe.getName().equals(ANALYSIS_ID_ELEM)) {
                    listener = new TmfNewAnalysisOutputListener(output, childCe.getAttribute(ID_ATTR), null);
                } else if (childCe.getName().equals(MODULE_CLASS_ELEM)) {
                    String contributorName = childCe.getContributor().getName();
                    Class<?> moduleClass = Platform.getBundle(contributorName).loadClass(childCe.getAttribute(CLASS_ATTR));
                    listener = new TmfNewAnalysisOutputListener(output, null, moduleClass.asSubclass(IAnalysisModule.class));
                }
            }
        } catch (InvalidRegistryObjectException | CoreException | ClassNotFoundException e) {
            Activator.logError("Error creating module output listener", e); //$NON-NLS-1$
        }
        return listener;
    }

    private static ITmfNewAnalysisModuleListener getListenerFromListenerElement(IConfigurationElement ce) {
        ITmfNewAnalysisModuleListener listener = null;
        try {
            listener = (ITmfNewAnalysisModuleListener) ce.createExecutableExtension(CLASS_ATTR);
        } catch (CoreException e) {
            Activator.logError("Error creating new module listener", e); //$NON-NLS-1$
        }
        return listener;
    }

}
