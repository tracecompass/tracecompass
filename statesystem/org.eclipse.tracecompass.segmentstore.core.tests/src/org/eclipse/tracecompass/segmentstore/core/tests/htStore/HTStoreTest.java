/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.htStore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.segmentstore.core.segmentHistoryTree.HistoryTreeSegmentStore;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.tests.AbstractTestSegmentStore;
import org.eclipse.tracecompass.segmentstore.core.tests.historytree.HistoryTreeSegmentStoreStub;
import org.junit.After;
import org.junit.Test;

/**
 * Unit tests for the history tree segment store. It tests the segment store
 * specific functionalities.
 *
 * @author Loïc Prieur-Drevon
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class HTStoreTest extends AbstractTestSegmentStore {

    private @Nullable Path fFilePath;

    @Override
    protected HistoryTreeSegmentStoreStub<@NonNull TestSegment> getSegmentStore() {
        try {
            Path tmpFile = Files.createTempFile("tmpSegStore", null);
            fFilePath = tmpFile;
            assertNotNull(tmpFile);
            return new HistoryTreeSegmentStoreStub<>(tmpFile, 1, TestSegment.DESERIALISER);
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create the segment store: " + e.getMessage());
        }
    }

    @Override
    protected ISegmentStore<@NonNull TestSegment> getSegmentStore(@NonNull TestSegment @NonNull [] data) {
        try {
            Path tmpFile = Files.createTempFile("tmpSegStore", null);
            fFilePath = tmpFile;
            assertNotNull(tmpFile);
            HistoryTreeSegmentStoreStub<TestSegment> store = new HistoryTreeSegmentStoreStub<>(tmpFile, 1, TestSegment.DESERIALISER);
            store.addAll(Arrays.asList(data));
            return store;
        } catch (IOException e) {
            throw new IllegalStateException("Couldn't create the segment store: " + e.getMessage());
        }
    }

    /**
     * Dispose of the segment store
     */
    @Override
    @After
    public void teardown() {
        fSegmentStore.dispose();
        // Delete the temp file
        if (Files.exists(fFilePath)) {
            try {
                Files.delete(fFilePath);
            } catch (IOException e) {
                throw new IllegalStateException("Error deleting the file: " + e.getMessage());
            }
        }
    }

    /**
     * Overrides the assert equals to compare 2 segments. The HT segments are not
     * identical to the original segments.
     *
     * @param expected
     *            The expected segment
     * @param actual
     *            The actual segment
     */
    @Override
    protected void assertSegmentsEqual(@Nullable ISegment expected, @Nullable ISegment actual) {
        assertNotNull(expected);
        assertNotNull(actual);
        assertEquals(expected.getStart(), actual.getStart());
        assertEquals(expected.getEnd(), actual.getEnd());
        assertEquals(expected.getLength(), actual.getLength());
    }

    @Override
    @Test
    public void testIterationOrderNonSortedInsertion() {
        /** The segments are not sorted, so this test does not apply */
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testToSpecifyArraySubtype() {
        super.testToSpecifyArraySubtype();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testToObjectArray() {
        super.testToObjectArray();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testToSpecificArray() {
        super.testToSpecificArray();
    }

    @Override
    @Test(expected = UnsupportedOperationException.class)
    public void testAddAllConstructor() {
        super.testAddAllConstructor();
    }

    /**
     * Test reading and re-reading a {@link HistoryTreeSegmentStore}
     *
     * @throws IOException
     *             Exception thrown by application
     */
    @Test
    public void testReadingSegmentStore() throws IOException {
        Path dirPath = Files.createTempDirectory("tmpSegStoreDir");
        Path filePath = Paths.get(dirPath.toString(), "tmpSegStore");
        assertNotNull(filePath);

        try {
            // Get the segment store, it should be in build mode and fill it with some data
            HistoryTreeSegmentStoreStub<TestSegment> segmentStore = new HistoryTreeSegmentStoreStub<>(filePath, 1, TestSegment.DESERIALISER);
            segmentStore.add(new TestSegment(1, 3, "abc"));
            segmentStore.finishedBuilding(4);
            segmentStore.dispose();

            // Open the segment store, it should be filled with the segment
            segmentStore = new HistoryTreeSegmentStoreStub<>(filePath, 1, TestSegment.DESERIALISER);
            assertEquals(1, segmentStore.size());
            segmentStore.dispose();

            // Re-open the segment store, it should be filled with the segment
            segmentStore = new HistoryTreeSegmentStoreStub<>(filePath, 1, TestSegment.DESERIALISER);
            assertEquals(1, segmentStore.size());
            segmentStore.dispose();
        } finally {
            Files.delete(filePath);
            Files.delete(dirPath);
        }
    }

}