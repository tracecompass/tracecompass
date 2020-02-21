/*******************************************************************************
 * Copyright (c) 2017 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.chart.ui.tests.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.math.BigDecimal;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.tmf.chart.ui.data.ChartRange;
import org.junit.Test;

/**
 * Test the {@link ChartRange} class
 *
 * @author Geneviève Bastien
 */
public class ChartRangeTest {

    private static final @NonNull BigDecimal ZERO = BigDecimal.ZERO;
    private static final @NonNull BigDecimal ONE = BigDecimal.ONE;
    private static final @NonNull BigDecimal TEN = BigDecimal.TEN;

    /**
     * Test the default constructor
     */
    @Test
    public void testDefaultConstructor() {
        ChartRange cr = new ChartRange();
        assertEquals(BigDecimal.ZERO, cr.getMinimum());
        assertEquals(BigDecimal.ONE, cr.getMaximum());
        assertEquals(BigDecimal.ONE, cr.getDelta());
    }

    /**
     * Test the constructor with parameters
     */
    @Test
    public void testConstructor() {
        // Test the constructor with 2 different values
        ChartRange cr = new ChartRange(ONE, TEN);
        assertEquals(ONE, cr.getMinimum());
        assertEquals(TEN, cr.getMaximum());
        assertEquals(new BigDecimal(9), cr.getDelta());

        // Test the constructor with 2 identical values
        cr = new ChartRange(ONE, ONE);
        assertEquals(BigDecimal.ONE, cr.getMinimum());
        assertEquals(new BigDecimal(2), cr.getMaximum());
        assertEquals(BigDecimal.ONE, cr.getDelta());
    }

    /**
     * Test the constructor when the maximum is lower than the minimum
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidConstructor() {
        ChartRange cr = new ChartRange(ONE, ZERO);
        assertNotNull(cr);
    }

    /**
     * Test the {@link ChartRange#clamp()} method
     */
    @Test
    public void testClamp() {
        BigDecimal minusTwo = new BigDecimal(-2);

        // Test a range that starts in the positive, that should be clamped to
        // zero
        ChartRange cr = new ChartRange(ONE, TEN);
        assertEquals(ONE, cr.getMinimum());
        assertEquals(TEN, cr.getMaximum());
        assertEquals(new BigDecimal(9), cr.getDelta());
        cr.clamp();
        assertEquals(ZERO, cr.getMinimum());
        assertEquals(TEN, cr.getMaximum());
        assertEquals(TEN, cr.getDelta());

        // Test a range in the negative, that should not be affected by the
        // clamp
        cr = new ChartRange(minusTwo, ONE);
        assertEquals(minusTwo, cr.getMinimum());
        assertEquals(ONE, cr.getMaximum());
        assertEquals(new BigDecimal(3), cr.getDelta());
        cr.clamp();
        assertEquals(minusTwo, cr.getMinimum());
        assertEquals(ONE, cr.getMaximum());
        assertEquals(new BigDecimal(3), cr.getDelta());
    }
}
