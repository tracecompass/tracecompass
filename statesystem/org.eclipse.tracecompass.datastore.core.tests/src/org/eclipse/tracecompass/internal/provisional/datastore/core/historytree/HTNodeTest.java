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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.eclipse.tracecompass.datastore.core.interval.HTInterval;
import org.eclipse.tracecompass.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.datastore.core.historytree.HtIo;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree.IHTNodeFactory;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHTNode.NodeType;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic.ClassicHistoryTreeStub;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping.OverlappingHistoryTreeStub;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Test the {@link HTNode} base class for nodes. This class has the different
 * node types as parameter. It tests specifically the leaf node methods.
 *
 * @author Geneviève Bastien
 * @param <E>
 *            The type of element to add in the nodes
 * @param <N>
 *            The type of node to test
 */
@RunWith(Parameterized.class)
public class HTNodeTest<E extends IHTInterval, N extends HTNode<E>> {

    /**
     * Factory to create new objects to insert in the nodes
     *
     * @param <T>
     *            The type of object to create
     */
    protected interface ObjectFactory<T extends IHTInterval> {
        /**
         * Create an object that fits in the tree with the given start/end time
         *
         * @param start
         *            The start time
         * @param end
         *            The end time
         * @return The object
         */
        T createObject(long start, long end);
    }

    /**
     * A factory to create base objects for test
     */
    protected static final ObjectFactory<HTInterval> BASE_OBJ_FACTORY = (s, e) -> new HTInterval(s, e);

