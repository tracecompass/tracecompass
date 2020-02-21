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

package org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.overlapping;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;

import org.eclipse.tracecompass.datastore.core.interval.IHTInterval;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.AbstractHistoryTreeTestBase;
import org.eclipse.tracecompass.internal.provisional.datastore.core.historytree.HtTestUtils;
import org.junit.Test;

/**
 * Test the overlapping history tree. This base class keeps the parameter such
 * that any tree implementing the overlapping history tree can extend this test
 * method
 *
 * @author Geneviève Bastien
 * @param <E>
 *            Type of interval
 * @param <N>
 *            Type of nodes in the tree
 */
public abstract class AbstractOverlappingHistoryTreeTestBase<E extends IHTInterval, N extends OverlappingNode<E>>
        extends AbstractHistoryTreeTestBase<E, N> {

    @Override
    protected abstract AbstractOverlappingHistoryTree<E, N> createHistoryTree(File stateHistoryFile,
            int blockSize,
            int maxChildren,
            int providerVersion,
            long treeStart) throws IOException;

    @Override
    protected abstract AbstractOverlappingHistoryTree<E, N> createHistoryTree(
            File existingStateFile,
            int expectedProviderVersion) throws IOException;

    /**
     * Test that the children start and end times are as expected
     */
    @Test
    public void testChildrenTimes() {
        AbstractOverlappingHistoryTree<E, N> ht = (AbstractOverlappingHistoryTree<E, N>) setupSmallTree();

        /* Fill a first node */
        OverlappingNode<E> node = ht.getLatestLeaf();
        long time = fillValues(ht, node.getNodeFreeSpace(), 1);

        /* Add elements that should add a sibling to the node */
        assertEquals(1, ht.getNodeCount());
        assertEquals(1, ht.getDepth());
        ht.insert(createInterval(time, time + 1));

        assertEquals(3, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        // The first node should have been closed, so let's check that the
        // parent has the right start and end end time
        OverlappingNode<E> parent = ht.getLatestNode(0);
        assertEquals(time, node.getNodeEnd());
        assertEquals(time, parent.getChildEnd(0));
        assertEquals(node.getNodeStart(), parent.getChildStart(0));

        /* Fill the latest leaf node (2nd child) */
        node = ht.getLatestLeaf();
        time += 1;
        time = fillValues(ht, node.getNodeFreeSpace(), time);

        /*
         * Add an element that should add another sibling to the previous nodes
         */
        ht.insert(createInterval(time, time + 1));
        assertEquals(4, ht.getNodeCount());
        assertEquals(2, ht.getDepth());

        // The second node should have been closed, so let's check that the
        // parent has the right start and end time
        assertEquals(time, node.getNodeEnd());
        assertEquals(time, parent.getChildEnd(1));
        assertEquals(node.getNodeStart(), parent.getChildStart(1));

        /* Fill the latest leaf node (3rd and last child) */
        node = ht.getLatestLeaf();
        time += 1;
        time = fillValues(ht, node.getNodeFreeSpace(), time);

        /* The new node created here should generate a new branch */
        ht.insert(createInterval(time, time + 1));
        assertEquals(7, ht.getNodeCount());
        assertEquals(3, ht.getDepth());

        // The third node and previous parent should have been closed, so let's
        // check that the parent has the right start and  end time
        assertEquals(time, node.getNodeEnd());
        assertEquals(time, parent.getChildEnd(2));
        assertEquals(node.getNodeStart(), parent.getChildStart(2));

        // Now verify the higher level
        node = parent;
        parent = ht.getLatestNode(0);
        assertEquals(time, node.getNodeEnd());
        assertEquals(time, parent.getChildEnd(0));

        // Assert the integrity of the tree
        ht.closeTree(ht.getTreeEnd());
        time = ht.getTreeEnd();

        /* The tree is closed, the last branch should have their end time set */
        // leaf level
        assertEquals(time, ht.getLatestNode(2).getNodeEnd());
        // Second level
        assertEquals(time, ht.getLatestNode(1).getNodeEnd());
        assertEquals(time, ht.getLatestNode(1).getChildEnd(0));
        // Head level
        assertEquals(time, parent.getNodeEnd());
        assertEquals(time, parent.getChildEnd(1));

        HtTestUtils.assertTreeIntegrity(ht);
    }

}
