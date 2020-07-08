/*******************************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.tmf.core.Activator;
import org.eclipse.tracecompass.internal.tmf.core.analysis.TmfAnalysisModuleSourceConfigElement;

/**
 * Manager for outputs of an analysis
 *
 * @author Matthew Khouzam
 * @since 6.1
 */
public final class TmfAnalysisOutputManager {
    private static @Nullable TmfAnalysisOutputManager fManager = null;

    private final Map<String, Map<String, Set<String>>> fExclusionList = new HashMap<>();

    /**
     * Get the instance
     *
     * @return the instance
     */
    public static TmfAnalysisOutputManager getInstance() {
        TmfAnalysisOutputManager manager = fManager;
        if (manager == null) {
            manager = new TmfAnalysisOutputManager();
            fManager = manager;
        }
        return manager;
    }

    /**
     * Load an exclusion from a
     * {@link TmfAnalysisModuleSourceConfigElement#HIDE_OUTPUT_ELEM}
     * configuration element
     *
     * @param ce
     *            the configuration element
     */
    public void loadExclusion(IConfigurationElement ce) {
        String moduleId = ce.getAttribute(TmfAnalysisModuleSourceConfigElement.ANALYSIS_MODULE_ATTR);
        String parentPluginId = ((IExtension) ce.getParent()).getNamespaceIdentifier();
        if (moduleId == null) {
            Activator.logWarning(String.format("plugin: %s loadExclusion issue: Analysis module ID not present", parentPluginId)); //$NON-NLS-1$
            return;
        }
        String ttid = ce.getAttribute(TmfAnalysisModuleSourceConfigElement.TRACETYPE_ELEM);
        if (ttid == null) {
            Activator.logWarning(String.format("plugin: %s loadExclusion issue: Analysis tracetype not present", parentPluginId)); //$NON-NLS-1$
            return;
        }
        String output = ce.getAttribute(TmfAnalysisModuleSourceConfigElement.OUTPUT_ATTR);
        if (output == null) {
            Activator.logWarning(String.format("plugin: %s loadExclusion issue: output ID not present", parentPluginId)); //$NON-NLS-1$
            return;
        }
        Map<String, Set<String>> map = fExclusionList.computeIfAbsent(ttid, (String s) -> new HashMap<>());
        Set<String> set = map.computeIfAbsent(moduleId, (s) -> new HashSet<>());
        Activator.log(new Status(IStatus.INFO, Activator.PLUGIN_ID, String.format("Plugin: %s is hiding (%s, %s, %s).", parentPluginId, ttid, moduleId, output))); //$NON-NLS-1$
        set.add(output);
    }

    /**
     * Is the output to be shown?
     *
     * @param tracetype
     *            the tracetype
     * @param analysisId
     *            the analysis id
     * @param outputId
     *            the output id
     * @return true if it should be hidden, false otherwise
     */
    public boolean isHidden(String tracetype, String analysisId, String outputId) {
        Map<String, Set<String>> map = fExclusionList.get(tracetype);
        if (map == null) {
            return false;
        }
        Set<String> set = map.get(analysisId);
        return set != null && set.contains(outputId);
    }
}
