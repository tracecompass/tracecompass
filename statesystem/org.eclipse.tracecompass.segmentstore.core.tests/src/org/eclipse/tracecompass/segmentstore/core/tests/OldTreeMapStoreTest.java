/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.junit.Test;

/**
 * Unit tests for intersecting elements in a TreeMapStore
 *
 * @author France Lapointe Nguyen
 * @deprecated the test is deprecated as the store is deprecated
 */
@Deprecated
public class OldTreeMapStoreTest extends AbstractTestSegmentStore {

    @Override
    protected ISegmentStore<@NonNull TestSegment> getSegmentStore() {
        return new TreeMapStore<>();
    }

    @Override
    protected ISegmentStore<@NonNull TestSegment> getSegmentStore(TestSegment [] data) {
        TreeMapStore<@NonNull TestSegment> treeMapStore = new TreeMapStore<>();
        treeMapStore.addAll(Arrays.asList(data));
        return treeMapStore;
    }

    /**
     * Try adding duplicate elements, they should be ignored
     */
    @Test
    public void testNoDuplicateElements() {
        for (TestSegment segment : SEGMENTS) {
            boolean ret = fSegmentStore.add(new TestSegment(segment.getStart(), segment.getEnd(), segment.getPayload()));
            assertFalse(ret);
        }
        assertEquals(SEGMENTS.size(), fSegmentStore.size());
    }
}