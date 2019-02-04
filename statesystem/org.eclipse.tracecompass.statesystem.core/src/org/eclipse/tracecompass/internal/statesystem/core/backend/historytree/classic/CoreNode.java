/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson, École Polytechnique de Montréal, and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Florian Wininger - Add Extension and Leaf Node
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.classic;

import java.nio.ByteBuffer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.IntegerRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTConfig;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTNode;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.ParentNode;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;

/**
 * A Core node is a first-level node of a History Tree which is not a leaf node.
 *
 * It extends HTNode by adding support for child nodes, and also extensions.
 *
 * @author Alexandre Montplaisir
 */
public final class CoreNode extends ParentNode {

    /** Nb. of children this node has */
    private int fNbChildren;

    /** Seq. numbers of the children nodes (size = MAX_NB_CHILDREN) */
    private int[] fChildren;

    /** Start times of each of the children (size = MAX_NB_CHILDREN) */
    private long[] fChildStart;
    private long[] fChildEnd;
    private int[] fChildMin;
    private int[] fChildMax;

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
    public CoreNode(HTConfig config, int seqNumber, int parentSeqNumber,
            long start) {
        super(config, seqNumber, parentSeqNumber, start);
        fNbChildren = 0;
        int size = config.getMaxChildren();

        /*
         * We instantiate the two following arrays at full size right away,
         * since we want to reserve that space in the node's header.
         * "nbChildren" will tell us how many relevant entries there are in
         * those tables.
         */
        fChildren = new int[size];
        fChildStart = new long[size];
        Arrays.fill(fChildStart, Long.MIN_VALUE);
        fChildEnd = new long[size];
        Arrays.fill(fChildEnd, Long.MAX_VALUE);
        fChildMin = new int[size];
        fChildMax = new int[size];
        Arrays.fill(fChildMax, Integer.MAX_VALUE);
    }

    @Override
    protected void readSpecificHeader(ByteBuffer buffer) {
        int size = getConfig().getMaxChildren();

        fNbChildren = buffer.getInt();

        fChildren = new int[size];
        for (int i = 0; i < size; i++) {
            fChildren[i] = buffer.getInt();
        }

        fChildStart = new long[size];
        for (int i = 0; i < size; i++) {
            fChildStart[i] = buffer.getLong();
        }

        fChildEnd = new long[size];
        for (int i = 0; i < size; i++) {
            fChildEnd[i] = buffer.getLong();
        }

        fChildMin = new int[size];
        for (int i = 0; i < size; i++) {
            fChildMin[i] = buffer.getInt();
        }

        fChildMax = new int[size];
        for (int i = 0; i < size; i++) {
            fChildMax[i] = buffer.getInt();
        }
    }

    @Override
    protected void writeSpecificHeader(ByteBuffer buffer) {
        buffer.putInt(fNbChildren);

        /* Write the "children's sequence number" array */
        for (int child : fChildren) {
            buffer.putInt(child);
        }

        /* Write the "children's start times" array */
        for (long start : fChildStart) {
            buffer.putLong(start);
        }

        /* Write the "children's end times" array */
        for (long end : fChildEnd) {
            buffer.putLong(end);
        }

        /* Write the "children's min quark" array */
        for (int min : fChildMin) {
            buffer.putInt(min);
        }

        /* Write the "children's max quark" array */
        for (int max : fChildMax) {
            buffer.putInt(max);
        }
    }

