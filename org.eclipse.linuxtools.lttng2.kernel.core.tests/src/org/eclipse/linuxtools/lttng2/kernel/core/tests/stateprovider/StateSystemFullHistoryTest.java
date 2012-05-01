/*******************************************************************************
 * Copyright (c) 2012 Ericsson
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

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.exceptions.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.exceptions.StateValueTypeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.exceptions.TmfTraceException;
import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.HistoryBuilder;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateHistoryBackend;
import org.eclipse.linuxtools.tmf.core.statesystem.IStateSystemBuilder;
import org.eclipse.linuxtools.tmf.core.statesystem.backend.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CtfKernelStateInput;
import org.eclipse.linuxtools.lttng2.kernel.core.trace.Attributes;
import org.junit.*;

/**
 * Unit tests for the StateHistorySystem, which uses a full (non-partial)
 * history and the non-threaded CTF kernel handler.
 * 
 * @author alexmont
 * 
 */
@SuppressWarnings("nls")
public class StateSystemFullHistoryTest {

    static File stateFile;
    static File stateFileBenchmark;

    static HistoryBuilder builder;
    static IStateChangeInput input;
    static IStateHistoryBackend hp;
    static IStateSystemBuilder ssb;

    /* Offset in the trace + start time of the trace */
    private final static long interestingTimestamp1 = 18670067372290L + 1331649577946812237L;

    protected static String getTestFileName() {
        return "/tmp/statefile.ht"; //$NON-NLS-1$
    }

