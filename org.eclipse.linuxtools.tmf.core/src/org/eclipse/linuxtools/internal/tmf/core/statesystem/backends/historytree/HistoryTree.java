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

import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateProvider;

/**
 * Meta-container for the History Tree. This structure contains all the
 * high-level data relevant to the tree.
 *
 * @author Alexandre Montplaisir
 */
public class HistoryTree {

    /**
     * Size of the "tree header" in the tree-file The nodes will use this offset
     * to know where they should be in the file. This should always be a
     * multiple of 4K.
     */
    public static final int TREE_HEADER_SIZE = 4096;

    private static final int HISTORY_FILE_MAGIC_NUMBER = 0x05FFA900;

    /** File format version. Increment when breaking compatibility. */
    private static final int FILE_VERSION = 3;

    // ------------------------------------------------------------------------
    // Tree-specific configuration
    // ------------------------------------------------------------------------

    /** Container for all the configuration constants */
    private final HTConfig config;

    /** Reader/writer object */
    private final HT_IO treeIO;

    // ------------------------------------------------------------------------
    // Variable Fields (will change throughout the existence of the SHT)
    // ------------------------------------------------------------------------

    /** Latest timestamp found in the tree (at any given moment) */
    private long treeEnd;

    /** The total number of nodes that exists in this tree */
    private int nodeCount;

