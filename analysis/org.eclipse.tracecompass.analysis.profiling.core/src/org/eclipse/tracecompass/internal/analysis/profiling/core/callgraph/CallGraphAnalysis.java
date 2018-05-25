/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callgraph.ICallGraphProvider;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.SymbolAspect;
import org.eclipse.tracecompass.internal.analysis.timing.core.Activator;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory.SegmentStoreType;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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
public class CallGraphAnalysis extends TmfAbstractAnalysisModule implements ISegmentStoreProvider, ICallGraphProvider {

    /**
     * ID
     */
    public static final String ID = "org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.callgraphanalysis"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Segment store
     */
    private final ISegmentStore<@NonNull ISegment> fStore;
    /**
     * This field will be set once the segment store is completed. Because the
     * segment store to return must be either null or completed
     */
    private @Nullable ISegmentStore<@NonNull ISegment> fCompletedStore = null;

    /**
     * Listeners. {@link ListenerList}s are typed since 4.6 (Neon), type these when
     * support for 4.5 (Mars) is no longer required.
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
     * The List of thread nodes. Each thread has a virtual node having the root
     * function as children
     */
    private List<ThreadNode> fThreadNodes = new ArrayList<>();

    private final @Nullable CallStackAnalysis fCallStackAnalysis;

    /**
     * Protected constructor, without the analysis
     */
    protected CallGraphAnalysis() {
        super();
        fStore = SegmentStoreFactory.createSegmentStore(SegmentStoreType.Fast);
        fCallStackAnalysis = null;
    }

    /**
     * Default constructor
     *
     * @param callStackAnalysis
     *            The callstack analysis this callgraph will be built upon
     */
    public CallGraphAnalysis(CallStackAnalysis callStackAnalysis) {
        super();
        fStore = SegmentStoreFactory.createSegmentStore(SegmentStoreType.Fast);
        fCallStackAnalysis = callStackAnalysis;
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
    public @NonNull Iterable<@NonNull ISegmentAspect> getSegmentAspects() {
        return Collections.singletonList(SymbolAspect.SYMBOL_ASPECT);
    }

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        CallStackAnalysis callStackAnalysis = fCallStackAnalysis;
        if (callStackAnalysis == null) {
            throw new NullPointerException("If the analysis is not set, this method should not be called");
        }
        return Collections.singleton(callStackAnalysis);
    }

