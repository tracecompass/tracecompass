/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore.statistics;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.SegmentStoreStatistics;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.junit.Test;

/**
 * Test the staticsmodule. This is done with two tests.
 * <ol>
 * <li>test the values vs some sample points calculated by hand (sanity test)
 * </li>
 * <li>2- test exhaustively vs a reference implementation.</li>
 * </ol>
 *
 * @author Matthew Khouzam
 *
 */
public class SegmentStoreStatisticsTest {

    private static final int MEDIUM_AMOUNT_OF_SEGMENTS = 100;
    private static final int LARGE_AMOUNT_OF_SEGMENTS = 1000000;

    private static final double NO_ERROR = 0.0;
    private static final double ERROR = 0.000001;
    private static final double APPROX_ERROR = 0.0001;

    private static void testOnlineVsOffline(List<@NonNull ISegment> fixture) {
        validate(new OfflineStatisticsCalculator(fixture), getSegStoreStat(fixture));
    }

    /**
     * Test incrementing
     */
    @Test
    public void climbTest() {
        List<@NonNull ISegment> fixture = new ArrayList<>(MEDIUM_AMOUNT_OF_SEGMENTS);
        for (int i = 0; i < MEDIUM_AMOUNT_OF_SEGMENTS; i++) {
            fixture.add(createDummySegment(i, i * 2));
        }
        SegmentStoreStatistics sss = getSegStoreStat(fixture);
        assertEquals("Average", 49.5, sss.getAverage(), ERROR);
        assertEquals("Min", 0, sss.getMin());
        assertEquals("Max", 99, sss.getMax());
        assertEquals("Standard Deviation", 29.0, sss.getStdDev(), 0.02);
        assertEquals("Min Segment", 0, sss.getMinSegment().getLength());
        assertEquals("Max Segment", 99, sss.getMaxSegment().getLength());
        testOnlineVsOffline(fixture);
    }

    private static SegmentStoreStatistics getSegStoreStat(List<@NonNull ISegment> fixture) {
        SegmentStoreStatistics sss = new SegmentStoreStatistics();
        for (ISegment seg : fixture) {
            sss.update(seg);
        }
        return sss;
    }

    /**
     * Test decrementing
     */
    @Test
    public void decrementingTest() {
        List<@NonNull ISegment> fixture = new ArrayList<>(MEDIUM_AMOUNT_OF_SEGMENTS);
        for (int i = MEDIUM_AMOUNT_OF_SEGMENTS; i >= 0; i--) {
            fixture.add(createDummySegment(i, i * 2));
        }
        SegmentStoreStatistics sss = getSegStoreStat(fixture);
        assertEquals("Average", 50, sss.getAverage(), NO_ERROR);
        assertEquals("Min", 0, sss.getMin());
        assertEquals("Max", 100, sss.getMax());
        assertEquals("Standard Deviation", 29.3, sss.getStdDev(), 0.01);
        assertEquals("Min Segment", 0, sss.getMinSegment().getLength());
        assertEquals("Max Segment", 100, sss.getMaxSegment().getLength());
        testOnlineVsOffline(fixture);
    }

    /**
     * Test small
     */
    @Test
    public void smallTest() {
        List<@NonNull ISegment> fixture = new ArrayList<>();
        for (int i = 1; i >= 0; i--) {
            fixture.add(createDummySegment(i, i * 2));
        }
        testOnlineVsOffline(fixture);
    }

