/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.analysis.graph.core.criticalpath;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex.EdgeDirection;
import org.eclipse.tracecompass.analysis.graph.core.criticalpath.CriticalPathAlgorithmException;

/**
 * Critical path bounded algorithm: backward resolution of blocking limited to
 * the blocking window
 *
 * This algorithm is described in
 *
 * F. Giraldeau and M.Dagenais, Wait analysis of distributed systems using
 * kernel tracing, IEEE Transactions on Parallel and Distributed Systems
 *
 * @author Francis Giraldeau
 */
public class CriticalPathAlgorithmBounded extends AbstractCriticalPathAlgorithm {

    /**
     * Constructor
     *
     * @param graph
     *            The graph on which to calculate the critical path
     */
    public CriticalPathAlgorithmBounded(TmfGraph graph) {
        super(graph);
    }

    @Override
    public TmfGraph compute(TmfVertex start, @Nullable TmfVertex end) throws CriticalPathAlgorithmException {
        /* Create new graph for the critical path result */
        TmfGraph criticalPath = new TmfGraph();

        /* Get the main graph from which to get critical path */
        TmfGraph graph = getGraph();

        /*
         * Calculate path starting from the object the start vertex belongs to
         */
        IGraphWorker parent = checkNotNull(graph.getParentOf(start));
        criticalPath.add(parent, new TmfVertex(start));
        TmfVertex currentVertex = start;
        TmfEdge nextEdge = currentVertex.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);

        long endTime = Long.MAX_VALUE;
        if (end != null) {
            endTime = end.getTs();
        }

