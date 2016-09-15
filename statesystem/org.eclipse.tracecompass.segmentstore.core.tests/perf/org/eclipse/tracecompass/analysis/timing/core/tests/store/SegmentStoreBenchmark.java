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

import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.segmentstore.core.arraylist.ArrayListStore;
import org.eclipse.tracecompass.internal.segmentstore.core.arraylist.LazyArrayListStore;
import org.eclipse.tracecompass.internal.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

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
public class SegmentStoreBenchmark {

    private ISegmentStore<@NonNull ISegment> fALS = new ArrayListStore<>();
    private ISegmentStore<@NonNull ISegment> fLALS = new LazyArrayListStore<>();
    private ISegmentStore<@NonNull ISegment> fTMS = new TreeMapStore<>();

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
            fuzz[i] = rng.nextInt();
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
            fuzz[i] = rng.nextInt();
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

    private void run(int size, int[] fuzz, String method) {
        long durationA = populate(size, fuzz, fALS);
        long durationL = populate(size, fuzz, fLALS);
        long durationT = populate(size, fuzz, fTMS);
        outputResults(durationA, durationL, durationT, method);
    }

    private static long populate(int size, int[] fuzz, ISegmentStore<@NonNull ISegment> store) {
        store.clear();
        long start = System.nanoTime();
        populate(size, fuzz, store, 1000000);
        long end = System.nanoTime();
        return end - start;
    }

    private void runIterate(int size, int[] fuzz, String method) {
        long durationA = addAndIterate(size, fuzz, fALS);
        long durationL = addAndIterate(size, fuzz, fLALS);
        long durationT = addAndIterate(size, fuzz, fTMS);

        outputResults(durationA, durationL, durationT, method);
    }

    private static long addAndIterate(int size, int[] fuzz, ISegmentStore<@NonNull ISegment> store) {
        long start = System.nanoTime();
        populate(size, fuzz, store);
        iterate(store);
        long end = System.nanoTime();
        return end - start;
    }

    private void runIterateAddIterate(int size, int[] fuzz, String method) {
        long durationA = runIterateAddIterate(size, fuzz, fALS);
        long durationL = runIterateAddIterate(size, fuzz, fLALS);
        long durationT = runIterateAddIterate(size, fuzz, fTMS);
        outputResults(durationA, durationL, durationT, method);
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

    private static void populate(int size, int[] fuzz, ISegmentStore<@NonNull ISegment> store, int count) {
        for (int i = 0; i < count; i++) {
            int start = i + fuzz[i % size];
            store.add(new BasicSegment(start, start + 10));
        }
    }

    private static void outputResults(long durationA, long durationL, long durationT, String method) {
        System.out.println("Time taken for test " + method + "\n ArrayList     " + String.format("%12d", durationA) + "\n LazyArrayList " + String.format("%12d", durationL) + "\n TreeMapStore  " + String.format("%12d", durationT));
    }
}
