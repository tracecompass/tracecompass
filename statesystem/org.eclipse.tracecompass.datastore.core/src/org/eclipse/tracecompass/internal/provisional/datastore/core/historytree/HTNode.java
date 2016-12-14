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
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.RangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.exceptions.RangeException;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree.IHTNodeFactory;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.provisional.datastore.core.serialization.ISafeByteBufferReader;
import org.eclipse.tracecompass.internal.provisional.datastore.core.serialization.SafeByteBufferFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * The base class for all the types of nodes that go in the History Tree.
 *
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 * @param <E>
 *            The type of objects that will be saved in the tree
 */
public class HTNode<E extends IHTInterval> implements IHTNode<E> {

    // ------------------------------------------------------------------------
    // Class fields
    // ------------------------------------------------------------------------

    /**
     * Size of the basic node header. This is backward-compatible with previous
     * state sytem history trees
     *
     * <pre>
     *  1 - byte (the type of node)
     * 16 - 2x long (start time, end time)
     * 16 - 3x int (seq number, parent seq number, intervalcount)
     *  1 - byte (done or not)
     * </pre>
     */
    @VisibleForTesting
    public static final int COMMON_HEADER_SIZE = Byte.BYTES
            + 2 * Long.BYTES
            + 3 * Integer.BYTES
            + Byte.BYTES;

    // ------------------------------------------------------------------------
    // Attributes
    // ------------------------------------------------------------------------

    /**
     * Default implementation of the interval comparator, which sorts first by
     * end times, then by start times
     */
    private final Comparator<E> fDefaultIntervalComparator = Comparator
            .comparingLong(E::getEnd)
            .thenComparingLong(E::getStart);

    /* Node configuration, defined by the history tree */
    private final int fBlockSize;
    private final int fMaxChildren;

    /* Time range of this node */
    private final long fNodeStart;
    private long fNodeEnd;

    /* Sequence number = position in the node section of the file */
    private final int fSequenceNumber;
    private int fParentSequenceNumber; /* = -1 if this node is the root node */

    /* Sum of bytes of all objects in the node */
    private int fSizeOfContentSection;

    /*
     * True if this node was saved on disk (meaning its end time is now fixed)
     */
    private volatile boolean fIsOnDisk;

    /* List containing all the intervals contained in this node */
    private final List<E> fIntervals;

    /* Lock used to protect the accesses to objects, nodeEnd and such */
    private final ReentrantReadWriteLock fRwl = new ReentrantReadWriteLock(false);

    /* Object containing extra data for core nodes */
    private final @Nullable CoreNodeData fExtraData;

    /**
     * A class that encapsulates data about children of this node. This class
     * will be constructed by the core node and contains the extra header data,
     * methods to read/write the header data, etc.
     *
     * This base class for CORE nodes just saves the children, ie their sequence
     * number.
     *
     * @author Geneviève Bastien
     */
    protected static class CoreNodeData {

        /** Back-reference to the node class */
        private final HTNode<?> fNode;

        /**
         * Seq. numbers of the children nodes (size = max number of nodes,
         * determined by configuration)
         */
        private final int[] fChildren;

        /** Nb. of children this node has */
        private int fNbChildren;

        /**
         * Constructor
         *
         * @param node
         *            The node containing this extra data.
         */
        public CoreNodeData(HTNode<?> node) {
            fNode = node;
            fNbChildren = 0;
            /*
             * We instantiate the following array at full size right away, since
             * we want to reserve that space in the node's header. "fNbChildren"
             * will tell us how many relevant entries there are in those tables.
             */
            fChildren = new int[fNode.fMaxChildren];
        }

        /**
         * Return this core data's full node. To be used by subclasses.
         *
         * @return The node
         */
        protected HTNode<?> getNode() {
            return fNode;
        }

        /**
         * Read the specific header for this extra node data
         *
         * @param buffer
         *            The byte buffer in which to read
         */
        protected void readSpecificHeader(ByteBuffer buffer) {
            fNode.takeWriteLock();
            try {
                int size = fNode.getMaxChildren();

                fNbChildren = buffer.getInt();

                for (int i = 0; i < fNbChildren; i++) {
                    fChildren[i] = buffer.getInt();
                }
                for (int i = fNbChildren; i < size; i++) {
                    buffer.getInt();
                }
            } finally {
                fNode.releaseWriteLock();
            }
        }