    @Override
    protected boolean executeAnalysis(@Nullable IProgressMonitor monitor) {
        ITmfTrace trace = getTrace();
        if (monitor == null || trace == null) {
            return false;
        }
        CallStackAnalysis callstackModule = fCallStackAnalysis;
        if (callstackModule == null) {
            return false;
        }
        callstackModule.schedule();
        callstackModule.waitForCompletion(monitor);
        // TODO:Look at updates while the state system's being built
        String[] threadsPattern = callstackModule.getThreadsPattern();
        String[] processesPattern = callstackModule.getProcessesPattern();
        String[] callStackPath = callstackModule.getCallStackPath();
        ITmfStateSystem ss = callstackModule.getStateSystem();
        if (ss == null || !iterateOverStateSystem(ss, threadsPattern, processesPattern, callStackPath, monitor)) {
            return false;
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
    protected boolean iterateOverStateSystem(ITmfStateSystem ss, String[] threadsPattern, String[] processesPattern, String[] callStackPath, IProgressMonitor monitor) {
        List<Integer> processQuarks = ss.getQuarks(processesPattern);
        for (int processQuark : processQuarks) {
            int processId = getProcessId(ss, processQuark, ss.getCurrentEndTime());
            for (int threadQuark : ss.getQuarks(processQuark, threadsPattern)) {
                if (!iterateOverQuark(ss, processId, threadQuark, callStackPath, monitor)) {
                    return false;
                }
            }
        }
        // Set the completed store and send updates
        fCompletedStore = fStore;
        sendUpdate(fStore);
        return true;
    }

    /**
     * Iterate over functions with the same quark, search for their callees then add
     * them to the segment store
     *
     * @param stateSystem
     *            The state system
     * @param processId
     *            The process ID of the traced application
     * @param threadQuark
     *            The thread quark
     * @param subAttributePath
     *            sub-Attributes path
     * @param monitor
     *            The monitor
     * @return Boolean
     */
    private boolean iterateOverQuark(ITmfStateSystem stateSystem, int processId, int threadQuark, String[] subAttributePath, IProgressMonitor monitor) {
        String threadName = stateSystem.getAttributeName(threadQuark);
        long threadId = getProcessId(stateSystem, threadQuark, stateSystem.getStartTime());
        try {
            long curTime = stateSystem.getStartTime();
            long limit = stateSystem.getCurrentEndTime();
            AbstractCalledFunction initSegment = CalledFunctionFactory.create(0, 0, 0, threadName, processId, null);
            ThreadNode init = new ThreadNode(initSegment, 0, threadId);
            while (curTime < limit) {
                if (monitor.isCanceled()) {
                    return false;
                }
                int callStackQuark = stateSystem.optQuarkRelative(threadQuark, subAttributePath);
                if (callStackQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                    return false;
                }
                fCurrentQuarks = stateSystem.getSubAttributes(callStackQuark, false);
                if (fCurrentQuarks.isEmpty()) {
                    return false;
                }
                final int depth = 0;
                int quarkParent = fCurrentQuarks.get(depth);
                ITmfStateInterval interval = stateSystem.querySingleState(curTime, quarkParent);
                Object stateValue = interval.getValue();

                if (stateValue != null) {
                    long intervalStart = interval.getStartTime();
                    long intervalEnd = interval.getEndTime();
                    // Create the segment for the first call event.
                    AbstractCalledFunction rootFunction = CalledFunctionFactory.create(intervalStart, intervalEnd + 1, depth, stateValue, processId, null);
                    fRootFunctions.add(rootFunction);
                    AggregatedCalledFunction firstNode = new AggregatedCalledFunction(rootFunction, fCurrentQuarks.size());
                    if (!findChildren(rootFunction, depth, stateSystem, fCurrentQuarks.size() + fCurrentQuarks.get(depth), firstNode, processId, monitor)) {
                        return false;
                    }
                    init.addChild(rootFunction, firstNode);
                }

                curTime = interval.getEndTime() + 1;
            }
            fThreadNodes.add(init);
        } catch (StateSystemDisposedException | TimeRangeException e) {
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
     * @param parentFunction
     *            The segment of the stack call event(the parent) callStackQuark
     * @param depth
     *            The depth of the parent function
     * @param ss
     *            The quark of the segment parent ss The actual state system
     * @param maxQuark
     *            The last quark in the state system
     * @param parent
     *            A node in the aggregation tree
     * @param processId
     *            The process ID of the traced application
     * @param monitor
     *            The progress monitor The progress monitor TODO: if stack size
     *            is an issue, convert to a stack instead of recursive function
     */
    private boolean findChildren(AbstractCalledFunction parentFunction, int depth, ITmfStateSystem ss,
            int maxQuark, AggregatedCalledFunction parent, int processId, IProgressMonitor monitor) {
        fStore.add(parentFunction);
        long curTime = parentFunction.getStart();
        long limit = parentFunction.getEnd();
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
            Object stateValue = interval.getValue();
            if (stateValue != null) {
                long intervalStart = interval.getStartTime();
                long intervalEnd = interval.getEndTime();
                if (intervalStart < parentFunction.getStart() || intervalEnd > limit) {
                    return true;
                }
                AbstractCalledFunction function = CalledFunctionFactory.create(intervalStart, intervalEnd + 1, parentFunction.getDepth() + 1, stateValue, processId, parentFunction);
                AggregatedCalledFunction childNode = new AggregatedCalledFunction(function, parent);
                // Search for the children with the next quark.
                findChildren(function, depth + 1, ss, maxQuark, childNode, processId, monitor);
                parentFunction.addChild(function);
                parent.addChild(function, childNode);

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
        return fCompletedStore;
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
        return Lists.newArrayList(fListeners.iterator());
    }

    /**
     * The functions of the first level
     *
     * @return Functions of the first level
     */
    public List<ICalledFunction> getRootFunctions() {
        return ImmutableList.copyOf(fRootFunctions);
    }

    /**
     * Merged threadnodes
     *
     * @return the merged threadnodes
     */
    public Collection<ThreadNode> getFlameGraph() {
        AbstractCalledFunction initSegment = CalledFunctionFactory.create(0, 0, 0, "", 0, null); //$NON-NLS-1$
        ThreadNode init = new ThreadNode(initSegment, 0, 0);
        fThreadNodes.forEach(
                tn -> tn.getChildren().forEach(
                        child -> init.addChild(initSegment, child.clone())));
        return Collections.singleton(init);

    }

    /**
     * List of thread nodes. Each thread has a virtual node having the root
     * functions called as children.
     *
     * @return The thread nodes
     */
    public List<ThreadNode> getThreadNodes() {
        return ImmutableList.copyOf(fThreadNodes);
    }

    private static int getProcessId(ITmfStateSystem ss, int processQuark, long curTime) {
        if (processQuark != ITmfStateSystem.ROOT_ATTRIBUTE) {
            try {
                ITmfStateInterval interval = ss.querySingleState(curTime, processQuark);
                String processName = ss.getAttributeName(processQuark);
                Object processValue = interval.getValue();
                if (processValue != null && (processValue instanceof Integer || processValue instanceof Long)) {
                    return ((Number) processValue).intValue();
                }
                return Integer.parseInt(processName);
            } catch (StateSystemDisposedException | NumberFormatException e) {
                /* use default processId */
            }
        }
        return -1;
    }

}