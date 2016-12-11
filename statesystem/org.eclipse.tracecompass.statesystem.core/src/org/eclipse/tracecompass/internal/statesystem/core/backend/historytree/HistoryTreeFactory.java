/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHistoryTree;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic.ClassicHistoryTree;

/**
 * Class that contains factory methods to build different types of history trees
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public final class HistoryTreeFactory {

    private HistoryTreeFactory() {
    }

    /**
     * Create a new History Tree from scratch, specifying all configuration
     * parameters.
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
     * @return The new history tree
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public static IHistoryTree<StateSystemInterval> createHistoryTree(File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart) throws IOException {

        return new ClassicHistoryTree<>(stateHistoryFile,
                blockSize,
                maxChildren,
                providerVersion,
                treeStart,
                StateSystemInterval.DESERIALISER);
    }

    /**
     * "Reader" factory : instantiate a SHTree from an existing tree file on
     * disk
     *
     * @param existingStateFile
     *            Path/filename of the history-file we are to open
     * @param expectedProviderVersion
     *            The expected version of the state provider
     * @return The history tree
     * @throws IOException
     *             If an error happens reading the file
     */
    public static IHistoryTree<StateSystemInterval> createFromFile(
            Path existingStateFile, int expectedProviderVersion) throws IOException {
        /*
         * Check the file exists and has a positive length. These verifications
         * will also be done in the HT's constructor.
         */
        if (!Files.isReadable(existingStateFile)) {
            throw new IOException("Selected state file does not exist or is not readable."); //$NON-NLS-1$
        }
        if (Files.size(existingStateFile) <= 0) {
            throw new IOException("Empty target file"); //$NON-NLS-1$
        }

        /* Read the magic number from the history tree file */
        ByteBuffer buffer = ByteBuffer.allocate(Integer.BYTES);
        try (ReadableByteChannel channel = Files.newByteChannel(existingStateFile, StandardOpenOption.READ);) {
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.clear();
            channel.read(buffer);
            buffer.flip();
        }

        /*
         * Check the magic number to see which history tree class to create
         */
        int magicNumber = buffer.getInt();
        switch (magicNumber) {
        case ClassicHistoryTree.HISTORY_FILE_MAGIC_NUMBER:
            return new ClassicHistoryTree<>(existingStateFile.toFile(),
                    expectedProviderVersion, StateSystemInterval.DESERIALISER);
        default:
            throw new IOException("Not a known history tree file"); //$NON-NLS-1$
        }
    }
}
