/*******************************************************************************
 * Copyright (c) 2016 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.trace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertNotEquals;

import java.util.function.Predicate;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.core.trace.ITmfTrace;
import org.eclipse.tracecompass.tmf.core.trace.TmfTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Tests for the event-searching methods in {@link TmfTraceUtils}.
 *
 * @author Alexandre Montplaisir
 */
public class TmfTraceUtilsSearchingTest {

    private static final @NonNull CtfTestTrace TEST_TRACE = CtfTestTrace.TRACE2;
    private static final long START_RANK = 500L;

    private static ITmfTrace fTrace;
    private static ITmfEvent fStartEvent;

    /**
     * Test setup
     */
    @BeforeClass
    public static void setUp() {
        fTrace = CtfTmfTestTraceUtils.getTrace(TEST_TRACE);

        ITmfContext ctx = fTrace.seekEvent(START_RANK);
        fStartEvent = fTrace.getNext(ctx);
        assertEquals("softirq_raise", fStartEvent.getName());
    }

    /**
     * Test cleanup
     */
    @AfterClass
    public static void tearDown() {
        if (fTrace != null) {
            fTrace.dispose();
        }
    }

    // ------------------------------------------------------------------------
    // Forwards searches
    // ------------------------------------------------------------------------

    /**
     * Test the {@link TmfTraceUtils#getNextEventMatching} method.
     */
    @Test
    public void testNextMatchingEvent() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("sched_switch");

        ITmfEvent actualEvent = TmfTraceUtils.getNextEventMatching(trace, START_RANK, predicate, null);

        ITmfContext ctx = trace.seekEvent(508L); // following sched_switch event
        ITmfEvent expectedEvent = trace.getNext(ctx);

