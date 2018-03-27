/*******************************************************************************
 * Copyright (c) 2016, 2018 EfficiOS Inc., Alexandre Montplaisir
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.tracecompass.tmf.ctf.core.tests.trim;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.tracecompass.testtraces.ctf.CtfTestTrace;
import org.eclipse.tracecompass.tmf.core.event.TmfEvent;
import org.eclipse.tracecompass.tmf.core.exceptions.TmfTraceException;
import org.eclipse.tracecompass.tmf.core.signal.TmfSignalManager;
import org.eclipse.tracecompass.tmf.core.signal.TmfTraceOpenedSignal;
import org.eclipse.tracecompass.tmf.core.timestamp.ITmfTimestamp;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimeRange;
import org.eclipse.tracecompass.tmf.core.timestamp.TmfTimestamp;
import org.eclipse.tracecompass.tmf.core.trace.ITmfContext;
import org.eclipse.tracecompass.tmf.ctf.core.event.CtfTmfEvent;
import org.eclipse.tracecompass.tmf.ctf.core.tests.shared.CtfTmfTestTraceUtils;
import org.eclipse.tracecompass.tmf.ctf.core.trace.CtfTmfTrace;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.junit.rules.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

/**
 * Tests related to the trimming feature of CTF traces
 * ({@link CtfTmfTrace#trim}).
 *
 * @author Alexandre Montplaisir
 */
@RunWith(Parameterized.class)
public class CtfTmfTraceTrimmingTest {

    /** Test timeout */
    @Rule
    public TestRule globalTimeout = new Timeout(5, TimeUnit.MINUTES);

    private static final Collection<CtfTestTrace> BLACKLISTED_TRACES = Arrays.asList(
            /*
             * Unstable, TODO: investigate.
             */
            CtfTestTrace.DYNSCOPE,
            /*
             * Ignore hello-lost, most of the trace range is lost events, so cutting would
             * give an empty trace.
             */
            CtfTestTrace.HELLO_LOST,

            /* Trimming doesn't work on experiments at the moment */
            CtfTestTrace.TRACE_EXPERIMENT);

    private final @NonNull CtfTestTrace fTestTrace;

    private CtfTmfTrace fOriginalTrace;
    private ITmfTimestamp fRequestedTraceCutStart;
    private ITmfTimestamp fRequestedTraceCutEnd;

    private CtfTmfTrace fNewTrace;
    private Path fNewTracePath;

    // ------------------------------------------------------------------------
    // Test suite definition
    // ------------------------------------------------------------------------

    /**
     * Test parameter generator
     *
     * @return The list of constructor parameters, one for each test instance.
     */
    @Parameters(name = "{index}: {0}")
    public static Iterable<Object[]> getTestTraces() {
        CtfTestTrace[] testTraces = CtfTestTrace.values();
        return Arrays.stream(testTraces)
                .filter(testTrace -> !BLACKLISTED_TRACES.contains(testTrace))
                .map(testTrace -> new Object[] { testTrace })
                .collect(Collectors.toList());
    }

    /**
     * Constructor. Receives parameters defined in {@link #getTestTraces()}.
     *
     * @param testTrace
     *            The test trace to use for this test instance.
     */
    public CtfTmfTraceTrimmingTest(@NonNull CtfTestTrace testTrace) {
        fTestTrace = testTrace;
    }

    // ------------------------------------------------------------------------
    // Test instance maintenance
    // ------------------------------------------------------------------------

    /**
     * Test setup
     *
     * @throws IOException
     *             failed to load the file
     * @throws TmfTraceException
     *             failed to load the trace
     */
    @Before
    public void setup() throws IOException, TmfTraceException {
        fOriginalTrace = CtfTmfTestTraceUtils.getTrace(fTestTrace);
        openTrace(fOriginalTrace);

        fRequestedTraceCutStart = getTraceCutStart(fOriginalTrace);
        fRequestedTraceCutEnd = getTraceCutEnd(fOriginalTrace);

        ITmfTimestamp requestedTraceCutEnd = fRequestedTraceCutEnd;
        ITmfTimestamp requestedTraceCutStart = fRequestedTraceCutStart;
        assertTrue(requestedTraceCutStart.compareTo(fOriginalTrace.getStartTime()) > 0);
        assertTrue(requestedTraceCutEnd.compareTo(fOriginalTrace.getEndTime()) < 0);
        assertTrue(fRequestedTraceCutStart.compareTo(fRequestedTraceCutEnd) < 0);

        TmfTimeRange range = new TmfTimeRange(
                requestedTraceCutStart,
                requestedTraceCutEnd);
        try {
            /* Perform the trim to create the new trace */
            Path newTracePath = Files.createTempDirectory("trimmed-trace-test");
            fNewTracePath = newTracePath;
            assertNotNull(newTracePath);
            fNewTracePath.toFile().delete();
            fOriginalTrace.trim(range, newTracePath, new NullProgressMonitor());

            /* Initialize the new trace */
            fNewTrace = new CtfTmfTrace();
            fNewTrace.initTrace(null, newTracePath.toString(), CtfTmfEvent.class);
            openTrace(fNewTrace);

        } catch (CoreException e) {
            /*
             * CoreException are more or less useless, all the interesting stuff is in their
             * "status" objects.
             */
            String msg;
            IStatus status = e.getStatus();
            IStatus[] children = status.getChildren();
            if (children == null) {
                msg = status.getMessage();
            } else {
                msg = Arrays.stream(children)
                        .map(IStatus::getMessage)
                        .collect(Collectors.joining("\n"));
            }
            fail(msg);
        }
    }

