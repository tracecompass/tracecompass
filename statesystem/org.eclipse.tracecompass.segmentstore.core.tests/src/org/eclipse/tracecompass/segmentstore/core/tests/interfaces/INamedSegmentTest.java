/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests.interfaces;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.Comparator;
import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory.SegmentStoreType;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.INamedSegment;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.SegmentAspects;
import org.eclipse.tracecompass.segmentstore.core.segment.interfaces.SegmentTypeComparators;
import org.junit.Test;

/**
 * Test the INamedSegment interface
 *
 * @author Geneviève Bastien
 */
@NonNullByDefault
public class INamedSegmentTest {

    // 5 test segments
    private static final BasicSegment BASE_SEGMENT = new BasicSegment(10, 12);
    private static final NamedSegment NAMED_SEGMENT1 = new NamedSegment(10, 12, "test");
    private static final NamedSegment NAMED_SEGMENT2 = new NamedSegment(12, 13, "abc");
    private static final NamedSegment NAMED_SEGMENT3 = new NamedSegment(14, 15, "abc");
    private static final NamedSegment NAMED_SEGMENT4 = new NamedSegment(14, 15, "");

    /**
     * A class for INamedSegment tests
     */
    private static class NamedSegment extends BasicSegment implements INamedSegment {

        private static final long serialVersionUID = -7955666081972046597L;
        private final String fName;

        public NamedSegment(long start, long end, String name) {
            super(start, end);
            fName = name;
        }

        @Override
        public @NonNull String getName() {
            return fName;
        }

    }

    /**
     * Test the {@link SegmentTypeComparators#NAMED_SEGMENT_COMPARATOR}
     * comparator
     */
    @Test
    public void testComparator() {
        Comparator<ISegment> cmp = SegmentTypeComparators.NAMED_SEGMENT_COMPARATOR;

        // Verify the comparator with the segments
        assertEquals(cmp.compare(NAMED_SEGMENT2, NAMED_SEGMENT3), cmp.compare(NAMED_SEGMENT3, NAMED_SEGMENT2));
        assertTrue(cmp.compare(NAMED_SEGMENT1, NAMED_SEGMENT2) > 0);
        assertEquals(cmp.compare(NAMED_SEGMENT1, NAMED_SEGMENT2), -1 * cmp.compare(NAMED_SEGMENT2, NAMED_SEGMENT1));
        assertTrue(cmp.compare(BASE_SEGMENT, NAMED_SEGMENT2) > 0);
        assertTrue(cmp.compare(NAMED_SEGMENT2, BASE_SEGMENT) < 0);
        assertTrue(cmp.compare(BASE_SEGMENT, NAMED_SEGMENT4) > 0);
        assertTrue(cmp.compare(NAMED_SEGMENT4, BASE_SEGMENT) < 0);

        // Add the segments to a segment store
        ISegmentStore<BasicSegment> segStore = SegmentStoreFactory.createSegmentStore(SegmentStoreType.Fast);
        segStore.add(BASE_SEGMENT);
        segStore.add(NAMED_SEGMENT1);
        segStore.add(NAMED_SEGMENT2);
        segStore.add(NAMED_SEGMENT3);
        segStore.add(NAMED_SEGMENT4);

        // Iterate with this comparator on the segment store
        Iterable<BasicSegment> iterable = segStore.iterator(cmp);
        Iterator<BasicSegment> iterator = iterable.iterator();
        assertTrue(iterator.hasNext());
        BasicSegment current = iterator.next();
        BasicSegment prev = current;
        int count = 1;
        while (iterator.hasNext()) {
            current = iterator.next();
            assertTrue(cmp.compare(prev, current) <= 0);
            prev = current;
            count++;
        }
        assertEquals(5, count);

        // Iterate with the reverse comparator
        iterable = segStore.iterator(NonNullUtils.checkNotNull(cmp.reversed()));
        iterator = iterable.iterator();
        assertTrue(iterator.hasNext());
        current = iterator.next();
        prev = current;
        count = 1;
        while (iterator.hasNext()) {
            current = iterator.next();
            assertTrue(cmp.compare(prev, current) >= 0);
            prev = current;
            count++;
        }
        assertEquals(5, count);
    }

    /**
     * Test the {@link SegmentAspects} method
     */
    @Test
    public void testNamedAspect() {
        assertNull(SegmentAspects.getName(BASE_SEGMENT));
        assertEquals("test", SegmentAspects.getName(NAMED_SEGMENT1));
        assertEquals("abc", SegmentAspects.getName(NAMED_SEGMENT2));
        assertEquals("", SegmentAspects.getName(NAMED_SEGMENT4));

    }

}