    @BeforeClass
    public static void initialize() {
        stateFile = new File(getTestFileName());
        stateFileBenchmark = new File(getTestFileName() + ".benchmark"); //$NON-NLS-1$
        try {
            input = new CtfKernelStateInput(CtfTestFiles.getTestTrace());
            hp = new HistoryTreeBackend(stateFile, input.getStartTime());
            builder = new HistoryBuilder(input, hp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.run();
        ssb = builder.getStateSystemBuilder();
        builder.close(); /* Waits for the construction to finish */
    }

    @AfterClass
    public static void cleanup() {
        boolean ret1, ret2;
        ret1 = stateFile.delete();
        ret2 = stateFileBenchmark.delete();
        if ( !(ret1 && ret2) ) {
            System.err.println("Error cleaning up during unit testing, " + //$NON-NLS-1$
            		"you might have leftovers state history files in /tmp"); //$NON-NLS-1$
        }
    }

    /**
     * Rebuild independently so we can benchmark it. Too bad JUnit doesn't allow
     * us to @Test the @BeforeClass...
     * 
     * @throws IOException 
     * @throws TmfTraceException 
     */
    @Test
    public void testBuild() throws IOException, TmfTraceException {
        HistoryBuilder zebuilder;
        IStateChangeInput zeinput;
        IStateHistoryBackend zehp = null;

        zeinput = new CtfKernelStateInput(CtfTestFiles.getTestTrace());
        zehp = new HistoryTreeBackend(stateFileBenchmark, zeinput.getStartTime());
        zebuilder = new HistoryBuilder(zeinput, zehp);
        zebuilder.run();
        zebuilder.close();

        assertEquals(CtfTestFiles.startTime, zehp.getStartTime());
        assertEquals(CtfTestFiles.endTime, zehp.getEndTime());
    }

    @Test
    public void testOpenExistingStateFile() throws IOException {
        IStateHistoryBackend hp2 = null;
        IStateSystemBuilder ssb2 = null;

        /* 'newStateFile' should have already been created */
        hp2 = new HistoryTreeBackend(stateFile);
        ssb2 = HistoryBuilder.openExistingHistory(hp2);

        assertNotNull(ssb2);
        assertEquals(CtfTestFiles.startTime, hp2.getStartTime());
        assertEquals(CtfTestFiles.endTime, hp2.getEndTime());
    }

    @Test
    public void testFullQuery1() throws StateValueTypeException,
            AttributeNotFoundException, TimeRangeException {

        List<ITmfStateInterval> list;
        ITmfStateInterval interval;
        int quark, quark2, valueInt;
        String valueStr;

        list = ssb.loadStateAtTime(interestingTimestamp1);

        quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
        interval = list.get(quark);
        valueInt = interval.getStateValue().unboxInt();
        assertEquals(1397, valueInt);

        quark = ssb.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
        interval = list.get(quark);
        valueStr = interval.getStateValue().unboxStr();
        assertEquals("gdbus", valueStr);

        /* Query a stack attribute, has to be done in two passes */
        quark = ssb.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_MODE_STACK);
        interval = list.get(quark);
        valueInt = interval.getStateValue().unboxInt(); /* The stack depth */
        quark2 = ssb.getQuarkRelative(quark, Integer.toString(valueInt));
        interval = list.get(quark2);
        valueStr = interval.getStateValue().unboxStr();
        assertTrue(valueStr.equals("sys_poll"));
    }

    @Test
    public void testFullQuery2() {
        //
    }

    @Test
    public void testFullQuery3() {
        //
    }

    @Test
    public void testSingleQuery1() throws AttributeNotFoundException,
            TimeRangeException, StateValueTypeException {

        long timestamp = interestingTimestamp1;
        int quark;
        ITmfStateInterval interval;
        String valueStr;

        quark = ssb.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
        interval = ssb.querySingleState(timestamp, quark);
        valueStr = interval.getStateValue().unboxStr();
        assertEquals("gdbus", valueStr);
    }

    @Test
    public void testSingleQuery2() {
        //
    }

    @Test
    public void testSingleQuery3() {
        //
    }

    /**
     * Test a range query (with no resolution parameter, so all intervals)
     */
    @Test
    public void testRangeQuery1() throws AttributeNotFoundException,
            TimeRangeException, StateValueTypeException {

        long time1 = interestingTimestamp1;
        long time2 = time1 + 1L * CtfTestFiles.NANOSECS_PER_SEC;
        int quark;
        List<ITmfStateInterval> intervals;

        quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
        intervals = ssb.queryHistoryRange(quark, time1, time2);
        assertEquals(487, intervals.size()); /* Number of context switches! */
        assertEquals(1685, intervals.get(100).getStateValue().unboxInt());
        assertEquals(1331668248427681372L, intervals.get(205).getEndTime());
    }

    /**
     * Range query, but with a t2 far off the end of the trace.
     * The result should still be valid.
     */
    @Test
    public void testRangeQuery2() throws TimeRangeException,
            AttributeNotFoundException {

        List<ITmfStateInterval> intervals;
        
        int quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.IRQ_STACK);
        long ts1 = ssb.getStartTime(); /* start of the trace */
        long ts2 = CtfTestFiles.startTime + 20L * CtfTestFiles.NANOSECS_PER_SEC; /* invalid, but ignored */

        intervals = ssb.queryHistoryRange(quark, ts1, ts2);

        /* Nb of IRQs on CPU 0 during the whole trace */
        assertEquals(1653, intervals.size());
    }

    /**
     * Test a range query with a resolution
     */
    @Test
    public void testRangeQuery3() throws AttributeNotFoundException,
            TimeRangeException, StateValueTypeException {

        long time1 = interestingTimestamp1;
        long time2 = time1 + 1L * CtfTestFiles.NANOSECS_PER_SEC;
        long resolution = 1000000; /* One query every millisecond */
        int quark;
        List<ITmfStateInterval> intervals;

        quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
        intervals = ssb.queryHistoryRange(quark, time1, time2, resolution);
        assertEquals(129, intervals.size()); /* Number of context switches! */
        assertEquals(1452, intervals.get(50).getStateValue().unboxInt());
        assertEquals(1331668248784789238L, intervals.get(100).getEndTime());
    }

    /**
     * Ask for a time range outside of the trace's range
     * 
     * @throws TimeRangeException
     */
    @Test(expected = TimeRangeException.class)
    public void testFullQueryInvalidTime1() throws TimeRangeException {
        long ts = CtfTestFiles.startTime + 20L * CtfTestFiles.NANOSECS_PER_SEC;
        ssb.loadStateAtTime(ts);

    }

    @Test(expected = TimeRangeException.class)
    public void testFullQueryInvalidTime2() throws TimeRangeException {
        long ts = CtfTestFiles.startTime - 20L * CtfTestFiles.NANOSECS_PER_SEC;
        ssb.loadStateAtTime(ts);

    }

