/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.segmentstore.statistics;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.SystemCall;
import org.eclipse.tracecompass.analysis.os.linux.core.latency.SystemCall.InitialInfo;
import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.SegmentStoreStatistics;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.junit.Test;

/**
 * Test the staticsmodule. This is done with two tests.
 * <ol>
 * <li>test the values vs some sample points calculated by hand (sanity test)
 * </li>
 * <li>2- test exhaustively vs a reference implementation.</li>
 * </ol>
 *
 * @author Matthew Khouzam
 *
 */
public class SegmentStoreStatisticsTest {

    private static final int MEDIUM_AMOUNT_OF_SEGMENTS = 100;
    private static final int LARGE_AMOUNT_OF_SEGMENTS = 1000000;

    private static final double NO_ERROR = 0.0;
    private static final double ERROR = 0.000001;

    private static void testOnlineVsOffline(List<@NonNull SystemCall> fixture) {
        SegmentStoreStatistics sss = getSegStoreStat(fixture);
        OfflineStatisticsCalculator osc = new OfflineStatisticsCalculator(fixture);
        assertEquals("Average", osc.getAvg(), sss.getAverage(), ERROR);
        assertEquals("Standard Deviation", osc.getStdDev(), sss.getStdDev(), ERROR);
        assertEquals("Min", osc.getMin(), sss.getMin());
        assertEquals("Max", osc.getMax(), sss.getMax());
    }

    /**
     * Test incrementing
     */
    @Test
    public void climbTest() {
        List<@NonNull SystemCall> fixture = new ArrayList<>();
        for (int i = 0; i < MEDIUM_AMOUNT_OF_SEGMENTS; i++) {
            fixture.add(createAnonSyscall(i, i * 2));
        }
        SegmentStoreStatistics sss = getSegStoreStat(fixture);
        assertEquals("Average", 49.5, sss.getAverage(), ERROR);
        assertEquals("Min", 0, sss.getMin());
        assertEquals("Max", 99, sss.getMax());
        assertEquals("Standard Deviation", 29.0, sss.getStdDev(), 0.02);
        testOnlineVsOffline(fixture);
    }

    private static SegmentStoreStatistics getSegStoreStat(List<@NonNull SystemCall> fixture) {
        SegmentStoreStatistics sss = new SegmentStoreStatistics();
        for (ISegment seg : fixture) {
            sss.update(seg);
        }
        return sss;
    }

    /**
     * Test decrementing
     */
    @Test
    public void decrementingTest() {
        List<@NonNull SystemCall> fixture = new ArrayList<>();
        for (int i = MEDIUM_AMOUNT_OF_SEGMENTS; i >= 0; i--) {
            fixture.add(createAnonSyscall(i, i * 2));
        }
        SegmentStoreStatistics sss = getSegStoreStat(fixture);
        assertEquals("Average", 50, sss.getAverage(), NO_ERROR);
        assertEquals("Min", 0, sss.getMin());
        assertEquals("Max", 100, sss.getMax());
        assertEquals("Standard Deviation", 29.3, sss.getStdDev(), 0.01);
        testOnlineVsOffline(fixture);
    }

    /**
     * Test small
     */
    @Test
    public void smallTest() {
        List<@NonNull SystemCall> fixture = new ArrayList<>();
        for (int i = 1; i >= 0; i--) {
            fixture.add(createAnonSyscall(i, i * 2));
        }
        testOnlineVsOffline(fixture);
    }

    /**
     * Test large
     */
    @Test
    public void largeTest() {
        List<@NonNull SystemCall> fixture = new ArrayList<>();
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            fixture.add(createAnonSyscall(i, i * 2));
        }
        testOnlineVsOffline(fixture);
    }

    /**
     * Test noise
     */
    @Test
    public void noiseTest() {
        Random rnd = new Random();
        rnd.setSeed(1234);
        List<@NonNull SystemCall> fixture = new ArrayList<>();
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            int end = start + Math.abs(rnd.nextInt(1000000));
            fixture.add(createAnonSyscall(start, end));
        }
        testOnlineVsOffline(fixture);
    }

    /**
     * Test gaussian noise
     */
    @Test
    public void gaussianNoiseTest() {
        Random rnd = new Random();
        rnd.setSeed(1234);
        List<@NonNull SystemCall> fixture = new ArrayList<>();
        for (int i = 1; i <= LARGE_AMOUNT_OF_SEGMENTS; i++) {
            int start = Math.abs(rnd.nextInt(100000000));
            final int delta = Math.abs(rnd.nextInt(1000));
            int end = start + delta * delta;
            fixture.add(createAnonSyscall(start, end));
        }
        testOnlineVsOffline(fixture);
    }

    private static @NonNull SystemCall createAnonSyscall(int start, int end) {
        return new SystemCall(new InitialInfo(start, "", Collections.EMPTY_MAP), end, 0);
    }
}
