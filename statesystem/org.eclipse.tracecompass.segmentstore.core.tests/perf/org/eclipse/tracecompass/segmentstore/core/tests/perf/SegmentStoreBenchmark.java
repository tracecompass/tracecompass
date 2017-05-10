/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.perf;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.internal.segmentstore.core.arraylist.ArrayListStore;
import org.eclipse.tracecompass.internal.segmentstore.core.arraylist.LazyArrayListStore;
import org.eclipse.tracecompass.internal.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
import org.eclipse.tracecompass.segmentstore.core.tests.historytree.HistoryTreeSegmentStoreStub;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Segmentstore benchmarks, tests the performance for loads and reads.
 *
 * NOTE : Do not add this to isTracecompassFastYet, it is not information that
 * is interesting for users, it is for developers.
 *
 * @category benchmark
 *
 * @author Matthew Khouzam
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
@NonNullByDefault
public class SegmentStoreBenchmark {

    private static final int DEFAULT_SAMPLE = 1000;
    private static final int DEFAULT_LOOP_COUNT = 10;

    private final ISegmentStore<@NonNull BasicSegment> fSegStore;
    private final String fName;
    private final Performance fPerf;

    /**
     * @return The arrays of parameters
     * @throws IOException
     *             Exceptions thrown when setting the on-disk backends
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() throws IOException {
        return Arrays.asList(new Object[][] {
                { "Array list store", new ArrayListStore<>() },
                { "Lazy array list store", new LazyArrayListStore<>() },
                { "Treemap store", new TreeMapStore<>() },
                { "HT store", new HistoryTreeSegmentStoreStub<>(NonNullUtils.checkNotNull(Files.createTempFile("tmpSegStore", null)), 0, BasicSegment.BASIC_SEGMENT_READ_FACTORY) },
        });
    }

    /**
     * Constructor
     *
     * @param name
     *            The name of this test
     * @param segStore
     *            The segment store to fill for the benchmarks
     */
    public SegmentStoreBenchmark(String name, ISegmentStore<@NonNull BasicSegment> segStore) {
        fSegStore = segStore;
        fName = name;
        fPerf = NonNullUtils.checkNotNull(Performance.getDefault());
    }

    /**
     * Get the number of segments to add to the segment store
     *
     * @return The number of segments to add to the segment store
     */
    protected long getSegmentStoreSize() {
        return 1000000;
    }

    /**
     * Add elements in order
     */
    @Test
    public void test1Ordered() {
        PerformanceMeter pMorderedInsertion = fPerf.createPerformanceMeter("Ordered Insertion: " + fName);
        int size = 1;
        int[] fuzz = { 0 };
        for (int i = 0; i < DEFAULT_LOOP_COUNT; i++) {
            fSegStore.clear();

            pMorderedInsertion.start();
            populate(size, fuzz, fSegStore, 0, getSegmentStoreSize());
            pMorderedInsertion.stop();
        }
        pMorderedInsertion.commit();
    }

    /**
     * Add elements almost in order, this represents a typical degenerate use
     * case. Then run all the iteration queries on the segment store.
     */
    @Test
    public void test2FuzzyOrder() {
        int[] fuzz = fuzzyArray(DEFAULT_SAMPLE);
        fullTest(DEFAULT_SAMPLE, fuzz, "Fuzzy");
    }

    /**
     * Test adding elements in a random order, this is an atypical degenerate
     * use case. Then run all the iteration queries on the segment store.
     */
    @Test
    public void test3Random() {
        int[] fuzz = randomArray(DEFAULT_SAMPLE);
        fullTest(DEFAULT_SAMPLE, fuzz, "Random");
    }

    /**
     * Add elements almost in order, this represents a typical degenerate use
     * case, and iterate while building then when done.
     */
    @Test
    public void test4FuzzyInsertIterTwice() {
        int[] fuzz = fuzzyArray(DEFAULT_SAMPLE);
        insertIterTwice(DEFAULT_SAMPLE, fuzz, "Fuzzy");
    }

    /**
     * Test adding elements in a random order then iterate over the list then
     * add more then iterate again, this is an atypical degenerate use case.
     */
    @Test
    public void test5RandomInsertIterTwice() {
        int[] fuzz = randomArray(DEFAULT_SAMPLE);
        insertIterTwice(DEFAULT_SAMPLE, fuzz, "Random");
    }

