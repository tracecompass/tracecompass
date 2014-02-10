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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStateStatistics;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.linuxtools.tmf.core.statistics.TmfStatisticsTotalsModule;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;

/**
 * Unit tests for the {@link TmfStateStatistics}
 *
 * @author Alexandre Montplaisir
 */
public class TmfStateStatisticsTest extends TmfStatisticsTest {

    private ITmfTrace fTrace;

    /**
     * Class setup
     */
    @BeforeClass
    public static void setUpClass() {
        assumeTrue(testTrace.exists());
    }

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        fTrace = testTrace.getTrace();

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

        backend = new TmfStateStatistics(totalsSS, eventTypesSS);
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        fTrace.dispose();
    }
}
