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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import java.util.Map;

import org.eclipse.linuxtools.ctf.core.CTFStrings;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statistics.ITmfStatistics;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStateStatistics;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStatisticsTotalsModule;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Unit tests for handling of lost events by the statistics backends.
 *
 * @author Alexandre Montplaisir
 */
public class TmfLostEventStatisticsTest {

    /** Time-out tests after 30 seconds */
    @Rule
    public TestRule globalTimeout= new Timeout(30000);

    /**Test trace with lost events */
    private static final CtfTmfTestTrace lostEventsTrace = CtfTmfTestTrace.HELLO_LOST;

    private ITmfTrace fTrace;

    /** The statistics back-end object for the trace with lost events */
    private ITmfStatistics fStats;

    // ------------------------------------------------------------------------
    // Maintenance
    // ------------------------------------------------------------------------

    /**
     * Class setup
     */
    @BeforeClass
    public static void setUpClass() {
        assumeTrue(lostEventsTrace.exists());
    }

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        fTrace = lostEventsTrace.getTrace();

        /* Prepare the two analysis-backed state systems */
        TmfStatisticsTotalsModule totalsMod = new TmfStatisticsTotalsModule();
        TmfStatisticsEventTypesModule eventTypesMod = new TmfStatisticsEventTypesModule();
        try {
            totalsMod.setTrace(fTrace);
            eventTypesMod.setTrace(fTrace);
        } catch (TmfAnalysisException e) {
            fail();
        }

        totalsMod.schedule();
        eventTypesMod.schedule();
        assertTrue(totalsMod.waitForCompletion());
        assertTrue(eventTypesMod.waitForCompletion());

        ITmfStateSystem totalsSS = totalsMod.getStateSystem();
        ITmfStateSystem eventTypesSS = eventTypesMod.getStateSystem();
        assertNotNull(totalsSS);
        assertNotNull(eventTypesSS);

        fStats = new TmfStateStatistics(totalsSS, eventTypesSS);
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
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
        long lostEventsInRange = eventsInRange.get(CTFStrings.LOST_EVENT_NAME);
        assertEquals(363494L, lostEventsInRange);
    }
}
