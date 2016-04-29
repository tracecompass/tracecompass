/*******************************************************************************
 * Copyright (c) 2014, 2016 Ericsson
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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.tmf.core.callstack.CallStackStateProvider;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.ui.analysis.TmfAnalysisViewOutput;

/**
 * The base classes for analyses who want to populate the CallStack View.
 *
 * @author Alexandre Montplaisir
 */
@NonNullByDefault
public abstract class AbstractCallStackAnalysis extends TmfStateSystemAnalysisModule {

    private static final String[] DEFAULT_PROCESSES_PATTERN =
            new String[] { CallStackStateProvider.PROCESSES, "*" }; //$NON-NLS-1$

    private static final String[] DEFAULT_THREADS_PATTERN =
            new String[] { "*" }; //$NON-NLS-1$

    private static final String[] DEFAULT_CALL_STACK_PATH =
            new String[] { CallStackStateProvider.CALL_STACK };

    /**
     * Abstract constructor (should only be called via the sub-classes'
     * constructors.
     */
    protected AbstractCallStackAnalysis() {
        super();
        registerOutput(new TmfAnalysisViewOutput(CallStackView.ID));
    }

    /**
     * The quark pattern, relative to the root, to get the list of attributes
     * representing the different processes of a trace.
     * <p>
     * If the trace does not define processes, an empty array can be returned.
     * <p>
     * The pattern is passed as-is to
     * {@link org.eclipse.tracecompass.statesystem.core.ITmfStateSystem#getQuarks(String...)}.
     * <p>
     * Override this method if the state system attributes do not match the
     * default pattern defined by {@link CallStackStateProvider}.
     *
     * @return The quark pattern to find the process attributes
     * @since 2.0
     */
    public String[] getProcessesPattern() {
        return DEFAULT_PROCESSES_PATTERN;
    }

    /**
     * The quark pattern, relative to an attribute found by
     * {@link #getProcessesPattern()}, to get the list of attributes
     * representing the threads of a process, or the threads a trace if the
     * process pattern was empty.
     * <p>
     * If the trace does not define threads, an empty array can be returned.
     * <p>
     * This will be passed as-is to
     * {@link org.eclipse.tracecompass.statesystem.core.ITmfStateSystem#getQuarks(int, String...)}.
     * <p>
     * Override this method if the state system attributes do not match the
     * default pattern defined by {@link CallStackStateProvider}.
     *
     * @return The quark pattern to find the thread attributes
     */
    public String[] getThreadsPattern() {
        return DEFAULT_THREADS_PATTERN;
    }

    /**
     * Get the call stack attribute path, relative to an attribute found by the
     * combination of {@link #getProcessesPattern()} and
     * {@link #getThreadsPattern()}.
     * <p>
     * Override this method if the state system attributes do not match the
     * default pattern defined by {@link CallStackStateProvider}.
     *
     * @return the relative path of the call stack attribute
     */
    public String[] getCallStackPath() {
        return DEFAULT_CALL_STACK_PATH;
    }
}
