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

import java.util.Map;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.analysis.graph.core.base.Messages;
import org.eclipse.tracecompass.tmf.core.dataprovider.X11ColorUtils;
import org.eclipse.tracecompass.tmf.core.model.StyleProperties;

import com.google.common.collect.ImmutableMap;

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
        EPS(String.valueOf(Messages.TmfEdge_Unknown), 0x40, 0x3b, 0x33, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** Unknown edge */
        UNKNOWN(String.valueOf(Messages.TmfEdge_Unknown), 0x40, 0x3b, 0x33, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** Default type for an edge */
        DEFAULT(String.valueOf(Messages.TmfEdge_Unknown), 0x40, 0x3b, 0x33, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** Worker is running */
        RUNNING(String.valueOf(Messages.TmfEdge_Running), 0x33, 0x99, 0x00, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupRunning)),
        /** Worker is blocked */
        BLOCKED(String.valueOf(Messages.TmfEdge_Blocked), 220, 20, 60, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** Worker is in an interrupt state */
        INTERRUPTED(String.valueOf(Messages.TmfEdge_Interrupted), 0xff, 0xdc, 0x00, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** Worker is preempted */
        PREEMPTED(String.valueOf(Messages.TmfEdge_Preempted), 0xc8, 0x64, 0x00, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** In a timer */
        TIMER(String.valueOf(Messages.TmfEdge_Timer), 0x33, 0x66, 0x99, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** Edge represents a network communication */
        NETWORK(String.valueOf(Messages.TmfEdge_Network), 0xff, 0x9b, 0xff, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** Worker is waiting for user input */
        USER_INPUT(String.valueOf(Messages.TmfEdge_UserInput), 0x5a, 0x01, 0x01, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** Block device */
        BLOCK_DEVICE(String.valueOf(Messages.TmfEdge_BlockDevice), 0x66, 0x00, 0xcc, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked)),
        /** inter-processor interrupt */
        IPI(String.valueOf(Messages.TmfEdge_IPI), 0x66, 0x66, 0xcc, 255, 1.0f, String.valueOf(Messages.TmfEdge_GroupBlocked));

        private final Map<String, Object> fMap;

        /**
         * A Linux style
         *
         * @param label
         *            the label of the style
         * @param red
         *            red value, must be between 0 and 255
         * @param green
         *            green value, must be between 0 and 255
         * @param blue
         *            blue value, must be between 0 and 255
         * @param alpha
         *            value, must be between 0 and 255
         * @param heightFactor
         *            the hint of the height, between 0 and 1.0
         * @param group
         *            the group string this style belongs to
         */
        private EdgeType(String label, int red, int green, int blue, int alpha, float heightFactor, String group) {
            if (red > 255 || red < 0) {
                throw new IllegalArgumentException("Red needs to be between 0 and 255"); //$NON-NLS-1$
            }
            if (green > 255 || green < 0) {
                throw new IllegalArgumentException("Green needs to be between 0 and 255"); //$NON-NLS-1$
            }
            if (blue > 255 || blue < 0) {
                throw new IllegalArgumentException("Blue needs to be between 0 and 255"); //$NON-NLS-1$
            }
            if (alpha > 255 || alpha < 0) {
                throw new IllegalArgumentException("alpha needs to be between 0 and 255"); //$NON-NLS-1$
            }
            if (heightFactor > 1.0 || heightFactor < 0) {
                throw new IllegalArgumentException("Height factor needs to be between 0 and 1.0, given hint : " + heightFactor); //$NON-NLS-1$
            }
            fMap = ImmutableMap.of(StyleProperties.STYLE_NAME, label,
                    StyleProperties.BACKGROUND_COLOR, X11ColorUtils.toHexColor(red, green, blue),
                    StyleProperties.HEIGHT, heightFactor,
                    StyleProperties.OPACITY, (float) alpha / 255,
                    StyleProperties.STYLE_GROUP, group);
        }

        /**
         * Get a map of the values corresponding to the fields in
         * {@link StyleProperties}
         *
         * @return the map corresponding to the api defined in
         *         {@link StyleProperties}
         * @since 2.1
         */
        public Map<String, Object> toMap() {
            return fMap;
        }
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

    @Override
    public String toString() {
        return "[" + fVertexFrom + "--" + fType + "->" + fVertexTo + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

}