        /**
         * Write the specific header for this extra node data
         *
         * @param buffer
         *            The byte buffer in which to write
         */
        protected void writeSpecificHeader(ByteBuffer buffer) {
            fNode.takeReadLock();
            try {
                buffer.putInt(fNbChildren);

                /* Write the "children's seq number" array */
                for (int i = 0; i < fNbChildren; i++) {
                    buffer.putInt(fChildren[i]);
                }
                for (int i = fNbChildren; i < fNode.getMaxChildren(); i++) {
                    buffer.putInt(0);
                }
            } finally {
                fNode.releaseReadLock();
            }
        }

        /**
         * Get the number of children
         *
         * @return The number of children
         */
        public int getNbChildren() {
            fNode.takeReadLock();
            try {
                return fNbChildren;
            } finally {
                fNode.releaseReadLock();
            }
        }

        /**
         * Get the child sequence number at an index
         *
         * @param index
         *            The index of the child to get
         * @return The sequence number of the child node
         */
        public int getChild(int index) {
            fNode.takeReadLock();
            try {
                if (index >= fNbChildren) {
                    throw new IndexOutOfBoundsException("The child at index " + index + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return fChildren[index];
            } finally {
                fNode.releaseReadLock();
            }
        }

        /**
         * Get the sequence number of the last child node of this one
         *
         * @return The sequence number of the last child
         */
        public int getLatestChild() {
            fNode.takeReadLock();
            try {
                return fChildren[fNbChildren - 1];
            } finally {
                fNode.releaseReadLock();
            }
        }

        /**
         * Add a new child to this node's data
         *
         * @param childNode
         *            The child node to add
         */
        public void linkNewChild(IHTNode<?> childNode) {
            fNode.takeWriteLock();
            try {
                if (fNbChildren >= fNode.getMaxChildren()) {
                    throw new IllegalStateException("Asked to link another child but parent already has maximum number of children"); //$NON-NLS-1$
                }

                fChildren[fNbChildren] = childNode.getSequenceNumber();
                fNbChildren++;

            } finally {
                fNode.releaseWriteLock();
            }
        }

        /**
         * Get the size of the extra header space necessary for this node's
         * extra data
         *
         * @return The extra header size
         */
        protected int getSpecificHeaderSize() {
            int maxChildren = fNode.getMaxChildren();
            int specificSize = Integer.BYTES /* 1x int (nbChildren) */

                    /* MAX_NB * int ('children' table) */
                    + Integer.BYTES * maxChildren;

            return specificSize;
        }

        @Override
        public String toString() {
            /* Only used for debugging, shouldn't be externalized */
            return String.format("Core Node, %d children %s", //$NON-NLS-1$
                    fNbChildren, Arrays.toString(Arrays.copyOf(fChildren, fNbChildren)));
        }

        /**
         * Inner method to select the sequence numbers for the children of the
         * current node that intersect the given timestamp. Useful for moving
         * down the tree.
         *
         * @param timeCondition
         *            The time-based RangeCondition to choose which children
         *            match.
         * @return Collection of sequence numbers of the child nodes that
         *         intersect t, non-null empty collection if this is a Leaf Node
         */
        public final Collection<Integer> selectNextChildren(RangeCondition<Long> timeCondition) {
            fNode.takeReadLock();
            try {
                return selectNextIndices(timeCondition).stream()
                        .map(i -> fChildren[i])
                        .collect(Collectors.toList());
            } finally {
                fNode.releaseReadLock();
            }
        }

        /**
         * Inner method to select the indices of the children of the current
         * node that intersect the given timestamp. Useful for moving down the
         * tree.
         *
         * This is the method that children implementations of this node should
         * override. They may call
         * <code>super.selectNextIndices(timeCondition)</code> to get access to
         * the subset of indices that match the parent's condition and add their
         * own filters. When this method is called a read-lock is already taken
         * on the node
         *
         * @param timeCondition
         *            The time-based RangeCondition to choose which children
         *            match.
         * @return Collection of the indices of the child nodes that intersect
         *         the time condition
         */
        protected Collection<Integer> selectNextIndices(RangeCondition<Long> timeCondition) {
            /* By default, all children are returned */
            List<Integer> childList = new ArrayList<>();
            for (int i = 0; i < fNbChildren; i++) {
                childList.add(i);
            }

            return childList;
        }

        @Override
        public int hashCode() {
            /*
             * Do not consider "fNode", since the node's equals/hashCode already
             * consider us.
             */
            return Objects.hash(fNbChildren, fChildren);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == null) {
                return false;
            }
            if (obj.getClass() != getClass()) {
                return false;
            }
            CoreNodeData other = (CoreNodeData) obj;
            return (fNbChildren == other.fNbChildren
                    && Arrays.equals(fChildren, other.fChildren));
        }

    }

