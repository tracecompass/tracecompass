/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.timing.core.callgraph;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.common.core.StreamUtils;
import org.eclipse.tracecompass.internal.analysis.timing.core.Activator;
import org.eclipse.tracecompass.internal.analysis.timing.core.store.ArrayListStore;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * Call stack analysis used to create a segment for each call function from an
 * entry/exit event. It builds a segment tree from the state system. An example
 * taken from the Fibonacci trace's callStack shows the structure of the segment
 * tree given by this analysis:
 *
 * <pre>
 * (Caller)  main
 *            ↓↑
 * (Callee) Fibonacci
 *           ↓↑    ↓↑
 *      Fibonacci Fibonacci
 *         ↓↑         ↓↑
 *         ...        ...
 * </pre>
 *
 * @author Sonia Farrah
 */
public abstract class CallGraphAnalysis extends TmfAbstractAnalysisModule implements ISegmentStoreProvider {

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Segment store
     */
    private final ISegmentStore<@NonNull ISegment> fStore = new ArrayListStore<>();

    /**
     * Listeners
     */
    private final ListenerList fListeners = new ListenerList(ListenerList.IDENTITY);

    /**
     * The Trace's root functions list
     */
    private final List<ICalledFunction> fRootFunctions = new ArrayList<>();

    /**
     * The sub attributes of a certain thread
     */
    private List<Integer> fCurrentQuarks = Collections.emptyList();

    /**
     * Default constructor
     */
    public CallGraphAnalysis() {
        super();
    }

    @Override
    public @NonNull String getHelpText() {
        String msg = Messages.CallGraphAnalysis_Description;
        return (msg != null) ? msg : super.getHelpText();
    }

    @Override
    public @NonNull String getHelpText(@NonNull ITmfTrace trace) {
        return getHelpText();
    }