    /** The nodes' block size */
    protected static final int BLOCKSIZE = HtTestUtils.BLOCKSIZE;
    /** The maximum number of children for the nodes */
    protected static final int NB_CHILDREN = 3;
    private static final long TREE_START = 10L;

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "Leaf node",
                        HTNode.COMMON_HEADER_SIZE,
                        HistoryTreeStub.NODE_FACTORY,
                        HtTestUtils.READ_FACTORY, BASE_OBJ_FACTORY
                },
                { "Classic leaf node",
                        HTNode.COMMON_HEADER_SIZE,
                        ClassicHistoryTreeStub.CLASSIC_NODE_FACTORY,
                        HtTestUtils.READ_FACTORY,
                        BASE_OBJ_FACTORY
                },
                { "Overlapping leaf node",
                        HTNode.COMMON_HEADER_SIZE,
                        OverlappingHistoryTreeStub.OVERLAPPING_NODE_FACTORY,
                        HtTestUtils.READ_FACTORY,
                        BASE_OBJ_FACTORY
                },
        });
    }

    private final HtIo<E, N> fHtIo;
    private final int fHeaderSize;
    private final NodeType fType;
    private final IHTIntervalReader<E> fHtObjectReader;
    private final IHTNodeFactory<E, N> fNodeFactory;
    private final ObjectFactory<E> fObjectFactory;

    /**
     * Constructor
     *
     * @param name
     *            The name of the test
     * @param headerSize
     *            The size of the header for this node type
     * @param factory
     *            The node factory to use
     * @param objReader
     *            The factory to read element data from disk
     * @param objFactory
     *            The factory to create objects for this tree
     * @throws IOException
     *             Any exception occurring with the file
     */
    public HTNodeTest(String name,
            int headerSize,
            IHTNodeFactory<E, N> factory,
            IHTIntervalReader<E> objReader,
            ObjectFactory<E> objFactory) throws IOException {
        this(name, headerSize, NodeType.LEAF, factory, objReader, objFactory);
    }

    /**
     * Constructor
     *
     * @param name
     *            The name of the test
     * @param headerSize
     *            The size of the header for this node
     * @param type
     *            The node type
     * @param nodeFactory
     *            The node factory to use
     * @param objReader
     *            The factory to read element data from disk
     * @param objFactory
     *            The factory to create objects for this tree
     * @throws IOException
     *             Any exception occurring with the file
     */
    protected HTNodeTest(String name,
            int headerSize,
            NodeType type,
            IHTNodeFactory<E, N> nodeFactory,
            IHTIntervalReader<E> objReader,
            ObjectFactory<E> objFactory) throws IOException {
        File file = File.createTempFile("tmp", null);
        assertNotNull(file);
        fHtObjectReader = objReader;
        fNodeFactory = nodeFactory;

        fHtIo = new HtIo<>(file,
                HtTestUtils.BLOCKSIZE,
                NB_CHILDREN,
                true,
                objReader,
                nodeFactory);

        fHeaderSize = headerSize;
        fType = type;
        fObjectFactory = objFactory;
    }

    /**
     * Get a new node
     *
     * @param seqNb
     *            The sequence number
     * @param parentNb
     *            The parent sequence number
     * @param nodeStart
     *            The node start
     * @return A new node, created with the factory sent in parameter in the
     *         constructor
     */
    public N newNode(int seqNb, int parentNb, long nodeStart) {
        return fNodeFactory.createNode(fType, BLOCKSIZE, NB_CHILDREN, seqNb, parentNb, nodeStart);
    }

    /**
     * Delete the file after test
     */
    @After
    public void cleanUp() {
        fHtIo.deleteFile();
    }

    /**
     * Fills a node with objects of length 1, going incrementally
     *
     * @param node
     *            The node to fill
     * @param nbObjects
     *            The number of objects to add
     * @param start
     *            The start time of the objects
     */
    protected void fillNode(HTNode<E> node, int nbObjects, long start) {
        for (int i = 0; i < nbObjects; i++) {
            node.add(fObjectFactory.createObject(i + start, i + start + 1));
        }
    }

    /**
     * Get the header size of this node
     *
     * @return The header size
     */
    protected int getHeaderSize() {
        return fHeaderSize;
    }

    /**
     * Create a new object for this type of node
     *
     * @param start
     *            The start of the object
     * @param end
     *            The end of the object
     * @return The new object
     */
    protected E createObject(long start, long end) {
        return fObjectFactory.createObject(start, end);
    }

    /**
     * Write a node to the file
     *
     * @param node
     *            Node to write to disk
     * @throws IOException
     *             Exceptions while writing to file
     */
    protected void write(HTNode<E> node) throws IOException {
        HtIo<E, N> htIo = fHtIo;

        // Close the node and write it to disk
        node.writeSelf(htIo.getFileWriter(node.getSequenceNumber()).getChannel());
    }

    /**
     * Reads a node from the history tree file
     *
     * @param seqNb
     *            The sequence number of the node to get
     * @return The read node
     * @throws IOException
     *             Exceptions while reading the node
     */
    protected HTNode<E> read(int seqNb) throws IOException {
        HtIo<E, N> htIo = fHtIo;

        return HTNode.readNode(BLOCKSIZE,
                NB_CHILDREN,
                htIo.supplyATReader(seqNb).getChannel(),
                fHtObjectReader,
                fNodeFactory);

    }

    /**
     * Test the leaf node methods without adding the node to disk
     */
    @Test
    public void testNodeData() {
        HTNode<E> node = newNode(0, -1, TREE_START);

        // Test the values at the beginning
        assertFalse(node.isOnDisk());
        assertEquals(TREE_START, node.getNodeStart());
        assertEquals(Long.MAX_VALUE, node.getNodeEnd());
        assertEquals(0, node.getSequenceNumber());
        assertEquals(-1, node.getParentSequenceNumber());
        assertEquals(fHeaderSize, node.getTotalHeaderSize());
        assertTrue(node.isEmpty());
        assertEquals(fType, node.getNodeType());
        assertEquals(HtTestUtils.BLOCKSIZE - fHeaderSize, node.getNodeFreeSpace());
        assertEquals(0, node.getNodeUsagePercent());

        // Add an element. It is possible to add an element outside the
        // boundaries of the node
        E object = fObjectFactory.createObject(0L, 10L);
        node.add(object);
        assertEquals(HtTestUtils.BLOCKSIZE - fHeaderSize - object.getSizeOnDisk(), node.getNodeFreeSpace());

        // Fill the node with objects
        int nbObjects = (HtTestUtils.BLOCKSIZE - fHeaderSize) / object.getSizeOnDisk();
        fillNode(node, nbObjects - 1, TREE_START);
        // Check the free space and sizes
        int expectedSize = HtTestUtils.BLOCKSIZE - fHeaderSize - object.getSizeOnDisk() * nbObjects;
        assertEquals(expectedSize, node.getNodeFreeSpace());
        int expectedNodeUsagePercent = expectedSize == 0 ? 100 : 99;
        assertEquals(expectedNodeUsagePercent, node.getNodeUsagePercent());
        assertEquals(nbObjects, node.getIntervals().size());

    }

    /**
     * Test adding an element to a full leaf node
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNodeInvalidAdd() {
        HTNode<E> node = newNode(0, -1, TREE_START);
        // Fill the node with objects
        E object = fObjectFactory.createObject(0L, 10L);
        int nbObjects = (HtTestUtils.BLOCKSIZE - fHeaderSize) / object.getSizeOnDisk();
        fillNode(node, nbObjects, TREE_START);

        // Add a new object
        node.add(object);
    }

    /**
     * Test closing a node at an invalid end time
     */
    @Test(expected = IllegalArgumentException.class)
    public void testNodeInvalidEnd() {
        HTNode<E> node = newNode(0, -1, TREE_START);
        // Fill the node with objects
        E object = fObjectFactory.createObject(0L, 10L);
        int nbObjects = (HtTestUtils.BLOCKSIZE - fHeaderSize) / object.getSizeOnDisk();
        fillNode(node, nbObjects, TREE_START);

        // Close the node at a wrong time
        node.closeThisNode(TREE_START);
    }

    /**
     * Test adding an element to a closed node
     */
    @Test
    public void testAddToCloseNode() {
        HTNode<E> node = newNode(0, -1, TREE_START);
        // Fill the node with objects
        E object = fObjectFactory.createObject(TREE_START, TREE_START + 10);
        int nbObjects = (HtTestUtils.BLOCKSIZE - fHeaderSize) / object.getSizeOnDisk();
        fillNode(node, nbObjects - 1, TREE_START);

        // Add a new object
        node.closeThisNode(TREE_START + nbObjects);
        // FIXME: shouldn't this fail?
        node.add(object);

    }

    /**
     * test closing, writing and reading a leaf node
     *
     * @throws IOException
     *             Exception while writing/reading the file
     */
    @Test
    public void testCloseNode() throws IOException {
        HTNode<E> node = newNode(0, -1, TREE_START);
        // Fill the node with objects
        E object = fObjectFactory.createObject(0L, 10L);
        int nbObjects = (HtTestUtils.BLOCKSIZE - fHeaderSize) / object.getSizeOnDisk();
        fillNode(node, nbObjects, TREE_START);
        assertEquals(nbObjects, node.getIntervals().size());

        // Close the node and write it to disk
        node.closeThisNode(TREE_START + nbObjects + 1);
        write(node);
        assertTrue(node.isOnDisk());

        // Read the node and make sure its data is equal to that of the original
        // node
        HTNode<E> readNode = read(0);
        assertTrue(readNode.isOnDisk());
        assertEquals(node, readNode);
    }

    /**
     * Test the {@link HTNode#getNbChildren()} method
     */
    @Test
    public void testNbChildren() {
        HTNode<E> node = newNode(0, -1, TREE_START);
        assertEquals(0, node.getNbChildren());
    }

    /**
     * Test the {@link HTNode#getChild(int)} method
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testGetChild() {
        HTNode<E> node = newNode(0, -1, TREE_START);
        node.getChild(0);
    }

    /**
     * Test the {@link HTNode#getLatestChild()} method
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetLatestChild() {
        HTNode<E> node = newNode(0, -1, TREE_START);
        node.getLatestChild();
    }

    /**
     * Test the {@link HTNode#linkNewChild(IHTNode)} method
     *
     * @throws IOException
     *             Exceptiosn thrown when reading/writing
     */
    @SuppressWarnings("unused")
    @Test(expected = UnsupportedOperationException.class)
    public void testLinkNewChild() throws IOException {
        HTNode<E> node = newNode(0, -1, TREE_START);
        HTNode<E> childNode = newNode(1, 0, TREE_START);
        node.linkNewChild(childNode);
    }

}
