/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTInterval;

import com.google.common.annotations.VisibleForTesting;

/**
 * History tree node for overlapping history tree. Those nodes keep both the
 * child node's start and end times
 *
 * @author Loic Prieur-Drevon
 * @author Geneviève Bastien
 *
 * @param <E>
 *            The type of objects that will be saved in the tree
 */
public class OverlappingNode<E extends IHTInterval> extends HTNode<E> {

    /**
     * Listeners for when nodes are closed, to update the child node's
     * information.
     */
    private final Set<NodeClosedListener> fListeners = new HashSet<>();

    /**
     * Listener to node close operations. This can be used by parent nodes to
     * update their children data when a node is closed.
     */
    @FunctionalInterface
    protected static interface NodeClosedListener {

        /**
         * A node was just closed
         *
         * @param node
         *            The node that was just closed
         * @param endtime
         *            The end time of the node
         */
        void nodeClosed(OverlappingNode<?> node, long endtime);
    }

    /**
     * Adds the data concerning the segment nodes, start/end of children
     */
    protected static class OverlappingExtraData extends CoreNodeData {

        private final long[] fChildStart;
        private final long[] fChildEnd;

        /**
         * Node data constructor
         *
         * @param node
         *            The node containing this extra data.
         */
        public OverlappingExtraData(OverlappingNode<?> node) {
            super(node);
            int size = node.getMaxChildren();
            /*
             * * We instantiate the two following arrays at full size right
             * away, since we want to reserve that space in the node's header.
             * "this.nbChildren" will tell us how many relevant entries there
             * are in those tables.
             */
            fChildStart = new long[size];
            fChildEnd = new long[size];
            for (int i = 0; i < size; i++) {
                fChildStart[i] = 0;
                fChildEnd[i] = Long.MAX_VALUE;
            }
        }

        @Override
        protected OverlappingNode<?> getNode() {
            /* Type enforced by constructor */
            return (OverlappingNode<?>) super.getNode();
        }

        @Override
        public void readSpecificHeader(@NonNull ByteBuffer buffer) {
            super.readSpecificHeader(buffer);
            int size = getNode().getMaxChildren();

            for (int i = 0; i < size; i++) {
                fChildStart[i] = buffer.getLong();
                fChildEnd[i] = buffer.getLong();
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
                    buffer.putLong(fChildStart[i]);
                    buffer.putLong(fChildEnd[i]);
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
             * MAX_NB * child start and child end arrays
             */
            specificSize += 2 * Long.BYTES * maxChildren;

            return specificSize;
        }

        @Override
        public void linkNewChild(IHTNode<?> childNode) {
            if (!(childNode instanceof OverlappingNode<?>)) {
                throw new IllegalArgumentException("Adding a node that is not an overlapping node to an overlapping tree!"); //$NON-NLS-1$
            }

            getNode().takeWriteLock();
            try {
                super.linkNewChild(childNode);
                final int childIndex = getNbChildren() - 1;

                // We do not know the end time at this point, add a listener to
                // update child end when the node is closed
                ((OverlappingNode<?>) childNode).addListener((node, endtime) -> {
                    // FIXME On who are we calling getNode() and fChildEnd here?
                    getNode().takeWriteLock();
                    try {
                        fChildEnd[childIndex] = endtime;
                    } finally {
                        getNode().releaseWriteLock();
                    }
                });

                fChildStart[childIndex] = childNode.getNodeStart();
                // The child may already be closed
                if (childNode.isOnDisk()) {
                    fChildEnd[childIndex] = childNode.getNodeEnd();
                }

            } finally {
                getNode().releaseWriteLock();
            }
        }

        @Override
        protected Collection<Integer> selectNextIndices(TimeRangeCondition rc) {
            OverlappingNode<?> node = getNode();

            if (rc.max() < node.getNodeStart()
                    || (node.isOnDisk() && rc.min() > node.getNodeEnd())) {
                return Collections.emptySet();
            }

            node.takeReadLock();
            try {
                List<Integer> childList = new ArrayList<>();
                for (int i = 0; i < getNbChildren(); i++) {
                    if (rc.intersects(fChildStart[i], fChildEnd[i])) {
                        childList.add(i);
                    }
                }
                return childList;

            } finally {
                node.releaseReadLock();
            }

        }

        /**
         * Get the start value of a child
         *
         * @param index
         *            The child index
         * @return The start value
         */
        public long getChildStart(int index) {
            getNode().takeReadLock();
            try {
                if (index >= getNbChildren()) {
                    throw new IndexOutOfBoundsException("The child at index " + index + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return fChildStart[index];
            } finally {
                getNode().releaseReadLock();
            }
        }

        /**
         * Get the start value of a child
         *
         * @param index
         *            The child index
         * @return The start value
         */
        public long getChildEnd(int index) {
            getNode().takeReadLock();
            try {
                if (index >= getNbChildren()) {
                    throw new IndexOutOfBoundsException("The child at index " + index + " does not exist"); //$NON-NLS-1$ //$NON-NLS-2$
                }
                return fChildEnd[index];
            } finally {
                getNode().releaseReadLock();
            }
        }

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), fChildStart, fChildEnd);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            /* super.equals already checks for null / same class */
            OverlappingExtraData other = (OverlappingExtraData) checkNotNull(obj);
            return (Arrays.equals(fChildStart, other.fChildStart)
                    && Arrays.equals(fChildEnd, other.fChildEnd));
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
    public OverlappingNode(NodeType type, int blockSize, int maxChildren,
            int seqNumber, int parentSeqNumber, long start) {
        super(type, blockSize, maxChildren, seqNumber, parentSeqNumber, start);
    }

    @Override
    protected @Nullable OverlappingExtraData createNodeExtraData(final NodeType type) {
        if (type == NodeType.CORE) {
            return new OverlappingExtraData(this);
        }
        return null;
    }

    @Override
    public void add(E newInterval) {
        super.add(newInterval);
    }

    @Override
    public void closeThisNode(long endtime) {
        super.closeThisNode(endtime);
        fListeners.forEach(l -> l.nodeClosed(this, endtime));
    }

    /**
     * The listener for this node
     *
     * @param listener
     *            The update listener
     */
    protected void addListener(NodeClosedListener listener) {
        fListeners.add(listener);
    }

    @Override
    protected @Nullable OverlappingExtraData getCoreNodeData() {
        return (OverlappingExtraData) super.getCoreNodeData();
    }

    /**
     * Get the start value of a child of this node
     *
     * @param index
     *            The index of the node to get the child start
     * @return The child start value
     */
    @VisibleForTesting
    long getChildStart(int index) {
        OverlappingExtraData extraData = getCoreNodeData();
        if (extraData != null) {
            return extraData.getChildStart(index);
        }
        throw new UnsupportedOperationException("A leaf node does not have children"); //$NON-NLS-1$
    }

    /**
     * Get the end value of a child of this node
     *
     * @param index
     *            The index of the node to get the child end
     * @return The child end value
     */
    @VisibleForTesting
    long getChildEnd(int index) {
        OverlappingExtraData extraData = getCoreNodeData();
        if (extraData != null) {
            return extraData.getChildEnd(index);
        }
        throw new UnsupportedOperationException("A leaf node does not have children"); //$NON-NLS-1$
    }

}