    /** "Cache" to keep the active nodes in memory */
    private final List<CoreNode> latestBranch;

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
    public HistoryTree(HTConfig conf) throws IOException {
        /*
         * Simple check to make sure we have enough place in the 0th block
         * for the tree configuration
         */
        if (conf.getBlockSize() < TREE_HEADER_SIZE) {
            throw new IllegalArgumentException();
        }

        config = conf;
        treeEnd = conf.getTreeStart();
        nodeCount = 0;
        latestBranch = Collections.synchronizedList(new ArrayList<CoreNode>());

        /* Prepare the IO object */
        treeIO = new HT_IO(config, true);

        /* Add the first node to the tree */
        CoreNode firstNode = initNewCoreNode(-1, conf.getTreeStart());
        latestBranch.add(firstNode);
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
    public HistoryTree(File existingStateFile, int expProviderVersion) throws IOException {
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
                    expProviderVersion != ITmfStateProvider.IGNORE_PROVIDER_VERSION) {
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

            this.nodeCount = buffer.getInt();
            rootNodeSeqNb = buffer.getInt();
            startTime = buffer.getLong();

            this.config = new HTConfig(existingStateFile, bs, maxc, expProviderVersion, startTime);
        }

        /*
         * FIXME We close fis here and the TreeIO will then reopen the same
         * file, not extremely elegant. But how to pass the information here to
         * the SHT otherwise?
         */
        this.treeIO = new HT_IO(config, false);

        this.latestBranch = buildLatestBranch(rootNodeSeqNb);
        this.treeEnd = getRootNode().getNodeEnd();

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
    private List<CoreNode> buildLatestBranch(int rootNodeSeqNb) throws ClosedChannelException {
        HTNode nextChildNode;

        List<CoreNode> list = new ArrayList<>();

        nextChildNode = treeIO.readNode(rootNodeSeqNb);
        list.add((CoreNode) nextChildNode);
        while (list.get(list.size() - 1).getNbChildren() > 0) {
            nextChildNode = treeIO.readNode(list.get(list.size() - 1).getLatestChild());
            list.add((CoreNode) nextChildNode);
        }
        return Collections.synchronizedList(list);
    }

    /**
     * "Save" the tree to disk. This method will cause the treeIO object to
     * commit all nodes to disk and then return the RandomAccessFile descriptor
     * so the Tree object can save its configuration into the header of the
     * file.
     *
     * @param requestedEndTime
     *            The greatest timestamp present in the history tree
     */
    public void closeTree(long requestedEndTime) {
        /* This is an important operation, queries can wait */
        synchronized (latestBranch) {
            /*
             * Work-around the "empty branches" that get created when the root
             * node becomes full. Overwrite the tree's end time with the
             * original wanted end-time, to ensure no queries are sent into
             * those empty nodes.
             *
             * This won't be needed once extended nodes are implemented.
             */
            this.treeEnd = requestedEndTime;

            /* Close off the latest branch of the tree */
            for (int i = 0; i < latestBranch.size(); i++) {
                latestBranch.get(i).closeThisNode(treeEnd);
                treeIO.writeNode(latestBranch.get(i));
            }

            try (FileChannel fc = treeIO.getFcOut();) {
                ByteBuffer buffer = ByteBuffer.allocate(TREE_HEADER_SIZE);
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.clear();

                /* Save the config of the tree to the header of the file */
                fc.position(0);

                buffer.putInt(HISTORY_FILE_MAGIC_NUMBER);

                buffer.putInt(FILE_VERSION);
                buffer.putInt(config.getProviderVersion());

                buffer.putInt(config.getBlockSize());
                buffer.putInt(config.getMaxChildren());

                buffer.putInt(nodeCount);

                /* root node seq. nb */
                buffer.putInt(latestBranch.get(0).getSequenceNumber());

                /* start time of this history */
                buffer.putLong(latestBranch.get(0).getNodeStart());

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

    /**
     * Get the start time of this tree.
     *
     * @return The start time
     */
    public long getTreeStart() {
        return config.getTreeStart();
    }

    /**
     * Get the current end time of this tree.
     *
     * @return The end time
     */
    public long getTreeEnd() {
        return treeEnd;
    }

    /**
     * Get the number of nodes in this tree.
     *
     * @return The number of nodes
     */
    public int getNodeCount() {
        return nodeCount;
    }

    /**
     * Get the current root node of this tree
     *
     * @return The root node
     */
    public CoreNode getRootNode() {
        return latestBranch.get(0);
    }

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
    public FileInputStream supplyATReader() {
        return treeIO.supplyATReader(getNodeCount());
    }

    /**
     * Return the file to which we will write the attribute tree.
     *
     * @return The file to which we will write the attribute tree
     */
    public File supplyATWriterFile() {
        return config.getStateFile();
    }

    /**
     * Return the position in the file (given by {@link #supplyATWriterFile})
     * where to start writing the attribute tree.
     *
     * @return The position in the file where to start writing
     */
    public long supplyATWriterFilePos() {
        return HistoryTree.TREE_HEADER_SIZE
                + ((long) getNodeCount() * config.getBlockSize());
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
    public HTNode readNode(int seqNumber) throws ClosedChannelException {
        /* Try to read the node from memory */
        synchronized (latestBranch) {
            for (HTNode node : latestBranch) {
                if (node.getSequenceNumber() == seqNumber) {
                    return node;
                }
            }
        }

        /* Read the node from disk */
        return treeIO.readNode(seqNumber);
    }

    /**
     * Write a node object to the history file.
     *
     * @param node
     *            The node to write to disk
     */
    public void writeNode(HTNode node) {
        treeIO.writeNode(node);
    }

    /**
     * Close the history file.
     */
    public void closeFile() {
        treeIO.closeFile();
    }

    /**
     * Delete the history file.
     */
    public void deleteFile() {
        treeIO.deleteFile();
    }

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
    public void insertInterval(HTInterval interval) throws TimeRangeException {
        if (interval.getStartTime() < config.getTreeStart()) {
            throw new TimeRangeException();
        }
        tryInsertAtNode(interval, latestBranch.size() - 1);
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
        HTNode targetNode = latestBranch.get(indexOfNode);

        /* Verify if there is enough room in this node to store this interval */
        if (interval.getIntervalSize() > targetNode.getNodeFreeSpace()) {
            /* Nope, not enough room. Insert in a new sibling instead. */
            addSiblingNode(indexOfNode);
            tryInsertAtNode(interval, latestBranch.size() - 1);
            return;
        }

        /* Make sure the interval time range fits this node */
        if (interval.getStartTime() < targetNode.getNodeStart()) {
            /*
             * No, this interval starts before the startTime of this node. We
             * need to check recursively in parents if it can fit.
             */
            assert (indexOfNode >= 1);
            tryInsertAtNode(interval, indexOfNode - 1);
            return;
        }

        /*
         * Ok, there is room, and the interval fits in this time slot. Let's add
         * it.
         */
        targetNode.addInterval(interval);

        /* Update treeEnd if needed */
        if (interval.getEndTime() > this.treeEnd) {
            this.treeEnd = interval.getEndTime();
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
        synchronized (latestBranch) {
            final long splitTime = treeEnd;

            assert (indexOfNode < latestBranch.size());

            /* Check if we need to add a new root node */
            if (indexOfNode == 0) {
                addNewRootNode();
                return;
            }

            /* Check if we can indeed add a child to the target parent */
            if (latestBranch.get(indexOfNode - 1).getNbChildren() == config.getMaxChildren()) {
                /* If not, add a branch starting one level higher instead */
                addSiblingNode(indexOfNode - 1);
                return;
            }

            /* Split off the new branch from the old one */
            for (int i = indexOfNode; i < latestBranch.size(); i++) {
                latestBranch.get(i).closeThisNode(splitTime);
                treeIO.writeNode(latestBranch.get(i));

                CoreNode prevNode = latestBranch.get(i - 1);
                CoreNode newNode = initNewCoreNode(prevNode.getSequenceNumber(),
                        splitTime + 1);
                prevNode.linkNewChild(newNode);

                latestBranch.set(i, newNode);
            }
        }
    }

    /**
     * Similar to the previous method, except here we rebuild a completely new
     * latestBranch
     */
    private void addNewRootNode() {
        final long splitTime = this.treeEnd;

        CoreNode oldRootNode = latestBranch.get(0);
        CoreNode newRootNode = initNewCoreNode(-1, config.getTreeStart());

        /* Tell the old root node that it isn't root anymore */
        oldRootNode.setParentSequenceNumber(newRootNode.getSequenceNumber());

        /* Close off the whole current latestBranch */

        for (int i = 0; i < latestBranch.size(); i++) {
            latestBranch.get(i).closeThisNode(splitTime);
            treeIO.writeNode(latestBranch.get(i));
        }

        /* Link the new root to its first child (the previous root node) */
        newRootNode.linkNewChild(oldRootNode);

        /* Rebuild a new latestBranch */
        int depth = latestBranch.size();
        latestBranch.clear();
        latestBranch.add(newRootNode);
        for (int i = 1; i < depth + 1; i++) {
            CoreNode prevNode = latestBranch.get(i - 1);
            CoreNode newNode = initNewCoreNode(prevNode.getParentSequenceNumber(),
                    splitTime + 1);
            prevNode.linkNewChild(newNode);
            latestBranch.add(newNode);
        }
    }

    /**
     * Add a new empty node to the tree.
     *
     * @param parentSeqNumber
     *            Sequence number of this node's parent
     * @param startTime
     *            Start time of the new node
     * @return The newly created node
     */
    private CoreNode initNewCoreNode(int parentSeqNumber, long startTime) {
        CoreNode newNode = new CoreNode(config, this.nodeCount, parentSeqNumber,
                startTime);
        this.nodeCount++;

        /* Update the treeEnd if needed */
        if (startTime >= this.treeEnd) {
            this.treeEnd = startTime + 1;
        }
        return newNode;
    }

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
        assert (potentialNextSeqNb != currentNode.getSequenceNumber());

        /*
         * Since this code path is quite performance-critical, avoid iterating
         * through the whole latestBranch array if we know for sure the next
         * node has to be on disk
         */
        if (currentNode.isOnDisk()) {
            return treeIO.readNode(potentialNextSeqNb);
        }
        return readNode(potentialNextSeqNb);
    }

    /**
     * Get the current size of the history file.
     *
     * @return The history file size
     */
    public long getFileSize() {
        return config.getStateFile().length();
    }

    // ------------------------------------------------------------------------
    // Test/debugging methods
    // ------------------------------------------------------------------------

    /**
     * Debugging method to make sure all intervals contained in the given node
     * have valid start and end times.
     *
     * @param zenode
     *            The node to check
     * @return True if everything is fine, false if there is at least one
     *         invalid timestamp (end time < start time, time outside of the
     *         range of the node, etc.)
     */
    @SuppressWarnings("nls")
    public boolean checkNodeIntegrity(HTNode zenode) {
        /* Only used for debugging, shouldn't be externalized */
        HTNode otherNode;
        CoreNode node;
        StringBuffer buf = new StringBuffer();
        boolean ret = true;

        // FIXME /* Only testing Core Nodes for now */
        if (!(zenode instanceof CoreNode)) {
            return true;
        }

        node = (CoreNode) zenode;

        try {
            /*
             * Test that this node's start and end times match the start of the
             * first child and the end of the last child, respectively
             */
            if (node.getNbChildren() > 0) {
                otherNode = treeIO.readNode(node.getChild(0));
                if (node.getNodeStart() != otherNode.getNodeStart()) {
                    buf.append("Start time of node (" + node.getNodeStart() + ") "
                            + "does not match start time of first child " + "("
                            + otherNode.getNodeStart() + "), " + "node #"
                            + otherNode.getSequenceNumber() + ")\n");
                    ret = false;
                }
                if (node.isOnDisk()) {
                    otherNode = treeIO.readNode(node.getLatestChild());
                    if (node.getNodeEnd() != otherNode.getNodeEnd()) {
                        buf.append("End time of node (" + node.getNodeEnd()
                                + ") does not match end time of last child ("
                                + otherNode.getNodeEnd() + ", node #"
                                + otherNode.getSequenceNumber() + ")\n");
                        ret = false;
                    }
                }
            }

            /*
             * Test that the childStartTimes[] array matches the real nodes' start
             * times
             */
            for (int i = 0; i < node.getNbChildren(); i++) {
                otherNode = treeIO.readNode(node.getChild(i));
                if (otherNode.getNodeStart() != node.getChildStart(i)) {
                    buf.append("  Expected start time of child node #"
                            + node.getChild(i) + ": " + node.getChildStart(i)
                            + "\n" + "  Actual start time of node #"
                            + otherNode.getSequenceNumber() + ": "
                            + otherNode.getNodeStart() + "\n");
                    ret = false;
                }
            }

        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }

        if (!ret) {
            System.out.println("");
            System.out.println("SHT: Integrity check failed for node #"
                    + node.getSequenceNumber() + ":");
            System.out.println(buf.toString());
        }
        return ret;
    }

    /**
     * Check the integrity of all the nodes in the tree. Calls
     * {@link #checkNodeIntegrity} for every node in the tree.
     */
    public void checkIntegrity() {
        try {
            for (int i = 0; i < nodeCount; i++) {
                checkNodeIntegrity(treeIO.readNode(i));
            }
        } catch (ClosedChannelException e) {
        }
    }

    /* Only used for debugging, shouldn't be externalized */
    @SuppressWarnings("nls")
    @Override
    public String toString() {
        return "Information on the current tree:\n\n" + "Blocksize: "
                + config.getBlockSize() + "\n" + "Max nb. of children per node: "
                + config.getMaxChildren() + "\n" + "Number of nodes: " + nodeCount
                + "\n" + "Depth of the tree: " + latestBranch.size() + "\n"
                + "Size of the treefile: " + this.getFileSize() + "\n"
                + "Root node has sequence number: "
                + latestBranch.get(0).getSequenceNumber() + "\n"
                + "'Latest leaf' has sequence number: "
                + latestBranch.get(latestBranch.size() - 1).getSequenceNumber();
    }

    /**
     * Start at currentNode and print the contents of all its children, in
     * pre-order. Give the root node in parameter to visit the whole tree, and
     * have a nice overview.
     */
    /* Only used for debugging, shouldn't be externalized */
    @SuppressWarnings("nls")
    private void preOrderPrint(PrintWriter writer, boolean printIntervals,
            CoreNode currentNode, int curDepth) {

        writer.println(currentNode.toString());
        if (printIntervals) {
            currentNode.debugPrintIntervals(writer);
        }

        try {
            for (int i = 0; i < currentNode.getNbChildren(); i++) {
                HTNode nextNode = treeIO.readNode(currentNode.getChild(i));
                assert (nextNode instanceof CoreNode); // TODO temporary
                for (int j = 0; j < curDepth; j++) {
                    writer.print("  ");
                }
                writer.print("+-");
                preOrderPrint(writer, printIntervals, (CoreNode) nextNode,
                              curDepth + 1);
            }
        } catch (ClosedChannelException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print out the full tree for debugging purposes
     *
     * @param writer
     *            PrintWriter in which to write the output
     * @param printIntervals
     *            Flag to enable full output of the interval information
     */
    public void debugPrintFullTree(PrintWriter writer, boolean printIntervals) {
        /* Only used for debugging, shouldn't be externalized */

        this.preOrderPrint(writer, false, latestBranch.get(0), 0);

        if (printIntervals) {
            writer.println("\nDetails of intervals:"); //$NON-NLS-1$
            this.preOrderPrint(writer, true, latestBranch.get(0), 0);
        }
        writer.println('\n');
    }

}