    @Test(expected = TimeRangeException.class)
    public void testSingleQueryInvalidTime1()
            throws AttributeNotFoundException, TimeRangeException {

        int quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
        long ts = CtfTestFiles.startTime + 20L * CtfTestFiles.NANOSECS_PER_SEC;
        ssb.querySingleState(ts, quark);
    }

    @Test(expected = TimeRangeException.class)
    public void testSingleQueryInvalidTime2()
            throws AttributeNotFoundException, TimeRangeException {

        int quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
        long ts = CtfTestFiles.startTime - 20L * CtfTestFiles.NANOSECS_PER_SEC;
        ssb.querySingleState(ts, quark);
    }

    @Test(expected = TimeRangeException.class)
    public void testRangeQueryInvalidTime1() throws AttributeNotFoundException,
            TimeRangeException {

        int quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
        long ts1 = CtfTestFiles.startTime - 20L * CtfTestFiles.NANOSECS_PER_SEC; /* invalid */
        long ts2 = CtfTestFiles.startTime + 1L * CtfTestFiles.NANOSECS_PER_SEC; /* valid */

        ssb.queryHistoryRange(quark, ts1, ts2);
    }

    @Test(expected = TimeRangeException.class)
    public void testRangeQueryInvalidTime2() throws TimeRangeException,
            AttributeNotFoundException {

        int quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
        long ts1 = CtfTestFiles.startTime - 1L * CtfTestFiles.NANOSECS_PER_SEC; /* invalid */
        long ts2 = CtfTestFiles.startTime + 20L * CtfTestFiles.NANOSECS_PER_SEC; /* invalid */

        ssb.queryHistoryRange(quark, ts1, ts2);
    }

    /**
     * Ask for a non-existing attribute
     * 
     * @throws AttributeNotFoundException
     */
    @Test(expected = AttributeNotFoundException.class)
    public void testQueryInvalidAttribute() throws AttributeNotFoundException {

        ssb.getQuarkAbsolute("There", "is", "no", "cow", "level");
    }

    /**
     * Query but with the wrong State Value type
     * 
     * @throws StateValueTypeException
     * @throws AttributeNotFoundException
     * @throws TimeRangeException
     */
    @Test(expected = StateValueTypeException.class)
    public void testQueryInvalidValuetype1() throws StateValueTypeException,
            AttributeNotFoundException, TimeRangeException {
        List<ITmfStateInterval> list;
        ITmfStateInterval interval;
        int quark;

        list = ssb.loadStateAtTime(interestingTimestamp1);
        quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
        interval = list.get(quark);

        /* This is supposed to be an int value */
        interval.getStateValue().unboxStr();
    }

    @Test(expected = StateValueTypeException.class)
    public void testQueryInvalidValuetype2() throws StateValueTypeException,
            AttributeNotFoundException, TimeRangeException {
        List<ITmfStateInterval> list;
        ITmfStateInterval interval;
        int quark;

        list = ssb.loadStateAtTime(interestingTimestamp1);
        quark = ssb.getQuarkAbsolute(Attributes.THREADS, "1432", Attributes.EXEC_NAME);
        interval = list.get(quark);

        /* This is supposed to be a String value */
        interval.getStateValue().unboxInt();
    }

    @Test
    public void testFullAttributeName() throws AttributeNotFoundException {
        int quark = ssb.getQuarkAbsolute(Attributes.CPUS, "0", Attributes.CURRENT_THREAD);
        String name = ssb.getFullAttributePath(quark);
        assertEquals(name, "CPUs/0/Current_thread");
    }

    @Test
    public void testGetQuarks_begin() {
        List<Integer> list = ssb.getQuarks("*", "1577", Attributes.EXEC_NAME);

        assertEquals(1, list.size());
    }

    @Test
    public void testGetQuarks_middle() {
        List<Integer> list = ssb.getQuarks(Attributes.THREADS, "*", Attributes.EXEC_NAME);

        /* Number of different kernel threads in the trace */
        assertEquals(168, list.size());
    }

    @Test
    public void testGetQuarks_end() {
        List<Integer> list = ssb.getQuarks(Attributes.THREADS, "1577", "*");

        /* There should be 4 sub-attributes for each Thread node */
        assertEquals(4, list.size());
    }
}
