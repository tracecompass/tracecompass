/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.flamegraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.AggregatedCalledFunction;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.internal.analysis.timing.core.callgraph.ThreadNode;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.segment.ISegmentAspect;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Test the CallGraphAnalysis.This creates a virtual state system in each test
 * and tests the aggregation tree returned by the CallGraphAnalysis.
 *
 * @author Sonia Farrah
 *
 */
public class AggregationTreeTest {

    private static final @NonNull String PROCESS_PATH = "Processes";
    private static final @NonNull String THREAD_PATH = "Thread";
    private static final @NonNull String CALLSTACK_PATH = "CallStack";
    private static final String QUARK_0 = "0";
    private static final String QUARK_1 = "1";
    private static final String QUARK_2 = "2";
    private static final String QUARK_3 = "3";
    private static final Integer SMALL_AMOUNT_OF_SEGMENT = 3;
    private static final int LARGE_AMOUNT_OF_SEGMENTS = 1000;
    private static final String @NonNull [] CSP = { CALLSTACK_PATH };
    private static final String @NonNull [] PP = { PROCESS_PATH };
    private static final String @NonNull [] TP = { THREAD_PATH };

    /**
     * This class is used to make the CallGraphAnalysis's method
     * iterateOverStateSystem() visible to test
     */
    private class CGAnalysis extends CallGraphAnalysis {

        private final class HelloAspect implements ISegmentAspect {
            @Override
            public @NonNull String getName() {
                return "aspect name";
            }

            @Override
            public @NonNull String getHelpText() {
                return "aspect help";
            }

            @Override
            public @Nullable Comparator<?> getComparator() {
                return null;
            }

            @Override
            public @Nullable Object resolve(@NonNull ISegment segment) {
                return "Hello";
            }
        }

        @Override
        protected boolean iterateOverStateSystem(ITmfStateSystem ss, String[] threadsPattern, String[] processesPattern, String[] callStackPath, IProgressMonitor monitor) {
            return super.iterateOverStateSystem(ss, threadsPattern, processesPattern, callStackPath, monitor);
        }

        @Override
        public @NonNull Iterable<@NonNull ISegmentAspect> getSegmentAspects() {
            ImmutableList<@NonNull ISegmentAspect> aspectList = ImmutableList.of(new HelloAspect());
            assertNotNull(aspectList);
            return aspectList;
        }

    }

    private static @NonNull ITmfStateSystemBuilder createFixture() {
        IStateHistoryBackend backend = StateHistoryBackendFactory.createInMemoryBackend("Test", 0L);
        return StateSystemFactory.newStateSystem(backend);
    }

    private CGAnalysis fCga;

    /**
     * Test an empty state system.
     */
    @Test
    public void emptyStateSystemTest() {
        ITmfStateSystemBuilder fixture = createFixture();
        fixture.closeHistory(1002);
        CGAnalysis cga = new CGAnalysis();
        assertTrue(cga.iterateOverStateSystem(fixture, TP, PP, CSP, new NullProgressMonitor()));
        List<ThreadNode> threads = cga.getThreadNodes();
        assertNotNull(threads);
        assertEquals("Number of threads found", 0, threads.size());
        cga.dispose();
    }

