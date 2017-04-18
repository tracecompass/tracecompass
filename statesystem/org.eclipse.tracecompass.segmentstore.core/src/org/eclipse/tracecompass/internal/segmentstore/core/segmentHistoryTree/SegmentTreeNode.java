/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree;

import java.nio.ByteBuffer;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping.OverlappingNode;
import org.eclipse.tracecompass.internal.provisional.segmentstore.core.BasicSegment2;
import org.eclipse.tracecompass.internal.provisional.segmentstore.core.ISegment2;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;

/**
 * The history tree node class for segment history tree. This is an extension of
 * the {@link OverlappingNode} class and keeps more information about the
 * children of a node, that will help for sorting segments, one of the specific
 * functionalities of the segment stores.
 *
 * @author Loic Prieur-Drevon
 * @author Geneviève Bastien
 * @param <E>
 *            type of {@link ISegment2}
 */
public class SegmentTreeNode<E extends ISegment2> extends OverlappingNode<E> {

    // These values represent the values for the current node only, not its
    // children
    private long fMaxStart = 0;
    private long fMinEnd = Long.MAX_VALUE;
    private long fShortest = Long.MAX_VALUE;
    private long fLongest = 0;

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
    public SegmentTreeNode(NodeType type, int blockSize, int maxChildren, int seqNumber, int parentSeqNumber, long start) {
        super(type, blockSize, maxChildren, seqNumber, parentSeqNumber, start);
        fMaxStart = start;
    }

    /**
     * Adds the data concerning the segment nodes, max start/min end and
     * durations
     *
     * @param <E>  type of {@link ISegment2}
     */
    protected static class OverlappingSegmentCoreData<E extends ISegment2> extends OverlappingExtraData {

        // These values cover the full subtrees of the child nodes
        // Max start of an interval
        private final long[] fChildMaxStart;
        // minimum end of an interval
        private final long[] fChildMinEnd;
        // minimum length
        private final long[] fMinLength;
        // maximum length
        private final long[] fMaxLength;

        /**
         * Segment history tree node data constructor
         *
         * @param node
         *            The node containing this extra data.
         */
        public OverlappingSegmentCoreData(SegmentTreeNode<?> node) {
            super(node);
            int size = getNode().getMaxChildren();
            /*
             * We instantiate the following arrays at full size right away,
             * since we want to reserve that space in the node's header.
             * "this.nbChildren" will tell us how many relevant entries there
             * are in those tables.
             */
            fChildMaxStart = new long[size];
            fChildMinEnd = new long[size];
            fMinLength = new long[size];
            fMaxLength = new long[size];
            for (int i = 0; i < size; i++) {
                fChildMaxStart[i] = 0;
                fChildMinEnd[i] = Long.MAX_VALUE;
                fMinLength[i] = Long.MAX_VALUE;
                fMaxLength[i] = Long.MIN_VALUE;
            }
        }

        @Override
        protected SegmentTreeNode<?> getNode() {
            /* Type enforced by constructor */
            return (SegmentTreeNode<?>) super.getNode();
        }

        @Override
        public void readSpecificHeader(@NonNull ByteBuffer buffer) {
            super.readSpecificHeader(buffer);
            int size = getNode().getMaxChildren();

            for (int i = 0; i < size; i++) {
                fChildMaxStart[i] = buffer.getLong();
                fChildMinEnd[i] = buffer.getLong();
                fMinLength[i] = buffer.getLong();
                fMaxLength[i] = buffer.getLong();
            }
        }

        @Override
        protected void writeSpecificHeader(@NonNull ByteBuffer buffer) {
            getNode().takeReadLock();
            try {
                super.writeSpecificHeader(buffer);

                int size = getNode().getMaxChildren();

                /*
                 * Write the children array
                 */
                for (int i = 0; i < size; i++) {
                    buffer.putLong(fChildMaxStart[i]);
                    buffer.putLong(fChildMinEnd[i]);
                    buffer.putLong(fMinLength[i]);
                    buffer.putLong(fMaxLength[i]);
                }
            } finally {
                getNode().releaseReadLock();
            }
        }

