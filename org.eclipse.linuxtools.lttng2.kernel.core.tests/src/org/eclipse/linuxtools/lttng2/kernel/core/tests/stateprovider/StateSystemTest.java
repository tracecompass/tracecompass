/*******************************************************************************
 * Copyright (c) 2012, 2013 Ericsson
 * Copyright (c) 2010, 2011 École Polytechnique de Montréal
 * Copyright (c) 2010, 2011 Alexandre Montplaisir <alexandre.montplaisir@gmail.com>
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 *******************************************************************************/

package org.eclipse.linuxtools.lttng2.kernel.core.tests.stateprovider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.List;

import org.eclipse.linuxtools.internal.lttng2.kernel.core.Attributes;
import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateSystemDisposedException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.ITmfStateSystem;
import org.eclipse.linuxtools.tmf.core.statevalue.ITmfStateValue;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.junit.Test;

/**
 * Base unit tests for the StateHistorySystem. Extension can be made to test
 * different state back-end types or configurations.
 *
 * @author Alexandre Montplaisir
 */
@SuppressWarnings("javadoc")
public abstract class StateSystemTest {

    /** Test trace used for these tests */
    protected static final CtfTmfTestTrace testTrace = CtfTmfTestTrace.TRACE2;

    /** Expected start time of the test trace/state history */
    protected static final long startTime = 1331668247314038062L;

    /** Expected end time of the state history built from the test trace */
    protected static final long endTime = 1331668259054285979L;

    /** Number of nanoseconds in one second */
    private static final long NANOSECS_PER_SEC = 1000000000L;

    protected static ITmfStateSystem ssq;

    /* Offset in the trace + start time of the trace */
    static final long interestingTimestamp1 = 18670067372290L + 1331649577946812237L;

