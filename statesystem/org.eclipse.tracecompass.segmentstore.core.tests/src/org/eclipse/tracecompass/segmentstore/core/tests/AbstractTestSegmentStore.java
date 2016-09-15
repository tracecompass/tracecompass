/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Unit tests for intersecting elements in an SegmentStore
 *
 * Originally the TreeMapStoreTest, copied for this internal implementation. The
 * test was barely changed as it tests the interface and not the internals.
 *
 * @author Matthew Khouzam
 */
public abstract class AbstractTestSegmentStore {

    /**
     * The segment store
     */
    protected ISegmentStore<@NonNull ISegment> fSegmentStore;

    /**
     * Get the segment store to test
     *
     * @return the segment store
     */
    protected abstract ISegmentStore<@NonNull ISegment> getSegmentStore();

    private static final @NonNull ISegment SEGMENT_2_6 = new BasicSegment(2, 6);
    private static final @NonNull ISegment SEGMENT_4_6 = new BasicSegment(4, 6);
    private static final @NonNull ISegment SEGMENT_4_8 = new BasicSegment(4, 8);
    private static final @NonNull ISegment SEGMENT_6_8 = new BasicSegment(6, 8);
    private static final @NonNull ISegment SEGMENT_10_14 = new BasicSegment(10, 14);
    /**
     * A sample segment list
     */
    protected static final List<@NonNull ISegment> SEGMENTS = ImmutableList.of(SEGMENT_2_6, SEGMENT_4_6, SEGMENT_4_8, SEGMENT_6_8, SEGMENT_10_14);
    private static final List<@NonNull ISegment> REVERSE_SEGMENTS = Lists.reverse(SEGMENTS);

    /**
     * Constructor
     */
    public AbstractTestSegmentStore() {
        super();
    }

    /**
     * Initialize data (test vector) that will be tested
     */
    @Before
    public void setup() {
        fSegmentStore = getSegmentStore();
        for (ISegment segment : SEGMENTS) {
            fSegmentStore.add(segment);
        }
    }

    /**
     * Dispose of the segment store
     */
    @After
    public void teardown() {
        fSegmentStore.dispose();
    }

    /**
     * Testing method size()
     */
    @Test
    public void testSize() {
        assertEquals(SEGMENTS.size(), fSegmentStore.size());
    }

    /**
     * Test the contains() method.
     */
    @Test
    public void testContains() {
        ISegment otherSegment = new BasicSegment(0, 20);

        assertTrue(fSegmentStore.contains(SEGMENT_2_6));
        assertTrue(fSegmentStore.contains(SEGMENT_4_8));
        assertFalse(fSegmentStore.contains(otherSegment));
    }

    /**
     * Test the toArray() method.
     */
    @Test
    public void testToObjectArray() {
        Object[] array = fSegmentStore.toArray();

        assertEquals(SEGMENTS.size(), array.length);
        assertTrue(Arrays.asList(array).containsAll(SEGMENTS));
    }

    /**
     * Test the toArray(T[]) method.
     */
    @Test
    public void testToSpecificArray() {
        ISegment[] array = fSegmentStore.toArray(new ISegment[0]);

        assertEquals(SEGMENTS.size(), array.length);
        assertTrue(Arrays.asList(array).containsAll(SEGMENTS));
    }

    /**
     * Test the toArray(T[]) method with a subtype of ISegment.
     */
    @Test
    public void testToSpecifyArraySubtype() {
        ISegmentStore<@NonNull ISegment> tms2 = getSegmentStore();
        BasicSegment otherSegment = new BasicSegment(2, 6);
        tms2.add(otherSegment);
        BasicSegment[] array = tms2.toArray(new BasicSegment[0]);

        assertEquals(1, array.length);
        assertTrue(Arrays.asList(array).contains(otherSegment));

        tms2.dispose();
    }

    /**
     * Test the iteration order of the complete segment store.
     */
    @Test
    public void testIterationOrder() {
        int i = 0;
        for (ISegment segment : fSegmentStore) {
            assertEquals(SEGMENTS.get(i++), segment);
        }
    }

    /**
     * Test the iteration order when the elements are not inserted in sorted
     * order.
     */
    @Test
    public void testIterationOrderNonSortedInsertion() {
        /* Prepare the segment store, we don't use the 'fixture' in this test */
        ISegmentStore<@NonNull ISegment> store = getSegmentStore();
        for (ISegment segment : REVERSE_SEGMENTS) {
            store.add(checkNotNull(segment));
        }

        /*
         * Test each element one by one, the iteration order should follow the
         * start times, not the insertion order.
         */
        int i = 0;
        for (ISegment segment : store) {
            assertEquals(SEGMENTS.get(i++), segment);
        }

        /* Manually dispose our own store */
        store.dispose();
    }

