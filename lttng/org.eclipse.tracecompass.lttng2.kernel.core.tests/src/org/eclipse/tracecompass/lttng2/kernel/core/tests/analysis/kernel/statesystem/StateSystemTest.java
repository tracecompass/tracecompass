/*******************************************************************************
 * Copyright (c) 2012, 2016 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 *******************************************************************************/

package org.eclipse.tracecompass.lttng2.kernel.core.tests.analysis.kernel.statesystem;

import static org.eclipse.tracecompass.statesystem.core.ITmfStateSystem.INVALID_ATTRIBUTE;
import static org.eclipse.tracecompass.statesystem.core.ITmfStateSystem.ROOT_ATTRIBUTE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.kernel.Attributes;
import org.eclipse.tracecompass.statesystem.core.ITmfStateSystem;
import org.eclipse.tracecompass.statesystem.core.StateSystemUtils;
import org.eclipse.tracecompass.statesystem.core.exceptions.AttributeNotFoundException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateSystemDisposedException;
import org.eclipse.tracecompass.statesystem.core.exceptions.StateValueTypeException;
import org.eclipse.tracecompass.statesystem.core.exceptions.TimeRangeException;
import org.eclipse.tracecompass.statesystem.core.interval.ITmfStateInterval;
import org.eclipse.tracecompass.statesystem.core.statevalue.ITmfStateValue;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

