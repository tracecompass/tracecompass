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
import static org.junit.Assert.*;

import org.eclipse.tracecompass.common.core.math.SaturatedArithmetic;
import org.junit.Test;

/**
 * Test suite for the {@link SaturatedArithmetic} All tests must test
 * reciprocity as well as low and high limits
 *
 * @author Matthew Khouzam
 */
public class SaturatedArithmeticTest {

    /**
     * test absorbtion
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
        assertEquals(0, multiply(Long.MAX_VALUE, 0));
        assertEquals(0, multiply(0, Long.MAX_VALUE));
        assertEquals(0, multiply(Long.MIN_VALUE, 0));
        assertEquals(0, multiply(0, Long.MIN_VALUE));
    }

    /**
     * test identity
     */
    @Test
    public void testMult1() {
        assertEquals(0, multiply(0, 1));
        assertEquals(1, multiply(1, 1));
        assertEquals(42, multiply(42, 1));
        assertEquals(42, multiply(1, 42));
        assertEquals(-42, multiply(-42, 1));
        assertEquals(-42, multiply(1, -42));
        assertEquals(Long.MAX_VALUE, multiply(Long.MAX_VALUE, 1));
        assertEquals(Long.MAX_VALUE, multiply(1, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, multiply(Long.MIN_VALUE, 1));
        assertEquals(Long.MIN_VALUE, multiply(1, Long.MIN_VALUE));
    }

    /**
     * test typical
     */
    @Test
    public void testMult100() {
        assertEquals(10000, multiply(100, 100));
        assertEquals(-10000, multiply(100, -100));
        assertEquals(-10000, multiply(-100, 100));
        assertEquals(10000, multiply(-100, -100));

        assertEquals(Long.MAX_VALUE, multiply(Long.MAX_VALUE, 100));
        assertEquals(Long.MAX_VALUE, multiply(100, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, multiply(Long.MIN_VALUE, 100));
        assertEquals(Long.MIN_VALUE, multiply(100, Long.MIN_VALUE));

        assertEquals(Long.MIN_VALUE, multiply(Long.MAX_VALUE, -100));
        assertEquals(Long.MIN_VALUE, multiply(-100, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, multiply(Long.MIN_VALUE, -100));
        assertEquals(Long.MAX_VALUE, multiply(-100, Long.MIN_VALUE));
    }

    /**
     * test limit
     */
    @Test
    public void testMultLimit() {
        assertEquals(Long.MAX_VALUE, multiply(Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, multiply(Long.MAX_VALUE, Long.MIN_VALUE));
        assertEquals(Long.MIN_VALUE, multiply(Long.MIN_VALUE, Long.MAX_VALUE));
        assertEquals(Long.MAX_VALUE, multiply(Long.MIN_VALUE, Long.MIN_VALUE));
    }

    /**
     * test identity
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

        assertEquals(Long.MAX_VALUE, add(Long.MAX_VALUE, 0));
        assertEquals(Long.MAX_VALUE, add(0, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, add(Long.MIN_VALUE, 0));
        assertEquals(Long.MIN_VALUE, add(0, Long.MIN_VALUE));
    }

    /**
     * test typical
     */
    @Test
    public void testAdd100() {
        assertEquals(200, add(100, 100));
        assertEquals(0, add(100, -100));
        assertEquals(0, add(-100, 100));
        assertEquals(-200, add(-100, -100));

        assertEquals(Long.MAX_VALUE, add(Long.MAX_VALUE, 100));
        assertEquals(Long.MAX_VALUE, add(100, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE + 100, add(Long.MIN_VALUE, 100));
        assertEquals(Long.MIN_VALUE + 100, add(100, Long.MIN_VALUE));

        assertEquals(Long.MAX_VALUE - 100, add(Long.MAX_VALUE, -100));
        assertEquals(Long.MAX_VALUE - 100, add(-100, Long.MAX_VALUE));
        assertEquals(Long.MIN_VALUE, add(Long.MIN_VALUE, -100));
        assertEquals(Long.MIN_VALUE, add(-100, Long.MIN_VALUE));
    }

    /**
     * test limit
     */
    @Test
    public void testAddLimit() {
        assertEquals(Long.MAX_VALUE, add(Long.MAX_VALUE, Long.MAX_VALUE));
        assertEquals(-1, add(Long.MAX_VALUE, Long.MIN_VALUE)); // min value is 1
                                                               // larger than
                                                               // max value
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
