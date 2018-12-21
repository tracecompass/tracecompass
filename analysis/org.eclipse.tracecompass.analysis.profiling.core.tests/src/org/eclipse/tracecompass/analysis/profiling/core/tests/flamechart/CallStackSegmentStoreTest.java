/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.profiling.core.tests.flamechart;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import java.util.Iterator;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.profiling.core.tests.CallStackTestBase;
import org.eclipse.tracecompass.analysis.profiling.core.tests.data.TestDataSmallCallStack;
import org.eclipse.tracecompass.analysis.profiling.core.tests.stubs.CallStackAnalysisStub;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.junit.Test;

import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;

/**
 * Test the callstack analysis as a segment store
 *
 * @author Geneviève Bastien
 */
public class CallStackSegmentStoreTest extends CallStackTestBase {

    /**
     * Constructor
     */
    public CallStackSegmentStoreTest() {
        super(new TestDataSmallCallStack());
    }

    /**
     * Test the callstack data using the callstack object
     */
    @Test
    public void testSeriesSegmentStoreIterator() {
        CallStackAnalysisStub module = getModule();
        assertNotNull(module);

        ISegmentStore<@NonNull ISegment> segmentStore = module.getSegmentStore();
        assertNotNull(segmentStore);

        Iterator<@NonNull ISegment> iterator = segmentStore.iterator();
        assertEquals("Segment store iterator count", 21, Iterators.size(iterator));
        assertEquals("Segment store size", 21, segmentStore.size());
        assertFalse(segmentStore.isEmpty());
    }

    /**
     * Test the segment store's intersecting query methods
     */
    @Test
    public void testIntersectingSegmentStore() {
        CallStackAnalysisStub module = getModule();
        assertNotNull(module);

        ISegmentStore<@NonNull ISegment> segmentStore = module.getSegmentStore();
        assertNotNull(segmentStore);

        // Test with some boundaries: all elements that start or end at 10 should be
        // included
        Iterable<@NonNull ISegment> elements = segmentStore.getIntersectingElements(10L);
        assertEquals("Intersecting 10", 9, Iterables.size(elements));

        elements = segmentStore.getIntersectingElements(10L, 15L);
        assertEquals("Between 10 and 15", 12, Iterables.size(elements));
    }

}
