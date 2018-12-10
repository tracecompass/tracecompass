/*******************************************************************************
 * Copyright (c) 2012, 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.event;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.ctf.core.CTFStrings;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statistics.ITmfStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStateStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsTotalsModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Unit tests for handling of lost events by the statistics backends.
 *
 * @author Alexandre Montplaisir
 */
public class CtfTmfLostEventStatisticsTest {

    /** Time-out tests after 1 minute */
    @Rule
    public TestRule globalTimeout = new Timeout(1, TimeUnit.MINUTES);

    /**Test trace with lost events */
    private static final @NonNull CtfTestTrace lostEventsTrace = CtfTestTrace.HELLO_LOST;

    private ITmfTrace fTrace;

    /** The statistics back-end object for the trace with lost events */
    private ITmfStatistics fStats;

    /* The two analysis modules needed for fStats */
    private TmfStatisticsTotalsModule fTotalsMod;
    private TmfStatisticsEventTypesModule fEventTypesMod;

    // ------------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------------

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        ITmfTrace trace = CtfTmfTestTraceUtils.getTrace(lostEventsTrace);
        fTrace = trace;

        /* Prepare the two analysis-backed state systems */
        fTotalsMod = new TmfStatisticsTotalsModule();
        fEventTypesMod = new TmfStatisticsEventTypesModule();
        try {
            fTotalsMod.setTrace(trace);
            fEventTypesMod.setTrace(trace);
        } catch (TmfAnalysisException e) {
            fail();
        }

        fTotalsMod.schedule();
        fEventTypesMod.schedule();
        assertTrue(fTotalsMod.waitForCompletion());
        assertTrue(fEventTypesMod.waitForCompletion());

        ITmfStateSystem totalsSS = fTotalsMod.getStateSystem();
        ITmfStateSystem eventTypesSS = fEventTypesMod.getStateSystem();
        assertNotNull(totalsSS);
        assertNotNull(eventTypesSS);

        fStats = new TmfStateStatistics(totalsSS, eventTypesSS);
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        fStats.dispose();
        fTotalsMod.dispose();
        fEventTypesMod.dispose();
        fTrace.dispose();
    }

    // ------------------------------------------------------------------------
    // Test methods
    // ------------------------------------------------------------------------

    /*
     * Trace start = 1376592664828559410
     * Trace end   = 1376592665108210547
     */

    private static final long rangeStart = 1376592664900000000L;
    private static final long rangeEnd =   1376592665000000000L;

    /**
     * Test the total number of "real" events. Make sure the lost events aren't
     * counted in the total.
     */
    @Test
    public void testLostEventsTotals() {
        long realEvents = fStats.getEventsTotal();
        assertEquals(32300, realEvents);
    }

    /**
     * Test the number of real events in a given range. Lost events shouldn't be
     * counted.
     */
    @Test
    public void testLostEventsTotalInRange() {
        long realEventsInRange = fStats.getEventsInRange(rangeStart, rangeEnd);
        assertEquals(11209L, realEventsInRange);
    }

    /**
     * Test the total number of lost events reported in the trace.
     */
    @Test
    public void testLostEventsTypes() {
        Map<String, Long> events = fStats.getEventTypesTotal();
        Long lostEvents = events.get(CTFStrings.LOST_EVENT_NAME);
        assertEquals(Long.valueOf(967700L), lostEvents);
    }

    /**
     * Test the number of lost events reported in a given range.
     */
    @Test
    public void testLostEventsTypesInRange() {
        Map<String, Long> eventsInRange = fStats.getEventTypesInRange(rangeStart, rangeEnd);
        Long lostEventsInRange = eventsInRange.get(CTFStrings.LOST_EVENT_NAME);
        assertNotNull(lostEventsInRange);
        assertEquals(365752L, lostEventsInRange.longValue());
    }
}
