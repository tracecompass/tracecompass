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

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import java.util.Objects;
import java.util.concurrent.ExecutionException;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
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

    private static final LoadingCache<CacheKey, HTNode> NODE_CACHE =
        checkNotNull(CacheBuilder.newBuilder()
            .maximumSize(CACHE_SIZE)
            .build(new CacheLoader<CacheKey, HTNode>() {
                @Override
                public HTNode load(CacheKey key) throws IOException {
                    HT_IO io = key.fStateHistory;
                    int seqNb = key.fSeqNumber;

                    LOGGER.finest(() -> "[HtIo:CacheMiss] seqNum=" + seqNb); //$NON-NLS-1$

                    synchronized (io) {
                        io.seekFCToNodePos(io.fFileChannelIn, seqNb);
                        return HTNode.readNode(io.fConfig, io.fFileChannelIn, key.fStateHistory.fNodeFactory);
                    }
                }
            }));


    // ------------------------------------------------------------------------
    // Instance fields
    // ------------------------------------------------------------------------

    /* Configuration of the History Tree */
    private final HTConfig fConfig;

    /* Fields related to the file I/O */
    private final FileInputStream fFileInputStream;
    private final FileOutputStream fFileOutputStream;
    private final FileChannel fFileChannelIn;
    private final FileChannel fFileChannelOut;

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
            fFileInputStream = new FileInputStream(historyTreeFile);
            fFileOutputStream = new FileOutputStream(historyTreeFile, false);
        } else {
            /*
             * We want to open an existing file, make sure we don't squash the
             * existing content when opening the fos!
             */
            fFileInputStream = new FileInputStream(historyTreeFile);
            fFileOutputStream = new FileOutputStream(historyTreeFile, true);
        }
        fFileChannelIn = fFileInputStream.getChannel();
        fFileChannelOut = fFileOutputStream.getChannel();
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
        LOGGER.finest(() -> "[HtIo:CacheLookup] seqNum=" + seqNumber); //$NON-NLS-1$
        CacheKey key = new CacheKey(this, seqNumber);
        try {
            return checkNotNull(NODE_CACHE.get(key));

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
                seekFCToNodePos(fFileChannelOut, seqNumber);
                node.writeSelf(fFileChannelOut);
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
        return fFileChannelOut;
    }

    /**
     * Retrieve the input stream with which to write the attribute tree.
     *
     * @param nodeOffset
     *            The offset in the file, in number of nodes. This should be
     *            after all the nodes.
     * @return The correctly-seeked input stream
     */
    public FileInputStream supplyATReader(int nodeOffset) {
        try {
            /*
             * Position ourselves at the start of the Mapping section in the
             * file (which is right after the Blocks)
             */
            seekFCToNodePos(fFileChannelIn, nodeOffset);
        } catch (IOException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
        return fFileInputStream;
    }

    /**
     * Close all file channels and streams.
     */
    public synchronized void closeFile() {
        try {
            fFileInputStream.close();
            fFileOutputStream.close();
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
     * Seek the given FileChannel to the position corresponding to the node that
     * has seqNumber
     *
     * @param fc
     *            the channel to seek
     * @param seqNumber
     *            the node sequence number to seek the channel to
     * @throws IOException
     *             If some other I/O error occurs
     */
    private void seekFCToNodePos(FileChannel fc, int seqNumber)
            throws IOException {
        /*
         * Cast to (long) is needed to make sure the result is a long too and
         * doesn't get truncated
         */
        fc.position(IHistoryTree.TREE_HEADER_SIZE
                + ((long) seqNumber) * fConfig.getBlockSize());
    }

}
