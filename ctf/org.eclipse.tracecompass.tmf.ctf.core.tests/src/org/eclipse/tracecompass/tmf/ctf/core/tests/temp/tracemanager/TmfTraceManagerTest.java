/*******************************************************************************
 * Copyright (c) 2013, 2019 Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License 2.0 which
 * accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Alexandre Montplaisir - Initial API and implementation
 *   Patrick Tasse - Support selection range
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.temp.tracemanager;

import static org.eclipse.tracecompass.common.core.NonNullUtils.checkNotNull;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.common.core.math.SaturatedArithmetic;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.project.model.TraceTypePreferences;
import org.eclipse.tracecompass.tmf.core.signal.TmfSelectionRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceClosedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceSelectedSignal;
import org.eclipse.tracecompass.tmf.core.signal.TmfWindowRangeUpdatedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceContext;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceManager;
import org.eclipse.tracecompass.tmf.core.trace.experiment.TmfExperiment;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;

import com.google.common.collect.ImmutableSet;

/**
 * Test suite for the {@link TmfTraceManager}.
 *
 * @author Alexandre Montplaisir
 */
public class TmfTraceManagerTest {

    /** Time-out tests after 1 minute. */
    @Rule
    public TestRule globalTimeout = new Timeout(2, TimeUnit.MINUTES);

    private static ITmfTrace trace1;
    private static final long t1start = 1331668247314038062L;
    private static final long t1end = 1331668259054285979L;

    private static ITmfTrace trace2;
    private static final long t2start = 1332170682440133097L;
    private static final long t2end = 1332170692664579801L;

    private static ITmfTrace trace3;

    private static final long ONE_SECOND = 1000000000L;

    private TmfTraceManager tm;

