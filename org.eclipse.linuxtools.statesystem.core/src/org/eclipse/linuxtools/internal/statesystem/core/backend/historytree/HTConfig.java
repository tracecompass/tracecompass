/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.statesystem.core.backend.historytree;

import java.io.File;

/**
 * Configuration object for the {@link HistoryTree}.
 *
 * @author Alexandre Montplaisir
 */
public final class HTConfig {

    private static final int DEFAULT_BLOCKSIZE = 64 * 1024;
    private static final int DEFAULT_MAXCHILDREN = 50;

    private final File stateFile;
    private final int blockSize;
    private final int maxChildren;
    private final int providerVersion;
    private final long treeStart;

    /**
     * Full constructor.
     *
     * @param newStateFile
     *            The name of the history file
     * @param blockSize
     *            The size of each "block" on disk. One node will always fit in
     *            one block.
     * @param maxChildren
     *            The maximum number of children allowed per core (non-leaf)
     *            node.
     * @param providerVersion
     *            The version of the state provider. If a file already exists,
     *            and their versions match, the history file will not be rebuilt
     *            uselessly.
     * @param startTime
     *            The start time of the history
     */
    public HTConfig(File newStateFile, int blockSize, int maxChildren,
            int providerVersion, long startTime) {
        this.stateFile = newStateFile;
        this.blockSize = blockSize;
        this.maxChildren = maxChildren;
        this.providerVersion = providerVersion;
        this.treeStart = startTime;
    }

    /**
     * Version of the constructor using default values for 'blockSize' and
     * 'maxChildren'.
     *
     * @param newStateFile
     *            The name of the history file
     * @param providerVersion
     *            The version of the state provider. If a file already exists,
     *            and their versions match, the history file will not be rebuilt
     *            uselessly.
     * @param startTime
     *            The start time of the history
     */
    public HTConfig(File newStateFile, int providerVersion, long startTime) {
        this(newStateFile, DEFAULT_BLOCKSIZE, DEFAULT_MAXCHILDREN, providerVersion, startTime);
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    /**
     * Get the history file.
     *
     * @return The history file
     */
    public File getStateFile() {
        return stateFile;
    }

    /**
     * Get the configure block size.
     *
     * @return The block size
     */
    public int getBlockSize() {
        return blockSize;
    }

    /**
     * Get the maximum amount of children allowed.
     *
     * @return The maximum amount of children
     */
    public int getMaxChildren() {
        return maxChildren;
    }

    /**
     * Get the state provider's version.
     *
     * @return The state provider's version
     */
    public int getProviderVersion() {
        return providerVersion;
    }

    /**
     * Get the start time of the history
     *
     * @return The start time
     */
    public long getTreeStart() {
        return treeStart;
    }
}
