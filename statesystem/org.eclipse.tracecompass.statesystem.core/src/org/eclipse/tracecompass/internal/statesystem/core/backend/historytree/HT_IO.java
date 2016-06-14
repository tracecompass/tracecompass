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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.logging.Logger;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.common.core.log.TraceCompassLog;
import org.eclipse.tracecompass.internal.statesystem.core.Activator;

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
class HT_IO {

    @NonNullByDefault
    private static final class CacheElement {
        private final HTNode value;
        private final HT_IO key;

        public CacheElement(HT_IO ss, HTNode node) {
            key = ss;
            value = node;
        }

        public HT_IO getKey() {
            return key;
        }

        public HTNode getValue() {
            return value;
        }
    }

    /* Configuration of the History Tree */
    private final HTConfig fConfig;

    /* Fields related to the file I/O */
    private final FileInputStream fFileInputStream;
    private final FileOutputStream fFileOutputStream;
    private final FileChannel fFileChannelIn;
    private final FileChannel fFileChannelOut;

    // TODO test/benchmark optimal cache size
    /**
     * Cache size, must be a power of 2
     */
    private static final int CACHE_SIZE = 256;
    private static final int CACHE_MASK = CACHE_SIZE - 1;
    private static final CacheElement NODE_CACHE[] = new CacheElement[CACHE_SIZE];

    private static final Logger LOGGER = TraceCompassLog.getLogger(HT_IO.class);

    /**
     * Standard constructor
     *
     * @param config
     *            The configuration object for the StateHistoryTree
     * @param newFile
     *            Flag indicating that the file must be created from scratch
     *
     * @throws IOException
     *             An exception can be thrown when file cannot be accessed
     */
    public HT_IO(HTConfig config, boolean newFile) throws IOException {
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
    public synchronized @NonNull HTNode readNode(int seqNumber) throws ClosedChannelException {
        /* Do a cache lookup */
        int offset = (seqNumber + hashCode()) & CACHE_MASK;
        CacheElement cachedNode = NODE_CACHE[offset];

        if (cachedNode != null && cachedNode.getKey() == this && cachedNode.getValue().getSequenceNumber() == seqNumber) {
            LOGGER.finest(() -> "[HtIo:CacheHit] seqNum=" + seqNumber); //$NON-NLS-1$
            return cachedNode.getValue();
        }

        /* Lookup on disk */
        try {
            seekFCToNodePos(fFileChannelIn, seqNumber);
            HTNode readNode = HTNode.readNode(fConfig, fFileChannelIn);
            LOGGER.finest(() -> "[HtIo:CacheMiss] seqNum=" + seqNumber); //$NON-NLS-1$

            /* Put the node in the cache. */
            NODE_CACHE[offset] = new CacheElement(this, readNode);
            return readNode;

        } catch (ClosedChannelException e) {
            throw e;
        } catch (IOException e) {
            /*
             * Other types of IOExceptions shouldn't happen at this point though
             */
            Activator.getDefault().logError(e.getMessage(), e);
            throw new IllegalStateException();
        }
    }

    public synchronized void writeNode(HTNode node) {
        try {
            /* Insert the node into the cache. */
            int seqNumber = node.getSequenceNumber();
            int offset = (seqNumber + hashCode()) & CACHE_MASK;
            NODE_CACHE[offset] = new CacheElement(this, node);

            /* Position ourselves at the start of the node and write it */
            seekFCToNodePos(fFileChannelOut, seqNumber);
            node.writeSelf(fFileChannelOut);
        } catch (IOException e) {
            /* If we were able to open the file, we should be fine now... */
            Activator.getDefault().logError(e.getMessage(), e);
        }
    }

    public FileChannel getFcOut() {
        return fFileChannelOut;
    }

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

    public synchronized void closeFile() {
        try {
            fFileInputStream.close();
            fFileOutputStream.close();
        } catch (IOException e) {
            Activator.getDefault().logError(e.getMessage(), e);
        }
    }

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
        fc.position(HistoryTree.TREE_HEADER_SIZE
                + ((long) seqNumber) * fConfig.getBlockSize());
    }

}
