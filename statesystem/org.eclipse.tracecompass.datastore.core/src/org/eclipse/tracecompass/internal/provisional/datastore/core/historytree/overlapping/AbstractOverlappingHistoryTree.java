/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.tracecompass.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree;

import com.google.common.annotations.VisibleForTesting;

/**
 * Overlapping history tree, where children node's ranges are allowed to overlap
 * and children will start at the time of the next interval to insert or at
 * minimum at the time of its sibling node
 *
 * @author Loic Prieur-Drevon
 * @author Geneviève Bastien
 * @param <E>
 *            The type of objects that will be saved in the tree
 * @param <N> The type of node used by this tree
 */
public abstract class AbstractOverlappingHistoryTree<E extends IHTInterval, N extends OverlappingNode<E>>
        extends AbstractHistoryTree<E, N> {

    // ------------------------------------------------------------------------
    // Constructors/"Destructors"
    // ------------------------------------------------------------------------

    /**
     * Create a new Overlapping History Tree from scratch, specifying all
     * configuration parameters.
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
     *            The factory to create new tree data elements when reading from
     *            the disk
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public AbstractOverlappingHistoryTree(File stateHistoryFile,
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
     * @param expectedProviderVersion
     *            The expected version of the state provider
     * @param objectReader
     *            The factory used to read segments from the history tree
     * @throws IOException
     *             If an error happens reading the file
     */
    public AbstractOverlappingHistoryTree(File existingStateFile,
            int expectedProviderVersion,
            IHTIntervalReader<E> objectReader) throws IOException {
        super(existingStateFile, expectedProviderVersion, objectReader);
    }

    @Override
    protected long getNewBranchStart(int depth, E interval) {
        // The node starts at the time of the interval to add, but should not
        // start before the previous sibling
        // TODO: Do some benchmark to see if this Math.max is efficient enough
        // as opposed to just interval.getStart which would require to start the
        // new branch higher up in the tree
        return Math.max(interval.getStart(), getLatestNode(depth).getNodeStart());
    }

    // ------------------------------------------------------------------------
    // Test-specific methods
    // ------------------------------------------------------------------------

    @Override
    @VisibleForTesting
    protected N getLatestLeaf() {
        return super.getLatestLeaf();
    }

    @Override
    @VisibleForTesting
    protected int getDepth() {
        return super.getDepth();
    }

    @Override
    @VisibleForTesting
    protected N getLatestNode(int depth) {
        return super.getLatestNode(depth);
    }

    @Override
    @VisibleForTesting
    protected boolean verifyChildrenSpecific(N parent, int index, N child) {
        return (parent.getChildStart(index) == child.getNodeStart() &&
                parent.getChildEnd(index) == child.getNodeEnd());

    }

    @Override
    @VisibleForTesting
    protected boolean verifyIntersectingChildren(N parent, N child) {
        int childSequence = child.getSequenceNumber();
        boolean shouldBeInCollection;
        Collection<Integer> nextChildren;
        for (long t = parent.getNodeStart(); t < parent.getNodeEnd(); t++) {
            TimeRangeCondition timeCondition = TimeRangeCondition.singleton(t);
            shouldBeInCollection = (timeCondition.intersects(child.getNodeStart(), child.getNodeEnd()));
            nextChildren = parent.selectNextChildren(timeCondition);
            if (shouldBeInCollection != nextChildren.contains(childSequence)) {
                return false;
            }
        }
        return true;
    }

}
