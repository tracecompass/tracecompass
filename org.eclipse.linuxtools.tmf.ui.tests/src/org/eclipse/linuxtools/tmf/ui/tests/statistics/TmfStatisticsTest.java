package org.eclipse.linuxtools.tmf.ui.tests.statistics;

import org.eclipse.linuxtools.tmf.ui.viewers.statistics.model.TmfStatistics;

import junit.framework.TestCase;

/**
 * TmfStatistics Test Cases.
 */
public class TmfStatisticsTest extends TestCase {

    // ------------------------------------------------------------------------
    // Fields
    // ------------------------------------------------------------------------

    TmfStatistics stats = new TmfStatistics();

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
     * Test incrementing the total counter
     */
    public void testIncrementTotal() {
        for (int i = 1; i < 10; ++i) {
            stats.incrementTotal();
            assertEquals(i, stats.getTotal());
        }
        // Checks if the partial counter was affected
        assertEquals(0, stats.getPartial());
    }

    /**
     * Test incrementing the total counter by an amount
     */
    public void testIncrementTotal1Arg() {
        int i = 1, expected = 0;
        while (expected < 100) {
            expected += i;
            stats.incrementTotal(i);
            assertEquals(expected, stats.getTotal());
            i += i;
        }
        // Increment by a negative number do nothing
        stats.incrementTotal(-10);
        assertEquals(expected, stats.getTotal());

        // Checks if the partial counter was affected
        assertEquals(0, stats.getPartial());
    }

    /**
     * Test incrementing the partial counter
     */
    public void testIncrementPartial() {
        for (int i = 1; i < 10; ++i) {
            stats.incrementPartial();
            assertEquals(i, stats.getPartial());
        }
        // Checks if the total counter was affected
        assertEquals(0, stats.getTotal());
    }

    /**
     * Test incrementing the partial counter by a certain amount
     */
    public void testIncrementPartial1Arg() {
        int i = 1, expected = 0;
        while (expected < 100) {
            expected += i;
            stats.incrementPartial(i);
            assertEquals(expected, stats.getPartial());
            i += i;
        }
        // Increment by a negative number. It should do nothing.
        stats.incrementPartial(-10);
        assertEquals(expected, stats.getPartial());

        // Checks if the total counter was affected
        assertEquals(0, stats.getTotal());
    }

    /**
     * Test of the reset for the total counter
     */
    public void testResetTotal() {
        stats.incrementTotal(123);
        assertEquals(123, stats.getTotal());

        stats.resetTotalCount();
        assertEquals(0, stats.getTotal());

        // test when already at 0
        stats.resetTotalCount();
        assertEquals(0, stats.getTotal());

        // The counters should still be in a usable state
        stats.incrementPartial();
        stats.incrementPartial(3);
        assertEquals(4, stats.getPartial());

        stats.incrementTotal();
        stats.incrementTotal(2);
        assertEquals(3, stats.getTotal());
    }

    /**
     * Test of the reset for the partial counter
     */
    public void testResetPartial() {
        stats.incrementPartial(456);
        assertEquals(456, stats.getPartial());

        stats.resetPartialCount();
        assertEquals(0, stats.getPartial());

        // test when already at 0
        stats.resetPartialCount();
        assertEquals(0, stats.getPartial());

        // The counters should still be in a usable state
        stats.incrementPartial();
        stats.incrementPartial(2);
        assertEquals(3, stats.getPartial());

        stats.incrementTotal();
        stats.incrementTotal(3);
        assertEquals(4, stats.getTotal());
    }
}
