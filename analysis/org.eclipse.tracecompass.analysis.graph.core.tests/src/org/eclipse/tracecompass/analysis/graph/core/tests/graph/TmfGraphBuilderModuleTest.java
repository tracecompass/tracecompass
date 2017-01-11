/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.tests.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.analysis.graph.core.building.TmfGraphBuilderModule;
import org.eclipse.tracecompass.analysis.graph.core.tests.Activator;
import org.eclipse.tracecompass.analysis.graph.core.tests.stubs.TestGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.tests.stubs.module.GraphBuilderModuleStub;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStubNs;
import org.junit.Test;

/**
 * Test suite for the {@link TmfGraphBuilderModule} class
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class TmfGraphBuilderModuleTest {

    private static final String STUB_TRACE_FILE = "testfiles/stubtrace.xml";

    /**
     * With this trace, the resulting graph should look like this:
     *
     * <pre>
     *           0  1  2  3  4  5  6  7  8  9  10  11  12  13
     * Player 1     *--*        *-----*                *
     *                 |        |                      |
     * Player 2        *--------*               *------*
     * </pre>
     *
     * @return
     */
    private TmfGraphBuilderModule getModule(TmfTrace trace) {
        trace.traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        GraphBuilderModuleStub module = null;
        for (GraphBuilderModuleStub mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, GraphBuilderModuleStub.class)) {
            module = mod;
        }
        assertNotNull(module);

        return module;
    }

    /**
     * Test the graph builder execution
     */
    @Test
    public void testBuildGraph() {
        TmfXmlTraceStub trace = TmfXmlTraceStubNs.setupTrace(Activator.getAbsoluteFilePath(STUB_TRACE_FILE));

        TmfGraphBuilderModule module = getModule(trace);
        module.schedule();
        module.waitForCompletion();

        TmfGraph graph = module.getGraph();
        assertNotNull(graph);

        assertEquals(2, graph.getWorkers().size());
        assertEquals(9, graph.size());

        List<TmfVertex> vertices = graph.getNodesOf(new TestGraphWorker(1));
        assertEquals(5, vertices.size());

        long timestamps1[] = { 1, 2, 5, 7, 12 };
        boolean hasEdges1[][] = {
                { false, true, false, false },
                { true, false, false, true },
                { false, true, true, false },
                { true, false, false, false},
                { false, false, true, false} };
        for (int i = 0; i < vertices.size(); i++) {
            TmfVertex v = vertices.get(i);
            assertEquals(timestamps1[i], v.getTs());
            assertEquals(hasEdges1[i][0], v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE) != null);
            assertEquals(hasEdges1[i][1], v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE) != null);
            assertEquals(hasEdges1[i][2], v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE) != null);
            assertEquals(hasEdges1[i][3], v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE) != null);
        }

        vertices = graph.getNodesOf(new TestGraphWorker(2));
        assertEquals(4, vertices.size());

        long timestamps2[] = { 2, 5, 10, 12 };
        boolean hasEdges2[][] = {
                { false, true, true, false },
                { true, false, false, true },
                { false, true, false, false },
                { true, false, false, true} };
        for (int i = 0; i < vertices.size(); i++) {
            TmfVertex v = vertices.get(i);
            assertEquals(timestamps2[i], v.getTs());
            assertEquals(hasEdges2[i][0], v.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE) != null);
            assertEquals(hasEdges2[i][1], v.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE) != null);
            assertEquals(hasEdges2[i][2], v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE) != null);
            assertEquals(hasEdges2[i][3], v.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE) != null);
        }

        trace.dispose();
    }

}
