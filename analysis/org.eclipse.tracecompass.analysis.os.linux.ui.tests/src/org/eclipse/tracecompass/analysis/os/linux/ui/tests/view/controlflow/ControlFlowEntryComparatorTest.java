/*******************************************************************************
 * Copyright (c) 2016 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/

package org.eclipse.tracecompass.analysis.os.linux.ui.tests.view.controlflow;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.swt.SWT;
import org.eclipse.tracecompass.internal.analysis.os.linux.core.threadstatus.ThreadEntryModel;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.ControlFlowColumnComparators;
import org.eclipse.tracecompass.internal.analysis.os.linux.ui.views.controlflow.IControlFlowEntryComparator;
import org.eclipse.tracecompass.tmf.ui.views.timegraph.ITimeGraphEntryComparator;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.ITimeGraphEntry;
import org.eclipse.tracecompass.tmf.ui.widgets.timegraph.model.TimeGraphEntry;
import org.junit.Test;

/**
 * Test cases for verifying the ControlFlowEntry comparators used in the Control Flow  View.
 *
 * @author Bernd Hufmann
 *
 */
public class ControlFlowEntryComparatorTest {

    private static final @NonNull String TRACE1_NAME = "/synctraces/scp_dest";
    private static final @NonNull String TRACE2_NAME = "/synctraces/scp_src";
    private static final @NonNull String TRACE3_NAME = "/debuginfo-test-app4";

    private static final @NonNull String TRACE_EXEC_NAME1 = "AAA";
    private static final @NonNull String TRACE_EXEC_NAME2 = "BBB";
    private static final @NonNull String TRACE_EXEC_NAME3 = "CCC";

    private static final int TRACE_TID1 = 1;
    private static final int TRACE_TID2 = 2;
    private static final int TRACE_TID3 = 3;

    private static final int TRACE_PTID1 = 1;
    private static final int TRACE_PTID2 = 2;
    private static final int TRACE_PTID3 = 3;

    private static final int TRACE_START_TIME1 = 1;
    private static final int TRACE_START_TIME2 = 2;
    private static final int TRACE_START_TIME3 = 3;

    private static final int TRACE_END_TIME = 4;

    /**
     * Test {@link IControlFlowEntryComparator#PROCESS_NAME_COMPARATOR}
     */
    @Test
    public void execNameComparatorTest() {
        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME2, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME3, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        List<TimeGraphEntry> expected = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);

