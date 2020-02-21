/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemFactory;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.backend.IStateHistoryBackend;
import org.eclipse.tracecompass.statesystem.core.backend.StateHistoryBackendFactory;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

/**
 * Test the 2D State System queries
 *
 * @author Loïc Prieur-Drevon
 */
public class StateSystem2DTest {

    private static final long START_TIME = 50L;
    private static final @NonNull String STRING_ATTRIBUTE = "String";
    private static final @NonNull String INTEGER_ATTRIBUTE = "Integer";

    private ITmfStateSystemBuilder fStateSystem;

    /**
     * Build a small state history tree
     *
     * @throws IOException
     *             If the state system file could not be created
     */
    @Before
    public void setupStateSystem() throws IOException {
        IStateHistoryBackend backend = StateHistoryBackendFactory.createHistoryTreeBackendNewFile("test",
                NonNullUtils.checkNotNull(File.createTempFile("2Dtest", "ht")), 0, START_TIME, 0);
        fStateSystem = StateSystemFactory.newStateSystem(NonNullUtils.checkNotNull(backend));
        int stringQuark = fStateSystem.getQuarkAbsoluteAndAdd(STRING_ATTRIBUTE);
        int integerQuark = fStateSystem.getQuarkAbsoluteAndAdd(INTEGER_ATTRIBUTE);

        fStateSystem.modifyAttribute(60L, "String1", stringQuark);
        fStateSystem.modifyAttribute(70L, 0, integerQuark);
        fStateSystem.modifyAttribute(80L, 1, integerQuark);
        fStateSystem.modifyAttribute(90L, "String2", stringQuark);
        fStateSystem.modifyAttribute(100L, 2, integerQuark);
        fStateSystem.modifyAttribute(110L, 3, integerQuark);
        fStateSystem.modifyAttribute(130L, "String3", stringQuark);
        fStateSystem.modifyAttribute(140L, "String4", stringQuark);
        fStateSystem.modifyAttribute(160L, 4, integerQuark);

        fStateSystem.closeHistory(200L);
    }

    /**
     * Clean-up
     */
    @After
    public void tearDown() {
        fStateSystem.dispose();
        fStateSystem.removeFiles();
    }

