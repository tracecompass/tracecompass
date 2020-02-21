/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.segmentstore.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Arrays;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.segmentstore.core.treemap.TreeMapStore;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.junit.Test;

/**
 * Test specific behavior for a TreeMapStore segment store.
 *
 * @author France Lapointe Nguyen
 */
public class TreeMapStoreTest extends AbstractTestSegmentStore {

    @Override
    protected ISegmentStore<@NonNull TestSegment> getSegmentStore() {
        return new TreeMapStore<>();
    }

    @Override
    protected ISegmentStore<@NonNull TestSegment> getSegmentStore(@NonNull TestSegment @NonNull [] data) {
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