/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Marc-Andre Laperle - Initial API and implementation
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace.indexer;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.BTree;
import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.BTreeCheckpointVisitor;
import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.IBTreeVisitor;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
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
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(i), new TmfLongLocation((long) i), 0);
            fBTree.insert(checkpoint);
        }

        final TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(123), new TmfLongLocation(123L), 0);

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
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345 + checkpointIndex), new TmfLongLocation(123456L + checkpointIndex), 0);
            BTreeCheckpointVisitor treeVisitor = new BTreeCheckpointVisitor(checkpoint);
            fBTree.accept(treeVisitor);
            assertEquals(checkpoint, treeVisitor.getCheckpoint());
        }
    }

    /**
     * Test setSize, size
     */
    @Override
    @Test
    public void testSetGetSize() {
        assertEquals(0, fBTree.size());
        int expected = CHECKPOINTS_INSERT_NUM;
        for (int i = 0; i < expected; ++i) {
            fBTree.insert(new TmfCheckpoint(new TmfTimestamp(0), new TmfLongLocation(0L), 0));
            fBTree.setSize(fBTree.size() + 1);
        }
        assertEquals(expected, fBTree.size());
    }

}
