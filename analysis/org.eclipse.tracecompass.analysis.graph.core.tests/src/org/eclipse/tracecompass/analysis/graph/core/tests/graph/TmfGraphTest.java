/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francis Giraldeau - Initial API and implementation
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.tests.graph;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.graph.core.base.CycleDetectedException;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.ITmfGraphVisitor;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.analysis.graph.core.tests.stubs.TestGraphWorker;
import org.eclipse.tracecompass.internal.analysis.graph.core.base.TmfGraphStatistics;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.junit.Test;

/**
 * Test the basic functionalities of the {@link TmfGraph}, {@link TmfVertex} and
 * {@link TmfEdge} classes.
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class TmfGraphTest {

    private static final @NonNull IGraphWorker WORKER1 = new TestGraphWorker(1);
    private static final @NonNull IGraphWorker WORKER2 = new TestGraphWorker(2);
    private static final @NonNull IGraphWorker WORKER3 = new TestGraphWorker(3);

    private final @NonNull TmfGraph fGraph = new TmfGraph();
    private final @NonNull TmfVertex fV0 = new TmfVertex(0);
    private final @NonNull TmfVertex fV1 = new TmfVertex(1);

    /**
     * Test the graph constructor
     */
    @Test
    public void testDefaultConstructor() {
        TmfGraph g = new TmfGraph();
        assertEquals(0, g.size());
    }

    /**
     * Test the {@link TmfGraph#add(IGraphWorker, TmfVertex)} method: vertices are
     * added, but no edge between them is created
     */
    @Test
    public void testAddVertex() {
        fGraph.add(WORKER1, fV0);
        fGraph.add(WORKER1, fV1);
        List<TmfVertex> list = fGraph.getNodesOf(WORKER1);
        assertEquals(2, list.size());
        for (int i = 0; i < list.size() - 1; i++) {
            TmfVertex vertex = list.get(i);
            assertEquals(i, vertex.getTs());
            assertNull(vertex.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE));
            assertNull(vertex.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE));
            assertNull(vertex.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
            assertNull(vertex.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
        }
    }

    /**
     * Test the {@link TmfGraph#append(IGraphWorker, TmfVertex)} and
     * {@link TmfGraph#append(IGraphWorker, TmfVertex, EdgeType)} methods: vertices
     * are added and links are created between them.
     */
    @Test
    public void testAppendVertex() {
        /* Append without type */
        fGraph.append(WORKER1, fV0);
        TmfEdge edge = fGraph.append(WORKER1, fV1);
        assertNotNull(edge);
        assertEquals(EdgeType.DEFAULT, edge.getType());
        assertEquals(fV1, edge.getVertexTo());
        assertEquals(fV0, edge.getVertexFrom());
        assertEquals(fV1.getTs() - fV0.getTs(), edge.getDuration());

        List<TmfVertex> list = fGraph.getNodesOf(WORKER1);
        assertEquals(2, list.size());
        checkLinkHorizontal(list);
        assertEquals(fV0, fGraph.getHead(WORKER1));
        assertEquals(fV1, fGraph.getTail(WORKER1));

        /* Append with a type */
        TmfVertex v2 = new TmfVertex(2);
        edge = fGraph.append(WORKER1, v2, EdgeType.BLOCKED);
        assertNotNull(edge);
        assertEquals(EdgeType.BLOCKED, edge.getType());
        assertEquals(v2, edge.getVertexTo());
        assertEquals(fV1, edge.getVertexFrom());
        assertEquals(v2.getTs() - fV1.getTs(), edge.getDuration());

        list = fGraph.getNodesOf(WORKER1);
        assertEquals(3, list.size());
        checkLinkHorizontal(list);
        assertEquals(fV0, fGraph.getHead(WORKER1));
        assertEquals(v2, fGraph.getTail(WORKER1));
    }

    /**
     * Test that appending vertices in non chronological order gives error
     */
    @Test(expected = IllegalArgumentException.class)
    public void testIllegalVertex() {
        fGraph.append(WORKER1, fV1);
        fGraph.append(WORKER1, fV0);
    }

    /**
     * Test the {@link TmfGraph#link(TmfVertex, TmfVertex)} and
     * {@link TmfGraph#link(TmfVertex, TmfVertex, EdgeType)} methods
     */
    @Test
    public void testLink() {
        // Start with a first node
        fGraph.add(WORKER1, fV0);

        // Link with second node not in graph
        TmfEdge edge = fGraph.link(fV0, fV1);
        assertEquals(fV1, edge.getVertexTo());
        assertEquals(fV0, edge.getVertexFrom());
        assertEquals(EdgeType.DEFAULT, edge.getType());
        assertEquals(fV1.getTs() - fV0.getTs(), edge.getDuration());

        List<TmfVertex> list = fGraph.getNodesOf(WORKER1);
        assertEquals(2, list.size());
        edge = fV1.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE);
        assertNotNull(edge);
        assertEquals(fV0, edge.getVertexFrom());
        edge = fV0.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
        assertNotNull(edge);
        assertEquals(fV1, edge.getVertexTo());

        // Link with second node for the same object
        TmfVertex v2 = new TmfVertex(2);
        fGraph.add(WORKER1, v2);
        edge = fGraph.link(fV1, v2, EdgeType.NETWORK);
        assertEquals(v2, edge.getVertexTo());
        assertEquals(fV1, edge.getVertexFrom());
        assertEquals(EdgeType.NETWORK, edge.getType());

        list = fGraph.getNodesOf(WORKER1);
        assertEquals(3, list.size());
        edge = fV1.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
        assertNotNull(edge);
        assertEquals(v2, edge.getVertexTo());
        edge = v2.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE);
        assertNotNull(edge);
        assertEquals(fV1, edge.getVertexFrom());

        // Link with second node for another object
        TmfVertex v3 = new TmfVertex(3);
        fGraph.add(WORKER2, v3);
        edge = fGraph.link(v2, v3, EdgeType.NETWORK);
        assertEquals(v3, edge.getVertexTo());
        assertEquals(v2, edge.getVertexFrom());
        assertEquals(EdgeType.NETWORK, edge.getType());

        list = fGraph.getNodesOf(WORKER2);
        assertEquals(1, list.size());

        list = fGraph.getNodesOf(WORKER1);
        assertEquals(3, list.size());
        edge = v3.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE);
        assertNotNull(edge);
        assertEquals(v2, edge.getVertexFrom());
        edge = v2.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE);
        assertNotNull(edge);
        assertEquals(v3, edge.getVertexTo());

    }

    /**
     * Verify that vertices in the list form a chain linked by edges and have no
     * vertical edges
     */
    private static void checkLinkHorizontal(List<TmfVertex> list) {
        if (list.isEmpty()) {
            return;
        }
        for (int i = 0; i < list.size() - 1; i++) {
            TmfVertex v0 = list.get(i);
            TmfVertex v1 = list.get(i + 1);
            TmfEdge edge = v0.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
            assertNotNull(edge);
            assertEquals(v0, edge.getVertexFrom());
            assertEquals(v1, edge.getVertexTo());
            edge = v1.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE);
            assertNotNull(edge);
            assertEquals(v0, edge.getVertexFrom());
            assertEquals(v1, edge.getVertexTo());
            assertNull(v1.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
            assertNull(v1.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
            assertNull(v0.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE));
            assertNull(v0.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
        }
    }

    /**
     * Test the {@link TmfGraph#getTail(IGraphWorker)} and
     * {@link TmfGraph#removeTail(IGraphWorker)} methods
     */
    @Test
    public void testTail() {
        fGraph.append(WORKER1, fV0);
        fGraph.append(WORKER1, fV1);
        assertEquals(fV1, fGraph.getTail(WORKER1));
        assertEquals(fV1, fGraph.removeTail(WORKER1));
        assertEquals(fV0, fGraph.getTail(WORKER1));
    }

    /**
     * Test the {@link TmfGraph#getHead()} methods
     */
    @Test
    public void testHead() {
        assertNull(fGraph.getHead());
        fGraph.append(WORKER1, fV0);
        fGraph.append(WORKER1, fV1);
        assertEquals(fV0, fGraph.getHead());
        assertEquals(fV0, fGraph.getHead(WORKER1));
        assertEquals(fV0, fGraph.getHead(fV1));
        assertEquals(fV0, fGraph.getHead(fV0));
    }

    /**
     * Test the {@link TmfGraph#getHead()} methods with 2 workers
     */
    @Test
    public void testHead2() {
        fGraph.append(WORKER1, fV1);
        fGraph.append(WORKER2, fV0);
        assertEquals(fV0, fGraph.getHead());
        assertEquals(fV1, fGraph.getHead(WORKER1));
        assertEquals(fV0, fGraph.getHead(WORKER2));
        assertEquals(fV1, fGraph.getHead(fV1));
        assertEquals(fV0, fGraph.getHead(fV0));
    }

    /**
     * The test {@link TmfGraph#getParentOf(TmfVertex)} method
     */
    @Test
    public void testParent() {
        fGraph.append(WORKER1, fV0);
        fGraph.append(WORKER2, fV1);
        assertEquals(WORKER1, fGraph.getParentOf(fV0));
        assertNotSame(WORKER1, fGraph.getParentOf(fV1));
        assertEquals(WORKER2, fGraph.getParentOf(fV1));
    }

    /**
     * Test the {@link TmfGraph#getVertexAt(ITmfTimestamp, IGraphWorker)} method
     */
    @Test
    public void testVertexAt() {
        TmfVertex[] vertices = new TmfVertex[5];
        for (int i = 0; i < 5; i++) {
            TmfVertex v = new TmfVertex((i + 1) * 5);
            vertices[i] = v;
            fGraph.append(WORKER1, v);
        }
        assertEquals(vertices[0], fGraph.getVertexAt(TmfTimestamp.fromSeconds(5), WORKER1));
        assertEquals(vertices[0], fGraph.getVertexAt(TmfTimestamp.fromSeconds(0), WORKER1));
        assertEquals(vertices[1], fGraph.getVertexAt(TmfTimestamp.fromSeconds(6), WORKER1));
        assertEquals(vertices[3], fGraph.getVertexAt(TmfTimestamp.fromSeconds(19), WORKER1));
        assertNull(fGraph.getVertexAt(TmfTimestamp.fromSeconds(19), WORKER2));
        assertEquals(vertices[3], fGraph.getVertexAt(TmfTimestamp.fromSeconds(20), WORKER1));
        assertEquals(vertices[4], fGraph.getVertexAt(TmfTimestamp.fromSeconds(21), WORKER1));
        assertNull(fGraph.getVertexAt(TmfTimestamp.fromSeconds(26), WORKER1));
    }

    /**
     * Test the {@link TmfVertex#linkHorizontal(TmfVertex)} with non
     * chronological timestamps
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheckHorizontal() {
        TmfVertex n0 = new TmfVertex(10);
        TmfVertex n1 = new TmfVertex(0);
        n0.linkHorizontal(n1);
    }

    /**
     * Test the {@link TmfVertex#linkVertical(TmfVertex)} with non chronological
     * timestamps
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCheckVertical() {
        TmfVertex n0 = new TmfVertex(10);
        TmfVertex n1 = new TmfVertex(0);
        n0.linkVertical(n1);
    }

    private class ScanCountVertex implements ITmfGraphVisitor {
        public int nbVertex = 0;
        public int nbVLink = 0;
        public int nbHLink = 0;
        public int nbStartVertex = 0;

        @Override
        public void visitHead(TmfVertex node) {
            nbStartVertex++;
        }

        @Override
        public void visit(TmfVertex node) {
            nbVertex++;

        }

        @Override
        public void visit(TmfEdge edge, boolean horizontal) {
            if (horizontal) {
                nbHLink++;
            } else {
                nbVLink++;
            }
        }
    }

    /**
     * The following graph will be used
     *
     * <pre>
     * ____0___1___2___3___4___5___6___7___8___9___10___11___12___13___14___15
     *
     * A   *-------*       *---*-------*---*---*    *---*----*----*---------*
     *             |           |           |            |    |
     * B       *---*---*-------*   *-------*------------*    *----------*
     * </pre>
     */
    @SuppressWarnings("null")
    private static @NonNull TmfGraph buildFullGraph() {
        TmfGraph graph = new TmfGraph();
        TmfVertex[] vertexA;
        TmfVertex[] vertexB;
        long[] timesA = { 0, 2, 4, 5, 7, 8, 9, 10, 11, 12, 13, 15 };
        long[] timesB = { 1, 2, 3, 5, 6, 8, 11, 12, 14 };
        vertexA = new TmfVertex[timesA.length];
        vertexB = new TmfVertex[timesB.length];
        for (int i = 0; i < timesA.length; i++) {
            vertexA[i] = new TmfVertex(timesA[i]);
        }
        for (int i = 0; i < timesB.length; i++) {
            vertexB[i] = new TmfVertex(timesB[i]);
        }
        graph.append(WORKER1, vertexA[0]);
        graph.append(WORKER1, vertexA[1]);
        graph.add(WORKER1, vertexA[2]);
        graph.append(WORKER1, vertexA[3]);
        graph.append(WORKER1, vertexA[4]);
        graph.append(WORKER1, vertexA[5]);
        graph.append(WORKER1, vertexA[6]);
        graph.add(WORKER1, vertexA[7]);
        graph.append(WORKER1, vertexA[8]);
        graph.append(WORKER1, vertexA[9]);
        graph.append(WORKER1, vertexA[10]);
        graph.append(WORKER1, vertexA[11]);
        graph.append(WORKER2, vertexB[0]);
        graph.append(WORKER2, vertexB[1]);
        graph.append(WORKER2, vertexB[2]);
        graph.append(WORKER2, vertexB[3]);
        graph.add(WORKER2, vertexB[4]);
        graph.append(WORKER2, vertexB[5]);
        graph.append(WORKER2, vertexB[6]);
        graph.add(WORKER2, vertexB[7]);
        graph.append(WORKER2, vertexB[8]);
        vertexA[1].linkVertical(vertexB[1]);
        vertexB[3].linkVertical(vertexA[3]);
        vertexA[5].linkVertical(vertexB[5]);
        vertexB[6].linkVertical(vertexA[8]);
        vertexA[9].linkVertical(vertexB[7]);
        return graph;
    }

    /**
     * Test the {@link TmfGraph#scanLineTraverse(IGraphWorker, ITmfGraphVisitor)} method
     */
    @Test
    public void testScanCount() {
        TmfGraph graph = buildFullGraph();
        ScanCountVertex visitor = new ScanCountVertex();
        graph.scanLineTraverse(graph.getHead(WORKER1), visitor);
        assertEquals(21, visitor.nbVertex);
        assertEquals(6, visitor.nbStartVertex);
        assertEquals(5, visitor.nbVLink);
        assertEquals(15, visitor.nbHLink);
    }

    /**
     * Test the {@link TmfGraphStatistics} class
     */
    @Test
    public void testGraphStatistics() {
        TmfGraph graph = buildFullGraph();
        TmfGraphStatistics stats = new TmfGraphStatistics();
        stats.computeGraphStatistics(graph, WORKER1);
        assertEquals(12, stats.getSum(WORKER1).longValue());
        assertEquals(11, stats.getSum(WORKER2).longValue());
        assertEquals(23, stats.getSum().longValue());
    }

    /**
     * This visitor throws an exception if it visits twice the same vertex
     *
     * @author Francis Giraldeau
     *
     */
    private class DuplicateDetectorVisitor implements ITmfGraphVisitor {
        private final Set<TmfVertex> set = new HashSet<>();
        @Override
        public void visitHead(TmfVertex vertex) {
        }
        @Override
        public void visit(TmfEdge edge, boolean horizontal) {
        }
        @Override
        public void visit(TmfVertex vertex) {
            if (set.contains(vertex)) {
                throw new RuntimeException("node already visited");
            }
            set.add(vertex);
        }
    }

    /**
     * Test that exception is thrown if a node is linked horizontally to itself
     */
    @Test(expected = IllegalArgumentException.class)
    public void testHorizontalSelfLink() {
        TmfVertex n0 = new TmfVertex(0);
        n0.linkHorizontal(n0);
    }

    /**
     * Test that exception is thrown if a node is linked vertically to itself.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testVerticalSelfLink() {
        TmfVertex n0 = new TmfVertex(0);
        n0.linkVertical(n0);
    }

    /**
     * Test that the visitor detect a cycle in horizontal links. A cycle may
     * exists only between vertices with equal timetstamps.
     */
    @Test(expected = CycleDetectedException.class)
    public void testHorizontalCycle() {
        TmfVertex n0 = new TmfVertex(0);
        TmfVertex n1 = new TmfVertex(0);
        n0.linkHorizontal(n1);
        n1.linkHorizontal(n0);
        TmfGraph graph = new TmfGraph();
        graph.add(WORKER1, n0);
        graph.add(WORKER1, n1);
        graph.scanLineTraverse(n0, new DuplicateDetectorVisitor());
    }

    /**
     * Test that scanLineTraverse terminates with the following graph:
     *
     * <pre>
     *          ^
     *          |
     *    +----->
     *    ^
     *    |
     *    +
     *
     * </pre>
     */
    @Test
    public void testScanLineTerminates() {
        TmfVertex n10 = new TmfVertex(0);
        TmfVertex n20 = new TmfVertex(0);
        TmfVertex n21 = new TmfVertex(1);
        TmfVertex n30 = new TmfVertex(1);
        TmfGraph graph = new TmfGraph();
        n10.linkVertical(n20);
        n20.linkHorizontal(n21);
        n21.linkVertical(n30);
        graph.add(WORKER1, n10);
        graph.add(WORKER2, n20);
        graph.add(WORKER2, n21);
        graph.add(WORKER3, n30);
        graph.scanLineTraverse(n20, new DuplicateDetectorVisitor());
    }

}
