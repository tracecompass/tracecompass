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
import static org.junit.Assert.assertTrue;

import org.eclipse.linuxtools.internal.tmf.core.trace.indexer.TmfMemoryIndex;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.ITmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.linuxtools.tmf.core.trace.location.TmfLongLocation;
import org.junit.Test;

/**
 * Test for the TmfMemoryIndex class
 *
 * @author Marc-Andre Laperle
 */
public class TmfMemoryIndexTest extends AbstractCheckpointCollectionTest {

    private TmfMemoryIndex fMemoryIndex;

    @Override
    protected TmfMemoryIndex createCollection() {
        fMemoryIndex = new TmfMemoryIndex(getTrace());
        return fMemoryIndex;
    }

    /**
     * Test a single insertion
     */
    @Override
    @Test
    public void testInsert() {
        TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(12345), new TmfLongLocation(123456L), 0);
        fMemoryIndex.insert(checkpoint);

        ITmfCheckpoint indexCheckpoint = fMemoryIndex.get(0);
        assertEquals(checkpoint, indexCheckpoint);

        long found = fMemoryIndex.binarySearch(checkpoint);
        assertEquals(0, found);
    }

    /**
     * Tests that binarySearch find the correct checkpoint and ends with a perfect match
     */
    @Test
    public void testBinarySearch() {
        for (long i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(new TmfTimestamp(i), new TmfLongLocation(i), 0);
            fMemoryIndex.insert(checkpoint);
        }

        TmfCheckpoint expectedCheckpoint = new TmfCheckpoint(new TmfTimestamp(122), new TmfLongLocation(122L), 0);
        int expectedRank = 122;

        long rank = fMemoryIndex.binarySearch(expectedCheckpoint);
        ITmfCheckpoint found = fMemoryIndex.get(rank);

        assertEquals(expectedRank, rank);
        assertEquals(found, expectedCheckpoint);
    }

    /**
     * Test dispose
     */
    @Test
    public void testDispose() {
        fMemoryIndex.dispose();
        assertTrue(fMemoryIndex.isEmpty());
    }
}