        /*
         * Run through all horizontal edges from this object and resolve each
         * blocking as they come
         */
        while (nextEdge != null) {
            TmfVertex nextVertex = nextEdge.getVertexTo();
            if (nextVertex.getTs() >= endTime) {
                break;
            }
            switch (nextEdge.getType()) {
            case IPI:
            case USER_INPUT:
            case BLOCK_DEVICE:
            case TIMER:
            case INTERRUPTED:
            case PREEMPTED:
            case RUNNING:
                /**
                 * This edge is not blocked, so nothing to resolve, just add the
                 * edge to the critical path
                 */
                /**
                 * TODO: Normally, the parent of the link's vertex to should be
                 * the object itself, verify if that is true
                 */
                IGraphWorker parentTo = checkNotNull(graph.getParentOf(nextEdge.getVertexTo()));
                if (parentTo != parent) {
                    throw new CriticalPathAlgorithmException("no, the parents of horizontal edges are not always identical... shouldn't they be?"); //$NON-NLS-1$
                }
                criticalPath.append(parentTo, new TmfVertex(nextEdge.getVertexTo()), nextEdge.getType());
                break;
            case NETWORK:
            case BLOCKED:
                List<TmfEdge> links = resolveBlockingBounded(nextEdge, nextEdge.getVertexFrom());
                Collections.reverse(links);
                appendPathComponent(criticalPath, graph, currentVertex, links);
                break;
            case EPS:
                if (nextEdge.getDuration() != 0) {
                    throw new CriticalPathAlgorithmException("epsilon duration is not zero " + nextEdge); //$NON-NLS-1$
                }
                break;
            case DEFAULT:
                throw new CriticalPathAlgorithmException("Illegal link type " + nextEdge.getType()); //$NON-NLS-1$
            case UNKNOWN:
            default:
                break;
            }
            currentVertex = nextVertex;
            nextEdge = currentVertex.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
        }
        return criticalPath;
    }

    /** Add the links to the critical path, with currentVertex to glue to */
    private void appendPathComponent(TmfGraph criticalPath, TmfGraph graph, TmfVertex currentVertex, List<TmfEdge> links) {
        IGraphWorker currentActor = checkNotNull(graph.getParentOf(currentVertex));
        if (links.isEmpty()) {
            /*
             * The next vertex should not be null, since we glue only after
             * resolve of the blocking of the edge to that vertex
             */
            TmfEdge next = currentVertex.getEdge(EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
            if (next == null) {
                return;
            }
            criticalPath.append(currentActor, new TmfVertex(next.getVertexTo()), next.getType());
            return;
        }
        // FIXME: assert last link.to actor == currentActor

        // attach subpath to b1
        TmfVertex b1 = checkNotNull(criticalPath.getTail(currentActor));

        // glue head
        TmfEdge lnk = links.get(0);
        TmfVertex anchor = null;
        IGraphWorker objSrc = checkNotNull(graph.getParentOf(lnk.getVertexFrom()));
        if (objSrc.equals( currentActor)) {
            anchor = b1;
        } else {
            anchor = new TmfVertex(currentVertex);
            criticalPath.add(objSrc, anchor);
            b1.linkVertical(anchor);
            /* fill any gap with UNKNOWN */
            if (lnk.getVertexFrom().compareTo(anchor) > 0) {
                anchor = new TmfVertex(lnk.getVertexFrom());
                TmfEdge edge = checkNotNull(criticalPath.append(objSrc, anchor));
                edge.setType(TmfEdge.EdgeType.UNKNOWN);
            }
        }

        // glue body
        TmfEdge prev = null;
        for (TmfEdge link : links) {
            // check connectivity
            if (prev != null && prev.getVertexTo() != link.getVertexFrom()) {
                anchor = copyLink(criticalPath, graph, anchor, prev.getVertexTo(), link.getVertexFrom(),
                        prev.getVertexTo().getTs(), TmfEdge.EdgeType.DEFAULT);
            }
            anchor = copyLink(criticalPath, graph, anchor, link.getVertexFrom(), link.getVertexTo(),
                    link.getVertexTo().getTs(), link.getType());
            prev = link;
        }
    }

    /**
     * Resolve a blocking by going through the graph vertically from the
     * blocking edge
     *
     * FIXME: build a tree with partial subpath in order to return the best
     * path, not the last one traversed
     *
     * @param blocking
     *            The blocking edge
     * @param bound
     *            The vertex that limits the boundary until which to resolve the
     *            blocking
     * @return The list of non-blocking edges
     */
    private List<TmfEdge> resolveBlockingBounded(TmfEdge blocking, TmfVertex bound) {

        LinkedList<TmfEdge> subPath = new LinkedList<>();
        TmfVertex junction = findIncoming(blocking.getVertexTo(), EdgeDirection.OUTGOING_HORIZONTAL_EDGE);
        /* if wake-up source is not found, return empty list */
        if (junction == null) {
            return subPath;
        }

        TmfEdge down = checkNotNull(junction.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE));
        subPath.add(down);
        TmfVertex vertexFrom = down.getVertexFrom();

        TmfVertex currentBound = bound.compareTo(blocking.getVertexFrom()) < 0 ? blocking.getVertexFrom() : bound;

        Stack<TmfVertex> stack = new Stack<>();
        while (vertexFrom != null && vertexFrom.compareTo(currentBound) > 0) {
            /* shortcut for down link that goes beyond the blocking */
            TmfEdge inVerticalEdge = vertexFrom.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE);
            if (inVerticalEdge != null && inVerticalEdge.getVertexFrom().compareTo(currentBound) <= 0) {
                subPath.add(inVerticalEdge);
                break;
            }

            /**
             * Add DOWN links to explore stack in case dead-end occurs. Here is
             * an example to illustrate the procedure.
             *
             * <pre>
             *           -------------------------
             *            BLOCKED    | PREEMPT
             *           -------------------------
             *                       ^
             *                       |WAKE-UP
             *                       |
             *         +--------------------+
             * +-------+      INTERRUPT     +--------+
             *         +--------------------+
             *           ^   ^   |       ^
             *           |   |   |       |
             *           +   +   v       +
             *           1   2   3       4
             * </pre>
             *
             * The event wake-up is followed backward. The edge 4 will never be
             * visited (it cannot be the cause of the wake-up, because it occurs
             * after it). The edge 3 will not be explored, because it is
             * outgoing. The edges 2 and 1 will be pushed on the stack. When the
             * beginning of the interrupt is reached, then the edges on the
             * stack will be explored.
             *
             * If a dead-end is reached, while the stack is not empty, the
             * accumulated path is rewinded, and a different incoming edge is
             * tried. The backward traversal ends if there is nothing left to
             * explore, or if the start of the blocking window start is reached.
             *
             * Do not add if left is BLOCKED, because this link would be visited
             * twice
             */
            TmfEdge incomingEdge = vertexFrom.getEdge(EdgeDirection.INCOMING_HORIZONTAL_EDGE);
            if (inVerticalEdge != null &&
                    (incomingEdge == null ||
                            (incomingEdge.getType() != TmfEdge.EdgeType.BLOCKED &&
                            incomingEdge.getType() != TmfEdge.EdgeType.NETWORK))) {
                stack.push(vertexFrom);
            }
            if (incomingEdge != null) {
                if (incomingEdge.getType() == TmfEdge.EdgeType.BLOCKED || incomingEdge.getType() == TmfEdge.EdgeType.NETWORK) {
                    subPath.addAll(resolveBlockingBounded(incomingEdge, currentBound));
                } else {
                    subPath.add(incomingEdge);
                }
                vertexFrom = incomingEdge.getVertexFrom();
            } else {
                if (!stack.isEmpty()) {
                    TmfVertex v = stack.pop();
                    /* rewind subpath */
                    while (!subPath.isEmpty() && subPath.getLast().getVertexFrom() != v) {
                        subPath.removeLast();
                    }
                    TmfEdge edge = v.getEdge(EdgeDirection.INCOMING_VERTICAL_EDGE);
                    if (edge != null) {
                        subPath.add(edge);
                        vertexFrom = edge.getVertexFrom();
                        continue;
                    }
                }
                vertexFrom = null;
            }

        }
        return subPath;
    }

}
