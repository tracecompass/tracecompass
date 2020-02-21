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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Random;

import org.eclipse.tracecompass.internal.tmf.core.trace.indexer.ICheckpointCollection;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.indexer.checkpoint.TmfCheckpoint;
import org.eclipse.tracecompass.tmf.core.trace.location.TmfLongLocation;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.TmfTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Common code for ICheckpointCollection test classes
 *
 * @author Marc-Andre Laperle
 */
public abstract class AbstractCheckpointCollectionTest {

    private static final String INDEX_FILE_NAME = "checkpoint.idx"; //$NON-NLS-1$

    /**
     * The number of checkpoints to be inserted in insert tests
     */
    protected static final int CHECKPOINTS_INSERT_NUM = 50000;
    /**
     * The collection being tested
     */
    protected ICheckpointCollection fCheckpointCollection = null;

    private TmfTraceStub fTrace;
    private File fFile = new File(INDEX_FILE_NAME);

    /**
     * Setup the test. Make sure the index is deleted.
     */
    @Before
    public void setUp() {
        fTrace = new TmfTraceStub();
        if (fFile.exists()) {
            fFile.delete();
        }
        fCheckpointCollection = createCollection();
    }

    /**
     * Tear down the test. Make sure the index is deleted.
     */
    @After
    public void tearDown() {
        fTrace.dispose();
        fTrace = null;
        if (fCheckpointCollection != null) {
            fCheckpointCollection.dispose();
        }
        if (fFile.exists()) {
            fFile.delete();
        }
    }

    /**
     * Get the trace being tested.
     *
     * @return the trace being tested.
     */
    public ITmfTrace getTrace() {
        return fTrace;
    }

    /**
     * Returns whether or not the collection is persisted to disk
     *
     * @return true if the collection is persisted to disk, false otherwise
     */
    public boolean isPersistableCollection() {
        return false;
    }

    /**
     * Get the file used for the index being tested.
     *
     * @return the file used for the index being tested.
     */
    public File getFile() {
        return fFile;
    }

    /**
     * Test constructing a new checkpoint collection
     */
    @Test
    public void testConstructor() {
        if (isPersistableCollection()) {
            assertTrue(fFile.exists());
        }
        assertTrue(fCheckpointCollection.isCreatedFromScratch());
    }

    /**
     * Test constructing a new checkpoint collection, existing file
     */
    @Test
    public void testConstructorExistingFile() {
        if (isPersistableCollection()) {
            assertTrue(fFile.exists());
            fCheckpointCollection.dispose();

            fCheckpointCollection = createCollection();
            assertFalse(fCheckpointCollection.isCreatedFromScratch());
        }
    }

    /**
     * Test that a new checkpoint collection is considered created from scratch
     * and vice versa
     */
    @Test
    public void testIsCreatedFromScratch() {
        assertTrue(fCheckpointCollection.isCreatedFromScratch());

        if (isPersistableCollection()) {
            fCheckpointCollection.dispose();
            fCheckpointCollection = createCollection();
            assertFalse(fCheckpointCollection.isCreatedFromScratch());
        }
    }

    /**
     * Test setTimeRange, getTimeRange
     */
    @Test
    public void testSetGetTimeRange() {
        if (isPersistableCollection()) {
            TmfTimeRange timeRange = new TmfTimeRange(TmfTimestamp.fromSeconds(0), TmfTimestamp.fromSeconds(100));
            fCheckpointCollection.setTimeRange(timeRange);
            assertEquals(timeRange, fCheckpointCollection.getTimeRange());
        }
    }

    /**
     * Create a collection for the test
     *
     * @return the collection
     */
    abstract protected ICheckpointCollection createCollection();

    /**
     * Test setNbEvents, getNbEvents
     */
    @Test
    public void testSetGetNbEvents() {
        if (isPersistableCollection()) {
            int expected = 12345;
            fCheckpointCollection.setNbEvents(expected);
            assertEquals(expected, fCheckpointCollection.getNbEvents());
        }
    }

