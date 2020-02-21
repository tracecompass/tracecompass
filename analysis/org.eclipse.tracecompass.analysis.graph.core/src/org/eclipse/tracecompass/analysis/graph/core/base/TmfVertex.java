/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Francis Giraldeau - Initial implementation and API
 *   Geneviève Bastien - Initial implementation and API
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.graph.core.base;

import java.util.Comparator;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.graph.core.base.TmfEdge.EdgeType;
import org.eclipse.tracecompass.internal.analysis.graph.core.base.Messages;

/**
 * Timed vertex for TmfGraph
 *
 * @author Francis Giraldeau
 * @author Geneviève Bastien
 */
public class TmfVertex implements Comparable<TmfVertex> {

    private static final String UNKNOWN_EDGE_DIRECTION_TYPE = "Unknown edge direction type : "; //$NON-NLS-1$

    private static long count = 0;

    /**
     * Describe the four edges coming in and out of a vertex
     */
    public enum EdgeDirection {
        /**
         * Constant for the outgoing vertical edge (to other object)
         */
        OUTGOING_VERTICAL_EDGE,
        /**
         * Constant for the incoming vertical edge (from other object)
         */
        INCOMING_VERTICAL_EDGE,
        /**
         * Constant for the outgoing horizontal edge (to same object)
         */
        OUTGOING_HORIZONTAL_EDGE,
        /**
         * Constant for the incoming horizontal edge (from same object)
         */
        INCOMING_HORIZONTAL_EDGE
    }

    /**
     * Compare vertices by ascending timestamps
     */
    public static Comparator<TmfVertex> ascending = Objects.requireNonNull(Comparator.nullsLast(Comparator.comparing(TmfVertex::getTs)));

    /**
     * Compare vertices by descending timestamps
     */
    public static Comparator<TmfVertex> descending = Objects.requireNonNull(Comparator.nullsLast(Comparator.comparing(TmfVertex::getTs).reversed()));

    private @Nullable TmfEdge fOutgoingVertical = null;
    private @Nullable TmfEdge fIncomingVertical = null;
    private @Nullable TmfEdge fOutgoingHorizontal = null;
    private @Nullable TmfEdge fIncomingHorizontal = null;
    private final long fTimestamp;
    private final long fId;

    /**
     * Default Constructor
     */
    public TmfVertex() {
        this(0);
    }

    /**
     * Constructor with timestamp
     *
     * @param ts
     *            The vertex's timestamp
     */
    public TmfVertex(final long ts) {
        fTimestamp = ts;
        synchronized (TmfVertex.class) {
            fId = count++;
        }
    }

    /**
     * Copy constructor. Keeps same timestamp, but does not keep edges
     *
     * @param node
     *            vertex to copy
     */
    public TmfVertex(TmfVertex node) {
        this(node.fTimestamp);
    }

    /**
     * Copy constructor, but changes the timestamp
     *
     * @param node
     *            vertex to copy
     * @param ts
     *            The timestamp of this new node
     */
    public TmfVertex(TmfVertex node, final long ts) {
        fTimestamp = ts;
        synchronized (TmfVertex.class) {
            fId = count++;
        }
        fOutgoingVertical = node.fOutgoingVertical;
        fIncomingVertical = node.fIncomingVertical;
        fOutgoingHorizontal = node.fOutgoingHorizontal;
        fIncomingHorizontal = node.fIncomingHorizontal;
    }

    /*
     * Getters and setters
     */

    /**
     * Returns the timestamps of this node
     *
     * @return the timstamp
     */
    public long getTs() {
        return fTimestamp;
    }

    /**
     * Returns the unique ID of this node
     *
     * @return the vertex's id
     */
    public long getID() {
        return fId;
    }

    /**
     * Adds an horizontal edge from the current vertex to the 'to' vertex
     *
     * @param to
     *            The vertex to link to, belongs to the same object
     *
     * @return The new edge
     */
    public TmfEdge linkHorizontal(TmfVertex to) {
        checkTimestamps(to);
        checkNotSelf(to);
        return linkHorizontalRaw(to);
    }

    /**
     * Adds an horizontal edge from the current vertex to the 'to' vertex
     *
     * @param to
     *            The vertex to link to, belongs to the same object
     * @param type
     *            The type of the link to add
     * @param linkQualifier
     *            An optional qualifier to identify this link
     *
     * @return The new edge
     * @since 2.1
     */
    public TmfEdge linkHorizontal(TmfVertex to, EdgeType type, @Nullable String linkQualifier) {
        checkTimestamps(to);
        checkNotSelf(to);
        TmfEdge link = linkHorizontalRaw(to);
        link.setType(type, linkQualifier);
        return link;
    }

    private TmfEdge linkHorizontalRaw(TmfVertex node) {
        TmfEdge link = new TmfEdge(this, node);
        fOutgoingHorizontal = link;
        node.fIncomingHorizontal = link;
        return link;
    }

