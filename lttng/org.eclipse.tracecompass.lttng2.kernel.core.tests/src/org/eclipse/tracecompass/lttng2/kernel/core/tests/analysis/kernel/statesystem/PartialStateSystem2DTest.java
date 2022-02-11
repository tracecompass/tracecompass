/*******************************************************************************
 * Copyright (c) 2022 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel.statesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.tracecompass.analysis.os.linux.core.kernel.KernelAnalysisModule;
import org.eclipse.tracecompass.lttng2.kernel.core.tests.Activator;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystemBuilder;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.analysis.os.linux.core.tests.stubs.trace.TmfXmlKernelTraceStub;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfAnalysisException;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;
import com.google.common.collect.TreeMultimap;

/**
 * Test suite for the 2D queries of the partial state system using the
 * KernelAnalysisModule. This test was inspired by the test StateSystem2DTest
 * where the intervals are added manually to the state system. In this test we
 * are creating the intervals from an xml trace because the 2D query of the
 * partial state needs to read the trace everytime since only the states at the
 * checkpoints are saved on disk.
 *
 * @author Abdellah Rahmani
 */
public class PartialStateSystem2DTest {

    private static final String PSS_USAGE_FILE = "testfiles/partialSS_2d.xml";
    private static final @NonNull String TEST_FILE_NAME = "test-partial-2DQuery";
    private static final long START_TIME = 10L;
    private static File fStateFile;
    private IKernelTrace fTrace;
    private TestLttngKernelAnalysisModule fModule;

    private static void deleteSuppFiles(ITmfTrace trace) {
        /* Remove supplementary files */
        if (trace != null) {
            File suppDir = new File(TmfTraceManager.getSupplementaryFileDir(trace));
            for (File file : suppDir.listFiles()) {
                file.delete();
            }
        }
    }

    /**
     * Setup the trace for the tests
     */
    @Before
    public void setUp() {
        IKernelTrace trace = new TmfXmlKernelTraceStub();
        IPath filePath = Activator.getAbsoluteFilePath(PSS_USAGE_FILE);
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

        fStateFile = new File(TmfTraceManager.getSupplementaryFileDir(trace) + TEST_FILE_NAME);
        if (fStateFile.exists()) {
            fStateFile.delete();
        }

        fModule = new TestLttngKernelAnalysisModule(TEST_FILE_NAME);
        try {
            assertTrue(fModule.setTrace(trace));
        } catch (TmfAnalysisException e) {
            fail();
        }
        assertNotNull(fModule);
        fModule.schedule();
        assertTrue(fModule.waitForCompletion());
        fTrace = trace;
    }

    /**
     * Dispose everything
     */
    @After
    public void cleanup() {
        final ITmfTrace testTrace = fTrace;
        if (testTrace != null) {
            testTrace.dispose();
        }
        fModule.dispose();
    }

    /**
     * This method verifies several assertions to validate the output of the 2D
     * query that uses a continuous time range
     *
     * @param iterable
     *            The variable that contains the output (intervals) provided by
     *            the 2D query
     * @param quarks
     *            The collection of the requested attribute quarks
     * @param start
     *            Start time of the trace (or the history tree)
     * @param end
     *            The end time of the trace
     * @param totalCount
     *            The expected total number of intervals
     */
    protected static void testContinuous(Iterable<ITmfStateInterval> iterable, Collection<Integer> quarks, long start, long end, int totalCount) {
        Multimap<Integer, ITmfStateInterval> treeMap = TreeMultimap.create(Comparator.naturalOrder(),
                Comparator.comparing(ITmfStateInterval::getStartTime));

        iterable.forEach(interval -> assertTrue("Interval: " + interval + " was already returned: " + treeMap,
                treeMap.put(interval.getAttribute(), interval)));
        assertEquals("Wrong number of intervals returned", totalCount, treeMap.size());
        assertEquals("There should only be as many Sets of intervals as quarks",
                quarks.size(), treeMap.keySet().size());

        for (Integer quark : quarks) {
            Collection<ITmfStateInterval> orderedSet = treeMap.get(quark);
            assertFalse("There should be intervals for quark: " + quark, orderedSet.isEmpty());
            ITmfStateInterval previous = null;
            for (ITmfStateInterval interval : orderedSet) {
                if (previous == null) {
                    assertTrue("The first interval: " + interval + "should intersect start: " + start, interval.intersects(start));
                } else {
                    assertEquals("Current interval: " + interval + " should have been contiguous to the previous one " + previous,
                            previous.getEndTime() + 1, interval.getStartTime());
                }
                previous = interval;
            }
            assertNotNull("There should have been at least one interval for quark " + quark, previous);
            assertTrue("last interval: " + previous + " should intersect end " + end, previous.intersects(end));
        }
    }

