/*******************************************************************************
 * Copyright (c) 2012, 2017 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
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
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Unit tests for the {@link TmfStateStatistics}
 *
 * @author Alexandre Montplaisir
 */
public class TmfStateStatisticsTest extends TmfStatisticsTest {

    private static ITmfTrace fTrace;

    private static TmfStatisticsTotalsModule fTotalsMod;
    private static TmfStatisticsEventTypesModule fEventTypesMod;

    /**
     * Test setup
     */
    @BeforeClass
    public static void setUp() {
        ITmfTrace trace = CtfTmfTestTraceUtils.getTrace(testTrace);
        fTrace = trace;

        /* Prepare the two analysis-backed state systems */
        fTotalsMod = new TmfStatisticsTotalsModule();
        fEventTypesMod = new TmfStatisticsEventTypesModule();
        try {
            fTotalsMod.setTrace(trace);
            fEventTypesMod.setTrace(trace);
        } catch (TmfAnalysisException e) {
            fail(e.getMessage());
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
    @AfterClass
    public static void tearDown() {
        fTotalsMod.dispose();
        fEventTypesMod.dispose();
        fTrace.dispose();
        TmfTraceManager.deleteSupplementaryFiles(NonNullUtils.checkNotNull(fTrace));
    }
}
