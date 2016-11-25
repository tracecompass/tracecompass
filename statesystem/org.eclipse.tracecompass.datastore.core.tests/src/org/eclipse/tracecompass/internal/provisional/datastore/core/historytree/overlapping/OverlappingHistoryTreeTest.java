/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTree;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTreeTestBase;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HtTestUtils;
import org.eclipse.tracecompass.internal.provisional.datastore.core.interval.HTInterval;
import org.junit.Test;

/**
 * Tests for the concrete {@link OverlappingHistoryTree} class.
 *
 * @author Geneviève Bastien
 */
public class OverlappingHistoryTreeTest
        extends AbstractHistoryTreeTestBase<HTInterval, OverlappingNode<HTInterval>> {

    private static final HTInterval DEFAULT_OBJECT = new HTInterval(0, 0);

    @Override
    protected OverlappingHistoryTreeStub createHistoryTree(File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart) throws IOException {

        return new OverlappingHistoryTreeStub(stateHistoryFile,
                blockSize,
                maxChildren,
                providerVersion,
                treeStart);
    }

    @Override
    protected OverlappingHistoryTreeStub createHistoryTree(@NonNull File existingStateFile,
            int expectedProviderVersion) throws IOException {
        return new OverlappingHistoryTreeStub(existingStateFile, expectedProviderVersion);
    }

    @Override
    protected HTInterval createInterval(long start, long end) {
        return new HTInterval(start, end);
    }

    @Override
    protected long fillValues(
            AbstractHistoryTree<HTInterval, OverlappingNode<HTInterval>> ht,
            int fillSize, long start) {

        int nbValues = fillSize / DEFAULT_OBJECT.getSizeOnDisk();
        for (int i = 0; i < nbValues; i++) {
            ht.insert(new HTInterval(start + i, start + i + 1));
        }
        return start + nbValues;
    }

    /**
     * Test insertions at different moments
     */
    @Test
    public void testInsertions() {
        long start = 10L;
        final OverlappingHistoryTreeStub ht = (OverlappingHistoryTreeStub) setupSmallTree(3, start);

        /* Fill a first node */
        OverlappingNode<HTInterval> node = ht.getLatestLeaf();
        long time = fillValues(ht, node.getNodeFreeSpace(), start);

        /*
         * Add an element that starts before the last end time and make sure it
         * is added to the next node
         */
        long lastStart = time - 10;
        assertEquals(1, ht.getNodeCount());
        assertEquals(1, ht.getDepth());
        ht.insert(createInterval(lastStart, time + 1));

        assertEquals(3, ht.getNodeCount());
        assertEquals(2, ht.getDepth());
        assertEquals(1, ht.getLastInsertionLocation());

        /*
         * Add an element that starts before the node start and make sure it is
         * added in the parent
         */
        ht.insert(createInterval(lastStart - 5, time + 1));
        assertEquals(0, ht.getLastInsertionLocation());

        /* Fill the latest leaf node (2nd child) */
        node = ht.getLatestLeaf();
        time += 1;
        time = fillValues(ht, node.getNodeFreeSpace(), time);

        /*
         * Add an interval that should add another sibling to the previous
         * nodes, but that starts before last node. The sibling should not start
         * before previous one, so the element will be added at the top
         */
        ht.insert(createInterval(lastStart - 5, time + 1));
        assertEquals(4, ht.getNodeCount());
        assertEquals(2, ht.getDepth());
        assertEquals(0, ht.getLastInsertionLocation());

        OverlappingNode<HTInterval> newNode = ht.getLatestLeaf();
        assertTrue(node.getSequenceNumber() != newNode.getSequenceNumber());
        assertEquals(lastStart, newNode.getNodeStart());

        // Fill the last node with values in the past and make sure there are no
        // new nodes
        node = ht.getLatestLeaf();
        time = fillValues(ht, node.getNodeFreeSpace(), node.getNodeStart());
        assertEquals(4, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        // Add an interval that will create a new branch
        ht.insert(createInterval(time, time + 1));
        assertEquals(7, ht.getNodeCount());
        assertEquals(3, ht.getDepth());
        assertEquals(2, ht.getLastInsertionLocation());

        // Fill the last leaf node
        node = ht.getLatestLeaf();
        time = fillValues(ht, node.getNodeFreeSpace(), time);

        // Add an element that starts at the beginning and make sure it is
        // inserted at the top level.
        ht.insert(createInterval(start + 1, time));
        assertEquals(0, ht.getLastInsertionLocation());

        // An element with the correct start/end should create a sibling now
        ht.insert(createInterval(time, time + 1));
        assertEquals(8, ht.getNodeCount());

        // Close the tree and assert integrity
        ht.closeTree(ht.getTreeEnd());

        HtTestUtils.assertTreeIntegrity(ht);
    }

}
