/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.function.Function;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.timing.core.statistics.IStatistics;
import org.eclipse.tracecompass.analysis.timing.core.statistics.Statistics;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Base class to test statistics for different object types. This is done with
 * two tests.
 * <ol>
 * <li>test the values vs some sample points calculated by hand (sanity test)
 * </li>
 * <li>2- test exhaustively vs a reference implementation.</li>
 * </ol>
 *
 * Each implementation will need to provide the object type to use for the test
 * and implement a method to get objects that will return the values to test.
 * Any additional dataset that should be tested for a specific object should be
 * implemented in a concrete class for that object type.
 *
 * This test class tests statistics with positive and negative values. If for certain objects, negative values are not supported, the following test methods should be overridden, to be ignored:
 * <ol>
 * <li>{@link #testLimitDataset2()}</li>
 * <li>{@link #testLargeDatasetNegative()}<li>
 * </ol>
 *
 * @author Matthew Khouzam
 * @author Geneviève Bastien
 * @param <E>
 *            The type of object to calculate statistics on
 */
public abstract class AbstractStatisticsTest<@NonNull E> {

    private static final int MEDIUM_AMOUNT_OF_SEGMENTS = 100;
    private static final int LARGE_AMOUNT_OF_SEGMENTS = 1000000;

    private static final double ERROR = 0.000001;
    private static final double APPROX_ERROR = 0.0001;

    private final @Nullable Function<@NonNull E, @NonNull Long> fMapper;

    /**
     * Constructor
     *
     * @param mapper
     *            A mapper function that takes an object to computes statistics
     *            for and returns the value to use for the statistics. If the
     *            mapper is <code>null</code>, a default Long identity function
     *            will be used
     */
    public AbstractStatisticsTest(Function<E, @NonNull Long> mapper) {
        fMapper = mapper;
    }

    /**
     * Return the default mapper, or if it is null, a default mapper that casts to Long
     * @return
     */
    private @NonNull Function<@NonNull E, @NonNull Long> getMapper() {
        Function<@NonNull E, @NonNull Long> mapper = fMapper;
        if (mapper == null) {
            // Data type should be long, so define a default mapper
            return e -> (Long) e;
        }
        return mapper;
    }

    private void testOnlineVsOffline(Collection<E> fixture) {

        validate(new OfflineStatisticsCalculator<>(fixture, getMapper()), buildStats(fixture));
    }

    private Statistics<E> buildStats(Collection<E> fixture) {
        Statistics<E> sss = createStatistics();
        for (E seg : fixture) {
            sss.update(seg);
        }
        return sss;
    }

    private static <@NonNull E> void validate(IStatistics<E> expected, IStatistics<E> toBeTested) {
        assertEquals("# of elements", expected.getNbElements(), toBeTested.getNbElements());
        assertEquals("Sum of values", expected.getTotal(), toBeTested.getTotal(), ERROR * expected.getTotal());
        assertEquals("Mean", expected.getMean(), toBeTested.getMean(), ERROR * expected.getMean());
        assertEquals("Min", expected.getMin(), toBeTested.getMin());
        assertEquals("Max", expected.getMax(), toBeTested.getMax());
        assertEquals("Min Element", expected.getMinObject(), toBeTested.getMinObject());
        assertEquals("Max Element", expected.getMaxObject(), toBeTested.getMaxObject());
        assertEquals("Standard Deviation", expected.getStdDev(), toBeTested.getStdDev(), APPROX_ERROR * expected.getStdDev());
    }

    /**
     * Create a statistics object by calling the appropriate constructor whether the mapper function is null or not
     */
    private @NonNull Statistics<E> createStatistics() {
        Function<@NonNull E, @NonNull Long> mapper = fMapper;
        if (mapper == null) {
            return new Statistics<>();
        }
        return new Statistics<>(mapper);
    }

    /**
     * Create the fixture of elements of the generic type from the expected
     * values for a test. For instance, if the test wants to test values {2, 4,
     * 6} for a Statistics object for class Foo, then this method will return a
     * Collection of Foo objects whose mapper function will map respectively to
     * {2, 4, 6}
     *
     * @param longFixture
     *            The long values that objects should map to for this test
     * @return A collection of E elements that map to the long values
     */
    protected abstract Collection<E> createElementsWithValues(Collection<@NonNull Long> longFixture);

    /**
     * Test statistics on empty dataset
     */
    @Test
    public void testEmpty() {
        // Verify the expected default values
        Statistics<E> stats = createStatistics();
        assertEquals("Mean", 0, stats.getMean(), ERROR);
        assertEquals("Min", Long.MAX_VALUE, stats.getMin());
        assertEquals("Max", Long.MIN_VALUE, stats.getMax());
        assertEquals("Standard Deviation", Double.NaN, stats.getStdDev(), ERROR);
        assertNull(stats.getMinObject());
        assertNull(stats.getMaxObject());
        assertEquals("Nb objects", 0, stats.getNbElements());
        assertEquals("Total", 0, stats.getTotal(), ERROR);
    }

    /**
     * Test statistics with values added in ascending order
     */
    @Test
    public void testAscending() {
        // Create a fixture of long values in ascending order
        List<@NonNull Long> longFixture = new ArrayList<>(MEDIUM_AMOUNT_OF_SEGMENTS);
        for (long i = 0; i <= MEDIUM_AMOUNT_OF_SEGMENTS; i++) {
            longFixture.add(i);
        }

        // Create the statistics object for the objects that will return those
        // values
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);
        Statistics<E> sss = buildStats(fixture);
        assertEquals("Mean", 50, sss.getMean(), ERROR);
        assertEquals("Min", 0, sss.getMin());
        assertEquals("Max", MEDIUM_AMOUNT_OF_SEGMENTS, sss.getMax());
        assertEquals("Standard Deviation", 29.3, sss.getStdDev(), 0.02);

        // Compare with an offline algorithm
        testOnlineVsOffline(fixture);
    }

    /**
     * Test statistics with values added in descending order
     */
    @Test
    public void testDescending() {
        // Create a fixture of long values in descending order.
        List<@NonNull Long> longFixture = new ArrayList<>(MEDIUM_AMOUNT_OF_SEGMENTS);
        for (long i = MEDIUM_AMOUNT_OF_SEGMENTS; i >= 0; i--) {
            longFixture.add(i);
        }

        // Create the statistics object for the objects that will return those
        // values
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);
        Statistics<E> sss = buildStats(fixture);
        assertEquals("Mean", 50, sss.getMean(), ERROR);
        assertEquals("Min", 0, sss.getMin());
        assertEquals("Max", MEDIUM_AMOUNT_OF_SEGMENTS, sss.getMax());
        assertEquals("Standard Deviation", 29.3, sss.getStdDev(), 0.02);

        // Compare with an offline algorithm
        testOnlineVsOffline(fixture);

    }

    /**
     * Test a data set with a small number of objects
     */
    @Test
    public void testSmallDataset() {
        // Create fixture with only 1 element
        List<@NonNull Long> longFixture = new ArrayList<>(1);
        longFixture.add(1L);

        // Create the statistics object for the objects that will return those
        // values
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);

        // Compare with an offline algorithm
        testOnlineVsOffline(fixture);
    }

    /**
     * Test a dataset with positive limit values
     */
    @Test
    public void testLimitDataset() {
        // Create a fixture with max values
        List<@NonNull Long> longFixture = new ArrayList<>(1);
        longFixture.add(Long.MAX_VALUE);
        longFixture.add(Long.MAX_VALUE);

        // Create the statistics object for the objects that will return those
        // values
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);
        Statistics<E> sss = buildStats(fixture);
        // Test some values
        assertEquals("Mean", Long.MAX_VALUE, sss.getMean(), ERROR);
        assertEquals("Total", (double) 2 * Long.MAX_VALUE, sss.getTotal(), ERROR);
        assertEquals("Standard deviation", Double.NaN, sss.getStdDev(), ERROR);

        // Compare with an offline algorithm
        testOnlineVsOffline(fixture);
    }

    /**
     * Test a dataset with negative limit values
     *
     * NOTE: This test has negative values
     */
    @Test
    public void testLimitDataset2() {
        // Create a fixture with min values
        List<@NonNull Long> longFixture = new ArrayList<>(1);
        longFixture.add(Long.MIN_VALUE);
        longFixture.add(Long.MIN_VALUE);
        longFixture.add(Long.MIN_VALUE);

        // Create the statistics object for the objects that will return those
        // values
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);
        Statistics<E> sss = buildStats(fixture);
        // Test some values
        assertEquals("Mean", Long.MIN_VALUE, sss.getMean(), ERROR);
        assertEquals("Total", (double) 3 * Long.MIN_VALUE, sss.getTotal(), ERROR);
        assertEquals("Standard deviation", 0, sss.getStdDev(), ERROR);

        // Compare with an offline algorithm
        testOnlineVsOffline(fixture);
    }

    /**
     * Test a data set with a large number of objects of random values
     */
    @Test
    public void testLargeDataset() {
        // Create a fixture of a large number of random values
        List<@NonNull Long> longFixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        Random rng = new Random(10);
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            longFixture.add(Math.abs(rng.nextLong()));
        }
        // Create the statistics object for the objects that will return those
        // values
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);

        // Compare with an offline algorithm
        testOnlineVsOffline(fixture);

    }

    /**
     * Test a data set with a large number of objects of random values
     *
     * NOTE: This test contains negative values
     */
    @Test
    public void testLargeDatasetNegative() {
        // Create a fixture of a large number of random values
        List<@NonNull Long> longFixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        Random rng = new Random(10);
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            longFixture.add(rng.nextLong());
        }
        // Create the statistics object for the objects that will return those
        // values
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);

        // Compare with an offline algorithm
        testOnlineVsOffline(fixture);

    }

    /**
     * Test a random dataset where the distribution follows white noise
     */
    @Test
    public void testNoiseDataset() {
        // Create a fixture of a large number of random values
        List<@NonNull Long> longFixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        Random rng = new Random(1234);
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            longFixture.add(Long.valueOf(Math.abs(rng.nextInt(1000000))));
        }
        // Create the statistics object for the objects that will return those
        // values
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);

        // Compare with an offline algorithm
        testOnlineVsOffline(fixture);

    }

    /**
     * Test a random dataset where the distribution follows gaussian noise
     */
    @Test
    public void gaussianNoiseTest() {
        // Create a fixture of a large number of random values
        List<@NonNull Long> longFixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        Random rng = new Random(1234);
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            longFixture.add(Long.valueOf(Math.abs(rng.nextInt(1000))));
        }
        // Create the statistics object for the objects that will return those
        // values
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);

        // Compare with an offline algorithm
        testOnlineVsOffline(fixture);

    }

    /**
     * Test building a statistics store with streams
     */
    @Test
    public void streamBuildingTest() {
        Statistics<E> expected = createStatistics();
        List<@NonNull Long> longFixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        for (long i = 0; i < LARGE_AMOUNT_OF_SEGMENTS; i++) {
            longFixture.add(i);
        }
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);
        fixture.forEach(e -> expected.update(e));
        Statistics<E> actual = fixture.stream()
                .<org.eclipse.tracecompass.analysis.timing.core.statistics.Statistics<E>> collect(() -> createStatistics(),
                        Statistics<E>::update, Statistics<E>::merge);
        validate(expected, actual);
    }

    /**
     * Test building a statistics store with parallel streams
     */
    @Test
    public void parallelStreamBuildingTest() {
        Statistics<E> expected = createStatistics();
        List<@NonNull Long> longFixture = new ArrayList<>(LARGE_AMOUNT_OF_SEGMENTS);
        for (long i = 0; i < LARGE_AMOUNT_OF_SEGMENTS; i++) {
            longFixture.add(i);
        }
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);
        fixture.forEach(e -> expected.update(e));
        Statistics<E> actual = fixture.parallelStream()
                .<org.eclipse.tracecompass.analysis.timing.core.statistics.Statistics<E>> collect(() -> createStatistics(),
                        Statistics<E>::update, Statistics<E>::merge);
        validate(expected, actual);
    }

    /**
     * Test statistics nodes being merged. Two identical blocks.
     */
    @Test
    public void testMergeStatisticsNodes() {
        // Create a fixture of a few values
        int nbElements = 10;
        List<@NonNull Long> longFixture = new ArrayList<>(nbElements);
        for (long i = 0; i < nbElements; i++) {
            longFixture.add(i);
        }

        // Get an object fixture
        Collection<@NonNull E> fixture = createElementsWithValues(longFixture);
        IStatistics<@NonNull E> expected = createStatistics();
        IStatistics<@NonNull E> statsA = createStatistics();
        IStatistics<@NonNull E> statsB = createStatistics();

        Collection<@NonNull E> allElements = new ArrayList<>(2 * nbElements);

        fixture.stream().forEach(obj -> {
            // Since we will merge the statistics, the object should be added
            // twice to the expected statistics and the allElements collection
            expected.update(obj);
            expected.update(obj);
            statsA.update(obj);
            statsB.update(obj);
            allElements.add(obj);
            allElements.add(obj);
        });
        // Merge the 2 statistics
        statsA.merge(statsB);
        assertEquals("Merged size", 2 * nbElements, statsA.getNbElements());

        // Compare the results of the merge with the expected results
        validate(expected, statsA);

        // Compare with the offline comparator
        IStatistics<@NonNull E> offline = new OfflineStatisticsCalculator<>(allElements, getMapper());
        validate(offline, statsA);
    }

    /**
     * Test statistics nodes being merged. Two random blocks.
     */
    @Test
    public void testMergeStatisticsRandomNodes() {
        Random rnd = new Random();
        rnd.setSeed(1234);
        // Create 2 fixtures of a random sizes
        int size = 2 + rnd.nextInt(1000);
        int size2 = 2 + rnd.nextInt(1000);

        List<@NonNull Long> longFixture1 = new ArrayList<>(size);
        for (long i = 0; i < size; i++) {
            longFixture1.add(Long.valueOf(Math.abs(rnd.nextInt(1000))));
        }
        Collection<@NonNull E> fixture1 = createElementsWithValues(longFixture1);

        List<@NonNull Long> longFixture2 = new ArrayList<>(size2);
        for (long i = 0; i < size2; i++) {
            longFixture2.add(Long.valueOf(Math.abs(rnd.nextInt(1000))));
        }
        Collection<@NonNull E> fixture2 = createElementsWithValues(longFixture2);

        // Create the statistics objects to merge
        IStatistics<@NonNull E> expected = createStatistics();
        IStatistics<@NonNull E> statsA = createStatistics();
        IStatistics<@NonNull E> statsB = createStatistics();
        Collection<@NonNull E> allElements = new ArrayList<>(size + size2);

        fixture1.stream().forEach(obj -> {
            expected.update(obj);
            statsA.update(obj);
            allElements.add(obj);
        });

        fixture2.stream().forEach(obj -> {
            expected.update(obj);
            statsB.update(obj);
            allElements.add(obj);
        });

        // Make sure statsA and statsB have the expected size
        assertEquals("size of statsA", size, statsA.getNbElements());
        assertEquals("size of statsB", size2, statsB.getNbElements());

        // Merge the 2 statistics
        statsA.merge(statsB);
        assertEquals("Merged size", size + size2, statsA.getNbElements());

        // Compare the results of the merge with the expected results
        validate(expected, statsA);

        // Compare with the offline comparator
        IStatistics<@NonNull E> offline = new OfflineStatisticsCalculator<>(allElements, getMapper());
        validate(offline, statsA);
    }

    /**
     * Test corner cases when merging statistics nodes
     */
    @Test
    public void mergeStatisticsCornerCaseNodesTest() {
        // Create a fixtures of one element
        Collection<@NonNull E> oneFixture = createElementsWithValues(ImmutableList.of(10L));
        // Create a small fixtures of a few elements
        Collection<@NonNull E> smallFixture = createElementsWithValues(ImmutableList.of(0L, 10L, 5L, 12L, 7L, 1234L));

        // Control statistics, not to be modified
        Statistics<E> noElements = createStatistics();
        Statistics<E> oneElement = createStatistics();
        oneElement.update(oneFixture.iterator().next());
        Statistics<E> allElements = createStatistics();
        oneFixture.stream().forEach(obj -> allElements.update(obj));
        smallFixture.stream().forEach(obj -> allElements.update(obj));

        // The statistics objects to test
        Statistics<E> testStats = createStatistics();
        Statistics<E> testStats2 = createStatistics();

        // Test merging empty stats on a non-empty one
        testStats.update(oneFixture.iterator().next());
        testStats.merge(testStats2);
        validate(oneElement, testStats);
        validate(noElements, testStats2);

        // Test merging a one element statistics an empty stats
        testStats2.merge(testStats);
        validate(oneElement, testStats);
        validate(oneElement, testStats2);

        // Test merging stats with only 1 segment
        Statistics<E> testStats3 = createStatistics();
        smallFixture.stream().forEach(obj -> testStats3.update(obj));
        testStats3.merge(testStats2);
        validate(oneElement, testStats2);
        validate(allElements, testStats3);

        // Test merging on stats with only 1 segment
        Statistics<E> testStats4 = createStatistics();
        smallFixture.stream().forEach(obj -> testStats4.update(obj));
        testStats2.merge(testStats4);
        validate(allElements, testStats2);

    }

}
