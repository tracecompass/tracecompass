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
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisParamProvider;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;

/**
 * Test parameter provider for the PARAM_TEST that would apply only to
 * CtfTmfTrace (though it is associated with an analysis that supports all trace
 * types)
 *
 * @author Geneviève Bastien
 */
public class TestAnalysisParameterProvider extends TmfAbstractAnalysisParamProvider {

    private int fValue = 10;

    @Override
    public String getName() {
        return "test parameter provider";
    }

    @Override
    public Object getParameter(String name) {
        if (name.equals(TestAnalysis.PARAM_TEST)) {
            return fValue;
        }
        return null;
    }

    @Override
    public boolean appliesToTrace(ITmfTrace trace) {
        return (trace instanceof TmfTraceStub);
    }

    /**
     * Sets a new value for the parameter
     *
     * @param value
     *            new parameter value
     */
    public void setValue(int value) {
        fValue = value;
        notifyParameterChanged(TestAnalysis.PARAM_TEST);
    }

}
