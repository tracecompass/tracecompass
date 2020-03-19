/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.PrintStream;
import java.nio.channels.ClosedChannelException;

import org.eclipse.tracecompass.datastore.core.interval.HTInterval;
import org.eclipse.tracecompass.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHTNode.NodeType;

/**
 * Some utility classes and methods for the history tree tests
 *
 * @author Geneviève Bastien
 */
public class HtTestUtils {

    /**
     * Default block size for the tests
     */
    public static final int BLOCKSIZE = 4096;

    /**
     * The factory to read BaseHtObject nodes
     */
    public static final IHTIntervalReader<HTInterval> READ_FACTORY = HTInterval.INTERVAL_READER;

    private HtTestUtils() {
        // Do nothing, private constructor
    }

    /**
     * Helper method to check the integrity of a tree. For each node of the
     * tree, it makes sure that elements in nodes are within the node's range
     * and for core nodes, it makes sure that the children's data is OK.
     *
     * @param tree
     *            The history to check
     */
    public static final <E extends IHTInterval, N extends HTNode<E>>
    void assertTreeIntegrity(AbstractHistoryTree<E, N> tree) {

        try {
            for (int i = 0; i < tree.getNodeCount(); i++) {
                assertNodeIntegrity(tree, tree.getNode(i));
            }
        } catch (ClosedChannelException e) {
            fail(e.getMessage());
        }
    }

    private static <E extends IHTInterval, N extends HTNode<E>>
    void assertNodeIntegrity(AbstractHistoryTree<E, N> tree, N node) {

        if (node.getNodeType() == NodeType.CORE) {
            assertChildrenIntegrity(tree, node);
        }

        /* Check that all intervals are within the node's range */
        for (E object : node.getIntervals()) {
            assertTrue(String.format("Object start (%d) >= node start (%d)", object.getStart(), node.getNodeStart()), object.getStart() >= node.getNodeStart());
            assertTrue(String.format("Object start (%d) <= node end (%d)", object.getStart(), node.getNodeEnd()), object.getStart() <= node.getNodeEnd());
            assertTrue(String.format("Object end (%d) >= node start (%d)", object.getEnd(), node.getNodeStart()), object.getEnd() >= node.getNodeStart());
            assertTrue(String.format("Object end (%d) <= node end (%d)", object.getEnd(), node.getNodeEnd()), object.getEnd() <= node.getNodeEnd());
        }

    }

    private static <E extends IHTInterval, N extends HTNode<E>>
    void assertChildrenIntegrity(AbstractHistoryTree<E, N> tree, N node) {

        try {
            /*
             * Test that this node's start and end times match the start of the
             * first child and the end of the last child, respectively
             */
            if (node.getNbChildren() > 0) {
                N childNode = tree.getNode(node.getChild(0));
                assertEquals("Equals start time of parent " + node.getSequenceNumber() + " and first child " + childNode.getSequenceNumber(),
                        node.getNodeStart(), childNode.getNodeStart());
                if (node.isOnDisk()) {
                    childNode = tree.getNode(node.getLatestChild());
                    assertEquals("Equals end time of parent " + node.getSequenceNumber() + " and last child " + childNode.getSequenceNumber(),
                            node.getNodeEnd(), childNode.getNodeEnd());
                }
            }

            /*
             * Test that children range is within the parent's range
             */
            for (int i = 0; i < node.getNbChildren(); i++) {
                N childNode = tree.getNode(node.getChild(i));
                assertTrue("Child at index " + i + " of parent " + node.getSequenceNumber() + " has valid start time",
                        node.getNodeStart() <= childNode.getNodeStart());
                if (node.isOnDisk() && childNode.isOnDisk()) {
                    assertTrue("Child at index " + i + " of parent " + node.getSequenceNumber() + " has valid end time",
                            childNode.getNodeEnd() <= node.getNodeEnd());
                }
                assertTrue("Child at index " + i + " of parent " + node.getSequenceNumber() + " specific children", tree.verifyChildrenSpecific(node, i, childNode));
                assertTrue("Child at index " + i + " of parent " + node.getSequenceNumber() + " intersecting children", tree.verifyIntersectingChildren(node, childNode));
            }

        } catch (ClosedChannelException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Print out the full tree for debugging purposes
     *
     * @param writer
     *            PrintWriter in which to write the output
     * @param tree
     *            The history tree to print
     * @param printIntervals
     *            Flag to enable full output of the interval information
     * @param ts
     *            The timestamp that nodes have to intersect for intervals to be
     *            printed. A negative value will print intervals for all nodes.
     *            The timestamp only applies if printIntervals is true.
     */
    public static <E extends IHTInterval, N extends HTNode<E>> void debugPrintFullTree(
            PrintStream writer,
            AbstractHistoryTree<E, N> tree,
            boolean printIntervals,
            long ts) {

        preOrderPrint(writer, tree, false, tree.getLatestBranch().get(0), 0, ts);

        if (printIntervals) {
            preOrderPrint(writer, tree, true, tree.getLatestBranch().get(0), 0, ts);
        }
        writer.println('\n');
    }

    private static <E extends IHTInterval, N extends HTNode<E>> void preOrderPrint(
            PrintStream writer,
            AbstractHistoryTree<E, N> tree,
            boolean printIntervals,
            N node,
            int curDepth,
            long ts) {

        writer.println(node.toString());
        /*
         * Print intervals only if timestamp is negative or within the range of
         * the node
         */
        if (printIntervals &&
                (ts <= 0 ||
                        (ts >= node.getNodeStart() && ts <= node.getNodeEnd()))) {
            node.debugPrintIntervals(writer);
        }

        switch (node.getNodeType()) {
        case LEAF:
            /* Stop if it's the leaf node */
            return;

        case CORE:
            try {
                /* Print the child nodes */
                for (int i = 0; i < node.getNbChildren(); i++) {
                    N nextNode = tree.readNode(node.getChild(i));
                    for (int j = 0; j < curDepth; j++) {
                        writer.print("  ");
                    }
                    writer.print("+-");
                    preOrderPrint(writer, tree, printIntervals, nextNode, curDepth + 1, ts);
                }
            } catch (ClosedChannelException e) {
                throw new RuntimeException(e);
            }
            break;

        default:
            break;
        }
    }
}
