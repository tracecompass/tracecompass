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

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.common.core.log.TraceCompassLogUtils;
import org.eclipse.tracecompass.internal.statesystem.core.Activator;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.IHistoryTree.IHTNodeFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * This class abstracts inputs/outputs of the HistoryTree nodes.
 *
 * It contains all the methods and descriptors to handle reading/writing nodes
 * to the tree-file on disk and all the caching mechanisms.
 *
 * This abstraction is mainly for code isolation/clarification purposes. Every
 * HistoryTree must contain 1 and only 1 HT_IO element.
 *
 * @author Alexandre Montplaisir
 */
public class HT_IO {

    private static final Logger LOGGER = TraceCompassLog.getLogger(HT_IO.class);

    // ------------------------------------------------------------------------
    // Global cache of nodes
    // ------------------------------------------------------------------------

    private static final class CacheKey {

        public final HT_IO fStateHistory;
        public final int fSeqNumber;

        public CacheKey(HT_IO stateHistory,  int seqNumber) {
            fStateHistory = stateHistory;
            fSeqNumber = seqNumber;
        }

        @Override
        public int hashCode() {
            return Objects.hash(fStateHistory, fSeqNumber);
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            CacheKey other = (CacheKey) obj;
            return (fStateHistory.equals(other.fStateHistory) &&
                    fSeqNumber == other.fSeqNumber);
        }
    }

    private static final int CACHE_SIZE = 256;

    private static final CacheLoader<CacheKey, HTNode> NODE_LOADER = new CacheLoader<CacheKey, HTNode>() {
        @Override
        public HTNode load(CacheKey key) throws IOException {
            HT_IO io = key.fStateHistory;
            int seqNb = key.fSeqNumber;

            TraceCompassLogUtils.traceInstant(LOGGER, Level.FINEST, "Ht_Io:CacheMiss", "seqNum", seqNb); //$NON-NLS-1$ //$NON-NLS-2$

            synchronized (io) {
                MappedByteBuffer buffer = io.mapNodeToBuffer(seqNb, MapMode.READ_ONLY);
                return HTNode.readNode(io.fConfig, buffer, key.fStateHistory.fNodeFactory);
            }
        }
    };

