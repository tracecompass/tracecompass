/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.analysis.graph.core.building.TmfGraphBuilderModule;
import org.eclipse.tracecompass.analysis.os.linux.core.execution.graph.OsWorker;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.Activator;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.Test;

/**
 * Test that the execution graph is built correctly
 *
 * @author Geneviève Bastien
 */
public class LttngExecutionGraphTest {

    private static final @NonNull String TEST_ANALYSIS_ID = "org.eclipse.tracecompass.analysis.os.linux.execgraph";

    /**
     * Setup the trace for the tests
     *
     * @param traceFile
     *            File name relative to this plugin for the trace file to load
     * @return The trace with its graph module executed
     */
    public ITmfTrace setUpTrace(String traceFile) {
        TmfXmlKernelTraceStub trace = new TmfXmlKernelTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(traceFile);
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
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, TmfGraphBuilderModule.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        return trace;
    }

    /**
     * Test the graph building with sched events only
     *
     * TODO: Add wakeup events to this test case
     */
    @Test
    public void testSchedEvents() {
        ITmfTrace trace = setUpTrace("testfiles/graph/sched_only.xml");
        assertNotNull(trace);

        TmfGraphBuilderModule module = TmfTraceUtils.getAnalysisModuleOfClass(trace, TmfGraphBuilderModule.class, TEST_ANALYSIS_ID);
        assertNotNull(module);
        module.schedule();
        assertTrue(module.waitForCompletion());

        TmfGraph graph = module.getGraph();
        assertNotNull(graph);

        Set<IGraphWorker> workers = graph.getWorkers();
        assertEquals(2, workers.size());
        for (IGraphWorker worker: workers) {
            assertTrue(worker instanceof OsWorker);
            OsWorker lttngWorker = (OsWorker) worker;
            switch(lttngWorker.getHostThread().getTid()) {
            case 1:
            {
                List<TmfVertex> nodesOf = graph.getNodesOf(lttngWorker);
                assertEquals(4, nodesOf.size());
                /* Check first vertice has outgoing edge preempted */
                TmfVertex v = nodesOf.get(0);
                assertEquals(10, v.getTs());
                assertNull(v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
                TmfEdge edge = v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
                assertNotNull(edge);
                assertEquals(EdgeType.PREEMPTED, edge.getType());
                v = nodesOf.get(1);
                assertEquals(v, edge.getVertexTo());

                /* Check second vertice has outgoing edge running */
                assertEquals(20, v.getTs());
                assertNull(v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
                assertNotNull(v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE));
                edge = v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
                assertNotNull(edge);
                assertEquals(EdgeType.RUNNING, edge.getType());
                v = nodesOf.get(2);
                assertEquals(v, edge.getVertexTo());

                /* Check third vertice has outgoing edge preempted */
                assertEquals(30, v.getTs());
                assertNull(v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
                assertNotNull(v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE));
                edge = v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
                assertNotNull(edge);
                assertEquals(EdgeType.PREEMPTED, edge.getType());
                v = nodesOf.get(3);
                assertEquals(v, edge.getVertexTo());

                /* Check 4th vertice */
                assertEquals(40, v.getTs());
                assertNull(v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
                assertNotNull(v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE));
            }
                break;
            case 2:
            {
                List<TmfVertex> nodesOf = graph.getNodesOf(lttngWorker);
                assertEquals(4, nodesOf.size());
                /* Check first vertice has outgoing edge preempted */
                TmfVertex v = nodesOf.get(0);
                assertEquals(10, v.getTs());
                assertNull(v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
                TmfEdge edge = v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
                assertNotNull(edge);
                assertEquals(EdgeType.RUNNING, edge.getType());
                v = nodesOf.get(1);
                assertEquals(v, edge.getVertexTo());

                /* Check second vertice has outgoing edge running */
                assertEquals(20, v.getTs());
                assertNull(v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
                assertNotNull(v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE));
                edge = v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
                assertNotNull(edge);
                assertEquals(EdgeType.BLOCKED, edge.getType());
                v = nodesOf.get(2);
                assertEquals(v, edge.getVertexTo());

                /* Check third vertice has outgoing edge preempted */
                assertEquals(30, v.getTs());
                assertNull(v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
                assertNotNull(v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE));
                edge = v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
                assertNotNull(edge);
                assertEquals(EdgeType.RUNNING, edge.getType());
                v = nodesOf.get(3);
                assertEquals(v, edge.getVertexTo());

                /* Check 4th vertice */
                assertEquals(40, v.getTs());
                assertNull(v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
                assertNotNull(v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE));
                assertNull(v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE));
            }
                break;
            default:
                fail("Unknown worker");
                break;
            }
        }
        trace.dispose();
    }
}
