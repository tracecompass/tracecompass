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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.datastore.core.interval.IHTInterval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * This is the base class for all history tree implementation tests. Specific
 * tree's tests can extend this class to have some basic tests.
 *
 * It tests the {@link AbstractHistoryTree} class. This test class will fill
 * nodes only with sequential objects, so that each extending test for trees
 * will have to add the tests and filling methods that correspond to their own
 * use cases.
 *
 * @author Geneviève Bastien
 * @param <E>
 *            The type of objects that will be saved in the tree
 * @param <N>
 *            The base type of the nodes of this tree
 */
public abstract class AbstractHistoryTreeTestBase<E extends IHTInterval, N extends HTNode<E>> {

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
     * @throws IOException failed to delete file
     */
    @After
    public void cleanup() throws IOException {
        if (fTempFile != null) {
            Files.delete(fTempFile.toPath());
        }
    }

    /**
     * Setup a history tree.
     *
     * @param maxChildren
     *            The max number of children per node in the tree (tree config
     *            option)
     * @param treeStart
     *            The start of the tree
     * @return The new history tree
     */
    protected AbstractHistoryTree<E, N> setupSmallTree(int maxChildren, long treeStart) {
        AbstractHistoryTree<E, N> ht = null;
        try {
            File newFile = fTempFile;
            assertNotNull(newFile);

            ht = createHistoryTree(newFile,
                    HtTestUtils.BLOCKSIZE,
                    maxChildren, /* Number of children */
                    1, /* Provider version */
                    1); /* Start time */

        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertNotNull(ht);
        return Objects.requireNonNull(ht);
    }

    /**
     * Create the history tree to test
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
     * @return The history tree stub
     * @throws IOException
     *             Any exception thrown by the history tree
     */
    protected abstract AbstractHistoryTree<E, N> createHistoryTree(File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart) throws IOException;

    /**
     * "Reader" constructor : instantiate a history tree from an existing tree
     * file on disk
     *
     * @param existingStateFile
     *            Path/filename of the history-file we are to open
     * @param expProviderVersion
     *            The expected version of the state provider
     * @return The history tree stub
     * @throws IOException
     *             If an error happens reading the file
     */
    protected abstract AbstractHistoryTree<E, N> createHistoryTree(
            File existingStateFile,
            int expProviderVersion) throws IOException;

    /**
     * Create an interval that fits in the tree with the given start/end time
     *
     * @param start
     *            The start time
     * @param end
     *            The end time
     * @return The object
     */
    protected abstract E createInterval(long start, long end);

    /**
     * Create a reader history tree
     *
     * @return The history tree stub
     * @throws IOException
     *             If an error happened reading the file
     */
    protected final AbstractHistoryTree<E, N> createHistoryTreeReader() throws IOException {
        File tempFile = fTempFile;
        assertNotNull(tempFile);
        return createHistoryTree(tempFile, 1);
    }

    /**
     * Setup a history tree with config MAX_CHILDREN = 3 and start time of 1
     *
     * @return A new history tree
     */
    protected AbstractHistoryTree<E, N> setupSmallTree() {
        return setupSmallTree(3, 1);
    }

    /**
     * Add sequential elements to the history up to a certain size. Any element
     * that would go above the fixed limit should not be added
     *
     * @param ht
     *            The tree to which to add values
     * @param fillSize
     *            The limit on the size of the elements to add
     * @param start
     *            The start time of the values
     * @return The latest end time
     */
    protected abstract long fillValues(AbstractHistoryTree<E, N> ht, int fillSize, long start);

    /**
     * Insert objects in the tree to fill the current leaf node to capacity,
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
    private long fillNextLeafNode(AbstractHistoryTree<E, N> ht, long leafNodeStart) {
        int prevCount = ht.getNodeCount();
        int prevDepth = ht.getDepth();

        /* Fill the following leaf node */
        N node = ht.getLatestLeaf();
        long ret = fillValues(ht, node.getNodeFreeSpace(), leafNodeStart);

        /* Make sure we haven't changed the depth or node count */
        assertEquals(prevCount, ht.getNodeCount());
        assertEquals(prevDepth, ht.getDepth());

        return ret;
    }

