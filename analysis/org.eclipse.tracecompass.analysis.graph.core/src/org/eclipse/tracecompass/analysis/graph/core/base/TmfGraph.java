/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francis Giraldeau - Initial implementation and API
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.base;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.CountDownLatch;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ListMultimap;

/**
 * Undirected, unweighed, timed graph data type for dependencies between
 * elements of a system.
 *
 * Vertices are timed: each vertex has a timestamp associated, so the vertex
 * belongs to an object (the key of the multimap) at a given time. This is why
 * we use a ListMultimap to represent the graph, instead of a simple list.
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class TmfGraph {

    private final ListMultimap<IGraphWorker, TmfVertex> fNodeMap;
    private final Map<TmfVertex, IGraphWorker> fReverse;

    /* Latch tracking if the graph is done building or not */
    private final CountDownLatch fFinishedLatch = new CountDownLatch(1);

    /**
     * Constructor
     */
    public TmfGraph() {
        fNodeMap = NonNullUtils.checkNotNull(ArrayListMultimap.create());
        fReverse = new HashMap<>();
    }

    /**
     * Add node to the provided object without linking
     *
     * @param worker
     *            The key of the object the vertex belongs to
     * @param vertex
     *            The new vertex
     */
    public void add(IGraphWorker worker, TmfVertex vertex) {
        List<TmfVertex> list = fNodeMap.get(worker);
        list.add(vertex);
        fReverse.put(vertex, worker);
    }

    /**
     * Add node to object's list and make horizontal link with tail.
     *
     * @param worker
     *            The key of the object the vertex belongs to
     * @param vertex
     *            The new vertex
     * @return The edge constructed
     */
    public @Nullable TmfEdge append(IGraphWorker worker, TmfVertex vertex) {
        return append(worker, vertex, EdgeType.DEFAULT);
    }

    /**
     * Add node to object's list and make horizontal link with tail.
     *
     * @param worker
     *            The key of the object the vertex belongs to
     * @param vertex
     *            The new vertex
     * @param type
     *            The type of edge to create
     * @return The edge constructed
     */
    public @Nullable TmfEdge append(IGraphWorker worker, TmfVertex vertex, EdgeType type) {
        List<TmfVertex> list = fNodeMap.get(worker);
        TmfVertex tail = getTail(worker);
        TmfEdge link = null;
        if (tail != null) {
            link = tail.linkHorizontal(vertex);
            link.setType(type);
        }
        list.add(vertex);
        fReverse.put(vertex, worker);
        return link;
    }

    /**
     * Add a link between two vertices of the graph. The from vertex must be in
     * the graph. If the 'to' vertex is not in the graph, it will be appended to
     * the object the 'from' vertex is for. Otherwise a vertical or horizontal
     * link will be created between the vertices.
     *
     * Caution: this will remove without warning any previous link from the
     * 'from' vertex
     *
     * @param from
     *            The source vertex
     * @param to
     *            The destination vertex
     * @return The newly created edge
     */
    public TmfEdge link(TmfVertex from, TmfVertex to) {
        return link(from, to, EdgeType.DEFAULT);
    }

    /**
     * Add a link between two vertices of the graph. The from vertex must be in
     * the graph. If the 'to' vertex is not in the graph, it will be appended to
     * the object the 'from' vertex is for. Otherwise a vertical or horizontal
     * link will be created between the vertices.
     *
     * Caution: this will remove without warning any previous link from the
     * 'from' vertex
     *
     * @param from
     *            The source vertex
     * @param to
     *            The destination vertex
     * @param type
     *            The type of edge to create
     * @return The newly created edge
     */
    public TmfEdge link(TmfVertex from, TmfVertex to, EdgeType type) {
        IGraphWorker ofrom = fReverse.get(from);
        IGraphWorker oto = fReverse.get(to);
        if (ofrom == null) {
            throw new IllegalArgumentException(Messages.TmfGraph_FromNotInGraph);
        }

        /* to vertex not in the graph, add it to ofrom */
        if (oto == null) {
            this.add(ofrom, to);
            oto = ofrom;
        }

        TmfEdge link;
        if (oto.equals(ofrom)) {
            link = from.linkHorizontal(to);
        } else {
            link = from.linkVertical(to);
        }
        link.setType(type);
        return link;
    }

    /**
     * Returns tail node of the provided object
     *
     * @param worker
     *            The key of the object the vertex belongs to
     * @return The last vertex of obj
     */
    public @Nullable TmfVertex getTail(IGraphWorker worker) {
        List<TmfVertex> list = fNodeMap.get(worker);
        if (!list.isEmpty()) {
            return list.get(list.size() - 1);
        }
        return null;
    }

    /**
     * Removes the last vertex of the provided object
     *
     * @param worker
     *            The key of the object the vertex belongs to
     * @return The removed vertex
     */
    public @Nullable TmfVertex removeTail(IGraphWorker worker) {
        List<TmfVertex> list = fNodeMap.get(worker);
        if (!list.isEmpty()) {
            TmfVertex last = list.remove(list.size() - 1);
            fReverse.remove(last);
            return last;
        }
        return null;
    }

    /**
     * Returns head node of the provided object. This is the very first node of
     * an object
     *
     * @param worker
     *            The key of the object the vertex belongs to
     * @return The head vertex
     */
    public @Nullable TmfVertex getHead(IGraphWorker worker) {
        IGraphWorker ref = worker;
        List<TmfVertex> list = fNodeMap.get(ref);
        if (!list.isEmpty()) {
            return list.get(0);
        }
        return null;
    }

    /**
     * Returns the head node of the object of the nodeMap that has the earliest
     * head vertex time
     *
     * @return The head vertex
     */
    public @Nullable TmfVertex getHead() {
        if (fNodeMap.isEmpty()) {
            return null;
        }
        IGraphWorker headWorker = fNodeMap.keySet().stream()
                .filter(k -> !fNodeMap.get(k).isEmpty())
                .sorted((k1, k2) -> fNodeMap.get(k1).get(0).compareTo(fNodeMap.get(k2).get(0)))
                .findFirst()
                .get();
        return getHead(headWorker);
    }

    /**
     * Returns head vertex from a given node. That is the first of the current
     * sequence of edges, the one with no left edge when going back through the
     * original vertex's left edge
     *
     * @param vertex
     *            The vertex for which to get the head
     * @return The head vertex from the requested vertex
     */
    public TmfVertex getHead(TmfVertex vertex) {
        TmfVertex headNode = vertex;
        TmfEdge edge = headNode.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE);
        while (edge != null) {
            headNode = edge.getVertexFrom();
            if (headNode == vertex) {
                throw new CycleDetectedException();
            }
            edge = headNode.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE);
        }
        return headNode;
    }

    /**
     * Returns all nodes of the provided object.
     *
     * @param obj
     *            The key of the object the vertex belongs to
     * @return The list of vertices for the object
     */
    public List<TmfVertex> getNodesOf(IGraphWorker obj) {
        return fNodeMap.get(obj);
    }

    /**
     * Returns the object the vertex belongs to
     *
     * @param node
     *            The vertex to get the parent for
     * @return The object the vertex belongs to
     */
    public @Nullable IGraphWorker getParentOf(TmfVertex node) {
        return fReverse.get(node);
    }

    /**
     * Returns the graph objects
     *
     * @return The vertex map
     */
    public Set<IGraphWorker> getWorkers() {
        return ImmutableSet.copyOf(fNodeMap.keySet());
    }

    /**
     * Returns the number of vertices in the graph
     *
     * @return number of vertices
     */
    public int size() {
        return fReverse.size();
    }

    @Override
    public String toString() {
        return NonNullUtils.nullToEmptyString(String.format("Graph { actors=%d, nodes=%d }", //$NON-NLS-1$
                fNodeMap.keySet().size(), fNodeMap.values().size()));
    }

    /**
     * Dumps the full graph
     *
     * @return A string with the graph dump
     */
    public String dump() {
        StringBuilder str = new StringBuilder();
        for (IGraphWorker obj : fNodeMap.keySet()) {
            str.append(String.format("%10s ", obj)); //$NON-NLS-1$
            str.append(fNodeMap.get(obj));
            str.append("\n"); //$NON-NLS-1$
        }
        return NonNullUtils.nullToEmptyString(str.toString());
    }

    // ----------------------------------------------
    // Graph operations and visits
    // ----------------------------------------------

    /**
     * Visits a graph from the start vertex and every vertex of the graph having
     * a path to/from them that intersects the start vertex
     *
     * Each time the worker changes, it goes back to the beginning of the
     * current horizontal sequence and visits all nodes from there.
     *
     * Parts of the graph that are totally disjoints from paths to/from start
     * will not be visited by this method
     *
     * @param start
     *            The vertex to start the scan for
     * @param visitor
     *            The visitor
     */
    public void scanLineTraverse(final @Nullable TmfVertex start, final ITmfGraphVisitor visitor) {
        if (start == null) {
            return;
        }
        Stack<TmfVertex> stack = new Stack<>();
        HashSet<TmfVertex> visited = new HashSet<>();
        stack.add(start);
        while (!stack.isEmpty()) {
            TmfVertex curr = stack.pop();
            if (visited.contains(curr)) {
                continue;
            }
            // process one line
            TmfVertex n = getHead(curr);
            visitor.visitHead(n);
            while (true) {
                visitor.visit(n);
                visited.add(n);

                // Only visit links up-right, guarantee to visit once only
                TmfEdge edge = n.getEdge(EdgeDirection.OUTGOING_VERTICAL_EDGE);
                if (edge != null) {
                    stack.push(edge.getVertexTo());
                    visitor.visit(edge, false);
                }
                edge = n.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE);
                if (edge != null) {
                    stack.push(edge.getVertexFrom());
                }
                edge = n.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
                if (edge != null) {
                    visitor.visit(edge, true);
                    n = edge.getVertexTo();
                } else {
                    // end of the horizontal list
                    break;
                }
            }
        }
    }

    /**
     * @see TmfGraph#scanLineTraverse(TmfVertex, ITmfGraphVisitor)
     *
     * @param start
     *            The worker from which to start the scan
     * @param visitor
     *            The visitor
     */
    public void scanLineTraverse(@Nullable IGraphWorker start, final ITmfGraphVisitor visitor) {
        if (start == null) {
            return;
        }
        scanLineTraverse(getHead(start), visitor);
    }

    /**
     * Return the vertex for an object at a given timestamp, or the first vertex
     * after the timestamp
     *
     * @param startTime
     *            The desired time
     * @param worker
     *            The object for which to get the vertex
     * @return Vertex at timestamp or null if no vertex at or after timestamp
     */
    public @Nullable TmfVertex getVertexAt(ITmfTimestamp startTime, IGraphWorker worker) {
        List<TmfVertex> list = fNodeMap.get(worker);

        long ts = startTime.getValue();
        // Scan the list until vertex is later than time
        for (TmfVertex vertex : list) {
            if (vertex.getTs() >= ts) {
                return vertex;
            }
        }
        return null;
    }

    /**
     * Returns whether the graph is completed or not
     *
     * @return whether the graph is done building
     */
    public boolean isDoneBuilding() {
        return fFinishedLatch.getCount() == 0;
    }

    /**
     * Countdown the latch to show that the graph is done building
     */
    public void closeGraph() {
        fFinishedLatch.countDown();
    }

}