    /**
     * Constructor
     *
     * @param type
     *            The type of this node
     * @param blockSize
     *            The size (in bytes) of a serialized node on disk
     * @param maxChildren
     *            The maximum allowed number of children per node
     * @param seqNumber
     *            The (unique) sequence number assigned to this particular node
     * @param parentSeqNumber
     *            The sequence number of this node's parent node
     * @param start
     *            The earliest timestamp stored in this node
     */
    public HTNode(NodeType type, int blockSize, int maxChildren, int seqNumber,
            int parentSeqNumber, long start) {
        fBlockSize = blockSize;
        fMaxChildren = maxChildren;

        fNodeStart = start;
        fSequenceNumber = seqNumber;
        fParentSequenceNumber = parentSeqNumber;

        fSizeOfContentSection = 0;
        fIsOnDisk = false;
        fIntervals = new ArrayList<>();

        fExtraData = createNodeExtraData(type);
    }

    /**
     * Reader factory method. Build a Node object (of the right type) by reading
     * a block in the file.
     *
     * @param blockSize
     *            The size (in bytes) of a serialized node on disk
     * @param maxChildren
     *            The maximum allowed number of children per node
     * @param fc
     *            FileChannel to the history file, ALREADY SEEKED at the start
     *            of the node.
     * @param objectReader
     *            The reader to read serialized node objects
     * @param nodeFactory
     *            The factory to create the nodes for this tree
     * @return The node object
     * @throws IOException
     *             If there was an error reading from the file channel
     */
    public static final <E extends IHTInterval, N extends HTNode<E>> N readNode(
            int blockSize,
            int maxChildren,
            FileChannel fc,
            IHTIntervalReader<E> objectReader,
            IHTNodeFactory<E, N> nodeFactory) throws IOException {

        N newNode;

        ByteBuffer buffer = ByteBuffer.allocate(blockSize);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        int res = fc.read(buffer);
        if (res != blockSize) {
            throw new IOException("The block for the HTNode is not the right size: " + res); //$NON-NLS-1$
        }
        buffer.flip();

        /* Read the common header part */
        byte typeByte = buffer.get();
        NodeType type = NodeType.fromByte(typeByte);
        long start = buffer.getLong();
        long end = buffer.getLong();
        int seqNb = buffer.getInt();
        int parentSeqNb = buffer.getInt();
        int intervalCount = buffer.getInt();
        buffer.get(); // TODO Used to be "isDone", to be removed from the header

        /* Now the rest of the header depends on the node type */
        switch (type) {
        case CORE:
        case LEAF:
            newNode = nodeFactory.createNode(type, blockSize, maxChildren, seqNb, parentSeqNb, start);
            newNode.readSpecificHeader(buffer);
            break;

        default:
            /* Unrecognized node type */
            throw new IOException();
        }

        /*
         * At this point, we should be done reading the header and 'buffer'
         * should only have the intervals left
         */
        ISafeByteBufferReader readBuffer = SafeByteBufferFactory.wrapReader(buffer, res - buffer.position());
        for (int i = 0; i < intervalCount; i++) {
            E interval = objectReader.readInterval(readBuffer);
            newNode.add(interval);
        }

        /* Assign the node's other information we have read previously */
        newNode.setNodeEnd(end);
        newNode.setOnDisk();

        return newNode;
    }

    /**
     * Create a node's extra data object for the given node type
     *
     * @param type
     *            The type of node
     * @return The node's extra data object, or <code>null</code> if there is
     *         none
     */
    protected @Nullable CoreNodeData createNodeExtraData(NodeType type) {
        if (type == NodeType.CORE) {
            return new CoreNodeData(this);
        }
        return null;
    }

