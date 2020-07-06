/**********************************************************************
 * Copyright (c) 2020 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 **********************************************************************/

package org.eclipse.tracecompass.analysis.timing.core.tests.statistics;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.math.BigDecimal;
import java.math.BigInteger;

import org.eclipse.tracecompass.internal.analysis.timing.core.segmentstore.statistics.NumberComparator;
import org.junit.Test;

/**
 * Test Comparisons of {@link NumberComparator}. More to test the types than the
 * results
 *
 * @author Matthew Khouzam
 */
public class NumberComparatorTest {
    private static NumberComparator COMPARATOR = new NumberComparator();

    /**
     * Tests longs
     */
    @Test
    public void longTest() {
        Number negInf = Long.MIN_VALUE;
        Number posInf = Long.MAX_VALUE;
        Number zero = 0L;
        Number one = 1L;
        runTests(negInf, posInf, zero, one);
    }

    /**
     * Test integers
     */
    @Test
    public void intTest() {
        Number negInf = Integer.MIN_VALUE;
        Number posInf = Integer.MAX_VALUE;
        Number zero = 0;
        Number one = 1;
        runTests(negInf, posInf, zero, one);
    }

    /**
     * Test doubles
     */
    @Test
    public void doubleTest() {
        Number negInf = Double.MIN_VALUE;
        Number posInf = Double.MAX_VALUE;
        Number zero = 0.0;
        Number one = 1.0;
        runTests(negInf, posInf, zero, one);
    }

    /**
     * Test floats
     */
    @Test
    public void floatTest() {
        Number negInf = Float.MIN_VALUE;
        Number posInf = Float.MAX_VALUE;
        Number zero = 0.0f;
        Number one = 1.0f;
        runTests(negInf, posInf, zero, one);
    }

    /**
     * Tests shorts
     */
    @Test
    public void shortTest() {
        Number negInf = Short.MIN_VALUE;
        Number posInf = Short.MAX_VALUE;
        Number zero = (short) 0;
        Number one = (short) 1;
        runTests(negInf, posInf, zero, one);
    }

    /**
     * Test bytes
     */
    @Test
    public void byteTest() {
        Number negInf = Byte.MIN_VALUE;
        Number posInf = Byte.MAX_VALUE;
        Number zero = (byte) 0;
        Number one = (byte) 1;
        runTests(negInf, posInf, zero, one);
    }

    /**
     * Test big integers
     */
    @Test
    public void bigIntTest() {
        Number negInf = new BigInteger("-10000000000000000000000000000");
        Number posInf = new BigInteger("10000000000000000000000000000");
        Number zero = BigInteger.ZERO;
        Number one = BigInteger.ONE;
        runTests(negInf, posInf, zero, one);
    }

    /**
     * Test big decimals
     */
    @Test
    public void bigDecTest() {
        Number negInf = new BigDecimal("-10000000000000000000000000000");
        Number posInf = new BigDecimal("10000000000000000000000000000");
        Number zero = BigDecimal.ZERO;
        Number one = BigDecimal.ONE;
        runTests(negInf, posInf, zero, one);
    }

    /**
     * Mixed types not supported
     */
    @Test(expected = IllegalArgumentException.class)
    public void failLITest() {
        assertComparison(0, 0, 0L);
    }

    /**
     * Mixed types not supported
     */
    @Test(expected = IllegalArgumentException.class)
    public void failILTest() {
        assertComparison(0, 0L, 0);
    }

    /**
     * Mixed types not supported
     */
    @Test(expected = IllegalArgumentException.class)
    public void failDFTest() {
        assertComparison(0, 0.0, 0.0f);
    }

    /**
     * Mixed types not supported
     */
    @Test(expected = IllegalArgumentException.class)
    public void failFDTest() {
        assertComparison(0, 0.0f, 0.0);
    }

    /**
     * Mixed types not supported
     */
    @Test(expected = IllegalArgumentException.class)
    public void failSBTest() {
        assertComparison(0, (short) 0, (byte) 0);
    }

    /**
     * Mixed types not supported
     */
    @Test(expected = IllegalArgumentException.class)
    public void failBSTest() {
        assertComparison(0, (byte) 0, (short) 0);
    }

    /**
     * Mixed types not supported
     */
    @Test(expected = IllegalArgumentException.class)
    public void failBiBdTest() {
        assertComparison(0, BigInteger.ZERO, BigDecimal.ZERO);
    }

    /**
     * Mixed types not supported
     */
    @Test(expected = IllegalArgumentException.class)
    public void failBdBiTest() {
        assertComparison(0, BigDecimal.ZERO, BigInteger.ZERO);
    }

    /**
     * Basic test, boundary and simple sanity.
     *
     * @param negInf
     *            negative infinite if applicable
     * @param posInf
     *            positive infinite if applicable
     * @param zero
     *            zero
     * @param one
     *            one (non-zero non huge number)
     */
    private static void runTests(Number negInf, Number posInf, Number zero, Number one) {
        assertComparison(0, negInf, negInf);
        assertComparison(0, posInf, posInf);
        assertComparison(0, zero, zero);
        assertComparison(0, one, one);
        assertComparison(1, posInf, negInf);
        assertComparison(-1, negInf, posInf);
        assertComparison(1, one, zero);
        assertComparison(-1, zero, one);
    }

    /**
     * Assert comparison
     *
     * @param expected
     *            result, 0, -1 or +1, if it's less than 0, all less than zero
     *            returned values are valid, same applies for greater than zero.
     * @param left
     *            left operand
     * @param right
     *            right operand
     */
    private static void assertComparison(int expected, Number left, Number right) {
        int actual = COMPARATOR.compare(left, right);
        if (expected < 0) {
            assertTrue("Number " + left + " expected to be smaller than " + right, actual < 0);
        } else if (expected > 0) {
            assertTrue("Number " + left + " expected to be larger than " + right, actual > 0);
        } else {
            assertEquals("Number " + left + " expected to be equal to " + right, 0, actual);
        }
    }
}
