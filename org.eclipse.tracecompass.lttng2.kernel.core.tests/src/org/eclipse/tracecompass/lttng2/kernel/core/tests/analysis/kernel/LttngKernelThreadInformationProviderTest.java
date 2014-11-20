/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.lttng2.kernel.core.StateValues;
import org.eclipse.tracecompass.lttng2.kernel.core.analysis.kernel.LttngKernelAnalysis;
import org.eclipse.tracecompass.lttng2.kernel.core.analysis.kernel.LttngKernelThreadInformationProvider;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.Activator;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.statesystem.core.statevalue.TmfStateValue;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.tests.stubs.trace.xml.TmfXmlTraceStub;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Test analysis-specific methods for the {@link LttngKernelAnalysis} class.
 *
 * @author Geneviève Bastien
 */
public class LttngKernelThreadInformationProviderTest {

    private static final @NonNull String LTTNG_KERNEL_FILE = "testfiles/lttng_kernel_analysis.xml";
    /**
     * The ID of the cpu usage analysis module for development traces
     */
    public static final String LTTNG_KERNEL_ANALYSIS_ID = "lttng2.kernel.core.tests.kernel.analysis";

    private ITmfTrace fTrace;
    private LttngKernelAnalysis fModule;

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
        fTrace = new TmfXmlTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(LTTNG_KERNEL_FILE);
        IStatus status = fTrace.validate(null, filePath.toOSString());
        if (!status.isOK()) {
            fail(status.getException().getMessage());
        }
        try {
            fTrace.initTrace(null, filePath.toOSString(), TmfEvent.class);
        } catch (TmfTraceException e) {
            fail(e.getMessage());
        }
        deleteSuppFiles(fTrace);
        ((TmfTrace) fTrace).traceOpened(new TmfTraceOpenedSignal(this, fTrace, null));
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(fTrace, LttngKernelAnalysis.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        fModule = TmfTraceUtils.getAnalysisModuleOfClass(fTrace, LttngKernelAnalysis.class, LTTNG_KERNEL_ANALYSIS_ID);
        assertNotNull(fModule);
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
     * Test the
     * {@link LttngKernelThreadInformationProvider#getThreadIds(LttngKernelAnalysis)}
     * method
     */
    @Test
    public void testGetThreadQuarks() {
        Collection<Integer> threadIds = LttngKernelThreadInformationProvider.getThreadIds(fModule);
        assertEquals(7, threadIds.size());
    }

    /**
     * Test the
     * {@link LttngKernelThreadInformationProvider#getThreadOnCpu(LttngKernelAnalysis, long, long)}
     * method
     */
    @Test
    public void testGetThreadOnCpu() {

        /* Check with invalid timestamps */
        Integer tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 0, -1);
        assertNull(tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 0, 80);
        assertNull(tid);

        /* Check with invalid cpus */
        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 2, 20);
        assertNull(tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, -1, 20);
        assertNull(tid);

        /* Check valid values */
        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 0, 4);
        assertNull(tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 0, 15);
        assertNull(tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 1, 15);
        assertEquals(Integer.valueOf(11), tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 1, 29);
        assertEquals(Integer.valueOf(20), tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 1, 30);
        assertEquals(Integer.valueOf(21), tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 0, 59);
        assertEquals(Integer.valueOf(11), tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 1, 59);
        assertEquals(Integer.valueOf(30), tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 0, 60);
        assertEquals(Integer.valueOf(11), tid);

