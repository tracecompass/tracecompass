/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.htStore;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree.IHTNodeFactory;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTCoreNodeTest;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTNode;
import org.eclipse.tracecompass.internal.provisional.segmentstore.core.BasicSegment2;
import org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree.SegmentTreeNode;
import org.eclipse.tracecompass.segmentstore.core.tests.historytree.SegmentTreeNodeStub;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import com.google.common.collect.ImmutableSet;

/**
 * Tests the segment history tree core node. It extends the unit tests of the
 * node from the datastore.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
@RunWith(Parameterized.class)
public class SegmentTreeCoreNodeTest extends HTCoreNodeTest<BasicSegment2, SegmentTreeNodeStub> {

    /**
     * A factory to create base objects for test
     */
    private static final ObjectFactory<BasicSegment2> BASE_SEGMENT_FACTORY = (s, e) -> new BasicSegment2(s, e);

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
    public SegmentTreeCoreNodeTest(String name,
            int headerSize,
            IHTNodeFactory<BasicSegment2, SegmentTreeNodeStub> factory,
            IHTIntervalReader<BasicSegment2> objReader,
            ObjectFactory<BasicSegment2> objFactory) throws IOException {
        super(name, headerSize, factory, objReader, objFactory);
    }

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "Segment tree core node",
                    HTNode.COMMON_HEADER_SIZE + Integer.BYTES + Integer.BYTES * NB_CHILDREN + 6 * Long.BYTES * NB_CHILDREN,
                    SegmentTreeNodeStub.NODE_FACTORY,
                    BasicSegment2.BASIC_SEGMENT_READ_FACTORY,
                    BASE_SEGMENT_FACTORY },
        });
    }

    /**
     * Test the specific methods of this node type
     */
    @Test
    public void testSpecifics() {
        long start = 10L;
        int shortLen = 10;
        int longLen = 50;
        SegmentTreeNodeStub stub = newNode(0, -1, start);

        // Verify the default values
        assertEquals(start, stub.getMaxStart());
        assertEquals(Long.MAX_VALUE, stub.getMinEnd());
        assertEquals(Long.MAX_VALUE, stub.getShortest());
        assertEquals(0, stub.getLongest());

        // Add a new element and verify the data
        BasicSegment2 segment = new BasicSegment2(start, start + shortLen);
        stub.add(segment);
        assertEquals(start, stub.getMaxStart());
        assertEquals(start + shortLen, stub.getMinEnd());
        assertEquals(shortLen, stub.getShortest());
        assertEquals(shortLen, stub.getLongest());

        // Add a new element and verify the data: longest length and max start
        // should be updated
        segment = new BasicSegment2(start + shortLen, start + longLen);
        stub.add(segment);
        assertEquals(start + shortLen, stub.getMaxStart());
        assertEquals(start + shortLen, stub.getMinEnd());
        assertEquals(shortLen, stub.getShortest());
        assertEquals(longLen - shortLen, stub.getLongest());

    }

    /**
     * Test the specific methods to retrieve data from the children of this node
     */
    @Test
    public void testChildren() {
        long start = 10L;
        int shortLen = 10;
        int longLen = 50;

        // Create 2 nodes and link a child to the parent
        SegmentTreeNodeStub parentStub = newNode(0, -1, start);
        SegmentTreeNodeStub stub = newNode(0, -1, start);

        parentStub.linkNewChild(stub);

        // Verify the default values
        assertEquals(start, parentStub.getMaxStart(0));
        assertEquals(Long.MAX_VALUE, parentStub.getMinEnd(0));
        assertEquals(Long.MAX_VALUE, parentStub.getShortest(0));
        assertEquals(0, parentStub.getLongest(0));

        // Add a few segments to the child and verify its own data
        BasicSegment2 segment = new BasicSegment2(start, start + shortLen);
        stub.add(segment);
        segment = new BasicSegment2(start + shortLen, start + longLen);
        stub.add(segment);

        assertEquals(start + shortLen, stub.getMaxStart());
        assertEquals(start + shortLen, stub.getMinEnd());
        assertEquals(shortLen, stub.getShortest());
        assertEquals(longLen - shortLen, stub.getLongest());

        // Close the child node
        stub.closeThisNode(start + longLen);
        // It should update the parent's data on this node...
        assertEquals(start + shortLen, parentStub.getMaxStart(0));
        assertEquals(start + shortLen, parentStub.getMinEnd(0));
        assertEquals(shortLen, parentStub.getShortest(0));
        assertEquals(longLen - shortLen, parentStub.getLongest(0));

        // ... but not the parent's own data
        assertEquals(start, parentStub.getMaxStart());
        assertEquals(Long.MAX_VALUE, parentStub.getMinEnd());
        assertEquals(Long.MAX_VALUE, parentStub.getShortest());
        assertEquals(0, parentStub.getLongest());
    }

    /**
     * Test the {@link SegmentTreeNode#selectNextChildren(org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition)} method
     */
    @Test
    public void testIntersectingChildren() {
        long start = 10L;
        long step = 1000;

        // Create a parent node and link 3 children nodes that will overlap
        SegmentTreeNodeStub parentStub = newNode(0, -1, start);
        SegmentTreeNodeStub stub = newNode(1, 0, start);
        SegmentTreeNodeStub stub2 = newNode(2, 0, start + step);
        SegmentTreeNodeStub stub3 = newNode(3, 0, start + 2 * step);

        // Add the children to the parent
        parentStub.linkNewChild(stub);
        parentStub.linkNewChild(stub2);
        parentStub.linkNewChild(stub3);

        // Close child nodes
        stub.closeThisNode(start + 2 * step);
        stub2.closeThisNode(start + 3 * step);
        stub3.closeThisNode(start + 4 * step);

        // All 3 nodes should be returned for the whole time range
        Collection<Integer> children = parentStub.selectNextChildren(TimeRangeCondition.forContinuousRange(0L, Long.MAX_VALUE));
        assertArrayEquals(ImmutableSet.of(stub.getSequenceNumber(), stub2.getSequenceNumber(), stub3.getSequenceNumber()).toArray(), children.toArray());

        // All 3 nodes should be returned from start + step to start + 3*step
        children = parentStub.selectNextChildren(TimeRangeCondition.forContinuousRange(start + step, start + 3 * step));
        assertArrayEquals(ImmutableSet.of(stub.getSequenceNumber(), stub2.getSequenceNumber(), stub3.getSequenceNumber()).toArray(), children.toArray());

        // Test a range that is entirely within first node and intersects the
        // beginning of node 2
        children = parentStub.selectNextChildren(TimeRangeCondition.forContinuousRange(start + step - 2, start + 2 * step - 2));
        assertArrayEquals(ImmutableSet.of(stub.getSequenceNumber(), stub2.getSequenceNumber()).toArray(), children.toArray());

        // Test a range that is entirely third node and intersects the
        // end of node 2
        children = parentStub.selectNextChildren(TimeRangeCondition.forContinuousRange(start + 3 * step - 2, start + 4 * step - 2));
        assertArrayEquals(ImmutableSet.of(stub2.getSequenceNumber(), stub3.getSequenceNumber()).toArray(), children.toArray());

    }
}
