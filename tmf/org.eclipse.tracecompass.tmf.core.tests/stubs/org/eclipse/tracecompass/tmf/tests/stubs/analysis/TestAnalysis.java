/*******************************************************************************
 * Copyright (c) 2013, 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.tests.stubs.analysis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * Simple analysis type for test
 */
public class TestAnalysis extends TmfAbstractAnalysisModule {

    /**
     * Test parameter. If set, simulate cancellation
     */
    public static final @NonNull String PARAM_TEST = "test";

    private int fOutput = 0;

    /**
     * Constructor
     */
    public TestAnalysis() {
        super();
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {
        return true;
    }

    @Override
    protected boolean executeAnalysis(final IProgressMonitor monitor) {
        Object parameter = getParameter(PARAM_TEST);
        if (!(parameter instanceof Integer)) {
            throw new RuntimeException("The parameter should be set");
        }
        int integer = ((Integer) parameter).intValue();
        /* If PARAM_TEST is set to 0, simulate cancellation */
        fOutput = integer;
        if (integer == 0) {
            return false;
        } else if (integer == 999) {
            /* just stay in an infinite loop until cancellation */
            while (!monitor.isCanceled()) {
                try {
                    Thread.sleep(0);
                } catch (InterruptedException e) {
                    break;
                }
            }
            return !monitor.isCanceled();
        }
        return true;
    }

    @Override
    protected void canceling() {
        fOutput = -1;
    }

    @Override
    public synchronized Object getParameter(String name) {
        Object value = super.getParameter(name);
        if ((value != null) && name.equals(PARAM_TEST) && (value instanceof String)) {
            return Integer.decode((String) value);
        }
        return value;
    }

    @Override
    protected void parameterChanged(String name) {
        schedule();
    }

    /**
     * Get the analysis output value
     *
     * @return The analysis output
     */
    public int getAnalysisOutput() {
        return fOutput;
    }

}
