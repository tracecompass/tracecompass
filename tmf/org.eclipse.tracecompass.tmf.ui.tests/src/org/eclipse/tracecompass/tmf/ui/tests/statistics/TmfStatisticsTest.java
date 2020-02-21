/*******************************************************************************
 * Copyright (c) 2013, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Port to JUnit4
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ui.tests.statistics;

import static org.junit.Assert.assertEquals;

import org.eclipse.tracecompass.internal.tmf.ui.viewers.statistics.model.TmfStatisticsValues;
import org.junit.Test;

/**
 * TmfStatistics Test Cases.
 */
public class TmfStatisticsTest {

    private TmfStatisticsValues stats = new TmfStatisticsValues();

    /**
     * Test the initial state of the counters
     */
    @Test
    public void testInitialState() {
        assertEquals(0, stats.getTotal());
        assertEquals(0, stats.getPartial());
    }

    /**
     * Test incrementing the total counter by an amount
     */
    @Test
    public void testSetValue() {
        final int i = 100;

        /* Set the Global counter */
        stats.setValue(true, i);
        assertEquals(i, stats.getTotal());
        // Try to assign a negative number. Should do nothing.
        stats.setValue(true, -10);
        assertEquals(i, stats.getTotal());
        // Checks if the partial counter was affected
        assertEquals(0, stats.getPartial());

        /* Set the time range counter */
        stats.resetTotalCount();
        stats.setValue(false, i);
        assertEquals(i, stats.getPartial());
        // Try to assign a negative number. Should do nothing.
        stats.setValue(false, -10);
        assertEquals(i, stats.getPartial());
        // Checks if the total counter was affected
        assertEquals(0, stats.getTotal());
    }

    /**
     * Test of the reset for the total counter
     */
    @Test
    public void testResetTotal() {
        stats.setValue(true, 123);
        assertEquals(123, stats.getTotal());

        stats.resetTotalCount();
        assertEquals(0, stats.getTotal());

        // test when already at 0
        stats.resetTotalCount();
        assertEquals(0, stats.getTotal());
    }

    /**
     * Test of the reset for the partial counter
     */
    @Test
    public void testResetPartial() {
        stats.setValue(false, 456);
        assertEquals(456, stats.getPartial());

        stats.resetPartialCount();
        assertEquals(0, stats.getPartial());

        // test when already at 0
        stats.resetPartialCount();
        assertEquals(0, stats.getPartial());
    }
}
