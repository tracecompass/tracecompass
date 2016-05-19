/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.latency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.latency.SystemCallLatencyAnalysis;
import org.eclipse.tracecompass.segmentstore.core.ISegment;
import org.eclipse.tracecompass.segmentstore.core.ISegmentStore;
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
 * Tests for the system call analysis
 *
 * @author Matthew Khouzam
 */
public class SyscallAnalysisTest {

    private SystemCallLatencyAnalysis fSyscallModule;

    /**
     * Test setup
     */
    @Before
    public void setUp() {
        ITmfTrace trace = KernelCtfTraceStub.getTrace(CtfTestTrace.ARM_64_BIT_HEADER);
        /* Make sure the Kernel analysis has run */
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, SystemCallLatencyAnalysis.class)) {
            fSyscallModule = (SystemCallLatencyAnalysis) mod;
        }
        assertNotNull(fSyscallModule);
        fSyscallModule.schedule();
        fSyscallModule.waitForCompletion();
    }

    /**
     * Dispose everything
     */
    @After
    public void cleanup() {
        final SystemCallLatencyAnalysis syscallModule = fSyscallModule;
        if( syscallModule != null) {
            syscallModule.dispose();
        }
    }

    /**
     * This will load the analysis and test it. as it depends on Kernel, this
     * test runs the kernel trace first then the analysis
     */
    @Test
    public void testSmallTraceSequential() {
        final SystemCallLatencyAnalysis syscallModule = fSyscallModule;
        assertNotNull(syscallModule);
        ISegmentStore<@NonNull ISegment> segmentStore = syscallModule.getSegmentStore();
        assertNotNull(segmentStore);
        assertEquals(1801, segmentStore.size());
    }
}
