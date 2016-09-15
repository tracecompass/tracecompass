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

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
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
    protected ISegmentStore<@NonNull ISegment> getSegmentStore() {
        return new TreeMapStore<>();
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
}