        @Override
        protected int getSpecificHeaderSize() {
            int maxChildren = getNode().getMaxChildren();
            int specificSize = super.getSpecificHeaderSize();
            /*
             * MAX_NB * Timevalue (max starts, min ends, min length, max length
             * table)
             */
            specificSize += 4 * Long.BYTES * maxChildren;

            return specificSize;
        }

        @Override
        public void linkNewChild(IHTNode<?> childNode) {
            // The child node should be a SegmentTreeNode
            if (!(childNode instanceof SegmentTreeNode)) {
                throw new IllegalArgumentException("Adding a node that is not an segment tree node to an segment tree!"); //$NON-NLS-1$
            }
            getNode().takeWriteLock();
            try {

                super.linkNewChild(childNode);
                final int childIndex = getNbChildren() - 1;

                // The child node should be a SegmentTreeNode, but in case it
                // isn't, just return here
                if (!(childNode instanceof SegmentTreeNode<?>)) {
                    return;
                }

                SegmentTreeNode<E> segmentNode = (SegmentTreeNode<E>) childNode;
                // The child node may already have segments, so we update
                // child's data with what is already in there
                updateChild(segmentNode, childIndex);

                // Add a listener on the child node to update its data in the
                // children's arrays
                segmentNode.addListener((node, endtime) -> updateChild((SegmentTreeNode<E>) node, childIndex));

            } finally {
                getNode().releaseWriteLock();
            }
        }

        private void updateChild(SegmentTreeNode<E> child, int childIndex) {
            fChildMaxStart[childIndex] = child.getMaxStart();
            fChildMinEnd[childIndex] = child.getMinEnd();
            fMinLength[childIndex] = child.getShortest();
            fMaxLength[childIndex] = child.getLongest();
            // The child node's extra data applies to the child and its subtree,
            // so also update with the child's children
            for (int i = 0; i < child.getNbChildren(); i++) {
                fChildMaxStart[childIndex] = Math.max(fChildMaxStart[childIndex], child.getMaxStart(i));
                fChildMinEnd[childIndex] = Math.min(fChildMinEnd[childIndex], child.getMinEnd(i));
                fMinLength[childIndex] = Math.min(fMinLength[childIndex], child.getShortest(i));
                fMaxLength[childIndex] = Math.max(fMaxLength[childIndex], child.getLongest(i));
            }
        }

        /* Make sure it is visible to the enclosing class */
        @Override
        protected Collection<Integer> selectNextIndices(TimeRangeCondition rc) {
            return super.selectNextIndices(rc);
        }