        Collections.sort(testVec, IControlFlowEntryComparator.PROCESS_NAME_COMPARATOR);
        assertEquals(expected, testVec);
    }

    /**
     * Test {@link IControlFlowEntryComparator#TID_COMPARATOR}
     */
    @Test
    public void tidComparatorTest() {
        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID2, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID3, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        List<TimeGraphEntry> expected = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);

        Collections.sort(testVec, IControlFlowEntryComparator.TID_COMPARATOR);
        assertEquals(expected, testVec);
    }

    /**
     * Helper function to make entries to test. As they are only looking up the
     * tid, that is the only parameter that needs to be passed.
     *
     * @param tid
     *            the TID to lookup
     * @param traceEndTime
     * @param traceStartTime1
     * @param tracePtid1
     * @param traceTid1
     * @param traceExecName1
     * @return a {@link TimeGraphEntry} with the correct tid.
     */
    private static TimeGraphEntry generateCFVEntry(int id, @NonNull String execName, int tid, int ptid, int startTime, int endTime) {
        return new TimeGraphEntry(new ThreadEntryModel(0, -1, Collections.singletonList(execName), startTime, endTime, tid, ptid, -1));
    }

    /**
     * Test {@link IControlFlowEntryComparator#PTID_COMPARATOR}
     */
    @Test
    public void ptidComparatorTest() {
        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID2, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID3, TRACE_START_TIME1, TRACE_END_TIME);
        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        List<TimeGraphEntry> expected = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);

        Collections.sort(testVec, IControlFlowEntryComparator.PTID_COMPARATOR);
        assertEquals(expected, testVec);
    }

    /**
     * Test {@link IControlFlowEntryComparator#BIRTH_TIME_COMPARATOR}
     */
    @Test
    public void birthTimeComparatorTest() {
        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME2, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME3, TRACE_END_TIME);
        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        List<TimeGraphEntry> expected = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);

        Collections.sort(testVec, IControlFlowEntryComparator.BIRTH_TIME_COMPARATOR);
        assertEquals(expected, testVec);
    }

    /**
     * Test {@link ControlFlowColumnComparators#PROCESS_NAME_COLUMN_COMPARATOR}
     */
    @Test
    public void execNameColumnComparatorTest() {
        TimeGraphEntry trace1Entry = new TimeGraphEntry(TRACE1_NAME, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace2Entry = new TimeGraphEntry(TRACE2_NAME, TRACE_START_TIME2, TRACE_END_TIME);
        TimeGraphEntry trace3Entry = new TimeGraphEntry(TRACE3_NAME, TRACE_START_TIME3, TRACE_END_TIME);

        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry, trace2Entry, trace3Entry);
        List<TimeGraphEntry> expectedDown = Arrays.asList(trace3Entry, trace1Entry, trace2Entry);
        List<TimeGraphEntry> expectedUp = Arrays.asList(trace2Entry, trace1Entry, trace3Entry);
        runTest(ControlFlowColumnComparators.PROCESS_NAME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME2, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME3, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry3, trace1Entry2, trace1Entry1);
        runTest(ControlFlowColumnComparators.PROCESS_NAME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);
    }

    /**
     * Test {@link ControlFlowColumnComparators#PROCESS_NAME_COLUMN_COMPARATOR}
     *
     * Note, that when the exec name is the same: The order that is birth time,
     * TID and PTID. Note that for secondary comparators the sort direction is
     * not changed.
     */
    @Test
    public void execNameSecondaryTimeColumnComparatorTest() {
        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME2, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME3, TRACE_END_TIME);

        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        List<TimeGraphEntry> expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        List<TimeGraphEntry> expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.PROCESS_NAME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID2, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID3, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.PROCESS_NAME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID2, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID3, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.PROCESS_NAME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);
    }

    /**
     * Test {@link ControlFlowColumnComparators#TID_COLUMN_COMPARATOR}
     */
    @Test
    public void tidColumnComparatorTest() {
        TimeGraphEntry trace1Entry = new TimeGraphEntry(TRACE1_NAME, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace2Entry = new TimeGraphEntry(TRACE2_NAME, TRACE_START_TIME2, TRACE_END_TIME);
        TimeGraphEntry trace3Entry = new TimeGraphEntry(TRACE3_NAME, TRACE_START_TIME3, TRACE_END_TIME);

        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry, trace2Entry, trace3Entry);
        List<TimeGraphEntry> expectedDown = Arrays.asList(trace1Entry, trace2Entry, trace3Entry);
        List<TimeGraphEntry> expectedUp = Arrays.asList(trace1Entry, trace2Entry, trace3Entry);
        runTest(ControlFlowColumnComparators.TID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID2, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID3, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry3, trace1Entry2, trace1Entry1);
        runTest(ControlFlowColumnComparators.TID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);
    }

    /**
     * Test {@link ControlFlowColumnComparators#TID_COLUMN_COMPARATOR}
     *
     * Note, that when TID is the same: The order for for that is birth
     * time, process name and PTID. Note that for secondary comparators the
     * sort direction is not changed.
     */
    @Test
    public void tidSecondaryTimeColumnComparatorTest() {

        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME2, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME3, TRACE_END_TIME);

        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        List<TimeGraphEntry> expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        List<TimeGraphEntry> expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.TID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME2, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME3, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.TID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID2, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID3, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.TID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);
    }

    /**
     * Test {@link ControlFlowColumnComparators#PTID_COLUMN_COMPARATOR}
     */
    @Test
    public void ptidColumnComparatorTest() {
        TimeGraphEntry trace1Entry = new TimeGraphEntry(TRACE1_NAME, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace2Entry = new TimeGraphEntry(TRACE2_NAME, TRACE_START_TIME2, TRACE_END_TIME);
        TimeGraphEntry trace3Entry = new TimeGraphEntry(TRACE3_NAME, TRACE_START_TIME3, TRACE_END_TIME);

        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry, trace2Entry, trace3Entry);
        List<TimeGraphEntry> expectedDown = Arrays.asList(trace1Entry, trace2Entry, trace3Entry);
        List<TimeGraphEntry> expectedUp = Arrays.asList(trace1Entry, trace2Entry, trace3Entry);
        runTest(ControlFlowColumnComparators.PTID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID2, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID3, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry3, trace1Entry2, trace1Entry1);
        runTest(ControlFlowColumnComparators.PTID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);
    }

    /**
     * Test {@link ControlFlowColumnComparators#PTID_COLUMN_COMPARATOR}
     *
     * Note, that when PTID is the same: The order for that is birth time,
     * process name and TID. Note that for secondary comparators the sort
     * direction is not changed.
     */
    @Test
    public void ptidSecondaryTimeColumnComparatorTest() {
        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME2, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME3, TRACE_END_TIME);

        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        List<TimeGraphEntry> expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        List<TimeGraphEntry> expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.PTID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME2, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME3, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.PTID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID2, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID3, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.PTID_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);
    }

    /**
     * Test {@link ControlFlowColumnComparators#BIRTH_TIME_COLUMN_COMPARATOR}
     */
    @Test
    public void birthTimeColumnComparatorTest() {
        TimeGraphEntry trace1Entry = new TimeGraphEntry(TRACE1_NAME, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace2Entry = new TimeGraphEntry(TRACE2_NAME, TRACE_START_TIME2, TRACE_END_TIME);
        TimeGraphEntry trace3Entry = new TimeGraphEntry(TRACE3_NAME, TRACE_START_TIME3, TRACE_END_TIME);

        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry, trace2Entry, trace3Entry);
        List<TimeGraphEntry> expectedDown = Arrays.asList(trace1Entry, trace2Entry, trace3Entry);
        List<TimeGraphEntry> expectedUp = Arrays.asList(trace3Entry, trace2Entry, trace1Entry);
        runTest(ControlFlowColumnComparators.BIRTH_TIME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME2, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME3, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry3, trace1Entry2, trace1Entry1);
        runTest(ControlFlowColumnComparators.BIRTH_TIME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);
    }

    /**
     * Test {@link ControlFlowColumnComparators#BIRTH_TIME_COLUMN_COMPARATOR}
     *
     * Note, that when when birth time is the same: The order for that is
     * process name, TID and PTID. Note that for secondary comparators the
     * sort direction is not changed.
     */
    @Test
    public void birthTimeSecondaryTimeColumnComparatorTest() {
        TimeGraphEntry trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME2, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        TimeGraphEntry trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME3, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);

        List<TimeGraphEntry> testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        List<TimeGraphEntry> expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        List<TimeGraphEntry> expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.BIRTH_TIME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID2, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID2, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.BIRTH_TIME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

        trace1Entry1 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID1, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry2 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID2, TRACE_START_TIME1, TRACE_END_TIME);
        trace1Entry3 = generateCFVEntry(0, TRACE_EXEC_NAME1, TRACE_TID1, TRACE_PTID3, TRACE_START_TIME1, TRACE_END_TIME);

        testVec = Arrays.asList(trace1Entry2, trace1Entry3, trace1Entry1);
        expectedDown = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        expectedUp = Arrays.asList(trace1Entry1, trace1Entry2, trace1Entry3);
        runTest(ControlFlowColumnComparators.BIRTH_TIME_COLUMN_COMPARATOR, testVec, expectedDown, expectedUp);

    }

    private static void runTest(ITimeGraphEntryComparator comparator, List<TimeGraphEntry> testVec, List<TimeGraphEntry> expectedDown, List<TimeGraphEntry> expectedUp) {
        comparator.setDirection(SWT.DOWN);
        Collections.sort(testVec, comparator);
        assertEquals(expectedDown, testVec);

        comparator.setDirection(SWT.UP);
        Comparator<ITimeGraphEntry> reverseComp = comparator;
        reverseComp = Collections.reverseOrder(reverseComp);
        Collections.sort(testVec, reverseComp);
        assertEquals(expectedUp, testVec);
    }

}
