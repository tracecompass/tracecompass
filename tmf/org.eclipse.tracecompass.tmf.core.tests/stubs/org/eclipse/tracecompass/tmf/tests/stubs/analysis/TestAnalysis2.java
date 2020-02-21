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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub2;

/**
 * Simple analysis type for test
 */
public class TestAnalysis2 extends TmfAbstractAnalysisModule {

    @Override
    public boolean canExecute(ITmfTrace trace) {
        /* This just makes sure the trace is or contains a trace stub 2 */
        for (ITmfTrace aTrace : TmfTraceManager.getTraceSet(trace)) {
            if (TmfTraceStub2.class.isAssignableFrom(aTrace.getClass())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void canceling() {

    }

    @Override
    protected boolean executeAnalysis(final IProgressMonitor monitor) {
        return false;
    }

}
