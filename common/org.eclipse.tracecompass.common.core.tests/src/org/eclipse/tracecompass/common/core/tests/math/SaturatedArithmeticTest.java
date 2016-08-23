/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.common.core.tests.math;

import static org.eclipse.tracecompass.common.core.math.SaturatedArithmetic.add;
import static org.eclipse.tracecompass.common.core.math.SaturatedArithmetic.multiply;
import static org.eclipse.tracecompass.common.core.math.SaturatedArithmetic.sameSign;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.eclipse.tracecompass.common.core.math.SaturatedArithmetic;
import org.junit.Test;

/**
 * Test suite for the {@link SaturatedArithmetic} class.
 * <p>
 * All tests must test reciprocity as well as low and high limits.
 *
 * @author Matthew Khouzam
 */
public class SaturatedArithmeticTest {

    /**
     * test multiplication absorption (int)
     */
    @Test
    public void testMult0() {
        assertEquals(0, multiply(0, 0));
        assertEquals(0, multiply(0, 1));
        assertEquals(0, multiply(1, 0));
        assertEquals(0, multiply(42, 0));
        assertEquals(0, multiply(0, 42));
        assertEquals(0, multiply(-42, 0));
        assertEquals(0, multiply(0, -42));
        assertEquals(0, multiply(Integer.MAX_VALUE, 0));
        assertEquals(0, multiply(0, Integer.MAX_VALUE));
        assertEquals(0, multiply(Integer.MIN_VALUE, 0));
        assertEquals(0, multiply(0, Integer.MIN_VALUE));
    }

