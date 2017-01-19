/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.internal.provisional.datastore.core.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.internal.datastore.core.condition.ContinuousRangeCondition;
import org.eclipse.tracecompass.internal.datastore.core.condition.DiscreteRangeCondition;
import org.junit.Test;

import com.google.common.collect.ImmutableList;

/**
 * Test the {@link RangeCondition} static methods
 *
 * @author Geneviève Bastien
 */
public class RangeConditionTest {

    /**
     * Test the {@link RangeCondition#singleton(Comparable)} method
     */
    @Test
    public void testSingleton() {
        /* A test value */
        Long value = 3L;
        RangeCondition<Long> cnd = RangeCondition.singleton(3L);
        /* Make sure the return value is a discrete range of the right value */
        assertTrue(cnd instanceof DiscreteRangeCondition);
        assertEquals(value, cnd.max());
        assertEquals(value, cnd.min());
    }

    /**
     * Test the
     * {@link RangeCondition#forContinuousRange(Comparable, Comparable)} method
     * for value with bound1 < bound2
     */
    @Test
    public void testForContinuousRangeValid() {
        Long min = 1L;
        Long max = 10L;
        RangeCondition<Long> cnd = RangeCondition.forContinuousRange(min, max);
        assertTrue(cnd instanceof ContinuousRangeCondition);
        assertEquals(min, cnd.min());
        assertEquals(max, cnd.max());
    }

    /**
     * Test the
     * {@link RangeCondition#forContinuousRange(Comparable, Comparable)} method
     * for value with bound1 > bound2
     */
    @Test(expected = IllegalArgumentException.class)
    public void testForContinuousRangeInvalid() {
        Long min = 1L;
        Long max = 10L;
        RangeCondition.forContinuousRange(max, min);
    }

    /**
     * Test the {@link RangeCondition#fromCollection(java.util.Collection)}
     * method
     */
    @Test
    public void testFromCollection() {
        // Add a list of unsorted elements
        ImmutableList<Long> elements = ImmutableList.of(1L, 4L, 2L, 10L, -3L);
        RangeCondition<Long> cnd = RangeCondition.fromCollection(elements);
        assertTrue(cnd instanceof DiscreteRangeCondition);
        assertEquals(-3L, (long) cnd.min());
        assertEquals(10L, (long) cnd.max());
    }

    /**
     * Test the {@link RangeCondition#forDiscreteRange(long, long, long)} method
     * for value with bound1 < bound2
     */
    @Test
    public void testForDiscreteRangeValid() {

        // Test with valid values and a step of 1: all values should be in the
        // range
        Long min = 1L;
        Long max = 10L;
        Long step = 1L;
        RangeCondition<Long> cnd = RangeCondition.forDiscreteRange(min, max, step);
        assertTrue(cnd instanceof DiscreteRangeCondition);
        assertEquals(min, cnd.min());
        assertEquals(max, cnd.max());
        for (Long i = min; i <= max; i++) {
            assertTrue(cnd.test(i));
        }

        // Test with a step of 0, it should take 1 as a default value
        step = 0L;
        cnd = RangeCondition.forDiscreteRange(min, max, step);
        assertTrue(cnd instanceof DiscreteRangeCondition);
        assertEquals(min, cnd.min());
        assertEquals(max, cnd.max());
        for (Long i = min; i <= max; i++) {
            assertTrue(cnd.test(i));
        }

        // Test with a step of 2. Make sure that values have the right steps and
        // the max element is part of the condition
        step = 2L;
        cnd = RangeCondition.forDiscreteRange(min, max, step);
        assertTrue(cnd instanceof DiscreteRangeCondition);
        assertEquals(min, cnd.min());
        assertEquals(max, cnd.max());
        for (Long i = min; i < max; i++) {
            if ((i - min) % step == 0) {
                assertTrue(cnd.test(i));
            } else {
                assertFalse(cnd.test(i));
            }
        }
        assertTrue(cnd.test(max));
    }

    /**
     * Test the {@link RangeCondition#forDiscreteRange(long, long, long)} method
     * for value with bound1 > bound2
     */
    @Test(expected = IllegalArgumentException.class)
    public void testForDiscreteRangeInvalid() {
        Long min = 1L;
        Long max = 10L;
        Long step = 1L;
        RangeCondition.forDiscreteRange(max, min, step);
    }

    /**
     * Test the {@link RangeCondition#forDiscreteRange(long, long, long)} method
     * with a step < 0
     */
    @Test(expected = IllegalArgumentException.class)
    public void testForDiscreteRangeInvalidStep() {
        Long min = 1L;
        Long max = 10L;
        Long step = -2L;
        RangeCondition.forDiscreteRange(max, min, step);
    }

}
