/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.signal;

import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;

/**
 * Signal indicating an analysis has started. Views and outputs may use it to
 * update themselves with the results.
 *
 * @author Geneviève Bastien
 */
public class TmfStartAnalysisSignal extends TmfSignal {

    private final IAnalysisModule fModule;

    /**
     * Constructor for a new signal.
     *
     * @param source
     *            The object sending this signal
     * @param module
     *            The analysis module
     */
    public TmfStartAnalysisSignal(Object source, IAnalysisModule module) {
        super(source);
        fModule = module;
    }

    /**
     * Get the trace object concerning this signal
     *
     * @return The trace
     */
    public IAnalysisModule getAnalysisModule() {
        return fModule;
    }

    @Override
    public String toString() {
        return "[" + this.getClass().getSimpleName() + " (" + fModule.getName() + ")]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
