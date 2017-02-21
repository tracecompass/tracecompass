/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.exceptions.RangeException;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTInterval;

import com.google.common.annotations.VisibleForTesting;

/**
 * The type of node used for classic history tree
 *
 * @author Geneviève Bastien
 *
 * @param <E>
 *            The type of objects that will be saved in the tree
 */
public class ClassicNode<E extends IHTInterval> extends HTNode<E> {

    /**
     * Adds the data concerning the classic nodes, the start of each child node
     */
    protected static class ClassicCoreNodeData extends CoreNodeData {

        /** Start times of each of the children (size = MAX_NB_CHILDREN) */
        private final long[] fChildStart;

        /**
         * Classic history tree node data constructor
         *
         * @param node
         *            The node containing this extra data.
         */
        public ClassicCoreNodeData(ClassicNode<?> node) {
            super(node);

            int size = node.getMaxChildren();
            /*
             * * We instantiate the two following arrays at full size right
             * away, since we want to reserve that space in the node's header.
             * "this.nbChildren" will tell us how many relevant entries there
             * are in those tables.
             */
            fChildStart = new long[size];
        }

        @Override
        protected ClassicNode<?> getNode() {
            /* Type enforced by constructor */
            return (ClassicNode<?>) super.getNode();
        }


        @Override
        public void readSpecificHeader(@NonNull ByteBuffer buffer) {
            super.readSpecificHeader(buffer);

            int size = getNode().getMaxChildren();

            for (int i = 0; i < getNbChildren(); i++) {
                fChildStart[i] = buffer.getLong();
            }
            for (int i = getNbChildren(); i < size; i++) {
                buffer.getLong();
            }
        }

        @Override
        protected void writeSpecificHeader(@NonNull ByteBuffer buffer) {
            super.writeSpecificHeader(buffer);

            int size = getNode().getMaxChildren();

            /* Write the "children's start times" array */
            for (int i = 0; i < getNbChildren(); i++) {
                buffer.putLong(fChildStart[i]);
            }
            for (int i = getNbChildren(); i < size; i++) {
                buffer.putLong(0);
            }
        }

        @Override
        protected int getSpecificHeaderSize() {
            int maxChildren = getNode().getMaxChildren();
            int specificSize = super.getSpecificHeaderSize();
            /* MAX_NB * Timevalue for start time */
            specificSize += Long.BYTES * maxChildren;

            return specificSize;
        }

        @Override
        public void linkNewChild(IHTNode<?> childNode) {
            getNode().takeWriteLock();
            try {
                super.linkNewChild(childNode);
                int nbChildren = getNbChildren();

                fChildStart[nbChildren - 1] = childNode.getNodeStart();

            } finally {
                getNode().releaseWriteLock();
            }
        }

        @Override
        protected Collection<Integer> selectNextIndices(TimeRangeCondition rc) {
            ClassicNode<?> node = getNode();

            if (rc.min() < node.getNodeStart()
                    || (node.isOnDisk() && rc.max() > node.getNodeEnd())) {
                throw new RangeException("Requesting children outside the node's range: " + rc.toString()); //$NON-NLS-1$
            }

            node.takeReadLock();
            try {
                int nbChildren = getNbChildren();
                if (nbChildren == 0) {
                    return Collections.EMPTY_LIST;
                }

                List<Integer> matchingChildren = new LinkedList<>();
                /* Check all children except the last one */
                for (int i = 0; i < nbChildren - 1; i++) {
                    long childStart = fChildStart[i];
                    /* Nodes are sequential */
                    long childEnd = fChildStart[i + 1] - 1;

                    if (rc.intersects(childStart, childEnd)) {
                        matchingChildren.add(i);
                    }
                }

                /* Check the last child */
                {
                    int i = nbChildren - 1;
                    long childStart = fChildStart[i];
                    long childEnd = getNode().getNodeEnd();
                    if (rc.intersects(childStart, childEnd)) {
                        matchingChildren.add(i);
                    }
                }

                return matchingChildren;

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

        @Override
        public int hashCode() {
            return Objects.hash(super.hashCode(), fChildStart);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (!super.equals(obj)) {
                return false;
            }
            /* super.equals already checks for null / same class */
            ClassicCoreNodeData other = (ClassicCoreNodeData) checkNotNull(obj);
            return (Arrays.equals(fChildStart, other.fChildStart));
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
    public ClassicNode(NodeType type, int blockSize, int maxChildren,
            int seqNumber, int parentSeqNumber, long start) {
        super(type, blockSize, maxChildren, seqNumber, parentSeqNumber, start);
    }

    @Override
    protected @Nullable ClassicCoreNodeData createNodeExtraData(final NodeType type) {
        if (type == NodeType.CORE) {
            return new ClassicCoreNodeData(this);
        }
        return null;
    }

    @Override
    protected @Nullable ClassicCoreNodeData getCoreNodeData() {
        return (ClassicCoreNodeData) super.getCoreNodeData();
    }

    /**
     * Get the start value of a child of this node
     *
     * @param index The index of the node to get the child start
     * @return The child start value
     */
    @VisibleForTesting
    long getChildStart(int index) {
        ClassicCoreNodeData extraData = getCoreNodeData();
        if (extraData != null) {
            return extraData.getChildStart(index);
        }
        throw new UnsupportedOperationException("A leaf node does not have children"); //$NON-NLS-1$
    }

}
