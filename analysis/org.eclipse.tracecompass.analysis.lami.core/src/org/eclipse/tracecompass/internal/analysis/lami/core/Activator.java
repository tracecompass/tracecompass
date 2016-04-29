/*******************************************************************************
 * Copyright (c) 2015, 2016 EfficiOS Inc. and others
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.lami.core;

import java.nio.file.Path;
import java.util.List;

import org.eclipse.tracecompass.common.core.TraceCompassActivator;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.LamiConfigUtils;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysis;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisFactoryException;
import org.eclipse.tracecompass.internal.provisional.analysis.lami.core.module.LamiAnalysisFactoryFromConfigFile;
import org.eclipse.tracecompass.tmf.core.analysis.ondemand.OnDemandAnalysisManager;

/**
 * Plugin activator
 */
public class Activator extends TraceCompassActivator {

    private static final String PLUGIN_ID = "org.eclipse.tracecompass.analysis.lami.core"; //$NON-NLS-1$

    /**
     * Return the singleton instance of this activator.
     *
     * @return The singleton instance
     */
    public static Activator instance() {
        return (Activator) TraceCompassActivator.getInstance(PLUGIN_ID);
    }

    /**
     * Constructor
     */
    public Activator() {
        super(PLUGIN_ID);
    }

    private void loadUserDefinedAnalyses() {
        final Path configDirPath = LamiConfigUtils.getConfigDirPath();

        try {
            final List<LamiAnalysis> analyses = LamiAnalysisFactoryFromConfigFile.buildFromConfigDir(configDirPath, true, trace -> true);

            OnDemandAnalysisManager manager = OnDemandAnalysisManager.getInstance();
            analyses.forEach(manager::registerAnalysis);

        } catch (LamiAnalysisFactoryException e) {
            logWarning("Cannot load user-defined external analyses", e); //$NON-NLS-1$
        }
    }

    @Override
    protected void startActions() {
        loadUserDefinedAnalyses();
    }

    @Override
    protected void stopActions() {
    }

}
