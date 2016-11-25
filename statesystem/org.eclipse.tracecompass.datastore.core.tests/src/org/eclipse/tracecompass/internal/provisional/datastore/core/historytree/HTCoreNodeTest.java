/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree.IHTNodeFactory;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HistoryTreeStub;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.IHTNode.NodeType;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.classic.ClassicHistoryTreeStub;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping.OverlappingHistoryTreeStub;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.IHTIntervalReader;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Basic test class for core nodes. These tests will test nodes with children.
 * We are testing only the current node, so it does not matter if children are
 * core or leaf nodes (here they would be core). Specific implementations of
 * core nodes can extend this method to add specific tests for their use cases.
 *
 * @author Geneviève Bastien
 * @param <E>
 *            The type of element to add in the nodes
 * @param <N>
 *            The type of node to test
 */
@RunWith(Parameterized.class)
public class HTCoreNodeTest<E extends IHTInterval, N extends HTNode<E>> extends HTNodeTest<E, N> {

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "Core node",
                        HTNode.COMMON_HEADER_SIZE + Integer.BYTES + Integer.BYTES * NB_CHILDREN,
                        HistoryTreeStub.NODE_FACTORY,
                        HtTestUtils.READ_FACTORY,
                        HTNodeTest.BASE_OBJ_FACTORY
                },
                { "Classic core node",
                        HTNode.COMMON_HEADER_SIZE + Integer.BYTES + Integer.BYTES * NB_CHILDREN + Long.BYTES * NB_CHILDREN,
                        ClassicHistoryTreeStub.CLASSIC_NODE_FACTORY,
                        HtTestUtils.READ_FACTORY,
                        HTNodeTest.BASE_OBJ_FACTORY
                },
                { "Overlapping core node",
                        HTNode.COMMON_HEADER_SIZE + Integer.BYTES + Integer.BYTES * NB_CHILDREN + 2 * Long.BYTES * NB_CHILDREN,
                        OverlappingHistoryTreeStub.OVERLAPPING_NODE_FACTORY,
                        HtTestUtils.READ_FACTORY,
                        HTNodeTest.BASE_OBJ_FACTORY
                },
        });
    }

    /**
     * Constructor
     *
     * @param name
     *            The name of the test
     * @param headerSize
     *            The size of the header for this node type
     * @param factory
     *            The node factory to use
     * @param readFactory
     *            The factory to read element data from disk
     * @param objFactory
     *            The factory to create objects for this tree
     * @throws IOException
     *             Any exception occurring with the file
     */
    public HTCoreNodeTest(String name,
            int headerSize,
            IHTNodeFactory<E, N> factory,
            IHTIntervalReader<E> readFactory,
            ObjectFactory<E> objFactory) throws IOException {

        super(name, headerSize, NodeType.CORE, factory, readFactory, objFactory);
    }

    /**
     * Test getting existing children
     */
    @Test
    public void testGetChild2() {
        long start = 10L;
        HTNode<E> node = newNode(0, -1, 10L);
        assertEquals(0, node.getNbChildren());

        // Add the child node to the parent and make sure it can be retrieved
        HTNode<E> childNode = newNode(1, 0, start);
        node.linkNewChild(childNode);
        assertEquals(1, node.getNbChildren());
        assertEquals(childNode.getSequenceNumber(), node.getChild(0));

        // Add a second child and retrieve both children
        HTNode<E> childNode2 = newNode(2, 0, start);
        node.linkNewChild(childNode2);
        assertEquals(2, node.getNbChildren());
        assertEquals(childNode.getSequenceNumber(), node.getChild(0));
        assertEquals(childNode2.getSequenceNumber(), node.getChild(1));

        // Add a third child with a non-sequential number and retrieve all
        // children
        HTNode<E> childNode3 = newNode(10, 0, start);
        node.linkNewChild(childNode3);
        assertEquals(3, node.getNbChildren());
        assertEquals(childNode.getSequenceNumber(), node.getChild(0));
        assertEquals(childNode2.getSequenceNumber(), node.getChild(1));
        assertEquals(childNode3.getSequenceNumber(), node.getChild(2));
    }

    /**
     * Test getting the latest child from a childless node
     */
    @Test(expected = IndexOutOfBoundsException.class)
    @Override
    public void testGetLatestChild() {
        super.testGetLatestChild();
    }

    /**
     * Test the {@link HTNode#getLatestChild()} method with children
     */
    @Test
    public void testGetLatestChild2() {
        long start = 10L;
        HTNode<E> node = newNode(0, -1, 10L);
        assertEquals(0, node.getNbChildren());

        // Add the child node to the parent and make sure it can be retrieved
        HTNode<E> childNode = newNode(1, 0, start);
        node.linkNewChild(childNode);
        assertEquals(1, node.getNbChildren());
        assertEquals(childNode.getSequenceNumber(), node.getLatestChild());

        // Add a second child that becomes the latest child
        HTNode<E> childNode2 = newNode(2, 0, start);
        node.linkNewChild(childNode2);
        assertEquals(2, node.getNbChildren());
        assertEquals(childNode2.getSequenceNumber(), node.getLatestChild());

        // Add a third child that becomes the latest child
        HTNode<E> childNode3 = newNode(10, 0, start);
        node.linkNewChild(childNode3);
        assertEquals(3, node.getNbChildren());
        assertEquals(childNode3.getSequenceNumber(), node.getLatestChild());
    }

    @Test
    @Override
    public void testLinkNewChild() throws IOException {

        E object = createObject(10L, 11L);
        long time = 10L;

        // Create and fill 2 nodes
        int nbObjects = (HtTestUtils.BLOCKSIZE - getHeaderSize()) / object.getSizeOnDisk();
        HTNode<E> node = newNode(0, -1, time);
        fillNode(node, nbObjects, time);
        HTNode<E> childNode = newNode(1, 0, time);
        fillNode(childNode, nbObjects, time);

        // Add the child node to the parent and make sure it was added
        node.linkNewChild(childNode);
        assertEquals(0, childNode.getNbChildren());
        assertEquals(1, node.getNbChildren());

        // Close the first child, then fill and add a second one
        time = time + nbObjects + 1;
        childNode.closeThisNode(time);

        HTNode<E> childNode2 = newNode(2, 0, time);
        fillNode(childNode2, nbObjects, time);
        node.linkNewChild(childNode2);
        assertEquals(0, childNode2.getNbChildren());
        assertEquals(2, node.getNbChildren());

        // Close the second child, then fill and add a third one
        time = time + nbObjects + 1;
        childNode2.closeThisNode(time);

        HTNode<E> childNode3 = newNode(3, 0, time);
        fillNode(childNode3, nbObjects, time);
        node.linkNewChild(childNode3);
        assertEquals(0, childNode3.getNbChildren());
        assertEquals(3, node.getNbChildren());

        time = time + nbObjects + 1;

        // Try to add a 4th child, there should be no place for it
        HTNode<E> noPlace = newNode(4, 0, time);
        Exception exception = null;
        try {
            node.linkNewChild(noPlace);
        } catch (IllegalStateException e) {
            exception = e;
        }
        assertNotNull(exception);

        // Close the last child and first node
        childNode3.closeThisNode(time);
        node.closeThisNode(time);

        // Write them all to disk
        write(node);
        write(childNode);
        write(childNode2);
        write(childNode3);
        assertTrue(node.isOnDisk());
        assertTrue(childNode.isOnDisk());
        assertTrue(childNode2.isOnDisk());
        assertTrue(childNode3.isOnDisk());

        // Read the node and make sure its data is equal to that of the original
        // node
        HTNode<E> readNode = read(0);
        HTNode<E> readChildNode = read(1);
        HTNode<E> readChildNode2 = read(2);
        HTNode<E> readChildNode3 = read(3);
        assertTrue(readNode.isOnDisk());
        assertEquals(node, readNode);

        assertTrue(readChildNode.isOnDisk());
        assertEquals(childNode, readChildNode);

        assertTrue(readChildNode2.isOnDisk());
        assertEquals(childNode2, readChildNode2);

        assertTrue(readChildNode3.isOnDisk());
        assertEquals(childNode3, readChildNode3);
    }

}
