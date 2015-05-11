/*******************************************************************************
 * Copyright (c) 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Add methods to get attribute paths
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.views.callstack;

import org.eclipse.tracecompass.tmf.core.callstack.CallStackStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * The base classes for analyses who want to populate the CallStack View.
 *
 * @author Alexandre Montplaisir
 */
public abstract class AbstractCallStackAnalysis extends TmfStateSystemAnalysisModule {

    private static final String[] DEFAULT_THREADS_PATTERN =
            new String[] { CallStackStateProvider.THREADS, "*" }; //$NON-NLS-1$;

    private static final String[] DEFAULT_CALL_STACK_PATH =
            new String[] { CallStackStateProvider.CALL_STACK };

    /**
     * Abstract constructor (should only be called via the sub-classes'
     * constructors.
     */
    public AbstractCallStackAnalysis() {
        super();
        registerOutput(new TmfAnalysisViewOutput(CallStackView.ID));
    }

    /**
     * Get the pattern of thread attributes. Override this method if the state
     * system attributes do not match the default pattern defined by
     * {@link CallStackStateProvider}.
     *
     * @return the absolute pattern of the thread attributes
     */
    public String[] getThreadsPattern() {
        return DEFAULT_THREADS_PATTERN;
    }

    /**
     * Get the call stack attribute path relative to a thread attribute found by
     * {@link #getThreadsPattern()}. Override this method if the state system
     * attributes do not match the default pattern defined by
     * {@link CallStackStateProvider}.
     *
     * @return the relative path of the call stack attribute
     */
    public String[] getCallStackPath() {
        return DEFAULT_CALL_STACK_PATH;
    }
}
