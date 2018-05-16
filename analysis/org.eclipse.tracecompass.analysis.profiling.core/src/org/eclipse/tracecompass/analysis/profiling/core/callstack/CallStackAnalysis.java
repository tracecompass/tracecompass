/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.profiling.core.callstack;

import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;

/**
 * The base classes for analyses who want to populate the CallStack state
 * system.
 *
 * @author Matthew Khouzam
 * @since 2.1
 */
public abstract class CallStackAnalysis extends TmfStateSystemAnalysisModule {

    private static final String[] DEFAULT_PROCESSES_PATTERN = new String[] { CallStackStateProvider.PROCESSES, "*" }; //$NON-NLS-1$
    private static final String[] DEFAULT_THREADS_PATTERN = new String[] { "*" }; //$NON-NLS-1$
    private static final String[] DEFAULT_CALL_STACK_PATH = new String[] { CallStackStateProvider.CALL_STACK };

    /**
     * Abstract constructor (should only be called via the sub-classes'
     * constructors.
     */
    protected CallStackAnalysis() {
        super();
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