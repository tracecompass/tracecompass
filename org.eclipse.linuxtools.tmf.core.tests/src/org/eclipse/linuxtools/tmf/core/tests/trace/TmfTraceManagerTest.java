/*******************************************************************************
 * Copyright (c) 2013 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assume.assumeTrue;

import java.io.File;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTrace;
import org.eclipse.linuxtools.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.linuxtools.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.linuxtools.tmf.core.trace.ITmfTrace;
import org.eclipse.linuxtools.tmf.core.trace.TmfExperiment;
import org.eclipse.linuxtools.tmf.core.trace.TmfTraceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test suite for the {@link TmfTraceManager}.
 *
 * @author Alexandre Montplaisir
 */
public class TmfTraceManagerTest {

    private static final int SCALE = ITmfTimestamp.NANOSECOND_SCALE;

    private static ITmfTrace trace1;
    private static final long t1start = 1331668247314038062L;
    private static final long t1end =   1331668259054285979L;

    private static ITmfTrace trace2;
    private static final long t2start = 1332170682440133097L;
    private static final long t2end =   1332170692664579801L;

    private static final long ONE_SECOND = 1000000000L;

    private TmfTraceManager tm;


    /**
     * Test class initialization
     */
    @BeforeClass
    public static void setUpClass() {
        assumeTrue(CtfTmfTestTrace.TRACE2.exists());
        assumeTrue(CtfTmfTestTrace.KERNEL.exists());
        trace1 = CtfTmfTestTrace.TRACE2.getTrace();
        trace2 = CtfTmfTestTrace.KERNEL.getTrace();

        trace1.indexTrace(true);
        trace2.indexTrace(true);
    }

    /**
     * Test initialization
     */
    @Before
    public void setUp() {
        tm = TmfTraceManager.getInstance();
    }

    /**
     * Test clean-up
     */
    @After
    public void tearDown() {
        while (tm.getActiveTrace() != null) {
            closeTrace(tm.getActiveTrace());
        }
    }

    // ------------------------------------------------------------------------
    // Dummy actions (fake signals)
    // ------------------------------------------------------------------------

