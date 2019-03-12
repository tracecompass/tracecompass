/*******************************************************************************
 * Copyright (c) 2016, 2019 Ericsson
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callgraph.ICallGraphProvider;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.IFlameChartProvider;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.IAnalysisProgressListener;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.ISegmentStoreProvider;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callstack.SymbolAspect;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.analysis.TmfAbstractAnalysisModule;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;

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
 * FIXME: Remove the implemented ISegmentStoreProvider interface at next major
 * API break
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

    private final ListenerList<IAnalysisProgressListener> fListeners = new ListenerList<>(ListenerList.IDENTITY);

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

    /**
     * @deprecated Use the {@link IFlameChartProvider}'s segment store instead
     */
    @Override
    @Deprecated
    public @NonNull Iterable<@NonNull ISegmentAspect> getSegmentAspects() {
        return Collections.singletonList(SymbolAspect.SYMBOL_ASPECT);
    }

    @Override
    protected Iterable<IAnalysisModule> getDependentAnalyses() {
        CallStackAnalysis callStackAnalysis = fCallStackAnalysis;
        if (callStackAnalysis == null) {
            throw new NullPointerException("If the analysis is not set, this method should not be called"); //$NON-NLS-1$
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
        ITmfStateSystem ss = callstackModule.getStateSystem();
        if (ss == null || !iterateOverStateSystem(ss, threadsPattern, processesPattern, monitor)) {
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
     * @param monitor
     *            The monitor
     * @return true if completed successfully
     */
    @VisibleForTesting
    protected boolean iterateOverStateSystem(ITmfStateSystem ss, String[] threadsPattern, String[] processesPattern, IProgressMonitor monitor) {
        List<Integer> processQuarks = ss.getQuarks(processesPattern);
        Map<ThreadNode, List<Integer>> mainAttribs = new HashMap<>();
        for (int processQuark : processQuarks) {
            int processId = getProcessId(ss, processQuark, ss.getCurrentEndTime());
            for (int threadQuark : ss.getQuarks(processQuark, threadsPattern)) {
                int callStackQuark = ss.optQuarkRelative(threadQuark, CallStackAnalysis.CALL_STACK);
                if (callStackQuark == ITmfStateSystem.INVALID_ATTRIBUTE) {
                    continue;
                }
                List<Integer> subAttributes = ss.getSubAttributes(callStackQuark, false);
                if (subAttributes.isEmpty()) {
                    continue;
                }
                String threadName = ss.getAttributeName(threadQuark);
                long threadId = getProcessId(ss, threadQuark, ss.getStartTime());
                AbstractCalledFunction initSegment = CalledFunctionFactory.create(0, 0, -1, threadName, processId, null);
                ThreadNode init = new ThreadNode(initSegment, 0, threadId);
                fThreadNodes.add(init);
                mainAttribs.put(init, subAttributes);

            }
        }
        iterateOverCallStack2D(ss, mainAttribs, monitor);
        return true;
    }

    /** A class that represents a time range for an interval or function */
    private static class CallgraphRange {
        private final long fStart;
        private final long fEnd;

        public CallgraphRange(long start, long end) {
            fStart = start;
            fEnd = end;
        }

        /* Is there a common range with the other range */
        public boolean overlap(CallgraphRange other) {
            if (fStart <= other.fEnd && fEnd >= other.fStart) {
                return true;
            }
            return false;
        }

        /* Do 2 time ranges overlap or are they contiguous */
        public boolean overlapOrContiguous(CallgraphRange other) {
            // Do these overlap
            if (overlap(other)) {
                return true;
            }
            // Are they contiguous
            if (fStart - 1 == other.fEnd || fEnd + 1 == other.fStart) {
                return true;
            }
            return false;
        }

        /* Is the other range fully included in this range */
        public boolean includes(CallgraphRange other) {
            return (fStart <= other.fStart && fEnd >= other.fEnd);
        }

        /* Is the interval fully included in this range */
        public boolean includes(ITmfStateInterval interval) {
            return (fStart <= interval.getStartTime() && fEnd >= interval.getEndTime());
        }

        /*
         * Get the callgraph range that represents the intersection with the
         * other range
         */
        public @Nullable CallgraphRange getIntersection(CallgraphRange other) {
            if (fStart > other.fEnd || fEnd < other.fStart) {
                return null;
            }
            return new CallgraphRange(Math.max(fStart, other.fStart), Math.min(fEnd, other.fEnd));
        }

        /*
         * Return a range that is the union of this and the other range. It
         * supposes that both ranges overlap or are contiguous
         */
        public CallgraphRange getUnion(CallgraphRange other) {
            return new CallgraphRange(Math.min(fStart, other.fStart), Math.max(fEnd, other.fEnd));
        }

    }

    /** A class that associates a range with a function */
    private static class FunctionCall {
        CallgraphRange fRange;
        AbstractCalledFunction fFunc;

        public FunctionCall(CallgraphRange range, AbstractCalledFunction function) {
            fRange = range;
            fFunc = function;
        }
    }

    /** Represent a callgraph level in the algorithm */
    private static class CallGraphLevel {

        private final ThreadNode fThreadNode;
        private final List<CallgraphRange> fRanges = new ArrayList<>();
        private final Map<AggregatedCalledFunction, FunctionCall> fAggregated = new HashMap<>();
        private final List<ITmfStateInterval> fOrphanedIntervals = new ArrayList<>();
        private final @Nullable CallGraphLevel fParent;
        private final int fDepth;
        private @Nullable CallGraphLevel fChild = null;

        public CallGraphLevel(ThreadNode threadNode, int depth, @Nullable CallGraphLevel parent) {
            fThreadNode = threadNode;
            fDepth = depth;
            fParent = parent;
        }

        public void addInterval(ITmfStateInterval interval) {
            fOrphanedIntervals.add(interval);
        }

        public void setChild(CallGraphLevel childLvl) {
            fChild = childLvl;
        }

        public void setCovered(CallgraphRange newRange) {
            // Try to get the biggest range including the new one from all the
            // others
            List<CallgraphRange> toRemove = new ArrayList<>();
            CallgraphRange addRange = newRange;
            for (CallgraphRange range : fRanges) {
                if (newRange.overlapOrContiguous(range)) {
                    addRange = addRange.getUnion(range);
                    toRemove.add(range);
                }
            }
            for (CallgraphRange range : toRemove) {
                fRanges.remove(range);
            }
            fRanges.add(addRange);
        }

        private @Nullable AggregatedCalledFunction findAggregated(CallgraphRange range) {
            for (Entry<AggregatedCalledFunction, FunctionCall> entry : fAggregated.entrySet()) {
                if (entry.getValue().fRange.includes(range)) {
                    return entry.getKey();
                }
            }
            return null;
        }

        /* Find an aggregated call in the parent that spans this range */
        public @Nullable AggregatedCalledFunction findParentAggregated(CallgraphRange range) {
            CallGraphLevel parent = fParent;
            if (parent == null) {
                return fThreadNode;
            }
            return parent.findAggregated(range);
        }

        private boolean isRangeCovered(CallgraphRange range) {
            for (CallgraphRange coveredRange : fRanges) {
                if (coveredRange.includes(range)) {
                    return true;
                }
            }
            return false;
        }

        /*
         * Recursively adopt all children intervals. Return whether the range is
         * fully covered or if there is still missing information
         */
        public boolean recursiveCoverChildren(CallgraphRange range, AbstractCalledFunction function, AggregatedCalledFunction aggregated) {
            CallGraphLevel child = fChild;
            // Last level, coverage complete
            if (child == null) {
                return true;
            }
            // Set child's already covered ranges (nulls) as covered
            for (CallgraphRange childRange : child.fRanges) {
                CallgraphRange covered = range.getIntersection(childRange);
                if (covered != null) {
                    setCovered(covered);
                }
            }
            List<ITmfStateInterval> toRemove = new ArrayList<>();
            // Look if any orphaned interval in the child is within range
            for (ITmfStateInterval interval : child.fOrphanedIntervals) {
                if (!range.includes(interval)) {
                    continue;
                }
                /*
                 * The interval is within range, process it
                 *
                 * Create a function and aggregated call site for it
                 */
                toRemove.add(interval);
                CallgraphRange childRange = new CallgraphRange(interval.getStartTime(), interval.getEndTime());
                AbstractCalledFunction childFunc = CalledFunctionFactory.create(childRange.fStart, childRange.fEnd + 1, fDepth + 1, Objects.requireNonNull(interval.getValue()), fThreadNode.getProcessId(), function);
                AggregatedCalledFunction childAgg = new AggregatedCalledFunction(childFunc, aggregated);

                /*
                 * Recursively try to adopt intervals in the child
                 *
                 * Is the child fully covered ?
                 */
                if (child.recursiveCoverChildren(childRange, childFunc, childAgg)) {
                    /*
                     * Yes, add the child to the current aggregated call and set
                     * this range as covered in both child and current level
                     */
                    aggregated.addChild(childFunc, childAgg);
                    child.setCovered(childRange);
                    setCovered(childRange);
                } else {
                    /* No, save the new aggregated call in the child */
                    child.fAggregated.put(childAgg, new FunctionCall(childRange, childFunc));
                }
            }
            // Remove orphaned intervals that found a parent
            child.fOrphanedIntervals.removeAll(toRemove);
            return isRangeCovered(range);
        }

        public void tryToCompleteParentCoverage(CallgraphRange range) {
            CallGraphLevel parent = fParent;
            if (parent == null) {
                return;
            }
            parent.tryToCompleteCoverage(range, this);
        }

        private void tryToCompleteCoverage(CallgraphRange range, CallGraphLevel child) {
            List<AggregatedCalledFunction> toRemove = new ArrayList<>();
            /*
             * Look at all the aggregated function to see those that overlap the
             * current range, there could be many in case the range represents a
             * null interval
             */
            for (Entry<AggregatedCalledFunction, FunctionCall> entry : fAggregated.entrySet()) {
                CallgraphRange parentRange = entry.getValue().fRange;
                if (parentRange.overlap(range)) {
                    /*
                     * This function overlaps the range, maybe the child
                     * coverage is complete now
                     */
                    if (child.isRangeCovered(parentRange)) {
                        /*
                         * Function range fully covered in the child, add it to
                         * its parent now and try to complete the parent
                         */
                        toRemove.add(entry.getKey());
                        AggregatedCalledFunction parent = findParentAggregated(parentRange);
                        if (parent == null) {
                            // The parent was already covered, probably because
                            // there were null values above, just ignore
                            continue;
                        }
                        parent.addChild(entry.getValue().fFunc, entry.getKey());
                        setCovered(parentRange);
                        tryToCompleteParentCoverage(parentRange);
                    }
                }
            }
            for (AggregatedCalledFunction agg : toRemove) {
                fAggregated.remove(agg);
            }
        }

        public @Nullable FunctionCall getParentData(AggregatedCalledFunction parentCall) {
            CallGraphLevel parent = fParent;
            if (parent == null) {
                return null;
            }
            return parent.fAggregated.get(parentCall);
        }

        public int getDepth() {
            return fDepth;
        }

        public int getProcessId() {
            return fThreadNode.getProcessId();
        }

    }

    private static boolean iterateOverCallStack2D(ITmfStateSystem ss, Map<ThreadNode, List<Integer>> parentAttribs, IProgressMonitor monitor) {
        try {
            long start = ss.getStartTime();
            long end = ss.getCurrentEndTime();

            Map<Integer, CallGraphLevel> attribToLevel = new HashMap<>();
            List<Integer> attributes = new ArrayList<>();

            // Create the levels for all the thread nodes and attributes
            for (Entry<ThreadNode, List<Integer>> entry : parentAttribs.entrySet()) {
                ThreadNode threadNode = entry.getKey();
                List<Integer> subAttributes = entry.getValue();
                attributes.addAll(subAttributes);
                CallGraphLevel prevLevel = null;
                for (int i = 0; i < subAttributes.size(); i++) {
                    CallGraphLevel level = new CallGraphLevel(threadNode, i, prevLevel);
                    if (prevLevel != null) {
                        prevLevel.setChild(level);
                    }
                    prevLevel = level;
                    attribToLevel.put(subAttributes.get(i), level);
                }
            }

            /*
             * Do a 2D query, starting from the end of the state system, the
             * intervals ending last (ie typically the ones of lower depth) will
             * come first, though they are not sorted by end time per se, but as
             * a general trend, the callstack will be parsed from the end.
             */
            for (ITmfStateInterval interval : ss.query2D(attributes, end, start)) {
                if (monitor.isCanceled()) {
                    return false;
                }
                CallGraphLevel level = attribToLevel.get(interval.getAttribute());
                if (level == null) {
                    throw new NullPointerException("The level should not be null, we created it just before!"); //$NON-NLS-1$
                }

                long intervalStart = interval.getStartTime();
                long intervalEnd = interval.getEndTime();
                CallgraphRange range = new CallgraphRange(intervalStart, intervalEnd);
                Object value = interval.getValue();
                /* Is the interval null ? */
                if (value == null) {
                    /*
                     * Yes, there is no function to process at this level so we
                     * set this range as covered
                     */
                    level.setCovered(range);

                } else {
                    /* No, this interval represents a called function */
                    /*
                     * Is there a parent aggregated site already for this
                     * function ?
                     */
                    AggregatedCalledFunction parent = level.findParentAggregated(range);
                    if (parent == null) {
                        /* No, keep this interval for later and continue */
                        level.addInterval(interval);
                        continue;
                    }
                    /*
                     * Yes, create the function and aggregated callsite from
                     * this interval
                     */
                    FunctionCall parentData = level.getParentData(parent);
                    AbstractCalledFunction function = CalledFunctionFactory.create(intervalStart, intervalEnd + 1, level.getDepth(), value, level.getProcessId(), (parentData == null) ? null : parentData.fFunc);
                    AggregatedCalledFunction aggregated = new AggregatedCalledFunction(function, parent);
                    /*
                     * See if there are any children intervals to process and
                     * add to this aggregated site
                     */
                    /*
                     * Do we have all children information for this interval's
                     * function ?
                     */
                    if (!level.recursiveCoverChildren(range, function, aggregated)) {
                        /*
                         * No, save this function to be completed later and
                         * continue
                         */
                        level.fAggregated.put(aggregated, new FunctionCall(range, function));
                        continue;
                    }
                    /*
                     * Yes, add the current site to the parent and set this
                     * range as covered for the current level
                     */
                    parent.addChild(function, aggregated);
                    level.setCovered(range);
                }

                /*
                 * See if we can complete the parent(s) with this new information
                 */
                level.tryToCompleteParentCoverage(range);
            }
        } catch (StateSystemDisposedException e) {
            return false;
        }
        return true;
    }

    /**
     * @deprecated Use the {@link IFlameChartProvider}'s segment store instead
     */
    @Override
    @Deprecated
    public void addListener(@NonNull IAnalysisProgressListener listener) {
        fListeners.add(listener);
    }

    /**
     * @deprecated Use the {@link IFlameChartProvider}'s segment store instead
     */
    @Override
    @Deprecated
    public void removeListener(@NonNull IAnalysisProgressListener listener) {
        fListeners.remove(listener);
    }

    @Override
    protected void canceling() {
        // Do nothing
    }

    /**
     * @deprecated Use the {@link IFlameChartProvider}'s segment store instead
     */
    @Override
    @Deprecated
    public @Nullable ISegmentStore<@NonNull ISegment> getSegmentStore() {
        return null;
    }

    /**
     * Merged threadnodes
     *
     * @return the merged threadnodes
     */
    public Collection<ThreadNode> getFlameGraph() {
        AbstractCalledFunction initSegment = CalledFunctionFactory.create(0, 0, -1, "", 0, null); //$NON-NLS-1$
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