/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson, École Polytechnique de Montréal, and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.ClosedChannelException;

import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;

/**
 * Meta-container for the History Tree. This structure contains all the
 * high-level data relevant to the tree.
 *
 * @author Alexandre Montplaisir
 */
public interface IHistoryTree {

    /**
     * Size of the "tree header" in the tree-file The nodes will use this offset
     * to know where they should be in the file. This should always be a
     * multiple of 4K.
     */
    int TREE_HEADER_SIZE = 4096;

    /**
     * "Save" the tree to disk. This method will cause the treeIO object to
     * commit all nodes to disk and then return the RandomAccessFile descriptor
     * so the Tree object can save its configuration into the header of the
     * file.
     *
     * @param requestedEndTime
     *            The greatest timestamp present in the history tree
     */
    void closeTree(long requestedEndTime);

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    /**
     * Get the start time of this tree.
     *
     * @return The start time
     */
    long getTreeStart();

    /**
     * Get the current end time of this tree.
     *
     * @return The end time
     */
    long getTreeEnd();

    /**
     * Get the number of nodes in this tree.
     *
     * @return The number of nodes
     */
    int getNodeCount();

    /**
     * Get the current root node of this tree
     *
     * @return The root node
     */
    HTNode getRootNode();

    // ------------------------------------------------------------------------
    // HT_IO interface
    // ------------------------------------------------------------------------

    /**
     * Return the FileInputStream reader with which we will read an attribute
     * tree (it will be sought to the correct position).
     *
     * @return The FileInputStream indicating the file and position from which
     *         the attribute tree can be read.
     */
    FileInputStream supplyATReader();

    /**
     * Return the file to which we will write the attribute tree.
     *
     * @return The file to which we will write the attribute tree
     */
    File supplyATWriterFile();

    /**
     * Return the position in the file (given by {@link #supplyATWriterFile})
     * where to start writing the attribute tree.
     *
     * @return The position in the file where to start writing
     */
    long supplyATWriterFilePos();

    /**
     * Read a node from the tree.
     *
     * @param seqNumber
     *            The sequence number of the node to read
     * @return The node
     * @throws ClosedChannelException
     *             If the tree IO is unavailable
     */
    HTNode readNode(int seqNumber) throws ClosedChannelException;

    /**
     * Write a node object to the history file.
     *
     * @param node
     *            The node to write to disk
     */
    void writeNode(HTNode node);

    /**
     * Close the history file.
     */
    void closeFile();

    /**
     * Delete the history file.
     */
    void deleteFile();

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Insert an interval in the tree.
     *
     * @param interval
     *            The interval to be inserted
     * @throws TimeRangeException
     *             If the start of end time of the interval are invalid
     */
    void insertInterval(HTInterval interval) throws TimeRangeException;

    /**
     * Inner method to select the next child of the current node intersecting
     * the given timestamp. Useful for moving down the tree following one
     * branch.
     *
     * @param currentNode
     *            The node on which the request is made
     * @param t
     *            The timestamp to choose which child is the next one
     * @return The child node intersecting t
     * @throws ClosedChannelException
     *             If the file channel was closed while we were reading the tree
     */
    HTNode selectNextChild(CoreNode currentNode, long t) throws ClosedChannelException;

    /**
     * Get the current size of the history file.
     *
     * @return The history file size
     */
    long getFileSize();

}