    private void openTrace(ITmfTrace trace) {
        TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, trace, null));
        selectTrace(trace);
    }

    private void closeTrace(ITmfTrace trace) {
        TmfSignalManager.dispatchSignal(new TmfTraceClosedSignal(this, trace));
        /*
         * In TMF, the next tab would now be selected (if there are some), which
         * would select another trace automatically.
         */
        if (tm.getOpenedTraces().size() > 0) {
            selectTrace(tm.getOpenedTraces().toArray(new ITmfTrace[0])[0]);
        }
    }

    private void selectTrace(ITmfTrace trace) {
        TmfSignalManager.dispatchSignal(new TmfTraceSelectedSignal(this, trace));
    }

    private void selectTimestamp(ITmfTimestamp ts) {
        TmfSignalManager.dispatchSignal(new TmfTimeSynchSignal(this, ts));
    }

    private void selectTimeRange(TmfTimeRange tr) {
        TmfSignalManager.dispatchSignal(new TmfRangeSynchSignal(this, tr));
    }

    // ------------------------------------------------------------------------
    // General tests
    // ------------------------------------------------------------------------

    /**
     * Test that the manager is correctly initialized
     */
    @Test
    public void testInitialize() {
        TmfTraceManager mgr = TmfTraceManager.getInstance();
        assertNotNull(mgr);
        assertSame(tm, mgr);
    }

    /**
     * Test the contents of a trace set with one trace.
     */
    @Test
    public void testTraceSet() {
        openTrace(trace1);
        openTrace(trace2);
        selectTrace(trace2);

        ITmfTrace[] expected = new ITmfTrace[] { trace2 };
        ITmfTrace[] actual = tm.getActiveTraceSet();

        assertEquals(1, actual.length);
        assertArrayEquals(expected, actual);
    }

    /**
     * Test the contents of a trace set with an experiment.
     */
    @Test
    public void testTraceSetExperiment() {
        TmfExperiment exp = createExperiment(trace1, trace2);
        openTrace(trace1);
        openTrace(exp);

        ITmfTrace[] expected = new ITmfTrace[] { trace1, trace2 };
        ITmfTrace[] actual = tm.getActiveTraceSet();

        assertEquals(2, actual.length);
        assertArrayEquals(expected, actual);
    }

    /**
     * Test the {@link TmfTraceManager#getSupplementaryFileDir} method.
     */
    @Test
    public void testSupplementaryFileDir() {
        String name1 = trace1.getName();
        String name2 = trace2.getName();
        String basePath = System.getProperty("java.io.tmpdir") + File.separator;

        String expected1 = basePath + name1 + File.separator;
        String expected2 = basePath + name2 + File.separator;

        assertEquals(expected1, TmfTraceManager.getSupplementaryFileDir(trace1));
        assertEquals(expected2, TmfTraceManager.getSupplementaryFileDir(trace2));
    }

    // ------------------------------------------------------------------------
    // Test a single trace
    // ------------------------------------------------------------------------

    /**
     * Test the initial range of a single trace.
     */
    @Test
    public void testTraceInitialRange() {
        openTrace(trace2);
        final TmfTimeRange expectedRange = new TmfTimeRange(
                trace2.getStartTime(),
                calculateOffset(trace2.getStartTime(), trace2.getInitialRangeOffset()));
        TmfTimeRange actualRange = tm.getCurrentRange();
        assertEquals(expectedRange, actualRange);
    }

    /**
     * Try selecting a timestamp contained inside the trace's range. The trace's
     * current time should get updated correctly.
     */
    @Test
    public void testNewTimestamp() {
        openTrace(trace2);
        ITmfTimestamp ts = new TmfTimestamp(t2start + ONE_SECOND, SCALE);
        selectTimestamp(ts);

        ITmfTimestamp afterTs = tm.getSelectionBeginTime();
        assertEquals(ts, afterTs);
        afterTs = tm.getSelectionEndTime();
        assertEquals(ts, afterTs);
    }

    /**
     * Try selecting a timestamp happening before the trace's start. The change
     * should be ignored.
     */
    @Test
    public void testTimestampBefore() {
        openTrace(trace2);
        ITmfTimestamp beforeTs = tm.getSelectionBeginTime();
        ITmfTimestamp ts = new TmfTimestamp(t2start - ONE_SECOND, SCALE);
        selectTimestamp(ts);

        ITmfTimestamp curTs = tm.getSelectionBeginTime();
        assertEquals(beforeTs, curTs);
        curTs = tm.getSelectionEndTime();
        assertEquals(beforeTs, curTs);
    }

    /**
     * Try selecting a timestamp happening after the trace's end. The change
     * should be ignored.
     */
    @Test
    public void testTimestampAfter() {
        openTrace(trace2);
        ITmfTimestamp beforeTs = tm.getSelectionBeginTime();
        ITmfTimestamp ts = new TmfTimestamp(t2end + ONE_SECOND, SCALE);
        selectTimestamp(ts);

        ITmfTimestamp curTs = tm.getSelectionBeginTime();
        assertEquals(beforeTs, curTs);
        curTs = tm.getSelectionEndTime();
        assertEquals(beforeTs, curTs);
    }

    /**
     * Test selecting a normal sub-range of a single trace.
     */
    @Test
    public void testTraceNewTimeRange() {
        openTrace(trace2);
        TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t2start + ONE_SECOND, SCALE),
                new TmfTimestamp(t2end - ONE_SECOND, SCALE));
        selectTimeRange(range);

        TmfTimeRange curRange = tm.getCurrentRange();
        assertEquals(range.getStartTime(), curRange.getStartTime());
        assertEquals(range.getEndTime(), curRange.getEndTime());
    }

    /**
     * Test selecting a range whose start time is before the trace's start time.
     * The selected range should get clamped to the trace's range.
     */
    @Test
    public void testTraceTimeRangeClampingStart() {
        openTrace(trace2);
        TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t2start - ONE_SECOND, SCALE), // minus here
                new TmfTimestamp(t2end - ONE_SECOND, SCALE));
        selectTimeRange(range);

        TmfTimeRange curRange = tm.getCurrentRange();
        assertEquals(t2start, curRange.getStartTime().getValue());
        assertEquals(range.getEndTime(), curRange.getEndTime());
    }

    /**
     * Test selecting a range whose end time is after the trace's end time.
     * The selected range should get clamped to the trace's range.
     */
    @Test
    public void testTraceTimeRangeClampingEnd() {
        openTrace(trace2);
        TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t2start + ONE_SECOND, SCALE),
                new TmfTimestamp(t2end + ONE_SECOND, SCALE)); // plus here
        selectTimeRange(range);

        TmfTimeRange curRange = tm.getCurrentRange();
        assertEquals(range.getStartTime(), curRange.getStartTime());
        assertEquals(t2end, curRange.getEndTime().getValue());
    }

    /**
     * Test selecting a range whose both start and end times are outside of the
     * trace's range. The selected range should get clamped to the trace's
     * range.
     */
    @Test
    public void testTraceTimeRangeClampingBoth() {
        openTrace(trace2);
        TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t2start - ONE_SECOND, SCALE), // minus here
                new TmfTimestamp(t2end + ONE_SECOND, SCALE)); // plus here
        selectTimeRange(range);

        TmfTimeRange curRange = tm.getCurrentRange();
        assertEquals(t2start, curRange.getStartTime().getValue());
        assertEquals(t2end, curRange.getEndTime().getValue());
    }

    // ------------------------------------------------------------------------
    // Test multiple, non-overlapping traces in parallel
    // ------------------------------------------------------------------------

    /**
     * Test, with two traces in parallel, when we select a timestamp that is
     * part of the first trace.
     *
     * The first trace's timestamp should be updated, but the second trace's one
     * should not change.
     */
    @Test
    public void testTwoTracesTimestampValid() {
        openTrace(trace1);
        openTrace(trace2);
        selectTrace(trace1);
        TmfTimestamp ts = new TmfTimestamp(t1start + ONE_SECOND, SCALE);
        selectTimestamp(ts);

        /* Timestamp of trace1 should have been updated */
        assertEquals(ts, tm.getSelectionBeginTime());
        assertEquals(ts, tm.getSelectionEndTime());

        /* Timestamp of trace2 should not have changed */
        selectTrace(trace2);
        assertEquals(trace2.getStartTime(), tm.getSelectionBeginTime());
        assertEquals(trace2.getStartTime(), tm.getSelectionEndTime());
    }

    /**
     * Test, with two traces in parallel, when we select a timestamp that is
     * between two traces.
     *
     * None of the trace's timestamps should be updated (we are not in an
     * experiment!)
     */
    @Test
    public void testTwoTracesTimestampInBetween() {
        openTrace(trace1);
        openTrace(trace2);
        selectTrace(trace1);
        TmfTimestamp ts = new TmfTimestamp(t1end + ONE_SECOND, SCALE);
        selectTimestamp(ts);

        /* Timestamp of trace1 should not have changed */
        assertEquals(trace1.getStartTime(), tm.getSelectionBeginTime());
        assertEquals(trace1.getStartTime(), tm.getSelectionEndTime());

        /* Timestamp of trace2 should not have changed */
        selectTrace(trace2);
        assertEquals(trace2.getStartTime(), tm.getSelectionBeginTime());
        assertEquals(trace2.getStartTime(), tm.getSelectionEndTime());
    }

    /**
     * Test, with two traces in parallel, when we select a timestamp that is
     * completely out of the trace's range.
     *
     * None of the trace's timestamps should be updated.
     */
    @Test
    public void testTwoTracesTimestampInvalid() {
        openTrace(trace1);
        openTrace(trace2);
        selectTrace(trace1);
        TmfTimestamp ts = new TmfTimestamp(t2end + ONE_SECOND, SCALE);
        selectTimestamp(ts);

        /* Timestamp of trace1 should not have changed */
        assertEquals(trace1.getStartTime(), tm.getSelectionBeginTime());
        assertEquals(trace1.getStartTime(), tm.getSelectionEndTime());

        /* Timestamp of trace2 should not have changed */
        selectTrace(trace2);
        assertEquals(trace2.getStartTime(), tm.getSelectionBeginTime());
        assertEquals(trace2.getStartTime(), tm.getSelectionEndTime());
    }

    /**
     * Test, with two traces opened in parallel (not in an experiment), if we
     * select a time range valid in one of them. That trace's time range should
     * be updated, but not the other one.
     */
    @Test
    public void testTwoTracesTimeRangeAllInOne() {
        openTrace(trace1);
        openTrace(trace2);
        selectTrace(trace1);
        TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t1start + ONE_SECOND, SCALE),
                new TmfTimestamp(t1end - ONE_SECOND, SCALE));
        selectTimeRange(range);

        /* Range of trace1 should be equal to the requested one */
        assertEquals(range, tm.getCurrentRange());

        /* The range of trace 2 should not have changed */
        selectTrace(trace2);
        assertEquals(getInitialRange(trace2), tm.getCurrentRange());
    }

    /**
     * Test, with two traces in parallel, when we select a time range that is
     * only partially valid for one of the traces.
     *
     * The first trace's time range should be clamped to a valid range, and the
     * second one's should not change.
     */
    @Test
    public void testTwoTracesTimeRangePartiallyInOne() {
        openTrace(trace1);
        openTrace(trace2);
        selectTrace(trace1);
        TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t1start + ONE_SECOND, SCALE),
                new TmfTimestamp(t1end + ONE_SECOND, SCALE));
        selectTimeRange(range);

        /* Range of trace1 should get clamped to its end time */
        TmfTimeRange expectedRange = new TmfTimeRange(
                new TmfTimestamp(t1start + ONE_SECOND, SCALE),
                new TmfTimestamp(t1end, SCALE));
        assertEquals(expectedRange, tm.getCurrentRange());

        /* Range of trace2 should not have changed */
        selectTrace(trace2);
        assertEquals(getInitialRange(trace2), tm.getCurrentRange());
    }

    /**
     * Test, with two traces in parallel, when we select a time range that is
     * only partially valid for both traces.
     *
     * Each trace's time range should get clamped to respectively valid ranges.
     */
    @Test
    public void testTwoTracesTimeRangeInBoth() {
        openTrace(trace1);
        openTrace(trace2);
        selectTrace(trace1);
        TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t1end - ONE_SECOND, SCALE),
                new TmfTimestamp(t2start + ONE_SECOND, SCALE));
        selectTimeRange(range);

        /* Range of trace1 should be clamped to its end time */
        TmfTimeRange expectedRange = new TmfTimeRange(
                new TmfTimestamp(t1end - ONE_SECOND, SCALE),
                new TmfTimestamp(t1end, SCALE));
        assertEquals(expectedRange, tm.getCurrentRange());

        /* Range of trace2 should be clamped to its start time */
        selectTrace(trace2);
        expectedRange = new TmfTimeRange(
                new TmfTimestamp(t2start, SCALE),
                new TmfTimestamp(t2start + ONE_SECOND, SCALE));
        assertEquals(expectedRange, tm.getCurrentRange());
    }

    /**
     * Test, with two traces in parallel, when we select a time range that is
     * not valid for any trace.
     *
     * Each trace's time range should not be modified.
     */
    @Test
    public void testTwoTracesTimeRangeInBetween() {
        openTrace(trace1);
        openTrace(trace2);
        selectTrace(trace1);
        TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t1end + ONE_SECOND, SCALE),
                new TmfTimestamp(t1end - ONE_SECOND, SCALE));
        selectTimeRange(range);

        /* Range of trace1 should not have changed */
        TmfTimeRange expectedRange = getInitialRange(trace1);
        TmfTimeRange curRange = tm.getCurrentRange();
        assertEquals(expectedRange.getStartTime(), curRange.getStartTime());
        assertEquals(expectedRange.getEndTime(), curRange.getEndTime());

        /* Range of trace2 should not have changed */
        selectTrace(trace2);
        expectedRange = getInitialRange(trace2);
        curRange = tm.getCurrentRange();
        assertEquals(expectedRange.getStartTime(), curRange.getStartTime());
        assertEquals(expectedRange.getEndTime(), curRange.getEndTime());
    }

    // ------------------------------------------------------------------------
    // Test an experiment
    // ------------------------------------------------------------------------

    /**
     * Test in an experiment when we select a timestamp that is part of one of
     * the experiment's traces.
     *
     * The experiment's current time should be correctly updated.
     */
    @Test
    public void testExperimentTimestampInTrace() {
        TmfExperiment exp = createExperiment(trace1, trace2);
        openTrace(exp);
        TmfTimestamp ts = new TmfTimestamp(t1start + ONE_SECOND, SCALE);
        selectTimestamp(ts);

        /* The experiment's current time should be updated. */
        assertEquals(ts, tm.getSelectionBeginTime());
        assertEquals(ts, tm.getSelectionEndTime());
    }

    /**
     * Test in an experiment when we select a timestamp that is between two
     * traces in the experiment.
     *
     * The experiment's current time should still be updated, since the
     * timestamp is valid in the experiment itself.
     */
    @Test
    public void testExperimentTimestampInBetween() {
        TmfExperiment exp = createExperiment(trace1, trace2);
        openTrace(exp);
        TmfTimestamp ts = new TmfTimestamp(t1end + ONE_SECOND, SCALE);
        selectTimestamp(ts);

        /* The experiment's current time should be updated. */
        assertEquals(ts, tm.getSelectionBeginTime());
        assertEquals(ts, tm.getSelectionEndTime());
    }

    /**
     * Test in an experiment when we select a timestamp that is outside of the
     * total range of the experiment.
     *
     * The experiment's current time should not be updated.
     */
    @Test
    public void testExperimentTimestampInvalid() {
        TmfExperiment exp = createExperiment(trace1, trace2);
        openTrace(exp);
        TmfTimestamp ts = new TmfTimestamp(t2end + ONE_SECOND, SCALE);
        selectTimestamp(ts);

        /* The experiment's current time should NOT be updated. */
        assertEquals(trace1.getStartTime(), tm.getSelectionBeginTime());
        assertEquals(trace1.getStartTime(), tm.getSelectionEndTime());
    }

    /**
     * Test the initial range of an experiment.
     */
    @Test
    public void testExperimentInitialRange() {
        TmfExperiment exp = createExperiment(trace1, trace2);
        openTrace(exp);
        /*
         * The initial range should be == to the initial range of the earliest
         * trace (here trace1).
         */
        final TmfTimeRange actualRange = tm.getCurrentRange();

        assertEquals(getInitialRange(trace1), actualRange);
        assertEquals(getInitialRange(exp), actualRange);
    }

    /**
     * Test the range clamping with the start time of the range outside of the
     * earliest trace's range. Only that start time should get clamped.
     */
    @Test
    public void testExperimentRangeClampingOne() {
        TmfExperiment exp = createExperiment(trace1, trace2);
        openTrace(exp);

        final TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t1start - ONE_SECOND, SCALE),
                new TmfTimestamp(t1end - ONE_SECOND, SCALE));
        selectTimeRange(range);

        TmfTimeRange actualRange = tm.getCurrentRange();
        assertEquals(t1start, actualRange.getStartTime().getValue());
        assertEquals(t1end - ONE_SECOND, actualRange.getEndTime().getValue());
    }

    /**
     * Test the range clamping when both the start and end times of the signal's
     * range are outside of the trace's range. The range should clamp to the
     * experiment's range.
     */
    @Test
    public void testExperimentRangeClampingBoth() {
        TmfExperiment exp = createExperiment(trace1, trace2);
        openTrace(exp);

        final TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t1start - ONE_SECOND, SCALE),
                new TmfTimestamp(t2end + ONE_SECOND, SCALE));
        selectTimeRange(range);

        TmfTimeRange actualRange = tm.getCurrentRange();
        assertEquals(t1start, actualRange.getStartTime().getValue());
        assertEquals(t2end, actualRange.getEndTime().getValue());
    }

    /**
     * Test selecting a range in-between two disjoint traces in an experiment.
     * The range should still get correctly selected, even if no trace has any
     * events in that range.
     */
    @Test
    public void testExperimentRangeInBetween() {
        TmfExperiment exp = createExperiment(trace1, trace2);
        openTrace(exp);

        final TmfTimeRange range = new TmfTimeRange(
                new TmfTimestamp(t1end + ONE_SECOND, SCALE),
                new TmfTimestamp(t2start - ONE_SECOND, SCALE));
        selectTimeRange(range);

        TmfTimeRange actualRange = tm.getCurrentRange();
        assertEquals(range, actualRange);
    }

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    private static TmfExperiment createExperiment(ITmfTrace t1, ITmfTrace t2) {
        ITmfTrace[] traces = new ITmfTrace[] { t1, t2 };
        TmfExperiment exp = new TmfExperiment(ITmfEvent.class, "test-exp", traces);
        exp.indexTrace(true);
        return exp;
    }

    private static TmfTimeRange getInitialRange(ITmfTrace trace) {
        return new TmfTimeRange(
                trace.getStartTime(),
                calculateOffset(trace.getStartTime(), trace.getInitialRangeOffset()));
    }

    /**
     * Basically a "initial + offset" operation, but for ITmfTimetamp objects.
     */
    private static ITmfTimestamp calculateOffset(ITmfTimestamp initialTs, ITmfTimestamp offsetTs) {
        long start = initialTs.normalize(0, SCALE).getValue();
        long offset = offsetTs.normalize(0, SCALE).getValue();
        return new TmfTimestamp(start + offset, SCALE);
    }
}
