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

import org.eclipse.tracecompass.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;

/**
 * Basic implementation of {@link AbstractOverlappingHistoryTree} that can
 * be used directly by clients.
 *
 * @author Loic Prieur-Drevon
 * @author Geneviève Bastien
 * @param <E>
 *            The type of objects that will be saved in the tree
 */
public class OverlappingHistoryTree<E extends IHTInterval>
        extends AbstractOverlappingHistoryTree<E, OverlappingNode<E>> {

    /**
     * The magic number for this file format.
     */
    public static final int HISTORY_FILE_MAGIC_NUMBER = 0x05FFA800;

    /** File format version. Increment when breaking compatibility. */
    private static final int FILE_VERSION = 1;

    private final IHTNodeFactory<E, OverlappingNode<E>> fNodeFactory =
            (t, b, m, seq, p, start) -> new OverlappingNode<>(t, b, m, seq, p, start);

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
    public OverlappingHistoryTree(File stateHistoryFile,
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
    public OverlappingHistoryTree(File existingStateFile,
            int expectedProviderVersion,
            IHTIntervalReader<E> objectReader) throws IOException {
        super(existingStateFile, expectedProviderVersion, objectReader);
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
    protected IHTNodeFactory<E, OverlappingNode<E>> getNodeFactory() {
        return fNodeFactory;
    }

}
