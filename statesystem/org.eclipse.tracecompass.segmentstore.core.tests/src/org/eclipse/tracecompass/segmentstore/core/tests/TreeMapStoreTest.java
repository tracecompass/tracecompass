/*******************************************************************************
 * Copyright (c) 2015 Ericsson
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
import org.eclipse.tracecompass.internal.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.junit.Test;

/**
 * Test specific behavior for a TreeMapStore segment store.
 *
 * @author France Lapointe Nguyen
 */
public class TreeMapStoreTest extends AbstractTestSegmentStore {

    @Override
    protected ISegmentStore<@NonNull ISegment> getSegmentStore() {
        return new TreeMapStore<>();
    }

    /**
     * The TreeMapStore does not have a bulk loader, if it ever gets one, it should be tested here.
     */
    @Override
    protected ISegmentStore<@NonNull ISegment> getSegmentStore(@NonNull ISegment @NonNull [] data) {
        TreeMapStore<@NonNull ISegment> treeMapStore = new TreeMapStore<>();
        treeMapStore.addAll(Arrays.asList(data));
        return treeMapStore;
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