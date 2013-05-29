/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.internal.tmf.core.statesystem.backends.historytree;

import java.io.File;

/**
 * Configuration object for a StateHistoryTree.
 *
 * @author alexmont
 */
final class HTConfig {

    private static final int DEFAULT_BLOCKSIZE = 64 * 1024;
    private static final int DEFAULT_MAXCHILDREN = 50;

    private final File stateFile;
    private final int blockSize;
    private final int maxChildren;
    private final int providerVersion;
    private final long treeStart;

    HTConfig(File newStateFile, int blockSize, int maxChildren,
            int providerVersion, long startTime) {
        this.stateFile = newStateFile;
        this.blockSize = blockSize;
        this.maxChildren = maxChildren;
        this.providerVersion = providerVersion;
        this.treeStart = startTime;
    }

    /**
     * Version using default values for blocksize and maxchildren
     *
     * @param stateFileName
     * @param startTime
     */
    HTConfig(File newStateFile, int providerVersion, long startTime) {
        this(newStateFile, DEFAULT_BLOCKSIZE, DEFAULT_MAXCHILDREN, providerVersion, startTime);
    }

    // ------------------------------------------------------------------------
    // Getters
    // ------------------------------------------------------------------------

    public File getStateFile() {
        return stateFile;
    }

    public int getBlockSize() {
        return blockSize;
    }

    public int getMaxChildren() {
        return maxChildren;
    }

    public int getProviderVersion() {
        return providerVersion;
    }

    public long getTreeStart() {
        return treeStart;
    }
}
