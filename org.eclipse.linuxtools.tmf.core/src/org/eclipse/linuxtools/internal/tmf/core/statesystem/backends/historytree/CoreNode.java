/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A Core node is a first-level node of a History Tree which is not a leaf node.
 *
 * It extends HTNode by adding support for child nodes, and also extensions.
 *
 * @author Alexandre Montplaisir
 *
 */
public class CoreNode extends HTNode {

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
    private volatile int extension = -1;

    /**
     * Lock used to gate the accesses to the children arrays. Meant to be a
     * different lock from the one in {@link HTNode}.
     */
    private final ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(false);

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
    protected CoreNode(HTConfig config, int seqNumber, int parentSeqNumber,
            long start) {
        super(config, seqNumber, parentSeqNumber, start);
        this.nbChildren = 0;
        int size = config.getMaxChildren();

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
        int size = getConfig().getMaxChildren();

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
        int size = getConfig().getMaxChildren();

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

    /**
     * Return the number of child nodes this node has.
     *
     * @return The number of child nodes
     */
    public int getNbChildren() {
        rwl.readLock().lock();
        int ret = nbChildren;
        rwl.readLock().unlock();
        return ret;
    }

    /**
     * Get the child node corresponding to the specified index
     *
     * @param index The index of the child to lookup
     * @return The child node
     */
    public int getChild(int index) {
        rwl.readLock().lock();
        try {
            return children[index];
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Get the latest (right-most) child node of this node.
     *
     * @return The latest child node
     */
    public int getLatestChild() {
        rwl.readLock().lock();
        try {
            return children[nbChildren - 1];
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Get the start time of the specified child node.
     *
     * @param index
     *            The index of the child node
     * @return The start time of the that child node.
     */
    public long getChildStart(int index) {
        rwl.readLock().lock();
        try {
            return childStart[index];
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Get the start time of the latest (right-most) child node.
     *
     * @return The start time of the latest child
     */
    public long getLatestChildStart() {
        rwl.readLock().lock();
        try {
            return childStart[nbChildren - 1];
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Get the sequence number of the extension to this node (if there is one).
     *
     * @return The sequence number of the extended node. '-1' is returned if
     *         there is no extension node.
     */
    public int getExtensionSequenceNumber() {
        return extension;
    }

    /**
     * Tell this node that it has a new child (Congrats!)
     *
     * @param childNode
     *            The SHTNode object of the new child
     */
    public void linkNewChild(CoreNode childNode) {
        rwl.writeLock().lock();
        try {
            assert (nbChildren < getConfig().getMaxChildren());

            children[nbChildren] = childNode.getSequenceNumber();
            childStart[nbChildren] = childNode.getNodeStart();
            nbChildren++;

        } finally {
            rwl.writeLock().unlock();
        }
    }

    @Override
    public byte getNodeType() {
        return 1;
    }

    @Override
    protected int getSpecificHeaderSize() {
        int maxChildren = getConfig().getMaxChildren();
        int specificSize =
                  SIZE_INT /* 1x int (extension node) */
                + SIZE_INT /* 1x int (nbChildren) */

                /* MAX_NB * int ('children' table) */
                + SIZE_INT * maxChildren

                /* MAX_NB * Timevalue ('childStart' table) */
                + SIZE_LONG * maxChildren;

        return specificSize;
    }

    @Override
    public String toStringSpecific() {
        /* Only used for debugging, shouldn't be externalized */
        return "Core Node, " + nbChildren + " children, "; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
