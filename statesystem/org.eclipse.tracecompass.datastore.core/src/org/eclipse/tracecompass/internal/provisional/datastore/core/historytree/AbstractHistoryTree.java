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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Predicate;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.datastore.core.historytree.HtIo;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.RangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.exceptions.RangeException;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHTNode.NodeType;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTIntervalReader;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Base class for history trees that encapsulates the logic to read from/write
 * to a file.
 *
 * @author Alexandre Montplaisir
 * @author Geneviève Bastien
 * @param <E>
 *            The type of intervals that will be saved in the tree
 * @param <N>
 *            The base type of the nodes of this tree
 */
public abstract class AbstractHistoryTree<E extends IHTInterval, N extends HTNode<E>>
        implements IHistoryTree<E> {

    /**
     * Interface for history to create the various HTNodes
     *
     * @param <E>
     *            The type of intervals that will be saved in the node
     * @param <N>
     *            The base type of the nodes of this tree
     */
    @FunctionalInterface
    public interface IHTNodeFactory<E extends IHTInterval, N extends HTNode<E>> {

        /**
         * Creates a new node for the specific history tree
         *
         * @param type
         *            The type of node to create. See {@link IHTNode.NodeType}.
         * @param blockSize
         *            The size (in bytes) of each node once serialized to disk
         * @param maxChildren
         *            The maximum number of amount a single core node can have
         * @param seqNumber
         *            The (unique) sequence number assigned to this particular
         *            node
         * @param parentSeqNumber
         *            The sequence number of this node's parent node
         * @param start
         *            The earliest timestamp stored in this node
         * @return The new core node
         */
        N createNode(NodeType type, int blockSize, int maxChildren,
                int seqNumber, int parentSeqNumber, long start);
    }

    // ------------------------------------------------------------------------
    // Tree-specific configuration
    // ------------------------------------------------------------------------

    /* Tree configuration constants */
    private final File fHistoryFile;
    private final int fBlockSize;
    private final int fMaxChildren;
    private final int fProviderVersion;
    private final long fTreeStart;
    private final IHTIntervalReader<E> fIntervalReader;

    /** Reader/writer object */
    private HtIo<E, N> fTreeIO;

    // ------------------------------------------------------------------------
    // Variable Fields (will change throughout the existence of the SHT)
    // ------------------------------------------------------------------------

    /** Latest timestamp found in the tree (at any given moment) */
    private long fTreeEnd;

    /** The total number of nodes that exists in this tree */
    private int fNodeCount;

    /** "Cache" to keep the active nodes in memory */
    private final List<N> fLatestBranch;

    /* Lock used to protect the accesses to the HT_IO object */
    private final ReentrantReadWriteLock fRwl = new ReentrantReadWriteLock(false);

    /**
     * Create a new State History from scratch, specifying all configuration
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
     * @param intervalReader
     *            The factory to create new tree intervals when reading from
     *            the disk
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public AbstractHistoryTree(File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart,
            IHTIntervalReader<E> intervalReader) throws IOException {
        /*
         * Simple check to make sure we have enough place in the 0th block for
         * the tree configuration
         */
        if (blockSize < TREE_HEADER_SIZE) {
            throw new IllegalArgumentException();
        }

        fHistoryFile = stateHistoryFile;
        fBlockSize = blockSize;
        fMaxChildren = maxChildren;
        fProviderVersion = providerVersion;
        fTreeStart = treeStart;
        fIntervalReader = intervalReader;

        fTreeEnd = treeStart;
        fNodeCount = 0;
        fLatestBranch = NonNullUtils.checkNotNull(Collections.synchronizedList(new ArrayList<>()));

        /* Prepare the IO object */
        fTreeIO = new HtIo<>(stateHistoryFile,
                blockSize,
                maxChildren,
                true,
                intervalReader,
                getNodeFactory());

        /* Add the first node to the tree */
        N firstNode = initNewLeafNode(-1, treeStart);
        fLatestBranch.add(firstNode);
    }

    /**
     * "Reader" constructor : instantiate a SHTree from an existing tree file on
     * disk
     *
     * @param existingStateFile
     *            Path/filename of the history-file we are to open
     * @param expectedProviderVersion
     *            The expected version of the state provider
     * @param intervalReader
     *            The factory used to read segments from the history tree
     * @throws IOException
     *             If an error happens reading the file
     */
    public AbstractHistoryTree(File existingStateFile,
            int expectedProviderVersion,
            IHTIntervalReader<E> intervalReader) throws IOException {
        /*
         * Open the file ourselves, get the tree header information we need,
         * then pass on the descriptor to the TreeIO object.
         */
        int rootNodeSeqNb, res;
        int bs, maxc;
        long startTime;

        /* Java I/O mumbo jumbo... */
        if (!existingStateFile.exists()) {
            throw new IOException("Selected state file does not exist"); //$NON-NLS-1$
        }
        if (existingStateFile.length() <= 0) {
            throw new IOException("Empty target file"); //$NON-NLS-1$
        }

        try (FileInputStream fis = new FileInputStream(existingStateFile);
                FileChannel fc = fis.getChannel();) {

            ByteBuffer buffer = ByteBuffer.allocate(TREE_HEADER_SIZE);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            buffer.clear();

            res = fc.read(buffer);
            if (res != TREE_HEADER_SIZE) {
                throw new IOException("Invalid header size"); //$NON-NLS-1$
            }

            buffer.flip();

            /*
             * Check the magic number to make sure we're opening the right type
             * of file
             */
            res = buffer.getInt();
            if (res != getMagicNumber()) {
                throw new IOException("Wrong magic number"); //$NON-NLS-1$
            }

            res = buffer.getInt(); /* File format version number */
            if (res != getFileVersion()) {
                throw new IOException("Mismatching History Tree file format versions"); //$NON-NLS-1$
            }

            res = buffer.getInt(); /* Event handler's version number */
            if (res != expectedProviderVersion) {
                /*
                 * The existing history was built using an event handler that
                 * doesn't match the current one in the framework.
                 *
                 * Information could be all wrong. Instead of keeping an
                 * incorrect history file, a rebuild is done.
                 */
                throw new IOException("Mismatching event handler versions"); //$NON-NLS-1$
            }

            bs = buffer.getInt(); /* Block Size */
            maxc = buffer.getInt(); /* Max nb of children per node */

            fNodeCount = buffer.getInt();
            rootNodeSeqNb = buffer.getInt();
            startTime = buffer.getLong();

            /* Set all other permanent configuration options */
            fHistoryFile = existingStateFile;
            fBlockSize = bs;
            fMaxChildren = maxc;
            fProviderVersion = expectedProviderVersion;
            fIntervalReader = intervalReader;
            fTreeStart = startTime;
        }

        /*
         * FIXME We close fis here and the TreeIO will then reopen the same
         * file, not extremely elegant. But how to pass the information here to
         * the SHT otherwise?
         */
        fTreeIO = new HtIo<>(fHistoryFile,
                fBlockSize,
                fMaxChildren,
                false,
                fIntervalReader,
                getNodeFactory());

        fLatestBranch = buildLatestBranch(rootNodeSeqNb);
        fTreeEnd = getRootNode().getNodeEnd();

        /*
         * Make sure the history start time we read previously is consistent
         * with was is actually in the root node.
         */
        if (startTime != getRootNode().getNodeStart()) {
            throw new IOException("Inconsistent start times in the " + //$NON-NLS-1$
                    "history file, it might be corrupted."); //$NON-NLS-1$
        }
    }

    /**
     * Rebuild the latestBranch "cache" object by reading the nodes from disk
     * (When we are opening an existing file on disk and want to append to it,
     * for example).
     *
     * @param rootNodeSeqNb
     *            The sequence number of the root node, so we know where to
     *            start
     * @throws ClosedChannelException
     */
    private List<N> buildLatestBranch(int rootNodeSeqNb) throws ClosedChannelException {
        List<N> list = new ArrayList<>();

        N nextChildNode = fTreeIO.readNode(rootNodeSeqNb);
        list.add(nextChildNode);

        // TODO: Do we need the full latest branch? The latest leaf may not be
        // the one we'll query first... Won't it build itself later?

        /* Follow the last branch up to the leaf */
        while (nextChildNode.getNodeType() == HTNode.NodeType.CORE) {
            nextChildNode = fTreeIO.readNode(nextChildNode.getLatestChild());
            list.add(nextChildNode);
        }
        return Collections.synchronizedList(list);
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public long getTreeStart() {
        return fTreeStart;
    }

    @Override
    public long getTreeEnd() {
        return fTreeEnd;
    }

  /**
  * Get the number of nodes in this tree.
  *
  * @return The number of nodes
  */
    public int getNodeCount() {
        return fNodeCount;
    }

  /**
  * Get the current root node of this tree
  *
  * @return The root node
  */
    public N getRootNode() {
        return fLatestBranch.get(0);
    }

    @Override
    public long getFileSize() {
        return fHistoryFile.length();
    }

    /**
     * Return the latest branch of the tree. That branch is immutable.
     *
     * @return The immutable latest branch
     */
    @VisibleForTesting
    List<N> getLatestBranch() {
        return ImmutableList.copyOf(fLatestBranch);
    }

    /**
     * Get the node in the latest branch at a depth. If the depth is too large,
     * it will throw an IndexOutOfBoundsException
     *
     * @param depth
     *            The depth at which to get the node
     * @return The node at depth
     */
    protected N getLatestNode(int depth) {
        if (depth > fLatestBranch.size()) {
            throw new IndexOutOfBoundsException("Trying to get latest node too deep"); //$NON-NLS-1$
        }
        return fLatestBranch.get(depth);
    }

    /**
     * Get the magic number for the history file. This number should be specific
     * for each implementation of the history tree.
     *
     * @return The magic number for the history file
     */
    protected abstract int getMagicNumber();

    /**
     * Get the file version for the history file. This file version should be
     * modified for a history tree class whenever changing the format of the
     * file. Different versions of the file may not be compatible.
     *
     * @return The file version for the history file.
     */
    protected abstract int getFileVersion();

    /**
     * Get the factory to use to create new nodes for this history tree.
     *
     * This method is called in the constructor of the abstract class, so
     * assigning the factory to a final field may cause NullPointerException
     * since that final field may not be initialized the first time this is
     * called.
     *
     * @return The NodeFactory for the History Tree
     */
    protected abstract IHTNodeFactory<E, N> getNodeFactory();

    /**
     * Read a node with a given sequence number
     *
     * @param seqNum
     *            The sequence number of the node to read
     * @return The HTNode object
     * @throws ClosedChannelException
     *             Exception thrown when reading the node, if the file was
     *             closed
     */
    @VisibleForTesting
    @NonNull N getNode(int seqNum) throws ClosedChannelException {
        // First, check in the latest branch if the node is there
        for (N node : fLatestBranch) {
            if (node.getSequenceNumber() == seqNum) {
                return node;
            }
        }
        return fTreeIO.readNode(seqNum);
    }

    /**
     * Retrieve the TreeIO object. Should only be used for testing.
     *
     * @return The TreeIO
     */
    @VisibleForTesting
    HtIo<E, N> getTreeIO() {
        return fTreeIO;
    }

    // ------------------------------------------------------------------------
    // HT_IO interface
    // ------------------------------------------------------------------------

    // TODO Remove from here
    @Override
    public FileInputStream supplyATReader() {
        fRwl.readLock().lock();
        try {
            return fTreeIO.supplyATReader(getNodeCount());
        } finally {
            fRwl.readLock().unlock();
        }
    }

    // TODO Remove from here
    @Override
    public File supplyATWriterFile() {
        return fHistoryFile;
    }

    // TODO Remove from here
    @Override
    public long supplyATWriterFilePos() {
        return IHistoryTree.TREE_HEADER_SIZE
                + ((long) getNodeCount() * fBlockSize);
    }

    /**
     * Read a node from the tree.
     *
     * @param seqNumber
     *            The sequence number of the node to read
     * @return The node
     * @throws ClosedChannelException
     *             If the tree IO is unavailable
     */
    public N readNode(int seqNumber) throws ClosedChannelException {
        /* Try to read the node from memory */
        synchronized (fLatestBranch) {
            for (N node : fLatestBranch) {
                if (node.getSequenceNumber() == seqNumber) {
                    return node;
                }
            }
        }

        fRwl.readLock().lock();
        try {
            /* Read the node from disk */
            return fTreeIO.readNode(seqNumber);
        } finally {
            fRwl.readLock().unlock();
        }
    }

    /**
     * Write a node object to the history file.
     *
     * @param node
     *            The node to write to disk
     */
    public void writeNode(N node) {
        fRwl.readLock().lock();
        try {
            fTreeIO.writeNode(node);
        } finally {
            fRwl.readLock().unlock();
        }
    }

    /**
     * Close the history file.
     */
    @Override
    public void closeFile() {
        fRwl.writeLock().lock();
        try {
            fTreeIO.closeFile();
            clearContent();
        } finally {
            fRwl.writeLock().unlock();
        }
    }

    /**
     * Delete the history file.
     */
    @Override
    public void deleteFile() {
        fRwl.writeLock().lock();
        try {
            fTreeIO.deleteFile();
            clearContent();
        } finally {
            fRwl.writeLock().unlock();
        }
    }

    @Override
    public void cleanFile() throws IOException {
        fRwl.writeLock().lock();
        try {
            closeTree(fTreeEnd);
            fTreeIO.deleteFile();

            fTreeIO = new HtIo<>(fHistoryFile,
                    fBlockSize,
                    fMaxChildren,
                    true,
                    fIntervalReader,
                    getNodeFactory());

            clearContent();
            /* Add the first node to the tree */
            N firstNode = initNewLeafNode(-1, fTreeStart);
            fLatestBranch.add(firstNode);
        } finally {
            fRwl.writeLock().unlock();
        }
    }

    private void clearContent() {
        // Re-initialize the content of the tree after the file is deleted or
        // closed
        fNodeCount = 0;
        fLatestBranch.clear();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    /**
     * Insert an interval in the tree.
     *
     * @param interval
     *            The interval to be inserted
     * @throws RangeException
     *             If the start of end time of the interval are invalid
     */
    @Override
    public synchronized void insert(E interval) throws RangeException {
        if (interval.getStart() < fTreeStart) {
            throw new RangeException("Interval Start:" + interval.getStart() + ", Config Start:" + fTreeStart); //$NON-NLS-1$ //$NON-NLS-2$
        }
        tryInsertAtNode(interval, fLatestBranch.size() - 1);
    }

    /**
     * Add a new empty core node to the tree.
     *
     * @param parentSeqNumber
     *            Sequence number of this node's parent
     * @param startTime
     *            Start time of the new node
     * @return The newly created node
     */
    protected final N initNewCoreNode(int parentSeqNumber, long startTime) {
        N newNode = getNodeFactory().createNode(NodeType.CORE, fBlockSize, fMaxChildren,
                fNodeCount, parentSeqNumber, startTime);
        fNodeCount++;
        return newNode;
    }

    /**
     * Add a new empty leaf node to the tree.
     *
     * @param parentSeqNumber
     *            Sequence number of this node's parent
     * @param startTime
     *            Start time of the new node
     * @return The newly created node
     */
    protected final N initNewLeafNode(int parentSeqNumber, long startTime) {
        N newNode = getNodeFactory().createNode(NodeType.LEAF, fBlockSize, fMaxChildren,
                fNodeCount, parentSeqNumber, startTime);
        fNodeCount++;
        return newNode;
    }

    /**
     * Inner method to find in which node we should add the interval.
     *
     * @param interval
     *            The interval to add to the tree
     * @param depth
     *            The index *in the latestBranch* where we are trying the
     *            insertion
     */
    protected final void tryInsertAtNode(E interval, int depth) {
        N targetNode = getLatestBranch().get(depth);
        informInsertingAtDepth(depth);

        /* Verify if there is enough room in this node to store this interval */
        if (interval.getSizeOnDisk() > targetNode.getNodeFreeSpace()) {
            /* Nope, not enough room. Insert in a new sibling instead. */
            addSiblingNode(depth, getNewBranchStart(depth, interval));
            tryInsertAtNode(interval, getLatestBranch().size() - 1);
            return;
        }

        /* Make sure the interval time range fits this node */
        if (interval.getStart() < targetNode.getNodeStart()) {
            /*
             * No, this interval starts before the startTime of this node. We
             * need to check recursively in parents if it can fit.
             */
            tryInsertAtNode(interval, depth - 1);
            return;
        }

        /*
         * Ok, there is room, and the interval fits in this time slot. Let's add
         * it.
         */
        targetNode.add(interval);

        updateEndTime(interval);
    }

    /**
     * Informs the tree that the insertion is requested at a given depth. When
     * this is called, the element is not yet inserted, but the last call to
     * this for an element will represent the depth at which is was really
     * inserted. By default, this method does nothing and should not be
     * necessary for concrete implementations, but it can be used by unit tests
     * to check to position of insertion of elements.
     *
     * @param depth
     *            The depth at which the last insertion was done
     */
    @VisibleForTesting
    protected void informInsertingAtDepth(int depth) {

    }

    /**
     * Get the start time of the new node of the branch that will be added
     * starting at depth.
     *
     * Note that the depth is the depth of the last node that was filled and to
     * which a sibling should be added. But depending on the returned start
     * time, the actual new branch may start at a lower depth if the start time
     * happens to be lesser than the parent's start time.
     *
     * @param depth
     *            The depth of the last node that was filled and at which the
     *            new branch should start.
     * @param interval
     *            The interval that is about to be inserted
     * @return The value that should be the start time of the sibling node
     */
    protected abstract long getNewBranchStart(int depth, E interval);

    /**
     * Method to add a sibling to any node in the latest branch. This will add
     * children back down to the leaf level, if needed.
     *
     * @param depth
     *            The depth in latestBranch where we start adding
     * @param newNodeStartTime
     *            The start time of the new node
     */
    private final void addSiblingNode(int depth, long newNodeStartTime) {
        synchronized (fLatestBranch) {
            final long splitTime = fTreeEnd;

            if (depth >= fLatestBranch.size()) {
                /*
                 * We need to make sure (indexOfNode - 1) doesn't get the last
                 * node in the branch, because that one is a Leaf Node.
                 */
                throw new IllegalStateException();
            }

            /* Check if we need to add a new root node */
            if (depth == 0) {
                addNewRootNode(newNodeStartTime);
                return;
            }

            /*
             * Check if we can indeed add a child to the target parent and if
             * the new start time is not before the target parent.
             */
            if (fLatestBranch.get(depth - 1).getNbChildren() == fMaxChildren ||
                    newNodeStartTime < fLatestBranch.get(depth - 1).getNodeStart()) {
                /* If not, add a branch starting one level higher instead */
                addSiblingNode(depth - 1, newNodeStartTime);
                return;
            }

            /*
             * Close nodes from the leaf up because some parent nodes may need
             * to get updated when their children are closed
             */
            for (int i = fLatestBranch.size() - 1; i >= depth; i--) {
                fLatestBranch.get(i).closeThisNode(splitTime);
                fTreeIO.writeNode(fLatestBranch.get(i));
            }

            /* Split off the new branch from the old one */
            for (int i = depth; i < fLatestBranch.size(); i++) {
                N prevNode = fLatestBranch.get(i - 1);
                N newNode;

                switch (fLatestBranch.get(i).getNodeType()) {
                case CORE:
                    newNode = initNewCoreNode(prevNode.getSequenceNumber(), newNodeStartTime);
                    break;
                case LEAF:
                    newNode = initNewLeafNode(prevNode.getSequenceNumber(), newNodeStartTime);
                    break;
                default:
                    throw new IllegalStateException();
                }

                prevNode.linkNewChild(newNode);
                fLatestBranch.set(i, newNode);
            }
        }
    }

    /**
     * Similar to the previous method, except here we rebuild a completely new
     * latestBranch
     */
    private void addNewRootNode(long newNodeStartTime) {
        final long nodeEnd = fTreeEnd;

        N oldRootNode = fLatestBranch.get(0);
        N newRootNode = initNewCoreNode(-1, fTreeStart);

        /* Tell the old root node that it isn't root anymore */
        oldRootNode.setParentSequenceNumber(newRootNode.getSequenceNumber());

        /* Close off the whole current latestBranch */
        for (int i = fLatestBranch.size() - 1; i >= 0; i--) {
            fLatestBranch.get(i).closeThisNode(nodeEnd);
            fTreeIO.writeNode(fLatestBranch.get(i));
        }

        /* Link the new root to its first child (the previous root node) */
        newRootNode.linkNewChild(oldRootNode);

        /* Rebuild a new latestBranch */
        int depth = fLatestBranch.size();
        fLatestBranch.clear();
        fLatestBranch.add(newRootNode);

        // Create new coreNode
        for (int i = 1; i < depth; i++) {
            N prevNode = fLatestBranch.get(i - 1);
            N newNode = initNewCoreNode(prevNode.getSequenceNumber(), newNodeStartTime);
            prevNode.linkNewChild(newNode);
            fLatestBranch.add(newNode);
        }

        // Create the new leafNode
        N prevNode = fLatestBranch.get(depth - 1);
        N newNode = initNewLeafNode(prevNode.getSequenceNumber(), newNodeStartTime);
        prevNode.linkNewChild(newNode);
        fLatestBranch.add(newNode);
    }

    /**
     * Update the tree's end time with this interval data
     *
     * @param interval
     *            The interval that was just added to the tree
     */
    protected void updateEndTime(E interval) {
        fTreeEnd = Math.max(fTreeEnd, interval.getEnd());
    }

    @Override
    public void closeTree(long requestedEndTime) {
        /* This is an important operation, queries can wait */
        synchronized (fLatestBranch) {
            /*
             * Work-around the "empty branches" that get created when the root
             * node becomes full. Overwrite the tree's end time with the
             * original wanted end-time, to ensure no queries are sent into
             * those empty nodes.
             */
            fTreeEnd = requestedEndTime;

            /* Close off the latest branch of the tree */
            for (int i = fLatestBranch.size() - 1; i >= 0; i--) {
                fLatestBranch.get(i).closeThisNode(fTreeEnd);
                fTreeIO.writeNode(fLatestBranch.get(i));
            }

            try (FileOutputStream fc = fTreeIO.getFileWriter(-1);) {
                ByteBuffer buffer = ByteBuffer.allocate(TREE_HEADER_SIZE);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.clear();

                buffer.putInt(getMagicNumber());

                buffer.putInt(getFileVersion());
                buffer.putInt(fProviderVersion);

                buffer.putInt(fBlockSize);
                buffer.putInt(fMaxChildren);

                buffer.putInt(fNodeCount);

                /* root node seq. nb */
                buffer.putInt(fLatestBranch.get(0).getSequenceNumber());

                /* start time of this history */
                buffer.putLong(fLatestBranch.get(0).getNodeStart());

                buffer.flip();
                fc.write(buffer.array());
                /* done writing the file header */

            } catch (IOException e) {
                /*
                 * If we were able to write so far, there should not be any
                 * problem at this point...
                 */
                throw new RuntimeException("State system write error"); //$NON-NLS-1$
            }
        }
    }

    @Override
    public Iterable<E> getMatchingIntervals(RangeCondition<Long> timeCondition,
            Predicate<E> extraPredicate) {

        // TODO Change this to evaluate the nodes lazily

        List<Iterable<E>> intervalsOfNodes = new LinkedList<>();

        /* Queue is a stack of nodes containing nodes intersecting t */
        Deque<Integer> queue = new LinkedList<>();
        /* We start by reading the information in the root node */
        queue.add(getRootNode().getSequenceNumber());

        /* Then we follow the down in the relevant children */
        try {
            while (!queue.isEmpty()) {
                int sequenceNumber = queue.pop();
                HTNode<E> currentNode = readNode(sequenceNumber);
                RangeCondition<Long> nodeCondition = timeCondition.subCondition(
                        currentNode.getNodeStart(), currentNode.getNodeEnd());

                if (nodeCondition == null) {
                    continue;
                }

                if (currentNode.getNodeType() == HTNode.NodeType.CORE) {
                    /* Here we add the relevant children nodes for BFS */
                    queue.addAll(currentNode.selectNextChildren(nodeCondition));
                }
                Iterable<E> nodeIntervals = currentNode.getMatchingIntervals(nodeCondition, extraPredicate);
                intervalsOfNodes.add(nodeIntervals);
            }
        } catch (ClosedChannelException e) {
        }
        return Iterables.concat(intervalsOfNodes);
    }

    @Override
    public String toString() {
        return "Information on the current tree:\n\n" + "Blocksize: " //$NON-NLS-1$ //$NON-NLS-2$
                + fBlockSize + "\n" + "Max nb. of children per node: " //$NON-NLS-1$//$NON-NLS-2$
                + fMaxChildren + "\n" + "Number of nodes: " + fNodeCount //$NON-NLS-1$//$NON-NLS-2$
                + "\n" + "Depth of the tree: " + fLatestBranch.size() + "\n" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + "Size of the treefile: " + getFileSize() + "\n" //$NON-NLS-1$//$NON-NLS-2$
                + "Root node has sequence number: " //$NON-NLS-1$
                + fLatestBranch.get(0).getSequenceNumber() + "\n" //$NON-NLS-1$
                + "'Latest leaf' has sequence number: " //$NON-NLS-1$
                + fLatestBranch.get(fLatestBranch.size() - 1).getSequenceNumber();
    }


    // ------------------------------------------------------------------------
    // Test-specific methods
    // ------------------------------------------------------------------------

    /**
     * Get the current depth of the tree.
     *
     * @return The current depth
     */
    @VisibleForTesting
    protected int getDepth() {
        return getLatestBranch().size();
    }

    /**
     * Get the leaf (bottom-most) node of the latest branch.
     *
     * @return The latest leaf
     */
    @VisibleForTesting
    protected N getLatestLeaf() {
        List<N> latestBranch = getLatestBranch();
        return latestBranch.get(latestBranch.size() - 1);
    }

    /**
     * Verify a node's specific information about a child.
     *
     * @param parent
     *            The parent node
     * @param index
     *            The index of the child in the parent's extra data
     * @param child
     *            The child node to verify
     * @return False if a problem was found, true otherwise
     */
    @VisibleForTesting
    protected boolean verifyChildrenSpecific(N parent,
            int index,
            N child) {
        // Nothing to do for the default implementation
        return true;
    }

    /**
     * This method should verify in the whole time range of the parent node that
     * the child node appears or not as a next children for a given timestamp.
     *
     * @param parent
     *            The parent node
     * @param child
     *            The child node
     * @return False if a problem was found, true otherwise
     */
    @VisibleForTesting
    protected boolean verifyIntersectingChildren(N parent, N child) {
        int childSequence = child.getSequenceNumber();
        boolean shouldBeInCollection;
        Collection<Integer> nextChildren;
        for (long t = parent.getNodeStart(); t < parent.getNodeEnd(); t++) {
            shouldBeInCollection = true;
            nextChildren = parent.selectNextChildren(RangeCondition.singleton(t));
            if (shouldBeInCollection != nextChildren.contains(childSequence)) {
                return false;
            }
        }
        return true;
    }
}
