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

package org.eclipse.tracecompass.analysis.os.linux.core.tests.kernel;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelThreadInformationProvider;
import org.eclipse.tracecompass.analysis.os.linux.core.model.ProcessStatus;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.LinuxTestCase;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.kernel.KernelAnalysisTestFactory;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.interval.TmfStateInterval;
import org.eclipse.tracecompass.tmf.core.analysis.IAnalysisModule;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

/**
 * Test analysis-specific methods for the {@link KernelAnalysisModule} class.
 *
 * @author Geneviève Bastien
 */
public class KernelThreadInformationProviderTest {

    private static final @NonNull LinuxTestCase KERNEL_TEST_CASE = KernelAnalysisTestFactory.KERNEL_SCHED;

    private ITmfTrace fTrace;
    private KernelAnalysisModule fModule;

    private static void deleteSuppFiles(@NonNull ITmfTrace trace) {
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
        ITmfTrace trace = KERNEL_TEST_CASE.getKernelTrace();
        deleteSuppFiles(trace);
        ((TmfTrace) trace).traceOpened(new TmfTraceOpenedSignal(this, trace, null));
        IAnalysisModule module = null;
        for (IAnalysisModule mod : TmfTraceUtils.getAnalysisModulesOfClass(trace, KernelAnalysisModule.class)) {
            module = mod;
        }
        assertNotNull(module);
        module.schedule();
        module.waitForCompletion();
        fModule = TmfTraceUtils.getAnalysisModuleOfClass(trace, KernelAnalysisModule.class, KernelAnalysisModule.ID);
        fTrace = trace;
    }

    /**
     * Clean up
     */
    @After
    public void tearDown() {
        ITmfTrace trace = fTrace;
        if (trace != null) {
            deleteSuppFiles(trace);
            trace.dispose();
        }
    }

    /**
     * Test the
     * {@link KernelThreadInformationProvider#getThreadIds(KernelAnalysisModule)}
     * method
     */
    @Test
    public void testGetThreadQuarks() {
        KernelAnalysisModule module = checkNotNull(fModule);
        Collection<Integer> threadIds = KernelThreadInformationProvider.getThreadIds(module);
        assertEquals(ImmutableSet.<Integer>of(10,11,12,20,21,30,100), threadIds);
    }

