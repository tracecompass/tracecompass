/*******************************************************************************
 * Copyright (c) 2010, 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic;

import java.io.File;
import java.io.IOException;
import java.util.Collection;

import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.RangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTIntervalReader;

import com.google.common.annotations.VisibleForTesting;

/**
 * Classic history tree, where children nodes do not overlap and are sequential,
 * ie the start of node(i+1) is equal to end of node(i) - 1
 *
 * @author Alexandre Montplaisir
 * @param <E>
 *            The type of objects that will be saved in the tree
 */
public class ClassicHistoryTree<E extends IHTInterval>
        extends AbstractHistoryTree<E, ClassicNode<E>> {

    /** The magic number for this file format. */
    public static final int HISTORY_FILE_MAGIC_NUMBER = 0x05FFA900;

    /** File format version. Increment when breaking compatibility. */
    private static final int FILE_VERSION = 8;


    // ------------------------------------------------------------------------
    // Constructors/"Destructors"
    // ------------------------------------------------------------------------

    /**
     * Create a new Classic (aka Sequential) History Tree from scratch,
     * specifying all configuration parameters.
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
    public ClassicHistoryTree(File stateHistoryFile,
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
     * "Reader" constructor : instantiate a Classic History Tree from an
     * existing tree file on disk
     *
     * @param existingStateFile
     *            Path/filename of the history-file we are to open
     * @param expProviderVersion
     *            The expected version of the state provider
     * @param intervalReader
     *            The factory used to read segments from the history tree
     * @throws IOException
     *             If an error happens reading the file
     */
    public ClassicHistoryTree(File existingStateFile,
            int expProviderVersion,
            IHTIntervalReader<E> intervalReader) throws IOException {
        super(existingStateFile, expProviderVersion, intervalReader);
    }

    @Override
    protected int getMagicNumber() {
        return HISTORY_FILE_MAGIC_NUMBER;
    }

    @Override
    protected int getFileVersion() {
        return FILE_VERSION;
    }

    @Override
    protected IHTNodeFactory<E, ClassicNode<E>> getNodeFactory() {
        return (t, b, m, seq, p, start) -> new ClassicNode<>(t, b, m, seq, p, start);
    }

    @Override
    protected long getNewBranchStart(int depth, E interval) {
        // The new branch starts at the end of the tree + 1, because the last
        // branch closed at tree end and they must be sequential
        return getTreeEnd() + 1;
    }

    // ------------------------------------------------------------------------
    // Test-specific methods
    // ------------------------------------------------------------------------

    @Override
    @VisibleForTesting
    protected boolean verifyChildrenSpecific(ClassicNode<E> parent,
            int index, ClassicNode<E> child) {
        return (parent.getChildStart(index) == child.getNodeStart());
    }

    @Override
    @VisibleForTesting
    protected boolean verifyIntersectingChildren(ClassicNode<E> parent, ClassicNode<E> child) {
        int childSequence = child.getSequenceNumber();
        for (long t = parent.getNodeStart(); t < parent.getNodeEnd(); t++) {
            RangeCondition<Long> timeCondition = RangeCondition.singleton(t);
            boolean shouldBeInCollection = timeCondition.intersects(child.getNodeStart(), child.getNodeEnd());
            Collection<Integer> nextChildren = parent.selectNextChildren(timeCondition);
            /* There should be only one intersecting child */
            if (nextChildren.size() != 1
                    || shouldBeInCollection != nextChildren.contains(childSequence)) {
                return false;
            }
        }
        return true;
    }

}
