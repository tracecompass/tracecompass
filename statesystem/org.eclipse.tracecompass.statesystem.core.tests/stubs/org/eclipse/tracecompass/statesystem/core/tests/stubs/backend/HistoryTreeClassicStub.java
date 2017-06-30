/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.stubs.backend;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.channels.ClosedChannelException;
import java.util.Collection;
import java.util.List;

import org.eclipse.tracecompass.internal.statesystem.core.Activator;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTConfig;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTNode;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.IHistoryTree;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.ParentNode;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.classic.CoreNode;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.classic.HistoryTreeClassic;

import com.google.common.collect.Iterables;

/**
 * Stub class to unit test the history tree. You can set the size of the
 * interval section before using the tree, in order to fine-tune the test.
 *
 * Note to developers: This tree is not meant to be used with a backend. It just
 * exposes some info from the history tree.
 *
 * @author Geneviève Bastien
 */
public class HistoryTreeClassicStub extends HistoryTreeClassic {

    /**
     * Minimum size a block of this tree should have
     */
    public static final int MINIMUM_BLOCK_SIZE = IHistoryTree.TREE_HEADER_SIZE;

    /**
     * Constructor for this history tree stub
     *
     * @param conf
     *            The config to use for this History Tree.
     * @throws IOException
     *             If an error happens trying to open/write to the file
     *             specified in the config
     */
    public HistoryTreeClassicStub(HTConfig conf) throws IOException {
        super(conf);
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
    public HistoryTreeClassicStub(File existingStateFile, int expProviderVersion) throws IOException {
        super(existingStateFile, expProviderVersion);
    }

    // ------------------------------------------------------------------------
    // Extra test accessors
    // ------------------------------------------------------------------------

    @Override
    public List<HTNode> getLatestBranch() {
        /* Super method is not public */
        return checkNotNull(super.getLatestBranch());
    }

    /**
     * Get the latest leaf of the tree
     *
     * @return The current leaf node of the tree
     */
    public HTNode getLatestLeaf() {
        List<HTNode> latest = getLatestBranch();
        return Iterables.getLast(latest);
    }

    /**
     * Get the node from the latest branch at a given position, 0 being the root
     * and <size of latest branch - 1> being a leaf node.
     *
     * @param pos
     *            The position at which to return the node
     * @return The node at position pos
     */
    public HTNode getNodeAt(int pos) {
        List<HTNode> latest = getLatestBranch();
        return latest.get(pos);
    }

    /**
     * Get the depth of the tree
     *
     * @return The depth of the tree
     */
    public int getDepth() {
        return getLatestBranch().size();
    }

    // ------------------------------------------------------------------------
    // Debug printing methods
    // ------------------------------------------------------------------------

    /**
     * Print out the full tree for debugging purposes
     *
     * @param writer
     *            PrintWriter in which to write the output
     * @param printIntervals
     *            Flag to enable full output of the interval information
     * @param ts
     *            The timestamp that nodes have to intersect for intervals to be
     *            printed. A negative value will print intervals for all nodes.
     *            The timestamp only applies if printIntervals is true.
     */
    public void debugPrintFullTree(PrintWriter writer, boolean printIntervals, long ts) {
        preOrderPrint(writer, false, getLatestBranch().get(0), 0, ts);

        if (printIntervals) {
            preOrderPrint(writer, true, getLatestBranch().get(0), 0, ts);
        }
        writer.println('\n');
    }

    /**
     * Start at currentNode and print the contents of all its children, in
     * pre-order. Give the root node in parameter to visit the whole tree, and
     * have a nice overview.
     */
    private void preOrderPrint(PrintWriter writer, boolean printIntervals,
            HTNode currentNode, int curDepth, long ts) {

        writer.println(currentNode.toString());
        /*
         * Print intervals only if timestamp is negative or within the range of
         * the node
         */
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
                /* If node extensions were used, they would be printed here. */

                /* Print the child nodes */
                for (int i = 0; i < node.getNbChildren(); i++) {
                    HTNode nextNode = getTreeIO().readNode(node.getChild(i));
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

    // ------------------------------------------------------------------------
    // Assertion methods, for use with JUnit tests
    // ------------------------------------------------------------------------

    /**
     * Check the integrity of all the nodes in the tree. Calls
     * {@link #assertNodeIntegrity} for every node in the tree.
     */
    public void assertIntegrity() {
        try {
            for (int i = 0; i < getNodeCount(); i++) {
                assertNodeIntegrity(getNode(i));
            }
        } catch (ClosedChannelException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Debugging method to make sure all intervals contained in the given node
     * have valid start and end times.
     *
     * @param node
     *            The node to check
     */
    private void assertNodeIntegrity(HTNode node) {
        if (node instanceof ParentNode) {
            assertChildrenIntegrity((ParentNode) node);
        }

        /* Check that all intervals are within the node's range */
        // TODO: Get the intervals of a node

    }

    private void assertChildrenIntegrity(ParentNode node) {
        try {
            /*
             * Test that this node's start and end times match the start of the
             * first child and the end of the last child, respectively
             */
            if (node.getNbChildren() > 0) {
                HTNode childNode = getNode(node.getChild(0));
                assertEquals("Equals start time of parent " + node.getSequenceNumber() + " and first child " + childNode.getSequenceNumber(),
                        node.getNodeStart(), childNode.getNodeStart());
                if (node.isOnDisk()) {
                    childNode = getNode(node.getLatestChild());
                    assertEquals("Equals end time of parent " + node.getSequenceNumber() + " and last child " + childNode.getSequenceNumber(),
                            node.getNodeEnd(), childNode.getNodeEnd());
                }
            }

            /*
             * Test that the childStartTimes[] array matches the real nodes'
             * start times
             *
             * Also test that children range is within the parent's range
             */
            for (int i = 0; i < node.getNbChildren(); i++) {
                HTNode childNode = getNode(node.getChild(i));
                assertEquals("Start time in parent " + node.getSequenceNumber() + " of child at index " + i,
                        childNode.getNodeStart(), node.getChildStart(i));
                assertTrue("Child at index " + i + " of parent " + node.getSequenceNumber() + " has correct start time",
                        node.getNodeStart() <= childNode.getNodeStart());
                if (node.isOnDisk() && childNode.isOnDisk()) {
                    assertTrue("Child at index " + i + " of parent " + node.getSequenceNumber() + " has correct end time",
                            childNode.getNodeEnd() <= node.getNodeEnd());
                    assertEquals("End time in parent " + node.getSequenceNumber() + " of child at index " + i,
                            childNode.getNodeEnd(), node.getChildEnd(i));
                }
                testIntersectingChildren(node, childNode);
            }

        } catch (ClosedChannelException e) {
            fail(e.getMessage());
        }
    }

    private static void testIntersectingChildren(ParentNode parent, HTNode child) {
        int childSequence = child.getSequenceNumber();
        boolean shouldBeInCollection;
        Collection<Integer> nextChildren;
        for (long t = parent.getNodeStart(); t < parent.getNodeEnd(); t++) {
            shouldBeInCollection = (t >= child.getNodeStart() && t <= child.getNodeEnd());
            nextChildren = parent.selectNextChildren(t);
            assertEquals(shouldBeInCollection, nextChildren.contains(childSequence));
        }
    }

}
