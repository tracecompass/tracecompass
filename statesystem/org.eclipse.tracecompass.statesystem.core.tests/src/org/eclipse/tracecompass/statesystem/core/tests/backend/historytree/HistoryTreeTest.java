/*******************************************************************************
 * Copyright (c) 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests.backend.historytree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTConfig;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTInterval;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.HTNode;
import org.eclipse.tracecompass.internal.statesystem.core.backend.historytree.IHistoryTree;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.statesystem.core.tests.stubs.backend.HistoryTreeClassicStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests the history tree
 *
 * @author Geneviève Bastien
 */
public class HistoryTreeTest {


    /* Minimal allowed blocksize */
    private static final int BLOCK_SIZE = HistoryTreeClassicStub.MINIMUM_BLOCK_SIZE;

    private static final HTInterval NULL_INTERVAL = new HTInterval(10, 20, 1, TmfStateValue.nullValue());

    /* String with 23 characters, interval in file will be 25 bytes long */
    private static final String TEST_STRING = "abcdefghifklmnopqrstuvw";
    private static final TmfStateValue STRING_VALUE = TmfStateValue.newValueString(TEST_STRING);
    private static final HTInterval STRING_INTERVAL = new HTInterval(10, 20, 1, STRING_VALUE);

    private static final TmfStateValue LONG_VALUE = TmfStateValue.newValueLong(10L);
    private static final HTInterval LONG_INTERVAL = new HTInterval(10, 20, 1, LONG_VALUE);

    private static final TmfStateValue INT_VALUE = TmfStateValue.newValueInt(1);
    private static final HTInterval INT_INTERVAL = new HTInterval(10, 20, 1, INT_VALUE);

    private @Nullable File fTempFile;

