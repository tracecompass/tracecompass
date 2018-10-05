/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.tests.stubs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.List;
import java.util.Set;

import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;

/**
 * Class that implements static operations on vertices and edges. The sets of
 * nodes and vertices can then be transformed to a graph.
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class GraphOps {

    /**
     * Check whether 2 graphs are identical
     *
     * @param g1
     *            The first graph to compare
     * @param g2
     *            The second graph
     */
    public static void checkEquality(TmfGraph g1, TmfGraph g2) {
        Set<IGraphWorker> obj1 = g1.getWorkers();
        Set<IGraphWorker> obj2 = g2.getWorkers();
        assertEquals("Graph objects", obj1, obj2);
        for (IGraphWorker graphObject : obj1) {
            assertNotNull(graphObject);
            List<TmfVertex> nodesOf1 = g1.getNodesOf(graphObject);
            List<TmfVertex> nodesOf2 = g2.getNodesOf(graphObject);
            for (int i = 0; i < nodesOf1.size(); i++) {
                TmfVertex v1 = nodesOf1.get(i);
                TmfVertex v2 = nodesOf2.get(i);
                assertEquals("Node timestamps for " + graphObject + ", node " + i, v1.getTs(), v2.getTs());
                /* Check each edge */
                for (EdgeDirection dir : EdgeDirection.values()) {
                    TmfEdge edge1 = v1.getEdge(dir);
                    TmfEdge edge2 = v2.getEdge(dir);
                    if (edge1 == null) {
                        assertNull("Expected null edge for " + graphObject + ", node " + i + " and dir " + dir, edge2);
                        continue;
                    }
                    assertNotNull("Expected non null edge for " + graphObject + ", node " + i + " and dir " + dir, edge2);
                    assertEquals("Edge type for " + graphObject + ", node " + i + " and dir " + dir, edge1.getType(), edge2.getType());
                    assertEquals("Edge duration for " + graphObject + ", node " + i + " edge direction " + dir, edge1.getDuration(), edge2.getDuration());
                    assertEquals("From objects for " + graphObject + ", node " + i + " and dir " + dir, g1.getParentOf(edge1.getVertexFrom()), g2.getParentOf(edge2.getVertexFrom()));
                    assertEquals("To objects for " + graphObject + ", node " + i + " and dir " + dir, g1.getParentOf(edge1.getVertexTo()), g2.getParentOf(edge2.getVertexTo()));
                    assertEquals("Edge qualifier for " + graphObject + ", node " + i + " and dir " + dir, edge1.getLinkQualifier(), edge2.getLinkQualifier());
                }
            }
        }
    }

}
