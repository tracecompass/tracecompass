/*******************************************************************************
 * Copyright (c) 2016 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.statesystem.core.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

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
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
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
     */
    @Before
    public void setupStateSystem() {
        try {
            IStateHistoryBackend backend = null;
            try {
                backend = StateHistoryBackendFactory.createHistoryTreeBackendNewFile("test",
                        NonNullUtils.checkNotNull(File.createTempFile("2Dtest", "ht")), 0, START_TIME, 0);
            } catch (IOException e) {
                fail(e.getMessage());
            }
            fStateSystem = StateSystemFactory.newStateSystem(NonNullUtils.checkNotNull(backend));
            int stringQuark = fStateSystem.getQuarkAbsoluteAndAdd(STRING_ATTRIBUTE);
            int integerQuark = fStateSystem.getQuarkAbsoluteAndAdd(INTEGER_ATTRIBUTE);

            fStateSystem.modifyAttribute(60L, TmfStateValue.newValueString("String1"), stringQuark);
            fStateSystem.modifyAttribute(70L, TmfStateValue.newValueInt(0), integerQuark);
            fStateSystem.modifyAttribute(80L, TmfStateValue.newValueInt(1), integerQuark);
            fStateSystem.modifyAttribute(90L, TmfStateValue.newValueString("String2"), stringQuark);
            fStateSystem.modifyAttribute(100L, TmfStateValue.newValueInt(2), integerQuark);
            fStateSystem.modifyAttribute(110L, TmfStateValue.newValueInt(3), integerQuark);
            fStateSystem.modifyAttribute(130L, TmfStateValue.newValueString("String3"), stringQuark);
            fStateSystem.modifyAttribute(140L, TmfStateValue.newValueString("String4"), stringQuark);
            fStateSystem.modifyAttribute(160L, TmfStateValue.newValueInt(4), integerQuark);

            fStateSystem.closeHistory(200L);
        } catch (StateValueTypeException e) {
            fail(e.getMessage());
        }
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

        /* Assert that intervals are distinct and sort them */
        iterable.forEach(interval -> assertTrue(treeMap.put(interval.getAttribute(), interval)));
        /* Check the number of distinct elements */
        assertEquals(totalCount, treeMap.size());
        /* There should only be as many Sets of intervals as quarks */
        assertEquals(quarks.size(), treeMap.keySet().size());

        for (Integer quark : quarks) {
            Collection<ITmfStateInterval> orderedSet = treeMap.get(quark);
            /* There should be intervals for this quark */
            assertTrue(!orderedSet.isEmpty());
            ITmfStateInterval previous = null;
            for (ITmfStateInterval interval : orderedSet) {
                if (previous == null) {
                    /* Assert that the first interval intersects start. */
                    assertTrue(interval.intersects(start));
                } else {
                    /*
                     * Assert that this interval is contiguous to the previous
                     * one.
                     */
                    assertEquals(previous.getEndTime() + 1, interval.getStartTime());
                }
                previous = interval;
            }
            /* Assert that the last interval intersects end. */
            assertNotNull(previous);
            assertTrue(previous.intersects(end));
        }
    }

    /**
     * Test the continuous 2D query method.
     */
    @Test
    public void testContinuous2DQuery() {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);
        long end = ss.getCurrentEndTime();

        try {
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
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    private static void testDiscrete(Iterable<ITmfStateInterval> iterable, Collection<Integer> quarks, Collection<Long> times, int totalCount) {
        Set<ITmfStateInterval> set = new HashSet<>();
        int countTimeStamps = 0;

        for (ITmfStateInterval interval : iterable) {
            assertTrue(quarks.contains(interval.getAttribute()));

            /* Assert that intervals are distinct */
            assertTrue(set.add(interval));

            /* Count how many time stamps this interval overlaps. */
            int timeStamps = (int) times.stream().filter(interval::intersects).count();
            assertTrue(timeStamps > 0);
            countTimeStamps += timeStamps;
        }

        /* Check the number of distinct elements */
        assertEquals(totalCount, set.size());
        /* Check that all time stamps are covered */
        assertEquals(times.size() * quarks.size(), countTimeStamps);
    }

    /**
     * Test the discrete 2D query method.
     */
    @Test
    public void testDiscrete2DQuery() {
        ITmfStateSystem ss = fStateSystem;
        assertNotNull(ss);
        long end = ss.getCurrentEndTime();
        Collection<Long> times = StateSystemUtils.getTimes(START_TIME, end, 30L);
        assertEquals(6, times.size());
        assertTrue(Ordering.natural().isStrictlyOrdered(times));

        try {
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
        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

}
