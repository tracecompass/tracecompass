/*******************************************************************************
 * Copyright (c) 2010, 2016 Ericsson, École Polytechnique de Montréal, and others
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Florian Wininger - Add Extension and Leaf Node
 *   Patrick Tasse - Add message to exceptions
 *******************************************************************************/

package org.eclipse.tracecompass.internal.statesystem.core.backend.historytree;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.statesystem.core.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableList;

/**
 * Meta-container for the History Tree. This structure contains all the
 * high-level data relevant to the tree.
 *
 * @author Alexandre Montplaisir
 */
public class HistoryTreeClassic implements IHistoryTree {

    /**
     * The magic number for this file format. Package-private for the factory
     * class.
     */
    static final int HISTORY_FILE_MAGIC_NUMBER = 0x05FFA900;

    /** File format version. Increment when breaking compatibility. */
    private static final int FILE_VERSION = 7;

    // ------------------------------------------------------------------------
    // Tree-specific configuration
    // ------------------------------------------------------------------------

    /** Container for all the configuration constants */
    private final HTConfig fConfig;

    /** Reader/writer object */
    private final HT_IO fTreeIO;

    // ------------------------------------------------------------------------
    // Variable Fields (will change throughout the existence of the SHT)
    // ------------------------------------------------------------------------

    /** Latest timestamp found in the tree (at any given moment) */
    private long fTreeEnd;

    /** The total number of nodes that exists in this tree */
    private int fNodeCount;

    /** "Cache" to keep the active nodes in memory */
    private final @NonNull List<@NonNull HTNode> fLatestBranch;

    // ------------------------------------------------------------------------
    // Constructors/"Destructors"
    // ------------------------------------------------------------------------

    /**
     * Create a new State History from scratch, using a {@link HTConfig} object
     * for configuration.
     *
     * @param conf
     *            The config to use for this History Tree.
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public HistoryTreeClassic(HTConfig conf) throws IOException {
        /*
         * Simple check to make sure we have enough place in the 0th block for
         * the tree configuration
         */
        if (conf.getBlockSize() < TREE_HEADER_SIZE) {
            throw new IllegalArgumentException();
        }

        fConfig = conf;
        fTreeEnd = conf.getTreeStart();
        fNodeCount = 0;
        fLatestBranch = Collections.synchronizedList(new ArrayList<>());

        /* Prepare the IO object */
        fTreeIO = new HT_IO(fConfig, true);