    /**
     * Test that nodes are filled
     *
     * It fills nodes with sequential elements, so that leafs should be filled.
     */
    @Test
    public void testSequentialFill() {
        AbstractHistoryTree<E, N> ht = setupSmallTree();

        // Make sure it is empty
        N node = ht.getLatestLeaf();
        assertEquals(0, node.getNodeUsagePercent());

        /* Fill ~10% of the node with elements */
        int initialFreeSpace = node.getNodeFreeSpace();
        int limit = node.getNodeFreeSpace() / 10;
        long start = fillValues(ht, limit, 1);
        assertTrue(node.getNodeFreeSpace() > initialFreeSpace - limit);
        assertTrue(node.getNodeFreeSpace() < initialFreeSpace);

        /* Add elements up to ~20% */
        start = fillValues(ht, limit, start);
        assertTrue(node.getNodeFreeSpace() > initialFreeSpace - 2 * limit);
        assertTrue(node.getNodeFreeSpace() < initialFreeSpace - limit);

        /* Add elements up to ~30% */
        start = fillValues(ht, limit, start);
        assertTrue(node.getNodeFreeSpace() > initialFreeSpace - 3 * limit);
        assertTrue(node.getNodeFreeSpace() < initialFreeSpace - 2 * limit);

        /* Add elements up to ~40% */
        fillValues(ht, limit, start);
        assertTrue(node.getNodeFreeSpace() > initialFreeSpace - 4 * limit);
        assertTrue(node.getNodeFreeSpace() < initialFreeSpace - 3 * limit);

        // Assert the integrity of the tree
        ht.closeTree(ht.getTreeEnd());
        HtTestUtils.assertTreeIntegrity(ht);

    }

