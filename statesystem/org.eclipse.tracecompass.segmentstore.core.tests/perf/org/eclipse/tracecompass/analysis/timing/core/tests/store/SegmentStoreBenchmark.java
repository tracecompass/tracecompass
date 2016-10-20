/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.store;

import static org.junit.Assert.assertNotNull;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
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
 * is interesting for users, it is for developpers.
 *
 * @category benchmark
 *
 * @author Matthew Khouzam
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(Parameterized.class)
public class SegmentStoreBenchmark {

    private final ISegmentStore<@NonNull ISegment> fSegStore;
    private final String fName;
    private static final Format FORMAT = new DecimalFormat("###,###.##"); //$NON-NLS-1$

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
    public void test1AddInOrder() {
        int size = 1;
        int[] fuzz = { 0 };
        run(size, fuzz, new Object() {
        }.getClass().getEnclosingMethod().getName());
    }

    /**
     * Add elements almost in order, this represents a typical degenerate use
     * case.
     */
    @Test
    public void test2AddFuzzyOrder() {
        int size = 1000;
        int[] fuzz = new int[size];
        Random rng = new Random(10);
        for (int i = 0; i < size; i++) {
            fuzz[i] = rng.nextInt(1000);
        }
        String name = new Object() {
        }.getClass().getEnclosingMethod().getName();
        assertNotNull(name);
        run(size, fuzz, name);
    }

    /**
     * Add elements almost in order, this represents a typical degenerate use
     * case, then iterate over the list.
     */
    @Test
    public void test3AddFuzzyOrderThenIterate() {
        int size = 1000;
        int[] fuzz = new int[size];
        Random rng = new Random(10);
        for (int i = 0; i < size; i++) {
            fuzz[i] = rng.nextInt(1000);
        }
        String name = new Object() {
        }.getClass().getEnclosingMethod().getName();
        assertNotNull(name);
        runIterate(size, fuzz, name);
    }

    /**
     * Add elements almost in order, this represents a typical degenerate use
     * case, and iterate while building then when done.
     */
    @Test
    public void test4AddFuzzyOrderThenIterateThenAddThenIterate() {
        int size = 1000;
        int[] fuzz = new int[size];
        Random rng = new Random(10);
        for (int i = 0; i < size; i++) {
            fuzz[i] = rng.nextInt(1000);
        }
        String name = new Object() {
        }.getClass().getEnclosingMethod().getName();
        assertNotNull(name);
        runIterateAddIterate(size, fuzz, name);
    }

    /**
     * Test adding elements in a random order, this is an atypical degenerate
     * use case.
     */
    @Test
    public void test5AddRandomOrder() {
        int size = 1000;
        int[] fuzz = new int[size];
        Random rng = new Random(10);
        for (int i = 0; i < size; i++) {
            fuzz[i] = Math.abs(rng.nextInt());
        }
        String name = new Object() {
        }.getClass().getEnclosingMethod().getName();
        assertNotNull(name);
        runIterate(size, fuzz, name);
    }

    /**
     * Test adding elements in a random order then iterate over the list, this
     * is an atypical degenerate use case.
     */
    @Test
    public void test6AddRandomOrderThenIterate() {
        int size = 1000;
        int[] fuzz = new int[size];
        Random rng = new Random(10);
        for (int i = 0; i < size; i++) {
            fuzz[i] = Math.abs(rng.nextInt());
        }
        String name = new Object() {
        }.getClass().getEnclosingMethod().getName();
        assertNotNull(name);
        runIterate(size, fuzz, name);
    }

    /**
     * Test adding elements in a random order then iterate over the list then
     * add more then iterate again, this is an atypical degenerate use case.
     */
    @Test
    public void test7AddRandomOrderThenIterateThenAddThenIterate() {
        int size = 1000;
        int[] fuzz = new int[size];
        Random rng = new Random(10);
        for (int i = 0; i < size; i++) {
            fuzz[i] = rng.nextInt(1000);
        }
        String name = new Object() {
        }.getClass().getEnclosingMethod().getName();
        assertNotNull(name);
        runIterateAddIterate(size, fuzz, name);
    }

    /**
     * Test adding elements in a random order then iterate over the list, this
     * is an atypical degenerate use case.
     */
    @Test
    public void test8AddFuzzyOrderThenIterateByStartTime() {
        int size = 1000;
        int[] fuzz = new int[size];
        Random rng = new Random(10);
        for (int i = 0; i < size; i++) {
            fuzz[i] = rng.nextInt(1000);
        }
        String name = new Object() {
        }.getClass().getEnclosingMethod().getName();
        assertNotNull(name);
        runIterateCompare(size, fuzz, name, SegmentComparators.INTERVAL_START_COMPARATOR);
    }

