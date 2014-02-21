/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.statistics;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Base unit test class for any type of ITmfStatistics. Sub-classes should
 * implement a "@BeforeClass" method to setup the 'backend' fixture accordingly.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TmfStatisticsTest {

    /** Time-out tests after 30 seconds */
    @Rule public TestRule globalTimeout= new Timeout(30000);

    /** Test trace used for these tests */
    protected static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.KERNEL;

    /** The statistics back-end object */
    protected static ITmfStatistics backend;

    /* Known values about the trace */
    private static final int totalNbEvents = 695319;
    private static final long tStart = 1332170682440133097L; /* Timestamp of first event */
    private static final long tEnd   = 1332170692664579801L; /* Timestamp of last event */

    /* Timestamps of interest */
    private static final long t1 = 1332170682490946000L;
    private static final long t2 = 1332170682490947524L; /* event exactly here */
    private static final long t3 = 1332170682490948000L;
    private static final long t4 = 1332170682490949000L;
    private static final long t5 = 1332170682490949270L; /* following event here */
    private static final long t6 = 1332170682490949300L;

    private static final String eventType = "lttng_statedump_process_state";


    // ------------------------------------------------------------------------
    // Tests for histogramQuery()
    // ------------------------------------------------------------------------

    /**
     * Test the {@link ITmfStatistics#histogramQuery} method for the small known
     * interval.
     */
    @Test
    public void testHistogramQuerySmall() {
        final int NB_REQ = 10;
        List<Long> results = backend.histogramQuery(t1, t6, NB_REQ);

        /* Make sure the returned array has the right size */
        assertEquals(NB_REQ, results.size());

        /* Check the contents of each "bucket" */
        assertEquals(0, results.get(0).longValue());
        assertEquals(0, results.get(1).longValue());
        assertEquals(0, results.get(2).longValue());
        assertEquals(0, results.get(3).longValue());
        assertEquals(1, results.get(4).longValue());
        assertEquals(0, results.get(5).longValue());
        assertEquals(0, results.get(6).longValue());
        assertEquals(0, results.get(7).longValue());
        assertEquals(0, results.get(8).longValue());
        assertEquals(1, results.get(9).longValue());

    }

    /**
     * Test the {@link ITmfStatistics#histogramQuery} method over the whole
     * trace.
     */
    @Test
    public void testHistogramQueryFull() {
        final int NB_REQ = 10;
        List<Long> results = backend.histogramQuery(tStart, tEnd, NB_REQ);

        /* Make sure the returned array has the right size */
        assertEquals(NB_REQ, results.size());

        /* Check the total number of events */
        long count = 0;
        for (long val : results) {
            count += val;
        }
        assertEquals(totalNbEvents, count);

        /* Check the contents of each "bucket" */
        assertEquals(94161, results.get(0).longValue());
        assertEquals(87348, results.get(1).longValue());
        assertEquals(58941, results.get(2).longValue());
        assertEquals(59879, results.get(3).longValue());
        assertEquals(66941, results.get(4).longValue());
        assertEquals(68939, results.get(5).longValue());
        assertEquals(72746, results.get(6).longValue());
        assertEquals(60749, results.get(7).longValue());
        assertEquals(61208, results.get(8).longValue());
        assertEquals(64407, results.get(9).longValue());
    }

    // ------------------------------------------------------------------------
    // Test for getEventsTotal()
    // ------------------------------------------------------------------------

    /**
     * Basic test for {@link ITmfStatistics#getEventsTotal}
     */
    @Test
    public void testGetEventsTotal() {
        long count = backend.getEventsTotal();
        assertEquals(totalNbEvents, count);
    }

    // ------------------------------------------------------------------------
    // Test for getEventTypesTotal()
    // ------------------------------------------------------------------------

    /**
     * Basic test for {@link ITmfStatistics#getEventTypesTotal}
     */
    @Test
    public void testEventTypesTotal() {
        Map<String, Long> res = backend.getEventTypesTotal();
        assertEquals(126, res.size()); /* Number of different event types in the trace */

        long count = sumOfEvents(res);
        assertEquals(totalNbEvents, count);
    }

    // ------------------------------------------------------------------------
    // Tests for getEventsInRange(ITmfTimestamp start, ITmfTimestamp end)
    // ------------------------------------------------------------------------

    /**
     * Test for {@link ITmfStatistics#getEventsInRange} over the whole trace.
     */
    @Test
    public void testGetEventsInRangeWholeRange() {
        long count = backend.getEventsInRange(tStart, tEnd);
        assertEquals(totalNbEvents, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventsInRange} for the whole range,
     * except the start time (there is only one event at the start time).
     */
    @Test
    public void testGetEventsInRangeMinusStart() {
        long count = backend.getEventsInRange(tStart + 1, tEnd);
        assertEquals(totalNbEvents - 1, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventsInRange} for the whole range,
     * except the end time (there is only one event at the end time).
     */
    @Test
    public void testGetEventsInRangeMinusEnd() {
        long count = backend.getEventsInRange(tStart, tEnd - 1);
        assertEquals(totalNbEvents - 1, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventsInRange} when both the start and
     * end times don't match an event.
     */
    @Test
    public void testGetEventsInRangeNoEventsAtEdges() {
        long count = backend.getEventsInRange(t1, t6);
        assertEquals(2, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventsInRange} when the *start* of the
     * interval is exactly on an event (that event should be included).
     */
    @Test
    public void testGetEventsInRangeEventAtStart() {
        long count = backend.getEventsInRange(t2, t3);
        assertEquals(1, count);

        count = backend.getEventsInRange(t2, t6);
        assertEquals(2, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventsInRange} when the *end* of the
     * interval is exactly on an event (that event should be included).
     */
    @Test
    public void testGetEventsInRangeEventAtEnd() {
        long count = backend.getEventsInRange(t4, t5);
        assertEquals(1, count);

        count = backend.getEventsInRange(t1, t5);
        assertEquals(2, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventsInRange} when there are events
     * matching exactly both the start and end times of the range (both should
     * be included).
     */
    @Test
    public void testGetEventsInRangeEventAtBoth() {
        long count = backend.getEventsInRange(t2, t5);
        assertEquals(2, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventsInRange} when there are no events
     * in a given range.
     */
    @Test
    public void testGetEventsInRangeNoEvents() {
        long count = backend.getEventsInRange(t3, t4);
        assertEquals(0, count);
    }

    // ------------------------------------------------------------------------
    // Tests for getEventTypesInRange(ITmfTimestamp start, ITmfTimestamp end)
    // ------------------------------------------------------------------------

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange} over the whole trace.
     */
    @Test
    public void testGetEventTypesInRangeWholeRange() {
        Map<String, Long> result = backend.getEventTypesInRange(tStart, tEnd);
        /* Number of events of that type in the whole trace */
        assertEquals(new Long(464L), result.get(eventType));

        long count = sumOfEvents(result);
        assertEquals(totalNbEvents, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange} for the whole range,
     * except the start time (there is only one event at the start time).
     */
    @Test
    public void testGetEventTypesInRangeMinusStart() {
        Map<String, Long> result = backend.getEventTypesInRange(tStart + 1, tEnd);

        long count = sumOfEvents(result);
        assertEquals(totalNbEvents - 1, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange} for the whole range,
     * except the end time (there is only one event at the end time).
     */
    @Test
    public void testGetEventTypesInRangeMinusEnd() {
        Map<String, Long> result = backend.getEventTypesInRange(tStart, tEnd - 1);

        long count = sumOfEvents(result);
        assertEquals(totalNbEvents - 1, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange} when both the start
     * and end times don't match an event.
     */
    @Test
    public void testGetEventTypesInRangeNoEventsAtEdges() {
        Map<String, Long> result = backend.getEventTypesInRange(t1, t6);
        assertEquals(new Long(2L), result.get(eventType));

        long count = sumOfEvents(result);
        assertEquals(2, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange} when the *start* of
     * the interval is exactly on an event (that event should be included).
     */
    @Test
    public void testGetEventTypesInRangeEventAtStart() {
        Map<String, Long> result = backend.getEventTypesInRange(t2, t3);
        assertEquals(new Long(1L), result.get(eventType));
        long count = sumOfEvents(result);
        assertEquals(1, count);

        result = backend.getEventTypesInRange(t2, t6);
        assertEquals(new Long(2L), result.get(eventType));
        count = sumOfEvents(result);
        assertEquals(2, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange} when the *end* of
     * the interval is exactly on an event (that event should be included).
     */
    @Test
    public void testGetEventTypesInRangeEventAtEnd() {
        Map<String, Long> result = backend.getEventTypesInRange(t4, t5);
        assertEquals(new Long(1L), result.get(eventType));
        long count = sumOfEvents(result);
        assertEquals(1, count);

        result = backend.getEventTypesInRange(t1, t5);
        assertEquals(new Long(2L), result.get(eventType));
        count = sumOfEvents(result);
        assertEquals(2, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange} when there are
     * events matching exactly both the start and end times of the range (both
     * should be included).
     */
    @Test
    public void testGetEventTypesInRangeEventAtBoth() {
        Map<String, Long> result = backend.getEventTypesInRange(t2, t5);
        assertEquals(new Long(2L), result.get(eventType));
        long count = sumOfEvents(result);
        assertEquals(2, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange} when there are no
     * events in a given range.
     */
    @Test
    public void testGetEventTypesInRangeNoEvents() {
        Map<String, Long> result = backend.getEventTypesInRange(t3, t4);
        long count = sumOfEvents(result);
        assertEquals(0, count);
    }

    // ------------------------------------------------------------------------
    // Convenience methods
    // ------------------------------------------------------------------------

    private static long sumOfEvents(Map<String, Long> map) {
        long count = 0;
        for (long val : map.values()) {
            count += val;
        }
        return count;
    }
}
