/*******************************************************************************
 * Copyright (c) 2014, 2015 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.core.tests.kernelanalysis;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
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
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelAnalysis;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.KernelThreadInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.kernelanalysis.StateValues;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.Activator;
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
 * Test analysis-specific methods for the {@link KernelAnalysis} class.
 *
 * @author Geneviève Bastien
 */
public class KernelThreadInformationProviderTest {

    private static final @NonNull String LTTNG_KERNEL_FILE = "testfiles/lttng_kernel_analysis.xml";

    private ITmfTrace fTrace;
    private KernelAnalysis fModule;

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
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(fTrace, KernelAnalysis.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        fModule = TmfTraceUtils.getAnalysisModuleOfClass(fTrace, KernelAnalysis.class, KernelAnalysis.ID);
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
     * {@link KernelThreadInformationProvider#getThreadIds(KernelAnalysis)}
     * method
     */
    @Test
    public void testGetThreadQuarks() {
        KernelAnalysis module = checkNotNull(fModule);
        Collection<Integer> threadIds = KernelThreadInformationProvider.getThreadIds(module);
        assertEquals(7, threadIds.size());
    }

    /**
     * Test the
     * {@link KernelThreadInformationProvider#getThreadOnCpu(KernelAnalysis, long, long)}
     * method
     */
    @Test
    public void testGetThreadOnCpu() {
        KernelAnalysis module = checkNotNull(fModule);

        /* Check with invalid timestamps */
        Integer tid = KernelThreadInformationProvider.getThreadOnCpu(module, 0, -1);
        assertNull(tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 0, 80);
        assertNull(tid);

        /* Check with invalid cpus */
        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 2, 20);
        assertNull(tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, -1, 20);
        assertNull(tid);

        /* Check valid values */
        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 0, 4);
        assertNull(tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 0, 15);
        assertNull(tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 1, 15);
        assertEquals(Integer.valueOf(11), tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 1, 29);
        assertEquals(Integer.valueOf(20), tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 1, 30);
        assertEquals(Integer.valueOf(21), tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 0, 59);
        assertEquals(Integer.valueOf(11), tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 1, 59);
        assertEquals(Integer.valueOf(30), tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 0, 60);
        assertEquals(Integer.valueOf(11), tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 1, 60);
        assertEquals(Integer.valueOf(21), tid);

    }

    /**
     * Test the
     * {@link KernelThreadInformationProvider#getParentPid(KernelAnalysis, Integer, long)}
     * method
     */
    @Test
    public void testGetPpid() {
        KernelAnalysis module = checkNotNull(fModule);

        /* Check with invalid timestamps */
        Integer ppid = KernelThreadInformationProvider.getParentPid(module, 11, -1);
        assertNull(ppid);

        ppid = KernelThreadInformationProvider.getParentPid(module, 11, 80);
        assertNull(ppid);

        /* Check with invalid cpus */
        ppid = KernelThreadInformationProvider.getParentPid(module, -4, 20);
        assertNull(ppid);

        ppid = KernelThreadInformationProvider.getParentPid(module, 12, 20);
        assertNull(ppid);

        /* Check values with no parent */
        ppid = KernelThreadInformationProvider.getParentPid(module, 10, 20);
        assertEquals(Integer.valueOf(0), ppid);

        ppid = KernelThreadInformationProvider.getParentPid(module, 30, 60);
        assertEquals(Integer.valueOf(0), ppid);

        /* Check parent determined at statedump */
        ppid = KernelThreadInformationProvider.getParentPid(module, 11, 4);
        assertNull(ppid);

        ppid = KernelThreadInformationProvider.getParentPid(module, 11, 5);
        assertEquals(Integer.valueOf(10), ppid);

        /* Check parent after process fork */
        ppid = KernelThreadInformationProvider.getParentPid(module, 21, 25);
        assertEquals(Integer.valueOf(20), ppid);

        ppid = KernelThreadInformationProvider.getParentPid(module, 21, 70);
        assertEquals(Integer.valueOf(20), ppid);

    }

    /**
     * Test the {@link KernelThreadInformationProvider#getExecutableName(KernelAnalysis, Integer)} method
     */
    @Test
    public void testGetExecutableName() {
        KernelAnalysis module = checkNotNull(fModule);

        /* Check with invalid threads */
        String execName = KernelThreadInformationProvider.getExecutableName(module, 101);
        assertNull(execName);

        execName = KernelThreadInformationProvider.getExecutableName(module, -2);
        assertNull(execName);

        /* Check valid value */
        execName = KernelThreadInformationProvider.getExecutableName(module, 20);
        assertEquals("proc20", execName);

        /* Check valid value with process name change in history */
        execName = KernelThreadInformationProvider.getExecutableName(module, 21);
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
     * {@link KernelThreadInformationProvider#getStatusIntervalsForThread(KernelAnalysis, Integer, long, long, long, IProgressMonitor)}
     * method
     */
    @Test
    public void testGetStatusIntervalsForThread() {
        KernelAnalysis module = checkNotNull(fModule);

        IProgressMonitor monitor = new NullProgressMonitor();
        Integer process21 = 21;
        Integer process20 = 20;

        /* Check invalid time ranges */
        List<ITmfStateInterval> intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, -15, -5, 3, monitor);
        assertTrue(intervals.isEmpty());

        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 80, 1500000000L, 50, monitor);
        assertTrue(intervals.isEmpty());

        /* Check invalid quarks */
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, -1, 0, 70L, 3, monitor);
        assertTrue(intervals.isEmpty());

        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, 0, 0, 70L, 3, monitor);
        assertTrue(intervals.isEmpty());

        /* Check different time ranges and resolutions */
        ITmfStateValue[] values = { TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE, StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 0, 70L, 3, monitor);
        testIntervals("tid 21 [0,70,3]", intervals, values);

        ITmfStateValue[] values2 = { TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 1, 70L, 30, monitor);
        testIntervals("tid 21 [0,70,30]", intervals, values2);

        ITmfStateValue[] values3 = { StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE,
                StateValues.PROCESS_STATUS_RUN_SYSCALL_VALUE };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 25, 50L, 3, monitor);
        testIntervals("tid 21 [25,50,3]", intervals, values3);

        ITmfStateValue[] values4 = { TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_WAIT_BLOCKED_VALUE,
                StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE, StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 0, 70L, 3, monitor);
        testIntervals("tid 20 [0,70,3]", intervals, values4);

        ITmfStateValue[] values5 = { TmfStateValue.nullValue(), StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 1, 70L, 30, monitor);
        testIntervals("tid 20 [0,70,30]", intervals, values5);

        ITmfStateValue[] values6 = { StateValues.PROCESS_STATUS_RUN_USERMODE_VALUE,
                StateValues.PROCESS_STATUS_WAIT_FOR_CPU_VALUE };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 25, 50L, 3, monitor);
        testIntervals("tid 20 [25,50,3]", intervals, values6);

    }

}