/**
 * Base unit tests for the StateHistorySystem. Extension can be made to test
 * different state back-end types or configurations.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("javadoc")
public abstract class StateSystemTest {

    /** Timeout the tests after 2 minutes */
    @Rule
    public TestRule timeoutRule = new Timeout(2, TimeUnit.MINUTES);

    /** Test trace used for these tests */
    protected static final @NonNull CtfTestTrace testTrace = CtfTestTrace.TRACE2;

    /** Expected start time of the test trace/state history */
    protected static final long startTime = 1331668247314038062L;

    /** Expected end time of the state history built from the test trace */
    protected static final long endTime = 1331668259054285979L;

    /** Offset in the trace + start time of the trace */
    protected static final long interestingTimestamp1 = 18670067372290L + 1331649577946812237L;

    /** Number of nanoseconds in one second */
    private static final long NANOSECS_PER_SEC = 1000000000L;

    protected static ITmfStateSystem fixture;

    /**
     * Test set-up
     */
    @Before
    public void setUp() {
        /* Subclasses should set-up 'fixture' */
        assertNotNull(fixture);
    }

    @Test
    public void testFullQuery1() {
        List<ITmfStateInterval> list;
        ITmfStateInterval interval;
        int quark, valueInt;
        String valueStr;

        try {
            list = fixture.queryFullState(interestingTimestamp1);

            quark = fixture.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            interval = list.get(quark);
            valueInt = interval.getStateValue().unboxInt();
            assertEquals(1397, valueInt);

            quark = fixture.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
            interval = list.get(quark);
            valueStr = interval.getStateValue().unboxStr();
            assertEquals("gdbus", valueStr);

            quark = fixture.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.SYSTEM_CALL);
            interval = list.get(quark);
            valueStr = interval.getStateValue().unboxStr();
            assertEquals("poll", valueStr);

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    @Test
    public void testSingleQuery1() {
        long timestamp = interestingTimestamp1;
        int quark;
        ITmfStateInterval interval;
        String valueStr;

        try {
            quark = fixture.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
            interval = fixture.querySingleState(timestamp, quark);
            valueStr = interval.getStateValue().unboxStr();
            assertEquals("gdbus", valueStr);

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    /**
     * Test a range query (with no resolution parameter, so all intervals)
     */
    @Test
    public void testRangeQuery1() {
        long time1 = interestingTimestamp1;
        long time2 = time1 + 1L * NANOSECS_PER_SEC;
        int quark;
        List<ITmfStateInterval> intervals;

        final ITmfStateSystem ss = fixture;
        assertNotNull(ss);

        try {
            quark = ss.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            intervals = StateSystemUtils.queryHistoryRange(ss, quark, time1, time2);
            assertEquals(487, intervals.size()); /* Number of context switches! */
            assertEquals(1685, intervals.get(100).getStateValue().unboxInt());
            assertEquals(1331668248427681372L, intervals.get(205).getEndTime());

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    /**
     * Range query, but with a t2 far off the end of the trace. The result
     * should still be valid.
     */
    @Test
    public void testRangeQuery2() {
        List<ITmfStateInterval> intervals;

        final ITmfStateSystem ss = fixture;
        assertNotNull(ss);

        try {
            int quark = ss.getQuarkAbsolute(Attributes.CPUS, Integer.toString(0), Attributes.IRQS, "1");
            long ts1 = ss.getStartTime(); /* start of the trace */
            long ts2 = startTime + 20L * NANOSECS_PER_SEC; /* invalid, but ignored */

            intervals = StateSystemUtils.queryHistoryRange(ss, quark, ts1, ts2);

            /* Activity of IRQ 1 over the whole trace */
            assertEquals(65, intervals.size());

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    /**
     * Test a range query with a resolution
     */
    @Test
    public void testRangeQuery3() {
        long time1 = interestingTimestamp1;
        long time2 = time1 + 1L * NANOSECS_PER_SEC;
        long resolution = 1000000; /* One query every millisecond */
        int quark;
        List<ITmfStateInterval> intervals;

        final ITmfStateSystem ss = fixture;
        assertNotNull(ss);

        try {
            quark = ss.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            intervals = StateSystemUtils.queryHistoryRange(ss, quark, time1, time2, resolution, null);
            assertEquals(126, intervals.size()); /* Number of context switches! */
            assertEquals(1452, intervals.get(50).getStateValue().unboxInt());
            assertEquals(1331668248815698779L, intervals.get(100).getEndTime());

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    /**
     * Ask for a time range outside of the trace's range
     */
    @Test(expected = TimeRangeException.class)
    public void testFullQueryInvalidTime1() throws TimeRangeException,
            StateSystemDisposedException {
        long ts = startTime + 20L * NANOSECS_PER_SEC;
        fixture.queryFullState(ts);
    }

    @Test(expected = TimeRangeException.class)
    public void testFullQueryInvalidTime2() throws TimeRangeException,
            StateSystemDisposedException {
        long ts = startTime - 20L * NANOSECS_PER_SEC;
        fixture.queryFullState(ts);
    }

    @Test(expected = TimeRangeException.class)
    public void testSingleQueryInvalidTime1() throws TimeRangeException {
        try {
            int quark = fixture.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            long ts = startTime + 20L * NANOSECS_PER_SEC;
            fixture.querySingleState(ts, quark);

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    @Test(expected = TimeRangeException.class)
    public void testSingleQueryInvalidTime2() throws TimeRangeException {
        try {
            int quark = fixture.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            long ts = startTime - 20L * NANOSECS_PER_SEC;
            fixture.querySingleState(ts, quark);

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    @Test(expected = TimeRangeException.class)
    public void testRangeQueryInvalidTime1() throws TimeRangeException {
        final ITmfStateSystem ss = fixture;
        assertNotNull(ss);

        try {
            int quark = ss.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            long ts1 = startTime - 20L * NANOSECS_PER_SEC; /* invalid */
            long ts2 = startTime + 1L * NANOSECS_PER_SEC; /* valid */
            StateSystemUtils.queryHistoryRange(ss, quark, ts1, ts2);

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    @Test(expected = TimeRangeException.class)
    public void testRangeQueryInvalidTime2() throws TimeRangeException {
        final ITmfStateSystem ss = fixture;
        assertNotNull(ss);

        try {
            int quark = ss.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            long ts1 = startTime - 1L * NANOSECS_PER_SEC; /* invalid */
            long ts2 = startTime + 20L * NANOSECS_PER_SEC; /* invalid */
            StateSystemUtils.queryHistoryRange(ss, quark, ts1, ts2);

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    /**
     * Ask for a non-existing attribute
     *
     * @throws AttributeNotFoundException
     */
    @Test(expected = AttributeNotFoundException.class)
    public void testQueryInvalidAttribute() throws AttributeNotFoundException {
        fixture.getQuarkAbsolute("There", "is", "no", "cow", "level");
    }

    /**
     * Query but with the wrong State Value type
     */
    @Test(expected = StateValueTypeException.class)
    public void testQueryInvalidValuetype1() throws StateValueTypeException {
        List<ITmfStateInterval> list;
        ITmfStateInterval interval;
        int quark;

        try {
            list = fixture.queryFullState(interestingTimestamp1);
            quark = fixture.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            interval = list.get(quark);

            /* This is supposed to be an int value */
            interval.getStateValue().unboxStr();

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    @Test(expected = StateValueTypeException.class)
    public void testQueryInvalidValuetype2() throws StateValueTypeException {
        List<ITmfStateInterval> list;
        ITmfStateInterval interval;
        int quark;

        try {
            list = fixture.queryFullState(interestingTimestamp1);
            quark = fixture.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
            interval = list.get(quark);

            /* This is supposed to be a String value */
            interval.getStateValue().unboxInt();

        } catch (AttributeNotFoundException | StateSystemDisposedException e) {
            fail();
        }
    }

    @Test
    public void testOptQuarkAbsolute() {
        int quark = fixture.optQuarkAbsolute();
        assertEquals(ROOT_ATTRIBUTE, quark);

        quark = fixture.optQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
        assertNotEquals(INVALID_ATTRIBUTE, quark);
        assertEquals(Attributes.EXEC_NAME, fixture.getAttributeName(quark));

        quark = fixture.optQuarkAbsolute(Attributes.THREADS, "1432", "absent");
        assertEquals(INVALID_ATTRIBUTE, quark);

        quark = fixture.optQuarkAbsolute(Attributes.THREADS, "absent", Attributes.EXEC_NAME);
        assertEquals(INVALID_ATTRIBUTE, quark);

        quark = fixture.optQuarkAbsolute("absent", "1432", Attributes.EXEC_NAME);
        assertEquals(INVALID_ATTRIBUTE, quark);
    }

    @Test
    public void testOptQuarkRelative() {
        int threadsQuark = INVALID_ATTRIBUTE;
        try {
            threadsQuark = fixture.getQuarkAbsolute(Attributes.THREADS);
        } catch (AttributeNotFoundException e) {
            fail();
        }
        assertNotEquals(INVALID_ATTRIBUTE, threadsQuark);

        int quark = fixture.optQuarkRelative(threadsQuark);
        assertEquals(threadsQuark, quark);

        quark = fixture.optQuarkRelative(threadsQuark, "1432", Attributes.EXEC_NAME);
        assertNotEquals(INVALID_ATTRIBUTE, quark);
        assertEquals(Attributes.EXEC_NAME, fixture.getAttributeName(quark));

        quark = fixture.optQuarkRelative(threadsQuark, "1432", "absent");
        assertEquals(INVALID_ATTRIBUTE, quark);

        quark = fixture.optQuarkRelative(threadsQuark, "absent", Attributes.EXEC_NAME);
        assertEquals(INVALID_ATTRIBUTE, quark);
    }

    @Test
    public void testFullAttributeName() {
        try {
            int quark = fixture.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            String name = fixture.getFullAttributePath(quark);
            assertEquals(name, "CPUs/0/Current_thread");

        } catch (AttributeNotFoundException e) {
            fail();
        }
    }

    @Test
    public void testGetQuarks_begin() {
        List<Integer> list = fixture.getQuarks("*", "1577", Attributes.EXEC_NAME);

        assertEquals(1, list.size());
    }

    @Test
    public void testGetQuarks_middle() {
        List<Integer> list = fixture.getQuarks(Attributes.THREADS, "*", Attributes.EXEC_NAME);

        /* Number of different kernel threads in the trace */
        assertEquals(169, list.size());
    }

    @Test
    public void testGetQuarks_end() {
        List<Integer> list = fixture.getQuarks(Attributes.THREADS, "1577", "*");

        /* There should be 4 sub-attributes for this Thread node */
        assertEquals(4, list.size());
    }

    @Test
    public void testGetQuarks_middle_end() {
        List<Integer> list = fixture.getQuarks(Attributes.THREADS, "*", "*");

        /* There should be 716 attributes as not all have a system call or priority at this point*/
        assertEquals(716, list.size());
    }

    @Test
    public void testGetQuarks_empty() {
        List<Integer> list = fixture.getQuarks();

        assertEquals(Arrays.asList(ITmfStateSystem.ROOT_ATTRIBUTE), list);
    }

    @Test
    public void testGetQuarks_relative() {
        int threadsQuark = INVALID_ATTRIBUTE;
        try {
            threadsQuark = fixture.getQuarkAbsolute(Attributes.THREADS);
        } catch (AttributeNotFoundException e) {
            fail();
        }
        assertNotEquals(INVALID_ATTRIBUTE, threadsQuark);

        List<Integer> list = fixture.getQuarks(threadsQuark, "*", Attributes.EXEC_NAME);

        /* Number of different kernel threads in the trace */
        assertEquals(169, list.size());
    }

    @Test
    public void testGetQuarks_relative_up_wildcard() {
        int threadsQuark = INVALID_ATTRIBUTE;
        try {
            threadsQuark = fixture.getQuarkAbsolute(Attributes.THREADS);
        } catch (AttributeNotFoundException e) {
            fail();
        }
        assertNotEquals(INVALID_ATTRIBUTE, threadsQuark);

        List<Integer> list = fixture.getQuarks(threadsQuark, "..", Attributes.CPUS, "*");

        /* There should be 2 CPUs */
        assertEquals(2, list.size());
    }

    @Test
    public void testGetQuarks_relative_empty() {
        int threadsQuark = INVALID_ATTRIBUTE;
        try {
            threadsQuark = fixture.getQuarkAbsolute(Attributes.THREADS);
        } catch (AttributeNotFoundException e) {
            fail();
        }
        assertNotEquals(INVALID_ATTRIBUTE, threadsQuark);

        List<Integer> list = fixture.getQuarks(threadsQuark, new String[0]);
        assertEquals(Arrays.asList(threadsQuark), list);

        list = fixture.getQuarks(threadsQuark);
        assertEquals(Arrays.asList(threadsQuark), list);
    }

    @Test
    public void testGetQuarksNoMatch() {
        List<Integer> list = fixture.getQuarks("invalid");
        assertEquals(0, list.size());

        list = fixture.getQuarks("*", "invalid");
        assertEquals(0, list.size());

        list = fixture.getQuarks("invalid", "*");
        assertEquals(0, list.size());

        list = fixture.getQuarks(Attributes.THREADS, "*", "invalid");
        assertEquals(0, list.size());
    }

    // ------------------------------------------------------------------------
    // Tests verifying the *complete* results of a full queries
    // ------------------------------------------------------------------------

    protected long getStartTimes(int idx) {
        return TestValues.startTimes[idx];
    }

    protected long getEndTimes(int idx) {
        return TestValues.endTimes[idx];
    }

    protected ITmfStateValue getStateValues(int idx) {
        return TestValues.values[idx];
    }

    @Test
    public void testFullQueryThorough() {
        try {
            List<ITmfStateInterval> state = fixture.queryFullState(interestingTimestamp1);
            assertEquals(TestValues.size, state.size());

            for (int i = 0; i < state.size(); i++) {
                /* Test each component of the intervals */
                assertEquals(getStartTimes(i), state.get(i).getStartTime());
                assertEquals(getEndTimes(i), state.get(i).getEndTime());
                assertEquals(i, state.get(i).getAttribute());
                assertEquals(getStateValues(i), state.get(i).getStateValue());
            }

        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    @Test
    public void testFirstIntervalIsConsidered() {
        try {
            int quark = fixture.getQuarkAbsolute(Attributes.THREADS, "1397");
            List<ITmfStateInterval> list = fixture.queryFullState(1331668248014135800L);
            ITmfStateInterval interval = list.get(quark);
            assertEquals(1331668247516664825L, interval.getStartTime());

            int valueInt = interval.getStateValue().unboxInt();
            assertEquals(1, valueInt);

        } catch (StateSystemDisposedException | AttributeNotFoundException e) {
            fail();
        }
    }

    @Test
    public void testParentAttribute() {
        String[] path = { "CPUs/0/Current_thread",
                          "CPUs/0",
                          "CPUs" };
        try {
            int q = fixture.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            for (int i = 0; i < path.length; i++) {
                String name = fixture.getFullAttributePath(q);
                assertEquals(path[i], name);
                q = fixture.getParentAttributeQuark(q);
            }
            assertEquals(ROOT_ATTRIBUTE, q);
            q = fixture.getParentAttributeQuark(q);
            assertEquals(ROOT_ATTRIBUTE, q);
        } catch (AttributeNotFoundException e) {
            fail();
        }
    }

}
