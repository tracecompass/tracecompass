/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Francis Giraldeau - Initial implementation and API
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.synchronization.graph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.Stack;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

/**
 * Minimal graph implementation to compute timestamps transforms of a trace from
 * a given synchronized set of traces. The graph is implemented as an adjacency
 * list and is directed. To create undirected graph, add the edge in both
 * directions.
 *
 * @author Francis Giraldeau <francis.giraldeau@gmail.com>
 * @param <V>
 *            The vertices type
 * @param <E>
 *            The edge annotation type
 */
public class SyncGraph<V, E> {

    private Multimap<V, Edge<V, E>> fAdjacentEdges;
    private Set<V> fVertices;

    /**
     * Construct empty graph
     */
    public SyncGraph() {
        fAdjacentEdges = ArrayListMultimap.create();
        fVertices = new HashSet<>();
    }

    /**
     * Add edge from v to w and annotation label
     *
     * @param from
     *            from vertex
     * @param to
     *            to vertex
     * @param label
     *            the edge label
     */
    public void addEdge(V from, V to, E label) {
        fAdjacentEdges.put(from, new Edge<>(from, to, label));
        fVertices.add(from);
        fVertices.add(to);
    }

    /**
     * Get the number of edges
     *
     * @return number of edges
     */
    public int getNbEdges() {
        return fAdjacentEdges.entries().size();
    }

    /**
     * Get the number of vertices
     *
     * @return number of vertices
     */
    public int getNbVertices() {
        return fVertices.size();
    }

    /**
     * Returns the adjacent edges of the given vertex
     *
     * @param v
     *            the vertex
     * @return the adjacent vertices
     */
    public Collection<Edge<V, E>> getAdjacentEdges(V v) {
        return fAdjacentEdges.get(v);
    }

    /**
     * Returns a path between start and end vertices.
     *
     * @param start
     *            vertex
     * @param end
     *            vertex
     * @return the list of edges between start and end vertices
     */
    public List<Edge<V, E>> path(V start, V end) {
        ArrayList<Edge<V, E>> path = new ArrayList<>();
        HashMap<V, Edge<V, E>> hist = new HashMap<>();
        HashSet<V> visited = new HashSet<>();
        Queue<V> queue = new LinkedList<>();
        queue.offer(start);
        /**
         * Build the map of nodes reachable from the start node, by recursively
         * visiting all accessible nodes. It is a breadth-first search, so the
         * edges kept for each node will be the shortest path to that node.
         */
        while (!queue.isEmpty()) {
            V node = queue.poll();
            visited.add(node);
            for (Edge<V, E> e : getAdjacentEdges(node)) {
                V to = e.getTo();
                if (!visited.contains(to)) {
                    queue.offer(e.getTo());
                    if (!hist.containsKey(e.getTo())) {
                        hist.put(e.getTo(), e);
                    }
                }
            }
        }
        /*
         * Find path from start to end by traversing the edges backward, from
         * the end node
         */
        V node = end;
        Edge<V, E> edge = hist.get(node);
        while (edge != null && node != start) {
            path.add(edge);
            node = edge.getFrom();
            edge = hist.get(node);
        }
        Collections.reverse(path);
        return path;
    }

    /**
     * Check if this graph is connected, ie there are no partitions, all
     * vertices are reachable from every other one. It is a depth-first visit of
     * all vertices reachable from the first vertex of the graph.
     *
     * @return true if the graph is connected, false otherwise
     */
    public boolean isConnected() {
        HashSet<V> visited = new HashSet<>();
        Stack<V> stack = new Stack<>();
        stack.push(fVertices.iterator().next());
        while (!stack.isEmpty()) {
            V node = stack.pop();
            visited.add(node);
            for (Edge<V, E> edge : getAdjacentEdges(node)) {
                if (!visited.contains(edge.getTo())) {
                    stack.push(edge.getTo());
                }
            }
        }
        return visited.size() == fVertices.size();
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        for (V key : fAdjacentEdges.keySet()) {
            str.append(key + ": " + fAdjacentEdges.get(key) + "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return str.toString();
    }

}