    /**
     * Test adding elements in a random order then iterate over the list, this
     * is an atypical degenerate use case.
     */
    @Test
    public void test9AddFuzzyOrderThenIterateByEndTime() {
        int size = 1000;
        int[] fuzz = new int[size];
        Random rng = new Random(10);
        for (int i = 0; i < size; i++) {
            fuzz[i] = rng.nextInt(1000);
        }
        String name = new Object() {
        }.getClass().getEnclosingMethod().getName();
        assertNotNull(name);
        runIterateCompare(size, fuzz, name, SegmentComparators.INTERVAL_END_COMPARATOR);
    }

    /**
     * Test adding elements in a random order then iterate over the list, this
     * is an atypical degenerate use case.
     */
    @Test
    public void testAAddFuzzyOrderThenIterateByDuration() {
        int size = 1000;
        int[] fuzz = new int[size];
        Random rng = new Random(10);
        for (int i = 0; i < size; i++) {
            fuzz[i] = rng.nextInt(1000);
        }
        String name = new Object() {
        }.getClass().getEnclosingMethod().getName();
        assertNotNull(name);
        runIterateCompare(size, fuzz, name, SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
    }

    private void runIterateCompare(int size, int[] fuzz, String method, @NonNull Comparator<@NonNull ISegment> comparator) {
        long start = System.nanoTime();
        populate(size, fuzz, fSegStore);
        long startTime = fuzz[0];
        long endTime = fSegStore.size() - 1 + fuzz[fSegStore.size() % size];
        iterateCompare(startTime, endTime, fSegStore, comparator);
        long end = System.nanoTime();
        long duration = end - start;
        outputResults(duration, method);
    }

    private void run(int size, int[] fuzz, String method) {
        long duration = populate(size, fuzz, fSegStore);
        outputResults(duration, method);
    }


    private long populate(int size, int[] fuzz, ISegmentStore<@NonNull ISegment> store) {
        store.clear();
        long start = System.nanoTime();
        populate(size, fuzz, store, getSegmentStoreSize());
        long end = System.nanoTime();
        return end - start;
    }

    private void runIterate(int size, int[] fuzz, String method) {
        long duration = addAndIterate(size, fuzz, fSegStore);

        outputResults(duration, method);
    }

    private long addAndIterate(int size, int[] fuzz, ISegmentStore<@NonNull ISegment> store) {
        long start = System.nanoTime();
        populate(size, fuzz, store);
        iterate(store);
        long end = System.nanoTime();
        return end - start;
    }

    private void runIterateAddIterate(int size, int[] fuzz, String method) {
        long duration = runIterateAddIterate(size, fuzz, fSegStore);
        outputResults(duration, method);
    }

    private static long runIterateAddIterate(int size, int[] fuzz, ISegmentStore<@NonNull ISegment> store) {
        store.clear();
        long start = System.nanoTime();
        for (int i = 0; i < 50000; i++) {
            long startTime = i + fuzz[i % size];
            store.add(new BasicSegment(startTime, startTime + 10));
        }
        iterate(store);
        for (int i = 50000; i < 100000; i++) {
            long startTime = i + fuzz[i % size];
            store.add(new BasicSegment(startTime, startTime + 10));
        }
        iterate(store);
        long end = System.nanoTime();
        return end - start;
    }

    private static Object iterate(ISegmentStore<@NonNull ISegment> store) {
        Object shutupCompilerWarnings = null;
        for (ISegment elem : store) {
            shutupCompilerWarnings = elem;
        }
        return shutupCompilerWarnings;
    }

    private static Object iterateCompare(long startTime, long endTime, ISegmentStore<@NonNull ISegment> store, @NonNull Comparator<@NonNull ISegment> comparator) {
        Object shutupCompilerWarnings = null;
        for (ISegment elem : store.getIntersectingElements(startTime, endTime, comparator)) {
            shutupCompilerWarnings = elem;
        }
        return shutupCompilerWarnings;
    }

    private static void populate(int size, int[] fuzz, ISegmentStore<@NonNull ISegment> store, long count) {
        for (int i = 0; i < count; i++) {
            long start = (long) i + fuzz[i % size];
            store.add(new BasicSegment(start, start + 10));
        }
    }

    private void outputResults(long duration, String method) {
        System.out.println(fName + ": Time taken for test " + method + ": " + FORMAT.format(duration));
    }
}