    /**
     * Test get size
     */
    @Test
    public void testGetSize() {
        assertEquals(0, fCheckpointCollection.size());
        int expected = CHECKPOINTS_INSERT_NUM;
        for (int i = 0; i < expected; ++i) {
            fCheckpointCollection.insert(new TmfCheckpoint(TmfTimestamp.fromSeconds(i), new TmfLongLocation(i), i));
        }
        assertEquals(expected, fCheckpointCollection.size());
    }

    /**
     * Test delete
     */
    @Test
    public void testDelete() {
        if (isPersistableCollection()) {
            assertTrue(fFile.exists());
            fCheckpointCollection.delete();
            assertFalse(fFile.exists());
        }
    }

    /**
     * Test version change
     *
     * @throws IOException
     *             can throw this
     */
    @Test
    public void testVersionChange() throws IOException {
        fCheckpointCollection.dispose();
        try (RandomAccessFile f = new RandomAccessFile(fFile, "rw");) {
            f.writeInt(-1);
        }

        fCheckpointCollection = createCollection();
        assertTrue(fCheckpointCollection.isCreatedFromScratch());
    }

    /**
     * Test version change
     *
     * @throws IOException
     *             can throw this
     */
    @Test
    public void testDeleteWhenInvalidBug479675() throws IOException {
        insertAlot();
        try (RandomAccessFile f = new RandomAccessFile(fFile, "rw");) {
            f.writeInt(-1);
        }

        fCheckpointCollection = createCollection();
        if (isPersistableCollection()) {
            ICheckpointCollection old = fCheckpointCollection;
            try {
                fCheckpointCollection = createCollection();
                assertEquals(0, fCheckpointCollection.size());
            } finally {
                old.dispose();
            }
        }
        assertTrue(fCheckpointCollection.isCreatedFromScratch());
    }

    /**
     * Test a single insertion
     */
    @Test
    public void testInsert() {
        TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(12345), new TmfLongLocation(123456L), 0);
        fCheckpointCollection.insert(checkpoint);

