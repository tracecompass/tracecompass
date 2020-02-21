/*******************************************************************************
 * Copyright (c) 2013, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.tracecompass.analysis.profiling.core.callstack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callgraph.ICallGraphProvider;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackHostUtils.TraceHostIdResolver;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackSeries.IThreadIdResolver;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.SymbolAspect;
import org.eclipse.tracecompass.internal.tmf.core.analysis.callsite.CallsiteAnalysis;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.event.aspect.ITmfEventAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfCpuAspect;
import org.eclipse.tracecompass.tmf.core.event.aspect.TmfDeviceAspect;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.statesystem.TmfStateSystemAnalysisModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * The base classes for analyses who want to populate the CallStack state
 * system.
 *
 * @author Matthew Khouzam
 * TODO: Have this class also implement ICallGraphProvider at the next API break
 */
public abstract class CallStackAnalysis extends TmfStateSystemAnalysisModule implements IFlameChartProvider {

    /** CallStack stack-attribute
     * @since 1.1*/
    public static final String CALL_STACK = "CallStack"; //$NON-NLS-1$

    private static final String[] DEFAULT_PROCESSES_PATTERN = new String[] { CallStackStateProvider.PROCESSES, "*" }; //$NON-NLS-1$

    private static final String[] DEFAULT_THREADS_PATTERN = new String[] { "*" }; //$NON-NLS-1$

    private static final String[] DEFAULT_CALL_STACK_PATH = new String[] { CALL_STACK };

    private final CallGraphAnalysis fCallGraphAnalysis;

    private final ListenerList<IAnalysisProgressListener> fListeners = new ListenerList<>(ListenerList.IDENTITY);

    private @Nullable CallStackSeries fCallStacks = null;
    private final List<String[]> fPatterns;

    private boolean fAutomaticCallgraph;

