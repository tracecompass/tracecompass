/*******************************************************************************
 * Copyright (c) 2013, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.profiling.core.callstack;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callgraph.ICallGraphProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

/**
 * The base classes for analyses who want to populate the CallStack state
 * system.
 *
 * @author Matthew Khouzam
 * @since 2.1
 */
public abstract class CallStackAnalysis extends TmfStateSystemAnalysisModule implements IFlameChartProvider {

    private static final String[] DEFAULT_PROCESSES_PATTERN = new String[] { CallStackStateProvider.PROCESSES, "*" }; //$NON-NLS-1$
    private static final String[] DEFAULT_THREADS_PATTERN = new String[] { "*" }; //$NON-NLS-1$
    private static final String[] DEFAULT_CALL_STACK_PATH = new String[] { CallStackStateProvider.CALL_STACK };

    private final CallGraphAnalysis fCallGraphAnalysis;

    /**
     * Abstract constructor (should only be called via the sub-classes'
     * constructors.
     */
    protected CallStackAnalysis() {
        super();
        fCallGraphAnalysis = new CallGraphAnalysis(this);
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

    // ------------------------------------------------------------------------
    // Method overwrites for sub-modules
    // ------------------------------------------------------------------------

    @Override
    protected boolean executeAnalysis(@Nullable IProgressMonitor monitor) {
        boolean result = super.executeAnalysis(monitor);
        if (!result) {
            return false;
        }
        fCallGraphAnalysis.schedule();
        return result;
    }

    @Override
    public boolean setTrace(@NonNull ITmfTrace trace) throws TmfAnalysisException {
        boolean ret = super.setTrace(trace);
        if (!ret) {
            return ret;
        }
        ret = fCallGraphAnalysis.setTrace(trace);
        return ret;
    }

    @Override
    public void dispose() {
        fCallGraphAnalysis.dispose();
        super.dispose();
    }

    // ------------------------------------------------------------------------
    // ISegmentStoreProvider
    // ------------------------------------------------------------------------

    @Override
    public void addListener(IAnalysisProgressListener listener) {
        fCallGraphAnalysis.addListener(listener);
    }

    @Override
    public void removeListener(IAnalysisProgressListener listener) {
        fCallGraphAnalysis.removeListener(listener);
    }

    @Override
    public Iterable<ISegmentAspect> getSegmentAspects() {
        return fCallGraphAnalysis.getSegmentAspects();
    }

    @Override
    public @Nullable ISegmentStore<ISegment> getSegmentStore() {
        return fCallGraphAnalysis.getSegmentStore();
    }

    /**
     * Get the callgraph module associated with this callstack
     *
     * @return The callgraph module
     */
    public ICallGraphProvider getCallGraph() {
        return fCallGraphAnalysis;
    }

}