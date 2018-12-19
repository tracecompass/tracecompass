/*******************************************************************************
 * Copyright (c) 2018 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.tests;

import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.profiling.core.callstack.CallStackAnalysis;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.IntervalInfo;
import org.eclipse.tracecompass.statesystem.core.tests.shared.utils.StateIntervalStub;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStubNs;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;

/**
 * Get the data for all tests using the callstack.xml test files. This class is
 * the single place that should be updated whenever there is change to the test
 * trace data.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class CallStackXmlData {

    private static final String CALLSTACK_FILE = "testfiles/traces/callstack.xml";

    private static final List<ExpectedCallStackElement> CALLSTACK_RAW_DATA = new ArrayList<>();
    private static final long START = 1L;
    private static final long END = 20L;

    static {
        // Prepare pid 1, tid 2
        CALLSTACK_RAW_DATA.add(new ExpectedCallStackElement(1, 2, ImmutableList.of(
                new ExpectedFunction(1, 10, "op1", ImmutableList.of(
                        new ExpectedFunction(3, 7, "op2", ImmutableList.of(
                                new ExpectedFunction(4, 5, "op3", Collections.emptyList()))))),
                new ExpectedFunction(12, 20, "op4", Collections.emptyList()))));
        // Prepare pid 1, tid 3
        CALLSTACK_RAW_DATA.add(new ExpectedCallStackElement(1, 3, ImmutableList.of(
                new ExpectedFunction(3, 20, "op2", ImmutableList.of(
                        new ExpectedFunction(5, 6, "op3", Collections.emptyList()),
                        new ExpectedFunction(7, 13, "op2", Collections.emptyList()))))));
        // Prepare pid 5, tid 6
        CALLSTACK_RAW_DATA.add(new ExpectedCallStackElement(5, 6, ImmutableList.of(
                new ExpectedFunction(1, 20, "op1", ImmutableList.of(
                        new ExpectedFunction(2, 7, "op3", ImmutableList.of(
                                new ExpectedFunction(4, 6, "op1", Collections.emptyList()))),
                        new ExpectedFunction(8, 11, "op2", ImmutableList.of(
                                new ExpectedFunction(9, 10, "op3", Collections.emptyList()))),
                        new ExpectedFunction(12, 20, "op4", Collections.emptyList()))))));
        // Prepare pid 5, tid 7
        CALLSTACK_RAW_DATA.add(new ExpectedCallStackElement(5, 7, ImmutableList.of(
                new ExpectedFunction(1, 20, "op5", ImmutableList.of(
                        new ExpectedFunction(2, 6, "op2", Collections.emptyList()),
                        new ExpectedFunction(9, 13, "op2", ImmutableList.of(
                                new ExpectedFunction(10, 11, "op3", Collections.emptyList()))),
                        new ExpectedFunction(15, 19, "op2", Collections.emptyList()))))));
    }

    private @Nullable TmfXmlTraceStub fTrace;

    private static class ExpectedCallStackElement {
        private final int fPid;
        private final int fTid;
        private final Collection<ExpectedFunction> fCalls;

        private ExpectedCallStackElement(int pid, int tid, Collection<ExpectedFunction> calls) {
            fPid = pid;
            fTid = tid;
            fCalls = calls;
        }
    }

    /**
     * Get the expected data for this callstack. This data is hand-made. Various
     * additional methods will make use of it to transform it in expected flame
     * chart and call graph data
     *
     * @author Geneviève Bastien
     */
    private static class ExpectedFunction {

        private int fStart;
        private int fEnd;
        private String fFunction;
        private List<ExpectedFunction> fChildren;

        private ExpectedFunction(int start, int end, String function, List<ExpectedFunction> children) {
            fStart = start;
            fEnd = end;
            fFunction = function;
            fChildren = children;
        }

        public long getDuration() {
            return fEnd - fStart;
        }

        public long getSelfTime() {
            // Remove the length of all children
            long self = fEnd - fStart;
            for (ExpectedFunction child : fChildren) {
                self -= child.getDuration();
            }
            return self;
        }

    }

    /**
     * Get the test trace
     *
     * @return The test trace, initialized
     */
    public ITmfTrace getTrace() {
        TmfXmlTraceStub trace = fTrace;
        if (trace == null) {
            trace = new TmfXmlTraceStubNs();
            IPath filePath = ActivatorTest.getAbsoluteFilePath(CALLSTACK_FILE);
            IStatus status = trace.validate(null, filePath.toOSString());
            if (!status.isOK()) {
                fail(status.getException().getMessage());
            }
            try {
                trace.initTrace(null, filePath.toOSString(), TmfEvent.class);
            } catch (TmfTraceException e) {
                fail(e.getMessage());
            }
            trace.traceOpened(new TmfTraceOpenedSignal(this, trace, null));
            fTrace = trace;
        }
        return trace;
    }

    /**
     * Dispose of the trace
     */
    public void dispose() {
        TmfXmlTraceStub trace = fTrace;
        if (trace != null) {
            trace.dispose();
        }
    }

    /**
     * Convert the callstack to the state system content
     *
     * @param processPattern
     *            The process string to group the elements
     * @return The list of interval infos, ie attribute paths and their
     *         corresponding intervals, that should be in the state system
     */
    public Set<IntervalInfo> toStateSystemInterval(String processPattern) {
        Set<IntervalInfo> intervals = new HashSet<>();
        for (ExpectedCallStackElement expected : CALLSTACK_RAW_DATA) {
            Multimap<@NonNull Integer, @NonNull ITmfStateInterval> intervalsByDepth = Objects.requireNonNull(LinkedHashMultimap.create());
            Map<Integer, ITmfStateInterval> lastIntervals = new HashMap<>();
            for (ExpectedFunction expFunction : expected.fCalls) {
                createIntervalsRecursive(expFunction, intervalsByDepth, lastIntervals, 1);
            }
            // Add null intervals at the end if necessary, then create the
            // interval info for this depth
            for (Entry<Integer, ITmfStateInterval> lastInterval : lastIntervals.entrySet()) {
                if (lastInterval.getValue().getEndTime() < END) {
                    intervalsByDepth.put(lastInterval.getKey(), new StateIntervalStub((int) lastInterval.getValue().getEndTime() + 1, (int) END, (Object) null));
                }
                intervals.add(new IntervalInfo(new ArrayList<>(intervalsByDepth.get(lastInterval.getKey())),
                        processPattern, String.valueOf(expected.fPid),
                        String.valueOf(expected.fTid),
                        CallStackAnalysis.CALL_STACK, String.valueOf(lastInterval.getKey())));
            }
        }
        return intervals;
    }

    private void createIntervalsRecursive(ExpectedFunction expFunction, Multimap<Integer, ITmfStateInterval> intervals, Map<Integer, ITmfStateInterval> lastIntervals, int depth) {
        // Get last interval at this depth
        ITmfStateInterval lastAtDepth = lastIntervals.get(depth);
        long start = lastAtDepth == null ? START : lastAtDepth.getEndTime() + 1;

        // Do we need a null interval before next one to fill the gap?
        if (start < expFunction.fStart) {
            intervals.put(depth, new StateIntervalStub((int) start, expFunction.fStart - 1, (Object) null));
        }

        // Add the new interval
        StateIntervalStub newInterval = new StateIntervalStub(expFunction.fStart, expFunction.fEnd - 1, expFunction.fFunction);
        intervals.put(depth, newInterval);
        lastIntervals.put(depth, newInterval);

        // Recursively visit the children
        for (ExpectedFunction expChild : expFunction.fChildren) {
            createIntervalsRecursive(expChild, intervals, lastIntervals, depth + 1);
        }
    }

    /**
     * A data structure for unit tests that represents aggregated call graph
     * data
     */
    public static class AggregateData {

        private long fDuration = 0;
        private long fSelfTime = 0;
        private int fNbCalls = 0;
        private Map<String, AggregateData> fChildren = new HashMap<>();

        private void addCall(ExpectedFunction expFunction) {
            fDuration += expFunction.getDuration();
            fSelfTime += expFunction.getSelfTime();
            fNbCalls++;
        }

        private void addChildren(Map<String, AggregateData> children) {
            for (Entry<String, AggregateData> child : children.entrySet()) {
                AggregateData aggregateData = fChildren.get(child.getKey());
                AggregateData childData = child.getValue();
                if (aggregateData == null) {
                    fChildren.put(child.getKey(), childData);
                } else {
                    aggregateData.fDuration += childData.fDuration;
                    aggregateData.fSelfTime += childData.fSelfTime;
                    aggregateData.fNbCalls += childData.fNbCalls;
                    aggregateData.addChildren(childData.fChildren);
                }
            }
        }

        /**
         * Get the duration of this aggregated function
         *
         * @return The duration
         */
        public long getDuration() {
            return fDuration;
        }

        /**
         * Get the self time of this aggregated function
         *
         * @return The self time
         */
        public long getSelfTime() {
            return fSelfTime;
        }

        /**
         * Get the number of times this function is called
         *
         * @return Number of calls
         */
        public int getNbCalls() {
            return fNbCalls;
        }

        /**
         * Get the children of this function, where the key is the symbol
         *
         * @return The children
         */
        public Map<String, AggregateData> getChildren() {
            return fChildren;
        }

    }

    /**
     * Get the expected callgraph, grouped per thread. The key of the map is the
     * thread ID
     *
     * @return The expected callgraph
     */
    public Map<Integer, Map<String, AggregateData>> getExpectedCallGraph() {
        Map<Integer, Map<String, AggregateData>> callgraph = new HashMap<>();
        for (ExpectedCallStackElement expected : CALLSTACK_RAW_DATA) {
            Map<String, AggregateData> aggregate = new HashMap<>();
            for (ExpectedFunction expFunction : expected.fCalls) {
                createCallGraphRecursive(expFunction, aggregate);
            }
            callgraph.put(expected.fTid, aggregate);
        }
        return callgraph;
    }

    private void createCallGraphRecursive(ExpectedFunction expFunction, Map<String, AggregateData> aggregate) {
        AggregateData aggregateData = aggregate.get(expFunction.fFunction);
        if (aggregateData == null) {
            aggregateData = new AggregateData();
            aggregate.put(expFunction.fFunction, aggregateData);
        }
        aggregateData.addCall(expFunction);

        // Recursively visit the children
        Map<String, AggregateData> childAggregate = new HashMap<>();
        for (ExpectedFunction expChild : expFunction.fChildren) {
            createCallGraphRecursive(expChild, childAggregate);
        }
        aggregateData.addChildren(childAggregate);
    }

}
