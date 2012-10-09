package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatisticsValues;

import junit.framework.TestCase;

/**
 * TmfStatistics Test Cases.
 */
public class TmfStatisticsTest extends TestCase {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    TmfStatisticsValues stats = new TmfStatisticsValues();

    // ------------------------------------------------------------------------
    // Checks initial state
    // ------------------------------------------------------------------------

    /**
     * Test the initial state of the counters
     */
    public void testInitialState() {
        assertEquals(0, stats.getTotal());
        assertEquals(0, stats.getPartial());
    }

    // ------------------------------------------------------------------------
    // Increment Total no parameter
    // ------------------------------------------------------------------------

    /**
     * Test incrementing the total counter by an amount
     */
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