    /**
     * Test the
     * {@link KernelThreadInformationProvider#getThreadOnCpu(KernelAnalysisModule, long, long)}
     * method
     */
    @Test
    public void testGetThreadOnCpu() {
        KernelAnalysisModule module = checkNotNull(fModule);

        /* Check with invalid timestamps */
        Integer tid = KernelThreadInformationProvider.getThreadOnCpu(module, 0, -1);
        assertNull(tid);

        tid = KernelThreadInformationProvider.getThreadOnCpu(module, 0, 90);
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
     * Test the {@link KernelThreadInformationProvider#getThreadsOfCpus} method.
     */
    @Test
    public void testGetThreadsOfCpus() {
        KernelAnalysisModule module = checkNotNull(fModule);
        final long start = 45L;
        final long end = 65L;

        Set<Integer> tids = KernelThreadInformationProvider.getThreadsOfCpus(module, Collections.singleton(0L), start, end);
        assertNotNull(tids);
        /*
         * Only threads 11 should be present, due to the sched_switch at t=35.
         */
        assertEquals(Collections.singleton(11), tids);

        tids = KernelThreadInformationProvider.getThreadsOfCpus(module, Collections.singleton(1L), start, end);
        assertNotNull(tids);
        /* Threads 21 and 30 get scheduled on CPU 1 in the range. */
        assertEquals(ImmutableSet.of(21, 30), tids);
    }

    /**
     * Test the {@link KernelThreadInformationProvider#getActiveThreadsForRange}
     * method.
     */
    @Test
    public void testIsThreadActiveRange() {
        KernelAnalysisModule module = checkNotNull(fModule);
        final long start = 45L;
        final long end = 65L;

        /*
         * Threads 11 (running on CPU 0), 21 (wait_for_cpu) and 30 (running on
         * CPU 1) should be active in the range.
         */
        Set<Integer> tids = KernelThreadInformationProvider.getActiveThreadsForRange(module, start, end);
        assertNotNull(tids);
        assertEquals(ImmutableSet.of(11, 21, 30), tids);
    }

    /**
     * Test the
     * {@link KernelThreadInformationProvider#getParentPid(KernelAnalysisModule, Integer, long)}
     * method
     */
    @Test
    public void testGetPpid() {
        KernelAnalysisModule module = checkNotNull(fModule);

        /* Check with invalid timestamps */
        Integer ppid = KernelThreadInformationProvider.getParentPid(module, 11, -1);
        assertNull(ppid);

        ppid = KernelThreadInformationProvider.getParentPid(module, 11, 90);
        assertNull(ppid);

        /* Check with invalid tids */
        ppid = KernelThreadInformationProvider.getParentPid(module, -4, 20);
        assertNull(ppid);

        ppid = KernelThreadInformationProvider.getParentPid(module, 13, 20);
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

        ppid = KernelThreadInformationProvider.getParentPid(module, 12, 10);
        assertEquals(Integer.valueOf(11), ppid);

        /* Check parent after process fork */
        ppid = KernelThreadInformationProvider.getParentPid(module, 21, 25);
        assertEquals(Integer.valueOf(20), ppid);

        ppid = KernelThreadInformationProvider.getParentPid(module, 21, 70);
        assertEquals(Integer.valueOf(20), ppid);

    }

    /**
     * Test the {@link KernelThreadInformationProvider#getExecutableName(KernelAnalysisModule, Integer)} method
     */
    @Test
    public void testGetExecutableName() {
        KernelAnalysisModule module = checkNotNull(fModule);

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
        assertEquals("tid21", execName);

    }

    private static void testIntervals(String info, List<ITmfStateInterval> intervals, ProcessStatus[] values) {
        assertEquals(info + " interval count", values.length, intervals.size());
        for (int i = 0; i < values.length; i++) {
            assertEquals(info + " interval " + i, values[i], ProcessStatus.getStatusFromStateValue(intervals.get(i).getStateValue()));
        }
    }

    private static void testIterator(String info, Iterator<ITmfStateInterval> intervals, ProcessStatus[] values) {
        for (int i = 0; i < values.length; i++) {
            assertTrue(intervals.hasNext());
            ITmfStateInterval interval = intervals.next();
            assertEquals(info + " interval " + i, values[i], ProcessStatus.getStatusFromStateValue(interval.getStateValue()));
        }
        assertFalse(intervals.hasNext());
    }

    private static void compareIntervals(List<ITmfStateInterval> expecteds, List<ITmfStateInterval> actuals) {
        assertEquals(expecteds.size(), actuals.size());
        for (int i = 0; i < expecteds.size(); i++) {
            ITmfStateInterval expected = expecteds.get(i);
            ITmfStateInterval actual = actuals.get(i);

            /* Only compare bounds and value, attribute doesn't matter here */
            assertEquals(expected.getStartTime(), actual.getStartTime());
            assertEquals(expected.getEndTime(), actual.getEndTime());
            assertEquals(expected.getStateValue(), actual.getStateValue());
        }
    }

    /**
     * Test the
     * {@link KernelThreadInformationProvider#getStatusIntervalsForThread(KernelAnalysisModule, Integer, long, long, long, IProgressMonitor)}
     * method
     */
    @Test
    public void testGetStatusIntervalsForThread() {
        KernelAnalysisModule module = checkNotNull(fModule);

        IProgressMonitor monitor = new NullProgressMonitor();
        Integer process21 = 21;
        Integer process20 = 20;

        /* Check invalid time ranges */
        List<ITmfStateInterval> intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, -15, -5, 3, monitor);
        assertTrue(intervals.isEmpty());

        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 90, 1500000000L, 50, monitor);
        assertTrue(intervals.isEmpty());

        /* Check invalid quarks */
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, -1, 0, 70L, 3, monitor);
        assertTrue(intervals.isEmpty());

        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, 0, 0, 70L, 3, monitor);
        assertTrue(intervals.isEmpty());

        /* Check different time ranges and resolutions */
        ProcessStatus[] values = { ProcessStatus.NOT_ALIVE, ProcessStatus.WAIT_CPU,
                ProcessStatus.RUN, ProcessStatus.WAIT_CPU,
                ProcessStatus.RUN };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 0, 70L, 3, monitor);
        testIntervals("tid 21 [0,70,3]", intervals, values);

        ProcessStatus[] values2 = { ProcessStatus.NOT_ALIVE, ProcessStatus.RUN,
                ProcessStatus.RUN };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 1, 70L, 30, monitor);
        testIntervals("tid 21 [0,70,30]", intervals, values2);

        ProcessStatus[] values3 = { ProcessStatus.WAIT_CPU,
                ProcessStatus.RUN };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 25, 50L, 3, monitor);
        testIntervals("tid 21 [25,50,3]", intervals, values3);

        ImmutableList<ITmfStateInterval> expectedIntervals = ImmutableList.of(
                new TmfStateInterval( 1,  9, 0, ProcessStatus.NOT_ALIVE.getStateValue().unboxValue()),
                new TmfStateInterval(10, 19, 0, ProcessStatus.WAIT_UNKNOWN.getStateValue().unboxValue()),
                new TmfStateInterval(20, 29, 0, ProcessStatus.RUN.getStateValue().unboxValue()),
                new TmfStateInterval(30, 69, 0, ProcessStatus.WAIT_BLOCKED.getStateValue().unboxValue())
                );

        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 0, 70L, 3, monitor);
        compareIntervals(expectedIntervals, intervals);

        expectedIntervals = ImmutableList.of(
                new TmfStateInterval( 1,  9, 0, ProcessStatus.NOT_ALIVE.getStateValue().unboxValue()),
                new TmfStateInterval(30, 69, 0, ProcessStatus.WAIT_BLOCKED.getStateValue().unboxValue())
                );
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 1, 70L, 30, monitor);
        compareIntervals(expectedIntervals, intervals);

        ProcessStatus[] values6 = { ProcessStatus.RUN,
                ProcessStatus.WAIT_BLOCKED };

        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 25, 50L, 3, monitor);
        testIntervals("tid 20 [25,50,3]", intervals, values6);

        ProcessStatus[] values7 = { ProcessStatus.WAIT_CPU };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 80L, 85L, 3, monitor);
        testIntervals("tid 20 [80,85,3]", intervals, values7);
    }

    /**
     * Test the
     * {@link KernelThreadInformationProvider#getStatusIntervalsForThread(KernelAnalysisModule, Integer, long, long)}
     * method
     */
    @Test
    public void testGetStatusIntervalsForThreadIt() {
        KernelAnalysisModule module = checkNotNull(fModule);

        Integer process21 = 21;
        Integer process20 = 20;

        /* Check invalid time ranges */
        Iterator<ITmfStateInterval> intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, -15, -5);
        assertFalse(intervals.hasNext());

        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 90, 1500000000L);
        assertFalse(intervals.hasNext());

        /* Check invalid quarks */
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, -1, 0, 70L);
        assertFalse(intervals.hasNext());

        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, 0, 0, 70L);
        assertFalse(intervals.hasNext());

        /* Check different time ranges */
        ProcessStatus[] values = { ProcessStatus.NOT_ALIVE, ProcessStatus.WAIT_CPU,
                ProcessStatus.RUN, ProcessStatus.WAIT_CPU,
                ProcessStatus.RUN };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 0, 70L);
        testIterator("tid 21 [0,70]", intervals, values);

        ProcessStatus[] values2 = { ProcessStatus.WAIT_CPU,
                ProcessStatus.RUN };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process21, 25, 50L);
        testIterator("tid 21 [25,50]", intervals, values2);

        ProcessStatus[] values3 = { ProcessStatus.NOT_ALIVE, ProcessStatus.WAIT_UNKNOWN,
                ProcessStatus.RUN, ProcessStatus.WAIT_BLOCKED };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 0, 70L);
        testIterator("tid 20 [0,70]", intervals, values3);

        ProcessStatus[] values4 = { ProcessStatus.RUN,
                ProcessStatus.WAIT_BLOCKED };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 25, 50L);
        testIterator("tid 20 [25,50]", intervals, values4);

        ProcessStatus[] values5 = { ProcessStatus.WAIT_CPU };
        intervals = KernelThreadInformationProvider.getStatusIntervalsForThread(module, process20, 80L, 85L);
        testIterator("tid 20 [80,85]", intervals, values5);
    }

}
