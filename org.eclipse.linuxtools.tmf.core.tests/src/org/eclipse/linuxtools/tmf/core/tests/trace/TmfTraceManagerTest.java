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
 *******************************************************************************/

package org.eclipse.linuxtools.tmf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import org.eclipse.linuxtools.tmf.core.event.ITmfEvent;
import org.eclipse.linuxtools.tmf.core.signal.TmfRangeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfSignalManager;
import org.eclipse.linuxtools.tmf.core.signal.TmfTimeSynchSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.linuxtools.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.linuxtools.tmf.core.tests.shared.CtfTmfTestTraces;
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
        assumeTrue(CtfTmfTestTraces.tracesExist());
        trace1 = CtfTmfTestTraces.getTestTrace(1);
        trace2 = CtfTmfTestTraces.getTestTrace(0);

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
    }

    private void selectTrace(ITmfTrace trace) {
        TmfSignalManager.dispatchSignal(new TmfTraceSelectedSignal(this, trace));
    }

    private void selectTimestamp(ITmfTimestamp ts) {
        TmfSignalManager.dispatchSignal(new TmfTimeSynchSignal(this, ts));
    }

    private void selectTimeRange(TmfTimeRange tr) {
        TmfSignalManager.dispatchSignal(new TmfRangeSynchSignal(this, tr, null));
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

        ITmfTimestamp afterTs = tm.getCurrentTime();
        assertEquals(ts, afterTs);
    }

    /**
     * Try selecting a timestamp happening before the trace's start. The change
     * should be ignored.
     */
    @Test
    public void testTimestampBefore() {
        openTrace(trace2);
        ITmfTimestamp beforeTs = tm.getCurrentTime();
        ITmfTimestamp ts = new TmfTimestamp(t2start - ONE_SECOND, SCALE);
        selectTimestamp(ts);

        ITmfTimestamp curTs = tm.getCurrentTime();
        assertEquals(beforeTs, curTs);
    }

    /**
     * Try selecting a timestamp happening after the trace's end. The change
     * should be ignored.
     */
    @Test
    public void testTimestampAfter() {
        openTrace(trace2);
        ITmfTimestamp beforeTs = tm.getCurrentTime();
        ITmfTimestamp ts = new TmfTimestamp(t2end + ONE_SECOND, SCALE);
        selectTimestamp(ts);

        ITmfTimestamp curTs = tm.getCurrentTime();
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
    // Test multiple traces in parallel
    // ------------------------------------------------------------------------

    // ------------------------------------------------------------------------
    // Test an experiment
    // ------------------------------------------------------------------------

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
        final TmfTimeRange traceInitialRange = new TmfTimeRange(
                trace1.getStartTime(),
                calculateOffset(trace1.getStartTime(), trace1.getInitialRangeOffset()));

        final TmfTimeRange expInitialRange = new TmfTimeRange(
                exp.getStartTime(),
                calculateOffset(exp.getStartTime(), exp.getInitialRangeOffset()));

        final TmfTimeRange actualRange = tm.getCurrentRange();

        assertEquals(traceInitialRange, actualRange);
        assertEquals(expInitialRange, actualRange);
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

    /**
     * Basically a "initial + offset" operation, but for ITmfTimetamp objects.
     */
    private static ITmfTimestamp calculateOffset(ITmfTimestamp initialTs, ITmfTimestamp offsetTs) {
        long start = initialTs.normalize(0, SCALE).getValue();
        long offset = offsetTs.normalize(0, SCALE).getValue();
        return new TmfTimestamp(start + offset, SCALE);
    }
}