    @Test
    public void testFullQuery1() {
        List<ITmfStateInterval> list;
        ITmfStateInterval interval;
        int quark, valueInt;
        String valueStr;

        try {
            list = ssq.queryFullState(interestingTimestamp1);

            quark = ssq.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            interval = list.get(quark);
            valueInt = interval.getStateValue().unboxInt();
            assertEquals(1397, valueInt);

            quark = ssq.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
            interval = list.get(quark);
            valueStr = interval.getStateValue().unboxStr();
            assertEquals("gdbus", valueStr);

            quark = ssq.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.SYSTEM_CALL);
            interval = list.get(quark);
            valueStr = interval.getStateValue().unboxStr();
            assertTrue(valueStr.equals("sys_poll"));

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (StateValueTypeException e) {
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
            quark = ssq.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
            interval = ssq.querySingleState(timestamp, quark);
            valueStr = interval.getStateValue().unboxStr();
            assertEquals("gdbus", valueStr);

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (StateValueTypeException e) {
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

        try {
            quark = ssq.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            intervals = ssq.queryHistoryRange(quark, time1, time2);
            assertEquals(487, intervals.size()); /* Number of context switches! */
            assertEquals(1685, intervals.get(100).getStateValue().unboxInt());
            assertEquals(1331668248427681372L, intervals.get(205).getEndTime());

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (StateValueTypeException e) {
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

        try {
            int quark = ssq.getQuarkAbsolute(Attributes.RESOURCES, Attributes.IRQS, "1");
            long ts1 = ssq.getStartTime(); /* start of the trace */
            long ts2 = startTime + 20L * NANOSECS_PER_SEC; /* invalid, but ignored */

            intervals = ssq.queryHistoryRange(quark, ts1, ts2);

            /* Activity of IRQ 1 over the whole trace */
            assertEquals(65, intervals.size());

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
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

        try {
            quark = ssq.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            intervals = ssq.queryHistoryRange(quark, time1, time2, resolution, null);
            assertEquals(126, intervals.size()); /* Number of context switches! */
            assertEquals(1452, intervals.get(50).getStateValue().unboxInt());
            assertEquals(1331668248815698779L, intervals.get(100).getEndTime());

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (StateValueTypeException e) {
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
        ssq.queryFullState(ts);
    }

    @Test(expected = TimeRangeException.class)
    public void testFullQueryInvalidTime2() throws TimeRangeException,
            StateSystemDisposedException {
        long ts = startTime - 20L * NANOSECS_PER_SEC;
        ssq.queryFullState(ts);
    }

    @Test(expected = TimeRangeException.class)
    public void testSingleQueryInvalidTime1() throws TimeRangeException {
        try {
            int quark = ssq.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            long ts = startTime + 20L * NANOSECS_PER_SEC;
            ssq.querySingleState(ts, quark);

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    @Test(expected = TimeRangeException.class)
    public void testSingleQueryInvalidTime2() throws TimeRangeException {
        try {
            int quark = ssq.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            long ts = startTime - 20L * NANOSECS_PER_SEC;
            ssq.querySingleState(ts, quark);

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    @Test(expected = TimeRangeException.class)
    public void testRangeQueryInvalidTime1() throws TimeRangeException {
        try {
            int quark = ssq.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            long ts1 = startTime - 20L * NANOSECS_PER_SEC; /* invalid */
            long ts2 = startTime + 1L * NANOSECS_PER_SEC; /* valid */
            ssq.queryHistoryRange(quark, ts1, ts2);

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    @Test(expected = TimeRangeException.class)
    public void testRangeQueryInvalidTime2() throws TimeRangeException {
        try {
            int quark = ssq.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            long ts1 = startTime - 1L * NANOSECS_PER_SEC; /* invalid */
            long ts2 = startTime + 20L * NANOSECS_PER_SEC; /* invalid */
            ssq.queryHistoryRange(quark, ts1, ts2);

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (StateSystemDisposedException e) {
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
        ssq.getQuarkAbsolute("There", "is", "no", "cow", "level");
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
            list = ssq.queryFullState(interestingTimestamp1);
            quark = ssq.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            interval = list.get(quark);

            /* This is supposed to be an int value */
            interval.getStateValue().unboxStr();

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    @Test(expected = StateValueTypeException.class)
    public void testQueryInvalidValuetype2() throws StateValueTypeException {
        List<ITmfStateInterval> list;
        ITmfStateInterval interval;
        int quark;

        try {
            list = ssq.queryFullState(interestingTimestamp1);
            quark = ssq.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
            interval = list.get(quark);

            /* This is supposed to be a String value */
            interval.getStateValue().unboxInt();

        } catch (AttributeNotFoundException e) {
            fail();
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    @Test
    public void testFullAttributeName() {
        try {
            int quark = ssq.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
            String name = ssq.getFullAttributePath(quark);
            assertEquals(name, "CPUs/0/Current_thread");

        } catch (AttributeNotFoundException e) {
            fail();
        }
    }

    @Test
    public void testGetQuarks_begin() {
        List<Integer> list = ssq.getQuarks("*", "1577", Attributes.EXEC_NAME);

        assertEquals(1, list.size());
    }

    @Test
    public void testGetQuarks_middle() {
        List<Integer> list = ssq.getQuarks(Attributes.THREADS, "*", Attributes.EXEC_NAME);

        /* Number of different kernel threads in the trace */
        assertEquals(168, list.size());
    }

    @Test
    public void testGetQuarks_end() {
        List<Integer> list = ssq.getQuarks(Attributes.THREADS, "1577", "*");

        /* There should be 4 sub-attributes for each Thread node */
        assertEquals(4, list.size());
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
            List<ITmfStateInterval> state = ssq.queryFullState(interestingTimestamp1);
            assertEquals(TestValues.size, state.size());

            for (int i = 0; i < state.size(); i++) {
                /* Test each component of the intervals */
                assertEquals(getStartTimes(i), state.get(i).getStartTime());
                assertEquals(getEndTimes(i), state.get(i).getEndTime());
                assertEquals(i, state.get(i).getAttribute());
                assertEquals(getStateValues(i), state.get(i).getStateValue());
            }

        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        }
    }

    @Test
    public void testFirstIntervalIsConsidered() {
        try {
            List<ITmfStateInterval> list = ssq.queryFullState(1331668248014135800L);
            ITmfStateInterval interval = list.get(233);
            assertEquals(1331668247516664825L, interval.getStartTime());

            int valueInt = interval.getStateValue().unboxInt();
            assertEquals(1, valueInt);
        } catch (TimeRangeException e) {
            fail();
        } catch (StateSystemDisposedException e) {
            fail();
        } catch (StateValueTypeException e) {
            fail();
        }
    }
}