    /**
     * Test large
     */
    @Test
    public void largeTest() {
        List<@NonNull ISegment> fixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            fixture.add(createDummySegment(i, i * 2));
        }
        testOnlineVsOffline(fixture);
    }

    /**
     * Test noise
     */
    @Test
    public void noiseTest() {
        Random rnd = new Random();
        rnd.setSeed(1234);
        List<@NonNull ISegment> fixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            int end = start + Math.abs(rnd.nextInt(1000000));
            fixture.add(createDummySegment(start, end));
        }
        testOnlineVsOffline(fixture);
    }

    /**
     * Test gaussian noise
     */
    @Test
    public void gaussianNoiseTest() {
        Random rnd = new Random();
        rnd.setSeed(1234);
        List<@NonNull ISegment> fixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            final int delta = Math.abs(rnd.nextInt(1000));
            int end = start + delta * delta;
            fixture.add(createDummySegment(start, end));
        }
        testOnlineVsOffline(fixture);
    }

    /**
     * Test building a statistics store with streams
     */
    @Test
    public void streamBuildingTest() {
        SegmentStoreStatistics expected = new SegmentStoreStatistics();
        List<@NonNull ISegment> fixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        for (long i = 0; i < LARGE_AMOUNT_OF_SEGMENTS; i++) {
            fixture.add(new BasicSegment(i, i + 2));
        }
        fixture.forEach(e -> expected.update(e));
        SegmentStoreStatistics actual = fixture.stream()
                .<@NonNull SegmentStoreStatistics> collect(SegmentStoreStatistics::new, SegmentStoreStatistics::update, SegmentStoreStatistics::merge);
        validate(expected, actual);
    }

    /**
     * Test building a statistics store with parallel streams
     */
    @Test
    public void parallelStreamBuildingTest() {
        SegmentStoreStatistics expected = new SegmentStoreStatistics();
        List<@NonNull ISegment> fixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        for (long i = 0; i < LARGE_AMOUNT_OF_SEGMENTS; i++) {
            fixture.add(new BasicSegment(i, i + 2));
        }
        fixture.forEach(e -> expected.update(e));
        SegmentStoreStatistics actual = fixture.parallelStream()
                .<@NonNull SegmentStoreStatistics> collect(SegmentStoreStatistics::new, SegmentStoreStatistics::update, SegmentStoreStatistics::merge);
        validate(expected, actual);
    }

    /**
     * Test statistics nodes being merged. Two contiguous blocks.
     */
    @Test
    public void mergeStatisticsNodesTest() {
        // calculates stats for all the segments
        SegmentStoreStatistics expected = new SegmentStoreStatistics();
        // calculates stats for half of the segments
        SegmentStoreStatistics a = new SegmentStoreStatistics();
        // calculates stats for another half of the segments
        SegmentStoreStatistics b = new SegmentStoreStatistics();
        List<@NonNull ISegment> fixture = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            ISegment seg = new BasicSegment(i, i * 2 + 2);
            expected.update(seg);
            a.update(seg);
            fixture.add(seg);
        }
        for (int i = 0; i < 10; i++) {
            ISegment seg = new BasicSegment(i, i * 2 + 2);
            expected.update(seg);
            b.update(seg);
            fixture.add(seg);
        }
        a.merge(b);
        OfflineStatisticsCalculator offlineExpected = new OfflineStatisticsCalculator(fixture);
        // Compare the expected stats with the offline algorithm
        validate(offlineExpected, expected);
        // Compare the results of the merge with the expected results
        validate(expected, a);
    }

    /**
     * Test statistics nodes being merged. Two random blocks.
     */
    @Test
    public void mergeStatisticsRandomNodesTest() {
        // calculates stats for all the segments
        SegmentStoreStatistics expected = new SegmentStoreStatistics();
        // calculates stats for half of the segments, randomly
        SegmentStoreStatistics a = new SegmentStoreStatistics();
        // calculates stats for the other half of the segments
        SegmentStoreStatistics b = new SegmentStoreStatistics();
        List<@NonNull ISegment> fixture = new ArrayList<>();
        Random rnd = new Random();
        rnd.setSeed(10);
        int size = rnd.nextInt(1000);
        int size2 = rnd.nextInt(1000);
        for (int i = 0; i < size; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            final int delta = Math.abs(rnd.nextInt(1000));
            int end = start + delta * delta;
            ISegment seg = new BasicSegment(start, end);
            expected.update(seg);
            a.update(seg);
            fixture.add(seg);
        }
        for (int i = 0; i < size2; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            final int delta = Math.abs(rnd.nextInt(1000));
            int end = start + delta * delta;
            ISegment seg = new BasicSegment(start, end);
            expected.update(seg);
            b.update(seg);
            fixture.add(seg);
        }
        a.merge(b);
        assertEquals(size + size2, a.getNbSegments());
        OfflineStatisticsCalculator offlineExpected = new OfflineStatisticsCalculator(fixture);
        // Compare the expected stats with the offline algorithm
        validate(offlineExpected, expected);
        // Compare the results of the merge with the expected results
        validate(expected, a);
    }

    /**
     * Test statistics nodes being merged. Two overlapping blocks.
     */
    @Test
    public void mergeStatisticsOverlappingNodesTest() {
        // calculates stats for all the segments
        SegmentStoreStatistics expected = new SegmentStoreStatistics();
        // calculates stats for half of the segments
        SegmentStoreStatistics a = new SegmentStoreStatistics();
        // calculates stats for the other half of the segments
        SegmentStoreStatistics b = new SegmentStoreStatistics();
        List<@NonNull ISegment> fixture = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            BasicSegment seg = new BasicSegment(i, i * 2 + 2);
            expected.update(seg);
            if ((i & 2) != 0) {
                a.update(seg);
            } else {
                b.update(seg);
            }
            fixture.add(seg);
        }
        a.merge(b);
        OfflineStatisticsCalculator offlineExpected = new OfflineStatisticsCalculator(fixture);
        validate(offlineExpected, expected);
        validate(expected, a);
    }

    private static @NonNull SegmentStoreStatistics fillSmallStatistics() {
        SegmentStoreStatistics stats = new SegmentStoreStatistics();
        for (int i = 0; i < 10; i++) {
            BasicSegment seg = new BasicSegment(i, i * 2 + 2);
            stats.update(seg);
        }
        return stats;
    }

    /**
     * Test statistics nodes being merged. corner cases.
     */
    @Test
    public void mergeStatisticsCorenerCaseNodesTest() {
        ISegment segment = new BasicSegment(1, 5);

        // Control statistics, not to be modified
        SegmentStoreStatistics noSegments = new SegmentStoreStatistics();
        SegmentStoreStatistics oneSegment = new SegmentStoreStatistics();
        oneSegment.update(segment);

        // The segment store statistics to test
        SegmentStoreStatistics testStats = new SegmentStoreStatistics();
        SegmentStoreStatistics testStats2 = new SegmentStoreStatistics();

        // Test merging empty stats on a non-empty one
        testStats.update(segment);
        testStats.merge(testStats2);
        validate(oneSegment, testStats);
        validate(noSegments, testStats2);

        // Test merging on an empty stats
        testStats2.merge(testStats);
        validate(oneSegment, testStats);
        validate(oneSegment, testStats2);

        // Fill a small segment store and add the one extra segment to it
        SegmentStoreStatistics expected = fillSmallStatistics();
        expected.update(segment);

        // Test merging stats with only 1 segment
        testStats = fillSmallStatistics();
        testStats.merge(testStats2);
        validate(oneSegment, testStats2);
        validate(expected, testStats);

        // Test merging on stats with only 1 segment
        testStats = fillSmallStatistics();
        testStats2.merge(testStats);
        validate(fillSmallStatistics(), testStats);
        validate(expected, testStats2);

    }

    private static void validate(SegmentStoreStatistics expected, SegmentStoreStatistics toBeTested) {
        assertEquals("# of Segments", expected.getNbSegments(), toBeTested.getNbSegments());
        assertEquals("Total duration", expected.getTotal(), toBeTested.getTotal(), ERROR * expected.getTotal());
        assertEquals("Average", expected.getAverage(), toBeTested.getAverage(), ERROR * expected.getAverage());
        assertEquals("Min", expected.getMin(), toBeTested.getMin());
        assertEquals("Max", expected.getMax(), toBeTested.getMax());
        assertEquals("Min Segment", expected.getMinSegment().getLength(), toBeTested.getMinSegment().getLength());
        assertEquals("Max Segment", expected.getMaxSegment().getLength(), toBeTested.getMaxSegment().getLength());
        assertEquals("Standard Deviation", expected.getStdDev(), toBeTested.getStdDev(), APPROX_ERROR * expected.getStdDev());
    }

    private static void validate(OfflineStatisticsCalculator osc, SegmentStoreStatistics sss) {
        assertEquals("# of Segments", osc.count(), sss.getNbSegments());
        assertEquals("Total duration", osc.getTotal(), sss.getTotal(), ERROR * osc.getTotal());
        assertEquals("Average", osc.getAvg(), sss.getAverage(), ERROR * osc.getAvg());
        assertEquals("Min", osc.getMin(), sss.getMin());
        assertEquals("Max", osc.getMax(), sss.getMax());
        assertEquals("Min Segment", osc.getMin(), sss.getMinSegment().getLength());
        assertEquals("Max Segment", osc.getMax(), sss.getMaxSegment().getLength());
        assertEquals("Standard Deviation", osc.getStdDev(), sss.getStdDev(), ERROR * osc.getStdDev());
    }

    private static @NonNull BasicSegment createDummySegment(int start, int end) {
        return new BasicSegment(start, end);
    }
}