    private static void testContinuous(Iterable<ITmfStateInterval> iterable, Collection<Integer> quarks, long start, long end, int totalCount) {
        Multimap<Integer, ITmfStateInterval> treeMap = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));

        iterable.forEach(interval -> assertTrue("Interval: " + interval + " was already returned: " + treeMap,
                treeMap.put(interval.getAttribute(), interval)));
        assertEquals("Wrong number of intervals returned", totalCount, treeMap.size());
        assertEquals("There should only be as many Sets of intervals as quarks",
                quarks.size(), treeMap.keySet().size());

        for (Integer quark : quarks) {
            Collection<ITmfStateInterval> orderedSet = treeMap.get(quark);
            assertFalse("There should be intervals for quark: " + quark, orderedSet.isEmpty());
            ITmfStateInterval previous = null;
            for (ITmfStateInterval interval : orderedSet) {
                if (previous == null) {
                    assertTrue("The first interval: " + interval + "should intersect start: " + start, interval.intersects(start));
                } else {
                    assertEquals("Current interval: " + interval + " should have been contiguous to the previous one " + previous,
                            previous.getEndTime() + 1, interval.getStartTime());
                }
                previous = interval;
            }
            assertNotNull("There should have been at least one interval for quark " + quark, previous);
            assertTrue("last interval: " + previous + " should intersect end " + end, previous.intersects(end));
        }
    }

    /**
     * Test the continuous 2D query method.
     *
     * @throws AttributeNotFoundException
     *             if the requested attribute simply did not exist in the system.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     * @throws TimeRangeException
     *             If the smallest time is before the state system start time.
     * @throws IndexOutOfBoundsException
     *             If the smallest attribute is <0 or if the largest is >= to the
     *             number of attributes.
     */
    @Test
    public void testContinuous2DQuery() throws AttributeNotFoundException, IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);
        long end = ss.getCurrentEndTime();

        /* Make sure all and only the String intervals are returned */
        int stringQuark = fStateSystem.getQuarkAbsolute(STRING_ATTRIBUTE);
        Iterable<ITmfStateInterval> iterable = ss.query2D(Collections.singleton(stringQuark), START_TIME, end);
        testContinuous(iterable, Collections.singleton(stringQuark), START_TIME, end, 5);

        /* Make sure all and only the Integer intervals are returned */
        int integerQuark = fStateSystem.getQuarkAbsolute(INTEGER_ATTRIBUTE);
        iterable = ss.query2D(Collections.singleton(integerQuark), START_TIME, end);
        testContinuous(iterable, Collections.singleton(integerQuark), START_TIME, end, 6);

        /* Make sure all intervals are returned */
        Collection<Integer> quarks = ImmutableList.of(stringQuark, integerQuark);
        iterable = ss.query2D(quarks, START_TIME, end);
        testContinuous(iterable, quarks, START_TIME, end, 11);
    }

    private static void testDiscrete(Iterable<ITmfStateInterval> iterable, Collection<Integer> quarks, Collection<Long> times, int totalCount) {
        Set<ITmfStateInterval> set = new HashSet<>();
        int countTimeStamps = 0;

        for (ITmfStateInterval interval : iterable) {
            assertTrue(quarks.contains(interval.getAttribute()));
            assertTrue("interval: " + interval + " was returned twice", set.add(interval));

            int timeStamps = (int) times.stream().filter(interval::intersects).count();
            assertTrue("interval: " + interval + " does not intersect any time stamp: " + times, timeStamps > 0);
            countTimeStamps += timeStamps;
        }

        assertEquals("incorrect number of intervals returned", totalCount, set.size());
        assertEquals("All the queried time stamps were not covered", times.size() * quarks.size(), countTimeStamps);
    }

    /**
     * Test the discrete 2D query method.
     *
     * @throws AttributeNotFoundException
     *             if the requested attribute simply did not exist in the system.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     * @throws TimeRangeException
     *             If the smallest time is before the state system start time.
     * @throws IndexOutOfBoundsException
     *             If the smallest attribute is <0 or if the largest is >= to the
     *             number of attributes.
     */
    @Test
    public void testDiscrete2DQuery() throws AttributeNotFoundException,
            IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);
        long end = ss.getCurrentEndTime();
        Collection<Long> times = StateSystemUtils.getTimes(START_TIME, end, 30L);
        assertEquals(6, times.size());
        assertTrue(Ordering.natural().isStrictlyOrdered(times));

        /* Make sure all and only the String intervals are returned */
        int stringQuark = fStateSystem.getQuarkAbsolute(STRING_ATTRIBUTE);
        Iterable<ITmfStateInterval> iterable = ss.query2D(Collections.singleton(stringQuark), times);
        testDiscrete(iterable, Collections.singleton(stringQuark), times, 4);

        /* Make sure all and only the Integer intervals are returned */
        int integerQuark = fStateSystem.getQuarkAbsolute(INTEGER_ATTRIBUTE);
        iterable = ss.query2D(Collections.singleton(integerQuark), times);
        testDiscrete(iterable, Collections.singleton(integerQuark), times, 4);

        /* Make sure all intervals are returned */
        Collection<Integer> quarks = ImmutableList.of(stringQuark, integerQuark);
        iterable = ss.query2D(quarks, times);
        testDiscrete(iterable, quarks, times, 8);
    }

    /**
     * Test index out of bound queries
     *
     * @throws StateSystemDisposedException
     *             ss was closed
     * @throws TimeRangeException
     *             time was out of range
     * @throws IndexOutOfBoundsException
     *             queried an attribute that was out of bounds
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testIOOB2DQuery() throws IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);
        Collection<Long> times = Collections.singleton(77L);

        int stringQuark = Integer.MAX_VALUE;
        ss.query2D(Collections.singleton(stringQuark), times);
    }

    /**
     * Test a too low time range exception on continuous queries
     *
     * @throws StateSystemDisposedException
     *             ss was closed
     * @throws TimeRangeException
     *             time was out of range
     * @throws IndexOutOfBoundsException
     *             queried an attribute that was out of bounds
     */
    @Test(expected = TimeRangeException.class)
    public void testTimeRangeException2DContinous() throws IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);

        ss.query2D(Collections.singleton(0), 0L, 77L);
    }

    /**
     * Test a too low time range exception on discrete queries
     *
     * @throws StateSystemDisposedException
     *             ss was closed
     * @throws TimeRangeException
     *             time was out of range
     * @throws IndexOutOfBoundsException
     *             queried an attribute that was out of bounds
     */
    @Test(expected = TimeRangeException.class)
    public void testTimeRangeException2DDiscrete() throws IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);

        ss.query2D(Collections.singleton(0), Collections.singleton(Long.MIN_VALUE));
    }

    /**
     * Test Empty queries
     *
     * @throws StateSystemDisposedException
     *             ss was closed
     * @throws TimeRangeException
     *             time was out of range
     * @throws IndexOutOfBoundsException
     *             queried an attribute that was out of bounds
     */
    @Test
    public void testEmpty2DQuery() throws IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);

        /* Test on a discrete query */
        Iterable<ITmfStateInterval> iterable = ss.query2D(Collections.emptyList(), Collections.singleton(77L));
        assertNotNull(iterable);
        assertTrue(Iterables.isEmpty(iterable));

        /* Test on an empty time sample */
        iterable = ss.query2D(Collections.singleton(0), Collections.emptyList());
        assertNotNull(iterable);
        assertTrue(Iterables.isEmpty(iterable));

        /* Test on a continuous query */
        iterable = ss.query2D(Collections.emptyList(), 77L, 78L);
        assertNotNull(iterable);
        assertTrue(Iterables.isEmpty(iterable));
    }

    /**
     * Test the continuous 2D query method when start time > end time. Since
     * this state system only has one node, it does not compare behavior between
     * reverse and not reverse queries.
     *
     * @throws AttributeNotFoundException
     *             if the requested attribute simply did not exist in the
     *             system.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     * @throws TimeRangeException
     *             If the smallest time is before the state system start time.
     * @throws IndexOutOfBoundsException
     *             If the smallest attribute is <0 or if the largest is >= to
     *             the number of attributes.
     */
    @Test
    public void testReverse2DQuery() throws AttributeNotFoundException, IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);
        long end = ss.getCurrentEndTime();

        /* Make sure all and only the String intervals are returned */
        int stringQuark = fStateSystem.getQuarkAbsolute(STRING_ATTRIBUTE);
        Iterable<ITmfStateInterval> iterable = ss.query2D(Collections.singleton(stringQuark), end, START_TIME);
        testContinuous(iterable, Collections.singleton(stringQuark), START_TIME, end, 5);

        /* Make sure all and only the Integer intervals are returned */
        int integerQuark = fStateSystem.getQuarkAbsolute(INTEGER_ATTRIBUTE);
        iterable = ss.query2D(Collections.singleton(integerQuark), end, START_TIME);
        testContinuous(iterable, Collections.singleton(integerQuark), START_TIME, end, 6);

        /* Make sure all intervals are returned */
        Collection<Integer> quarks = ImmutableList.of(stringQuark, integerQuark);
        iterable = ss.query2D(quarks, end, START_TIME);
        testContinuous(iterable, quarks, START_TIME, end, 11);
    }

}
