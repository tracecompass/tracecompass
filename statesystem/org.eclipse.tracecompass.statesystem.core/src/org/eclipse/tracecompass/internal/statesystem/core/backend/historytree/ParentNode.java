/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson, École Polytechnique de Montréal, and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.util.Collection;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;

/**
 * A Core node is a first-level node of a History Tree which is not a leaf node.
 *
 * It extends HTNode by adding support for child nodes, and also extensions.
 *
 * @author Alexandre Montplaisir
 * @author Florian Wininger
 * @author Loïc Prieur-Drevon
 */
public abstract class ParentNode extends HTNode {

    /**
     * Initial constructor. Use this to initialize a new EMPTY node.
     *
     * @param config
     *            Configuration of the History Tree
     * @param seqNumber
     *            The (unique) sequence number assigned to this particular node
     * @param parentSeqNumber
     *            The sequence number of this node's parent node
     * @param start
     *            The earliest timestamp stored in this node
     */
    public ParentNode(HTConfig config, int seqNumber, int parentSeqNumber,
            long start) {
        super(config, seqNumber, parentSeqNumber, start);
    }

    /**
     * Return the number of child nodes this node has.
     *
     * @return The number of child nodes
     */
    public abstract int getNbChildren();

    /**
     * Get the child node corresponding to the specified index
     *
     * @param index The index of the child to lookup
     * @return The child node
     */
    public abstract int getChild(int index);

    /**
     * Get the latest (right-most) child node of this node.
     *
     * @return The latest child node
     */
    public abstract int getLatestChild();

    /**
     * Get the start time of the specified child node.
     *
     * @param index
     *            The index of the child node
     * @return The start time of the that child node.
     */
    public abstract long getChildStart(int index);

    /**
     * Tell this node that it has a new child (Congrats!)
     *
     * @param childNode
     *            The SHTNode object of the new child
     */
    public abstract void linkNewChild(HTNode childNode);

    /**
     * Inner method to select the sequence numbers for the children of the
     * current node that intersect the given timestamp. Useful for moving down
     * the tree.
     *
     * @param t
     *            The timestamp to choose which child is the next one
     * @return Collection of sequence numbers of the child nodes that intersect
     *         t, non-null empty collection if this is a Leaf Node
     * @throws TimeRangeException
     *             If t is out of the node's range
     */
    public abstract @NonNull Collection<@NonNull Integer> selectNextChildren(long t);

    /**
     * Get a collection of sequence numbers for the children nodes that contain
     * intervals with quarks from quarks, and times intersecting times from
     * times.
     *
     * @param quarks
     *            NumCondition on the quarks we are interested in.
     * @param subTimes
     *            NumCondition on the time stamps we are interested in.
     * @return a collection of the sequence numbers for the children that match
     *         the conditions.
     */
    public abstract Collection<Integer> selectNextChildren2D(IntegerRangeCondition quarks, TimeRangeCondition subTimes);

    /**
     * Get the end time for this child, the last child's end time will be
     * Long.MAX_VALUE if it isn't written to disk.
     *
     * @param index
     *            child position in this Parent
     * @return the next child's endTime
     */
    public abstract long getChildEnd(int index);

}
