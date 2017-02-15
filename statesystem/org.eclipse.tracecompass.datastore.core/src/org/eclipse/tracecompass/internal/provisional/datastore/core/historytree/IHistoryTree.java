/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.RangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.exceptions.RangeException;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTInterval;

/**
 * An interface for trees on disk that save temporal data.
 *
 * These trees will typically be built from the leaf up, so they will have 2
 * node types:
 * <ul>
 * <li>Leaf nodes which have no children and just contain on disk objects
 * <li>Core nodes which have children nodes and will save in their headers all
 * the information necessary to retrieve their children optimally
 * </ul>
 *
 * The {@link AbstractHistoryTree} supply a base implementation of the tree with
 * all the logic to write the data on disk and can be extended to just add the
 * specific behavior of an implementation.
 *
 * A Base class for the nodes are also available: {@link HTNode}, the base class
 * for all nodes, it contains a subclass {@link HTNode.CoreNodeData} that can
 * be extended to save header or any other data concerning the children of a
 * node for example, or any other type of data to save in the header of a node.
 *
 * @author Geneviève Bastien
 * @param <E>
 *            The type of objects that will be saved in the tree
 */
public interface IHistoryTree<E extends IHTInterval> {

    /**
     * Size of the "tree header" in the tree-file The nodes will use this offset
     * to know where they should be in the file. This should always be a
     * multiple of 4K.
     */
    public static final int TREE_HEADER_SIZE = 4096;

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
     * Get the current size of the history file.
     *
     * @return The history file size
     */
    long getFileSize();

    // ------------------------------------------------------------------------
    // HtIo interface
    // ------------------------------------------------------------------------

    /**
     * Close the history file.
     *
     * Once the file is closed, the history tree cannot be queried anymore.
     * Querying it might throw {@link ClosedChannelException} or have
     * unpredictable results.
     */
    void closeFile();

    /**
     * Delete the history file.
     *
     * Once the file is closed, the history tree cannot be queried anymore.
     * Querying it might throw {@link ClosedChannelException} or have
     * unpredictable results.
     */
    void deleteFile();

    /**
     * Creates a new empty tree file and removes the previous file. The history
     * tree can still be queried but will be empty after the call to this
     * method.
     *
     * @throws IOException
     *             Exceptions thrown when deleting or creating the file
     */
    void cleanFile() throws IOException;

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Insert an interval into the tree.
     *
     * @param interval
     *            The interval to be inserted
     * @throws RangeException
     *             If the start or end time of the object are invalid
     */
    void insert(E interval) throws RangeException;

    /**
     * "Save" the tree to disk. This method will cause the treeIO object to
     * commit all nodes to disk and the header of the tree should also be saved
     * on disk
     *
     * @param requestedEndTime
     *            The greatest timestamp present in the history tree
     */
    void closeTree(long requestedEndTime);

    /**
     * Query the tree to retrieve the intervals matching the given conditions.
     *
     * @param timeCondition
     *            Time-based RangeCondition, can represent a single timestamp, a
     *            series of punctual timestamps, or a time range.
     * @param extraPredicate
     *            Extra check to run on the elements to determine if they should
     *            be returned or not. This will be checked at the node level, so
     *            if it's known in advance it might be advantageous to pass it
     *            here rather than checking it yourself on the returned
     *            Iterable.
     * @return An Iterable of the matching elements
     */
    Iterable<E> getMatchingIntervals(RangeCondition<Long> timeCondition,
            Predicate<E> extraPredicate);

    /**
     * Query the tree to retrieve an interval matching the given conditions.
     *
     * @param timeCondition
     *            Time-based RangeCondition, can represent a single timestamp, a
     *            series of punctual timestamps, or a time range.
     * @param extraPredicate
     *            Extra check to run on the elements to determine if they should
     *            be returned or not. This will be checked at the node level, so
     *            if it's known in advance it might be advantageous to pass it
     *            here rather than checking it yourself on the returned
     *            Iterable.
     * @return An interval matching the given conditions, or <code>null</code>
     *         if no interval was found.
     */
    @Nullable E getMatchingInterval(RangeCondition<Long> timeCondition,
            Predicate<E> extraPredicate);

    // ------------------------------------------------------------------------
    // Attribute-tree reading/writing operations
    //
    // FIXME These are statesystem-specific, should be removed from this
    //       interface and its implementations. The SS should save that info
    //       in a separate file
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
}