    private static int[] randomArray(int size) {
        int[] fuzz = new int[DEFAULT_SAMPLE];
        Random rng = new Random(10);
        for (int i = 0; i < DEFAULT_SAMPLE; i++) {
            fuzz[i] = Math.abs(rng.nextInt());
        }
        return fuzz;
    }

    private static int[] fuzzyArray(int size) {
        int[] fuzz = new int[DEFAULT_SAMPLE];
        Random rng = new Random(10);
        for (int i = 0; i < DEFAULT_SAMPLE; i++) {
            fuzz[i] = rng.nextInt(DEFAULT_SAMPLE);
        }
        return fuzz;
    }

    private void fullTest(int size, int[] fuzz, String distributionName) {
        PerformanceMeter pMinsertion = fPerf.createPerformanceMeter(distributionName + " Insertion: " + fName);
        PerformanceMeter pMiterateStart = fPerf.createPerformanceMeter(distributionName + " Iterate sorted by start: " + fName);
        PerformanceMeter pMiterateEnd = fPerf.createPerformanceMeter(distributionName + " Iterate sorted by end: " + fName);
        PerformanceMeter pMiterateDuration = fPerf.createPerformanceMeter(distributionName + " Iterate sorted by length: " + fName);

        for (int i = 0; i < DEFAULT_LOOP_COUNT; i++) {
            fSegStore.clear();

            pMinsertion.start();
            populate(size, fuzz, fSegStore, 0, getSegmentStoreSize());
            pMinsertion.stop();

            pMiterateStart.start();
            int count = sortedIterate(fSegStore, SegmentComparators.INTERVAL_START_COMPARATOR);
            pMiterateStart.stop();
            assertEquals(fSegStore.size(), count);

            pMiterateEnd.start();
            count = sortedIterate(fSegStore, SegmentComparators.INTERVAL_END_COMPARATOR);
            pMiterateEnd.stop();
            assertEquals(fSegStore.size(), count);

            pMiterateDuration.start();
            count = sortedIterate(fSegStore, SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
            pMiterateDuration.stop();
            assertEquals(fSegStore.size(), count);
        }

        pMinsertion.commit();
        pMiterateStart.commit();
        pMiterateEnd.commit();
        pMiterateDuration.commit();
    }

    private void insertIterTwice(int size, int[] fuzz, String distributionName) {
        PerformanceMeter pMinsertion1 = fPerf.createPerformanceMeter(distributionName + " First Insertion: " + fName);
        PerformanceMeter pMiterate1 = fPerf.createPerformanceMeter(distributionName + " First Iteration: " + fName);
        PerformanceMeter pMinsertion2 = fPerf.createPerformanceMeter(distributionName + " Second Insertion: " + fName);
        PerformanceMeter pMiterate2 = fPerf.createPerformanceMeter(distributionName + " Second Iteration: " + fName);

        for (int i = 0; i < DEFAULT_LOOP_COUNT; i++) {
            fSegStore.clear();

            pMinsertion1.start();
            populate(size, fuzz, fSegStore, 0, getSegmentStoreSize() / 2);
            pMinsertion1.stop();

            pMiterate1.start();
            int count = iterate(fSegStore);
            pMiterate1.stop();
            assertEquals(fSegStore.size(), count);

            pMinsertion2.start();
            populate(size, fuzz, fSegStore, getSegmentStoreSize() / 2 + 1, getSegmentStoreSize());
            pMinsertion2.stop();

            pMiterate2.start();
            count = iterate(fSegStore);
            pMiterate2.stop();
            assertEquals(fSegStore.size(), count);
        }

        pMinsertion1.commit();
        pMiterate1.commit();
        pMinsertion2.commit();
        pMiterate2.commit();
    }

    private static int iterate(Iterable<@NonNull BasicSegment> store) {
        int count = 0;
        Iterator<@NonNull BasicSegment> iterator = store.iterator();
        while (iterator.hasNext()) {
            count++;
            iterator.next();
        }
        return count;
    }

    private static int sortedIterate(ISegmentStore<@NonNull BasicSegment> store, Comparator<ISegment> order) {
        Iterable<@NonNull BasicSegment> iterable = store.iterator(order);
        return iterate(iterable);
    }

    private static void populate(int size, int[] fuzz, ISegmentStore<@NonNull BasicSegment> store, long low, long high) {
        for (long i = low; i < high; i++) {
            long start = i + fuzz[(int) (i % size)];
            store.add(new BasicSegment(start, start + 10));
        }
    }
}