    /**
     * Abstract constructor (should only be called via the sub-classes'
     * constructors.
     */
    protected CallStackAnalysis() {
        super();
        fCallGraphAnalysis = new CallGraphAnalysis(this);
        fPatterns = ImmutableList.of(getProcessesPattern(), getThreadsPattern());
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
     * Resolve the device ID if applicable. A device is the hardware context the trace is running
     * on. An example would be CPU, GPU, DSP or even FPGA. This could allow device centric analyses
     * such as the Callsite analysis to enrich the view.
     *
     * @param quark
     *            quark of the state system to query
     * @param timestamp
     *            time stamp to query
     * @return the device ID
     * @since 1.2
     */
    public @Nullable Long resolveDeviceId(int quark, long timestamp) {
        return null;
    }


    /**
     * Resolve the device type if applicable. A device is the hardware context
     * the trace is running on. An example would be CPU, GPU, DSP or even FPGA.
     * This could allow device centric analyses such as the
     * {@link CallsiteAnalysis} to enrich the view.
     *
     * @implNote the default implementation is associating the callstack to a
     *           CPU first, then any device type or "unknown" if the trace has
     *           no device information. An implementer could override this to
     *           give a custom implementation with ordering, for example, if
     *           GPUs are higher priority, however, it is up to them to make
     *           sure the data is coherent.
     *
     * @param quark
     *            quark of the state system to query
     * @param timestamp
     *            time stamp to query
     * @return the device type
     * @since 1.2
     */
    public @Nullable String resolveDeviceType(int quark, long timestamp) {
        ITmfTrace trace = getTrace();
        if (trace != null) {
            for (ITmfEventAspect<?> aspect : trace.getEventAspects()) {
                if (aspect instanceof TmfCpuAspect) {
                    return ((TmfCpuAspect) aspect).getDeviceType();
                }
            }
            for (ITmfEventAspect<?> aspect : trace.getEventAspects()) {
                if (aspect instanceof TmfDeviceAspect) {
                    return ((TmfDeviceAspect) aspect).getDeviceType();
                }
            }
        }
        return "unknown"; //$NON-NLS-1$
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
     * Override this method if the state system attributes do not match the default
     * pattern defined by {@link CallStackStateProvider}.
     *
     * @return the relative path of the call stack attribute
     * @deprecated Use the {@link #CALL_STACK} value instead for the last part of
     *             the path
     */
    @Deprecated
    public String[] getCallStackPath() {
        return DEFAULT_CALL_STACK_PATH;
    }

    /**
     * Get the patterns for the the different levels of callstack in the state
     * system. By default, it returns a list of the patterns returned by
     * {@link #getProcessesPattern()} and {@link #getThreadsPattern()}. If the
     * analysis has another pattern hierarchy, this method should be overridden.
     *
     * @return The patterns for the different levels in the state system
     * @since 1.1
     */
    protected List<String[]> getPatterns() {
        return fPatterns;
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
        if (fAutomaticCallgraph) {
            fCallGraphAnalysis.schedule();
        }
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
    public @Nullable CallStackSeries getCallStackSeries() {
        CallStackSeries callstacks = fCallStacks;
        if (callstacks == null) {
            ITmfStateSystem ss = getStateSystem();
            ITmfTrace trace = getTrace();
            if (ss == null || trace == null) {
                return null;
            }
            callstacks = new CallStackSeries(ss, getPatterns(), 0, "", getCallStackHostResolver(trace), getCallStackTidResolver()); //$NON-NLS-1$
            fCallStacks = callstacks;
        }
        return callstacks;
    }

    /**
     * Get the callstack host ID resolver for this instrumented series. The default
     * is to use the host name of the trace.
     *
     * @param trace
     *            The trace this analysis is run on
     * @return The host ID resolver
     * @since 1.1
     */
    protected TraceHostIdResolver getCallStackHostResolver(ITmfTrace trace) {
        return new CallStackHostUtils.TraceHostIdResolver(trace);
    }

    /**
     * Get the callstack TID resolver for this instrumented series. The default is
     * to use the name of the second attribute as the thread ID.
     *
     * @return The thread ID resolver
     * @since 1.1
     */
    protected @Nullable IThreadIdResolver getCallStackTidResolver() {
        return new CallStackSeries.AttributeValueThreadResolver(1);
    }

    @Override
    public @Nullable ISegmentStore<ISegment> getSegmentStore() {
        CallStackSeries series = getCallStackSeries();
        if (series == null) {
            return null;
        }
        return series;
    }

    @Override
    public void addListener(@NonNull IAnalysisProgressListener listener) {
        fListeners.add(listener);
    }

    @Override
    public void removeListener(@NonNull IAnalysisProgressListener listener) {
        fListeners.remove(listener);
    }

    @Override
    public Iterable<ISegmentAspect> getSegmentAspects() {
        return Collections.singletonList(SymbolAspect.SYMBOL_ASPECT);
    }

    /**
     * Returns all the listeners
     *
     * @return latency listeners
     * @since 1.1
     */
    protected Iterable<IAnalysisProgressListener> getListeners() {
        List<IAnalysisProgressListener> listeners = new ArrayList<>();
        for (Object listener : fListeners.getListeners()) {
            if (listener != null) {
                listeners.add((IAnalysisProgressListener) listener);
            }
        }
        return listeners;
    }

    /**
     * Send the segment store to all its listener
     *
     * @param store
     *            The segment store to broadcast
     * @since 1.1
     */
    protected void sendUpdate(final ISegmentStore<ISegment> store) {
        for (IAnalysisProgressListener listener : getListeners()) {
            listener.onComplete(this, store);
        }
    }

    /**
     * Get the callgraph module associated with this callstack
     *
     * @return The callgraph module
     */
    public ICallGraphProvider getCallGraph() {
        return fCallGraphAnalysis;
    }

    /**
     * Set whether the callgraph execution should be triggered automatically after
     * building the callstack or if it should wait to be requested. This is used
     * in benchmark to control when the callgraph module will be built.
     *
     * @param trigger
     *            {@code true} means the callgraph analysis will be executed after
     *            the callstack, {@code false} means it will be executed on demand
     *            only.
     * @since 1.1
     */
    @VisibleForTesting
    public void triggerAutomatically(boolean trigger) {
        fAutomaticCallgraph = trigger;
    }

}