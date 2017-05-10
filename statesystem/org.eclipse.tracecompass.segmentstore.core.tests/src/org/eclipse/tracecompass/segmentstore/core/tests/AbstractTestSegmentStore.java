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
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.datastore.core.interval.IHTIntervalReader;
import org.eclipse.tracecompass.datastore.core.serialization.ISafeByteBufferWriter;
import org.eclipse.tracecompass.datastore.core.serialization.SafeByteBufferFactory;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentComparators;
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
    protected final ISegmentStore<@NonNull TestSegment> fSegmentStore;

    /**
     * Get the segment store to test
     *
     * @return the segment store
     */
    protected abstract ISegmentStore<@NonNull TestSegment> getSegmentStore();

    /**
     * Get the segment store to test with initial data
     *
     * @param data
     *            the data
     *
     * @return the segment store
     */
    protected abstract ISegmentStore<@NonNull TestSegment> getSegmentStore(@NonNull TestSegment @NonNull [] data);

    private static final @NonNull TestSegment SEGMENT_2_6 = new TestSegment(2, 6, "test");
    private static final @NonNull TestSegment SEGMENT_4_6 = new TestSegment(4, 6, "test2");
    private static final @NonNull TestSegment SEGMENT_4_8 = new TestSegment(4, 8, "test3");
    private static final @NonNull TestSegment SEGMENT_6_8 = new TestSegment(6, 8, "test");
    private static final @NonNull TestSegment SEGMENT_10_14 = new TestSegment(10, 14, "test");
    /**
     * A sample segment list
     */
    protected static final List<@NonNull TestSegment> SEGMENTS = ImmutableList.of(SEGMENT_2_6, SEGMENT_4_6, SEGMENT_4_8, SEGMENT_6_8, SEGMENT_10_14);
    private static final List<@NonNull TestSegment> REVERSE_SEGMENTS = Lists.reverse(SEGMENTS);

    /**
     * A test type of segment that can be serialized with the safe buffers. It
     * has an extra payload
     *
     * @author Geneviève Bastien
     */
    protected static final class TestSegment implements ISegment {

        /**
         * The reader for this class
         */
        public static final @NonNull IHTIntervalReader<@NonNull TestSegment> DESERIALISER = buffer -> new TestSegment(buffer.getLong(), buffer.getLong(), buffer.getString());

        /**
        *
        */
        private static final long serialVersionUID = -2242452053089575887L;

        private final long fStart;
        private final long fEnd;
        private final @NonNull String fPayload;

        /**
         * Constructor
         *
         * @param start
         *            Start of this segment
         * @param end
         *            End of this segment
         * @param payload
         *            Payload
         */
        public TestSegment(long start, long end, @NonNull String payload) {
            fStart = start;
            fEnd = end;
            fPayload = payload;
        }

        @Override
        public long getStart() {
            return fStart;
        }

        @Override
        public long getEnd() {
            return fEnd;
        }

        /**
         * Get the payload of this segment
         *
         * @return The payload
         */
        public String getPayload() {
            return fPayload;
        }

        @Override
        public int getSizeOnDisk() {
            return 2 * Long.BYTES + SafeByteBufferFactory.getStringSizeInBuffer(fPayload);
        }

        @Override
        public void writeSegment(@NonNull ISafeByteBufferWriter buffer) {
            buffer.putLong(fStart);
            buffer.putLong(fEnd);
            buffer.putString(fPayload);
        }

    }

    /**
     * Constructor
     */
    public AbstractTestSegmentStore() {
        super();
        fSegmentStore = getSegmentStore();
    }

    /**
     * Asserts that 2 segments are equal. Some backend may not return exactly
     * the same type of segment so the main assert will be false, but the
     * segments are in fact identical
     *
     * @param expected
     *            The expected segment
     * @param actual
     *            The actual segment
     */
    protected void assertSegmentsEqual(ISegment expected, ISegment actual) {
        assertEquals(expected, actual);
    }

    /**
     * Initialize data (test vector) that will be tested
     */
    @Before
    public void setup() {
        for (TestSegment segment : SEGMENTS) {
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
     * Testing isEmpty() method
     */
    @Test
    public void testIsEmpty() {
        assertFalse(fSegmentStore.isEmpty());
        fSegmentStore.clear();
        assertTrue(fSegmentStore.isEmpty());
    }

    /**
     * Testing adding a collection with the addAll method
     */
    @Test
    public void testAddAll() {
        assertFalse(fSegmentStore.isEmpty());
        fSegmentStore.clear();
        assertTrue(fSegmentStore.isEmpty());
        fSegmentStore.addAll(SEGMENTS);
        assertTrue(fSegmentStore.containsAll(SEGMENTS));
    }

    /**
     * Testing "copy" constructor
     */
    @Test
    public void testAddAllConstructor() {
        ISegmentStore<@NonNull TestSegment> other = getSegmentStore(fSegmentStore.toArray(new TestSegment[fSegmentStore.size()]));
        assertTrue(fSegmentStore.containsAll(other));
        assertTrue(other.containsAll(fSegmentStore));
    }

    /**
     * Testing "copy" constructor out of order
     */
    @Test
    public void testAddAllConstructorOutOfOrder() {
        ISegmentStore<@NonNull TestSegment> other = getSegmentStore(REVERSE_SEGMENTS.toArray(new TestSegment[fSegmentStore.size()]));
        assertTrue(fSegmentStore.containsAll(other));
        assertTrue(other.containsAll(fSegmentStore));
    }

    /**
     * Testing adding an out of order collection with the addAll method
     */
    @Test
    public void testAddAllOutOfOrder() {
        assertFalse(fSegmentStore.isEmpty());
        fSegmentStore.clear();
        assertTrue(fSegmentStore.isEmpty());
        fSegmentStore.addAll(REVERSE_SEGMENTS);
        assertTrue(fSegmentStore.containsAll(SEGMENTS));
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
     * Test containsAll() method
     */
    public void testContainsAll() {
        ISegmentStore<@NonNull TestSegment> store = getSegmentStore();

        store.add(SEGMENT_2_6);
        assertTrue(store.containsAll(Collections.emptyList()));
        assertTrue(store.containsAll(Collections.singleton(SEGMENT_2_6)));
        assertFalse(store.containsAll(Collections.singleton(SEGMENT_4_6)));
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
        ISegmentStore<@NonNull TestSegment> tms2 = getSegmentStore();
        TestSegment otherSegment = new TestSegment(2, 6, "test");
        tms2.add(otherSegment);
        TestSegment[] array = tms2.toArray(new TestSegment[0]);

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
            assertSegmentsEqual(SEGMENTS.get(i++), segment);
        }
    }

    /**
     * Test the iteration order when the elements are not inserted in sorted
     * order.
     */
    @Test
    public void testIterationOrderNonSortedInsertion() {
        /* Prepare the segment store, we don't use the 'fixture' in this test */
        ISegmentStore<@NonNull TestSegment> store = getSegmentStore();
        for (TestSegment segment : REVERSE_SEGMENTS) {
            store.add(checkNotNull(segment));
        }

        /*
         * Test each element one by one, the iteration order should follow the
         * start times, not the insertion order.
         */
        int i = 0;
        for (TestSegment segment : store) {
            assertSegmentsEqual(SEGMENTS.get(i++), segment);
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

        Iterable<TestSegment> intersectingElements;

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
        assertSegmentsEqual(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Range start time : On one segment start time Range end time : On one
         * segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(10, 14);
        assertEquals(1, Iterables.size(intersectingElements));
        assertSegmentsEqual(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Range start time : On last segment end time Range end time : After
         * last segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(14, 18);
        assertEquals(1, Iterables.size(intersectingElements));
        assertSegmentsEqual(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

        /*
         * Range start time : Before first segment start time Range end time :
         * On first segment start time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(1, 2);
        assertEquals(1, Iterables.size(intersectingElements));
        assertSegmentsEqual(SEGMENT_2_6, Iterables.getOnlyElement(intersectingElements));
    }

    /**
     * Testing method {@link ISegmentStore#getIntersectingElements(long time)}
     */
    @Test
    public void testGetIntersectingElementsTime() {

        Iterable<TestSegment> intersectingElements;

        /*
         * Time between segment start time and end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(3);
        assertEquals(1, Iterables.size(intersectingElements));
        assertSegmentsEqual(SEGMENT_2_6, Iterables.getOnlyElement(intersectingElements));

        /*
         * Time on segment start time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(2);
        assertEquals(1, Iterables.size(intersectingElements));
        assertSegmentsEqual(SEGMENT_2_6, Iterables.getOnlyElement(intersectingElements));

        /*
         * Time on segment end time
         */
        intersectingElements = fSegmentStore.getIntersectingElements(14);
        assertEquals(1, Iterables.size(intersectingElements));
        assertSegmentsEqual(SEGMENT_10_14, Iterables.getOnlyElement(intersectingElements));

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
        ISegmentStore<@NonNull TestSegment> store = getSegmentStore();
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
        Collection<@NonNull TestSegment> beforeExpected = ImmutableList.of(SEGMENT_2_6);
        Collection<@NonNull TestSegment> afterExpected = ImmutableList.of(SEGMENT_2_6, SEGMENT_4_8);
        Collection<@NonNull TestSegment> lastExpected = ImmutableList.of(SEGMENT_2_6, SEGMENT_4_8, SEGMENT_6_8);
        Collection<@NonNull TestSegment> fixture = new ArrayList<>();
        ISegmentStore<@NonNull TestSegment> store = getSegmentStore();

        // Add one segment to the segment store and iterate
        store.add(SEGMENT_2_6);
        for (TestSegment item : store) {
            fixture.add(item);
        }
        assertEquals(beforeExpected, fixture);

        // Add a second segment to the store and iterate
        fixture.clear();
        store.add(SEGMENT_4_8);
        for (TestSegment item : store) {
            fixture.add(item);
        }
        assertEquals(afterExpected, fixture);

        fixture.clear();
        // Take an iterator
        Iterator<@NonNull TestSegment> iter = store.iterator();

        // Add a third segment to the store and iterate
        store.add(SEGMENT_6_8);
        Iterator<@NonNull TestSegment> iter2 = store.iterator();
        fixture.clear();

        // Make sure the first iterator take has at least 2 elements (depends on
        // implementation) and the second has 3 elements
        while (iter.hasNext()) {
            fixture.add(iter.next());
        }
        assertTrue(fixture.size() >= 2);
        fixture.clear();
        while (iter2.hasNext()) {
            fixture.add(iter2.next());
        }
        assertEquals(lastExpected, fixture);
    }

    /**
     * Test to check ordered iterators
     */
    @Test
    public void testSortedIterator() {
        List<@NonNull Comparator<ISegment>> comparators = new LinkedList<>();
        comparators.add(SegmentComparators.INTERVAL_END_COMPARATOR);
        comparators.add(NonNullUtils.checkNotNull(SegmentComparators.INTERVAL_END_COMPARATOR.reversed()));
        comparators.add(SegmentComparators.INTERVAL_START_COMPARATOR);
        comparators.add(NonNullUtils.checkNotNull(SegmentComparators.INTERVAL_START_COMPARATOR.reversed()));
        comparators.add(SegmentComparators.INTERVAL_LENGTH_COMPARATOR);
        comparators.add(NonNullUtils.checkNotNull(SegmentComparators.INTERVAL_LENGTH_COMPARATOR.reversed()));

        Iterable<TestSegment> iterable;
        for (Comparator<ISegment> comparator : comparators) {
            iterable = fSegmentStore.iterator(comparator);
            verifySortedIterable(iterable, 5, comparator);
            iterable = fSegmentStore.getIntersectingElements(5, comparator);
            verifySortedIterable(iterable, 3, comparator);
            iterable = fSegmentStore.getIntersectingElements(7, 14, comparator);
            verifySortedIterable(iterable, 3, comparator);
        }
    }

    private static void verifySortedIterable(Iterable<TestSegment> iterable, int expectedSize, Comparator<ISegment> comparator) {
        // check its size
        assertEquals(expectedSize, Iterables.size(iterable));
        Iterator<TestSegment> iterator = iterable.iterator();
        // check the order
        ISegment prev, current = iterator.next();
        while (iterator.hasNext()) {
            prev = current;
            current = iterator.next();
            assertTrue(comparator.compare(prev, current) <= 0);
        }
    }

    /**
     * Test retainAll() contract
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAll() {
        ISegmentStore<@NonNull TestSegment> store = getSegmentStore();

        store.add(SEGMENT_2_6);
        store.retainAll(Collections.emptyList());
    }

    /**
     * Test remove() contract
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemove() {
        ISegmentStore<@NonNull TestSegment> store = getSegmentStore();

        store.add(SEGMENT_2_6);
        store.remove(SEGMENT_2_6);
    }

    /**
     * Test removeAll() contract
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAll() {
        ISegmentStore<@NonNull TestSegment> store = getSegmentStore();

        store.add(SEGMENT_2_6);
        store.removeAll(Collections.emptyList());
    }
}