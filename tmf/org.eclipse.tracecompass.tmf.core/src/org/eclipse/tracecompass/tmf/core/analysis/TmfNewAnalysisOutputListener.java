/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.analysis;

import org.eclipse.jdt.annotation.NonNull;

/**
 * This class listens when new analysis modules are created and registers an
 * output if the module corresponds to the output specifications
 *
 * @author Geneviève Bastien
 */
public class TmfNewAnalysisOutputListener implements ITmfNewAnalysisModuleListener {

    private final String fAnalysisId;
    private final Class<? extends IAnalysisModule> fAnalysisModuleClass;
    private final @NonNull IAnalysisOutput fOutput;

    /**
     * Constructor
     *
     * @param output
     *            The analysis output to add if the analysis corresponds to the
     *            ID or class
     * @param analysisId
     *            The analysis ID of the single analysis to match
     * @param moduleClass
     *            The module class this output applies to
     */
    public TmfNewAnalysisOutputListener(@NonNull IAnalysisOutput output, String analysisId, Class<? extends IAnalysisModule> moduleClass) {
        fOutput = output;
        fAnalysisId = analysisId;
        fAnalysisModuleClass = moduleClass;
    }

    @Override
    public void moduleCreated(IAnalysisModule module) {
        if (fAnalysisId != null) {
            if (module.getId().equals(fAnalysisId)) {
                module.registerOutput(fOutput);
            }
        } else if (fAnalysisModuleClass != null) {
            if (fAnalysisModuleClass.isAssignableFrom(module.getClass())) {
                module.registerOutput(fOutput);
            }
        }
    }

}