    /**
     * Test cascade state system. The call stack's structure used in this test
     * is shown below:
     *
     * <pre>
     *  ________
     *   ______
     *    ____
     *
     * </pre>
     */
    @Test
    public void cascadeTest() {
        ITmfStateSystemBuilder fixture = createFixture();
        // Build the state system
        long start = 1;
        long end = 1001;
        int threadQuark = fixture.getQuarkAbsoluteAndAdd(PROCESS_PATH, THREAD_PATH);
        int parentQuark = fixture.getQuarkRelativeAndAdd(threadQuark, CALLSTACK_PATH);
        fixture.updateOngoingState(TmfStateValue.newValueLong(100), threadQuark);
        for (int i = 1; i <= SMALL_AMOUNT_OF_SEGMENT; i++) {
            int quark = fixture.getQuarkRelativeAndAdd(parentQuark, Integer.toString(i));
            TmfStateValue statev = TmfStateValue.newValueLong(i);
            fixture.modifyAttribute(start, TmfStateValue.nullValue(), quark);
            fixture.modifyAttribute(start + i, statev, quark);
            fixture.modifyAttribute(end - i, TmfStateValue.nullValue(), quark);
        }

        fixture.closeHistory(1002);
        // Execute the CallGraphAnalysis
        CGAnalysis cga = new CGAnalysis();
        setCga(cga);
        assertTrue(cga.iterateOverStateSystem(fixture, TP, PP, CSP, new NullProgressMonitor()));
        @NonNull
        List<ThreadNode> threads = cga.getThreadNodes();
        // Test the threads generated by the analysis
        assertNotNull(threads);
        assertEquals("Number of thread nodes Found", 1, threads.size());
        assertEquals("Number of root functions ", 1, threads.get(0).getChildren().size());
        assertEquals("Thread id", 100, threads.get(0).getId());
        assertEquals("Thread name", "Thread", threads.get(0).getSymbol());
        Object[] children = threads.get(0).getChildren().toArray();
        AggregatedCalledFunction firstFunction = (AggregatedCalledFunction) children[0];
        assertEquals("Children number: First function", 1, firstFunction.getChildren().size());
        Object @NonNull [] firstFunctionChildren = firstFunction.getChildren().toArray();
        AggregatedCalledFunction secondFunction = (AggregatedCalledFunction) firstFunctionChildren[0];
        assertEquals("Children number: Second function", 1, secondFunction.getChildren().size());
        Object @NonNull [] secondFunctionChildren = secondFunction.getChildren().toArray();
        AggregatedCalledFunction thirdFunction = (AggregatedCalledFunction) secondFunctionChildren[0];
        assertEquals("Children number: Third function", 0, thirdFunction.getChildren().size());
        // Test links
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(secondFunction.getParent()).getSymbol(), firstFunction.getSymbol());
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(thirdFunction.getParent()).getSymbol(), secondFunction.getSymbol());
        // Test duration
        assertEquals("Test first function's duration", 998, firstFunction.getDuration());
        assertEquals("Test second function's duration", 996, secondFunction.getDuration());
        assertEquals("Test third function's duration", 994, thirdFunction.getDuration());
        // Test self time
        assertEquals("Test first function's self time", 2, firstFunction.getSelfTime());
        assertEquals("Test second function's self time", 2, secondFunction.getSelfTime());
        assertEquals("Test third function's self time", 994, thirdFunction.getSelfTime());
        // Test depth
        assertEquals("Test first function's depth", 0, firstFunction.getDepth());
        assertEquals("Test second function's depth", 1, secondFunction.getDepth());
        assertEquals("Test third function's depth", 2, thirdFunction.getDepth());
        // Test number of calls
        assertEquals("Test first function's nombre of calls", 1, firstFunction.getNbCalls());
        assertEquals("Test second function's nombre of calls", 1, secondFunction.getNbCalls());
        assertEquals("Test third function's nombre of calls", 1, thirdFunction.getNbCalls());
        cga.dispose();
    }

    /**
     * Test a state system with a two calls for the same function. The call
     * stack's structure used in this test is shown below:
     *
     * <pre>
     *                 Aggregated tree
     *  ___ main___      ___ main___
     *   _1_    _1_  =>      _1_
     *   _1_                 _1_
     * </pre>
     */
    @Test
    public void treeTest() {
        ITmfStateSystemBuilder fixture = createFixture();
        // Build the state system
        int threadQuark = fixture.getQuarkAbsoluteAndAdd(PROCESS_PATH, THREAD_PATH);
        int parentQuark = fixture.getQuarkRelativeAndAdd(threadQuark, CALLSTACK_PATH);
        fixture.updateOngoingState(TmfStateValue.newValueDouble(0.001), threadQuark);
        int quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_0);
        TmfStateValue statev = TmfStateValue.newValueLong(0);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(100, TmfStateValue.nullValue(), quark);

        quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_1);
        statev = TmfStateValue.newValueLong(1);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(50, TmfStateValue.nullValue(), quark);
        fixture.modifyAttribute(60, statev, quark);
        fixture.modifyAttribute(90, TmfStateValue.nullValue(), quark);

        quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_2);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(30, TmfStateValue.nullValue(), quark);
        fixture.closeHistory(102);

        // Execute the CallGraphAnalysis
        CGAnalysis cga = new CGAnalysis();
        setCga(cga);
        assertTrue(cga.iterateOverStateSystem(fixture, TP, PP, CSP, new NullProgressMonitor()));
        List<ThreadNode> threads = cga.getThreadNodes();
        // Test the threads generated by the analysis
        assertNotNull(threads);
        assertEquals("Number of thread nodes Found", 1, threads.size());
        assertEquals("Thread id", -1, threads.get(0).getId());
        assertEquals("Thread name", "Thread", threads.get(0).getSymbol());
        Object[] children = threads.get(0).getChildren().toArray();
        AggregatedCalledFunction firstFunction = (AggregatedCalledFunction) children[0];
        assertEquals("Children number: First function", 1, firstFunction.getChildren().size());
        Object[] firstFunctionChildren = firstFunction.getChildren().toArray();
        AggregatedCalledFunction secondFunction = (AggregatedCalledFunction) firstFunctionChildren[0];
        assertEquals("Children number: Second function", 1, secondFunction.getChildren().size());
        Object[] secondFunctionChildren = secondFunction.getChildren().toArray();
        AggregatedCalledFunction thirdFunction = (AggregatedCalledFunction) secondFunctionChildren[0];
        assertEquals("Children number: Third function", 0, thirdFunction.getChildren().size());
        // Test links
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(secondFunction.getParent()).getSymbol(), firstFunction.getSymbol());
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(thirdFunction.getParent()).getSymbol(), secondFunction.getSymbol());
        // Test duration
        assertEquals("Test first function's duration", 100, firstFunction.getDuration());
        assertEquals("Test second function's duration", 80, secondFunction.getDuration());
        assertEquals("Test third function's duration", 30, thirdFunction.getDuration());
        // Test self time
        assertEquals("Test first function's self time", 20, firstFunction.getSelfTime());
        assertEquals("Test second function's self time", 50, secondFunction.getSelfTime());
        assertEquals("Test third function's self time", 30, thirdFunction.getSelfTime());
        // Test depth
        assertEquals("Test first function's depth", 0, firstFunction.getDepth());
        assertEquals("Test second function's depth", 1, secondFunction.getDepth());
        assertEquals("Test third function's depth", 2, thirdFunction.getDepth());
        // Test number of calls
        assertEquals("Test first function's number of calls", 1, firstFunction.getNbCalls());
        assertEquals("Test second function's number of calls", 2, secondFunction.getNbCalls());
        assertEquals("Test third function's number of calls", 1, thirdFunction.getNbCalls());
        cga.dispose();
    }

    /**
     * Test the callees merge. The call stack's structure used in this test is
     * shown below:
     *
     * <pre>
     *                    Aggregated tree
     *  ___ main___        ___ main___
     *   _1_    _1_ =>         _1_
     *   _2_    _3_          _2_ _3_
     * </pre>
     */
    @Test
    public void mergeFirstLevelCalleesTest() {
        ITmfStateSystemBuilder fixture = createFixture();
        // Build the state system
        int threadQuark = fixture.getQuarkAbsoluteAndAdd(PROCESS_PATH, "123");
        int parentQuark = fixture.getQuarkRelativeAndAdd(threadQuark, CALLSTACK_PATH);
        fixture.updateOngoingState(TmfStateValue.newValueDouble(0.001), threadQuark);
        int quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_0);
        TmfStateValue statev = TmfStateValue.newValueLong(0);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(100, TmfStateValue.nullValue(), quark);

        quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_1);
        statev = TmfStateValue.newValueLong(1);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(50, TmfStateValue.nullValue(), quark);
        fixture.modifyAttribute(60, statev, quark);
        fixture.modifyAttribute(90, TmfStateValue.nullValue(), quark);

        quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_2);
        statev = TmfStateValue.newValueLong(2);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(30, TmfStateValue.nullValue(), quark);
        statev = TmfStateValue.newValueLong(3);
        fixture.modifyAttribute(60, statev, quark);
        fixture.modifyAttribute(80, TmfStateValue.nullValue(), quark);
        fixture.closeHistory(102);

        // Execute the CallGraphAnalysis
        CGAnalysis cga = new CGAnalysis();
        String @NonNull [] tp = { "123" };
        assertTrue(cga.iterateOverStateSystem(fixture, tp, PP, CSP, new NullProgressMonitor()));
        setCga(cga);
        List<ThreadNode> threads = cga.getThreadNodes();
        // Test the threads generated by the analysis
        assertNotNull(threads);
        assertEquals("Number of thread nodes Found", 1, threads.size());
        assertEquals("Thread id", 123, threads.get(0).getId());
        assertEquals("Thread name", "123", threads.get(0).getSymbol());
        assertEquals("Number of root functions ", 1, threads.get(0).getChildren().size());
        Object[] children = threads.get(0).getChildren().toArray();

        AggregatedCalledFunction firstFunction = (AggregatedCalledFunction) children[0];
        assertEquals("Children number: First function", 1, firstFunction.getChildren().size());
        Object[] firstFunctionChildren = firstFunction.getChildren().toArray();
        AggregatedCalledFunction secondFunction = (AggregatedCalledFunction) firstFunctionChildren[0];
        assertEquals("Children number: Second function", 2, secondFunction.getChildren().size());
        Object[] secondFunctionChildren = secondFunction.getChildren().toArray();
        AggregatedCalledFunction leaf1 = (AggregatedCalledFunction) secondFunctionChildren[0];
        AggregatedCalledFunction leaf2 = (AggregatedCalledFunction) secondFunctionChildren[1];
        assertEquals("Children number: First leaf function", 0, leaf1.getChildren().size());
        assertEquals("Children number: Second leaf function", 0, leaf2.getChildren().size());
        // Test links
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(secondFunction.getParent()).getSymbol(), firstFunction.getSymbol());
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(leaf1.getParent()).getSymbol(), secondFunction.getSymbol());
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(leaf2.getParent()).getSymbol(), secondFunction.getSymbol());
        // Test duration
        assertEquals("Test first function's duration", 100, firstFunction.getDuration());
        assertEquals("Test second function's duration", 80, secondFunction.getDuration());
        assertEquals("Test first leaf's duration", 30, leaf1.getDuration());
        assertEquals("Test second leaf's duration", 20, leaf2.getDuration());
        // Test self time
        assertEquals("Test first function's self time", 20, firstFunction.getSelfTime());
        assertEquals("Test second function's self time", 30, secondFunction.getSelfTime());
        assertEquals("Test first leaf's self time", 30, leaf1.getSelfTime());
        assertEquals("Test second leaf's self time", 20, leaf2.getSelfTime());
        // Test depth
        assertEquals("Test first function's depth", 0, firstFunction.getDepth());
        assertEquals("Test second function's depth", 1, secondFunction.getDepth());
        assertEquals("Test first leaf's depth", 2, leaf1.getDepth());
        assertEquals("Test second leaf's depth", 2, leaf2.getDepth());
        // Test number of calls
        assertEquals("Test first function's number of calls", 1, firstFunction.getNbCalls());
        assertEquals("Test second function's number of calls", 2, secondFunction.getNbCalls());
        assertEquals("Test first leaf's number of calls", 1, leaf1.getNbCalls());
        assertEquals("Test second leaf's number of calls", 1, leaf2.getNbCalls());
        cga.dispose();
    }

    /**
     * Build a call stack example.This call stack's structure is shown below :
     *
     * <pre>
     *  ___ main____
     *  ___1___    _1_
     *  _2_ _3_    _2_
     *  _4_        _4_
     * </pre>
     */
    private static void buildCallStack(ITmfStateSystemBuilder fixture) {
        int parentQuark = fixture.getQuarkAbsoluteAndAdd(PROCESS_PATH, THREAD_PATH, CALLSTACK_PATH);
        // Create the first function
        int quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_0);
        TmfStateValue statev = TmfStateValue.newValueLong(0);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(100, TmfStateValue.nullValue(), quark);
        // Create the first level functions
        quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_1);
        statev = TmfStateValue.newValueLong(1);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(50, TmfStateValue.nullValue(), quark);
        fixture.modifyAttribute(60, statev, quark);
        fixture.modifyAttribute(100, TmfStateValue.nullValue(), quark);
        // Create the third function
        quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_2);
        statev = TmfStateValue.newValueLong(2);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(10, TmfStateValue.nullValue(), quark);

        statev = TmfStateValue.newValueLong(3);
        fixture.modifyAttribute(20, statev, quark);
        fixture.modifyAttribute(30, TmfStateValue.nullValue(), quark);

        statev = TmfStateValue.newValueLong(2);
        fixture.modifyAttribute(60, statev, quark);
        fixture.modifyAttribute(90, TmfStateValue.nullValue(), quark);

        quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_3);
        statev = TmfStateValue.newValueLong(4);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(10, TmfStateValue.nullValue(), quark);

        fixture.modifyAttribute(60, statev, quark);
        fixture.modifyAttribute(80, TmfStateValue.nullValue(), quark);
        fixture.closeHistory(102);
    }

    /**
     * Test the merge of The callees children. The call stack's structure used
     * in this test is shown below:
     *
     * <pre>
     *                      Aggregated tree
     *   ___ main____        ____ main____
     *  ___1___    _1_            _1_
     *  _2_ _3_    _2_  =>      _2_ _3_
     *  _4_        _4_          _4_
     * </pre>
     */
    @Test
    public void mergeSecondLevelCalleesTest() {
        ITmfStateSystemBuilder fixture = createFixture();
        buildCallStack(fixture);
        // Execute the CallGraphAnalysis
        CGAnalysis cga = new CGAnalysis();
        setCga(cga);
        assertTrue(cga.iterateOverStateSystem(fixture, TP, PP, CSP, new NullProgressMonitor()));
        List<ThreadNode> threads = cga.getThreadNodes();
        // Test the threads generated by the analysis
        assertNotNull(threads);
        assertEquals("Number of thread nodes Found", 1, threads.size());
        assertEquals("Thread id", -1, threads.get(0).getId());
        assertEquals("Thread name", "Thread", threads.get(0).getSymbol());
        assertEquals("Number of root functions ", 1, threads.get(0).getChildren().size());
        Object[] children = threads.get(0).getChildren().toArray();
        AggregatedCalledFunction main = (AggregatedCalledFunction) children[0];
        assertEquals("Children number: main", 1, main.getChildren().size());
        Object[] mainChildren = main.getChildren().toArray();
        AggregatedCalledFunction function1 = (AggregatedCalledFunction) mainChildren[0];
        assertEquals("Children number: first function", 2, function1.getChildren().size());
        Object[] firstFunctionChildren = function1.getChildren().toArray();
        AggregatedCalledFunction function2 = (AggregatedCalledFunction) firstFunctionChildren[0];
        AggregatedCalledFunction function3 = (AggregatedCalledFunction) firstFunctionChildren[1];
        assertEquals("Children number: First child", 1, function2.getChildren().size());
        assertEquals("Children number: Second child", 0, function3.getChildren().size());
        Object[] firstChildCallee = function2.getChildren().toArray();
        AggregatedCalledFunction function4 = (AggregatedCalledFunction) firstChildCallee[0];
        assertEquals("Children number: leaf function", 0, function4.getChildren().size());
        // Test links
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(function1.getParent()).getSymbol(), main.getSymbol());
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(function2.getParent()).getSymbol(), function1.getSymbol());
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(function3.getParent()).getSymbol(), function1.getSymbol());
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(function4.getParent()).getSymbol(), function2.getSymbol());
        // Test duration
        assertEquals("Test main's duration", 100, main.getDuration());
        assertEquals("Test first function's duration", 90, function1.getDuration());
        assertEquals("Test first child's duration", 40, function2.getDuration());
        assertEquals("Test second child's duration", 10, function3.getDuration());
        assertEquals("Test leaf's duration", 30, function4.getDuration());
        // Test self time
        assertEquals("Test main's self time", 10, main.getSelfTime());
        assertEquals("Test first function's self time", 40, function1.getSelfTime());
        assertEquals("Test first child's self time", 10,
                function2.getSelfTime());
        assertEquals("Test second child's self time", 10, function3.getSelfTime());
        assertEquals("Test leaf's self time", 30, function4.getSelfTime());
        // Test depth
        assertEquals("Test main function's depth", 0, main.getDepth());
        assertEquals("Test first function's depth", 1, function1.getDepth());
        assertEquals("Test first child's depth", 2, function2.getDepth());
        assertEquals("Test second child's depth", 2, function3.getDepth());
        assertEquals("Test leaf's depth", 3, function4.getDepth());
        // Test number of calls
        assertEquals("Test main's number of calls", 1, main.getNbCalls());
        assertEquals("Test first function's number of calls", 2, function1.getNbCalls());
        assertEquals("Test first child's number of calls", 2, function2.getNbCalls());
        assertEquals("Test second child's number of calls", 1, function3.getNbCalls());
        assertEquals("Test leaf's number of calls", 2, function4.getNbCalls());
        cga.dispose();
    }

    /**
     * Test state system with a large amount of segments. All segments have the
     * same length. The call stack's structure used in this test is shown below:
     *
     * <pre>
     * _____
     * _____
     * _____
     * .....
     * </pre>
     */
    @Test
    public void largeTest() {
        ITmfStateSystemBuilder fixture = createFixture();
        int parentQuark = fixture.getQuarkAbsoluteAndAdd(PROCESS_PATH, THREAD_PATH, CALLSTACK_PATH);
        for (int i = 0; i < LARGE_AMOUNT_OF_SEGMENTS; i++) {
            TmfStateValue statev = TmfStateValue.newValueLong(i);
            fixture.pushAttribute(0, statev, parentQuark);
        }
        for (int i = 0; i < LARGE_AMOUNT_OF_SEGMENTS; i++) {
            fixture.popAttribute(10, parentQuark);
        }
        fixture.closeHistory(11);
        // Execute the callGraphAnalysis
        CGAnalysis cga = new CGAnalysis();
        setCga(cga);
        assertTrue(cga.iterateOverStateSystem(fixture, TP, PP, CSP, new NullProgressMonitor()));
        List<ThreadNode> threads = cga.getThreadNodes();
        // Test the threads generated by the analysis
        assertNotNull(threads);
        assertEquals("Thread id", -1, threads.get(0).getId());
        assertEquals("Thread name", "Thread", threads.get(0).getSymbol());
        Object[] children = threads.get(0).getChildren().toArray();
        AggregatedCalledFunction parent = (AggregatedCalledFunction) children[0];
        for (int i = 1; i < LARGE_AMOUNT_OF_SEGMENTS; i++) {
            children = parent.getChildren().toArray();
            AggregatedCalledFunction child = (AggregatedCalledFunction) children[0];
            assertEquals("Test parenthood", NonNullUtils.checkNotNull(child.getParent()).getSymbol(), NonNullUtils.checkNotNull(parent.getSymbol()));
            parent = child;
        }
        cga.dispose();
    }

    /**
     * Test mutliRoots state system.This tests if a root function called twice
     * will be merged into one function or not. The call stack's structure used
     * in this test is shown below:
     *
     * <pre>
     *              Aggregated tree
     * _1_  _1_  =>    _1_
     * _2_  _3_      _2_ _3_
     * </pre>
     */
    @Test
    public void multiFunctionRootsTest() {
        ITmfStateSystemBuilder fixture = createFixture();
        int parentQuark = fixture.getQuarkAbsoluteAndAdd(PROCESS_PATH, THREAD_PATH, CALLSTACK_PATH);
        // Create the first root function
        int quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_0);
        TmfStateValue statev = TmfStateValue.newValueLong(1);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(20, TmfStateValue.nullValue(), quark);
        // Create the second root function
        fixture.modifyAttribute(30, statev, quark);
        fixture.modifyAttribute(50, TmfStateValue.nullValue(), quark);
        // Create the first root function's callee
        quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_1);
        statev = TmfStateValue.newValueLong(2);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(10, TmfStateValue.nullValue(), quark);
        // Create the second root function's callee
        statev = TmfStateValue.newValueLong(3);
        fixture.modifyAttribute(30, statev, quark);
        fixture.modifyAttribute(40, TmfStateValue.nullValue(), quark);
        fixture.closeHistory(51);

        // Execute the callGraphAnalysis
        CGAnalysis cga = new CGAnalysis();
        setCga(cga);
        assertTrue(cga.iterateOverStateSystem(fixture, TP, PP, CSP, new NullProgressMonitor()));
        List<ThreadNode> threads = cga.getThreadNodes();
        // Test the threads generated by the analysis
        assertNotNull(threads);
        assertEquals("Number of thread nodes Found", 1, threads.size());
        assertEquals("Thread id", -1, threads.get(0).getId());
        assertEquals("Thread name", "Thread", threads.get(0).getSymbol());
        assertEquals("Number of root functions ", 1, threads.get(0).getChildren().size());
        Object[] children = threads.get(0).getChildren().toArray();
        AggregatedCalledFunction firstFunction = (AggregatedCalledFunction) children[0];
        assertEquals("Children number: First function", 2, firstFunction.getChildren().size());
        Object[] firstFunctionChildren = firstFunction.getChildren().toArray();
        AggregatedCalledFunction function2 = (AggregatedCalledFunction) firstFunctionChildren[0];
        AggregatedCalledFunction function3 = (AggregatedCalledFunction) firstFunctionChildren[1];
        assertEquals("Children number: Second function", 0, function2.getChildren().size());
        assertEquals("Children number: Third function", 0, function3.getChildren().size());
        // Test links
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(function2.getParent()).getSymbol(), firstFunction.getSymbol());
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(function3.getParent()).getSymbol(), firstFunction.getSymbol());
        // Test duration
        assertEquals("Test first function's duration", 40, firstFunction.getDuration());
        assertEquals("Test second function's duration", 10, function2.getDuration());
        assertEquals("Test third function's duration", 10, function3.getDuration());
        // Test self time
        assertEquals("Test first function's self time", 20, firstFunction.getSelfTime());
        assertEquals("Test second function's self time", 10, function2.getSelfTime());
        assertEquals("Test third function's self time", 10, function2.getSelfTime());
        // Test depth
        assertEquals("Test first function's depth", 0, firstFunction.getDepth());
        assertEquals("Test second function's depth", 1, function2.getDepth());
        assertEquals("Test third function's depth", 1, function3.getDepth());
        // Test number of calls
        assertEquals("Test first function's number of calls", 2, firstFunction.getNbCalls());
        assertEquals("Test second function's number of calls", 1, function2.getNbCalls());
        assertEquals("Test third function's number of calls", 1, function3.getNbCalls());
        cga.dispose();
    }

    /**
     * Test mutliRoots state system. The call stack's structure used in this
     * test is shown below:
     *
     * <pre>
     *                Aggregated tree
     * _0_  _1_   =>   _0_  _1_
     * _2_  _2_        _2_  _2_
     *
     * </pre>
     */
    @Test
    public void multiFunctionRootsSecondTest() {
        ITmfStateSystemBuilder fixture = createFixture();
        int parentQuark = fixture.getQuarkAbsoluteAndAdd(PROCESS_PATH, THREAD_PATH, CALLSTACK_PATH);
        // Create the first root function
        int quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_0);
        TmfStateValue statev = TmfStateValue.newValueLong(0);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(20, TmfStateValue.nullValue(), quark);
        // Create the second root function
        statev = TmfStateValue.newValueLong(1);
        fixture.modifyAttribute(30, statev, quark);
        fixture.modifyAttribute(50, TmfStateValue.nullValue(), quark);
        // Create the first root function's callee
        quark = fixture.getQuarkRelativeAndAdd(parentQuark, QUARK_1);
        statev = TmfStateValue.newValueLong(2);
        fixture.modifyAttribute(0, statev, quark);
        fixture.modifyAttribute(10, TmfStateValue.nullValue(), quark);
        // Create the second root function's callee
        fixture.modifyAttribute(30, statev, quark);
        fixture.modifyAttribute(40, TmfStateValue.nullValue(), quark);
        fixture.closeHistory(51);

        // Execute the callGraphAnalysis
        CGAnalysis cga = new CGAnalysis();
        setCga(cga);
        assertTrue(cga.iterateOverStateSystem(fixture, TP, PP, CSP, new NullProgressMonitor()));
        List<ThreadNode> threads = cga.getThreadNodes();
        // Test the threads generated by the analysis
        assertNotNull(threads);
        assertEquals("Number of thread nodes Found", 1, threads.size());
        assertEquals("Thread id", -1, threads.get(0).getId());
        assertEquals("Thread name", "Thread", threads.get(0).getSymbol());
        assertEquals("Number of root functions ", 2, threads.get(0).getChildren().size());
        Object[] children = threads.get(0).getChildren().toArray();
        AggregatedCalledFunction firstFunction = (AggregatedCalledFunction) children[0];
        AggregatedCalledFunction secondFunction = (AggregatedCalledFunction) children[1];

        assertEquals("Children number: First function", 1, firstFunction.getChildren().size());
        assertEquals("Children number: Second function", 1, secondFunction.getChildren().size());
        Object[] firstFunctionChildren = firstFunction.getChildren().toArray();
        Object[] secondFunctionChildren = secondFunction.getChildren().toArray();
        AggregatedCalledFunction function3 = (AggregatedCalledFunction) firstFunctionChildren[0];
        AggregatedCalledFunction function4 = (AggregatedCalledFunction) secondFunctionChildren[0];

        assertEquals("Children number: third function", 0, function3.getChildren().size());
        assertEquals("Children number: fourth function", 0, function4.getChildren().size());
        // Test links
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(function3.getParent()).getSymbol(), firstFunction.getSymbol());
        assertEquals("Test parenthood ", NonNullUtils.checkNotNull(function4.getParent()).getSymbol(), secondFunction.getSymbol());
        // Test duration
        assertEquals("Test second function's duration", 20, firstFunction.getDuration());
        assertEquals("Test second function's duration", 20, secondFunction.getDuration());
        assertEquals("Test first leaf's duration", 10, function3.getDuration());
        assertEquals("Test second leaf's duration", 10, function4.getDuration());
        // Test self time
        assertEquals("Test first function's self time", 10, firstFunction.getSelfTime());
        assertEquals("Test second function's duration", 10, secondFunction.getSelfTime());
        assertEquals("Test second function's self time", 10, function3.getSelfTime());
        assertEquals("Test second function's self time", 10, function4.getSelfTime());
        // Test depth
        assertEquals("Test first function's depth", 0, firstFunction.getDepth());
        assertEquals("Test first function's depth", 0, secondFunction.getDepth());
        assertEquals("Test third function's depth", 1, function3.getDepth());
        assertEquals("Test third function's depth", 1, function4.getDepth());
        // Test number of calls
        assertEquals("Test first function's number of calls", 1, firstFunction.getNbCalls());
        assertEquals("Test first function's number of calls", 1, secondFunction.getNbCalls());
        assertEquals("Test third function's number of calls", 1, function3.getNbCalls());
        assertEquals("Test third function's number of calls", 1, function4.getNbCalls());
        cga.dispose();
    }

    /**
     * Gets the call graph analysis
     *
     * @return the call graph analysis
     */
    protected CGAnalysis getCga() {
        return fCga;
    }

    /**
     * Set the callgraphanalysis
     *
     * @param cga
     */
    protected void setCga(CGAnalysis cga) {
        fCga = cga;
    }
}
