/*******************************************************************************
 * Copyright (c) 2015 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.contextswitch;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.contextswitch.KernelContextSwitchAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.tests.shared.TmfTestHelper;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test suite for the {@link KernelContextSwitchAnalysis} class
 *
 * @author Matthew Khouzam
 */
public class ContextSwitchProviderTest {

    private static final String CPU_USAGE_FILE = "testfiles/cpu_analysis.xml";

    private ITmfTrace fTrace;
    private KernelContextSwitchAnalysis fModule;

    private static void deleteSuppFiles(ITmfTrace trace) {
        /* Remove supplementary files */
        File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
        for (File file : suppDir.listFiles()) {
            file.delete();
        }
    }

    /**
     * Setup the trace for the tests
     */
    @Before
    public void setUp() {
        ITmfTrace trace = new TmfXmlKernelTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(CPU_USAGE_FILE);
        IStatus status = trace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            trace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }
        deleteSuppFiles(trace);
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));

        fModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelContextSwitchAnalysis.class, KernelContextSwitchAnalysis.ID);
        assertNotNull(fModule);
        fTrace = trace;
    }

    /**
     * Clean up
     */
    @After
    public void tearDown() {
        deleteSuppFiles(fTrace);
        fTrace.dispose();
    }

    /**
     * Test that the analysis executes without problems
     */
    @Test
    public void testAnalysisExecution() {
        /* Make sure the analysis hasn't run yet */
        assertNull(fModule.getStateSystem());

        /* Execute the analysis */
        assertTrue(TmfTestHelper.executeAnalysis(fModule));
        assertNotNull(fModule.getStateSystem());
    }

    /**
     * Test that the state system is returned with the expected results
     */
    @Test
    public void testReturnedStateSystem() {
        fModule.schedule();
        fModule.waitForCompletion();
        ITmfStateSystem ss = fModule.getStateSystem();
        assertNotNull(ss);
        assertEquals(1L, ss.getStartTime());
        assertEquals(25L, ss.getCurrentEndTime());

        try {
            int cpusQuark = ss.getQuarkAbsolute(Attributes.CPUS);

            /*
             * There should be 2 CPU entries: 0 and 1 and 3 process entries
             * under each
             */
            List<Integer> cpuQuarks = ss.getSubAttributes(cpusQuark, false);
            assertEquals(2, cpuQuarks.size());

            /* Proc 2 on CPU 0 should run from 1 to 20 seconds */
            int proc2Quark = ss.getQuarkAbsolute(Attributes.CPUS, "0");
            ITmfStateInterval interval = ss.querySingleState(2L, proc2Quark);
            assertEquals(1L, interval.getStartTime());
            assertEquals(19L, interval.getEndTime());

            /*
             * Query at the end and make sure all processes on all CPU have the
             * expected values
             */
            List<ITmfStateInterval> state = ss.queryFullState(25L);

            int quark = ss.getQuarkAbsolute("CPUs", "0");
            assertEquals(3L, state.get(quark).getStateValue().unboxLong());
            quark = ss.getQuarkAbsolute("CPUs", "1");
            assertEquals(5L, state.get(quark).getStateValue().unboxLong());

            state = ss.queryFullState(19L);

            quark = ss.getQuarkAbsolute("CPUs", "0");
            assertEquals(1L, state.get(quark).getStateValue().unboxLong());
            quark = ss.getQuarkAbsolute("CPUs", "1");
            assertEquals(4L, state.get(quark).getStateValue().unboxLong());

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail(e.getMessage());
        }
    }

    /**
     * Test the
     * {@link KernelContextSwitchAnalysis#getContextSwitchesRange(long, long)}
     * method.
     */
    @Test
    public void testUsageInRange() {
        fModule.schedule();
        fModule.waitForCompletion();

        /* This range should query the total range */
        Map<Integer, Long> expected = new HashMap<>();
        expected.put(0, 2L);
        expected.put(1, 6L);
        expected.put(-1, 8L);
        Map<Integer, Long> resultMap = fModule.getContextSwitchesRange(0L, 30L);
        assertEquals(expected, resultMap);

        /* Verify a range when a process runs at the start */
        expected.clear();
        expected.put(0, 1L);
        expected.put(1, 0L);
        expected.put(-1, 1L);
        resultMap = fModule.getContextSwitchesRange(22L, 25L);
        assertEquals(expected, resultMap);

        /* Verify a range when a process runs at the end */
        expected.clear();
        expected.put(0, 0L);
        expected.put(1, 2L);
        expected.put(-1, 2L);
        resultMap = fModule.getContextSwitchesRange(1L, 4L);
        assertEquals(expected, resultMap);

        /* Verify a range when a process runs at start and at the end */
        expected.clear();
        expected.put(0, 0L);
        expected.put(1, 2L);
        expected.put(-1, 2L);
        resultMap = fModule.getContextSwitchesRange(4L, 13L);
        assertEquals(expected, resultMap);
    }

    /**
     * Test the usage in invalid circumstances
     */
    @Test
    public void testInvalid() {
        Map<Integer, Long> resultMap = fModule.getContextSwitchesRange(0L, 30L);
        assertEquals(Collections.EMPTY_MAP, resultMap);

        fModule.schedule();
        fModule.waitForCompletion();

        resultMap = fModule.getContextSwitchesRange(30L, 0L);
        assertEquals(Collections.EMPTY_MAP, resultMap);

        resultMap = fModule.getContextSwitchesRange(0L, 0L);
        assertEquals(Collections.EMPTY_MAP, resultMap);

        resultMap = fModule.getContextSwitchesRange(-30L, 0L);
        assertEquals(Collections.EMPTY_MAP, resultMap);
    }
}
