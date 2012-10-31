/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import java.util.Map;

import org.eclipse.linuxtools.tmf.core.event.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.event.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.junit.Test;

/**
 * Base unit test class for any type of ITmfStatistics. Sub-classes should
 * implement a "@BeforeClass" method to setup the 'backend' fixture accordingly.
 *
 * @author Alexandre Montplaisir
 */
public abstract class TmfStatisticsTest {

    protected static ITmfStatistics backend;

    /* Known values about the trace */
    private static final int SCALE = ITmfTimestamp.NANOSECOND_SCALE;
    private static final int totalNbEvents = 695319;
    private static final ITmfTimestamp tStart = new TmfTimestamp(1332170682440133097L, SCALE); /* Timestamp of first event */
    private static final ITmfTimestamp tEnd   = new TmfTimestamp(1332170692664579801L, SCALE); /* Timestamp of last event */

    /* Timestamps of interest */
    private static final ITmfTimestamp t1 = new TmfTimestamp(1332170682490946000L, SCALE);
    private static final ITmfTimestamp t2 = new TmfTimestamp(1332170682490947524L, SCALE); /* event exactly here */
    private static final ITmfTimestamp t3 = new TmfTimestamp(1332170682490948000L, SCALE);
    private static final ITmfTimestamp t4 = new TmfTimestamp(1332170682490949000L, SCALE);
    private static final ITmfTimestamp t5 = new TmfTimestamp(1332170682490949270L, SCALE); /* following event here */
    private static final ITmfTimestamp t6 = new TmfTimestamp(1332170682490949300L, SCALE);

    private static final String eventType = "lttng_statedump_process_state"; //$NON-NLS-1$


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
        long count = backend.getEventsInRange(new TmfTimestamp(tStart.getValue() + 1, SCALE), tEnd);
        assertEquals(totalNbEvents - 1, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventsInRange} for the whole range,
     * except the end time (there is only one event at the end time).
     */
    @Test
    public void testGetEventsInRangeMinusEnd() {
        long count = backend.getEventsInRange(tStart, new TmfTimestamp(tEnd.getValue() - 1, SCALE));
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
        ITmfTimestamp newStart = new TmfTimestamp(tStart.getValue() + 1, SCALE);
        Map<String, Long> result = backend.getEventTypesInRange(newStart, tEnd);

        long count = sumOfEvents(result);
        assertEquals(totalNbEvents - 1, count);
    }

    /**
     * Test for {@link ITmfStatistics#getEventTypesInRange} for the whole range,
     * except the end time (there is only one event at the end time).
     */
    @Test
    public void testGetEventTypesInRangeMinusEnd() {
        ITmfTimestamp newEnd = new TmfTimestamp(tEnd.getValue() - 1, SCALE);
        Map<String, Long> result = backend.getEventTypesInRange(tStart, newEnd);

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
        for (Long val : map.values()) {
            count += val;
        }
        return count;
    }
}