    @Override
    public boolean canExecute(ITmfTrace trace) {
        /*
         * FIXME: change to !Iterables.isEmpty(getDependentAnalyses()) when
         * analysis dependencies work better
         */
        return true;
    }

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        return TmfTraceManager.getTraceSet(getTrace()).stream()
                .flatMap(trace -> StreamUtils.getStream(TmfTraceUtils.getAnalysisModulesOfClass(trace, CallStackAnalysis.class)))
                .distinct().collect(Collectors.toList());
    }

    @Override
    protected boolean executeAnalysis(@Nullable IProgressMonitor monitor) {
        ITmfTrace trace = getTrace();
        if (monitor == null || trace == null) {
            return false;
        }
        Iterable<IAnalysisModule> dependentAnalyses = getDependentAnalyses();
        for (IAnalysisModule module : dependentAnalyses) {
            if (!(module instanceof CallStackAnalysis)) {
                return false;
            }
            module.schedule();
        }
        // TODO:Look at updates while the state system's being built
        dependentAnalyses.forEach((t) -> t.waitForCompletion(monitor));
        for (IAnalysisModule module : dependentAnalyses) {
            CallStackAnalysis callstackModule = (CallStackAnalysis) module;
            String[] threadsPattern = callstackModule.getThreadsPattern();
            String[] processesPattern = callstackModule.getProcessesPattern();
            String[] callStackPath = callstackModule.getCallStackPath();
            ITmfStateSystem ss = callstackModule.getStateSystem();
            if (!iterateOverStateSystem(ss, threadsPattern, processesPattern, callStackPath, monitor)) {
                return false;
            }
        }
        monitor.worked(1);
        monitor.done();
        return true;

    }

    /**
     * Iterate over the process of the state system,then iterate over the
     * different threads of each process.
     *
     * @param ss
     *            The state system
     * @param threadsPattern
     *            The threads pattern
     * @param processesPattern
     *            The processes pattern
     * @param callStackPath
     *            The call stack path
     * @param monitor
     *            The monitor
     * @return Boolean
     */
    @VisibleForTesting
    protected boolean iterateOverStateSystem(@Nullable ITmfStateSystem ss, String[] threadsPattern, String[] processesPattern, String[] callStackPath, IProgressMonitor monitor) {
        if (ss == null) {
            return false;
        }
        List<Integer> processQuarks = ss.getQuarks(processesPattern);
        for (int processQuark : processQuarks) {
            for (int threadQuark : ss.getQuarks(processQuark, threadsPattern)) {
                if (!iterateOverQuark(ss, threadQuark, callStackPath, monitor)) {
                    return false;
                }
            }
        }
        sendUpdate(fStore);
        return true;
    }

    /**
     * Iterate over functions with the same quark,search for their callees then
     * add them to the segment store
     *
     * @param stateSystem
     *            The state system
     * @param quark
     *            The quark
     * @param subAttributePath
     *            sub-Attributes path
     * @param monitor
     *            The monitor
     * @return Boolean
     */
    private boolean iterateOverQuark(ITmfStateSystem stateSystem, int quark, String[] subAttributePath, IProgressMonitor monitor) {
        try {
            long curTime = stateSystem.getStartTime();
            long limit = stateSystem.getCurrentEndTime();
            while (curTime < limit) {
                if (monitor.isCanceled()) {
                    return false;
                }
                int callStackQuark = stateSystem.getQuarkRelative(quark, subAttributePath);
                fCurrentQuarks = stateSystem.getSubAttributes(callStackQuark, false);
                if (fCurrentQuarks.isEmpty()) {
                    return false;
                }
                final int depth = 0;
                int quarkParent = fCurrentQuarks.get(depth);
                ITmfStateInterval interval = stateSystem.querySingleState(curTime, quarkParent);
                ITmfStateValue stateValue = interval.getStateValue();

                if (!stateValue.isNull()) {
                    long intervalStart = interval.getStartTime();
                    long intervalEnd = interval.getEndTime();
                    // Create the segment for the first call event.
                    AbstractCalledFunction segment = CalledFunctionFactory.create(intervalStart, intervalEnd + 1, depth, stateValue, null);
                    fRootFunctions.add(segment);
                    if (!findChildren(segment, depth, stateSystem, fCurrentQuarks.size() + fCurrentQuarks.get(depth), monitor)) {
                        return false;
                    }

                }

                curTime = interval.getEndTime() + 1;
            }

        } catch (AttributeNotFoundException | StateSystemDisposedException | TimeRangeException e) {
            Activator.getInstance().logError(Messages.QueringStateSystemError, e);
            return false;
        }
        return true;
    }

    /**
     * Find the functions called by a parent function in a call stack then add
     * segments for each child, updating the self times of each node
     * accordingly.
     *
     * @param node
     *            The segment of the stack call event(the parent) callStackQuark
     * @param depth
     *            The depth of the parent function
     * @param ss
     *            The quark of the segment parent ss The actual state system
     * @param maxQuark
     *            The last quark in the state system
     * @param monitor
     *            The progress monitor The progress monitor TODO: if stack size
     *            is an issue, convert to a stack instead of recursive function
     */
    private boolean findChildren(AbstractCalledFunction node, int depth, ITmfStateSystem ss, int maxQuark, IProgressMonitor monitor) {
        fStore.add(node);
        long curTime = node.getStart();
        long limit = node.getEnd();
        ITmfStateInterval interval = null;
        while (curTime < limit) {
            if (monitor.isCanceled()) {
                return false;
            }
            try {
                if (depth + 1 < fCurrentQuarks.size()) {
                    interval = ss.querySingleState(curTime, fCurrentQuarks.get(depth + 1));
                } else {
                    return true;
                }
            } catch (StateSystemDisposedException e) {
                Activator.getInstance().logError(Messages.QueringStateSystemError, e);
                return false;
            }
            ITmfStateValue stateValue = interval.getStateValue();
            if (!stateValue.isNull()) {
                long intervalStart = interval.getStartTime();
                long intervalEnd = interval.getEndTime();
                if (intervalStart < node.getStart() || intervalEnd > limit) {
                    return true;
                }
                AbstractCalledFunction segment = CalledFunctionFactory.create(intervalStart, intervalEnd + 1, node.getDepth() + 1, stateValue, node);
                // Search for the children with the next quark.
                findChildren(segment, depth + 1, ss, maxQuark, monitor);
                node.addChild(segment);
            }
            curTime = interval.getEndTime() + 1;
        }
        return true;
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
    protected void canceling() {
        // Do nothing
    }

    @Override
    public @Nullable ISegmentStore<@NonNull ISegment> getSegmentStore() {
        return fStore;
    }

    /**
     * Update listeners
     *
     * @param store
     *            The segment store
     */
    protected void sendUpdate(final ISegmentStore<@NonNull ISegment> store) {
        getListeners().forEach(listener -> listener.onComplete(this, store));
    }

    /**
     * Get Listeners
     *
     * @return The listeners
     */
    protected Iterable<IAnalysisProgressListener> getListeners() {
        return Arrays.stream(fListeners.getListeners())
                .filter(listener -> listener instanceof IAnalysisProgressListener)
                .map(listener -> (IAnalysisProgressListener) listener)
                .collect(Collectors.toList());
    }

    /**
     * The functions of the first level
     *
     * @return Functions of the first level
     */
    public List<ICalledFunction> getRootFunctions() {
        return ImmutableList.copyOf(fRootFunctions);
    }

}