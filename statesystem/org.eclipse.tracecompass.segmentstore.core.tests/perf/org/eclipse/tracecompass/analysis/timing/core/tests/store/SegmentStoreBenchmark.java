/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.store;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.test.performance.Performance;
import org.eclipse.test.performance.PerformanceMeter;
import org.eclipse.tracecompass.internal.segmentstore.core.arraylist.ArrayListStore;
import org.eclipse.tracecompass.internal.segmentstore.core.arraylist.LazyArrayListStore;
import org.eclipse.tracecompass.internal.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
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
public class SegmentStoreBenchmark {

    private static final int DEFAULT_SAMPLE = 1000;
    private static final int DEFAULT_LOOP_COUNT = 10;

    private final ISegmentStore<@NonNull ISegment> fSegStore;
    private final String fName;
    private final Performance fPerf;

    /**
     * @return The arrays of parameters
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getParameters() {
        return Arrays.asList(new Object[][] {
                { "Array list store", new ArrayListStore<>() },
                { "Lazy array list store", new LazyArrayListStore<>() },
                { "Treemap store", new TreeMapStore<>() },
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
    public SegmentStoreBenchmark(String name, ISegmentStore<@NonNull ISegment> segStore) {
        fSegStore = segStore;
        fName = name;
        fPerf = Performance.getDefault();
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

            System.gc();
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

            System.gc();
            pMinsertion.start();
            populate(size, fuzz, fSegStore, 0, getSegmentStoreSize());
            pMinsertion.stop();

            System.gc();
            pMiterateStart.start();
            sortedIterate(fSegStore, SegmentComparators.INTERVAL_START_COMPARATOR);
            pMiterateStart.stop();

            System.gc();
            pMiterateEnd.start();
            sortedIterate(fSegStore, SegmentComparators.INTERVAL_END_COMPARATOR);
            pMiterateEnd.stop();

            System.gc();
            pMiterateDuration.start();
            sortedIterate(fSegStore, SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
            pMiterateDuration.stop();
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

            System.gc();
            pMinsertion1.start();
            populate(size, fuzz, fSegStore, 0, getSegmentStoreSize() / 2);
            pMinsertion1.stop();

            System.gc();
            pMiterate1.start();
            iterate(fSegStore);
            pMiterate1.stop();

            System.gc();
            pMinsertion2.start();
            populate(size, fuzz, fSegStore, getSegmentStoreSize() / 2 + 1, getSegmentStoreSize());
            pMinsertion2.stop();

            System.gc();
            pMiterate2.start();
            iterate(fSegStore);
            pMiterate2.stop();
        }

        pMinsertion1.commit();
        pMiterate1.commit();
        pMinsertion2.commit();
        pMiterate2.commit();
    }

    private static Object iterate(Iterable<@NonNull ISegment> store) {
        Object shutupCompilerWarnings = null;
        for (ISegment elem : store) {
            shutupCompilerWarnings = elem;
        }
        return shutupCompilerWarnings;
    }

    private static void sortedIterate(ISegmentStore<@NonNull ISegment> store, @NonNull Comparator<@NonNull ISegment> order) {
        Iterable<@NonNull ISegment> iterable = store.iterator(order);
        iterate(iterable);
    }

    private static void populate(int size, int[] fuzz, ISegmentStore<@NonNull ISegment> store, long low, long high) {
        for (long i = low; i < high; i++) {
            long start = i + fuzz[(int) (i % size)];
            store.add(new BasicSegment(start, start + 10));
        }
    }
}