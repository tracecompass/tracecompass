/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.ui.criticalpath.view;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.building.TmfGraphBuilderModule;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.ITmfNewAnalysisModuleListener;
import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * @author Geneviève Bastien
 */
public class CriticalPathModuleListener implements ITmfNewAnalysisModuleListener {

    /**
     * Constructor
     */
    public CriticalPathModuleListener() {
        // Do nothing
    }

    @Override
    public void moduleCreated(@Nullable IAnalysisModule module) {
        if (module instanceof TmfGraphBuilderModule) {
            module.registerOutput(new TmfAnalysisViewOutput(CriticalPathView.ID, module.getId()));
        }
    }

}