        /**
         * Get the maximum start value of a child and its subtree
         *
         * @param index
         *            The child index
         * @return The maximum start value of the child at index and its subtree
         */
        public long getMaxStart(int index) {
            getNode().takeReadLock();
            try {
                if (index >= getNbChildren()) {
                    throw new IndexOutOfBoundsException("The child at index " + index + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return fChildMaxStart[index];
            } finally {
                getNode().releaseReadLock();
            }
        }

        /**
         * Get the minimum end value of a child and its subtree
         *
         * @param index
         *            The child index
         * @return The minimum end value of the child at index and its subtree
         */
        public long getMinEnd(int index) {
            getNode().takeReadLock();
            try {
                if (index >= getNbChildren()) {
                    throw new IndexOutOfBoundsException("The child at index " + index + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return fChildMinEnd[index];
            } finally {
                getNode().releaseReadLock();
            }
        }

        /**
         * Get the shortest element length of a child and its subtree
         *
         * @param index
         *            The child index
         * @return The shortest length of the child at index and its subtree
         */
        public long getShortest(int index) {
            getNode().takeReadLock();
            try {
                if (index >= getNbChildren()) {
                    throw new IndexOutOfBoundsException("The child at index " + index + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return fMinLength[index];
            } finally {
                getNode().releaseReadLock();
            }
        }

        /**
         * Get the longest element length of a child and its subtree
         *
         * @param index
         *            The child index
         * @return The longest length of the child at index and its subtree
         */
        public long getLongest(int index) {
            getNode().takeReadLock();
            try {
                if (index >= getNbChildren()) {
                    throw new IndexOutOfBoundsException("The child at index " + index + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return fMaxLength[index];
            } finally {
                getNode().releaseReadLock();
            }
        }

        /**
         * Get the segment for a child node with the least value for the field
         * corresponding to the comparator's field.
         *
         * @param index
         *            The index of the child node
         * @param order
         *            The comparator with which to sort segments
         * @return A segment whose value for the field that correspond to the
         *         comparator is the least value of the child node
         */
        public ISegment2 getIndex(int index, Comparator<E> order) {
            if (order.equals(SegmentComparators.INTERVAL_START_COMPARATOR)) {
                return new BasicSegment2(getChildStart(index), getChildStart(index));
            } else if (order.equals(SegmentComparators.INTERVAL_START_COMPARATOR.reversed())) {
                return new BasicSegment2(fChildMaxStart[index], fChildMaxStart[index]);
            } else if (order.equals(SegmentComparators.INTERVAL_END_COMPARATOR)) {
                return new BasicSegment2(fChildMinEnd[index], fChildMinEnd[index]);
            } else if (order.equals(SegmentComparators.INTERVAL_END_COMPARATOR.reversed())) {
                return new BasicSegment2(getChildEnd(index), getChildEnd(index));
            } else if (order.equals(SegmentComparators.INTERVAL_LENGTH_COMPARATOR)) {
                return new BasicSegment2(0, fMinLength[index]);
            } else if (order.equals(SegmentComparators.INTERVAL_LENGTH_COMPARATOR.reversed())) {
                return new BasicSegment2(0, fMaxLength[index]);
            }
            // TODO: Don't know what to do with other comparators yet
            return new BasicSegment2(getChild(index), getChild(index));
        }

    }

    @Override
    protected @Nullable OverlappingSegmentCoreData<E> createNodeExtraData(final NodeType type) {
        if (type == NodeType.CORE) {
            return new OverlappingSegmentCoreData<>(this);
        }
        return null;
    }

    /**
     * Get the number of intervals in this node
     *
     * @return The number of intervals
     */
    public int getNumIntervals() {
        return getIntervals().size();
    }

    @Override
    public void add(E newInterval) {
        super.add(newInterval);
        updateBoundaries(newInterval);
    }

    /**
     * Get the maximum start time of an interval in this node
     *
     * @return the latest start time of this node's intervals
     */
    public long getMaxStart() {
        return fMaxStart;
    }

    /**
     * Get the earliest end time of an interval in this node
     *
     * @return the earliest end time of this node's intervals
     */
    public long getMinEnd() {
        return fMinEnd;
    }

    /**
     * Get the shortest duration of the intervals of this node
     *
     * @return the shortest duration of this node's intervals
     */
    public long getShortest() {
        return fShortest;
    }

    /**
     * Get the longest duration of the intervals of this node
     *
     * @return the longest duration of this node's intervals
     */
    public long getLongest() {
        return fLongest;
    }

    /**
     * Get the children intersecting the given time range, and return the least
     * element for each child wrt to the comparator
     *
     * @param range
     *            The range condition (start, end times) of the children
     * @param order
     *            The comparator to use to compare segments in child nodes
     * @return For each intersecting child, a Tuple with the segment with the
     *         least value for the comparator
     */
    public Set<Tuple<ISegment2>> selectNextChildren(TimeRangeCondition range, Comparator<E> order) {
        OverlappingSegmentCoreData<E> extraData = getCoreNodeData();
        if (extraData != null) {
            Set<Tuple<ISegment2>> set = new HashSet<>();
            for (Integer index : extraData.selectNextIndices(range)) {
                set.add(new Tuple<>(extraData.getIndex(index, order), extraData.getChild(index)));
            }
            return set;
        }
        return Collections.emptySet();
    }

    @Override
    protected @Nullable OverlappingSegmentCoreData<E> getCoreNodeData() {
        return (OverlappingSegmentCoreData<E>) super.getCoreNodeData();
    }

    /**
     * Get the maximum start value of a child subtree of this node
     *
     * @param index
     *            The index of the child subtree
     * @return The child subtree's maximal start value
     */
    protected long getMaxStart(int index) {
        OverlappingSegmentCoreData<E> extraData = getCoreNodeData();
        if (extraData != null) {
            return extraData.getMaxStart(index);
        }
        throw new UnsupportedOperationException("A leaf node does not have children"); //$NON-NLS-1$
    }

    /**
     * Get the minimum end value of a child subtree of this node
     *
     * @param index
     *            The index of the child subtree
     * @return The child subtree's minimum end value
     */
    protected long getMinEnd(int index) {
        OverlappingSegmentCoreData<E> extraData = getCoreNodeData();
        if (extraData != null) {
            return extraData.getMinEnd(index);
        }
        throw new UnsupportedOperationException("A leaf node does not have children"); //$NON-NLS-1$
    }

    /**
     * Get the length of the shortest element of a child subtree of this node
     *
     * @param index
     *            The index of the child subtree
     * @return The child subtree's shortest element length
     */
    protected long getShortest(int index) {
        OverlappingSegmentCoreData<E> extraData = getCoreNodeData();
        if (extraData != null) {
            return extraData.getShortest(index);
        }
        throw new UnsupportedOperationException("A leaf node does not have children"); //$NON-NLS-1$
    }

    /**
     * Get the length of the longest element of a child subtree of this node
     *
     * @param index
     *            The index of the child subtree
     * @return The child subtree's longest element length
     */
    protected long getLongest(int index) {
        OverlappingSegmentCoreData<E> extraData = getCoreNodeData();
        if (extraData != null) {
            return extraData.getLongest(index);
        }
        throw new UnsupportedOperationException("A leaf node does not have children"); //$NON-NLS-1$
    }

    /**
     * Class to store the node sequence numbers and Index element for
     * sortedIterators PriorityQueue
     */
    static class Tuple<E> {
        private final E fSegment;
        private final int fNodeSequenceNumber;

        /**
         * @param segment
         *            segment to use to order nodes in PriorityQueue
         * @param nodeSequenceNumber
         *            sequence number of the node we want to read
         */
        public Tuple(E segment, int nodeSequenceNumber) {
            fSegment = segment;
            fNodeSequenceNumber = nodeSequenceNumber;
        }

        /**
         * @return the element to compare or <code>null</code> if this node is
         *         empty
         */
        public E getSegment() {
            return fSegment;
        }

        /**
         * @return the sequence number to read the node
         */
        public int getSequenceNumber() {
            return fNodeSequenceNumber;
        }
    }

    /**
     * Get the tuples of minimal segments and sequence number for this node. If
     * the current node is empty, <code>null</code> is returned.
     *
     * @param order
     *            Comparator for which we need the key
     * @return Tuple to sort nodes in iterator, <code>null</code> if not is
     *         empty
     */
    @Nullable Tuple<E> key(Comparator<@NonNull E> order) {
        if (isEmpty()) {
            return null;
        }
        return new Tuple<>(Collections.min(this.getIntervals(), order), getSequenceNumber());
    }

    private void updateBoundaries(E segment) {
        fMaxStart = Math.max(fMaxStart, segment.getStart());
        fMinEnd = Math.min(fMinEnd, segment.getEnd());
        fShortest = Math.min(fShortest, segment.getLength());
        fLongest = Math.max(fLongest, segment.getLength());
    }

}
