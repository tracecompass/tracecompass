/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.historytree;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHistoryTree;
import org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree.SegmentHistoryTree;
import org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree.SegmentTreeNode;
import org.eclipse.tracecompass.segmentstore.core.ISegment;

/**
 * A stub segment history tree that extends the base segment history tree and
 * the stub nodes
 *
 * @author Geneviève Bastien
 * @param <E>
 *            The type of segments that goes in this tree
 */
public class SegmentHistoryTreeStub<E extends ISegment> extends SegmentHistoryTree<E> {

    private int fLastInsertionIndex;

    /**
     * Minimum size a block of this tree should have
     */
    public static final int MINIMUM_BLOCK_SIZE = IHistoryTree.TREE_HEADER_SIZE;

    /**
     * Constructor
     *
     * @param stateHistoryFile
     *            The name of the history file
     * @param blockSize
     *            The size of each "block" on disk in bytes. One node will
     *            always fit in one block. It should be at least 4096.
     * @param maxChildren
     *            The maximum number of children allowed per core (non-leaf)
     *            node.
     * @param providerVersion
     *            The version of the state provider. If a file already exists,
     *            and their versions match, the history file will not be rebuilt
     *            uselessly.
     * @param treeStart
     *            The start time of the history
     * @param intervalReader
     *            typed ISegment to allow access to the readSegment methods
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public SegmentHistoryTreeStub(File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart,
            IHTIntervalReader<E> intervalReader) throws IOException {

        super(stateHistoryFile,
                blockSize,
                maxChildren,
                providerVersion,
                treeStart,
                intervalReader);
    }

    /**
     * "Reader" constructor : instantiate a SHTree from an existing tree file on
     * disk
     *
     * @param existingStateFile
     *            Path/filename of the history-file we are to open
     * @param expProviderVersion
     *            The expected version of the state provider
     * @param factory
     *            typed ISegment to allow access to the readSegment methods
     * @throws IOException
     *             If an error happens reading the file
     */
    public SegmentHistoryTreeStub(File existingStateFile, int expProviderVersion, IHTIntervalReader<E> factory) throws IOException {
        super(existingStateFile, expProviderVersion, factory);
    }

    @Override
    protected void informInsertingAtDepth(int depth) {
        fLastInsertionIndex = depth;
    }

    /**
     * Get the index in the current branch where the last element was inserted
     *
     * @return The index in the branch of the last insertion
     */
    public int getLastInsertionLocation() {
        return fLastInsertionIndex;
    }

    @Override
    public SegmentTreeNode<E> getLatestLeaf() {
        return super.getLatestLeaf();
    }
}