    /**
     * Adds a vertical edge from the current vertex to the 'to' vertex
     *
     * @param to
     *            The vertex to link to, belongs to a different object
     * @return The new edge
     */
    public TmfEdge linkVertical(TmfVertex to) {
        checkTimestamps(to);
        checkNotSelf(to);
        return linkVerticalRaw(to);
    }

    /**
     * Adds a vertical edge from the current vertex to the 'to' vertex
     *
     * @param to
     *            The vertex to link to, belongs to a different object
     * @param type
     *            The type of the link to add
     * @param linkQualifier
     *            An optional qualifier to identify this link
     * @return The new edge
     * @since 2.1
     */
    public TmfEdge linkVertical(TmfVertex to, EdgeType type, @Nullable String linkQualifier) {
        checkTimestamps(to);
        checkNotSelf(to);
        TmfEdge link = linkVerticalRaw(to);
        link.setType(type, linkQualifier);
        return link;
    }

    private TmfEdge linkVerticalRaw(TmfVertex to) {
        TmfEdge link = new TmfEdge(this, to);
        fOutgoingVertical = link;
        to.fIncomingVertical = link;
        return link;
    }

    private void checkTimestamps(TmfVertex to) {
        if (this.fTimestamp > to.fTimestamp) {
            throw new IllegalArgumentException(Messages.TmfVertex_ArgumentTimestampLower +
                    String.format(": (curr=%d,next=%d,elapsed=%d)", fTimestamp, to.fTimestamp, to.fTimestamp - fTimestamp)); //$NON-NLS-1$
        }
    }

    private void checkNotSelf(TmfVertex to) {
        if (this == to) {
            throw new IllegalArgumentException(Messages.TmfVertex_CannotLinkToSelf);
        }
    }


    /**
     * Get an edge to or from this vertex in the appropriate direction
     *
     * @param dir
     *            The direction of the requested edge
     * @return The edge from this vertex to the requested direction
     */
    public @Nullable TmfEdge getEdge(EdgeDirection dir) {
        switch (dir) {
        case OUTGOING_VERTICAL_EDGE:
            return fOutgoingVertical;
        case INCOMING_VERTICAL_EDGE:
            return fIncomingVertical;
        case OUTGOING_HORIZONTAL_EDGE:
            return fOutgoingHorizontal;
        case INCOMING_HORIZONTAL_EDGE:
            return fIncomingHorizontal;
        default:
            throw new IllegalStateException(UNKNOWN_EDGE_DIRECTION_TYPE + dir);
        }
    }

    /**
     * Removes a directed edge from this vertex. The edge in that direction will
     * be null.
     *
     * @param dir
     *            The direction to remove the edge from
     */
    public void removeEdge(EdgeDirection dir) {
        switch (dir) {
        case OUTGOING_VERTICAL_EDGE:
            fOutgoingVertical = null;
            break;
        case INCOMING_VERTICAL_EDGE:
            fIncomingVertical = null;
            break;
        case OUTGOING_HORIZONTAL_EDGE:
            fOutgoingHorizontal = null;
            break;
        case INCOMING_HORIZONTAL_EDGE:
            fIncomingHorizontal = null;
            break;
        default:
            throw new IllegalStateException(UNKNOWN_EDGE_DIRECTION_TYPE + dir);
        }
    }

    /**
     * Get the neighbor of a vertex from a directed edge. Incoming edges will
     * return the vertex from the edge and outgoing edges will return the vertex
     * to. This method is a utility method that can be used in code where the
     * direction is a variable. If the edge direction is known (using one of the
     * EdgeDirection constant), it is preferable to use the
     * {@link TmfEdge#getVertexFrom()} and {@link TmfEdge#getVertexTo()}
     * directly.
     *
     * @param edge
     *            The edge for which to get the right neighbor
     * @param dir
     *            The direction of this edge
     * @return The vertex that neighbors another vertex in the requested
     *         direction
     */
    public static TmfVertex getNeighborFromEdge(TmfEdge edge, EdgeDirection dir) {
        switch (dir) {
        case OUTGOING_VERTICAL_EDGE:
        case OUTGOING_HORIZONTAL_EDGE:
            return edge.getVertexTo();
        case INCOMING_VERTICAL_EDGE:
        case INCOMING_HORIZONTAL_EDGE:
            return edge.getVertexFrom();
        default:
            throw new IllegalStateException(UNKNOWN_EDGE_DIRECTION_TYPE + dir);
        }
    }

    @Override
    public int compareTo(@Nullable TmfVertex other) {
        if (other == null) {
            return 1;
        }
        return Long.compare(fTimestamp, other.fTimestamp);
    }

    @Override
    public String toString() {
        return "[" + fId + "," + fTimestamp + "]"; //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
    }

}