    private static final LoadingCache<CacheKey, HTNode> NODE_CACHE = CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE).build(NODE_LOADER);

    private static final String READ_WRITE_MODE = "rw"; //$NON-NLS-1$

    // ------------------------------------------------------------------------
    // Instance fields
    // ------------------------------------------------------------------------

    /* Configuration of the History Tree */
    private final HTConfig fConfig;

    /* Fields related to the file I/O */
    private final RandomAccessFile fRandomAccessFile;
    private final FileChannel fFileChannel;

    private final IHTNodeFactory fNodeFactory;

    // ------------------------------------------------------------------------
    // Methods
    // ------------------------------------------------------------------------



    /**
     * Standard constructor
     *
     * @param config
     *            The configuration object for the StateHistoryTree
     * @param newFile
     *            Flag indicating that the file must be created from scratch
     * @param nodeFactory
     *            The factory to create new nodes for this tree
     *
     * @throws IOException
     *             An exception can be thrown when file cannot be accessed
     */
    public HT_IO(HTConfig config, boolean newFile, IHTNodeFactory nodeFactory) throws IOException {
        fConfig = config;

        File historyTreeFile = config.getStateFile();
        if (newFile) {
            boolean success1 = true;
            /* Create a new empty History Tree file */
            if (historyTreeFile.exists()) {
                success1 = historyTreeFile.delete();
            }
            boolean success2 = historyTreeFile.createNewFile();
            if (!(success1 && success2)) {
                /* It seems we do not have permission to create the new file */
                throw new IOException("Cannot create new file at " + //$NON-NLS-1$
                        historyTreeFile.getName());
            }
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(historyTreeFile, READ_WRITE_MODE);
        fRandomAccessFile = randomAccessFile;
        fFileChannel = randomAccessFile.getChannel();

        fNodeFactory = nodeFactory;
    }

    /**
     * Read a node from the file on disk.
     *
     * @param seqNumber
     *            The sequence number of the node to read.
     * @return The object representing the node
     * @throws ClosedChannelException
     *             Usually happens because the file was closed while we were
     *             reading. Instead of using a big reader-writer lock, we'll
     *             just catch this exception.
     */
    public @NonNull HTNode readNode(int seqNumber) throws ClosedChannelException {
        /* Do a cache lookup. If it's not present it will be loaded from disk */
        TraceCompassLogUtils.traceInstant(LOGGER, Level.FINEST, "Ht_Io:CacheLookup", "seqNum", seqNumber); //$NON-NLS-1$ //$NON-NLS-2$
        CacheKey key = new CacheKey(this, seqNumber);
        try {
            return Objects.requireNonNull(NODE_CACHE.get(key));

        } catch (ExecutionException e) {
            /* Get the inner exception that was generated */
            Throwable cause = e.getCause();
            if (cause instanceof ClosedChannelException) {
                throw (ClosedChannelException) cause;
            }
            /*
             * Other types of IOExceptions shouldn't happen at this point though.
             */
            Activator.getDefault().logError(e.getMessage(), e);
            throw new IllegalStateException();
        }
    }

    /**
     * Write the given node to disk.
     *
     * @param node
     *            The node to write.
     */
    public void writeNode(HTNode node) {
        try {
            int seqNumber = node.getSequenceNumber();

            /* "Write-back" the node into the cache */
            CacheKey key = new CacheKey(this, seqNumber);
            NODE_CACHE.put(key, node);

            /* Position ourselves at the start of the node and write it */
            synchronized (this) {
                node.writeSelf(mapNodeToBuffer(seqNumber, MapMode.READ_WRITE));
            }
        } catch (IOException e) {
            /* If we were able to open the file, we should be fine now... */
            Activator.getDefault().logError(e.getMessage(), e);
        }
    }

    /**
     * Get the output file channel, used for writing.
     *
     * @return The output file channel
     */
    public FileChannel getFcOut() {
        return fFileChannel;
    }

    /**
     * Retrieve the input stream with which to write the attribute tree.
     *
     * @param nodeOffset
     *            The offset in the file, in number of nodes. This should be
     *            after all the nodes.
     * @return The correctly-seeked input stream
     */
    public FileInputStream supplyATReader(long nodeOffset) {
        FileInputStream fileInputStream = null;
        try {
            /*
             * Position ourselves at the start of the Mapping section in the
             * file (which is right after the Blocks)
             */
            fileInputStream = new FileInputStream(fConfig.getStateFile());
            long offset = IHistoryTree.TREE_HEADER_SIZE + nodeOffset * fConfig.getBlockSize();
            fileInputStream.getChannel().position(offset);
        } catch (IOException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return fileInputStream;
    }

    /**
     * Close all file channels and streams.
     */
    public synchronized void closeFile() {
        try {
            fFileChannel.close();
            fRandomAccessFile.close();
        } catch (IOException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
    }

    /**
     * Delete the history tree file
     */
    public synchronized void deleteFile() {
        closeFile();

        File historyTreeFile = fConfig.getStateFile();
        if (!historyTreeFile.delete()) {
            /* We didn't succeed in deleting the file */
            Activator.getDefault().logError("Failed to delete" + historyTreeFile.getName()); //$NON-NLS-1$
        }
    }

    /**
     * Return the {@link MappedByteBuffer} for the queried node.
     *
     * @param seqNumber
     *            the node sequence number to seek the channel to, cast as long
     * @throws IOException
     *             If some other I/O error occurs
     */
    private MappedByteBuffer mapNodeToBuffer(long seqNumber, MapMode readWrite) throws IOException {
        long position = IHistoryTree.TREE_HEADER_SIZE + seqNumber * fConfig.getBlockSize();
        return fFileChannel.map(readWrite, position, fConfig.getBlockSize());
    }

}
