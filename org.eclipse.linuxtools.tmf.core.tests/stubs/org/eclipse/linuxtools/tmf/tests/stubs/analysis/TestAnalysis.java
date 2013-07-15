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

package org.eclipse.linuxtools.tmf.tests.stubs.analysis;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.linuxtools.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;

/**
 * Simple analysis type for test
 */
public class TestAnalysis extends TmfAbstractAnalysisModule {

    private int output = 0;

    /**
     * Test parameter. If set, simulate cancellation
     */
    public static final String PARAM_TEST = "test";

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
        if (getParameter(PARAM_TEST) == null) {
            throw new RuntimeException("The parameter should be set");
        }
        /* If PARAM_TEST is set to 0, simulate cancellation */
        if ((Integer) getParameter(PARAM_TEST) == 0) {
            output = 0;
            return false;
        } else if ((Integer) getParameter(PARAM_TEST) == 999) {
            /* just stay in an infinite loop until cancellation */
            while (!monitor.isCanceled()) {

            }
            return !monitor.isCanceled();
        }
        output = (Integer) getParameter(PARAM_TEST);
        return true;
    }

    @Override
    protected void canceling() {
        output = -1;
    }

    @Override
    public Object getParameter(String name) {
        Object value = super.getParameter(name);
        if ((value != null) && name.equals(PARAM_TEST) && (value instanceof String)) {
            return Integer.parseInt((String) value);
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
        return output;
    }

}
