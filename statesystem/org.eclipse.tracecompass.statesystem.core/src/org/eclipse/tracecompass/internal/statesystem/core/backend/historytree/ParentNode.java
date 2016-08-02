/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson, École Polytechnique de Montréal, and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

/**
 * A Core node is a first-level node of a History Tree which is not a leaf node.
 *
 * It extends HTNode by adding support for child nodes, and also extensions.
 *
 * @author Alexandre Montplaisir
 * @author Florian Wininger
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
     * Get the start time of the latest (right-most) child node.
     *
     * @return The start time of the latest child
     */
    public abstract long getLatestChildStart();

    /**
     * Tell this node that it has a new child (Congrats!)
     *
     * @param childNode
     *            The SHTNode object of the new child
     */
    public abstract void linkNewChild(HTNode childNode);

}
