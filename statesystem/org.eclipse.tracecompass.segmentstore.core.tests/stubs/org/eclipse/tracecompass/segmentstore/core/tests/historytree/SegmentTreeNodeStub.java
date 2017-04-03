/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.historytree;

import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree.IHTNodeFactory;
import org.eclipse.tracecompass.internal.provisional.segmentstore.core.BasicSegment2;
import org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree.SegmentTreeNode;

/**
 * A stub segment tree node to expose some functionalities of the segment tree
 * nodet to better unit test them
 *
 * @author Geneviève Bastien
 */
public class SegmentTreeNodeStub extends SegmentTreeNode<BasicSegment2> {

    /** Factory to create nodes of this type */
    public static final IHTNodeFactory<BasicSegment2, SegmentTreeNodeStub> NODE_FACTORY =
            (t, b, m, seq, p, start) -> new SegmentTreeNodeStub(t, b, m, seq, p, start);

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
     *            The start of the current node
     */
    public SegmentTreeNodeStub(NodeType type, int blockSize, int maxChildren,
            int seqNumber, int parentSeqNumber, long start) {
        super(type, blockSize, maxChildren, seqNumber, parentSeqNumber, start);
    }

    @Override
    public long getChildStart(int index) {
        return super.getChildStart(index);
    }

    @Override
    public long getChildEnd(int index) {
        return super.getChildEnd(index);
    }

    @Override
    public long getMaxStart(int index) {
        return super.getMaxStart(index);
    }

    @Override
    public long getMinEnd(int index) {
        return super.getMinEnd(index);
    }

    @Override
    public long getShortest(int index) {
        return super.getShortest(index);
    }

    @Override
    public long getLongest(int index) {
        return super.getLongest(index);
    }

}
