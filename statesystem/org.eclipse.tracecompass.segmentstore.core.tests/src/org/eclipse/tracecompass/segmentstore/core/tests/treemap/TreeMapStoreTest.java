/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.treemap;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

/**
 * Unit tests for intersecting elements in a TreeMapStore
 *
 * @author France Lapointe Nguyen
 */
public class TreeMapStoreTest {

    private TreeMapStore<@NonNull ISegment> fSegmentStore;

    private static final ISegment SEGMENT_2_6 = new BasicSegment(2, 6);
    private static final ISegment SEGMENT_4_6 = new BasicSegment(4, 6);
    private static final ISegment SEGMENT_4_8 = new BasicSegment(4, 8);
    private static final ISegment SEGMENT_6_8 = new BasicSegment(6, 8);
    private static final ISegment SEGMENT_10_14 = new BasicSegment(10, 14);

    private static final List<ISegment> SEGMENTS = ImmutableList.of(SEGMENT_2_6, SEGMENT_4_6, SEGMENT_4_8, SEGMENT_6_8, SEGMENT_10_14);
    private static final List<ISegment> REVERSE_SEGMENTS = Lists.reverse(SEGMENTS);

    /**
     * Initialize data (test vector) that will be tested
     */
    @Before
    public void setup() {
        fSegmentStore = new TreeMapStore<>();
        for (ISegment segment : SEGMENTS) {
            fSegmentStore.add(checkNotNull(segment));
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
        TreeMapStore<@NonNull BasicSegment> tms2 = new TreeMapStore<>();
        BasicSegment otherSegment = new BasicSegment(2, 6);
        tms2.add(otherSegment);
        BasicSegment[] array = tms2.toArray(new BasicSegment[0]);

        assertEquals(1, array.length);
        assertTrue(Arrays.asList(array).contains(otherSegment));

        tms2.dispose();
    }

    /**
     * Try adding duplicate elements, they should be ignored
     */
    @Test
    public void testNoDuplicateElements() {
        for (ISegment segment : SEGMENTS) {
            boolean ret = fSegmentStore.add(new BasicSegment(segment.getStart(), segment.getEnd()));
            assertFalse(ret);
        }
        assertEquals(SEGMENTS.size(), fSegmentStore.size());
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
        TreeMapStore<@NonNull ISegment> store = new TreeMapStore<>();
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
     * Testing method getIntersectingElements(long start, long end)
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
         * Range start time : Before first segment start time
         * Range end time : After last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(1, 15);
        assertEquals(5, Iterables.size(intersectingElements));

        /*
         * Range start time : On first segment start time
         * Range end time : On last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(2, 14);
        assertEquals(5, Iterables.size(intersectingElements));

        /*
         * Range start time : After one segment start time
         * Range end time : Before one segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(11, 13);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Range start time : On one segment start time
         * Range end time : On one segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(10, 14);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Range start time : On last segment end time
         * Range end time : After last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(14, 18);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Range start time : Before first segment start time
         * Range end time : On first segment start time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(1, 2);
        assertEquals(1, Iterables.size(intersectingElements));
        assertEquals(SEGMENT_2_6, Iterables.getOnlyElement(intersectingElements));
    }

    /**
     * Testing method getIntersectingElements(long start, long end)
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
     * Testing method getIntersectingElements(long start, long end)
     */
    @Test
    public void testDispose() {
        TreeMapStore<@NonNull ISegment> store = new TreeMapStore<>();
        store.add(SEGMENT_2_6);
        store.dispose();
        assertEquals(0, store.size());
    }
}