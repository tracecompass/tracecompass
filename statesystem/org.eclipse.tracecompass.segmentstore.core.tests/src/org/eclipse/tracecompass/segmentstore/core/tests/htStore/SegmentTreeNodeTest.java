/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.htStore;

import java.io.IOException;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree.IHTNodeFactory;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTNode;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HTNodeTest;
import org.eclipse.tracecompass.internal.provisional.segmentstore.core.BasicSegment2;
import org.eclipse.tracecompass.segmentstore.core.tests.historytree.SegmentTreeNodeStub;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests the segment history tree leaf node. It extends the unit tests of the
 * node from the datastore.
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
@RunWith(Parameterized.class)
public class SegmentTreeNodeTest extends HTNodeTest<BasicSegment2, SegmentTreeNodeStub> {

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
    public SegmentTreeNodeTest(String name,
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
                { "Segment tree node",
                        HTNode.COMMON_HEADER_SIZE,
                        SegmentTreeNodeStub.NODE_FACTORY,
                        BasicSegment2.BASIC_SEGMENT_READ_FACTORY,
                        BASE_SEGMENT_FACTORY
                },
        });
    }

}
