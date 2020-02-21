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

import org.eclipse.tracecompass.internal.provisional.datastore.core.condition.TimeRangeCondition;
import org.junit.Test;

/**
 * Test the continuous time range condition
 *
 * @author Loïc Prieur-Drevon
 */
public class ContinuousTimeRangeConditionTest {

    private static final long LOW = 0;
    private static final long HIGH = 10;
    private static final ContinuousTimeRangeCondition CONDITION = new ContinuousTimeRangeCondition(LOW, HIGH);

    /**
     * Ensure that we cannot build a condition with a bigger low than high bound.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructor() {
        new ContinuousTimeRangeCondition(HIGH, LOW);
    }

    /**
     * Ensure that the minimum and maximum functions return the correct values.
     */
    @Test
    public void testBounds() {
        long low = CONDITION.min();
        assertEquals(LOW, low);
        long high = CONDITION.max();
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
        TimeRangeCondition sub = CONDITION.subCondition(-5, 8);
        assertNotNull(sub);
        assertEquals(ContinuousTimeRangeCondition.class, sub.getClass());
        long low = sub.min();
        long high = sub.max();
        assertEquals(LOW, low);
        assertEquals(8, high);

        sub = CONDITION.subCondition(HIGH + 1, HIGH + 10);
        assertNull(sub);
        sub = CONDITION.subCondition(LOW - 10, LOW - 1);
        assertNull(sub);
    }

}
