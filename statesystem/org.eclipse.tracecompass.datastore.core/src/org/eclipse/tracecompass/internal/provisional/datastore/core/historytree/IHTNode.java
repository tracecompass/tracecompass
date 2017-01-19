/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.exceptions.RangeException;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTInterval;

/**
 * Interface for history tree nodes
 *
 * @author Geneviève Bastien
 * @param <E>
 *            The type of objects that will be saved in the tree
 */
public interface IHTNode<E extends IHTInterval> {

    /**
     * The type of node
     */
    public enum NodeType {
        /**
         * Core node, which is a "front" node, at any level of the tree except
         * the bottom-most one. It has children, and may have extensions.
         */
        CORE((byte) 1),
        /**
         * Leaf node, which is a node at the last bottom level of the tree. It
         * cannot have any children or extensions.
         */
        LEAF((byte) 2);

        private final byte fByte;

        NodeType(byte rep) {
            fByte = rep;
        }

        /**
         * Determine a node type by reading a serialized byte.
         *
         * @param rep
         *            The byte representation of the node type
         * @return The corresponding NodeType
         */
        public static NodeType fromByte(byte rep) {
            switch (rep) {
            case 1:
                return CORE;
            case 2:
                return LEAF;
            default:
                throw new IllegalArgumentException("The NodeType byte " + rep + " is not a valid type"); //$NON-NLS-1$ //$NON-NLS-2$
            }
        }

        /**
         * Get the byte representation of this node type. It can then be read
         * with {@link #fromByte}.
         *
         * @return The byte matching this node type
         */
        public byte toByte() {
            return fByte;
        }
    }

    /**
     * Write this node to the given file channel.
     *
     * @param fc
     *            The file channel to write to (should be sought to be correct
     *            position)
     * @throws IOException
     *             If there was an error writing
     */
    void writeSelf(FileChannel fc) throws IOException;

    /**
     * Get the start time of this node.
     *
     * @return The start time of this node
     */
    long getNodeStart();

    /**
     * Get the end time of this node. Will return {@link Long#MAX_VALUE} if the
     * node is not yet written to disk, as the real end time is not yet known.
     *
     * @return The end time of this node.
     */
    long getNodeEnd();

    /**
     * Get the sequence number of this node.
     *
     * @return The sequence number of this node
     */
    int getSequenceNumber();

    /**
     * Get the sequence number of this node's parent.
     *
     * @return The parent sequence number
     */
    int getParentSequenceNumber();

    /**
     * Change this node's parent. Used when we create a new root node for
     * example.
     *
     * @param newParent
     *            The sequence number of the node that is the new parent
     */
    void setParentSequenceNumber(int newParent);

    /**
     * Return if this node is "done" (full and written to disk).
     *
     * @return If this node is done or not
     */
    boolean isOnDisk();

    /**
     * Add an interval to this node. The caller of this method must make sure that
     * there is enough space on this node to add this object. Also, it is the
     * responsibility of the caller to make sure that the element to add is
     * within the boundary of this node. No check on start and end is expected
     * to be done in this method.
     *
     * @param newInterval
     *            Interval to add to this node
     */
    void add(E newInterval);

    /**
     * We've received word from the containerTree that newest nodes now exist to
     * our right. (Puts isDone = true and sets the endtime)
     *
     * @param endtime
     *            The nodeEnd time that the node will have
     */
    void closeThisNode(long endtime);

    /**
     * Retrieve the intervals inside this node that match the given conditions.
     *
     * @param timeCondition
     *            The time-based RangeCondition
     * @param extraPredicate
     *            Extra predicate to run on the elements. Only those also
     *            matching this predicate will be returned.
     * @return Iterable of the elements in this node matching the condtions
     */
    Iterable<E> getMatchingIntervals(TimeRangeCondition timeCondition,
            Predicate<E> extraPredicate);

    /**
     * Retrieve an interval inside this node that matches the given conditions.
     *
     * @param timeCondition
     *            The time-based RangeCondition
     * @param extraPredicate
     *            Extra predicate to run on the elements. Only intervals also
     *            matching this predicate will be returned.
     * @return An interval matching the conditions or <code>null</code> if no
     *         interval was found
     */
    @Nullable E getMatchingInterval(TimeRangeCondition timeCondition,
            Predicate<E> extraPredicate);

    /**
     * Return the total header size of this node (will depend on the node type).
     *
     * @return The total header size
     */
    int getTotalHeaderSize();

    /**
     * Returns the free space left in the node to write objects
     *
     * @return The amount of free space in the node (in bytes)
     */
    int getNodeFreeSpace();

    /**
     * Returns the current space utilization of this node, as a percentage.
     * (used space / total usable space, which excludes the header)
     *
     * @return The percentage (value between 0 and 100) of space utilization in
     *         this node.
     */
    long getNodeUsagePercent();

    /**
     * Get the type of this node
     *
     * @return The node type
     */
    NodeType getNodeType();

    /**
     * Return whether this node has elements in it or is empty
     *
     * @return <code>true</code> if the node is empty
     */
    boolean isEmpty();

    // ---------------------------------------
    // Methods for nodes with children. Leaf nodes can use these default methods
    // ---------------------------------------

    /**
     * Return the number of child nodes this node has.
     *
     * @return The number of child nodes
     */
    default int getNbChildren() {
        return 0;
    }

    /**
     * Get the child node corresponding to the specified index. It will throw an
     * {@link IndexOutOfBoundsException} if there is no children at this index.
     *
     * @param index
     *            The index of the child to lookup
     * @return The child node
     */
    default int getChild(int index) {
        throw new IndexOutOfBoundsException("This node does not have any children"); //$NON-NLS-1$
    }

    /**
     * Get the latest (right-most) child node of this node. This applies only if
     * the node is allowed to have children, ie is a {@link NodeType#CORE} node,
     * otherwise this method is not supported.
     *
     * @return The latest child node
     */
    default int getLatestChild() {
        throw new UnsupportedOperationException("This node does not support children"); //$NON-NLS-1$
    }

    /**
     * Tell this node that it has a new child. This applies only if the node is
     * allowed to have children, ie is a {@link NodeType#CORE} node, otherwise
     * this method is not supported.
     *
     * @param childNode
     *            The new child node to add to this one
     */
    default void linkNewChild(IHTNode<E> childNode) {
        throw new UnsupportedOperationException("This node does not support children"); //$NON-NLS-1$
    }

    /**
     * Method to select the sequence numbers for the children of the current
     * node that intersect the given timestamp. Useful when navigating the tree.
     *
     * @param timeCondition
     *            The time-based range condition to choose which child is the
     *            next one
     * @return Collection of sequence numbers of the child nodes that intersect
     *         the time condition, non-null empty collection if this is a Leaf
     *         Node
     * @throws RangeException
     *             If t is out of the node's range
     */
    default Collection<Integer> selectNextChildren(TimeRangeCondition timeCondition) {
        return Collections.emptyList();
    }

}