    /**
     * Test the discrete 2D query method.
     *
     * @throws AttributeNotFoundException
     *             if the requested attribute simply did not exist in the
     *             system.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     * @throws TimeRangeException
     *             If the smallest time is before the state system start time.
     * @throws IndexOutOfBoundsException
     *             If the smallest attribute is <0 or if the largest is >= to
     *             the number of attributes.
     */
    @Test
    public void testContinuous2DQuery() throws AttributeNotFoundException, IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) fModule.getStateSystem();
        assertNotNull(ss);
        long end = ss.getCurrentEndTime();

        /*
         * Make sure all and only the intervals giving the cpu 0 current theread
         * are returned
         */
        String attributePath = "0";
        int attributeQuark = ss.getQuarkAbsolute(Attributes.CPUS, attributePath, Attributes.CURRENT_THREAD);
        Iterable<ITmfStateInterval> iterable = ss.query2D(Collections.singleton(attributeQuark), START_TIME, end);
        testContinuous(iterable, Collections.singleton(attributeQuark), START_TIME, end, 4);

        /*
         * Make sure all and only the intervals giving the cpu 1 current theread
         * are returned
         */
        attributePath = "1";
        int integerQuark = ss.getQuarkAbsolute(Attributes.CPUS, attributePath, Attributes.CURRENT_THREAD);
        iterable = ss.query2D(Collections.singleton(integerQuark), START_TIME, end);
        testContinuous(iterable, Collections.singleton(integerQuark), START_TIME, end, 7);

        /*
         * Make sure all and only the intervals of the threads proc1 and proc2
         * are returned
         */
        int proc1Quark = ss.getQuarkAbsolute(Attributes.THREADS, "100");
        int proc2Quark = ss.getQuarkAbsolute(Attributes.THREADS, "200");
        Collection<Integer> quarks = ImmutableList.of(proc1Quark, proc2Quark);
        iterable = ss.query2D(quarks, START_TIME, end);
        testContinuous(iterable, quarks, START_TIME, end, 8);

        /*
         * Make sure all and only the intervals of the threads proc3 and proc4
         * are returned
         */
        int proc3Quark = ss.getQuarkAbsolute(Attributes.THREADS, "300");
        int proc4Quark = ss.getQuarkAbsolute(Attributes.THREADS, "400");
        quarks = ImmutableList.of(proc3Quark, proc4Quark);
        iterable = ss.query2D(quarks, START_TIME, end);
        testContinuous(iterable, quarks, START_TIME, end, 14);

        /*
         * Make sure all the intervals are returned. The expected total number
         * of the intervals is computed using the full state system
         */
        int nbAttributes = ss.getNbAttributes();
        List<Integer> quarksList = new ArrayList<Integer>();
        for (int i = 0; i < nbAttributes; i++) {
            quarksList.add(i);
        }
        iterable = ss.query2D(quarksList, START_TIME, end);
        testContinuous(iterable, quarksList, START_TIME, end, 61);
    }

    /**
     *
     * This method verifies several assertions to validate the output of the 2D
     * query that uses a discrete time condition
     *
     * @param iterable
     *            The variable that contains the output (intervals) provided by
     *            the 2D query
     * @param quarks
     *            The collection of the requested attribute quarks
     * @param times
     *            The collection of the requested timestamps
     * @param totalCount
     *            The expected total number of intervals
     */

    protected static void testDiscrete(Iterable<ITmfStateInterval> iterable, Collection<Integer> quarks, Collection<Long> times, int totalCount) {
        Set<ITmfStateInterval> set = new HashSet<>();
        int countTimeStamps = 0;

        for (ITmfStateInterval interval : iterable) {
            assertTrue(quarks.contains(interval.getAttribute()));
            assertTrue("interval: " + interval + " was returned twice", set.add(interval));

            int timeStamps = (int) times.stream().filter(interval::intersects).count();
            assertTrue("interval: " + interval + " does not intersect any time stamp: " + times, timeStamps > 0);
            countTimeStamps += timeStamps;
        }

        assertEquals("incorrect number of intervals returned", totalCount, set.size());
        assertEquals("All the queried time stamps were not covered", times.size() * quarks.size(), countTimeStamps);
    }

    /**
     * Test the discrete 2D query method.
     *
     * @throws AttributeNotFoundException
     *             if the requested attribute simply did not exist in the
     *             system.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     * @throws TimeRangeException
     *             If the smallest time is before the state system start time.
     * @throws IndexOutOfBoundsException
     *             If the smallest attribute is <0 or if the largest is >= to
     *             the number of attributes.
     */
    @Test
    public void testDiscrete2DQuery() throws AttributeNotFoundException,
            IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) fModule.getStateSystem();
        assertNotNull(ss);
        long end = ss.getCurrentEndTime();
        Collection<Long> times = StateSystemUtils.getTimes(START_TIME, end, 10L);
        assertEquals(4, times.size());
        assertTrue(Ordering.natural().isStrictlyOrdered(times));

        /*
         * Make sure all and only the intervals of the thread proc1 are returned
         */
        int proc1Quark = ss.getQuarkAbsolute(Attributes.THREADS, "100");
        Iterable<ITmfStateInterval> iterable = ss.query2D(Collections.singleton(proc1Quark), times);
        testDiscrete(iterable, Collections.singleton(proc1Quark), times, 4);

        /*
         * Make sure all and only the intervals of the thread proc3 are returned
         */
        int proc3Quark = ss.getQuarkAbsolute(Attributes.THREADS, "300");
        iterable = ss.query2D(Collections.singleton(proc3Quark), times);
        testDiscrete(iterable, Collections.singleton(proc3Quark), times, 4);

        /* Make sure all the intervals of proc1 and proc3 are returned */
        Collection<Integer> quarks = ImmutableList.of(proc1Quark, proc3Quark);
        iterable = ss.query2D(quarks, times);
        testDiscrete(iterable, quarks, times, 8);
    }

    /**
     * Test index out of bound queries
     *
     * @throws StateSystemDisposedException
     *             query sent after the state system was closed
     * @throws TimeRangeException
     *             time was out of range
     * @throws IndexOutOfBoundsException
     *             queried an attribute that was out of bounds
     */
    @Test(expected = IndexOutOfBoundsException.class)
    public void testIOOB2DQuery() throws IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) fModule.getStateSystem();
        assertNotNull(ss);
        Collection<Long> times = Collections.singleton(35L);
        int stringQuark = Integer.MAX_VALUE;
        ss.query2D(Collections.singleton(stringQuark), times);
    }

    /**
     * Test a too low time range exception on continuous queries
     *
     * @throws StateSystemDisposedException
     *             ss was closed
     * @throws TimeRangeException
     *             time was out of range
     * @throws IndexOutOfBoundsException
     *             queried an attribute that was out of bounds
     */
    @Test(expected = TimeRangeException.class)
    public void testTimeRangeException2DContinous() throws IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) fModule.getStateSystem();
        assertNotNull(ss);
        ss.query2D(Collections.singleton(0), 0L, 35L);
    }

    /**
     * Test a too low time range exception on discrete queries
     *
     * @throws StateSystemDisposedException
     *             ss was closed
     * @throws TimeRangeException
     *             time was out of range
     * @throws IndexOutOfBoundsException
     *             queried an attribute that was out of bounds
     */
    @Test(expected = TimeRangeException.class)
    public void testTimeRangeException2DDiscrete() throws IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) fModule.getStateSystem();
        assertNotNull(ss);
        ss.query2D(Collections.singleton(0), Collections.singleton(Long.MIN_VALUE));
    }

    /**
     * Test Empty queries
     *
     * @throws StateSystemDisposedException
     *             ss was closed
     * @throws TimeRangeException
     *             time was out of range
     * @throws IndexOutOfBoundsException
     *             queried an attribute that was out of bounds
     */
    @Test
    public void testEmpty2DQuery() throws IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) fModule.getStateSystem();
        assertNotNull(ss);

        /* Test on a discrete query */
        Iterable<ITmfStateInterval> iterable = ss.query2D(Collections.emptyList(), Collections.singleton(35L));
        assertNotNull(iterable);
        assertTrue(Iterables.isEmpty(iterable));

        /* Test on an empty time sample */
        iterable = ss.query2D(Collections.singleton(0), Collections.emptyList());
        assertNotNull(iterable);
        assertTrue(Iterables.isEmpty(iterable));

        /* Test on a continuous query */
        iterable = ss.query2D(Collections.emptyList(), 35L, 38L);
        assertNotNull(iterable);
        assertTrue(Iterables.isEmpty(iterable));
    }

    /**
     * Test the continuous 2D query method when start time > end time.
     *
     * @throws AttributeNotFoundException
     *             if the requested attribute simply did not exist in the
     *             system.
     * @throws StateSystemDisposedException
     *             If the query is sent after the state system has been disposed
     * @throws TimeRangeException
     *             If the smallest time is before the state system start time.
     * @throws IndexOutOfBoundsException
     *             If the smallest attribute is <0 or if the largest is >= to
     *             the number of attributes.
     */
    @Test
    public void testReverse2DQuery() throws AttributeNotFoundException, IndexOutOfBoundsException, TimeRangeException, StateSystemDisposedException {
        ITmfStateSystemBuilder ss = (ITmfStateSystemBuilder) fModule.getStateSystem();
        assertNotNull(ss);
        long end = ss.getCurrentEndTime();

        /*
         * Make sure all and only the intervals of the process proc1 are
         * returned
         */
        int proc1Quark = ss.getQuarkAbsolute(Attributes.THREADS, "100");
        Iterable<ITmfStateInterval> iterable = ss.query2D(Collections.singleton(proc1Quark), end, START_TIME);
        testContinuous(iterable, Collections.singleton(proc1Quark), START_TIME, end, 4);

        /*
         * Make sure all and only the intervals of the process proc3 are
         * returned
         */
        int proc3Quark = ss.getQuarkAbsolute(Attributes.THREADS, "300");
        iterable = ss.query2D(Collections.singleton(proc3Quark), end, START_TIME);
        testContinuous(iterable, Collections.singleton(proc3Quark), START_TIME, end, 7);

        /* Make sure all intervals of proc1 and proc3 are returned */
        Collection<Integer> quarks = ImmutableList.of(proc1Quark, proc3Quark);
        iterable = ss.query2D(quarks, end, START_TIME);
        testContinuous(iterable, quarks, START_TIME, end, 11);
    }

    @NonNullByDefault
    private static class TestLttngKernelAnalysisModule extends KernelAnalysisModule {
        private final String htFileName;

        /**
         * Constructor
         *
         * @param htFileName
         *            The History File Name
         */
        public TestLttngKernelAnalysisModule(String htFileName) {
            super();
            this.htFileName = htFileName;
        }

        @Override
        protected StateSystemBackendType getBackendType() {
            return StateSystemBackendType.PARTIAL;
        }

        @Override
        protected String getSsFileName() {
            return htFileName;
        }

    }
}