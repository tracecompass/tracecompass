/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.tests.stubs;

import org.eclipse.tracecompass.analysis.graph.core.base.IGraphWorker;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfGraph;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfVertex;

/**
 * Factory generating various scenarios of graphs to test critical path
 * algorithms on
 *
 * @author Geneviève Bastien
 * @author Francis Giraldeau
 */
public class GraphFactory {

    /**
     * First default actor of a graph
     */
    public static final IGraphWorker Actor0 = new TestGraphWorker(0);

    /**
     * Second default actor of the graph
     */
    public static final TestGraphWorker Actor1 = new TestGraphWorker(1);

    /** An optional link qualifier on some edges */
    private static final String LINK_QUALIFIER = "testLinkQualifier";

    /**
     * Simple RUNNING edge involving one object
     */
    public static final GraphBuilder GRAPH_BASIC =
            new GraphBuilder("basic") {

                @Override
                public TmfGraph build() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(1), EdgeType.RUNNING);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    return build();
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    return build();
                }

            };

    /**
     * Single object, timer starts at t2 and wakes up at t4. Blocked at t3
     *
     * <pre>
     *        /   -T-   \
     * * -R- * -R- * -B- * -R- *
     * </pre>
     */
    public static final GraphBuilder GRAPH_WAKEUP_SELF =
            new GraphBuilder("wakeup_self") {
                @Override
                public TmfGraph build() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    TmfVertex vStart = new TmfVertex(1);
                    graph.append(Actor0, vStart, EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING, LINK_QUALIFIER);
                    TmfVertex vEnd = new TmfVertex(3);
                    graph.append(Actor0, vEnd, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(4), EdgeType.RUNNING);
                    TmfEdge link = vStart.linkVertical(vEnd);
                    link.setType(EdgeType.TIMER);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(1), EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING, LINK_QUALIFIER);
                    graph.append(Actor0, new TmfVertex(3), EdgeType.TIMER);
                    graph.append(Actor0, new TmfVertex(4), EdgeType.RUNNING);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(1), EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(3), EdgeType.TIMER);
                    graph.append(Actor0, new TmfVertex(4), EdgeType.RUNNING);
                    return graph;
                }

            };

    /**
     * Single object, 4 vertices, blocked between 2 and 3, but nothing wakes up
     *
     * <pre>
     * * -R- * -B- * -R- *
     * </pre>
     */
    public static final GraphBuilder GRAPH_WAKEUP_MISSING =
            new GraphBuilder("wakeup_missing") {
                @Override
                public TmfGraph build() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(4), EdgeType.BLOCKED, LINK_QUALIFIER);
                    graph.append(Actor0, new TmfVertex(6), EdgeType.RUNNING);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(4), EdgeType.BLOCKED, LINK_QUALIFIER);
                    graph.append(Actor0, new TmfVertex(6), EdgeType.RUNNING);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    throw new UnsupportedOperationException();
                }
            };

    /**
     * Object woken from blockage by another network object
     *
     * <pre>
     * * - R - * - B - * - R - *
     *               /N
     *             *
     * </pre>
     */
    public static final GraphBuilder GRAPH_WAKEUP_UNKNOWN =
            new GraphBuilder("wakeup_unknown") {
                @Override
                public TmfGraph build() {
                    TmfGraph graph = new TmfGraph();
                    TmfVertex vIn = new TmfVertex(4);
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING);
                    graph.append(Actor0, vIn, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(6), EdgeType.RUNNING);

                    TmfVertex vNet = new TmfVertex(3);
                    graph.add(Actor1, vNet);
                    graph.link(vNet, vIn, EdgeType.NETWORK);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    TmfVertex vStartBlock = new TmfVertex(2);
                    TmfVertex vEndBlock = new TmfVertex(4);
                    graph.append(Actor0, vStartBlock, EdgeType.RUNNING);
                    graph.add(Actor0, vEndBlock);
                    graph.append(Actor0, new TmfVertex(6), EdgeType.RUNNING);

                    TmfVertex vStartOther = new TmfVertex(2);
                    TmfVertex vEndOther = new TmfVertex(3);
                    graph.add(Actor1, vStartOther);
                    graph.append(Actor1, vEndOther, EdgeType.UNKNOWN);

                    graph.link(vStartBlock, vStartOther);
                    graph.link(vEndOther, vEndBlock, EdgeType.NETWORK);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    TmfVertex vStartBlock = new TmfVertex(3);
                    TmfVertex vEndBlock = new TmfVertex(4);
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING);
                    graph.append(Actor0, vStartBlock, EdgeType.UNKNOWN);
                    graph.add(Actor0, vEndBlock);
                    graph.append(Actor0, new TmfVertex(6), EdgeType.RUNNING);
                    TmfEdge link = vStartBlock.linkVertical(vEndBlock);
                    link.setType(EdgeType.NETWORK);

                    return graph;
                }
            };

    /**
     * Object woken from blockage by another running object that was created by
     * first object
     *
     * <pre>
     * * -R- * -R- * -B- * -R- *
     *         \         |
     *          *  -R-   *
     * </pre>
     */
    public static GraphBuilder GRAPH_WAKEUP_NEW =
            new GraphBuilder("wakeup_new") {
                @Override
                public TmfGraph build() {
                    TmfGraph graph = new TmfGraph();
                    TmfVertex vSrcLink = new TmfVertex(2);
                    TmfVertex vBlockEnd = new TmfVertex(6);
                    TmfVertex vDstLink = new TmfVertex(3);
                    TmfVertex vWakeup = new TmfVertex(6);
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, vSrcLink, EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(4), EdgeType.RUNNING, LINK_QUALIFIER);
                    graph.append(Actor0, vBlockEnd, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(8), EdgeType.RUNNING, LINK_QUALIFIER);

                    graph.add(Actor1, vDstLink);
                    graph.append(Actor1, vWakeup, EdgeType.RUNNING, LINK_QUALIFIER);

                    graph.link(vSrcLink, vDstLink);
                    graph.link(vWakeup, vBlockEnd);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    TmfGraph graph = new TmfGraph();
                    TmfVertex vBlockStart = new TmfVertex(4);
                    TmfVertex vBlockEnd = new TmfVertex(6);
                    TmfVertex vDstLink = new TmfVertex(4);
                    TmfVertex vWakeup = new TmfVertex(6);
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING);
                    graph.append(Actor0, vBlockStart, EdgeType.RUNNING, LINK_QUALIFIER);
                    graph.add(Actor0, vBlockEnd);
                    graph.append(Actor0, new TmfVertex(8), EdgeType.RUNNING, LINK_QUALIFIER);

                    graph.add(Actor1, vDstLink);
                    graph.append(Actor1, vWakeup, EdgeType.RUNNING, LINK_QUALIFIER);

                    graph.link(vBlockStart, vDstLink);
                    graph.link(vWakeup, vBlockEnd);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    TmfGraph graph = new TmfGraph();
                    TmfVertex vSrcLink = new TmfVertex(2);
                    TmfVertex vBlockEnd = new TmfVertex(6);
                    TmfVertex vDstLink = new TmfVertex(3);
                    TmfVertex vWakeup = new TmfVertex(6);
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, vSrcLink, EdgeType.RUNNING);
                    graph.add(Actor0, vBlockEnd);
                    graph.append(Actor0, new TmfVertex(8), EdgeType.RUNNING, LINK_QUALIFIER);

                    graph.add(Actor1, vDstLink);
                    graph.append(Actor1, vWakeup, EdgeType.RUNNING, LINK_QUALIFIER);

                    graph.link(vSrcLink, vDstLink);
                    graph.link(vWakeup, vBlockEnd);
                    return graph;
                }
            };

    /**
     * Two objects join to unblock the first but with delay
     *
     * <pre>
     * 0: * --R-- * --B-- * --R-- *
     *              /
     * 1: * -R- * --R-- *
     * </pre>
     */
    public static GraphBuilder GRAPH_OPENED_DELAY =
            new GraphBuilder("opened") {
                @Override
                public TmfGraph build() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(3), EdgeType.RUNNING);
                    TmfVertex v1 = new TmfVertex(6);
                    graph.append(Actor0, v1, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(9), EdgeType.RUNNING);
                    graph.add(Actor1, new TmfVertex(0));
                    TmfVertex v2 = new TmfVertex(2);
                    graph.append(Actor1, v2, EdgeType.RUNNING);
                    graph.append(Actor1, new TmfVertex(5), EdgeType.RUNNING);
                    graph.link(v2, v1);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    TmfVertex v1 = new TmfVertex(3);
                    graph.append(Actor0, v1, EdgeType.RUNNING);
                    TmfVertex v2 = new TmfVertex(3);
                    TmfVertex v3 = new TmfVertex(6);
                    graph.add(Actor1, v2);
                    graph.add(Actor0, v3);
                    graph.link(v1, v2);
                    graph.link(v2, v3);
                    graph.append(Actor0, new TmfVertex(9), EdgeType.RUNNING);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    throw new UnsupportedOperationException();
                }
            };

    /**
     * Two objects join to unblock the first without delay
     *
     * <pre>
     * * --R-- * --B-- * --R-- *
     *                 |
     * * -------R----- * --R-- *
     * </pre>
     */
    public static GraphBuilder GRAPH_OPENED =
            new GraphBuilder("opened") {
                @Override
                public TmfGraph build() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(3), EdgeType.RUNNING);
                    TmfVertex v1 = new TmfVertex(6);
                    graph.append(Actor0, v1, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(9), EdgeType.RUNNING);
                    graph.add(Actor1, new TmfVertex(0));
                    TmfVertex v2 = new TmfVertex(6);
                    graph.append(Actor1, v2, EdgeType.RUNNING);
                    graph.append(Actor1, new TmfVertex(9), EdgeType.RUNNING);
                    graph.link(v2, v1);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    TmfGraph graph = new TmfGraph();
                    graph.add(Actor0, new TmfVertex(0));
                    TmfVertex v1 = new TmfVertex(3);
                    graph.append(Actor0, v1, EdgeType.RUNNING);
                    TmfVertex v2 = new TmfVertex(3);
                    TmfVertex v3 = new TmfVertex(6);
                    graph.add(Actor1, v2);
                    graph.append(Actor1, v3, EdgeType.RUNNING);
                    TmfVertex v4 = new TmfVertex(6);
                    graph.add(Actor0, v4);
                    graph.link(v1, v2);
                    graph.link(v3, v4);
                    graph.append(Actor0, new TmfVertex(9), EdgeType.RUNNING);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    throw new UnsupportedOperationException();
                }
            };

    /**
     * Two objects are blocked and mutually unblock at different times
     *
     * <pre>
     * 0: * -R- * -R- * -R- * -B- * -R- *
     *                |           |
     * 1: * -R- * -B- * -R- * -R- * -R- *
     * </pre>
     */
    public static GraphBuilder GRAPH_WAKEUP_MUTUAL =
            new GraphBuilder("wakeup_mutual") {
                @Override
                public TmfGraph build() {
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0Wakeup = new TmfVertex(2);
                    TmfVertex v0Unblock = new TmfVertex(4);
                    TmfVertex v1Unblock = new TmfVertex(2);
                    TmfVertex v1Wakeup = new TmfVertex(4);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(1), EdgeType.RUNNING);
                    graph.append(Actor0, v0Wakeup, EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(3), EdgeType.RUNNING);
                    graph.append(Actor0, v0Unblock, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(5), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, new TmfVertex(0));
                    graph.append(Actor1, new TmfVertex(1), EdgeType.RUNNING);
                    graph.append(Actor1, v1Unblock, EdgeType.BLOCKED);
                    graph.append(Actor1, new TmfVertex(3), EdgeType.RUNNING);
                    graph.append(Actor1, v1Wakeup, EdgeType.RUNNING);
                    graph.append(Actor1, new TmfVertex(5), EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0Wakeup, v1Unblock);
                    graph.link(v1Wakeup, v0Unblock);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0StartBlock = new TmfVertex(3);
                    TmfVertex v0EndBlock = new TmfVertex(4);
                    TmfVertex v1StartBlock = new TmfVertex(3);
                    TmfVertex v1EndBlock = new TmfVertex(4);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(1), EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING);
                    graph.append(Actor0, v0StartBlock, EdgeType.RUNNING);
                    graph.add(Actor0, v0EndBlock);
                    graph.append(Actor0, new TmfVertex(5), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, v1StartBlock);
                    graph.append(Actor1, v1EndBlock, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0StartBlock, v1StartBlock);
                    graph.link(v1EndBlock, v0EndBlock);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0Wakeup = new TmfVertex(2);
                    TmfVertex v0Unblock = new TmfVertex(4);
                    TmfVertex v1Unblock = new TmfVertex(2);
                    TmfVertex v1Wakeup = new TmfVertex(4);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(1), EdgeType.RUNNING);
                    graph.append(Actor0, v0Wakeup, EdgeType.RUNNING);
                    graph.add(Actor0, v0Unblock);
                    graph.append(Actor0, new TmfVertex(5), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, v1Unblock);
                    graph.append(Actor1, new TmfVertex(3), EdgeType.RUNNING);
                    graph.append(Actor1, v1Wakeup, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0Wakeup, v1Unblock);
                    graph.link(v1Wakeup, v0Unblock);
                    return graph;
                }
            };

    /**
     * Many objects wakeup the first object, the calls are embedded
     *
     * <pre>
     * 0: * -R- * -R- * -R- * -B- * -B- * -R- *
     *          |     |           |     |
     * 1:       |     * --- R --- *     |
     *          |                       |
     * 2:       *    ------ R ------    *
     * ...
     * </pre>
     */
    public static GraphBuilder GRAPH_WAKEUP_EMBEDDED =
            new GraphBuilder("wakeup_embeded") {
                private TestGraphWorker fActor2 = new TestGraphWorker(2);

                @Override
                public TmfGraph build() {
                    /* Initialize some vertices */
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0FirstFork = new TmfVertex(2);
                    TmfVertex v0SecondFork = new TmfVertex(4);
                    TmfVertex v0FirstUnblock = new TmfVertex(8);
                    TmfVertex v0SecondUnblock = new TmfVertex(10);
                    TmfVertex v1In = new TmfVertex(4);
                    TmfVertex v1Out = new TmfVertex(8);
                    TmfVertex v2In = new TmfVertex(2);
                    TmfVertex v2Out = new TmfVertex(10);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, v0FirstFork, EdgeType.RUNNING);
                    graph.append(Actor0, v0SecondFork, EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(6), EdgeType.RUNNING);
                    graph.append(Actor0, v0FirstUnblock, EdgeType.BLOCKED);
                    graph.append(Actor0, v0SecondUnblock, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(12), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, v1In);
                    graph.append(Actor1, v1Out, EdgeType.RUNNING);

                    /* Add actor 2's vertices and edges */
                    graph.add(fActor2, v2In);
                    graph.append(fActor2, v2Out, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0FirstFork, v2In);
                    graph.link(v0SecondFork, v1In);
                    graph.link(v1Out, v0FirstUnblock);
                    graph.link(v2Out, v0SecondUnblock);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    /* Initialize some vertices */
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0StartBlock = new TmfVertex(6);
                    TmfVertex v0FirstUnblock = new TmfVertex(8);
                    TmfVertex v0SecondUnblock = new TmfVertex(10);
                    TmfVertex v1In = new TmfVertex(6);
                    TmfVertex v1Out = new TmfVertex(8);
                    TmfVertex v2In = new TmfVertex(8);
                    TmfVertex v2Out = new TmfVertex(10);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(4), EdgeType.RUNNING);
                    graph.append(Actor0, v0StartBlock, EdgeType.RUNNING);
                    graph.add(Actor0, v0FirstUnblock);
                    graph.add(Actor0, v0SecondUnblock);
                    graph.append(Actor0, new TmfVertex(12), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, v1In);
                    graph.append(Actor1, v1Out, EdgeType.RUNNING);

                    /* Add actor 2's vertices and edges */
                    graph.add(fActor2, v2In);
                    graph.append(fActor2, v2Out, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0StartBlock, v1In);
                    graph.link(v1Out, v0FirstUnblock);
                    graph.link(v0FirstUnblock, v2In);
                    graph.link(v2Out, v0SecondUnblock);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    throw new UnsupportedOperationException();
                }
            };

    /**
     * Many objects wakeup the first object, the calls interleave
     *
     * <pre>
     * 0: * -R- * -R- * -R- * -B- * -B- * -R- *
     *          |     |           |     |
     * 1:       * ------ R ------ *     |
     *                |                 |
     * 2:             * ------ R ------ *
     * </pre>
     */
    public static GraphBuilder GRAPH_WAKEUP_INTERLEAVE =
            new GraphBuilder("wakeup_interleave") {
                private TestGraphWorker fActor2 = new TestGraphWorker(2);

                @Override
                public TmfGraph build() {
                    /* Initialize some vertices */
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0FirstFork = new TmfVertex(2);
                    TmfVertex v0SecondFork = new TmfVertex(4);
                    TmfVertex v0FirstUnblock = new TmfVertex(8);
                    TmfVertex v0SecondUnblock = new TmfVertex(10);
                    TmfVertex v1In = new TmfVertex(2);
                    TmfVertex v1Out = new TmfVertex(8);
                    TmfVertex v2In = new TmfVertex(4);
                    TmfVertex v2Out = new TmfVertex(10);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, v0FirstFork, EdgeType.RUNNING);
                    graph.append(Actor0, v0SecondFork, EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(6), EdgeType.RUNNING);
                    graph.append(Actor0, v0FirstUnblock, EdgeType.BLOCKED);
                    graph.append(Actor0, v0SecondUnblock, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(12), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, v1In);
                    graph.append(Actor1, v1Out, EdgeType.RUNNING);

                    /* Add actor 2's vertices and edges */
                    graph.add(fActor2, v2In);
                    graph.append(fActor2, v2Out, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0FirstFork, v1In);
                    graph.link(v0SecondFork, v2In);
                    graph.link(v1Out, v0FirstUnblock);
                    graph.link(v2Out, v0SecondUnblock);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    /* Initialize some vertices */
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0StartBlock = new TmfVertex(6);
                    TmfVertex v0FirstUnblock = new TmfVertex(8);
                    TmfVertex v0SecondUnblock = new TmfVertex(10);
                    TmfVertex v1In = new TmfVertex(6);
                    TmfVertex v1Out = new TmfVertex(8);
                    TmfVertex v2In = new TmfVertex(8);
                    TmfVertex v2Out = new TmfVertex(10);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(2), EdgeType.RUNNING);
                    graph.append(Actor0, new TmfVertex(4), EdgeType.RUNNING);
                    graph.append(Actor0, v0StartBlock, EdgeType.RUNNING);
                    graph.add(Actor0, v0FirstUnblock);
                    graph.add(Actor0, v0SecondUnblock);
                    graph.append(Actor0, new TmfVertex(12), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, v1In);
                    graph.append(Actor1, v1Out, EdgeType.RUNNING);

                    /* Add actor 2's vertices and edges */
                    graph.add(fActor2, v2In);
                    graph.append(fActor2, v2Out, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0StartBlock, v1In);
                    graph.link(v1Out, v0FirstUnblock);
                    graph.link(v0FirstUnblock, v2In);
                    graph.link(v2Out, v0SecondUnblock);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    throw new UnsupportedOperationException();
                }
            };

    /**
     * Objects block when creating new ones, nesting the blocks
     *
     * <pre>
     * ...
     * 0: * -R- * --------------B-------------- * -R- *
     *          |                               |
     * 1:       * -R- * --------B-------- * -R- *
     *                |                   |
     * 2:             * -R- * --B-- * -R- *
     *                      |       |
     * 3:                   * --R-- *
     * </pre>
     */
    public static GraphBuilder GRAPH_NESTED =
            new GraphBuilder("wakeup_nested") {
                private final TestGraphWorker fActor2 = new TestGraphWorker(2);
                private final TestGraphWorker fActor3 = new TestGraphWorker(3);

                @Override
                public TmfGraph build() {
                    /* Initialize some vertices */
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0Fork = new TmfVertex(1);
                    TmfVertex v0Return = new TmfVertex(6);
                    TmfVertex v1In = new TmfVertex(1);
                    TmfVertex v1Fork = new TmfVertex(2);
                    TmfVertex v1Return = new TmfVertex(5);
                    TmfVertex v1End = new TmfVertex(6);
                    TmfVertex v2In = new TmfVertex(2);
                    TmfVertex v2Fork = new TmfVertex(3);
                    TmfVertex v2Return = new TmfVertex(4);
                    TmfVertex v2End = new TmfVertex(5);
                    TmfVertex v3In = new TmfVertex(3);
                    TmfVertex v3End = new TmfVertex(4);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, v0Fork, EdgeType.RUNNING);
                    graph.append(Actor0, v0Return, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(7), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, v1In);
                    graph.append(Actor1, v1Fork, EdgeType.RUNNING);
                    graph.append(Actor1, v1Return, EdgeType.BLOCKED);
                    graph.append(Actor1, v1End, EdgeType.RUNNING);

                    /* Add actor 2's vertices and edges */
                    graph.add(fActor2, v2In);
                    graph.append(fActor2, v2Fork, EdgeType.RUNNING);
                    graph.append(fActor2, v2Return, EdgeType.BLOCKED);
                    graph.append(fActor2, v2End, EdgeType.RUNNING);

                    /* Add actor 3's vertices and edges */
                    graph.add(fActor3, v3In);
                    graph.append(fActor3, v3End, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0Fork, v1In);
                    graph.link(v1Fork, v2In);
                    graph.link(v2Fork, v3In);
                    graph.link(v3End, v2Return);
                    graph.link(v2End, v1Return);
                    graph.link(v1End, v0Return);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    /* Initialize some vertices */
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0Fork = new TmfVertex(1);
                    TmfVertex v0Return = new TmfVertex(6);
                    TmfVertex v1In = new TmfVertex(1);
                    TmfVertex v1Fork = new TmfVertex(2);
                    TmfVertex v1Return = new TmfVertex(5);
                    TmfVertex v1End = new TmfVertex(6);
                    TmfVertex v2In = new TmfVertex(2);
                    TmfVertex v2Fork = new TmfVertex(3);
                    TmfVertex v2Return = new TmfVertex(4);
                    TmfVertex v2End = new TmfVertex(5);
                    TmfVertex v3In = new TmfVertex(3);
                    TmfVertex v3End = new TmfVertex(4);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, v0Fork, EdgeType.RUNNING);
                    graph.add(Actor0, v0Return);
                    graph.append(Actor0, new TmfVertex(7), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, v1In);
                    graph.append(Actor1, v1Fork, EdgeType.RUNNING);
                    graph.add(Actor1, v1Return);
                    graph.append(Actor1, v1End, EdgeType.RUNNING);

                    /* Add actor 2's vertices and edges */
                    graph.add(fActor2, v2In);
                    graph.append(fActor2, v2Fork, EdgeType.RUNNING);
                    graph.add(fActor2, v2Return);
                    graph.append(fActor2, v2End, EdgeType.RUNNING);

                    /* Add actor 3's vertices and edges */
                    graph.add(fActor3, v3In);
                    graph.append(fActor3, v3End, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0Fork, v1In);
                    graph.link(v1Fork, v2In);
                    graph.link(v2Fork, v3In);
                    graph.link(v3End, v2Return);
                    graph.link(v2End, v1Return);
                    graph.link(v1End, v0Return);
                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    return criticalPathBounded();
                }
            };

    /**
     * An object is blocked until a few other objects exchange network messages
     *
     * <pre>
     * 0: * -R- * ----------------------- B ---------------------- * -R- *
     *                                                             |
     * 1:              * -R- * -R- *                               |
     *                        \  ----N----  \                      |
     * 2:                              * -R- * -R- *               |
     *                                        \  ----N----  \      |
     * 3:                                              * -R- * -R- *
     * </pre>
     */
    public static final GraphBuilder GRAPH_NET1 =
            new GraphBuilder("wakeup_net1") {
                private TestGraphWorker fActor2 = new TestGraphWorker(2);
                private TestGraphWorker fActor3 = new TestGraphWorker(3);

                @Override
                public TmfGraph build() {
                    /* Initialize some vertices */
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0Unblock = new TmfVertex(11);
                    TmfVertex v1Send = new TmfVertex(4);
                    TmfVertex v2Rcv = new TmfVertex(7);
                    TmfVertex v2Send = new TmfVertex(8);
                    TmfVertex v3Rcv = new TmfVertex(10);
                    TmfVertex v3End = new TmfVertex(11);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, new TmfVertex(1), EdgeType.RUNNING);
                    graph.append(Actor0, v0Unblock, EdgeType.BLOCKED);
                    graph.append(Actor0, new TmfVertex(12), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, new TmfVertex(3));
                    graph.append(Actor1, v1Send, EdgeType.RUNNING);
                    graph.append(Actor1, new TmfVertex(5), EdgeType.RUNNING);

                    /* Add actor 2's vertices and edges */
                    graph.add(fActor2, new TmfVertex(6));
                    graph.append(fActor2, v2Rcv, EdgeType.RUNNING);
                    graph.append(fActor2, v2Send, EdgeType.RUNNING);

                    /* Add actor 3's vertices and edges */
                    graph.add(fActor3, new TmfVertex(9));
                    graph.append(fActor3, v3Rcv, EdgeType.RUNNING);
                    graph.append(fActor3, v3End, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v1Send, v2Rcv, EdgeType.NETWORK);
                    graph.link(v2Send, v3Rcv, EdgeType.NETWORK);
                    graph.link(v3End, v0Unblock);

                    return graph;
                }

                @Override
                public TmfGraph criticalPathBounded() {
                    /* Initialize some vertices */
                    TmfGraph graph = new TmfGraph();
                    TmfVertex v0Fork = new TmfVertex(1);
                    TmfVertex v0Unblock = new TmfVertex(11);
                    TmfVertex v1Start = new TmfVertex(1);
                    TmfVertex v1Send = new TmfVertex(4);
                    TmfVertex v2Rcv = new TmfVertex(7);
                    TmfVertex v2Send = new TmfVertex(8);
                    TmfVertex v3Rcv = new TmfVertex(10);
                    TmfVertex v3End = new TmfVertex(11);

                    /* Add actor 0's vertices and edges */
                    graph.add(Actor0, new TmfVertex(0));
                    graph.append(Actor0, v0Fork, EdgeType.RUNNING);
                    graph.add(Actor0, v0Unblock);
                    graph.append(Actor0, new TmfVertex(12), EdgeType.RUNNING);

                    /* Add actor 1's vertices and edges */
                    graph.add(Actor1, v1Start);
                    graph.append(Actor1, new TmfVertex(3), EdgeType.UNKNOWN);
                    graph.append(Actor1, v1Send, EdgeType.RUNNING);

                    /* Add actor 2's vertices and edges */
                    graph.add(fActor2, v2Rcv);
                    graph.append(fActor2, v2Send, EdgeType.RUNNING);

                    /* Add actor 3's vertices and edges */
                    graph.add(fActor3, v3Rcv);
                    graph.append(fActor3, v3End, EdgeType.RUNNING);

                    /* Add vertical links */
                    graph.link(v0Fork, v1Start);
                    graph.link(v1Send, v2Rcv, EdgeType.NETWORK);
                    graph.link(v2Send, v3Rcv, EdgeType.NETWORK);
                    graph.link(v3End, v0Unblock);

                    return graph;
                }

                @Override
                public TmfGraph criticalPathUnbounded() {
                    throw new UnsupportedOperationException();
                }
            };

}
