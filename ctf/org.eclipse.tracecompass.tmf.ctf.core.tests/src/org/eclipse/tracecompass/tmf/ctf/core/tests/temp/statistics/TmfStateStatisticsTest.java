/*******************************************************************************
 * Copyright (c) 2012, 2014 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 ******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.temp.statistics;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStateStatistics;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsEventTypesModule;
import org.eclipse.tracecompass.tmf.core.statistics.TmfStatisticsTotalsModule;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.After;
import org.junit.Before;

/**
 * Unit tests for the {@link TmfStateStatistics}
 *
 * @author Alexandre Montplaisir
 */
public class TmfStateStatisticsTest extends TmfStatisticsTest {

    private ITmfTrace fTrace;

    private TmfStatisticsTotalsModule fTotalsMod;
    private TmfStatisticsEventTypesModule fEventTypesMod;

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        ITmfTrace trace = CtfTmfTestTraceUtils.getTrace(testTrace);
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

        backend = new TmfStateStatistics(totalsSS, eventTypesSS);
    }

    /**
     * Test cleanup
     */
    @After
    public void tearDown() {
        fTotalsMod.dispose();
        fEventTypesMod.dispose();
        TmfTraceManager.deleteSupplementaryFiles(NonNullUtils.checkNotNull(fTrace));
        fTrace.dispose();
    }
}