    /**
     * Test class initialization
     */
    @BeforeClass
    public static void setUpClass() {
        trace1 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.TRACE2);
        trace2 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.KERNEL);
        trace3 = CtfTmfTestTraceUtils.getTrace(CtfTestTrace.KERNEL_VM);

        trace1.indexTrace(true);
        trace2.indexTrace(true);
        trace3.indexTrace(true);

        // Deregister traces from signal manager so that they don't
        // interfere with the TmfTraceManager tests
        TmfSignalManager.deregister(trace1);
        TmfSignalManager.deregister(trace2);
        TmfSignalManager.deregister(trace3);
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

    /**
     * Test class clean-up
     */
    @AfterClass
    public static void tearDownClass() {
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.TRACE2);
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.KERNEL);
        CtfTmfTestTraceUtils.dispose(CtfTestTrace.KERNEL_VM);
    }

    // ------------------------------------------------------------------------
    // Dummy actions (fake signals)
    // ------------------------------------------------------------------------

    private void openTrace(ITmfTrace trace) {
        if (trace == null) {
            throw new IllegalArgumentException();
        }
        TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(this, trace, null));
        selectTrace(trace);
    }

    private void closeTrace(ITmfTrace trace) {
        if (trace == null) {
            throw new IllegalArgumentException();
        }
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

    private void selectTimestamp(@NonNull ITmfTimestamp ts) {
        TmfSignalManager.dispatchSignal(new TmfSelectionRangeUpdatedSignal(this, ts));
    }

    private void selectWindowRange(TmfTimeRange tr) {
        TmfSignalManager.dispatchSignal(new TmfWindowRangeUpdatedSignal(this, tr));
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

        Collection<ITmfTrace> expected = Collections.singleton(trace2);
        Collection<ITmfTrace> actual = tm.getActiveTraceSet();

        assertEquals(1, actual.size());
        assertEquals(expected, actual);
    }

    /**
     * Test getting the traces for a host
     */
    @Test
    public void testTraceSetForHost() {
        openTrace(trace1);
        openTrace(trace2);
        selectTrace(trace2);

        // Test the currently selected trace
        Collection<ITmfTrace> expected = Collections.singleton(trace2);
        Collection<ITmfTrace> actual = tm.getTracesForHost(trace2.getHostId());

        assertEquals(1, actual.size());
        assertEquals(expected, actual);

        // Test an opened trace, even though it is not selected
        expected = Collections.singleton(trace1);
        actual = tm.getTracesForHost(trace1.getHostId());

        assertEquals(1, actual.size());
        assertEquals(expected, actual);

        // Test a closed trace, should not return anything
        actual = tm.getTracesForHost(trace3.getHostId());

        assertTrue(actual.isEmpty());
    }

    /**
     * Test the contents of a trace set with an experiment.
     */
    @Test
    public void testTraceSetExperiment() {
        final ITmfTrace localTrace1 = trace1;
        final ITmfTrace localTrace2 = trace2;
        assertNotNull(localTrace1);
        assertNotNull(localTrace2);
        TmfExperiment exp = createExperiment(localTrace1, localTrace2);
        openTrace(localTrace1);
        openTrace(exp);

        Collection<ITmfTrace> expected = ImmutableSet.of(localTrace1, localTrace2);
        Collection<ITmfTrace> actual = tm.getActiveTraceSet();

        assertEquals(2, actual.size());
        assertEquals(expected, actual);
    }

    /**
     * Test the contents of a trace set for a host that is part of an experiment
     */
    @Test
    public void testTraceSetExperimentForHost() {
        final ITmfTrace localTrace1 = trace1;
        final ITmfTrace localTrace2 = trace2;
        assertNotNull(localTrace1);
        assertNotNull(localTrace2);
        TmfExperiment exp = createExperiment(localTrace1, localTrace2);
        openTrace(exp);
        selectTrace(exp);

        Collection<ITmfTrace> expected = Collections.singleton(trace2);
        Collection<ITmfTrace> actual = tm.getTracesForHost(trace2.getHostId());

        assertEquals(1, actual.size());
        assertEquals(expected, actual);

    }

    /**
     * Test the contents of a trace set with a nested experiment.
     */
    @Test
    public void testTraceSetNestedExperiment() {
        final ITmfTrace localTrace1 = trace1;
        final ITmfTrace localTrace2 = trace2;
        final ITmfTrace localTrace3 = trace3;
        assertNotNull(localTrace1);
        assertNotNull(localTrace2);
        assertNotNull(localTrace3);
        TmfExperiment nestedExp = createExperiment(localTrace2, localTrace3);
        TmfExperiment exp = createExperiment(localTrace1, nestedExp);

        Collection<ITmfTrace> expected = ImmutableSet.of(localTrace1, localTrace2, localTrace3);
        Collection<ITmfTrace> actual = TmfTraceManager.getTraceSet(exp);

        assertEquals(expected, actual);
    }

    /**
     * Test the contents of the complete trace set.
     */
    @Test
    public void testTraceSetWithExperiment() {
        final ITmfTrace localTrace1 = trace1;
        final ITmfTrace localTrace2 = trace2;
        assertNotNull(localTrace1);
        assertNotNull(localTrace2);
        /* Test with a trace */
        Collection<ITmfTrace> expected = Collections.singleton(localTrace1);
        Collection<ITmfTrace> actual = TmfTraceManager.getTraceSetWithExperiment(localTrace1);
        assertEquals(1, actual.size());
        assertEquals(expected, actual);

        /* Test with an experiment */
        TmfExperiment exp = createExperiment(localTrace1, localTrace2);
        assertNotNull(exp);
        expected = ImmutableSet.of(localTrace1, localTrace2, exp);
        actual = TmfTraceManager.getTraceSetWithExperiment(exp);
        assertEquals(3, actual.size());
        assertEquals(expected, actual);
    }

    /**
     * Test the contents of the complete trace set with a nested experiment.
     */
    @Test
    public void testTraceSetWithNestedExperiment() {
        final ITmfTrace localTrace1 = trace1;
        final ITmfTrace localTrace2 = trace2;
        final ITmfTrace localTrace3 = trace3;
        assertNotNull(localTrace1);
        assertNotNull(localTrace2);
        assertNotNull(localTrace3);
        TmfExperiment nestedExp = createExperiment(localTrace2, localTrace3);
        TmfExperiment exp = createExperiment(localTrace1, nestedExp);

        Collection<ITmfTrace> expected = ImmutableSet.of(exp, localTrace1, nestedExp, localTrace2, localTrace3);
        Collection<ITmfTrace> actual = TmfTraceManager.getTraceSetWithExperiment(exp);

        assertEquals(expected, actual);
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
        TmfTimeRange actualRange = tm.getCurrentTraceContext().getWindowRange();
        assertEquals(expectedRange, actualRange);
    }

    /**
     * Try selecting a timestamp contained inside the trace's range. The trace's
     * current time should get updated correctly.
     */
    @Test
    public void testNewTimestamp() {
        openTrace(trace2);
        ITmfTimestamp ts = TmfTimestamp.fromNanos(t2start + ONE_SECOND);
        selectTimestamp(ts);

        TmfTimeRange selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(ts, selection.getStartTime());
        assertEquals(ts, selection.getEndTime());
    }

    /**
     * Try selecting a timestamp happening before the trace's start. The change
     * should be ignored.
     */
    @Test
    public void testTimestampBefore() {
        openTrace(trace2);
        TmfTimeRange beforeTr = tm.getCurrentTraceContext().getSelectionRange();
        ITmfTimestamp ts = TmfTimestamp.fromNanos(t2start - ONE_SECOND);
        selectTimestamp(ts);

        TmfTimeRange selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(beforeTr, selection);
    }

    /**
     * Try selecting a timestamp happening after the trace's end. The change
     * should be ignored.
     */
    @Test
    public void testTimestampAfter() {
        openTrace(trace2);
        TmfTimeRange beforeTr = tm.getCurrentTraceContext().getSelectionRange();
        ITmfTimestamp ts = TmfTimestamp.fromNanos(t2end + ONE_SECOND);
        selectTimestamp(ts);

        TmfTimeRange selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(beforeTr, selection);
    }

    /**
     * Test selecting a normal sub-range of a single trace.
     */
    @Test
    public void testTraceNewTimeRange() {
        openTrace(trace2);
        TmfTimeRange range = new TmfTimeRange(
                TmfTimestamp.fromNanos(t2start + ONE_SECOND),
                TmfTimestamp.fromNanos(t2end - ONE_SECOND));
        selectWindowRange(range);

        TmfTimeRange curRange = tm.getCurrentTraceContext().getWindowRange();
        assertEquals(range, curRange);
    }

    /**
     * Test selecting a range whose start time is before the trace's start time.
     * The selected range should get clamped to the trace's range.
     */
    @Test
    public void testTraceTimeRangeClampingStart() {
        openTrace(trace2);
        TmfTimeRange range = new TmfTimeRange(
                TmfTimestamp.fromNanos(t2start - ONE_SECOND), // minus here
                TmfTimestamp.fromNanos(t2end - ONE_SECOND));
        selectWindowRange(range);

        TmfTimeRange curRange = tm.getCurrentTraceContext().getWindowRange();
        assertEquals(t2start, curRange.getStartTime().getValue());
        assertEquals(range.getEndTime(), curRange.getEndTime());
    }

    /**
     * Test selecting a range whose end time is after the trace's end time. The
     * selected range should get clamped to the trace's range.
     */
    @Test
    public void testTraceTimeRangeClampingEnd() {
        openTrace(trace2);
        TmfTimeRange range = new TmfTimeRange(
                TmfTimestamp.fromNanos(t2start + ONE_SECOND),
                TmfTimestamp.fromNanos(t2end + ONE_SECOND)); // plus here
        selectWindowRange(range);

        TmfTimeRange curRange = tm.getCurrentTraceContext().getWindowRange();
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
                TmfTimestamp.fromNanos(t2start - ONE_SECOND), // minus here
                TmfTimestamp.fromNanos(t2end + ONE_SECOND)); // plus here
        selectWindowRange(range);

        TmfTimeRange curRange = tm.getCurrentTraceContext().getWindowRange();
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
        ITmfTimestamp ts = TmfTimestamp.fromNanos(t1start + ONE_SECOND);
        selectTimestamp(ts);

        /* Timestamp of trace1 should have been updated */
        TmfTraceContext ctx = tm.getCurrentTraceContext();
        assertEquals(ts, ctx.getSelectionRange().getStartTime());
        assertEquals(ts, ctx.getSelectionRange().getEndTime());

        /* Timestamp of trace2 should not have changed */
        selectTrace(trace2);
        ctx = tm.getCurrentTraceContext();
        assertEquals(trace2.getStartTime(), ctx.getSelectionRange().getStartTime());
        assertEquals(trace2.getStartTime(), ctx.getSelectionRange().getEndTime());
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
        ITmfTimestamp ts = TmfTimestamp.fromNanos(t1end + ONE_SECOND);
        selectTimestamp(ts);

        /* Timestamp of trace1 should not have changed */
        TmfTimeRange selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(trace1.getStartTime(), selection.getStartTime());
        assertEquals(trace1.getStartTime(), selection.getEndTime());

        /* Timestamp of trace2 should not have changed */
        selectTrace(trace2);
        selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(trace2.getStartTime(), selection.getStartTime());
        assertEquals(trace2.getStartTime(), selection.getEndTime());
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
        ITmfTimestamp ts = TmfTimestamp.fromNanos(t2end + ONE_SECOND);
        selectTimestamp(ts);

        /* Timestamp of trace1 should not have changed */
        TmfTimeRange selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(trace1.getStartTime(), selection.getStartTime());
        assertEquals(trace1.getStartTime(), selection.getEndTime());

        /* Timestamp of trace2 should not have changed */
        selectTrace(trace2);
        selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(trace2.getStartTime(), selection.getStartTime());
        assertEquals(trace2.getStartTime(), selection.getEndTime());
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
                TmfTimestamp.fromNanos(t1start + ONE_SECOND),
                TmfTimestamp.fromNanos(t1end - ONE_SECOND));
        selectWindowRange(range);

        /* Range of trace1 should be equal to the requested one */
        assertEquals(range, tm.getCurrentTraceContext().getWindowRange());

        /* The range of trace 2 should not have changed */
        selectTrace(trace2);
        assertEquals(getInitialRange(trace2), tm.getCurrentTraceContext().getWindowRange());
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
                TmfTimestamp.fromNanos(t1start + ONE_SECOND),
                TmfTimestamp.fromNanos(t1end + ONE_SECOND));
        selectWindowRange(range);

        /* Range of trace1 should get clamped to its end time */
        TmfTimeRange expectedRange = new TmfTimeRange(
                TmfTimestamp.fromNanos(t1start + ONE_SECOND),
                TmfTimestamp.fromNanos(t1end));
        assertEquals(expectedRange, tm.getCurrentTraceContext().getWindowRange());

        /* Range of trace2 should not have changed */
        selectTrace(trace2);
        assertEquals(getInitialRange(trace2), tm.getCurrentTraceContext().getWindowRange());
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
        /* Enable time synchronization for trace2 */
        TmfTraceManager.getInstance().updateTraceContext(checkNotNull(trace2), builder -> builder.setSynchronized(true));
        selectTrace(trace1);
        TmfTimeRange range = new TmfTimeRange(
                TmfTimestamp.fromNanos(t1end - ONE_SECOND),
                TmfTimestamp.fromNanos(t2start + ONE_SECOND));
        selectWindowRange(range);

        /* Range of trace1 should be clamped to its end time */
        TmfTimeRange expectedRange = new TmfTimeRange(
                TmfTimestamp.fromNanos(t1end - ONE_SECOND),
                TmfTimestamp.fromNanos(t1end));
        assertEquals(expectedRange, tm.getCurrentTraceContext().getWindowRange());

        /* Range of trace2 should be clamped to its start time */
        selectTrace(trace2);
        expectedRange = new TmfTimeRange(
                TmfTimestamp.fromNanos(t2start),
                TmfTimestamp.fromNanos(t2start + ONE_SECOND));
        assertEquals(expectedRange, tm.getCurrentTraceContext().getWindowRange());
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
                TmfTimestamp.fromNanos(t1end + ONE_SECOND),
                TmfTimestamp.fromNanos(t1end - ONE_SECOND));
        selectWindowRange(range);

        /* Range of trace1 should not have changed */
        TmfTimeRange expectedRange = getInitialRange(trace1);
        TmfTimeRange curRange = tm.getCurrentTraceContext().getWindowRange();
        assertEquals(expectedRange.getStartTime(), curRange.getStartTime());
        assertEquals(expectedRange.getEndTime(), curRange.getEndTime());

        /* Range of trace2 should not have changed */
        selectTrace(trace2);
        expectedRange = getInitialRange(trace2);
        curRange = tm.getCurrentTraceContext().getWindowRange();
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
        ITmfTimestamp ts = TmfTimestamp.fromNanos(t1start + ONE_SECOND);
        selectTimestamp(ts);

        /* The experiment's current time should be updated. */
        TmfTimeRange selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(ts, selection.getStartTime());
        assertEquals(ts, selection.getEndTime());
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
        ITmfTimestamp ts = TmfTimestamp.fromNanos(t1end + ONE_SECOND);
        selectTimestamp(ts);

        /* The experiment's current time should be updated. */
        TmfTimeRange selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(ts, selection.getStartTime());
        assertEquals(ts, selection.getEndTime());
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
        ITmfTimestamp ts = TmfTimestamp.fromNanos(t2end + ONE_SECOND);
        selectTimestamp(ts);

        /* The experiment's current time should NOT be updated. */
        TmfTimeRange selection = tm.getCurrentTraceContext().getSelectionRange();
        assertEquals(trace1.getStartTime(), selection.getStartTime());
        assertEquals(trace1.getStartTime(), selection.getEndTime());
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
        final TmfTimeRange actualRange = tm.getCurrentTraceContext().getWindowRange();

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
                TmfTimestamp.fromNanos(t1start - ONE_SECOND),
                TmfTimestamp.fromNanos(t1end - ONE_SECOND));
        selectWindowRange(range);

        TmfTimeRange actualRange = tm.getCurrentTraceContext().getWindowRange();
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
                TmfTimestamp.fromNanos(t1start - ONE_SECOND),
                TmfTimestamp.fromNanos(t2end + ONE_SECOND));
        selectWindowRange(range);

        TmfTimeRange actualRange = tm.getCurrentTraceContext().getWindowRange();
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
                TmfTimestamp.fromNanos(t1end + ONE_SECOND),
                TmfTimestamp.fromNanos(t2start - ONE_SECOND));
        selectWindowRange(range);

        TmfTimeRange actualRange = tm.getCurrentTraceContext().getWindowRange();
        assertEquals(range, actualRange);
    }

    // ------------------------------------------------------------------------
    // Utility methods
    // ------------------------------------------------------------------------

    private static @NonNull TmfExperiment createExperiment(ITmfTrace t1, ITmfTrace t2) {
        ITmfTrace[] traces = new ITmfTrace[] { t1, t2 };
        TmfExperiment exp = new TmfExperiment(ITmfEvent.class, "test-exp", traces,
                TmfExperiment.DEFAULT_INDEX_PAGE_SIZE, null);
        exp.indexTrace(true);
        // Deregister experiment from signal manager so that it doesn't
        // interfere with the TmfTraceManager tests
        TmfSignalManager.deregister(exp);
        return exp;
    }

    private static TmfTimeRange getInitialRange(ITmfTrace trace) {
        long initialTimeRange = TraceTypePreferences.getInitialTimeRange(trace.getTraceTypeId(), trace.getInitialRangeOffset().toNanos());
        return new TmfTimeRange(
                trace.getStartTime(),
                calculateOffset(trace.getStartTime(), TmfTimestamp.fromNanos(initialTimeRange)));
    }

    /**
     * Basically a "initial + offset" operation, but for ITmfTimetamp objects.
     */
    private static @NonNull ITmfTimestamp calculateOffset(ITmfTimestamp initialTs, ITmfTimestamp offsetTs) {
        long start = initialTs.toNanos();
        long offset = offsetTs.toNanos();
        return TmfTimestamp.fromNanos(SaturatedArithmetic.add(start, offset));
    }
}