    /**
     * Test teardown
     */
    @After
    public void tearDown() {
        if (fOriginalTrace != null) {
            fOriginalTrace.dispose();
        }
        CtfTmfTestTraceUtils.dispose(fTestTrace);

        if (fNewTrace != null) {
            fNewTrace.dispose();
        }

        if (fNewTracePath != null) {
            FileUtils.deleteQuietly(fNewTracePath.toFile());
        }
    }

    /** Simulate a trace being opened */
    private static void openTrace(CtfTmfTrace trace) {
        trace.indexTrace(true);
        TmfSignalManager.dispatchSignal(new TmfTraceOpenedSignal(CtfTmfTraceTrimmingTest.class, trace, null));
    }

    /**
     * Get the timestamp at which we should start cutting the trace. It should be
     * roughly 1/4 into the trace.
     */
    private static ITmfTimestamp getTraceCutStart(CtfTmfTrace trace) {
        long start = trace.getStartTime().toNanos();
        long end = trace.getEndTime().toNanos();

        return TmfTimestamp.fromNanos(((end - start) / 4) + start);
    }

    /**
     * Get the timestamp at which we should end the trace cutting. It should be
     * roughly at half the trace.
     */
    private static ITmfTimestamp getTraceCutEnd(CtfTmfTrace trace) {
        long start = trace.getStartTime().toNanos();
        long end = trace.getEndTime().toNanos();

        return TmfTimestamp.fromNanos(((end - start) / 2) + start);
    }

    // ------------------------------------------------------------------------
    // Test methods and helpers
    // ------------------------------------------------------------------------

    /**
     * Test that all expected events are present in the new trace.
     */
    @Test
    public void testTrimEvents() {
        CtfTmfTrace initialTrace = fOriginalTrace;
        CtfTmfTrace trimmedTrace = fNewTrace;
        Path newTracePath = fNewTracePath;
        assertNotNull(initialTrace);
        assertNotNull(trimmedTrace);
        assertNotNull(newTracePath);

        /*
         * Verify the bounds of the new trace are fine. The actual trace can be smaller
         * than what was requested if there are no events exactly at the bounds, but
         * should not contain events outside of the requested range.
         */
        final long newTraceStartTime = trimmedTrace.getStartTime().toNanos();
        final long newTraceEndTime = trimmedTrace.getEndTime().toNanos();

        assertTrue("Cut trace start time " + newTraceStartTime
                + " is earlier than the requested " + fRequestedTraceCutStart,
                newTraceStartTime >= fOriginalTrace.getStartTime().toNanos());

        assertTrue("Cut trace end time " + newTraceEndTime
                + " is later than the requested " + fRequestedTraceCutEnd,
                newTraceEndTime <= fOriginalTrace.getEndTime().toNanos());

        /*
         * Verify that each trace event from the original trace in the given time range
         * is present in the new one.
         */
        ITmfTimestamp startTime = fRequestedTraceCutStart;
        ITmfContext initialContext = initialTrace.seekEvent(startTime);
        CtfTmfEvent initialEvent = initialTrace.getNext(initialContext);
        ITmfContext trimmedContext = trimmedTrace.seekEvent(startTime);
        CtfTmfEvent trimmedEvent = trimmedTrace.getNext(trimmedContext);

        int count = 0;
        while (initialEvent.getTimestamp().compareTo(fRequestedTraceCutEnd) <= 0) {
            assertNotNull(initialEvent);
            assertNotNull("Expected event not present in trimmed trace: " + eventToString(initialEvent), trimmedEvent);

            if (!eventsEquals(initialEvent, trimmedEvent)) {
                /*
                 * Skip the test for different events of the exact same timestamp. The library
                 * does not guarantee in which order events of the same timestamp are read.
                 */
                String comparator = eventToString(initialEvent) + "\n" + eventToString(trimmedEvent);
                assertEquals(fRequestedTraceCutEnd + "\n" + count + "\n" + comparator, initialEvent.getTimestamp(), trimmedEvent.getTimestamp());
                /*
                 * Display warnings
                 */
                System.err.println("The following events have the exact same timestamp, and may be read in any order:");
                System.err.println(comparator);
            }

            initialEvent = initialTrace.getNext(initialContext);
            trimmedEvent = trimmedTrace.getNext(trimmedContext);
            count++;
        }

        assertTrue("Trimmed trace is too small", count <= trimmedTrace.getNbEvents());
    }

    /**
     * {@link TmfEvent#equals} checks the container trace, among other things. Here
     * we want to compare events from different traces, so we have to implement our
     * own equals().
     */
    private static boolean eventsEquals(CtfTmfEvent event1, CtfTmfEvent event2) {
        return Objects.equals(event1.getTimestamp(), event2.getTimestamp())
                && Objects.equals(event1.getType(), event2.getType())
                && Objects.equals(event1.getContent(), event2.getContent())
                && Objects.equals(event1.getCPU(), event2.getCPU())
                && Objects.equals(event1.getChannel(), event2.getChannel());
    }

    private static String eventToString(CtfTmfEvent event) {
        return new ToStringBuilder(event)
                .append("Timestamp", event.getTimestamp())
                .append("Type", event.getType())
                .append("Content", event.getContent())
                .append("CPU", event.getCPU())
                .append("Channel", event.getChannel())
                .toString();
    }
}
