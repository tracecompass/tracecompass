/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/

package org.eclipse.tracecompass.internal.datastore.core.condition;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.RangeCondition;
import org.junit.Test;

/**
 * Test the continuous range condition with Integers.
 *
 * @author Loïc Prieur-Drevon
 */
public class ContinuousRangeConditionTest {

    private static final int LOW = 0;
    private static final int HIGH = 10;
    private static final ContinuousRangeCondition<Integer> CONDITION = new ContinuousRangeCondition<>(LOW, HIGH);

    /**
     * Ensure that we cannot build a condition with a bigger low than high bound.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor() {
        new ContinuousRangeCondition<>(HIGH, LOW);
    }

    /**
     * Ensure that the minimum and maximum functions return the correct values.
     */
    @Test
    public void testBounds() {
        int low = CONDITION.min();
        assertEquals(LOW, low);
        int high = CONDITION.max();
        assertEquals(HIGH, high);
    }

    /**
     * Test that the right elements are contained in the condition.
     */
    @Test
    public void testPredicate() {
        assertFalse(CONDITION.test(-5));
        assertTrue(CONDITION.test(LOW));
        assertTrue(CONDITION.test(5));
        assertTrue(CONDITION.test(HIGH));
        assertFalse(CONDITION.test(15));
    }

    /**
     * Test that the right intervals intersect the condition.
     */
    @Test
    public void testIntersects() {
        assertFalse(CONDITION.intersects(Integer.MIN_VALUE, LOW - 1));
        assertTrue(CONDITION.intersects(-5, 5));
        assertTrue(CONDITION.intersects(2, 8));
        assertTrue(CONDITION.intersects(5, 15));
        assertFalse(CONDITION.intersects(HIGH + 1, Integer.MAX_VALUE));
    }

    /**
     * Test that the returned subcondition has the correct bounds.
     */
    @Test
    public void testSubCondition() {
        RangeCondition<Integer> sub = CONDITION.subCondition(-5, 8);
        assertNotNull(sub);
        assertEquals(ContinuousRangeCondition.class, sub.getClass());
        int low = sub.min();
        int high = sub.max();
        assertEquals(LOW, low);
        assertEquals(8, high);
    }

}
