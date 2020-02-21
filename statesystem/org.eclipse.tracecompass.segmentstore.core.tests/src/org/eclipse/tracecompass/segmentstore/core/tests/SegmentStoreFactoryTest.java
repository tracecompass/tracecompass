/*******************************************************************************
 * Copyright (c) 2016 Ericsson
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
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.segmentstore.core.BasicSegment;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory;
import org.eclipse.tracecompass.segmentstore.core.SegmentStoreFactory.SegmentStoreType;
import org.junit.Test;

/**
 * Segment Store factory test
 *
 * @author Matthew Khouzam
 *
 */
public class SegmentStoreFactoryTest {

    /**
     * Simplest create function, should always work
     */
    @Test
    public void simpleCreate() {
        assertNotNull(SegmentStoreFactory.createSegmentStore());
    }

    /**
     * Create a fast segment store. Should always return the fastest
     * implementation available
     */
    @Test
    public void createFast() {
        assertNotNull(SegmentStoreFactory.createSegmentStore(SegmentStoreType.Fast));
    }

    /**
     * Create a stable segment store, should always return a segment store that
     * does not change much its performance memory or cpu
     */
    @Test
    public void createStable() {
        assertNotNull(SegmentStoreFactory.createSegmentStore(SegmentStoreType.Stable));
    }

    /**
     * Create a "set" like segment store
     */
    @Test
    public void createDistinct() {
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore(SegmentStoreType.Distinct);
        assertNotNull(fixture);
        testDistinct(fixture);
    }

    /**
     * Make sure mixing flags works
     */
    @Test
    public void createDistinctFast() {
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore(SegmentStoreType.Distinct, SegmentStoreType.Fast);
        testDistinct(fixture);
    }

    /**
     * Make sure mixing flags works
     */
    @Test
    public void createDistinctStable() {
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore(SegmentStoreType.Distinct, SegmentStoreType.Stable);
        testDistinct(fixture);
    }

    /**
     * Make sure mixing flags works
     */
    @Test
    public void createDoubleDistinct() {
        ISegmentStore<@NonNull ISegment> fixture = SegmentStoreFactory.createSegmentStore(SegmentStoreType.Distinct, SegmentStoreType.Distinct);
        assertNotNull(fixture);
        testDistinct(fixture);
    }

    /**
     * Make sure mixing flags works (power set of all types)
     */
    @Test
    public void createAllDressed() {
        testDistinct(SegmentStoreFactory.createSegmentStore(SegmentStoreType.Distinct, SegmentStoreType.Stable, SegmentStoreType.Fast));
        testDistinct(SegmentStoreFactory.createSegmentStore(SegmentStoreType.Distinct, SegmentStoreType.Fast, SegmentStoreType.Stable));
        testDistinct(SegmentStoreFactory.createSegmentStore(SegmentStoreType.Stable, SegmentStoreType.Distinct, SegmentStoreType.Fast));
        testDistinct(SegmentStoreFactory.createSegmentStore(SegmentStoreType.Stable, SegmentStoreType.Fast, SegmentStoreType.Distinct));
        testDistinct(SegmentStoreFactory.createSegmentStore(SegmentStoreType.Fast, SegmentStoreType.Distinct, SegmentStoreType.Stable));
        testDistinct(SegmentStoreFactory.createSegmentStore(SegmentStoreType.Fast, SegmentStoreType.Stable, SegmentStoreType.Distinct));
    }

    /**
     * Torture test, add many redundant flags
     */
    @SuppressWarnings("null")
    @Test
    public void createTortureTest() {
        Random rnd = new Random();
        List<@NonNull SegmentStoreType> args = new ArrayList<>();
        rnd.setSeed(1234);
        int nbValues = SegmentStoreType.values().length;
        args.add(SegmentStoreType.Distinct);
        for (int i = 0; i < 1000; i++) {
            int nextInt = rnd.nextInt(nbValues);
            args.add(SegmentStoreType.values()[nextInt]);
        }
        SegmentStoreType @NonNull [] array = args.toArray(new SegmentStoreType[args.size()]);
        assertNotNull(array);
        testDistinct(SegmentStoreFactory.createSegmentStore(array));
    }

    /**
     * Create a pre-loaded fast segment store
     */
    @Test
    public void createPreloaded() {
        ISegment[] data = new ISegment[1];
        data[0] = new BasicSegment(0, 0);
        ISegmentStore<@NonNull ISegment> segmentStore;
        segmentStore = SegmentStoreFactory.createSegmentStore(data);
        assertNotNull(segmentStore);
        assertEquals(1, segmentStore.size());
        segmentStore = SegmentStoreFactory.createSegmentStore(data, SegmentStoreType.Fast);
        assertNotNull(segmentStore);
        assertEquals(1, segmentStore.size());
        segmentStore = SegmentStoreFactory.createSegmentStore(data, SegmentStoreType.Stable);
        assertNotNull(segmentStore);
        assertEquals(1, segmentStore.size());
        segmentStore = SegmentStoreFactory.createSegmentStore(data, SegmentStoreType.Distinct);
        assertNotNull(segmentStore);
        assertEquals(1, segmentStore.size());
    }

    private static void testDistinct(ISegmentStore<@NonNull ISegment> fixture) {
        // test adding the same object
        ISegment seg = new BasicSegment(0, 0);
        fixture.add(seg);
        fixture.add(seg);
        assertEquals(1, fixture.size());
        fixture.clear();
        // test different equal objects
        fixture.add(new BasicSegment(0, 0));
        fixture.add(new BasicSegment(0, 0));
        assertEquals(1, fixture.size());
    }

}
