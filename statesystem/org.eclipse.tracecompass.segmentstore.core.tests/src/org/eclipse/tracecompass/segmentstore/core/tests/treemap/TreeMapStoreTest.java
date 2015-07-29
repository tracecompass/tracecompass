/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.treemap;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

/**
 * Unit tests for intersecting elements in a TreeMapStore
 *
 * @author France Lapointe Nguyen
 */
public class TreeMapStoreTest {

    private TreeMapStore<ISegment> fSegmentStore;

    private static final ISegment SEGMENT_2_4 = new BasicSegment(2, 4);
    private static final ISegment SEGMENT_6_8 = new BasicSegment(6, 8);
    private static final ISegment SEGMENT_10_14 = new BasicSegment(10, 14);

    private static final List<ISegment> SEGMENTS = ImmutableList.of(SEGMENT_2_4, SEGMENT_6_8, SEGMENT_10_14);

    /**
     * Initialize data (test vector) that will be tested
     */
    @Before
    public void setup() {
        fSegmentStore = new TreeMapStore<>();
        for (int i = 0; i < SEGMENTS.size(); i++) {
            fSegmentStore.addElement(NonNullUtils.checkNotNull(SEGMENTS.get(i)));
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
     * Testing method getNbElements
     */
    @Test
    public void testGetNbElements() {
        assertEquals(SEGMENTS.size(), fSegmentStore.getNbElements());
    }

    /**
     * Testing method getElementAtIndex
     */
    @Test
    public void testGetElementAtIndex() {
        for (int i = 0; i < SEGMENTS.size(); i++) {
            assertEquals(SEGMENTS.get(i), fSegmentStore.getElementAtIndex(i));
        }
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
         * Range start time : Before first segment start time Range end time :
         * After last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(1, 15);
        assertEquals(3, Iterables.size(intersectingElements));

        /*
         * Range start time : On first segment start time Range end time : On
         * last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(2, 14);
        assertEquals(3, Iterables.size(intersectingElements));

        /*
         * Range start time : After one segment start time Range end time :
         * Before one segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(11, 13);
        assertEquals(1, Iterables.size(intersectingElements));
        assert (SEGMENT_10_14.equals(intersectingElements));

        /*
         * Range start time : On one segment start time Range end time : On one
         * segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(6, 8);
        assertEquals(1, Iterables.size(intersectingElements));
        assert (SEGMENT_6_8.equals(intersectingElements));

        /*
         * Range start time : On last segment end time Range end time : After
         * last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(14, 18);
        assertEquals(1, Iterables.size(intersectingElements));
        assert (SEGMENT_10_14.equals(intersectingElements));

        /*
         * Range start time : Before first segment start time Range end time :
         * On first segment start time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(1, 2);
        assertEquals(1, Iterables.size(intersectingElements));
        assert (SEGMENT_2_4.equals(intersectingElements));
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
        assert (SEGMENT_2_4.equals(intersectingElements));

        /*
         * Time on segment start time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(2);
        assertEquals(1, Iterables.size(intersectingElements));
        assert (SEGMENT_2_4.equals(intersectingElements));

        /*
         * Time on segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(4);
        assertEquals(1, Iterables.size(intersectingElements));
        assert (SEGMENT_2_4.equals(intersectingElements));

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
        TreeMapStore<ISegment> store = new TreeMapStore<>();
        store.addElement(NonNullUtils.checkNotNull(SEGMENT_2_4));
        store.dispose();
        assertEquals(0, store.getNbElements());
    }
}