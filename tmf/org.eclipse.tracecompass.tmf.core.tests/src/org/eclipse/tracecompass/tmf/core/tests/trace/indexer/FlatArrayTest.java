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

import org.eclipse.tracecompass.internal.tmf.core.trace.indexer.FlatArray;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.indexer.ITmfPersistentlyIndexable;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;
import org.junit.Test;

/**
 * Tests for the FlatArray class
 *
 * @author Marc-Andre Laperle
 */
public class FlatArrayTest extends AbstractCheckpointCollectionTest {

    private FlatArray fFlatArray;

    @Override
    protected FlatArray createCollection() {
        fCheckpointCollection = fFlatArray = new FlatArray(getFile(), (ITmfPersistentlyIndexable) getTrace());
        return fFlatArray;
    }

    @Override
    public boolean isPersistableCollection() {
        return true;
    }

    /**
     * Tests that binarySearch find the correct checkpoint and ends with a
     * perfect match
     */
    @Test
    public void testBinarySearch() {
        for (long i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(i), new TmfLongLocation(i), 0);
            fFlatArray.insert(checkpoint);
        }

        TmfCheckpoint expectedCheckpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(122), new TmfLongLocation(122L), 0);
        int expectedRank = 122;

        long rank = fFlatArray.binarySearch(expectedCheckpoint);
        ITmfCheckpoint found = fFlatArray.get(rank);

        assertEquals(expectedRank, rank);
        assertEquals(found, expectedCheckpoint);
    }

    /**
     * Test many checkpoint insertions. Make sure they can be found after
     * re-opening the file
     */
    @Test
    public void testInsertAlotCheckEquals() {
        ArrayList<Integer> list = insertAlot();

        fFlatArray = createCollection();

        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            int checkpointIndex = list.get(i);
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(12345 + checkpointIndex),
                    new TmfLongLocation(123456L + checkpointIndex), checkpointIndex);
            ITmfCheckpoint found = fFlatArray.get(checkpointIndex);
            assertEquals(checkpoint, found);
        }
    }

}