    @Override
    public int getNbChildren() {
        rwl.readLock().lock();
        try {
            return fNbChildren;
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public int getChild(int index) {
        rwl.readLock().lock();
        try {
            return fChildren[index];
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public int getLatestChild() {
        rwl.readLock().lock();
        try {
            return fChildren[fNbChildren - 1];
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public long getChildStart(int index) {
        rwl.readLock().lock();
        try {
            return fChildStart[index];
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public long getChildEnd(int index) {
        rwl.readLock().lock();
        try {
            return fChildEnd[index];
        } finally {
            rwl.readLock().unlock();
        }
    }

    /**
     * Updates the end time for child node in header when closing branch
     *
     * @param child
     *            Child node whose bounds we must apply
     * @return -1 if child was not one of this node's children or index of the child
     *         within this node's children
     */
    public int closeChild(HTNode child) {
        int childSequenceNumber = child.getSequenceNumber();
        for (int i = 0; i < getNbChildren(); i++) {
            if (childSequenceNumber == getChild(i)) {
                fChildEnd[i] = child.getNodeEnd();
                fChildMin[i] = child.getMinQuark();
                fChildMax[i] = child.getMaxQuark();
                return i;
            }
        }
        return -1;
    }

    @Override
    public void linkNewChild(HTNode childNode) {
        rwl.writeLock().lock();
        try {
            if (fNbChildren >= getConfig().getMaxChildren()) {
                throw new IllegalStateException("Asked to link another child but parent already has maximum number of children"); //$NON-NLS-1$
            }

            fChildren[fNbChildren] = childNode.getSequenceNumber();
            fChildStart[fNbChildren] = childNode.getNodeStart();
            fNbChildren++;

        } finally {
            rwl.writeLock().unlock();
        }
    }

    @Override
    public Collection<Integer> selectNextChildren(long t) throws TimeRangeException {
        if (t < getNodeStart() || (isOnDisk() && t > getNodeEnd())) {
            throw new TimeRangeException("Requesting children outside the node's range: " + t); //$NON-NLS-1$
        }
        rwl.readLock().lock();
        try {
            List<Integer> next = new ArrayList<>();
            for (int i = 0; i < fNbChildren; i++) {
                if (t >= fChildStart[i] && t <= fChildEnd[i]) {
                    next.add(fChildren[i]);
                }
            }

            return next;
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public Collection<Integer> selectNextChildren(long t, int k) throws TimeRangeException {
        if (t < getNodeStart() || (isOnDisk() && t > getNodeEnd())) {
            throw new TimeRangeException("Requesting children outside the node's range: " + t); //$NON-NLS-1$
        }
        rwl.readLock().lock();
        try {
            List<Integer> next = new ArrayList<>();
            for (int i = 0; i < fNbChildren; i++) {
                if (t >= fChildStart[i] && t <= fChildEnd[i]
                        && k >= fChildMin[i] && k <= fChildMax[i]) {
                    next.add(fChildren[i]);
                }
            }

            return next;
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public void queueNextChildren2D(IntegerRangeCondition quarks, TimeRangeCondition times, Deque<Integer> queue, boolean reverse) {
        rwl.readLock().lock();
        try {
            /* Selectively search children */
            /*
             * Add all the nodes at the beginning of the queue for depth-first
             * search, the 'reverse' will specify which to insert first
             */
            Deque<Integer> toAdd = new ArrayDeque<>();
            for (int child = 0; child < fNbChildren; child++) {
                if (times.intersects(fChildStart[child], fChildEnd[child])
                        && quarks.intersects(fChildMin[child], fChildMax[child])) {
                    int potentialNextSeqNb = fChildren[child];
                    // Add them in the add list in the reverse order, so the
                    // order will be right in the final queue
                    if (!reverse) {
                        toAdd.addFirst(potentialNextSeqNb);
                    } else {
                        toAdd.add(potentialNextSeqNb);
                    }
                }
            }
            for (Integer seqNum : toAdd) {
                queue.addFirst(seqNum);
            }
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public NodeType getNodeType() {
        return NodeType.CORE;
    }

    @Override
    protected int getSpecificHeaderSize() {
        int maxChildren = getConfig().getMaxChildren();
        return    Integer.BYTES /* 1x int (nbChildren) */

                /* MAX_NB * int ('children' table) */
                + Integer.BYTES * maxChildren

                /* MAX_NB * Timevalue ('childStart' table) */
                + Long.BYTES * maxChildren
                /* MAX_NB * Timevalue ('childEnd' table) */
                + Long.BYTES * maxChildren

                /* MAX_NB * quark ('childMin' and 'childMax' table) */
                + 2 * Integer.BYTES * maxChildren;
    }

    @Override
    public int getMinQuark() {
        int min = super.getMinQuark();
        rwl.readLock().lock();
        try {
            for (int i = 0; i < fNbChildren; i++) {
                min = Integer.min(min, fChildMin[i]);
            }
            return min;
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public int getMaxQuark() {
        int max = super.getMaxQuark();
        rwl.readLock().lock();
        try {
            for (int i = 0; i < fNbChildren; i++) {
                max = Integer.max(max, fChildMax[i]);
            }
            return max;
        } finally {
            rwl.readLock().unlock();
        }
    }

    @Override
    public String toStringSpecific() {
        /* Only used for debugging, shouldn't be externalized */
        return String.format("Core Node, %d children %s", //$NON-NLS-1$
                fNbChildren, Arrays.toString(Arrays.copyOf(fChildren, fNbChildren)));
    }

}
