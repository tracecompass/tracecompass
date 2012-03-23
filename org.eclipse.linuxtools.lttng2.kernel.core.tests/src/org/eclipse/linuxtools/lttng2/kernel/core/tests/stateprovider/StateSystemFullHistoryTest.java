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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

import org.eclipse.linuxtools.tmf.core.interval.ITmfStateInterval;
import org.eclipse.linuxtools.tmf.core.statesystem.AttributeNotFoundException;
import org.eclipse.linuxtools.tmf.core.statesystem.StateHistorySystem;
import org.eclipse.linuxtools.tmf.core.statesystem.TimeRangeException;
import org.eclipse.linuxtools.tmf.core.statesystem.backend.historytree.HistoryTreeBackend;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.HistoryBuilder;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateChangeInput;
import org.eclipse.linuxtools.tmf.core.statesystem.helpers.IStateHistoryBackend;
import org.eclipse.linuxtools.tmf.core.statevalue.StateValueTypeException;
import org.eclipse.linuxtools.internal.lttng2.kernel.core.stateprovider.CTFKernelStateInput;
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

    protected static File stateFile;
    protected static File stateFileBenchmark;

    protected static HistoryBuilder builder;
    protected static IStateChangeInput input;
    protected static IStateHistoryBackend hp;
    protected static StateHistorySystem shs;

    private final static long interestingTimestamp1 = 18670067372290L;

    protected static String getTestFileName() {
        return "/tmp/statefile.ht"; //$NON-NLS-1$
    }

    @BeforeClass
    public static void initialize() {
        stateFile = new File(getTestFileName());
        stateFileBenchmark = new File(getTestFileName() + ".benchmark"); //$NON-NLS-1$
        try {
            input = new CTFKernelStateInput(CTFTestFiles.getTestTrace());
            hp = new HistoryTreeBackend(stateFile, input.getStartTime());
            builder = new HistoryBuilder(input, hp);
        } catch (Exception e) {
            e.printStackTrace();
        }
        builder.run();
        shs = (StateHistorySystem) builder.getSS();
    }

    @AfterClass
    public static void cleanup() {
        stateFile.delete();
        stateFileBenchmark.delete();
    }

    /**
     * Rebuild independently so we can benchmark it. Too bad JUnit doesn't allow
     * us to @Test the @BeforeClass...
     */
    @Test
    public void testBuild() {
        HistoryBuilder zebuilder;
        IStateChangeInput zeinput;
        IStateHistoryBackend zehp;

        try {
            zeinput = new CTFKernelStateInput(CTFTestFiles.getTestTrace());
            zehp = new HistoryTreeBackend(stateFileBenchmark,
                    zeinput.getStartTime());
            zebuilder = new HistoryBuilder(zeinput, zehp);
            zebuilder.run();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testOpenExistingStateFile() {
        IStateHistoryBackend hp2 = null;
        StateHistorySystem shs2 = null;
        try {
            /* 'newStateFile' should have already been created */
            hp2 = new HistoryTreeBackend(stateFile);
            shs2 = new StateHistorySystem(hp2, false);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assertTrue(shs2 != null);
    }

    @Test
    public void testFullQuery1() throws StateValueTypeException,
            AttributeNotFoundException, TimeRangeException {

        ITmfStateInterval interval;
        int quark, valueInt;
        String valueStr;

        shs.loadStateAtTime(interestingTimestamp1);

        quark = shs.getQuarkAbsolute("CPUs", "0", "Current_thread");
        interval = shs.queryState(quark);
        valueInt = interval.getStateValue().unboxInt();
        assertEquals(1397, valueInt);

        quark = shs.getQuarkAbsolute("Threads", "1432", "Exec_name");
        interval = shs.queryState(quark);
        valueStr = interval.getStateValue().unboxStr();
        assertEquals("gdbus", valueStr);

        // FIXME fails at the moment (attribute type is int, and = 3129??), I'll
        // figure it out later
        // quark = shs.getQuarkAbsolute("Threads", "3109", "Exec_mode_stack");
        // interval = shs.getState(quark);
        // valueStr = interval.getStateValue().unboxStr();
        // assertTrue( valueStr.equals("bloup") );
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

        quark = shs.getQuarkAbsolute("Threads", "1432", "Exec_name");
        interval = shs.querySingleState(timestamp, quark);
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

    @Test
    public void testRangeQuery1() throws AttributeNotFoundException,
            TimeRangeException, StateValueTypeException {

        long time1 = interestingTimestamp1;
        long time2 = time1 + 1L * CTFTestFiles.NANOSECS_PER_SEC;
        int quark;
        List<ITmfStateInterval> intervals;

        quark = shs.getQuarkAbsolute("CPUs", "0", "Current_thread");
        intervals = shs.queryHistoryRange(quark, time1, time2);
        assertEquals(487, intervals.size()); /* Number of context switches! */
        assertEquals(1685, intervals.get(100).getStateValue().unboxInt());
        assertEquals(18670480869135L, intervals.get(205).getEndTime());
    }

    /**
     * Ask for a time range outside of the trace's range
     * 
     * @throws TimeRangeException
     */
    @Test(expected = TimeRangeException.class)
    public void testFullQueryInvalidTime1() throws TimeRangeException {
        long ts = CTFTestFiles.startTime + 20L * CTFTestFiles.NANOSECS_PER_SEC;
        shs.loadStateAtTime(ts);

    }

    @Test(expected = TimeRangeException.class)
    public void testFullQueryInvalidTime2() throws TimeRangeException {
        long ts = CTFTestFiles.startTime - 20L * CTFTestFiles.NANOSECS_PER_SEC;
        shs.loadStateAtTime(ts);

    }

    @Test(expected = TimeRangeException.class)
    public void testSingleQueryInvalidTime1()
            throws AttributeNotFoundException, TimeRangeException {

        int quark = shs.getQuarkAbsolute("CPUs", "0", "Current_thread");
        long ts = CTFTestFiles.startTime + 20L * CTFTestFiles.NANOSECS_PER_SEC;
        shs.querySingleState(ts, quark);
    }

    @Test(expected = TimeRangeException.class)
    public void testSingleQueryInvalidTime2()
            throws AttributeNotFoundException, TimeRangeException {

        int quark = shs.getQuarkAbsolute("CPUs", "0", "Current_thread");
        long ts = CTFTestFiles.startTime - 20L * CTFTestFiles.NANOSECS_PER_SEC;
        shs.querySingleState(ts, quark);
    }

    @Test(expected = TimeRangeException.class)
    public void testRangeQueryInvalidTime1() throws AttributeNotFoundException,
            TimeRangeException {

        int quark = shs.getQuarkAbsolute("CPUs", "0", "Current_thread");
        long ts1 = CTFTestFiles.startTime - 20L * CTFTestFiles.NANOSECS_PER_SEC; /* invalid */
        long ts2 = CTFTestFiles.startTime + 1L * CTFTestFiles.NANOSECS_PER_SEC; /* valid */

        shs.queryHistoryRange(quark, ts1, ts2);
    }

    @Test(expected = TimeRangeException.class)
    public void testRangeQueryInvalidTime2() throws TimeRangeException,
            AttributeNotFoundException {

        int quark = shs.getQuarkAbsolute("CPUs", "0", "Current_thread");
        long ts1 = CTFTestFiles.startTime + 1L * CTFTestFiles.NANOSECS_PER_SEC; /* valid */
        long ts2 = CTFTestFiles.startTime + 20L * CTFTestFiles.NANOSECS_PER_SEC; /* invalid */

        shs.queryHistoryRange(quark, ts1, ts2);
    }

    @Test(expected = TimeRangeException.class)
    public void testRangeQueryInvalidTime3() throws TimeRangeException,
            AttributeNotFoundException {

        int quark = shs.getQuarkAbsolute("CPUs", "0", "Current_thread");
        long ts1 = CTFTestFiles.startTime - 1L * CTFTestFiles.NANOSECS_PER_SEC; /* invalid */
        long ts2 = CTFTestFiles.startTime + 20L * CTFTestFiles.NANOSECS_PER_SEC; /* invalid */

        shs.queryHistoryRange(quark, ts1, ts2);
    }

    /**
     * Ask for a non-existing attribute
     * 
     * @throws AttributeNotFoundException
     */
    @Test(expected = AttributeNotFoundException.class)
    public void testQueryInvalidAttribute() throws AttributeNotFoundException {

        shs.getQuarkAbsolute("There", "is", "no", "cow", "level");
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
        ITmfStateInterval interval;
        int quark;

        shs.loadStateAtTime(interestingTimestamp1);
        quark = shs.getQuarkAbsolute("CPUs", "0", "Current_thread");
        interval = shs.queryState(quark);

        /* This is supposed to be an int value */
        interval.getStateValue().unboxStr();
    }

    @Test(expected = StateValueTypeException.class)
    public void testQueryInvalidValuetype2() throws StateValueTypeException,
            AttributeNotFoundException, TimeRangeException {
        ITmfStateInterval interval;
        int quark;

        shs.loadStateAtTime(interestingTimestamp1);
        quark = shs.getQuarkAbsolute("Threads", "1432", "Exec_name");
        interval = shs.queryState(quark);

        /* This is supposed to be a String value */
        interval.getStateValue().unboxInt();
    }

    @Test
    public void testFullAttributeName() throws AttributeNotFoundException {
        int quark = shs.getQuarkAbsolute("CPUs", "0", "Current_thread");
        String name = shs.getFullAttributePath(quark);
        assertEquals(name, "CPUs/0/Current_thread");
    }

    @Test
    public void testGetQuarks_begin() {
        List<Integer> list = shs.getQuarks("*", "1577", "Exec_name");

        assertEquals(1, list.size());
        assertEquals(Integer.valueOf(479), list.get(0));
    }

    @Test
    public void testGetQuarks_middle() {
        List<Integer> list = shs.getQuarks("Threads", "*", "Exec_name");

        assertEquals(Integer.valueOf(36), list.get(4));
        assertEquals(Integer.valueOf(100), list.get(10));
        assertEquals(Integer.valueOf(116), list.get(12));
    }

    @Test
    public void testGetQuarks_end() {
        List<Integer> list = shs.getQuarks("Threads", "1577", "*");

        assertEquals(3, list.size());
        assertEquals(Integer.valueOf(479), list.get(1));
    }

    @Test
    public void testDebugPrinting() throws FileNotFoundException {
        shs.debugPrint(new PrintWriter(new File("/dev/null")));
    }
}