    /**
     * Test the addition of new nodes to the tree and make sure the tree is
     * built with the right structure
     */
    @Test
    public void testDepth() {
        AbstractHistoryTree<E, N> ht = setupSmallTree();

        /* Fill a first node */
        N node = ht.getLatestLeaf();
        long start = 1;
        long time = fillValues(ht, node.getNodeFreeSpace(), start);

        /*
         * Add intervals that should add a sibling to the node and a new root
         * node
         */
        assertEquals(1, ht.getNodeCount());
        assertEquals(1, ht.getDepth());
        ht.insert(createInterval(time, time + 1));
        time += 1;
        assertEquals(3, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        /* Fill the latest leaf node (2nd child) */
        node = ht.getLatestLeaf();
        time = fillValues(ht, node.getNodeFreeSpace(), time);

        /*
         * Add an interval that should add another sibling to the previous nodes
         */
        ht.insert(createInterval(time, time + 1));
        time += 1;
        assertEquals(4, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        /* Fill the latest leaf node (3rd and last child) */
        node = ht.getLatestLeaf();
        time = fillValues(ht, node.getNodeFreeSpace(), time);

        /* The new node created here should generate a new branch */
        ht.insert(createInterval(time, time + 1));
        time += 1;
        assertEquals(7, ht.getNodeCount());
        assertEquals(3, ht.getDepth());

        /*
         * Completely fill the second level, such that there will be a 4th level
         * added
         */
        while (ht.getDepth() < 4) {
            time = fillNextLeafNode(ht, time);
            ht.insert(createInterval(time, time + 1));
        }
        assertEquals(4, ht.getDepth());

        // Assert the integrity of the tree
        ht.closeTree(ht.getTreeEnd());
        HtTestUtils.assertTreeIntegrity(ht);
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
     *             If the file channel is closed
     */
    @Test
    public void testNodeSequenceNumbers() throws ClosedChannelException {

        long time = 1;

        AbstractHistoryTree<E, N> ht = setupSmallTree(2, time);
        time = fillNextLeafNode(ht, time);

        /* There is only one node in the tree at this point, with no parent */
        List<N> branch = ht.getLatestBranch();
        assertEquals(1, branch.size());
        assertEquals(0, branch.get(0).getSequenceNumber());
        assertEquals(-1, branch.get(0).getParentSequenceNumber());

        /* Create a new branch */
        ht.insert(createInterval(time, time + 1));
        time = fillNextLeafNode(ht, time + 1);
        assertEquals(3, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        /* Make sure the first node's parent was updated */
        N node = ht.getNode(0);
        assertEquals(0, node.getSequenceNumber());
        assertEquals(1, node.getParentSequenceNumber());

        /* Make sure the new branch is all right */
        branch = ht.getLatestBranch();
        assertEquals(2, branch.size());
        assertEquals(1, branch.get(0).getSequenceNumber());
        assertEquals(-1, branch.get(0).getParentSequenceNumber());
        assertEquals(2, branch.get(1).getSequenceNumber());
        assertEquals(1, branch.get(1).getParentSequenceNumber());

        /* Create a third branch */
        ht.insert(createInterval(time, time + 1));
        fillNextLeafNode(ht, time + 1);
        assertEquals(6, ht.getNodeCount());
        assertEquals(3, ht.getDepth());

        /* Make sure all previous nodes are still correct */
        node = ht.getNode(0);
        assertEquals(0, node.getSequenceNumber());
        assertEquals(1, node.getParentSequenceNumber());
        node = ht.getNode(1);
        assertEquals(1, node.getSequenceNumber());
        assertEquals(3, node.getParentSequenceNumber());
        node = ht.getNode(2);
        assertEquals(2, node.getSequenceNumber());
        assertEquals(1, node.getParentSequenceNumber());

        /* Verify the contents of the new latest branch */
        branch = ht.getLatestBranch();
        assertEquals(3, branch.size());
        assertEquals(3, branch.get(0).getSequenceNumber());
        assertEquals(-1, branch.get(0).getParentSequenceNumber());
        assertEquals(4, branch.get(1).getSequenceNumber());
        assertEquals(3, branch.get(1).getParentSequenceNumber());
        assertEquals(5, branch.get(2).getSequenceNumber());
        assertEquals(4, branch.get(2).getParentSequenceNumber());

        // Assert the integrity of the tree
        ht.closeTree(ht.getTreeEnd());
        HtTestUtils.assertTreeIntegrity(ht);
    }

    /**
     * Test reading a tree and make sure it is identical to the original one
     *
     * <p>
     * We are building a tree whose node sequence numbers will look like this at
     * the end:
     * </p>
     *
     * <pre>
     *       4
     *     /   \
     *    1     5
     *  / | \    \
     * 0  2  3    6
     * </pre>
     *
     * @throws IOException
     *             Exceptions with the HT file
     *
     */
    @Test
    public void testReadTree() throws IOException {

        long time = 1;

        // Build the tree for the test
        AbstractHistoryTree<E, N> ht = setupSmallTree();
        time = fillNextLeafNode(ht, time);

        /* Create a new branch */
        ht.insert(createInterval(time, time + 1));
        time = fillNextLeafNode(ht, time + 1);

        /* Fill the third child */
        ht.insert(createInterval(time, time + 1));
        time = fillNextLeafNode(ht, time + 1);

        /* Make sure the tree has the expected structure at this point */
        assertEquals(4, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        /* Add the third branch */
        ht.insert(createInterval(time, time + 1));
        time = fillNextLeafNode(ht, time + 1);

        /* Make sure the tree has the expected structure */
        assertEquals(7, ht.getNodeCount());
        assertEquals(3, ht.getDepth());

        // Close the tree and save the nodes for later
        ht.closeTree(time + 1);

        List<N> expectedNodes = new ArrayList<>(ht.getNodeCount());
        for (int i = 0; i < ht.getNodeCount(); i++) {
            expectedNodes.add(ht.getNode(i));
        }
        ht.closeFile();

        // Create a reader history tree
        ht = createHistoryTreeReader();

        // Make sure the number of nodes and depth is as expected
        assertEquals(7, ht.getNodeCount());
        assertEquals(3, ht.getDepth());

        for (int i = 0; i < ht.getNodeCount(); i++) {
            assertEquals(expectedNodes.get(i), ht.readNode(i));
        }

        // Assert the integrity of the read tree
        HtTestUtils.assertTreeIntegrity(ht);
    }

    /**
     * Test the tree end time
     */
    @Test
    public void testTreeEnd() {
        long time = 1;

        // Check the end time at the start
        AbstractHistoryTree<E, N> ht = setupSmallTree();
        assertEquals(time, ht.getTreeEnd());

        // Fill a node and check the end
        time = fillNextLeafNode(ht, time);
        assertEquals(time, ht.getTreeEnd());

        // Add an object that should not change the end time
        E object = createInterval(time - 10, time - 5);
        ht.insert(object);
        assertEquals(time, ht.getTreeEnd());

        // Assert the tree integrity
        ht.closeTree(ht.getTreeEnd());
        HtTestUtils.assertTreeIntegrity(ht);
    }

}