        tid = LttngKernelThreadInformationProvider.getThreadOnCpu(fModule, 1, 60);
        assertEquals(Integer.valueOf(21), tid);

    }

    /**
     * Test the
     * {@link LttngKernelThreadInformationProvider#getParentPid(LttngKernelAnalysis, Integer, long)}
     * method
     */
    @Test
    public void testGetPpid() {

        /* Check with invalid timestamps */
        Integer ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, 11, -1);
        assertNull(ppid);

        ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, 11, 80);
        assertNull(ppid);

        /* Check with invalid cpus */
        ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, -4, 20);
        assertNull(ppid);

        ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, 12, 20);
        assertNull(ppid);

        /* Check values with no parent */
        ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, 10, 20);
        assertEquals(Integer.valueOf(0), ppid);

        ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, 30, 60);
        assertEquals(Integer.valueOf(0), ppid);

        /* Check parent determined at statedump */
        ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, 11, 4);
        assertNull(ppid);

        ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, 11, 5);
        assertEquals(Integer.valueOf(10), ppid);

        /* Check parent after process fork */
        ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, 21, 25);
        assertEquals(Integer.valueOf(20), ppid);

        ppid = LttngKernelThreadInformationProvider.getParentPid(fModule, 21, 70);
        assertEquals(Integer.valueOf(20), ppid);

    }

    /**
     * Test the {@link LttngKernelThreadInformationProvider#getExecutableName(LttngKernelAnalysis, Integer)} method
     */
    @Test
    public void testGetExecutableName() {
        /* Check with invalid threads */
        String execName = LttngKernelThreadInformationProvider.getExecutableName(fModule, 101);
        assertNull(execName);

        execName = LttngKernelThreadInformationProvider.getExecutableName(fModule, -2);
        assertNull(execName);

        /* Check valid value */
        execName = LttngKernelThreadInformationProvider.getExecutableName(fModule, 20);
        assertEquals("proc20", execName);

        /* Check valid value with process name change in history */
        execName = LttngKernelThreadInformationProvider.getExecutableName(fModule, 21);
        assertEquals("proc21", execName);

    }

    private static void testIntervals(String info, List<ITmfStateInterval> intervals, ITmfStateValue[] values) {
        assertEquals(info + " interval count", values.length, intervals.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(info + " interval " + i, values[i], intervals.get(i).getStateValue());
        }
    }

    /**
     * Test the
     * {@link LttngKernelThreadInformationProvider#getStatusIntervalsForThread(LttngKernelAnalysis, Integer, long, long, long, IProgressMonitor)}
     * method
     */
    @Test
    public void testGetStatusIntervalsForThread() {

        IProgressMonitor monitor = new NullProgressMonitor();
        Integer process21 = 21;
        Integer process20 = 20;

        /* Check invalid time ranges */
        List<ITmfStateInterval> intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, process21, -15, -5, 3, monitor);
        assertTrue(intervals.isEmpty());

        intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, process21, 80, 1500000000L, 50, monitor);
        assertTrue(intervals.isEmpty());

        /* Check invalid quarks */
        intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, -1, 0, 70L, 3, monitor);
        assertTrue(intervals.isEmpty());

        intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, 0, 0, 70L, 3, monitor);
        assertTrue(intervals.isEmpty());

        /* Check different time ranges and resolutions */
        ITmfStateValue[] values = { TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE, StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE };
        intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, process21, 0, 70L, 3, monitor);
        testIntervals("tid 21 [0,70,3]", intervals, values);

        ITmfStateValue[] values2 = { TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE };
        intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, process21, 1, 70L, 30, monitor);
        testIntervals("tid 21 [0,70,30]", intervals, values2);

        ITmfStateValue[] values3 = { StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE };
        intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, process21, 25, 50L, 3, monitor);
        testIntervals("tid 21 [25,50,3]", intervals, values3);

        ITmfStateValue[] values4 = { TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE,
                StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE, StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE };
        intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, process20, 0, 70L, 3, monitor);
        testIntervals("tid 20 [0,70,3]", intervals, values4);

        ITmfStateValue[] values5 = { TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE };
        intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, process20, 1, 70L, 30, monitor);
        testIntervals("tid 20 [0,70,30]", intervals, values5);

        ITmfStateValue[] values6 = { StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE,
                StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE };
        intervals = LttngKernelThreadInformationProvider.getStatusIntervalsForThread(fModule, process20, 25, 50L, 3, monitor);
        testIntervals("tid 20 [25,50,3]", intervals, values6);

    }

}
