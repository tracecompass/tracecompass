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

package org.eclipse.linuxtools.tmf.core.analysis;

/**
 * This class listens when new analysis modules are created and registers an
 * output if the module corresponds to the output specifications
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public class TmfNewAnalysisOutputListener implements ITmfNewAnalysisModuleListener {

    private final String fAnalysisId;
    private final Class<? extends IAnalysisModule> fAnalysisModuleClass;
    private final IAnalysisOutput fOutput;

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
    public TmfNewAnalysisOutputListener(IAnalysisOutput output, String analysisId, Class<? extends IAnalysisModule> moduleClass) {
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