        long found = fCheckpointCollection.binarySearch(checkpoint);
        assertEquals(0, found);
        assertEquals(1, fCheckpointCollection.size());
    }

    /**
     * Generate many checkpoints and insert them in the collection
     *
     * @return the list of generated checkpoints
     */
    protected ArrayList<Integer> insertAlot() {
        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(12345 + i), new TmfLongLocation(123456L + i), i);
            fCheckpointCollection.insert(checkpoint);
        }

        assertEquals(CHECKPOINTS_INSERT_NUM, fCheckpointCollection.size());
        if (isPersistableCollection()) {
            fCheckpointCollection.dispose();
        }

        boolean random = true;
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            if (random) {
                Random rand = new Random();
                list.add(rand.nextInt(CHECKPOINTS_INSERT_NUM));
            } else {
                list.add(i);
            }
        }
        return list;
    }

    /**
     * Test many checkpoint insertions. Make sure they can be found after
     * re-opening the file
     */
    @Test
    public void testInsertAlot() {
        ArrayList<Integer> list = insertAlot();

        if (isPersistableCollection()) {
            fCheckpointCollection = createCollection();
        }
        assertEquals(CHECKPOINTS_INSERT_NUM, fCheckpointCollection.size());

        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            Integer randomCheckpoint = list.get(i);
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(12345 + randomCheckpoint), new TmfLongLocation(123456L + randomCheckpoint), 0);
            long found = fCheckpointCollection.binarySearch(checkpoint);
            assertEquals(randomCheckpoint.intValue(), found);
        }

        assertEquals(CHECKPOINTS_INSERT_NUM, fCheckpointCollection.size());
    }

    /**
     * Test many checkpoint insertions using the same timestamp. Make sure they
     * can be found after re-opening the file
     */
    @Test
    public void testInsertSameTimestamp() {
        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(12345), new TmfLongLocation(123456L + i), i);
            fCheckpointCollection.insert(checkpoint);
        }
        assertEquals(CHECKPOINTS_INSERT_NUM, fCheckpointCollection.size());

        if (isPersistableCollection()) {
            fCheckpointCollection.dispose();
        }

        boolean random = true;
        ArrayList<Integer> list = new ArrayList<>();
        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            if (random) {
                Random rand = new Random();
                list.add(rand.nextInt(CHECKPOINTS_INSERT_NUM));
            } else {
                list.add(i);
            }
        }

        if (isPersistableCollection()) {
            fCheckpointCollection = createCollection();
        }
        assertEquals(CHECKPOINTS_INSERT_NUM, fCheckpointCollection.size());

        for (int i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            Integer randomCheckpoint = list.get(i);
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(12345), new TmfLongLocation(123456L + randomCheckpoint), 0);
            long found = fCheckpointCollection.binarySearch(checkpoint);
            assertEquals(randomCheckpoint.intValue(), found);
        }
    }

    /**
     * Tests that binarySearch find the correct checkpoint when the time stamp
     * is between checkpoints
     */
    @Test
    public void testBinarySearchFindInBetween() {
        for (long i = 0; i < CHECKPOINTS_INSERT_NUM; i++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(2 * i), new TmfLongLocation(2 * i), i);
            fCheckpointCollection.insert(checkpoint);
        }
        assertEquals(CHECKPOINTS_INSERT_NUM, fCheckpointCollection.size());

        TmfCheckpoint searchedCheckpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(123), new TmfLongLocation(123L), 123);
        int expectedInsertionPoint = 61;
        int expectedRank = -(expectedInsertionPoint + 2);

        long rank = fCheckpointCollection.binarySearch(searchedCheckpoint);
        assertEquals(expectedRank, rank);
    }


    /**
     * Tests that binarySearch finds the correct checkpoint when searching for a
     * checkpoint with a null location. It should return the previous checkpoint
     * from the first checkpoint that matches the timestamp.
     */
    @Test
    public void testBinarySearchInBetweenSameTimestamp() {
        int checkpointNum = 0;
        for (; checkpointNum < CHECKPOINTS_INSERT_NUM / 2; checkpointNum++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(0), new TmfLongLocation(checkpointNum), checkpointNum);
            fCheckpointCollection.insert(checkpoint);
        }

        for (; checkpointNum < CHECKPOINTS_INSERT_NUM; checkpointNum++) {
            TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(1), new TmfLongLocation(checkpointNum), checkpointNum);
            fCheckpointCollection.insert(checkpoint);
        }
        assertEquals(CHECKPOINTS_INSERT_NUM, fCheckpointCollection.size());

        final TmfCheckpoint searchedCheckpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(1), null, 0);

        long found = fCheckpointCollection.binarySearch(searchedCheckpoint);

        int expectedInsertionPoint = CHECKPOINTS_INSERT_NUM / 2 - 1;
        int expectedRank = -(expectedInsertionPoint + 2);

        assertEquals(expectedRank, found);
    }

    /**
     * Test checkpoint insertions after reopening the file.
     */
    @Test
    public void testInsertAfterReopen() {
        if (!isPersistableCollection()) {
            return;
        }

        fCheckpointCollection.insert(new TmfCheckpoint(TmfTimestamp.fromSeconds(12345), new TmfLongLocation(123456L), 0));

        assertEquals(1, fCheckpointCollection.size());

        fCheckpointCollection.dispose();
        fCheckpointCollection = createCollection();
        assertEquals(1, fCheckpointCollection.size());

        TmfCheckpoint checkpoint = new TmfCheckpoint(TmfTimestamp.fromSeconds(12345), new TmfLongLocation(123456L), 0);
        long found = fCheckpointCollection.binarySearch(checkpoint);
        assertEquals(0, found);

        fCheckpointCollection.insert(new TmfCheckpoint(TmfTimestamp.fromSeconds(12345 + 1), new TmfLongLocation(123456L + 1), 1));
        assertEquals(2, fCheckpointCollection.size());

        fCheckpointCollection.dispose();
        fCheckpointCollection = createCollection();
        assertEquals(2, fCheckpointCollection.size());
    }

    /**
     * Test that a checkpoint can be inserted after reopening an empty index.
     */
    @Test
    public void testInsertAfterEmptyReopen() {
        fCheckpointCollection.dispose();
        fCheckpointCollection = createCollection();
        fCheckpointCollection.insert(new TmfCheckpoint(TmfTimestamp.fromSeconds(12345), new TmfLongLocation(123456L), 0));
        assertEquals(1, fCheckpointCollection.size());
    }
}
