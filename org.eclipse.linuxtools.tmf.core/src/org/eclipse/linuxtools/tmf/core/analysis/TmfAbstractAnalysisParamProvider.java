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

import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;

/**
 * Abstract class for parameter providers, implements methods and
 * functionalities to warn the analysis module of parameter changed
 *
 * @author Geneviève Bastien
 * @since 3.0
 */
public abstract class TmfAbstractAnalysisParamProvider implements IAnalysisParameterProvider {

    /**
     * The module registered with this provider
     */
    private IAnalysisModule fModule;

    @Override
    public void registerModule(IAnalysisModule module) {
        if (module == null) {
            throw new IllegalArgumentException();
        }
        ITmfTrace selectedTrace = TmfTraceManager.getInstance().getActiveTrace();
        /* If no trace is active, just register the module */
        if (selectedTrace == null) {
            fModule = module;
            return;
        }
        IAnalysisModule selectedModule = selectedTrace.getAnalysisModule(module.getId());
        /* register only if the module is for the currently selected trace */
        if (selectedModule == module) {
            fModule = module;
        }
    }

    /**
     * Gets the analysis module
     *
     * @return the {@link IAnalysisModule} associated with this provider
     */
    protected IAnalysisModule getModule() {
        return fModule;
    }

    /**
     * Notify the registered module that the said parameter has a new value. The
     * analysis module will decide what to do with this information
     *
     * @param name
     *            Name of the modified parameter
     */
    protected void notifyParameterChanged(String name) {
        if (fModule != null) {
            fModule.notifyParameterChanged(name);
        }
    }
}
