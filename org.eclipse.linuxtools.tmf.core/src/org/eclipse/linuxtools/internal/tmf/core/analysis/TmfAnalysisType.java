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

package org.eclipse.linuxtools.internal.tmf.core.analysis;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.Platform;
import org.eclipse.linuxtools.tmf.core.analysis.IAnalysisModuleHelper;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAnalysisModuleHelperCE;

/**
 * Utility class for accessing TMF analysis type extensions from the platform's
 * extensions registry.
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public final class TmfAnalysisType {

    /** Extension point ID */
    public static final String TMF_ANALYSIS_TYPE_ID = "org.eclipse.linuxtools.tmf.core.analysis"; //$NON-NLS-1$

    /** Extension point element 'module' */
    public static final String MODULE_ELEM = "module"; //$NON-NLS-1$

    /** Extension point element 'parameter' */
    public static final String PARAMETER_ELEM = "parameter"; //$NON-NLS-1$

    /** Extension point attribute 'ID' */
    public static final String ID_ATTR = "id"; //$NON-NLS-1$

    /** Extension point attribute 'name' */
    public static final String NAME_ATTR = "name"; //$NON-NLS-1$

    /** Extension point attribute 'analysis_module' */
    public static final String ANALYSIS_MODULE_ATTR = "analysis_module"; //$NON-NLS-1$

    /** Extension point attribute 'automatic' */
    public static final String AUTOMATIC_ATTR = "automatic"; //$NON-NLS-1$

    /** Extension point attribute 'icon' */
    public static final String ICON_ATTR = "icon"; //$NON-NLS-1$

    /** Extension point attribute 'default_value' */
    public static final String DEFAULT_VALUE_ATTR = "default_value"; //$NON-NLS-1$

    /** Extension point element 'tracetype' */
    public static final String TRACETYPE_ELEM = "tracetype"; //$NON-NLS-1$

    /** Extension point attribute 'class' */
    public static final String CLASS_ATTR = "class"; //$NON-NLS-1$

    /** Extension point attribute 'applies' */
    public static final String APPLIES_ATTR = "applies"; //$NON-NLS-1$

    /**
     * The mapping of available trace type IDs to their corresponding
     * configuration element
     */
    private final Map<String, IAnalysisModuleHelper> fAnalysisTypeAttributes = new HashMap<String, IAnalysisModuleHelper>();

    private static TmfAnalysisType fInstance = null;

    /**
     * Retrieves all configuration elements from the platform extension registry
     * for the trace type extension.
     *
     * @return an array of trace type configuration elements
     */
    public static IConfigurationElement[] getTypeElements() {
        IConfigurationElement[] elements =
                Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_ANALYSIS_TYPE_ID);
        List<IConfigurationElement> typeElements = new LinkedList<IConfigurationElement>();
        for (IConfigurationElement element : elements) {
            if (element.getName().equals(MODULE_ELEM)) {
                typeElements.add(element);
            }
        }
        return typeElements.toArray(new IConfigurationElement[typeElements.size()]);
    }

    private TmfAnalysisType() {
        populateAnalysisTypes();
    }

    /**
     * The analysis type instance
     *
     * @return the analysis type instance
     */
    public static synchronized TmfAnalysisType getInstance() {
        if (fInstance == null) {
            fInstance = new TmfAnalysisType();
        }
        return fInstance;
    }

    /**
     * Get the list of analysis modules
     *
     * @return list of analysis modules
     */
    public Map<String, IAnalysisModuleHelper> getAnalysisModules() {
        return fAnalysisTypeAttributes;
    }

    private void populateAnalysisTypes() {
        if (fAnalysisTypeAttributes.isEmpty()) {
            // Populate the Categories and Trace Types
            IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(TMF_ANALYSIS_TYPE_ID);
            for (IConfigurationElement ce : config) {
                String elementName = ce.getName();
                if (elementName.equals(TmfAnalysisType.MODULE_ELEM)) {
                    String analysisTypeId = ce.getAttribute(TmfAnalysisType.ID_ATTR);
                    fAnalysisTypeAttributes.put(analysisTypeId, new TmfAnalysisModuleHelperCE(ce));
                }
            }
        }
    }

}
