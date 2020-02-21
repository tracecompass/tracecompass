/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.core.tests.trace.indexer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.eclipse.tracecompass.internal.tmf.core.trace.indexer.BTree;
import org.eclipse.tracecompass.internal.tmf.core.trace.indexer.BTreeCheckpointVisitor;
import org.eclipse.tracecompass.internal.tmf.core.trace.indexer.IBTreeVisitor;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;
import org.junit.Test;

/**
 * Tests for the BTree class
 *
 * @author Marc-Andre Laperle
 */
public class BTreeTest extends AbstractCheckpointCollectionTest {

    private final int DEGREE = 15;
    private BTree fBTree;

    @Override
    protected BTree createCollection() {
        fCheckpointCollection = fBTree = new BTree(DEGREE, getFile(), (ITmfPersistentlyIndexable) getTrace());
        return fBTree;
    }

    @Override
    public boolean isPersistableCollection() {
        return true;
    }

    /**
     * Tests that accepts find the correct checkpoint and ends with a perfect
     * match
     */
    @Test
    public void testAccept() {
        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(i), new TmfLongLocation(i), 0);
            fBTree.insert(checkpoint);
        }

        final TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(123), new TmfLongLocation(123L), 0);

        class TestVisitor implements IBTreeVisitor {
            public int fLastCompare = 0;
            ITmfCheckpoint fFoundCheckpoint;

            @Override
            public int compare(ITmfCheckpoint checkRec) {
                fLastCompare = checkRec.compareTo(checkpoint);
                if (fLastCompare == 0) {
                    fFoundCheckpoint = checkRec;
                }
                return fLastCompare;
            }
        }
        final TestVisitor t = new TestVisitor();

        fBTree.accept(t);

        assertEquals(checkpoint, t.fFoundCheckpoint);
        assertEquals(0, t.fLastCompare);
    }

    /**
     * Test many checkpoint insertions. Make sure they can be found after
     * re-opening the file
     */
    @Test
    public void testInsertAlotCheckEquals() {
        ArrayList<Integer> list = insertAlot();

        fBTree = createCollection();

        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            Integer checkpointIndex = list.get(i);
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(12345 + checkpointIndex), new TmfLongLocation(123456L + checkpointIndex), 0);
            BTreeCheckpointVisitor treeVisitor = new BTreeCheckpointVisitor(checkpoint);
            fBTree.accept(treeVisitor);
            assertEquals(checkpoint, treeVisitor.getCheckpoint());
        }
    }
}
