/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 ******************************************************************************/

package org.eclipse.tracecompass.internal.datastore.core.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.junit.Test;

/**
 * Test the discrete time range condition.
 *
 * @author Loïc Prieur-Drevon
 */
public class DiscreteTimeRangeConditionTest {

    private static final long LOW = 0L;
    private static final long HIGH = 10L;
    private static final List<Long> VALUES = Arrays.asList(LOW, HIGH / 2L, HIGH);
    private static final TimeRangeCondition CONDITION = new ArrayTimeRangeCondition(VALUES);

    /**
     * Ensure that we cannot build a condition with an empty collection.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor() {
        new ArrayTimeRangeCondition(Collections.emptyList());
    }

    /**
     * Ensure that the minimum and maximum functions return the correct values.
     */
    @Test
    public void testBounds() {
        assertEquals(LOW, CONDITION.min());
        assertEquals(HIGH, CONDITION.max());
    }

    /**
     * Test that the right elements are contained in the condition.
     */
    @Test
    public void testPredicate() {
        assertFalse(CONDITION.test(-5L));
        for (Long v : VALUES) {
            assertTrue(CONDITION.test(v));
            assertFalse(CONDITION.test(v + 1L));
        }
        assertFalse(CONDITION.test(15L));
    }

    /**
     * Test that modifying the list used to populate the condition does not
     * affect the condition
     */
    @Test
    public void testPredicateAndAdd() {
        List<Long> values = new ArrayList<>();
        values.add(1L);
        values.add(5L);
        TimeRangeCondition condition = new ArrayTimeRangeCondition(values);
        assertFalse(condition.test(-5L));
        for (Long v : values) {
            assertTrue(condition.test(v));
            assertFalse(condition.test(v + 1L));
        }
        assertFalse(condition.test(15L));
        // Add the values to the initial set and make sure it is not part of the
        // condition
        values.add(15L);
        assertFalse(condition.test(15L));
    }

    /**
     * Test that the right intervals intersect the condition.
     */
    @Test
    public void testIntersects() {
        assertFalse(CONDITION.intersects(Long.MIN_VALUE, LOW - 1L));
        assertTrue(CONDITION.intersects(0L, 4L));
        assertFalse(CONDITION.intersects(1L, 4L));
        assertTrue(CONDITION.intersects(2L, 8L));
        assertFalse(CONDITION.intersects(6L, 9L));
        assertTrue(CONDITION.intersects(5L, 15L));
        assertFalse(CONDITION.intersects(HIGH + 1L, Long.MAX_VALUE));
    }

    /**
     * Test that the returned subcondition has the correct bounds.
     */
    @Test
    public void testSubCondition() {
        @Nullable TimeRangeCondition sub = CONDITION.subCondition(-5L, 8L);
        assertNotNull(sub);
        assertEquals(ArrayTimeRangeCondition.class, sub.getClass());
        long low = sub.min();
        long high = sub.max();
        assertEquals(LOW, low);
        assertEquals(HIGH / 2, high);

        // For a range where no value is include, it should return null
        sub = CONDITION.subCondition(LOW + 1L, HIGH / 2 - 1);
        assertNull(sub);

        // Test conditions for border values, sub conditions are inclusive
        sub = CONDITION.subCondition(LOW, HIGH / 2);
        assertNotNull(sub);
        low = sub.min();
        high = sub.max();
        assertEquals(LOW, low);
        assertEquals(HIGH / 2, high);
    }

}