    /**
     * test multiplication identity (int)
     */
    @Test
    public void testMult1() {
        assertEquals(0, multiply(0, 1));
        assertEquals(1, multiply(1, 1));
        assertEquals(42, multiply(42, 1));
        assertEquals(42, multiply(1, 42));
        assertEquals(-42, multiply(-42, 1));
        assertEquals(-42, multiply(1, -42));
        assertEquals(Integer.MAX_VALUE, multiply(Integer.MAX_VALUE, 1));
        assertEquals(Integer.MAX_VALUE, multiply(1, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, multiply(Integer.MIN_VALUE, 1));
        assertEquals(Integer.MIN_VALUE, multiply(1, Integer.MIN_VALUE));
    }

    /**
     * test multiplication typical (int)
     */
    @Test
    public void testMult100() {
        assertEquals(10000, multiply(100, 100));
        assertEquals(-10000, multiply(100, -100));
        assertEquals(-10000, multiply(-100, 100));
        assertEquals(10000, multiply(-100, -100));

        assertEquals(Integer.MAX_VALUE, multiply(Integer.MAX_VALUE, 100));
        assertEquals(Integer.MAX_VALUE, multiply(100, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, multiply(Integer.MIN_VALUE, 100));
        assertEquals(Integer.MIN_VALUE, multiply(100, Integer.MIN_VALUE));

        assertEquals(Integer.MIN_VALUE, multiply(Integer.MAX_VALUE, -100));
        assertEquals(Integer.MIN_VALUE, multiply(-100, Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, multiply(Integer.MIN_VALUE, -100));
        assertEquals(Integer.MAX_VALUE, multiply(-100, Integer.MIN_VALUE));
    }

    /**
     * test multiplication limit (int)
     */
    @Test
    public void testMultLimit() {
        assertEquals(Integer.MAX_VALUE, multiply(Integer.MAX_VALUE, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, multiply(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertEquals(Integer.MIN_VALUE, multiply(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertEquals(Integer.MAX_VALUE, multiply(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    /**
     * test multiplication absorption (long)
     */
    @Test
    public void testMult0L() {
        assertEquals(0, multiply(0L, 0L));
        assertEquals(0, multiply(0L, 1L));
        assertEquals(0, multiply(1L, 0L));
        assertEquals(0, multiply(42L, 0L));
        assertEquals(0, multiply(0L, 42L));
        assertEquals(0, multiply(-42L, 0L));
        assertEquals(0, multiply(0L, -42L));
        assertEquals(0, multiply(Long.MAX_VALUE, 0L));
        assertEquals(0, multiply(0L, Long.MAX_VALUE));
        assertEquals(0, multiply(Long.MIN_VALUE, 0L));
        assertEquals(0, multiply(0L, Long.MIN_VALUE));
    }

    /**
     * test multiplication identity (long)
     */
    @Test
    public void testMult1L() {
        assertEquals(0, multiply(0L, 1L));
        assertEquals(1, multiply(1L, 1L));
        assertEquals(42, multiply(42L, 1L));
        assertEquals(42, multiply(1L, 42L));
        assertEquals(-42, multiply(-42L, 1L));
        assertEquals(-42, multiply(1L, -42L));
        assertEquals(Long.MAX_VALUE, multiply(Long.MAX_VALUE, 1L));
        assertEquals(Long.MAX_VALUE, multiply(1L, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, multiply(Long.MIN_VALUE, 1L));
        assertEquals(Long.MIN_VALUE, multiply(1L, Long.MIN_VALUE));
    }

    /**
     * test multiplication typical (long)
     */
    @Test
    public void testMult100L() {
        assertEquals(10000, multiply(100L, 100L));
        assertEquals(-10000, multiply(100L, -100L));
        assertEquals(-10000, multiply(-100L, 100L));
        assertEquals(10000, multiply(-100L, -100L));

        assertEquals(Long.MAX_VALUE, multiply(Long.MAX_VALUE, 100L));
        assertEquals(Long.MAX_VALUE, multiply(100L, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, multiply(Long.MIN_VALUE, 100L));
        assertEquals(Long.MIN_VALUE, multiply(100L, Long.MIN_VALUE));

        assertEquals(Long.MIN_VALUE, multiply(Long.MAX_VALUE, -100L));
        assertEquals(Long.MIN_VALUE, multiply(-100L, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, multiply(Long.MIN_VALUE, -100L));
        assertEquals(Long.MAX_VALUE, multiply(-100L, Long.MIN_VALUE));
    }

    /**
     * test multiplication limit (long)
     */
    @Test
    public void testMultLimitL() {
        assertEquals(Long.MAX_VALUE, multiply(Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, multiply(Long.MAX_VALUE, Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, multiply(Long.MIN_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, multiply(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    /**
     * test addition identity (int)
     */
    @Test
    public void testAdd0() {
        assertEquals(0, add(0, 0));
        assertEquals(1, add(0, 1));
        assertEquals(1, add(1, 0));

        assertEquals(42, add(42, 0));
        assertEquals(42, add(0, 42));
        assertEquals(-42, add(-42, 0));
        assertEquals(-42, add(0, -42));

        assertEquals(Integer.MAX_VALUE, add(Integer.MAX_VALUE, 0));
        assertEquals(Integer.MAX_VALUE, add(0, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, add(Integer.MIN_VALUE, 0));
        assertEquals(Integer.MIN_VALUE, add(0, Integer.MIN_VALUE));
    }

    /**
     * test addition typical (int)
     */
    @Test
    public void testAdd100() {
        assertEquals(200, add(100, 100));
        assertEquals(0, add(100, -100));
        assertEquals(0, add(-100, 100));
        assertEquals(-200, add(-100, -100));

        assertEquals(Integer.MAX_VALUE, add(Integer.MAX_VALUE, 100));
        assertEquals(Integer.MAX_VALUE, add(100, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE + 100, add(Integer.MIN_VALUE, 100));
        assertEquals(Integer.MIN_VALUE + 100, add(100, Integer.MIN_VALUE));

        assertEquals(Integer.MAX_VALUE - 100, add(Integer.MAX_VALUE, -100));
        assertEquals(Integer.MAX_VALUE - 100, add(-100, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, add(Integer.MIN_VALUE, -100));
        assertEquals(Integer.MIN_VALUE, add(-100, Integer.MIN_VALUE));
    }

    /**
     * test addition limit (int)
     */
    @Test
    public void testAddLimit() {
        assertEquals(Integer.MAX_VALUE, add(Integer.MAX_VALUE, Integer.MAX_VALUE));
        // min value is 1 larger than max value
        assertEquals(-1, add(Integer.MAX_VALUE, Integer.MIN_VALUE));
        assertEquals(-1, add(Integer.MIN_VALUE, Integer.MAX_VALUE));
        assertEquals(Integer.MIN_VALUE, add(Integer.MIN_VALUE, Integer.MIN_VALUE));
    }

    /**
     * test addition identity (long)
     */
    @Test
    public void testAdd0L() {
        assertEquals(0, add(0L, 0L));
        assertEquals(1, add(0L, 1L));
        assertEquals(1, add(1L, 0L));

        assertEquals(42, add(42L, 0L));
        assertEquals(42, add(0L, 42L));
        assertEquals(-42, add(-42L, 0L));
        assertEquals(-42, add(0L, -42L));

        assertEquals(Long.MAX_VALUE, add(Long.MAX_VALUE, 0L));
        assertEquals(Long.MAX_VALUE, add(0L, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, add(Long.MIN_VALUE, 0L));
        assertEquals(Long.MIN_VALUE, add(0L, Long.MIN_VALUE));
    }

    /**
     * test addition typical (long)
     */
    @Test
    public void testAdd100L() {
        assertEquals(200, add(100L, 100L));
        assertEquals(0, add(100L, -100L));
        assertEquals(0, add(-100L, 100L));
        assertEquals(-200, add(-100L, -100L));

        assertEquals(Long.MAX_VALUE, add(Long.MAX_VALUE, 100L));
        assertEquals(Long.MAX_VALUE, add(100L, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE + 100, add(Long.MIN_VALUE, 100L));
        assertEquals(Long.MIN_VALUE + 100, add(100L, Long.MIN_VALUE));

        assertEquals(Long.MAX_VALUE - 100, add(Long.MAX_VALUE, -100L));
        assertEquals(Long.MAX_VALUE - 100, add(-100L, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, add(Long.MIN_VALUE, -100L));
        assertEquals(Long.MIN_VALUE, add(-100L, Long.MIN_VALUE));
    }

    /**
     * test addition limit (long)
     */
    @Test
    public void testAddLimitL() {
        assertEquals(Long.MAX_VALUE, add(Long.MAX_VALUE, Long.MAX_VALUE));
        // min value is 1 larger than max value
        assertEquals(-1, add(Long.MAX_VALUE, Long.MIN_VALUE));
        assertEquals(-1, add(Long.MIN_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, add(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    /**
     * test same sign
     */
    @Test
    public void testSameSign() {
        assertTrue(sameSign(0, 0));
        assertTrue(sameSign(0, -0));

        assertFalse(sameSign(0, -100));
        assertFalse(sameSign(-100, 0));
        assertTrue(sameSign(0, 100));
        assertTrue(sameSign(100, 0));

        assertFalse(sameSign(-0, -100));
        assertFalse(sameSign(-100, -0));
        assertTrue(sameSign(-0, 100));
        assertTrue(sameSign(100, -0));

        assertTrue(sameSign(100, 100));
        assertFalse(sameSign(100, -100));
        assertFalse(sameSign(-100, 100));
        assertTrue(sameSign(-100, -100));

        assertTrue(sameSign(Long.MAX_VALUE, 100));
        assertTrue(sameSign(100, Long.MAX_VALUE));
        assertFalse(sameSign(Long.MIN_VALUE, 100));
        assertFalse(sameSign(100, Long.MIN_VALUE));

        assertFalse(sameSign(Long.MAX_VALUE, -100));
        assertFalse(sameSign(-100, Long.MAX_VALUE));
        assertTrue(sameSign(Long.MIN_VALUE, -100));
        assertTrue(sameSign(-100, Long.MIN_VALUE));
    }

}
