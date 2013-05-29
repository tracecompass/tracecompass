/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree;

import java.nio.ByteBuffer;

/**
 * A Core node is a first-level node of a History Tree which is not a leaf node.
 *
 * It extends HTNode by adding support for child nodes, and also extensions.
 *
 * @author alexmont
 *
 */
class CoreNode extends HTNode {

    /** Number of bytes in a int */
    private static final int SIZE_INT = 4;

    /** Number of bytes in a long */
    private static final int SIZE_LONG = 8;

    /** Nb. of children this node has */
    private int nbChildren;

    /** Seq. numbers of the children nodes (size = MAX_NB_CHILDREN) */
    private int[] children;

    /** Start times of each of the children (size = MAX_NB_CHILDREN) */
    private long[] childStart;

    /** Seq number of this node's extension. -1 if none */
    private int extension;

    /**
     * Initial constructor. Use this to initialize a new EMPTY node.
     *
     * @param tree
     *            The HistoryTree to which this node belongs
     * @param seqNumber
     *            The (unique) sequence number assigned to this particular node
     * @param parentSeqNumber
     *            The sequence number of this node's parent node
     * @param start
     *            The earliest timestamp stored in this node
     */
    CoreNode(HistoryTree tree, int seqNumber, int parentSeqNumber,
            long start) {
        super(tree, seqNumber, parentSeqNumber, start);
        this.nbChildren = 0;
        int size = getTree().getConfig().getMaxChildren();

        /*
         * We instantiate the two following arrays at full size right away,
         * since we want to reserve that space in the node's header.
         * "this.nbChildren" will tell us how many relevant entries there are in
         * those tables.
         */
        this.children = new int[size];
        this.childStart = new long[size];
    }

    @Override
    protected void readSpecificHeader(ByteBuffer buffer) {
        int size = getTree().getConfig().getMaxChildren();

        extension = buffer.getInt();
        nbChildren = buffer.getInt();

        children = new int[size];
        for (int i = 0; i < nbChildren; i++) {
            children[i] = buffer.getInt();
        }
        for (int i = nbChildren; i < size; i++) {
            buffer.getInt();
        }

        this.childStart = new long[size];
        for (int i = 0; i < nbChildren; i++) {
            childStart[i] = buffer.getLong();
        }
        for (int i = nbChildren; i < size; i++) {
            buffer.getLong();
        }
    }

    @Override
    protected void writeSpecificHeader(ByteBuffer buffer) {
        int size = getTree().getConfig().getMaxChildren();

        buffer.putInt(extension);
        buffer.putInt(nbChildren);

        /* Write the "children's seq number" array */
        for (int i = 0; i < nbChildren; i++) {
            buffer.putInt(children[i]);
        }
        for (int i = nbChildren; i < size; i++) {
            buffer.putInt(0);
        }

        /* Write the "children's start times" array */
        for (int i = 0; i < nbChildren; i++) {
            buffer.putLong(childStart[i]);
        }
        for (int i = nbChildren; i < size; i++) {
            buffer.putLong(0);
        }
    }

    int getNbChildren() {
        return nbChildren;
    }

    int getChild(int index) {
        return children[index];
    }

    int getLatestChild() {
        return children[nbChildren - 1];
    }

    long getChildStart(int index) {
        return childStart[index];
    }

    long getLatestChildStart() {
        return childStart[nbChildren - 1];
    }

    int getExtensionSequenceNumber() {
        return extension;
    }

    /**
     * Tell this node that it has a new child (Congrats!)
     *
     * @param childNode
     *            The SHTNode object of the new child
     */
    void linkNewChild(CoreNode childNode) {
        assert (this.nbChildren < getTree().getConfig().getMaxChildren());

        this.children[nbChildren] = childNode.getSequenceNumber();
        this.childStart[nbChildren] = childNode.getNodeStart();
        this.nbChildren++;
    }

    @Override
    protected byte getNodeType() {
        return 1;
    }

    @Override
    protected int getTotalHeaderSize() {
        int maxChildren = getTree().getConfig().getMaxChildren();
        int specificSize =
                  SIZE_INT /* 1x int (extension node) */
                + SIZE_INT /* 1x int (nbChildren) */

                /* MAX_NB * int ('children' table) */
                + SIZE_INT * maxChildren

                /* MAX_NB * Timevalue ('childStart' table) */
                + SIZE_LONG * maxChildren;

        return COMMON_HEADER_SIZE + specificSize;
    }

    @Override
    protected String toStringSpecific() {
        /* Only used for debugging, shouldn't be externalized */
        return "Core Node, " + nbChildren + " children, "; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