    @Override
    public final void writeSelf(FileChannel fc) throws IOException {
        /*
         * Yes, we are taking the *read* lock here, because we are reading the
         * information in the node to write it to disk.
         */
        fRwl.readLock().lock();
        try {
            final int blockSize = getBlockSize();

            ByteBuffer buffer = ByteBuffer.allocate(blockSize);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.clear();

            /* Write the common header part */
            buffer.put(getNodeType().toByte());
            buffer.putLong(fNodeStart);
            buffer.putLong(fNodeEnd);
            buffer.putInt(fSequenceNumber);
            buffer.putInt(fParentSequenceNumber);
            buffer.putInt(fIntervals.size());
            buffer.put((byte) 1); // TODO Used to be "isDone", to be removed
                                  // from header

            /* Now call the inner method to write the specific header part */
            writeSpecificHeader(buffer);

            /* Back to us, we write the intervals */
            fIntervals.forEach(i -> i.writeSegment(SafeByteBufferFactory.wrapWriter(buffer, i.getSizeOnDisk())));
            if (blockSize - buffer.position() != getNodeFreeSpace()) {
                throw new IllegalStateException("Wrong free space: Actual: " + (blockSize - buffer.position()) + ", Expected: " + getNodeFreeSpace()); //$NON-NLS-1$ //$NON-NLS-2$
            }
            /*
             * Fill the rest with zeros
             */
            while (buffer.position() < blockSize) {
                buffer.put((byte) 0);
            }

            /* Finally, write everything in the Buffer to disk */
            buffer.flip();
            int res = fc.write(buffer);
            if (res != blockSize) {
                throw new IllegalStateException("Wrong size of block written: Actual: " + res + ", Expected: " + blockSize); //$NON-NLS-1$ //$NON-NLS-2$
            }

        } finally {
            fRwl.readLock().unlock();
        }
        fIsOnDisk = true;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get this node's block size.
     *
     * @return The block size
     */
    protected final int getBlockSize() {
        return fBlockSize;
    }

    /**
     * Get this node's maximum amount of children.
     *
     * @return The maximum amount of children
     */
    protected final int getMaxChildren() {
        return fMaxChildren;
    }

    /**
     * Get the interval comparator. Intervals will always be stored sorted
     * according to this comparator. This can be used by insertion or retrieval
     * algorithms.
     *
     * Sub-classes may override this to change or specify the interval
     * comparator.
     *
     * @return The way intervals are to be sorted in this node
     */
    protected Comparator<E> getIntervalComparator() {
        return fDefaultIntervalComparator;
    }

    @Override
    public long getNodeStart() {
        return fNodeStart;
    }

    @Override
    public long getNodeEnd() {
        if (fIsOnDisk) {
            return fNodeEnd;
        }
        return Long.MAX_VALUE;
    }

    @Override
    public int getSequenceNumber() {
        return fSequenceNumber;
    }

    @Override
    public int getParentSequenceNumber() {
        return fParentSequenceNumber;
    }

    @Override
    public void setParentSequenceNumber(int newParent) {
        fParentSequenceNumber = newParent;
    }

    @Override
    public boolean isOnDisk() {
        return fIsOnDisk;
    }

    /**
     * Get the node's extra data.
     *
     * @return The node extra data
     */
    protected @Nullable CoreNodeData getCoreNodeData() {
        return fExtraData;
    }

    /**
     * Get the list of objects in this node. This list is immutable. All objects
     * must be inserted through the {@link #add(IHTInterval)} method
     *
     * @return The list of intervals in this node
     */
    protected List<E> getIntervals() {
        return ImmutableList.copyOf(fIntervals);
    }

    /**
     * Set this node's end time. Called by the reader factory.
     *
     * @param end
     *            The end time of the node
     */
    protected void setNodeEnd(long end) {
        fNodeEnd = end;
    }

    /**
     * Set this node to be on disk. Called by the reader factory.
     */
    protected void setOnDisk() {
        fIsOnDisk = true;
    }

    @Override
    public void add(E newInterval) {
        fRwl.writeLock().lock();
        try {
            /*
             * Just in case, should be checked before even calling this function
             */
            int objSize = newInterval.getSizeOnDisk();
            if (objSize > getNodeFreeSpace()) {
                throw new IllegalArgumentException("The interval to insert (" + objSize + ") is larger than available space (" + getNodeFreeSpace() + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            }

            int insertPoint = Collections.binarySearch(fIntervals, newInterval, getIntervalComparator());
            insertPoint = (insertPoint >= 0 ? insertPoint : -insertPoint - 1);
            fIntervals.add(insertPoint, newInterval);

            fSizeOfContentSection += objSize;

        } finally {
            fRwl.writeLock().unlock();
        }
    }

    @Override
    public Iterable<E> getMatchingIntervals(RangeCondition<Long> timeCondition,
            Predicate<E> extraPredicate) {

        // TODO Use getIntervalComparator() to restrict the dataset further
        // TODO Benchmark using/returning streams instead of iterables

        if (isOnDisk()) {
            @NonNull Iterable<E> ret = fIntervals;
            ret = Iterables.filter(ret, interval -> timeCondition.intersects(interval.getStart(), interval.getEnd()));
            ret = Iterables.filter(ret, interval -> extraPredicate.test(interval));
            return ret;
        }

        takeReadLock();
        try {
        return fIntervals.stream()
                .filter(interval -> timeCondition.intersects(interval.getStart(), interval.getEnd()))
                .filter(extraPredicate)
                    /*
                     * Because this class works with read locks, we can't
                     * return a lazy stream unfortunately. Room for improvement?
                     */
                .collect(Collectors.toList());

        } finally {
            releaseReadLock();
        }
    }

    @Override
    public void closeThisNode(long endtime) {
        fRwl.writeLock().lock();
        try {
            /**
             * FIXME: was assert (endtime >= fNodeStart); but that exception is
             * reached with an empty node that has start time endtime + 1
             */
            // if (endtime < fNodeStart) {
            // throw new IllegalArgumentException("Endtime " + endtime + "
            // cannot be lower than start time " + fNodeStart);
            // }

            if (!fIntervals.isEmpty()) {
                /*
                 * Make sure there are no intervals in this node with their
                 * EndTime > the one requested. Only need to check the last one
                 * since they are sorted
                 */
                if (endtime < fIntervals.get(fIntervals.size() - 1).getEnd()) {
                    throw new IllegalArgumentException("Closing end time should be greater than or equal to the end time of the objects of this node"); //$NON-NLS-1$
                }
            }

            fNodeEnd = endtime;
        } finally {
            fRwl.writeLock().unlock();
        }
    }

    @Override
    public final int getTotalHeaderSize() {
        return COMMON_HEADER_SIZE + getSpecificHeaderSize();
    }

    /**
     * @return The offset, within the node, where the Data section ends
     */
    private int getDataSectionEndOffset() {
        return getTotalHeaderSize() + fSizeOfContentSection;
    }

    @Override
    public int getNodeFreeSpace() {
        fRwl.readLock().lock();
        try {
            int ret = getBlockSize() - getDataSectionEndOffset();
            return ret;
        } finally {
            fRwl.readLock().unlock();
        }
    }

    @Override
    public long getNodeUsagePercent() {
        fRwl.readLock().lock();
        try {
            final int blockSize = getBlockSize();
            float freePercent = (float) getNodeFreeSpace()
                    / (float) (blockSize - getTotalHeaderSize())
                    * 100F;
            return (long) (100L - freePercent);

        } finally {
            fRwl.readLock().unlock();
        }
    }

    @Override
    public NodeType getNodeType() {
        @Nullable
        CoreNodeData extraData = getCoreNodeData();
        if (extraData == null) {
            return NodeType.LEAF;
        }
        return NodeType.CORE;
    }

    /**
     * Return the specific header size of this node. This means the size
     * occupied by the type-specific section of the header (not counting the
     * common part).
     *
     * @return The specific header size
     */
    protected int getSpecificHeaderSize() {
        CoreNodeData extraData = fExtraData;
        if (extraData != null) {
            return extraData.getSpecificHeaderSize();
        }
        return 0;
    }

    /**
     * Read the specific header for this node
     *
     * @param buffer
     *            The buffer to read from
     */
    protected void readSpecificHeader(ByteBuffer buffer) {
        CoreNodeData extraData = fExtraData;
        if (extraData != null) {
            extraData.readSpecificHeader(buffer);
        }
    }

    /**
     * Write the type-specific part of the header in a byte buffer.
     *
     * @param buffer
     *            The buffer to write to. It should already be at the correct
     *            position.
     */
    protected void writeSpecificHeader(ByteBuffer buffer) {
        CoreNodeData extraData = fExtraData;
        if (extraData != null) {
            extraData.writeSpecificHeader(buffer);
        }
    }

    /**
     * Node-type-specific toString method. Used for debugging.
     *
     * @return A string representing the node
     */
    protected String toStringSpecific() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public boolean isEmpty() {
        return fIntervals.isEmpty();
    }

    // -------------------------------------------
    // Core node methods
    // -------------------------------------------

    @Override
    public int getNbChildren() {
        CoreNodeData extraData = fExtraData;
        if (extraData != null) {
            return extraData.getNbChildren();
        }
        return IHTNode.super.getNbChildren();
    }

    @Override
    public int getChild(int index) {
        CoreNodeData extraData = fExtraData;
        if (extraData != null) {
            return extraData.getChild(index);
        }
        return IHTNode.super.getChild(index);
    }

    @Override
    public int getLatestChild() {
        CoreNodeData extraData = fExtraData;
        if (extraData != null) {
            return extraData.getLatestChild();
        }
        return IHTNode.super.getLatestChild();
    }

    @Override
    public void linkNewChild(@NonNull IHTNode<E> childNode) {
        CoreNodeData extraData = fExtraData;
        if (extraData != null) {
            extraData.linkNewChild(childNode);
            return;
        }
        IHTNode.super.linkNewChild(childNode);
    }

    @Override
    public Collection<Integer> selectNextChildren(RangeCondition<Long> timeCondition)
            throws RangeException {
        CoreNodeData extraData = fExtraData;
        if (extraData != null) {
            return extraData.selectNextChildren(timeCondition);
        }
        return IHTNode.super.selectNextChildren(timeCondition);
    }

    // -----------------------------------------
    // Locking
    // -----------------------------------------

    /**
     * Takes a read lock on the fields of this class. Each call to this method
     * should be followed by a {@link HTNode#releaseReadLock()}, in a
     * try-finally clause
     */
    protected void takeReadLock() {
        fRwl.readLock().lock();
    }

    /**
     * Releases a read lock on the fields of this class. A call to this method
     * should have been preceded by a call to {@link HTNode#takeReadLock()}
     */
    protected void releaseReadLock() {
        fRwl.readLock().unlock();
    }

    /**
     * Takes a write lock on the fields of this class. Each call to this method
     * should be followed by a {@link HTNode#releaseWriteLock()}, in a
     * try-finally clause
     */
    protected void takeWriteLock() {
        fRwl.writeLock().lock();
    }

    /**
     * Releases a write lock on the fields of this class. A call to this method
     * should have been preceded by a call to {@link HTNode#takeWriteLock()}
     */
    protected void releaseWriteLock() {
        fRwl.writeLock().unlock();
    }

    // -----------------------------------------
    // Object methods
    // -----------------------------------------

    @SuppressWarnings("nls")
    @Override
    public String toString() {
        /* Only used for debugging, shouldn't be externalized */
        return String.format("Node #%d, %s, %s, %d intervals (%d%% used), [%d - %s]",
                fSequenceNumber,
                (fParentSequenceNumber == -1) ? "Root" : "Parent #" + fParentSequenceNumber,
                toStringSpecific(),
                fIntervals.size(),
                getNodeUsagePercent(),
                fNodeStart,
                (fIsOnDisk || fNodeEnd != 0) ? fNodeEnd : "...");
    }

    @Override
    public int hashCode() {
        return Objects.hash(fBlockSize, fMaxChildren, fNodeStart, fNodeEnd, fSequenceNumber, fParentSequenceNumber, fExtraData);
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        HTNode<? extends IHTInterval> other = (HTNode<?>) obj;
        return (fBlockSize == other.fBlockSize &&
                fMaxChildren == other.fMaxChildren &&
                fNodeStart == other.fNodeStart &&
                fNodeEnd == other.fNodeEnd &&
                fSequenceNumber == other.fSequenceNumber &&
                fParentSequenceNumber == other.fParentSequenceNumber &&
                Objects.equals(fExtraData, other.fExtraData));
    }

    // -----------------------------------------
    // Test-specific methods
    // -----------------------------------------

    /**
     * Print all current intervals into the given writer.
     *
     * @param writer
     *            The writer to write to
     */
    @VisibleForTesting
    public void debugPrintIntervals(PrintStream writer) {
        getIntervals().forEach(writer::println);
    }
}
