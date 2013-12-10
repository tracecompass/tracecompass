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
 *
 */
class HistoryTree {

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

    /** How many nodes exist in this tree, total */
    private int nodeCount;

    /** "Cache" to keep the active nodes in memory */
    private List<CoreNode> latestBranch;

    // ------------------------------------------------------------------------
    // Constructors/"Destructors"
    // ------------------------------------------------------------------------

    /**
     * Create a new State History from scratch, using a SHTConfig object for
     * configuration
     */
    HistoryTree(HTConfig conf) throws IOException {
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
        latestBranch = new ArrayList<CoreNode>();

        /* Prepare the IO object */
        treeIO = new HT_IO(this, true);

        /* Add the first node to the tree */
        CoreNode firstNode = initNewCoreNode(-1, conf.getTreeStart());
        latestBranch.add(firstNode);
    }

    /**
     * "Reader" constructor : instantiate a SHTree from an existing tree file on
     * disk
     *
     * @param existingFileName
     *            Path/filename of the history-file we are to open
     * @param expProviderVersion
     *            The expected version of the state provider
     * @throws IOException
     */
    HistoryTree(File existingStateFile, int expProviderVersion) throws IOException {
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

        FileInputStream fis = new FileInputStream(existingStateFile);
        ByteBuffer buffer = ByteBuffer.allocate(TREE_HEADER_SIZE);
        FileChannel fc = fis.getChannel();
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.clear();
        fc.read(buffer);
        buffer.flip();

        /*
         * Check the magic number,to make sure we're opening the right type of
         * file
         */
        res = buffer.getInt();
        if (res != HISTORY_FILE_MAGIC_NUMBER) {
            fc.close();
            fis.close();
            throw new IOException("Wrong magic number"); //$NON-NLS-1$
        }

        res = buffer.getInt(); /* File format version number */
        if (res != FILE_VERSION) {
            fc.close();
            fis.close();
            throw new IOException("Mismatching History Tree file format versions"); //$NON-NLS-1$
        }

        res = buffer.getInt(); /* Event handler's version number */
        if (res != expProviderVersion &&
                expProviderVersion != ITmfStateProvider.IGNORE_PROVIDER_VERSION) {
            /*
             * The existing history was built using a event handler that doesn't
             * match the current one in the framework. Information could be all
             * wrong, so we'll force a rebuild of the history file instead.
             */
            fc.close();
            fis.close();
            throw new IOException("Mismatching event handler versions"); //$NON-NLS-1$
        }

        bs = buffer.getInt(); /* Block Size */
        maxc = buffer.getInt(); /* Max nb of children per node */

        this.nodeCount = buffer.getInt();
        rootNodeSeqNb = buffer.getInt();
        startTime = buffer.getLong();

        this.config = new HTConfig(existingStateFile, bs, maxc, expProviderVersion, startTime);
        fc.close();
        fis.close();
        /*
         * FIXME We close fis here and the TreeIO will then reopen the same
         * file, not extremely elegant. But how to pass the information here to
         * the SHT otherwise?
         */
        this.treeIO = new HT_IO(this, false);

        rebuildLatestBranch(rootNodeSeqNb);
        this.treeEnd = latestBranch.get(0).getNodeEnd();

        /*
         * Make sure the history start time we read previously is consistent
         * with was is actually in the root node.
         */
        if (startTime != latestBranch.get(0).getNodeStart()) {
            fc.close();
            fis.close();
            throw new IOException("Inconsistent start times in the" + //$NON-NLS-1$
                    "history file, it might be corrupted."); //$NON-NLS-1$
        }
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
    void closeTree(long requestedEndTime) {
        FileChannel fc;
        ByteBuffer buffer;
        int i, res;

        /*
         * Work-around the "empty branches" that get created when the root node
         * becomes full. Overwrite the tree's end time with the original wanted
         * end-time, to ensure no queries are sent into those empty nodes.
         *
         * This won't be needed once extended nodes are implemented.
         */
        this.treeEnd = requestedEndTime;

        /* Close off the latest branch of the tree */
        for (i = 0; i < latestBranch.size(); i++) {
            latestBranch.get(i).closeThisNode(treeEnd);
            treeIO.writeNode(latestBranch.get(i));
        }

        fc = treeIO.getFcOut();
        buffer = ByteBuffer.allocate(TREE_HEADER_SIZE);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.clear();

        /* Save the config of the tree to the header of the file */
        try {
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
            res = fc.write(buffer);
            assert (res <= TREE_HEADER_SIZE);
            /* done writing the file header */

        } catch (IOException e) {
            /* We should not have any problems at this point... */
        } finally {
            try {
                fc.close();
            } catch (IOException e) {
            }
        }
        return;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    HTConfig getConfig() {
        return config;
    }

    long getTreeStart() {
        return config.getTreeStart();
    }

    long getTreeEnd() {
        return treeEnd;
    }

    int getNodeCount() {
        return nodeCount;
    }

    HT_IO getTreeIO() {
        return treeIO;
    }

    List<CoreNode> getLatestBranch() {
        return Collections.unmodifiableList(latestBranch);
    }

    // ------------------------------------------------------------------------
    // Operations
    // ------------------------------------------------------------------------

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
    private void rebuildLatestBranch(int rootNodeSeqNb) throws ClosedChannelException {
        HTNode nextChildNode;

        this.latestBranch = new ArrayList<CoreNode>();

        nextChildNode = treeIO.readNodeFromDisk(rootNodeSeqNb);
        latestBranch.add((CoreNode) nextChildNode);
        while (latestBranch.get(latestBranch.size() - 1).getNbChildren() > 0) {
            nextChildNode = treeIO.readNodeFromDisk(latestBranch.get(latestBranch.size() - 1).getLatestChild());
            latestBranch.add((CoreNode) nextChildNode);
        }
    }

    /**
     * Insert an interval in the tree
     *
     * @param interval
     *            The interval to be inserted
     */
    void insertInterval(HTInterval interval) throws TimeRangeException {
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
        int i;
        CoreNode newNode, prevNode;
        long splitTime = treeEnd;

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
        for (i = indexOfNode; i < latestBranch.size(); i++) {
            latestBranch.get(i).closeThisNode(splitTime);
            treeIO.writeNode(latestBranch.get(i));

            prevNode = latestBranch.get(i - 1);
            newNode = initNewCoreNode(prevNode.getSequenceNumber(),
                    splitTime + 1);
            prevNode.linkNewChild(newNode);

            latestBranch.set(i, newNode);
        }
    }

    /**
     * Similar to the previous method, except here we rebuild a completely new
     * latestBranch
     */
    private void addNewRootNode() {
        int i, depth;
        CoreNode oldRootNode, newRootNode, newNode, prevNode;
        long splitTime = this.treeEnd;

        oldRootNode = latestBranch.get(0);
        newRootNode = initNewCoreNode(-1, config.getTreeStart());

        /* Tell the old root node that it isn't root anymore */
        oldRootNode.setParentSequenceNumber(newRootNode.getSequenceNumber());

        /* Close off the whole current latestBranch */
        for (i = 0; i < latestBranch.size(); i++) {
            latestBranch.get(i).closeThisNode(splitTime);
            treeIO.writeNode(latestBranch.get(i));
        }

        /* Link the new root to its first child (the previous root node) */
        newRootNode.linkNewChild(oldRootNode);

        /* Rebuild a new latestBranch */
        depth = latestBranch.size();
        latestBranch = new ArrayList<CoreNode>();
        latestBranch.add(newRootNode);
        for (i = 1; i < depth + 1; i++) {
            prevNode = latestBranch.get(i - 1);
            newNode = initNewCoreNode(prevNode.getParentSequenceNumber(),
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
    HTNode selectNextChild(CoreNode currentNode, long t) throws ClosedChannelException {
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
        if (currentNode.isDone()) {
            return treeIO.readNodeFromDisk(potentialNextSeqNb);
        }
        return treeIO.readNode(potentialNextSeqNb);
    }

    long getFileSize() {
        return config.getStateFile().length();
    }

    // ------------------------------------------------------------------------
    // Test/debugging methods
    // ------------------------------------------------------------------------

    /* Only used for debugging, shouldn't be externalized */
    @SuppressWarnings("nls")
    boolean checkNodeIntegrity(HTNode zenode) {

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
                if (node.isDone()) {
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

    void checkIntegrity() {
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
    void debugPrintFullTree(PrintWriter writer, boolean printIntervals) {
        /* Only used for debugging, shouldn't be externalized */

        this.preOrderPrint(writer, false, latestBranch.get(0), 0);

        if (printIntervals) {
            writer.println("\nDetails of intervals:"); //$NON-NLS-1$
            this.preOrderPrint(writer, true, latestBranch.get(0), 0);
        }
        writer.println('\n');
    }

}
