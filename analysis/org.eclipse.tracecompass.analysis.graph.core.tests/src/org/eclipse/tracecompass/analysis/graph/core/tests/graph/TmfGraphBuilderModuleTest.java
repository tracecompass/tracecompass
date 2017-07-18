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
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.analysis.graph.core.building.AbstractTraceEventHandler;
import org.eclipse.tracecompass.analysis.graph.core.building.ITraceEventHandler;
import org.eclipse.tracecompass.analysis.graph.core.building.TmfGraphBuilderModule;
import org.eclipse.tracecompass.analysis.graph.core.tests.Activator;
import org.eclipse.tracecompass.analysis.graph.core.tests.stubs.TestGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.tests.stubs.module.GraphBuilderModuleStub;
import org.eclipse.tracecompass.analysis.graph.core.tests.stubs.module.GraphProviderStub;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
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
    private GraphBuilderModuleStub getModule(TmfTrace trace) {
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

    private class TestEventHandler extends AbstractTraceEventHandler {
        public TestEventHandler(int priority) {
            super(priority);
        }

        @Override
        public void handleEvent(@NonNull ITmfEvent event) {
            // Nothing to do, just stubs
        }
    }

    private class TestEventHandler2 extends TestEventHandler {
        public TestEventHandler2(int priority) {
            super(priority);
        }
    }

    /**
     * Test adding handlers to the graph provider
     */
    @Test
    public void testHandlers() {
        TmfXmlTraceStub trace = TmfXmlTraceStubNs.setupTrace(Activator.getAbsoluteFilePath(STUB_TRACE_FILE));

        try {
            GraphBuilderModuleStub module = getModule(trace);
            GraphProviderStub graphProvider = module.getGraphProvider();
            int origSize = graphProvider.getHandlers().size();
            // Add an handler with priority 5
            graphProvider.registerHandler(new TestEventHandler(5));
            List<@NonNull ITraceEventHandler> handlers = graphProvider.getHandlers();
            int newSize = handlers.size();
            assertTrue(areHandlersSorted(handlers));
            assertEquals(origSize + 1, newSize);

            // Add a new instance of the same handler, with same priority, it should not be
            // added
            graphProvider.registerHandler(new TestEventHandler(5));
            handlers = graphProvider.getHandlers();
            newSize = handlers.size();
            assertTrue(areHandlersSorted(handlers));
            assertEquals(origSize + 1, newSize);

            // Add the same handler with another priority, it should be added
            graphProvider.registerHandler(new TestEventHandler(7));
            handlers = graphProvider.getHandlers();
            newSize = handlers.size();
            assertTrue(areHandlersSorted(handlers));
            assertEquals(origSize + 2, newSize);

            // Add another class of handler with same priority as another one
            graphProvider.registerHandler(new TestEventHandler2(5));
            handlers = graphProvider.getHandlers();
            newSize = handlers.size();
            assertTrue(areHandlersSorted(handlers));
            assertEquals(origSize + 3, newSize);
        } finally {
            trace.dispose();
        }
    }

    private static boolean areHandlersSorted(List<@NonNull ITraceEventHandler> handlers) {
        // Verify that handlers are sorted by priority
        if (handlers.isEmpty()) {
            return true;
        }
        int prevPrio = -1;
        for (int i = 0; i < handlers.size(); i++) {
            int prio = handlers.get(i).getPriority();
            if (prevPrio > prio) {
                return false;
            }
            prevPrio = prio;
        }
        return true;
    }

}
