/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping.OverlappingHistoryTree;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping.OverlappingNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.HTInterval;

/**
 * A stub for the overlapping history tree, specifying the object type to
 * {@link HTInterval}.
 *
 * @author Geneviève Bastien
 */
public class OverlappingHistoryTreeStub extends OverlappingHistoryTree<HTInterval> {

    /**
     * A factory to create leaf and core nodes based on the BaseHtObject object
     */
    public static final IHTNodeFactory<HTInterval, OverlappingNode<HTInterval>> OVERLAPPING_NODE_FACTORY =
            (t, b, m, seq, p, start) -> new OverlappingNode<>(t, b, m, seq, p, start);

    private int fLastInsertionIndex;

    /**
     * Create a new Overlapping History Tree test stub from scratch, specifying
     * all configuration parameters.
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
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public OverlappingHistoryTreeStub(File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart) throws IOException {

        super(stateHistoryFile,
                blockSize,
                maxChildren,
                providerVersion,
                treeStart,
                HTInterval.INTERVAL_READER);
    }

    /**
     * "Reader" constructor : instantiate a SHTree from an existing tree file on
     * disk
     *
     * @param existingStateFile
     *            Path/filename of the history-file we are to open
     * @param expectedProviderVersion
     *            The expected version of the state provider
     * @throws IOException
     *             If an error happens reading the file
     */
    public OverlappingHistoryTreeStub(File existingStateFile,
            int expectedProviderVersion) throws IOException {
        super(existingStateFile, expectedProviderVersion, HTInterval.INTERVAL_READER);
    }

    @Override
    protected IHTNodeFactory<HTInterval, OverlappingNode<HTInterval>> getNodeFactory() {
        return OVERLAPPING_NODE_FACTORY;
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

    // ------------------------------------------------------------------------
    // Re-exported methods (to be visible for the tests in same package)
    // ------------------------------------------------------------------------

    @Override
    protected OverlappingNode<HTInterval> getLatestNode(int depth) {
        return super.getLatestNode(depth);
    }

    @Override
    protected int getDepth() {
        return super.getDepth();
    }

    @Override
    protected OverlappingNode<HTInterval> getLatestLeaf() {
        return super.getLatestLeaf();
    }

}
