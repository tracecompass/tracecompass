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

import org.eclipse.jdt.annotation.Nullable;

/**
 * Edge of a TmfGraph
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class TmfEdge {

    /**
     * Enumeration of the different types of edges
     *
     * FIXME: this sounds very specific to kernel traces, maybe it shouldn't be
     * here
     *
     * Comment by gbastien: I think the edge itself should be either a green
     * light or a red light and there could be a context specific qualifier to
     * go along
     *
     * How about something like this:
     *
     * <pre>
     * public enum EdgeState {
     *    PASS,
     *    STOP
     *    [,EPS] (for "fake" edge to allow 2 vertices at the same timestamp to many vertical edges)
     * }
     *
     * public ISomeInterface {
     * }
     *
     * public enum KernelEdgeType implements ISomeInterface {
     *     RUNNING, BLOCKED, INTERRUPTED, ...
     * }
     *
     * public class EdgeType {
     *     private EdgeState fState;
     *     private ISomeInterface fQualifier;
     * }
     * </pre>
     */
    public enum EdgeType {

        /**
         * Special edge, so it is possible to have two vertices at the same
         * timestamp
         */
        EPS,
        /** Unknown edge */
        UNKNOWN,
        /** Default type for an edge */
        DEFAULT,
        /** Worker is running */
        RUNNING,
        /** Worker is blocked */
        BLOCKED,
        /** Worker is in an interrupt state */
        INTERRUPTED,
        /** Worker is preempted */
        PREEMPTED,
        /** In a timer */
        TIMER,
        /** Edge represents a network communication */
        NETWORK,
        /** Worker is waiting for user input */
        USER_INPUT,
        /** Block device */
        BLOCK_DEVICE,
        /** inter-processor interrupt */
        IPI,
    }

    private final TmfVertex fVertexFrom;
    private final TmfVertex fVertexTo;
    private EdgeType fType;
    private @Nullable String fQualifier = null;

    /**
     * Constructor
     *
     * @param from
     *            The vertex this edge leaves from
     * @param to
     *            The vertex the edge leads to
     */
    public TmfEdge(TmfVertex from, TmfVertex to) {
        fVertexFrom = from;
        fVertexTo = to;
        fType = EdgeType.DEFAULT;
    }

    /*
     * Getters
     */

    /**
     * Get the origin vertex of this edge
     *
     * @return The origin vertex
     */
    public TmfVertex getVertexFrom() {
        return fVertexFrom;
    }

    /**
     * Get the destination vertex of this edge
     *
     * @return The destination vertex
     */
    public TmfVertex getVertexTo() {
        return fVertexTo;
    }

    /**
     * Get the edge type
     *
     * @return The type of the edge
     */
    public EdgeType getType() {
        return fType;
    }

    /**
     * Sets the edge type
     *
     * @param type
     *            The edge type
     */
    public void setType(final EdgeType type) {
        fType = type;
    }

    /**
     * Sets the edge type
     *
     * @param type
     *            The edge type
     * @param linkQualifier
     *            A string to qualify this link
     * @since 2.1
     */
    public void setType(EdgeType type, @Nullable String linkQualifier) {
        fType = type;
        fQualifier = linkQualifier;
    }

    /**
     * Get the link qualifier, ie a descriptor for the link. This has no effect on
     * the graph or critical path
     *
     * @return The link qualifier
     * @since 2.1
     */
    public @Nullable String getLinkQualifier() {
        return fQualifier;
    }

    /**
     * Returns the duration of the edge
     *
     * @return The duration (in nanoseconds)
     */
    public long getDuration() {
        return fVertexTo.getTs() - fVertexFrom.getTs();
    }

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "[" + fVertexFrom + "--" + fType + "->" + fVertexTo + "]";
    }

}