        /* Add the first node to the tree */
        LeafNode firstNode = initNewLeafNode(-1, conf.getTreeStart());
        fLatestBranch.add(firstNode);
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
    public HistoryTreeClassic(File existingStateFile, int expProviderVersion) throws IOException {
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
            fc.read(buffer);
            buffer.flip();

            /*
             * Check the magic number to make sure we're opening the right type
             * of file
             */
            res = buffer.getInt();
            if (res != HISTORY_FILE_MAGIC_NUMBER) {
                throw new IOException("Wrong magic number"); //$NON-NLS-1$
            }

            res = buffer.getInt(); /* File format version number */
            if (res != FILE_VERSION) {
                throw new IOException("Mismatching History Tree file format versions"); //$NON-NLS-1$
            }

            res = buffer.getInt(); /* Event handler's version number */
            if (res != expProviderVersion &&
                    expProviderVersion != ITmfStateSystemBuilder.IGNORE_PROVIDER_VERSION) {
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

            fConfig = new HTConfig(existingStateFile, bs, maxc, expProviderVersion, startTime);
        }

        /*
         * FIXME We close fis here and the TreeIO will then reopen the same
         * file, not extremely elegant. But how to pass the information here to
         * the SHT otherwise?
         */
        fTreeIO = new HT_IO(fConfig, false);

        fLatestBranch = buildLatestBranch(rootNodeSeqNb);
        fTreeEnd = getRootNode().getNodeEnd();

        /*
         * Make sure the history start time we read previously is consistent
         * with was is actually in the root node.
         */
        if (startTime != getRootNode().getNodeStart()) {
            throw new IOException("Inconsistent start times in the" + //$NON-NLS-1$
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
    private @NonNull List<@NonNull HTNode> buildLatestBranch(int rootNodeSeqNb) throws ClosedChannelException {
        List<@NonNull HTNode> list = new ArrayList<>();

        HTNode nextChildNode = fTreeIO.readNode(rootNodeSeqNb);
        list.add(nextChildNode);

        /* Follow the last branch up to the leaf */
        while (nextChildNode.getNodeType() == HTNode.NodeType.CORE) {
            nextChildNode = fTreeIO.readNode(((CoreNode) nextChildNode).getLatestChild());
            list.add(nextChildNode);
        }
        return Collections.synchronizedList(list);
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
             *
             * This won't be needed once extended nodes are implemented.
             */
            fTreeEnd = requestedEndTime;

            /* Close off the latest branch of the tree */
            for (int i = 0; i < fLatestBranch.size(); i++) {
                fLatestBranch.get(i).closeThisNode(fTreeEnd);
                fTreeIO.writeNode(fLatestBranch.get(i));
            }

            try (FileChannel fc = fTreeIO.getFcOut();) {
                ByteBuffer buffer = ByteBuffer.allocate(TREE_HEADER_SIZE);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.clear();

                /* Save the config of the tree to the header of the file */
                fc.position(0);

                buffer.putInt(HISTORY_FILE_MAGIC_NUMBER);

                buffer.putInt(FILE_VERSION);
                buffer.putInt(fConfig.getProviderVersion());

                buffer.putInt(fConfig.getBlockSize());
                buffer.putInt(fConfig.getMaxChildren());

                buffer.putInt(fNodeCount);

                /* root node seq. nb */
                buffer.putInt(fLatestBranch.get(0).getSequenceNumber());

                /* start time of this history */
                buffer.putLong(fLatestBranch.get(0).getNodeStart());

                buffer.flip();
                int res = fc.write(buffer);
                assert (res <= TREE_HEADER_SIZE);
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

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    @Override
    public long getTreeStart() {
        return fConfig.getTreeStart();
    }

    @Override
    public long getTreeEnd() {
        return fTreeEnd;
    }

    @Override
    public int getNodeCount() {
        return fNodeCount;
    }

    @Override
    public HTNode getRootNode() {
        return fLatestBranch.get(0);
    }

    /**
     * Return the latest branch of the tree. That branch is immutable. Used for
     * unit testing and debugging.
     *
     * @return The immutable latest branch
     */
    @VisibleForTesting
    protected List<@NonNull HTNode> getLatestBranch() {
        return ImmutableList.copyOf(fLatestBranch);
    }

    /**
     * Read a node at sequence number
     *
     * @param seqNum
     *            The sequence number of the node to read
     * @return The HTNode object
     * @throws ClosedChannelException
     *             Exception thrown when reading the node
     */
    @VisibleForTesting
    protected @NonNull HTNode getNode(int seqNum) throws ClosedChannelException {
        // First, check in the latest branch if the node is there
        for (HTNode node : fLatestBranch) {
            if (node.getSequenceNumber() == seqNum) {
                return node;
            }
        }
        return fTreeIO.readNode(seqNum);
    }

    // ------------------------------------------------------------------------
    // HT_IO interface
    // ------------------------------------------------------------------------

    @Override
    public FileInputStream supplyATReader() {
        return fTreeIO.supplyATReader(getNodeCount());
    }

    @Override
    public File supplyATWriterFile() {
        return fConfig.getStateFile();
    }

    @Override
    public long supplyATWriterFilePos() {
        return IHistoryTree.TREE_HEADER_SIZE
                + ((long) getNodeCount() * fConfig.getBlockSize());
    }

    @Override
    public HTNode readNode(int seqNumber) throws ClosedChannelException {
        /* Try to read the node from memory */
        synchronized (fLatestBranch) {
            for (HTNode node : fLatestBranch) {
                if (node.getSequenceNumber() == seqNumber) {
                    return node;
                }
            }
        }

        /* Read the node from disk */
        return fTreeIO.readNode(seqNumber);
    }

    @Override
    public void writeNode(HTNode node) {
        fTreeIO.writeNode(node);
    }

    @Override
    public void closeFile() {
        fTreeIO.closeFile();
    }

    @Override
    public void deleteFile() {
        fTreeIO.deleteFile();
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

    @Override
    public void insertInterval(HTInterval interval) throws TimeRangeException {
        if (interval.getStartTime() < fConfig.getTreeStart()) {
            throw new TimeRangeException("Interval Start:" + interval.getStartTime() + ", Config Start:" + fConfig.getTreeStart()); //$NON-NLS-1$ //$NON-NLS-2$
        }
        tryInsertAtNode(interval, fLatestBranch.size() - 1);
    }

    /**
     * Inner method to find in which node we should add the interval.
     *
     * @param interval
     *            The interval to add to the tree
     * @param indexOfNode
     *            The index *in the latestBranch* where we are trying the
     *            insertion
     */
    private void tryInsertAtNode(HTInterval interval, int indexOfNode) {
        HTNode targetNode = fLatestBranch.get(indexOfNode);

        /* Verify if there is enough room in this node to store this interval */
        if (interval.getSizeOnDisk() > targetNode.getNodeFreeSpace()) {
            /* Nope, not enough room. Insert in a new sibling instead. */
            addSiblingNode(indexOfNode);
            tryInsertAtNode(interval, fLatestBranch.size() - 1);
            return;
        }

        /* Make sure the interval time range fits this node */
        if (interval.getStartTime() < targetNode.getNodeStart()) {
            /*
             * No, this interval starts before the startTime of this node. We
             * need to check recursively in parents if it can fit.
             */
            tryInsertAtNode(interval, indexOfNode - 1);
            return;
        }

        /*
         * Ok, there is room, and the interval fits in this time slot. Let's add
         * it.
         */
        targetNode.addInterval(interval);

        /* Update treeEnd if needed */
        if (interval.getEndTime() > fTreeEnd) {
            fTreeEnd = interval.getEndTime();
        }
    }

    /**
     * Method to add a sibling to any node in the latest branch. This will add
     * children back down to the leaf level, if needed.
     *
     * @param indexOfNode
     *            The index in latestBranch where we start adding
     */
    private void addSiblingNode(int indexOfNode) {
        synchronized (fLatestBranch) {
            final long splitTime = fTreeEnd;

            if (indexOfNode >= fLatestBranch.size()) {
                /*
                 * We need to make sure (indexOfNode - 1) doesn't get the last
                 * node in the branch, because that one is a Leaf Node.
                 */
                throw new IllegalStateException();
            }

            /* Check if we need to add a new root node */
            if (indexOfNode == 0) {
                addNewRootNode();
                return;
            }

            /* Check if we can indeed add a child to the target parent */
            if (((CoreNode) fLatestBranch.get(indexOfNode - 1)).getNbChildren() == fConfig.getMaxChildren()) {
                /* If not, add a branch starting one level higher instead */
                addSiblingNode(indexOfNode - 1);
                return;
            }

            /* Split off the new branch from the old one */
            for (int i = indexOfNode; i < fLatestBranch.size(); i++) {
                fLatestBranch.get(i).closeThisNode(splitTime);
                fTreeIO.writeNode(fLatestBranch.get(i));

                CoreNode prevNode = (CoreNode) fLatestBranch.get(i - 1);
                HTNode newNode;

                switch (fLatestBranch.get(i).getNodeType()) {
                case CORE:
                    newNode = initNewCoreNode(prevNode.getSequenceNumber(), splitTime + 1);
                    break;
                case LEAF:
                    newNode = initNewLeafNode(prevNode.getSequenceNumber(), splitTime + 1);
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
    private void addNewRootNode() {
        final long splitTime = fTreeEnd;

        HTNode oldRootNode = fLatestBranch.get(0);
        CoreNode newRootNode = initNewCoreNode(-1, fConfig.getTreeStart());

        /* Tell the old root node that it isn't root anymore */
        oldRootNode.setParentSequenceNumber(newRootNode.getSequenceNumber());

        /* Close off the whole current latestBranch */

        for (int i = 0; i < fLatestBranch.size(); i++) {
            fLatestBranch.get(i).closeThisNode(splitTime);
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
            CoreNode prevNode = (CoreNode) fLatestBranch.get(i - 1);
            CoreNode newNode = initNewCoreNode(prevNode.getSequenceNumber(),
                    splitTime + 1);
            prevNode.linkNewChild(newNode);
            fLatestBranch.add(newNode);
        }

        // Create the new leafNode
        CoreNode prevNode = (CoreNode) fLatestBranch.get(depth - 1);
        LeafNode newNode = initNewLeafNode(prevNode.getSequenceNumber(), splitTime + 1);
        prevNode.linkNewChild(newNode);
        fLatestBranch.add(newNode);
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
    private @NonNull CoreNode initNewCoreNode(int parentSeqNumber, long startTime) {
        CoreNode newNode = new CoreNode(fConfig, fNodeCount, parentSeqNumber,
                startTime);
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
    private @NonNull LeafNode initNewLeafNode(int parentSeqNumber, long startTime) {
        LeafNode newNode = new LeafNode(fConfig, fNodeCount, parentSeqNumber,
                startTime);
        fNodeCount++;
        return newNode;
    }

    @Override
    public HTNode selectNextChild(CoreNode currentNode, long t) throws ClosedChannelException {
        assert (currentNode.getNbChildren() > 0);
        int potentialNextSeqNb = currentNode.getSequenceNumber();

        for (int i = 0; i < currentNode.getNbChildren(); i++) {
            if (t >= currentNode.getChildStart(i)) {
                potentialNextSeqNb = currentNode.getChild(i);
            } else {
                break;
            }
        }

        /*
         * Once we exit this loop, we should have found a children to follow. If
         * we didn't, there's a problem.
         */
        if (potentialNextSeqNb == currentNode.getSequenceNumber()) {
            throw new IllegalStateException("No next child node found"); //$NON-NLS-1$
        }

        /*
         * Since this code path is quite performance-critical, avoid iterating
         * through the whole latestBranch array if we know for sure the next
         * node has to be on disk
         */
        if (currentNode.isOnDisk()) {
            return fTreeIO.readNode(potentialNextSeqNb);
        }
        return readNode(potentialNextSeqNb);
    }

    @Override
    public long getFileSize() {
        return fConfig.getStateFile().length();
    }

    // ------------------------------------------------------------------------
    // Test/debugging methods
    // ------------------------------------------------------------------------

    /* Only used for debugging, shouldn't be externalized */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "Information on the current tree:\n\n" + "Blocksize: "
                + fConfig.getBlockSize() + "\n" + "Max nb. of children per node: "
                + fConfig.getMaxChildren() + "\n" + "Number of nodes: " + fNodeCount
                + "\n" + "Depth of the tree: " + fLatestBranch.size() + "\n"
                + "Size of the treefile: " + getFileSize() + "\n"
                + "Root node has sequence number: "
                + fLatestBranch.get(0).getSequenceNumber() + "\n"
                + "'Latest leaf' has sequence number: "
                + fLatestBranch.get(fLatestBranch.size() - 1).getSequenceNumber();
    }

    /**
     * Start at currentNode and print the contents of all its children, in
     * pre-order. Give the root node in parameter to visit the whole tree, and
     * have a nice overview.
     */
    /* Only used for debugging, shouldn't be externalized */
    @SuppressWarnings("nls")
    private void preOrderPrint(PrintWriter writer, boolean printIntervals,
            HTNode currentNode, int curDepth, long ts) {

        writer.println(currentNode.toString());
        /* Print intervals only if timestamp is negative or within the range of the node */
        if (printIntervals &&
                (ts <= 0 ||
                (ts >= currentNode.getNodeStart() && ts <= currentNode.getNodeEnd()))) {
            currentNode.debugPrintIntervals(writer);
        }

        switch (currentNode.getNodeType()) {
        case LEAF:
            /* Stop if it's the leaf node */
            return;

        case CORE:
            try {
                final CoreNode node = (CoreNode) currentNode;
                /* Print the extensions, if any */
                int extension = node.getExtensionSequenceNumber();
                while (extension != -1) {
                    HTNode nextNode = fTreeIO.readNode(extension);
                    preOrderPrint(writer, printIntervals, nextNode, curDepth, ts);
                }

                /* Print the child nodes */
                for (int i = 0; i < node.getNbChildren(); i++) {
                    HTNode nextNode = fTreeIO.readNode(node.getChild(i));
                    for (int j = 0; j < curDepth; j++) {
                        writer.print("  ");
                    }
                    writer.print("+-");
                    preOrderPrint(writer, printIntervals, nextNode, curDepth + 1, ts);
                }
            } catch (ClosedChannelException e) {
                Activator.getDefault().logError(e.getMessage());
            }
            break;

        default:
            break;
        }
    }

    @Override
    public void debugPrintFullTree(PrintWriter writer, boolean printIntervals, long ts) {
        /* Only used for debugging, shouldn't be externalized */

        preOrderPrint(writer, false, fLatestBranch.get(0), 0, ts);

        if (printIntervals) {
            preOrderPrint(writer, true, fLatestBranch.get(0), 0, ts);
        }
        writer.println('\n');
    }

}
