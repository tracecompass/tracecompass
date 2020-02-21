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

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.datastore.core.interval.HTInterval;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic.ClassicHistoryTree;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic.ClassicNode;

/**
 * A stub for the classic history tree. Limits type "E" to {@link HTInterval}.
 *
 * The advantage is that the node factory can now be declared statically, and
 * accessed directly by test methods that need it.
 *
 * @author Geneviève Bastien
 */
public class ClassicHistoryTreeStub extends ClassicHistoryTree<HTInterval> {

    /**
     * The magic number for this file format.
     */
    private static final int CLASSIC_HISTORY_STUB_FILE_MAGIC_NUMBER = 0x07E57A91;

    /** File format version. Increment when breaking compatibility. */
    private static final int FILE_VERSION = 1;

    /**
     * A factory to create leaf and core nodes based on the BaseHtObject object
     */
    public static final IHTNodeFactory<HTInterval, ClassicNode<HTInterval>> CLASSIC_NODE_FACTORY =
            (t, b, m, seq, p, start) -> new ClassicNode<>(t, b, m, seq, p, start);

    /**
     * Create a new Classic History Tree test stub from scratch, specifying all
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
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public ClassicHistoryTreeStub(File stateHistoryFile,
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
     * @param expProviderVersion
     *            The expected version of the state provider
     * @throws IOException
     *             If an error happens reading the file
     */
    public ClassicHistoryTreeStub(File existingStateFile, int expProviderVersion) throws IOException {
        super(existingStateFile, expProviderVersion, HTInterval.INTERVAL_READER);
    }


    @Override
    protected IHTNodeFactory<HTInterval, ClassicNode<HTInterval>> getNodeFactory() {
        return CLASSIC_NODE_FACTORY;
    }

    @Override
    protected int getMagicNumber() {
        return CLASSIC_HISTORY_STUB_FILE_MAGIC_NUMBER;
    }

    @Override
    protected int getFileVersion() {
        return FILE_VERSION;
    }

}
