/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.tests.callgraph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.profiling.core.callgraph.ICallGraphProvider;
import org.eclipse.tracecompass.analysis.profiling.core.tests.CallStackTestBase;
import org.eclipse.tracecompass.analysis.profiling.core.tests.CallStackXmlData.AggregateData;
import org.eclipse.tracecompass.analysis.profiling.core.tests.stubs.CallStackAnalysisStub;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.AggregatedCalledFunction;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.CallGraphAnalysis;
import org.eclipse.tracecompass.internal.analysis.profiling.core.callgraph.ThreadNode;
import org.junit.Test;

/**
 * Test the callgraph analysis with the call stack trace and module
 *
 * @author Geneviève Bastien
 */
public class CallGraphWithCallStackAnalysisTest extends CallStackTestBase {

    /**
     * Test the callgraph with a small trace
     */
    @Test
    public void testCallGraph() {
        CallStackAnalysisStub cga = getModule();
        ICallGraphProvider cg = cga.getCallGraph();

        assertTrue(cg instanceof CallGraphAnalysis);
        CallGraphAnalysis callgraph = (CallGraphAnalysis) cg;
        callgraph.schedule();
        assertTrue(callgraph.waitForCompletion());

        List<@NonNull ThreadNode> threadNodes = callgraph.getThreadNodes();
        assertFalse(threadNodes.isEmpty());

        Map<Integer, Map<String, AggregateData>> expected = getTraceData().getExpectedCallGraph();
        assertEquals("Number of threads", expected.size(), threadNodes.size());
        for (ThreadNode threadNode : threadNodes) {
            Map<String, AggregateData> expectedCg = expected.get((int) threadNode.getId());
            assertNotNull(expectedCg);

            compareCallGraphs(expectedCg, threadNode.getChildren(), 0);
        }

    }

    private static void compareCallGraphs(Map<String, AggregateData> expected, Collection<AggregatedCalledFunction> actual, int depth) {
        for (AggregatedCalledFunction function : actual) {
            AggregateData aggregateData = expected.get(function.getSymbol());
            assertNotNull("Unexpected function at depth " + depth + ": " + function.getSymbol(), aggregateData);
            assertEquals("Duration for " + function.getSymbol() + " at depth " + depth, aggregateData.getDuration(), function.getDuration());
            assertEquals("Self time for " + function.getSymbol() + " at depth " + depth, aggregateData.getSelfTime(), function.getSelfTime());
            assertEquals("Nb calls for " + function.getSymbol() + " at depth " + depth, aggregateData.getNbCalls(), function.getNbCalls());
            // Compare the children
            compareCallGraphs(aggregateData.getChildren(), function.getChildren(), depth + 1);
        }
        // The previous loop will have detected any extra function in the
        // actual. Now make sure there is no missing one
        assertEquals("Total number of calls", expected.size(), actual.size());
    }

}