    /**
     * Testing method
     * {@link ISegmentStore#getIntersectingElements(long start, long end)}
     */
    @Test
    public void testGetIntersectingElementsRange() {

        Iterable<ISegment> intersectingElements;

        /*
         * Range that does not include any segment
         */
        intersectingElements = fSegmentStore.getIntersectingElements(16, 20);
        assertEquals(0, Iterables.size(intersectingElements));

        /*
         * Range start time : Before first segment start time Range end time :
         * After last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(1, 15);
        assertEquals(5, Iterables.size(intersectingElements));

        /*
         * Range start time : On first segment start time Range end time : On
         * last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(2, 14);
        assertEquals(5, Iterables.size(intersectingElements));

        /*
         * Range start time : After one segment start time Range end time :
         * Before one segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(11, 13);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Range start time : On one segment start time Range end time : On one
         * segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(10, 14);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Range start time : On last segment end time Range end time : After
         * last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(14, 18);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Range start time : Before first segment start time Range end time :
         * On first segment start time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(1, 2);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_2_6, Iterables.getOnlyElement(intersectingElements));
    }

    /**
     * Testing method {@link ISegmentStore#getIntersectingElements(long time)}
     */
    @Test
    public void testGetIntersectingElementsTime() {

        Iterable<ISegment> intersectingElements;

        /*
         * Time between segment start time and end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(3);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_2_6, Iterables.getOnlyElement(intersectingElements));

        /*
         * Time on segment start time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(2);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_2_6, Iterables.getOnlyElement(intersectingElements));

        /*
         * Time on segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(14);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Time overlapping many segments
         */
        intersectingElements = fSegmentStore.getIntersectingElements(6);
        assertEquals(4, Iterables.size(intersectingElements));

        /*
         * Time between segments
         */
        intersectingElements = fSegmentStore.getIntersectingElements(9);
        assertEquals(0, Iterables.size(intersectingElements));

        /*
         * Time before all segment start time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(1);
        assertEquals(0, Iterables.size(intersectingElements));

        /*
         * Time after all segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(15);
        assertEquals(0, Iterables.size(intersectingElements));
    }

    /**
     * Testing method {@link ISegmentStore#dispose()}
     */
    @Test
    public void testDispose() {
        ISegmentStore<@NonNull ISegment> store = getSegmentStore();
        store.add(SEGMENT_2_6);
        store.dispose();
        assertEquals(0, store.size());
    }

    /**
     * Test iterating over a store being built.
     *
     * bug 500607
     */
    @Test
    public void testIterator() {
        Collection<@NonNull ISegment> beforeExpected = ImmutableList.of(SEGMENT_2_6);
        Collection<@NonNull ISegment> afterExpected = ImmutableList.of(SEGMENT_2_6, SEGMENT_4_8);
        Collection<@NonNull ISegment> lastExpected = ImmutableList.of(SEGMENT_2_6, SEGMENT_4_8, SEGMENT_6_8);
        Collection<@NonNull ISegment> fixture = new ArrayList<>();
        ISegmentStore<@NonNull ISegment> store = getSegmentStore();

        // Add one segment to the segment store and iterate
        store.add(SEGMENT_2_6);
        for (ISegment item : store) {
            fixture.add(item);
        }
        assertEquals(beforeExpected, fixture);

        // Add a second segment to the store and iterate
        fixture.clear();
        store.add(SEGMENT_4_8);
        for (ISegment item : store) {
            fixture.add(item);
        }
        assertEquals(afterExpected, fixture);

        fixture.clear();
        // Take an iterator
        Iterator<@NonNull ISegment> iter = store.iterator();

        // Add a third segment to the store and iterate
        store.add(SEGMENT_6_8);
        Iterator<@NonNull ISegment> iter2 = store.iterator();
        fixture.clear();

        // Make sure the first iterator take has only 2 elements and the second
        // has 3 elements
        while (iter.hasNext()) {
            fixture.add(iter.next());
        }
        assertEquals(afterExpected, fixture);
        fixture.clear();
        while (iter2.hasNext()) {
            fixture.add(iter2.next());
        }
        assertEquals(lastExpected, fixture);
    }

}