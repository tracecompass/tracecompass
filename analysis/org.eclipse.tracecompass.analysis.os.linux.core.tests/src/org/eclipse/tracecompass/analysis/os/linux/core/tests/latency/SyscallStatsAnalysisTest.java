/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.latency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.tracecompass.analysis.timing.core.segmentstore.statistics.SegmentStoreStatistics;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.statistics.SystemCallLatencyStatisticsAnalysisModule;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the system call statistics analysis
 *
 * @author Matthew Khouzam
 */
public class SyscallStatsAnalysisTest {

    private TmfTrace fTestTrace;
    private SystemCallLatencyStatisticsAnalysisModule fSyscallStatsModule;

    /**
     * Create the fixtures
     */
    @Before
    public void setupAnalysis() {
        ITmfTrace trace = KernelCtfTraceStub.getTrace(CtfTestTrace.ARM_64_BIT_HEADER);
        /* Make sure the Kernel analysis has run */
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, SystemCallLatencyAnalysis.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        SystemCallLatencyStatisticsAnalysisModule syscallStatsModule = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, SystemCallLatencyStatisticsAnalysisModule.class)) {
            syscallStatsModule = (SystemCallLatencyStatisticsAnalysisModule) mod;
        }
        assertNotNull(syscallStatsModule);
        syscallStatsModule.schedule();
        syscallStatsModule.waitForCompletion();
        fSyscallStatsModule = syscallStatsModule;
    }

    /**
     * Dispose everything
     */
    @After
    public void cleanup() {
        final TmfTrace testTrace = fTestTrace;
        if (testTrace != null) {
            testTrace.dispose();
        }
    }

    /**
     * This will load the analysis and test it. as it depends on Kernel, this
     * test runs the kernel trace first then the analysis
     */
    @Test
    public void testSmallTraceSequential() {
        final SystemCallLatencyStatisticsAnalysisModule syscallStatsModule = fSyscallStatsModule;
        assertNotNull(syscallStatsModule);
        SegmentStoreStatistics totalStats = syscallStatsModule.getTotalStats();
        assertNotNull(totalStats);
        assertEquals(1801, totalStats.getNbSegments());
        assertEquals(5904091700L, totalStats.getMax());
    }
}