        assertEquals(expectedEvent, actualEvent);
    }

    /**
     * Test that the presence of progress monitor does not affect the results.
     */
    @Test
    public void testNextMatchingEventProgressMonitor() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("sched_switch");

        IProgressMonitor monitor = new NullProgressMonitor();
        ITmfEvent actualEvent = TmfTraceUtils.getNextEventMatching(trace, START_RANK, predicate, monitor);

        ITmfContext ctx = trace.seekEvent(508L); // following sched_switch event
        ITmfEvent expectedEvent = trace.getNext(ctx);

        assertEquals(expectedEvent, actualEvent);
    }

    /**
     * Test the {@link TmfTraceUtils#getNextEventMatching} method where no event
     * matches the passed predicate. It should return null.
     */
    @Test
    public void testNextMatchingEventNoMatch() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("non-existent-event");

        ITmfEvent actualEvent = TmfTraceUtils.getNextEventMatching(trace, START_RANK, predicate, null);
        assertNull(actualEvent);
    }

    /**
     * Test the {@link TmfTraceUtils#getNextEventMatching} method, where the
     * event from which we start the search already matches the criterion (it
     * should be correctly skipped so we can advance).
     */
    @Test
    public void testNextMatchingEventStartMatches() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("softirq_raise");
        ITmfEvent foundEvent = TmfTraceUtils.getNextEventMatching(trace, START_RANK, predicate, null);

        assertNotNull(foundEvent);
        assertNotEquals(fStartEvent, foundEvent);
    }

    /**
     * Test with a progress monitor that cancels the job.
     */
    @Test
    public void testNextMatchingEventCancelled() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("sched_switch");

        IProgressMonitor monitor = new NullProgressMonitor();
        monitor.setCanceled(true);
        ITmfEvent actualEvent = TmfTraceUtils.getNextEventMatching(trace, START_RANK, predicate, monitor);

        /* No event should be returend */
        assertNull(actualEvent);
    }

    // ------------------------------------------------------------------------
    // Backwards searches
    // ------------------------------------------------------------------------

    /**
     * Test the {@link TmfTraceUtils#getPreviousEventMatching} method.
     */
    @Test
    public void testPreviousMatchingEvent() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("sched_switch");

        ITmfEvent actualEvent = TmfTraceUtils.getPreviousEventMatching(trace, START_RANK, predicate, null);

        ITmfContext ctx = trace.seekEvent(455L); // previous sched_switch event
        ITmfEvent expectedEvent = trace.getNext(ctx);

        assertEquals(expectedEvent, actualEvent);
    }

    /**
     * Test that the presence of progress monitor does not affect the results.
     */
    @Test
    public void testPreviousMatchingEventProgressMonitor() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("sched_switch");

        IProgressMonitor monitor = new NullProgressMonitor();
        ITmfEvent actualEvent = TmfTraceUtils.getPreviousEventMatching(trace, START_RANK, predicate, monitor);

        ITmfContext ctx = trace.seekEvent(455L); // previous sched_switch event
        ITmfEvent expectedEvent = trace.getNext(ctx);

        assertEquals(expectedEvent, actualEvent);
    }

    /**
     * Test the {@link TmfTraceUtils#getPreviousEventMatching} method where no event
     * matches the passed predicate. It should return null.
     */
    @Test
    public void testPreviousMatchingEventNoMatch() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("non-existent-event");

        ITmfEvent actualEvent = TmfTraceUtils.getPreviousEventMatching(trace, START_RANK, predicate, null);
        assertNull(actualEvent);
    }

    /**
     * Test the {@link TmfTraceUtils#getPreviousEventMatching} method, where the
     * event from which we start the search already matches the criterion (it
     * should be correctly skipped).
     */
    @Test
    public void testPreviousMatchingEventStartMatches() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("softirq_raise");
        ITmfEvent foundEvent = TmfTraceUtils.getPreviousEventMatching(trace, START_RANK, predicate, null);

        assertNotNull(foundEvent);
        assertNotEquals(fStartEvent, foundEvent);
    }

    /**
     * Test the {@link TmfTraceUtils#getPreviousEventMatching} method with an
     * event that is expected to take more than one inner request.
     */
    @Test
    public void testPreviousMatchingEventFar() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("sys_write");
        ITmfEvent actualEvent = TmfTraceUtils.getPreviousEventMatching(trace, START_RANK, predicate, null);

        ITmfContext ctx = trace.seekEvent(387L); // previous sched_switch event
        ITmfEvent expectedEvent = trace.getNext(ctx);

        assertEquals(expectedEvent, actualEvent);
    }

    /**
     * When doing a backwards search near the beginning of the trace (when
     * startRank < step), make sure that we do not go beyond the start rank.
     */
    @Test
    public void testPreviousMatchingBeginningOfTrace() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        final int startRank = 3;
        ITmfContext ctx = fTrace.seekEvent(startRank);
        ITmfEvent startEvent = fTrace.getNext(ctx);
        assertEquals("exit_syscall", startEvent.getName());

        /* There is a sys_mmap at rank 6, it should not be matched! */
        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("sys_mmap");
        ITmfEvent foundEvent = TmfTraceUtils.getPreviousEventMatching(trace, startRank, predicate, null);
        assertNull(foundEvent);

        /* Do not match the event itself either, or any later "exit_syscall" */
        predicate = event -> event.getName().equals("exit_syscall");
        foundEvent = TmfTraceUtils.getPreviousEventMatching(trace, startRank, predicate, null);
        assertNull(foundEvent);
    }

    /**
     * Test with a progress monitor that cancels the job.
     */
    @Test
    public void testPreviousMatchingEventCancelled() {
        ITmfTrace trace = fTrace;
        assertNotNull(trace);

        Predicate<@NonNull ITmfEvent> predicate = event -> event.getName().equals("sched_switch");

        IProgressMonitor monitor = new NullProgressMonitor();
        monitor.setCanceled(true);
        ITmfEvent actualEvent = TmfTraceUtils.getPreviousEventMatching(trace, START_RANK, predicate, monitor);

        /* No event should be returend */
        assertNull(actualEvent);
    }
}