    /**
     * Create the temporary file for this history tree
     */
    @Before
    public void setupTest() {
        try {
            fTempFile = File.createTempFile("tmpStateSystem", null);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Delete the temporary history tree file after the test
     */
    @After
    public void cleanup() {
        if (fTempFile != null) {
            fTempFile.delete();
        }
    }

    /**
     * Setup a history tree.
     *
     * @param maxChildren
     *            The max number of children per node in the tree (tree config
     *            option)
     */
    private HistoryTreeClassicStub setupSmallTree(int maxChildren) {
        HistoryTreeClassicStub ht = null;
        try {
            File newFile = fTempFile;
            assertNotNull(newFile);
            HTConfig config = new HTConfig(newFile,
                    BLOCK_SIZE,
                    maxChildren, /* Number of children */
                    1, /* Provider version */
                    1); /* Start time */
            ht = new HistoryTreeClassicStub(config);

        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertNotNull(ht);
        return ht;
    }

    /**
     * Setup a history tree with config MAX_CHILDREN = 3.
     */
    private HistoryTreeClassicStub setupSmallTree() {
        return setupSmallTree(3);
    }

    private static long fillValues(IHistoryTree ht, TmfStateValue value, int nbValues, long start) {
        for (int i = 0; i < nbValues; i++) {
            ht.insertInterval(new HTInterval(start + i, start + i + 1, 1, value));
        }
        return start + nbValues;
    }

    /**
     * Insert intervals in the tree to fill the current leaf node to capacity,
     * without exceeding it.
     *
     * This guarantees that the following insertion will create new nodes.
     *
     * @param ht
     *            The history tree in which to insert
     * @return Start time of the current leaf node. Future insertions should be
     *         greater than or equal to this to make sure the intervals go in
     *         the leaf node.
     */
    private static long fillNextLeafNode(HistoryTreeClassicStub ht, long leafNodeStart) {
        int prevCount = ht.getNodeCount();
        int prevDepth = ht.getDepth();

        /* Fill the following leaf node */
        HTNode node = ht.getLatestLeaf();
        int intervalSize = STRING_INTERVAL.getSizeOnDisk();
        int nodeFreeSpace = node.getNodeFreeSpace();
        int nbIntervals = nodeFreeSpace / intervalSize;
        long ret = fillValues(ht, STRING_VALUE, nbIntervals, leafNodeStart);

        /* Make sure we haven't changed the depth or node count */
        assertEquals(prevCount, ht.getNodeCount());
        assertEquals(prevDepth, ht.getDepth());

        return ret;
    }

    /**
     * Test that nodes are filled
     *
     * It fills nodes with sequential intervals from one attribute only, so that
     * leafs should be filled.
     */
    @Test
    public void testSequentialFill() {
        HistoryTreeClassicStub ht = setupSmallTree();

        HTNode node = ht.getLatestLeaf();
        assertEquals(0, node.getNodeUsagePercent());

        /* Add null intervals up to ~10% */
        int nodeFreeSpace = node.getNodeFreeSpace();
        int intervalSize = NULL_INTERVAL.getSizeOnDisk();
        int nbIntervals = nodeFreeSpace / 10 / intervalSize;
        long start = fillValues(ht, TmfStateValue.nullValue(), nbIntervals, 1);
        assertEquals(nodeFreeSpace - nbIntervals * intervalSize , node.getNodeFreeSpace());

        /* Add integer intervals up to ~20% */
        nodeFreeSpace = node.getNodeFreeSpace();
        intervalSize = INT_INTERVAL.getSizeOnDisk();
        nbIntervals = nodeFreeSpace / 10 / intervalSize;
        start = fillValues(ht, INT_VALUE, nbIntervals, start);
        assertEquals(nodeFreeSpace - nbIntervals * intervalSize , node.getNodeFreeSpace());

        /* Add long intervals up to ~30% */
        nodeFreeSpace = node.getNodeFreeSpace();
        intervalSize = LONG_INTERVAL.getSizeOnDisk();
        nbIntervals = nodeFreeSpace / 10 / intervalSize;
        start = fillValues(ht, LONG_VALUE, nbIntervals, start);
        assertEquals(nodeFreeSpace - nbIntervals * intervalSize , node.getNodeFreeSpace());

        /* Add string intervals up to ~40% */
        nodeFreeSpace = node.getNodeFreeSpace();
        intervalSize = STRING_INTERVAL.getSizeOnDisk();
        nbIntervals = nodeFreeSpace / 10 / intervalSize;
        start = fillValues(ht, STRING_VALUE, nbIntervals, start);
        assertEquals(nodeFreeSpace - nbIntervals * intervalSize , node.getNodeFreeSpace());

    }

    /**
     * Test the addition of new nodes to the tree and make sure the tree is
     * built with the right structure
     */
    @Test
    public void testDepth() {
        HistoryTreeClassicStub ht = setupSmallTree();

        /* Fill a first node */
        HTNode node = ht.getLatestLeaf();
        int nodeFreeSpace = node.getNodeFreeSpace();
        int nbIntervals = nodeFreeSpace / (STRING_INTERVAL.getSizeOnDisk());
        long start = fillValues(ht, STRING_VALUE, nbIntervals, 1);

        /* Add intervals that should add a sibling to the node */
        assertEquals(1, ht.getNodeCount());
        assertEquals(1, ht.getDepth());
        start = fillValues(ht, STRING_VALUE, 1, start);
        assertEquals(3, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        /* Fill the latest leaf node (2nd child) */
        node = ht.getLatestLeaf();
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / (STRING_INTERVAL.getSizeOnDisk());
        start = fillValues(ht, STRING_VALUE, nbIntervals, start);

        /*
         * Add an interval that should add another sibling to the previous nodes
         */
        start = fillValues(ht, STRING_VALUE, 1, start);
        assertEquals(4, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        /* Fill the latest leaf node (3rd and last child) */
        node = ht.getLatestLeaf();
        nodeFreeSpace = node.getNodeFreeSpace();
        nbIntervals = nodeFreeSpace / (STRING_INTERVAL.getSizeOnDisk());
        start = fillValues(ht, STRING_VALUE, nbIntervals, start);

        /* The new node created here should generate a new branch */
        start = fillValues(ht, STRING_VALUE, 1, start);
        assertEquals(7, ht.getNodeCount());
        assertEquals(3, ht.getDepth());
    }

    /**
     * Make sure the node sequence numbers and parent pointers are set correctly
     * when new nodes are created.
     *
     * <p>
     * We are building a tree whose node sequence numbers will look like this at
     * the end:
     * </p>
     *
     * <pre>
     *     3
     *    / \
     *   1   4
     *  / \   \
     * 0   2   5
     * </pre>
     *
     * <p>
     * However while building, the parent pointers may be different.
     * </p>
     *
     * @throws ClosedChannelException
     *             If the test fails
     */
    @Test
    public void testNodeSequenceNumbers() throws ClosedChannelException {
        /* Represents the start time of the current leaf node */
        long start = 1;

        HistoryTreeClassicStub ht = setupSmallTree(2);
        start = fillNextLeafNode(ht, start);

        List<HTNode> branch = ht.getLatestBranch();
        assertEquals(1, branch.size());
        assertEquals( 0, branch.get(0).getSequenceNumber());
        assertEquals(-1, branch.get(0).getParentSequenceNumber());

        /* Create a new branch */
        start = fillValues(ht, STRING_VALUE, 1, start);
        start = fillNextLeafNode(ht, start);
        assertEquals(3, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        /* Make sure the first node's parent was updated */
        HTNode node = ht.readNode(0);
        assertEquals(0, node.getSequenceNumber());
        assertEquals(1, node.getParentSequenceNumber());

        /* Make sure the new branch is alright */
        branch = ht.getLatestBranch();
        assertEquals(2, branch.size());
        assertEquals( 1, branch.get(0).getSequenceNumber());
        assertEquals(-1, branch.get(0).getParentSequenceNumber());
        assertEquals( 2, branch.get(1).getSequenceNumber());
        assertEquals( 1, branch.get(1).getParentSequenceNumber());

        /* Create a third branch */
        start = fillValues(ht, STRING_VALUE, 1, start);
        start = fillNextLeafNode(ht, start);
        assertEquals(6, ht.getNodeCount());
        assertEquals(3, ht.getDepth());

        /* Make sure all previous nodes are still correct */
        node = ht.readNode(0);
        assertEquals(0, node.getSequenceNumber());
        assertEquals(1, node.getParentSequenceNumber());
        node = ht.readNode(1);
        assertEquals(1, node.getSequenceNumber());
        assertEquals(3, node.getParentSequenceNumber());
        node = ht.readNode(2);
        assertEquals(2, node.getSequenceNumber());
        assertEquals(1, node.getParentSequenceNumber());

        /* Verify the contents of the new latest branch */
        branch = ht.getLatestBranch();
        assertEquals(3, branch.size());
        assertEquals( 3, branch.get(0).getSequenceNumber());
        assertEquals(-1, branch.get(0).getParentSequenceNumber());
        assertEquals( 4, branch.get(1).getSequenceNumber());
        assertEquals( 3, branch.get(1).getParentSequenceNumber());
        assertEquals( 5, branch.get(2).getSequenceNumber());
        assertEquals( 4, branch.get(2).getParentSequenceNumber());
    }
}
